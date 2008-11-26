package com.openexchange.test;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.json.JSONException;
import org.xml.sax.SAXException;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;

public class TaskTestManager extends TestCase {
	protected List<Task> createdTasks;
	protected AJAXClient client;
	protected TimeZone timezone;
	protected int taskFolderId;
	
	public TaskTestManager(AJAXClient client) throws AjaxException, IOException, SAXException, JSONException{
		this.client = client;
		createdTasks = new LinkedList<Task>();
		taskFolderId = client.getValues().getPrivateTaskFolder();
		try {
			timezone = client.getValues().getTimeZone();
		} catch (AjaxException e) {
		} catch (IOException e) {
		} catch (SAXException e) {
		} catch (JSONException e) {
		} finally {
			if(timezone == null){
				timezone = TimeZone.getTimeZone("Europe/Berlin");
			}
		}
	}

	/**
	 * Creates a task via HTTP-API and updates it with new id, 
	 * timestamp and all other information that is updated after
	 * such requests.
	 * 
	 */
	public Task insertTaskOnServer(Task taskToCreate){
		createdTasks.add(taskToCreate);
		InsertRequest request = new InsertRequest(taskToCreate, timezone);
		InsertResponse response = null;
		try {
			response = client.execute(request);
		} catch (AjaxException e) {
			fail("AjaxException during task creation: "+e.getLocalizedMessage());
		} catch (IOException e) {
			fail("IOException during task creation: "+e.getLocalizedMessage());
		} catch (SAXException e) {
			fail("SAXException during task creation: "+e.getLocalizedMessage());
		} catch (JSONException e) {
			fail("JSONException during task creation: "+e.getLocalizedMessage());
		} 
		response.fillTask(taskToCreate);
		return taskToCreate;
	}
	
	public Task updateTaskOnServer(Task taskToUpdate){
		UpdateRequest request = new UpdateRequest(taskToUpdate, timezone);
		UpdateResponse response = null; 
		try {
			response = client.execute(request);
		} catch (AjaxException e) {
			fail("AjaxException during task update: "+e.getLocalizedMessage());
		} catch (IOException e) {
			fail("IOException during task update: "+e.getLocalizedMessage());
		} catch (SAXException e) {
			fail("SAXException during task update: "+e.getLocalizedMessage());
		} catch (JSONException e) {
			fail("JSONException during task update: "+e.getLocalizedMessage());
		}
		taskToUpdate.setLastModified( response.getTimestamp() );
		return taskToUpdate;
	}
	
	public Task moveTaskOnServer(Task taskToMove, int sourceFolder){
		UpdateRequest request = new UpdateRequest(sourceFolder, taskToMove, timezone);
		UpdateResponse response = null; 
		try {
			response = client.execute(request);
		} catch (AjaxException e) {
			fail("AjaxException during task update: "+e.getLocalizedMessage());
		} catch (IOException e) {
			fail("IOException during task update: "+e.getLocalizedMessage());
		} catch (SAXException e) {
			fail("SAXException during task update: "+e.getLocalizedMessage());
		} catch (JSONException e) {
			fail("JSONException during task update: "+e.getLocalizedMessage());
		}
		taskToMove.setLastModified( response.getTimestamp() );
		return taskToMove;
	}
	
	public void deleteTaskOnServer(Task taskToDelete){
		DeleteRequest request = new DeleteRequest(taskToDelete);
		try {
			client.execute(request);
		} catch (AjaxException e) {
			fail("AjaxException during deletion of task " + taskToDelete.getObjectID() +": "+e.getLocalizedMessage());
		} catch (IOException e) {
			fail("IOException during deletion of task " + taskToDelete.getObjectID() +": "+e.getLocalizedMessage());
		} catch (SAXException e) {
			fail("SAXException during deletion of task " + taskToDelete.getObjectID() +": "+e.getLocalizedMessage());
		} catch (JSONException e) {
			fail("JSONException during deletion of task " + taskToDelete.getObjectID() +": "+e.getLocalizedMessage());
		}
	}
	
	public Task getTaskFromServer(int folder, int objectId){
		GetRequest request = new GetRequest(folder, objectId);
		GetResponse response = null;
		try {
			response = client.execute(request);
			return response.getTask(timezone);
		} catch (AjaxException e) {
			fail("AjaxException while getting task with ID " + objectId + ": " + e.getLocalizedMessage());
		} catch (IOException e) {
			fail("IOException while getting task with ID " + objectId + ": " + e.getLocalizedMessage());
		} catch (SAXException e) {
			fail("SAXException while getting task with ID " + objectId + ": " + e.getLocalizedMessage());
		} catch (JSONException e) {
			fail("JSONException while getting task with ID " + objectId + ": " + e.getLocalizedMessage());
		} catch (OXJSONException e) {
			fail("OXJSONException while getting task with ID " + objectId + ": " + e.getLocalizedMessage());
		}
		return null; //should have failed before
	}

	public Task getTaskFromServer(Task task){
		return getTaskFromServer(task.getParentFolderID(), task.getObjectID());
	}
	
