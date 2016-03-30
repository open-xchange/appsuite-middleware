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

import java.util.Date;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.reminder.actions.RangeRequest;
import com.openexchange.ajax.reminder.actions.RangeResponse;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.tasks.Task;

/**
 * Tests if problem described in bug 7377 appears again.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug7377Test extends AbstractTaskTest {

    private AJAXClient client1;
    private AJAXClient client2;
    private TimeZone tz1;
    private TimeZone tz2;
    private int folder1;
    private int folder2;

    public Bug7377Test(String name) {
        super(name);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client1 = getClient();
        client2 = new AJAXClient(AJAXClient.User.User2);
        tz1 = client1.getValues().getTimeZone();
        tz2 = client2.getValues().getTimeZone();
        folder1 = client1.getValues().getPrivateTaskFolder();
        folder2 = client2.getValues().getPrivateTaskFolder();
    }

    @After
    @Override
    protected void tearDown() throws Exception {
        if (null != client2) {
            client2.logout();
        }
        super.tearDown();
    }

    /**
     * Tests if on updating tasks the folder for the reminder gets lost.
     * @throws Throwable if this test fails.
     */
    public void testLostFolderInfo() throws Throwable {
        // Create a task.
        final Task task = new Task();
        task.setTitle("Test bug #7377");
        task.setParentFolderID(folder1);
        final InsertResponse iResponse = client1.execute(new InsertRequest(task, tz1));
        iResponse.fillTask(task);
        try {
            // Update timestamp
            final GetResponse gResponse = client1.execute(new GetRequest(getPrivateFolder(), task.getObjectID()));
            task.setLastModified(gResponse.getTimestamp());
            // Update task and insert reminder and don't send folder in task.
            task.setNote("Updated with reminder");
            final Date remindDate = new Date();
            task.setAlarm(remindDate);
            final UpdateResponse uResponse = client1.execute(new UpdateRequest(task, tz1));
            task.setLastModified(uResponse.getTimestamp());
            // Check reminder
            final RangeResponse rResponse = client1.execute(new RangeRequest(remindDate));
            
            if (rResponse.hasConflicts()) {
                System.out.println("Response has got " + rResponse.getConflicts().size() + " conflicts");
            }
            if (rResponse.getResponse() != null) {
            	System.out.println("There is a response!");
            	System.out.println("Response is " + rResponse.getResponse().getJSON());
                if (rResponse.hasError()) {
                    System.out.println("Response has got " + rResponse.getProblematics().length + " problematics");
                }
            	System.out.println("Response has got " + rResponse.getException() + " exceptions");
            }
            	
            
            ReminderObject[] reminder2 = rResponse.getReminder(tz1);
            assertTrue("Found no reminders for given timezone", 0 < reminder2.length);

            final ReminderObject reminder = rResponse.getReminderByTarget(tz1, task.getObjectID());
            assertNotNull("Can't find reminder for task.", reminder);
            assertNotSame("Found folder 0 for task reminder.", Integer.valueOf(0), Integer.valueOf(reminder.getFolder()));
        } finally {
            client1.execute(new DeleteRequest(task));
        }
    }

    public void testPublicFolderMove() throws Throwable {
        // Create task with 2 participants and reminder
        final Task task = new Task();
        task.setParentFolderID(folder1);
        task.setTitle("Test bug #7377");
        final Date remindDate = new Date();
        task.setAlarm(remindDate);
        final Participant[] parts = new Participant[] {
            new UserParticipant(client1.getValues().getUserId()),
            new UserParticipant(client2.getValues().getUserId())
        };
        task.setParticipants(parts);
        {
            InsertResponse response = client1.execute(new InsertRequest(task, tz1));
            response.fillTask(task);
        }
        FolderObject folder = null;
        try {
            // Check if user 2 sees the task.
            final Task task2;
            {
                GetResponse response = client2.execute(new GetRequest(folder2, task.getObjectID()));
                task2 = response.getTask(tz2);
            }
            // Update task and insert reminder and don't send folder in task.
            task2.setNote("Updated with reminder");
            task2.setAlarm(remindDate);
            {
                UpdateResponse response = client2.execute(new UpdateRequest(task2, tz2));
                task.setLastModified(response.getTimestamp());
            }
            // Create public folder
            {
                final FolderObject tmp = Create.setupPublicFolder("Bug7377TaskFolder1", FolderObject.TASK, client1.getValues().getUserId());
                tmp.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
                final CommonInsertResponse response = client1.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, tmp));
                tmp.setObjectID(response.getId());
                tmp.setLastModified(response.getTimestamp());
                folder = tmp;
            }
            // Now client1 moves to public folder.
            task.setNote("Moved to public");
            task.removeAlarm();
            task.setParentFolderID(folder.getObjectID());
            {
                UpdateResponse response = client1.execute(new UpdateRequest(folder1, task, tz1));
                task.setLastModified(response.getTimestamp());
            }
            // Check if task is there with user 2.
            client2.execute(new GetRequest(folder.getObjectID(), task.getObjectID()));
            // Check reminder
            {
                final RangeResponse response = client1.execute(new RangeRequest(remindDate));
                final ReminderObject reminder = response.getReminderByTarget(tz1, task.getObjectID());
                assertNotNull("Can't find reminder for task.", reminder);
                assertNotSame("Found folder 0 for task reminder.", Integer.valueOf(0), Integer.valueOf(reminder.getFolder()));
            }
            {
                final RangeResponse response = client2.execute(new RangeRequest(remindDate));
                final ReminderObject reminder = response.getReminderByTarget(tz2, task.getObjectID());
                assertNotNull("Can't find reminder for task.", reminder);
                assertNotSame("Found folder 0 for task reminder.", Integer.valueOf(0), Integer.valueOf(reminder.getFolder()));
            }
        } finally {
            client1.execute(new DeleteRequest(task));
            if (null != folder) {
                client1.execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD, folder.getObjectID(), folder.getLastModified()));
            }
        }
    }
}
