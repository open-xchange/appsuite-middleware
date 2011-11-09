package com.openexchange.ajax.importexport;

import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;

public class Bug20132Test_WrongRecurrenceDatePosition extends ManagedAppointmentTest {

	public Bug20132Test_WrongRecurrenceDatePosition(String name) {
		super(name);
	}
	
	public void testForCorrectRecurrenceDatePositionCalculation() throws Exception{
		System.out.println(ical);
		ICalImportRequest request = new ICalImportRequest(folder.getObjectID(), ical);
		ICalImportResponse response = getClient().execute(request);
		assertFalse(response.hasError());
	}


	public static String ical = "BEGIN:VCALENDAR\n" + 
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
}
