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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The <code>SizeAwareInputStream</code> is a utility class that can be used if file size has to be checked while someone reads the stream.
 * <p>
 * The method {@link #size(long)} is called whenever someone reads from the stream. This method is provided with the length of the stream up
 * until the time the method is called.
 * <p>
 * The <code>SizeAwareInputStream</code> can be used to find out about the total length of a stream or to monitor certain upload quotas
 * (if a quota is exceeded the size method may simply throw an <code>IOException</code>).
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.org">Francisco Laguna</a>
 */
public abstract class SizeAwareInputStream extends FilterInputStream {

    private long read = 0;

    /**
     * Initializes a new {@link SizeAwareInputStream}.
     *
     * @param delegate The input stream to read from
     */
    protected SizeAwareInputStream(final InputStream delegate) {
        super(delegate);
    }

    @Override
    public int read() throws IOException {
        int r = in.read();
        if (r != -1) {
            read++;
            size(read);
        }
        return r;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int r = in.read(b, off, len);
        if (r > 0) {
            read += r;
            size(read);
        }
        return r;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int r = in.read(b);
        if (r > 0) {
            read += r;
            size(read);
        }
        return r;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public long skip(final long n) throws IOException {
        return in.skip(n);
    }

    /**
     * Invoked to signal that total number of bytes has changed to given value.
     *
     * @param size The total number of bytes read so far
     * @throws IOException If an I/O error is raised
     */
    protected abstract void size(long size) throws IOException;

}
