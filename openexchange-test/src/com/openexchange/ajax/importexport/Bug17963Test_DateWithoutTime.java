
package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.groupware.importexport.ImportResult;

public class Bug17963Test_DateWithoutTime extends ManagedAppointmentTest {

    @Test
    public void testDateWithoutTime() throws Exception {
        String ical = "BEGIN:VCALENDAR\n" + "VERSION:2.0\n" + "BEGIN:VEVENT\n" + "DTSTART;TZID=Europe/Rome:20100202T103000\n" + "DTEND;TZID=Europe/Rome:20100202T120000\n" + "RRULE:FREQ=WEEKLY;BYDAY=TU;UNTIL=20100705T215959Z\n" + "EXDATE:20111128\n" + "DTSTAMP:20110105T174810Z\n" + "SUMMARY:Team-Meeting\n" + "END:VEVENT\n";

        ICalImportRequest request = new ICalImportRequest(folder.getObjectID(), ical);
        ICalImportResponse response = getClient().execute(request);
        ImportResult[] imports = response.getImports();
        assertEquals(1, imports.length);
    }
}
