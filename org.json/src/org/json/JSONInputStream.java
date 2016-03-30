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
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.helpers.UnsynchronizedByteArrayOutputStream;

/**
 * {@link JSONInputStream} - Directly converts a given {@link JSONValue} to a readable input stream.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSONInputStream extends InputStream {

    private static final int BUFSIZE = 8192;

    private static interface Bufferer {

        /**
         * Gets the closing character
         *
         * @return The closing character
         */
        char getClosing();

        /**
         * Signals if there is another element to write
         *
         * @return <code>true</code> if further element available; otherwise <code>false</code>
         */
        boolean hasNext();

        /**
         * Gets the next element to write
         *
         * @return The next element
         * @throws IOException If next element cannot be returned
         */
        Object next() throws IOException;

        /**
         * Attempts to write more bytes.
         *
         * @return <code>true</code> if more bytes available to write; otherwise <code>false</code> if no more bytes are available
         * @throws IOException If an I/O error occurs
         */
        boolean writeMoreBytes() throws IOException;
    }

    private abstract class AbstractBufferer implements Bufferer {

        private final Pattern patternSupplementaryCodePoints;

        protected AbstractBufferer() {
            super();
            patternSupplementaryCodePoints = Pattern.compile("\\\\u([0-9a-fA-F]{5,})");
        }

        @Override
        public boolean writeMoreBytes() throws IOException {
            out.reset();
            if (!hasNext()) {
                out.write(getClosing());
                count = 1;
                pos = 0;
                finished = true;
                nested = null;
                return false;
            }
            if (first) {
                first = false;
            } else {
                out.write(',');
            }
            writeValue(next());
            pos = 0;
            count = out.getCount();
            return true;
        }

        private void writeValue(final Object value) throws IOException {
            if (value instanceof JSONValue) {
                nested = new JSONInputStream((JSONValue) value, charset);
            } else if (value instanceof String) {
                out.write('"');
                out.write(toAsciiBytes(toAscii(value.toString())));
                out.write('"');
            } else {
                out.write(toAsciiBytes(null == value ? "null" : value.toString()));
            }
        }

        /** Writes specified String's ASCII bytes */
        protected byte[] toAsciiBytes(final String str) {
            if (null == str) {
                return null;
            }
            final int length = str.length();
            if (0 == length) {
                return new byte[0];
            }
            final byte[] ret = new byte[length];
            str.getBytes(0, length, ret, 0);
            return ret;
        }

        /** Converts specified String to JSON's ASCII notation */
        protected String toAscii(final String str) {
            if (null == str) {
                return str;
            }
            final int length = str.length();
            if (0 == length || isAscii(str, length)) {
                return replaceSupplementaryCodePoints(str);
            }
            final StringBuilder sa = new StringBuilder((length * 3) / 2 + 1);
            for (int i = 0; i < length; i++) {
                final char c = str.charAt(i);
                if ((c > 127) || (c < 32) || ('"' == c)) {
                    if (Character.isSupplementaryCodePoint(c)) {
                        final char[] chars = Character.toChars(c);
                        for (int j = 0; j < chars.length; j++) {
                            appendAsJsonUnicode(chars[j], sa);
                        }
                    } else {
                        appendAsJsonUnicode(c, sa);
                    }
                } else {
                    sa.append(c);
                }
            }
            return replaceSupplementaryCodePoints(sa.toString());
        }

        private void appendAsJsonUnicode(final int ch, final StringBuilder sa) {
            sa.append("\\u");
            final String hex = Integer.toString(ch, 16);
            for (int i = hex.length(); i < 4; i++) {
                sa.append('0');
            }
            sa.append(hex);
        }

        private boolean isAscii(final String s, final int length) {
            boolean isAscci = true;
            for (int i = 0; isAscci && (i < length); i++) {
                final char c = s.charAt(i);
                isAscci = (c < 128) && (c > 31) && (c != '"');
            }
            return isAscci;
        }

        private String replaceSupplementaryCodePoints(final String str) {
            final Matcher m = patternSupplementaryCodePoints.matcher(str);
            if (!m.find()) {
                return str;
            }
            final StringBuffer sb = new StringBuffer(str.length());
            do {
                int codePoint = Integer.parseInt(m.group(1), 16);
                if (Character.isSupplementaryCodePoint(codePoint)) {
                    final char[] chars = Character.toChars(codePoint);
                    final StringBuilder tmp = new StringBuilder(32);
                    for (int j = 0; j < chars.length; j++) {
                        appendAsJsonUnicode(chars[j], tmp);
                    }
                    m.appendReplacement(sb, Matcher.quoteReplacement(tmp.toString()));
                }
            } while (m.find());
            m.appendTail(sb);
            return sb.toString();
        }

    }

    private final class ArrayBufferer extends AbstractBufferer {

        private final Iterator<Object> arrIterator;

        ArrayBufferer(Iterator<Object> arrIterator) {
            super();
            this.arrIterator = arrIterator;
        }

        @Override
        public char getClosing() {
            return ']';
        }

        @Override
        public boolean hasNext() {
            return arrIterator.hasNext();
        }

        @Override
        public Object next() {
            return arrIterator.next();
        }

    }

    private final class ObjectBufferer extends AbstractBufferer {

        private final Iterator<Entry<String, Object>> objIterator;

        ObjectBufferer(final Iterator<Entry<String, Object>> objIterator) {
            super();
            this.objIterator = objIterator;
        }

        @Override
        public char getClosing() {
            return '}';
        }

        @Override
        public boolean hasNext() {
            return objIterator.hasNext();
        }

        @Override
        public Object next() throws IOException {
            final Entry<String, Object> entry = objIterator.next();
            out.write('"');
            out.write(toAsciiBytes(toAscii(entry.getKey())));
            out.write('"');
            out.write(':');
            return entry.getValue();
        }

    }

    protected final String charset;
    private final Bufferer bufferer;
    protected int pos;
    protected int count;
    protected boolean finished;
    protected boolean first;
    protected final UnsynchronizedByteArrayOutputStream out;
    protected InputStream nested;

    /**
     * Initializes a new {@link JSONInputStream}.
     *
     * @param jsonValue The JSON value to read from
     * @param charset The charset
     */
    public JSONInputStream(final JSONValue jsonValue, final String charset) {
        super();
        first = true;
        finished = false;
        this.charset = charset;
        out = new UnsynchronizedByteArrayOutputStream(BUFSIZE);
        if (jsonValue.isArray()) {
            bufferer = new ArrayBufferer(jsonValue.toArray().iterator());
            out.write('[');
        } else {
            bufferer = new ObjectBufferer(jsonValue.toObject().entrySet().iterator());
            out.write('{');
        }
        count = 1;
        pos = 0;
    }

    private boolean hasBytes() {
        return (pos < count) || (nested != null);
    }

    private boolean writeMoreBytes() throws IOException {
        return bufferer.writeMoreBytes();
    }

    @Override
    public int read() throws IOException {
        if (finished) {
            return -1;
        }
        if (!hasBytes()) {
            // Write more bytes to buffer
            if (!writeMoreBytes()) {
                // Last byte written
                return out.getBuf()[pos++];
            }
        }
        if (pos < count) {
            return out.getBuf()[pos++];
        }
        final int read = nested.read();
        if (read >= 0) {
            return read;
        }
        // Reached end of nested stream
        nested = null;
        return read();
    }

}
