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
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractUpdatesRequest.Ignore;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.TaskUpdatesResponse;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.ajax.task.actions.UpdateResponse;
import com.openexchange.ajax.task.actions.UpdatesRequest;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
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
        final int folderId = client.getValues().getPrivateTaskFolder();
        final int userId = client.getValues().getUserId();
        final TimeZone timeZone = client.getValues().getTimeZone();
        final InsertRequest[] inserts = new InsertRequest[total];
        //keep track of the modified ids to verify the updates request
        final Set<Integer> expectedUpdatedTaskIds = new HashSet<Integer>(UPDATES);
        final Set<Integer> expectedDeletedTaskIds = new HashSet<Integer>(DELETES);

        for (int i = 0; i < inserts.length; i++) {
            final Task task = new Task();
            task.setParentFolderID(folderId);
            task.setTitle("Task " + (i + 1));
            task.setParticipants(new Participant[] { new UserParticipant(userId) });
            inserts[i] = new InsertRequest(task, timeZone);
        }
        final MultipleResponse<InsertResponse> mInsert = client.execute(
            MultipleRequest.create(inserts));
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
            final InsertResponse insertR = mInsert.getResponse(i);
            task.setObjectID(insertR.getId());
            expectedUpdatedTaskIds.add(insertR.getId());
            task.setParentFolderID(folderId);
            task.setLastModified(insertR.getTimestamp());
            updates[i] = new UpdateRequest(task, timeZone);
        }
        final MultipleResponse<UpdateResponse> mUpdate = client.execute(
            MultipleRequest.create(updates));

        // And delete 2
        final DeleteRequest[] deletes = new DeleteRequest[DELETES];
        for (int i = 0; i < deletes.length; i++) {
            final InsertResponse insertR = mInsert.getResponse(total - (i + 1));
            deletes[i] = new DeleteRequest(folderId, insertR.getId(), insertR
                .getTimestamp());
            expectedDeletedTaskIds.add(insertR.getId());
        }
        client.execute(MultipleRequest.create(deletes));

        // Now request updates for the list
        columns = new int[] { Task.OBJECT_ID, Task.FOLDER_ID, Task.TITLE,
            Task.START_DATE, Task.END_DATE, Task.NOTE, Task.ALARM, Task.PERCENT_COMPLETED,
            Task.PRIORITY, Task.PARTICIPANTS, Task.STATUS };
        final TaskUpdatesResponse updatesR = client.execute(new UpdatesRequest(folderId, columns, 0, null, allR.getTimestamp(), Ignore.NONE));
        assertTrue("Only found " + updatesR.size()
            + " updated tasks but should be more than "
            + (UPDATES + DELETES) + '.', updatesR.size() >= UPDATES + DELETES);

        //Check exactly if above done updates and deletes are found.
        Set<Integer> newOrModifiedIds = updatesR.getNewOrModifiedIds();
        Set<Integer> deletedIds = updatesR.getDeletedIds();
        assertTrue(newOrModifiedIds.containsAll(expectedUpdatedTaskIds));
        assertTrue(deletedIds.containsAll(expectedDeletedTaskIds));

        // Clean up
        final DeleteRequest[] deletes2 = new DeleteRequest[UPDATES + UNTOUCHED];
        for (int i = 0; i < deletes2.length; i++) {
            final InsertResponse insertR = mInsert
                .getResponse(i);
            final Date lastModified;
            if (i < UPDATES) {
                lastModified = mUpdate.getResponse(i).getTimestamp();
            } else {
                lastModified = insertR.getTimestamp();
            }
            deletes2[i] = new DeleteRequest(folderId, insertR.getId(),
                lastModified);
        }
        client.execute(MultipleRequest.create(deletes2));
    }


}
