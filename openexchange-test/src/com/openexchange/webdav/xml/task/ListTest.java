package com.openexchange.webdav.xml.task;

import java.util.Date;

import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.TestException;
import com.openexchange.webdav.xml.TaskTest;
import com.openexchange.webdav.xml.XmlServlet;

public class ListTest extends TaskTest {
	
	public ListTest(final String name) {
		super(name);
	}
	
	public void testPropFindWithModified() throws Exception {
		final Task taskObj = createTask("testPropFindWithModified");
		final int objectId1 = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		final int objectId2 = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);

		// prevent master/slave problem
		Thread.sleep(1000);
		
		final Task loadTask = loadTask(getWebConversation(), objectId1, taskFolderId, getHostName(), getLogin(), getPassword());
		final Date modified = loadTask.getLastModified();
		
		final Task[] taskArray = listTask(webCon, taskFolderId, modified, true, false, PROTOCOL + hostName, login, password);
		
		assertTrue("check response", taskArray.length >= 2);
	}
	
	public void testPropFindWithDelete() throws Exception {
		final Task taskObj = createTask("testPropFindWithModified");
		final int objectId1 = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		final int objectId2 = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		
		// prevent master/slave problem
		Thread.sleep(1000);
		
		final Task loadTask = loadTask(getWebConversation(), objectId1, taskFolderId, getHostName(), getLogin(), getPassword());
		final Date modified = loadTask.getLastModified();
		
		final int[][] objectIdAndFolderId = { { objectId1, taskFolderId }, { objectId2, taskFolderId } };
		
		deleteTask(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password);
		
		final Task[] taskArray = listTask(webCon, taskFolderId, modified, false, true, PROTOCOL + hostName, login, password);
		
		assertTrue("wrong response array length", taskArray.length >= 2);
	}
	
	public void testPropFindWithObjectId() throws Exception {
		final Task taskObj = createTask("testPropFindWithObjectId");
		final int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		
		final Task loadTask = loadTask(webCon, objectId, taskFolderId, PROTOCOL + hostName, login, password);
	}
	
	public void testObjectNotFound() throws Exception {
		final Task taskObj = createTask("testObjectNotFound");
		final int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		
		try {
			final Task loadTask = loadTask(webCon, (objectId+1000), taskFolderId, PROTOCOL + hostName, login, password);
			fail("object not found exception expected!");
		} catch (final TestException exc) {
			assertExceptionMessage(exc.getMessage(), XmlServlet.OBJECT_NOT_FOUND_STATUS);
		}
		
		final int[][] objectIdAndFolderId = { { objectId ,taskFolderId } };
		deleteTask(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void testListWithAllFields() throws Exception {
		final Task taskObj = new Task();
		taskObj.setTitle("testListWithAllFields");
		taskObj.setStartDate(startTime);
		taskObj.setEndDate(endTime);
		taskObj.setParentFolderID(taskFolderId);
		taskObj.setPrivateFlag(true);
		taskObj.setLabel(2);
		taskObj.setNote("note");
		taskObj.setCategories("testcat1,testcat2,testcat3");
		taskObj.setActualCosts(1.5F);
		taskObj.setActualDuration(210);
		taskObj.setBillingInformation("billing information");
		taskObj.setCompanies("companies");
		taskObj.setCurrency("currency");
		taskObj.setDateCompleted(dateCompleted);
		taskObj.setPercentComplete(50);
		taskObj.setPriority(Task.HIGH);
		taskObj.setStatus(Task.IN_PROGRESS);
		taskObj.setTargetCosts(5.5F);
		taskObj.setTargetDuration(450);
		taskObj.setTripMeter("trip meter");
		
		final int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		
		// prevent master/slave problem
		Thread.sleep(1000);
		
		Task loadTask = loadTask(getWebConversation(), objectId, taskFolderId, getHostName(), getLogin(), getPassword());
		final Date modified = loadTask.getLastModified();
		
		final Task[] taskArray = listTask(webCon, taskFolderId, modified, true, false, PROTOCOL + hostName, login, password);
		
		assertEquals("wrong response array length", 1, taskArray.length);
		
		loadTask = taskArray[0];
		
		taskObj.setObjectID(objectId);
		taskObj.setStartDate(startTime);
		taskObj.setEndDate(endTime);
		compareObject(taskObj, loadTask);
	}
	
	public void testListWithAllFieldsOnUpdate() throws Exception {
		Task taskObj = createTask("testListWithAllFieldsOnUpdate");
		final int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		
		Task loadTask = loadTask(getWebConversation(), objectId, taskFolderId, getHostName(), getLogin(), getPassword());
		final Date modified = loadTask.getLastModified();
		
		taskObj = new Task();
		taskObj.setTitle("testListWithAllFieldsOnUpdate");
		taskObj.setStartDate(startTime);
		taskObj.setEndDate(endTime);
		taskObj.setPrivateFlag(true);
		taskObj.setLabel(2);
		taskObj.setNote("note");
		taskObj.setCategories("testcat1,testcat2,testcat3");
		taskObj.setActualCosts(1.5F);
		taskObj.setActualDuration(210);
		taskObj.setBillingInformation("billing information");
		taskObj.setCompanies("companies");
		taskObj.setCurrency("currency");
		taskObj.setDateCompleted(dateCompleted);
		taskObj.setPercentComplete(50);
		taskObj.setPriority(Task.HIGH);
		taskObj.setStatus(Task.IN_PROGRESS);
		taskObj.setTargetCosts(5.5F);
		taskObj.setTargetDuration(450);
		taskObj.setTripMeter("trip meter");
		taskObj.setParentFolderID(taskFolderId);
		
		updateTask(webCon, taskObj, objectId, taskFolderId, PROTOCOL + hostName, login, password);
		
		final Task[] taskArray = listTask(webCon, taskFolderId, modified, true, false, PROTOCOL + hostName, login, password);
		
		assertEquals("wrong response array length", 1, taskArray.length);
		
		loadTask = taskArray[0];
		
		taskObj.setObjectID(objectId);
		taskObj.setStartDate(startTime);
		taskObj.setEndDate(endTime);
		taskObj.setParentFolderID(taskFolderId);
		compareObject(taskObj, loadTask);
	}
	
	public void testList() throws Exception {
		final Task taskObj = createTask("testList");
		final int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password);
		
		final int[] idArray = listTask(getWebConversation(), taskFolderId, getHostName(), getLogin(), getPassword());
		
		boolean found = false;
		for (int a = 0; a < idArray.length; a++) {
			if (idArray[a] == objectId) {
				found = true;
				break;
			}
		}
		
		assertTrue("id " + objectId + " not found in response", found);
		deleteTask(getWebConversation(), objectId, taskFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
	}	
}

