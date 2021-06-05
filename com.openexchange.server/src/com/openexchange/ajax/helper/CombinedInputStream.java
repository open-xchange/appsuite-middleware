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

package com.openexchange.ajax.helper;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@link CombinedInputStream} - An {@link InputStream input stream} implementation combining an already read/consumed byte sequence with
 * remaining {@link InputStream input stream} delegatee.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CombinedInputStream extends InputStream {

    private final byte[] consumed;
    private final InputStream remaining;
    private int count;

    /**
     * Initializes a new {@link CombinedInputStream}.
     *
     * @param consumed The bytes already consumed from <code>remaining</code> input stream
     * @param remaining The remaining stream
     * @throws IllegalArgumentException If passed byte array or {@link InputStream} instance is <code>null</code>
     */
    public CombinedInputStream(final byte[] consumed, final InputStream remaining) {
        super();
        if (null == consumed) {
            throw new IllegalArgumentException("Byte array is null.");
        }
        if (null == remaining) {
            throw new IllegalArgumentException("Input stream array is null.");
        }
        this.consumed = new byte[consumed.length];
        System.arraycopy(consumed, 0, this.consumed, 0, consumed.length);
        this.remaining = remaining;
        count = 0;
    }

    @Override
    public int read() throws IOException {
        if (count < consumed.length) {
            return consumed[count++];
        }
        return remaining.read();
    }

    @Override
    public int read(final byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(final byte b[], final int off, final int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        if (count < consumed.length) {
            final int buffered = consumed.length - count;
            if (buffered >= len) {
                // Buffered content offers up to len bytes.
                final int retval = (buffered <= len) ? buffered : len; // Math.min(int a, int b)
                System.arraycopy(consumed, count, b, off, retval);
                count += retval;
                return retval;
            }
            // Buffered content does NOT offer up to len bytes.
            System.arraycopy(consumed, count, b, off, buffered);
            count += buffered;
            return buffered + remaining.read(b, off + buffered, len - buffered);
        }
        return remaining.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        remaining.close();
    }

}
