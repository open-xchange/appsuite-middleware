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
import static org.junit.Assert.assertNotNull;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.WebDAVClient;
import com.openexchange.dav.caldav.Abstract2UserCalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link PutTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 8.0.0
 */
public class PutTest extends Abstract2UserCalDAVTest {

    @Test
    public void testAddException() throws Exception {
        /*
         * create overridden instance w/o series master event on client
         */
        String uid = randomUID();
        Date start = TimeTools.D("next monday at 09:00", TimeZone.getTimeZone("Europe/Berlin"));
        Date end = TimeTools.D("next monday at 09:30", TimeZone.getTimeZone("Europe/Berlin"));
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.8.5//EN\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            getEuropeBerlinTimezoneComponent() +
            generateEventComponent(uid, format(start, "Europe/Berlin"), null, start, end, "Europe/Berlin", "test@example.com", getClient().getValues().getDefaultAddress()) +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        List<Appointment> appointments = getAppointments(getDefaultFolderID(), uid);
        assertNotNull("added appointment not found on server", appointments);
        assertEquals("unexpected number of added appointments", 1, appointments.size());
        Appointment appointment = appointments.get(0);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("RECURRENCE-ID wrong", format(start, "Europe/Berlin"), iCalResource.getVEvent().getPropertyValue("RECURRENCE-ID"));
        assertEquals("DTSTART wrong", start, iCalResource.getVEvent().getDTStart());
        assertEquals("DTEND wrong", end, iCalResource.getVEvent().getDTEnd());
    }

