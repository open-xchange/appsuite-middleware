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
import java.util.List;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.UserAgents;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link AlarmTestEMClient}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class AlarmTestEMClient extends CalDAVTest {

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.EM_CLIENT_6_0;
    }

    @Test
    public void testAcknowledgeReminder() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next friday at 08:00");
        Date end = TimeTools.D("next friday at 09:00");
        Date initialAcknowledged = TimeTools.D("next friday at 07:44");
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//eM Client/6.0.23421.0\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:W. Europe Standard Time\r\n" +
            "X-EM-DISPLAYNAME:(UTC+01:00) Amsterdam\\, Berlin\\, Bern\\, Rom\\, Stockholm\\, W\r\n" +
            " ien\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Zeit\r\n" +
            "DTSTART:00010101T030000\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Sommerzeit\r\n" +
            "DTSTART:00010101T020000\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTART;TZID=\"W. Europe Standard Time\":" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=\"W. Europe Standard Time\":" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:20151116T121948Z\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:test\r\n" +
            "CLASS:PUBLIC\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(initialAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(initialAcknowledged) + "\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
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
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//eM Client/6.0.23421.0\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:W. Europe Standard Time\r\n" +
            "X-EM-DISPLAYNAME:(UTC+01:00) Amsterdam\\, Berlin\\, Bern\\, Rom\\, Stockholm\\, W\r\n" +
            " ien\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Zeit\r\n" +
            "DTSTART:00010101T030000\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Sommerzeit\r\n" +
            "DTSTART:00010101T020000\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTART;TZID=\"W. Europe Standard Time\":" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=\"W. Europe Standard Time\":" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:20151116T121948Z\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:test\r\n" +
            "CLASS:PUBLIC\r\n" +
            "BEGIN:VALARM\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Open-XChange\r\n" +
            "X-MOZ-LASTACK:99991231T235859Z\r\n" +
            "ACKNOWLEDGED:99991231T235859Z\r\n" +
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
        Date start = TimeTools.D("next tuesday at 10:00");
        Date end = TimeTools.D("next tuesday at 11:00");
        Date initialAcknowledged = TimeTools.D("next tuesday at 09:44");
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//eM Client/6.0.23421.0\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:W. Europe Standard Time\r\n" +
            "X-EM-DISPLAYNAME:(UTC+01:00) Amsterdam\\, Berlin\\, Bern\\, Rom\\, Stockholm\\, W\r\n" +
            " ien\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Zeit\r\n" +
            "DTSTART:00010101T030000\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Sommerzeit\r\n" +
            "DTSTART:00010101T020000\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTART;TZID=\"W. Europe Standard Time\":" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=\"W. Europe Standard Time\":" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:20151116T121948Z\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:snooze\r\n" +
            "CLASS:PUBLIC\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(initialAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(initialAcknowledged) + "\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
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
        Date nextTrigger = TimeTools.D("next tuesday at 09:51:24");
        Date nextAcknowledged = TimeTools.D("next tuesday at 09:50:24");
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//eM Client/6.0.23421.0\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:W. Europe Standard Time\r\n" +
            "X-EM-DISPLAYNAME:(UTC+01:00) Amsterdam\\, Berlin\\, Bern\\, Rom\\, Stockholm\\, W\r\n" +
            " ien\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Zeit\r\n" +
            "DTSTART:00010101T030000\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Sommerzeit\r\n" +
            "DTSTART:00010101T020000\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTART;TZID=\"W. Europe Standard Time\":" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=\"W. Europe Standard Time\":" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:20151116T121948Z\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:snooze\r\n" +
            "CLASS:PUBLIC\r\n" +
            "X-MOZ-SNOOZE:" + formatAsUTC(nextTrigger) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Open-XChange\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(nextAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(nextAcknowledged) + "\r\n" +
            "END:VALARM\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER;VALUE=DATE-TIME:" + formatAsUTC(nextTrigger) + "\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(nextAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(nextAcknowledged) + "\r\n" +
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
        Date start = TimeTools.D("next tuesday at 10:00");
        Date end = TimeTools.D("next tuesday at 11:00");
        Date initialAcknowledged = TimeTools.D("next tuesday at 09:44");
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//eM Client/6.0.23421.0\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:W. Europe Standard Time\r\n" +
            "X-EM-DISPLAYNAME:(UTC+01:00) Amsterdam\\, Berlin\\, Bern\\, Rom\\, Stockholm\\, W\r\n" +
            " ien\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Zeit\r\n" +
            "DTSTART:00010101T030000\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Sommerzeit\r\n" +
            "DTSTART:00010101T020000\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTART;TZID=\"W. Europe Standard Time\":" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=\"W. Europe Standard Time\":" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:20151116T121948Z\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:snooze\r\n" +
            "CLASS:PUBLIC\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(initialAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(initialAcknowledged) + "\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
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
        Date nextAcknowledged = TimeTools.D("next tuesday at 09:29:00");
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//eM Client/6.0.23421.0\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:W. Europe Standard Time\r\n" +
            "X-EM-DISPLAYNAME:(UTC+01:00) Amsterdam\\, Berlin\\, Bern\\, Rom\\, Stockholm\\, W\r\n" +
            " ien\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Zeit\r\n" +
            "DTSTART:00010101T030000\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Sommerzeit\r\n" +
            "DTSTART:00010101T020000\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTART;TZID=\"W. Europe Standard Time\":" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=\"W. Europe Standard Time\":" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:20151116T121948Z\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:snooze\r\n" +
            "CLASS:PUBLIC\r\n" +
            "BEGIN:VALARM\r\n" +
            "TRIGGER;RELATED=START:-PT30M\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Open-XChange\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(nextAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(nextAcknowledged) + "\r\n" +
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
        assertEquals("reminder minutes wrong", 30, appointment.getAlarm());
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT30M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
    }

    @Test
    public void testAcknowledgeRecurringReminder() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next friday at 10:00");
        Date end = TimeTools.D("next friday at 10:15");
        Date initialAcknowledged = TimeTools.D("next friday at 09:44");
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//eM Client/6.0.23421.0\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:W. Europe Standard Time\r\n" +
            "X-EM-DISPLAYNAME:(UTC+01:00) Amsterdam\\, Berlin\\, Bern\\, Rom\\, Stockholm\\, W\r\n" +
            " ien\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Zeit\r\n" +
            "DTSTART:00010101T030000\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Sommerzeit\r\n" +
            "DTSTART:00010101T020000\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTART;TZID=\"W. Europe Standard Time\":" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=\"W. Europe Standard Time\":" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:RecurringReminder\r\n" +
            "CLASS:PUBLIC\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(initialAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(initialAcknowledged) + "\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
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
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//eM Client/6.0.23421.0\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:W. Europe Standard Time\r\n" +
            "X-EM-DISPLAYNAME:(UTC+01:00) Amsterdam\\, Berlin\\, Bern\\, Rom\\, Stockholm\\, W\r\n" +
            " ien\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Zeit\r\n" +
            "DTSTART:00010101T030000\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Sommerzeit\r\n" +
            "DTSTART:00010101T020000\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTART;TZID=\"W. Europe Standard Time\":" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=\"W. Europe Standard Time\":" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:RecurringReminder\r\n" +
            "CLASS:PUBLIC\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "BEGIN:VALARM\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Open-XChange\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(nextAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(nextAcknowledged) + "\r\n" +
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
        assertEquals("ACKNOWLEDGED wrong", formatAsUTC(nextAcknowledged), iCalResource.getVEvent().getVAlarm().getPropertyValue("ACKNOWLEDGED"));
        assertEquals("X-MOZ-LASTACK wrong", formatAsUTC(nextAcknowledged), iCalResource.getVEvent().getVAlarm().getPropertyValue("X-MOZ-LASTACK"));
    }

    @Test
    public void testSnoozeRecurringReminder() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next friday at 10:00");
        Date end = TimeTools.D("next friday at 10:15");
        Date initialAcknowledged = TimeTools.D("next friday at 09:44");
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//eM Client/6.0.23421.0\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:W. Europe Standard Time\r\n" +
            "X-EM-DISPLAYNAME:(UTC+01:00) Amsterdam\\, Berlin\\, Bern\\, Rom\\, Stockholm\\, W\r\n" +
            " ien\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Zeit\r\n" +
            "DTSTART:00010101T030000\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Sommerzeit\r\n" +
            "DTSTART:00010101T020000\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTART;TZID=\"W. Europe Standard Time\":" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=\"W. Europe Standard Time\":" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:RecurringReminder\r\n" +
            "CLASS:PUBLIC\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(initialAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(initialAcknowledged) + "\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
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
        Date nextTrigger = TimeTools.D("next friday at 09:51:24");
        Date nextAcknowledged = TimeTools.D("next friday at 09:50:24");
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//eM Client/6.0.23421.0\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:W. Europe Standard Time\r\n" +
            "X-EM-DISPLAYNAME:(UTC+01:00) Amsterdam\\, Berlin\\, Bern\\, Rom\\, Stockholm\\, W\r\n" +
            " ien\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Zeit\r\n" +
            "DTSTART:00010101T030000\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Sommerzeit\r\n" +
            "DTSTART:00010101T020000\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTART;TZID=\"W. Europe Standard Time\":" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=\"W. Europe Standard Time\":" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "X-MOZ-SNOOZE:" + formatAsUTC(nextTrigger) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:RecurringReminder\r\n" +
            "CLASS:PUBLIC\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "BEGIN:VALARM\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Open-XChange\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(nextAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(nextAcknowledged) + "\r\n" +
            "END:VALARM\r\n" +
            "BEGIN:VALARM\r\n" +
            "TRIGGER;VALUE=DATE-TIME:" + formatAsUTC(nextTrigger) + "\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(nextAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(nextAcknowledged) + "\r\n" +
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
        List<Component> vAlarms = iCalResource.getVEvent().getVAlarms();
        assertEquals("Unexpected number of VALARMs found", 2, vAlarms.size());
        // order of VALARM components seems to be important for the client ...
        assertEquals("ALARM wrong", "-PT15M", vAlarms.get(0).getPropertyValue("TRIGGER"));
        assertEquals("ACKNOWLEDGED wrong", formatAsUTC(nextAcknowledged), vAlarms.get(0).getPropertyValue("ACKNOWLEDGED"));
        assertEquals("X-MOZ-LASTACK wrong", formatAsUTC(nextAcknowledged), vAlarms.get(0).getPropertyValue("X-MOZ-LASTACK"));
        assertNotNull("No RELATED-TO found", vAlarms.get(1).getProperty("RELATED-TO"));
        assertEquals("ALARM wrong", formatAsUTC(nextTrigger), vAlarms.get(1).getPropertyValue("TRIGGER"));
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
        Date exceptionAcknowledged = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date seriesAcknowledged = calendar.getTime();
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//eM Client/6.0.23421.0\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:W. Europe Standard Time\r\n" +
            "X-EM-DISPLAYNAME:(UTC+01:00) Amsterdam\\, Berlin\\, Bern\\, Rom\\, Stockholm\\, W\r\n" +
            " ien\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Zeit\r\n" +
            "DTSTART:00010101T030000\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Sommerzeit\r\n" +
            "DTSTART:00010101T020000\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTART;TZID=\"W. Europe Standard Time\":" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=\"W. Europe Standard Time\":" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:daily\r\n" +
            "CLASS:PUBLIC\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:dailyEDIT\r\n" +
            "DTSTART;TZID=\"W. Europe Standard Time\":" + format(exceptionStart, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=\"W. Europe Standard Time\":" + format(exceptionEnd, "Europe/Berlin") + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "RECURRENCE-ID:" + formatAsUTC(exceptionStart) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(exceptionAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(exceptionAcknowledged) + "\r\n" +
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
        assertEquals("SUMMARY wrong", "dailyEDIT", iCalResource.getVEvents().get(1).getSummary());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvents().get(1).getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvents().get(1).getVAlarm().getPropertyValue("TRIGGER"));
        /*
         * acknowledge exception reminder in client
         */
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//eM Client/6.0.23421.0\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:W. Europe Standard Time\r\n" +
            "X-EM-DISPLAYNAME:(UTC+01:00) Amsterdam\\, Berlin\\, Bern\\, Rom\\, Stockholm\\, W\r\n" +
            " ien\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Zeit\r\n" +
            "DTSTART:00010101T030000\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Sommerzeit\r\n" +
            "DTSTART:00010101T020000\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTART;TZID=\"W. Europe Standard Time\":" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=\"W. Europe Standard Time\":" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:daily\r\n" +
            "CLASS:PUBLIC\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:dailyEDIT\r\n" +
            "DTSTART;TZID=\"W. Europe Standard Time\":" + format(exceptionStart, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=\"W. Europe Standard Time\":" + format(exceptionEnd, "Europe/Berlin") + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "RECURRENCE-ID:" + formatAsUTC(exceptionStart) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-MOZ-LASTACK:99991231T235859Z\r\n" +
            "ACKNOWLEDGED:99991231T235859Z\r\n" +
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
        assertEquals("SUMMARY wrong", "dailyEDIT", iCalResource.getVEvents().get(1).getSummary());
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
        Date exceptionAcknowledged = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date seriesAcknowledged = calendar.getTime();
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//eM Client/6.0.23421.0\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:W. Europe Standard Time\r\n" +
            "X-EM-DISPLAYNAME:(UTC+01:00) Amsterdam\\, Berlin\\, Bern\\, Rom\\, Stockholm\\, W\r\n" +
            " ien\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Zeit\r\n" +
            "DTSTART:00010101T030000\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Sommerzeit\r\n" +
            "DTSTART:00010101T020000\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTART;TZID=\"W. Europe Standard Time\":" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=\"W. Europe Standard Time\":" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:daily\r\n" +
            "CLASS:PUBLIC\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:dailyEDIT\r\n" +
            "DTSTART;TZID=\"W. Europe Standard Time\":" + format(exceptionStart, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=\"W. Europe Standard Time\":" + format(exceptionEnd, "Europe/Berlin") + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "RECURRENCE-ID:" + formatAsUTC(exceptionStart) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(exceptionAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(exceptionAcknowledged) + "\r\n" +
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
        assertEquals("SUMMARY wrong", "dailyEDIT", iCalResource.getVEvents().get(1).getSummary());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvents().get(1).getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvents().get(1).getVAlarm().getPropertyValue("TRIGGER"));
        /*
         * snooze exception reminder in client
         */
        calendar.setTime(exceptionStart);
        calendar.add(Calendar.MINUTE, -10);
        calendar.add(Calendar.SECOND, 24);
        Date nextTrigger = calendar.getTime();
        calendar.add(Calendar.MINUTE, -1);
        Date nextAcknowledged = calendar.getTime();
        iCal =
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//eM Client/6.0.23421.0\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:W. Europe Standard Time\r\n" +
            "X-EM-DISPLAYNAME:(UTC+01:00) Amsterdam\\, Berlin\\, Bern\\, Rom\\, Stockholm\\, W\r\n" +
            " ien\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Zeit\r\n" +
            "DTSTART:00010101T030000\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZNAME:Mitteleurop\u00e4ische Sommerzeit\r\n" +
            "DTSTART:00010101T020000\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTART;TZID=\"W. Europe Standard Time\":" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=\"W. Europe Standard Time\":" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:daily\r\n" +
            "CLASS:PUBLIC\r\n" +
            "RRULE:FREQ=DAILY\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(seriesAcknowledged) + "\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:dailyEDIT\r\n" +
            "DTSTART;TZID=\"W. Europe Standard Time\":" + format(exceptionStart, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=\"W. Europe Standard Time\":" + format(exceptionEnd, "Europe/Berlin") + "\r\n" +
            "CLASS:PUBLIC\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "RECURRENCE-ID:" + formatAsUTC(exceptionStart) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "X-MOZ-SNOOZE:" + formatAsUTC(nextTrigger) + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(nextAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(nextAcknowledged) + "\r\n" +
            "END:VALARM\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:Alarm\r\n" +
            "TRIGGER;VALUE=DATE-TIME:" + formatAsUTC(nextTrigger) + "\r\n" +
            "X-MOZ-LASTACK:" + formatAsUTC(nextAcknowledged) + "\r\n" +
            "ACKNOWLEDGED:" + formatAsUTC(nextAcknowledged) + "\r\n" +
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
        assertEquals("SUMMARY wrong", "dailyEDIT", iCalResource.getVEvents().get(1).getSummary());
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
