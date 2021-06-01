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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug66234Test}
 *
 * com.openexchange.usm.api.exceptions.OXCommunicationException: OX server returned error
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class Bug66234Test extends CalDAVTest {

    @Test
    public void testExceptionWithInvalidRecurrenceId() throws Exception {
        /*
         * create event series
         */
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        String uid = randomUID();
        Date start = TimeTools.D("next saturday at 15:30", timeZone);
        Date end = TimeTools.D("next saturday at 17:15", timeZone);
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
            "SUMMARY:Bug66234Test\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "RRULE:FREQ=DAILY;INTERVAL=1\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify series on server
         */
        Appointment appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        /*
         * verify series on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        /*
         * prepare & add invalid exception component
         */
        Date exceptionStart = TimeTools.D("last saturday at 15:30", timeZone);
        Date exceptionEnd = TimeTools.D("last saturday at 17:15", timeZone);
        Component exceptionEvent = get(uid).getVEvent();
        exceptionEvent.setLocation("edit");
        exceptionEvent.removeProperties("RRULE");
        exceptionEvent.removeProperties("EXDATE");
        exceptionEvent.setDTStart(exceptionStart, timeZone.getID());
        exceptionEvent.setDTEnd(exceptionEnd, timeZone.getID());
        exceptionEvent.setProperty("RECURRENCE-ID", formatAsUTC(exceptionStart));
        iCalResource.addComponent(exceptionEvent);
        /*
         * try and update the series with invalid exception
         */
        int responseCode = putICalUpdate(iCalResource);
        assertNotEquals("response code wrong", StatusCodes.SC_CREATED, responseCode);
        /*
         * check that no change exception was created
         */
        List<Appointment> changeExceptions = catm.getChangeExceptions(appointment.getParentFolderID(), appointment.getObjectID(), Appointment.ALL_COLUMNS);
        assertTrue("no change exceptions expected", null == changeExceptions || changeExceptions.isEmpty());
        iCalResource = get(uid);
        assertEquals("Expected one VEVENT component only", 1, iCalResource.getVEvents().size());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
    }
}
