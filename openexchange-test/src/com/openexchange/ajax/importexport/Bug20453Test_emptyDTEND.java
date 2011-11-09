package com.openexchange.ajax.importexport;

import java.io.IOException;

import org.json.JSONException;

import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.tools.servlet.AjaxException;

public class Bug20453Test_emptyDTEND extends ManagedAppointmentTest {


	public Bug20453Test_emptyDTEND(String name) {
		super(name);
	}
	
	public void testWhatever() throws AjaxException, IOException, JSONException{
		ICalImportResponse response = getClient().execute(new ICalImportRequest(folder.getObjectID(), ical , false));
		assertFalse("Should not stumble over an empty DTEND; fragment", response.hasError());
	}

	private String ical = 
			"BEGIN:VCALENDAR\n" + 
			"VERSION:2.0\n" + 
			"PRODID:-//Minter Software//EdgeDesk 4.03 MIMEDIR//EN\n" + 
			"METHOD:REQUEST\n" + 
			"BEGIN:VEVENT\n" + 
			"STATUS:CONFIRMED\n" + 
			"ORGANIZER:MAILTO:sheila@yoursweetlife.com\n" + 
			"DTSTART;TZID=America/New_York:20100719T154500\n" + 
			"DTEND;\n" + 
			"TRANSP:OPAQUE\n" + 
			"UID:249071279311970@visualmail4.webmail14\n" + 
			"DTSTAMP:20110914T205500Z\n" + 
			"SUMMARY:Dr Bowler\n" + 
			"PRIORITY:5\n" + 
			"END:VEVENT\n" + 
			"END:VCALENDAR\n" + 
			"BEGIN:VCALENDAR\n";
}
