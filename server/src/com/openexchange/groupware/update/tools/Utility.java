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

package com.openexchange.groupware.update.tools;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * {@link Utility} - TODO Short description of this class' purpose.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Utility {

    /**
     * Initializes a new {@link Utility}.
     */
    private Utility() {
        super();
    }

    /**
     * Generates a DB-style output for given map.
     * 
     * <pre>
     * +---------------+---------+
     * | schema        | version |
     * +---------------+---------+
     * | 345fg_dfghdfg | 12      |
     * +---------------+---------+
     * | dfgdg56       | 12      |
     * +---------------+---------+
     * </pre>
     * 
     * @param m The map
     * @param columnNames The map's column names
     * @return A DB-style output for given map
     */
    public static String toTable(final Map<?, ?> m, final String[] columnNames) {
        final int[] maxLengths = { getMaxLen(m.keySet(), columnNames[0].length()), getMaxLen(m.values(), columnNames[1].length()) };

        final int size = m.size();

        final StringBuilder sb = new StringBuilder(size * 64);

        sb.append('+').append('-');
        for (int i = 0; i < maxLengths[0]; i++) {
            sb.append('-');
        }
        for (int i = 1; i < maxLengths.length; i++) {
            sb.append('-').append('+').append('-');
            for (int j = 0; j < maxLengths[i]; j++) {
                sb.append('-');
            }
        }
        sb.append('-').append('+').append('\n');
        final String delimLine = sb.toString();
        sb.setLength(0);

        sb.append(delimLine);

        appendValues(columnNames, maxLengths, sb);

        sb.append(delimLine);

        final Iterator<?> iter = m.entrySet().iterator();
        for (int i = 0; i < size; i++) {
            final Map.Entry<?, ?> entry = (Entry<?, ?>) iter.next();
            final String[] values = new String[] { entry.getKey().toString(), entry.getValue().toString() };
            appendValues(values, maxLengths, sb);
            sb.append(delimLine);
        }
        return sb.toString();
    }

    private static void appendValues(final String[] values, final int[] maxLengths, final StringBuilder sb) {
        sb.append('|').append(' ').append(values[0]);
        for (int i = values[0].length(); i < maxLengths[0]; i++) {
            sb.append(' ');
        }
        for (int i = 1; i < values.length; i++) {
            sb.append(' ').append('|').append(' ').append(values[i]);
            for (int j = values[i].length(); j < maxLengths[i]; j++) {
                sb.append(' ');
            }
        }
        sb.append(' ').append('|').append('\n');
    }

    private static int getMaxLen(final Collection<? extends Object> c, final int startLen) {
        int max = startLen;
        for (final Object obj : c) {
            final int b = obj.toString().length();
            max = (max >= b) ? max : b;
        }
        return max;
    }

}
