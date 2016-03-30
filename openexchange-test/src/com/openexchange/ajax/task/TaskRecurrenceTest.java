/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax.task;

import org.junit.Test;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TestTask;

/**
 * {@link TaskRecurrenceTest}
 */
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
            .startsThisWeek(Task.TUESDAY)
            .startsAtNoon()
            .endsThisWeek(Task.TUESDAY)
            .endsInTheEvening()
            .everyWeekOn( Task.TUESDAY, Task.THURSDAY, Task.SATURDAY )
            .checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        runSimpleInsertTest(trainingDays);
    }

    @Test public void testInsertAndUpdateWeeklyRecurrence2Task() {
        TestTask trainingDays = getNewTask("Training days")
            .startsThisWeek(Task.TUESDAY)
            .startsAtNoon()
            .endsThisWeek(Task.TUESDAY)
            .endsInTheEvening()
            .everyWeekOn( Task.TUESDAY, Task.THURSDAY, Task.SATURDAY )
            .checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        TestTask laterTrainingDays = trainingDays.clone()
            .everyWeekOn( Task.TUESDAY, Task.THURSDAY, Task.SUNDAY );
        runInsertAndUpdateTest(trainingDays, laterTrainingDays, Task.DAYS);
    }

    //monthly
    @Test public void testMonthlyRecurrence() {
        Task myAAMeeting = getNewTask("my AA meeting")
            .startsWeekOfMonthOnDay(1, Task.MONDAY)
            .everyMonthOnNthWeekday(1, Task.MONDAY)
            .everyOtherMonth()
            .checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        runSimpleInsertTest(myAAMeeting);
    }

    @Test public void testInsertAndUpdateMonthlyRecurrence(){
        TestTask myAAMeeting = getNewTask("my AA meeting")
            .startsThisWeek(Task.MONDAY)
            .startsWeekOfMonth(1)
            .everyMonthOnNthWeekday(1, Task.MONDAY)
            .everyOtherMonth()
            .checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        TestTask myPostponedAAMeeting = myAAMeeting.clone()
            .startsThisWeek(Task.MONDAY)
            .startsWeekOfMonth(2)
            .everyMonthOnNthWeekday(2, Task.MONDAY)
            .checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        runInsertAndUpdateTest(myAAMeeting, myPostponedAAMeeting, Task.DAY_IN_MONTH, Task.START_DATE, Task.END_DATE);
    }

    @Test public void testInsertMonthlyRecurrence2(){
        Task myAAMeeting = getNewTask("my other AA group meeting")
            .startsThisMonth(6)
            .everyMonth()
            .onDay(6)
            .checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        runSimpleInsertTest(myAAMeeting);
    }

    @Test public void testInsertAndUpdateMonthlyRecurrence2(){
        TestTask myAAMeeting = getNewTask("my other AA group meeting")
            .startsThisMonth(6)
            .everyMonth()
            .onDay(6)
            .checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        TestTask myPostponedAAMeeting = myAAMeeting.clone()
            .startsThisMonth(5)
            .onDay(5)
            .checkConsistencyOf(TestTask.DATES, TestTask.RECURRENCES);
        runInsertAndUpdateTest(myAAMeeting, myPostponedAAMeeting, Task.DAYS, Task.DAY_IN_MONTH, Task.START_DATE);
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
