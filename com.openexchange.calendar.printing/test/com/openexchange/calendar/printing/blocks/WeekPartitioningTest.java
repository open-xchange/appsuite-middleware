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
public class WeekPartitioningTest extends AbstractDateTest {

    private Calendar calendar;

    protected WeekPartitioningStrategy strategy;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        strategy = new WeekPartitioningStrategy();
        strategy.setCalendar(CPCalendar.getCalendar());
        calendar = Calendar.getInstance();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
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
    public void testShouldDefaultToOneDayBeforeWeekStartIfNoneGiven() {
        CPAppointment first = null; // implies Sunday on European style calendar
        CPAppointment second = new CPAppointment();
        second.setStartDate(THURSDAY());
        List<Date> daysInbetween = strategy.getMissingDaysInbetween(first, second);
        int i = 0;
        for (Date day : daysInbetween) {
            calendar.setTime(day);
            assertEquals("Should be day #" + i, Calendar.MONDAY + i, calendar.get(Calendar.DAY_OF_WEEK));
            i++;
        }
        assertEquals("Should have three days inbetween", 3, i);
    }

    @Test
    public void testShouldDefaultToOneDayAfterLastWeekDayIfNoneGiven() {
        CPAppointment first = new CPAppointment();
        first.setStartDate(WEDNESDAY());
        CPAppointment second = null; // implies Monday after that on European style calendar
        List<Date> daysInbetween = strategy.getMissingDaysInbetween(first, second);

        int days = 0;
        calendar.setTime(WEDNESDAY());
        Calendar actual = Calendar.getInstance();
        for (Date day : daysInbetween) {
            days++;
            calendar.add(Calendar.DAY_OF_WEEK, 1);
            actual.setTime(day);
            assertEquals("Should be day #" + days, calendar.get(Calendar.DAY_OF_WEEK), actual.get(Calendar.DAY_OF_WEEK));

        }
        assertEquals("Should have four days inbetween Wednesday and the following Monday", 4, days);

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

        assertEquals("Should contain day info for every day", 7, numberOfDays);
        assertTrue("Should contain Monday, even though there is no appointment", days.contains(Integer.valueOf(Calendar.MONDAY)));
        assertTrue("Should contain Tuesday, even though there is no appointment", days.contains(Integer.valueOf(Calendar.TUESDAY)));
        assertTrue("Should contain Wednesday, even though there is no appointment", days.contains(Integer.valueOf(Calendar.WEDNESDAY)));
        assertTrue("Should contain Thursday", days.contains(Integer.valueOf(Calendar.THURSDAY)));
        assertTrue("Should contain Friday, even though there is no appointment", days.contains(Integer.valueOf(Calendar.FRIDAY)));
        assertTrue("Should contain Saturday", days.contains(Integer.valueOf(Calendar.SATURDAY)));
        assertTrue("Should contain Sunday, because there is an appointment", days.contains(Integer.valueOf(Calendar.SUNDAY)));
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
        assertEquals("Should have 7 encounters (Mo-Sun)", 7, encounters);
    }

    @Test
    public void testShouldAlwaysContainSevenDaybreaksBetweenWeekBreaks() {
        calendar.set(Calendar.YEAR, 2007); //1.1.2007 is a Monday, so we don't have to treat the first week differently: Other years might have only 4 days on the first week.
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        CPAppointment app1 = new CPAppointment();
        app1.setStartDate(calendar.getTime());
        app1.setEndDate(plusOneHour(calendar.getTime()));

        CPAppointment app2 = new CPAppointment();
        calendar.set(Calendar.DAY_OF_YEAR, 364);
        app2.setStartDate(calendar.getTime());
        app2.setEndDate(plusOneHour(calendar.getTime()));

        CPPartition partitions = strategy.partition(Arrays.asList(app1, app2));

        List<CPFormattingInformation> infos = partitions.getFormattingInformation();
        int days = 7;
        int weekCounter = 0;
        for (CPFormattingInformation info : infos) {
            if (info.getType() == AbstractWeekPartitioningStrategy.DAYBREAK) {
                days++;
            }
            if (info.getType() == AbstractWeekPartitioningStrategy.WEEKBREAK) {
                assertEquals("Should contain 7 days within each week, but not in week #" + weekCounter, 7, days);
                weekCounter++;
                days = 0;
            }
        }
        assertEquals("Should contain 7 days left when done with last week (#" + weekCounter + ")", 7, days);
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
    public void testShouldCountWeekEndDaysWhenCalculatingNumberOfDaybreaks() {
        CPAppointment app1 = new CPAppointment();
        app1.setTitle("First appointment");
        app1.setStartDate(THURSDAY());
        app1.setEndDate(plusOneHour(THURSDAY()));

        CPAppointment app2 = new CPAppointment();
        app2.setTitle("Second appointment");
        app2.setStartDate(SATURDAY());
        app2.setEndDate(plusOneHour(SATURDAY()));

        CPAppointment app3 = new CPAppointment();
        app3.setTitle("Third appointment");
        app3.setStartDate(WEDNESDAY_NEXT_WEEK());
        app3.setEndDate(plusOneHour(WEDNESDAY_NEXT_WEEK()));

        CPPartition partitions = strategy.partition(Arrays.asList(app1, app2, app3));

        List<CPFormattingInformation> infos = partitions.getFormattingInformation();
        int daysBeforeWednesday = 0, daysAfterWednesday = 0;
        boolean startCounting = false;
        for (CPFormattingInformation info : infos) {
            if (info.getPosition() == 2 && info.getType() == AbstractWeekPartitioningStrategy.WEEKBREAK) {
                startCounting = true;
            }
            if (startCounting && info.getType() == AbstractWeekPartitioningStrategy.DAYBREAK) {
                if (info.getPosition() == 2) {
                    daysBeforeWednesday++;
                }
            }
            if (info.getPosition() == 3) {
                daysAfterWednesday++;
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
    public void testShouldWorkEvenWithDifferentWeekStart() {
        CPCalendar cal = CPCalendar.getCalendar();
        cal.setFirstDayOfWeek(Calendar.THURSDAY);

        CPAppointment app1 = new CPAppointment();
        CPAppointment app2 = new CPAppointment();
        app1.setStartDate(WEDNESDAY());
        app1.setEndDate(plusOneHour(WEDNESDAY()));
        app2.setStartDate(THURSDAY());
        app2.setEndDate(plusOneHour(THURSDAY()));

        strategy.setCalendar(cal);
        CPPartition partition = strategy.partition(Arrays.asList(app1,app2));

        List<CPFormattingInformation> infos = partition.getFormattingInformation();
        boolean found = false;
        for(CPFormattingInformation info: infos){
            if(info.getPosition() == 1 && info.getType() == WeekPartitioningStrategy.WEEKBREAK) {
                found = true;
            }
        }
        assertTrue("Should place weekbreak between Wednesday and Thursday if first day of the week is set to the latter", found);
    }
}
