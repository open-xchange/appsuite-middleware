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

package com.openexchange.tools.servlet;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

/**
 * {@link CountingServletInputStream} - The delegating {@link ServletInputStream}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CountingServletInputStream extends ServletInputStream {

    private final ServletInputStream in;
    private final AtomicLong count;
    private volatile long mark;
    private volatile long max;

    /**
     * Initializes a new {@link CountingServletInputStream}.
     *
     * @param in The input stream to delegate to
     * @param max The maximum number of bytes allowed being read or <code>-1</code> for no limitation, just counting
     */
    public CountingServletInputStream(ServletInputStream in, long max) {
        super();
        this.in = in;
        this.max = max;
        count = new AtomicLong(0L);
        mark = -1L;
    }

    private void check(int consumed) throws IOException {
        long max = this.max;
        if (max > 0) {
            if (count.addAndGet(consumed) > max) {
                // Pass 0 (zero) as total size of the stream is unknown or requires to count the remaining bytes from stream respectively
                throw new IOException(new StringBuilder(32).append("Max. byte count of ").append(max).append(" exceeded.").toString());
            }
        } else {
            count.addAndGet(consumed);
        }
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
        check(1);
        return result;
    }

    @Override
    public int read(final byte[] b) throws IOException {
        int result = super.read(b);
        if (result < 0) {
            // There is no more data because the end of the stream has been reached.
            return result;
        }

        // Consumed more bytes from stream
        check(result);
        return result;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        int result = in.read(b, off, len);
        if (result < 0) {
            // There is no more data because the end of the stream has been reached.
            return result;
        }

        // Consumed more bytes from stream
        check(result);
        return result;
    }

    @Override
    public long skip(final long n) throws IOException {
        final long result = in.skip(n);
        final long max = this.max;
        if (max > 0) {
            this.max = max + n;
        }
        count.addAndGet(result);
        return result;
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public void mark(final int readlimit) {
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

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    @Override
    public boolean isFinished() {
        return in.isFinished();
    }

    @Override
    public boolean isReady() {
        return in.isReady();
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        in.setReadListener(readListener);
    }

}
