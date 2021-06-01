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

    private final boolean inclusive;

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
                from = Long.parseLong(sFrom);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        if (!"*".equals(sTo)) {
            try {
                to = Long.parseLong(sTo);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return new TimeFrame(from, to, inclusive);
    }

}
