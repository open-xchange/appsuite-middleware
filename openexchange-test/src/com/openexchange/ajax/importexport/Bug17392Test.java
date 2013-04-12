package com.openexchange.ajax.importexport;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.importexport.actions.ICalExportRequest;
import com.openexchange.ajax.importexport.actions.ICalExportResponse;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;

/**
 * These tests document the behavior of exports/imports of ical files
 * and how they treat series that span DST.
 * Note that we use the location of America/New York for these tests.
 * EST, eastern standard time, is at -5
 * EDT, eastern daylight time, is at -4.
 *
 * @author tierlieb
 */
public class Bug17392Test extends ManagedAppointmentTest {
	protected Calendar calendar;
	protected int startHour = 13;
	protected int estOffset = -5;
	protected int edtOffset = -4;
	protected String start = "20101015T"+startHour+"0000";
	protected String end =   "20101015T"+(startHour+1)+"0000";

	public Bug17392Test(String name) {
		super(name);
	}

	public Appointment importAndGet(String ical) throws Exception{
		ICalImportRequest request = new ICalImportRequest(folder.getObjectID(), ical);
		ICalImportResponse response = getClient().execute(request);
		JSONArray data = (JSONArray) response.getData();
		assertEquals(1, data.length());
		JSONObject jsonObject = data.getJSONObject(0);

		return calendarManager.get(
			jsonObject.getInt(AppointmentFields.FOLDER_ID),
			jsonObject.getInt(AppointmentFields.ID));
	}


