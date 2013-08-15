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
public class MonthPartitioningTest extends AbstractDateTest {

    private Calendar calendar;

    protected WeekPartitioningStrategy strategy;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        strategy = new MonthPartitioningStrategy();
        strategy.setCalendar(CPCalendar.getCalendar());
        calendar = Calendar.getInstance();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testShouldPartitionTwoDatesInTwoMonthsIntoTwoBlocks() {
        CPAppointment app1 = new CPAppointment();
        app1.setTitle("First appointment");
        app1.setStartDate(THURSDAY());
        app1.setEndDate(plusOneHour(THURSDAY()));

        CPAppointment app2 = new CPAppointment();
        app2.setTitle("Second appointment");
        app2.setStartDate(WEDNESDAY_NEXT_MONTH());
        app2.setEndDate(plusOneHour(WEDNESDAY_NEXT_MONTH()));

        CPPartition partitions = strategy.partition(Arrays.asList(new CPAppointment[] { app1, app2 }));
        assertEquals("Should contain two elements", 2, partitions.getAppointments().size());
        boolean monthbreakFound = false;
        for (CPFormattingInformation info : partitions.getFormattingInformation()) {
            if (info.getPosition() == 1 && info.getType() == AbstractWeekPartitioningStrategy.MONTHBREAK) {
                monthbreakFound = true;
            }
        }
        assertTrue("Should contain a month break after the first element", monthbreakFound);
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
    public void testShouldDefaultToOneDayBeforeMonthStartIfNoneGiven() {
        CPAppointment first = null; // implies a date one the 31.12.2008
        CPAppointment second = new CPAppointment();
        second.setStartDate(SUNDAY());
        List<Date> daysInbetween = strategy.getMissingDaysInbetween(first, second);
        int i = 0;
        for (Date day : daysInbetween) {
            calendar.setTime(day);
            i++;
            assertEquals("Should be day #" + i + " of month", i, calendar.get(Calendar.DAY_OF_MONTH));
        }
        assertEquals("Should have ten days (until 11.1.2) inbetween", 10, i);
    }

    @Test
    public void testShouldDefaultToOneDayAfterLastWeekDayIfNoneGiven() {
        CPAppointment first = new CPAppointment();
        first.setStartDate(WEDNESDAY());
        CPAppointment second = null; // implies end of month
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
        assertEquals("Should have 24 days between 7.1. and the end of month", 24, days);

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
        int numberOfDayNames = 0, numberOfDayBreaks = 0;
        for (CPFormattingInformation info : partitions.getFormattingInformation()) {
            if (info.getType() == MonthPartitioningStrategy.DAYNAME) {
                numberOfDayNames++;
            }
            if (info.getType() == MonthPartitioningStrategy.DAYBREAK) {
                numberOfDayBreaks++;
            }
        }
        assertEquals("Should contain day info for every day of January", 31, numberOfDayNames);
        assertEquals("Should contain day breaks for every day of January", 31, numberOfDayBreaks);
    }

    @Test
    public void testShouldAlwaysContainTwelveMonthbreaksPerYear() {
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
        List<Date> months = new LinkedList<Date>();
        for (CPFormattingInformation info : infos) {
            if (info.getType() == AbstractWeekPartitioningStrategy.MONTHBREAK) {
                months.add((Date) info.getAdditionalInformation());
            }
        }

        assertEquals("Should contain twelve months per year", 12, months.size());
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
    public void testShouldInsertFillerIfMonthsFirstWeekDoesContainDaysFromLastMonth() {
        // if the first day of a month is a wednesday, then you need to fill up the beginning of the week with place holders for week views.
        // Other views may chose to ignore them. The situation is: You have a month break (but no week break in the beginning, because the
        // week didn't begin on Wednesday), then four fillers, then three normal days, then the first week break of the month.
        CPAppointment app = new CPAppointment();
        app.setStartDate(WEDNESDAY());
        app.setEndDate(plusOneHour(WEDNESDAY()));
        CPPartition partition = strategy.partition(Arrays.asList(app));
        int daysCounted = 0, fillersCounted = 0;
        boolean doCounting = false;
        for (CPFormattingInformation info : partition.getFormattingInformation()) {
            if (info.getType() == MonthPartitioningStrategy.MONTHBREAK) {
                doCounting = true;
            }
            if (info.getType() == MonthPartitioningStrategy.WEEKBREAK) {
                doCounting = false;
            }
            if (doCounting && info.getType() == MonthPartitioningStrategy.FILLDAY) {
                fillersCounted++;
            }
            if (doCounting && info.getType() == MonthPartitioningStrategy.DAYBREAK) {
                daysCounted++;
            }
        }
        assertEquals("First week should have four normal days", 4, daysCounted);
        assertEquals("First week should have three filler markers", 3, fillersCounted);
    }

}
