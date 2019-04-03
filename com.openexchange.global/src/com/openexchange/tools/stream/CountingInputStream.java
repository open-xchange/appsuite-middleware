/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
