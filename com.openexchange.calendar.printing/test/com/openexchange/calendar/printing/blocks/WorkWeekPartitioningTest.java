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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.calendar.printing.blocks;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import com.openexchange.calendar.printing.AbstractDateTest;
import com.openexchange.calendar.printing.CPAppointment;
import com.openexchange.calendar.printing.CPCalendar;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class WorkWeekPartitioningTest extends AbstractDateTest {

    private WorkWeekPartitioningStrategy strategy;

    private Calendar calendar;

    private List<CPAppointment> getExemplaryWeeks() {
        calendar.set(Calendar.YEAR, 2009);
        calendar.set(Calendar.DAY_OF_MONTH, 25);
        calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
        Date date1 = calendar.getTime();
        CPAppointment app1 = new CPAppointment();
        app1.setStartDate(date1);
        app1.setEndDate(plusOneHour(date1));

        calendar.set(Calendar.DAY_OF_MONTH, 6);
        calendar.set(Calendar.MONTH, Calendar.OCTOBER);
        Date date2 = calendar.getTime();
        CPAppointment app2 = new CPAppointment();
        app2.setStartDate(date2);
        app2.setEndDate(plusOneHour(date2));

        return Arrays.asList(app1,app2);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        CPCalendar oxCal = CPCalendar.getCalendar();
        strategy = new WorkWeekPartitioningStrategy();
        strategy.setCalendar(oxCal);
        calendar = Calendar.getInstance();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testShouldPartitionConsecutiveDatesInOneWeekIntoOneBlock() {
        CPAppointment app1 = new CPAppointment();
        CPAppointment app2 = new CPAppointment();
        app1.setTitle("First appointment");
        app2.setTitle("Second appointment");
        app1.setStartDate(WEDNESDAY());
        app1.setEndDate(plusOneHour(WEDNESDAY()));
        app2.setStartDate(THURSDAY());
        app2.setEndDate(plusOneHour(THURSDAY()));

        CPPartition partitions = strategy.partition(Arrays.asList(new CPAppointment[] { app1, app2 }));
        boolean foundWeekBreak = false;
        for (CPFormattingInformation info : partitions.getFormattingInformation()) {
            if (info.getType() == AbstractWeekPartitioningStrategy.WEEKBREAK & info.getPosition() == 1) {
                foundWeekBreak = true;
            }
        }
        assertTrue("Two consecutive days, Wednesday and Thursday, should not have any week break info between them", !foundWeekBreak);
        assertEquals("Partition should contain two appointments", 2, partitions.getAppointments().size());
    }

    @Test
    public void testShouldNotShowWeekendAppointmentsAtAll() {
        CPAppointment weekendAppointment = new CPAppointment();
        weekendAppointment.setTitle("First appointment");
        weekendAppointment.setStartDate(SUNDAY());
        weekendAppointment.setEndDate(plusOneHour(SUNDAY()));

        CPPartition partitions = strategy.partition(Arrays.asList(new CPAppointment[] { weekendAppointment }));
        assertEquals("Partition should be empty", 0, partitions.getAppointments().size());
    }

    @Test
    public void testShouldPartitionTwoDatesInTwoWeeksIntoTwoBlocks() {
        CPAppointment app1 = new CPAppointment();
        app1.setTitle("First appointment");
        app1.setStartDate(THURSDAY());
        app1.setEndDate(plusOneHour(THURSDAY()));

        CPAppointment app2 = new CPAppointment();
        app2.setTitle("Second appointment");
        app2.setStartDate(WEDNESDAY_NEXT_WEEK());
        app2.setEndDate(plusOneHour(WEDNESDAY_NEXT_WEEK()));

        CPPartition partitions = strategy.partition(Arrays.asList(new CPAppointment[] { app1, app2 }));
        assertEquals("Should contain two elements", 2, partitions.getAppointments().size());
        boolean daybreakFound = false, weekbreakFound = false;
        for (CPFormattingInformation info : partitions.getFormattingInformation()) {
            if (info.getPosition() == 1 && info.getType() == AbstractWeekPartitioningStrategy.DAYBREAK) {
                daybreakFound = true;
            }
            if (info.getPosition() == 1 && info.getType() == AbstractWeekPartitioningStrategy.WEEKBREAK) {
                weekbreakFound = true;
            }
        }
        assertTrue("Should contain a day break after the first element", daybreakFound);
        assertTrue("Should contain a week break after the first element", weekbreakFound);
    }

    @Test
    public void testShouldDetermineMissingDays() {
        List<Date> daysInbetween = strategy.getMissingDaysInbetween(THURSDAY(), SUNDAY());
        assertEquals("Should have two days inbetween", 2, daysInbetween.size());
        calendar.setTime(daysInbetween.get(0));
        assertEquals("First day inbetween would be Friday", Calendar.FRIDAY, calendar.get(Calendar.DAY_OF_WEEK));
        calendar.setTime(daysInbetween.get(1));
        assertEquals("Second day inbetween would be Saturday", Calendar.SATURDAY, calendar.get(Calendar.DAY_OF_WEEK));
    }

    @Test
    public void testShouldDetermineMissingDaysBetweenTwoWeeks() {
        List<Date> daysInbetween = strategy.getMissingDaysInbetween(THURSDAY(), WEDNESDAY_NEXT_WEEK());
        assertEquals("Should have five days inbetween", 5, daysInbetween.size());
        calendar.setTime(daysInbetween.get(0));
        assertEquals("First day inbetween would be Friday", Calendar.FRIDAY, calendar.get(Calendar.DAY_OF_WEEK));
        calendar.setTime(daysInbetween.get(1));
        assertEquals("Second day inbetween would be Saturday", Calendar.SATURDAY, calendar.get(Calendar.DAY_OF_WEEK));
        calendar.setTime(daysInbetween.get(2));
        assertEquals("Third day inbetween would be Sunday", Calendar.SUNDAY, calendar.get(Calendar.DAY_OF_WEEK));
        calendar.setTime(daysInbetween.get(3));
        assertEquals("Fourth day inbetween would be Monday", Calendar.MONDAY, calendar.get(Calendar.DAY_OF_WEEK));
        calendar.setTime(daysInbetween.get(4));
        assertEquals("Fifth day inbetween would be Tuesday", Calendar.TUESDAY, calendar.get(Calendar.DAY_OF_WEEK));
    }

    @Test
    public void testShouldDefaultToOneDayBeforeWorkweekStartIfNoneGiven(){
        CPAppointment first = null; //implies Sunday on European style calendar
        CPAppointment second = new CPAppointment();
        second.setStartDate(THURSDAY());
        List<Date> daysInbetween = strategy.getMissingDaysInbetween(first, second);
        int i = 0;
        for(Date day: daysInbetween){
            calendar.setTime(day);
            assertEquals("Should be day #"+i, Calendar.MONDAY + i, calendar.get(Calendar.DAY_OF_WEEK));
            i++;
        }
        assertEquals("Should have three days inbetween", 3, i);
    }

    @Test
    public void testShouldDefaultToOneDayAfterLastWorkweekDayIfNoneGiven(){
        CPAppointment  first = new CPAppointment();
        first.setStartDate(MONDAY_NEXT_WEEK());
        CPAppointment second = null; //implies Saturday on European style calendar
        List<Date> daysInbetween = strategy.getMissingDaysInbetween(first, second);
        int i = 0;
        for(Date day: daysInbetween){
            i++;
            calendar.setTime(day);
            assertEquals("Should be day #"+i, Calendar.MONDAY + i, calendar.get(Calendar.DAY_OF_WEEK));
        }
        assertEquals("Should have four days inbetween Monday and Saturday", 4, i);
    }

    @Test
    public void testShouldGiveDayInfo() {
        CPAppointment app1 = new CPAppointment();
        app1.setTitle("First appointment");
        app1.setStartDate(THURSDAY());
        app1.setEndDate(plusOneHour(THURSDAY()));

        CPAppointment app2 = new CPAppointment();
        app2.setTitle("Second appointment");
        app2.setStartDate(SUNDAY());
        app2.setEndDate(plusOneHour(SUNDAY()));

        CPPartition partitions = strategy.partition(Arrays.asList(new CPAppointment[] { app1, app2 }));
        List<Integer> days = new LinkedList<Integer>();
        int numberOfDays = 0;
        for (CPFormattingInformation info : partitions.getFormattingInformation()) {
            if (info.getType() == 10) {
                days.add((Integer) info.getAdditionalInformation());
                numberOfDays++;
            }
        }

        assertEquals("Should contain day info for every work day", 5, numberOfDays);
        assertTrue("Should contain Monday, even though there is no appointment", days.contains(Integer.valueOf(Calendar.MONDAY)));
        assertTrue("Should contain Tuesday, even though there is no appointment", days.contains(Integer.valueOf(Calendar.TUESDAY)));
        assertTrue("Should contain Wednesday, even though there is no appointment", days.contains(Integer.valueOf(Calendar.WEDNESDAY)));
        assertTrue("Should contain Thursday", days.contains(Integer.valueOf(Calendar.THURSDAY)));
        assertTrue("Should contain Friday, even though there is no appointment", days.contains(Integer.valueOf(Calendar.FRIDAY)));
        assertTrue("Should not contain Saturday, because that is a weekend day", !days.contains(Integer.valueOf(Calendar.SATURDAY)));
        assertTrue(
            "Should not contain Sunday, because that is a weekend day, although there is an appointment",
            !days.contains(Integer.valueOf(Calendar.SUNDAY)));
    }

    @Test
    public void testShouldPutDaybreakbeforeDayNameInfo() {
        CPAppointment app1 = new CPAppointment();
        app1.setTitle("First appointment");
        app1.setStartDate(THURSDAY());
        app1.setEndDate(plusOneHour(THURSDAY()));

        CPAppointment app2 = new CPAppointment();
        app2.setTitle("Second appointment");
        app2.setStartDate(SUNDAY());
        app2.setEndDate(plusOneHour(SUNDAY()));

        CPPartition partitions = strategy.partition(Arrays.asList(new CPAppointment[] { app1, app2 }));

        boolean foundDayname = false, foundDaybreak = false;
        Integer dayname = null;
        int encounters = 0;
        for (CPFormattingInformation info : partitions.getFormattingInformation()) {
            if (info.getType() == 0) {
                if (foundDaybreak) {
                    fail("Encountered daybreak info twice without dayname info inbetween");
                }
                foundDaybreak = true;
            }
            if (info.getType() == 10) {
                if (foundDayname) {
                    fail("Encountered dayname info twice without daybreak info inbetween");
                }
                foundDayname = true;
                dayname = (Integer) info.getAdditionalInformation();
            }
            if (foundDayname) {
                assertTrue("Did not find daybreak before day #" + dayname, foundDaybreak);
                foundDaybreak = foundDayname = false;
                encounters++;
            }
        }
        assertEquals("Should have 5 encounters (Mo-Fr)", 5, encounters);
    }

    @Test
    public void testShouldPutMarkersCorrectlyEvenIfOmittingWeekendAppointments() {
        CPAppointment app1 = new CPAppointment();
        app1.setTitle("First appointment");
        app1.setStartDate(THURSDAY());
        app1.setEndDate(plusOneHour(THURSDAY()));

        CPAppointment app2 = new CPAppointment();
        app2.setTitle("Omitted appointment");
        app2.setStartDate(SUNDAY());
        app2.setEndDate(plusOneHour(SUNDAY()));

        CPAppointment app3 = new CPAppointment();
        app3.setTitle("Second appointment");
        app3.setStartDate(MONDAY_NEXT_WEEK());
        app3.setEndDate(plusOneHour(MONDAY_NEXT_WEEK()));

        CPPartition partitions = strategy.partition(Arrays.asList(new CPAppointment[] { app1, app2, app2, app2, app3 }));

        boolean rightPos = false, wrongPos = false;

        for (CPFormattingInformation info : partitions.getFormattingInformation()) {
            if (info.getPosition() == 1 && info.getType() == AbstractWeekPartitioningStrategy.WEEKBREAK) {
                rightPos = true;
            }
            if(info.getPosition() > 1 && info.getType() == AbstractWeekPartitioningStrategy.WEEKBREAK) {
                wrongPos = true;
            }
        }
        assertEquals("Should leave only two appointments, Thursday and Monday", 2, partitions.getAppointments().size());
        assertTrue("Should find weekbreak right before position of Monday date", rightPos);
        assertFalse("Should not find weekbreak elsewhere", wrongPos);
    }

    @Test
    public void testShouldAlwaysContainFiveDaybreaksBetweenWeekBreaks(){
        List<CPAppointment> list = getExemplaryWeeks();

        CPPartition partition = strategy.partition(list);
        List<CPFormattingInformation> infos = partition.getFormattingInformation();
        int days = 5;
        int weekCounter = 0;
        for(CPFormattingInformation info : infos){
            if(info.getType() == AbstractWeekPartitioningStrategy.DAYBREAK) {
                days++;
            }
            if(info.getType() == AbstractWeekPartitioningStrategy.WEEKBREAK){
                weekCounter++;
                assertEquals("Should contain 5 days within each week, but not in week #"+weekCounter, 5, days);
                days = 0;
            }
        }
        assertEquals("Should contain 5 days left when done with last week (#"+weekCounter+")", 5, days);
    }

    @Test
    public void testShouldAlwaysContainFiveDaybreaksBetweenWeekBreaksOverAWholeFuckingYear() {
        calendar.set(Calendar.YEAR, 2007); //1.1.2007 is a Monday. Other years might not have 5 work days on the first week.
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        CPAppointment app1 = new CPAppointment();
        app1.setStartDate(calendar.getTime());
        app1.setEndDate(plusOneHour(calendar.getTime()));

        CPAppointment app2 = new CPAppointment();
        calendar.set(Calendar.DAY_OF_YEAR, 364);
        app2.setStartDate(calendar.getTime());
        app2.setEndDate(plusOneHour(calendar.getTime()));

        CPPartition partitions = strategy.partition(Arrays.asList( app1, app2 ));

        List<CPFormattingInformation> infos = partitions.getFormattingInformation();
        int days = 5;
        int weekCounter = 0;
        for(CPFormattingInformation info : infos){
            if(info.getType() == AbstractWeekPartitioningStrategy.DAYBREAK) {
                days++;
            }
            if(info.getType() == AbstractWeekPartitioningStrategy.WEEKBREAK){
                weekCounter++;
                assertEquals("Should contain 5 days within each week, but not in week #"+weekCounter, 5, days);
                days = 0;
            }
        }
        assertEquals("Should contain 5 days left when done with last week (#"+weekCounter+")", 5, days);
    }


    @Test
    public void testShouldNotMissDayInSecondWeek() {
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.YEAR, 2009);

        calendar.set(Calendar.DAY_OF_MONTH, 29);
        calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
        Date start = calendar.getTime();
        CPAppointment app1 = new CPAppointment();
        app1.setStartDate(start);
        app1.setEndDate(plusOneHour(start));

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, Calendar.OCTOBER);
        Date end = calendar.getTime();
        CPAppointment app2 = new CPAppointment();
        app2.setStartDate(end);
        app2.setEndDate(plusOneHour(end));

        List<Date> daysInbetween = strategy.getMissingDaysInbetween(start, end);
        assertEquals("Should find one day inbetween (when using plain dates)", 1, daysInbetween.size());
        calendar.setTime(daysInbetween.get(0));
        assertEquals("Should be Wednesday (when using plain dates)", Calendar.WEDNESDAY, calendar.get(Calendar.DAY_OF_WEEK));

        daysInbetween = strategy.getMissingDaysInbetween(app1, app2);
        assertEquals("Should find one day inbetween (when using CPAppointments)", 1, daysInbetween.size());
        calendar.setTime(daysInbetween.get(0));
        assertEquals("Should be Wednesday (when using CPAppointments)", Calendar.WEDNESDAY, calendar.get(Calendar.DAY_OF_WEEK));
    }

    @Test
    public void testShouldNotAddAnotherWeekIfLastDayWasLastWorkWeekDay(){
        for(int day: new int[]{2,3,4}){
            calendar.set(Calendar.YEAR, 2009);
            calendar.set(Calendar.DAY_OF_MONTH, 25);
            calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
            Date date1 = calendar.getTime();
            CPAppointment app1 = new CPAppointment();
            app1.setStartDate(date1);
            app1.setEndDate(plusOneHour(date1));

            //2009-10-2 is a Friday, so last day of the workweek, 10-3 and 10-4 are weekend days
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.MONTH, Calendar.OCTOBER);
            Date date2 = calendar.getTime();
            CPAppointment app2 = new CPAppointment();
            app2.setStartDate(date2);
            app2.setEndDate(plusOneHour(date2));

            CPPartition partition = strategy.partition(Arrays.asList(app1,app2));

            List<CPFormattingInformation> infos = partition.getFormattingInformation();

            int dayBreaksAfterLastDate = 0;
            for(CPFormattingInformation info:infos){
                if(info.getType() == AbstractWeekPartitioningStrategy.DAYBREAK && info.getPosition() == 2) {
                    dayBreaksAfterLastDate++;
                }
            }
            assertEquals("Should not have added daybreaks after last appointment on 2009-10-"+day, 0, dayBreaksAfterLastDate);
        }
    }

    @Test
    public void testShouldCountWeekEndDaysWhenCalculatingNumberOfDaybreaks() {
        CPAppointment app1 = new CPAppointment();
        app1.setTitle("First appointment");
        app1.setStartDate(THURSDAY());
        app1.setEndDate(plusOneHour(THURSDAY()));

        CPAppointment app2 = new CPAppointment();
        app2.setTitle("Omitted appointment");
        app2.setStartDate(SATURDAY());
        app2.setEndDate(plusOneHour(SATURDAY()));

        CPAppointment app3 = new CPAppointment();
        app3.setTitle("Second appointment");
        app3.setStartDate(WEDNESDAY_NEXT_WEEK());
        app3.setEndDate(plusOneHour(WEDNESDAY_NEXT_WEEK()));

        CPPartition partitions = strategy.partition(Arrays.asList(app1, app2, app3));

        List<CPFormattingInformation> infos = partitions.getFormattingInformation();
        int daysBeforeWednesday = 0, daysAfterWednesday = 0;
        boolean startCounting = false;
        for(CPFormattingInformation info: infos){
            if(info.getPosition() == 1 && info.getType() == AbstractWeekPartitioningStrategy.WEEKBREAK) {
                startCounting = true;
            }
            if(startCounting && info.getType() == AbstractWeekPartitioningStrategy.DAYBREAK) {
                if(info.getPosition() == 1) {
                    daysBeforeWednesday ++;
                }
            }
            if(info.getPosition() == 2) {
                daysAfterWednesday ++;
            }
        }
        assertEquals("Should find 3 daybreaks in week before Wednesday", 3, daysBeforeWednesday);
        assertEquals("Should find 2 daybreaks in week after Wednesday", 2, daysAfterWednesday);
    }

    @Test
    public void testShouldCountWholeDaysWhenDeterminingMissingDays() {
        calendar.set(Calendar.YEAR, 2009);
        calendar.set(Calendar.DAY_OF_MONTH, 25);
        calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        Date date1 = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, 27);
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        Date date2 = calendar.getTime();

        List<Date> daysInbetween = strategy.getMissingDaysInbetween(date1, date2);
        assertEquals("Should find one day inbetween", 1, daysInbetween.size());
    }

    @Test
    public void testShouldWorkWithDifferentWorkWeekLength() {
        CPAppointment app1 = new CPAppointment();
        CPAppointment app2 = new CPAppointment();
        CPAppointment app3 = new CPAppointment();
        CPAppointment app4 = new CPAppointment();
        app1.setStartDate(WEDNESDAY());
        app1.setEndDate(plusOneHour(WEDNESDAY()));
        app2.setStartDate(THURSDAY());
        app2.setEndDate(plusOneHour(THURSDAY()));
        app3.setStartDate(SUNDAY());
        app3.setEndDate(plusOneHour(SUNDAY()));
        app4.setStartDate(MONDAY_NEXT_WEEK());
        app4.setEndDate(plusOneHour(MONDAY_NEXT_WEEK()));

        CPCalendar cal = CPCalendar.getCalendar();
        cal.setWorkWeekStartingDay(Calendar.WEDNESDAY);
        cal.setWorkWeekDurationInDays(2);
        strategy.setCalendar(cal);
        CPPartition partitions = strategy.partition(Arrays.asList(app1,app2,app3,app4));

        assertEquals("Should contain only two appointments", 2, partitions.getAppointments().size());
        boolean foundWednesday = false, foundThursday = false;

        for(CPAppointment app : partitions.getAppointments()){
            calendar.setTime(app.getStartDate());
            if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
                foundWednesday = true;
            }
            if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
                foundThursday = true;
            }
        }
        assertTrue("Wednesday should be in there", foundWednesday);
        assertTrue("Thursday should be in there", foundThursday);
    }
}
