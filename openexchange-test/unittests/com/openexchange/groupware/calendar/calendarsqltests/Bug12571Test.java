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

package com.openexchange.groupware.calendar.calendarsqltests;

import static com.openexchange.groupware.calendar.tools.CommonAppointments.D;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;


public class Bug12571Test extends CalendarSqlTest {
    /**
     * Test for <a href= "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=12571">bug #12571</a><br>
     * <i>Yearly recurrence shifted by one day</i>
     */
    public void testProperOccurrencesOfYearlyApp() {
        try {
            {
                // Create yearly recurring appointment
                final CalendarDataObject appointment = appointments.buildBasicAppointment(D("01/11/2008 12:00"), D("01/11/2008 13:00"));
                appointment.setTitle("Test for bug #12571");
                appointment.setFullTime(false);
                appointment.setRecurrenceType(CalendarObject.YEARLY);
                appointment.setInterval(1);
                appointment.setDays(Appointment.DAY);
                appointment.setDayInMonth(1);
                appointment.setMonth(10);
                appointment.setOccurrence(10);
                appointments.save(appointment);
                clean.add(appointment);
                // Reload appointment for calculation
                final CalendarDataObject reloaded = appointments.reload(appointment);
                // Perform calculation
                final RecurringResultsInterface results = getTools().calculateRecurring(reloaded, 0, 0, 0);
                final int size = results.size();
                assertEquals("Unexpected number of recurring results", 10, size);
                final Calendar checker = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
                checker.setTimeInMillis(reloaded.getStartDate().getTime());
                int year = checker.get(Calendar.YEAR);
                for (int i = 0; i < size; i++) {
                    final RecurringResultInterface result = results.getRecurringResult(i);
                    checker.setTimeInMillis(result.getStart());
                    assertEquals("Unexpected day-of-month in " + (i + 1) + ". occurrence", 1, checker.get(Calendar.DAY_OF_MONTH));
                    assertEquals("Unexpected month in " + (i + 1) + ". occurrence", Calendar.NOVEMBER, checker.get(Calendar.MONTH));
                    assertEquals("Unexpected year in " + (i + 1) + ". occurrence", year++, checker.get(Calendar.YEAR));
                }
            }
            {
                // Create yearly recurring appointment
                final CalendarDataObject appointment = appointments.buildBasicAppointment(D("01/04/2008 12:00"), D("01/04/2008 13:00"));
                appointment.setTitle("Test for bug #12571");
                appointment.setFullTime(false);
                appointment.setRecurrenceType(CalendarObject.YEARLY);
                appointment.setInterval(1);
                appointment.setDays(Appointment.TUESDAY);
                appointment.setDayInMonth(1);
                appointment.setMonth(3);
                appointment.setOccurrence(10);
                appointments.save(appointment);
                clean.add(appointment);
                // Reload appointment for calculation
                final CalendarDataObject reloaded = appointments.reload(appointment);
                // Perform calculation
                final RecurringResultsInterface results = getTools().calculateRecurring(reloaded, 0, 0, 0);
                final int size = results.size();
                assertEquals("Unexpected number of recurring results", 10, size);
                final Calendar checker = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
                checker.setTimeInMillis(reloaded.getStartDate().getTime());
                int year = checker.get(Calendar.YEAR);
                for (int i = 0; i < size; i++) {
                    final RecurringResultInterface result = results.getRecurringResult(i);
                    checker.setTimeInMillis(result.getStart());
                    assertEquals(
                        "Unexpected day-of-week in " + (i + 1) + ". occurrence",
                        Calendar.TUESDAY,
                        checker.get(Calendar.DAY_OF_WEEK));
                    assertEquals("Unexpected month in " + (i + 1) + ". occurrence", Calendar.APRIL, checker.get(Calendar.MONTH));
                    assertEquals("Unexpected year in " + (i + 1) + ". occurrence", year++, checker.get(Calendar.YEAR));
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
