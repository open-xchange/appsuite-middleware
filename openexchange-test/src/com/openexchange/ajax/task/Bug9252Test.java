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

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;

/**
 * Tests problem described in bug #9295.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug9252Test extends AbstractTaskTest {

    private AJAXClient client1;

    private AJAXClient client2;

    /**
     * @param name
     */
    public Bug9252Test() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client1 = getClient();
        client2 = testUser2.getAjaxClient();
    }

    /**
     * Tests if tasks in public folders created by other users can be read.
     *
     * @throws Throwable if this test fails.
     */
    @Test
    public void testReadAccess() throws Throwable {
        // Create public folder.
        final FolderObject folder = Create.setupPublicFolder("Bug9295TaskFolder", FolderObject.TASK, client1.getValues().getUserId());
        folder.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        final CommonInsertResponse fInsertR = client1.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder));
        folder.setObjectID(fInsertR.getId());
        try {
            // Create a task in there.
            final Task task = com.openexchange.test.common.groupware.tasks.Create.createWithDefaults();
            task.setParentFolderID(folder.getObjectID());
            task.setTitle("Test bug #9295");
            final InsertResponse iResponse = client1.execute(new InsertRequest(task, client1.getValues().getTimeZone()));
            task.setObjectID(iResponse.getId());
            // Now second user tries to read the task.
            final GetResponse gResponse = client2.execute(new GetRequest(folder.getObjectID(), task.getObjectID()));
            final Task reload = gResponse.getTask(client2.getValues().getTimeZone());
            TaskTools.compareAttributes(task, reload);
        } finally {
            client1.execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD, folder.getObjectID(), new Date()));
        }
    }
}
