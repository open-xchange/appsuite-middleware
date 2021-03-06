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

package com.openexchange.dav.caldav.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug43376Test}
 *
 * MacOS: Wiping a reminder leads to a change exception.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug43376Test extends CalDAVTest {

    @Test
    public void testSnoozeReminderOfOccurrence() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next saturday at 15:30");
        Date end = TimeTools.D("next saturday at 17:15");
        Date initialAcknowledged = TimeTools.D("next saturday at 15:14");
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.8.5//EN\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SUMMARY:RecurringReminder\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY;INTERVAL=1\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:7B669A77-E205-4B03-A1AF-40FB146C4A3F\r\n" +
            "UID:7B669A77-E205-4B03-A1AF-40FB146C4A3F\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "DESCRIPTION:Ereignisbenachrichtigung\r\n" +
            "ACTION:DISPLAY\r\n" +
            "END:VALARM\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:56C5C265-7442-44E6-8F9C-17C71DCF932A\r\n" +
            "UID:56C5C265-7442-44E6-8F9C-17C71DCF932A\r\n" +
            "TRIGGER;VALUE=DATE-TIME:19760401T005545Z\r\n" +
            "X-APPLE-DEFAULT-ALARM:TRUE\r\n" +
            "ACTION:NONE\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        assertTrue("no reminder found", appointment.containsAlarm());
        assertEquals("reminder minutes wrong", 15, appointment.getAlarm());
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        /*
         * snooze reminder in client
         */
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        calendar.setTime(start);
        calendar.add(Calendar.DATE, 2);
        Date exceptionStart = calendar.getTime();
        calendar.setTime(end);
        calendar.add(Calendar.DATE, 2);
        Date exceptionEnd = calendar.getTime();
        calendar.setTime(exceptionStart);
        calendar.add(Calendar.MINUTE, -14);
        calendar.add(Calendar.SECOND, 17);
        Date acknowledgedDate = calendar.getTime();
        calendar.add(Calendar.MINUTE, 5);
        Date nextTrigger = calendar.getTime();
        String relatedUID = randomUID();
        iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.8.5//EN\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SUMMARY:RecurringReminder\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY;INTERVAL=1\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:56C5C265-7442-44E6-8F9C-17C71DCF932A\r\n" +
            "UID:56C5C265-7442-44E6-8F9C-17C71DCF932A\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(initialAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(acknowledgedDate) + "\r\n" +
            "END:VALARM\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:56C5C265-7442-44E6-8F9C-17C71DCF932A\r\n" +
            "UID:56C5C265-7442-44E6-8F9C-17C71DCF932A\r\n" +
            "TRIGGER;VALUE=DATE-TIME:19760401T005545Z\r\n" +
            "X-APPLE-DEFAULT-ALARM:TRUE\r\n" +
            "ACTION:NONE\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(exceptionStart, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(exceptionEnd, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SUMMARY:RecurringReminder\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "RECURRENCE-ID:" + formatAsUTC(exceptionStart) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:" + randomUID() + "\r\n" +
            "UID:" + relatedUID + "\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "ACTION:DISPLAY\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(acknowledgedDate) + "\r\n" +
            "END:VALARM\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:" + randomUID() + "\r\n" +
            "UID:" + randomUID() + "\r\n" +
            "TRIGGER;VALUE=DATE-TIME:" + formatAsUTC(nextTrigger) + "\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "RELATED-TO:" + relatedUID + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "END:VALARM\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:" + randomUID() + "\r\n" +
            "UID:" + randomUID() + "\r\n" +
            "TRIGGER;VALUE=DATE-TIME:19760401T005545Z\r\n" +
            "X-APPLE-DEFAULT-ALARM:TRUE\r\n" +
            "ACTION:NONE\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(uid, iCal, iCalResource.getETag()));
        /*
         * verify appointment & exception on server
         */
        appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        assertTrue("no reminder found", appointment.containsAlarm());
        assertEquals("reminder minutes wrong", 15, appointment.getAlarm());
        assertTrue("change exceptions found on server", null == appointment.getChangeException() || 0 == appointment.getChangeException().length);
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("More than one VEVENT in iCal found", 1, iCalResource.getVEvents().size());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        List<Component> vAlarms = iCalResource.getVEvent().getVAlarms();
        assertEquals("Unexpected number of VALARMs found", 2, vAlarms.size());
        for (Component vAlarm : vAlarms) {
            if (null != vAlarm.getProperty("RELATED-TO")) {
                if ("DATE-TIME".equals(vAlarm.getProperty("TRIGGER").getAttribute("VALUE"))) {
                    assertEquals("ALARM wrong", formatAsUTC(nextTrigger), vAlarm.getPropertyValue("TRIGGER"));
                } else {
                    assertEquals("ALARM wrong", "-PT8M43S", vAlarm.getPropertyValue("TRIGGER"));
                }
            } else {
                assertEquals("ALARM wrong", "-PT15M", vAlarm.getPropertyValue("TRIGGER"));
                assertEquals("ACKNOWLEDGED wrong", formatAsUTC(acknowledgedDate), vAlarm.getPropertyValue("ACKNOWLEDGED"));
            }
        }
    }

    @Test
    public void testSnoozeReminderOfFurtherOccurrence() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next saturday at 15:30");
        Date end = TimeTools.D("next saturday at 17:15");
        Date initialAcknowledged = TimeTools.D("next saturday at 15:14");

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        calendar.setTime(start);
        calendar.add(Calendar.DATE, 1);
        Date firstExceptionStart = calendar.getTime();
        calendar.setTime(end);
        calendar.add(Calendar.DATE, 1);
        Date firstExceptionEnd = calendar.getTime();
        calendar.setTime(firstExceptionStart);
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.8.5//EN\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SUMMARY:RecurringReminder\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY;INTERVAL=1\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:7B669A77-E205-4B03-A1AF-40FB146C4A3F\r\n" +
            "UID:7B669A77-E205-4B03-A1AF-40FB146C4A3F\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "DESCRIPTION:Ereignisbenachrichtigung\r\n" +
            "ACTION:DISPLAY\r\n" +
            "END:VALARM\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:56C5C265-7442-44E6-8F9C-17C71DCF932A\r\n" +
            "UID:56C5C265-7442-44E6-8F9C-17C71DCF932A\r\n" +
            "TRIGGER;VALUE=DATE-TIME:19760401T005545Z\r\n" +
            "X-APPLE-DEFAULT-ALARM:TRUE\r\n" +
            "ACTION:NONE\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(firstExceptionStart, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(firstExceptionEnd, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SUMMARY:RecurringReminder EDIT\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "RECURRENCE-ID:" + formatAsUTC(firstExceptionStart) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:8B669A77-E205-4B03-A1AF-40FB146C4A3F\r\n" +
            "UID:8B669A77-E205-4B03-A1AF-40FB146C4A3F\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "DESCRIPTION:Ereignisbenachrichtigung\r\n" +
            "ACTION:DISPLAY\r\n" +
            "END:VALARM\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:56C5C265-7442-44E6-8F9C-17C71DCF932A\r\n" +
            "UID:56C5C265-7442-44E6-8F9C-17C71DCF932A\r\n" +
            "TRIGGER;VALUE=DATE-TIME:19760401T005545Z\r\n" +
            "X-APPLE-DEFAULT-ALARM:TRUE\r\n" +
            "ACTION:NONE\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        assertTrue("no reminder found", appointment.containsAlarm());
        assertEquals("reminder minutes wrong", 15, appointment.getAlarm());
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        /*
         * snooze reminder in client
         */
        calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        calendar.setTime(start);
        calendar.add(Calendar.DATE, 2);
        Date exceptionStart = calendar.getTime();
        calendar.setTime(end);
        calendar.add(Calendar.DATE, 2);
        Date exceptionEnd = calendar.getTime();
        calendar.setTime(exceptionStart);
        calendar.add(Calendar.MINUTE, -14);
        calendar.add(Calendar.SECOND, 17);
        Date acknowledgedDate = calendar.getTime();
        calendar.add(Calendar.MINUTE, 5);
        Date nextTrigger = calendar.getTime();
        String relatedUID = randomUID();
        iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.8.5//EN\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SEQUENCE:2\r\n" +
            "SUMMARY:RecurringReminder\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY;INTERVAL=1\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:56C5C265-7442-44E6-8F9C-17C71DCF932A\r\n" +
            "UID:56C5C265-7442-44E6-8F9C-17C71DCF932A\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(initialAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(acknowledgedDate) + "\r\n" +
            "END:VALARM\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:56C5C265-7442-44E6-8F9C-17C71DCF932A\r\n" +
            "UID:56C5C265-7442-44E6-8F9C-17C71DCF932A\r\n" +
            "TRIGGER;VALUE=DATE-TIME:19760401T005545Z\r\n" +
            "X-APPLE-DEFAULT-ALARM:TRUE\r\n" +
            "ACTION:NONE\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(firstExceptionStart, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(firstExceptionEnd, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SEQUENCE:2\r\n" +
            "SUMMARY:RecurringReminder EDIT\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "RECURRENCE-ID:" + formatAsUTC(firstExceptionStart) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:8B669A77-E205-4B03-A1AF-40FB146C4A3F\r\n" +
            "UID:8B669A77-E205-4B03-A1AF-40FB146C4A3F\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "DESCRIPTION:Ereignisbenachrichtigung\r\n" +
            "ACTION:DISPLAY\r\n" +
            "END:VALARM\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:56C5C265-7442-44E6-8F9C-17C71DCF932A\r\n" +
            "UID:56C5C265-7442-44E6-8F9C-17C71DCF932A\r\n" +
            "TRIGGER;VALUE=DATE-TIME:19760401T005545Z\r\n" +
            "X-APPLE-DEFAULT-ALARM:TRUE\r\n" +
            "ACTION:NONE\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(exceptionStart, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(exceptionEnd, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SEQUENCE:2\r\n" +
            "SUMMARY:RecurringReminder\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "RECURRENCE-ID:" + formatAsUTC(exceptionStart) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:" + randomUID() + "\r\n" +
            "UID:" + relatedUID + "\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "ACTION:DISPLAY\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(acknowledgedDate) + "\r\n" +
            "END:VALARM\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:" + randomUID() + "\r\n" +
            "UID:" + randomUID() + "\r\n" +
            "TRIGGER;VALUE=DATE-TIME:" + formatAsUTC(nextTrigger) + "\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "RELATED-TO:" + relatedUID + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "END:VALARM\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:" + randomUID() + "\r\n" +
            "UID:" + randomUID() + "\r\n" +
            "TRIGGER;VALUE=DATE-TIME:19760401T005545Z\r\n" +
            "X-APPLE-DEFAULT-ALARM:TRUE\r\n" +
            "ACTION:NONE\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(uid, iCal, iCalResource.getETag()));
        /*
         * verify appointment & exception on server
         */
        appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        assertTrue("no reminder found", appointment.containsAlarm());
        assertEquals("reminder minutes wrong", 15, appointment.getAlarm());
        assertTrue("additional change exception found on server", null != appointment.getChangeException() && 1 == appointment.getChangeException().length);
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("More than two VEVENT in iCal found", 2, iCalResource.getVEvents().size());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        List<Component> vAlarms = iCalResource.getVEvent().getVAlarms();
        assertEquals("Unexpected number of VALARMs found", 2, vAlarms.size());
        for (Component vAlarm : vAlarms) {
            if (null != vAlarm.getProperty("RELATED-TO")) {
                if ("DATE-TIME".equals(vAlarm.getProperty("TRIGGER").getAttribute("VALUE"))) {
                    assertEquals("ALARM wrong", formatAsUTC(nextTrigger), vAlarm.getPropertyValue("TRIGGER"));
                } else {
                    assertEquals("ALARM wrong", "-PT8M43S", vAlarm.getPropertyValue("TRIGGER"));
                }
            } else {
                assertEquals("ALARM wrong", "-PT15M", vAlarm.getPropertyValue("TRIGGER"));
                assertEquals("ACKNOWLEDGED wrong", formatAsUTC(acknowledgedDate), vAlarm.getPropertyValue("ACKNOWLEDGED"));
            }
        }
    }

}
