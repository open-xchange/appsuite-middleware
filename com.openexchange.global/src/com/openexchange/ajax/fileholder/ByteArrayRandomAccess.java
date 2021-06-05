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

package com.openexchange.ajax.fileholder;

import java.io.IOException;
import com.openexchange.ajax.fileholder.IFileHolder.RandomAccess;

/**
 * {@link ByteArrayRandomAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ByteArrayRandomAccess implements RandomAccess {

    private final byte[] bytes;
    private int pos;

    /**
     * Initializes a new {@link ByteArrayRandomAccess}.
     */
    public ByteArrayRandomAccess(byte[] bytes) {
        super();
        this.bytes = bytes;
        pos = 0;
    }

    @Override
    public long length() throws IOException {
        return bytes.length;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }

        int count = bytes.length;
        if (pos >= count) {
            return -1;
        }

        int length = len;
        int avail = count - pos;
        if (length > avail) {
            length = avail;
        }
        if (length <= 0) {
            return 0;
        }
        System.arraycopy(bytes, pos, b, off, length);
        pos += length;
        return length;
    }

    @Override
    public void seek(long pos) throws IOException {
        this.pos = (int) pos;
    }

    @Override
    public void close() throws IOException {
        // Nothing to do
    }

}
