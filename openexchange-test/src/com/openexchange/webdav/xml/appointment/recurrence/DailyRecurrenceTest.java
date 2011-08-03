package com.openexchange.webdav.xml.appointment.recurrence;

import java.util.Date;
import com.openexchange.groupware.container.Appointment;

public class DailyRecurrenceTest extends AbstractRecurrenceTest {
	
	public DailyRecurrenceTest(final String name) {
		super(name);
	}

	public void testDailyRecurrenceFromWinter2SummerTime() throws Exception {
		final Date until = simpleDateFormatUTC.parse("2007-04-01 00:00:00");
		
		final Date startDate = simpleDateFormatUTC.parse("2007-03-01 08:00:00");
		final Date endDate = simpleDateFormatUTC.parse("2007-03-01 10:00:00");
		
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testDailyRecurrenceFromWinter2SummerTime");
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		appointmentObj.setObjectID(objectId);
		Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		final Date modified = loadAppointment.getLastModified();
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, decrementDate(modified), getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}	
	
	public void testDailyRecurrenceFromSummer2WinterTime() throws Exception {
		final Date until = simpleDateFormatUTC.parse("2007-11-01 00:00:00");
		
		final Date startDate = simpleDateFormatUTC.parse("2007-10-01 08:00:00");
		final Date endDate = simpleDateFormatUTC.parse("2007-10-01 10:00:00");
		
		final Appointment appointmentObj = new Appointment();
		appointmentObj.setTitle("testDailyRecurrenceFromSummer2WinterTime");
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);
		appointmentObj.setShownAs(Appointment.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(Appointment.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		final int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		appointmentObj.setObjectID(objectId);
		Appointment loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		final Date modified = loadAppointment.getLastModified();
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, decrementDate(modified), getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}	
}