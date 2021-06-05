/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.filestore.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;

/**
 * A {@link HashingFileStorage} generates UUIDs for every file that is stored in it.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class HashingFileStorage extends DefaultFileStorage {

    /**
     * Initializes a new {@link HashingFileStorage}.
     *
     * @param uri The URI that fully qualifies this file storage
     * @param storage The parent directory; e.g. <code>"file:/Mount/disk/1_ctx_store/hashed"</code>
     */
    public HashingFileStorage(URI uri, File storage) {
        super(uri, storage);
    }

    @Override
    public SortedSet<String> getFileList() throws OXException {
        final SortedSet<String> files = new TreeSet<String>();
        final int beginIndex = storage.getAbsolutePath().length() + 1;
        visit(new Visitor() {

            @Override
            public void visit(File f) {
                if (f.isFile()) {
                    files.add(f.getAbsolutePath().substring(beginIndex));
                }
            }

        });
        return files;
    }

    @Override
    public void recreateStateFile() throws OXException {
        // Nope, no state file used.
    }

    @Override
    public void remove() throws OXException {
        visit(new Visitor() {

            @Override
            public void visit(final File f) {
                f.delete();
            }

        });
    }

    @Override
    public String saveNewFile(final InputStream file) throws OXException {
        File filePath = null;
        boolean success = false;
        OutputStream out = null;
        try {
            final String[] filestorePath = generateName();
            final File path = new File(storage, filestorePath[0]);
            if (!path.exists() && !path.mkdirs() && !path.exists()) {
                throw FileStorageCodes.CREATE_DIR_FAILED.create(path.toString());
            }

            filePath = new File(path, filestorePath[1]);
            try {
                out = new FileOutputStream(filePath);
            } catch (FileNotFoundException e) {
                throw FileStorageCodes.FILE_NOT_FOUND.create(e, filePath.toString());
            }

            final int buflen = 65536;
            final byte[] buf = new byte[buflen];
            for (int read; (read = file.read(buf, 0, buflen)) > 0;) {
                out.write(buf, 0, read);
            }
            out.flush();

            String identifier = new StringBuilder(filestorePath[0]).append('/').append(filestorePath[1]).toString();
            success = true;
            return identifier;
        } catch (IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        } finally {
            Streams.close(file, out);
            if (false == success && null != filePath && filePath.exists()) {
                try {
                    filePath.delete();
                } catch (Exception e) {
                    LoggerFactory.getLogger(HashingFileStorage.class).warn("Error cleaning up after failed save. Consider running the consistency tool.", e);
                }
            }
        }
    }

    public String[] generateName() {
        // The random UUID for the file
        final UUID uuid = UUID.randomUUID();

        // Build the directory prefix
        StringBuilder b = new StringBuilder(10);
        int i = 0;
        String prefix = Integer.toHexString(uuid.hashCode());
        int length = prefix.length();
        for (; i < length && i < 6; i++) {
            b.append(prefix.charAt(i));
            if (((i & 1) == 1) && (i > 0)) {
                b.append('/');
            }
        }
        for (; i < 6; i++) {
            b.append('0');
            if (((i & 1) == 1) && (i > 0)) {
                b.append('/');
            }
        }
        b.setLength(b.length()-1);

        return new String[] { b.toString(), UUIDs.getUnformattedString(uuid) };
    }

    @Override
    public boolean stateFileIsCorrect() throws OXException {
        return true; // We're not using a state file
    }

    // Visits all nodes - depth first
    protected void visit(final Visitor visitor) {
        recurse(storage, visitor);
    }

    protected void recurse(File f, Visitor visitor) {
        if (f.isFile()) {
            visitor.visit(f);
            return;
        }

        File[] files = f.listFiles();
        if (files != null) {
            for (final File file : files) {
                recurse(file, visitor);
            }
            visitor.visit(f);
        }
    }

    private static interface Visitor {
        public void visit(File f);
    }

}
