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

public class Bug4342Test extends ReminderTest {

    public Bug4342Test(final String name) {
        super(name);
    }

    public void testBug4342() throws Exception {
        final TimeZone timeZone = ConfigTools.getTimeZone(getWebConversation(), getHostName(), getSessionId());

        final Calendar c = TimeTools.createCalendar(timeZone);
        final Calendar rCal = TimeTools.createCalendar(timeZone);
        rCal.setTime(c.getTime());

        final FolderObject folderObj = FolderTest.getStandardCalendarFolder(getWebConversation(), getHostName(), getSessionId());
        final int folderId = folderObj.getObjectID();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testBug4342");
        appointmentObj.setStartDate(c.getTime());
        c.add(Calendar.HOUR, 1);
        appointmentObj.setEndDate(c.getTime());
        appointmentObj.setShownAs(Appointment.ABSENT);
        rCal.add(Calendar.MINUTE, -45);
        appointmentObj.setAlarm(45);
        appointmentObj.setParentFolderID(folderId);
        appointmentObj.setIgnoreConflicts(true);

        final int targetId = AppointmentTest.insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());

        final ReminderObject reminderObj = new ReminderObject();
        reminderObj.setTargetId(targetId);
        reminderObj.setFolder(folderId);
        reminderObj.setDate(rCal.getTime());

        ReminderObject[] reminderArray = listReminder(getWebConversation(), c.getTime(), timeZone, getHostName(), getSessionId());

        int pos = -1;
        for (int a = 0; a < reminderArray.length; a++) {
            if (reminderArray[a].getTargetId() == targetId) {
                pos = a;
                reminderObj.setObjectId(reminderArray[a].getObjectId());
                compareReminder(reminderObj, reminderArray[a]);
            }
        }

        appointmentObj.removeParentFolderID();
        rCal.setTime(appointmentObj.getStartDate());
        rCal.add(Calendar.MINUTE, -30);
        appointmentObj.setAlarm(30);

        reminderObj.setDate(rCal.getTime());

        AppointmentTest.updateAppointment(getWebConversation(), appointmentObj, targetId, folderId, timeZone, getHostName(), getSessionId());

        reminderArray = listReminder(getWebConversation(), c.getTime(), timeZone, getHostName(), getSessionId());

        pos = -1;
        for (int a = 0; a < reminderArray.length; a++) {
            if (reminderArray[a].getTargetId() == targetId) {
                pos = a;
                reminderObj.setObjectId(reminderArray[a].getObjectId());
                compareReminder(reminderObj, reminderArray[a]);
            }
        }

        deleteReminder(getWebConversation(), reminderArray[pos].getObjectId(), getHostName(), getSessionId());
        AppointmentTest.deleteAppointment(getWebConversation(), targetId, folderId, getHostName(), getSessionId(), false);
    }
}
