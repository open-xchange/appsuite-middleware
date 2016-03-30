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

import java.util.TimeZone;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Create;
import com.openexchange.groupware.tasks.Task;

/**
 * checks if the problem described in bug report #11195 appears again.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug11195Test extends AbstractTaskTest {

    /**
     * Default constructor.
     * @param name name of the test.
     */
    public Bug11195Test(final String name) {
        super(name);
    }

    /**
     * Tries to move a task into some other task folder.
     * @throws Throwable if some exception occurs.
     */
    public void testMove() throws Throwable {
        final AJAXClient client = getClient();
        final int folder = client.getValues().getPrivateTaskFolder();
        final TimeZone tz = client.getValues().getTimeZone();
        final Task task = Create.createWithDefaults(folder, "Bug 11195 test");
        final FolderObject moveTo = com.openexchange.ajax.folder.Create
            .createPrivateFolder("Bug 11195 test", FolderObject.TASK, client.getValues().getUserId());
        moveTo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
        try {
            // Insert task
            {
                final InsertResponse response = client.execute(new InsertRequest(task, tz));
                response.fillTask(task);
            }
            // Create folder to move task to
            {
                final CommonInsertResponse response = client.execute(
                    new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, moveTo));
                moveTo.setObjectID(response.getId());
                moveTo.setLastModified(response.getTimestamp());
            }
            // Move task
            final Task move = new Task();
            {
                move.setObjectID(task.getObjectID());
                move.setParentFolderID(moveTo.getObjectID());
                move.setLastModified(task.getLastModified());
                final UpdateResponse response = TaskTools.update(client,
                    new UpdateRequest(task.getParentFolderID(), move, tz));
                task.setLastModified(response.getTimestamp());
            }
            // Try to get it from the destination folder
            {
                final GetResponse response = TaskTools.get(client, new GetRequest(
                    moveTo.getObjectID(), task.getObjectID(), false));
                assertFalse("Task was not moved.", response.hasError());
                task.setParentFolderID(moveTo.getObjectID());
            }
        } finally {
            if (null != task.getLastModified()) {
                client.execute(new DeleteRequest(task));
            }
            if (null != moveTo.getLastModified()) {
                client.execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD, moveTo.getObjectID(), moveTo.getLastModified()));
            }
        }
    }
}
