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

import com.openexchange.exception.OXException;
import static com.openexchange.groupware.calendar.tools.CommonAppointments.D;
import java.util.Date;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;


public class Bug11695Test extends CalendarSqlTest {
    /**
     * Test for <a href= "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=11695">bug #11695</a>
     *
     * @throws OXException If an OX error occurs
     */
    public void testShouldCalculateProperWeeklyRecurrence() throws OXException {
        final Date start = D("04/09/2008 22:00");
        final Date end = D("04/09/2008 23:00");
        // Create Weekly recurrence
        final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
        appointment.setRecurrenceType(CalendarDataObject.WEEKLY);
        appointment.setDays(CalendarDataObject.MONDAY + CalendarDataObject.FRIDAY);
        appointment.setTitle("testShouldCalculateProperWeeklyRecurrence");
        appointment.setInterval(1);
        appointment.setOccurrence(2);
        appointments.save(appointment);
        clean.add(appointment);
        // Check for 2 occurrences
        final RecurringResultsInterface results = getTools().calculateRecurring(appointment, 0, 0, 0);
        assertEquals("Unexpected size in recurring results of weekly recurrence appointment", 2, results.size());
        final RecurringResultInterface firstResult = results.getRecurringResult(0);
        assertEquals("Unexpected first occurrence", D("05/09/2008 22:00"), new Date(firstResult.getStart()));
        final RecurringResultInterface secondResult = results.getRecurringResult(1);
        assertEquals("Unexpected second occurrence", D("08/09/2008 22:00"), new Date(secondResult.getStart()));
    }

    /**
     * Another test for <a href= "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=11695">bug #11695</a>
     *
     * @throws OXException If an OX error occurs
     */
    public void testShouldCalculateProperWeeklyRecurrence2() throws OXException {
        final Date start = D("14/09/2008 22:00");
        final Date end = D("14/09/2008 23:00");
        // Create Weekly recurrence
        final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
        appointment.setRecurrenceType(CalendarDataObject.WEEKLY);
        appointment.setDays(62); // Monday til friday
        appointment.setTitle("testShouldCalculateProperWeeklyRecurrence2");
        appointment.setInterval(1);
        appointment.setUntil(new Date(1222041600000L));
        appointments.save(appointment);
        clean.add(appointment);
        // Check for 6 occurrences
        final long[] expectedLongs = new long[] {
            D("15/09/2008 22:00").getTime(), D("16/09/2008 22:00").getTime(), D("17/09/2008 22:00").getTime(),
            D("18/09/2008 22:00").getTime(), D("19/09/2008 22:00").getTime(), D("22/09/2008 22:00").getTime() };
        final RecurringResultsInterface results = getTools().calculateRecurring(appointment, 0, 0, 0);
        assertEquals("Unexpected size in recurring results of weekly recurrence appointment", expectedLongs.length, results.size());
        for (int i = 0; i < expectedLongs.length; i++) {
            assertEquals("Unexpected " + (i + 1) + " occurrence", new Date(expectedLongs[i]), new Date(
                results.getRecurringResult(i).getStart()));
        }
    }
}
