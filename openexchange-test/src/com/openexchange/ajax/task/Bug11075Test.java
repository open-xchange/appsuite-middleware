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
import java.util.Date;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.ajax.task.actions.AbstractTaskRequest;
import com.openexchange.ajax.task.actions.SearchRequest;
import com.openexchange.ajax.task.actions.SearchResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.server.impl.OCLPermission;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug11075Test extends AbstractTaskTest {

    /**
     * @param name
     */
    public Bug11075Test() {
        super();
    }

    /**
     * Creates two public task folder where the user has only permission to read
     * own objects. Then a SQL command for searching tasks failed.
     * 
     * @throws Throwable if some exception occurs.
     */
    @Test
    public void testBug() throws Throwable {
        final AJAXClient client = getClient();
        final InsertRequest[] inserts = new InsertRequest[2];
        for (int i = 0; i < inserts.length; i++) {
            inserts[i] = new InsertRequest(EnumAPI.OX_OLD, createFolder("Bug11075Test_" + i, client.getValues().getUserId()));
        }
        final MultipleResponse<InsertResponse> mInsert = client.execute(MultipleRequest.create(inserts));
        final int[] folderIds = new int[inserts.length];
        Date timestamp = new Date(0);
        for (int i = 0; i < folderIds.length; i++) {
            final CommonInsertResponse response = mInsert.getResponse(i);
            folderIds[i] = response.getId();
            if (response.getTimestamp().after(timestamp)) {
                timestamp = response.getTimestamp();
            }
        }
        try {
            final TaskSearchObject search = new TaskSearchObject();
            search.setPattern("");
            final SearchRequest request = new SearchRequest(search, AbstractTaskRequest.GUI_COLUMNS);
            final SearchResponse response = TaskTools.search(client, request);
            assertFalse("Searching over all folders failed.", response.hasError());
        } finally {
            client.execute(new DeleteRequest(EnumAPI.OX_OLD, folderIds, timestamp));
        }
    }

    private static final FolderObject createFolder(final String name, final int userId) {
        final FolderObject folder = new FolderObject();
        folder.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        folder.setFolderName(name);
        folder.setModule(FolderObject.TASK);
        folder.setType(FolderObject.PUBLIC);
        final OCLPermission perm1 = new OCLPermission();
        perm1.setEntity(userId);
        perm1.setGroupPermission(false);
        perm1.setFolderAdmin(true);
        perm1.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_OWN_OBJECTS, OCLPermission.WRITE_OWN_OBJECTS, OCLPermission.DELETE_OWN_OBJECTS);
        folder.setPermissionsAsArray(new OCLPermission[] { perm1 });
        return folder;
    }
}
