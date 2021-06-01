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
import java.util.Calendar;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug37112Test}
 *
 * Event disappears when editing one occurrence in a repeating event
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug37112Test extends CalDAVTest {

    private FolderObject publicFolder = null;
    private String publicFolderID = null;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        publicFolder = createPublicFolder();
        publicFolderID = String.valueOf(publicFolder.getObjectID());
    }

    @Test
    public void testCreateExceptionInClient() throws Exception {
        /*
         * create recurring appointment on client
         */
        String uid = randomUID();
        Date start = TimeTools.D("next monday at 12:00");
        Date end = TimeTools.D("next monday at 13:00");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        calendar.add(Calendar.DATE, 7);
        Date until = calendar.getTime();
        String iCal = "BEGIN:VCALENDAR" + "\r\n" + "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN" + "\r\n" + "VERSION:2.0" + "\r\n" + "BEGIN:VTIMEZONE" + "\r\n" + "TZID:Europe/Berlin" + "\r\n" + "X-LIC-LOCATION:Europe/Berlin" + "\r\n" + "BEGIN:DAYLIGHT" + "\r\n" + "TZOFFSETFROM:+0100" + "\r\n" + "TZOFFSETTO:+0200" + "\r\n" + "TZNAME:CEST" + "\r\n" + "DTSTART:19700329T020000" + "\r\n" + "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3" + "\r\n" + "END:DAYLIGHT" + "\r\n" + "BEGIN:STANDARD" + "\r\n" + "TZOFFSETFROM:+0200" + "\r\n" + "TZOFFSETTO:+0100" + "\r\n" + "TZNAME:CET" + "\r\n" + "DTSTART:19701025T030000" + "\r\n" + "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10" + "\r\n" + "END:STANDARD" + "\r\n" + "END:VTIMEZONE" + "\r\n" + "BEGIN:VEVENT" + "\r\n" + "CREATED:" + formatAsUTC(new Date()) + "\r\n" + "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" + "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" + "UID:" + uid + "\r\n" + "SUMMARY:testserie" + "\r\n" + "RRULE:FREQ=DAILY;UNTIL=" + formatAsUTC(until) + "\r\n" + "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" + "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" + "TRANSP:OPAQUE" + "\r\n" + "END:VEVENT" + "\r\n" + "END:VCALENDAR";
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICal(publicFolderID, uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = super.getAppointment(publicFolderID, uid);
        assertNotNull("appointment not found on server", appointment);
        super.rememberForCleanUp(appointment);
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = super.get(publicFolderID, uid, null);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        /*
         * create exception on client
         */
        calendar.setTime(start);
        calendar.add(Calendar.DATE, 2);
        Date exceptionStart = calendar.getTime();
        calendar.setTime(end);
        calendar.add(Calendar.DATE, 2);
        Date exceptionEnd = calendar.getTime();
        String iCalException = "BEGIN:VEVENT" + "\r\n" + "CREATED:" + formatAsUTC(new Date()) + "\r\n" + "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" + "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" + "UID:" + uid + "\r\n" + "SUMMARY:testserie edit" + "\r\n" + "RECURRENCE-ID;TZID=Europe/Berlin:" + format(exceptionStart, "Europe/Berlin") + "\r\n" + iCalResource.getVEvent().getProperty("ORGANIZER").toString() + "\r\n" + iCalResource.getVEvent().getProperty("ATTENDEE").toString() + "\r\n" + "DTSTART;TZID=Europe/Berlin:" + format(exceptionStart, "Europe/Berlin") + "\r\n" + "DTEND;TZID=Europe/Berlin:" + format(exceptionEnd, "Europe/Berlin") + "\r\n" + "SEQUENCE:1" + "\r\n" + "CLASS:PUBLIC" + "\r\n" + "TRANSP:OPAQUE" + "\r\n" + "X-MOZ-GENERATION:1" + "\r\n" + "END:VEVENT";
        iCalResource.addComponent(SimpleICal.parse(iCalException, "VEVENT"));
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICalUpdate(iCalResource));
        /*
         * verify appointment & exception on server
         */
        appointment = getAppointment(publicFolderID, uid);
        assertNotNull("Appointment not found on server", appointment);
        assertNotNull("No chnage exceptions found on server", appointment.getChangeException());
        assertEquals("Unexpected number of change excpetions", 1, appointment.getChangeException().length);
        /*
         * verify appointment & exception on client
         */
        iCalResource = get(publicFolderID, uid, null);
        assertNotNull("No VEVENTs in iCal found", iCalResource.getVEvents());
        assertEquals("Not all VEVENTs in iCal found", 2, iCalResource.getVEvents().size());

    }

}
