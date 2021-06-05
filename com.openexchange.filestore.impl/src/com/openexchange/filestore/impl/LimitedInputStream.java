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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link LimitedInputStream}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class LimitedInputStream extends FilterInputStream {

    /**
     * The maximum size of an item, in bytes.
     */
    private final long sizeMax;

    /**
     * The current number of bytes.
     */
    private long count;

    /**
     * Creates a new instance.
     *
     * @param inputStream The input stream, which shall be limited.
     * @param pSizeMax The limit; no more than this number of bytes shall be returned by the source stream.
     */
    public LimitedInputStream(InputStream inputStream, long pSizeMax) {
        super(inputStream);
        sizeMax = pSizeMax;
    }

    /**
     * Called to indicate, that the input streams limit has been exceeded.
     *
     * @param numBytes The total number of bytes available from the underlying stream
     * @param maxNumBytes The input stream's limit, in bytes.
     * @throws IOException If the called method is expected to raise an I/O error
     */
    protected void raiseError(long numBytes, long maxNumBytes) throws IOException {
        throw new StorageFullIOException(numBytes, maxNumBytes);
    }

    /**
     * Called to check, whether the input stream's limit is reached.
     *
     * @throws IOException If the given limit is exceeded
     */
    private void checkLimit() throws IOException {
        if (count > sizeMax) {
            // Safely count remaining bytes to determine total number of bytes provided by underlying stream
            raiseError(count + countRemainingBytesSafely(), sizeMax);
        }
    }

    private long countRemainingBytesSafely() {
        long count = 0;
        int buflen = 0xFFFF;
        byte[] buf = new byte[buflen];
        int read;
        do {
            try {
                read = super.read(buf, 0, buflen);
            } catch (IOException e) {
                // Failed reading from stream
                read = 0;
            }
            if (read <= 0) {
                // No further bytes available
                return count;
            }
            count += read;
        } while (true);
    }

    @Override
    public int read() throws IOException {
        int res = super.read();
        if (res != -1) {
            count++;
            checkLimit();
        }
        return res;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int res = super.read(b, off, len);
        if (res > 0) {
            count += res;
            checkLimit();
        }
        return res;
    }

}
