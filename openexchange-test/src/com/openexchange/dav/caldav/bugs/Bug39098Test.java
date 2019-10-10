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
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug39098Test}
 *
 * Event disappears when editing one occurrence in a repeating event
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug39098Test extends CalDAVTest {

    @Test
    public void testCreateInAsiaShanghai() throws Exception {

        /*
         * create appointment on client
         */
        TimeZone asiaShanghai = TimeZone.getTimeZone("Asia/Shanghai");
        Date startDate = TimeTools.D("may 30 at 7 pm", asiaShanghai);
        Date endDate = TimeTools.D("may 30 at 8 pm", asiaShanghai);
        String uid = randomUID();
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.10.3//EN\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Asia/Shanghai\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0900\r\n" +
            "RRULE:FREQ=YEARLY;UNTIL=19910914T150000Z;BYMONTH=9;BYDAY=3SU\r\n" +
            "DTSTART:19890917T000000\r\n" +
            "TZNAME:GMT+8\r\n" +
            "TZOFFSETTO:+0800\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0800\r\n" +
            "DTSTART:19910414T000000\r\n" +
            "TZNAME:GMT+8\r\n" +
            "TZOFFSETTO:+0900\r\n" +
            "RDATE:19910414T000000\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:20150629T100123Z\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTART;TZID=Asia/Shanghai:" + format(startDate, asiaShanghai) + "\r\n" +
            "DTEND;TZID=Asia/Shanghai:" + format(endDate, asiaShanghai) + "\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "SUMMARY:test\r\n" +
            "DTSTAMP:20150629T100123Z\r\n" +
            "SEQUENCE:0\r\n" +
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
        assertEquals(startDate, appointment.getStartDate());
        assertEquals(endDate, appointment.getEndDate());
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals(startDate, iCalResource.getVEvent().getDTStart());
        assertEquals(endDate, iCalResource.getVEvent().getDTEnd());
    }

}
