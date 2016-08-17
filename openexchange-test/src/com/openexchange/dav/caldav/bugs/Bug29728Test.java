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
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug29728Test}
 *
 * Blackberry Z10 cause NullPointerException in CaldavPerformer
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug29728Test extends CalDAVTest {

	@Test
	public void testCreateSeriesWithDeleteExceptions() throws Exception {
		/*
		 * create appointment in default folder on client
		 */
		String uid = randomUID();
        Date start = TimeTools.D("next friday at 8:00");
    	Date end = TimeTools.D("next thursday at 10:00");
    	Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    	calendar.setTime(start);
        calendar.add(Calendar.WEEK_OF_YEAR, 2);
        Date ex1 = calendar.getTime();
        calendar.add(Calendar.WEEK_OF_YEAR, 2);
        Date ex2 = calendar.getTime();
		String iCal =
		    "BEGIN:VCALENDAR\r\n" +
		    "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\r\n" +
		    "VERSION:2.0\r\n" +
		    "BEGIN:VTIMEZONE\r\n" +
		    "TZID:Europe/Berlin\r\n" +
		    "X-LIC-LOCATION:Europe/Berlin\r\n" +
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
		    "SUMMARY:serie\r\n" +
		    "RRULE:FREQ=DAILY\r\n" +
            "EXDATE:" + formatAsUTC(ex1) + "\r\n" +
            "EXDATE:" + formatAsUTC(ex2) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
		    "LOCATION:da\r\n" +
		    "SEQUENCE:2\r\n" +
		    "END:VEVENT\r\n" +
		    "END:VCALENDAR\r\n"
		;
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
	}

}
