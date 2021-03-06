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

package com.openexchange.dav.caldav.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.UserAgents;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link AlarmTestIOSCalendar}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class AlarmTestIOSCalendar extends CalDAVTest {

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.IOS_9_1;
    }

    @Test
    public void testAcknowledgeReminder() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next sunday at 16:00");
        Date end = TimeTools.D("next sunday at 17:00");
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Dem\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Erinnerung\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:71926843-FB96-440E-B84F-0185F967096D\r\n" +
            "X-WR-ALARMUID:71926843-FB96-440E-B84F-0185F967096D\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
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
         * acknowledge reminder in client
         */
        Date acknowledgedDate = TimeTools.D("next sunday at 15:47:32");
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Dem\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(acknowledgedDate) + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:3C9EBB4D-8AA0-4B37-B3BB-9EEF8C70F2B0\r\n" +
            "X-WR-ALARMUID:3C9EBB4D-8AA0-4B37-B3BB-9EEF8C70F2B0\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(uid, iCal, iCalResource.getETag()));
        /*
         * verify appointment on server
         */
        appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        assertFalse("reminder still found", appointment.containsAlarm());
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertAcknowledgedOrDummyAlarm(iCalResource.getVEvent(), formatAsUTC(acknowledgedDate));
    }

    @Test
    public void testSnoozeReminder() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next sunday at 16:00");
        Date end = TimeTools.D("next sunday at 17:00");
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Dem\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Erinnerung\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:71926843-FB96-440E-B84F-0185F967096D\r\n" +
            "X-WR-ALARMUID:71926843-FB96-440E-B84F-0185F967096D\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
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
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeTools.D("next sunday at 15:47"));
        calendar.add(Calendar.SECOND, 32);
        Date acknowledgedDate = calendar.getTime();
        calendar.add(Calendar.MINUTE, 5);
        Date nextTrigger = calendar.getTime();
        String relatedUID = iCalResource.getVEvent().getVAlarm().getPropertyValue("UID");
        iCalResource.getVEvent().getVAlarm().setProperty("ACKNOWLEDGED", formatAsUTC(acknowledgedDate));
        Component snoozeAlarm = new Component("VALARM");
        snoozeAlarm.setProperty("UID", randomUID());
        snoozeAlarm.setProperty("X-WR-ALARMUID", randomUID());
        snoozeAlarm.setProperty("ACTION", "DISPLAY");
        snoozeAlarm.setProperty("DESCRIPTION", "Alarm");
        snoozeAlarm.setProperty("TRIGGER", formatAsUTC(nextTrigger), Collections.singletonMap("VALUE", "DATE-TIME"));
        snoozeAlarm.setProperty("RELATED-TO", relatedUID);
        iCalResource.getVEvent().getComponents().add(snoozeAlarm);
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * verify appointment on server
         */
        appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        assertTrue("no reminder found", appointment.containsAlarm());
        assertEquals("reminder minutes wrong", 15, appointment.getAlarm());
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        List<Component> vAlarms = iCalResource.getVEvent().getVAlarms();
        assertEquals("Unexpected number of VALARMs found", 2, vAlarms.size());
        for (Component vAlarm : vAlarms) {
            if (null != vAlarm.getProperty("RELATED-TO")) {
                assertEquals("ALARM wrong", formatAsUTC(nextTrigger), vAlarm.getPropertyValue("TRIGGER"));
            } else {
                assertEquals("ALARM wrong", "-PT15M", vAlarm.getPropertyValue("TRIGGER"));
            }
        }
    }

    @Test
    public void testEditReminder() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next sunday at 16:00");
        Date end = TimeTools.D("next sunday at 17:00");
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Dem\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Erinnerung\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:71926843-FB96-440E-B84F-0185F967096D\r\n" +
            "X-WR-ALARMUID:71926843-FB96-440E-B84F-0185F967096D\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
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
         * edit reminder in client
         */
        iCalResource.getVEvent().getVAlarm().setProperty("TRIGGER", "-PT20M");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * verify appointment on server
         */
        appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        assertTrue("no reminder found", appointment.containsAlarm());
        assertEquals("reminder minutes wrong", 20, appointment.getAlarm());
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT20M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
    }

    @Test
    public void testAcknowledgeRecurringReminder() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next saturday at 15:30");
        Date end = TimeTools.D("next saturday at 17:15");
        Date initialAcknowledged = TimeTools.D("next saturday at 15:14");
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Serie\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Erinnerung\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:016232FB-1CA9-4657-99F7-51052C884495\r\n" +
            "X-WR-ALARMUID:016232FB-1CA9-4657-99F7-51052C884495\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
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
         * acknowledge reminder in client
         */
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        calendar.setTime(initialAcknowledged);
        calendar.add(Calendar.MINUTE, 3);
        calendar.add(Calendar.SECOND, 17);
        Date acknowledgedDate = calendar.getTime();
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Serie\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(initialAcknowledged) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(acknowledgedDate) + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:F7FCDC9A-BA2A-4548-BC5A-815008F0FC6E\r\n" +
            "X-WR-ALARMUID:F7FCDC9A-BA2A-4548-BC5A-815008F0FC6E\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(uid, iCal, iCalResource.getETag()));
        /*
         * verify appointment on server
         */
        appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        assertTrue("no reminder found", appointment.containsAlarm());
        assertEquals("reminder minutes wrong", 15, appointment.getAlarm());
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        assertEquals("ACKNOWLEDGED wrong", formatAsUTC(acknowledgedDate), iCalResource.getVEvent().getVAlarm().getPropertyValue("ACKNOWLEDGED"));
    }

    @Test
    public void testSnoozeRecurringReminder() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();

        Date start = TimeTools.D("next friday at 10:00");
        Date end = TimeTools.D("next friday at 11:00");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        calendar.setTime(start);
        calendar.add(Calendar.DATE, 2);
        calendar.add(Calendar.MINUTE, -16);
        Date seriesAcknowledged = calendar.getTime();
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Serie\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Erinnerung\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:016232FB-1CA9-4657-99F7-51052C884495\r\n" +
            "X-WR-ALARMUID:016232FB-1CA9-4657-99F7-51052C884495\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
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
        calendar.setTime(start);
        calendar.add(Calendar.DATE, 2);
        calendar.add(Calendar.MINUTE, -14);
        calendar.add(Calendar.SECOND, 17);
        Date acknowledgedDate = calendar.getTime(); // 09:46:17
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Serie\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(acknowledgedDate) + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:F7FCDC9A-BA2A-4548-BC5A-815008F0FC6E\r\n" +
            "X-WR-ALARMUID:F7FCDC9A-BA2A-4548-BC5A-815008F0FC6E\r\n" +
            "END:VALARM\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Erinnerung\r\n" +
            "RELATED-TO:F7FCDC9A-BA2A-4548-BC5A-815008F0FC6E\r\n" +
            "TRIGGER:-PT8M43S\r\n" +
            "UID:48DA570F-2E00-46BD-9CCA-A92D44E07AD8\r\n" +
            "X-WR-ALARMUID:48DA570F-2E00-46BD-9CCA-A92D44E07AD8\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(uid, iCal, iCalResource.getETag()));
        /*
         * verify appointment on server
         */
        appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        assertTrue("no reminder found", appointment.containsAlarm());
        assertEquals("reminder minutes wrong", 15, appointment.getAlarm());
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        List<Component> vAlarms = iCalResource.getVEvent().getVAlarms();
        assertEquals("Unexpected number of VALARMs found", 2, vAlarms.size());
        assertEquals("ALARM wrong", "-PT15M", vAlarms.get(0).getPropertyValue("TRIGGER"));
        assertEquals("ACKNOWLEDGED wrong", formatAsUTC(acknowledgedDate), vAlarms.get(0).getPropertyValue("ACKNOWLEDGED"));
        assertNotNull("No RELATED-TO found", vAlarms.get(1).getProperty("RELATED-TO"));
        assertEquals("ALARM wrong", "-PT8M43S", vAlarms.get(1).getPropertyValue("TRIGGER"));
    }

    @Test
    public void testAcknowledgeExceptionReminder() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next friday at 10:00");
        Date end = TimeTools.D("next friday at 11:00");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        calendar.setTime(start);
        calendar.add(Calendar.DATE, 2);
        Date exceptionStart = calendar.getTime();
        calendar.add(Calendar.HOUR, 1);
        Date exceptionEnd = calendar.getTime();
        calendar.setTime(exceptionStart);
        calendar.add(Calendar.MINUTE, -16);
        calendar.add(Calendar.DATE, 1);
        Date seriesAcknowledged = calendar.getTime();
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Serie\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:AD79E8D2-9D87-4281-BADB-D14C069C463F\r\n" +
            "X-WR-ALARMUID:AD79E8D2-9D87-4281-BADB-D14C069C463F\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(exceptionEnd, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(exceptionStart, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RECURRENCE-ID;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:SerieEdit\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:AFB070AC-B007-488C-8AAA-0A5F9EE48CBC\r\n" +
            "X-WR-ALARMUID:AFB070AC-B007-488C-8AAA-0A5F9EE48CBC\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment & exception on server
         */
        Appointment appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        assertTrue("no reminder found", appointment.containsAlarm());
        assertEquals("reminder minutes wrong", 15, appointment.getAlarm());
        assertNotNull("No change exceptions found on server", appointment.getChangeException());
        assertEquals("Unexpected number of change excpetions", 1, appointment.getChangeException().length);
        Appointment changeExcpetion = getChangeExcpetions(appointment).get(0);
        rememberForCleanUp(changeExcpetion);
        assertEquals("reminder minutes wrong", 15, changeExcpetion.getAlarm());
        /*
         * verify appointment & exception on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        assertEquals("ACKNOWLEDGED wrong", formatAsUTC(seriesAcknowledged), iCalResource.getVEvent().getVAlarm().getPropertyValue("ACKNOWLEDGED"));
        assertEquals("X-MOZ-LASTACK wrong", formatAsUTC(seriesAcknowledged), iCalResource.getVEvent().getVAlarm().getPropertyValue("X-MOZ-LASTACK"));
        assertEquals("Not all VEVENTs in iCal found", 2, iCalResource.getVEvents().size());
        assertEquals("UID wrong", uid, iCalResource.getVEvents().get(1).getUID());
        assertEquals("SUMMARY wrong", "SerieEdit", iCalResource.getVEvents().get(1).getSummary());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvents().get(1).getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvents().get(1).getVAlarm().getPropertyValue("TRIGGER"));
        /*
         * acknowledge exception reminder in client
         */
        calendar.setTime(exceptionStart);
        calendar.add(Calendar.MINUTE, -14);
        calendar.add(Calendar.SECOND, 52);
        Date exceptionAcknowledged = calendar.getTime();
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
                "SEQUENCE:2\r\n" +
            "SUMMARY:Serie\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:AD79E8D2-9D87-4281-BADB-D14C069C463F\r\n" +
            "X-WR-ALARMUID:AD79E8D2-9D87-4281-BADB-D14C069C463F\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(exceptionEnd, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(exceptionStart, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RECURRENCE-ID;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
                "SEQUENCE:2\r\n" +
            "SUMMARY:SerieEdit\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(exceptionAcknowledged) + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:AFB070AC-B007-488C-8AAA-0A5F9EE48CBC\r\n" +
            "X-WR-ALARMUID:AFB070AC-B007-488C-8AAA-0A5F9EE48CBC\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(uid, iCal, iCalResource.getETag()));
        /*
         * verify appointment & exception on server
         */
        appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        assertTrue("no reminder found", appointment.containsAlarm());
        assertEquals("reminder minutes wrong", 15, appointment.getAlarm());
        assertNotNull("No change exceptions found on server", appointment.getChangeException());
        assertEquals("Unexpected number of change excpetions", 1, appointment.getChangeException().length);
        changeExcpetion = getChangeExcpetions(appointment).get(0);
        rememberForCleanUp(changeExcpetion);
        assertFalse("reminder still found", changeExcpetion.containsAlarm());
        /*
         * verify appointment & exception on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        assertEquals("ACKNOWLEDGED wrong", formatAsUTC(seriesAcknowledged), iCalResource.getVEvent().getVAlarm().getPropertyValue("ACKNOWLEDGED"));
        assertEquals("X-MOZ-LASTACK wrong", formatAsUTC(seriesAcknowledged), iCalResource.getVEvent().getVAlarm().getPropertyValue("X-MOZ-LASTACK"));
        assertEquals("Not all VEVENTs in iCal found", 2, iCalResource.getVEvents().size());
        assertEquals("UID wrong", uid, iCalResource.getVEvents().get(1).getUID());
        assertEquals("SUMMARY wrong", "SerieEdit", iCalResource.getVEvents().get(1).getSummary());
        assertAcknowledgedOrDummyAlarm(iCalResource.getVEvents().get(1), formatAsUTC(exceptionAcknowledged));
    }

    @Test
    public void testSnoozeExceptionReminder() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next friday at 10:00");
        Date end = TimeTools.D("next friday at 11:00");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        calendar.setTime(start);
        calendar.add(Calendar.DATE, 2);
        Date exceptionStart = calendar.getTime();
        calendar.add(Calendar.HOUR, 1);
        Date exceptionEnd = calendar.getTime();
        calendar.setTime(exceptionStart);
        calendar.add(Calendar.MINUTE, -16);
        calendar.add(Calendar.DATE, 1);
        Date seriesAcknowledged = calendar.getTime();
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:Serie\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:AD79E8D2-9D87-4281-BADB-D14C069C463F\r\n" +
            "X-WR-ALARMUID:AD79E8D2-9D87-4281-BADB-D14C069C463F\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(exceptionEnd, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(exceptionStart, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "RECURRENCE-ID;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "SEQUENCE:0\r\n" +
            "SUMMARY:SerieEdit\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "UID:" + uid + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER:-PT15M\r\n" +
            "UID:AFB070AC-B007-488C-8AAA-0A5F9EE48CBC\r\n" +
            "X-WR-ALARMUID:AFB070AC-B007-488C-8AAA-0A5F9EE48CBC\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment & exception on server
         */
        Appointment appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        assertTrue("no reminder found", appointment.containsAlarm());
        assertEquals("reminder minutes wrong", 15, appointment.getAlarm());
        assertNotNull("No change exceptions found on server", appointment.getChangeException());
        assertEquals("Unexpected number of change excpetions", 1, appointment.getChangeException().length);
        Appointment changeExcpetion = getChangeExcpetions(appointment).get(0);
        rememberForCleanUp(changeExcpetion);
        assertEquals("reminder minutes wrong", 15, changeExcpetion.getAlarm());
        /*
         * verify appointment & exception on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        assertEquals("ACKNOWLEDGED wrong", formatAsUTC(seriesAcknowledged), iCalResource.getVEvent().getVAlarm().getPropertyValue("ACKNOWLEDGED"));
        assertEquals("X-MOZ-LASTACK wrong", formatAsUTC(seriesAcknowledged), iCalResource.getVEvent().getVAlarm().getPropertyValue("X-MOZ-LASTACK"));
        assertEquals("Not all VEVENTs in iCal found", 2, iCalResource.getVEvents().size());
        assertEquals("UID wrong", uid, iCalResource.getVEvents().get(1).getUID());
        assertEquals("SUMMARY wrong", "SerieEdit", iCalResource.getVEvents().get(1).getSummary());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvents().get(1).getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvents().get(1).getVAlarm().getPropertyValue("TRIGGER"));
        /*
         * snooze exception reminder in client
         */
        calendar.setTime(exceptionStart);
        calendar.add(Calendar.MINUTE, -14);
        calendar.add(Calendar.SECOND, 52);
        Date exceptionAcknowledged = calendar.getTime();
        calendar.add(Calendar.MINUTE, 9);
        Date nextTrigger = calendar.getTime();
        String relatedUID = iCalResource.getVEvents().get(1).getVAlarm().getPropertyValue("UID");
        iCalResource.getVEvents().get(1).getVAlarm().setProperty("ACKNOWLEDGED", formatAsUTC(exceptionAcknowledged));
        Component snoozeAlarm = new Component("VALARM");
        snoozeAlarm.setProperty("UID", randomUID());
        snoozeAlarm.setProperty("X-WR-ALARMUID", randomUID());
        snoozeAlarm.setProperty("ACTION", "DISPLAY");
        snoozeAlarm.setProperty("DESCRIPTION", "Alarm");
        snoozeAlarm.setProperty("TRIGGER", formatAsUTC(nextTrigger), Collections.singletonMap("VALUE", "DATE-TIME"));
        snoozeAlarm.setProperty("RELATED-TO", relatedUID);
        iCalResource.getVEvents().get(1).getComponents().add(snoozeAlarm);
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * verify appointment & exception on server
         */
        appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        assertTrue("no reminder found", appointment.containsAlarm());
        assertEquals("reminder minutes wrong", 15, appointment.getAlarm());
        assertNotNull("No change exceptions found on server", appointment.getChangeException());
        assertEquals("Unexpected number of change excpetions", 1, appointment.getChangeException().length);
        changeExcpetion = getChangeExcpetions(appointment).get(0);
        rememberForCleanUp(changeExcpetion);
        assertTrue("no reminder found", changeExcpetion.containsAlarm());
        assertEquals("reminder minutes wrong", 15, changeExcpetion.getAlarm());
        /*
         * verify appointment & exception on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        assertEquals("ACKNOWLEDGED wrong", formatAsUTC(seriesAcknowledged), iCalResource.getVEvent().getVAlarm().getPropertyValue("ACKNOWLEDGED"));
        assertEquals("X-MOZ-LASTACK wrong", formatAsUTC(seriesAcknowledged), iCalResource.getVEvent().getVAlarm().getPropertyValue("X-MOZ-LASTACK"));
        assertEquals("Not all VEVENTs in iCal found", 2, iCalResource.getVEvents().size());
        assertEquals("UID wrong", uid, iCalResource.getVEvents().get(1).getUID());
        assertEquals("SUMMARY wrong", "SerieEdit", iCalResource.getVEvents().get(1).getSummary());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvents().get(1).getVAlarm());
        List<Component> vAlarms = iCalResource.getVEvents().get(1).getVAlarms();
        assertEquals("Unexpected number of VALARMs found", 2, vAlarms.size());
        for (Component vAlarm : vAlarms) {
            if (null != vAlarm.getProperty("RELATED-TO")) {
                assertEquals("ALARM wrong", formatAsUTC(nextTrigger), vAlarm.getPropertyValue("TRIGGER"));
            } else {
                assertEquals("ALARM wrong", "-PT15M", vAlarm.getPropertyValue("TRIGGER"));
            }
        }
    }

}
