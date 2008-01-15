package com.openexchange.ajax.appointment.recurrence;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.container.AppointmentObject;

public class Bug10760Test extends AbstractRecurrenceTest {
	
	private static final Log LOG = LogFactory.getLog(Bug10760Test.class);
	
	public Bug10760Test(String name) {
		super(name);
	}
	
	public void testDummy() {
		
	}

	public void testBug10760() throws Exception {
		final String title = "testBug10760";
		final AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle(title);
		appointmentObj.setStartDate(new Date(startTime));
		appointmentObj.setEndDate(new Date(endTime));
		appointmentObj.setShownAs(AppointmentObject.ABSENT);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setRecurrenceType(AppointmentObject.DAILY);
		appointmentObj.setInterval(1);
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);
		
		appointmentObj.setRecurrencePosition(2);
		final int newObjectId = updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, timeZone, getHostName(), getSessionId());
		
		final AppointmentObject[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, APPOINTMENT_FIELDS, new Date(), new Date(), timeZone, false, getHostName(), getSessionId());
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == newObjectId) {
				assertEquals("recurrence id is not equals expected", objectId, appointmentArray[a].getRecurrenceID());
				assertEquals("recurrence pos is not equals expected", 2, appointmentArray[a].getRecurrencePosition());
			}
		}
	}
}
