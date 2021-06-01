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
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug55916Test} - after creating an event in korganizer, the cal-dav agent crash
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.2
 */
public class Bug55916Test extends CalDAVTest {

    @Test
    public void testKOrganizer() throws Exception {
        /*
         * create appointment
         */
        String filename = String.valueOf(System.currentTimeMillis() / 1000) + ".R271";
        String uid = randomUID();
        Date start = TimeTools.D("tomorrow at 15:30");
        Date end = TimeTools.D("tomorrow at 16:00");
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" + 
            "PRODID:-//K Desktop Environment//NONSGML libkcal 4.3//EN\r\n" + 
            "VERSION:2.0\r\n" + 
            "X-KDE-ICAL-IMPLEMENTATION-VERSION:1.0\r\n" + 
            "BEGIN:VTIMEZONE\r\n" + 
            "TZID:Europe/Berlin\r\n" + 
            "BEGIN:STANDARD\r\n" + 
            "TZNAME:CET\r\n" + 
            "TZOFFSETFROM:+0000\r\n" + 
            "TZOFFSETTO:+0100\r\n" + 
            "DTSTART:19791231T230000\r\n" + 
            "RDATE;VALUE=DATE-TIME:19791231T230000\r\n" + 
            "END:STANDARD\r\n" + 
            "BEGIN:DAYLIGHT\r\n" + 
            "TZNAME:CEST\r\n" + 
            "TZOFFSETFROM:+0100\r\n" + 
            "TZOFFSETTO:+0200\r\n" + 
            "DTSTART:19810329T020000\r\n" + 
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" + 
            "END:DAYLIGHT\r\n" + 
            "BEGIN:DAYLIGHT\r\n" + 
            "TZNAME:CEST\r\n" + 
            "TZOFFSETFROM:+0100\r\n" + 
            "TZOFFSETTO:+0200\r\n" + 
            "DTSTART:19800406T020000\r\n" + 
            "RDATE;VALUE=DATE-TIME:19800406T020000\r\n" + 
            "END:DAYLIGHT\r\n" + 
            "BEGIN:STANDARD\r\n" + 
            "TZNAME:CET\r\n" + 
            "TZOFFSETFROM:+0200\r\n" + 
            "TZOFFSETTO:+0100\r\n" + 
            "DTSTART:19971026T030000\r\n" + 
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" + 
            "END:STANDARD\r\n" + 
            "BEGIN:STANDARD\r\n" + 
            "TZNAME:CET\r\n" + 
            "TZOFFSETFROM:+0200\r\n" + 
            "TZOFFSETTO:+0100\r\n" + 
            "DTSTART:19800928T030000\r\n" + 
            "RRULE:FREQ=YEARLY;UNTIL=19961027T030000;COUNT=16;BYDAY=-1SU;BYMONTH=9\r\n" + 
            "RDATE;VALUE=DATE-TIME:19950924T030000\r\n" + 
            "END:STANDARD\r\n" + 
            "END:VTIMEZONE\r\n" + 
            "BEGIN:VEVENT\r\n" + 
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "LAST-MODIFIED:20190402T123639Z\r\n" + 
            "SUMMARY:qwer6z7\r\n" + 
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "TRANSP:OPAQUE\r\n" + 
            "END:VEVENT\r\n" + 
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(getDefaultFolderID(), filename, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        /*
         * upload the same appointment again, using a different resource name
         */
        String filename2 = String.valueOf(System.currentTimeMillis() / 1000 + 7) + ".R271";
        assertEquals("response code wrong", StatusCodes.SC_FORBIDDEN, putICal(getDefaultFolderID(), filename2, iCal));
    }

}
