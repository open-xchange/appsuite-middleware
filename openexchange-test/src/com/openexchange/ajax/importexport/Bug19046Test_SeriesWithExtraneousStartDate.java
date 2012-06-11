package com.openexchange.ajax.importexport;

import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.groupware.importexport.ImportResult;

public class Bug19046Test_SeriesWithExtraneousStartDate extends
		ManagedAppointmentTest {

	public Bug19046Test_SeriesWithExtraneousStartDate(String name) {
		super(name);
	}

	/*
	 * Behaviour: Only shows series, not exception
	 */
	public void testExtraneousStartDate() throws Exception{
		String ical =
		"BEGIN:VCALENDAR\n"+
		"PRODID:Open-Xchange\n"+
		"VERSION:2.0\n"+
		"CALSCALE:GREGORIAN\n"+
		"BEGIN:VEVENT\n"+
		"SUMMARY:Workshop Manager Guidelines\n"+
		"DTSTART:20110430T090000Z\n"+
		"DTEND:20110430T140000Z\n"+
		"CLASS:PUBLIC\n"+
		"TRANSP:OPAQUE\n"+
		"RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=WE\n"+
		"END:VEVENT\n"+
		"END:VCALENDAR\n";

		ICalImportRequest request = new ICalImportRequest(folder.getObjectID(), ical);
		ICalImportResponse response = getClient().execute(request);
		ImportResult[] imports = response.getImports();
		assertEquals(1, imports.length);
		assertFalse(response.hasConflicts() || response.hasError());
	}

}
