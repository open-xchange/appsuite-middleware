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

import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.task.actions.UpdateRequest;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug12364Test extends AbstractAJAXSession {

    /**
     * Default constructor.
     */
    public Bug12364Test() {
        super();
    }

    @Test
    public void testMoveTasks() throws Throwable {
        final AJAXClient myClient = getClient();
        final TimeZone tz = myClient.getValues().getTimeZone();
        final FolderObject folder1;
        final FolderObject folder2;
        {
            folder1 = Create.createPublicFolder(myClient, "bug 12364 test folder 1", FolderObject.TASK);
            folder2 = Create.createPublicFolder(myClient, "bug 12364 test folder 2", FolderObject.TASK);
        }
        try {
            // Create tasks.
            final Task task1 = new Task();
            task1.setTitle("bug 12364 test 1");
            task1.setParentFolderID(folder1.getObjectID());
            final Task task2 = new Task();
            task2.setTitle("bug 12364 test 2");
            task2.setParentFolderID(folder2.getObjectID());
            TaskTools.insert(myClient, task1, task2);
            // Move them
            task1.setParentFolderID(folder2.getObjectID());
            task2.setParentFolderID(folder1.getObjectID());
            final UpdateRequest request1 = new UpdateRequest(folder1.getObjectID(), task1, tz);
            final UpdateRequest request2 = new UpdateRequest(folder2.getObjectID(), task2, tz);
            myClient.execute(MultipleRequest.create(new UpdateRequest[] { request1, request2
            }));
        } finally {
            myClient.execute(new DeleteRequest(EnumAPI.OX_OLD, folder1, folder2));
        }
    }
}
