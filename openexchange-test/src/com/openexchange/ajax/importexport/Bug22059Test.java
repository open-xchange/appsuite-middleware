package com.openexchange.ajax.importexport;

import java.util.Date;
import java.util.TimeZone;

import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

public class Bug22059Test extends ManagedAppointmentTest {

	public Bug22059Test(String name) {
		super(name);
	}
	
	public static String ical = "BEGIN:VCALENDAR\n" + 
			"PRODID:Strato Communicator 3.5\n" + 
			"VERSION:2.0\n" + 
			"CALSCALE:GREGORIAN\n" + 
			"BEGIN:VTIMEZONE\n" + 
			"TZID:Europe/Berlin\n" + 
			"X-LIC-LOCATION:Europe/Berlin\n" + 
			"BEGIN:DAYLIGHT\n" + 
			"TZOFFSETFROM:+0100\n" + 
			"TZOFFSETTO:+0200\n" + 
			"TZNAME:CEST\n" + 
			"DTSTART:19700329T020000\n" + 
			"RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\n" + 
			"END:DAYLIGHT\n" + 
			"BEGIN:STANDARD\n" + 
			"TZOFFSETFROM:+0200\n" + 
			"TZOFFSETTO:+0100\n" + 
			"TZNAME:CET\n" + 
			"DTSTART:19701025T030000\n" + 
			"RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\n" + 
			"END:STANDARD\n" + 
			"END:VTIMEZONE\n" + 
			"BEGIN:VEVENT\n" + 
			"DTSTAMP:20120425T130117Z\n" + 
			"SUMMARY:Gymnastik\n" + 
			"LOCATION:Rehacenter\n" + 
			"CLASS:PUBLIC\n" + 
			"LAST-MODIFIED:20080807T182746Z\n" + 
			"DTEND;TZID=Europe/Berlin:20080813T091500\n" + 
			"CREATED:20080807T182746Z\n" + 
			"DTSTART;TZID=Europe/Berlin:20080813T084500\n" + 
			"BEGIN:VALARM\n" + 
			"ACTION:DISPLAY\n" + 
			"TRIGGER:-PT30M\n" + 
			"DESCRIPTION:Gymnastik\n" + 
			"END:VALARM\n" + 
			"TRANSP:OPAQUE\n" + 
			"UID:7417677-1335358877@com35.strato.de\n" + 
			"END:VEVENT\n" + 
			"END:VCALENDAR\n" + 
			"";
	
	public void testIt() throws Exception {
		ICalImportResponse icalResponse = getClient().execute(new ICalImportRequest(folder.getObjectID(), ical , false));
		assertFalse("Should work", icalResponse.hasError());
		assertTrue("Should import one", icalResponse.getImports().length == 1);
		int oid = Integer.parseInt(icalResponse.getImports()[0].getObjectId());
		GetResponse getResponse = getClient().execute(new GetRequest( folder.getObjectID(), oid));
		Appointment actualAppointment = getResponse.getAppointment(TimeZone.getTimeZone("Europe/Berlin"));
		assertEquals("Gymnastik", actualAppointment.getTitle());

	}
}
