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

package com.openexchange.config;

import java.util.Arrays;
import com.openexchange.tools.strings.TimeSpanParser;

/**
 * {@link ConfigTools} collect common parsing operations for configuration options.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ConfigTools {

    private static final String NON_PERSISTENT_IDENTIFIER = "web-browser";

    /**
     * The cookie is not stored persistently and will be deleted when the Web browser exits
     */
    private static final int NON_PERSISTENT_VALUE = -1;

    /**
     * A time span specification consists of a number and a unit of measurement. Units are:
     * <ul>
     * <li>ms for milliseconds</li>
     * <li>s for seconds</li>
     * <li>m for minutes</li>
     * <li>h for hours</li>
     * <li>D for days</li>
     * <li>W for weeks</li>
     * </ul>
     * So, for example 2D 1h 12ms would be 2 days and one hour and 12 milliseconds
     *
     * @param span The span description or special identifier <code>"web-browser"</code> to let the Cookie be deleted when the Web browser
     *            exits
     * @return The parsed time span in seconds or <code>-1</code> to let the Cookie be deleted when the Web browser exits
     */
    public static int parseTimespanSecs(final String span) {
        if (null == span) {
            return NON_PERSISTENT_VALUE;
        }
        final String tmp = span.trim();
        if (NON_PERSISTENT_IDENTIFIER.equals(tmp.toLowerCase())) {
            return NON_PERSISTENT_VALUE;
        }
        return (int) (TimeSpanParser.parseTimespan(tmp).longValue() / 1000);
    }

    /**
     * A time span specification consists of a number and a unit of measurement. Units are:
     * <ul>
     * <li>ms for milliseconds</li>
     * <li>s for seconds</li>
     * <li>m for minutes</li>
     * <li>h for hours</li>
     * <li>D for days</li>
     * <li>W for weeks</li>
     * </ul>
     * So, for example 2D 1h 12ms would be 2 days and one hour and 12 milliseconds
     *
     * @param span The span description or special identifier <code>"web-browser"</code> to let the Cookie be deleted when the Web browser
     *            exits
     * @return The parsed time span in milliseconds or <code>-1</code> to let the Cookie be deleted when the Web browser exits
     */
    public static long parseTimespan(final String span) {
        if (null == span) {
            return NON_PERSISTENT_VALUE;
        }
        final String tmp = span.trim();
        if (NON_PERSISTENT_IDENTIFIER.equals(tmp.toLowerCase())) {
            return NON_PERSISTENT_VALUE;
        }
        return TimeSpanParser.parseTimespan(tmp).longValue();
    }

    /**
     * Searches for the property with the specified name. If the name is found, it is supposed to be a <code>long</code> value. If parsing
     * to <code>long</code> fails or name is not found, the default value is returned.
     * <p>
     * Parses the property as a signed decimal <code>long</code>. The characters in the property must all be decimal digits, except that the
     * first character may be an ASCII minus sign <code>'-'</code> (<code>'&#92;u002D'</code>) to indicate a negative value.
     *
     * @param name The property name.
     * @param defaultValue The default value
     * @param service The configuration service reference
     * @return The <code>long</code> value or given default value argument.
     */
    public static long getLongProperty(final String name, final long defaultValue, final ConfigurationService service) {
        if (null == service) {
            return defaultValue;
        }
        final String property = service.getProperty(name);
        if (null == property) {
            return defaultValue;
        }
        try {
            return Long.parseLong(property.trim());
        } catch (final NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parses a byte value including an optional unit, where the unit can be one of <code>B</code>, <code>kB</code>,
     * <code>MB</code>, <code>GB</code>, or <code>TB</code>, ignoring case.
     *
     * @param value The value to parse, e.g. <code>10 MB</code> or <code>17.5kB</code>
     * @return The parsed number in bytes
     * @throws NumberFormatException If the supplied string is not parsable or greater then <code>Long.MAX_VALUE</code>
     */
    public static long parseBytes(String value) throws NumberFormatException {
        StringBuilder numberBuilder = new StringBuilder(8);
        StringBuilder unitbuilder = new StringBuilder(4);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isDigit(c) || '.' == c || '-' == c) {
                numberBuilder.append(c);
            } else if (false == Character.isWhitespace(c)) {
                unitbuilder.append(c);
            }
        }
        double number = Double.parseDouble(numberBuilder.toString());
        if (0 < unitbuilder.length()) {
            String unit = unitbuilder.toString().toUpperCase();
            int exp = Arrays.asList("B", "KB", "MB", "GB", "TB").indexOf(unit);
            if (0 <= exp) {
                number *= Math.pow(1024, exp);
            } else {
                throw new NumberFormatException(value);
            }
        }
        if (Long.MAX_VALUE >= number) {
            return (long) number;
        }
        throw new NumberFormatException(value);
    }

}
