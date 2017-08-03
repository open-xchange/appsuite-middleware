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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.compat.Appointment2Event.getRecurrenceData;
import static com.openexchange.chronos.compat.Appointment2Event.getRecurrenceID;
import static com.openexchange.chronos.compat.Event2Appointment.asInt;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Period;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.chronos.compat.PositionAwareRecurrenceId;
import com.openexchange.chronos.compat.SeriesPattern;
import com.openexchange.chronos.exception.ProblemSeverity;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.rdb.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.TimeZones;

/**
 * {@link Compat}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Compat {

    /**
     * Initializes a new {@link Compat}.
     */
    private Compat() {
        super();
    }


    /**
     * Adjusts certain properties of an event after loading it from the database.
     * <p/>
     * <b>Note:</b> This method requires that the properties {@link EventField#ALL_DAY}, {@link EventField#RECURRENCE_RULE},
     * {@link EventField#START_TIMEZONE}, {@link EventField#START_DATE} and {@link EventField#END_DATE} were loaded.
     *
     * @param eventStorage The associated event storage
     * @param connection The connection to use
     * @param event The event to adjust
     * @return The (possibly adjusted) event reference
     */
    public static Event adjustAfterLoad(RdbEventStorage eventStorage, Connection connection, Event event) throws OXException, SQLException {
        if (event.containsEndDate() && null != event.getEndDate() && null != event.getStartDate()) {
            /*
             * take over 'all-day' character & timezone from start date
             */
            if (event.getStartDate().isAllDay()) {
                event.setEndDate(event.getEndDate().toAllDay());
            } else if (null != event.getStartDate().getTimeZone()) {
                event.setEndDate(new DateTime(event.getStartDate().getTimeZone(), event.getEndDate().getTimestamp()));
            } else {
                event.setEndDate(new DateTime(null, event.getEndDate().getTimestamp()));
            }
        }
        if (isSeriesMaster(event)) {
            event = adjustRecurrenceForMasterAfterLoad(eventStorage, event);
        } else if (isSeriesException(event)) {
            event = adjustRecurrenceForExceptionAfterLoad(eventStorage, connection, event);
        } else if (null != event.getId() && event.getId().equals(event.getSeriesId())) {
            /*
             * ensure to remove recurrence remnants for events that used to be a series, but are no longer
             */
            event.removeSeriesId();
            event.removeRecurrenceId();
            event.removeDeleteExceptionDates();
        }
        /*
         * enhance organizer with static properties
         */
        if (event.containsOrganizer() && null != event.getOrganizer() && null != eventStorage.getEntityResolver()) {
            event.setOrganizer(eventStorage.getEntityResolver().applyEntityData(event.getOrganizer(), CalendarUserType.INDIVIDUAL));
        }
        /*
         * derive calendar user based on present public folder and created by info
         */
        if (event.containsFolderId()) {
            if (null == event.getFolderId() || "0".equals(event.getFolderId())) {
                /*
                 * event from personal calendar, take over calendar user from created by
                 */
                event.setCalendarUser(event.getCreatedBy());
            } else {
                /*
                 * event in public folder, assume no specific calendar user
                 */
                event.setCalendarUser(0);
            }
        }
        /*
         * take over last-modified from timestamp
         */
        if (event.containsTimestamp()) {
            event.setLastModified(new Date(event.getTimestamp()));
        }
        return event;
    }


    /**
     * Adjusts certain properties of an event prior inserting it into the database.
     *
     * @param eventStorage The associated event storage
     * @param connection The connection to use
     * @param event The event to adjust
     * @return The adjusted event data to store
     */
    public static Event adjustPriorInsert(RdbEventStorage eventStorage, Connection connection, Event event) throws OXException, SQLException {
        Event eventData = adjustPriorSave(eventStorage, connection, event);
        /*
         * derive created- / modified-by from calendar user if required
         */
        if (false == eventData.containsCreatedBy()) {
            eventData.setCreatedBy(eventData.getCalendarUser());
        }
        if (false == eventData.containsModifiedBy()) {
            eventData.setModifiedBy(eventData.getCalendarUser());
        }
        return eventData;
    }

    /**
     * Adjusts certain properties of an event prior updating it in the database.
     *
     * @param eventStorage The associated event storage
     * @param connection The connection to use
     * @param event The event to adjust
     * @return The adjusted event data to store
     */
    public static Event adjustPriorUpdate(RdbEventStorage eventStorage, Connection connection, Event event) throws OXException, SQLException {
        return adjustPriorSave(eventStorage, connection, event);
    }

    private static Event adjustPriorSave(RdbEventStorage eventStorage, Connection connection, Event event) throws OXException, SQLException {
        /*
         * prepare event data for insert
         */
        Event eventData = new Event();
        EventMapper.getInstance().copy(event, eventData, EventMapper.getInstance().getMappedFields());
        if (isSeriesMaster(eventData)) {
            RecurrenceData recurrenceData = new DefaultRecurrenceData(eventData);
            if (eventData.containsRecurrenceRule() && null != eventData.getRecurrenceRule()) {
                /*
                 * convert recurrence rule to legacy pattern & derive "absolute duration" / "recurrence calculator" field
                 */
                SeriesPattern seriesPattern = Event2Appointment.getSeriesPattern(Services.getService(RecurrenceService.class), recurrenceData);
                long absoluteDuration = new Period(eventData).getTotalDays();
                eventData.setRecurrenceRule(absoluteDuration + "~" + seriesPattern.getDatabasePattern());
            }
            if (eventData.containsStartDate() || eventData.containsEndDate()) {
                /*
                 * expand recurrence master start- and enddate to cover the whole series period
                 */
                TimeZone timeZone = recurrenceData.getSeriesStart().getTimeZone();
                Period seriesPeriod = getImplicitSeriesPeriod(Services.getService(RecurrenceService.class), event);
                if (seriesPeriod.isAllDay()) {
                    eventData.setStartDate(new DateTime(seriesPeriod.getStartDate().getTime()).toAllDay());
                    eventData.setEndDate(new DateTime(seriesPeriod.getEndDate().getTime()).toAllDay());
                } else {
                    eventData.setStartDate(new DateTime(timeZone, seriesPeriod.getStartDate().getTime()));
                    eventData.setEndDate(new DateTime(timeZone, seriesPeriod.getEndDate().getTime()));
                }
            }
        }
        if (isSeriesException(eventData)) {
            RecurrenceData recurrenceData;
            if (null != eventData.getRecurrenceId() && RecurrenceData.class.isInstance(eventData.getRecurrenceId())) {
                recurrenceData = (RecurrenceData) eventData.getRecurrenceId();
            } else {
                recurrenceData = eventStorage.selectRecurrenceData(connection, asInt(eventData.getSeriesId()), false);
            }
            if (eventData.containsRecurrenceRule() && null != eventData.getRecurrenceRule()) {
                // TODO really required to also store series pattern for exceptions?
                /*
                 * convert recurrence rule to legacy pattern & derive "absolute duration" / "recurrence calculator" field
                 */
                SeriesPattern seriesPattern = Event2Appointment.getSeriesPattern(Services.getService(RecurrenceService.class), recurrenceData);
                long absoluteDuration = new Period(eventData).getTotalDays();
                eventData.setRecurrenceRule(absoluteDuration + "~" + seriesPattern.getDatabasePattern());
            }
            /*
             * transform recurrence ids to legacy "recurrence date positions" (UTC dates with truncated time fraction)
             */
            if (eventData.containsRecurrenceId() && null != eventData.getRecurrenceId()) {
                int recurrencePosition;
                Date recurrenceDatePosition;
                if (PositionAwareRecurrenceId.class.isInstance(eventData.getRecurrenceId())) {
                    recurrencePosition = ((PositionAwareRecurrenceId) eventData.getRecurrenceId()).getRecurrencePosition();
                    recurrenceDatePosition = ((PositionAwareRecurrenceId) eventData.getRecurrenceId()).getRecurrenceDatePosition();
                } else {
                    recurrencePosition = Event2Appointment.getRecurrencePosition(Services.getService(RecurrenceService.class), recurrenceData, eventData.getRecurrenceId());
                    recurrenceDatePosition = Event2Appointment.getRecurrenceDatePosition(eventData.getRecurrenceId());
                }
                eventData.setRecurrenceId(new StoredRecurrenceId(recurrencePosition, recurrenceDatePosition.getTime()));
            }
        }
        /*
         * truncate milliseconds from creation date to avoid bad rounding in MySQL versions >= 5.6.4.
         * See: http://dev.mysql.com/doc/refman/5.6/en/fractional-seconds.html
         * See: com.openexchange.sql.tools.SQLTools.toTimestamp(Date)
         */
        if (eventData.containsCreated() && null != eventData.getCreated()) {
            eventData.setCreated(new Date((eventData.getCreated().getTime() / 1000) * 1000));
        }
        if (eventData.containsDeleteExceptionDates()) {
            eventData.setDeleteExceptionDates(getRecurrenceIds(Event2Appointment.getRecurrenceDatePositions(eventData.getDeleteExceptionDates())));
        }
        return eventData;
    }

    private static Event adjustRecurrenceForMasterAfterLoad(RdbEventStorage eventStorage, Event event) throws OXException {
        RecurrenceData recurrenceData = null;
        if (null != event.getRecurrenceRule()) {
            /*
             * convert legacy series pattern into proper recurrence rule after extracting
             * series pattern and "absolute duration" / "recurrence calculator" field
             */
            TimeZone timeZone = null != event.getStartDate().getTimeZone() ? event.getStartDate().getTimeZone() : TimeZones.UTC;
            int absoluteDuration = 0;
            try {
                int idx = event.getRecurrenceRule().indexOf('~');
                absoluteDuration = Integer.parseInt(event.getRecurrenceRule().substring(0, idx));
                String databasePattern = event.getRecurrenceRule().substring(idx + 1);
                recurrenceData = getRecurrenceData(new SeriesPattern(databasePattern), timeZone, event.getStartDate().isAllDay());
            } catch (IllegalArgumentException | OXException e) {
                String message = "Ignoring invalid legacy series pattern \"" + event.getRecurrenceRule() + '"';
                eventStorage.addInvalidDataWaring(event.getId(), EventField.RECURRENCE_RULE, ProblemSeverity.MAJOR, message, e);
            }
            /*
             * adjust the recurrence master's actual start- and enddate
             */
            Period seriesPeriod = new Period(event);
            Period masterPeriod = getRecurrenceMasterPeriod(seriesPeriod, absoluteDuration);
            if (masterPeriod.isAllDay()) {
                event.setStartDate(new DateTime(masterPeriod.getStartDate().getTime()).toAllDay());
                event.setEndDate(new DateTime(masterPeriod.getEndDate().getTime()).toAllDay());
            } else {
                event.setStartDate(new DateTime(event.getStartDate().getTimeZone(), masterPeriod.getStartDate().getTime()));
                event.setEndDate(new DateTime(event.getEndDate().getTimeZone(), masterPeriod.getEndDate().getTime()));
            }
        }
        if (null != recurrenceData) {
            /*
             * apply recurrence rule & transform legacy "recurrence date positions" for exceptions to recurrence ids
             */
            event.setRecurrenceRule(recurrenceData.getRecurrenceRule());
            if (event.containsDeleteExceptionDates() && null != event.getDeleteExceptionDates()) {
                event.setDeleteExceptionDates(getRecurrenceIDs(eventStorage, event.getId(), recurrenceData, getDates(event.getDeleteExceptionDates()), EventField.DELETE_EXCEPTION_DATES));
            }
        } else {
            /*
             * ensure to remove recurrence remnants for malformed recurrence data, or events that used to be a series, but are no longer
             */
            event.removeRecurrenceRule();
            event.removeSeriesId();
            event.removeRecurrenceId();
            event.removeDeleteExceptionDates();
        }
        return event;
    }

    private static Event adjustRecurrenceForExceptionAfterLoad(RdbEventStorage eventStorage, Connection connection, Event event) throws OXException, SQLException {
        /*
         * drop recurrence information for change exceptions
         */
        event.removeRecurrenceRule();
        /*
         * transform change exception's legacy "recurrence date position" to recurrence id & apply actual recurrence id
         */
        if (event.containsRecurrenceId() && null != event.getRecurrenceId() && StoredRecurrenceId.class.isInstance(event.getRecurrenceId())) {
            /*
             * load recurrence data from storage for further processing of change exception
             */
            RecurrenceId recurrenceId = null;
            RecurrenceData recurrenceData = eventStorage.selectRecurrenceData(connection, asInt(event.getSeriesId()), false);
            if (null != recurrenceData) {
                int recurrencePosition = ((StoredRecurrenceId) event.getRecurrenceId()).getRecurrencePosition();
                try {
                    recurrenceId = getRecurrenceID(Services.getService(RecurrenceService.class), recurrenceData, recurrencePosition);
                } catch (OXException e) {
                    if (false == "CAL-4061".equals(e.getErrorCode())) {
                        throw e;
                    }
                    eventStorage.addInvalidDataWaring(event.getId(), EventField.RECURRENCE_ID, ProblemSeverity.MINOR, "Skipping invalid recurrence position \"" + recurrencePosition + '"', e);
                }
            }
            if (null != recurrenceId) {
                /*
                 * take over valid recurrence id
                 */
                event.setRecurrenceId(recurrenceId);
            } else {
                /*
                 * ensure to remove recurrence remnants for malformed recurrence data
                 */
                event.removeSeriesId();
                event.removeRecurrenceId();
            }
        }
        return event;
    }

    private static List<Date> getDates(SortedSet<RecurrenceId> recurrenceIds) {
        if (null == recurrenceIds) {
            return null;
        }
        List<Date> dates = new ArrayList<Date>(recurrenceIds.size());
        for (RecurrenceId recurrenceId : recurrenceIds) {
            dates.add(new Date(recurrenceId.getValue().getTimestamp()));
        }
        return dates;
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

    /**
     * Calculates the actual start- and end-date of the "master" recurrence for a specific series pattern, i.e. the start- and end-date of
     * a serie's first occurrence.
     *
     * @param seriesPeriod The implicit series period, i.e. the period spanning from the first until the "last" occurrence
     * @param absoluteDuration The absolute duration of one occurrence in days (the legacy "recurrence calculator" value)
     * @return The actual start- and end-date of the recurrence master, wrapped into a {@link Period} structure
     */
    private static Period getRecurrenceMasterPeriod(Period seriesPeriod, int absoluteDuration) {
        /*
         * determine "date" fraction of series start
         */
        Calendar calendar = initCalendar(TimeZones.UTC, seriesPeriod.getStartDate());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DATE);
        Date startDate = calendar.getTime();
        /*
         * apply same "date" fraction to series end
         */
        calendar.setTime(seriesPeriod.getEndDate());
        calendar.set(year, month, date);
        /*
         * adjust end date considering absolute duration
         */
        if (calendar.getTime().before(startDate)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        calendar.add(Calendar.DAY_OF_YEAR, absoluteDuration);
        Date endDate = calendar.getTime();
        return new Period(startDate, endDate, seriesPeriod.isAllDay());
    }

    /**
     * Calculates the implicit start- and end-date of a recurring event series, i.e. the period spanning from the first until the "last"
     * occurrence.
     *
     * @param recurrenceData The recurrence data
     * @param masterPeriod The actual start- and end-date of the recurrence master, wrapped into a {@link Period} structure
     * @return The implicit period of a recurring event series
     */
    private static Period getImplicitSeriesPeriod(RecurrenceService recurrenceService, Event seriesMaster) throws OXException {
        /*
         * remember time fraction of actual start- and end-date
         */
        DateTime seriesStart = seriesMaster.getStartDate();
        TimeZone timeZone = seriesStart.isFloating() ? TimeZones.UTC : seriesStart.getTimeZone();
        Calendar calendar = initCalendar(timeZone, seriesStart.getTimestamp());
        int startHour = calendar.get(Calendar.HOUR_OF_DAY);
        int startMinute = calendar.get(Calendar.MINUTE);
        int startSecond = calendar.get(Calendar.SECOND);
        calendar.setTimeInMillis(seriesMaster.getEndDate().getTimestamp());
        int endHour = calendar.get(Calendar.HOUR_OF_DAY);
        int endMinute = calendar.get(Calendar.MINUTE);
        int endSecond = calendar.get(Calendar.SECOND);
        /*
         * iterate recurrence and take over start date of first occurrence
         */
        Date startDate;
        Iterator<RecurrenceId> iterator = recurrenceService.iterateRecurrenceIds(new DefaultRecurrenceData(seriesMaster));
        if (iterator.hasNext()) {
            calendar.setTimeInMillis(iterator.next().getValue().getTimestamp());
            calendar.set(Calendar.HOUR_OF_DAY, startHour);
            calendar.set(Calendar.MINUTE, startMinute);
            calendar.set(Calendar.SECOND, startSecond);
            startDate = calendar.getTime();
        } else {
            startDate = new Date(seriesMaster.getStartDate().getTimestamp());
        }
        /*
         * iterate recurrence and take over end date of "last" occurrence
         */
        long millis = seriesMaster.getEndDate().getTimestamp();
        for (int i = 1; i <= SeriesPattern.MAX_OCCURRENCESE && iterator.hasNext(); millis = iterator.next().getValue().getTimestamp(), i++)
            ;
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, endHour);
        calendar.set(Calendar.MINUTE, endMinute);
        calendar.set(Calendar.SECOND, endSecond);
        calendar.add(Calendar.DAY_OF_YEAR, (int) new Period(seriesMaster).getTotalDays());
        Date endDate = calendar.getTime();
        /*
         * adjust end date if it falls into other timezone observance with different offset, just like it's done at
         * com.openexchange.calendar.CalendarOperation.calculateImplictEndOfSeries(CalendarDataObject, String, boolean)
         */
        int startOffset = timeZone.getOffset(startDate.getTime());
        int endOffset = timeZone.getOffset(endDate.getTime());
        if (startOffset != endOffset) {
            endDate.setTime(endDate.getTime() + endOffset - startOffset);
        }
        return new Period(startDate, endDate, seriesMaster.getStartDate().isAllDay());
    }

    /**
     * Calculates the recurrence identifiers, i.e. the start times of the specific occurrences of a recurring event, for a list of legacy
     * recurrence date position. Invalid recurrence date positions are skipped silently.
     *
     * @param eventStorage The associated event storage
     * @param event The event being adjusted
     * @param recurrenceData The corresponding recurrence data
     * @param recurrenceDatePositions The legacy recurrence date positions, i.e. the dates where the original occurrences would have been,
     *            as UTC date with truncated time fraction
     * @param field The event field being processed
     * @return The recurrence identifiers
     */
    private static SortedSet<RecurrenceId> getRecurrenceIDs(RdbEventStorage eventStorage, String eventId, RecurrenceData recurrenceData, Collection<Date> recurrenceDatePositions, EventField field) throws OXException {
        RecurrenceService recurrenceService = Services.getService(RecurrenceService.class);
        try {
            return Appointment2Event.getRecurrenceIDs(recurrenceService, recurrenceData, recurrenceDatePositions);
        } catch (OXException e) {
            if (false == "CAL-4061".equals(e.getErrorCode())) {
                throw e;
            }
        }
        /*
         * invalid recurrence id, fallback & convert as much as possible, one after another
         */
        SortedSet<RecurrenceId> recurrenceIDs = new TreeSet<RecurrenceId>();
        for (Date date : recurrenceDatePositions) {
            try {
                recurrenceIDs.add(getRecurrenceID(recurrenceService, recurrenceData, date));
            } catch (OXException e) {
                if (false == "CAL-4061".equals(e.getErrorCode())) {
                    throw e;
                }
                eventStorage.addInvalidDataWaring(eventId, field, ProblemSeverity.MINOR, "Skipping invalid recurrence date position \"" + date.getTime() + '"', e);
            }
        }
        return recurrenceIDs;
    }

}
