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
		
		ContactObject[] contactArray = GroupUserTest.searchUser(webCon, userParticipant2, new Date(0), PROTOCOL + hostName, login, password);
		assertTrue("contact array size is not > 0", contactArray.length > 0);
		int userParticipantId = contactArray[0].getInternalUserId();
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
	
	public void _notestNewTaskWithUsers() throws Exception {
		Task taskObj = createTask("testNewTaskWithUsers");
		
		UserParticipant[] users = new UserParticipant[1];
		users[0] = new UserParticipant();
		users[0].setIdentifier(userId);
		users[0].setConfirm(CalendarObject.ACCEPT);
		
		taskObj.setUsers(users);
		
		insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
	}
}

