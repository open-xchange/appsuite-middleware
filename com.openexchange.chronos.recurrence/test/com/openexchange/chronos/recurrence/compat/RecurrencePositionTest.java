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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.recurrence.compat;

import static com.openexchange.time.TimeTools.D;
import static org.junit.Assert.assertEquals;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.junit.Test;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.chronos.recurrence.TestRecurrenceConfig;
import com.openexchange.chronos.recurrence.service.RecurrenceServiceImpl;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.java.util.TimeZones;

/**
 * {@link RecurrencePositionTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RecurrencePositionTest {

    private RecurrenceService recurrenceService = new RecurrenceServiceImpl(new TestRecurrenceConfig());

    @Test
    public void testPacificHonolulu() throws Exception {
        test("Pacific/Honolulu");
    }

    @Test
    public void testPacificSamoa() throws Exception {
        test("Pacific/Samoa");
    }

    @Test
    public void testAmericaNewYork() throws Exception {
        test("America/New_York");
    }

    @Test
    public void testAsiaMacao() throws Exception {
        test("Asia/Macao");
    }

    @Test
    public void testPacificNauro() throws Exception {
        test("Pacific/Nauro");
    }

    @Test
    public void testPacificApia() throws Exception {
        test("Pacific/Apia");
    }

    private void test(String timeZoneID) throws Exception {
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneID);
        /*
         * test non-all day appointments
         */
        testAtMidnight(timeZone);
        testAtNoon(timeZone);
        testUntilMidnight(timeZone);
        testOverMidnight(timeZone);
        testOneDay(timeZone);
        /*
         * test all-day appointments
         */
        testAllDay(timeZone);
        testAllDayTwoDays(timeZone);
    }

    private void testAllDay(TimeZone timeZone) throws Exception {
        /*
         * prepare all day event series
         */
        Event event = getAllDayEventSeries("FREQ=DAILY", "today at 00:00:00", "tomorrow at 00:00:00");
        DefaultRecurrenceData recurrenceData = new DefaultRecurrenceData(event.getRecurrenceRule(), event.getStartDate(), null);
        /*
         * check recurrence position conversion
         */
        int position = 8;
        RecurrenceId recurrenceID = Appointment2Event.getRecurrenceID(recurrenceService, recurrenceData, position);
        assertEquals(position, Event2Appointment.getRecurrencePosition(recurrenceService, recurrenceData, new DefaultRecurrenceId(recurrenceID.getValue())));
        /*
         * check recurrence date position conversion
         */
        Calendar calendar = CalendarUtils.initCalendar(TimeZones.UTC, event.getStartDate().getTimestamp());
        calendar.add(Calendar.DATE, 17);
        Date datePosition = calendar.getTime();
        recurrenceID = Appointment2Event.getRecurrenceID(recurrenceService, recurrenceData, datePosition);
        assertEquals(datePosition, Event2Appointment.getRecurrenceDatePosition(new DefaultRecurrenceId(recurrenceID.getValue())));
    }

    private void testAllDayTwoDays(TimeZone timeZone) throws Exception {
        /*
         * prepare all day event series
         */
        Event event = getAllDayEventSeries("FREQ=WEEKLY", "yesterday at 00:00:00", "tomorrow at 00:00:00");
        DefaultRecurrenceData recurrenceData = new DefaultRecurrenceData(event.getRecurrenceRule(), event.getStartDate(), null);
        /*
         * check recurrence position conversion
         */
        int position = 35;
        RecurrenceId recurrenceID = Appointment2Event.getRecurrenceID(recurrenceService, recurrenceData, position);
        assertEquals(position, Event2Appointment.getRecurrencePosition(recurrenceService, recurrenceData, recurrenceID));
        /*
         * check recurrence date position conversion
         */
        Calendar calendar = CalendarUtils.initCalendar(TimeZones.UTC, event.getStartDate().getTimestamp());
        calendar.add(Calendar.WEEK_OF_YEAR, 8);
        Date datePosition = calendar.getTime();
        recurrenceID = Appointment2Event.getRecurrenceID(recurrenceService, recurrenceData, datePosition);
        assertEquals(datePosition, Event2Appointment.getRecurrenceDatePosition(new DefaultRecurrenceId(recurrenceID.getValue())));
    }

    private void testAtMidnight(TimeZone timeZone) throws Exception {
        /*
         * prepare event series
         */
        Event event = getEventSeries("FREQ=DAILY", timeZone, "tomorrow at 00:00:00", "tomorrow at 01:00:00");
        DefaultRecurrenceData recurrenceData = new DefaultRecurrenceData(event.getRecurrenceRule(), event.getStartDate(), null);
        /*
         * check recurrence position conversion
         */
        int position = 8;
        RecurrenceId recurrenceID = Appointment2Event.getRecurrenceID(recurrenceService, recurrenceData, position);
        assertEquals(position, Event2Appointment.getRecurrencePosition(recurrenceService, recurrenceData, new DefaultRecurrenceId(recurrenceID.getValue())));
        /*
         * check recurrence date position conversion
         */
        Calendar calendar = CalendarUtils.initCalendar(timeZone, event.getStartDate().getTimestamp());
        calendar.add(Calendar.DATE, 17);
        Date datePosition = CalendarUtils.truncateTime(calendar.getTime(), TimeZones.UTC);
        recurrenceID = Appointment2Event.getRecurrenceID(recurrenceService, recurrenceData, datePosition);
        assertEquals(datePosition, Event2Appointment.getRecurrenceDatePosition(new DefaultRecurrenceId(recurrenceID.getValue())));
    }

    private void testAtNoon(TimeZone timeZone) throws Exception {
        /*
         * prepare event series
         */
        Event event = getEventSeries("FREQ=DAILY", timeZone, "tomorrow at 12:00:00", "tomorrow at 13:00:00");
        DefaultRecurrenceData recurrenceData = new DefaultRecurrenceData(event.getRecurrenceRule(), event.getStartDate(), null);
        /*
         * check recurrence position conversion
         */
        int position = 8;
        RecurrenceId recurrenceID = Appointment2Event.getRecurrenceID(recurrenceService, recurrenceData, position);
        assertEquals(position, Event2Appointment.getRecurrencePosition(recurrenceService, recurrenceData, new DefaultRecurrenceId(recurrenceID.getValue())));
        /*
         * check recurrence date position conversion
         */
        Calendar calendar = CalendarUtils.initCalendar(timeZone, event.getStartDate().getTimestamp());
        calendar.add(Calendar.DATE, 17);
        Date datePosition = CalendarUtils.truncateTime(calendar.getTime(), TimeZones.UTC);
        recurrenceID = Appointment2Event.getRecurrenceID(recurrenceService, recurrenceData, datePosition);
        assertEquals(datePosition, Event2Appointment.getRecurrenceDatePosition(new DefaultRecurrenceId(recurrenceID.getValue())));
    }

    private void testUntilMidnight(TimeZone timeZone) throws Exception {
        /*
         * prepare event series
         */
        Event event = getEventSeries("FREQ=DAILY", timeZone, "today at 23:00:00", "tomorrow at 00:00:00");
        DefaultRecurrenceData recurrenceData = new DefaultRecurrenceData(event.getRecurrenceRule(), event.getStartDate(), null);
        /*
         * check recurrence position conversion
         */
        int position = 8;
        RecurrenceId recurrenceID = Appointment2Event.getRecurrenceID(recurrenceService, recurrenceData, position);
        assertEquals(position, Event2Appointment.getRecurrencePosition(recurrenceService, recurrenceData, new DefaultRecurrenceId(recurrenceID.getValue())));
        /*
         * check recurrence date position conversion
         */
        Calendar calendar = CalendarUtils.initCalendar(timeZone, event.getStartDate().getTimestamp());
        calendar.add(Calendar.DATE, 17);
        Date datePosition = CalendarUtils.truncateTime(calendar.getTime(), TimeZones.UTC);
        recurrenceID = Appointment2Event.getRecurrenceID(recurrenceService, recurrenceData, datePosition);
        assertEquals(datePosition, Event2Appointment.getRecurrenceDatePosition(new DefaultRecurrenceId(recurrenceID.getValue())));
    }

    private void testOverMidnight(TimeZone timeZone) throws Exception {
        /*
         * prepare event series
         */
        Event event = getEventSeries("FREQ=DAILY", timeZone, "today at 23:00:00", "tomorrow at 01:00:00");
        DefaultRecurrenceData recurrenceData = new DefaultRecurrenceData(event.getRecurrenceRule(), event.getStartDate(), null);
        /*
         * check recurrence position conversion
         */
        int position = 8;
        RecurrenceId recurrenceID = Appointment2Event.getRecurrenceID(recurrenceService, recurrenceData, position);
        assertEquals(position, Event2Appointment.getRecurrencePosition(recurrenceService, recurrenceData, new DefaultRecurrenceId(recurrenceID.getValue())));
        /*
         * check recurrence date position conversion
         */
        Calendar calendar = CalendarUtils.initCalendar(timeZone, event.getStartDate().getTimestamp());
        calendar.add(Calendar.DATE, 17);
        Date datePosition = CalendarUtils.truncateTime(calendar.getTime(), TimeZones.UTC);
        recurrenceID = Appointment2Event.getRecurrenceID(recurrenceService, recurrenceData, datePosition);
        assertEquals(datePosition, Event2Appointment.getRecurrenceDatePosition(new DefaultRecurrenceId(recurrenceID.getValue())));
    }

    private void testOneDay(TimeZone timeZone) throws Exception {
        /*
         * prepare event series
         */
        Event event = getEventSeries("FREQ=DAILY", timeZone, "today at 00:00:00", "tomorrow at 00:00:00");
        DefaultRecurrenceData recurrenceData = new DefaultRecurrenceData(event.getRecurrenceRule(), event.getStartDate(), null);
        /*
         * check recurrence position conversion
         */
        int position = 8;
        RecurrenceId recurrenceID = Appointment2Event.getRecurrenceID(recurrenceService, recurrenceData, position);
        assertEquals(position, Event2Appointment.getRecurrencePosition(recurrenceService, recurrenceData, new DefaultRecurrenceId(recurrenceID.getValue())));
        /*
         * check recurrence date position conversion
         */
        Calendar calendar = CalendarUtils.initCalendar(timeZone, event.getStartDate().getTimestamp());
        calendar.add(Calendar.DATE, 17);
        Date datePosition = CalendarUtils.truncateTime(calendar.getTime(), TimeZones.UTC);
        recurrenceID = Appointment2Event.getRecurrenceID(recurrenceService, recurrenceData, datePosition);
        assertEquals(datePosition, Event2Appointment.getRecurrenceDatePosition(new DefaultRecurrenceId(recurrenceID.getValue())));
    }

    private Event getEventSeries(String rrule, TimeZone timeZone, String start, String end) throws Exception {
        Event event = new Event();
        event.setRecurrenceRule(rrule);
        event.setSummary("RecurrencePositionTest");
        event.setStartDate(new DateTime(timeZone, D(start, timeZone).getTime()));
        event.setEndDate(new DateTime(timeZone, D(end, timeZone).getTime()));
        return event;
    }

    private Event getAllDayEventSeries(String rrule, String start, String end) throws Exception {
        Event event = new Event();
        event.setRecurrenceRule(rrule);
        event.setSummary("RecurrencePositionTest");
        event.setStartDate(new DateTime(D(start, TimeZones.UTC).getTime()).toAllDay());
        event.setEndDate(new DateTime(D(end, TimeZones.UTC).getTime()).toAllDay());
        return event;
    }

}
