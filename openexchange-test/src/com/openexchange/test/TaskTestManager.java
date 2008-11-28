package com.openexchange.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Mapping;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.Mapping.Mapper;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;

public class TaskTestManager extends TestCase {
	protected List<Task> createdEntities;
	protected AJAXClient client;
	protected TimeZone timezone;
	protected int taskFolderId;
	
	public TaskTestManager(AJAXClient client){
		this.client = client;
		createdEntities = new LinkedList<Task>();
		try {
			taskFolderId = client.getValues().getPrivateTaskFolder();
		} catch (AjaxException e) {
			fail("AjaxException during task creation: "+e.getLocalizedMessage());
		} catch (IOException e) {
			fail("IOException during task creation: "+e.getLocalizedMessage());
		} catch (SAXException e) {
			fail("SAXException during task creation: "+e.getLocalizedMessage());
		} catch (JSONException e) {
			fail("JSONException during task creation: "+e.getLocalizedMessage());
		} 
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
		createdEntities.add(taskToCreate);
		InsertRequest request = new InsertRequest(taskToCreate, timezone);
		InsertResponse response = null;
		try {
			response = client.execute(request);
			response.fillTask(taskToCreate);
		} catch (AjaxException e) {
			fail("AjaxException during task creation: "+e.getLocalizedMessage());
		} catch (IOException e) {
			fail("IOException during task creation: "+e.getLocalizedMessage());
		} catch (SAXException e) {
			fail("SAXException during task creation: "+e.getLocalizedMessage());
		} catch (JSONException e) {
			fail("JSONException during task creation: "+e.getLocalizedMessage());
		} 

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
		for(CalendarObject co: createdEntities){
			if(taskToUpdate.getObjectID() == co.getObjectID()){
				co.setLastModified( response.getTimestamp());
				continue;
			}
		}
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
		System.out.println("delete: " + taskToDelete.getLastModified().getTime());
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
	
	/**
	 * Performs an AllRequest for all columns on the server and returns
	 * the tasks in a requested folder.
	 * @param folderID
	 * @return
	 */
	public Task[] getAllTasksOnServer(int folderID){
		AllRequest allTasksRequest = new AllRequest(folderID, Task.ALL_COLUMNS, Task.OBJECT_ID, Order.ASCENDING);
		try {
			CommonAllResponse allTasksResponse = client.execute(allTasksRequest);
			JSONArray jsonTasks = (JSONArray) allTasksResponse.getData();
			List<Task> tasks = new LinkedList<Task>();
			for(int j = 0; j < jsonTasks.length(); j++){
				JSONArray taskAsArray = (JSONArray) jsonTasks.get(j);
				Task task = transformAllRequestArrayToTask(taskAsArray);
				tasks.add(task);
			}
			return tasks.toArray(new Task[tasks.size()]);
			
		} catch (AjaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;	
	}
	
	/**
	 * Transforms a value object into whatever is required for that column,
	 * e.g. a date object for the column start_date.
	 * @param column
	 * @param value
	 * @return
	 */
	protected static Object transformColumn(int column, Object value){
		if(column == Task.START_DATE 				
			|| column == Task.END_DATE
			|| column == Task.UNTIL
			|| column == AppointmentObject.RECURRENCE_DATE_POSITION 
			|| column == AppointmentObject.CREATION_DATE
			|| column == AppointmentObject.LAST_MODIFIED){
			return new Date( (Long) value );
		}
		return value;
	}

	/**
	 * An AllRequest answers with a JSONArray of JSONArray, each of which
	 * contains a field belonging to a task. This method assembles a task
	 * from this array.
	 * @return
	 * @throws JSONException 
	 */
	protected static Task transformAllRequestArrayToTask(JSONArray taskAsArray) throws JSONException{
		Task resultingTask = new Task();
		
		for(int i = 0; i < Task.ALL_COLUMNS.length; i++){
			int column = Task.ALL_COLUMNS[i];
			Mapper attributeMapping = Mapping.getMapping(column);
			if( taskAsArray.isNull(i) || attributeMapping == null || taskAsArray.get(i) == null)
				continue;
			
			Object newValue = transformColumn(column, taskAsArray.get(i));
			attributeMapping.set(resultingTask, newValue);
		}
		
		return resultingTask;
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
		for(Task task: createdEntities){
			deleteTaskOnServer(task);
		}
	}
	
	/**
	 * Finds a task within a list of tasks.
	 * Fails if not found and returns null;
	 * @param tasks
	 * @return
	 */
	public Task findTaskByID(int id, List<Task> tasks){
		for(Task task: tasks){
			if(id == task.getObjectID())
				return task;
		}
		fail("Task with id="+id+" not found");
		return null;
	}
	
	/**
	 * Finds a task within an array of tasks.
	 * Fails if not found and returns null;
	 * @param tasks
	 * @return
	 */
	public Task findTaskByID(int id, Task[] tasks){
		return findTaskByID(id, Arrays.asList(tasks));
	}

}
