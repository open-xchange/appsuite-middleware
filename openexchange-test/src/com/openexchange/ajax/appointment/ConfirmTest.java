package com.openexchange.ajax.appointment;

import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.FolderTest;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConfirmTest extends AppointmentTest {

	private static final Log LOG = LogFactory.getLog(ConfirmTest.class);
	
	public ConfirmTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
    public void _notestConfirm() throws Exception {
		final FolderObject sharedFolderObject = FolderTest.getStandardCalendarFolder(getSecondWebConversation(), getHostName(), getSecondSessionId());
		final int secondFolderId = sharedFolderObject.getObjectID();
		final int secondUserId = sharedFolderObject.getCreatedBy();
		
        AppointmentObject appointmentObj = createAppointmentObject("testConfirm");
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(secondUserId);
		
		appointmentObj.setParticipants(participants);

        int objectId = insertAppointment(getSecondWebConversation(), appointmentObj, timeZone, PROTOCOL + getHostName(), getSecondSessionId());
        
        confirmAppointment(getWebConversation(), objectId, AppointmentObject.ACCEPT, null, PROTOCOL + getHostName(), getSessionId());
		
		AppointmentObject loadAppointment = loadAppointment(getSecondWebConversation(), objectId, secondFolderId, timeZone, PROTOCOL + getHostName(), getSecondSessionId());
		
		boolean found = false;
		
		UserParticipant[] users = loadAppointment.getUsers();
		for (int a = 0; a < users.length; a++) {
			if (users[a].getIdentifier() == userId) {
				found = true;
				assertEquals("wrong confirm status", AppointmentObject.ACCEPT, users[a].getConfirm());
			}
		}
		
		assertTrue("user participant with id " + userId + " not found", found);
		
        deleteAppointment(getSecondWebConversation(), objectId, secondFolderId, PROTOCOL + getHostName(), getSecondSessionId());
    }
	
	public void testDummy() throws Exception {
		
	}
}