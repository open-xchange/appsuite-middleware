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
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.FolderTest;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.config.ConfigTools;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.reminder.ReminderObject;

public class UpdatesTest extends ReminderTest {

    public UpdatesTest(final String name) {
        super(name);
    }

    public void testRange() throws Exception {
        final int userId = ConfigTools.getUserId(getWebConversation(), getHostName(), getSessionId());
        final TimeZone timeZone = ConfigTools.getTimeZone(getWebConversation(), getHostName(), getSessionId());

        Calendar c = TimeTools.createCalendar(timeZone);
        c.add(Calendar.HOUR_OF_DAY, 2);

        final long startTime = c.getTimeInMillis();
        final long endTime = startTime + 3600000;

        final FolderObject folderObj = FolderTest.getStandardCalendarFolder(getWebConversation(), getHostName(), getSessionId());
        final int folderId = folderObj.getObjectID();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testRange");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setAlarm(45);
        appointmentObj.setParentFolderID(folderId);
        appointmentObj.setIgnoreConflicts(true);

        final int targetId = AppointmentTest.insertAppointment(
            getWebConversation(),
            appointmentObj,
            timeZone,
            getHostName(),
            getSessionId());
        final ReminderObject[] reminderObj = listUpdates(
            getWebConversation(),
            new Date(System.currentTimeMillis() - 5000),
            getHostName(),
            getSessionId(),
            timeZone);

        int pos = -1;
        for (int a = 0; a < reminderObj.length; a++) {
            if (reminderObj[a].getTargetId() == targetId) {
                pos = a;
            }
        }

        assertTrue("reminder not found in response", (pos > -1));

        assertTrue("object id not found", reminderObj[pos].getObjectId() > 0);
        assertNotNull("last modified is null", reminderObj[pos].getLastModified());
        assertEquals("target id is not equals", targetId, reminderObj[pos].getTargetId());
        assertEquals("folder id is not equals", folderId, reminderObj[pos].getFolder());
        assertEquals("user id is not equals", userId, reminderObj[pos].getUser());

        final long expectedAlarm = startTime - (45 * 60 * 1000);
        assertEquals("alarm is not equals", new Date(expectedAlarm), reminderObj[pos].getDate());

        final AJAXClient client = new AJAXClient(new AJAXSession(getWebConversation(), getHostName(), getSessionId()), false);
        final GetResponse aGetR = client.execute(new GetRequest(folderId, targetId));
        client.execute(new DeleteRequest(targetId, folderId, aGetR.getTimestamp()));
    }
}
