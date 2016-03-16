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

import java.util.Date;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.CalendarObject;


public class Bug12496Test extends CalendarSqlTest {
    /**
     * Test for <a href= "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=12496">bug #12496</a><br>
     * <i>NullPointerException if a daily full time series is changed to not full time</i>
     */
    public void testChangeFulltimeRecAppToNonFulltime() throws Exception {
        try {
            final CalendarDataObject fulltimeSeries = appointments.buildBasicAppointment(new Date(1225670400000L), new Date(1225756800000L));
            fulltimeSeries.setTitle("Fulltime-Recurring-Appointment");
            fulltimeSeries.setFullTime(true);
            fulltimeSeries.setRecurrenceType(CalendarObject.DAILY);
            fulltimeSeries.setInterval(1);
            fulltimeSeries.setOccurrence(5);
            // Save
            appointments.save(fulltimeSeries);
            clean.add(fulltimeSeries);
            // Change the recurring appointment to be non-fulltime
            final CalendarDataObject update = appointments.createIdentifyingCopy(fulltimeSeries);
            update.setFullTime(false);
            // 3. November 2008 08:00:00 UTC
            final Date newStart = new Date(1225699200000L);
            update.setStartDate(newStart);
            // 3. November 2008 10:00:00 UTC
            final Date newEnd = new Date(1225706400000L);
            update.setEndDate(newEnd);
            update.setRecurrenceType(CalendarObject.DAILY);
            update.setInterval(1);
            update.setOccurrence(5);
            // Save
            appointments.save(update);
            // Load first occurrence and verify
            final CalendarDataObject firstOccurrence = appointments.reload(fulltimeSeries);
            firstOccurrence.calculateRecurrence();
            final RecurringResultsInterface recuResults = getTools().calculateRecurring(
                firstOccurrence,
                0,
                0,
                1,
                CalendarCollection.MAX_OCCURRENCESE,
                true);
            if (recuResults.size() == 0) {
                fail("No occurrence at position " + 1);
            }
            final RecurringResultInterface result = recuResults.getRecurringResult(0);
            firstOccurrence.setStartDate(new Date(result.getStart()));
            firstOccurrence.setEndDate(new Date(result.getEnd()));
            firstOccurrence.setRecurrencePosition(result.getPosition());
            // Check against some expected values
            assertEquals("Unexpected start date: ", newStart, firstOccurrence.getStartDate());
            assertEquals("Unexpected end date: ", newEnd, firstOccurrence.getEndDate());
        } catch (final Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
