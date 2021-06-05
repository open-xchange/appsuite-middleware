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

package com.openexchange.chronos;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * {@link Period}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Period {

    private final Date startDate;
    private final Date endDate;
    private final boolean allDay;

    /**
     * Initializes a new {@link Period}.
     *
     * @param startDate The start date of the period
     * @param endDate The end date of the period
     * @param allDay <code>true</code> if the period is "all-day", <code>false</code>, otherwise
     */
    public Period(Date startDate, Date endDate, boolean allDay) {
        super();
        this.startDate = startDate;
        this.endDate = endDate;
        this.allDay = allDay;
    }

    /**
     * Initializes a new {@link Period} based on an event.
     *
     * @param event The event to get the period for
     */
    public Period(Event event) {
        this(new Date(event.getStartDate().getTimestamp()), new Date(event.getEndDate().getTimestamp()), event.getStartDate().isAllDay());
    }

    /**
     * Gets the start date of the period.
     *
     * @return The start date
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Gets the end date of the period.
     *
     * @return The end date
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Gets the all-day character of the period.
     *
     * @return The all-day character
     */
    public boolean isAllDay() {
        return allDay;
    }

    /**
     * Gets the duration of this period in days.
     *
     * @return The duration in total days
     */
    public long getTotalDays() {
        return TimeUnit.MILLISECONDS.toDays(getDuration());
    }

    /**
     * Gets the duration of this period in milliseconds.
     *
     * @return The duration in milliseconds
     */
    public long getDuration() {
        return endDate.getTime() - startDate.getTime();
    }

    @Override
    public String toString() {
        return "Period [startDate=" + startDate + ", endDate=" + endDate + ", allDay=" + allDay + "]";
    }

}
