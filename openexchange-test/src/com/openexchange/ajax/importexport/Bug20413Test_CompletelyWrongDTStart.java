package com.openexchange.ajax.importexport;

import java.util.Calendar;
import org.json.JSONArray;
import org.json.JSONObject;

import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.groupware.container.Appointment;

public class Bug20413Test_CompletelyWrongDTStart extends ManagedAppointmentTest {

	public Bug20413Test_CompletelyWrongDTStart(String name) {
		super(name);
	}
	
	private final String ical = 
		"BEGIN:VCALENDAR\n" + 
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
		"DTSTAMP:20110919T123902Z\n" + 
		"SUMMARY:Gebur Tstag\n" + 
		"CLASS:PUBLIC\n" + 
		"LAST-MODIFIED:20110729T094040Z\n" + 
		"DTSTART;VALUE=DATE:-88646400\n" + 
		"CREATED:20110729T094040Z\n" + 
		"RRULE:FREQ=YEARLY;UNTIL=20310312;INTERVAL=1\n" + 
		"TRANSP:OPAQUE\n" + 
		"END:VEVENT\n" + 
		"END:VCALENDAR";
	
	public void testDTStartMonstrosity() throws Exception {
		ICalImportResponse response = getClient().execute(new ICalImportRequest(folder.getObjectID(), ical));
		JSONArray arr = (JSONArray)response.getData();
		
		
		assertEquals(1, arr.length());
		
		JSONObject jsonObject = arr.getJSONObject(0);
		Appointment actual = calendarManager.get(jsonObject.getInt("folder_id"), jsonObject.getInt("id"));
		Calendar startDate = Calendar.getInstance();
		startDate.setTime(actual.getStartDate());
		//NOTE: Completely irrelevant. Date format not allowed. 
		assertEquals(Calendar.NOVEMBER, startDate.get(Calendar.MONTH));
		assertEquals(9, startDate.get(Calendar.DAY_OF_MONTH));
		
	}

}
