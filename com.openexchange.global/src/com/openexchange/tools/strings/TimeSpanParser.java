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

package com.openexchange.tools.strings;

import static com.openexchange.java.Autoboxing.L;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * {@link TimeSpanParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class TimeSpanParser  implements StringParser {

    private static final Map<String, Long> UNITS = new HashMap<String, Long>() {
        private static final long serialVersionUID = 4341197305332412108L;
    {
        put("MS", L(1L));
        put("S", L(1000L));
        put("M", L(1000L*60));
        put("H", L(1000L*60*60));
        put("D", L(1000L*60*60*24));
        put("W", L(1000L*60*60*24*7));
    }};

    /**
     * A timespan specification consists of a number and a unit of measurement. Units are:
     * <ul>
     * <li><tt>ms</tt> for miliseconds</li>
     * <li><tt>s</tt> for seconds</li>
     * <li><tt>m</tt> for minutes</li>
     * <li><tt>h</tt> for hours</li>
     * <li><tt>D</tt> for days</li>
     * <li><tt>W</tt> for weeks</li>
     * </ul>
     *
     * So, for example <tt>&quot;2D 1h 12ms&quot;</tt> would be 2 days and one hour and 12 milliseconds
     *
     * @param span The span string
     * @return The parsed <tt>Long</tt> value
     */
    public static Long parseTimespan(final String span) {
        if (span == null) {
            return Long.valueOf(-1);
        }
        final StringBuilder numberBuilder = new StringBuilder();
        final StringBuilder unitBuilder = new StringBuilder();
        int mode = 0;
        long tally = 0;

        final int length = span.length();
        for (int i = 0; i < length; i++) {
            final char c = span.charAt(i);
            if (Character.isDigit(c)) {
                if (mode == 0) {
                    numberBuilder.append(c);
                } else {
                    final String unit = 0 == unitBuilder.length() ? "MS" : unitBuilder.toString().toUpperCase();
                    final Long factor = UNITS.get(unit);
                    if (factor == null) {

                        throw new IllegalArgumentException("I don't know unit " + unit);
                    }
                    tally += Long.parseLong(numberBuilder.toString()) * factor.longValue();
                    numberBuilder.setLength(0);
                    unitBuilder.setLength(0);
                    mode = 0;
                    numberBuilder.append(c);
                }
            } else if (Character.isLetter(c)) {
                mode = 1;
                unitBuilder.append(c);
            } else {
                // IGNORE
            }
        }
        if (numberBuilder.length() != 0) {
            final String unit = 0 == unitBuilder.length() ? "MS" : unitBuilder.toString().toUpperCase();
            final Long factor = UNITS.get(unit);
            if (factor == null) {
                throw new IllegalArgumentException("I don't know unit " + unit);
            }
            tally += Long.parseLong(numberBuilder.toString()) * factor.longValue();
        }
        return Long.valueOf(tally);
    }

    private static final Set<Class<?>> SUPPORTED = new HashSet<Class<?>>() {
        private static final long serialVersionUID = -551644321593953282L;
    {
        add(Long.class);
        add(long.class);
        add(Date.class);
    }};

    @Override
    public <T> T parse(final String s, final Class<T> t) {
        if(!SUPPORTED.contains(t)) {
            return null;
        }
        Long timespan = null;
        try {
            timespan = parseTimespan(s);
        } catch (final IllegalArgumentException x) {
            return null;
        }
        if(t == Long.class || t == long.class) {
            return (T) timespan;
        } else if (t == Date.class) {
            return (T) new Date(timespan);
        }
        return null;
    }

}
