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

package com.openexchange.find.util;

import com.openexchange.find.common.CommonFacetType;


/**
 * A {@link TimeFrame} denotes a custom value for facets of type {@link CommonFacetType#DATE}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class TimeFrame {

    private final long from;

    private final long to;

    private boolean inclusive;

    private TimeFrame(final long from, final long to, final boolean inclusive) {
        super();
        this.from = from;
        this.to = to;
        this.inclusive = inclusive;
    }

    /**
     * Gets the start time of this time frame.
     *
     * @return A timestamp in milliseconds since midnight, January 1, 1970 UTC.
     */
    public long getFrom() {
        return from;
    }

    /**
     * Gets the end time of this time frame.
     *
     * @return A timestamp in milliseconds since midnight, January 1, 1970 UTC or <code>-1</code>
     * if open ended.
     */
    public long getTo() {
        return to;
    }

    /**
     * Denotes if this time frame is inclusive (i.e. timestamps should be matched with <code>&ge;</code>
     * and <code>&le;</code>) or not.
     *
     * @return <code>true</code> if so, <code>false</code> otherwise.
     */
    public boolean isInclusive() {
        return inclusive;
    }

    /**
     * Parses a string pattern in the form of <code>[* TO 1407142786432]</code> and returns an according
     * {@link TimeFrame} instance.
     *
     * @return The instance or <code>null</code>, if the given pattern was invalid.
     */
    public static TimeFrame valueOf(String query) {
        if (query == null) {
            return null;
        }

        char[] chars = query.trim().toCharArray();
        int length = chars.length;
        if (length < 8) { // Minimum: [* TO *]
            return null;
        }

        boolean inclusive;
        if (chars[0] == '[' && chars[length - 1] == ']') {
            inclusive = true;
        } else if (chars[0] == '{' && chars[length - 1] == '}') {
            inclusive = false;
        } else {
            return null;
        }

        String[] times = new String(chars, 1, length - 2).split("\\sTO\\s");
        if (times.length != 2) {
            return null;
        }

        String sFrom = times[0].trim();
        String sTo = times[1].trim();
        long from = 0L;
        long to = -1L;
        if (!"*".equals(sFrom)) {
            try {
                from = Long.valueOf(sFrom);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        if (!"*".equals(sTo)) {
            try {
                to = Long.valueOf(sTo);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return new TimeFrame(from, to, inclusive);
    }

}
