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
 * The SizeAwareInputStream is a utility class that can be used if file size has to be checked while someone reads the stream. The method
 * #size(long) is called whenever someone reads from the stream. This method is provided with the length of the stream up until the time the
 * method is called. The SizeAwareInputStream can be used to find out about the total length of a stream or to monitor certain upload quotas
 * (if a quota is exceeded the size method may simply throw an IOException).
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.org">Francisco Laguna</a>
 */
public class SizeAwareInputStream extends FilterInputStream {

    private long read = 0;

    public SizeAwareInputStream(final InputStream delegate) {
        super(delegate);
    }

    @Override
    public int read() throws IOException {
        final int r = in.read();
        if (r != -1) {
            read++;
            size(read);
        }
        return r;
    }

    @Override
    public int read(final byte[] arg0, final int arg1, final int arg2) throws IOException {
        final int r = in.read(arg0, arg1, arg2);
        if (r > 0) {
            read += r;
            size(read);
        }
        return r;
    }

    @Override
    public int read(final byte[] arg0) throws IOException {
        final int r = in.read(arg0);
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
    public long skip(final long arg0) throws IOException {
        return in.skip(arg0);
    }

    public void size(final long size) throws IOException {
        // Override me
    }

}
