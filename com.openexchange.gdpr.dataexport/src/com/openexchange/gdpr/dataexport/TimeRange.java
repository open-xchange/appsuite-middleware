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

package com.openexchange.gdpr.dataexport;

import com.openexchange.java.Strings;

/**
 * {@link TimeRange} - A pair of starting and ending time of the day.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class TimeRange implements Comparable<TimeRange> {

    /**
     * Parses given time range.
     *
     * @param timeRange The time range to parse
     * @return The parsed instance
     */
    public static TimeRange parseFrom(String timeRange) {
        if (Strings.isEmpty(timeRange)) {
            throw new IllegalArgumentException("Time range must no be null or empty");
        }

        String[] tokens = Strings.splitBy(timeRange, '-', true);
        if (tokens.length != 2) {
            throw new IllegalArgumentException("Illegal time range: " + timeRange);
        }

        TimeOfTheDay start = TimeOfTheDay.parseFrom(tokens[0]);
        TimeOfTheDay end = TimeOfTheDay.parseFrom(tokens[1]);
        return new TimeRange(start, end);
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    private final TimeOfTheDay start;
    private final TimeOfTheDay end;
    private int hash;

    /**
     * Initializes a new {@link TimeRange}.
     *
     * @param start The start time
     * @param end The end time
     */
    public TimeRange(TimeOfTheDay start, TimeOfTheDay end) {
        super();
        if (end.compareTo(start) <= 0) {
            throw new IllegalArgumentException("End time must not be less than/equal to start time");
        }
        this.start = start;
        this.end = end;
        hash = 0;
    }

    /**
     * Gets the starting time of the day.
     *
     * @return The starting time of the day
     */
    public TimeOfTheDay getStart() {
        return start;
    }

    /**
     * Gets the ending time of the day.
     *
     * @return The ending time of the day
     */
    public TimeOfTheDay getEnd() {
        return end;
    }

    /**
     * Checks if given time of the day is included by this time range.
     *
     * @param time The time of the day to check
     * @return <code>true</code> if included; otherwise <code>false</code>
     */
    public boolean contains(TimeOfTheDay time) {
        return time == null ? false : (start.compareTo(time) <= 0 && end.compareTo(time) > 0);
    }

    /**
     * Checks if given time range overlaps with this time range.
     *
     * @param time The time range to check
     * @return <code>true</code> if overlapping; otherwise <code>false</code>
     */
    public boolean overlapsWith(TimeRange range) {
        return range == null ? false : start.compareTo(range.end) != -1 || end.compareTo(range.start) != 1 ? true : false;
    }

    @Override
    public int compareTo(TimeRange o) {
        return start.compareTo(o.start);
    }
    

    @Override
    public int hashCode() {
        int result = hash;
        if (result == 0) {
            int prime = 31;
            result = 1;
            result = prime * result + ((start == null) ? 0 : start.hashCode());
            result = prime * result + ((end == null) ? 0 : end.hashCode());
            hash = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TimeRange)) {
            return false;
        }
        TimeRange other = (TimeRange) obj;
        if (start == null) {
            if (other.start != null) {
                return false;
            }
        } else if (!start.equals(other.start)) {
            return false;
        }
        if (end == null) {
            if (other.end != null) {
                return false;
            }
        } else if (!end.equals(other.end)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(start).append('-').append(end).toString();
    }

}
