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

package com.openexchange.groupware.datahandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.java.Streams;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link InputStreamCopy}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> - extracted class
 * @since v7.10.0
 */
final class InputStreamCopy implements Closeable {

    private static final int DEFAULT_BUF_SIZE = 0x2000;

    private byte[] bytes;

    private File file;

    private final long size;

    private final String prefix;

    public InputStreamCopy(final InputStream orig, final String prefix, final boolean createFile) throws IOException {
        super();
        this.prefix = prefix;
        if (createFile) {
            size = copy2File(orig);
        } else {
            size = copy2ByteArr(orig);
        }
    }

    public long getSize() {
        return size;
    }

    public InputStream getInputStream() throws IOException {
        return bytes == null ? (file == null ? null : (new BufferedInputStream(new FileInputStream(file), DEFAULT_BUF_SIZE))) : (new UnsynchronizedByteArrayInputStream(bytes));
    }

    @Override
    public void close() {
        if (file != null) {
            if (file.exists()) {
                file.delete();
            }
            file = null;
        }
        if (bytes != null) {
            bytes = null;
        }
    }

    private int copy2ByteArr(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(DEFAULT_BUF_SIZE << 1);
        final byte[] bbuf = new byte[DEFAULT_BUF_SIZE];
        int len;
        while ((len = in.read(bbuf)) > 0) {
            out.write(bbuf, 0, len);
        }
        out.flush();
        this.bytes = out.toByteArray();
        out.close();
        return bytes.length;
    }

    private long copy2File(final InputStream in) throws IOException {
        long totalBytes = 0;
        {
            final File tmpFile = File.createTempFile(prefix, null, ServerConfig.getTmpDir());
            tmpFile.deleteOnExit();
            OutputStream out = null;
            try {
                out = new BufferedOutputStream(new FileOutputStream(tmpFile), DEFAULT_BUF_SIZE);
                final byte[] bbuf = new byte[DEFAULT_BUF_SIZE];
                int len;
                while ((len = in.read(bbuf)) > 0) {
                    out.write(bbuf, 0, len);
                    totalBytes += len;
                }
                out.flush();
            } finally {
                Streams.close(out);
            }
            file = tmpFile;
        }
        return totalBytes;
    }
}
