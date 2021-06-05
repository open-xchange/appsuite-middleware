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

package com.openexchange.java;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * {@link ByteArrayBufferedInputStream} - A {@link BufferedInputStream} backed by a {@link ByteArrayInputStream} instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ByteArrayBufferedInputStream extends BufferedInputStream {

    private final ByteArrayInputStream bytes;

    /**
     * Creates a <code>ByteArrayBufferedInputStream</code> from given {@link ByteArrayInputStream} instance.
     *
     * @param bytes The underlying {@link ByteArrayInputStream} instance
     */
    public ByteArrayBufferedInputStream(ByteArrayInputStream bytes) {
        super(bytes, 1);
        this.bytes = bytes;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return bytes.read(b);
    }

    @Override
    public int read() {
        return bytes.read();
    }

    @Override
    public int read(byte[] b, int off, int len) {
        return bytes.read(b, off, len);
    }

    @Override
    public long skip(long n) {
        return bytes.skip(n);
    }

    @Override
    public int available() {
        return bytes.available();
    }

    @Override
    public boolean markSupported() {
        return bytes.markSupported();
    }

    @Override
    public void mark(int readAheadLimit) {
        bytes.mark(readAheadLimit);
    }

    @Override
    public void reset() {
        bytes.reset();
    }

    @Override
    public void close() throws IOException {
        bytes.close();
    }

    @Override
    public String toString() {
        return bytes.toString();
    }

}
