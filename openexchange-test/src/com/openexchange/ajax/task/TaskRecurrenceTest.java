/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.task;

import org.junit.Test;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.tasks.TestTask;

/**
 * {@link TaskRecurrenceTest}
 */
public class TaskRecurrenceTest extends AbstractTaskTestForAJAXClient {
    /* unlimited recurrences */
    //daily
    @Test
    public void testInsertDailyRecurrenceTask() {
        TestTask todaysTask = getNewTask("This I'll do every day from now on. Promised.").startsToday().everyDay().checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        runSimpleInsertTest(todaysTask);
    }

    @Test
    public void testInsertAndUpdateDailyRecurrenceTask() {
        TestTask todaysTask = getNewTask("This I'll do every day from now on. Promised.").startsToday().everyDay().checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        TestTask tomorrowsTask = todaysTask.clone().startsTomorrow().checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        runInsertAndUpdateTest(todaysTask, tomorrowsTask, Task.START_DATE, Task.END_DATE);
    }

    //weekly
    @Test
    public void testWeeklyRecurrence() {
        TestTask tomorrowsTask = getNewTask("This, I'll do every week from now. Well, from tomorrow. Maybe.").startsTomorrow().everyWeek().checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        runSimpleInsertTest(tomorrowsTask);
    }

    @Test
    public void testInsertAndUpdateWeeklyRecurrenceTask() {
        TestTask tomorrowsTask = getNewTask("This, I'll do every week from now. Well, from tomorrow. Maybe.").startsTomorrow().everyWeek().checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        TestTask laterTask = tomorrowsTask.clone().startsTheFollowingDay().checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        runInsertAndUpdateTest(tomorrowsTask, laterTask, Task.START_DATE, Task.END_DATE, Task.DAYS);
    }

    @Test
    public void testWeeklyRecurrence2() {
        Task trainingDays = getNewTask("Training days").startsThisWeek(Task.TUESDAY).startsAtNoon().endsThisWeek(Task.TUESDAY).endsInTheEvening().everyWeekOn(Task.TUESDAY, Task.THURSDAY, Task.SATURDAY).checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        runSimpleInsertTest(trainingDays);
    }

    @Test
    public void testInsertAndUpdateWeeklyRecurrence2Task() {
        TestTask trainingDays = getNewTask("Training days").startsThisWeek(Task.TUESDAY).startsAtNoon().endsThisWeek(Task.TUESDAY).endsInTheEvening().everyWeekOn(Task.TUESDAY, Task.THURSDAY, Task.SATURDAY).checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        TestTask laterTrainingDays = trainingDays.clone().everyWeekOn(Task.TUESDAY, Task.THURSDAY, Task.SUNDAY);
        runInsertAndUpdateTest(trainingDays, laterTrainingDays, Task.DAYS);
    }

    //monthly
    @Test
    public void testMonthlyRecurrence() {
        Task myAAMeeting = getNewTask("my AA meeting").startsWeekOfMonthOnDay(1, Task.MONDAY).everyMonthOnNthWeekday(1, Task.MONDAY).everyOtherMonth().checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        runSimpleInsertTest(myAAMeeting);
    }

    @Test
    public void testInsertAndUpdateMonthlyRecurrence() {
        TestTask myAAMeeting = getNewTask("my AA meeting").startsThisWeek(Task.MONDAY).startsWeekOfMonth(1).everyMonthOnNthWeekday(1, Task.MONDAY).everyOtherMonth().checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        TestTask myPostponedAAMeeting = myAAMeeting.clone().startsThisWeek(Task.MONDAY).startsWeekOfMonth(2).everyMonthOnNthWeekday(2, Task.MONDAY).checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        runInsertAndUpdateTest(myAAMeeting, myPostponedAAMeeting, Task.DAY_IN_MONTH, Task.START_DATE, Task.END_DATE);
    }

    @Test
    public void testInsertMonthlyRecurrence2() {
        Task myAAMeeting = getNewTask("my other AA group meeting").startsThisMonth(6).everyMonth().onDay(6).checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        runSimpleInsertTest(myAAMeeting);
    }

    @Test
    public void testInsertAndUpdateMonthlyRecurrence2() {
        TestTask myAAMeeting = getNewTask("my other AA group meeting").startsThisMonth(6).everyMonth().onDay(6).checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        TestTask myPostponedAAMeeting = myAAMeeting.clone().startsThisMonth(5).onDay(5).checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        runInsertAndUpdateTest(myAAMeeting, myPostponedAAMeeting, Task.DAYS, Task.DAY_IN_MONTH, Task.START_DATE);
    }
}
