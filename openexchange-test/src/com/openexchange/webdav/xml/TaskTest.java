package com.openexchange.webdav.xml;

import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Task;
import java.util.Date;
import org.jdom.Element;

public class TaskTest extends AbstractWebdavTest {
	
	protected int userParticipantId2 = -1;
	
	protected int userParticipantId3 = -1;
	
	protected int groupParticipantId1 = -1;
	
	protected int taskFolderId = -1;
	/*
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testNewTask() throws Exception {
		Task taskObj = createTask("testNewTask");
		saveTask(taskObj);
	}
	
	public void testNewTaskWithParticipants() throws Exception {
		Task taskObj = createTask("testNewTask");
				
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[3];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[1] = new UserParticipant();
		participants[1].setIdentifier(userParticipantId2);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId1);
		
		taskObj.setParticipants(participants);
		
		saveTask(taskObj);
	}
	
	public void testUpdateTask() throws Exception {
		Task taskObj = createTask("testUpdateTask");
		int objectId = saveTask(taskObj);
		
		taskObj = createTask("testUpdateTask");
		taskObj.setObjectID(objectId);
		
		saveTask(taskObj);
	}
	
	public void testUpdateTaskWithParticipants() throws Exception {
		Task taskObj = createTask("testUpdateTask");
		int objectId = saveTask(taskObj);
		
		taskObj = createTask("testUpdateTask");
		taskObj.setObjectID(objectId);
		
		com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[3];
		participants[0] = new UserParticipant();
		participants[0].setIdentifier(userId);
		participants[2] = new GroupParticipant();
		participants[2].setIdentifier(groupParticipantId1);
		
		taskObj.setParticipants(participants);
		
		saveTask(taskObj);
	}

	public void testDelete() throws Exception {
		Task taskObj = createTask("testDelete");
		int objectId = saveTask(taskObj);
		
		taskObj = new Task();
		taskObj.setObjectID(objectId);
		deleteObject(taskObj, taskFolderId);
	}
	
	public void testPropFind() throws Exception {
		listObjects(taskFolderId, new Date(0), false);
	}

	public void testPropFindWithDelete() throws Exception {
		listObjects(taskFolderId, new Date(0), false);
	}
	
	public void testPropFindWithObjectId() throws Exception {
		Task taskObj = createTask("testPropFindWithObjectId");
		int objectId = saveTask(taskObj);
		
		loadObject(objectId);
	}
	
	public void testConfirm() throws Exception {
		Task taskObj = createTask("testConfirm");
				
		int objectId = saveTask(taskObj);
		
		// confirmObject(objectId);
	}
	
	protected int saveTask(Task taskObj) throws Exception {
		TaskWriter taskWriter = new TaskWriter(sessionObj);
		Element e_prop = new Element("prop", webdav);
		taskWriter.addContent2PropElement(e_prop, taskObj, false);
		//byte[] b = writeRequest(e_prop);
		return sendPut(new byte[0]);
	}
	
	private Task createTask(String title) throws Exception {
		Task taskObj = new Task();
		taskObj.setTitle(title);
		taskObj.setStartDate(new Date());
		taskObj.setEndDate(new Date());
		taskObj.setStatus(Task.IN_PROGRESS);
		taskObj.setPercentComplete(50);
		taskObj.setParentFolderID(taskFolderId);
		
		return taskObj;
	}
	 */
}

