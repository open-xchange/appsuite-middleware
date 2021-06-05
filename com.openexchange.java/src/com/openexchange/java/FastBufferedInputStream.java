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
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link FastBufferedInputStream} - Extends {@link BufferedInputStream} and provides an improved implementation for {@link #available()}:
 * <p>
 * This method returns positive value if something is available, otherwise it will return zero.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FastBufferedInputStream extends BufferedInputStream {

    /**
     * Creates a <code>FastBufferedInputStream</code> and saves its argument, the input stream <code>in</code>, for later use.
     * <p>
     * An internal buffer array of size <code>65536</code> is created and stored in <code>buf</code>.
     *
     * @param in the underlying input stream.
     */
    public FastBufferedInputStream(final InputStream in) {
        super(in, 65536);
    }

    /**
     * Creates a <code>FastBufferedInputStream</code> with the specified buffer size, and saves its argument, the input stream <code>in</code>,
     * for later use. An internal buffer array of length <code>size</code> is created and stored in <code>buf</code>.
     *
     * @param in the underlying input stream.
     * @param size the buffer size.
     * @exception IllegalArgumentException If <code>size &lt;= 0</code>.
     */
    public FastBufferedInputStream(final InputStream in, final int size) {
        super(in, size);
    }

    @Override
    public int available() throws IOException {
        // This method returns positive value if something is available, otherwise it will return zero.
        if (in == null) {
            throw new IOException("Stream closed");
        }
        final int n = count - pos;
        return n > 0 ? n : in.available();
    }

}
