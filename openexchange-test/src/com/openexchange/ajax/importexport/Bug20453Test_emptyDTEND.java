
package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertFalse;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.exception.OXException;

public class Bug20453Test_emptyDTEND extends ManagedAppointmentTest {

    @Test
    public void testWhatever() throws IOException, JSONException, OXException {
        final ICalImportResponse response = getClient().execute(new ICalImportRequest(folder.getObjectID(), ical, false));
        assertFalse("Should not stumble over an empty DTEND; fragment", response.hasError());
    }

    private final String ical = "BEGIN:VCALENDAR\n" + "VERSION:2.0\n" + "PRODID:-//Minter Software//EdgeDesk 4.03 MIMEDIR//EN\n" + "METHOD:REQUEST\n" + "BEGIN:VEVENT\n" + "STATUS:CONFIRMED\n" + "ORGANIZER:MAILTO:sheila@yoursweetlife.com\n" + "DTSTART;TZID=America/New_York:20100719T154500\n" + "DTEND;\n" + "TRANSP:OPAQUE\n" + "UID:249071279311970@visualmail4.webmail14\n" + "DTSTAMP:20110914T205500Z\n" + "SUMMARY:Dr Bowler\n" + "PRIORITY:5\n" + "END:VEVENT\n" + "END:VCALENDAR\n" + "BEGIN:VCALENDAR\n";
}
