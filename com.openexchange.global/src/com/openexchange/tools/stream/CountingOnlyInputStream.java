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

package com.openexchange.tools.stream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link CountingOnlyInputStream} - An {@link InputStream} that counts (and optionally limits) the number of bytes read.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CountingOnlyInputStream extends FilterInputStream {

    // ----------------------------------------------------------------------------------------------------------------------

    /** The current byte count */
    protected final AtomicLong count;
    private volatile long mark;

    /**
     * Wraps another input stream, counting the number of bytes read.
     *
     * @param in The input stream to be wrapped
     */
    public CountingOnlyInputStream(InputStream in) {
        super(in);
        count = new AtomicLong(0L);
        mark = -1L;
    }

    /**
     * Adds given number of consumed bytes to the counter.
     *
     * @param consumed The number of consumed bytes
     * @return The new number of counted bytes so far
     * @throws IOException If an I/O error should be advertised
     */
    @SuppressWarnings("unused")
    protected long add(int consumed) throws IOException {
        return count.addAndGet(consumed);
    }

    /**
     * Sets the byte count back to <code>0</code> (zero).
     *
     * @return The previous count prior to resetting
     */
    public long resetByteCount() {
        final long tmp = count.get();
        count.set(0L);
        return tmp;
    }

    /**
     * Gets the number of bytes read so far.
     *
     * @return The number of bytes read so far
     */
    public long getCount() {
        return count.get();
    }

    @Override
    public int read() throws IOException {
        int result = in.read();
        if (result < 0) {
            // The end of the stream is reached
            return result;
        }

        // Consumed 1 more byte from stream
        add(1);
        return result;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int result = in.read(b, 0, b.length);
        if (result < 0) {
            // There is no more data because the end of the stream has been reached.
            return result;
        }

        // Consumed more bytes from stream
        add(result);
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = in.read(b, off, len);
        if (result < 0) {
            // There is no more data because the end of the stream has been reached.
            return result;
        }

        // Consumed more bytes from stream
        add(result);
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        long result = in.skip(n);
        count.addAndGet(result);
        return result;
    }

    @Override
    public void mark(int readlimit) {
        /*
         * It's okay to mark even if mark isn't supported, as reset won't work
         */
        in.mark(readlimit);
        mark = count.get();
    }

    @Override
    public void reset() throws IOException {
        if (!in.markSupported()) {
            throw new IOException("Mark not supported");
        }

        long mark = this.mark;
        if (mark == -1) {
            throw new IOException("Mark not set");
        }

        in.reset();
        count.set(mark);
    }
}
