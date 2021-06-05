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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.ajax.task.actions.SearchRequest;
import com.openexchange.ajax.task.actions.SearchResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.common.groupware.tasks.Create;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * Checks if bug 11650 appears again.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug11650Test extends AbstractTaskTest {

    /**
     * Default constructor.
     *
     * @param name Name of the test.
     */
    public Bug11650Test() {
        super();
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    /**
     * Checks if the search in shared task folder is broken.
     *
     * @throws Throwable if an exception occurs.
     */
    @Test
    public void testSearchInSharedFolder() throws Throwable {
        final AJAXClient client = getClient();
        final AJAXClient client2 = testUser2.getAjaxClient();
        final int privateTaskFID = client.getValues().getPrivateTaskFolder();
        final FolderObject folder = createFolder(client.getValues().getUserId(), client2.getValues().getUserId());
        folder.setParentFolderID(privateTaskFID);
        // Share a folder.
        final CommonInsertResponse fResponse = client.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder));
        fResponse.fillObject(folder);
        final Task task = Create.createWithDefaults();
        task.setTitle("Bug11650Test");
        task.setParentFolderID(folder.getObjectID());
        try {
            {
                // Insert a task for searching for it.
                final InsertResponse insert = Executor.execute(client, new InsertRequest(task, client.getValues().getTimeZone()));
                insert.fillTask(task);
            }
            {
                // Search in that shared task folder.
                final TaskSearchObject search = new TaskSearchObject();
                search.setPattern("*");
                search.addFolder(folder.getObjectID());
                final SearchResponse response = Executor.execute(client2, new SearchRequest(search, SearchRequest.GUI_COLUMNS));
                assertThat("Searching for tasks in a shared folder failed.", !response.hasError());
            }
        } finally {
            client.execute(new DeleteRequest(EnumAPI.OX_OLD, folder.getObjectID(), folder.getLastModified()));
        }
    }

    private static FolderObject createFolder(final int owner, final int sharee) {
        assertNotEquals("Owner and sharee are the same. Sharing is not possible", owner, sharee);
        final FolderObject folder = new FolderObject();
        folder.setFolderName("Bug 11650 folder");
        folder.setModule(FolderObject.TASK);
        folder.setType(FolderObject.PRIVATE);
        final OCLPermission perm1 = new OCLPermission();
        perm1.setEntity(owner);
        perm1.setGroupPermission(false);
        perm1.setFolderAdmin(true);
        perm1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        final OCLPermission perm2 = new OCLPermission();
        perm2.setEntity(sharee);
        perm2.setGroupPermission(false);
        perm2.setFolderAdmin(false);
        perm2.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        folder.setPermissionsAsArray(new OCLPermission[] { perm1, perm2 });
        return folder;
    }
}
