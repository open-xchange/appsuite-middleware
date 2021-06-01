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
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
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

    public Bug10119Test() {
        super();
    }

    /**
     * Checks if the updates action works correctly if 1 item is deleted and
     * another created.
     * 
     * @throws Throwable if an exception occurs.
     */
    @Test
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
