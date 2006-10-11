package com.openexchange.webdav.xml.task;

import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.groupware.ldap.Resource;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.xml.GroupUserTest;
import com.openexchange.webdav.xml.TaskTest;
import java.util.Date;

public class UpdateTest extends TaskTest {
	
	public void testUpdateTask() throws Exception {
		Task taskObj = createTask("testUpdateTask");
		int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		
		taskObj = createTask("testUpdateTask2");
		taskObj.setNote(null);
		
		updateTask(webCon, taskObj, objectId, taskFolderId, PROTOCOL + hostName, login, password);
	}
	
	public void testUpdateTaskWithParticipants() throws Exception {
		Task taskObj = createTask("testUpdateTask");
		int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		
		taskObj = createTask("testUpdateTask");
		
		ContactObject[] contactArray = GroupUserTest.searchUser(webCon, userParticipant3, new Date(0), PROTOCOL + hostName, login, password);
		assertTrue("contact array size is not > 0", contactArray.length > 0);
		int userParticipantId = contactArray[0].getInternalUserId();
		Group[] groupArray = GroupUserTest.searchGroup(webCon, "*", new Date(0), PROTOCOL + hostName, login, password);
		assertTrue("group array size is not > 0", groupArray.length > 0);
		int groupParticipantId = groupArray[0].getIdentifier();
		Resource[] resourceArray = GroupUserTest.searchResource(webCon, "*", new Date(0), PROTOCOL + hostName, login, password);
		assertTrue("resource array size is not > 0", resourceArray.length > 0);
		int resourceParticipantId = resourceArray[0].getIdentifier();
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[4];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId);
		participants[3] = new ResourceParticipant();
		participants[3].setIdentifier(resourceParticipantId);
		
		taskObj.setParticipants(participants);
		
		updateTask(webCon, taskObj, objectId, taskFolderId, PROTOCOL + hostName, login, password);
	}
}

