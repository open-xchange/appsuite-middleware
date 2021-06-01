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
 * {@link MWB464Test}
 *
 * Calendar Invite displayed 1 hour early
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class MWB464Test extends ManagedAppointmentTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        TimeZone timeZone = getClient().getValues().getTimeZone();
        assertNotNull(timeZone);
        if (false == "America/Denver".equals(timeZone.getID())) {
            getClient().getValues().setTimeZone(TimeZone.getTimeZone("America/Denver"));
        }
    }

    @Test
    public void testMountainStandardTime() throws Exception {
        String uid = UUIDs.getUnformattedStringFromRandom();
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "METHOD:REQUEST\r\n" +
            "PRODID:Microsoft Exchange Server 2010\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:US Mountain Standard Time\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:16010101T000000\r\n" +
            "TZOFFSETFROM:-0700\r\n" +
            "TZOFFSETTO:-0700\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:16010101T000000\r\n" +
            "TZOFFSETFROM:-0700\r\n" +
            "TZOFFSETTO:-0700\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:MWB464Test\r\n" +
            "DTSTART;TZID=US Mountain Standard Time:20200709T120000\r\n" +
            "DTEND;TZID=US Mountain Standard Time:20200709T130000\r\n" +
            "CLASS:PUBLIC\r\n" +
            "DTSTAMP:20200708T000305Z\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
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
        Appointment appointment = getResponse.getAppointment(TimeZone.getTimeZone("America/Denver"));
        assertEquals("Timezone wrong", "America/Phoenix", appointment.getTimezone());
        DateTime expectedStart = DateTime.parse("America/Phoenix", "20200709T120000");
        assertEquals("Wrong start", expectedStart.getTimestamp(), appointment.getStartDate().getTime());
        DateTime expectedEnd = DateTime.parse("America/Phoenix", "20200709T130000");
        assertEquals("Wrong end", expectedEnd.getTimestamp(), appointment.getEndDate().getTime());
    }

}
