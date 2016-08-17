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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug40657Test}
 *
 * Destructive update of appointments
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug40657Test extends CalDAVTest {

	@Test
	public void testApplyToAllFutureOccurrences() throws Exception {
		/*
		 * create daily appointment series including exception on client
		 */
		String uid = randomUID();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Date start = TimeTools.D("next monday at 09:00");
        calendar.setTime(start);
        calendar.add(Calendar.HOUR, 1);
    	Date end = calendar.getTime();
    	calendar.setTime(start);
    	calendar.add(Calendar.DATE, 1);
        Date recurrenceID = calendar.getTime();
        calendar.add(Calendar.HOUR, 3);
        Date exceptionStart = calendar.getTime();
        calendar.add(Calendar.HOUR, 1);
        Date exceptionEnd = calendar.getTime();
        String iCal =
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
            "TRANSP:OPAQUE\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "SEQUENCE:3\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SUMMARY:test99\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY;INTERVAL=1\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:DC22FB18-20C8-4BFC-9DB2-87EC952B615E\r\n" +
            "UID:DC22FB18-20C8-4BFC-9DB2-87EC952B615E\r\n" +
            "TRIGGER;VALUE=DATE-TIME:19760401T005545Z\r\n" +
            "X-APPLE-DEFAULT-ALARM:TRUE\r\n" +
            "ACTION:NONE\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(exceptionEnd, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "SUMMARY:test99\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(exceptionStart, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "SEQUENCE:4\r\n" +
            "RECURRENCE-ID;TZID=Europe/Berlin:" + format(recurrenceID, "Europe/Berlin") + "\r\n" +
            "BEGIN:VALARM\r\n" +
            "X-WR-ALARMUID:6125CE9E-F034-4C42-9480-CF8058393F5F\r\n" +
            "UID:6125CE9E-F034-4C42-9480-CF8058393F5F\r\n" +
            "TRIGGER;VALUE=DATE-TIME:19760401T005545Z\r\n" +
            "X-APPLE-DEFAULT-ALARM:TRUE\r\n" +
            "ACTION:NONE\r\n" +
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
        assertNull(appointment.getUntil());
        assertNotNull(appointment.getChangeException());
        assertEquals(1, appointment.getChangeException().length);
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals(2, iCalResource.getVEvents().size());
        assertEquals(recurrenceID, iCalResource.getVEvents().get(1).getRecurrenceID());
        assertNotNull(iCalResource.getVEvent().getProperty("RRULE"));
        assertFalse(iCalResource.getVEvent().getProperty("RRULE").getValue().contains("UNTIL"));
        /*
         * update appointment on client
         */
        calendar.setTime(start);
        calendar.add(Calendar.DATE, 3);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date until = calendar.getTime();
        Property rRule = iCalResource.getVEvent().getProperty("RRULE");
        iCalResource.getVEvent().setProperty("RRULE", rRule.getValue() + ";UNTIL=" + formatAsUTC(until));
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * verify appointment on server
         */
        appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        assertNotNull(appointment.getUntil());
        assertNotNull(appointment.getChangeException());
        assertEquals(1, appointment.getChangeException().length);
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals(2, iCalResource.getVEvents().size());
        assertEquals(recurrenceID, iCalResource.getVEvents().get(1).getRecurrenceID());
        assertNotNull(iCalResource.getVEvent().getProperty("RRULE"));
        assertTrue(iCalResource.getVEvent().getProperty("RRULE").getValue().contains("UNTIL"));
	}

}
