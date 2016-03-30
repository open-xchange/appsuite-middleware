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

package com.openexchange.tools.encoding;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.BitSet;
import com.openexchange.java.Charsets;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * URL encoding and decoding. RFC 2396
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein </a>
 */
public final class URLCoder {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(URLCoder.class);

    private URLCoder() {
        super();
    }

    public static String encode(final String source) {
        return encode(source, Charsets.UTF_8);
    }

    /**
     * Decodes an URL using the charset UTF-8.
     *
     * @param url URL to decode.
     * @return the decoded URL.
     */
    public static String decode(final String url) {
        return decode(url, Charsets.UTF_8);
    }

    public static String decode(final String source, final Charset charset) {
        final ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream();
        int pos = 0;
        while (pos < source.length()) {
            final char chr = source.charAt(pos++);
            if ('\u0025' == chr) {
                baos.write(Hex.toByte(source.substring(pos, pos + 2)));
                pos += 2;
            } else {
                baos.write((byte) chr);
            }
        }
        return Charsets.toString(baos.toByteArray(), charset);
    }

    public static String encode(final String source, final Charset charset) {
        final byte[] bytes = Charsets.getBytes(source, charset);
        final StringBuilder builder = new StringBuilder(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            if (needToBeEncoded.get(bytes[i] < 0 ? 256 + bytes[i] : bytes[i])) {
                builder.append('\u0025');
                builder.append(Hex.toHex(bytes[i]));
            } else {
                builder.append((char) bytes[i]);
            }
        }
        return builder.toString();
    }

    private static BitSet needToBeEncoded = new BitSet(256);

    static {
        // ASCII Control Characters
        for (int i = 0; i < 0x20; i++) {
            needToBeEncoded.set(i);
        }
        // Non-ASCII characters
        for (int i = 0x80; i <= 0xFF; i++) {
            needToBeEncoded.set(i);
        }
        // Reserved Characters
        needToBeEncoded.set(0x24); // Dollar ("$")
        needToBeEncoded.set(0x26); // Ampersand ("&")
        needToBeEncoded.set(0x2b); // Plus ("+")
        needToBeEncoded.set(0x2c); // Comma (",")
        needToBeEncoded.set(0x2f); // Forward slash/Virgule ("/")
        needToBeEncoded.set(0x3a); // Colon (":")
        needToBeEncoded.set(0x3b); // Semi-colon (";")
        needToBeEncoded.set(0x3d); // Equals ("=")
        needToBeEncoded.set(0x3f); // Question mark ("?")
        needToBeEncoded.set(0x40); // 'At' symbol ("@")
        // Unsafe characters
        needToBeEncoded.set(0x20); // Space
        needToBeEncoded.set(0x22); // Quotation marks (<">)
        needToBeEncoded.set(0x3c); // 'Less Than' symbol ("<")
        needToBeEncoded.set(0x3e); // 'Greater Than' symbol (">")
        needToBeEncoded.set(0x23); // 'Pound' character ("#")
        needToBeEncoded.set(0x25); // Percent character ("%")
        needToBeEncoded.set(0x7b); // Left Curly Brace ("{")
        needToBeEncoded.set(0x7d); // Right Curly Brace ("}")
        needToBeEncoded.set(0x7c); // Vertical Bar/Pipe ("|")
        needToBeEncoded.set(0x5c); // Backslash ("\")
        needToBeEncoded.set(0x5e); // Caret ("^")
        needToBeEncoded.set(0x7e); // Tilde ("~")
        needToBeEncoded.set(0x5b); // Left Square Bracket ("[")
        needToBeEncoded.set(0x5d); // Right Square Bracket ("]")
        needToBeEncoded.set(0x60); // Grave Accent ("`")
    }
}
