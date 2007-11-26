package com.openexchange.ajax.appointment;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.AppointmentObject;

public class Bug8836Test extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(Bug8836Test.class);
	
	public Bug8836Test(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testBug8836() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testBug8836");
		appointmentObj.setIgnoreConflicts(true);
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		
		appointmentObj.setObjectID(objectId);
		appointmentObj.setPrivateFlag(true);
		
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, getHostName(), getSessionId());
		Date modified = new Date(loadAppointment.getLastModified().getTime()+1);
		
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, modified, timeZone, getHostName(), getSessionId());

		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, getHostName(), getSessionId());
		modified = new Date(loadAppointment.getLastModified().getTime()+1);
		
		compareObject(appointmentObj, loadAppointment);

		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, modified, getHostName(), getSessionId());
	}
}