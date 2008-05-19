package com.openexchange.webdav.xml.appointment.recurrence;

import com.openexchange.groupware.container.AppointmentObject;
import java.util.Date;

public class DailyRecurrenceTest extends AbstractRecurrenceTest {
	
	public DailyRecurrenceTest(String name) {
		super(name);
	}

	public void testDailyRecurrenceFromWinter2SummerTime() throws Exception {
		Date until = simpleDateFormatUTC.parse("2007-04-01 00:00:00");
		
		Date startDate = simpleDateFormatUTC.parse("2007-03-01 08:00:00");
		Date endDate = simpleDateFormatUTC.parse("2007-03-01 10:00:00");
		
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
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		final Date modified = loadAppointment.getLastModified();
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}	
	
	public void testDailyRecurrenceFromSummer2WinterTime() throws Exception {
		Date until = simpleDateFormatUTC.parse("2007-11-01 00:00:00");
		
		Date startDate = simpleDateFormatUTC.parse("2007-10-01 08:00:00");
		Date endDate = simpleDateFormatUTC.parse("2007-10-01 10:00:00");
		
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
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		final Date modified = loadAppointment.getLastModified();
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}	
}