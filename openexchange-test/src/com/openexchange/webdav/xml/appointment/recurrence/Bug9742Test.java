package com.openexchange.webdav.xml.appointment.recurrence;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.container.AppointmentObject;

public class Bug9742Test extends AbstractRecurrenceTest {
	
	private static final Log LOG = LogFactory.getLog(Bug9742Test.class);
	
	public Bug9742Test(String name) {
		super(name);
	}
	
	public void testDummy() {
		
	}
	
	public void testBug6960() throws Exception {
		final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.setTime(startTime);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		calendar.add(Calendar.DAY_OF_MONTH, 3);
		
		final Date recurrenceDatePosition = calendar.getTime();
		
		calendar.add(Calendar.DAY_OF_MONTH, 3);
		
		final Date until = calendar.getTime();
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testBug6960");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		final Calendar calendarException = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendarException.setTime(recurrenceDatePosition);
		calendarException.set(Calendar.HOUR_OF_DAY, 10);
		
		final Date exceptionStartDate = calendarException.getTime();
		
		calendarException.set(Calendar.HOUR_OF_DAY, 12);
		
		final Date exceptionEndDate = calendarException.getTime();
		
		AppointmentObject exceptionAppointmentObject = new AppointmentObject();
		exceptionAppointmentObject.setTitle("testBug6960 - change exception");
		exceptionAppointmentObject.setStartDate(exceptionStartDate);
		exceptionAppointmentObject.setEndDate(exceptionEndDate);
		exceptionAppointmentObject.setRecurrenceDatePosition(recurrenceDatePosition);
		exceptionAppointmentObject.setShownAs(AppointmentObject.ABSENT);
		exceptionAppointmentObject.setParentFolderID(appointmentFolderId);
		exceptionAppointmentObject.setIgnoreConflicts(true);

		int exceptionObjectId = updateAppointment(getWebConversation(), exceptionAppointmentObject, objectId, appointmentFolderId, getHostName(), getLogin(), getPassword());
		
		final AppointmentObject loadAppointment = loadAppointment(getWebConversation(), exceptionObjectId, appointmentFolderId, getHostName(), getLogin(), getPassword());
		final Date modified = loadAppointment.getLastModified();
		
		deleteAppointment(getWebConversation(), exceptionObjectId, appointmentFolderId, recurrenceDatePosition, getHostName(), getLogin(), getPassword());
		
		final AppointmentObject[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, modified, true, true, getHostName(), getLogin(), getPassword());
		boolean found = false;
		
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == exceptionObjectId) {
				found = true;
				
				assertEquals("recurrence id not equals expected", objectId, appointmentArray[a].getRecurrenceID());				
				break;
			}
		}
		
		assertTrue("object id " + exceptionObjectId + " not found in response", found);

		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
}