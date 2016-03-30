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

package com.openexchange.ajax.reminder;

import java.util.Calendar;
import java.util.TimeZone;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.FolderTest;
import com.openexchange.ajax.config.ConfigTools;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.reminder.ReminderObject;

public class DeleteTest extends ReminderTest {

    public DeleteTest(final String name) {
        super(name);
    }

    public void testDelete() throws Exception {
        final TimeZone timeZone = ConfigTools.getTimeZone(getWebConversation(), getHostName(), getSessionId());

        final Calendar c = TimeTools.createCalendar(timeZone);

        final FolderObject folderObj = FolderTest.getStandardCalendarFolder(getWebConversation(), getHostName(), getSessionId());
        final int folderId = folderObj.getObjectID();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testDelete");
        appointmentObj.setStartDate(c.getTime());
        c.add(Calendar.HOUR, 1);
        appointmentObj.setEndDate(c.getTime());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setAlarm(45);
        appointmentObj.setParentFolderID(folderId);
        appointmentObj.setIgnoreConflicts(true);

        final int targetId = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());

        final ReminderObject[] reminderObj = listReminder(getWebConversation(), c.getTime(), timeZone, getHostName(), getSessionId());

        int pos = -1;
        for (int a = 0; a < reminderObj.length; a++) {
            if (reminderObj[a].getTargetId() == targetId) {
                pos = a;
            }
        }
        assertNotSame("Reminder not found.", -1, pos);
        deleteReminder(getWebConversation(), reminderObj[pos].getObjectId(), getHostName(), getSessionId());
        AppointmentTest.deleteAppointment(getWebConversation(), targetId, folderId, getHostName(), getSessionId(), false);
    }

    public void testDeleteWithNonExisting() throws Exception {
        final TimeZone timeZone = ConfigTools.getTimeZone(getWebConversation(), getHostName(), getSessionId());

        final Calendar c = TimeTools.createCalendar(timeZone);

        final FolderObject folderObj = FolderTest.getStandardCalendarFolder(getWebConversation(), getHostName(), getSessionId());
        final int folderId = folderObj.getObjectID();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testDeleteWithNonExisting");
        appointmentObj.setStartDate(c.getTime());
        c.add(Calendar.HOUR, 1);
        appointmentObj.setEndDate(c.getTime());
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setAlarm(45);
        appointmentObj.setParentFolderID(folderId);
        appointmentObj.setIgnoreConflicts(true);

        final int targetId = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());

        final ReminderObject[] reminderObj = listReminder(getWebConversation(), c.getTime(), timeZone, getHostName(), getSessionId());

        int pos = -1;
        for (int a = 0; a < reminderObj.length; a++) {
            if (reminderObj[a].getTargetId() == targetId) {
                pos = a;
            }
        }
        assertNotSame("Reminder not found.", -1, pos);
        final int[] failedObjects = deleteReminder(getWebConversation(), reminderObj[pos].getObjectId()+1000, getHostName(), getSessionId());
        assertTrue("failed object size is not > 0", failedObjects.length > 0);
        assertEquals("fail object id not equals expected", reminderObj[pos].getObjectId()+1000, failedObjects[0]);

        AppointmentTest.deleteAppointment(getWebConversation(), targetId, folderId, getHostName(), getSessionId(), false);
    }
}
