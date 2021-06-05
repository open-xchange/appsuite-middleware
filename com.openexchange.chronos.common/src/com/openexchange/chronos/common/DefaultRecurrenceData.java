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

package com.openexchange.chronos.common;

import static com.openexchange.chronos.common.CalendarUtils.combine;
import java.util.Arrays;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.RecurrenceData;

/**
 * {@link DefaultRecurrenceData}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultRecurrenceData implements RecurrenceData {

    private final String rrule;
    private final DateTime seriesStart;
    private final long[] exceptionDates;
    private final long[] recurrenceDates;

    /**
     * Initializes a new {@link DefaultRecurrenceData} based on a recurrence rule and start date, without further exception dates.
     *
     * @param rrule The underlying recurrence rule
     * @param seriesStart The series start date, usually the date of the first occurrence
     */
    public DefaultRecurrenceData(String rrule, DateTime seriesStart) {
        this(rrule, seriesStart, null);
    }

    /**
     * Initializes a new {@link DefaultRecurrenceData}.
     *
     * @param rrule The underlying recurrence rule
     * @param seriesStart The series start date, usually the date of the first occurrence
     * @param exceptionDates The list of exception dates to exclude from the recurrence set, or <code>null</code> if there are none
     */
    public DefaultRecurrenceData(String rrule, DateTime seriesStart, long[] exceptionDates) {
        this(rrule, seriesStart, exceptionDates, null);
    }

    /**
     * Initializes a new {@link DefaultRecurrenceData}.
     *
     * @param rrule The underlying recurrence rule
     * @param seriesStart The series start date, usually the date of the first occurrence
     * @param exceptionDates The list of exception dates to exclude from the recurrence set, or <code>null</code> if there are none
     * @param recurrenceDates The list of recurrence dates to include in the recurrence set, or <code>null</code> if there are none
     */
    public DefaultRecurrenceData(String rrule, DateTime seriesStart, long[] exceptionDates, long[] recurrenceDates) {
        super();
        this.rrule = rrule;
        this.seriesStart = seriesStart;
        this.exceptionDates = exceptionDates;
        this.recurrenceDates = recurrenceDates;
    }

    /**
     * Initializes a new {@link DefaultRecurrenceData} based on a series master event.
     * <p/>
     * The exception dates are derived from the delete exception dates of the recurrence master event (as per
     * {@link Event#getDeleteExceptionDates()}) and overridden instances (as per {@link Event#getChangeExceptionDates()}). Also, the
     * recurrence dates are taken over from the recurrence master (as per {@link Event#getRecurrenceDates()}).
     *
     * @param seriesMaster The series master event
     * @param changeExceptionDates The recurrence identifiers of the overridden occurrences, or <code>null</code> if there are none
     */
    public DefaultRecurrenceData(Event seriesMaster) {
        this(seriesMaster.getRecurrenceRule(),
            seriesMaster.getStartDate(),
            CalendarUtils.getExceptionDates(combine(seriesMaster.getDeleteExceptionDates(), seriesMaster.getChangeExceptionDates())),
            CalendarUtils.getExceptionDates(seriesMaster.getRecurrenceDates())
        );
    }

    @Override
    public String getRecurrenceRule() {
        return rrule;
    }

    @Override
    public DateTime getSeriesStart() {
        return seriesStart;
    }

    @Override
    public long[] getExceptionDates() {
        return exceptionDates;
    }

    @Override
    public long[] getRecurrenceDates() {
        return recurrenceDates;
    }

    @Override
    public String toString() {
        return new StringBuilder("DefaultRecurrenceData [")
            .append("rrule=").append(rrule)
            .append(", start=").append(seriesStart)
            .append(", timezone=").append(null != seriesStart && null != seriesStart.getTimeZone() ? seriesStart.getTimeZone().getID() : null)
            .append(", exDates=").append(null == exceptionDates ? null : Arrays.toString(exceptionDates))
            .append(", rDates=").append(null == recurrenceDates ? null : Arrays.toString(recurrenceDates))
            .append(']')
        .toString();
    }

}
