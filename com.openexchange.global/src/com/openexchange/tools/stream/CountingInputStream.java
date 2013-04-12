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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link CountingInputStream} - An {@link InputStream} that counts the number of bytes read.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CountingInputStream extends FilterInputStream {

    private final AtomicLong count;

    private volatile long mark;

    private volatile long max;

    /**
     * Wraps another input stream, counting the number of bytes read.
     *
     * @param in the input stream to be wrapped
     */
    public CountingInputStream(final InputStream in, final long max) {
        super(in);
        this.max = max;
        count = new AtomicLong(0L);
        mark = -1L;
    }

    /**
     * Set the byte count back to 0L.
     *
     * @return The count previous to resetting
     */
    public long resetByteCount() {
        final long tmp = count.get();
        count.set(0L);
        return tmp;
    }

    /**
     * Returns the number of bytes read.
     */
    public long getCount() {
        return count.get();
    }

    @Override
    public int read() throws IOException {
        final int result = in.read();
        final long max = this.max;
        if (count.addAndGet((result >= 0L) ? 1 : 0L) > max) {
            throw new IOException("Max. byte count of " + max + " exceeded.");
        }
        return result;
    }

    @Override
    public int read(final byte[] b) throws IOException {
        final int result = super.read(b);
        final long max = this.max;
        if (count.addAndGet((result >= 0L) ? result : 0L) > max) {
            throw new IOException("Max. byte count of " + max + " exceeded.");
        }
        return result;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        final int result = in.read(b, off, len);
        final long max = this.max;
        if (count.addAndGet((result >= 0L) ? result : 0L) > max) {
            throw new IOException("Max. byte count of " + max + " exceeded.");
        }
        return result;
    }

    @Override
    public long skip(final long n) throws IOException {
        final long result = in.skip(n);
        max += n;
        count.addAndGet(result);
        return result;
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
        final long mark = this.mark;
        if (mark == -1) {
            throw new IOException("Mark not set");
        }

        in.reset();
        count.set(mark);
    }
}
