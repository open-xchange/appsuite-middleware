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
import static org.junit.Assert.assertNotNull;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;

/**
 * {@link NewTest} - 500 an CalDAVWriteEntityQueueableOperation.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug21794Test extends CalDAVTest {

	@Test
	public void testDeclineImportedAppointment() throws Exception {
		/*
		 * create appointment
		 */
		String uid = "WEBEX-MEETING CENTER-6.0292133-" + new Date().getTime();
		String resourceName = uid.replace(".", "");
    	Date start = TimeTools.D("next monday at 15:30");
    	Date end = TimeTools.D("next monday at 16:30");
		String iCal =
				"BEGIN:VCALENDAR" + "\r\n" +
				"VERSION:2.0" + "\r\n" +
				"PRODID:-//Apple Inc.//iCal 5.0.2//EN" + "\r\n" +
				"CALSCALE:GREGORIAN" + "\r\n" +
				"BEGIN:VTIMEZONE" + "\r\n" +
				"TZID:Europe/Amsterdam" + "\r\n" +
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
				"DTEND;TZID=Europe/Amsterdam:" + format(end, "Europe/Amsterdam") + "\r\n" +
				"TRANSP:OPAQUE" + "\r\n" +
				"ORGANIZER;CN=\"webex\":MAILTO:messenger@example.com" + "\r\n" +
				"UID:" + uid + "\r\n" +
				"DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
				"LOCATION:https://open-xchange.webex.com/open-xchange-en" + "\r\n" +
				"DESCRIPTION:stripped" + "\r\n" +
				"SEQUENCE:2" + "\r\n" +
				"SUMMARY:Agorum Integration" + "\r\n" +
				"DTSTART;TZID=Europe/Amsterdam:" + format(start, "Europe/Amsterdam") + "\r\n" +
				"CREATED:" + formatAsUTC(TimeTools.D("yesterday noon")) + "\r\n" +
				"ATTENDEE;CN=\"Open-Xchange Presenter\";CUTYPE=INDIVIDUAL;ROLE=REQ-PARTICIP" + "\r\n" +
				" ANT:MAILTO:webmeeting1@example.com" + "\r\n" +
				"END:VEVENT" + "\r\n" +
				"END:VCALENDAR"
		;

		assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICal(resourceName, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = super.getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        super.rememberForCleanUp(appointment);
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(resourceName);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        /*
         * decline appointment on client
         */
        List<Property> attendees = iCalResource.getVEvent().getProperties("ATTENDEE");
        for (Property property : attendees) {
			if (property.getValue().contains(super.getAJAXClient().getValues().getDefaultAddress())) {
				for (Entry<String, String> attribute : property.getAttributes().entrySet()) {
					if (attribute.getKey().equals("PARTSTAT")) {
						attribute.setValue("DECLINED");
						break;
					}
				}
				break;
			}
		}
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICalUpdate(iCalResource));
        /*
         * verify appointment on server
         */
        appointment = super.getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        UserParticipant[] users = appointment.getUsers();
        assertNotNull("appointment has no users", users);
        UserParticipant declinedPartipant = null;
        for (UserParticipant user : users) {
			if (getAJAXClient().getValues().getUserId() == user.getIdentifier()) {
				declinedPartipant = user;
				break;
			}
		}
        assertNotNull("declining participant not found", declinedPartipant);
        assertEquals("confirmation status wrong", Appointment.DECLINE, declinedPartipant.getConfirm());
        /*
         * verify appointment on client
         */
        iCalResource = super.get(resourceName);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        Property attendee = null;
        attendees = iCalResource.getVEvent().getProperties("ATTENDEE");
        for (Property property : attendees) {
			if (property.getValue().contains(super.getAJAXClient().getValues().getDefaultAddress())) {
				attendee = property;
				break;
			}
		}
        assertNotNull("declining attendee not found", attendee);
        assertEquals("partstat status wrong", "DECLINED", attendee.getAttribute("PARTSTAT"));
	}

}
