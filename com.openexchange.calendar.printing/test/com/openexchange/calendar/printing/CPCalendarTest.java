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

package com.openexchange.calendar.printing;

import java.util.Arrays;
import java.util.Calendar;
import junit.framework.TestCase;
import org.junit.Test;


/**
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CPCalendarTest extends TestCase {
    private CPCalendar calendar;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        calendar = new CPCalendar();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testShouldWrapAroundProperlyForLastDayOfTheWeek(){
        int[] days = new int[]{Calendar.SATURDAY, Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY};

        for(int i = 1; i < days.length; i++){
            calendar.setFirstDayOfWeek(days[i]);
            assertEquals("Should find the day before", days[i-1], calendar.getLastDayOfWeek());
        }
    }

    @Test
    public void testShouldListAllWorkDays(){
        int[] workDays = new int[]{Calendar.SATURDAY, Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY};
        calendar.setWorkWeekStartingDay(Calendar.SATURDAY);
        calendar.setWorkWeekDurationInDays(workDays.length);

        for(int workDay: workDays){
            assertTrue("Should be a work day: "+workDay, calendar.getWorkWeekDays().contains(Integer.valueOf(workDay)));
        }
        assertEquals("Should contain only the given days, not more: "+Arrays.toString(workDays), workDays.length , calendar.getWorkWeekDurationInDays());
    }

    @Test
    public void testShouldKnowFirstAndLastDayOfTheWeek(){
        calendar.setFirstDayOfWeek(Calendar.WEDNESDAY);
        assertEquals("First week day", Calendar.WEDNESDAY, calendar.getFirstDayOfWeek());
        assertEquals("Last week day", Calendar.TUESDAY, calendar.getLastDayOfWeek());

        Calendar date = Calendar.getInstance();
        date.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        assertTrue("Check for first day of week", calendar.isOnFirstDayOfWeek(date.getTime()));
        assertFalse(calendar.isOnLastDayOfWeek(date.getTime()));

        date.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        assertTrue("Check for last day of week", calendar.isOnLastDayOfWeek(date.getTime()));
        assertFalse(calendar.isOnFirstDayOfWeek(date.getTime()));
    }

    @Test
    public void testShouldKnowFirstAndLastWorkingDayOfTheWeek(){
        calendar.setWorkWeekStartingDay(Calendar.WEDNESDAY);
        calendar.setWorkWeekDurationInDays(5);
        assertEquals("First work week day", Calendar.WEDNESDAY, calendar.getFirstDayOfWorkWeek());
        assertEquals("Last work week day", Calendar.SUNDAY, calendar.getLastDayOfWorkWeek());

        Calendar date = Calendar.getInstance();
        date.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        assertTrue("Check for first day of work week", calendar.isOnFirstDayOfWorkWeek(date.getTime()));
        assertFalse(calendar.isOnLastDayOfWorkWeek(date.getTime()));

        date.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        assertTrue("Check for last day of work week", calendar.isOnLastDayOfWorkWeek(date.getTime()));
        assertFalse(calendar.isOnFirstDayOfWorkWeek(date.getTime()));
    }

}
