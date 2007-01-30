package com.openexchange.webdav.xml.task;

import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.TaskTest;
import com.openexchange.webdav.xml.XmlServlet;
import java.util.Date;

public class DeleteTest extends TaskTest {
	
	public DeleteTest(String name) {
		super(name);
	}
	
	public void testDelete() throws Exception {
		Task taskObj = createTask("testDelete");
		int objectId1 = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		int objectId2 = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		
		int[][] objectIdAndFolderId = { { objectId1, taskFolderId }, { objectId2, taskFolderId } };
		
		deleteTask(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password);
	}
	
	public void testDeleteConcurentConflict() throws Exception {
		Task appointmentObj = createTask("testUpdateTaskConcurentConflict");
		int objectId = insertTask(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		try {
			deleteTask(webCon, objectId, taskFolderId, new Date(0), PROTOCOL + hostName, login, password );
			fail("expected concurent modification exception!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.MODIFICATION_STATUS);
		}
		
		deleteTask(webCon, objectId, taskFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void testDeleteNotFound() throws Exception {
		Task appointmentObj = createTask("testUpdateTaskNotFound");
		int objectId = insertTask(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		try {
			deleteTask(webCon, (objectId + 1000), taskFolderId, PROTOCOL + hostName, login, password );
			fail("expected object not found exception!");
		} catch (TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.OBJECT_NOT_FOUND_STATUS);
		}
		
		deleteTask(webCon, objectId, taskFolderId, PROTOCOL + hostName, login, password );
	}
	
}

