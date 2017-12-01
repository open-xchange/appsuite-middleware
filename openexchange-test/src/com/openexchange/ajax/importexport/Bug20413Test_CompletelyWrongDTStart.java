
package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import org.json.JSONArray;
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.importexport.actions.AbstractImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportParser;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.java.Charsets;

public class Bug20413Test_CompletelyWrongDTStart extends ManagedAppointmentTest {

    private final String ical = "BEGIN:VCALENDAR\n" + "PRODID:Strato Communicator 3.5\n" + "VERSION:2.0\n" + "CALSCALE:GREGORIAN\n" + "BEGIN:VTIMEZONE\n" + "TZID:Europe/Berlin\n" + "X-LIC-LOCATION:Europe/Berlin\n" + "BEGIN:DAYLIGHT\n" + "TZOFFSETFROM:+0100\n" + "TZOFFSETTO:+0200\n" + "TZNAME:CEST\n" + "DTSTART:19700329T020000\n" + "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\n" + "END:DAYLIGHT\n" + "BEGIN:STANDARD\n" + "TZOFFSETFROM:+0200\n" + "TZOFFSETTO:+0100\n" + "TZNAME:CET\n" + "DTSTART:19701025T030000\n" + "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\n" + "END:STANDARD\n" + "END:VTIMEZONE\n" + "BEGIN:VEVENT\n" + "DTSTAMP:20110919T123902Z\n" + "SUMMARY:Gebur Tstag\n" + "CLASS:PUBLIC\n" + "LAST-MODIFIED:20110729T094040Z\n" + "DTSTART;VALUE=DATE:-88646400\n" + "CREATED:20110729T094040Z\n" + "RRULE:FREQ=YEARLY;UNTIL=20310312;INTERVAL=1\n" + "TRANSP:OPAQUE\n" + "END:VEVENT\n" + "END:VCALENDAR";

    @Test
    public void testDTStartMonstrosity() throws Exception {
        ICalImportResponse response = getClient().execute(new TruncatedImportRequest(ical, folder.getObjectID()));
        JSONArray arr = (JSONArray) response.getData();

        response.getImports()[0].getException().getLogMessage();
        assertEquals(1, arr.length());
        assertNotNull(response.getException());
        assertTrue(response.getException().getLogMessage().contains("Data truncation [field Start date, limit 19, current 0]"));

    }

    private static final class TruncatedImportRequest extends AbstractImportRequest<ICalImportResponse> {

        public TruncatedImportRequest(String iCal, int folderID) {
            super(Action.ICal, folderID, new ByteArrayInputStream(Charsets.getBytes(iCal, Charsets.UTF_8)));
        }

        @Override
        public AbstractAJAXParser<? extends ICalImportResponse> getParser() {
            return new ICalImportParser(false);
        }

    }

}
