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

package com.openexchange.chronos.storage.rdb;

import static com.openexchange.chronos.common.CalendarUtils.initCalendar;
import static com.openexchange.chronos.common.CalendarUtils.isFloating;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
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
     * @param event The event to adjust
     * @return The (possibly adjusted) event reference
     */
    public static Event adjustAfterLoad(Event event) throws OXException {
        if (event.containsRecurrenceRule() && null != event.getRecurrenceRule()) {
            /*
             * extract series pattern and "absolute duration" / "recurrence calculator" field
             */
            String value = event.getRecurrenceRule();
            int idx = value.indexOf('~');
            int absoluteDuration = Integer.parseInt(value.substring(0, idx));
            String databasePattern = value.substring(idx + 1);
            String timeZone = null != event.getStartTimeZone() ? event.getStartTimeZone() : "UTC";
            boolean allDay = event.isAllDay();
            /*
             * convert legacy series pattern into proper recurrence rule
             */
            SeriesPattern seriesPattern = new SeriesPattern(databasePattern, timeZone, allDay);
            RecurrenceData recurrenceData = Appointment2Event.getRecurrenceData(seriesPattern);
            if (isSeriesMaster(event)) {
                /*
                 * apply recurrence rule & adjust the recurrence master's actual start- and enddate
                 */
                event.setRecurrenceRule(recurrenceData.getRecurrenceRule());
                Period seriesPeriod = new Period(event);
                Period masterPeriod = getRecurrenceMasterPeriod(seriesPeriod, absoluteDuration);
                event.setStartDate(masterPeriod.getStartDate());
                event.setEndDate(masterPeriod.getEndDate());
                /*
                 * transform legacy "recurrence date positions" for exceptions to recurrence ids
                 */
                if (event.containsDeleteExceptionDates() && null != event.getDeleteExceptionDates()) {
                    event.setDeleteExceptionDates(Appointment2Event.getRecurrenceIDs(Services.getService(RecurrenceService.class), recurrenceData, getDates(event.getDeleteExceptionDates())));
                }
                if (event.containsChangeExceptionDates() && null != event.getChangeExceptionDates()) {
                    event.setChangeExceptionDates(Appointment2Event.getRecurrenceIDs(Services.getService(RecurrenceService.class), recurrenceData, getDates(event.getChangeExceptionDates())));
                }
            } else if (isSeriesException(event)) {
                /*
                 * drop recurrence information for change exceptions
                 */
                event.removeRecurrenceRule();
                /*
                 * transform change exception's legacy "recurrence date position" to recurrence id & apply actual recurrence id
                 */
                if (event.containsRecurrenceId() && null != event.getRecurrenceId() && StoredRecurrenceId.class.isInstance(event.getRecurrenceId())) {
                    event.setRecurrenceId(Appointment2Event.getRecurrenceID(Services.getService(RecurrenceService.class), recurrenceData, ((StoredRecurrenceId) event.getRecurrenceId()).getRecurrencePosition()));
                }
                if (event.containsChangeExceptionDates() && null != event.getChangeExceptionDates()) {
                    event.setChangeExceptionDates(Appointment2Event.getRecurrenceIDs(Services.getService(RecurrenceService.class), recurrenceData, getDates(event.getChangeExceptionDates())));
                }
            }
        }
        /*
         * take over timezone
         */
        if (event.containsStartTimeZone()) {
            event.setEndTimeZone(event.getStartTimeZone());
        }

        return event;
    }

    /**
     * Adjusts certain properties of an event prior inserting it into the database.
     *
     * @param event The event to adjust
     * @param connection The connection to use
     * @param contextID The context identifier
     * @return The adjusted event data to store
     */
    public static Event adjustPriorSave(Connection connection, int contextID, Event event) throws OXException, SQLException {
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
                Period seriesPeriod = getImplicitSeriesPeriod(Services.getService(RecurrenceService.class), event);
                eventData.setStartDate(seriesPeriod.getStartDate());
                eventData.setEndDate(seriesPeriod.getEndDate());
            }
        }
        if (isSeriesException(eventData)) {
            RecurrenceData recurrenceData;
            if (null != eventData.getRecurrenceId() && RecurrenceData.class.isInstance(eventData.getRecurrenceId())) {
                recurrenceData = (RecurrenceData) eventData.getRecurrenceId();
            } else {
                recurrenceData = RdbEventStorage.selectRecurrenceData(connection, contextID, eventData.getSeriesId(), false);
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
                if (PositionAwareRecurrenceId.class.isInstance(eventData.getRecurrenceId())) {
                    recurrencePosition = ((PositionAwareRecurrenceId) eventData.getRecurrenceId()).getRecurrencePosition();
                } else {
                    recurrencePosition = Event2Appointment.getRecurrencePosition(Services.getService(RecurrenceService.class), recurrenceData, eventData.getRecurrenceId());
                }
                eventData.setRecurrenceId(new StoredRecurrenceId(recurrencePosition));
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
        if (eventData.containsChangeExceptionDates()) {
            eventData.setChangeExceptionDates(getRecurrenceIds(Event2Appointment.getRecurrenceDatePositions(eventData.getChangeExceptionDates())));
        }
        return eventData;
    }

    private static List<Date> getDates(SortedSet<RecurrenceId> recurrenceIds) {
        if (null == recurrenceIds) {
            return null;
        }
        List<Date> dates = new ArrayList<Date>(recurrenceIds.size());
        for (RecurrenceId recurrenceId : recurrenceIds) {
            dates.add(new Date(recurrenceId.getValue()));
        }
        return dates;
    }

    private static SortedSet<RecurrenceId> getRecurrenceIds(List<Date> dates) {
        if (null == dates) {
            return null;
        }
        SortedSet<RecurrenceId> recurrenceIds = new TreeSet<RecurrenceId>();
        for (Date date : dates) {
            recurrenceIds.add(new DefaultRecurrenceId(date));
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
        TimeZone timeZone = isFloating(seriesMaster) || null == seriesMaster.getStartTimeZone() ? TimeZones.UTC : TimeZone.getTimeZone(seriesMaster.getStartTimeZone());
        Calendar calendar = initCalendar(timeZone, seriesMaster.getStartDate());
        int startHour = calendar.get(Calendar.HOUR_OF_DAY);
        int startMinute = calendar.get(Calendar.MINUTE);
        int startSecond = calendar.get(Calendar.SECOND);
        calendar.setTime(seriesMaster.getEndDate());
        int endHour = calendar.get(Calendar.HOUR_OF_DAY);
        int endMinute = calendar.get(Calendar.MINUTE);
        int endSecond = calendar.get(Calendar.SECOND);
        /*
         * iterate recurrence and take over start date of first occurrence
         */
        Date startDate;
        Iterator<RecurrenceId> iterator = recurrenceService.iterateRecurrenceIds(new DefaultRecurrenceData(seriesMaster), null, null);
        if (iterator.hasNext()) {
            calendar.setTimeInMillis(iterator.next().getValue());
            calendar.set(Calendar.HOUR_OF_DAY, startHour);
            calendar.set(Calendar.MINUTE, startMinute);
            calendar.set(Calendar.SECOND, startSecond);
            startDate = calendar.getTime();
        } else {
            startDate = seriesMaster.getStartDate();
        }
        /*
         * iterate recurrence and take over end date of "last" occurrence
         */
        long millis = seriesMaster.getEndDate().getTime();
        for (int i = 1; i <= SeriesPattern.MAX_OCCURRENCESE && iterator.hasNext(); millis = iterator.next().getValue(), i++)
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
        return new Period(startDate, endDate, seriesMaster.isAllDay());
    }

}
