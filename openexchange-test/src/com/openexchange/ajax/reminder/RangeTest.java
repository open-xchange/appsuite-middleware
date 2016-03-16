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
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.reminder.actions.RangeRequest;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.reminder.ReminderObject;

public class RangeTest extends AbstractAJAXSession {

    /**
     * Default constructor.
     * @param name Test name.
     */
    public RangeTest(final String name) {
        super(name);
    }

    public void testRange() throws Exception {
        final AJAXClient client = getClient();
        final int userId = client.getValues().getUserId();
        final TimeZone timeZone = client.getValues().getTimeZone();

        final Calendar c = TimeTools.createCalendar(timeZone);

        final int folderId = client.getValues().getPrivateAppointmentFolder();

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testRange");
        appointmentObj.setStartDate(c.getTime());
        c.add(Calendar.HOUR, 1);
        appointmentObj.setEndDate(c.getTime());
        c.add(Calendar.HOUR, -1);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setAlarm(45);
        appointmentObj.setParentFolderID(folderId);
        appointmentObj.setIgnoreConflicts(true);

        final CommonInsertResponse aInsertR = Executor.execute(client, new InsertRequest(appointmentObj, timeZone));
        final int targetId = aInsertR.getId();
        final ReminderObject[] reminderObj = Executor.execute(client, new RangeRequest(c.getTime())).getReminder(timeZone);

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

        c.add(Calendar.MINUTE, -45);
        final Date expected = c.getTime();
        assertEquals("alarm is not equals", expected, reminderObj[pos].getDate());

        final GetResponse aGetR = Executor.execute(client, new GetRequest(folderId, targetId));
        Executor.execute(client, new DeleteRequest(targetId, folderId, aGetR.getTimestamp()));
    }
}
