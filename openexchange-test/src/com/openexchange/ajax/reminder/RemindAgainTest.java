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
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.reminder.actions.RangeRequest;
import com.openexchange.ajax.reminder.actions.RemindAgainRequest;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link RemindAgainTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RemindAgainTest extends AbstractAJAXSession {

    /**
     * Default constructor.
     * @param name Test name.
     */
    public RemindAgainTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRemindAgain() throws Exception {
        final AJAXClient client = getClient();
        final int userId = client.getValues().getUserId();
        final TimeZone timeZone = client.getValues().getTimeZone();
        /*
         * Create task
         */
        final int folderId = client.getValues().getPrivateTaskFolder();
        final Task task = com.openexchange.groupware.tasks.Create.createTask();
        final Calendar c = TimeTools.createCalendar(timeZone);
        c.set(Calendar.HOUR_OF_DAY, 0);
        task.setParentFolderID(folderId);
        task.setTitle("testRemindAgain");
        task.setStartDate(c.getTime());
        c.add(Calendar.DAY_OF_YEAR, 1); // Due date: One day later
        task.setEndDate(c.getTime());
        c.add(Calendar.DAY_OF_YEAR, -3); // Reminder 2 days before start date
        task.setAlarm(c.getTime());
        c.add(Calendar.DAY_OF_YEAR, 2);

        Task reload = null;

        int targetId = -1;

        final com.openexchange.ajax.task.actions.InsertResponse insertR = client.execute(new com.openexchange.ajax.task.actions.InsertRequest(task, timeZone));
        try {
            targetId = insertR.getId();
            final com.openexchange.ajax.task.actions.GetResponse getR = com.openexchange.ajax.task.TaskTools.get(client, new com.openexchange.ajax.task.actions.GetRequest(folderId, targetId));
            reload = getR.getTask(timeZone);

            /*
             * Get reminder
             */
            ReminderObject[] reminderObjs = Executor.execute(client, new RangeRequest(c.getTime())).getReminder(timeZone);
            int pos = -1;
            for (int a = 0; a < reminderObjs.length; a++) {
                if (reminderObjs[a].getTargetId() == targetId) {
                    pos = a;
                }
            }

            assertTrue("reminder not found in response", (pos > -1));
            ReminderObject reminderObject = reminderObjs[pos];
            assertTrue("object id not found", reminderObject.getObjectId() > 0);
            assertNotNull("last modified is null", reminderObject.getLastModified());
            assertEquals("target id is not equal", targetId, reminderObject.getTargetId());
            assertEquals("folder id is not equal", folderId, reminderObject.getFolder());
            assertEquals("user id is not equal", userId, reminderObject.getUser());

            /*
             * Remind again
             */
            c.add(Calendar.DAY_OF_YEAR, -2); // Reminder
            final Date newAlarm = c.getTime();
            reminderObject.setDate(newAlarm);
            Executor.execute(client, new RemindAgainRequest(reminderObject));

            c.add(Calendar.DAY_OF_YEAR, 2);
            reminderObjs = Executor.execute(client, new RangeRequest(c.getTime())).getReminder(timeZone);
            pos = -1;
            for (int a = 0; a < reminderObjs.length; a++) {
                if (reminderObjs[a].getTargetId() == targetId) {
                    pos = a;
                }
            }

            assertTrue("reminder not found in response", (pos > -1));
            reminderObject = reminderObjs[pos];
            assertTrue("object id not found", reminderObject.getObjectId() > 0);
            assertNotNull("last modified is null", reminderObject.getLastModified());
            assertEquals("target id is not equal", targetId, reminderObject.getTargetId());
            assertEquals("folder id is not equal", folderId, reminderObject.getFolder());
            assertEquals("user id is not equal", userId, reminderObject.getUser());

            final Date timzonedNewAlarm = new Date(newAlarm.getTime());
            final int offset = timeZone.getOffset(timzonedNewAlarm.getTime());
            timzonedNewAlarm.setTime(timzonedNewAlarm.getTime() - offset);
            assertEquals("alarm is not equal", timzonedNewAlarm, reminderObject.getDate());

        } finally {
            if (null != reload) {
                /*
                 * Delete task
                 */
                reload = com.openexchange.ajax.task.TaskTools.get(client, new com.openexchange.ajax.task.actions.GetRequest(folderId, targetId)).getTask(timeZone);
                client.execute(new com.openexchange.ajax.task.actions.DeleteRequest(reload));
            }
        }
    }
}
