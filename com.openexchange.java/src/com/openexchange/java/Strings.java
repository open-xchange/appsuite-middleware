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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.java;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link Strings} - A library for performing operations that create Strings
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Strings {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Initializes a new {@link Strings}.
     */
    private Strings() {
        super();
    }

    /**
     * Builds up a string from passed objects.
     *
     * @param objects The objects
     * @return The string build up from concatenating objects' string representation
     */
    public static String concat(final Object... objects) {
        if (null == objects || 0 == objects.length) {
            return "";
        }
        final StringAllocator sb = new StringAllocator(2048);
        for (final Object object : objects) {
            sb.append(String.valueOf(object));
        }
        return sb.toString();
    }

    /**
     * Gets the lineSeparator
     *
     * @return The lineSeparator
     */
    public static String getLineSeparator() {
        return LINE_SEPARATOR;
    }

    /**
     * High speed test for whitespace! Faster than the java one (from some testing).
     *
     * @return <code>true</code> if the indicated character is whitespace; otherwise <code>false</code>
     */
    public static boolean isWhitespace(final char c) {
        switch (c) {
        case 9: // 'unicode: 0009
        case 10: // 'unicode: 000A'
        case 11: // 'unicode: 000B'
        case 12: // 'unicode: 000C'
        case 13: // 'unicode: 000D'
        case 28: // 'unicode: 001C'
        case 29: // 'unicode: 001D'
        case 30: // 'unicode: 001E'
        case 31: // 'unicode: 001F'
        case ' ': // Space
            // case Character.SPACE_SEPARATOR:
            // case Character.LINE_SEPARATOR:
        case Character.PARAGRAPH_SEPARATOR:
            return true;
        default:
            return false;
        }
    }

    /**
     * High speed test for ASCII numbers!
     *
     * @return <code>true</code> if the indicated character is whitespace; otherwise <code>false</code>
     */
    public static boolean isDigit(final char c) {
        switch (c) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            return true;
        default:
            return false;
        }
    }

    /**
     * Gets specified string's ASCII bytes
     *
     * @param str The string
     * @return The ASCII bytes
     */
    public static byte[] toAsciiBytes(final CharSequence cs) {
        return Charsets.toAsciiBytes(cs);
    }

    /**
     * Gets specified string's ASCII bytes
     *
     * @param str The string
     * @return The ASCII bytes
     */
    public static byte[] toAsciiBytes(final String str) {
        return Charsets.toAsciiBytes(str);
    }

    /**
     * Writes specified string's ASCII bytes to given stream.
     *
     * @param str The string
     * @param out The stream to write to
     * @throws IOException If an I/O error occurs
     */
    public static void writeAsciiBytes(final String str, final OutputStream out) throws IOException {
        Charsets.writeAsciiBytes(str, out);
    }

    private static final Pattern P_SPLIT_COMMA = Pattern.compile("\\s*,\\s*");

    /**
     * Splits given string by comma separator.
     *
     * @param s The string to split
     * @return The splitted string
     */
    public static String[] splitByComma(final String s) {
        if (null == s) {
            return null;
        }
        return P_SPLIT_COMMA.split(s, 0);
    }

    private static final Pattern P_SPLIT_CRLF = Pattern.compile("\r?\n");

    /**
     * Splits given string by CR?LF; yields line-wise output.
     *
     * @param s The string to split
     * @return The splitted string
     */
    public static String[] splitByCRLF(final String s) {
        if (null == s) {
            return null;
        }
        return P_SPLIT_CRLF.split(s, 0);
    }

    private static final Pattern P_SPLIT_WHITESPACE = Pattern.compile("\\s+");

    /**
     * Splits given string by whitespaces.
     *
     * @param s The string to split
     * @return The splitted string
     */
    public static String[] splitByWhitespaces(final String s) {
        if (null == s) {
            return null;
        }
        return P_SPLIT_WHITESPACE.split(s, 0);
    }

    /**
     * Returns a literal replacement <code>String</code> for the specified <code>String</code>. This method produces a <code>String</code>
     * that will work as a literal replacement <code>s</code> in the <code>appendReplacement</code> method of the {@link Matcher} class. The
     * <code>String</code> produced will match the sequence of characters in <code>s</code> treated as a literal sequence. Slashes ('\') and
     * dollar signs ('$') will be given no special meaning.
     *
     * @param s The string to be literalized
     * @return A literal string replacement
     */
    public static String quoteReplacement(final String s) {
        if (isEmpty(s) || ((s.indexOf('\\') == -1) && (s.indexOf('$') == -1))) {
            return s;
        }
        final int length = s.length();
        final StringAllocator sb = new StringAllocator(length << 1);
        for (int i = 0; i < length; i++) {
            final char c = s.charAt(i);
            if (c == '\\') {
                sb.append('\\');
                sb.append('\\');
            } else if (c == '$') {
                sb.append('\\');
                sb.append('$');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Checks for an empty string.
     *
     * @param string The string
     * @return <code>true</code> if empty; else <code>false</code>
     */
    public static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    /**
     * Fixes possible charset problem in given string.
     * <p>
     * E.g.:&nbsp;&quot;&#195;&#164&quot; instead of &quot;&auml;&quot;
     *
     * @param s The string to check
     * @return The fixed string
     */
    public static String fixCharsetProblem(final String s) {
        if (isEmpty(s)) {
            return s;
        }
        try {
            final byte[] bytes = s.getBytes(Charsets.ISO_8859_1);
            if (isUTF8Bytes(bytes)) {
                return new String(bytes, Charsets.UTF_8);
            }
            return s;
        } catch (final UnsupportedCharsetException e) {
            return s;
        }
    }

    private static final CharsetDecoder UTF8_CHARSET_DECODER;
    static {
        final CharsetDecoder utf8Decoder = Charsets.UTF_8.newDecoder();
        utf8Decoder.onMalformedInput(CodingErrorAction.REPORT);
        utf8Decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        UTF8_CHARSET_DECODER = utf8Decoder;
    }

    /**
     * Checks given bytes for valid UTF-8 bytes.
     *
     * @param bytes The bytes to check
     * @return <code>true</code> for valid UTF-8 bytes; otherwise <code>false</code>
     */
    public static boolean isUTF8Bytes(final byte[] bytes) {
        try {
            UTF8_CHARSET_DECODER.decode(ByteBuffer.wrap(bytes));
            return true;
        } catch (final CharacterCodingException e) {
            return false;
        }
    }

    /**
     * Joins a collection of objects by connecting the results of their #toString() method with a connector
     *
     * @param coll Collection to be connected
     * @param connector Connector place between two objects
     * @return connected strings or null if collection == null or empty string if collection is empty
     */
    public static String join(final Collection<? extends Object> coll, final String connector) {
        if (coll == null) {
            return null;
        }
        if (coll.size() == 0) {
            return "";
        }
        final com.openexchange.java.StringAllocator builder = new com.openexchange.java.StringAllocator();
        for (final Object obj : coll) {
            if (obj == null) {
                builder.append("null");
            } else {
                builder.append(obj.toString());
            }
            builder.append(connector);
        }
        return builder.substring(0, builder.length() - connector.length());
    }

    public static <T> String join(final T[] arr, final String connector) {
        return join(Arrays.asList(arr), connector);
    }

    public static String join(final int[] arr, final String connector) {
        final List<Integer> list = new LinkedList<Integer>();
        for (final int i : arr) {
            list.add(Autoboxing.I(i));
        }
        return join(list, connector);
    }

    public static String join(final byte[] arr, final String connector) {
        final List<Byte> list = new LinkedList<Byte>();
        for (final Byte i : arr) {
            list.add(i);
        }
        return join(list, connector);
    }

    /**
     * Removes byte order marks from UTF8 strings.
     *
     * @return new instance of trimmed string - or reference to old one if unchanged
     */
    public static String trimBOM(final String str) {
        final byte[][] byteOrderMarks =
            new byte[][] {
                new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF }, new byte[] { (byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x0 },
                new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF }, new byte[] { (byte) 0xFE, (byte) 0xFF }, new byte[] { (byte) 0xFE, (byte) 0xFF } };

        final byte[] bytes = str.getBytes();
        for (final byte[] bom : byteOrderMarks) {
            if (bom.length > bytes.length) {
                continue;
            }

            final String pattern = new String(bom);
            if (!str.startsWith(pattern)) {
                continue;
            }

            final int bomLen = new String(bom).getBytes().length; // sadly the BOM got encoded meanwhile

            final int len = bytes.length - bomLen;
            final byte[] trimmed = new byte[len];
            for (int i = 0; i < len; i++) {
                trimmed[i] = bytes[i + bomLen];
            }
            return new String(trimmed);
        }

        return str;
    }

    /**
     * Abbreviates a String using ellipses. This will turn "Now is the time for all good men" into "Now is the time for..."
     * <p>
     * Specifically:
     * <ul>
     * <li>If <code>str</code> is less than <code>maxWidth</code> characters long, return it.</li>
     * <li>Else abbreviate it to <code>(substring(str, 0, max-3) + "...")</code>.</li>
     * <li>If <code>maxWidth</code> is less than <code>4</code>, throw an <code>IllegalArgumentException</code>.</li>
     * <li>In no case will it return a String of length greater than <code>maxWidth</code>.</li>
     * </ul>
     * </p>
     *
     * <pre>
     * StringUtils.abbreviate(null, *)      = null
     * StringUtils.abbreviate("", 4)        = ""
     * StringUtils.abbreviate("abcdefg", 6) = "abc..."
     * StringUtils.abbreviate("abcdefg", 7) = "abcdefg"
     * StringUtils.abbreviate("abcdefg", 8) = "abcdefg"
     * StringUtils.abbreviate("abcdefg", 4) = "a..."
     * StringUtils.abbreviate("abcdefg", 3) = IllegalArgumentException
     * </pre>
     *
     * @param str The String to check, may be null
     * @param maxWidth The maximum length of result String, must be at least 4
     * @return The abbreviated String, <code>null</code> if null String input
     * @throws IllegalArgumentException If the width is too small
     */
    public static String abbreviate(final String str, final int maxWidth) {
        return abbreviate(str, 0, maxWidth);
    }

    /**
     * Abbreviates a String using ellipses. This will turn "Now is the time for all good men" into "...is the time for..."
     * <p>
     * Works like <code>abbreviate(String, int)</code>, but allows you to specify a "left edge" offset. Note that this left edge is not
     * necessarily going to be the leftmost character in the result, or the first character following the ellipses, but it will appear
     * somewhere in the result.
     * <p>
     * In no case will it return a String of length greater than <code>maxWidth</code>.
     *
     * <pre>
     * StringUtils.abbreviate(null, *, *)                = null
     * StringUtils.abbreviate("", 0, 4)                  = ""
     * StringUtils.abbreviate("abcdefghijklmno", -1, 10) = "abcdefg..."
     * StringUtils.abbreviate("abcdefghijklmno", 0, 10)  = "abcdefg..."
     * StringUtils.abbreviate("abcdefghijklmno", 1, 10)  = "abcdefg..."
     * StringUtils.abbreviate("abcdefghijklmno", 4, 10)  = "abcdefg..."
     * StringUtils.abbreviate("abcdefghijklmno", 5, 10)  = "...fghi..."
     * StringUtils.abbreviate("abcdefghijklmno", 6, 10)  = "...ghij..."
     * StringUtils.abbreviate("abcdefghijklmno", 8, 10)  = "...ijklmno"
     * StringUtils.abbreviate("abcdefghijklmno", 10, 10) = "...ijklmno"
     * StringUtils.abbreviate("abcdefghijklmno", 12, 10) = "...ijklmno"
     * StringUtils.abbreviate("abcdefghij", 0, 3)        = IllegalArgumentException
     * StringUtils.abbreviate("abcdefghij", 5, 6)        = IllegalArgumentException
     * </pre>
     *
     * @param str The String to check, may be null
     * @param offset The left edge of source String
     * @param maxWidth The maximum length of result String, must be at least <code>4</code>
     * @return The abbreviated String, <code>null</code> if null String input
     * @throws IllegalArgumentException If the width is too small
     */
    public static String abbreviate(final String str, final int offset, final int maxWidth) {
        if (str == null) {
            return null;
        }
        if (maxWidth < 4) {
            throw new IllegalArgumentException("Minimum abbreviation width is 4");
        }
        if (str.length() <= maxWidth) {
            return str;
        }
        int off = offset;
        if (off > str.length()) {
            off = str.length();
        }
        if ((str.length() - off) < (maxWidth - 3)) {
            off = str.length() - (maxWidth - 3);
        }
        if (off <= 4) {
            return str.substring(0, maxWidth - 3) + "...";
        }
        if (maxWidth < 7) {
            throw new IllegalArgumentException("Minimum abbreviation width with offset is 7");
        }
        if ((off + (maxWidth - 3)) < str.length()) {
            return "..." + abbreviate(str.substring(off), maxWidth - 3);
        }
        return "..." + str.substring(str.length() - (maxWidth - 3));
    }

}