    @Test
    public void testAddSecondException() throws Exception {
        /*
         * create overridden instance w/o series master event on client
         */
        String uid = randomUID();
        Date start = TimeTools.D("next monday at 09:00", TimeZone.getTimeZone("Europe/Berlin"));
        Date end = TimeTools.D("next monday at 09:30", TimeZone.getTimeZone("Europe/Berlin"));
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.8.5//EN\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            getEuropeBerlinTimezoneComponent() +
            generateEventComponent(uid, format(start, "Europe/Berlin"), null, start, end, "Europe/Berlin", "test@example.com", getClient().getValues().getDefaultAddress()) +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        List<Appointment> appointments = getAppointments(getDefaultFolderID(), uid);
        assertNotNull("added appointment not found on server", appointments);
        assertEquals("unexpected number of added appointments", 1, appointments.size());
        Appointment appointment = appointments.get(0);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("RECURRENCE-ID wrong", format(start, "Europe/Berlin"), iCalResource.getVEvent().getPropertyValue("RECURRENCE-ID"));
        assertEquals("DTSTART wrong", start, iCalResource.getVEvent().getDTStart());
        assertEquals("DTEND wrong", end, iCalResource.getVEvent().getDTEnd());
        /*
         * update event & add another overridden instance w/o series master event on client
         */
        Date start2 = CalendarUtils.add(start, Calendar.DATE, 1, TimeZone.getTimeZone("Europe/Berlin"));
        Date end2 = CalendarUtils.add(end, Calendar.DATE, 1, TimeZone.getTimeZone("Europe/Berlin"));
        iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.8.5//EN\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            getEuropeBerlinTimezoneComponent() +
            generateEventComponent(uid, format(start, "Europe/Berlin"), null, start, end, "Europe/Berlin", "test@example.com", getClient().getValues().getDefaultAddress()) +
            generateEventComponent(uid, format(start2, "Europe/Berlin"), null, start2, end2, "Europe/Berlin", "test@example.com", getClient().getValues().getDefaultAddress()) +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(uid, iCal, iCalResource.getETag()));
        /*
         * verify appointments on server
         */
        appointments = getAppointments(getDefaultFolderID(), uid);
        assertNotNull("updated appointments not found on server", appointments);
        assertEquals("unexpected number of updated appointments", 2, appointments.size());
        appointment = appointments.get(0);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        appointment = appointments.get(1);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        /*
         * verify appointments on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENTs in iCal found", iCalResource.getVEvents());
        assertEquals("Unexpected number of VEVENTs in iCal found", 2, iCalResource.getVEvents().size());
        assertNotNull("No VEVENT for first occurrence in iCal found", iCalResource.getVEvent(format(start, "Europe/Berlin")));
        assertNotNull("No VEVENT for second occurrence in iCal found", iCalResource.getVEvent(format(start2, "Europe/Berlin")));
    }

    @Test
    public void testRemoveSecondException() throws Exception {
        /*
         * create overridden instance w/o series master event on client
         */
        String uid = randomUID();
        Date start = TimeTools.D("next monday at 09:00", TimeZone.getTimeZone("Europe/Berlin"));
        Date end = TimeTools.D("next monday at 09:30", TimeZone.getTimeZone("Europe/Berlin"));
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.8.5//EN\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            getEuropeBerlinTimezoneComponent() +
            generateEventComponent(uid, format(start, "Europe/Berlin"), null, start, end, "Europe/Berlin", "test@example.com", getClient().getValues().getDefaultAddress()) +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        List<Appointment> appointments = getAppointments(getDefaultFolderID(), uid);
        assertNotNull("added appointment not found on server", appointments);
        assertEquals("unexpected number of added appointments", 1, appointments.size());
        Appointment appointment = appointments.get(0);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("RECURRENCE-ID wrong", format(start, "Europe/Berlin"), iCalResource.getVEvent().getPropertyValue("RECURRENCE-ID"));
        assertEquals("DTSTART wrong", start, iCalResource.getVEvent().getDTStart());
        assertEquals("DTEND wrong", end, iCalResource.getVEvent().getDTEnd());
        /*
         * update event & add another overridden instance w/o series master event on client
         */
        Date start2 = CalendarUtils.add(start, Calendar.DATE, 1, TimeZone.getTimeZone("Europe/Berlin"));
        Date end2 = CalendarUtils.add(end, Calendar.DATE, 1, TimeZone.getTimeZone("Europe/Berlin"));
        iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.8.5//EN\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            getEuropeBerlinTimezoneComponent() +
            generateEventComponent(uid, format(start, "Europe/Berlin"), null, start, end, "Europe/Berlin", "test@example.com", getClient().getValues().getDefaultAddress()) +
            generateEventComponent(uid, format(start2, "Europe/Berlin"), null, start2, end2, "Europe/Berlin", "test@example.com", getClient().getValues().getDefaultAddress()) +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(uid, iCal, iCalResource.getETag()));
        /*
         * verify appointments on server
         */
        appointments = getAppointments(getDefaultFolderID(), uid);
        assertNotNull("updated appointments not found on server", appointments);
        assertEquals("unexpected number of updated appointments", 2, appointments.size());
        appointment = appointments.get(0);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        appointment = appointments.get(1);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        /*
         * verify appointments on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENTs in iCal found", iCalResource.getVEvents());
        assertEquals("Unexpected number of VEVENTs in iCal found", 2, iCalResource.getVEvents().size());
        assertNotNull("No VEVENT for first occurrence in iCal found", iCalResource.getVEvent(format(start, "Europe/Berlin")));
        assertNotNull("No VEVENT for second occurrence in iCal found", iCalResource.getVEvent(format(start2, "Europe/Berlin")));
        /*
         * update event & remove first overridden instance again on client
         */
        iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.8.5//EN\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            getEuropeBerlinTimezoneComponent() +
            generateEventComponent(uid, format(start2, "Europe/Berlin"), null, start2, end2, "Europe/Berlin", "test@example.com", getClient().getValues().getDefaultAddress()) +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(uid, iCal, iCalResource.getETag()));
        /*
         * verify appointment on server
         */
        appointments = getAppointments(getDefaultFolderID(), uid);
        assertNotNull("remaining appointment not found on server", appointments);
        assertEquals("unexpected number of appointments", 1, appointments.size());
        appointment = appointments.get(0);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("RECURRENCE-ID wrong", format(start2, "Europe/Berlin"), iCalResource.getVEvent().getPropertyValue("RECURRENCE-ID"));
        assertEquals("DTSTART wrong", start2, iCalResource.getVEvent().getDTStart());
        assertEquals("DTEND wrong", end2, iCalResource.getVEvent().getDTEnd());
    }

    @Test
    public void testAddMasterAfterException() throws Exception {
        /*
         * create overridden instance w/o series master event on client
         */
        String uid = randomUID();
        Date start = TimeTools.D("next monday at 09:00", TimeZone.getTimeZone("Europe/Berlin"));
        Date end = TimeTools.D("next monday at 09:30", TimeZone.getTimeZone("Europe/Berlin"));
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.8.5//EN\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            getEuropeBerlinTimezoneComponent() +
            generateEventComponent(uid, format(start, "Europe/Berlin"), null, start, end, "Europe/Berlin", "test@example.com", getClient().getValues().getDefaultAddress()) +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        List<Appointment> appointments = getAppointments(getDefaultFolderID(), uid);
        assertNotNull("added appointment not found on server", appointments);
        assertEquals("unexpected number of added appointments", 1, appointments.size());
        Appointment appointment = appointments.get(0);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("RECURRENCE-ID wrong", format(start, "Europe/Berlin"), iCalResource.getVEvent().getPropertyValue("RECURRENCE-ID"));
        assertEquals("DTSTART wrong", start, iCalResource.getVEvent().getDTStart());
        assertEquals("DTEND wrong", end, iCalResource.getVEvent().getDTEnd());
        /*
         * update event & add another series master event on client
         */
        Date masterStart = CalendarUtils.add(start, Calendar.DATE, -3, TimeZone.getTimeZone("Europe/Berlin"));
        Date masterEnd = CalendarUtils.add(end, Calendar.DATE, -3, TimeZone.getTimeZone("Europe/Berlin"));
        iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.8.5//EN\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            getEuropeBerlinTimezoneComponent() +
            generateEventComponent(uid, null, "FREQ=DAILY;COUNT=4", masterStart, masterEnd, "Europe/Berlin", "test@example.com", getClient().getValues().getDefaultAddress()) +
            generateEventComponent(uid, format(start, "Europe/Berlin"), null, start, end, "Europe/Berlin", "test@example.com", getClient().getValues().getDefaultAddress()) +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(uid, iCal, iCalResource.getETag()));
        /*
         * verify appointments on server
         */
        appointments = getAppointments(getDefaultFolderID(), uid);
        assertNotNull("updated appointments not found on server", appointments);
        assertEquals("unexpected number of updated appointments", 2, appointments.size());
        appointment = appointments.get(0);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        appointment = appointments.get(1);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        /*
         * verify appointments on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENTs in iCal found", iCalResource.getVEvents());
        assertEquals("Unexpected number of VEVENTs in iCal found", 2, iCalResource.getVEvents().size());
        assertNotNull("No VEVENT for first occurrence in iCal found", iCalResource.getVEvent(format(start, "Europe/Berlin")));
        assertNotNull("No VEVENT for master event in iCal found", iCalResource.getVEvent(null));
    }

    @Test
    public void testDontAddConflictingEvent() throws Exception {
        /*
         * create appointment on client
         */
        String uid = randomUID();
        String summary = "test";
        String location = "testcity";
        Date start = TimeTools.D("tomorrow at 3pm");
        Date end = TimeTools.D("tomorrow at 4pm");
        String iCal = generateICal(start, end, uid, summary, location);
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = getAppointment(uid);
        rememberForCleanUp(appointment);
        assertAppointmentEquals(appointment, start, end, uid, summary, location);
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("SUMMARY wrong", summary, iCalResource.getVEvent().getSummary());
        assertEquals("LOCATION wrong", location, iCalResource.getVEvent().getLocation());
        /*
         * try to create appointment with same uid in other private folder
         */
        FolderObject subfolder = createFolder(randomUID());
        assertEquals("response code wrong", StatusCodes.SC_FORBIDDEN, putICal(String.valueOf(subfolder.getObjectID()), uid, iCal));
        /*
         * try to create appointment with same uid in other public folder
         */
        FolderObject publicFolder = createPublicFolder(randomUID());
        assertEquals("response code wrong", StatusCodes.SC_FORBIDDEN, putICal(String.valueOf(publicFolder.getObjectID()), uid, iCal));
        /*
         * try to create appointment with same uid, but different filename, in same folder
         */
        String resourceName = randomUID();
        assertEquals("response code wrong", StatusCodes.SC_FORBIDDEN, putICal(resourceName, iCal));
        /*
         * try to create appointment with same uid, but different filename, in other private folder
         */
        assertEquals("response code wrong", StatusCodes.SC_FORBIDDEN, putICal(String.valueOf(subfolder.getObjectID()), resourceName, iCal));
        /*
         * try to create appointment with same uid, but different filename, in other public folder
         */
        assertEquals("response code wrong", StatusCodes.SC_FORBIDDEN, putICal(String.valueOf(publicFolder.getObjectID()), resourceName, iCal));
    }

    @Test
    public void testMultipleAttendees() throws Exception {
        /*
         * create overridden instance w/o series master event on client
         */
        String uid = randomUID();
        Date start = TimeTools.D("next monday at 09:00", TimeZone.getTimeZone("Europe/Berlin"));
        Date end = TimeTools.D("next monday at 09:30", TimeZone.getTimeZone("Europe/Berlin"));
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.8.5//EN\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            getEuropeBerlinTimezoneComponent() +
            generateEventComponent(uid, format(start, "Europe/Berlin"), null, start, end, "Europe/Berlin", "test@example.com", getClient().getValues().getDefaultAddress(), client2.getValues().getDefaultAddress()) +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        List<Appointment> appointments = getAppointments(getDefaultFolderID(), uid);
        assertNotNull("added appointment not found on server", appointments);
        assertEquals("unexpected number of added appointments", 1, appointments.size());
        Appointment appointment = appointments.get(0);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("RECURRENCE-ID wrong", format(start, "Europe/Berlin"), iCalResource.getVEvent().getPropertyValue("RECURRENCE-ID"));
        assertEquals("DTSTART wrong", start, iCalResource.getVEvent().getDTStart());
        assertEquals("DTEND wrong", end, iCalResource.getVEvent().getDTEnd());
        /*
         * create the same event resource for other user on second client
         */
        WebDAVClient webDAVClient2 = new WebDAVClient(testUser2, getDefaultUserAgent(), null);
        String folderId2 = encodeFolderID(String.valueOf(client2.getValues().getPrivateAppointmentFolder()));
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(webDAVClient2, folderId2, uid, iCal, Collections.emptyMap()));
        /*
         * verify appointment on second client
         */
        ICalResource iCalResource2 = get(webDAVClient2, folderId2, uid, null, null);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("RECURRENCE-ID wrong", format(start, "Europe/Berlin"), iCalResource.getVEvent().getPropertyValue("RECURRENCE-ID"));
        assertEquals("DTSTART wrong", start, iCalResource.getVEvent().getDTStart());
        assertEquals("DTEND wrong", end, iCalResource.getVEvent().getDTEnd());
        /*
         * delete appointment on first client
         */
        assertEquals("response code wrong", StatusCodes.SC_NO_CONTENT, delete(getDefaultFolderID(), uid, iCalResource.getETag(), null));
        /*
         * verify appointment on second client
         */
        iCalResource2 = get(webDAVClient2, folderId2, uid, null, null);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("RECURRENCE-ID wrong", format(start, "Europe/Berlin"), iCalResource.getVEvent().getPropertyValue("RECURRENCE-ID"));
        assertEquals("DTSTART wrong", start, iCalResource.getVEvent().getDTStart());
        assertEquals("DTEND wrong", end, iCalResource.getVEvent().getDTEnd());
    }

    private static String generateEventComponent(String uid, String recurrenceId, String rrule, Date start, Date end, String timezoneId, String organizerMail, String... attendeeMails) {
        String iCal = // @formatter:off
            "BEGIN:VEVENT\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "DTEND;TZID=" + timezoneId + ":" + format(end, timezoneId) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "SEQUENCE:3\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SUMMARY:summary\r\n" +
            (null == recurrenceId ? "" : ("RECURRENCE-ID;TZID=" + timezoneId + ":" + format(start, timezoneId) + "\r\n")) +
            (null == rrule ? "" : ("RRULE:" + rrule + "\r\n")) +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=" + timezoneId + ":" + format(start, timezoneId) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "ORGANIZER:mailto:" + organizerMail + "\r\n" +
            "ATTENDEE;PARTSTAT=ACCEPTED:mailto:" + organizerMail + "\r\n"
        ; // @formatter:on
        for (String attendeeMail : attendeeMails) {
            iCal += "ATTENDEE;PARTSTAT=NEEDS-ACTION:mailto:" + attendeeMail + "\r\n";
        }
        return iCal + "END:VEVENT\r\n";
    }

    private static String getEuropeBerlinTimezoneComponent() {
        return // @formatter:off
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
            "END:VTIMEZONE\r\n"
        ; // @formatter:on
    }

}
