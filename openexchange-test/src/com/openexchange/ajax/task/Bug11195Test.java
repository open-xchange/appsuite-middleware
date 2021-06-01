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

import static org.junit.Assert.assertFalse;
import java.util.TimeZone;
import org.junit.Test;
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
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.common.groupware.tasks.Create;

/**
 * checks if the problem described in bug report #11195 appears again.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug11195Test extends AbstractTaskTest {

    /**
     * Default constructor.
     *
     * @param name name of the test.
     */
    public Bug11195Test() {
        super();
    }

    /**
     * Tries to move a task into some other task folder.
     *
     * @throws Throwable if some exception occurs.
     */
    @Test
    public void testMove() throws Throwable {
        final AJAXClient client = getClient();
        final int folder = client.getValues().getPrivateTaskFolder();
        final TimeZone tz = client.getValues().getTimeZone();
        final Task task = Create.createWithDefaults(folder, "Bug 11195 test");
        final FolderObject moveTo = com.openexchange.ajax.folder.Create.createPrivateFolder("Bug 11195 test", FolderObject.TASK, client.getValues().getUserId());
        moveTo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
        try {
            // Insert task
            {
                final InsertResponse response = client.execute(new InsertRequest(task, tz));
                response.fillTask(task);
            }
            // Create folder to move task to
            {
                final CommonInsertResponse response = client.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, moveTo));
                moveTo.setObjectID(response.getId());
                moveTo.setLastModified(response.getTimestamp());
            }
            // Move task
            final Task move = new Task();
            {
                move.setObjectID(task.getObjectID());
                move.setParentFolderID(moveTo.getObjectID());
                move.setLastModified(task.getLastModified());
                final UpdateResponse response = client.execute(new UpdateRequest(task.getParentFolderID(), move, tz));
                task.setLastModified(response.getTimestamp());
            }
            // Try to get it from the destination folder
            {
                final GetResponse response = client.execute(new GetRequest(moveTo.getObjectID(), task.getObjectID(), false));
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
