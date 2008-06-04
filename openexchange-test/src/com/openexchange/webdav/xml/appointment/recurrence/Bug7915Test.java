package com.openexchange.webdav.xml.appointment.recurrence;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.container.AppointmentObject;

public class Bug7915Test extends AbstractRecurrenceTest {
	
	private static final Log LOG = LogFactory.getLog(Bug7915Test.class);
	
	public Bug7915Test(String name) {
		super(name);
		simpleDateFormatUTC.setTimeZone(timeZoneUTC);
	}
	
	public void testBug7915() throws Exception {
		final Date startDate = simpleDateFormatUTC.parse("2007-06-01 00:00:00");
		final Date endDate = simpleDateFormatUTC.parse("2007-06-02 00:00:00");

		final Date until = simpleDateFormatUTC.parse("2007-06-15 00:00:00");
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testBug7915");
		appointmentObj.setStartDate(startDate);
		appointmentObj.setEndDate(endDate);
		appointmentObj.setFullTime(true);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		final Date exceptionStartDate = simpleDateFormatUTC.parse("2007-06-06 00:00:00");
		final Date exceptionEndDate = simpleDateFormatUTC.parse("2007-06-07 00:00:00");
		
		final Date recurrenceDatePosition = simpleDateFormatUTC.parse("2007-06-06 00:00:00");
		
		AppointmentObject exceptionAppointmentObject = new AppointmentObject();
		exceptionAppointmentObject.setTitle("testBug7915 - change exception (2007-06-06)");
		exceptionAppointmentObject.setStartDate(exceptionStartDate);
		exceptionAppointmentObject.setEndDate(exceptionEndDate);
		exceptionAppointmentObject.setFullTime(true);
		exceptionAppointmentObject.setRecurrenceDatePosition(recurrenceDatePosition);
		exceptionAppointmentObject.setShownAs(AppointmentObject.ABSENT);
		exceptionAppointmentObject.setParentFolderID(appointmentFolderId);
		exceptionAppointmentObject.setIgnoreConflicts(true);

		int exceptionObjectId = updateAppointment(getWebConversation(), exceptionAppointmentObject, objectId, appointmentFolderId, getHostName(), getLogin(), getPassword());
		
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		final Date modified = loadAppointment.getLastModified();
		
		exceptionAppointmentObject.setObjectID(exceptionObjectId);
		loadAppointment = loadAppointment(getWebConversation(), exceptionObjectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(exceptionAppointmentObject, loadAppointment);
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);

		loadAppointment = loadAppointment(getWebConversation(), exceptionObjectId, appointmentFolderId, modified, getHostName(), getLogin(), getPassword());
		compareObject(exceptionAppointmentObject, loadAppointment);
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
}