package com.openexchange.webdav.xml.task;

import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.GroupUserTest;
import com.openexchange.webdav.xml.TaskTest;
import com.openexchange.webdav.xml.XmlServlet;
import java.util.Date;

public class NewTest extends TaskTest {
	
	public NewTest(String name) {
		super(name);
	}
	
	public void testNewTask() throws Exception {
		Task taskObj = createTask("testNewTask");
		insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
	}
	
	public void testNewTaskWithParticipants() throws Exception {
		Task taskObj = createTask("testNewTaskWithParticipants");
		
		int userParticipantId2 = GroupUserTest.getUserId(getWebConversation(), PROTOCOL + getHostName(), userParticipant2, getPassword());
		assertTrue("user participant not found", userParticipantId2 != -1);
		int userParticipantId3 = GroupUserTest.getUserId(getWebConversation(), PROTOCOL + getHostName(), userParticipant3, getPassword());
		assertTrue("user participant not found", userParticipantId3 != -1);
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[1];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userParticipantId2);
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userParticipantId3);
		
		taskObj.setParticipants(participants);
		
		insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
	}
	
	public void testNewTaskWithAlarm() throws Exception {
		Task taskObj = createTask("testNewTaskWithAlarm");
		taskObj.setAlarm(new Date(startTime.getTime()-(2*dayInMillis)));
		int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		taskObj.setObjectID(objectId);
		Task loadTask = loadTask(getWebConversation(), objectId, taskFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
		compareObject(taskObj, loadTask);
		int[][] objectIdAndFolderId = { {objectId, taskFolderId } };
		deleteTask(getWebConversation(), objectIdAndFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}
	
	public void testNewTaskWithUsers() throws Exception {
		Task taskObj = createTask("testNewTaskWithUsers");
		
		int userParticipantId = GroupUserTest.getUserId(getWebConversation(), PROTOCOL + getHostName(), userParticipant2, getPassword());
		assertTrue("user participant not found", userParticipantId != -1);
		
		UserParticipant[] users = new UserParticipant[1];
		users[0] = new UserParticipant();
		users[0].setIdentifier(userParticipantId);
		users[0].setConfirm(CalendarObject.ACCEPT);
		
		taskObj.setUsers(users);
		
		insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
	}
	
	public void testTaskWithPrivateFlagInPublicFolder() throws Exception {
		FolderObject folderObj = new FolderObject();
		folderObj.setFolderName("testTaskWithPrivateFlagInPublicFolder" + System.currentTimeMillis());
		folderObj.setModule(FolderObject.TASK);
		folderObj.setType(FolderObject.PUBLIC);
		folderObj.setParentFolderID(2);
		
		OCLPermission[] permission = new OCLPermission[] { 
			FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION)
		};
		
		folderObj.setPermissionsAsArray( permission );
		
		final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword());
		
		Task taskObj = new Task();
		taskObj.setTitle("testTaskWithPrivateFlagInPublicFolder");
		taskObj.setPrivateFlag(true);
		taskObj.setParentFolderID(parentFolderId);

		try {
			int objectId = insertTask(getWebConversation(), taskObj, PROTOCOL + getHostName(), getLogin(), getPassword());
			deleteTask(getWebConversation(), objectId, parentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
			fail("conflict exception expected!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.USER_INPUT_STATUS);
		}
	}
}

