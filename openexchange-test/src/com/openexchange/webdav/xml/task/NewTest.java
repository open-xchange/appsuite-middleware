package com.openexchange.webdav.xml.task;

import java.util.Date;

import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TaskException;
import com.openexchange.groupware.tasks.TaskException.Code;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.GroupUserTest;
import com.openexchange.webdav.xml.TaskTest;
import com.openexchange.webdav.xml.XmlServlet;

public class NewTest extends TaskTest {
	
	public NewTest(final String name) {
		super(name);
	}
	
	public void testNewTask() throws Exception {
		final Task taskObj = createTask("testNewTask");
		insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
	}
	
	public void testNewTaskWithParticipants() throws Exception {
		final Task taskObj = createTask("testNewTaskWithParticipants");
		
		final int userParticipantId2 = GroupUserTest.getUserId(getWebConversation(), PROTOCOL + getHostName(), userParticipant2, getPassword());
		assertTrue("user participant not found", userParticipantId2 != -1);
		final int userParticipantId3 = GroupUserTest.getUserId(getWebConversation(), PROTOCOL + getHostName(), userParticipant3, getPassword());
		assertTrue("user participant not found", userParticipantId3 != -1);
		
		final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[1];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userParticipantId2);
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userParticipantId3);
		
		taskObj.setParticipants(participants);
		
		insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
	}
	
	public void testNewTaskWithAlarm() throws Exception {
		final Task taskObj = createTask("testNewTaskWithAlarm");
		taskObj.setAlarm(new Date(startTime.getTime()-(2*dayInMillis)));
		final int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		taskObj.setObjectID(objectId);
		final Task loadTask = loadTask(getWebConversation(), objectId, taskFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(taskObj, loadTask);
		final int[][] objectIdAndFolderId = { {objectId, taskFolderId } };
		deleteTask(getWebConversation(), objectIdAndFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testNewTaskWithUsers() throws Exception {
		final Task taskObj = createTask("testNewTaskWithUsers");
		
		final int userParticipantId = GroupUserTest.getUserId(getWebConversation(), PROTOCOL + getHostName(), userParticipant2, getPassword());
		assertTrue("user participant not found", userParticipantId != -1);
		
		final UserParticipant[] users = new UserParticipant[1];
		users[0] = new UserParticipant();
		users[0].setIdentifier(userParticipantId);
		users[0].setConfirm(CalendarObject.ACCEPT);
		
		taskObj.setUsers(users);
		
		insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
	}
	
	public void testTaskWithPrivateFlagInPublicFolder() throws Exception {
		final FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testTaskWithPrivateFlagInPublicFolder" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.TASK);
		folderObj.setType(FolderObject.PUBLIC);
		folderObj.setParentFolderID(2);
		
		final OCLPermission[] permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		final Task taskObj = new Task();
		taskObj.setTitle("testTaskWithPrivateFlagInPublicFolder");
		taskObj.setPrivateFlag(true);
		taskObj.setParentFolderID(parentFolderId);

		try {
			final int objectId = insertTask(getWebConversation(), taskObj, PROTOCOL + getHostName(), getLogin(), getPassword());
			deleteTask(getWebConversation(), objectId, parentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
			fail("conflict exception expected!");
		} catch (final TestException exc) {
			assertExceptionMessage(exc.getMessage(), new TaskException(Code.PRIVATE_FLAG).getErrorCode());
		}
	}
}

