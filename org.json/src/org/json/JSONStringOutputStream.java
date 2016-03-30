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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import com.fasterxml.jackson.core.io.CharTypes;

/**
 * {@link JSONStringOutputStream} - An JSON string output stream.
 * <p>
 * Derived from Jackson's <a
 * href="http://fasterxml.github.com/jackson-core/javadoc/2.1.0/com/fasterxml/jackson/core/json/UTF8JsonGenerator.html"
 * >&quot;com.fasterxml.jackson.core.json.UTF8JsonGenerator&quot;</a> class.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSONStringOutputStream extends OutputStream {

    /**
     * This is the default set of escape codes, over 7-bit ASCII range (first 128 character codes), used for single-byte UTF-8 characters.
     */
    private final static int[] sOutputEscapes = CharTypes.get7BitOutputEscapes();

    private final static byte[] HEX_CHARS = CharTypes.copyHexBytes();

    private final static Charset UTF8_CHARSET = Charset.forName("UTF-8");

    private final static byte BYTE_u = (byte) 'u';
    private final static byte BYTE_0 = (byte) '0';
    private final static byte BYTE_BACKSLASH = (byte) '\\';

    private final static int SURR1_FIRST = 0xD800;
    private final static int SURR2_LAST = 0xDFFF;

    private final OutputStream out;

    /**
     * Initializes a new {@link JSONStringOutputStream}.
     * 
     * @param out The output stream to write to
     */
    public JSONStringOutputStream(final OutputStream out) {
        super();
        if ((out instanceof BufferedOutputStream) || (out instanceof ByteArrayOutputStream)) {
            this.out = out;
        } else {
            // Initialize with AJP's max. size for a SEND_BODY_CHUNK package
            this.out = new BufferedOutputStream(out, 8184);
        }
    }

    @Override
    public void write(final int ch) throws IOException {
        writeChar(ch, 127, sOutputEscapes);
    }

    @Override
    public void write(final byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        final int[] escCodes = sOutputEscapes;
        final int maxUnescaped = 127;

        int offset = off;
        final int end = offset + len;
        int prev = 0;
        while (offset < end) {
            int ch = b[offset++];
            if (ch < 0) {
                if (prev == 0) {
                    prev = ch;
                    continue;
                }
                ch = new String(new byte[] { (byte) prev, (byte) ch }, UTF8_CHARSET).charAt(0);
                prev = 0;
            } else {
                if (prev != 0) {
                    writeChar(prev, maxUnescaped, escCodes);
                    prev = 0;
                }
            }
            writeChar(ch, maxUnescaped, escCodes);
        }
        if (prev != 0) {
            writeChar(prev, maxUnescaped, escCodes);
            prev = 0;
        }
    }

    private void writeChar(int c, final int maxUnescaped, final int[] escCodes) throws IOException {
        int ch = c;
        if (ch < 0) {
            ch = ch & 0xff;
        }
        if (ch <= 0x7F) {
            if (escCodes[ch] == 0) {
                out.write(ch);
                return;
            }
            final int escape = escCodes[ch];
            if (escape > 0) { // 2-char escape, fine
                out.write(BYTE_BACKSLASH);
                out.write((byte) escape);
            } else {
                // ctrl-char, 6-byte escape...
                _writeGenericEscape(ch);
            }
            return;
        }
        if (ch > maxUnescaped) { // Allow forced escaping if non-ASCII (etc) chars:
            _writeGenericEscape(ch);
            return;
        }
        if (ch <= 0x7FF) {
            // fine, just needs 2 byte output
            out.write((byte) (0xc0 | (ch >> 6)));
            out.write((byte) (0x80 | (ch & 0x3f)));
        } else {
            _outputMultiByteChar(ch);
        }
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    /**
     * Method called to write a generic Unicode escape for given character.
     * 
     * @param charToEscape Character to escape using escape sequence (\\uXXXX)
     */
    private void _writeGenericEscape(final int charToEscape) throws IOException {
        int c = charToEscape;
        out.write(BYTE_BACKSLASH);
        out.write(BYTE_u);
        if (c > 0xFF) {
            final int hi = (c >> 8) & 0xFF;
            out.write(HEX_CHARS[hi >> 4]);
            out.write(HEX_CHARS[hi & 0xF]);
            c &= 0xFF;
        } else {
            out.write(BYTE_0);
            out.write(BYTE_0);
        }
        // We know it's a control char, so only the last 2 chars are non-0
        out.write(HEX_CHARS[c >> 4]);
        out.write(HEX_CHARS[c & 0xF]);
    }

    /**
     * @param ch
     * @param outputPtr Position within output buffer to append multi-byte in
     * @return New output position after appending
     * @throws IOException
     */
    private void _outputMultiByteChar(final int ch) throws IOException {
        if (ch >= SURR1_FIRST && ch <= SURR2_LAST) { // yes, outside of BMP; add an escape
            out.write(BYTE_BACKSLASH);
            out.write(BYTE_u);

            out.write(HEX_CHARS[(ch >> 12) & 0xF]);
            out.write(HEX_CHARS[(ch >> 8) & 0xF]);
            out.write(HEX_CHARS[(ch >> 4) & 0xF]);
            out.write(HEX_CHARS[ch & 0xF]);
        } else {
            out.write((byte) (0xe0 | (ch >> 12)));
            out.write((byte) (0x80 | ((ch >> 6) & 0x3f)));
            out.write((byte) (0x80 | (ch & 0x3f)));
        }
    }

}
