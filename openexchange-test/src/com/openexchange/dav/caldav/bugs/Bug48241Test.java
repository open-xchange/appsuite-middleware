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
import java.util.Date;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug48241Test}
 *
 * response mail for accept /decline is not sent to organizer
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.3
 */
public class Bug48241Test extends CalDAVTest {

    @Test
    public void testAcceptImportedAppointment() throws Exception {
        /*
         * create appointment
         */
        String uid = randomUID();
        Date start = TimeTools.D("next friday at 14:30");
        Date end = TimeTools.D("next friday at 15:30");
        String iCal = "BEGIN:VCALENDAR" + "\r\n" + "VERSION:2.0" + "\r\n" + "METHOD:REQUEST" + "\r\n" + "PRODID:-//Apple Inc.//Mac OS X 10.8.2//EN" + "\r\n" + "CALSCALE:GREGORIAN" + "\r\n" + "BEGIN:VTIMEZONE" + "\r\n" + "TZID:Europe/Berlin" + "\r\n" + "BEGIN:DAYLIGHT" + "\r\n" + "TZOFFSETFROM:+0100" + "\r\n" + "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU" + "\r\n" + "DTSTART:19810329T020000" + "\r\n" + "TZNAME:CEST" + "\r\n" + "TZOFFSETTO:+0200" + "\r\n" + "END:DAYLIGHT" + "\r\n" + "BEGIN:STANDARD" + "\r\n" + "TZOFFSETFROM:+0200" + "\r\n" + "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU" + "\r\n" + "DTSTART:19961027T030000" + "\r\n" + "TZNAME:CET" + "\r\n" + "TZOFFSETTO:+0100" + "\r\n" + "END:STANDARD" + "\r\n" + "END:VTIMEZONE" + "\r\n" + "BEGIN:VEVENT" + "\r\n" + "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" + "TRANSP:OPAQUE" + "\r\n" + "ORGANIZER;CN=Horst:mailto:horst@example.com" + "\r\n" + "UID:" + uid + "\r\n" + "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" + "LOCATION:loc" + "\r\n" + "DESCRIPTION:stripped" + "\r\n" + "SEQUENCE:2" + "\r\n" + "SUMMARY:test accept" + "\r\n" + "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" + "CREATED:" + formatAsUTC(TimeTools.D("yesterday noon")) + "\r\n" + "ATTENDEE;CN=Horst;CUTYPE=INDIVIDUAL;PARTSTAT=ACCEPTED;ROLE=REQ-PARTICIPANT:mailto:horst@example.com" + "\r\n" + "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION:mailto:" + getClient().getValues().getDefaultAddress() + "\r\n" + "END:VEVENT" + "\r\n" + "END:VCALENDAR";
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = super.getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        super.rememberForCleanUp(appointment);
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        Property attendee = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("ATTENDEE not found", attendee);
        assertEquals("PARTSTAT wrong", "NEEDS-ACTION", attendee.getAttribute("PARTSTAT"));
        /*
         * accept appointment on client
         */
        attendee.getAttributes().put("PARTSTAT", "ACCEPTED");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * verify appointment on server
         */
        appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        UserParticipant[] users = appointment.getUsers();
        assertNotNull("appointment has no users", users);
        UserParticipant partipant = null;
        for (UserParticipant user : users) {
            if (getAJAXClient().getValues().getUserId() == user.getIdentifier()) {
                partipant = user;
                break;
            }
        }
        assertNotNull("accepting participant not found", partipant);
        assertEquals("confirmation status wrong", Appointment.ACCEPT, partipant.getConfirm());
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        attendee = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("ATTENDEE not found", attendee);
        assertEquals("PARTSTAT wrong", "ACCEPTED", attendee.getAttribute("PARTSTAT"));
    }

}
