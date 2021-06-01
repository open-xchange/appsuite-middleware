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

package com.openexchange.ajax.task;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.ListRequest;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Task;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ListTest extends AbstractTaskTest {

    private static final int NUMBER = 10;
    private static final int DELETES = 2;

    @Test
    public void testTaskList() throws Throwable {
        final InsertRequest[] inserts = new InsertRequest[NUMBER];
        for (int i = 0; i < inserts.length; i++) {
            final Task task = new Task();
            task.setTitle("Task " + (i + 1));
            task.setParentFolderID(getPrivateFolder());
            inserts[i] = new InsertRequest(task, getTimeZone());
        }
        final MultipleResponse<InsertResponse> mInsert = getClient().execute(MultipleRequest.create(inserts));

        final int[][] tasks = new int[NUMBER][2];
        for (int i = 0; i < tasks.length; i++) {
            final InsertResponse insertR = mInsert.getResponse(i);
            tasks[i] = new int[] { insertR.getFolderId(), insertR.getId() };
        }
        final int[] columns = new int[] { Task.TITLE, Task.OBJECT_ID, Task.LAST_MODIFIED };
        final CommonListResponse listR = getClient().execute(new ListRequest(tasks, columns));
        final DeleteRequest[] deletes = new DeleteRequest[inserts.length];
        for (int i = 0; i < inserts.length; i++) {
            deletes[i] = new DeleteRequest(tasks[i][0], tasks[i][1], listR.getTimestamp());
        }
        getClient().execute(MultipleRequest.create(deletes));
    }

    public void oldRemovedObjectHandling() throws Throwable {
        final int[] columns = new int[] { Task.TITLE, Task.OBJECT_ID, Task.LAST_MODIFIED };
        final CommonListResponse listR = getClient().execute(new ListRequest(new int[][] { { getPrivateFolder(), Integer.MAX_VALUE } }, columns, false));
        assertTrue("No error when listing not existing object.", listR.hasError());
    }

    @Test
    public void testRemovedObjectHandling() throws Throwable {
        final int folderA = getClient().getValues().getPrivateTaskFolder();
        final AJAXClient clientB = testUser2.getAjaxClient();
        final int folderB = clientB.getValues().getPrivateTaskFolder();
        // Create some tasks.
        final InsertRequest[] inserts = new InsertRequest[NUMBER];
        for (int i = 0; i < inserts.length; i++) {
            final Task task = new Task();
            task.setTitle("Task " + (i + 1));
            task.setParentFolderID(folderA);
            task.addParticipant(new UserParticipant(getClient().getValues().getUserId()));
            task.addParticipant(new UserParticipant(clientB.getValues().getUserId()));
            inserts[i] = new InsertRequest(task, getTimeZone());
        }
        final MultipleResponse<InsertResponse> mInsert = getClient().execute(MultipleRequest.create(inserts));
        final List<InsertResponse> toDelete = new ArrayList<InsertResponse>(NUMBER);
        final Iterator<InsertResponse> iter = mInsert.iterator();
        while (iter.hasNext()) {
            toDelete.add(iter.next());
        }
        // A now gets all of the folder.
        final int[] columns = new int[] { Task.TITLE, Task.OBJECT_ID, Task.FOLDER_ID };
        final CommonAllResponse allR = getClient().execute(new AllRequest(folderA, columns, Task.TITLE, Order.ASCENDING));

        // Now B deletes some of them.
        final DeleteRequest[] deletes1 = new DeleteRequest[DELETES];
        for (int i = 0; i < deletes1.length; i++) {
            final InsertResponse insertR = toDelete.remove((NUMBER - DELETES) / 2 + i);
            deletes1[i] = new DeleteRequest(folderB, insertR.getId(), insertR.getTimestamp());
        }
        clientB.execute(MultipleRequest.create(deletes1));

        // List request of A must now not contain the deleted objects and give
        // no error.
        final CommonListResponse listR = getClient().execute(new ListRequest(allR.getListIDs(), columns, true));

        final DeleteRequest[] deletes2 = new DeleteRequest[toDelete.size()];
        for (int i = 0; i < deletes2.length; i++) {
            final InsertResponse insertR = toDelete.get(i);
            deletes2[i] = new DeleteRequest(insertR.getFolderId(), insertR.getId(), listR.getTimestamp());
        }
        getClient().execute(MultipleRequest.create(deletes2));
    }
}
