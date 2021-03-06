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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import com.openexchange.java.util.Tools;

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
     * Checks if given string starts with specified prefix
     *
     * @param s The string to check
     * @param prefix The prefix
     * @return <code>true</code> if given string starts with specified prefix; otherwise <code>false</code>
     */
    public static boolean startsWithAny(String s, String prefix) {
        if (null == s) {
            return false;
        }
        return null != prefix && s.startsWith(prefix, 0) ? true : false;
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
     * Checks if given string contains sequence
     *
     * @param s The string to check
     * @param sequence The sequence that might be contained
     * @return <code>true</code> if given string contains specified sequence; otherwise <code>false</code>
     */
    public static boolean containsAny(String s, String sequence) {
        return null != s && null != sequence && s.indexOf(sequence, 0) >= 0 ? true : false;
    }

    /**
     * Checks if given string contains any of specified sequences
     *
     * @param s The string to check
     * @param sequences The sequences that might be contained
     * @return <code>true</code> if given string contains any of specified sequences; otherwise <code>false</code>
     */
    public static boolean containsAny(String s, String... sequences) {
        if (null == s) {
            return false;
        }
        for (int i = sequences.length; i-- > 0;) {
            String sequence = sequences[i];
            if (null != sequence && s.indexOf(sequence, 0) >= 0) {
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
     * @param c The character to check
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
     * Gets the character for given digit (between 0 inclusive and 10 exclusive).
     *
     * @param digit The digit
     * @return The digit's character representation
     */
    public static char charForDigit(int digit) {
        switch (digit) {
            case 0:
                return '0';
            case 1:
                return '1';
            case 2:
                return '2';
            case 3:
                return '3';
            case 4:
                return '4';
            case 5:
                return '5';
            case 6:
                return '6';
            case 7:
                return '7';
            case 8:
                return '8';
            case 9:
                return '9';
            default:
                throw new IllegalArgumentException("Digit needs to be between 0 (inclusive) and 10 (exclusive)");
        }
    }

    /**
     * Gets the digit for given character (between '0' inclusive and '9' inclusive).
     *
     * @param c The character
     * @return The characters's digit representation or <code>-1</code>
     */
    public static int digitForChar(char c) {
        switch (c) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            default:
                return -1;
        }
    }

    /**
     * High speed test for ASCII numbers!
     *
     * @param c The character to check
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
     * High speed test for punctuation character!
     *
     * @param c The character to check
     * @return <code>true</code> if the indicated character is a punctuation; otherwise <code>false</code>
     */
    public static boolean isPunctuation(char c) {
        switch (c) {
            case '!':
            case '"':
            case '#':
            case '$':
            case '%':
            case '&':
            case '\'':
            case '(':
            case ')':
            case '*':
            case '+':
            case ',':
            case '-':
            case '.':
            case '/':
            case ':':
            case ';':
            case '<':
            case '=':
            case '>':
            case '?':
            case '@':
            case '[':
            case ']':
            case '\\':
            case '^':
            case '_':
            case 96:
            case 180:
            case '{':
            case '|':
            case '}':
            case '~':
                return true;
            default:
                return false;
        }
    }

    /**
     * High speed test for ASCII letter!
     *
     * @param c The character to check
     * @return <code>true</code> if the indicated character is an ASCII letter; otherwise <code>false</code>
     */
    public static boolean isAsciiLetter(final char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    /**
     * High speed test for ASCII letter or digit!
     *
     * @param c The character to check
     * @return <code>true</code> if the indicated character is an ASCII letter or digit; otherwise <code>false</code>
     */
    public static boolean isAsciiLetterOrDigit(final char c) {
        return isDigit(c) || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    /**
     * High speed test for hex character.
     *
     * @param c The character to check
     * @return <code>true</code> if the indicated character is hex character; otherwise <code>false</code>
     */
    public static boolean isHex(char c) {
        return isHex(c, true);
    }

    /**
     * High speed test for hex character.
     *
     * @param c The character to check
     * @return <code>true</code> if the indicated character is hex character; otherwise <code>false</code>
     */
    public static boolean isHex(char c, boolean considerUpperCase) {
        if (considerUpperCase) {
            return isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
        }
        return isDigit(c) || (c >= 'a' && c <= 'f');
    }

    /**
     * Gets specified string's ASCII bytes
     *
     * @param cs The string
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

    // private static final Pattern P_SPLIT_COMMA = Pattern.compile("\\s*,\\s*");

    /**
     * Splits given string by comma separator.
     *
     * @param s The string to split
     * @return The split string
     */
    public static String[] splitByComma(final String s) {
        return splitBy(s, ',', true);
    }

    /**
     * Splits given string by comma separator and adds the results to the passed collection.
     *
     * @param s The string to split
     * @param matchList The collection to add the strings to
     * @return The passed collection reference, with the strings added
     */
    public static <C extends Collection<String>> C splitByComma(String s, C matchList) {
        return splitBy(s, ',', true, matchList);
    }

    // private static final Pattern P_SPLIT_COLON = Pattern.compile("\\s*\\:\\s*");

    /**
     * Splits given string by colon separator.
     *
     * @param s The string to split
     * @return The split string
     */
    public static String[] splitByColon(final String s) {
        return splitBy(s, ':', true);
    }

    /**
     * Splits given string by semi-colon separator.
     *
     * @param s The string to split
     * @return The split string
     */
    public static String[] splitBySemiColon(final String s) {
        return splitBy(s, ';', true);
    }

    // private static final Pattern P_SPLIT_DOT = Pattern.compile("\\s*\\.\\s*");

    /**
     * Splits given string by dots.
     *
     * @param s The string to split
     * @return The split string
     */
    public static String[] splitByDots(final String s) {
        return splitBy(s, '.', true);
    }

    /**
     * Splits given string by specified character.
     *
     * @param s The string to split
     * @param delim The delimiter to split by
     * @param trimMatches <code>true</code> to trim tokens; otherwise <code>false</code>
     * @return The split string
     */
    public static String[] splitBy(String s, char delim, boolean trimMatches) {
        if (null == s) {
            return null;
        }
        int length = s.length();
        if (length == 0) {
            return new String[] { trimMatches ? s.trim() : s };
        }

        int pos = s.indexOf(delim, 0);
        if (pos < 0) {
            return new String[] { trimMatches ? s.trim() : s };
        }

        List<String> matchList = splitBy(s, delim, trimMatches, new ArrayList<String>());
        return matchList.toArray(new String[matchList.size()]);
    }

    /**
     * Splits given string by specified character and adds the results to the passed collection.
     *
     * @param s The string to split
     * @param delim The delimiter to split by
     * @param trimMatches <code>true</code> to trim tokens; otherwise <code>false</code>
     * @param matchList The collection to add the strings to
     * @return The passed collection reference, with the strings added
     */
    public static <C extends Collection<String>> C splitBy(String s, char delim, boolean trimMatches, C matchList) {
        if (null == s || null == matchList) {
            return matchList;
        }
        int length = s.length();
        if (length == 0) {
            matchList.add(trimMatches ? s.trim() : s);
            return matchList;
        }
        int pos = s.indexOf(delim, 0);
        if (pos < 0) {
            matchList.add(trimMatches ? s.trim() : s);
            return matchList;
        }

        int prevPos = 0;
        do {
            matchList.add(trimMatches ? s.substring(prevPos, pos).trim() : s.substring(prevPos, pos));
            prevPos = pos + 1;
            pos = prevPos < length ? s.indexOf(delim, prevPos) : -1;
        } while (pos >= 0);
        if (prevPos <= length) {
            matchList.add(trimMatches ? s.substring(prevPos).trim() : s.substring(prevPos));
        }
        return matchList;
    }

    // private static final Pattern P_SPLIT_AMP = Pattern.compile("&");

    /**
     * Splits given string by ampersands <code>'&'</code>.
     *
     * @param s The string to split
     * @return The split string
     */
    public static String[] splitByAmps(final String s) {
        return splitBy(s, '&', false);
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

    // private static final Pattern P_SPLIT_TAB = Pattern.compile("\t");

    /**
     * Splits given string by tabs.
     *
     * @param s The string to split
     * @return The split string
     */
    public static String[] splitByTab(final String s) {
        return splitBy(s, '\t', false);
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
     * Replaces all occurrences of the specified sequence in string with given replacement character.
     *
     * @param s The string to replace in
     * @param sequence The sequence to replace
     * @param replacement The replacement character
     * @return The string with all occurrences replaced
     */
    @SuppressWarnings("null")
    public static String replaceSequenceWith(String s, String sequence, char replacement) {
        if ((null == s) || (null == sequence)) {
            return s;
        }

        int length = s.length();
        StringBuilder sb = null;
        int pos = 0;
        int prev = 0;
        while (prev < length && (pos = s.indexOf(sequence, prev)) >= 0) {
            if (null == sb) {
                sb = new StringBuilder(length);
                if (pos > 0) {
                    sb.append(s, 0, pos);
                }
            } else {
                sb.append(s, prev, pos);
            }
            sb.append(replacement);
            prev = pos + sequence.length();
        }

        if (prev > 0) {
            sb.append(s.substring(prev));
        }
        return null == sb ? s : sb.toString();
    }

    /**
     * Replaces all occurrences of the specified sequence in string with given replacement.
     *
     * @param s The string to replace in
     * @param sequence The sequence to replace
     * @param replacement The replacement
     * @return The string with all occurrences replaced
     */
    @SuppressWarnings("null")
    public static String replaceSequenceWith(String s, String sequence, String replacement) {
        if ((null == s) || (null == sequence) || (null == replacement)) {
            return s;
        }

        int length = s.length();
        StringBuilder sb = null;
        int pos = 0;
        int prev = 0;
        while (prev < length && (pos = s.indexOf(sequence, prev)) >= 0) {
            if (null == sb) {
                sb = new StringBuilder(length);
                if (pos > 0) {
                    sb.append(s, 0, pos);
                }
            } else {
                sb.append(s, prev, pos);
            }
            sb.append(replacement);
            prev = pos + sequence.length();
        }

        if (prev > 0) {
            sb.append(s.substring(prev));
        }
        return null == sb ? s : sb.toString();
    }

    /**
     * Replaces whitespaces in given string with specified <code>replacement</code>.
     *
     * @param s The string replacement
     * @param replacement The string replacement
     * @return The replaced string
     */
    public static String replaceWhitespacesWith(String s, String replacement) {
        if (null == s) {
            return null;
        }

        int length = s.length();
        String repl = replacement == null ? "" : replacement;
        boolean prevWs = false;
        StringBuilder sb = null;
        for (int i = 0; i < length; i++) {
            char ch = s.charAt(i);
            if (isWhitespace(ch)) {
                if (sb == null) {
                    sb = new StringBuilder(length);
                    if (i > 0) {
                        sb.append(s, 0, i);
                    }
                }
                if (!prevWs) {
                    sb.append(repl);
                    prevWs = true;
                }
            } else {
                if (sb != null) {
                    sb.append(ch);
                    prevWs = false;
                }
            }
        }
        return sb == null ? s : sb.toString();
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
     * Checks for empty strings.
     *
     * @param strings The strings
     * @return <code>true</code> if all input strings are <code>null</code>, empty or only consists of white-space characters; else <code>false</code>
     */
    public static boolean isEmpty(final String... strings) {
        for (String string : strings) {
            if (!isEmpty(string)) {
                return false;
            }
        }
        return true;
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
     * Checks for a non-empty strings.
     *
     * @param strings The strings
     * @return <code>true</code> if all input strings are non-empty strings; else <code>false</code>
     */
    public static boolean isNotEmpty(final String... strings) {
        for (String string : strings) {
            if (isEmpty(string)) {
                return false;
            }
        }
        return true;
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
        } catch (UnsupportedCharsetException e) {
            return s;
        }
    }

    /* In a holder class to defer initialization until needed. */
    private static class Holder {

        static final CharsetDecoder UTF8_CHARSET_DECODER;
        static {
            final CharsetDecoder utf8Decoder = Charsets.UTF_8.newDecoder();
            utf8Decoder.onMalformedInput(CodingErrorAction.REPORT);
            utf8Decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
            UTF8_CHARSET_DECODER = utf8Decoder;
        }
    }

    /**
     * Checks given bytes for valid UTF-8 bytes.
     *
     * @param bytes The bytes to check
     * @return <code>true</code> for valid UTF-8 bytes; otherwise <code>false</code>
     */
    public static boolean isUTF8Bytes(final byte[] bytes) {
        try {
            Holder.UTF8_CHARSET_DECODER.decode(ByteBuffer.wrap(bytes));
            return true;
        } catch (CharacterCodingException e) {
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

        int size = coll.size();
        if (size == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder(size << 4);
        Iterator<? extends Object> it = coll.iterator();
        {
            Object obj = it.next();
            builder.append(obj == null ? "null" : obj.toString());
        }
        while (it.hasNext()) {
            Object obj = it.next();
            builder.append(connector).append(obj == null ? "null" : obj.toString());
        }
        return builder.toString();
    }

    /**
     * Joins a collection of objects by connecting the results of their #toString() method with a connector.
     * Can be <code>null</code> if collection == null or empty string if collection is empty
     *
     * @param coll Collection to be connected
     * @param connector Connector place between two objects
     * @param builder The string builder to use
     */
    public static void join(final Collection<? extends Object> coll, final String connector, final StringBuilder builder) {
        if (coll == null) {
            return;
        }

        int size = coll.size();
        if (size == 0) {
            return;
        }

        Iterator<? extends Object> it = coll.iterator();
        {
            Object obj = it.next();
            builder.append(obj == null ? "null" : obj.toString());
        }
        while (it.hasNext()) {
            Object obj = it.next();
            builder.append(connector).append(obj == null ? "null" : obj.toString());
        }
    }

    /**
     * Joins an array of integers by connecting their String representations with a connector.
     * Can be <code>null</code> if collection == null or empty string if collection is empty
     *
     * @param arr Integers to be connected
     * @param connector Connector place between two objects
     * @param builder The string builder to use
     */
    public static void join(final int[] arr, final String connector, final StringBuilder builder) {
        final List<Integer> list = new LinkedList<Integer>();
        for (final int i : arr) {
            list.add(Autoboxing.I(i));
        }
        join(list, connector, builder);
    }

    /**
     * Joins the specified array of {@link T} objects with the specified <code>connector</code>
     * starting from <code>beginIndex</code> and going until the <code>endIndex</code> inclusively.
     * If the <code>endIndex</code> lies outside the <code>array</code> length, then the array's length
     * will be used as an <code>endIndex</code>.
     *
     * @param array The elements of the array to join
     * @param connector The connector string
     * @param beginIndex The begin index
     * @param endIndex The end index
     * @return The joined String or <code>null</code> if an empty or <code>null</code> array is provided.
     * @throws IllegalArgumentException if the begin index lies after the end index.
     */
    public static <T> String join(T[] array, String connector, int beginIndex, int endIndex) {
        if (array == null || array.length == 0) {
            return null;
        }
        if (endIndex - beginIndex < 0) {
            throw new IllegalArgumentException("The begin index cannot lie after the end index (" + beginIndex + ">" + endIndex + ")");
        }
        if (endIndex > array.length) {
            endIndex = array.length;
        }
        StringBuilder builder = new StringBuilder(array.length << 4);
        int index = beginIndex;
        builder.append(array[index++]);
        while (index < endIndex) {
            builder.append(connector).append(array[index++]);
        }
        return builder.toString();
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
        for (byte i : arr) {
            list.add(Byte.valueOf(i));
        }
        return join(list, connector);
    }

    private static final byte[][] BYTE_ORDER_MARKS = new byte[][] { new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF }, new byte[] { (byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x0 }, new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF }, new byte[] { (byte) 0xFE, (byte) 0xFF }, new byte[] { (byte) 0xFE, (byte) 0xFF } };
    /**
     * Removes byte order marks from UTF8 strings.
     *
     * @param str The string to remove byte marks on
     * @return new instance of trimmed string - or reference to old one if unchanged
     */
    public static String trimBOM(final String str) {
        final byte[] bytes = str.getBytes();
        for (final byte[] bom : BYTE_ORDER_MARKS) {
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
        return concat(Character.valueOf('"'), s, Character.valueOf('"'));
    }

    /**
     * Puts double quotes around a string, optionally escaping quotes within the string.
     *
     * @param s The string to quote.
     * @param escape <code>true</code> to escape quotes within the given string prior quoting, <code>false</code>, otherwise
     * @return The quoted string.
     */
    public static String quote(final String s, boolean escape) {
        String value = escape && null != s && s.contains("\"") ? s.replaceAll("\"", "\\\\\"") : s;
        return concat(Character.valueOf('"'), value, Character.valueOf('"'));
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
     * @param c The character to remove
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
     * Generates a string of code points for given string and print it
     * either to the given stream or to {@link System#out}
     *
     * @param str The string
     * @param out The print stream to print to
     */
    public static void outCodePoints(final String str, final PrintStream out) {
        if (null == out) {
            System.out.println(toCodePoints(str));
        } else {
            out.println(toCodePoints(str));
        }
    }

    /**
     * ASCII-wise to upper-case
     *
     * @param chars The {@link CharSequence} to transform
     * @return A new String with upper case characters
     */
    public static String toUpperCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }

        int length = chars.length();
        StringBuilder builder = null;
        for (int i = 0; i < length; i++) {
            char c = chars.charAt(i);
            if (null == builder) {
                if ((c >= 'a') && (c <= 'z')) {
                    builder = new StringBuilder(length);
                    if (i > 0) {
                        builder.append(chars, 0, i);
                    }
                    builder.append((char) (c & 0x5f));
                }
            } else {
                builder.append((c >= 'a') && (c <= 'z') ? (char) (c & 0x5f) : c);
            }
        }
        return null == builder ? chars.toString() : builder.toString();
    }

    /**
     * ASCII-wise to lower-case
     *
     * @param chars The {@link CharSequence} to transform
     * @return A new String with lower case characters
     */
    public static String toLowerCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }

        int length = chars.length();
        StringBuilder builder = null;
        for (int i = 0; i < length; i++) {
            char c = chars.charAt(i);
            if (null == builder) {
                if ((c >= 'A') && (c <= 'Z')) {
                    builder = new StringBuilder(length);
                    if (i > 0) {
                        builder.append(chars, 0, i);
                    }
                    builder.append((char) (c ^ 0x20));
                }
            } else {
                builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
            }
        }
        return null == builder ? chars.toString() : builder.toString();
    }

    // @formatter:off
    private static char[] lowercases = {
        '\000', '\001', '\002', '\003', '\004', '\005', '\006', '\007', '\010', '\011', '\012', '\013', '\014', '\015', '\016', '\017',
        '\020', '\021', '\022', '\023', '\024', '\025', '\026', '\027', '\030', '\031', '\032', '\033', '\034', '\035', '\036', '\037',
        '\040', '\041', '\042', '\043', '\044', '\045', '\046', '\047', '\050', '\051', '\052', '\053', '\054', '\055', '\056', '\057',
        '\060', '\061', '\062', '\063', '\064', '\065', '\066', '\067', '\070', '\071', '\072', '\073', '\074', '\075', '\076', '\077',
        '\100', '\141', '\142', '\143', '\144', '\145', '\146', '\147', '\150', '\151', '\152', '\153', '\154', '\155', '\156', '\157',
        '\160', '\161', '\162', '\163', '\164', '\165', '\166', '\167', '\170', '\171', '\172', '\133', '\134', '\135', '\136', '\137',
        '\140', '\141', '\142', '\143', '\144', '\145', '\146', '\147', '\150', '\151', '\152', '\153', '\154', '\155', '\156', '\157',
        '\160', '\161', '\162', '\163', '\164', '\165', '\166', '\167', '\170', '\171', '\172', '\173', '\174', '\175', '\176', '\177' };
    // @formatter:on

    /**
     * Fast lower-case conversion.
     *
     * @param s The string
     * @return The lower-case string
     */
    @SuppressWarnings("null")
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
     * Accepts a string of separated values, splits it around matches of the given {@link java.util.regex.Pattern regular expression}, trims
     * the split values and returns them as an array.
     * <p>
     * <div style="background-color:#FFDDDD; padding:6px; margin:0px;">
     * <b>Note</b>: The separator is interpreted a regular expression. Please consider {@link java.util.regex.Pattern#quote(String) quoting}
     * in case separator should be interpreted as a literal pattern or use the {@link #splitBy(String, char, boolean) splitBy() method}
     * </div>
     *
     * @param input The string of separated values
     * @param separator The separator as a regular expression used to split the input around this separator
     * @return The split and trimmed input as an array
     * @throws IllegalArgumentException If input or the separator are missing or if the separator isn't a valid pattern
     * @see #splitBy(String, char, boolean)
     */
    public static String[] trimAndSplit(String input, String separator) {
        if (input == null) {
            throw new IllegalArgumentException("Missing input");
        }
        if (Strings.isEmpty(input)) {
            return new String[0];
        }
        if (Strings.isEmpty(separator)) {
            throw new IllegalArgumentException("Missing separator");
        }

        try {
            return input.split(separator);
        } catch (PatternSyntaxException pse) {
            throw new IllegalArgumentException("Illegal pattern syntax", pse);
        }
    }

    /**
     * Accepts a string of separated values, splits it around matches of the given {@link java.util.regex.Pattern regular expression}, trims
     * the split values and returns them as a list.
     * <p>
     * <div style="background-color:#FFDDDD; padding:6px; margin:0px;">
     * <b>Note</b>: The separator is interpreted a regular expression. Please consider {@link java.util.regex.Pattern#quote(String) quoting}
     * in case separator should be interpreted as a literal pattern or use the {@link #splitBy(String, char, boolean) splitBy() method}
     * </div>
     *
     * @param input The string of separated values
     * @param separator The separator as a regular expression used to split the input around this separator
     * @return The split and trimmed input as a list or an empty list
     * @throws IllegalArgumentException If input or the separator are missing or if the separator isn't a valid pattern
     * @see #splitBy(String, char, boolean)
     */
    public static List<String> splitAndTrim(String input, String separator) {
        try {
            String[] tokens = trimAndSplit(input, separator);
            if (tokens.length == 0) {
                return Collections.emptyList();
            }
            List<String> trimmedSplits = new ArrayList<String>(tokens.length);
            for (String token : tokens) {
                trimmedSplits.add(token.trim());
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
                string = string.substring(1, string.length());
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
     * Converts specified wild-card string to a regular expression with line boundaries.
     * <p>
     * That is resulting regular expression matches a complete line, not a single word by prepending/appending:
     * <table border="0" cellpadding="1" cellspacing="0">
     * <tr><th>&nbsp;</th></tr>
     * <tr><td valign="top" headers="construct bounds"><tt>^&nbsp;&nbsp;&nbsp;&nbsp;</tt></td>
     * <td headers="matches">The beginning of a line</td></tr>
     * <tr><td valign="top" headers="construct bounds"><tt>$&nbsp;&nbsp;&nbsp;&nbsp;</tt></td>
     * <td headers="matches">The end of a line</td></tr>
     * </table>
     *
     * @param wildcard The wild-card string to convert
     * @return An appropriate regular expression ready for being used in a {@link Pattern pattern}
     */
    public static String wildcardToRegex(String wildcard) {
        return wildcardToRegex(wildcard, true);
    }

    /**
     * Converts specified wild-card string to a regular expression.
     * <p>
     * If argument <code>withLineBoundaries</code> is set to <code>true</code>, the resulting regular expression matches a complete line,
     * not a single word by prepending/appending:
     * <table border="0" cellpadding="1" cellspacing="0">
     * <tr><th>&nbsp;</th></tr>
     * <tr><td valign="top" headers="construct bounds"><tt>^&nbsp;&nbsp;&nbsp;&nbsp;</tt></td>
     * <td headers="matches">The beginning of a line</td></tr>
     * <tr><td valign="top" headers="construct bounds"><tt>$&nbsp;&nbsp;&nbsp;&nbsp;</tt></td>
     * <td headers="matches">The end of a line</td></tr>
     * </table>
     *
     * @param wildcard The wild-card string to convert
     * @param withLineBoundaries <code>true</code> to add line boundaries (beginning and end of a line) to resulting regular expression; otherwise <code>false</code> to not add them
     * @return An appropriate regular expression ready for being used in a {@link Pattern pattern}
     */
    public static String wildcardToRegex(String wildcard, boolean withLineBoundaries) {
        if (wildcard == null) {
            return null;
        }

        StringBuilder s = withLineBoundaries ? initForWildcard2Regex(0, wildcard, true) : null;
        int len = wildcard.length();
        for (int i = 0; i < len; i++) {
            char c = wildcard.charAt(i);
            switch (c) {
                case '*':
                    if (s == null) {
                        s = initForWildcard2Regex(i, wildcard, withLineBoundaries);
                    }
                    s.append(".*");
                    break;
                case '?':
                    if (s == null) {
                        s = initForWildcard2Regex(i, wildcard, withLineBoundaries);
                    }
                    s.append('.');
                    break;
                case '(':
                case ')':
                case '[':
                case ']':
                case '$':
                case '^':
                case '.':
                case '{':
                case '}':
                case '|':
                case '\\':
                    if (s == null) {
                        s = initForWildcard2Regex(i, wildcard, withLineBoundaries);
                    }
                    s.append('\\');
                    s.append(c);
                    break;
                default:
                    if (s != null) {
                        s.append(c);
                    }
                    break;
            }
        }
        if (s != null) {
            if (withLineBoundaries) {
                s.append('$');
            }
            return s.toString();
        }
        return wildcard; // Return as-is
    }

    private static StringBuilder initForWildcard2Regex(int index, String wildcard, boolean withLineBoundaries) {
        StringBuilder s = new StringBuilder(wildcard.length() << 1);
        if (withLineBoundaries) {
            s.append('^');
        }
        if (index > 0) {
            s.append(wildcard, 0, index);
        }
        return s;
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

        // First delete trailing, then leading ones
        if (len < sb.length()) {
            sb.delete(len, sb.length());
        }
        if (st > 0) {
            sb.delete(0, st);
        }

        return sb;
    }

    /**
     * Generates a whitespace-separated string from given string array.
     *
     * @param strings The strings to concatenate
     * @return The resulting string
     */
    public static String toWhitespaceSeparatedList(String[] strings) {
        return toDelimiterSeparatedList(strings, ' ');
    }

    /**
     * Generates a comma-separated string from given string array.
     *
     * @param strings The strings to concatenate
     * @return The resulting string
     */
    public static String toCommaSeparatedList(String[] strings) {
        return toDelimiterSeparatedList(strings, ',');
    }

    /**
     * Generates a delimiter-separated string from given string array.
     *
     * @param strings The strings to concatenate
     * @param delim The delimiter to use
     * @return The resulting string
     */
    public static String toDelimiterSeparatedList(String[] strings, char delim) {
        if (null == strings || strings.length <= 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(strings.length << 3);
        sb.append(strings[0]);
        for (int i = 1; i < strings.length; i++) {
            sb.append(delim).append(strings[i]);
        }
        return sb.toString();
    }

    /**
     * Parses a positive <code>int</code> value from passed {@link String} instance.
     *
     * @param s The string to parse
     * @return The parsed positive <code>int</code> value or <code>-1</code> if parsing failed
     */
    public static final int parseUnsignedInt(String s) {
        return Tools.getUnsignedInteger(s);
    }

    /**
     * Parses a positive <code>int</code> value from passed {@link String} instance.
     *
     * @param s The string to parse
     * @return The parsed positive <code>int</code> value or <code>-1</code> if parsing failed
     */
    public static final int getUnsignedInt(String s) {
        return Tools.getUnsignedInteger(s);
    }

    /**
     * Parses a positive <code>long</code> value from passed {@link String} instance.
     *
     * @param s The string to parse
     * @return The parsed positive <code>long</code> value or <code>-1</code> if parsing failed
     */
    public static final long parseUnsignedLong(String s) {
        return Tools.getUnsignedLong(s);
    }

    /**
     * Parses a positive <code>long</code> value from passed {@link String} instance.
     *
     * @param s The string to parse
     * @return The parsed positive <code>long</code> value or <code>-1</code> if parsing failed
     */
    public static final long getUnsignedLong(String s) {
        return Tools.getUnsignedLong(s);
    }

    /**
     * Checks if specified {@link String} instance contains a surrogate pair (aka astral character),
     * which reside in the range of 65,535 (0xFFFF) to 1,114,111 (0x10FFFF) of the Unicode characters spectrum.
     *
     * @param str The string to check
     * @return <code>true</code> if string contains a surrogate pair; otherwise <code>false</code>
     */
    public static boolean containsSurrogatePairs(String str) {
        if (null == str) {
            return false;
        }

        int len = str.length();
        for (int i = 1; i < len; i++) {
            if (Character.isSurrogatePair(str.charAt(i - 1), str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Replaces possible surrogate pairs (aka astral characters) in specified {@link String} instance with given character.
     *
     * @param str The string to replace in
     * @param newChar The character to use to replace surrogate pairs
     * @return The resulting string
     */
    public static String replaceSurrogatePairs(String str, char newChar) {
        if (null == str) {
            return str;
        }

        String s = Normalizer.normalize(str, Form.NFC);
        StringBuilder sb = null;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            if (i + 1 < len) {
                char nc = s.charAt(i + 1);
                if (Character.isSurrogatePair(ch, nc)) {
                    i++;
                    if (null == sb) {
                        sb = new StringBuilder(len);
                        if (i - 1 > 0) {
                            sb.append(s, 0, i - 1);
                        }
                    }
                    sb.append(newChar);
                } else {
                    if (null != sb) {
                        sb.append(ch);
                    }
                }
            } else {
                if (null != sb) {
                    sb.append(ch);
                }
            }
        }

        return null == sb ? s : sb.toString();
    }

    private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * Turns array of bytes into string representing each byte as unsigned hex number.
     *
     * @param hash Array of bytes to convert to hex-string
     * @return Generated hex string
     */
    public static char[] asHexChars(byte[] hash) {
        if (hash == null) {
            return null;
        }
        int length = hash.length;
        if (length <= 0) {
            return new char[0];
        }
        char[] buf = new char[length << 1];
        for (int i = 0, x = 0; i < length; i++) {
            buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
            buf[x++] = HEX_CHARS[hash[i] & 0xf];
        }
        return buf;
    }

    /**
     * Turns array of bytes into string representing each byte as unsigned hex number.
     *
     * @param hash Array of bytes to convert to hex-string
     * @return Generated hex string
     */
    public static String asHex(byte[] hash) {
        char[] chars = asHexChars(hash);
        return chars == null ? null : new String(chars);
    }

    /**
     * Converts the specified byte count to its counterpart human readable format.
     *
     * @param bytes The amount of bytes to convert
     * @param si Whether the SI notation will be used.
     * @return The human readable format
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", Double.valueOf(bytes / Math.pow(unit, exp)), pre);
    }

    /**
     * Checks whether the specified string's characters are <b>not</b> only ASCII 7 bit
     *
     * @param s The string to check
     * @return <code>true</code> if string's characters are <b>not</b> only ASCII 7 bit; otherwise <code>false</code>
     * @throws IllegalArgumentException If given string is <code>null</code>
     */
    public static boolean isNotAscii(final String s) {
        return isAscii(s) == false;
    }

    /**
     * Checks whether the specified string's characters are ASCII 7 bit
     *
     * @param s The string to check
     * @return <code>true</code> if string's characters are ASCII 7 bit; otherwise <code>false</code>
     * @throws IllegalArgumentException If given string is <code>null</code>
     */
    public static boolean isAscii(final String s) {
        if (s == null) {
            throw new IllegalArgumentException("String must not be null");
        }
        final int length = s.length();
        boolean isAscci = true;
        for (int i = 0; isAscci && (i < length); i++) {
            isAscci = (s.charAt(i) < 128);
        }
        return isAscci;
    }

}
