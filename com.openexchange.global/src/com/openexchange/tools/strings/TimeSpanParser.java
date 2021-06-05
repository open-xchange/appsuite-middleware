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

package com.openexchange.tools.strings;

import static com.openexchange.java.Autoboxing.L;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.java.Strings;


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
     * @return The parsed <tt>long</tt> value
     * @throws IllegalArgumentException If a unit identifier is unknown
     */
    public static Long parseTimespan(final String span) {
        return Long.valueOf(parseTimespanToPrimitive(span));
    }

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
     * @return The parsed <tt>long</tt> value
     * @throws IllegalArgumentException If a unit identifier is unknown
     */
    public static long parseTimespanToPrimitive(final String span) {
        if (span == null) {
            return -1L;
        }

        final StringBuilder numberBuilder = new StringBuilder();
        final StringBuilder unitBuilder = new StringBuilder();
        int mode = 0;
        long tally = 0;

        final int length = span.length();
        for (int i = 0; i < length; i++) {
            final char c = span.charAt(i);
            if (Strings.isDigit(c)) {
                if (mode == 0) {
                    numberBuilder.append(c);
                } else {
                    final String unit = 0 == unitBuilder.length() ? "MS" : unitBuilder.toString().toUpperCase();
                    final Long factor = UNITS.get(unit);
                    if (factor == null) {
                        throw new IllegalArgumentException("Unknown unit: " + unit);
                    }
                    tally += Long.parseLong(numberBuilder.toString()) * factor.longValue();
                    numberBuilder.setLength(0);
                    unitBuilder.setLength(0);
                    mode = 0;
                    numberBuilder.append(c);
                }
            } else if (Strings.isAsciiLetter(c)) {
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
        return tally;
    }

    private static final Set<Class<?>> SUPPORTED = ImmutableSet.of(Long.class, long.class, Date.class);

    @Override
    public <T> T parse(final String s, final Class<T> t) {
        if (!SUPPORTED.contains(t)) {
            return null;
        }

        long timespan;
        try {
            timespan = parseTimespanToPrimitive(s);
        } catch (IllegalArgumentException x) {
            return null;
        }

        if (t == Long.class || t == long.class) {
            @SuppressWarnings("unchecked") T lng = (T) Long.valueOf(timespan);
            return lng;
        } else if (t == Date.class) {
            @SuppressWarnings("unchecked") T date = (T) new Date(timespan);
            return date;
        }
        return null;
    }

}
