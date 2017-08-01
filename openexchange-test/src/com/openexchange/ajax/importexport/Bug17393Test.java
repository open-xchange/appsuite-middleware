
package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertFalse;
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;

/** @author Tobias Prinz */
public class Bug17393Test extends ManagedAppointmentTest {

    public static String CULPRIT = "BEGIN:VCALENDAR\n" + "PRODID:Open-Xchange\n" + "VERSION:2.0\n" + "CALSCALE:GREGORIAN\n" + "METHOD:REQUEST\n" + "\n" + "\n" + "BEGIN:VEVENT\n" + "DTSTAMP:20101101T150332Z\n" + "SUMMARY:Vorlesung Digital Audio Coding\n" + "DESCRIPTION:CSP_MA 3.FS\n" + "DTSTART:20101011T130000Z\n" + "DTEND:20101011T143000Z\n" + "CLASS:PUBLIC\n" + "LOCATION:Sr K 2026\n" + "TRANSP:OPAQUE\n" + "RRULE:FREQ=WEEKLY;INTERVAL=1;UNTIL=20110204;BYDAY=MO\n" + "EXDATE:20101220T140000Z,20101220T140000Z\n" + "CREATED:20101101T135448Z\n" + "LAST-MODIFIED:20101029T081757Z\n" + "ORGANIZER:mailto:wolf@idmt.fraunhofer.de\n" + "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL:mailto:wolf@idmt.fraunhofer.de\n" + "END:VEVENT\n" + "\n" + "BEGIN:VEVENT\n" + "DTSTAMP:20101101T150332Z\n" + "SUMMARY:Testterminserie 1\n" + "DTSTART:20101011T090000Z\n" + "DTEND:20101011T100000Z\n" + "CLASS:PUBLIC\n" + "TRANSP:OPAQUE\n" + "RRULE:FREQ=WEEKLY;INTERVAL=1;UNTIL=20110204;BYDAY=MO\n" + "EXDATE:20101108T090000Z\n" + "CREATED:20101101T135448Z\n" + "LAST-MODIFIED:20101101T150325Z\n" + "ORGANIZER:mailto:wolf@idmt.fraunhofer.de\n" + "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL:mailto:wolf@idmt.fraunhofer.de\n" + "BEGIN:VALARM\n" + "TRIGGER:-PT15M\n" + "ACTION:DISPLAY\n" + "DESCRIPTION:Open-XChange\n" + "END:VALARM\n" + "END:VEVENT\n" + "\n" + "BEGIN:VEVENT\n" + "DTSTAMP:20101101T150332Z\n" + "SUMMARY:Testterminserie 12\n" + "DTSTART:20101101T090000Z\n" + "DTEND:20101101T100000Z\n" + "CLASS:PUBLIC\n" + "TRANSP:OPAQUE\n" + "RECURRENCE-ID:20101101T090000Z\n" + "CREATED:20101101T150325Z\n" + "LAST-MODIFIED:20101101T150325Z\n" + "ORGANIZER:mailto:wolf@idmt.fraunhofer.de\n" + "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL:mailto:wolf@idmt.fraunhofer.de\n" + "END:VEVENT\n" + "END:VCALENDAR\n";

    @Test
    public void testChangeException() throws Exception {
        ICalImportRequest request = new ICalImportRequest(folder.getObjectID(), CULPRIT);
        ICalImportResponse response = getClient().execute(request);
        assertFalse(response.hasConflicts());
        assertFalse(response.hasError());
    }
}