	public Task getAllTasksOnServer(){
		return null;
		
	}

	public Task listContactsOnServer(){
		return null;
		
	}

	public Task searchForTasksOnServer(){
		return null;
		
	}

	public Task getUpdatedTasksOnServer(){
		return null;
		
	}
	

	/**
	 * removes all tasks created by this fixture
	 */
	public void cleanUp(){
		for(Task task: createdTasks){
			deleteTaskOnServer(task);
		}
	}
	
	public static void assertCalendarObjectsHaveSameRecurrence(CalendarObject co1, CalendarObject co2){
		assertEquals("recurrenceCalculator should be equal" , co1.getRecurrenceCalculator(), co2.getRecurrenceCalculator());
		assertEquals("recurrenceCount should be equal" , co1.getRecurrenceCount(), co2.getRecurrenceCount());
		assertEquals("recurrenceCalculator should be equal" , co1.getRecurrenceID(), co2.getRecurrenceID());
		assertEquals("recurrencePosition should be equal" , co1.getRecurrencePosition(), co2.getRecurrencePosition());
		assertEquals("recurrenceType should be equal" , co1.getRecurrenceType(), co2.getRecurrenceType() );
		assertEquals("recurrenceDatePosition should be equal" , co1.getRecurrenceDatePosition(), co2.getRecurrenceDatePosition());
	}

	public static void assertCalendarObjectsAreAlmostEqual(CalendarObject expectedCalendarObject, CalendarObject comparedCalendarObject){
		assertEquals("'Almost equal' means titles should match", expectedCalendarObject.getTitle(), comparedCalendarObject.getTitle() );
		assertOXDateEquals("'Almost equal' means start date should match", expectedCalendarObject.getStartDate(), comparedCalendarObject.getStartDate() );
		assertOXDateEquals("'Almost equal' means end date should match", expectedCalendarObject.getEndDate(), comparedCalendarObject.getEndDate() );
		assertCalendarObjectsHaveSameRecurrence(expectedCalendarObject, comparedCalendarObject);
	}

	public static void assertTasksAreAlmostEqual(Task expectedTask, Task comparedTask){
		assertCalendarObjectsAreAlmostEqual(expectedTask, comparedTask);
	}
	
	/**
	 * Assures that both start and end date are in the future
	 * @param task
	 */
	public static void assertTaskIsInTheFuture(Task task){
		Date now = new Date();
		assertTrue("Start date not in the future", now.compareTo( task.getStartDate() ) < 0);
		assertTrue("End date not in the future", now.compareTo( task.getStartDate() ) < 0);
	}
	
	/**
	 * Assures that both start and end date are in the past
	 * @param task
	 */
	public static void assertTaskIsInThePast(Task task){
		Date now = new Date();
		assertTrue("Start date not in the past", now.compareTo( task.getStartDate() ) > 0);
		assertTrue("End date not in the past", now.compareTo( task.getStartDate() ) > 0);
	}
	
	/**
	 * Assures that the start date is in the past and end date is in the future
	 * @param task
	 */
	public static void assertTaskIsOngoing(Task task){
		Date now = new Date();
		assertTrue("Start date not in the past", now.compareTo( task.getStartDate() ) > 0);
		assertTrue("End date not in the future", now.compareTo( task.getStartDate() ) < 0);
	}
	
	public static void assertFirstDateOccursLaterThanSecond(Date firstDate, Date secondDate){
		assertTrue(firstDate.compareTo(secondDate) > 0);
	}

	public static void assertFirstDateOccursEarlierThanSecond(Date firstDate, Date secondDate){
		assertTrue(firstDate.compareTo(secondDate) < 0);
	}
	
	public static void assertOXDateEquals(String message, Date date1, Date date2){
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(date1);
		cal2.setTime(date2);
		cal1.set(Calendar.MILLISECOND,0);
		cal2.set(Calendar.MILLISECOND,0);
		assertEquals("Years hould be equal", cal1.get(Calendar.YEAR) , cal2.get(Calendar.YEAR));
		assertEquals("Months should be equal", cal1.get(Calendar.MONTH) , cal2.get(Calendar.MONTH));
		assertEquals("Days should be equal", cal1.get(Calendar.DAY_OF_MONTH) , cal2.get(Calendar.DAY_OF_MONTH));
		assertEquals("Hours should be equal", cal1.get(Calendar.HOUR_OF_DAY) , cal2.get(Calendar.HOUR_OF_DAY));
		assertEquals("Minutes should be equal", cal1.get(Calendar.MINUTE) , cal2.get(Calendar.MINUTE));
		assertEquals("Seconds should be equal", cal1.get(Calendar.SECOND) , cal2.get(Calendar.SECOND));
		assertEquals(message, cal1,cal2);
	}
	
	public static void assertOXDateEquals(Date date1, Date date2){
		assertOXDateEquals("After setting their millisecond part to zero, these two dates should be equal", date1, date2);
	}
	
	public static void assertDateInRecurrence(Date date, CalendarObject calendarObject){
		fail("NOT IMPLEMENTED");
	}

}
