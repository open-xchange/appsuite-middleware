package com.openexchange.webdav.xml.task;

import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.xml.GroupUserTest;
import com.openexchange.webdav.xml.TaskTest;
import java.util.Date;

public class NewTest extends TaskTest {
	
	public void testNewTask() throws Exception {
		Task taskObj = createTask("testNewTask");
		insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
	}
	
	public void testNewTaskWithParticipants() throws Exception {
		Task taskObj = createTask("testNewTaskWithParticipants");
		
		int userParticipantId = GroupUserTest.getUserId(getWebConversation(), PROTOCOL + getHostName(), userParticipant2, getPassword());
		assertTrue("user participant not found", userParticipantId != -1);
		Group[] groupArray = GroupUserTest.searchGroup(webCon, groupParticipant, new Date(0), PROTOCOL + hostName, login, password);
		assertTrue("group array size is not > 0", groupArray.length > 0);
		int groupParticipantId = groupArray[0].getIdentifier();
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userParticipantId);
		participants[1] = new GroupParticipant();
		participants[1].setIdentifier(groupParticipantId);
		
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
	
	public void _notestNewTaskWithUsers() throws Exception {
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
}

