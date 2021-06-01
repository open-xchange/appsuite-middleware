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

package com.openexchange.message.timeline.util;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * {@link LimitReader} - A {@link Reader reader} that limits the number of characters which can be read.
 * <p>
 * If limit is exceeded a <code>LimitExceededIOException</code> is thrown.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public final class LimitReader extends FilterReader {

    private long left;
    private long mark = -1;

    /**
     * Wraps another reader, limiting the number of characters which can be read.
     *
     * @param in the reader to be wrapped
     * @param limit the maximum number of characters to be read
     */
    public LimitReader(final Reader in, final long limit) {
        super(in);
        if (null == in) {
            throw new IllegalArgumentException("Reader must not be null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("Limit must be non-negative");
        }
        left = limit;
    }

    @Override
    public void mark(final int readlimit) throws IOException {
        in.mark(readlimit);
        mark = left;
    }

    @Override
    public int read() throws IOException {
        if (left <= 0) {
            throw new LimitExceededIOException("Max. character count exceeded.");
        }

        final int result = in.read();
        if (result != -1) {
            --left;
        }
        return result;
    }

    @Override
    public int read(final char[] b, final int off, final int len) throws IOException {
        if (left <= 0) {
            throw new LimitExceededIOException("Max. character count exceeded.");
        }

        final int result = in.read(b, off, (int) Math.min(len, left));
        if (result != -1) {
            left -= result;
        }
        return result;
    }

    @Override
    public void reset() throws IOException {
        if (!in.markSupported()) {
            throw new IOException("Mark not supported");
        }
        if (mark == -1) {
            throw new IOException("Mark not set");
        }
        in.reset();
        left = mark;
    }

    @Override
    public long skip(final long n) throws IOException {
        final long skipped = in.skip(Math.min(n, left));
        left -= skipped;
        return skipped;
    }
}
