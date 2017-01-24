
package com.openexchange.groupware.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;

public class TestTaskTest {

    protected TestTask task;
    protected TimeZone timezone = TimeZone.getTimeZone("Europe/Berlin");

    protected TestTask getNewTask() {
        TestTask newTask = new TestTask();
        newTask.setTimezone(timezone);
        return newTask;
    }

    @Before
    public void setUp() {
        task = new TestTask();
        task.setTimezone(timezone);
    }

    @Test
    public void testCalendarUsedShouldBeLenientWhenOverflowing() {
        Calendar cal = Calendar.getInstance(timezone);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        task.setStartDate(cal.getTime());
        task.checkConsistencyOf(TestTask.DATES);
        task.startsTheFollowingDay();
        cal.setTime(task.getStartDate());
        assertEquals("Should be the 1st day of the month", 1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals("Should be February", Calendar.FEBRUARY, cal.get(Calendar.MONTH));
    }

    @Test
    public void testCalendarUsedShouldBeLenientWhenUnderrunning() {
        Calendar cal = Calendar.getInstance(timezone);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        task.setStartDate(cal.getTime());
        task.checkConsistencyOf(TestTask.DATES);
        task.startsTheDayBefore();
        cal.setTime(task.getStartDate());
        assertEquals("Should be the 31st day of the month", 31, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals("Should be January", Calendar.JANUARY, cal.get(Calendar.MONTH));
    }

    @Test
    public void testOXandJavaConventionsShouldBeTheSame() {
        assertEquals("This will only work if an OX-monday is the same as a Java-monday", Calendar.MONDAY, TestTask.MONDAY);
    }

    @Test
    public void testTaskEverydayAtNoonStartingTomorrow() {
        task.startsAtNoon().startsTheFollowingDay().everyDay();
        Date now = new Date();
        /*
         * this is the difference in milliseconds - note that due to DST, noon of
         * tomorrow might be less (more more) than 12 hours away. Luckily, we're
         * testing in the Europe/Berlin timezone for both dates.
         **/
        long dateDiff = task.getStartDate().getTime() - now.getTime();
        assertTrue("Should be at least 12 hours difference between now and noon, tomorrow", dateDiff > 12 * 60 * 60 * 1000);
    }

    @Test
    public void testShouldNotBreakIfGivenStartDateButNotEndDate() {
        task.startsAtNoon();
        assertNull(task.getEndDate());
        task.checkConsistencyOf(TestTask.DATES);
        assertNotNull(task.getEndDate());
    }

    @Test
    public void testShouldNotBreakIfGivenEndDateButNotStartDate() {
        task.endsAtNoon();
        assertNull(task.getStartDate());
        task.checkConsistencyOf(TestTask.DATES);
        assertNotNull(task.getStartDate());
    }

    @Test
    public void testShouldNotBreakIfGivenEndDateBeforeStartDate() {
        task.startsInTheEvening().endsAtNoon();
        assertTrue(task.getEndDate().compareTo(task.getStartDate()) < 0);
        task.checkConsistencyOf(TestTask.DATES);
        assertTrue(task.getEndDate().compareTo(task.getStartDate()) >= 0);
    }

    @Test
    public void testShouldNotBreakWhenToldToOccurTheFollowingDayWhileMissingStartDate() {
        task.startsTheFollowingDay();
        assertNotNull(task.getStartDate());
        Date now = new Date();
        TaskAsserts.assertFirstDateOccursLaterThanSecond(task.getStartDate(), now);
    }

    @Test
    public void testTomorrowAndTheNextDayFromNowShouldBeTheSame() {
        Date now = new Date();
        TestTask task1 = getNewTask();
        task1.setStartDate(now);
        task1.setEndDate(now);
        task1.startsTheFollowingDay();
        task1.endsTheFollowingDay();

        TestTask task2 = getNewTask();
        task2.setStartDate(now);
        task2.setEndDate(now);
        task2.startsTomorrow();
        task2.endsTomorrow();

        assertTrue("Start dates should be equal", TaskAsserts.checkOXDatesAreEqual(task1.getStartDate(), task2.getStartDate()));
        assertTrue("End dates should be equal", TaskAsserts.checkOXDatesAreEqual(task1.getEndDate(), task2.getEndDate()));
    }

    @Test
    public void testShouldCopyNecessaryInformationForUpdate() {
        TestTask task1 = getNewTask();
        task1.setObjectID(666);
        task1.setLastModified(new Date());
        TestTask task2 = getNewTask().makeRelatedTo(task1);
        assertEquals("To change a task, you need its timestamp for 'last_modified'", task1.getLastModified(), task2.getLastModified());
        assertEquals("To change a task, you need its id", task1.getObjectID(), task2.getObjectID());
    }

    @Test
    public void testShiftingDateByOneDay() {
        Calendar now = Calendar.getInstance(timezone);
        Date shiftedDate = task.shiftDateByDays(now.getTime(), 1);
        Calendar tomorrow = Calendar.getInstance(timezone);
        tomorrow.setTime(now.getTime());
        tomorrow.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH) + 1); //works because another test assures that overflowing works
        assertEquals("the shiftByDay-method should produce the same result as adding one to the date of a calendar", tomorrow.getTimeInMillis(), shiftedDate.getTime());
    }

}
