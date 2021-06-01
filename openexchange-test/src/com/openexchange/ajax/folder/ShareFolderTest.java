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
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.Abstrac2UserAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link ShareFolderTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ShareFolderTest extends Abstrac2UserAJAXSession {

    private FolderObject testFolder;
    private int parentId = -1;

    /**
     * Initializes a new {@link ShareFolderTest}.
     *
     * @param name name of the test.
     */
    public ShareFolderTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Create folder
        final OCLPermission perm1 = Create.ocl(getClient().getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        final OCLPermission perm2 = Create.ocl(client2.getValues().getUserId(), false, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        parentId = getClient().getValues().getPrivateAppointmentFolder();
        testFolder = Create.folder(parentId, "TestShared" + UUID.randomUUID().toString(), FolderObject.CALENDAR, FolderObject.PRIVATE, perm1, perm2);
        InsertRequest insFolder = new InsertRequest(EnumAPI.OX_OLD, testFolder);
        InsertResponse folderInsertResponse = getClient().execute(insFolder);
        testFolder.setObjectID(folderInsertResponse.getId());
        testFolder.setLastModified(getClient().execute(new GetRequest(EnumAPI.OX_OLD, testFolder.getObjectID())).getTimestamp());
    }

    @Test
    public void testShareFolder() throws Throwable {
        final int folderId = testFolder.getObjectID();

        final int shareFolderId = FolderObject.SYSTEM_SHARED_FOLDER_ID;
        final List<FolderObject> l = FolderTools.getSubFolders(client2, Integer.toString(shareFolderId), true);
        assertTrue("No shared subfolder available for second user " + client2.getValues().getUserId(), l != null && !l.isEmpty());

        /*-
         * Expected:
         *
         * - Shared folders
         *       |
         *        - ...
         *       |
         *        - Calendar
         *              |
         *              - TestShared...
         */

        boolean found = false;
        Next: for (FolderObject virtualFO : l) {
            final List<FolderObject> subList = FolderTools.getSubFolders(client2, virtualFO.getFullName(), true);
            for (final FolderObject sharedFolder : subList) {
                if (sharedFolder.getObjectID() == parentId) {

                    final List<FolderObject> subsubList = FolderTools.getSubFolders(client2, Integer.toString(parentId), true);
                    for (final FolderObject subsharedFolder : subsubList) {
                        if (subsharedFolder.getObjectID() == folderId) {
                            found = true;
                            break Next;
                        }
                    }

                } else if (sharedFolder.getObjectID() == folderId) {
                    found = true;
                    break Next;
                }
            }
        }
        assertTrue("Folder " + folderId + " not beneath shared folder of second user " + client2.getValues().getUserId(), found);
    }

}
