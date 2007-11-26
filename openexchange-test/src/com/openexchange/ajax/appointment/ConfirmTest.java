package com.openexchange.ajax.appointment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.FolderTest;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.UserParticipant;

public class ConfirmTest extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(ConfirmTest.class);
	
	public ConfirmTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testDummy() throws Exception {
		
	}
	
    public void testConfirm() throws Exception {
		final FolderObject sharedFolderObject = FolderTest.getStandardCalendarFolder(getSecondWebConversation(), getHostName(), getSecondSessionId());
		final int secondUserId = sharedFolderObject.getCreatedBy();
		
        AppointmentObject appointmentObj = createAppointmentObject("testConfirm");
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(secondUserId);
		
		appointmentObj.setParticipants(participants);
		
		appointmentObj.setIgnoreConflicts(true);

        int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSessionId());
        
        confirmAppointment(getSecondWebConversation(), objectId, AppointmentObject.ACCEPT, null, PROTOCOL + getHostName(), getSecondSessionId());
		
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, timeZone, PROTOCOL + getHostName(), getSessionId());
		
		boolean found = false;
		
		UserParticipant[] users = loadAppointment.getUsers();
		for (int a = 0; a < users.length; a++) {
			if (users[a].getIdentifier() == secondUserId) {
				found = true;
				assertEquals("wrong confirm status", AppointmentObject.ACCEPT, users[a].getConfirm());
			}
		}
		
		assertTrue("user participant with id " + secondUserId + " not found", found);
		
        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getSessionId());
    }
}