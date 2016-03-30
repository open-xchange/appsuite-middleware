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

package org.json;

import java.io.IOException;
import java.io.Reader;
import com.fasterxml.jackson.core.io.CharTypes;

/**
 * {@link JSONStringEncoderReader} - A wrapper for a {@link Reader reader} suitable for directly outputting as a JSON string.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class JSONStringEncoderReader extends Reader {

    /**
     * This is the default set of escape codes, over 7-bit ASCII range (first 128 character codes), used for single-byte UTF-8 characters.
     */
    private final static int[] sOutputEscapes = CharTypes.get7BitOutputEscapes();

    private final static char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    private final static int SURR1_FIRST = 0xD800;
    private final static int SURR2_LAST = 0xDFFF;

    private final Reader in;
    private CharStack buffer;

    /**
     * Initializes a new {@link JSONStringEncoderReader}.
     */
    public JSONStringEncoderReader(Reader in) {
        super();
        this.in = in;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    private int checkBuffer() {
        if (null == buffer) {
            return -1;
        }
        int bufferedChar = buffer.next();
        if (bufferedChar < 0) {
            buffer = null;
        }
        return bufferedChar;
    }

    @Override
    public int read() throws IOException {
        int buffered = checkBuffer();
        if (buffered > 0) {
            return buffered;
        }

        int read = in.read();
        if (read < 0) {
            return read; // EOF
        }
        return readChar(read, 0, sOutputEscapes);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        boolean first = true;
        int offset = off;
        int end = offset + len;
        int count = 0;
        while (offset < end) {
            int next = read();

            if (next < 0) {
                // No more characters available
                return first ? -1 : count;
            }

            cbuf[offset++] = (char) next;
            count++;

            if (first) {
                first = false; // Switch flag
            }
        }
        return count;
    }

    private int readChar(int ch, int maxUnescaped, int[] escCodes) {
        if (ch <= 0x7F) {
            int escape = escCodes[ch];
            if (escape == 0) {
                return ch;
            }
            if (escape > 0) {
                // '\\' + ch
                buffer = new SingletonCharStack(escape);
                return '\\';
            }

            return asGenericEscape(ch);
        }

        if (maxUnescaped > 0 && ch > maxUnescaped) {
            return asGenericEscape(ch);
        }

        if (ch <= 0x7FF) {
            return ch;
        }

        return asMultiByteChar(ch);
    }

    private int asGenericEscape(int ch) {
        int c = ch;
        if (c > 0xFF) {
            int hi = (c >> 8) & 0xFF;
            buffer = new ArrayCharStack(new char[] { 'u', HEX_CHARS[hi >> 4], HEX_CHARS[hi & 0xF], HEX_CHARS[c >> 4], HEX_CHARS[c & 0xF] });
            return '\\';
        }

        buffer = new ArrayCharStack(new char[] { 'u', '0', '0', HEX_CHARS[c >> 4], HEX_CHARS[c & 0xF] });
        return '\\';
    }

    private int asMultiByteChar(int ch) {
        if (ch >= SURR1_FIRST && ch <= SURR2_LAST) {
            buffer = new ArrayCharStack(new char[] {  'u', HEX_CHARS[(ch >> 12) & 0xF], HEX_CHARS[(ch >> 8) & 0xF], HEX_CHARS[(ch >> 4) & 0xF], HEX_CHARS[ch & 0xF] });
            return '\\';
        }
        return ch;
    }

    // ---------------------------------------------------------------

    private static interface CharStack {

        int next();
    }

    private static class ArrayCharStack implements CharStack {

        private final char[] chars;
        private int pos;

        ArrayCharStack(char[] chars) {
            super();
            this.chars = chars;
            pos = 0;
        }

        @Override
        public int next() {
            return pos < chars.length ? chars[pos++] : -1;
        }
    }

    private static class SingletonCharStack implements CharStack {

        private int ch;

        SingletonCharStack(int ch) {
            super();
            this.ch = ch;
        }

        @Override
        public int next() {
            if (ch < 0) {
                return -1;
            }
            int ch = this.ch;
            this.ch = -1;
            return ch;
        }
    }

}
