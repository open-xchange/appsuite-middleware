package com.openexchange.ajax.appointment;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.AppointmentObject;

public class DeleteTest extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(DeleteTest.class);
	
	public DeleteTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
    public void testDelete() throws Exception {
        AppointmentObject appointmentObj = createAppointmentObject("testDelete");
		appointmentObj.setIgnoreConflicts(true);
        int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        int id = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        
        deleteAppointment(getWebConversation(), id, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
		try {
			deleteAppointment(getWebConversation(), id, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
			fail("OXObjectNotFoundException expected!");
		} catch (Exception ex) {
			assertTrue(true);
		}
    }
	
	public void testDeleteRecurrenceWithPosition() throws Exception {
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		Date until = new Date(c.getTimeInMillis() + (15*dayInMillis));
		
		int changeExceptionPosition = 3;
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testDeleteRecurrenceWithPosition");
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setUntil(until);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, startTime, endTime);
		
		appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testDeleteRecurrenceWithPosition - exception");
		appointmentObj.setStartDate(new Date(startTime + 60*60*1000));
		appointmentObj.setEndDate(new Date(endTime + 60*60*1000));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrencePosition(changeExceptionPosition);
		appointmentObj.setIgnoreConflicts(true);
		
		int newObjectId = updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(newObjectId);

		assertFalse("object id of the update is equals with the old object id", newObjectId == objectId);
		
		loadAppointment = loadAppointment(getWebConversation(), newObjectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());
		
		loadAppointment = loadAppointment(getWebConversation(), newObjectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());
		
		
		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
	}
}

