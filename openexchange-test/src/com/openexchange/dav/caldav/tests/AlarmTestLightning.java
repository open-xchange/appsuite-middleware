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

package com.openexchange.dav.caldav.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.UserAgents;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link AlarmTestLightning}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class AlarmTestLightning extends CalDAVTest {

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.LIGHTNING_4_0_3_1;
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
            "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "TZNAME:CEST\r\n" +
            "DTSTART:19700329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "TZNAME:CET\r\n" +
            "DTSTART:19701025T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:test\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "CLASS:PUBLIC\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "TRIGGER;VALUE=DURATION:-PT15M\r\n" +
            "DESCRIPTION:Mozilla Standardbeschreibung\r\n" +
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
            "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "TZNAME:CEST\r\n" +
            "DTSTART:19700329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "TZNAME:CET\r\n" +
            "DTSTART:19701025T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:test\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(acknowledgedDate) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "SEQUENCE:0\r\n" +
            "X-MOZ-GENERATION:1\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "TRIGGER;VALUE=DURATION:-PT15M\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-LIC-ERROR;X-LIC-ERRORTYPE=PROPERTY-PARSE-ERROR:Parse error in property n\r\n" +
            " ame: ACKNOWLEDGED\r\n" +
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
        assertNull("ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
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
            "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "TZNAME:CEST\r\n" +
            "DTSTART:19700329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "TZNAME:CET\r\n" +
            "DTSTART:19701025T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:test\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "CLASS:PUBLIC\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "TRIGGER;VALUE=DURATION:-PT15M\r\n" +
            "DESCRIPTION:Mozilla Standardbeschreibung\r\n" +
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
        Date acknowledgedDate = TimeTools.D("next sunday at 15:47:32");
        Date nextTrigger = TimeTools.D("next sunday at 15:52:32");
        Date nextAcknowledged = TimeTools.D("next sunday at 15:51:32");
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "TZNAME:CEST\r\n" +
            "DTSTART:19700329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "TZNAME:CET\r\n" +
            "DTSTART:19701025T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:test\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(acknowledgedDate) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "SEQUENCE:0\r\n" +
            "X-MOZ-SNOOZE-TIME:" + formatAsUTC(nextTrigger) + "\r\n" +
            "X-MOZ-GENERATION:1\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "TRIGGER;VALUE=DURATION:-PT15M\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-LIC-ERROR;X-LIC-ERRORTYPE=PROPERTY-PARSE-ERROR:Parse error in property n\r\n" +
            " ame: ACKNOWLEDGED\r\n" +
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
        assertEquals("X-MOZ-LASTACK wrong", formatAsUTC(nextAcknowledged), iCalResource.getVEvent().getVAlarm().getPropertyValue("X-MOZ-LASTACK"));
        assertEquals("X-MOZ-SNOOZE-TIME wrong", formatAsUTC(nextTrigger), iCalResource.getVEvent().getPropertyValue("X-MOZ-SNOOZE-TIME"));
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
            "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "TZNAME:CEST\r\n" +
            "DTSTART:19700329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "TZNAME:CET\r\n" +
            "DTSTART:19701025T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:test\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "CLASS:PUBLIC\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "TRIGGER;VALUE=DURATION:-PT15M\r\n" +
            "DESCRIPTION:Mozilla Standardbeschreibung\r\n" +
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
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "TZNAME:CEST\r\n" +
            "DTSTART:19700329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "TZNAME:CET\r\n" +
            "DTSTART:19701025T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:test\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "SEQUENCE:0\r\n" +
            "X-MOZ-GENERATION:1\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "TRIGGER;VALUE=DURATION:-PT20M\r\n" +
            "DESCRIPTION:Mozilla Standardbeschreibung\r\n" +
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
            "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "TZNAME:CEST\r\n" +
            "DTSTART:19700329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "TZNAME:CET\r\n" +
            "DTSTART:19701025T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:recurring\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "CLASS:PUBLIC\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "TRIGGER;VALUE=DURATION:-PT15M\r\n" +
            "DESCRIPTION:Mozilla Standardbeschreibung\r\n" +
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
        calendar.add(Calendar.DATE, 1);
        Date nextAcknowledged = calendar.getTime();
        calendar.setTime(initialAcknowledged);
        calendar.add(Calendar.MINUTE, 3);
        calendar.add(Calendar.SECOND, 17);
        Date acknowledgedDate = calendar.getTime();
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "TZNAME:CEST\r\n" +
            "DTSTART:19700329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "TZNAME:CET\r\n" +
            "DTSTART:19701025T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:recurring\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(acknowledgedDate) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "X-MOZ-GENERATION:1\r\n" +
            "CLASS:PUBLIC\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "TRIGGER;VALUE=DURATION:-PT15M\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-LIC-ERROR;X-LIC-ERRORTYPE=PROPERTY-PARSE-ERROR:Parse error in property n\r\n" +
            " ame: ACKNOWLEDGED\r\n" +
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
        assertEquals("X-MOZ-LASTACK wrong", formatAsUTC(nextAcknowledged), iCalResource.getVEvent().getPropertyValue("X-MOZ-LASTACK"));
    }

    @Test
    public void testSnoozeRecurringReminder() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next friday at 10:00");
        Date end = TimeTools.D("next friday at 10:15");
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "TZNAME:CEST\r\n" +
            "DTSTART:19700329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "TZNAME:CET\r\n" +
            "DTSTART:19701025T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:recurring\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "CLASS:PUBLIC\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "TRIGGER;VALUE=DURATION:-PT15M\r\n" +
            "DESCRIPTION:Mozilla Standardbeschreibung\r\n" +
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
        Date acknowledgedDate = TimeTools.D("next friday at 09:46:24");
        Date nextTrigger = TimeTools.D("next friday at 09:51:24");
        Date nextAcknowledged = TimeTools.D("next friday at 09:50:24");
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "TZNAME:CEST\r\n" +
            "DTSTART:19700329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "TZNAME:CET\r\n" +
            "DTSTART:19701025T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:recurring\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(acknowledgedDate) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "SEQUENCE:0\r\n" +
            "X-MOZ-SNOOZE-TIME-" + start.getTime() + "000:" + formatAsUTC(nextTrigger) + "\r\n" +
            "X-MOZ-GENERATION:1\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "TRIGGER;VALUE=DURATION:-PT15M\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-LIC-ERROR;X-LIC-ERRORTYPE=PROPERTY-PARSE-ERROR:Parse error in property n\r\n" +
            " ame: ACKNOWLEDGED\r\n" +
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
        assertEquals("X-MOZ-LASTACK wrong", formatAsUTC(nextAcknowledged), iCalResource.getVEvent().getPropertyValue("X-MOZ-LASTACK"));
        assertEquals("X-MOZ-SNOOZE-TIME wrong", formatAsUTC(nextTrigger), iCalResource.getVEvent().getPropertyValue("X-MOZ-SNOOZE-TIME-" + start.getTime() + "000"));
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
            "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "TZNAME:CEST\r\n" +
            "DTSTART:19700329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "TZNAME:CET\r\n" +
            "DTSTART:19701025T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:sdfs\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "SEQUENCE:0\r\n" +
            "X-MOZ-GENERATION:1\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "TRIGGER;VALUE=DURATION:-PT15M\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-LIC-ERROR;X-LIC-ERRORTYPE=PROPERTY-PARSE-ERROR:Parse error in property n\r\n" +
            " ame: ACKNOWLEDGED\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:edit\r\n" +
            "RECURRENCE-ID:" + formatAsUTC(exceptionStart) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(exceptionStart, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(exceptionEnd, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "CLASS:PUBLIC\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "SEQUENCE:0\r\n" +
            "X-MOZ-GENERATION:1\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "TRIGGER;VALUE=DURATION:-PT15M\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-LIC-ERROR;X-LIC-ERRORTYPE=PROPERTY-PARSE-ERROR:Parse error in property n\r\n" +
            " ame: ACKNOWLEDGED\r\n" +
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
        assertEquals("SUMMARY wrong", "edit", iCalResource.getVEvents().get(1).getSummary());
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
            "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "TZNAME:CEST\r\n" +
            "DTSTART:19700329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "TZNAME:CET\r\n" +
            "DTSTART:19701025T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:sdfs\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(exceptionAcknowledged) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "SEQUENCE:0\r\n" +
            "X-MOZ-GENERATION:1\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "TRIGGER;VALUE=DURATION:-PT15M\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-LIC-ERROR;X-LIC-ERRORTYPE=PROPERTY-PARSE-ERROR:Parse error in property n\r\n" +
            " ame: ACKNOWLEDGED\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:edit\r\n" +
            "RECURRENCE-ID:" + formatAsUTC(exceptionStart) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(exceptionStart, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(exceptionEnd, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "CLASS:PUBLIC\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "SEQUENCE:0\r\n" +
            "X-MOZ-GENERATION:1\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "TRIGGER;VALUE=DURATION:-PT15M\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-LIC-ERROR;X-LIC-ERRORTYPE=PROPERTY-PARSE-ERROR:Parse error in property n\r\n" +
            " ame: ACKNOWLEDGED\r\n" +
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
        assertEquals("SUMMARY wrong", "edit", iCalResource.getVEvents().get(1).getSummary());
        assertNull("ALARM in iCal exception found", iCalResource.getVEvents().get(1).getVAlarm());
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
            "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "TZNAME:CEST\r\n" +
            "DTSTART:19700329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "TZNAME:CET\r\n" +
            "DTSTART:19701025T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:sdfs\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "SEQUENCE:0\r\n" +
            "X-MOZ-GENERATION:1\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "TRIGGER;VALUE=DURATION:-PT15M\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-LIC-ERROR;X-LIC-ERRORTYPE=PROPERTY-PARSE-ERROR:Parse error in property n\r\n" +
            " ame: ACKNOWLEDGED\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:edit\r\n" +
            "RECURRENCE-ID:" + formatAsUTC(exceptionStart) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(exceptionStart, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(exceptionEnd, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "CLASS:PUBLIC\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "SEQUENCE:0\r\n" +
            "X-MOZ-GENERATION:1\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "TRIGGER;VALUE=DURATION:-PT15M\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-LIC-ERROR;X-LIC-ERRORTYPE=PROPERTY-PARSE-ERROR:Parse error in property n\r\n" +
            " ame: ACKNOWLEDGED\r\n" +
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
        assertEquals("SUMMARY wrong", "edit", iCalResource.getVEvents().get(1).getSummary());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvents().get(1).getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvents().get(1).getVAlarm().getPropertyValue("TRIGGER"));
        /*
         * snooze exception reminder in client
         */
        calendar.setTime(exceptionStart);
        calendar.add(Calendar.MINUTE, -14);
        calendar.add(Calendar.SECOND, 52);
        Date exceptionAcknowledged = calendar.getTime();
        calendar.add(Calendar.MINUTE, 5);
        Date nextTrigger = calendar.getTime();
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "TZNAME:CEST\r\n" +
            "DTSTART:19700329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "TZNAME:CET\r\n" +
            "DTSTART:19701025T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:sdfs\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(exceptionAcknowledged) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "SEQUENCE:0\r\n" +
            "X-MOZ-SNOOZE-TIME-" + exceptionStart.getTime() + "000:" + formatAsUTC(nextTrigger) + "\r\n" +
            "X-MOZ-GENERATION:1\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "TRIGGER;VALUE=DURATION:-PT15M\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-LIC-ERROR;X-LIC-ERRORTYPE=PROPERTY-PARSE-ERROR:Parse error in property n\r\n" +
            " ame: ACKNOWLEDGED\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:edit\r\n" +
            "RECURRENCE-ID:" + formatAsUTC(exceptionStart) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(exceptionStart, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(exceptionEnd, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "CLASS:PUBLIC\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "SEQUENCE:0\r\n" +
            "X-MOZ-GENERATION:1\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "TRIGGER;VALUE=DURATION:-PT15M\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-LIC-ERROR;X-LIC-ERRORTYPE=PROPERTY-PARSE-ERROR:Parse error in property n\r\n" +
            " ame: ACKNOWLEDGED\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(uid, iCal, iCalResource.getETag()));

        // TODO: The snooze information is hidden within the series master. This is currently not considered.

    }

}
