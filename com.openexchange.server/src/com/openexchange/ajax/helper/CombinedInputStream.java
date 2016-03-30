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

package com.openexchange.ajax.helper;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@link CombinedInputStream} - An {@link InputStream input stream} implementation combining an already read/consumed byte sequence with
 * remaining {@link InputStream input stream} delegatee.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CombinedInputStream extends InputStream {

    private final byte[] consumed;
    private final InputStream remaining;
    private int count;

    /**
     * Initializes a new {@link CombinedInputStream}.
     *
     * @param consumed The bytes already consumed from <code>remaining</code> input stream
     * @param remaining The remaining stream
     * @throws IllegalArgumentException If passed byte array or {@link InputStream} instance is <code>null</code>
     */
    public CombinedInputStream(final byte[] consumed, final InputStream remaining) {
        super();
        if (null == consumed) {
            throw new IllegalArgumentException("Byte array is null.");
        }
        if (null == remaining) {
            throw new IllegalArgumentException("Input stream array is null.");
        }
        this.consumed = new byte[consumed.length];
        System.arraycopy(consumed, 0, this.consumed, 0, consumed.length);
        this.remaining = remaining;
        count = 0;
    }

    @Override
    public int read() throws IOException {
        if (count < consumed.length) {
            return consumed[count++];
        }
        return remaining.read();
    }

    @Override
    public int read(final byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(final byte b[], final int off, final int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        if (count < consumed.length) {
            final int buffered = consumed.length - count;
            if (buffered >= len) {
                // Buffered content offers up to len bytes.
                final int retval = (buffered <= len) ? buffered : len; // Math.min(int a, int b)
                System.arraycopy(consumed, count, b, off, retval);
                count += retval;
                return retval;
            }
            // Buffered content does NOT offer up to len bytes.
            System.arraycopy(consumed, count, b, off, buffered);
            count += buffered;
            return buffered + remaining.read(b, off + buffered, len - buffered);
        }
        return remaining.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        remaining.close();
    }

}
