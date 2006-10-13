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
	
	public void _notestUpdateTaskWithParticipants() throws Exception {
		Task taskObj = createTask("testUpdateTask");
		int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		
		taskObj = createTask("testUpdateTask");
		
		int userParticipantId = GroupUserTest.getUserId(getWebConversation(), PROTOCOL + getHostName(), userParticipant3, getPassword());
		assertTrue("user participant not found", userParticipantId != -1);
		Group[] groupArray = GroupUserTest.searchGroup(webCon, groupParticipant, new Date(0), PROTOCOL + hostName, login, password);
		assertTrue("group array size is not > 0", groupArray.length > 0);
		int groupParticipantId = groupArray[0].getIdentifier();
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[3];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userParticipantId);
		participants[1] = new GroupParticipant();
		participants[1].setIdentifier(groupParticipantId);
		
		taskObj.setParticipants(participants);
		
		updateTask(webCon, taskObj, objectId, taskFolderId, PROTOCOL + hostName, login, password);
	}
}

