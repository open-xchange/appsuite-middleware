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
