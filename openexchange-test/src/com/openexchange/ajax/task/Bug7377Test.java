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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.reminder.ReminderTools;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.OCLPermission;

/**
 * Tests problem described in bug #7377.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug7377Test extends AbstractTaskTest {

    private AJAXClient client1;
    
    /**
     * @param name
     */
    public Bug7377Test(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client1 = new AJAXClient(getSession());
    }

    /**
     * Tests if on updating tasks the folder for the reminder gets lost.
     * @throws Throwable if this test fails.
     */
    public void testLostFolderInfo() throws Throwable {
        // Create a task.
        final Task task = new Task();
        task.setTitle("Test bug #8504");
        final int folderId = getPrivateTaskFolder();
        task.setParentFolderID(folderId);
        final AJAXSession session = getSession();
        final InsertResponse iResponse = TaskTools.insert(session,
            new InsertRequest(task, client1.getTimeZone()));
        task.setObjectID(iResponse.getId());
        try {
            // Update timestamp
            final GetResponse gResponse = TaskTools.get(session,
                new GetRequest(getPrivateTaskFolder(), task.getObjectID()));
            task.setLastModified(gResponse.getTimestamp());
            // Update task and insert reminder and don't send folder in task.
            task.setNote("Updated with reminder");
            final Date remindDate = new Date();
            task.setAlarm(remindDate);
            task.getParentFolderID();
            task.removeParentFolderID();
            final UpdateResponse uResponse = TaskTools.update(session,
                new SpecialUpdateRequest(folderId, task, client1.getTimeZone()));
            task.setLastModified(uResponse.getTimestamp());
            // Check reminder
            final com.openexchange.ajax.reminder.actions.GetResponse rResponse =
                ReminderTools.get(session, new com.openexchange.ajax.reminder
                .actions.GetRequest(remindDate));
            final ReminderObject reminder = rResponse.getReminderByTarget(
                client1.getTimeZone(), task.getObjectID());
    
            assertNotNull("Can't find reminder for task.", reminder);
            assertNotSame("Found folder 0 for task reminder.", 0, Integer
                .parseInt(reminder.getFolder()));
        } finally {
            final Date lastModified = task.containsLastModified() ? task
                .getLastModified() : new Date();
            TaskTools.delete(session, new DeleteRequest(folderId, task
                .getObjectID(), lastModified));
        }
    }

    public void testPublicFolderMove() throws Throwable {
        
//        final FolderObject public1 = new FolderObject();
//        public1.setFolderName("Bug7377TaskFolder1");
//        public1.setModule(FolderObject.TASK);
//        public1.setType(FolderObject.PUBLIC);
//        final OCLPermission perm1 = new OCLPermission();
//        perm1.setEntity(entity);
//        
//        public1.setPermissionsAsArray(new OCLPermission[] {
//            new OCLPermission()
//        });
    }
    
    private static class SpecialUpdateRequest extends UpdateRequest {

        private final int folderId;
        
        public SpecialUpdateRequest(final int folderId, final Task task,
            final TimeZone timeZone) {
            super(task, timeZone);
            this.folderId = folderId;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Parameter[] getParameters() {
            return new Parameter[] {
                new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet
                    .ACTION_UPDATE),
                new Parameter(AJAXServlet.PARAMETER_INFOLDER, String.valueOf(
                    folderId)),
                new Parameter(AJAXServlet.PARAMETER_ID, String.valueOf(getTask()
                    .getObjectID())),
                new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(
                    getTask().getLastModified().getTime()))
            };
        }
    }
}