	@Override
	protected void setUp() throws Exception {
		super.setUp();
		calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.set(Calendar.YEAR, 2010);
		calendar.set(Calendar.MONTH, Calendar.OCTOBER);
		calendar.set(Calendar.DAY_OF_MONTH, 15);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, startHour);
	}

	public void testZuluTimezoneImport() throws Exception{
		String ical =
			"BEGIN:VCALENDAR\n"
			+ "VERSION:2.0\n"
			+ "BEGIN:VEVENT\n"
			+ "SUMMARY:Zulu-Time Appointment\n"
			+ "DTSTART:"+start+"Z\n"
			+ "DTEND:"+end+"Z\n"
			//+ "RRULE:FREQ=MONTHLY;INTERVAL=1;COUNT=2\n"
			+ "END:VEVENT\n";

		Appointment app = importAndGet(ical);

		Date actualStart = app.getStartDate();

		calendar.setTime(actualStart);
		int actualHour = calendar.get(Calendar.HOUR_OF_DAY);

		assertEquals("Should not be shifted by time zones, because it is UTC/Zulu time", startHour, actualHour);
	}

	public void testTzidTimezoneImport() throws Exception{
		String ical =
			"BEGIN:VCALENDAR\n"
			+ "VERSION:2.0\n"
			+ "BEGIN:VEVENT\n"
			+ "SUMMARY: EST/New York Time Appointment\n"
			+ "DTSTART;TZID=America/New_York:"+start+"\n"
			+ "DTEND;TZID=America/New_York:"+end+"\n"
			+ "END:VEVENT\n";

		Appointment app = importAndGet(ical);
		Date actualStart = app.getStartDate();

		calendar.setTime(actualStart);
		int actualHour = calendar.get(Calendar.HOUR_OF_DAY);

		assertEquals("Should be shifted by +4 hours, because it was 1300 EDT originally", startHour-edtOffset, actualHour);
	}


	public void testTzidExport() throws Exception{
		verifyTimezoneDoesNotGetLost("America/New_York");
		verifyTimezoneDoesNotGetLost("Europe/Berlin");
	}


	public void testRoundtripUTC() throws Exception{
		roundtrip("UTC");
	}

	public void testRoundtripNewYork() throws Exception{
		roundtrip("America/New_York");
	}
	public void testRoundtripBerlin() throws Exception{
		roundtrip("Europe/Berlin");
	}
	private void verifyTimezoneDoesNotGetLost(String tzid) throws OXException, IOException, SAXException, JSONException {
		Appointment app = new Appointment();
		app.setTimezone(tzid);
		app.setTitle("Appointment to be exported - this better contain a TZID!");
		app.setParentFolderID(folder.getObjectID());

		app.setStartDate(calendar.getTime());
		calendar.add(Calendar.HOUR, 1);
		app.setEndDate(calendar.getTime());

		calendarManager.insert(app);

		ICalExportRequest request = new ICalExportRequest(folder.getObjectID());
		ICalExportResponse response = getClient().execute(request);
		String ical = (String) response.getData();
		// System.out.println(ical);
		assertTrue("Export should contain a TZID for " + tzid + System.getProperty("line.separator") + ical, ical.contains("DTSTART;TZID="+tzid));
	}

	public void roundtrip(String tzid) throws Exception{

		//construct date
		Calendar startingpoint = Calendar.getInstance(TimeZone.getTimeZone(tzid));
		startingpoint.set(Calendar.YEAR, 2010);
		startingpoint.set(Calendar.MONTH, Calendar.MARCH);
		startingpoint.set(Calendar.DAY_OF_MONTH, 5);
		startingpoint.set(Calendar.HOUR_OF_DAY, startHour);

		//create appointment
		Appointment app = new Appointment();
		app.setTimezone(tzid);
		app.setTitle("Appointment series to do a round-trip");
		app.setParentFolderID(folder.getObjectID());
		app.setStartDate(startingpoint.getTime());
		startingpoint.add(Calendar.HOUR, 1);
		app.setEndDate(startingpoint.getTime());
		app.setRecurrenceType(Appointment.MONTHLY);
		app.setRecurrenceCount(2);
		app.setInterval(1);
		app.setDayInMonth(startingpoint.get(Calendar.DAY_OF_MONTH));
		calendarManager.insert(app);

		//export
		AJAXRequest<?> request = new ICalExportRequest(folder.getObjectID());
		AbstractAJAXResponse response = getClient().execute(request);
		String ical = (String) response.getData();

		//remove original
		calendarManager.delete(app);

		//import again
		request = new ICalImportRequest(folder.getObjectID(), ical);
		response = getClient().execute(request);
		JSONArray data = (JSONArray) response.getData();
		assertEquals(1, data.length());
		int objID = data.getJSONObject(0).getInt(AppointmentFields.ID);

		//verify occurrence during winter
		TimeZone appointmentTz = TimeZone.getTimeZone(tzid);

		Calendar wintertime = Calendar.getInstance(appointmentTz);
		request = new GetRequest(folder.getObjectID(), objID, true);
		response = getClient().execute(request);
		Appointment winterApp = ((GetResponse) response).getAppointment(appointmentTz);
		wintertime.setTime(winterApp.getStartDate());

		//verify occurrence during summer
		request = new GetRequest(folder.getObjectID(), objID, 2, true);
		response = getClient().execute(request);
		Appointment summerApp = ((GetResponse) response).getAppointment(appointmentTz);

		Calendar summertime = Calendar.getInstance(appointmentTz);
		summertime.setTime(summerApp.getStartDate());

		//correcting offsets
		TimeZone userTz = getClient().getValues().getTimeZone();
		int userTzOffset = userTz.getOffset(winterApp.getStartDate().getTime())/1000/60/60;
		int appTzOffset = appointmentTz.getOffset(winterApp.getStartDate().getTime())/1000/60/60;

		assertEquals("Precondition: First occurrence should be in March", Calendar.MARCH, wintertime.get(Calendar.MONTH));
		assertEquals("Precondition: Second occurrence should be in April", Calendar.APRIL, summertime.get(Calendar.MONTH));

		assertEquals("First date (during wintertime) on "+tzid+" should be at", startHour+userTzOffset, wintertime.get(Calendar.HOUR_OF_DAY)+appTzOffset);
		assertEquals("Second date (during summer time) on "+tzid+" should be at", startHour+userTzOffset, summertime.get(Calendar.HOUR_OF_DAY)+appTzOffset);
	}
}
