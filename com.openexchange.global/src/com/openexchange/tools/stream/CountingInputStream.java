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

import java.io.IOException;
import java.io.InputStream;

/**
 * {@link CountingInputStream} - An {@link InputStream} that counts (and optionally limits) the number of bytes read.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CountingInputStream extends CountingOnlyInputStream {

    public static interface IOExceptionCreator {

        /**
         * Create the appropriate {@code IOException} if specified max. number of bytes has been exceeded.
         *
         * @param total The optional total size or <code>-1</code> if unknown
         * @param max The max. number of bytes that were exceeded
         * @return The appropriate {@code IOException} instance
         */
        IOException createIOException(long total, long max);
    }

    private static final IOExceptionCreator DEFAULT_EXCEPTION_CREATOR = new IOExceptionCreator() {

        @Override
        public IOException createIOException(long total, long max) {
            return new IOException(new StringBuilder(32).append("Max. byte count of ").append(max).append(" exceeded.").toString());
        }
    };

    // ----------------------------------------------------------------------------------------------------------------------

    private volatile long max;
    private final IOExceptionCreator exceptionCreator;

    /**
     * Wraps another input stream, counting the number of bytes read.
     *
     * @param in The input stream to be wrapped
     * @param max The maximum number of bytes allowed being read or <code>-1</code> for no limitation, just counting
     */
    public CountingInputStream(InputStream in, long max) {
        this(in, max, null);
    }

    /**
     * Wraps another input stream, counting the number of bytes read.
     *
     * @param in The input stream to be wrapped
     * @param max The maximum number of bytes allowed being read or <code>-1</code> for no limitation, just counting
     * @param exceptionCreator The exception creator or <code>null</code>
     */
    public CountingInputStream(InputStream in, long max, IOExceptionCreator exceptionCreator) {
        super(in);
        this.max = max;
        this.exceptionCreator = null == exceptionCreator ? DEFAULT_EXCEPTION_CREATOR : exceptionCreator;
    }

    @Override
    protected long add(int consumed) throws IOException {
        long newCount = super.add(consumed);

        long max = this.max;
        if (max > 0) {
            if (newCount > max) {
                // Pass 0 (zero) as total size of the stream is unknown or requires to count the remaining bytes from stream respectively
                throw exceptionCreator.createIOException(0L, max);
            }
        }
        return newCount;
    }

    @Override
    public long skip(long n) throws IOException {
        long result = super.skip(n);

        // Adjust "max" by number of skipped bytes
        long max = this.max;
        if (max > 0) {
            this.max = max + result;
        }

        return result;
    }

}
