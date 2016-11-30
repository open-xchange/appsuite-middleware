/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.filestore.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
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

    public HashingFileStorage(File storage) {
        super(storage);
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
        OutputStream out = null;
        try {
            final String[] filestorePath = generateName();
            final File path = new File(storage, filestorePath[0]);
            if (!path.exists() && !path.mkdirs() && !path.exists()) {
                throw FileStorageCodes.CREATE_DIR_FAILED.create(path.toString());
            }

            {
                final File filePath = new File(path, filestorePath[1]);
                try {
                    out = new FileOutputStream(filePath);
                } catch (final FileNotFoundException e) {
                    throw FileStorageCodes.FILE_NOT_FOUND.create(e, filePath.toString());
                }
            }

            final int buflen = 65536;
            final byte[] buf = new byte[buflen];
            for (int read; (read = file.read(buf, 0, buflen)) > 0;) {
                out.write(buf, 0, read);
            }
            out.flush();

            return new StringBuilder(filestorePath[0]).append('/').append(filestorePath[1]).toString();
        } catch (final IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        } finally {
            Streams.close(file, out);
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
