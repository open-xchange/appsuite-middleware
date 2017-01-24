
package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertFalse;
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.groupware.importexport.ImportResult;

public class Bug20715Test_UidIsNotcaseSensitive extends ManagedAppointmentTest {

    public static String uid = String.valueOf(Math.random());

    @Test
    public void testUidShouldNotIgnoreCase() throws Exception {
        String ical = "BEGIN:VCALENDAR\n" + "VERSION:2.0\n" + "CALSCALE:GREGORIAN\n" +

            "BEGIN:VEVENT\n" + "DTSTAMP:20110930T140717Z\n" + "SUMMARY:First appointment with UID\n" + "DTEND;TZID=Europe/Berlin:20111102\n" + "CREATED:20110930T135935Z\n" + "UID:" + uid + "a\n" + "DTSTART;TZID=Europe/Berlin:20111101\n" + "TRANSP:OPAQUE\n" + "END:VEVENT\n" +

            "BEGIN:VEVENT\n" + "DTSTAMP:20110930T140717Z\n" + "SUMMARY:Second appointment with slightly different UID\n" + "DTEND;TZID=Europe/Berlin:20111102\n" + "CREATED:20110930T135935Z\n" + "UID:" + uid + "A\n" + "DTSTART;TZID=Europe/Berlin:20111101\n" + "TRANSP:OPAQUE\n" + "END:VEVENT\n" +

            "END:VCALENDAR";

        ICalImportRequest importRequest = new ICalImportRequest(folder.getObjectID(), ical);
        ICalImportResponse importResponse = getClient().execute(importRequest);

        ImportResult[] imports = importResponse.getImports();
        assertFalse(importResponse.hasError());
    }

}
