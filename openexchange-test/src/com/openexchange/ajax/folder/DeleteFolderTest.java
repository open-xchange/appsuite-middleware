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

package com.openexchange.ajax.folder;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link DeleteFolderTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DeleteFolderTest extends AbstractAJAXSession {

    private FolderObject testFolder;
    private int parentId = -1;

    /**
     * Initializes a new {@link DeleteFolderTest}.
     *
     * @param name name of the test.
     */
    public DeleteFolderTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Create folder
        final OCLPermission perm1 = Create.ocl(getClient().getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        final OCLPermission perm2 = Create.ocl(testUser2.getAjaxClient().getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        parentId = FolderObject.SYSTEM_PUBLIC_FOLDER_ID;
        testFolder = Create.folder(parentId, "TestDeletion" + UUID.randomUUID().toString(), FolderObject.CALENDAR, FolderObject.PUBLIC, perm1, perm2);
        InsertRequest insFolder = new InsertRequest(EnumAPI.OX_OLD, testFolder);
        InsertResponse folderInsertResponse = testUser2.getAjaxClient().execute(insFolder);
        testFolder.setObjectID(folderInsertResponse.getId());
        testFolder.setLastModified(testUser2.getAjaxClient().execute(new GetRequest(EnumAPI.OX_OLD, testFolder.getObjectID())).getTimestamp());
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testUnauthorizedDeletion() throws Throwable {
        final int folderId = testFolder.getObjectID();

        final List<FolderObject> l = FolderTools.getSubFolders(getClient(), Integer.toString(FolderObject.SYSTEM_PUBLIC_FOLDER_ID), true);
        assertTrue("No public subfolders available for user " + getClient().getValues().getUserId(), l != null && !l.isEmpty());

        boolean found = false;
        Next: for (FolderObject subfolder : l) {
            if (subfolder.getObjectID() == folderId) {
                found = true;
                break Next;
            }
        }
        assertTrue("Folder " + folderId + " not beneath public folder of user " + getClient().getValues().getUserId(), found);

        GetRequest getQ = new GetRequest(EnumAPI.OX_OLD, folderId);
        GetResponse getR = testUser2.getAjaxClient().execute(getQ);
        FolderObject origFolder = getR.getFolder();
        List<OCLPermission> permissions = new ArrayList<OCLPermission>();
        permissions.addAll(origFolder.getPermissions());
        Iterator<OCLPermission> iter = permissions.iterator();
        while (iter.hasNext()) {
            if (iter.next().getEntity() == getClient().getValues().getUserId()) {
                iter.remove();
            }
        }

        FolderObject changed = new FolderObject();
        changed.setObjectID(folderId);
        changed.setLastModified(getR.getTimestamp());
        changed.setPermissions(permissions);
        UpdateRequest updQ = new UpdateRequest(EnumAPI.OX_OLD, changed);
        testUser2.getAjaxClient().execute(updQ);

        getQ = new GetRequest(EnumAPI.OX_OLD, folderId);
        getR = testUser2.getAjaxClient().execute(getQ);
        origFolder = getR.getFolder();
        origFolder.setLastModified(getR.getTimestamp());

        // Delete should fail
        CommonDeleteResponse deleteResponse = getClient().execute(new DeleteRequest(EnumAPI.OX_OLD, false, origFolder).setFailOnErrorParam(Boolean.TRUE));
        assertTrue("Delete attempt should have failed", deleteResponse.hasError());

    }

}
