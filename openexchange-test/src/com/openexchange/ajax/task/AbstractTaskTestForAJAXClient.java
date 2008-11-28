package com.openexchange.ajax.task;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;

import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TestTask;
import com.openexchange.test.TaskTestManager;

public abstract class AbstractTaskTestForAJAXClient extends AbstractAJAXSession {

	public AbstractTaskTestForAJAXClient(String name) {
		super(name);
	}

	public TestTask getNewTask() {
		return getNewTask("Default task, created by " + this.getClass().getName());
	}

	public TestTask getNewTask(String title) {
		TestTask task = new TestTask();
		task.setTitle(title);
		UserValues values = getClient().getValues();
		try {
			task.setTimezone(values.getTimeZone());
			task.setParentFolderID(values.getPrivateTaskFolder());
			task.setCreatedBy(values.getUserId());
			task.setModifiedBy(values.getUserId());
		} catch (Exception e) {
			fail("Setup failed, could not get necessary values for timezone or private folder");
		}
		return task;
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}
	
	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Does an insert and compares data from both get and all request
	 */
	public void runSimpleInsertTest(Task task) {
		TaskTestManager testManager = null;
		try {
			testManager = new TaskTestManager(getClient());
		} catch (Exception e) {
			fail("Setup failed, TestManager could not be instantiated");
		}
		testManager.insertTaskOnServer(task);
		Task resultingTask = testManager.getTaskFromServer(task);
		TaskTestManager.assertAllTaskFieldsMatchExcept(task, resultingTask, new HashSet<Integer>());
		testManager.cleanUp();
	}

	/**
	 * Does an insert and an update and compares data from both get and all request
	 * @param insertTask Task to insert at first
	 * @param updateTask Task used to update insertTask - this tasks gets changed to have the correct LAST_MODIFIED, PARENT_FOLDER and OBJECT_ID, otherwise the update wouldn't work at all
	 * @param fieldsThatChange Fields that are expected to change. These are not checked for being equal but for being changed - they are not ignored. The following fields are always ignored: CREATION_DATE, LAST_MODIFIED 
	 */
	public void runInsertAndUpdateTest(Task insertTask, Task updateTask, int... fieldsThatChange) {
		Set<Integer> changingFields = new HashSet<Integer>();
		for(int field: fieldsThatChange){
			changingFields.add(Integer.valueOf(field));
		}
		changingFields.add( Integer.valueOf( Task.CREATION_DATE ) );
		changingFields.add( Integer.valueOf( Task.LAST_MODIFIED ) ); //must be different afterwards
		
		TaskTestManager testManager = new TaskTestManager(getClient());
		testManager.insertTaskOnServer(insertTask);
		
		updateTask.setLastModified(insertTask.getLastModified());
		updateTask.setParentFolderID(insertTask.getParentFolderID());
		updateTask.setObjectID(insertTask.getObjectID());
		testManager.updateTaskOnServer(updateTask);
		
		Task getResult = testManager.getTaskFromServer(insertTask);
		TaskTestManager.assertAllTaskFieldsMatchExcept(insertTask, getResult, changingFields);
		TaskTestManager.assertTaskFieldsDiffer(insertTask, getResult, changingFields);
		
		Task[] allResults = testManager.getAllTasksOnServer(insertTask.getParentFolderID());
		Task allResult = testManager.findTaskByID(insertTask.getObjectID(), allResults);
		TaskTestManager.assertAllTaskFieldsMatchExcept(insertTask, allResult, changingFields);
		TaskTestManager.assertTaskFieldsDiffer(insertTask, getResult, changingFields);
		
		testManager.cleanUp();
	}

}