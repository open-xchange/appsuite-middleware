package com.openexchange.ajax.importexport;

import org.json.JSONArray;
import org.json.JSONObject;

import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.groupware.container.Appointment;

public class Bug20896Test_AlarmsChange extends ManagedAppointmentTest {

	public Bug20896Test_AlarmsChange(String name) {
		super(name);
	}
	
	public String ical = 
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
		"DTSTAMP:20111130T124433Z\n" + 
		"SUMMARY:2 Tage vorher\n" + 
		"CLASS:PRIVATE\n" + 
		"LAST-MODIFIED:20111130T124235Z\n" + 
		"DTEND;TZID=Europe/Berlin:20300328T180000\n" + 
		"CREATED:20111130T124235Z\n" + 
		"DTSTART;TZID=Europe/Berlin:20300328T170000\n" + 
		"BEGIN:VALARM\n" + 
		"ACTION:DISPLAY\n" + 
		"TRIGGER:-P2D\n" + 
		"DESCRIPTION:2 Tage vorher\n" + 
		"END:VALARM\n" + 
		"TRANSP:OPAQUE\n" + 
		"END:VEVENT\n" + 
		
		"BEGIN:VEVENT\n" + 
		"DTSTAMP:20111130T124433Z\n" + 
		"SUMMARY:4 Wochen vorher\n" + 
		"CLASS:PRIVATE\n" + 
		"LAST-MODIFIED:20111130T124311Z\n" + 
		"DTEND;TZID=Europe/Berlin:20300328T230000\n" + 
		"CREATED:20111130T124311Z\n" + 
		"DTSTART;TZID=Europe/Berlin:20300328T220000\n" + 
		"BEGIN:VALARM\n" + 
		"ACTION:DISPLAY\n" + 
		"TRIGGER:-P4W\n" + 
		"DESCRIPTION:4 Wochen vorher\n" + 
		"END:VALARM\n" + 
		"TRANSP:OPAQUE\n" + 
		"END:VEVENT\n" + 
		
		"BEGIN:VEVENT\n" + 
		"DTSTAMP:20111130T124433Z\n" + 
		"SUMMARY:15 Minuten vorher\n" + 
		"CLASS:PRIVATE\n" + 
		"LAST-MODIFIED:20111130T124258Z\n" + 
		"DTEND;TZID=Europe/Berlin:20300328T160000\n" + 
		"CREATED:20111130T124258Z\n" + 
		"DTSTART;TZID=Europe/Berlin:20300328T150000\n" + 
		"BEGIN:VALARM\n" + 
		"ACTION:DISPLAY\n" + 
		"TRIGGER:-PT15M\n" + 
		"DESCRIPTION:15 Minuten vorher\n" + 
		"END:VALARM\n" + 
		"TRANSP:OPAQUE\n" + 
		"END:VEVENT\n" + 
		
		"BEGIN:VEVENT\n" + 
		"DTSTAMP:20111130T124433Z\n" + 
		"SUMMARY:2 Stunden vorher\n" + 
		"CLASS:PRIVATE\n" + 
		"LAST-MODIFIED:20111130T124245Z\n" + 
		"DTEND;TZID=Europe/Berlin:20300328T170000\n" + 
		"CREATED:20111130T124245Z\n" + 
		"DTSTART;TZID=Europe/Berlin:20300328T160000\n" + 
		"BEGIN:VALARM\n" + 
		"ACTION:DISPLAY\n" + 
		"TRIGGER:-PT2H\n" + 
		"DESCRIPTION:2 Stunden vorher\n" + 
		"END:VALARM\n" + 
		"TRANSP:OPAQUE\n" + 
		"END:VEVENT\n" + 
		
		"BEGIN:VEVENT\n" + 
		"DTSTAMP:20111130T124433Z\n" + 
		"SUMMARY:3 Monate vorher erinnern (Erwartet 4 Wochen im OX)\n" + 
		"CLASS:PRIVATE\n" + 
		"LAST-MODIFIED:20111130T124151Z\n" + 
		"DTEND;TZID=Europe/Berlin:20300328T200000\n" + 
		"CREATED:20111130T124110Z\n" + 
		"DTSTART;TZID=Europe/Berlin:20300328T190000\n" + 
		"BEGIN:VALARM\n" + 
		"ACTION:DISPLAY\n" + 
		"TRIGGER:-P4W\n" + 
		"DESCRIPTION:3 Monate vorher erinnern (Erwartet 4 Wochen im OX)\n" + 
		"END:VALARM\n" + 
		"TRANSP:OPAQUE\n" + 
		"END:VEVENT\n" + 
		
		"BEGIN:VEVENT\n" + 
		"DTSTAMP:20111130T124433Z\n" + 
		"SUMMARY:2 Wochen vorher\n" + 
		"CLASS:PRIVATE\n" + 
		"LAST-MODIFIED:20111130T124220Z\n" + 
		"DTEND;TZID=Europe/Berlin:20300328T190000\n" + 
		"CREATED:20111130T124220Z\n" + 
		"DTSTART;TZID=Europe/Berlin:20300328T180000\n" + 
		"BEGIN:VALARM\n" + 
		"ACTION:DISPLAY\n" + 
		"TRIGGER:-P2W\n" + 
		"DESCRIPTION:2 Wochen vorher\n" + 
		"END:VALARM\n" + 
		"TRANSP:OPAQUE\n" + 
		"END:VEVENT\n" + 
		"END:VCALENDAR";
	
	public void testWhatever() throws Exception {
		int fid = folder.getObjectID();
		ICalImportResponse importResponse = getClient().execute(new ICalImportRequest(fid, ical));
		JSONArray data = (JSONArray)importResponse.getData();
		int[] expectations = new int[]{
			2 * 24* 60, 
			4 * 7 * 24 * 60,
			15,
			2 * 60,
			4 * 7 * 24 * 60,
			2 * 7 * 24 * 60,
		};
		for(int i = 0, max = data.length(); i < max; i++){
			JSONObject json = data.getJSONObject(i);
			if(!json.has("id")){
				continue;
			}
			int id = json.getInt("id");
			Appointment appointment = calendarManager.get(fid, id);
			assertEquals("Problem with '"+appointment.getTitle()+"'", expectations[i], appointment.getAlarm());
		}
	}

}
