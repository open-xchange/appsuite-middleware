package com.openexchange.ajax.task;

import org.junit.Before;
import org.junit.Test;

import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TestTask;
import com.openexchange.test.TaskTestManager;

public class TaskRecurrenceTest extends AbstractAJAXSession {
	
	/*
	 * SETUP GOES HERE
	 */
	public TestTask getNewTask(){
		return getNewTask("Default task, created by " + this.getClass().getName());
	}
	
	public TestTask getNewTask(String title){
		TestTask task = new TestTask();
		task.setTitle(title);
		UserValues values = getClient().getValues();
		try {
			task.setTimezone(values.getTimeZone());
			task.setParentFolderID(values.getPrivateTaskFolder());
		} catch (Exception e) {
			fail("Setup failed, could not get necessary values for timezone or private folder");
		}
		return task;
	}
	
	public TaskRecurrenceTest(String name) {
		super(name);
	}

	@Before public void setUp() throws Exception{
		super.setUp();
	}
	
	/*
	 * TESTS GO HERE
	 */
	
	/* unlimited recurrences */
	@Test public void testInsertDailyRecurrenceTask(){
		TestTask todaysTask = getNewTask("This I'll do every day from now on. Promised.")
			.startsToday()
			.everyDay()
			.doSanityCheck(TestTask.SANITY_DATE);
		runSimpleInsertTest(todaysTask);
	}

	@Test public void testInsertAndUpdateDailyRecurrenceTask(){
		TestTask todaysTask = getNewTask("This I'll do every day from now on. Promised.")
			.startsToday()
			.everyDay()
			.doSanityCheck(TestTask.SANITY_DATE);
		TestTask tomorrowsTask = getNewTask("This I'll do every day from now on. Well, maybe tomorrow.")
			.startsToday()
			.everyDay()
			.doSanityCheck(TestTask.SANITY_DATE)
			.makeRelatedTo(todaysTask);
		runInsertAndUpdateTest(todaysTask, tomorrowsTask);
	}

	@Test public void testWeeklyRecurrence(){
		TestTask tomorrowsTask = getNewTask("This, I'll do every week from now. Well, from tomorrow. Maybe.")
			.startsTomorrow()
			.everyWeek()
			.doSanityCheck(TestTask.SANITY_DATE);
		runSimpleInsertTest(tomorrowsTask);
	}
	
	@Test public void testWeeklyRecurrence2(){
		Task trainingDays = getNewTask("Training days")
			.startsAtNoon()
			.endInTheEvening()
			.everyWeekOn( Task.TUESDAY, Task.THURSDAY, Task.SATURDAY )
			.doSanityCheck(TestTask.SANITY_DATE);
		runSimpleInsertTest(trainingDays);
	}
	
	@Test public void testMonthlyRecurrence(){
		Task myAAMeeting = getNewTask("my AA meeting")
			.everyMonthOnNthWeekday(1, Task.MONDAY)
			.everyOtherMonth()
			.doSanityCheck(TestTask.SANITY_DATE);
		runSimpleInsertTest(myAAMeeting);
	}

	@Test public void testMonthlyRecurrence2(){
		Task myAAMeeting = getNewTask("my other AA group meeting")
			.everyMonth()
			.onDay(6)
			.doSanityCheck(TestTask.SANITY_DATE);
		runSimpleInsertTest(myAAMeeting);
	}
	
	@Test public void testYearlyRecurrence(){
		Task organizeSilvesterParty = getNewTask("Have to organize Silvester party")
			.everyYear()
			.onDay(30)
			.inMonth(12)
			.doSanityCheck(TestTask.SANITY_DATE);
		runSimpleInsertTest(organizeSilvesterParty);
	}
	
	@Test public void testYearlyRecurrence2(){
		Task breakNewYearsVows = getNewTask("Forget New Year's vows now")
			.everyYear()
			.inMonth(1)
			.onWeekDays(Task.MONDAY)
			.doSanityCheck(TestTask.SANITY_DATE);
		runSimpleInsertTest(breakNewYearsVows);
	}
	
	/* limited recurrences */
	
	/*
	 * TEST LOGIC GOES HERE
	 */
	public void runSimpleInsertTest(Task task){
		TaskTestManager fixture = null;
		try {
			fixture = new TaskTestManager(getClient());
		} catch (Exception e) {
			fail("Setup failed, TestManager could not be instantiated");
		}
		fixture.insertTaskOnServer(task);
		Task resultingTask =fixture.getTaskFromServer(task);
		TaskTestManager.assertTasksAreAlmostEqual(task, resultingTask);
		fixture.cleanUp();
	}
	
	public void runInsertAndUpdateTest(Task insertTask, Task updateTask, int... fieldsThatChange){
		TaskTestManager fixture = null;
		try {
			fixture = new TaskTestManager(getClient());
		} catch (Exception e) {
			fail("Setup failed, TestManager could not be instantiated");
		}
		fixture.insertTaskOnServer(insertTask);
		
		updateTask.setLastModified(insertTask.getLastModified());
		updateTask.setParentFolderID(insertTask.getParentFolderID());
		updateTask.setObjectID(insertTask.getObjectID());
		
		fixture.updateTaskOnServer(updateTask);
		Task resultingTask = fixture.getTaskFromServer(insertTask);
		TaskTestManager.assertTasksAreAlmostEqual(insertTask, resultingTask);
		fixture.cleanUp();
	}
}
