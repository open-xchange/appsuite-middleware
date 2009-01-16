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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

/**
 * {@link MatcherReplacer} - Implements terminal append-and-replace steps for a {@link Matcher matcher} based on an unsynchronized
 * {@link StringBuilder string buffer}.
 * <p>
 * This class is considered to be the unsynchronized replacement for {@link Matcher#appendReplacement(StringBuffer, String)} and
 * {@link Matcher#appendTail(StringBuffer)} which only accept a synchronized string buffer.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MatcherReplacer {

    private final Matcher matcher;

    private final String input;

    private int lastPos;

    /**
     * Initializes a new {@link MatcherReplacer}.
     * 
     * @param matcher The matcher
     * @param input The input string from which the matcher was created
     */
    public MatcherReplacer(final Matcher matcher, final String input) {
        super();
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
     *     mr.appendReplacement(sb, &quot;dog&quot;);
     * }
     * mr.appendTail(sb);
     * System.out.println(sb.toString());
     * </pre>
     * 
     * </blockquote>
     * 
     * @param sb The target string builder
     * @param replacement The literal replacement string
     */
    public void appendReplacement(final StringBuilder sb, final String replacement) {
        sb.append(input.substring(lastPos, matcher.start()));
        sb.append(replacement);
        lastPos = matcher.end();
    }

    /**
     * Implements a terminal append-and-replace step.
     * <p>
     * This method reads characters from the input sequence, starting at the append position, and appends them to the given string buffer.
     * It is intended to be invoked after one or more invocations of the {@link #appendReplacement appendReplacement} method in order to
     * copy the remainder of the input sequence.
     * </p>
     * 
     * @param sb The target string builder
     */
    public void appendTail(final StringBuilder sb) {
        sb.append(input.substring(lastPos));
    }

}
