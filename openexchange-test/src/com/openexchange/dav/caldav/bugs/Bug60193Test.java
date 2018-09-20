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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.Collections;
import java.util.Date;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.WebDAVClient;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;

/**
 * {@link Bug60193Test}
 *
 * Calendar: Accepted appointments continue to show up as to be accepted
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.1
 */
public class Bug60193Test extends CalDAVTest {

    @Test
    public void testRSVPAfterAcceptOnServer() throws Exception {
        /*
         * as user b, create appointment via caldav & set rsvp flag for user a
         */
        String uid = randomUID();
        Date start = TimeTools.D("next monday at 11:30");
        Date end = TimeTools.D("next monday at 12:15");
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
            "SUMMARY:Test\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "ORGANIZER:mailto:" + getClient2().getValues().getDefaultAddress() + "\r\n" +
            "ATTENDEE;PARTSTAT=ACCEPTED:mailto:" + getClient2().getValues().getDefaultAddress() + "\r\n" +
            "ATTENDEE;PARTSTAT=NEEDS-ACTION;RSVP=TRUE:mailto:" + getClient().getValues().getDefaultAddress() + "\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        WebDAVClient webDAVClient2 = new WebDAVClient(testUser2, getDefaultUserAgent(), null);
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(webDAVClient2, String.valueOf(getClient2().getValues().getPrivateAppointmentFolder()), uid, iCal, Collections.emptyMap()));
        /*
         * as user a, check appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        Property userAttendee = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("attendee not found in ical", userAttendee);
        assertEquals("RSVP wrong in user attendee", "TRUE", userAttendee.getAttribute("RSVP"));
        /*
         * as user a, accept appointment on server
         */
        Appointment appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        Participant participant = null;
        for (Participant p : appointment.getParticipants()) {
            if (getClient().getValues().getUserId() == p.getIdentifier()) {
                participant = p;
                break;
            }
        }
        assertNotNull("participant not found on server", participant);
        getManager().confirm(appointment, Appointment.ACCEPT, "ok");
        /*
         * as user a, check appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        userAttendee = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("attendee not found in ical", userAttendee);
        assertEquals("PARTSTAT wrong in user attendee", "ACCEPTED", userAttendee.getAttribute("PARTSTAT"));
        assertNotEquals("RSVP wrong in user attendee", "TRUE", userAttendee.getAttribute("RSVP"));
    }

    @Test
    public void testRSVPAfterAcceptOnClient() throws Exception {
        /*
         * as user b, create appointment via caldav & set rsvp flag for user a
         */
        String uid = randomUID();
        Date start = TimeTools.D("next monday at 11:30");
        Date end = TimeTools.D("next monday at 12:15");
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
            "SUMMARY:Test\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "ORGANIZER:mailto:" + getClient2().getValues().getDefaultAddress() + "\r\n" +
            "ATTENDEE;PARTSTAT=ACCEPTED:mailto:" + getClient2().getValues().getDefaultAddress() + "\r\n" +
            "ATTENDEE;PARTSTAT=NEEDS-ACTION;RSVP=TRUE:mailto:" + getClient().getValues().getDefaultAddress() + "\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        WebDAVClient webDAVClient2 = new WebDAVClient(testUser2, getDefaultUserAgent(), null);
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(webDAVClient2, String.valueOf(getClient2().getValues().getPrivateAppointmentFolder()), uid, iCal, Collections.emptyMap()));
        /*
         * as user a, check appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        Property userAttendee = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("attendee not found in ical", userAttendee);
        assertEquals("RSVP wrong in user attendee", "TRUE", userAttendee.getAttribute("RSVP"));
        /*
         * as user a, accept appointment on client
         */
        userAttendee.getAttributes().put("PARTSTAT", "ACCEPTED");
        userAttendee.getAttributes().remove("RSVP");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * as user a, check appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        userAttendee = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("attendee not found in ical", userAttendee);
        assertEquals("PARTSTAT wrong in user attendee", "ACCEPTED", userAttendee.getAttribute("PARTSTAT"));
        assertNull("RSVP wrong in user attendee", userAttendee.getAttribute("RSVP"));
    }

    @Test
    public void testRSVPAfterAcceptOnClientWithoutRemovingRSVP() throws Exception {
        /*
         * as user b, create appointment via caldav & set rsvp flag for user a
         */
        String uid = randomUID();
        Date start = TimeTools.D("next monday at 11:30");
        Date end = TimeTools.D("next monday at 12:15");
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
            "SUMMARY:Test\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "ORGANIZER:mailto:" + getClient2().getValues().getDefaultAddress() + "\r\n" +
            "ATTENDEE;PARTSTAT=ACCEPTED:mailto:" + getClient2().getValues().getDefaultAddress() + "\r\n" +
            "ATTENDEE;PARTSTAT=NEEDS-ACTION;RSVP=TRUE:mailto:" + getClient().getValues().getDefaultAddress() + "\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        WebDAVClient webDAVClient2 = new WebDAVClient(testUser2, getDefaultUserAgent(), null);
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(webDAVClient2, String.valueOf(getClient2().getValues().getPrivateAppointmentFolder()), uid, iCal, Collections.emptyMap()));
        /*
         * as user a, check appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        Property userAttendee = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("attendee not found in ical", userAttendee);
        assertEquals("RSVP wrong in user attendee", "TRUE", userAttendee.getAttribute("RSVP"));
        /*
         * as user a, accept appointment on client
         */
        userAttendee.getAttributes().put("PARTSTAT", "ACCEPTED");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * as user a, check appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        userAttendee = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("attendee not found in ical", userAttendee);
        assertEquals("PARTSTAT wrong in user attendee", "ACCEPTED", userAttendee.getAttribute("PARTSTAT"));
        assertNotEquals("RSVP wrong in user attendee", "TRUE", userAttendee.getAttribute("RSVP"));
    }

    @Test
    public void testRSVPAfterAcceptingAcceptedOnClient() throws Exception {
        /*
         * as user b, "import" appointment via caldav & set rsvp flag for user a (already accepted)
         */
        String uid = randomUID();
        Date start = TimeTools.D("next monday at 11:30");
        Date end = TimeTools.D("next monday at 12:15");
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
            "SUMMARY:Test\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "ORGANIZER:mailto:" + getClient2().getValues().getDefaultAddress() + "\r\n" +
            "ATTENDEE;PARTSTAT=ACCEPTED:mailto:" + getClient2().getValues().getDefaultAddress() + "\r\n" +
            "ATTENDEE;PARTSTAT=ACCEPTED;RSVP=TRUE:mailto:" + getClient().getValues().getDefaultAddress() + "\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        WebDAVClient webDAVClient2 = new WebDAVClient(testUser2, getDefaultUserAgent(), null);
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(webDAVClient2, String.valueOf(getClient2().getValues().getPrivateAppointmentFolder()), uid, iCal, Collections.emptyMap()));
        /*
         * as user a, check appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        Property userAttendee = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("attendee not found in ical", userAttendee);
        assertEquals("RSVP wrong in user attendee", "TRUE", userAttendee.getAttribute("RSVP"));
        assertEquals("PARTSTAT wrong in user attendee", "ACCEPTED", userAttendee.getAttribute("PARTSTAT"));
        /*
         * as user a, accept appointment on client
         */
        userAttendee.getAttributes().put("PARTSTAT", "ACCEPTED");
        userAttendee.getAttributes().remove("RSVP");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * as user a, check appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        userAttendee = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("attendee not found in ical", userAttendee);
        assertEquals("PARTSTAT wrong in user attendee", "ACCEPTED", userAttendee.getAttribute("PARTSTAT"));
        assertNull("RSVP wrong in user attendee", userAttendee.getAttribute("RSVP"));
    }

}
