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
import java.util.Date;
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.importexport.ImportResult;

/**
 * @author tobiasp
 */
public class ICalSeriesTests extends ManagedAppointmentTest {

    @Test
    public void testDeleteException() throws Exception {
        String ical = // @formatter:off
            "BEGIN:VCALENDAR\n" +
            "VERSION:2.0\n" +
            "BEGIN:VEVENT\n" +
            "DTSTART;TZID=Europe/Rome:20100202T103000\n" +
            "DTEND;TZID=Europe/Rome:20100202T120000\n" +
            "RRULE:FREQ=DAILY;UNTIL=20100204T215959Z\n" +
            "EXDATE;TZID=Europe/Rome:20100203T103000\n" +
            "DTSTAMP:20110105T174810Z\n" +
            "SUMMARY:Exceptional Meeting #1\n" +
            "END:VEVENT\n"
        ; // @formatter:on
        testChangeException(ical, "Exceptional Meeting #1", 1);
    }

    @Test
    public void testChangeExceptionWithMasterFirst() throws Exception {
        String uid = "change-exception-" + new Date().getTime();

        String title = "Change to exceptional meeting #2: Five hours later";
        String ical =
            "BEGIN:VCALENDAR\n" +
            "VERSION:2.0\n" +
            "BEGIN:VEVENT\n" +
            "DTSTART;TZID=Europe/Rome:20100202T110000\n" +
            "DTEND;TZID=Europe/Rome:20100202T120000\n" +
            "RRULE:FREQ=DAILY;UNTIL=20100228T215959Z\n" +
            "DTSTAMP:20110105T174810Z\n" +
            "SUMMARY:Exceptional meeting #2\n" +
            "UID:" + uid + "\n" +
            "END:VEVENT\n" +
            "BEGIN:VEVENT\n" +
            "DTSTART;TZID=Europe/Rome:20100204T160000\n" +
            "DTEND;TZID=Europe/Rome:20100204T170000\n" +
            "DTSTAMP:20110105T174810Z\n" +
            "SUMMARY:" + title + "\n" +
            "RECURRENCE-ID:20100204T100000Z\n" +
            "UID:" + uid + "\n" +
            "END:VEVENT\n"
        ;
        testChangeException(ical, title, 2);
    }

    protected void testChangeException(String ical, String expectedTitle, int expectedLength) throws Exception {
        AJAXClient client = getClient();
        int fid = folder.getObjectID();

        ICalImportRequest request = new ICalImportRequest(fid, ical);
        ICalImportResponse response = client.execute(request);

        ImportResult[] imports = response.getImports();
        assertNotNull(imports);
        assertEquals(expectedLength, imports.length);

        Appointment matchingAppointment = null;
        for (ImportResult result : imports) {
            Appointment appointment = catm.get(fid, Integer.parseInt(result.getObjectId()));
            assertNotNull(appointment);
            if (expectedTitle.equals(appointment.getTitle())) {
                matchingAppointment = appointment;
                break;
            }
        }
        assertNotNull("No appointment with title " + expectedTitle + " found", matchingAppointment);
    }

}
