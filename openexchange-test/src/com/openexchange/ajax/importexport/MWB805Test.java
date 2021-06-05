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

package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.java.util.UUIDs;

/**
 * {@link MWB805Test}
 *
 * WebEX invitations are displaying the incorrect timezone
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class MWB805Test extends ManagedAppointmentTest {

    private TimeZone userTimezone;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        userTimezone = getClient().getValues().getTimeZone();
        assertNotNull(userTimezone);
        if (false == "America/Denver".equals(userTimezone.getID())) {
            userTimezone = TimeZone.getTimeZone("America/Denver");
            getClient().getValues().setTimeZone(userTimezone);
        }
    }

    @Test
    public void testWebExTimeZoneEurope() throws Exception {
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//Microsoft Corporation//Outlook 12.0 MIMEDIR//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:20001029T030000\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=-1SU;BYMONTH=10;BYHOUR=3\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "TZNAME:Standard Time\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:20000326T020000\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=-1SU;BYMONTH=3;BYHOUR=2\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "TZNAME:Daylight Savings Time\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "UID:" + UUIDs.getUnformattedStringFromRandom() + "\r\n" +
            "DTSTAMP:20201216T140925Z\r\n" +
            "SUMMARY:MWB805Test\r\n" +
            "DTSTART;TZID=\"Europe\":20201216T183000\r\n" +
            "DTEND;TZID=\"Europe\":20201216T193000\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        importAndCheck(iCal, DateTime.parse("Europe/Berlin", "20201216T183000"), DateTime.parse("Europe/Berlin", "20201216T193000"));
    }

    @Test
    public void testWebExTimeZoneEastern() throws Exception {
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//Microsoft Corporation//Outlook 12.0 MIMEDIR//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Eastern\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:20001105T020000\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=1SU;BYMONTH=11\r\n" +
            "TZOFFSETFROM:-0400\r\n" +
            "TZOFFSETTO:-0500\r\n" +
            "TZNAME:Standard Time\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:20000312T020000\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=2SU;BYMONTH=3\r\n" +
            "TZOFFSETFROM:-0500\r\n" +
            "TZOFFSETTO:-0400\r\n" +
            "TZNAME:Daylight Savings Time\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "UID:" + UUIDs.getUnformattedStringFromRandom() + "\r\n" +
            "DTSTAMP:20171013T193710Z\r\n" +
            "SUMMARY:MWB805Test\r\n" +
            "DTSTART;TZID=\"Eastern\":20171014T160000\r\n" +
            "DTEND;TZID=\"Eastern\":20171014T170000\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        importAndCheck(iCal, DateTime.parse("America/New_York", "20171014T160000"), DateTime.parse("America/New_York", "20171014T170000"));
    }

    @Test
    public void testWebExTimeZonePacific() throws Exception {
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//Microsoft Corporation//Outlook 12.0 MIMEDIR//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Pacific\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:20001105T020000\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=1SU;BYMONTH=11;BYHOUR=2\r\n" +
            "TZOFFSETFROM:-0700\r\n" +
            "TZOFFSETTO:-0800\r\n" +
            "TZNAME:Standard Time\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:20000312T020000\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=2SU;BYMONTH=3;BYHOUR=2\r\n" +
            "TZOFFSETFROM:-0800\r\n" +
            "TZOFFSETTO:-0700\r\n" +
            "TZNAME:Daylight Savings Time\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "UID:" + UUIDs.getUnformattedStringFromRandom() + "\r\n" +
            "DTSTAMP:20131029T182028Z\r\n" +
            "SUMMARY:MWB805Test\r\n" +
            "DTSTART;TZID=\"Pacific\":20131030T110000\r\n" +
            "DTEND;TZID=\"Pacific\":20131030T120000\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        importAndCheck(iCal, DateTime.parse("America/Los_Angeles", "20131030T110000"), DateTime.parse("America/Los_Angeles", "20131030T120000"));
    }

    private void importAndCheck(String iCal, DateTime expectedStart, DateTime expectedEnd) throws Exception {
        /*
         * import iCal & check results
         */
        ICalImportResponse importResponse = getClient().execute(new ICalImportRequest(folder.getObjectID(), iCal, false));
        assertNotNull("Should have processed 1 event", importResponse.getImports());
        assertEquals("Should have processed 1 event", 1, importResponse.getImports().length);
        int objectID = Integer.parseInt(importResponse.getImports()[0].getObjectId());
        /*
         * get & check appointment in client timezone
         */
        GetResponse getResponse = getClient().execute(new GetRequest(folder.getObjectID(), objectID, true));
        Appointment appointment = getResponse.getAppointment(userTimezone);
        assertEquals("Wrong start", expectedStart.getTimestamp(), appointment.getStartDate().getTime());
        assertEquals("Wrong end", expectedEnd.getTimestamp(), appointment.getEndDate().getTime());
    }

}
