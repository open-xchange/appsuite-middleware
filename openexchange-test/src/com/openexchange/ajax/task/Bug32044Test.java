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

package com.openexchange.ajax.task;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.reminder.actions.RangeRequest;
import com.openexchange.ajax.reminder.actions.RangeResponse;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.util.TimeZones;
import com.openexchange.server.impl.OCLPermission;

/**
 * Preconditions: Two users have access to a task. Both users created a reminder.
 * Verifies that a moved task gets second users reminders updates or deleted accordingly.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug32044Test extends AbstractAJAXSession {

    private AJAXClient client1, client2;
    private TimeZone timeZone1, timeZone2;
    private Calendar cal;
    private Task task;
    private FolderObject folder1, folder2;

    public Bug32044Test(String name) {
        super(name);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client1 = getClient();
        timeZone1 = client1.getValues().getTimeZone();
        client2 = new AJAXClient(User.User2);
        timeZone2 = client2.getValues().getTimeZone();
        cal = TimeTools.createCalendar(TimeZones.UTC);
        // Create a shared folder
        folder1 = com.openexchange.ajax.folder.Create.createPrivateFolder("test for bug 32044 folder 1", FolderObject.TASK, client1.getValues().getUserId(),
            com.openexchange.ajax.folder.Create.ocl(client2.getValues().getUserId(), false, false, OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS)
        );
        folder1.setParentFolderID(client1.getValues().getPrivateTaskFolder());
        CommonInsertResponse response = client1.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder1));
        folder1.setObjectID(response.getId());
        folder1.setLastModified(response.getTimestamp());
        // Create a not shared folder
        folder2 = com.openexchange.ajax.folder.Create.createPrivateFolder("test for bug 32044 folder 2", FolderObject.TASK, client1.getValues().getUserId());
        folder2.setParentFolderID(client1.getValues().getPrivateTaskFolder());
        response = client1.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder2));
        folder2.setObjectID(response.getId());
        folder2.setLastModified(response.getTimestamp());
        // Create a task in folder 1
        task = new Task();
        task.setParentFolderID(folder1.getObjectID());
        task.setTitle("Test for bug 32044");
        cal.set(Calendar.HOUR_OF_DAY, 0);
        task.setStartDate(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 1);
        task.setEndDate(cal.getTime());
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.add(Calendar.HOUR_OF_DAY, 1);
        task.setAlarm(cal.getTime());
        client1.execute(new InsertRequest(task, timeZone1)).fillTask(task);
        // User 2 creates a reminder for this task
        Task addReminder = new Task();
        addReminder.setObjectID(task.getObjectID());
        addReminder.setLastModified(task.getLastModified());
        addReminder.setParentFolderID(task.getParentFolderID());
        addReminder.setAlarm(cal.getTime());
        UpdateResponse updateResponse = client2.execute(new UpdateRequest(addReminder, timeZone2));
        task.setLastModified(updateResponse.getTimestamp());
        // end of reminder range request
        cal.add(Calendar.HOUR_OF_DAY, 1);
    }

    @After
    @Override
    protected void tearDown() throws Exception {
        task = client1.execute(new GetRequest(task)).getTask(timeZone1);
        client1.execute(new DeleteRequest(task));
        GetResponse response = client1.execute(new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_OLD, folder2.getObjectID(), false));
        if (!response.hasError()) {
            client1.execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD, folder2));
        }
        response = client1.execute(new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_OLD, folder1.getObjectID(), false));
        if (!response.hasError()) {
            client1.execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD, folder1));
        }
        client2.logout();
        super.tearDown();
    }

    @Test
    public void testForRemovedReminder() throws OXException, IOException, JSONException {
        RangeResponse rangeResponse = client2.execute(new RangeRequest(cal.getTime()));
        boolean found = false;
        for (ReminderObject reminder : rangeResponse.getReminder(timeZone2)) {
            if (reminder.getModule() == Types.TASK && reminder.getTargetId() == task.getObjectID() && reminder.getFolder() == folder1.getObjectID()) {
                found = true;
                break;
            }
        }
        assertTrue("User 2 can not see his own set reminder.", found);
        Task move = Create.cloneForUpdate(task);
        move.setParentFolderID(folder2.getObjectID());
        UpdateResponse response = client1.execute(new UpdateRequest(folder1.getObjectID(), move, timeZone1));
        task.setLastModified(response.getTimestamp());
        task.setParentFolderID(folder2.getObjectID());
        // User 2 tries to read now his reminders while the task was moved into a folder where he does not have access.
        rangeResponse = client2.execute(new RangeRequest(cal.getTime(), false));
        if (rangeResponse.hasError()) {
            fail("Reminder must be removed for second user after moving task into a not visible folder: " + rangeResponse.getErrorMessage());
        }
        found = false;
        for (ReminderObject reminder : rangeResponse.getReminder(timeZone2)) {
            if (reminder.getModule() == Types.TASK && reminder.getTargetId() == task.getObjectID() && reminder.getFolder() == folder1.getObjectID()) {
                found = true;
                break;
            }
        }
        assertFalse("User 2 should not have a reminder for the task anymore after moving it to a not visible folder.", found);
    }
}
