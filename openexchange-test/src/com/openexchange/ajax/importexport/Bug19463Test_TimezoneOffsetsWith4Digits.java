
package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertFalse;
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;

public class Bug19463Test_TimezoneOffsetsWith4Digits extends ManagedAppointmentTest {

    String ical = "BEGIN:VCALENDAR\n" + "METHOD:REQUEST\n" + "PRODID:Microsoft Windows Phone\n" + "VERSION:2.0\n" + "BEGIN:VTIMEZONE\n" + "TZID:W. Europe Standard Time\n" + "BEGIN:STANDARD\n" + "DTSTART:20000101T030000\n" + "TZOFFSETFROM:0200\n" + "TZOFFSETTO:0100\n" + "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=5SU;BYMONTH=10\n" + "END:STANDARD\n" + "BEGIN:DAYLIGHT\n" + "DTSTART:20000101T020000\n" + "TZOFFSETFROM:0100\n" + "TZOFFSETTO:0200\n" + "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=5SU;BYMONTH=3\n" + "END:DAYLIGHT\n" + "END:VTIMEZONE\n" + "BEGIN:VEVENT\n" + "ORGANIZER:MAILTO:oxpro-a01@qs-c4.de\n" + "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION:MAILTO:oxpro-a01@qs-c4.\n" + " de\n" + "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION:MAILTO:oxpro-a02@qs-c4.\n" + " de\n" + "STATUS:CONFIRMED\n" + "X-MICROSOFT-CDO-ALLDAYEVENT:FALSE\n" + "BEGIN:VALARM\n" + "ACTION:DISPLAY\n" + "TRIGGER:-PT15M\n" + "END:VALARM\n" + "SUMMARY:Neuer Serien-Termin\n" + "LOCATION:\n" + "DESCRIPTION;CHARSET=UTF-8:t\u00e4glich\n" + " jeden 2. tag\n" + " ab 10.5.\n" + " bis 31.05.\n" + "DTSTART;TZID=W. Europe Standard Time:20110510T170000\n" + "DTEND;TZID=W. Europe Standard Time:20110510T180000\n" + "RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=TU\n" + "DTSTAMP:20110510T113750Z\n" + "LAST-MODIFIED:20110510T113750Z\n" + "CLASS:PUBLIC\n" + "END:VEVENT\n" + "END:VCALENDAR\n";

    @Test
    public void testTimezoneOffsetWith4DigitsIsPassed() throws Exception {
        ICalImportResponse response = getClient().execute(new ICalImportRequest(folder.getObjectID(), ical, false));
        assertFalse("Should not fail because of 4-digit timezone code", response.hasError());

    }

}
