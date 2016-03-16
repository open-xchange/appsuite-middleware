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
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractUpdatesRequest.Ignore;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.TaskUpdatesResponse;
import com.openexchange.ajax.task.actions.UpdatesRequest;
import com.openexchange.groupware.tasks.Task;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug10119Test extends AbstractTaskTest {

    public Bug10119Test(final String name) {
        super(name);
    }

    /**
     * Checks if the updates action works correctly if 1 item is deleted and
     * another created.
     * @throws Throwable if an exception occurs.
     */
    public void testFunambol() throws Throwable {
        final AJAXClient client = getClient();
        final int folderId = client.getValues().getPrivateTaskFolder();
        final TimeZone timeZone = client.getValues().getTimeZone();
        // If the insert is really,really fast it is on the same time stamp as this server time stamp and the first task is missing in the
        // updates request because that one checks greater and not greater or equal.
        final Date beforeInsert = new Date(client.getValues().getServerTime().getTime() - 1);
        final MultipleResponse<InsertResponse> mInsert;
        {
            final InsertRequest[] initialInserts = new InsertRequest[2];
            for (int i = 0; i < initialInserts.length; i++) {
                final Task task = new Task();
                task.setParentFolderID(folderId);
                task.setTitle("Initial" + (i + 1));
                initialInserts[i] = new InsertRequest(task, timeZone);
            }
            mInsert = client.execute(MultipleRequest.create(initialInserts));
        }
        final int[] columns = new int[] { Task.TITLE, Task.OBJECT_ID, Task.FOLDER_ID };
        final TaskUpdatesResponse uResponse;
        {
            final UpdatesRequest uRequest = new UpdatesRequest(folderId, columns, 0, null, new Date(beforeInsert.getTime() - 1));
            uResponse = client.execute(uRequest);
            assertTrue("Can't find initial inserts. Only found " + uResponse.size() + " changed tasks.", uResponse.size() >= 2);
        }
        // Delete one
        {
            final InsertResponse secondInsert = mInsert.getResponse(1);
            client.execute(new DeleteRequest(folderId, secondInsert.getId(), secondInsert.getTimestamp()));
        }
        // Insert one
        final InsertResponse iResponse;
        {
            final Task task = new Task();
            task.setParentFolderID(folderId);
            task.setTitle("anotherInsert");
            iResponse = client.execute(new InsertRequest(task, timeZone));
        }
        // Check if we see 2 updates, 1 insert and 1 delete.
        {
            final TaskUpdatesResponse uResponse2 = client.execute(new UpdatesRequest(folderId, columns, 0, null, uResponse.getTimestamp(), Ignore.NONE));
            assertTrue("Can't get created and deleted item.", uResponse2.size() >= 2);
        }
        // Delete all.
        {
            final DeleteRequest[] deletes = new DeleteRequest[2];
            final InsertResponse firstInsert = mInsert.getResponse(0);
            deletes[0] = new DeleteRequest(folderId, firstInsert.getId(), firstInsert.getTimestamp());
            deletes[1] = new DeleteRequest(folderId, iResponse.getId(), iResponse.getTimestamp());
            client.execute(MultipleRequest.create(deletes));
        }
    }
}
