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
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link MWB1037Test}
 *
 * Wrong VTIMEZONE definition for Europe/Amsterdam
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v8.0.0
 */
public class MWB1037Test extends CalDAVTest {

    @Test
    public void testMissingStandardInEuropeAmsterdam() throws Exception {
        String incompleteVTimeZone = // @formatter:off
            "BEGIN:VTIMEZONE\r\n" +
            "TZID;X-RICAL-TZSOURCE=TZINFO:Europe/Amsterdam\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:20210328T020000\r\n" +
            "RDATE:20210328T020000\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "TZNAME:CEST\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n"
        ; // @formatter:on
        testIncompleteTimezone("Europe/Amsterdam", incompleteVTimeZone);
    }

    @Test
    public void testMissingDaylightInEuropeUzhgorod() throws Exception {
        String incompleteVTimeZone = // @formatter:off
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Uzhgorod\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:+0300\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "TZNAME:EET\r\n" +
            "DTSTART:19701025T040000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n"
        ; // @formatter:on
        testIncompleteTimezone("Europe/Uzhgorod", incompleteVTimeZone);
    }

    @Test
    public void testMissingDaylightInAmericaMetlakatla() throws Exception {
        String incompleteVTimeZone = // @formatter:off
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:America/Metlakatla\r\n" +
            "BEGIN:STANDARD\r\n" +
            "TZOFFSETFROM:-0800\r\n" +
            "TZOFFSETTO:-0900\r\n" +
            "TZNAME:AKST\r\n" +
            "DTSTART:19701101T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=11;BYDAY=1SU\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n"
        ; // @formatter:on
        testIncompleteTimezone("America/Metlakatla", incompleteVTimeZone);
    }

    @Test
    public void testMissingMissingStandardInAsiaFamagusta() throws Exception {
        String incompleteVTimeZone = // @formatter:off
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Asia/Famagusta\r\n" +
            "TZURL:http://tzurl.org/zoneinfo-outlook/Asia/Famagusta\r\n" +
            "X-LIC-LOCATION:Asia/Famagusta\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0300\r\n" +
            "TZNAME:EEST\r\n" +
            "DTSTART:19700329T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n"
        ; // @formatter:on
        testIncompleteTimezone("Asia/Famagusta", incompleteVTimeZone);
    }

    private void testIncompleteTimezone(String timeZoneId, String incompleteVTimeZone) throws Exception {
        /*
         * create event with bogus timezone definition on client
         */
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
        String uid = randomUID();
        Date start = TimeTools.D("next monday at 14:30", timeZone);
        Date end = TimeTools.D("next monday at 14:45", timeZone);
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.8.5//EN\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            incompleteVTimeZone +
            "BEGIN:VEVENT\r\n" +
            "DTSTART;TZID=" + timeZoneId + ":" + format(start, timeZoneId) + "\r\n" +
            "DTEND;TZID=" + timeZoneId + ":" + format(end, timeZoneId) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:MWB1037Test\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
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
        assertEquals("wrong timezone", timeZoneId, appointment.getTimezone());
        assertEquals("start wrong", start, appointment.getStartDate());
        assertEquals("end wrong", end, appointment.getEndDate());
        /*
         * verify event on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VTIMEZONE in iCal found", iCalResource.getVCalendar().getComponents("VTIMEZONE"));
        assertEquals("Unexpected number of VTIMEZONE components in iCal", 1, iCalResource.getVCalendar().getComponents("VTIMEZONE").size());
        Component timeZoneComponent = iCalResource.getVCalendar().getComponents("VTIMEZONE").get(0);
        assertNotNull("No STANDARD component in iCal timezone found", timeZoneComponent.getComponents("STANDARD"));
        assertEquals("Unexpected number of STANDARD components in iCal timezone found", 1, timeZoneComponent.getComponents("STANDARD").size());
        assertNotNull("No DAYLIGHT component in iCal timezone found", timeZoneComponent.getComponents("DAYLIGHT"));
        assertEquals("Unexpected number of DAYLIGHT components in iCal timezone found", 1, timeZoneComponent.getComponents("DAYLIGHT").size());

    }

    @Test
    public void testIncompleteTimezone() throws Exception {
        /*
         * create event with bogus timezone definition on client
         */
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Amsterdam");
        String uid = randomUID();
        Date start = TimeTools.D("next monday at 14:30", timeZone);
        Date end = TimeTools.D("next monday at 14:45", timeZone);
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.8.5//EN\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID;X-RICAL-TZSOURCE=TZINFO:Europe/Amsterdam\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:20210328T020000\r\n" +
            "RDATE:20210328T020000\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "TZNAME:CEST\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTART;TZID=Europe/Amsterdam:" + format(start, "Europe/Amsterdam") + "\r\n" +
            "DTEND;TZID=Europe/Amsterdam:" + format(end, "Europe/Amsterdam") + "\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "SUMMARY:MWB1037Test\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
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
        assertEquals("wrong timezone", "Europe/Amsterdam", appointment.getTimezone());
        assertEquals("start wrong", start, appointment.getStartDate());
        assertEquals("end wrong", end, appointment.getEndDate());
        /*
         * verify event on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VTIMEZONE in iCal found", iCalResource.getVCalendar().getComponents("VTIMEZONE"));
        assertEquals("Unexpected number of VTIMEZONE components in iCal", 1, iCalResource.getVCalendar().getComponents("VTIMEZONE").size());
        Component timeZoneComponent = iCalResource.getVCalendar().getComponents("VTIMEZONE").get(0);
        assertNotNull("No STANDARD component in iCal timezone found", timeZoneComponent.getComponents("STANDARD"));
        assertEquals("Unexpected number of STANDARD components in iCal timezone found", 1, timeZoneComponent.getComponents("STANDARD").size());
        assertNotNull("No DAYLIGHT component in iCal timezone found", timeZoneComponent.getComponents("DAYLIGHT"));
        assertEquals("Unexpected number of DAYLIGHT components in iCal timezone found", 1, timeZoneComponent.getComponents("DAYLIGHT").size());
    }

}
