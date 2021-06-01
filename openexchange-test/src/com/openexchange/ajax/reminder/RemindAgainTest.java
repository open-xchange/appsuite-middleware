/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.reminder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.reminder.actions.RangeRequest;
import com.openexchange.ajax.reminder.actions.RemindAgainRequest;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link RemindAgainTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RemindAgainTest extends AbstractAJAXSession {

    @Test
    public void testRemindAgain() throws Exception {
        final AJAXClient client = getClient();
        final int userId = client.getValues().getUserId();
        final TimeZone timeZone = client.getValues().getTimeZone();
        /*
         * Create task
         */
        final int folderId = client.getValues().getPrivateTaskFolder();
        final Task task = com.openexchange.test.common.groupware.tasks.Create.createTask();
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
            final com.openexchange.ajax.task.actions.GetResponse getR = client.execute(new com.openexchange.ajax.task.actions.GetRequest(folderId, targetId));
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
                reload = client.execute(new com.openexchange.ajax.task.actions.GetRequest(folderId, targetId)).getTask(timeZone);
                client.execute(new com.openexchange.ajax.task.actions.DeleteRequest(reload));
            }
        }
    }
}
