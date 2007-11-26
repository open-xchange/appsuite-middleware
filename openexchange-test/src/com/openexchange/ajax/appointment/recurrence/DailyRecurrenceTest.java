package com.openexchange.ajax.appointment.recurrence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.container.AppointmentObject;

public class DailyRecurrenceTest extends AbstractRecurrenceTest {
	
	private static final Log LOG = LogFactory.getLog(DailyRecurrenceTest.class);
	
	public DailyRecurrenceTest(String name) {
		super(name);
		simpleDateFormatUTC.setTimeZone(timeZoneUTC);
	}
	
	public void testDummy() {
		
	}
	
	public void testDailyRecurrenceFromWinter2SummerTime() throws Exception {
		Date until = simpleDateFormatUTC.parse("2007-04-01 00:00:00");
		
		Date start = simpleDateFormatUTC.parse("2007-02-01 00:00:00");
		Date end = simpleDateFormatUTC.parse("2007-05-01 00:00:00");
		
		Date startDate = simpleDateFormat.parse("2007-03-01 08:00:00");
		Date endDate = simpleDateFormat.parse("2007-03-01 10:00:00");
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testDailyRecurrenceFromWinter2SummerTime");
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startDate.getTime(), endDate.getTime());
		
		boolean found = false;
		
		List<Occurrence> occurrenceList = new ArrayList<Occurrence>();
		
