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

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonUpdatesResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.ajax.task.actions.UpdatesRequest;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Task;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class UpdatesTest extends AbstractTaskTest {

    private static final int UNTOUCHED = 3;

    private static final int UPDATES = 5;

    private static final int DELETES = 2;

    /**
     * @param name
     */
    public UpdatesTest(final String name) {
        super(name);
    }

    public void testUpdates() throws Throwable {
        final int total = UPDATES + UNTOUCHED + DELETES;
        final AJAXClient client = getClient();
        final int folderId = client.getPrivateTaskFolder();
        final TimeZone timeZone = client.getTimeZone();
        final InsertRequest[] inserts =
            new InsertRequest[total];
        for (int i = 0; i < inserts.length; i++) {
            final Task task = new Task();
            task.setParentFolderID(folderId);
            task.setTitle("Task " + (i + 1));
            inserts[i] = new InsertRequest(task, timeZone);
        }
        final MultipleResponse mInsert =  Executor.multiple(client,
            new MultipleRequest(inserts));
        int[] columns = new int[] { Task.TITLE, Task.OBJECT_ID, Task.FOLDER_ID };
        final CommonAllResponse allR = TaskTools.all(client, new AllRequest(
            folderId, columns, Task.TITLE, Order.ASCENDING));
        assertTrue("Can't find " + total + " inserted tasks.",
            allR.getArray().length >= total);
        // Now update 5
        final UpdateRequest[] updates = new UpdateRequest[UPDATES];
        for (int i = 0; i < updates.length; i++) {
            final Task task = new Task();
            task.setTitle("UpdatedTask " + (i + 1));
            final InsertResponse insertR = (InsertResponse) mInsert
            .getResponse(i);
            task.setObjectID(insertR.getId());
            task.setParentFolderID(folderId);
            task.setLastModified(insertR.getTimestamp());
            updates[i] = new UpdateRequest(task, timeZone);
        }
        final MultipleResponse mUpdate = Executor.multiple(client,
            new MultipleRequest(updates));
        // And delete 2
        final DeleteRequest[] deletes = new DeleteRequest[DELETES];
        for (int i = 0; i < deletes.length; i++) {
            final InsertResponse insertR = (InsertResponse) mInsert
                .getResponse(total - (i + 1));
            deletes[i] = new DeleteRequest(folderId, insertR.getId(), insertR
                .getTimestamp());
        }
        Executor.multiple(client, new MultipleRequest(deletes));
        // Now request updates for the list
        columns = new int[] { Task.OBJECT_ID, Task.FOLDER_ID, Task.TITLE,
            Task.START_DATE, Task.END_DATE, Task.PERCENT_COMPLETED,
            Task.PRIORITY };
        final CommonUpdatesResponse updatesR = TaskTools.updates(client,
            new UpdatesRequest(folderId, columns, 0, null, allR.getTimestamp()));
        // TODO add deletes.
        assertTrue("Only found " + updatesR.size()
            + " updated tasks but should be more than "
            + UPDATES + '.', updatesR.size() >= UPDATES);
        // Clean up
        final DeleteRequest[] deletes2 = new DeleteRequest[UPDATES + UNTOUCHED];
        for (int i = 0; i < deletes2.length; i++) {
            final InsertResponse insertR = (InsertResponse) mInsert
                .getResponse(i);
            final Date lastModified;
            if (i < UPDATES) {
                lastModified = ((UpdateResponse) mUpdate.getResponse(i))
                    .getTimestamp();
            } else {
                lastModified = insertR.getTimestamp();
            }
            deletes2[i] = new DeleteRequest(folderId, insertR.getId(),
                lastModified);
        }
        Executor.execute(client, new MultipleRequest(deletes2));
    }
}
