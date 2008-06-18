package com.openexchange.webdav.xml.task;

import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.TaskTest;

public class ConfirmTest extends TaskTest {
	
	public ConfirmTest(final String name) {
		super(name);
	}
	
	public void testDummy() {
		
	}
	
	public void testConfirm() throws Exception {
		final FolderObject sharedFolderObject = FolderTest.getTaskDefaultFolder(getSecondWebConversation(), PROTOCOL + getHostName(), getSecondLogin(), getPassword());
		final int secondUserId = sharedFolderObject.getCreatedBy();
		
        final Task TaskObj = createTask("testConfirm");
		UserParticipant[] participants = new UserParticipant[1];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(secondUserId);
		
		TaskObj.setParticipants(participants);

        final int objectId = insertTask(getWebConversation(), TaskObj, PROTOCOL + getHostName(), getLogin(), getPassword());

        confirmTask(getSecondWebConversation(), objectId, Task.ACCEPT, null, PROTOCOL + getHostName(), getSecondLogin(), getPassword());
		
		final Task loadTask = loadTask(getWebConversation(), objectId, taskFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		boolean found = false;
		
		participants = loadTask.getUsers();
		for (int a = 0; a < participants.length; a++) {
			if (participants[a].getIdentifier() == secondUserId) {
				found = true;
				assertEquals("wrong confirm status", Task.ACCEPT, participants[a].getConfirm());
			}
		}
		
		assertTrue("user participant with id " + secondUserId + " not found", found);
		
        deleteTask(getWebConversation(), new int[][] { { objectId, taskFolderId } }, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
}

