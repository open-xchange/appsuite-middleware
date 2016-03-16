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

package com.openexchange.webdav.xml.task;

import java.util.Date;
import java.util.Locale;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.GroupUserTest;
import com.openexchange.webdav.xml.TaskTest;

public class NewTest extends TaskTest {

    public NewTest(final String name) {
        super(name);
    }

    public void testNewTask() throws Exception {
        final Task taskObj = createTask("testNewTask");
        insertTask(webCon, taskObj, PROTOCOL + hostName, login, password, context);
    }

    public void testNewTaskWithParticipants() throws Exception {
        final Task taskObj = createTask("testNewTaskWithParticipants");

        final int userParticipantId2 = GroupUserTest.getUserId(getWebConversation(), PROTOCOL + getHostName(), userParticipant2, getPassword(), context);
        assertTrue("user participant not found", userParticipantId2 != -1);
        final int userParticipantId3 = GroupUserTest.getUserId(getWebConversation(), PROTOCOL + getHostName(), userParticipant3, getPassword(), context);
        assertTrue("user participant not found", userParticipantId3 != -1);

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[1];
        participants[0] = new UserParticipant();
        participants[0].setIdentifier(userParticipantId2);
        participants[0] = new UserParticipant();
        participants[0].setIdentifier(userParticipantId3);

        taskObj.setParticipants(participants);

        insertTask(webCon, taskObj, PROTOCOL + hostName, login, password, context);
    }

    public void testNewTaskWithAlarm() throws Exception {
        final Task taskObj = createTask("testNewTaskWithAlarm");
        taskObj.setAlarm(new Date(startTime.getTime()-(2*dayInMillis)));
        final int objectId = insertTask(webCon, taskObj, PROTOCOL + hostName, login, password, context);
        taskObj.setObjectID(objectId);
        final Task loadTask = loadTask(getWebConversation(), objectId, taskFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
        compareObject(taskObj, loadTask);
        final int[][] objectIdAndFolderId = { {objectId, taskFolderId } };
        deleteTask(getWebConversation(), objectIdAndFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
    }

    public void testNewTaskWithUsers() throws Exception {
        final Task taskObj = createTask("testNewTaskWithUsers");

        final int userParticipantId = GroupUserTest.getUserId(getWebConversation(), PROTOCOL + getHostName(), userParticipant2, getPassword(), context);
        assertTrue("user participant not found", userParticipantId != -1);

        final UserParticipant[] users = new UserParticipant[1];
        users[0] = new UserParticipant();
        users[0].setIdentifier(userParticipantId);
        users[0].setConfirm(CalendarObject.ACCEPT);

        taskObj.setUsers(users);

        insertTask(webCon, taskObj, PROTOCOL + hostName, login, password, context);
    }

    public void testTaskWithPrivateFlagInPublicFolder() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testTaskWithPrivateFlagInPublicFolder" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.TASK);
        folderObj.setType(FolderObject.PUBLIC);
        folderObj.setParentFolderID(2);

        final OCLPermission[] permission = new OCLPermission[] {
            FolderTest.createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION)
        };

        folderObj.setPermissionsAsArray( permission );

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);

        final Task taskObj = new Task();
        taskObj.setTitle("testTaskWithPrivateFlagInPublicFolder");
        taskObj.setPrivateFlag(true);
        taskObj.setParentFolderID(parentFolderId);

        try {
            final int objectId = insertTask(getWebConversation(), taskObj, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
            deleteTask(getWebConversation(), objectId, parentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword(), context);
            fail("conflict exception expected!");
        } catch (final OXException exc) {
        	assertExceptionMessage(exc.getDisplayMessage(Locale.ENGLISH), "TSK-0008");
        }
    }
    // Bug 12011
    public void testBulkAdd() throws Exception {
        int[] objectIds = null;

        final int NUMBER_OF_TASKS = 30;
        final Task[] tasks = new Task[NUMBER_OF_TASKS];

        for(int i = 0; i < NUMBER_OF_TASKS; i++) {
            tasks[i] = createTask("TASK - "+i);
        }

        try {

            objectIds = insertTasks(webCon, PROTOCOL + hostName, login, password, context, tasks);

            int i = 0;

            for(final int objectId : objectIds) {
                final Task task = loadTask(getWebConversation(), objectId, tasks[i].getParentFolderID(), PROTOCOL + getHostName(), getLogin(), getPassword(), context);
                tasks[i].setObjectID(objectId);
                compareObject(task, tasks[i]);
                i++;

            }

        } finally {
            if(null != objectIds) {
                int i = 0;
                try {
                    for(final int objectId : objectIds) {
                        deleteTask(getWebConversation(), objectId, tasks[i].getParentFolderID(), PROTOCOL + getHostName(), getLogin(), getPassword(), context);
                        i++;
                    }
                } catch (final Exception x) {
                    x.printStackTrace(); // Not that interesting
                }
            }
        }
    }
}
