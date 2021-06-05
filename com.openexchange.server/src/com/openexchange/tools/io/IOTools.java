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

package com.openexchange.tools.io;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;

public class IOTools {

    private IOTools() {
        super();
    }

    public static final void reallyBloodySkip(final InputStream is, long bytes) throws IOException {
        if (bytes <= 0) {
            return;
        }
        long bytesToSkip = bytes;
        byte buffer[] = new byte[(int) Math.min(0xFFFF, bytesToSkip)];
        while (0 < bytesToSkip) {
            int read = is.read(buffer, 0, (int) Math.min(buffer.length, bytesToSkip));
            if (read < 0) {
                break;
            }
            bytesToSkip -= read;
        }
    }

    /**
     * Copies content from <code>in</code> to <code>out</code>.
     * <p>
     * &nbsp;&nbsp;&nbsp;<strong>Note: Passed streams do <i>not</i> get {@link Closeable#close() closed}!</strong>
     *
     * @param in The stream source
     * @param out The stream sink
     * @throws IOException If an I/O error occurs
     */
    public static final void copy(final InputStream in, final OutputStream out) throws IOException {
        final InputStream inputStream = Streams.bufferedInputStreamFor(in);
        final OutputStream outputStream = Streams.bufferedOutputStreamFor(out);

        for(int i; (i = inputStream.read()) >= 0;) {
            outputStream.write(i);
        }
        outputStream.flush();
    }

    public static final byte[] getBytes(final InputStream stream) throws IOException {
        final InputStream in = Streams.bufferedInputStreamFor(stream);
        try {
            final ByteArrayOutputStream out = Streams.newByteArrayOutputStream();
            for(int i; (i = in.read()) >= 0;) {
                out.write(i);
            }
            return out.toByteArray();
        } finally {
            in.close();
        }

    }

    public static final String getFileContents(final File file) throws FileNotFoundException {
        final StringBuilder sb = new StringBuilder();
        Scanner scanner = null;
        try {
            scanner = new Scanner(file, Charsets.UTF_8_NAME);
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
                sb.append('\n');
            }
        } finally {
            if (null != scanner) {
                scanner.close();
            }
        }
        return sb.toString();
    }
}
