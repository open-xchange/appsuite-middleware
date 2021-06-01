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
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;

/**
 * {@link MWB161Test}
 * 
 * import of an ics file results in Error while reading/writing from/to the database
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.4
 */
public class MWB161Test extends ManagedAppointmentTest {

    @Test
    public void testInvalidAlarm() throws Exception {
        String uid = UUIDs.getUnformattedStringFromRandom();
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:icalendar-ruby\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "METHOD:PUBLISH\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Paris\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:20200329T030000\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\r\n" +
            "TZNAME:CEST\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:20191027T020000\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\r\n" +
            "TZNAME:CET\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTAMP:20200302T163050Z\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTART;TZID=Europe/Paris:20200504T083000\r\n" +
            "DTEND;TZID=Europe/Paris:20200504T091500\r\n" +
            "CLASS:PRIVATE\r\n" +
            "DESCRIPTION:Zugangsinformationen:\\n* In Stock 2.OG mit Fahrstuhl\\n* Mit Fah\r\n" +
            " rstuhl\\n* Nicht Barrierefrei\\n\\nUm Ihren Termin zu stornieren oder zu vers\r\n" +
            " chieben\\, klicken Sie auf den folgenden Link :\\nhttp://www.doctolib.de/app\r\n" +
            " ointments/anonymous/...\\n\r\n" +
            "GEO:52.4880182;13.3405172\r\n" +
            "LOCATION:Innsbrucker Strasse 58\\, 10825 Berlin\r\n" +
            "PRIORITY:2\r\n" +
            "SUMMARY:Termin bei Gastroenterologie am Bayerischen Platz\r\n" +
            "BEGIN:VALARM\r\n" +
            "TRIGGER:-PT2H\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        /*
         * import iCal & check results
         */
        ICalImportResponse importResponse = getClient().execute(new ICalImportRequest(folder.getObjectID(), iCal, false));
        assertNotNull("Should have processed 1 event", importResponse.getImports());
        assertEquals("Should have processed 1 event", 1, importResponse.getImports().length);
        OXException error = importResponse.getImports()[0].getException();
        assertNotNull("No error for imported event", error);
        assertEquals("Unexpected error code for imported event", "CAL-4005", error.getErrorCode());
    }

}
