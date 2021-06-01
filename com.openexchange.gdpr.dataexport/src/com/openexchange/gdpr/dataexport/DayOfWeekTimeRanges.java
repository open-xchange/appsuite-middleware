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

import java.time.DayOfWeek;
import java.util.List;
import com.google.common.collect.ImmutableList;

/**
 * {@link DayOfWeekTimeRanges} - Represents time ranges for a certain day of the week.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DayOfWeekTimeRanges {

    private final DayOfWeek dayOfWeek;
    private final List<TimeRange> ranges;

    /**
     * Initializes a new {@link DayOfWeekTimeRanges}.
     *
     * @param dayOfWeek The day of the week
     * @param ranges The time ranges within that day
     */
    public DayOfWeekTimeRanges(DayOfWeek dayOfWeek, List<TimeRange> ranges) {
        super();
        this.dayOfWeek = dayOfWeek;
        this.ranges = ImmutableList.copyOf(ranges);
    }

    /**
     * Gets the day of the week.
     *
     * @return The day of the week
     */
    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * Gets the (immutable) time ranges.
     *
     * @return The time ranges
     */
    public List<TimeRange> getRanges() {
        return ranges;
    }

    @Override
    public String toString() {
        return new StringBuilder(dayOfWeek.toString()).append(' ').append(ranges).toString();
    }

}
