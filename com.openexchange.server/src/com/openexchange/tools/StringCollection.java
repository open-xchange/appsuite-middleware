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

package com.openexchange.tools;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import com.openexchange.groupware.calendar.old.CalendarCollectionFieldMapper;
import com.openexchange.java.AllocatingStringWriter;

/**
 * {@link StringCollection} - Provides useful string utility methods mainly for SQL.
 *
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class StringCollection {

    static final byte[] DSO = "\\".getBytes();

    static final byte[] DSOR = "\\\\".getBytes();

    static final byte[] DAP = "'".getBytes();

    static final byte[] DAPR = "\\'".getBytes();

    private StringCollection() {
        super();
    }

    /**
     * Prepares specified string for being used in a prepared statement's WHERE clause as a search pattern. <b>Note</b>: This methods
     * assumes that a <code>java.sql.PreparedStatement</code> is going to be used thus "'" (single-quote) and "\" (backslash) are not
     * escaped since this is automatically done by <code>java.sql.PreparedStatement.setString()</code> method.
     * <ul>
     * <li>Any contained SQL wildcard characters (<code>'%'</code> and <code>'_'</code>) are escaped.</li>
     * <li>Wildcard characters <code>'*'</code> and <code>'?'</code> are replaced with corresponding SQL wildcard characters
     * <code>'%'</code> and <code>'_'</code>.</li>
     * <li>If pattern does not start with/end with SQL wildcard character, <code>'%'</code> character is prepended/appended to pattern.</li>
     * </ul>
     * <p>
     * E.g.: <code>"Foo%Bar*xxx?Hoo"</code> =&gt; <code>"Foo\%Bar%xxx_Hoo"</code>
     *
     * @param s The string to be prepared for SQL search
     * @return A prepared search string for being used in <code>java.sql.PreparedStatement.setString()</code>
     */
    public static String prepareForSearch(final String s) {
        return prepareForSearch(s, true);
    }

    /**
     * Prepares specified string for being used in a prepared statement's WHERE clause as a search pattern. <b>Note</b>: This methods
     * assumes that a <code>java.sql.PreparedStatement</code> is going to be used thus "'" (single-quote) and "\" (backslash) are not
     * escaped since this is automatically done by <code>java.sql.PreparedStatement.setString()</code> method.
     * <ul>
     * <li>Any contained SQL wildcard characters (<code>'%'</code> and <code>
     * '_'</code>) are escaped.</li>
     * <li>Wildcard characters <code>'*'</code> and <code>'?'</code> are replaced with corresponding SQL wildcard characters
     * <code>'%'</code> and <code>'_'</code>.</li>
     * </ul>
     * <p>
     * E.g.: <code>"Foo%Bar*xxx?Hoo"</code> =&gt; <code>"Foo\%Bar%xxx_Hoo"</code>
     *
     * @param s The string to be prepared for SQL search
     * @param surroundWithWildcard <code>true</code> to prepend/append <code>'%'</code> character, if pattern does not start with/end with
     *            SQL wildcard character; otherwise <code>false</code>
     * @return A prepared search string for being used in <code>java.sql.PreparedStatement.setString()</code>
     */
    public static String prepareForSearch(final String s, final boolean surroundWithWildcard) {
        return prepareForSearch(s, surroundWithWildcard, true);
    }

    /**
     * Prepares specified string for being used in a statement's WHERE clause as a search pattern.
     * <ul>
     * <li>Any contained SQL wildcard characters (<code>'%'</code> and <code>
     * '_'</code>) are escaped.</li>
     * <li>Wildcard characters <code>'*'</code> and <code>'?'</code> are replaced with corresponding SQL wildcard characters
     * <code>'%'</code> and <code>'_'</code>.</li>
     * </ul>
     * <p>
     * E.g.: <code>"Foo%Bar*xxx?Hoo"</code> =&gt; <code>"Foo\%Bar%xxx_Hoo"</code>
     *
     * @param s The string to be prepared for SQL search
     * @param surroundWithWildcard <code>true</code> to prepend/append <code>'%'</code> character, if pattern does not start with/end with
     *            SQL wildcard character; otherwise <code>false</code>
     * @param preparedStatement <code>true</code> if search string is going to be inserted through
     *            <code>java.sql.PreparedStatement.setString()</code> to omit escaping of "'" (single-quote) and "\" (backslash); otherwise
     *            <code>false</code>
     * @return A prepared search string
     */
    public static String prepareForSearch(final String s, final boolean surroundWithWildcard, final boolean preparedStatement) {
        if (s == null) {
            return s;
        }
        String value = s.trim();
        if (!preparedStatement) {
            // Escape every backslash and single-quote character
            value = value.replaceAll("\\\\", quoteReplacement("\\\\")).replaceAll("'", quoteReplacement("\\'"));
        }
        value = value.replace("%", "\\%").replace("_", "\\_").replace('*', '%').replace('?', '_');
        if (surroundWithWildcard) {
            if (value.length() > 0) {
                if (value.charAt(0) != '%') {
                    // Prepend '%' character
                    value = new StringBuilder(value.length() + 1).append('%').append(value).toString();
                }
                final int length = value.length();
                if (value.charAt(length - 1) != '%' || (length > 1 && value.charAt(length - 2) == '\\')) {
                    // Append '%' character
                    value = new StringBuilder(length + 1).append(value).append('%').toString();
                }
            } else {
                value = "%";
            }
        }
        return value;
    }

    /**
     * Prepares specified string for being used in a statement's WHERE clause as a search pattern.
     * <ul>
     * <li>Any contained SQL wildcard characters (<code>'%'</code> and <code>
     * '_'</code>) are escaped.</li>
     * <li>Wildcard characters <code>'*'</code> and <code>'?'</code> are replaced with corresponding SQL wildcard characters
     * <code>'%'</code> and <code>'_'</code>.</li>
     * </ul>
     * <p>
     * E.g.: <code>"Foo%Bar*xxx?Hoo"</code> =&gt; <code>"Foo\%Bar%xxx_Hoo"</code>
     *
     * @param s The string to be prepared for SQL search
     * @param prependWildcard <code>true</code> to prepend <code>'%'</code> character, if pattern does not start with <code>'*'</code>
     *            wildcard character; otherwise <code>false</code>
     * @param appendWildcard <code>true</code> to append <code>'%'</code> character, if pattern does not end with <code>'*'</code> wildcard
     *            character; otherwise <code>false</code>
     * @param preparedStatement <code>true</code> if search string is going to be inserted through
     *            <code>java.sql.PreparedStatement.setString()</code> to omit escaping of "'" (single-quote) and "\" (backslash); otherwise
     *            <code>false</code>
     * @return A prepared search string
     */
    public static String prepareForSearch(final String s, final boolean prependWildcard, final boolean appendWildcard, final boolean preparedStatement) {
        if (s == null) {
            return s;
        }
        String value = s.trim();
        if (!preparedStatement) {
            // Escape every backslash and single-quote character
            value = value.replaceAll("\\\\", quoteReplacement("\\\\")).replaceAll("'", quoteReplacement("\\'"));
        }
        value = value.replace("%", "\\%").replace("_", "\\_").replace('*', '%').replace('?', '_');
        if (prependWildcard) {
            final int length = value.length();
            if (length > 0) {
                if (value.charAt(0) != '%') {
                    // Prepend '%' character
                    value = new StringBuilder(length + 1).append('%').append(value).toString();
                }
            } else {
                value = "%";
            }
        }
        if (appendWildcard) {
            final int length = value.length();
            if (length > 0) {
                if (value.charAt(length - 1) != '%' || (length > 1 && value.charAt(length - 2) == '\\')) {
                    // Append '%' character
                    value = new StringBuilder(length + 1).append(value).append('%').toString();
                }
            } else {
                value = "%";
            }
        }
        return value;
    }

    /**
     * Returns a literal replacement <code>String</code> for the specified <code>String</code>. This method produces a <code>String</code>
     * that will work use as a literal replacement <code>s</code> in the <code>appendReplacement</code> method of the {@link Matcher} class.
     * The <code>String</code> produced will match the sequence of characters in <code>s</code> treated as a literal sequence. Slashes ('\')
     * and dollar signs ('$') will be given no special meaning.
     *
     * @param s The string to be literalized
     * @return A literal string replacement
     */
    public static String quoteReplacement(final String s) {
        StringBuilder sb = null;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '$') {
                if (sb == null) {
                    sb = new StringBuilder(s.length());
                    if (i > 0) {
                        sb.append(s, 0, i);
                    }
                }
                sb.append('\\');
                sb.append(c);
            } else {
                if (sb != null) {
                    sb.append(c);
                }
            }
        }
        return sb == null ? s : sb.toString();
    }

    public static String disarmSQLString(final String s) {
        return new String(replaceGivenBytes(replaceGivenBytes(s.getBytes(com.openexchange.java.Charsets.UTF_8), DSO, DSOR), DAP, DAPR));
    }

    /**
     * public static byte[] replaceGivenBytes(byte b[], byte replace[], byte replacement[])<BR>
     * Replace (replace) with (replacement) in source (b)<BR>
     * <BR>
     *
     * @param byte b[]
     * @param byte replace[]
     * @param byte replacement[]
     * @return byte[]
     */
    public static byte[] replaceGivenBytes(final byte b[], final byte replace[], final byte replacement[]) {
        byte r[] = new byte[(b.length + (replacement.length << 1))];
        int c = 0;
        final int l = replace.length;
        for (int a = 0; a < b.length; a++) {
            boolean found = false;
            int fc = 1;
            if (b[a] == replace[0]) {
                found = true;
                for (int n = 1; n < l; n++) {
                    final int m = a + n;
                    if (m < b.length) {
                        if (b[(a + n)] == replace[n]) {
                            found = true;
                            fc++;
                        } else {
                            found = false;
                        }
                    } else {
                        found = false;
                    }
                }
            }
            if (r.length < (c + replacement.length)) {
                r = expandArray(r, c, (c + replacement.length));
            }
            if (found && fc == replace.length) {
                System.arraycopy(replacement, 0, r, c, replacement.length);
                c = c + replacement.length;
                a = (a + l) - 1;
            } else {
                r[c] = b[a];
                c++;
            }
        }
        r = blurTrim(r, c);
        return r;
    }

    /**
     * public static final String getSqlInString<BR>
     * returns a normal (number based) SQL IN String for subqueries<BR>
     * <BR>
     *
     * @param int arr[]
     * @return SQLInString or null
     */
    public static String getSqlInString(final int arr[]) {
        if (arr == null) {
            return null;
        }
        final int length = arr.length;
        if (length <= 0) {
            return null;
        }
        final StringBuilder sb = new StringBuilder(length << 2);
        sb.append('(');
        sb.append(arr[0]);
        for (int a = 1; a < length; a++) {
            sb.append(',');
            sb.append(arr[a]);
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * returns a SQL IN String containing all the integers in the set
     *
     * @param set
     * @return
     */
    public static String getSqlInString(final Set<Integer> set) {
        if (set == null || set.isEmpty()) {
            return null;
        }
        final int size = set.size();
        final StringBuilder sb = new StringBuilder(size * 5);
        final Integer[] values = set.toArray(new Integer[set.size()]);
        sb.append('(');
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append(',');
                sb.append(values[i]);
            } else {
                sb.append(values[i]);
            }
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * public static final String getSqlInString<BR>
     * returns a normal (number based) SQL IN String for subqueries<BR>
     * <BR>
     *
     * @param Object arr[]
     * @return SQLInString or null
     */
    public static String getSqlInString(final Object arr[]) {
        if (arr == null || arr.length == 0) {
            return null;
        }
        final StringBuilder sb = new StringBuilder(arr.length * 5);
        sb.append('(');
        for (int a = 0; a < arr.length; a++) {
            if (a > 0) {
                sb.append(',');
                sb.append(arr[a]);
            } else {
                sb.append(arr[a]);
            }
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * public static final String getSqlInString<BR>
     * returns a normal (number based) SQL IN String for subqueries<BR>
     * <BR>
     *
     * @param int arr[][]
     * @return SQLInString or null
     */
    public static String getSqlInString(final int arr[][]) {
        final StringBuilder sb = new StringBuilder();
        if (arr.length > 0) {
            sb.append('(');
            for (int a = 0; a < arr.length; a++) {
                if (a > 0) {
                    sb.append(',');
                    sb.append(arr[a][0]);
                } else {
                    sb.append(arr[a][0]);
                }
            }
        } else {
            return null;
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * public static final String getSqlInString<BR>
     * returns a normal (number based) SQL IN String for subqueries<BR>
     * <BR>
     *
     * @param int i
     * @param int arr[]
     * @return SQLInString or null
     */
    public static String getSqlInString(final int i, final int arr[]) {
        if (null == arr) {
            return new StringBuilder(8).append('(').append(i).append(')').toString();
        }
        final int length = arr.length;
        if (0 == length) {
            return new StringBuilder(8).append('(').append(i).append(')').toString();
        }
        final StringBuilder sb = new StringBuilder(length << 1);
        sb.append('(');
        sb.append(i);
        if (length > 0) {
            for (int a = 0; a < length; a++) {
                sb.append(',');
                sb.append(arr[a]);
            }
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * public static final String getSqlInStringFromMap<BR>
     * returns a normal (number based) SQL IN String for subqueries<BR>
     * <BR>
     *
     * @param Map
     * @return SQLInString or null
     */
    public static String getSqlInStringFromMap(final Map<?, ?> m) {
        if (m == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append('(');
        final int size = m.size();
        if (size > 0) {
            final Iterator<?> it = m.keySet().iterator();
            sb.append(it.next().toString());
            for (int k = 1; k < size; k++) {
                sb.append(',');
                sb.append(it.next().toString());
            }
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * public static byte[] blurTrim(byte b[], int c)<BR>
     * Same as String.trim() but should be faster because we know the end (c).<BR>
     * <BR>
     *
     * @param byte b[]
     * @param int c
     * @return byte[]
     */
    public static byte[] blurTrim(final byte b[], final int c) {
        final byte r[] = new byte[c];
        System.arraycopy(b, 0, r, 0, c);
        return r;
    }

    /**
     * public static byte[] expandArray(byte b[], int c, int l)<BR>
     * Expand a byte array.<BR>
     * <BR>
     *
     * @param byte b[]
     * @param int c (last position in b)
     * @param int l (last position in b + replacement.length)
     * @return byte[]
     */
    public static byte[] expandArray(final byte b[], final int c, final int l) {
        final byte r[] = new byte[((b.length + l) << 1)];
        System.arraycopy(b, 0, r, 0, c);
        return r;
    }

    public static String date2SQLString(final Date d) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return (sdf.format(d));
    }

    public static String date2String(final Date d) {
        final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH);
        return (sdf.format(d));
    }

    public static String getSelect(final int[] cols, final String table) {
        final StringBuilder sb = new StringBuilder(256);
        sb.append("SELECT ");
        boolean first = true;
        for (int a = 0; a < cols.length; a++) {
            final String temp = CalendarCollectionFieldMapper.getFieldName(cols[a]);
            if (temp != null) {
                if (first) {
                    sb.append(temp);
                    first = false;
                } else {
                    sb.append(',');
                    sb.append(temp);
                }
            }
        }
        sb.append(" FROM ");
        sb.append(table);
        return sb.toString();
    }

    public static String convertArray2String(final int i[]) {
        if (i == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        for (int a = 0; a < i.length; a++) {
            sb.append(i[a]);
            sb.append(',');
        }

        return sb.delete(sb.length() - 1, sb.length()).toString();
    }

    public static String convertArray2String(final String s[]) {
        if (s == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        for (int a = 0; a < s.length; a++) {
            sb.append(s[a]);
            sb.append(',');
        }

        return sb.delete(sb.length() - 1, sb.length()).toString();
    }

    public static int[] convertStringArray2IntArray(final String s[]) {
        final int[] i = new int[s.length];
        for (int a = 0; a < i.length; a++) {
            i[a] = Integer.parseInt(s[a]);
        }
        return i;
    }

    public static String convertArraytoString(final Object[] o) {
        final StringBuilder sb = new StringBuilder();
        for (int a = 0; a < o.length; a++) {
            sb.append(o[a]);
        }
        return sb.toString();
    }

    public static String getStackAsString() {
        final Throwable t = new Throwable();
        t.fillInStackTrace();
        final AllocatingStringWriter sw = new AllocatingStringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Pattern to check whether a string contains escaped REGEX wildcards or not
     */
    private static final Pattern ESCAPED_REGEX_WILDCARD_PATTERN = Pattern.compile("(([\\\\]+)(\\*|\\?))");

    private static final Pattern REGEX_WILDCARD_PATTERN = Pattern.compile("((\\*|\\?))");

    /**
     * Determines whether the specified string contains any REGEX wildcard characters '*' or '?' that are not escaped
     *
     * @param s The string to check
     * @return true if the specified string contains REGEX wildcards '*' or '?' that are not escaped; false otherwise
     */
    public static boolean containsWildcards(final String s) {
        return REGEX_WILDCARD_PATTERN.matcher(s).find() && !ESCAPED_REGEX_WILDCARD_PATTERN.matcher(s).find();
    }
}
