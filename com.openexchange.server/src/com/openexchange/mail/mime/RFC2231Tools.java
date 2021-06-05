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

package com.openexchange.mail.mime;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.java.CharsetDetector;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.mail.utils.MessageUtility;

/**
 * {@link RFC2231Tools} - A collection of <small><b><a href="http://www.ietf.org/rfc/rfc2231.txt">RFC2231</a></b></small> related utility
 * methods
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RFC2231Tools {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RFC2231Tools.class);

    private static final Locale ENGLISH = Locale.ENGLISH;

    /**
     * No instantiation
     */
    private RFC2231Tools() {
        super();
    }

    private static final Pattern PAT_CL = Pattern.compile("([\\p{ASCII}&&[^']]+)'([\\p{ASCII}&&[^']]*)'(\\p{ASCII}+)");

    /**
     * Checks if given string possibly contains contains a <small><b><a href="http://www.ietf.org/rfc/rfc2231.txt">RFC2231</a></b></small>
     * value.
     *
     * @param toCheck The string to check
     * @return <code>true</code> for RFC2231 value; otherwise <code>false</code>
     */
    public static boolean containsRFC2231Value(String toCheck) {
        return PAT_CL.matcher(toCheck).find();
    }

    /**
     * Parses given <small><b><a href="http://www.ietf.org/rfc/rfc2231.txt">RFC2231</a></b></small> value into its charset, language and
     * rfc2231-encoded value. Therefore <small><b><a href="http://www.ietf.org/rfc/rfc2231.txt">RFC2231</a></b></small> value should match
     * pattern:
     *
     * <pre>
     * &lt;charset-name&gt; + &quot;'&quot; + &lt;language-code&gt; + &quot;'&quot; + &lt;encoded-data&gt;
     * </pre>
     *
     * @param rfc2231Value The rfc2231 value
     * @return An array of {@link String} containing charset, language, and rfc2231-encoded value or <code>null</code> if value does not
     *         match pattern.
     */
    public static String[] parseRFC2231Value(String rfc2231Value) {
        final Matcher m = PAT_CL.matcher(rfc2231Value);
        if (!m.matches()) {
            return null;
        }
        return new String[] { m.group(1), m.group(2), m.group(3) };
    }

    /**
     * Decodes specified string according to mail-safe encoding introduced in <small><b><a
     * href="http://www.ietf.org/rfc/rfc2231.txt">RFC2231</a></b></small>
     * <p>
     * This method assumes that encoding information are contained in given string; e.g.
     *
     * <pre>
     * utf-8'EN'%C2%A4%20txt
     * </pre>
     *
     * @param encoded The encoded string
     * @return The decoded string
     */
    public static String rfc2231Decode(String encoded) {
        final Matcher m = PAT_CL.matcher(encoded);
        if (!m.matches()) {
            return encoded;
        }
        return rfc2231Decode(m.group(3), m.group(1));
    }

    private static final int RADIX = 16;

    /**
     * Decodes specified string according to mail-safe encoding introduced in <small><b><a
     * href="http://www.ietf.org/rfc/rfc2231.txt">RFC2231</a></b></small>
     *
     * @param encoded The encoded string
     * @param charset The charset name
     * @return The decoded string
     */
    public static String rfc2231Decode(String encoded, String charset) {
        if ((encoded == null) || (encoded.length() == 0)) {
            return encoded;
        } else if (null == charset || !CharsetDetector.isValid(charset)) {
            return encoded;
        }

        int length = encoded.length();
        byte[] bytes = new byte[length];
        int pos = 0;
        int i = 0;
        while (i < length) {
            char c = encoded.charAt(i);
            if ('%' == c) {
                if ((i < (length - 2)) && isHexDigit(encoded.charAt(i + 1)) && isHexDigit(encoded.charAt(i + 2))) {
                    bytes[pos++] = ((byte) ((Character.digit(encoded.charAt(i + 1), RADIX) << 4) + Character.digit(encoded.charAt(i + 2), RADIX)));
                    i += 2;
                } else if ((i < (length - 1)) && isHexDigit(encoded.charAt(i + 1))) {
                    bytes[pos++] = ((byte) (Character.digit(encoded.charAt(i + 1), RADIX)));
                    i += 1;
                } else {
                    bytes[pos++] = ((byte) c);
                }
            } else {
                bytes[pos++] = ((byte) c);
            }
            i++;
        }
        try {
            return MessageUtility.readStream(Streams.newByteArrayInputStream(bytes, 0, pos), charset, false, -1);
        } catch (IOException e) {
            // Cannot occur
            throw new IllegalStateException(e);
        }
    }

    /**
     * Retries decoding with self-allocated char buffer with double capacity.
     *
     * @param cs The charset object
     * @param bb The allocated byte buffer
     * @return The decoded string
     */
    private static String rfc2231DecodeRetry(Charset cs, ByteBuffer bb) {
        try {
            /*
             * Set position back to zero
             */
            bb.rewind();
            /*
             * Obtain charset decoder
             */
            final CharsetDecoder decoder =
                cs.newDecoder().onMalformedInput(java.nio.charset.CodingErrorAction.REPLACE).onUnmappableCharacter(
                    java.nio.charset.CodingErrorAction.REPLACE);

            int n = (int) (bb.remaining() * decoder.averageCharsPerByte() * 2);
            java.nio.CharBuffer out = java.nio.CharBuffer.allocate(n);
            if (n == 0) {
                return "";
            }
            decoder.reset();
            for (;;) {
                java.nio.charset.CoderResult cr;
                if (bb.hasRemaining()) {
                    cr = decoder.decode(bb, out, true);
                } else {
                    cr = decoder.flush(out);
                }
                if (cr.isUnderflow()) {
                    break;
                }
                if (cr.isOverflow()) {
                    n *= 2;
                    final CharBuffer o = CharBuffer.allocate(n);
                    out.flip();
                    o.put(out);
                    out = o;
                    continue;
                }
                cr.throwException();
            }
            out.flip();
            return out.toString();
        } catch (CharacterCodingException e) {
            /*
             * Cannot occur
             */
            throw new Error(e);
        }
    }

    private static boolean isHexDigit(char c) {
        final char ch = Character.toLowerCase(c);
        return ((ch >= '0') && (ch <= '9')) || ((ch >= 'a') && (ch <= 'f'));
    }

    /**
     * Encodes given string according to mechanism provided in <small><b><a
     * href="http://www.ietf.org/rfc/rfc2231.txt">RFC2231</a></b></small>.
     *
     * @param toEncode The string to encode
     * @param charset The charset encoding
     * @param language The language to append
     * @param prepend <code>true</code> to prepend charset and language information; otherwise <code>false</code>
     * @return The encoded string
     */
    public static String rfc2231Encode(String toEncode, String charset, String language, boolean prepend) {
        return rfc2231Encode(toEncode, charset, language, prepend, false);
    }

    /**
     * Encodes given string according to mechanism provided in <small><b><a
     * href="http://www.ietf.org/rfc/rfc2231.txt">RFC2231</a></b></small>.
     *
     * @param toEncode The string to encode
     * @param charset The charset encoding
     * @param language The language to append
     * @param prepend <code>true</code> to prepend charset and language information; otherwise <code>false</code>
     * @param force <code>true</code> to force encoding even if string to encode only consists of ASCII characters; otherwise
     *            <code>false</code>
     * @return The encoded string
     */
    public static String rfc2231Encode(String toEncode, String charset, String language, boolean prepend, boolean force) {
        if ((toEncode == null) || (toEncode.length() == 0)) {
            return toEncode;
        } else if (!force && isAscii(toEncode)) {
            return toEncode;
        } else if (!CharsetDetector.isValid(charset)) {
            return toEncode;
        }
        final StringBuilder retval = new StringBuilder(toEncode.length() * 3);
        if (prepend) {
            retval.append(charset.toLowerCase(ENGLISH)).append('\'').append(
                (language == null) || (language.length() == 0) ? "" : language).append('\'');
        }
        try {
            final Charset cs = Charsets.forName(charset);
            final int length = toEncode.length();
            for (int i = 0; i < length; i++) {
                final char c = toEncode.charAt(i);
                if (!isAscii(c) || (c == ' ')) {
                    final byte[] bytes = String.valueOf(c).getBytes(cs);
                    for (int j = 0; j < bytes.length; j++) {
                        retval.append('%').append(Integer.toHexString(bytes[j] & 0xFF).toUpperCase(ENGLISH));
                    }
                } else {
                    retval.append(c);
                }
            }
        } catch (UnsupportedCharsetException e) {
            /*
             * Cannot occur
             */
            LOG.error("", e);
        }
        return retval.toString();
    }

    /**
     * Checks whether the specified string's characters are ASCII 7 bit
     *
     * @param s The string to check
     * @return <code>true</code> if string's characters are ASCII 7 bit; otherwise <code>false</code>
     */
    public static boolean isAscii(String s) {
        if (null == s) {
            return true;
        }
        final int length = s.length();
        if (0 == length) {
            return true;
        }
        boolean isAscci = true;
        for (int i = 0; (i < length) && isAscci; i++) {
            isAscci &= (s.charAt(i) < 128);
        }
        return isAscci;
    }

    /**
     * Checks whether the character is ASCII 7 bit
     *
     * @param c The character to check
     * @return <code>true</code> if character is ASCII 7 bit; otherwise <code>false</code>
     */
    public static boolean isAscii(char c) {
        return (c < 128);
    }

}
