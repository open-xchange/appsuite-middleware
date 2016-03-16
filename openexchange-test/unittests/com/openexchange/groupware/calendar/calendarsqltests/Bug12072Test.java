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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.Constants;

public class Bug12072Test extends CalendarSqlTest {

    /**
     * Test for <a href= "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=12072">bug #12072</a>
     *
     * @throws OXException If an OX error occurs
     */
    public void testShouldNotIndicateConflictingResources() throws OXException {
        final long today = getTools().normalizeLong(System.currentTimeMillis());
        final int weekDayOfToday, weekDayOfTomorrow;
        final Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(today);
        weekDayOfToday = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DAY_OF_WEEK, 1);
        weekDayOfTomorrow = cal.get(Calendar.DAY_OF_WEEK);
        Date start = new Date(today + (10 * Constants.MILLI_HOUR));
        Date end = new Date(today + (11 * Constants.MILLI_HOUR));
        // Create Weekly recurrence
        final CalendarDataObject appointment = appointments.buildAppointmentWithResourceParticipants(resource1);
        appointment.setParentFolderID(appointments.getPrivateFolder());
        appointment.setIgnoreConflicts(true);
        appointment.setStartDate(start);
        appointment.setEndDate(end);
        appointment.setContext(ctx);
        appointment.setTimezone("utc");
        appointment.setRecurrenceType(CalendarDataObject.WEEKLY);
        appointment.setDays(convertCalendarDAY_OF_WEEK2CalendarDataObjectDAY_OF_WEEK(weekDayOfToday));
        appointment.setTitle("Everything can happen on a X-day");
        appointment.setInterval(1);
        appointment.setOccurrence(5);
        appointments.save(appointment);
        clean.add(appointment);
        // Now create a second weekly recurrence with demanding resource on
        // following day which should not indicate any conflicts
        start = new Date(start.getTime() + Constants.MILLI_DAY);
        end = new Date(end.getTime() + Constants.MILLI_DAY);
        final CalendarDataObject update = appointments.buildAppointmentWithResourceParticipants(resource1);
        update.setParentFolderID(appointments.getPrivateFolder());
        update.setIgnoreConflicts(true);
        update.setStartDate(start);
        update.setEndDate(end);
        update.setContext(ctx);
        update.setTimezone("utc");
        update.setRecurrenceType(CalendarDataObject.WEEKLY);
        update.setDays(convertCalendarDAY_OF_WEEK2CalendarDataObjectDAY_OF_WEEK(weekDayOfTomorrow));
        update.setTitle("Everything can happen on a X1-day");
        update.setInterval(1);
        update.setOccurrence(5);
        final CalendarDataObject[] conflicts = appointments.save(update);
        clean.add(update);
        assertTrue("", conflicts == null || conflicts.length == 0);
    }
}
