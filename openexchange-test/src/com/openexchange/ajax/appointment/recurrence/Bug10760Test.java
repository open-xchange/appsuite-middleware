package com.openexchange.ajax.appointment.recurrence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.openexchange.groupware.container.AppointmentObject;

public class Bug10760Test extends AbstractRecurrenceTest {
	
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
		final List<Integer> tmp = new ArrayList<Integer>();
		for (int i = 0; i < APPOINTMENT_FIELDS.length; i++) {
		    tmp.add(Integer.valueOf(APPOINTMENT_FIELDS[i]));
		}
		final Integer[] checkedFields = new Integer[] {
		    Integer.valueOf(AppointmentObject.RECURRENCE_ID),
		    Integer.valueOf(AppointmentObject.RECURRENCE_POSITION) };
        for (int i = 0; i < checkedFields.length; i++) {
    		if (!tmp.contains(checkedFields[i])) {
    		    tmp.add(checkedFields[i]);
    		}
        }
        final int[] fields = new int[tmp.size()];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = tmp.get(i).intValue();
        }
		final AppointmentObject[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, fields, new Date(), new Date(), timeZone, false, getHostName(), getSessionId());
		for (int a = 0; a < appointmentArray.length; a++) {
			if (appointmentArray[a].getObjectID() == newObjectId) {
				assertEquals("recurrence id is not equals expected", objectId, appointmentArray[a].getRecurrenceID());
				assertEquals("recurrence pos is not equals expected", 2, appointmentArray[a].getRecurrencePosition());
			}
		}
	}
}
