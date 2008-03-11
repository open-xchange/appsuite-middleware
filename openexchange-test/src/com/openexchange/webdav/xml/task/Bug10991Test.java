package com.openexchange.webdav.xml.task;

import java.io.ByteArrayInputStream;

import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.xml.AttachmentTest;
import com.openexchange.webdav.xml.TaskTest;

public class Bug10991Test extends TaskTest {
	
	public Bug10991Test(String name) {
		super(name);
	}
	
	public void testBug10991() throws Exception {
		Task taskObj = createTask("testBug10991");
		final int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		taskObj.setObjectID(objectId);
		
		final AttachmentMetadata attachmentMeta = new AttachmentImpl();
		attachmentMeta.setAttachedId(objectId);
		attachmentMeta.setFolderId(taskFolderId);
		attachmentMeta.setFileMIMEType("text/plain");
		attachmentMeta.setModuleId(Types.TASK);
		attachmentMeta.setFilename("test.txt");
		
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("test".getBytes());
		AttachmentTest.insertAttachment(webCon, attachmentMeta, byteArrayInputStream, getHostName(), getLogin(), getPassword());
		
		
		
		final Task loadTask = loadTask(getWebConversation(), objectId, taskFolderId, getHostName(), getLogin(), getPassword());
		final Task[] taskArray = listTask(getWebConversation(), taskFolderId, loadTask.getLastModified(), true, false, getHostName(), getLogin(), getPassword());
		
		boolean found = false;
		for (int a = 0; a < taskArray.length; a++) {
			if (taskArray[a].getObjectID() == objectId) {
				compareObject(taskObj, taskArray[a]);
				found = true;
			}
		}
		
		assertTrue("task not found" , found);
	}
}