		AppointmentObject[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, _fields, start, end, timeZone, true, getHostName(), getSessionId());
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				occurrenceList.add(new Occurrence(appointmentArray[a].getStartDate(), appointmentArray[a].getEndDate(), appointmentArray[a].getRecurrencePosition()));
				found = true;
			}
		}
		
		assertTrue("appointment not found in response", found);
		
		Occurrence[] occurrenceArray = occurrenceList.toArray(new Occurrence[occurrenceList.size()]);
		
		
		for (int a = 1; a <= 31; a++) {
			Occurrence occurrence = getOccurrenceByPosition(occurrenceArray, a);
			int day = a;
			if (day < 10) {
				assertOccurrence(day, simpleDateFormat.parse("2007-03-0" + day + " 08:00:00"), simpleDateFormat.parse("2007-03-0" + day + " 10:00:00"), occurrence, timeZone);
			} else {
				assertOccurrence(day, simpleDateFormat.parse("2007-03-" + day + " 08:00:00"), simpleDateFormat.parse("2007-03-" + day + " 10:00:00"), occurrence, timeZone);
			}
		}

		Occurrence occurrence = getOccurrenceByPosition(occurrenceArray, 32);
		assertOccurrence(32, simpleDateFormat.parse("2007-04-01 08:00:00"), simpleDateFormat.parse("2007-04-01 10:00:00"), occurrence, timeZone);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}

	public void testFullTimeDailyRecurrenceFromWinter2SummerTime() throws Exception {
		Date until = simpleDateFormatUTC.parse("2007-04-01 00:00:00");
		
		Date start = simpleDateFormatUTC.parse("2007-02-01 00:00:00");
		Date end = simpleDateFormatUTC.parse("2007-05-01 00:00:00");
		
		Date startDate = simpleDateFormatUTC.parse("2007-03-01 00:00:00");
		Date endDate = simpleDateFormatUTC.parse("2007-03-02 00:00:00");
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testFullTimeDailyRecurrenceFromWinter2SummerTime");
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);
		appointmentObj.setFullTime(true);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startDate.getTime(), endDate.getTime());
		
		boolean found = false;
		
		List<Occurrence> occurrenceList = new ArrayList<Occurrence>();
		
		AppointmentObject[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, _fields, start, end, timeZone, true, getHostName(), getSessionId());
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				occurrenceList.add(new Occurrence(appointmentArray[a].getStartDate(), appointmentArray[a].getEndDate(), appointmentArray[a].getRecurrencePosition()));
				found = true;
			}
		}
		
		assertTrue("appointment not found in response", found);
		
		Occurrence[] occurrenceArray = occurrenceList.toArray(new Occurrence[occurrenceList.size()]);
		
		
		for (int a = 1; a <= 31; a++) {
			Occurrence occurrence = getOccurrenceByPosition(occurrenceArray, a);
			int day = a;
			if (day < 10) {
				assertOccurrence(day, simpleDateFormatUTC.parse("2007-03-0" + day + " 00:00:00"), simpleDateFormatUTC.parse("2007-03-0" + (day+1) + " 00:00:00"), occurrence);
			} else {
				assertOccurrence(day, simpleDateFormatUTC.parse("2007-03-" + day + " 00:00:00"), simpleDateFormatUTC.parse("2007-03-" + (day+1) + " 00:00:00"), occurrence);
			}
		}

		Occurrence occurrence = getOccurrenceByPosition(occurrenceArray, 32);
		assertOccurrence(32, simpleDateFormatUTC.parse("2007-04-01 00:00:00"), simpleDateFormatUTC.parse("2007-04-02 00:00:00"), occurrence, timeZone);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testDailyRecurrenceFromSummer2WinterTime() throws Exception {
		Date until = simpleDateFormatUTC.parse("2007-11-01 00:00:00");
		
		Date start = simpleDateFormatUTC.parse("2007-09-01 00:00:00");
		Date end = simpleDateFormatUTC.parse("2007-12-01 00:00:00");
		
		Date startDate = simpleDateFormat.parse("2007-10-01 08:00:00");
		Date endDate = simpleDateFormat.parse("2007-10-01 10:00:00");
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testDailyRecurrenceFromSummer2WinterTime");
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startDate.getTime(), endDate.getTime());
		
		boolean found = false;
		
		List<Occurrence> occurrenceList = new ArrayList<Occurrence>();
		
		AppointmentObject[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, _fields, start, end, timeZone, true, getHostName(), getSessionId());
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				occurrenceList.add(new Occurrence(appointmentArray[a].getStartDate(), appointmentArray[a].getEndDate(), appointmentArray[a].getRecurrencePosition()));
				found = true;
			}
		}
		
		assertTrue("appointment not found in response", found);
		
		Occurrence[] occurrenceArray = occurrenceList.toArray(new Occurrence[occurrenceList.size()]);
		
		
		for (int a = 1; a <= 31; a++) {
			Occurrence occurrence = getOccurrenceByPosition(occurrenceArray, a);
			int day = a;
			if (day < 10) {
				assertOccurrence(day, simpleDateFormat.parse("2007-10-0" + day + " 08:00:00"), simpleDateFormat.parse("2007-10-0" + day + " 10:00:00"), occurrence);
			} else {
				assertOccurrence(day, simpleDateFormat.parse("2007-10-" + day + " 08:00:00"), simpleDateFormat.parse("2007-10-" + day + " 10:00:00"), occurrence);
			}
		}

		Occurrence occurrence = getOccurrenceByPosition(occurrenceArray, 32);
		assertOccurrence(32, simpleDateFormat.parse("2007-11-01 08:00:00"), simpleDateFormat.parse("2007-11-01 10:00:00"), occurrence);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
	
	public void testFullTimeDailyRecurrenceFromSummer2WinterTime() throws Exception {
		Date until = simpleDateFormatUTC.parse("2007-11-01 00:00:00");
		
		Date start = simpleDateFormatUTC.parse("2007-09-01 00:00:00");
		Date end = simpleDateFormatUTC.parse("2007-12-01 00:00:00");
		
		Date startDate = simpleDateFormatUTC.parse("2007-10-01 00:00:00");
		Date endDate = simpleDateFormatUTC.parse("2007-10-02 00:00:00");
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testFullTimeDailyRecurrenceFromSummer2WinterTime");
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
                appointmentObj.setFullTime(true);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startDate.getTime(), endDate.getTime());
		
		boolean found = false;
		
		List<Occurrence> occurrenceList = new ArrayList<Occurrence>();
		
		AppointmentObject[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, _fields, start, end, timeZone, true, getHostName(), getSessionId());
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == objectId) {
				occurrenceList.add(new Occurrence(appointmentArray[a].getStartDate(), appointmentArray[a].getEndDate(), appointmentArray[a].getRecurrencePosition()));
				found = true;
			}
		}
		
		assertTrue("appointment not found in response", found);
		
		Occurrence[] occurrenceArray = occurrenceList.toArray(new Occurrence[occurrenceList.size()]);
		
		
		for (int a = 1; a <= 31; a++) {
			Occurrence occurrence = getOccurrenceByPosition(occurrenceArray, a);
			int day = a;
			if (day < 10) {
				assertOccurrence(day, simpleDateFormatUTC.parse("2007-10-0" + day + " 00:00:00"), simpleDateFormatUTC.parse("2007-10-0" + (day+1) + " 00:00:00"), occurrence);
			} else {
				assertOccurrence(day, simpleDateFormatUTC.parse("2007-10-" + day + " 00:00:00"), simpleDateFormatUTC.parse("2007-10-" + (day+1) + " 00:00:00"), occurrence);
			}
		}

		Occurrence occurrence = getOccurrenceByPosition(occurrenceArray, 32);
		assertOccurrence(32, simpleDateFormatUTC.parse("2007-11-01 00:00:00"), simpleDateFormatUTC.parse("2007-11-02 00:00:00"), occurrence);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
}

