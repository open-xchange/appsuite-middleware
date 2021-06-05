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

package com.openexchange.drive.impl.internal;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.openexchange.tools.io.IOTools;

/**
 * {@link PartialInputStream}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PartialInputStream extends FilterInputStream {

    private final long length;
    private long bytesRead;

    /**
     * Initializes a new {@link PartialInputStream}.
     *
     * @param in the underlying input stream
     * @param offset The offset where to start reading
     * @param length The number of bytes to read from the offset, or <code>-1</code> to read until the end
     * @throws IOException
     */
    public PartialInputStream(InputStream in, long offset, long length) throws IOException {
        super(in);
        this.length = length;
        IOTools.reallyBloodySkip(this, offset);
        this.bytesRead = 0;
    }

    /**
     * Initializes a new {@link PartialInputStream}.
     *
     * @param in the underlying input stream
     * @param offset The offset where to start reading
     * @throws IOException
     */
    public PartialInputStream(InputStream in, long offset) throws IOException {
        this(in, offset, -1);
    }

    @Override
    public int read() throws IOException {
        if (-1 != length && bytesRead >= length) {
            return -1;
        }
        int read = super.read();
        if (-1 != read) {
            bytesRead++;
        }
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (-1 != length && bytesRead >= length) {
            return -1;
        }
        int toRead = -1 == length ? len : Math.min((int)(length - bytesRead), len);
        int read = super.read(b, off, toRead);
        if (-1 != read) {
            this.bytesRead += read;
        }
        return read;
    }

}
