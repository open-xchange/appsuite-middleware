package com.openexchange.ajax.importexport;

import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.importexport.ImportResult;

public class Bug20945Test_UnexpectedError26 extends ManagedAppointmentTest {

	public Bug20945Test_UnexpectedError26(String name) {
		super(name);
	}
	
	public String ical = "BEGIN:VCALENDAR\n" + 
			"PRODID:Strato Communicator 3.5\n" + 
			"VERSION:2.0\n" + 
			"CALSCALE:GREGORIAN\n" + 
			"BEGIN:VEVENT\n" + 
			"DTSTAMP:20111208T080947Z\n" + 
			"DTEND;VALUE=DATE:19700101\n" + 
			"SUMMARY:Geburtstag Damian Anell\n" + 
			"CLASS:PRIVATE\n" + 
			"LAST-MODIFIED:20101104T081557Z\n" + 
			"DTSTART;VALUE=DATE:19691231\n" + 
			"CREATED:20101104T081557Z\n" + 
			"DESCRIPTION:1969\n" + 
			"RRULE:FREQ=YEARLY;UNTIL=20370101;INTERVAL=1\n" + 
			"BEGIN:VALARM\n" + 
			"ACTION:DISPLAY\n" + 
			"TRIGGER:-PT15H\n" + 
			"DESCRIPTION:Geburtstag Damian Anell\n" + 
			"END:VALARM\n" + 
			"TRANSP:OPAQUE\n" + 
			"UID:133473615-1323331787@com35.strato.de\n" + 
			"END:VEVENT\n" + 

			"END:VCALENDAR";
	
	public void testIt() throws Exception {
		ICalImportResponse response = getClient().execute(new ICalImportRequest(folder.getObjectID(), ical));
		
		assertFalse(response.hasConflicts() || response.hasError());
	}

}
