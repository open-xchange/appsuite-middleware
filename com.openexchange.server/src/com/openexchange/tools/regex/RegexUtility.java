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

package com.openexchange.tools.regex;

import java.util.regex.Pattern;

/**
 * {@link RegexUtility} - Provides simple helper methods to compose regular expressions in an easier way.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RegexUtility {

    /**
     * The regex group type:<br>
     * <table border="1">
     * <tr>
     * <th align="right">None</th>
     * <td align="left">&nbsp;<code>&quot;X&quot;</code></td>
     * </tr>
     * <tr>
     * <th align="right">Capturing</th>
     * <td align="left">&nbsp;<code>&quot;(X)&quot;</code></td>
     * </tr>
     * <tr>
     * <th align="right">Non-capturing</th>
     * <td align="left">&nbsp;<code>&quot;(?:X)&quot;</code></td>
     * </tr>
     * <tr>
     * <th align="right">Zero width positive look-ahead</th>
     * <td align="left">&nbsp;<code>&quot;(?=X)&quot;</code></td>
     * </tr>
     * <tr>
     * <th align="right">Zero width negative look-ahead</th>
     * <td align="left">&nbsp;<code>&quot;(?!X)&quot;</code></td>
     * </tr>
     * <tr>
     * <th align="right">Zero width positive look-behind</th>
     * <td align="left">&nbsp;<code>&quot;(?&lt;=X)&quot;</code></td>
     * </tr>
     * <tr>
     * <th align="right">Zero width negative look-behind</th>
     * <td align="left">&nbsp;<code>&quot;(?&lt;!X)&quot;</code></td>
     * </tr>
     * <tr>
     * <th align="right">Independent-non-capturing</th>
     * <td align="left">&nbsp;<code>&quot;(?&gt;X)&quot;</code></td>
     * </tr>
     * </table>
     */
    public static enum GroupType {

        /**
         * X, with no grouping: <code>X</code>
         */
        NONE(""),
        /**
         * X, as a capturing group: <code>(X)</code>
         */
        CAPTURING("("),
        /**
         * X, as a non-capturing group: <code>(?:X)</code>
         */
        NON_CAPTURING("(?:"),
        /**
         * X, via zero-width positive lookahead: <code>(?=X)</code>
         */
        ZERO_WIDTH_POSITIVE_LOOKAHEAD("(?="),
        /**
         * X, via zero-width negative lookahead: <code>(?!X)</code>
         */
        ZERO_WIDTH_NEGATIVE_LOOKAHEAD("(?!"),
        /**
         * X, via zero-width positive lookbehind: <code>(?&lt;=X)</code>
         */
        ZERO_WIDTH_POSITIVE_LOOKBEHIND("(?<="),
        /**
         * X, via zero-width negative lookbehind: <code>(?&lt;!X)</code>
         */
        ZERO_WIDTH_NEGATIVE_LOOKBEHIND("(?<!"),
        /**
         * X, as an independent, non-capturing group: <code>(?&gt;X)</code>
         */
        INDEPENDENT_NON_CAPTURING("(?>");

        private final String openingParenthesis;

        private GroupType(final String openingParenthesis) {
            this.openingParenthesis = openingParenthesis;
        }

        String getOpeningParenthesis() {
            return openingParenthesis;
        }

        @Override
        public String toString() {
            return new StringBuilder(6).append(openingParenthesis).append('X').append(')').toString();
        }
    }

    /**
     * The regex quantifier; either greedy <code>&quot;&quot;</code>, reluctant <code>&quot;?&quot;</code>, or possessive
     * <code>&quot;+&quot;</code>
     */
    public static enum QuantifierType {
        /**
         * Greedy quantifier &lt;empty-string&gt;
         */
        GREEDY(""),
        /**
         * Reluctant quantifier <code>?</code>
         */
        RELUCTANT("?"),
        /**
         * Possessive quantifier <code>+</code>
         */
        POSSESSIVE("+");

        private final String appendix;

        private QuantifierType(final String appendix) {
            this.appendix = appendix;
        }

        String getAppendix() {
            return appendix;
        }

        @Override
        public String toString() {
            return appendix;
        }
    }

    /**
     * Initializes a new {@link RegexUtility}
     */
    private RegexUtility() {
        super();
    }

    /**
     * Returns a literal pattern for the specified string.
     * <p>
     * This method produces a <code>String</code> that can be used to create a {@link Pattern} that would match the string <code>s</code> as
     * if it were a literal pattern.
     * </p>
     * Meta-Characters or escape sequences in the input sequence will be given no special meaning.
     *
     * @param s The string to be literalized
     * @return A literal string replacement
     */
    public static String quote(final String s) {
        final String slashE = "\\E";
        int slashEIndex = s.indexOf(slashE);
        if (slashEIndex == -1) {
            return new StringBuilder(4 + s.length()).append("\\Q").append(s).append(slashE).toString();
        }
        final StringBuilder sb = new StringBuilder(s.length() << 1);
        sb.append("\\Q");
        slashEIndex = 0;
        int current = 0;
        while ((slashEIndex = s.indexOf(slashE, current)) != -1) {
            sb.append(s.substring(current, slashEIndex));
            current = slashEIndex + 2;
            sb.append("\\E\\\\E\\Q");
        }
        sb.append(s.substring(current, s.length()));
        sb.append(slashE);
        return sb.toString();
    }

    /**
     * Concatenates specified first regular expression with given regular expressions.
     *
     * @param regex The first regular expression
     * @param regexes The array of regular expressions to append
     * @return The concatenated regex
     */
    public static String concat(final String regex, final String... regexes) {
        int length = regex.length();
        for (int i = 0; i < regexes.length; i++) {
            length += regexes[i].length();
        }
        final StringBuilder sb = new StringBuilder(length).append(regex);
        for (int i = 0; i < regexes.length; i++) {
            sb.append(regexes[i]);
        }
        return sb.toString();
    }

    /**
     * Combines specified regular expressions with OR operator <code>'|'</code>:
     *
     * <pre>
     * regex1 | regex2
     * </pre>
     *
     * @param regex1 The first regular expression
     * @param regex2 The second regular expression
     * @return The OR-combined regular expression
     */
    public static String OR(final String regex1, final String regex2) {
        return new StringBuilder(regex1.length() + regex2.length() + 1).append(regex1).append('|').append(regex2).toString();
    }

    /**
     * Appends the greedy optional operator <code>'?'</code> to specified regular expression
     *
     * <pre>
     * regex?
     * </pre>
     *
     * @param regex The regular expression
     * @return The greedy optional regular expression
     */
    public static String optional(final String regex) {
        return optional(regex, QuantifierType.GREEDY, GroupType.NONE);
    }

    /**
     * Appends the grouped greedy optional operator <code>'?'</code> to specified regular expression
     *
     * <pre>
     * (&lt;group-type&gt;regex)?
     * </pre>
     *
     * @param regex The regular expression
     * @param groupType The group type
     * @return The grouped greedy optional regular expression
     */
    public static String optional(final String regex, final GroupType groupType) {
        return optional(regex, QuantifierType.GREEDY, groupType);
    }

    /**
     * Appends the optional operator <code>'?'</code> and grouping to specified regular expression
     *
     * <pre>
     * (&lt;group-type&gt;regex)?&lt;quantifier-type&gt;
     * </pre>
     *
     * @param regex The regular expression
     * @param qt The quantifier type
     * @param groupType The group type
     * @return The grouped optional regular expression
     */
    public static String optional(final String regex, final QuantifierType qt, final GroupType groupType) {
        if (GroupType.NONE.equals(groupType)) {
            return new StringBuilder(regex.length() + 2).append(regex).append('?').append(qt.getAppendix()).toString();
        }
        return _optional(regex, qt, groupType);
    }

    private static String _optional(final String regex, final QuantifierType qt, final GroupType groupType) {
        return new StringBuilder(regex.length() + 7).append(groupType.getOpeningParenthesis()).append(regex).append(')').append('?').append(
            qt.getAppendix()).toString();
    }

    /**
     * Groups given regular expression
     *
     * <pre>
     * (regex) OR (?:regex)
     * </pre>
     *
     * @param regex The regular expression
     * @param capturing <code>true</code> as capturing group; <code>false</code> for non-capturing
     * @return The grouped regular expression
     * @see #group(String, com.openexchange.tools.regex.RegexUtility.GroupType) to define more group types
     */
    public static String group(final String regex, final boolean capturing) {
        return _group(regex, capturing ? GroupType.CAPTURING : GroupType.NON_CAPTURING);
    }

    /**
     * Groups given regular expression as stated by specified <code>groupType</code>.
     *
     * <pre>
     * (&lt;group-type&gt;regex)
     * </pre>
     *
     * @param regex The regular expression
     * @param groupType The group type
     * @return The grouped regular expression
     */
    public static String group(final String regex, final GroupType groupType) {
        if (GroupType.NONE.equals(groupType)) {
            return regex;
        }
        return _group(regex, groupType);
    }

    private static String _group(final String regex, final GroupType groupType) {
        return new StringBuilder(regex.length() + 5).append(groupType.getOpeningParenthesis()).append(regex).append(')').toString();
    }

    /**
     * Appends the greedy one-or-more-times operator <code>'+'</code> to specified regular expression
     *
     * <pre>
     * regex+
     * </pre>
     *
     * @param regex The regular expression
     * @return The greedy one-or-more-times regular expression
     */
    public static String oneOrMoreTimes(final String regex) {
        return oneOrMoreTimes(regex, QuantifierType.GREEDY, GroupType.NONE);
    }

    /**
     * Appends the grouped greedy one-or-more-times operator <code>'+'</code> to specified regular expression
     *
     * <pre>
     * (&lt;group-type&gt;regex)+
     * </pre>
     *
     * @param regex The regular expression
     * @param groupType The group type
     * @return The grouped greedy one-or-more-times regular expression
     */
    public static String oneOrMoreTimes(final String regex, final GroupType groupType) {
        return oneOrMoreTimes(regex, QuantifierType.GREEDY, groupType);
    }

    /**
     * Appends the one-or-more-times operator <code>'+'</code> and grouping to specified regular expression
     *
     * <pre>
     * (&lt;group-type&gt;regex)+&lt;quantifier-type&gt;
     * </pre>
     *
     * @param regex The regular expression
     * @param qt The quantifier type
     * @param groupType The group type
     * @return The grouped one-or-more-times regular expression
     */
    public static String oneOrMoreTimes(final String regex, final QuantifierType qt, final GroupType groupType) {
        if (GroupType.NONE.equals(groupType)) {
            return new StringBuilder(regex.length() + 2).append(regex).append('+').append(qt.getAppendix()).toString();
        }
        return _oneOrMoreTimes(regex, qt, groupType);
    }

    private static String _oneOrMoreTimes(final String regex, final QuantifierType qt, final GroupType groupType) {
        return new StringBuilder(regex.length() + 7).append(groupType.getOpeningParenthesis()).append(regex).append(')').append('+').append(
            qt.getAppendix()).toString();
    }

    /**
     * Appends the greedy zero-or-more-times operator <code>'*'</code> to specified regular expression
     *
     * <pre>
     * regex*
     * </pre>
     *
     * @param regex The regular expression
     * @return The greedy zero-or-more-times regular expression
     */
    public static String zeroOrMoreTimes(final String regex) {
        return zeroOrMoreTimes(regex, QuantifierType.GREEDY, GroupType.NONE);
    }

    /**
     * Appends the grouped greedy zero-or-more-times operator <code>'*'</code> to specified regular expression
     *
     * <pre>
     * (&lt;group-type&gt;regex)*
     * </pre>
     *
     * @param regex The regular expression
     * @param groupType The group type
     * @return The grouped greedy zero-or-more-times regular expression
     */
    public static String zeroOrMoreTimes(final String regex, final GroupType groupType) {
        return zeroOrMoreTimes(regex, QuantifierType.GREEDY, groupType);
    }

    /**
     * Appends the zero-or-more-times operator <code>'*'</code> and grouping to specified regular expression
     *
     * <pre>
     * (&lt;group-type&gt;regex)*&lt;quantifier-type&gt;
     * </pre>
     *
     * @param regex The regular expression
     * @param qt The quantifier type
     * @param groupType The group type
     * @return The grouped zero-or-more-times regular expression
     */
    public static String zeroOrMoreTimes(final String regex, final QuantifierType qt, final GroupType groupType) {
        if (GroupType.NONE.equals(groupType)) {
            return new StringBuilder(regex.length() + 2).append(regex).append('*').append(qt.getAppendix()).toString();
        }
        return _zeroOrMoreTimes(regex, qt, groupType);
    }

    private static String _zeroOrMoreTimes(final String regex, final QuantifierType qt, final GroupType groupType) {
        return new StringBuilder(regex.length() + 6).append(groupType.getOpeningParenthesis()).append(regex).append(')').append('*').append(
            qt.getAppendix()).toString();
    }

    /**
     * Appends the greedy exactly-n-times operator <code>{n}</code> to specified regular expression
     *
     * <pre>
     * regex{n}
     * </pre>
     *
     * @param regex The regular expression
     * @param n The <code>exactly-n-times</code> number
     * @return The greedy exactly-n-times regular expression
     */
    public static String exactlyNTimes(final String regex, final int n) {
        return exactlyNTimes(regex, n, QuantifierType.GREEDY, GroupType.NONE);
    }

    /**
     * Appends the grouped greedy exactly-n-times operator <code>{n}</code> to specified regular expression
     *
     * <pre>
     * (&lt;group-type&gt;regex){n}
     * </pre>
     *
     * @param regex The regular expression
     * @param n The <code>exactly-n-times</code> number
     * @param groupType The group type
     * @return The grouped greedy exactly-n-times regular expression
     */
    public static String exactlyNTimes(final String regex, final int n, final GroupType groupType) {
        return exactlyNTimes(regex, n, QuantifierType.GREEDY, groupType);
    }

    /**
     * Appends the exactly-n-times operator <code>{n}</code> and grouping to specified regular expression
     *
     * <pre>
     * (&lt;group-type&gt;regex){n}&lt;quantifier-type&gt;
     * </pre>
     *
     * @param regex The regular expression
     * @param n The <code>exactly-n-times</code> number
     * @param qt The quantifier type
     * @param groupType The group type
     * @return The grouped exactly-n-times regular expression
     */
    public static String exactlyNTimes(final String regex, final int n, final QuantifierType qt, final GroupType groupType) {
        if (GroupType.NONE.equals(groupType)) {
            return new StringBuilder(regex.length() + 5).append(regex).append('{').append(n).append('}').append(qt.getAppendix()).toString();
        }
        return _exactlyNTimes(regex, n, qt, groupType);
    }

    private static String _exactlyNTimes(final String regex, final int n, final QuantifierType qt, final GroupType groupType) {
        return new StringBuilder(regex.length() + 9).append(groupType.getOpeningParenthesis()).append(regex).append(')').append('{').append(
            n).append('}').append(qt.getAppendix()).toString();
    }

    /**
     * Appends the greedy at-least-n-times operator <code>{n,}</code> to specified regular expression
     *
     * <pre>
     * regex{n,}
     * </pre>
     *
     * @param regex The regular expression
     * @param n The <code>at-least-n-times</code> number
     * @return The greedy at-least-n-times regular expression
     */
    public static String atLeastNTimes(final String regex, final int n) {
        return atLeastNTimes(regex, n, QuantifierType.GREEDY, GroupType.NONE);
    }

    /**
     * Appends the grouped greedy at-least-n-times operator <code>{n,}</code> to specified regular expression
     *
     * <pre>
     * (&lt;group-type&gt;regex){n,}
     * </pre>
     *
     * @param regex The regular expression
     * @param n The <code>at-least-n-times</code> number
     * @param groupType The group type
     * @return The grouped greedy at-least-n-times regular expression
     */
    public static String atLeastNTimes(final String regex, final int n, final GroupType groupType) {
        return atLeastNTimes(regex, n, QuantifierType.GREEDY, groupType);
    }

    /**
     * Appends the at-least-n-times operator <code>{n}</code> and grouping to specified regular expression
     *
     * <pre>
     * (&lt;group-type&gt;regex){n,}&lt;quantifier-type&gt;
     * </pre>
     *
     * @param regex The regular expression
     * @param n The <code>at-least-n-times</code> number
     * @param qt The quantifier type
     * @param groupType The group type
     * @return The grouped at-least-n-times regular expression
     */
    public static String atLeastNTimes(final String regex, final int n, final QuantifierType qt, final GroupType groupType) {
        if (GroupType.NONE.equals(groupType)) {
            return new StringBuilder(regex.length() + 6).append(regex).append('{').append(n).append(',').append('}').append(
                qt.getAppendix()).toString();
        }
        return _atLeastNTimes(regex, n, qt, groupType);
    }

    private static String _atLeastNTimes(final String regex, final int n, final QuantifierType qt, final GroupType groupType) {
        return new StringBuilder(regex.length() + 10).append(groupType.getOpeningParenthesis()).append(regex).append(')').append('{').append(
            n).append(',').append('}').append(qt.getAppendix()).toString();
    }

    /**
     * Appends the greedy at-least-n-but-not-more-than-m-times operator <code>{n,m}</code> to specified regular expression.
     *
     * <pre>
     * regex{n,m}
     * </pre>
     *
     * @param regex The regular expression
     * @param n The <code>at-least-n-times</code> number
     * @param m The <code>not-more-than-m-times</code> number
     * @return The greedy at-least-n-but-not-more-than-m-times regular expression
     */
    public static String atLeastNButNotMoreThanMTimes(final String regex, final int n, final int m) {
        return atLeastNButNotMoreThanMTimes(regex, n, m, QuantifierType.GREEDY, GroupType.NONE);
    }

    /**
     * Appends the grouped greedy at-least-n-but-not-more-than-m-times operator <code>{n,m}</code> to specified regular expression.
     *
     * <pre>
     * (&lt;group-type&gt;regex){n,m}
     * </pre>
     *
     * @param regex The regular expression
     * @param n The <code>at-least-n-times</code> number
     * @param m The <code>not-more-than-m-times</code> number
     * @param groupType The group type
     * @return The grouped greedy at-least-n-but-not-more-than-m-times regular expression
     */
    public static String atLeastNButNotMoreThanMTimes(final String regex, final int n, final int m, final GroupType groupType) {
        return atLeastNButNotMoreThanMTimes(regex, n, m, QuantifierType.GREEDY, groupType);
    }

    /**
     * Appends the at-least-n-but-not-more-than-m-times operator <code>{n,m}</code> and grouping to specified regular expression.
     *
     * <pre>
     * (&lt;group-type&gt;regex){n,m}&lt;quantifier-type&gt;
     * </pre>
     *
     * @param regex The regular expression
     * @param n The <code>at-least-n-times</code> number
     * @param m The <code>not-more-than-m-times</code> number
     * @param qt The quantifier type
     * @param groupType The group type
     * @return The grouped at-least-n-but-not-more-than-m-times regular expression
     */
    public static String atLeastNButNotMoreThanMTimes(final String regex, final int n, final int m, final QuantifierType qt, final GroupType groupType) {
        if (GroupType.NONE.equals(groupType)) {
            return new StringBuilder(regex.length() + 8).append(regex).append('{').append(n).append(',').append(m).append('}').append(
                qt.getAppendix()).toString();
        }
        return _atLeastNButNotMoreThanMTimes(regex, n, m, qt, groupType);
    }

    private static String _atLeastNButNotMoreThanMTimes(final String regex, final int n, final int m, final QuantifierType qt, final GroupType groupType) {
        return new StringBuilder(regex.length() + 12).append(groupType.getOpeningParenthesis()).append(regex).append(')').append('{').append(
            n).append(',').append(m).append('}').append(qt.getAppendix()).toString();
    }
}
