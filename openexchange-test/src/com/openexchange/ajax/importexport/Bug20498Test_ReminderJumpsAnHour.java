package com.openexchange.ajax.importexport;

import java.util.TimeZone;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.importexport.ImportResult;

public class Bug20498Test_ReminderJumpsAnHour extends ManagedAppointmentTest{

	public Bug20498Test_ReminderJumpsAnHour(String name) {
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
		"DTSTAMP:20110930T140717Z\n" +
		"SUMMARY:Geburtstag: Vorname Nachname (01.11.1971)\n" +
		"CLASS:PUBLIC\n" +
		"LAST-MODIFIED:20110930T135935Z\n" +
		"DTEND;TZID=Europe/Berlin:20111102\n" +
		"CREATED:20110930T135935Z\n" +
		"DTSTART;TZID=Europe/Berlin:20111101\n" +
		"RRULE:FREQ=YEARLY;INTERVAL=1\n" +
		"BEGIN:VALARM\n" +
		"ACTION:DISPLAY\n" +
		"TRIGGER:-P2W\n" +
		"DESCRIPTION:Vorname Nachname\n" +
		"END:VALARM\n" +
		"TRANSP:OPAQUE\n" +
		"END:VEVENT\n" +
		"END:VCALENDAR";


	public void testReminderTwoWeeksBefore() throws Exception{
		ICalImportRequest importRequest = new ICalImportRequest(folder.getObjectID(), ical);
		ICalImportResponse importResponse = getClient().execute(importRequest);

		ImportResult[] imports = importResponse.getImports();
		assertEquals(1, imports.length);
		int id = Integer.parseInt(imports[0].getObjectId());
		TimeZone tz = getClient().getValues().getTimeZone();

		Appointment actual = getClient().execute(new GetRequest(folder.getObjectID(), id)).getAppointment(tz);

		assertEquals(2 * 7 * 24 * 60, actual.getAlarm());
	}

	public void testReminderFourDaysBefore() throws Exception{
		ICalImportRequest importRequest = new ICalImportRequest(folder.getObjectID(), ical.replace("-P2W", "-P4D"));
		ICalImportResponse importResponse = getClient().execute(importRequest);

		ImportResult[] imports = importResponse.getImports();
		assertEquals(1, imports.length);
		int id = Integer.parseInt(imports[0].getObjectId());
		TimeZone tz = getClient().getValues().getTimeZone();

		Appointment actual = getClient().execute(new GetRequest(folder.getObjectID(), id)).getAppointment(tz);

		assertEquals(4 * 24 * 60, actual.getAlarm());
	}

	public void testReminderEightMinutesBefore() throws Exception{
		ICalImportRequest importRequest = new ICalImportRequest(folder.getObjectID(), ical.replace("-P2W", "-P8M"));
		ICalImportResponse importResponse = getClient().execute(importRequest);

		ImportResult[] imports = importResponse.getImports();
		assertEquals(1, imports.length);
		int id = Integer.parseInt(imports[0].getObjectId());
		TimeZone tz = getClient().getValues().getTimeZone();

		Appointment actual = getClient().execute(new GetRequest(folder.getObjectID(), id)).getAppointment(tz);

		assertEquals(8, actual.getAlarm());
	}
	
	public void testReminderEightMinutesBeforeCorrectly() throws Exception{
		ICalImportRequest importRequest = new ICalImportRequest(folder.getObjectID(), ical.replace("-P2W", "-PT8M"));
		ICalImportResponse importResponse = getClient().execute(importRequest);

		ImportResult[] imports = importResponse.getImports();
		assertEquals(1, imports.length);
		int id = Integer.parseInt(imports[0].getObjectId());
		TimeZone tz = getClient().getValues().getTimeZone();
		
		Appointment actual = getClient().execute(new GetRequest(folder.getObjectID(), id)).getAppointment(tz);
	
		assertEquals(8, actual.getAlarm());
	}
	
	public void testReminderSixteenHoursBefore() throws Exception{
		ICalImportRequest importRequest = new ICalImportRequest(folder.getObjectID(), ical.replace("-P2W", "-P16H"));
		ICalImportResponse importResponse = getClient().execute(importRequest);

		ImportResult[] imports = importResponse.getImports();
		assertEquals(1, imports.length);
		int id = Integer.parseInt(imports[0].getObjectId());
		TimeZone tz = getClient().getValues().getTimeZone();
		
		Appointment actual = getClient().execute(new GetRequest(folder.getObjectID(), id)).getAppointment(tz);
	
		assertEquals(16*60, actual.getAlarm());
	}
	
	public void testReminderSixteenHoursBeforeCorrectly() throws Exception{
		ICalImportRequest importRequest = new ICalImportRequest(folder.getObjectID(), ical.replace("-P2W", "-PT16H"));
		ICalImportResponse importResponse = getClient().execute(importRequest);

		ImportResult[] imports = importResponse.getImports();
		assertEquals(1, imports.length);
		int id = Integer.parseInt(imports[0].getObjectId());
		TimeZone tz = getClient().getValues().getTimeZone();
		
		Appointment actual = getClient().execute(new GetRequest(folder.getObjectID(), id)).getAppointment(tz);
	
		assertEquals(16*60, actual.getAlarm());
	}
	
	public void testReminderFourWeeksBefore() throws Exception{
		ICalImportRequest importRequest = new ICalImportRequest(folder.getObjectID(), ical.replace("-P2W", "-P4W"));
		ICalImportResponse importResponse = getClient().execute(importRequest);

		ImportResult[] imports = importResponse.getImports();
		assertEquals(1, imports.length);
		int id = Integer.parseInt(imports[0].getObjectId());
		TimeZone tz = getClient().getValues().getTimeZone();
		
		Appointment actual = getClient().execute(new GetRequest(folder.getObjectID(), id)).getAppointment(tz);
	
		assertEquals(4*7*24*60, actual.getAlarm());
	}
	
	public void testReminderWithCombinedTimeBefore() throws Exception{
		ICalImportRequest importRequest = new ICalImportRequest(folder.getObjectID(), ical.replace("-P2W", "-PT1W2D3H4M5S"));
		ICalImportResponse importResponse = getClient().execute(importRequest);

		ImportResult[] imports = importResponse.getImports();
		assertEquals(1, imports.length);
		int id = Integer.parseInt(imports[0].getObjectId());
		TimeZone tz = getClient().getValues().getTimeZone();
		
		Appointment actual = getClient().execute(new GetRequest(folder.getObjectID(), id)).getAppointment(tz);
	
		assertEquals(  ((((1*7)+2)*24+3)*60+4), actual.getAlarm()); //NOTE: No seconds.
	}
	
	public void testBiggerThanOxSupposedlyAllows() throws Exception{
		ICalImportRequest importRequest = new ICalImportRequest(folder.getObjectID(), ical.replace("-P2W", "-PT6W"));
		ICalImportResponse importResponse = getClient().execute(importRequest);

		ImportResult[] imports = importResponse.getImports();
		assertEquals(1, imports.length);
		int id = Integer.parseInt(imports[0].getObjectId());
		TimeZone tz = getClient().getValues().getTimeZone();
		
		Appointment actual = getClient().execute(new GetRequest(folder.getObjectID(), id)).getAppointment(tz);
	
		assertEquals(  6*7*24*60, actual.getAlarm());
	}
}
