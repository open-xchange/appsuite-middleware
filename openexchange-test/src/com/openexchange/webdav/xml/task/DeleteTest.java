package com.openexchange.webdav.xml.task;

import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.xml.TaskTest;

public class DeleteTest extends TaskTest {
	
	public void testDelete() throws Exception {
		Task taskObj = createTask("testDelete");
		int objectId1 = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		int objectId2 = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		
		int[][] objectIdAndFolderId = { { objectId1, taskFolderId }, { objectId2, taskFolderId } };
		
		deleteTask(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password);
	}
	
}

