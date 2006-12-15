package com.openexchange.webdav.xml.appointment;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;

public class ConfirmTest extends AppointmentTest {
	
	public ConfirmTest(String name) {
		super(name);
	}

	public void setUp() throws Exception {
		super.setUp();
	}
	
	public void testDummy() {
		
	}
	
    public void _notestConfirm() throws Exception {
		final FolderObject sharedFolderObject = FolderTest.getAppointmentDefaultFolder(getSecondWebConversation(), PROTOCOL + getHostName(), getSecondLogin(), getPassword());
		final int secondUserId = sharedFolderObject.getCreatedBy();
		
        AppointmentObject appointmentObj = createAppointmentObject("testConfirm");
		appointmentObj.setIgnoreConflicts(true);
		UserParticipant[] users = new UserParticipant[2];
		users[0] = new UserParticipant();
		users[0].setIdentifier(userId);
		users[1] = new UserParticipant();
		users[1].setIdentifier(secondUserId);
		
		appointmentObj.setUsers(users);

        int objectId = insertAppointment(getWebConversation(), appointmentObj, PROTOCOL + getHostName(), getLogin(), getPassword());
        
        confirmAppointment(getSecondWebConversation(), objectId, AppointmentObject.ACCEPT, null, PROTOCOL + getHostName(), getSecondLogin(), getPassword());
		
		AppointmentObject loadAppointment = loadAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		boolean found = false;
		
		users = loadAppointment.getUsers();
		for (int a = 0; a < users.length; a++) {
			if (users[a].getIdentifier() == secondUserId) {
				found = true;
				assertEquals("wrong confirm status", AppointmentObject.ACCEPT, users[a].getConfirm());
			}
		}
		
		assertTrue("user participant with id " + secondUserId + " not found", found);
		
        deleteAppointment(getWebConversation(), new int[][] { { objectId, appointmentFolderId } }, PROTOCOL + getHostName(), getLogin(), getPassword());
    }
}

