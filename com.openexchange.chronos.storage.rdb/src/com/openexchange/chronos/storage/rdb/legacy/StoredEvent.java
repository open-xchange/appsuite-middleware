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

package com.openexchange.chronos.storage.rdb.legacy;

import static com.openexchange.chronos.common.CalendarUtils.initCalendar;
import static com.openexchange.chronos.common.CalendarUtils.isAllDay;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.compat.Event2Appointment.asInt;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Period;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.chronos.compat.PositionAwareRecurrenceId;
import com.openexchange.chronos.compat.SeriesPattern;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.exception.ProblemSeverity;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.rdb.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.TimeZones;
import com.openexchange.server.ServiceLookup;

/**
 * {@link StoredEvent}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class StoredEvent extends DelegatingEvent {

    private final ServiceLookup services;
    private final RdbEventStorage eventStorage;
    private final Connection connection;

    /**
     * Initializes a new {@link StoredEvent}.
     *
     * @param eventStorage A reference to the event storage
     * @param connection The database connection to use
     * @param event The event delegate
     */
    StoredEvent(RdbEventStorage eventStorage, Connection connection, Event event) {
        super(event);
        this.connection = connection;
        this.eventStorage = eventStorage;
        this.services = Services.get();
    }

    @Override
    public boolean containsStartDate() {
        /*
         * a changed recurrence rule requires an update of the start-date, too
         */
        return delegate.containsStartDate() || delegate.containsRecurrenceRule();
    }

    @Override
    public boolean containsEndDate() {
        /*
         * a changed recurrence rule requires an update of the end-date, too
         */
        return delegate.containsEndDate() || delegate.containsRecurrenceRule();
    }

    @Override
    public boolean containsRecurrenceRule() {
        /*
         * a changed start- or end-date requires an update of the recurrence rule, too
         */
        return delegate.containsRecurrenceRule() || isSeriesMaster(delegate) && (delegate.containsStartDate() || delegate.containsEndDate());
    }

    @Override
    public Date getCreated() {
        /*
         * truncate milliseconds from creation date to avoid bad rounding in MySQL versions >= 5.6.4.
         * See: http://dev.mysql.com/doc/refman/5.6/en/fractional-seconds.html
         * See: com.openexchange.sql.tools.SQLTools.toTimestamp(Date)
         */
        Date created = delegate.getCreated();
        return null != created ? new Date((created.getTime() / 1000) * 1000) : null;
    }

    @Override
    public SortedSet<RecurrenceId> getChangeExceptionDates() {
        /*
         * transform recurrence ids to legacy "recurrence date positions" (UTC dates with truncated time fraction)
         */
        return getRecurrenceIds(Event2Appointment.getRecurrenceDatePositions(delegate.getChangeExceptionDates()));
    }

    @Override
    public SortedSet<RecurrenceId> getDeleteExceptionDates() {
        /*
         * transform recurrence ids to legacy "recurrence date positions" (UTC dates with truncated time fraction)
         */
        return getRecurrenceIds(Event2Appointment.getRecurrenceDatePositions(delegate.getDeleteExceptionDates()));
    }

    @Override
    public RecurrenceId getRecurrenceId() {
        RecurrenceId recurrenceId = delegate.getRecurrenceId();
        if (null == recurrenceId) {
            return recurrenceId;
        }
        /*
         * transform recurrence id to legacy "recurrence position" (1-based, sequential position in the series)
         */
        if (PositionAwareRecurrenceId.class.isInstance(recurrenceId)) {
            return new StoredRecurrenceId(((PositionAwareRecurrenceId) recurrenceId).getRecurrencePosition());
        }
        try {
            RecurrenceData recurrenceData;
            if (RecurrenceData.class.isInstance(recurrenceId)) {
                recurrenceData = (RecurrenceData) recurrenceId;
            } else {
                recurrenceData = eventStorage.selectRecurrenceData(connection, asInt(delegate.getSeriesId()), false);
                // no recurrence data found for the events series, maybe already deleted?
                if (null == recurrenceData) {
                    throw unsupportedDataError(EventField.RECURRENCE_ID, ProblemSeverity.CRITICAL, "Unable to get recurrence data for series " + delegate.getSeriesId(), null);
                }
            }
            int recurrencePosition = Event2Appointment.getRecurrencePosition(services.getService(RecurrenceService.class), recurrenceData, recurrenceId);
            return new StoredRecurrenceId(recurrencePosition);
        } catch (OXException | SQLException e) {
            throw unsupportedDataError(EventField.RECURRENCE_ID, ProblemSeverity.CRITICAL, "Error deriving stored recurrence id", e);
        }
    }

    @Override
    public String getRecurrenceRule() {
        String recurrenceRule = delegate.getRecurrenceRule();
        if (null == recurrenceRule) {
            return recurrenceRule;
        }
        /*
         * convert recurrence rule to legacy pattern, including "absolute duration" / "recurrence calculator" field
         */
        try {
            RecurrenceData recurrenceData;
            if (isSeriesMaster(delegate)) {
                recurrenceData = new DefaultRecurrenceData(recurrenceRule, delegate.getStartDate(), null);
            } else if (isSeriesException(delegate)) {
                // TODO really required to also store series pattern for exceptions?
                if (null != delegate.getRecurrenceId() && RecurrenceData.class.isInstance(delegate.getRecurrenceId())) {
                    recurrenceData = (RecurrenceData) delegate.getRecurrenceId();
                } else {
                    recurrenceData = eventStorage.selectRecurrenceData(connection, asInt(delegate.getSeriesId()), false);
                }
            } else {
                throw new IllegalStateException("Unable to derive stored recurrence rule from non-recurring event");
            }
            SeriesPattern seriesPattern = Event2Appointment.getSeriesPattern(services.getService(RecurrenceService.class), recurrenceData);
            long absoluteDuration = new Period(delegate).getTotalDays();
            return absoluteDuration + "~" + seriesPattern.getDatabasePattern();
        } catch (OXException | SQLException e) {
            throw unsupportedDataError(EventField.RECURRENCE_RULE, ProblemSeverity.CRITICAL, "Error deriving stored recurrence rule", e);
        }
    }

    @Override
    public DateTime getStartDate() {
        DateTime startDate = delegate.getStartDate();
        if (null == startDate || false == isSeriesMaster(delegate)) {
            return startDate;
        }
        /*
         * expand recurrence master start-date to cover the whole series period
         */
        try {
            /*
             * remember time fraction of actual start-date
             */
            TimeZone timeZone = startDate.isFloating() ? TimeZones.UTC : startDate.getTimeZone();
            Calendar calendar = initCalendar(timeZone, startDate.getTimestamp());
            int startHour = calendar.get(Calendar.HOUR_OF_DAY);
            int startMinute = calendar.get(Calendar.MINUTE);
            int startSecond = calendar.get(Calendar.SECOND);
            /*
             * iterate recurrence and take over start date of first occurrence
             */
            DefaultRecurrenceData recurrenceData = new DefaultRecurrenceData(delegate.getRecurrenceRule(), startDate, null);
            Iterator<RecurrenceId> iterator = services.getService(RecurrenceService.class).iterateRecurrenceIds(recurrenceData);
            if (iterator.hasNext()) {
                calendar.setTimeInMillis(iterator.next().getValue().getTimestamp());
                calendar.set(Calendar.HOUR_OF_DAY, startHour);
                calendar.set(Calendar.MINUTE, startMinute);
                calendar.set(Calendar.SECOND, startSecond);
                return isAllDay(delegate) ? new DateTime(calendar.getTimeInMillis()).toAllDay() : new DateTime(timeZone, calendar.getTimeInMillis());
            } else {
                return startDate;
            }
        } catch (OXException e) {
            throw unsupportedDataError(EventField.START_DATE, ProblemSeverity.CRITICAL, "Error deriving stored start date", e);
        }
    }

    @Override
    public DateTime getEndDate() {
        DateTime endDate = delegate.getEndDate();
        if (null == endDate || false == isSeriesMaster(delegate)) {
            return endDate;
        }
        /*
         * expand recurrence master end-date to cover the whole series period
         */
        try {
            /*
             * remember time fraction of actual start- and end-date
             */
            DateTime seriesStart = delegate.getStartDate();
            TimeZone timeZone = seriesStart.isFloating() ? TimeZones.UTC : seriesStart.getTimeZone();
            Calendar calendar = initCalendar(timeZone, seriesStart.getTimestamp());
            int startHour = calendar.get(Calendar.HOUR_OF_DAY);
            int startMinute = calendar.get(Calendar.MINUTE);
            int startSecond = calendar.get(Calendar.SECOND);
            calendar.setTimeInMillis(endDate.getTimestamp());
            int endHour = calendar.get(Calendar.HOUR_OF_DAY);
            int endMinute = calendar.get(Calendar.MINUTE);
            int endSecond = calendar.get(Calendar.SECOND);
            /*
             * iterate recurrence and take over start date of first occurrence
             */
            DefaultRecurrenceData recurrenceData = new DefaultRecurrenceData(delegate.getRecurrenceRule(), seriesStart, null);
            Date start;
            Iterator<RecurrenceId> iterator = services.getService(RecurrenceService.class).iterateRecurrenceIds(recurrenceData);
            if (iterator.hasNext()) {
                calendar.setTimeInMillis(iterator.next().getValue().getTimestamp());
                calendar.set(Calendar.HOUR_OF_DAY, startHour);
                calendar.set(Calendar.MINUTE, startMinute);
                calendar.set(Calendar.SECOND, startSecond);
                start = calendar.getTime();
            } else {
                start = new Date(delegate.getStartDate().getTimestamp());
            }
            /*
             * iterate recurrence and take over end date of "last" occurrence
             */
            long millis = delegate.getEndDate().getTimestamp();
            for (int i = 1; i <= SeriesPattern.MAX_OCCURRENCESE && iterator.hasNext(); millis = iterator.next().getValue().getTimestamp(), i++) {
                ;
            }
            calendar.setTimeInMillis(millis);
            calendar.set(Calendar.HOUR_OF_DAY, endHour);
            calendar.set(Calendar.MINUTE, endMinute);
            calendar.set(Calendar.SECOND, endSecond);
            calendar.add(Calendar.DAY_OF_YEAR, (int) new Period(delegate).getTotalDays());
            Date end = calendar.getTime();
            /*
             * adjust end date if it falls into other timezone observance with different offset, just like it's done at
             * com.openexchange.calendar.CalendarOperation.calculateImplictEndOfSeries(CalendarDataObject, String, boolean)
             */
            int startOffset = timeZone.getOffset(start.getTime());
            int endOffset = timeZone.getOffset(end.getTime());
            if (startOffset != endOffset) {
                end.setTime(end.getTime() + endOffset - startOffset);
            }
            return isAllDay(delegate) ? new DateTime(end.getTime()).toAllDay() : new DateTime(timeZone, end.getTime());
        } catch (OXException e) {
            throw unsupportedDataError(EventField.END_DATE, ProblemSeverity.CRITICAL, "Error deriving stored end date", e);
        }
    }

    /**
     * Initializes a new {@link CalendarExceptionCodes#UNSUPPORTED_DATA} error, wrapped within an {@link UnsupportedOperationException}
     * for this stored event.
     *
     * @param field The corresponding event field of the unsupported data
     * @param severity The problem severity
     * @param message The message providing details of the error
     * @param cause The optional initial cause
     * @return The initialized unsupported operation exception
     */
    private UnsupportedOperationException unsupportedDataError(EventField field, ProblemSeverity severity, String message, Throwable cause) {
        return new UnsupportedOperationException(eventStorage.getUnsupportedDataError(getId(), field, severity, message, cause));
    }

    private static SortedSet<RecurrenceId> getRecurrenceIds(List<Date> dates) {
        if (null == dates) {
            return null;
        }
        SortedSet<RecurrenceId> recurrenceIds = new TreeSet<RecurrenceId>();
        for (Date date : dates) {
            recurrenceIds.add(new DefaultRecurrenceId(new DateTime(date.getTime())));
        }
        return recurrenceIds;
    }

}
