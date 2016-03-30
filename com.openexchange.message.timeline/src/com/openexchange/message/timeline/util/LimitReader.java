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
