package com.openexchange.ajax.appointment;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.webdav.xml.FolderTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Bug6055Test extends AppointmentTest {
	
	private static final Log LOG = LogFactory.getLog(Bug6055Test.class);
	
	public Bug6055Test(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testBug6055() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testBug6055");
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setIgnoreConflicts(true);
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new ExternalUserParticipant();
		participants[1].setEmailAddress("externaluser1@ox6-test.tux");
		participants[2] = new ExternalUserParticipant();
		participants[2].setEmailAddress("externaluser2@ox6-test.tux");
		participants[3] = new ExternalUserParticipant();
		participants[3].setEmailAddress("externaluser3@ox6-test.tux");
		appointmentObj.setParticipants(participants);
		
		int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
		appointmentObj.setObjectID(objectId);

		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());

		participants = new com.openexchange.groupware.container.Participant[3];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new ExternalUserParticipant();
		participants[1].setEmailAddress("externaluser1@ox6-test.tux");
		participants[2] = new ExternalUserParticipant();
		participants[2].setEmailAddress("externaluser3@ox6-test.tux");
		
		appointmentObj.setParticipants(participants);
		
		updateAppointment(getWebConversation(), appointmentObj, objectId, appointmentFolderId, timeZone, getHostName(), getSessionId());
		
		loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, getHostName(), getSessionId());
		compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());

		deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId());
	}
}