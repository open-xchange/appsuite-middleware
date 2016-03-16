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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link MatcherReplacer} - Implements faster append-and-replace steps for a {@link Matcher matcher} based on an unsynchronized
 * {@link StringBuilder string buffer}.
 * <p>
 * This class is considered to be the unsynchronized replacement for {@link Matcher#appendReplacement(StringBuffer, String)} and
 * {@link Matcher#appendTail(StringBuffer)} which only accept a synchronized string buffer.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MatcherReplacer {

    private Matcher matcher;

    private String input;

    private int lastPos;

    /**
     * Initializes an empty new {@link MatcherReplacer}.
     */
    public MatcherReplacer() {
        this(Pattern.compile(".+").matcher(""), "");
    }

    /**
     * Initializes a new {@link MatcherReplacer}.
     *
     * @param matcher The matcher
     * @param input The input string from which the matcher was created
     */
    public MatcherReplacer(final Matcher matcher, final String input) {
        super();
        if (null == matcher) {
            throw new IllegalArgumentException("matcher is null");
        }
        if (null == input) {
            throw new IllegalArgumentException("input is null");
        }
        this.matcher = matcher;
        this.input = input;
        lastPos = 0;
    }

    /**
     * Resets this replacer to specified matcher and input string.
     *
     * @param matcher The matcher
     * @param input The input string from which the matcher was created
     */
    public void resetTo(final Matcher matcher, final String input) {
        if (null == matcher) {
            throw new IllegalArgumentException("matcher is null");
        }
        if (null == input) {
            throw new IllegalArgumentException("input is null");
        }
        this.matcher = matcher;
        this.input = input;
        lastPos = 0;
    }

    /**
     * Implements a non-terminal append-and-replace step. The replacement string is considered to be <b>literalized</b>; meaning <b>no
     * references to subsequences captured during the previous match are possible</b>.
     * <p>
     * This method performs the following actions:
     * </p>
     * <ol>
     * <li>
     * <p>
     * It reads characters from the input sequence, starting at the append position, and appends them to the given string builder. It stops
     * after reading the last character preceding the previous match, that is, the character at index {@link Matcher#start()}&nbsp;
     * <tt>-</tt>&nbsp; <tt>1</tt>.
     * </p>
     * </li>
     * <li>
     * <p>
     * It appends the given replacement string to the string builder.
     * </p>
     * </li>
     * <li>
     * <p>
     * It sets the append position of this matcher to the index of the last character matched, plus one, that is, to {@link Matcher#end()}.
     * </p>
     * </li>
     * </ol>
     * <p>
     * This method is intended to be used in a loop together with the {@link #appendTail appendTail} and {@link Matcher#find find} methods.
     * The following code, for example, writes <tt>one dog two dogs in the
     * yard</tt> to the standard-output stream:
     * </p>
     * <blockquote>
     *
     * <pre>
     * Pattern p = Pattern.compile(&quot;cat&quot;);
     * Matcher m = p.matcher(&quot;one cat two cats in the yard&quot;);
     * MatcherReplacer mr = new MatcherReplacer(m, s);
     * StringBuilder sb = new StringBuilder(256);
     * while (m.find()) {
     *     mr.appendLiteralizedReplacement(sb, &quot;dog&quot;);
     *     // DON'T: mr.appendLiteralizedReplacement(sb, Matcher.quote(&quot;a text with $1 references and \\ escape characters&quot;));
     * }
     * mr.appendTail(sb);
     * System.out.println(sb.toString());
     * </pre>
     *
     * </blockquote>
     *
     * @param sb The target string allocator
     * @param replacement The literal replacement string
     * @see #appendReplacement(StringBuilder, String)
     */
    public void appendLiteralReplacement(final StringBuilder sb, final String replacement) {
        sb.append(input.substring(lastPos, matcher.start()));
        sb.append(replacement);
        lastPos = matcher.end();
    }

    /**
     * Implements a non-terminal append-and-replace step. The replacement string is <b>not</b> considered to be literalized; meaning <b>
     * references to subsequences captured during the previous match are possible</b>.
     * <p>
     * Note that backslashes (<tt>\</tt>) and dollar signs (<tt>$</tt>) in the replacement string may cause the results to be different than
     * if it were being treated as a literal replacement string. Dollar signs may be treated as references to captured subsequences as
     * described above, and backslashes are used to escape literal characters in the replacement string. If a literalized string replacement
     * is desired, use {@link #appendLiteralReplacement(StringBuilder, String)} or quote replacement with
     * {@link Matcher#quoteReplacement(String)} prior to passing to this routine.
     * <p>
     * This method performs the following actions:
     * </p>
     * <ol>
     * <li>
     * <p>
     * It reads characters from the input sequence, starting at the append position, and appends them to the given string builder. It stops
     * after reading the last character preceding the previous match, that is, the character at index {@link Matcher#start()}&nbsp;
     * <tt>-</tt>&nbsp; <tt>1</tt>.
     * </p>
     * </li>
     * <li>
     * <p>
     * It appends the given replacement string to the string builder.
     * </p>
     * </li>
     * <li>
     * <p>
     * It sets the append position of this matcher to the index of the last character matched, plus one, that is, to {@link Matcher#end()}.
     * </p>
     * </li>
     * </ol>
     * <p>
     * The replacement string may contain references to subsequences captured during the previous match: Each occurrence of <tt>$</tt>
     * <i>g</i><tt></tt> will be replaced by the result of evaluating {@link Matcher#group(int) group}<tt>(</tt><i>g</i><tt>)</tt>. The
     * first number after the <tt>$</tt> is always treated as part of the group reference. Subsequent numbers are incorporated into g if
     * they would form a legal group reference. Only the numerals '0' through '9' are considered as potential components of the group
     * reference. If the second group matched the string <tt>"foo"</tt>, for example, then passing the replacement string <tt>"$2bar"</tt>
     * would cause <tt>"foobar"</tt> to be appended to the string builder. A dollar sign (<tt>$</tt>) may be included as a literal in the
     * replacement string by preceding it with a backslash (<tt>\$</tt>).
     * <p>
     * This method is intended to be used in a loop together with the {@link #appendTail appendTail} and {@link Matcher#find find} methods.
     * The following code, for example, writes <tt>one dog two dogs in the
     * yard</tt> to the standard-output stream:
     * </p>
     * <blockquote>
     *
     * <pre>
     * Pattern p = Pattern.compile(&quot;cat&quot;);
     * Matcher m = p.matcher(&quot;one cat two cats in the yard&quot;);
     * MatcherReplacer mr = new MatcherReplacer(m, s);
     * StringBuilder sb = new StringBuilder(256);
     * while (m.find()) {
     *     mr.appendReplacement(sb, &quot;dog&quot;);
     * }
     * mr.appendTail(sb);
     * System.out.println(sb.toString());
     * </pre>
     *
     * </blockquote>
     *
     * @param sb The target string allocator
     * @param replacement The replacement string possibly containing group references
     * @throws IllegalArgumentException If the replacement string refers to a capturing group that does not exist in the pattern
     * @see #appendLiteralReplacement(StringBuilder, String)
     */
    public void appendReplacement(final StringBuilder sb, final String replacement) {
        sb.append(input.substring(lastPos, matcher.start()));
        int cursor = 0;
        final int rlen = replacement.length();
        while (cursor < rlen) {
            char nextChar = replacement.charAt(cursor);
            if ('\\' == nextChar) {
                cursor++;
                nextChar = replacement.charAt(cursor);
                sb.append(nextChar);
                cursor++;
            } else if ('$' == nextChar) {
                // Skip past $
                cursor++;

                // The first number is always a group
                int refNum = replacement.charAt(cursor) - '0';
                if ((refNum < 0) || (refNum > 9)) {
                    throw new IllegalArgumentException("Illegal group reference");
                }
                cursor++;

                // Capture the largest legal group string
                boolean done = false;
                while (!done) {
                    if (cursor >= rlen) {
                        break;
                    }
                    final int nextDigit = replacement.charAt(cursor) - '0';
                    if ((nextDigit < 0) || (nextDigit > 9)) { // not a number
                        break;
                    }
                    final int newRefNum = (refNum * 10) + nextDigit;
                    if (matcher.groupCount() < newRefNum) {
                        done = true;
                    } else {
                        refNum = newRefNum;
                        cursor++;
                    }
                }

                // Append group
                if (matcher.group(refNum) != null) {
                    sb.append(matcher.group(refNum));
                }
            } else {
                sb.append(nextChar);
                cursor++;
            }
        }
        lastPos = matcher.end();
    }

    /**
     * Implements a terminal append-and-replace step.
     * <p>
     * This method reads characters from the input sequence, starting at the append position, and appends them to the given string builder.
     * It is intended to be invoked after one or more invocations of the {@link #appendReplacement appendReplacement} method in order to
     * copy the remainder of the input sequence.
     * </p>
     *
     * @param sb The target string allocator
     */
    public void appendTail(final StringBuilder sb) {
        sb.append(input.substring(lastPos));
    }

}
