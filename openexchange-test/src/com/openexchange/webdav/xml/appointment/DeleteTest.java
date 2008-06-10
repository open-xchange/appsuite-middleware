package com.openexchange.webdav.xml.appointment;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.XmlServlet;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DeleteTest extends AppointmentTest {
	
	public DeleteTest(String name) {
		super(name);
	}
	
	public void testDelete() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testDelete");
		appointmentObj.setIgnoreConflicts(true);
		int objectId1 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		int objectId2 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		int[][] objectIdAndFolderId = { { objectId1, appointmentFolderId }, { objectId2, appointmentFolderId } };
		
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password);
	}
	
	public void testDeleteConcurentConflict() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testUpdateAppointmentConcurentConflict");
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		try {
			deleteAppointment(webCon, objectId, appointmentFolderId, new Date(0), PROTOCOL + hostName, login, password );
			fail("expected concurent modification exception!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.MODIFICATION_STATUS);
		}
		
		deleteAppointment(webCon, objectId, appointmentFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void testDeleteNotFound() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testUpdateAppointmentNotFound");
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		try {
			deleteAppointment(webCon, (objectId + 1000), appointmentFolderId, PROTOCOL + hostName, login, password );
			fail("expected object not found exception!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.OBJECT_NOT_FOUND_STATUS);
		}
		
		deleteAppointment(webCon, objectId, appointmentFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void testDeleteRecurrenceWithDatePosition() throws Exception {
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
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(webCon, objectId, appointmentFolderId, PROTOCOL + hostName, login, password);
		compareObject(appointmentObj, loadAppointment);
		
		final Date modified = loadAppointment.getLastModified();
		
		deleteAppointment(webCon, objectId, appointmentFolderId, modified, new Date(c.getTimeInMillis() + changeExceptionPosition * dayInMillis), PROTOCOL + getHostName(), getLogin(), getPassword());
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		assertEqualsAndNotNull("delete exception is not equals", loadAppointment.getDeleteException(), new Date[] { new Date(c.getTimeInMillis() + changeExceptionPosition * dayInMillis) } );
		
		deleteAppointment(webCon, new int[][] { { objectId, appointmentFolderId } }, PROTOCOL + hostName, login, password);
	}
	
	public void testDeleteRecurrenceWithDeleteExceptions() throws Exception {
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		Date until = new Date(c.getTimeInMillis() + (15*dayInMillis));
		
		int changeExceptionPosition = 3;
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testDeleteRecurrenceWithDeleteExceptions");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		appointmentObj.setObjectID(objectId);
		
		AppointmentObject loadAppointment = loadAppointment(webCon, objectId, appointmentFolderId, PROTOCOL + hostName, login, password);
		final Date modified = loadAppointment.getLastModified();
		
		loadAppointment = loadAppointment(webCon, objectId, appointmentFolderId, modified, PROTOCOL + hostName, login, password);
		compareObject(appointmentObj, loadAppointment);
		
		appointmentObj.setDeleteExceptions(new Date[] { new Date(c.getTimeInMillis() + changeExceptionPosition * dayInMillis) } );
		
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, getHostName(), getLogin(), getPassword());
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, modified, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(appointmentObj, loadAppointment);
		
		deleteAppointment(webCon, new int[][] { { objectId, appointmentFolderId } }, PROTOCOL + hostName, login, password);
	}
}

