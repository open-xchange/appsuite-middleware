
package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;

public class Bug19681_TimezoneForUtcProperties extends ManagedAppointmentTest {

    String ical = "BEGIN:VCALENDAR\n"
        +"VERSION:2.0\n"
        +"PRODID:-//Minter Software//EdgeDesk 4.03 MIMEDIR//EN\n"
        +"METHOD:REQUEST\n"
        +"BEGIN:VEVENT\n"
        +"STATUS:CONFIRMED\n"
        +"ORGANIZER:MAILTO:test1@fulledtest.com\n"
        +"DTSTART;TZID=America/New_York:20110701T120000\n"
        +"DTEND;TZID=America/New_York:20110701T123000\n"
        +"RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=FR;WKST=SU;COUNT=10\n"
        +"TRANSP:OPAQUE\n"
        +"UID:250541309547419@visualmail4.webmail1\n"
        +"DTSTAMP;TZID=America/New_York:20110701T192800\n"
        +"DTSTAMP:20110701T192800\n"
        +"SUMMARY:Test Calendar Export\n"
        +"DESCRIPTION:To Ox Suppot. With Recurrences\n\n"
        +"PRIORITY:5\n"
        +"END:VEVENT\n"
        +"END:VCALENDAR\n";

    public void noTestErroneousFile() throws Exception {
        // disabled test, since a workaround was added with fixes for bugs #28942 and #27706:
        // non-utc timestamps in utc-only datetime properties are now converted implicitly at
        // com.openexchange.data.conversion.ical.ical4j.ICal4JParser.workaroundFor27706And28942(String)
        ICalImportResponse response = getClient().execute(new ICalImportRequest(folder.getObjectID(), ical, false));
        assertTrue("Needs to fail because TZID is not allowed with DTSTAMP", response.hasError());
    }

    @Test
    public void testCorrectFile() throws Exception {
        String correct = ical.replace("DTSTAMP;TZID=America/New_York:20110701T192800", "DTSTAMP:20110701T192800");
        ICalImportResponse response = getClient().execute(new ICalImportRequest(folder.getObjectID(), correct));
        assertFalse("Should run smoothly with correct DTSTAMP", response.hasError());
        assertFalse("Should run smoothly with correct DTSTAMP", response.hasConflicts());
    }

}
