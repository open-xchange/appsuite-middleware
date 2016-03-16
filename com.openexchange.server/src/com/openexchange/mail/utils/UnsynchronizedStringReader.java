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

package com.openexchange.mail.utils;

import java.io.IOException;
import java.io.Reader;

/**
 * {@link UnsynchronizedStringReader} - An unsynchronized string {@link Reader reader}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UnsynchronizedStringReader extends Reader {

    private final String str;

    private final int length;

    private int next = 0;

    private int mark = 0;

    /**
     * Create a new string reader.
     *
     * @param s String providing the character stream.
     */
    public UnsynchronizedStringReader(final String s) {
        this.str = s;
        this.length = s.length();
    }

    /**
     * Read a single character.
     *
     * @return The character read, or -1 if the end of the stream has been reached
     * @exception IOException If an I/O error occurs
     */
    @Override
    public int read() throws IOException {
        if (next >= length) {
            return -1;
        }
        return str.charAt(next++);
    }

    /**
     * Read characters into a portion of an array.
     *
     * @param cbuf Destination buffer
     * @param off Offset at which to start writing characters
     * @param len Maximum number of characters to read
     * @return The number of characters read, or -1 if the end of the stream has been reached
     * @exception IOException If an I/O error occurs
     */
    @Override
    public int read(final char cbuf[], final int off, final int len) throws IOException {
        if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        if (next >= length) {
            return -1;
        }
        final int n = Math.min(length - next, len);
        str.getChars(next, next + n, cbuf, off);
        next += n;
        return n;
    }

    /**
     * Skips the specified number of characters in the stream. Returns the number of characters that were skipped.
     * <p>
     * The <code>ns</code> parameter may be negative, even though the <code>skip</code> method of the {@link Reader} superclass throws an
     * exception in this case. Negative values of <code>ns</code> cause the stream to skip backwards. Negative return values indicate a skip
     * backwards. It is not possible to skip backwards past the beginning of the string.
     * <p>
     * If the entire string has been read or skipped, then this method has no effect and always returns 0.
     *
     * @exception IOException If an I/O error occurs
     */
    @Override
    public long skip(final long ns) throws IOException {
        if (next >= length) {
            return 0;
        }
        // Bound skip by beginning and end of the source
        long n = Math.min(length - next, ns);
        n = Math.max(-next, n);
        next += n;
        return n;
    }

    /**
     * Tell whether this stream is ready to be read.
     *
     * @return True if the next read() is guaranteed not to block for input
     * @exception IOException If the stream is closed
     */
    @Override
    public boolean ready() throws IOException {
        return true;
    }

    /**
     * Tell whether this stream supports the mark() operation, which it does.
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Mark the present position in the stream. Subsequent calls to reset() will reposition the stream to this point.
     *
     * @param readAheadLimit Limit on the number of characters that may be read while still preserving the mark. Because the stream's input
     *            comes from a string, there is no actual limit, so this argument must not be negative, but is otherwise ignored.
     * @exception IllegalArgumentException If readAheadLimit is < 0
     * @exception IOException If an I/O error occurs
     */
    @Override
    public void mark(final int readAheadLimit) throws IOException {
        if (readAheadLimit < 0) {
            throw new IllegalArgumentException("Read-ahead limit < 0");
        }
        mark = next;
    }

    /**
     * Reset the stream to the most recent mark, or to the beginning of the string if it has never been marked.
     *
     * @exception IOException If an I/O error occurs
     */
    @Override
    public void reset() throws IOException {
        next = mark;
    }

    /**
     * Close the stream.
     */
    @Override
    public void close() {
        // Nope
    }

}
