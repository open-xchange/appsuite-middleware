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

import java.io.IOException;
import it.geosolutions.imageio.stream.eraf.EnhancedRandomAccessFile;

/**
 * {@link RandomAccessFileInputStream}
 *
 * Provides an input stream for part of a file.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RandomAccessFileInputStream extends java.io.InputStream {

    private final EnhancedRandomAccessFile file;
    private final long endPosition;
    private long markedPosition;

    /**
     * Initializes a new {@link RandomAccessFileInputStream}.
     *
     * @param eraf The random access file to create the random access stream for
     * @param offset The offset to begin to read data from, or <code>0</code> to start at the beginning
     * @param length The number of bytes to read starting from the offset, or <code>-1</code> to read until the end
     * @throws IOException if the offset is less than 0 or if an I/O error occurs.
     */
    public RandomAccessFileInputStream(EnhancedRandomAccessFile eraf, long offset, long length) throws IOException {
        super();
        this.file = eraf;
        this.endPosition = -1 == length ? file.length() : Math.min(file.length(), offset + length);
        this.file.seek(offset);
    }

    @Override
    public synchronized int read() throws IOException {
        if (file.getFilePointer() >= endPosition) {
            return -1;
        }
        return file.read();
    }

    @Override
    public synchronized void close() throws IOException {
        this.file.close();
        super.close();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void mark(int readlimit) {
        try {
            this.markedPosition = file.getFilePointer();
        } catch (IOException e) {
            // indicate failure in following reset() call
            this.markedPosition = Long.MIN_VALUE;
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        if (Long.MIN_VALUE == this.markedPosition) {
            throw new IOException("No position marked");
        }
        file.seek(markedPosition);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        int available = this.available();
        if (0 >= available) {
            return -1;
        }
        return file.read(b, off, Math.min(available, len));
    }

    @Override
    public synchronized int available() throws IOException {
        return (int)(endPosition - file.getFilePointer());
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        if (n > Integer.MAX_VALUE) {
            throw new IOException("can skip at maximum " + Integer.MAX_VALUE + " bytes");
        }
        return file.skipBytes((int)(n & 0xFFFFFFFF));
    }

}
