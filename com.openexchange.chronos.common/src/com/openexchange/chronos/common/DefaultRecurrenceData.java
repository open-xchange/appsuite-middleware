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
        StringBuilder sb = new StringBuilder("DefaultRecurrenceData [rrule=").append(rrule);
        sb.append(", seriesStart=").append(seriesStart);
        sb.append(", exceptionDates=").append((null == exceptionDates ? "null" : Arrays.toString(exceptionDates)));
        sb.append(", recurrenceDates=").append((null == exceptionDates ? "null" : Arrays.toString(exceptionDates)));
        sb.append(']');
        return sb.toString();
    }

}
