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

package com.openexchange.java;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnsupportedCharsetException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
     * Checks if given string starts with any of specified prefixes
     *
     * @param s The string to check
     * @param prefixes The prefixes
     * @return <code>true</code> if given string starts with any of specified prefixes; otherwise <code>false</code>
     */
    public static boolean startsWithAny(String s, String... prefixes) {
        if (null == s) {
            return false;
        }
        for (int i = prefixes.length; i-- > 0;) {
            String prefix = prefixes[i];
            if (null != prefix && s.startsWith(prefix, 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds up a string from passed objects.
     *
     * @param objects The objects
     * @return The string build up from concatenating objects' string representation
     */
    public static String concat(Object... objects) {
        if (null == objects || 0 == objects.length) {
            return "";
        }
        StringBuilder sb = new StringBuilder(2048);
        for (Object object : objects) {
            sb.append(object == null ? "null" : object.toString());
        }
        return sb.toString();
    }

    /**
     * Builds up a string from passed strings.
     *
     * @param strings The strings
     * @return The string build up from concatenating strings
     */
    public static String concat(String... strings) {
        if (null == strings || 0 == strings.length) {
            return "";
        }
        StringBuilder sb = new StringBuilder(2048);
        for (String str : strings) {
            sb.append(str == null ? "null" : str);
        }
        return sb.toString();
    }

    /**
     * Builds up a string from passed objects.
     *
     * @param delimiter The delimiter string
     * @param objects The objects
     * @return The string build up from concatenating objects' string representation
     */
    public static String concat(String delimiter, Object... objects) {
        if (null == objects) {
            return "";
        }
        if (null == delimiter) {
            return concat(objects);
        }
        int length = objects.length;
        if (length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(2048);
        sb.append(objects[0] == null ? "null" : objects[0].toString());
        for (int i = 1; i < length; i++) {
            sb.append(delimiter).append(objects[i] == null ? "null" : objects[i].toString());
        }
        return sb.toString();
    }

    /**
     * Builds up a string from passed objects.
     *
     * @param delimiter The delimiter string
     * @param strings The strings
     * @return The string build up from concatenating objects' string representation
     */
    public static String concat(String delimiter, String... strings) {
        if (null == strings) {
            return "";
        }
        if (null == delimiter) {
            return concat(strings);
        }
        int length = strings.length;
        if (length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(2048);
        sb.append(strings[0] == null ? "null" : strings[0]);
        for (int i = 1; i < length; i++) {
            sb.append(delimiter).append(strings[i] == null ? "null" : strings[i]);
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

    /**
     * Splits given string by tokens/quoted-strings.
     *
     * @param str The string to split
     * @return The tokens/quoted-strings
     */
    public static String[] splitByTokensOrQuotedStrings(String str) {
        if (null == str) {
            return null;
        }
        List<String> splitted = new LinkedList<String>();
        int inQuotes = 0;
        boolean escaped = false;
        StringBuilder s = new StringBuilder(16);

        int length = str.length();
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if (Strings.isWhitespace(c)) {
                if (inQuotes > 0) {
                    if (inQuotes == c && !escaped) {
                        inQuotes = 0;
                    }
                    s.append(c);
                    escaped = false;
                } else {
                    if (s.length() > 0) {
                        splitted.add(s.toString().trim());
                        s.setLength(0);
                    }
                }
            } else if ('\\' == c) {
                escaped = !escaped;
                s.append(c);
            } else {
                if (('"' == c || '\'' == c) && !escaped) {
                    inQuotes = inQuotes > 0 ? 0 : c;
                }
                s.append(c);
                escaped = false;
            }
        }
        if (s.length() > 0) {
            splitted.add(s.toString().trim());
        }
        return splitted.toArray(new String[splitted.size()]);
    }

    /**
     * Splits given string by specifies delimiter.
     *
     * @param str The string to split
     * @param delim The delimiting character
     * @return The split string
     */
    public static String[] splitByDelimNotInQuotes(String str, char delim) {
        if (null == str) {
            return null;
        }
        List<String> splitted = new LinkedList<String>();
        boolean inQuotes = false;
        boolean escaped = false;
        StringBuilder s = new StringBuilder(16);

        int length = str.length();
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if (c == delim) {
                if (inQuotes) {
                    if ('"' == c && !escaped) {
                        inQuotes = !inQuotes;
                    }
                    s.append(c);
                    escaped = false;
                } else {
                    splitted.add(s.toString().trim());
                    s.setLength(0);
                }
            } else if ('\\' == c) {
                escaped = !escaped;
                s.append(c);
            } else {
                if ('"' == c && !escaped) {
                    inQuotes = !inQuotes;
                }
                s.append(c);
                escaped = false;
            }
        }
        splitted.add(s.toString().trim());
        return splitted.toArray(new String[splitted.size()]);
    }

    /**
     * Splits given string by comma separator.
     *
     * @param str The string to split
     * @return The split string
     */
    public static String[] splitByCommaNotInQuotes(String str) {
        return splitByDelimNotInQuotes(str, ',');
    }

    private static final Pattern P_SPLIT_COMMA = Pattern.compile("\\s*,\\s*");

    /**
     * Splits given string by comma separator.
     *
     * @param s The string to split
     * @return The split string
     */
    public static String[] splitByComma(final String s) {
        if (null == s) {
            return null;
        }
        return P_SPLIT_COMMA.split(s, 0);
    }

    private static final Pattern P_SPLIT_COLON = Pattern.compile("\\s*\\:\\s*");

    /**
     * Splits given string by colon separator.
     *
     * @param s The string to split
     * @return The split string
     */
    public static String[] splitByColon(final String s) {
        if (null == s) {
            return null;
        }
        return P_SPLIT_COLON.split(s, 0);
    }

    private static final Pattern P_SPLIT_DOT = Pattern.compile("\\s*\\.\\s*");

    /**
     * Splits given string by dots.
     *
     * @param s The string to split
     * @return The split string
     */
    public static String[] splitByDots(final String s) {
        if (null == s) {
            return null;
        }
        return P_SPLIT_DOT.split(s, 0);
    }

    private static final Pattern P_SPLIT_AMP = Pattern.compile("&");

    /**
     * Splits given string by ampersands <code>'&'</code>.
     *
     * @param s The string to split
     * @return The split string
     */
    public static String[] splitByAmps(final String s) {
        if (null == s) {
            return null;
        }
        return P_SPLIT_AMP.split(s, 0);
    }

    private static final Pattern P_SPLIT_CRLF = Pattern.compile("\r?\n");

    /**
     * Splits given string by CR?LF; yields line-wise output.
     *
     * @param s The string to split
     * @return The split string
     */
    public static String[] splitByCRLF(final String s) {
        if (null == s) {
            return null;
        }
        return P_SPLIT_CRLF.split(s, 0);
    }

    private static final Pattern P_SPLIT_TAB = Pattern.compile("\t");

    /**
     * Splits given string by tabs.
     *
     * @param s The string to split
     * @return The split string
     */
    public static String[] splitByTab(final String s) {
        if (null == s) {
            return null;
        }
        return P_SPLIT_TAB.split(s, 0);
    }

    private static final Pattern P_SPLIT_WHITESPACE = Pattern.compile("\\s+");

    /**
     * Splits given string by whitespaces.
     *
     * @param s The string to split
     * @return The split string
     */
    public static String[] splitByWhitespaces(final String s) {
        if (null == s) {
            return null;
        }
        return P_SPLIT_WHITESPACE.split(s, 0);
    }

    /**
     * Replaces whitespaces in given string with specified <code>replacement</code>.
     *
     * @param s The string replacement
     * @param replacement The string replacement
     * @return The replaced string
     */
    public static String replaceWhitespacesWith(final String s, final String replacement) {
        if (null == s) {
            return null;
        }
        return P_SPLIT_WHITESPACE.matcher(s).replaceAll(null == replacement ? "" : quoteReplacement(replacement));
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
        final StringBuilder sb = new StringBuilder(length << 1);
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

    private static final Pattern PATTERN_CONTROL = Pattern.compile("[\\x00-\\x1F\\x7F]+");

    /**
     * Replaces control characters with space characters.
     * <p>
     * A subsequent range of control characters gets replaced with a whitespace; e.g.
     * <pre>
     * <code>"\r\n\t"</code> -&gt; <code>" "</code>
     * </pre>
     *
     * @param str The string to sanitize
     * @return The sanitized string
     */
    public static String sanitizeString(final String str) {
        if (isEmpty(str)) {
            return str;
        }
        return PATTERN_CONTROL.matcher(str).replaceAll(" ");
    }

    /**
     * Checks for an empty string.
     *
     * @param str The string
     * @return <code>true</code> if input is <code>null</code>, empty or only consists of white-space characters; else <code>false</code>
     */
    public static boolean isEmpty(final String str) {
        if (null == str) {
            return true;
        }
        final int len = str.length();
        boolean isWhitespace = true;
        for (int i = len; isWhitespace && i-- > 0;) {
            isWhitespace = isWhitespace(str.charAt(i));
        }
        return isWhitespace;
    }

    /**
     * Checks for a non-empty string.
     *
     * @param string The string
     * @return <code>true</code> if input is a non-empty string; else <code>false</code>
     */
    public static boolean isNotEmpty(final String string) {
        return !isEmpty(string);
    }

    /**
     * Checks for an empty character sequence.
     *
     * @param charSeq The character sequence
     * @return <code>true</code> if input is <code>null</code>, empty or only consists of white-space characters; else <code>false</code>
     */
    public static boolean isEmptyCharSequence(final CharSequence charSeq) {
        if (null == charSeq) {
            return true;
        }
        final int len = charSeq.length();
        boolean isWhitespace = true;
        for (int i = len; isWhitespace && i-- > 0;) {
            isWhitespace = isWhitespace(charSeq.charAt(i));
        }
        return isWhitespace;
    }

    /**
     * Checks for a non-empty character sequence.
     *
     * @param charSeq The character sequence
     * @return <code>true</code> if input is a non-empty string; else <code>false</code>
     */
    public static boolean isNotEmptyCharSequence(final CharSequence charSeq) {
        return !isEmptyCharSequence(charSeq);
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
        final int size = coll.size();
        if (size == 0) {
            return "";
        }
        final StringBuilder builder = new StringBuilder(size << 4);
        for (final Object obj : coll) {
            builder.append(obj == null ? "null" : obj.toString()).append(connector);
        }
        return builder.substring(0, builder.length() - connector.length());
    }

    /**
     * Joins a collection of objects by connecting the results of their #toString() method with a connector
     *
     * @param coll Collection to be connected
     * @param connector Connector place between two objects
     * @param builder The string builder to use
     * @return connected strings or null if collection == null or empty string if collection is empty
     */
    public static void join(final Collection<? extends Object> coll, final String connector, final StringBuilder builder) {
        if (coll == null) {
            return;
        }
        final int size = coll.size();
        if (size == 0) {
            return;
        }
        for (final Object obj : coll) {
            builder.append(obj == null ? "null" : obj.toString()).append(connector);
        }
        builder.setLength(builder.length() - connector.length());
    }

    /**
     * Joins an array of integers by connecting their String representations with a connector
     *
     * @param arr Integers to be connected
     * @param connector Connector place between two objects
     * @param builder The string builder to use
     * @return connected strings or null if collection == null or empty string if collection is empty
     */
    public static void join(final int[] arr, final String connector, final StringBuilder builder) {
        final List<Integer> list = new LinkedList<Integer>();
        for (final int i : arr) {
            list.add(Autoboxing.I(i));
        }
        join(list, connector, builder);
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
        final byte[][] byteOrderMarks = new byte[][] {
            new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF },
            new byte[] { (byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x0 }, new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF },
            new byte[] { (byte) 0xFE, (byte) 0xFF }, new byte[] { (byte) 0xFE, (byte) 0xFF } };

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
     * StringUtils.abbreviate(null, *) = null
     * StringUtils.abbreviate("", 4) = ""
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
     * StringUtils.abbreviate(null, *, *) = null
     * StringUtils.abbreviate("", 0, 4) = ""
     * StringUtils.abbreviate("abcdefghijklmno", -1, 10) = "abcdefg..."
     * StringUtils.abbreviate("abcdefghijklmno", 0, 10) = "abcdefg..."
     * StringUtils.abbreviate("abcdefghijklmno", 1, 10) = "abcdefg..."
     * StringUtils.abbreviate("abcdefghijklmno", 4, 10) = "abcdefg..."
     * StringUtils.abbreviate("abcdefghijklmno", 5, 10) = "...fghi..."
     * StringUtils.abbreviate("abcdefghijklmno", 6, 10) = "...ghij..."
     * StringUtils.abbreviate("abcdefghijklmno", 8, 10) = "...ijklmno"
     * StringUtils.abbreviate("abcdefghijklmno", 10, 10) = "...ijklmno"
     * StringUtils.abbreviate("abcdefghijklmno", 12, 10) = "...ijklmno"
     * StringUtils.abbreviate("abcdefghij", 0, 3) = IllegalArgumentException
     * StringUtils.abbreviate("abcdefghij", 5, 6) = IllegalArgumentException
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

    /**
     * Puts double quotes around a string.
     *
     * @param s The string to quote.
     * @return The quoted string.
     */
    public static String quote(final String s) {
        return concat('"', s, '"');
    }

    /**
     * Removes single or double quotes from a string if its quoted.
     *
     * @param s The value to be unquoted
     * @return The unquoted value or <code>null</code>
     */
    public static String unquote(final String s) {
        if (!isEmpty(s) && ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    /**
     * Removes parenthesis from a string if parenthized.
     *
     * @param s The value to be un-parenthized
     * @return The un-parenthized value or <code>null</code>
     */
    public static String unparenthize(final String s) {
        if (!isEmpty(s) && ((s.startsWith("(") && s.endsWith(")")) || (s.startsWith("{") && s.endsWith("}")) || (s.startsWith("[") && s.endsWith("]")))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    /**
     * Removes surrounding characters from a string in case it is actually surrounded.
     *
     * @param s The value to be un-char'd
     * @return The un-char'd value or <code>null</code>
     */
    public static String unchar(final String s, final char c) {
        return unchar(s, c, c);
    }

    /**
     * Removes surrounding characters from a string in case it is actually surrounded.
     *
     * @param s The value to be un-char'd
     * @param start The possible starting character
     * @param end The possible ending character
     * @return The un-char'd value or <code>null</code>
     */
    public static String unchar(final String s, final char start, final char end) {
        if (!isEmpty(s) && (s.startsWith(Character.toString(start)) && s.endsWith(Character.toString(end)))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    /**
     * Generates a string of code points for given string
     *
     * @param str The string
     * @return The code points
     */
    public static String toCodePoints(final String str) {
        if (null == str) {
            return null;
        }
        final int length = str.length();
        final StringBuilder sb = new StringBuilder(length << 1);
        for (int i = 0; i < length; i++) {
            sb.append(' ').append(str.codePointAt(i));
        }
        return sb.deleteCharAt(0).toString();
    }

    /**
     * Generates a string of code points for given string
     *
     * @param str The string
     * @param out The print stream to print to
     * @return The code points
     */
    public static void outCodePoints(final String str, final PrintStream out) {
        if (null == out) {
            System.out.println(toCodePoints(str));
        } else {
            out.println(toCodePoints(str));
        }
    }

    /** ASCII-wise to upper-case */
    public static String toUpperCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'a') && (c <= 'z') ? (char) (c & 0x5f) : c);
        }
        return builder.toString();
    }

    /** ASCII-wise to lower-case */
    public static String toLowerCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

    private static char[] lowercases = {
        '\000', '\001', '\002', '\003', '\004', '\005', '\006', '\007', '\010', '\011', '\012', '\013', '\014', '\015', '\016', '\017',
        '\020', '\021', '\022', '\023', '\024', '\025', '\026', '\027', '\030', '\031', '\032', '\033', '\034', '\035', '\036', '\037',
        '\040', '\041', '\042', '\043', '\044', '\045', '\046', '\047', '\050', '\051', '\052', '\053', '\054', '\055', '\056', '\057',
        '\060', '\061', '\062', '\063', '\064', '\065', '\066', '\067', '\070', '\071', '\072', '\073', '\074', '\075', '\076', '\077',
        '\100', '\141', '\142', '\143', '\144', '\145', '\146', '\147', '\150', '\151', '\152', '\153', '\154', '\155', '\156', '\157',
        '\160', '\161', '\162', '\163', '\164', '\165', '\166', '\167', '\170', '\171', '\172', '\133', '\134', '\135', '\136', '\137',
        '\140', '\141', '\142', '\143', '\144', '\145', '\146', '\147', '\150', '\151', '\152', '\153', '\154', '\155', '\156', '\157',
        '\160', '\161', '\162', '\163', '\164', '\165', '\166', '\167', '\170', '\171', '\172', '\173', '\174', '\175', '\176', '\177' };

    /**
     * Fast lower-case conversion.
     *
     * @param s The string
     * @return The lower-case string
     */
    public static String asciiLowerCase(String s) {
        if (null == s) {
            return null;
        }

        char[] c = null;
        int i = s.length();

        // look for first conversion
        while (i-- > 0) {
            char c1 = s.charAt(i);
            if (c1 <= 127) {
                char c2 = lowercases[c1];
                if (c1 != c2) {
                    c = s.toCharArray();
                    c[i] = c2;
                    break;
                }
            }
        }

        while (i-- > 0) {
            if (c[i] <= 127) {
                c[i] = lowercases[c[i]];
            }
        }

        return c == null ? s : new String(c);
    }

    /**
     * Takes a String of separated values, splits it at the separator, trims the split values and returns them as List.
     *
     * @param input String of separated values
     * @param separator the separator as regular expression used to split the input around this separator
     * @return the split and trimmed input as List or an empty list
     * @throws IllegalArgumentException if input or the separator are missing or if the separator isn't a valid pattern
     */
    public static List<String> splitAndTrim(String input, String separator) {
        if (input == null) {
            throw new IllegalArgumentException("Missing input");
        }
        if (Strings.isEmpty(input)) {
            return Collections.emptyList();
        }
        if (Strings.isEmpty(separator)) {
            throw new IllegalArgumentException("Missing separator");
        }

        try {
            String[] splits = input.split(separator);
            ArrayList<String> trimmedSplits = new ArrayList<String>(splits.length);
            for (String string : splits) {
                trimmedSplits.add(string.trim());
            }
            return trimmedSplits;
        } catch (PatternSyntaxException pse) {
            throw new IllegalArgumentException("Illegal pattern syntax", pse);
        }
    }

    /**
     * Gets a value indicating whether the supplied strings are equal, using their {@link Form#NFC} normalization from, i.e. canonical
     * decomposition, followed by canonical composition.
     *
     * @param s1 The first string
     * @param s2 The second string
     * @return <code>true</code> if the normalized forms of the strings are equal, <code>false</code>, otherwise
     */
    public static boolean equalsNormalized(String s1, String s2) {
        if (null == s1) {
            return null == s2;
        }
        if (null == s2) {
            return false;
        }
        return Normalizer.normalize(s1, Form.NFC).equals(Normalizer.normalize(s2, Form.NFC));
    }

    /**
     * Gets a value indicating whether the supplied strings are equal ignoring case, using their {@link Form#NFC} normalization from, i.e.
     * canonical decomposition, followed by canonical composition.
     *
     * @param s1 The first string
     * @param s2 The second string
     * @return <code>true</code> if the normalized forms of the strings are equal ignoring case, <code>false</code>, otherwise
     */
    public static boolean equalsNormalizedIgnoreCase(String s1, String s2) {
        if (null == s1) {
            return null == s2;
        }
        if (null == s2) {
            return false;
        }
        return Normalizer.normalize(s1, Form.NFC).equalsIgnoreCase(Normalizer.normalize(s2, Form.NFC));
    }

    /**
     * Fast check if passed string is numeric.
     * <p>
     * <b>Note</b>: Does no honor possible overflow error; e.g. in case parsed as <code>int</code> value
     *
     * @param str The string to check
     * @return <code>true</code> if string is numeric; otherwise <code>false</code>
     */
    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    /**
     * Fast parsing to an integer
     *
     * @param s The string to parse
     * @return The <code>int</code> value
     * @throws NumberFormatException If string appears not be an integer
     */
    public static int parseInt(final String s) {
        if (s == null) {
            throw new NumberFormatException("Null string");
        }

        // Check for a sign.
        final int len = s.length();
        if (len <= 0) {
            throw new NumberFormatException("Empty string");
        }

        int num = 0;
        int sign = -1;
        final char ch = s.charAt(0);
        if (ch == '-') {
            if (len == 1) {
                throw new NumberFormatException("Missing digits:  " + s);
            }
            sign = 1;
        } else {
            final int d = ch - '0';
            if (d < 0 || d > 9) {
                throw new NumberFormatException("Malformed:  " + s);
            }
            num = -d;
        }

        // Build the number.
        final int max = (sign == -1) ? -Integer.MAX_VALUE : Integer.MIN_VALUE;
        final int multmax = max / 10;
        int i = 1;
        while (i < len) {
            int d = s.charAt(i++) - '0';
            if (d < 0 || d > 9) {
                throw new NumberFormatException("Malformed:  " + s);
            }
            if (num < multmax) {
                throw new NumberFormatException("Over/underflow:  " + s);
            }
            num *= 10;
            if (num < (max + d)) {
                throw new NumberFormatException("Over/underflow:  " + s);
            }
            num -= d;
        }

        return sign * num;
    }

    /**
     * Fast parsing to a positive integer
     *
     * @param s The string to parse
     * @return The <code>int</code> value or <code>-1</code> if string appears not be a positive integer
     */
    public static int parsePositiveInt(final String s) {
        if (s == null) {
            return -1;
        }

        // Check for a sign.
        final int len = s.length();
        if (len <= 0) {
            return -1;
        }

        final char ch = s.charAt(0);
        if (ch == '-') {
            return -1;
        }

        int num;
        {
            final int d = ch - '0';
            if (d < 0 || d > 9) {
                return -1;
            }
            num = -d;
        }

        // Build the number.
        final int sign = -1;
        final int max = -Integer.MAX_VALUE;
        final int multmax = max / 10;
        int i = 1;
        while (i < len) {
            final int d = s.charAt(i++) - '0';
            if (d < 0 || d > 9) {
                return -1;
            }
            if (num < multmax) {
                return -1;
            }
            num *= 10;
            if (num < (max + d)) {
                return -1;
            }
            num -= d;
        }

        return sign * num;
    }

    /**
     * Returns the reverse of the supplied character sequence.
     *
     * @param string The string to reverse
     * @return The reversed string
     * @see StringBuilder#reverse
     */
    public static String reverse(String string) {
        return new StringBuilder(string).reverse().toString();
    }

    /**
     * Removes all leading occurrences of the supplied characters from the given string.
     *
     * @param string The string to trim
     * @param trimChars The characters to remove
     * @return The trimmed string
     */
    public static String trimStart(String string, char... trimChars) {
        if (null != string && null != trimChars && 0 < trimChars.length) {
            while (0 < string.length() && contains(string.charAt(0), trimChars)) {
                string = string.substring(1, string.length() - 1);
            }
        }
        return string;
    }

    /**
     * Removes all trailing occurrences of the supplied characters from the given string.
     *
     * @param string The string to trim
     * @param trimChars The characters to remove
     * @return The trimmed string
     */
    public static String trimEnd(String string, char... trimChars) {
        if (null != string && null != trimChars && 0 < trimChars.length) {
            while (0 < string.length() && contains(string.charAt(string.length() - 1), trimChars)) {
                string = string.substring(0, string.length() - 1);
            }
        }
        return string;
    }

    /**
     * Converts specified wildcard string to a regular expression
     *
     * @param wildcard The wildcard string to convert
     * @return An appropriate regular expression ready for being used in a {@link Pattern pattern}
     */
    public static String wildcardToRegex(String wildcard) {
        final StringBuilder s = new StringBuilder(wildcard.length());
        s.append('^');
        final int len = wildcard.length();
        for (int i = 0; i < len; i++) {
            final char c = wildcard.charAt(i);
            if (c == '*') {
                s.append(".*");
            } else if (c == '?') {
                s.append('.');
            } else if (c == '(' || c == ')' || c == '[' || c == ']' || c == '$' || c == '^' || c == '.' || c == '{' || c == '}' || c == '|' || c == '\\') {
                s.append('\\');
                s.append(c);
            } else {
                s.append(c);
            }
        }
        s.append('$');
        return (s.toString());
    }

    /**
     * Converts int[] to String[]
     *
     * @param arr int[] that should be converted
     * @return String[] ints as String
     */
    public static String[] convert(final int[] arr) {
        String connector = ";";
        StringBuilder builder = new StringBuilder();
        Strings.join(arr, connector, builder);
        return builder.toString().split(connector);
    }

    private static boolean contains(char c, char[] charArray) {
        for (char character : charArray) {
            if (c == character) {
                return true;
            }
        }
        return false;

    }

    /**
     * Omits leading and trailing whitespaces in given <code>StringBuilder</code> instance.
     *
     * @param sb The <code>StringBuilder</code> instance
     * @return The <code>StringBuilder</code> instance with leading and trailing whitespaces omitted
     */
    public static StringBuilder trim(StringBuilder sb) {
        if (null == sb) {
            return null;
        }

        int len = sb.length();
        int st = 0;

        while ((st < len) && (sb.charAt(st) <= ' ')) {
            st++;
        }
        while ((st < len) && (sb.charAt(len - 1) <= ' ')) {
            len--;
        }

        if ((st > 0) || (len < sb.length())) {
            for (int i = sb.length(); i > len; i--) {
                sb.deleteCharAt(i - 1);
            }
            for (int i = st; i-- > 0;) {
                sb.deleteCharAt(0);
            }
        }

        return sb;
    }

    /**
     *
     * @param array
     * @return
     */
    public static String toWhitespaceSeparatedList(String[] array) {
        StringBuilder sb = new StringBuilder();
        if (null != array && array.length > 0) {
            for (String s : array) {
                sb.append(s).append(" ");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     *
     * @param array
     * @return
     */
    public static String toCommaSeparatedList(String[] array) {
        StringBuilder sb = new StringBuilder();
        if (null != array && array.length > 0) {
            for (String s : array) {
                sb.append(s).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

}
