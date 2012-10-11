package com.openexchange.ajax.importexport;

import java.util.Calendar;

import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.importexport.ImportResult;

public class Bug20718Test_JumpDuringDstCrossing extends ManagedAppointmentTest {

	public Bug20718Test_JumpDuringDstCrossing(String name) {
		super(name);
	}

	private String ical(String tzid){
		return "BEGIN:VCALENDAR\n" + 
		"PRODID:-//Microsoft Corporation//Outlook 14.0 MIMEDIR//EN\n" + 
		"VERSION:2.0\n" + 
		"METHOD:PUBLISH\n" + 
		"BEGIN:VTIMEZONE\n" + 
		"TZID:Amsterdam\\, Berlin\\, Bern\\, Rom\\, Stockholm\\, Wien\n" + 
		"BEGIN:STANDARD\n" + 
		"DTSTART:16011028T030000\n" + 
		"RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\n" + 
		"TZOFFSETFROM:+0200\n" + 
		"TZOFFSETTO:+0100\n" + 
		"END:STANDARD\n" + 
		"BEGIN:DAYLIGHT\n" + 
		"DTSTART:16010325T020000\n" + 
		"RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\n" + 
		"TZOFFSETFROM:+0100\n" + 
		"TZOFFSETTO:+0200\n" + 
		"END:DAYLIGHT\n" + 
		"END:VTIMEZONE\n" + 
		"\n" + 
		"BEGIN:VEVENT\n" + 
		"CLASS:PUBLIC\n" + 
		"DTEND;TZID=\""+tzid+"\":20081009T170000\n" + 
		"DTSTART;TZID=\"Amsterdam, Berlin, Bern, Rom, Stockholm, Wien\":20081009T130000\n" + 
		"RRULE:FREQ=WEEKLY;BYDAY=TH\n" + 
		"SUMMARY:geblockt f\u00fcr Prof. Bruce-Boye - keine Termine\n" + 
		"UID:AAAAAGQnWJsQLItElPc+mdUPXr/kSyMA\n" + 
		"END:VEVENT\n" + 
		"END:VCALENDAR";
	}
	
	public void testOriginalOutlookCase() throws Exception{
		doTest("Amsterdam, Berlin, Bern, Rom, Stockholm, Wien");
	}
	
	public void testRegression1() throws Exception{
		doTest("Zulu");
	}

	public void testRegression2() throws Exception{
		doTest("Europe/Berlin");
	}
	
	public void doTest(String tzid) throws Exception{
		ICalImportRequest impRequest = new ICalImportRequest(folder.getObjectID(), ical(tzid));
		ICalImportResponse impResponse = getClient().execute(impRequest);
		assertFalse(impResponse.hasError());
		ImportResult[] imports = impResponse.getImports();
		assertEquals(1, imports.length);
		
		int objectID = Integer.parseInt(imports[0].getObjectId());
		
		Appointment summer = calendarManager.get(folder.getObjectID(), objectID, 1);
		Appointment winter = calendarManager.get(folder.getObjectID(), objectID, 4);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(summer.getStartDate());
		int summerStart = cal.get(Calendar.HOUR_OF_DAY);
		cal.setTime(winter.getStartDate());
		int winterStart = cal.get(Calendar.HOUR_OF_DAY);
		
		assertEquals(summerStart,winterStart);
	}

}
