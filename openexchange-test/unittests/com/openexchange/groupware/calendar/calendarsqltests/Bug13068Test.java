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
import com.openexchange.api2.ReminderService;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.reminder.ReminderHandler;
import com.openexchange.tools.iterator.SearchIterator;


public class Bug13068Test extends CalendarSqlTest {
    /**
     * Test for <a href="http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=13068">bug #13068</a>
     */
    public void testRemoveReminderIfChangedIntoPast() throws Throwable {
        final long oneHour = 3600000;
        final long tomorrow = System.currentTimeMillis() + 24 * 3600000;
        final long yesterday = System.currentTimeMillis() - 24 * 3600000;

        CalendarDataObject appointment = appointments.buildBasicAppointment(new Date(tomorrow), new Date(tomorrow + oneHour));
        appointment.setTitle("Bug 13068 Test");
        appointment.setAlarm(5);
        appointment.setAlarmFlag(true);
        appointment.setIgnoreConflicts(true);
        appointments.save(appointment);
        clean.add(appointment);

        final ReminderService reminderInterface = new ReminderHandler(ctx);
        SearchIterator<?> iterator = reminderInterface.listReminder(Types.APPOINTMENT, appointment.getObjectID());

        assertTrue("Reminder expected", iterator.hasNext());

        CalendarDataObject updateAppointment = appointments.createIdentifyingCopy(appointment);
        updateAppointment.setStartDate(new Date(yesterday));
        updateAppointment.setEndDate(new Date(yesterday + oneHour));
        appointments.save(updateAppointment);

        iterator = reminderInterface.listReminder(Types.APPOINTMENT, appointment.getObjectID());

        assertFalse("No Reminder expected", iterator.hasNext());
    }
}
