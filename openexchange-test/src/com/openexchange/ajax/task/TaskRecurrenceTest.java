package com.openexchange.ajax.task;

import org.junit.Test;

import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TestTask;

public class TaskRecurrenceTest extends AbstractTaskTestForAJAXClient {
	
	public TaskRecurrenceTest(String name) {
		super(name);
	}

	/* unlimited recurrences */
	//daily
	@Test public void testInsertDailyRecurrenceTask(){
		TestTask todaysTask = getNewTask("This I'll do every day from now on. Promised.")
			.startsToday()
			.everyDay()
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		runSimpleInsertTest(todaysTask);
	}

	@Test public void testInsertAndUpdateDailyRecurrenceTask(){
		TestTask todaysTask = getNewTask("This I'll do every day from now on. Promised.")
			.startsToday()
			.everyDay()
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		TestTask tomorrowsTask = todaysTask.clone()
			.startsTomorrow()
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		runInsertAndUpdateTest(todaysTask, tomorrowsTask, Task.START_DATE, Task.END_DATE);
	}

	//weekly
	@Test public void testWeeklyRecurrence(){
		TestTask tomorrowsTask = getNewTask("This, I'll do every week from now. Well, from tomorrow. Maybe.")
			.startsTomorrow()
			.everyWeek()
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		runSimpleInsertTest(tomorrowsTask);
	}
	
	@Test public void testInsertAndUpdateWeeklyRecurrenceTask(){
		TestTask tomorrowsTask = getNewTask("This, I'll do every week from now. Well, from tomorrow. Maybe.")
			.startsTomorrow()
			.everyWeek()
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		TestTask laterTask = tomorrowsTask.clone()
			.startsTheFollowingDay()
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		runInsertAndUpdateTest(tomorrowsTask, laterTask, Task.START_DATE, Task.END_DATE, Task.DAYS);
	}
	
	@Test public void testWeeklyRecurrence2(){
		Task trainingDays = getNewTask("Training days")
			.startsAtNoon()
			.endInTheEvening()
			.everyWeekOn( Task.TUESDAY, Task.THURSDAY, Task.SATURDAY )
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		runSimpleInsertTest(trainingDays);
	}
	
	@Test public void testInsertAndUpdateWeeklyRecurrence2Task(){
		TestTask trainingDays = getNewTask("Training days")
			.startsAtNoon()
			.endInTheEvening()
			.everyWeekOn( Task.TUESDAY, Task.THURSDAY, Task.SATURDAY )
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		TestTask laterTrainingDays = trainingDays.clone()
			.everyWeekOn( Task.TUESDAY, Task.THURSDAY, Task.SUNDAY );
		runInsertAndUpdateTest(trainingDays, laterTrainingDays, Task.DAYS);
	}
	
	
	//monthly
	@Test public void testMonthlyRecurrence(){
		Task myAAMeeting = getNewTask("my AA meeting")
			.everyMonthOnNthWeekday(1, Task.MONDAY)
			.everyOtherMonth()
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		runSimpleInsertTest(myAAMeeting);
	}
	
	@Test public void testInsertAndUpdateMonthlyRecurrence(){
		TestTask myAAMeeting = getNewTask("my AA meeting")
			.everyMonthOnNthWeekday(1, Task.MONDAY)
			.everyOtherMonth()
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		TestTask myPostponedAAMeeting = myAAMeeting.clone()
			.everyMonthOnNthWeekday(2, Task.MONDAY)
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		runInsertAndUpdateTest(myAAMeeting, myPostponedAAMeeting, Task.DAY_IN_MONTH);
	}

	@Test public void testInsertMonthlyRecurrence2(){
		Task myAAMeeting = getNewTask("my other AA group meeting")
			.everyMonth()
			.onDay(6)
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		runSimpleInsertTest(myAAMeeting);
	}
	
	@Test public void testInsertAndUpdateMonthlyRecurrence2(){
		TestTask myAAMeeting = getNewTask("my other AA group meeting")
			.everyMonth()
			.onDay(6)
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		TestTask myPostponedAAMeeting = myAAMeeting.clone()
			.onDay(5)
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		runInsertAndUpdateTest(myAAMeeting, myPostponedAAMeeting, Task.DAYS, Task.DAY_IN_MONTH);
	}
	
	//yearly
	@Test public void failtestInsertYearlyRecurrence(){
		Task organizeSilvesterParty = getNewTask("Have to organize Silvester party")
			.everyYear()
			.onDay(30)
			.inMonth(12)
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		runSimpleInsertTest(organizeSilvesterParty);
	}

	@Test public void failtestInsertAndUpdateYearlyRecurrence(){
		TestTask organizeSilvesterParty = getNewTask("Have to organize Silvester party")
			.everyYear()
			.onDay(30)
			.inMonth(12)
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		TestTask organizeSilvesterPartyEarlier = organizeSilvesterParty.clone()
			.onDay(29)
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		runInsertAndUpdateTest(organizeSilvesterParty, organizeSilvesterPartyEarlier, Task.DAYS);
	}

	@Test public void failtestInsertYearlyRecurrence2(){
		Task breakNewYearsVows = getNewTask("Forget New Year's vows now")
			.everyYear()
			.inMonth(1)
			.onWeekDays(Task.MONDAY)
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		runSimpleInsertTest(breakNewYearsVows);
	}
	
	@Test public void failtestInsertAndUpdateYearlyRecurrence2(){
		TestTask breakNewYearsVows = getNewTask("Forget New Year's vows now")
			.everyYear()
			.inMonth(1)
			.onWeekDays(Task.MONDAY)
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		TestTask breakNewYearsVowsLater = breakNewYearsVows.clone()
			.onWeekDays(Task.TUESDAY)
			.checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
		runInsertAndUpdateTest(breakNewYearsVows, breakNewYearsVowsLater, Task.START_DATE);
	}
	
	/* limited recurrences */
	

}
