package com.openexchange.webdav.xml.appointment;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.webdav.xml.AppointmentTest;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DeleteTest extends AppointmentTest {
	
	public void testDelete() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testDelete");
		int objectId1 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		int objectId2 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		int[][] objectIdAndFolderId = { { objectId1, appointmentFolderId }, { objectId2, appointmentFolderId } };
		
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password);
	}
	
	public void _notestDeleteRecurrenceWithDatePosition() throws Exception {
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		Date until = new Date(c.getTimeInMillis() + (15*dayInMillis));
		
		int changeExceptionPosition = 3;
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testDeleteRecurrenceWithDatePosition");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(webCon, objectId, appointmentFolderId, PROTOCOL + hostName, login, password);
		compareObject(appointmentObj, loadAppointment);
		
		appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testDeleteRecurrenceWithDatePosition - exception");
		appointmentObj.setStartDate(new Date(startTime.getTime() + 60*60*1000));
		appointmentObj.setEndDate(new Date(endTime.getTime() + 60*60*1000));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceDatePosition(new Date(c.getTimeInMillis() + changeExceptionPosition * dayInMillis));
		
		int newObjectId = updateAppointment(webCon, appointmentObj, objectId, appointmentFolderId, PROTOCOL + hostName, login, password);
		assertFalse("object id of the update is equals with the old object id", newObjectId == objectId);
		
		loadAppointment = loadAppointment(webCon, newObjectId, appointmentFolderId, PROTOCOL + hostName, login, password);
		compareObject(appointmentObj, loadAppointment);
		
		deleteAppointment(webCon, new int[][] { { objectId }, { appointmentFolderId } }, PROTOCOL + hostName, login, password);
	}
}

