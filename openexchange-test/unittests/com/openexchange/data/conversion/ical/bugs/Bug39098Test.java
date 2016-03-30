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

package com.openexchange.data.conversion.ical.bugs;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.data.conversion.ical.AbstractICalParserTest;
import com.openexchange.groupware.calendar.CalendarDataObject;

/**
 * {@link Bug39098Test}
 *
 * iCal Appointment entered in Asia/Shanghai timezone gets synced with -1 h offset
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug39098Test extends AbstractICalParserTest {

    public void testAsiaShanghai() throws Exception {
        String iCal =
            "BEGIN:VCALENDAR\n"+
            "VERSION:2.0\n"+
            "PRODID:-//Apple Inc.//Mac OS X 10.10.3//EN\n"+
            "CALSCALE:GREGORIAN\n"+
            "BEGIN:VTIMEZONE\n"+
            "TZID:Asia/Shanghai\n"+
            "BEGIN:STANDARD\n"+
            "TZOFFSETFROM:+0900\n"+
            "RRULE:FREQ=YEARLY;UNTIL=19910914T150000Z;BYMONTH=9;BYDAY=3SU\n"+
            "DTSTART:19890917T000000\n"+
            "TZNAME:GMT+8\n"+
            "TZOFFSETTO:+0800\n"+
            "END:STANDARD\n"+
            "BEGIN:DAYLIGHT\n"+
            "TZOFFSETFROM:+0800\n"+
            "DTSTART:19910414T000000\n"+
            "TZNAME:GMT+8\n"+
            "TZOFFSETTO:+0900\n"+
            "RDATE:19910414T000000\n"+
            "END:DAYLIGHT\n"+
            "END:VTIMEZONE\n"+
            "BEGIN:VEVENT\n"+
            "CREATED:20150629T100123Z\n"+
            "UID:335A47A1-464E-440E-9DA7-037507822ED7\n"+
            "DTSTART;TZID=Asia/Shanghai:20150630T190000\n"+
            "DTEND;TZID=Asia/Shanghai:20150630T200000\n"+
            "TRANSP:OPAQUE\n"+
            "SUMMARY:test\n"+
            "DTSTAMP:20150629T100123Z\n"+
            "SEQUENCE:0\n"+
            "END:VEVENT\n"+
            "END:VCALENDAR\n"
        ;
        TimeZone userTimeZone = TimeZone.getTimeZone("Asia/Shanghai");
        /*
         * parse appointment & verify start- and enddate
         */
        CalendarDataObject appointment = parseAppointment(iCal, userTimeZone);
        Calendar calendar = Calendar.getInstance(userTimeZone);
        calendar.set(2015, 5, 30, 19, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date expectedStartDate = calendar.getTime();
        assertEquals(expectedStartDate, appointment.getStartDate());
        calendar.set(2015, 5, 30, 20, 0, 0);
        Date expectedEndDate = calendar.getTime();
        assertEquals(expectedEndDate, appointment.getEndDate());
    }

}
