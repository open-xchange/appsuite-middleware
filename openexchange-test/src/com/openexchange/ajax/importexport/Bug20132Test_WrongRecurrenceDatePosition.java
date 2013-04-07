package com.openexchange.ajax.importexport;

import java.util.List;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.groupware.importexport.ImportResult;

public class Bug20132Test_WrongRecurrenceDatePosition extends ManagedAppointmentTest {

	public Bug20132Test_WrongRecurrenceDatePosition(String name) {
		super(name);
	}

	public void testForCorrectRecurrenceDatePositionCalculation() throws Exception{
		String ical = "BEGIN:VCALENDAR\n" +
		"PRODID:-//Google Inc//Google Calendar 70.9054//EN\n" +
		"VERSION:2.0\n" +
		"CALSCALE:GREGORIAN\n" +
		"METHOD:PUBLISH\n" +

		"BEGIN:VEVENT\n" +
		"DTSTART;TZID=Africa/Ceuta:20110616T180000\n" +
		"DTEND;TZID=Africa/Ceuta:20110616T190000\n" +
		"RRULE:FREQ=WEEKLY;BYDAY=TH\n" +
		"EXDATE;TZID=Africa/Ceuta:20110721T180000\n" +
		"EXDATE;TZID=Africa/Ceuta:20110714T180000\n" +
		"DTSTAMP:20110717T154458Z\n" +
		"UID:D3D1882A85A5496795DCE32F04EF5F8E00000000000000000000000000000000\n" +
		"CREATED:20110531T052752Z\n" +
		"DESCRIPTION:\n" +
		"LAST-MODIFIED:20110713T155545Z\n" +
		"SEQUENCE:1\n" +
		"STATUS:CONFIRMED\n" +
		"SUMMARY:Series\n"+
		"END:VEVENT\n" +

		"BEGIN:VEVENT\n" +
		"DTSTART;TZID=Europe/Berlin:20110727T170000\n" +
		"DTEND;TZID=Europe/Berlin:20110727T180000\n" +
		"DTSTAMP:20110717T154458Z\n" +
		"UID:D3D1882A85A5496795DCE32F04EF5F8E00000000000000000000000000000000\n" +
		"RECURRENCE-ID;TZID=Europe/Berlin:20110728T180000\n" +
		"CREATED:20110531T052752Z\n" +
		"DESCRIPTION:\n" +
		"LAST-MODIFIED:20110713T155545Z\n" +
		"SEQUENCE:2\n" +
		"STATUS:CONFIRMED\n" +
		"SUMMARY:Exception to series\n"+
		"END:VEVENT\n" +


		"END:VCALENDAR\n";

		ICalImportRequest request = new ICalImportRequest(folder.getObjectID(), ical);
		ICalImportResponse response = getClient().execute(request);
		assertFalse(response.hasError());
	}


	public void testFieldTooLongException() throws Exception, Exception, Exception{
		String ical = "BEGIN:VCALENDAR\n" +
		"PRODID:-//Google Inc//Google Calendar 70.9054//EN\n" +
		"VERSION:2.0\n" +
		"CALSCALE:GREGORIAN\n" +
		"METHOD:PUBLISH\n" +

		"BEGIN:VEVENT\n" +
		"DTSTART;TZID=Africa/Ceuta:20110616T180000\n" +
		"DTEND;TZID=Africa/Ceuta:20110616T190000\n" +
		"DTSTAMP:20110717T154458Z\n" +
		"DESCRIPTION:\n" +
		"STATUS:CONFIRMED\n" +
		"SUMMARY:Here comes a long (actually too long) story: Lorem " +
		"ipsum dolor sit amet, consectetur adipisicing elit, " +
		"sed do eiusmod tempor incididunt ut labore et dolore " +
		"magna aliqua. Ut enim ad minim veniam, quis nostrud " +
		"exercitation ullamco laboris nisi ut aliquip ex ea " +
		"commodo consequat. Duis aute irure dolor in reprehenderit " +
		"in voluptate velit esse cillum dolore eu fugiat nulla " +
		"pariatur. Excepteur sint occaecat cupidatat non proident, " +
		"sunt in culpa qui officia deserunt mollit anim id est " +
		"laborum\n"+
		"END:VEVENT\n" +

		"END:VCALENDAR\n";

		ICalImportRequest request = new ICalImportRequest(folder.getObjectID(), ical, false);
		ICalImportResponse response = getClient().execute(request);
		assertTrue(response.hasError());
		ImportResult[] imports = response.getImports();
		assertEquals(1, imports.length);
		List<ConversionWarning> warnings = imports[0].getWarnings();
		assertEquals(1, warnings.size());
		String message = warnings.get(0).getMessage();
        assertTrue(message.contains("truncated"));
		assertTrue(message.contains("Here comes a long (actually too long) story"));
		//System.out.println(message);
	}
}
