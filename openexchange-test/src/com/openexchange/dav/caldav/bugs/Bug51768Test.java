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

import static org.junit.Assert.*;
import java.util.Date;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug51768Test}
 *
 * Entering an emoji as appointment title breaks the complete appointment
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class Bug51768Test extends CalDAVTest {

    @Test
    public void testCreateSeriesWithIncorrectString() throws Exception {
        /*
         * create daily appointment series on client
         */
        String summary = "Pile of \uD83D\uDCA9 poo";
        String expectedSummary = summary.replaceAll("\uD83D\uDCA9", "");
        String uid = randomUID();
        Date start = TimeTools.D("next monday at 09:00");
        Date end = TimeTools.D("next monday at 09:30");
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
            "TRANSP:OPAQUE\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "SEQUENCE:3\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SUMMARY:" + summary + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY;INTERVAL=1\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        assertEquals("Title wrong", expectedSummary, appointment.getTitle());
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No SUMMARY in iCal found", iCalResource.getVEvent().getSummary());
        assertEquals("SUMMARY wrong", expectedSummary, iCalResource.getVEvent().getSummary());
        assertEquals("DTSTART wrong", start, iCalResource.getVEvent().getDTStart());
        assertEquals("DTEND wrong", end, iCalResource.getVEvent().getDTEnd());
    }

    @Test
    public void testUpdateSeriesWithIncorrectString() throws Exception {
        /*
         * create daily appointment series on client
         */
        String summary = "Pile of poo";
        String expectedSummary = summary.replaceAll("\uD83D\uDCA9", "");
        String uid = randomUID();
        Date start = TimeTools.D("next monday at 09:00");
        Date end = TimeTools.D("next monday at 09:30");
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
            "TRANSP:OPAQUE\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "SEQUENCE:3\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SUMMARY:" + summary + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY;INTERVAL=1\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        assertEquals("Title wrong", expectedSummary, appointment.getTitle());
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No SUMMARY in iCal found", iCalResource.getVEvent().getSummary());
        assertEquals("SUMMARY wrong", expectedSummary, iCalResource.getVEvent().getSummary());
        assertEquals("DTSTART wrong", start, iCalResource.getVEvent().getDTStart());
        assertEquals("DTEND wrong", end, iCalResource.getVEvent().getDTEnd());
        /*
         * update appointment on client
         */
        summary = "Pile of \uD83D\uDCA9 poo";
        expectedSummary = summary.replaceAll("\uD83D\uDCA9", "");
        iCalResource.getVEvent().setSummary(summary);
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * verify appointment on server
         */
        appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        assertEquals("Title wrong", expectedSummary, appointment.getTitle());
        /*
         * verify appointment on client
         */
        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No SUMMARY in iCal found", iCalResource.getVEvent().getSummary());
        assertEquals("SUMMARY wrong", expectedSummary, iCalResource.getVEvent().getSummary());
        assertEquals("DTSTART wrong", start, iCalResource.getVEvent().getDTStart());
        assertEquals("DTEND wrong", end, iCalResource.getVEvent().getDTEnd());
    }

}
