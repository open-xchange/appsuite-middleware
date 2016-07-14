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

package com.openexchange.dav.caldav.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug31453Test}
 *
 * Calendar reminders do not show up in iCal when creating an event in a public calendar
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug31453Test extends CalDAVTest {

    private FolderObject publicFolder = null;
    private String publicFolderID = null;

    private static final String[] DUMMY_ALARM_INDICATORS = new String[] {"TRIGGER;VALUE=DATE-TIME:19760401T005545Z", "X-APPLE-LOCAL-DEFAULT-ALARM:TRUE", "X-APPLE-DEFAULT-ALARM:TRUE"};

    @Before
    public void setUp() throws Exception {
        publicFolder = createPublicFolder();
        publicFolderID = String.valueOf(publicFolder.getObjectID());
    }

    @Test
	public void testCreateReminderInClient() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next sunday at 14:15");
        Date end = TimeTools.D("next sunday at 15:30");
        String iCal =
                "BEGIN:VCALENDAR" + "\r\n" +
                "VERSION:2.0" + "\r\n" +
                "METHOD:REQUEST" + "\r\n" +
                "PRODID:-//Apple Inc.//Mac OS X 10.8.2//EN" + "\r\n" +
                "CALSCALE:GREGORIAN" + "\r\n" +
                "BEGIN:VTIMEZONE" + "\r\n" +
                "TZID:Europe/Berlin" + "\r\n" +
                "BEGIN:DAYLIGHT" + "\r\n" +
                "TZOFFSETFROM:+0100" + "\r\n" +
                "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU" + "\r\n" +
                "DTSTART:19810329T020000" + "\r\n" +
                "TZNAME:CEST" + "\r\n" +
                "TZOFFSETTO:+0200" + "\r\n" +
                "END:DAYLIGHT" + "\r\n" +
                "BEGIN:STANDARD" + "\r\n" +
                "TZOFFSETFROM:+0200" + "\r\n" +
                "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU" + "\r\n" +
                "DTSTART:19961027T030000" + "\r\n" +
                "TZNAME:CET" + "\r\n" +
                "TZOFFSETTO:+0100" + "\r\n" +
                "END:STANDARD" + "\r\n" +
                "END:VTIMEZONE" + "\r\n" +
                "BEGIN:VEVENT" + "\r\n" +
                "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
                "TRANSP:OPAQUE" + "\r\n" +
                "UID:" + uid + "\r\n" +
                "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
                "LOCATION:loc" + "\r\n" +
                "DESCRIPTION:stripped" + "\r\n" +
                "SEQUENCE:2" + "\r\n" +
                "SUMMARY:test" + "\r\n" +
                "BEGIN:VALARM" + "\r\n" +
                "X-WR-ALARMUID:" + uid + "\r\n" +
                "UID:" + uid + "\r\n" +
                "TRIGGER:-PT15M" + "\r\n" +
                "DESCRIPTION:Ereignisbenachrichtigung" + "\r\n" +
                "ACTION:DISPLAY" + "\r\n" +
                "END:VALARM" + "\r\n" +
                "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
                "CREATED:" + formatAsUTC(TimeTools.D("yesterday noon")) + "\r\n" +
                "END:VEVENT" + "\r\n" +
                "END:VCALENDAR"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICal(publicFolderID, uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = super.getAppointment(publicFolderID, uid);
        assertNotNull("appointment not found on server", appointment);
        super.rememberForCleanUp(appointment);
        assertTrue("no reminder found", appointment.containsAlarm());
        assertEquals("reminder minuteswrong", 15, appointment.getAlarm());
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = super.get(publicFolderID, uid, null);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
	}

    @Test
    public void testRemoveReminderInClient() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next sunday at 14:15");
        Date end = TimeTools.D("next sunday at 15:30");
        String iCal =
                "BEGIN:VCALENDAR" + "\r\n" +
                "VERSION:2.0" + "\r\n" +
                "METHOD:REQUEST" + "\r\n" +
                "PRODID:-//Apple Inc.//Mac OS X 10.8.2//EN" + "\r\n" +
                "CALSCALE:GREGORIAN" + "\r\n" +
                "BEGIN:VTIMEZONE" + "\r\n" +
                "TZID:Europe/Berlin" + "\r\n" +
                "BEGIN:DAYLIGHT" + "\r\n" +
                "TZOFFSETFROM:+0100" + "\r\n" +
                "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU" + "\r\n" +
                "DTSTART:19810329T020000" + "\r\n" +
                "TZNAME:CEST" + "\r\n" +
                "TZOFFSETTO:+0200" + "\r\n" +
                "END:DAYLIGHT" + "\r\n" +
                "BEGIN:STANDARD" + "\r\n" +
                "TZOFFSETFROM:+0200" + "\r\n" +
                "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU" + "\r\n" +
                "DTSTART:19961027T030000" + "\r\n" +
                "TZNAME:CET" + "\r\n" +
                "TZOFFSETTO:+0100" + "\r\n" +
                "END:STANDARD" + "\r\n" +
                "END:VTIMEZONE" + "\r\n" +
                "BEGIN:VEVENT" + "\r\n" +
                "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
                "TRANSP:OPAQUE" + "\r\n" +
                "UID:" + uid + "\r\n" +
                "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
                "LOCATION:loc" + "\r\n" +
                "DESCRIPTION:stripped" + "\r\n" +
                "SEQUENCE:2" + "\r\n" +
                "SUMMARY:test" + "\r\n" +
                "BEGIN:VALARM" + "\r\n" +
                "X-WR-ALARMUID:" + uid + "\r\n" +
                "UID:" + uid + "\r\n" +
                "TRIGGER:-PT15M" + "\r\n" +
                "DESCRIPTION:Ereignisbenachrichtigung" + "\r\n" +
                "ACTION:DISPLAY" + "\r\n" +
                "END:VALARM" + "\r\n" +
                "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
                "CREATED:" + formatAsUTC(TimeTools.D("yesterday noon")) + "\r\n" +
                "END:VEVENT" + "\r\n" +
                "END:VCALENDAR"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICal(publicFolderID, uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = super.getAppointment(publicFolderID, uid);
        assertNotNull("appointment not found on server", appointment);
        super.rememberForCleanUp(appointment);
        assertTrue("no reminder found", appointment.containsAlarm());
        assertEquals("reminder minutes wrong", 15, appointment.getAlarm());
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = super.get(publicFolderID, uid, null);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        /*
         * remove reminder on client
         */
        Component vAlarm = iCalResource.getVEvent().getVAlarm();
        iCalResource.getVEvent().getComponents().remove(vAlarm);
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICalUpdate(iCalResource));
        /*
         * verify appointment on server
         */
        appointment = super.getAppointment(publicFolderID, uid);
        assertNotNull("appointment not found on server", appointment);
        assertFalse("reminder found", appointment.containsAlarm());
        assertEquals("reminder minutes wrong", 0, appointment.getAlarm());
        /*
         * verify appointment on client
         */
        iCalResource = super.get(publicFolderID, uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertDummyAlarm(iCalResource.getVEvent());
    }

    @Test
    public void testEditReminderInClient() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next sunday at 14:15");
        Date end = TimeTools.D("next sunday at 15:30");
        String iCal =
                "BEGIN:VCALENDAR" + "\r\n" +
                "VERSION:2.0" + "\r\n" +
                "METHOD:REQUEST" + "\r\n" +
                "PRODID:-//Apple Inc.//Mac OS X 10.8.2//EN" + "\r\n" +
                "CALSCALE:GREGORIAN" + "\r\n" +
                "BEGIN:VTIMEZONE" + "\r\n" +
                "TZID:Europe/Berlin" + "\r\n" +
                "BEGIN:DAYLIGHT" + "\r\n" +
                "TZOFFSETFROM:+0100" + "\r\n" +
                "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU" + "\r\n" +
                "DTSTART:19810329T020000" + "\r\n" +
                "TZNAME:CEST" + "\r\n" +
                "TZOFFSETTO:+0200" + "\r\n" +
                "END:DAYLIGHT" + "\r\n" +
                "BEGIN:STANDARD" + "\r\n" +
                "TZOFFSETFROM:+0200" + "\r\n" +
                "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU" + "\r\n" +
                "DTSTART:19961027T030000" + "\r\n" +
                "TZNAME:CET" + "\r\n" +
                "TZOFFSETTO:+0100" + "\r\n" +
                "END:STANDARD" + "\r\n" +
                "END:VTIMEZONE" + "\r\n" +
                "BEGIN:VEVENT" + "\r\n" +
                "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
                "TRANSP:OPAQUE" + "\r\n" +
                "UID:" + uid + "\r\n" +
                "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
                "LOCATION:loc" + "\r\n" +
                "DESCRIPTION:stripped" + "\r\n" +
                "SEQUENCE:2" + "\r\n" +
                "SUMMARY:test" + "\r\n" +
                "BEGIN:VALARM" + "\r\n" +
                "X-WR-ALARMUID:" + uid + "\r\n" +
                "UID:" + uid + "\r\n" +
                "TRIGGER:-PT15M" + "\r\n" +
                "DESCRIPTION:Ereignisbenachrichtigung" + "\r\n" +
                "ACTION:DISPLAY" + "\r\n" +
                "END:VALARM" + "\r\n" +
                "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
                "CREATED:" + formatAsUTC(TimeTools.D("yesterday noon")) + "\r\n" +
                "END:VEVENT" + "\r\n" +
                "END:VCALENDAR"
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICal(publicFolderID, uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = super.getAppointment(publicFolderID, uid);
        assertNotNull("appointment not found on server", appointment);
        super.rememberForCleanUp(appointment);
        assertTrue("no reminder found", appointment.containsAlarm());
        assertEquals("reminder minutes wrong", 15, appointment.getAlarm());
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = super.get(publicFolderID, uid, null);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT15M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        /*
         * edit reminder on client
         */
        iCalResource.getVEvent().getVAlarm().setProperty("TRIGGER", "-PT20M");
        iCalResource.getVEvent().getVAlarm().removeProperties("ACKNOWLEDGED");
        iCalResource.getVEvent().getVAlarm().removeProperties("X-MOZ-LASTACK");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICalUpdate(iCalResource));
        /*
         * verify appointment on server
         */
        appointment = super.getAppointment(publicFolderID, uid);
        assertNotNull("appointment not found on server", appointment);
        assertTrue("no reminder found", appointment.containsAlarm());
        assertEquals("reminder minutes wrong", 20, appointment.getAlarm());
        /*
         * verify appointment on client
         */
        iCalResource = super.get(publicFolderID, uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT20M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
    }

    @Test
    public void testCreateReminderAtServer() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next friday at 08:50");
        Date end = TimeTools.D("next friday at 09:20");
        Appointment appointment = generateAppointment(start, end, uid, "test", "test");
        appointment.setAlarm(30);
        appointment = create(publicFolderID, appointment);
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = super.get(String.valueOf(publicFolder.getObjectID()), uid, null);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT30M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
    }

    @Test
    public void testRemoveReminderAtServer() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next friday at 08:50");
        Date end = TimeTools.D("next friday at 09:20");
        Appointment appointment = generateAppointment(start, end, uid, "test", "test");
        appointment.setAlarm(30);
        appointment = create(publicFolderID, appointment);
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = super.get(String.valueOf(publicFolder.getObjectID()), uid, null);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT30M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        /*
         * remove reminder at server
         */
        appointment.setAlarm(-1);
        super.update(appointment);
        /*
         * verify appointment on client
         */
        iCalResource = super.get(publicFolderID, uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertDummyAlarm(iCalResource.getVEvent());
    }

    @Test
    public void testEditReminderAtServer() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next friday at 08:50");
        Date end = TimeTools.D("next friday at 09:20");
        Appointment appointment = generateAppointment(start, end, uid, "test", "test");
        appointment.setAlarm(30);
        appointment = create(publicFolderID, appointment);
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = super.get(String.valueOf(publicFolder.getObjectID()), uid, null);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT30M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
        /*
         * edit reminder at server
         */
        appointment.setAlarm(20);
        super.update(appointment);
        /*
         * verify appointment on client
         */
        iCalResource = super.get(publicFolderID, uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertNotNull("No ALARM in iCal found", iCalResource.getVEvent().getVAlarm());
        assertEquals("ALARM wrong", "-PT20M", iCalResource.getVEvent().getVAlarm().getPropertyValue("TRIGGER"));
    }

}
