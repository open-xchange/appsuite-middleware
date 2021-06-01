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

package com.openexchange.ajax.share.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link CreateSubfolderTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CreateSubfolderTest extends ShareTest {

    /**
     * Initializes a new {@link CreateSubfolderTest}.
     *
     * @param name The test name
     */
    public CreateSubfolderTest() {
        super();
    }

    @Test
    public void testCreateSubfolderWithAdminFlagRandomly() throws Exception {
        testCreateSubfolderWithAdminFlag(randomFolderAPI(), FolderObject.INFOSTORE);
    }

    public void noTestCreateSubfolderWithAdminFlagExtensively() throws Exception {
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            testCreateSubfolderWithAdminFlag(api, FolderObject.INFOSTORE);
        }
    }

    private void testCreateSubfolderWithAdminFlag(EnumAPI api, int module) throws Exception {
        testCreateSubfolderWithAdminFlag(api, module, getDefaultFolder(module));
    }

    private void testCreateSubfolderWithAdminFlag(EnumAPI api, int module, int parent) throws Exception {
        /*
         * create share with guest permissions that allow subfolder creation (yet no update/delete)
         */
        OCLGuestPermission guestPermission = createNamedAuthorPermission();
        guestPermission.setFolderPermission(OCLPermission.CREATE_SUB_FOLDERS);
        guestPermission.setFolderAdmin(true);
        /*
         * create folder shared to guest user
         */
        FolderObject folder = insertSharedFolder(api, module, parent, guestPermission);
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission permission : folder.getPermissions()) {
            if (permission.getEntity() != getClient().getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(api, module, folder.getObjectID(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(guest, guestPermission.getRecipient(), guestPermission.getApiClient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
        /*
         * create subfolder as guest
         */
        String originalName = randomUID();
        FolderObject subfolder = new FolderObject();
        subfolder.setFolderName(originalName);
        subfolder.setModule(folder.getModule());
        subfolder.setType(folder.getType());
        subfolder.setParentFolderID(folder.getObjectID());
        subfolder.setPermissionsAsArray(folder.getPermissionsAsArray());
        InsertResponse insertResponse = guestClient.execute(new InsertRequest(api, subfolder));
        insertResponse.fillObject(subfolder);
        subfolder.setLastModified(insertResponse.getTimestamp());
        /*
         * verify folder as sharing user
         */
        FolderObject reloadedSubfolder = getFolder(api, subfolder.getObjectID());
        assertNotNull(reloadedSubfolder);
        super.remember(reloadedSubfolder);
        assertEquals("Folder name wrong", originalName, reloadedSubfolder.getFolderName());
        /*
         * try to rename the folder as guest
         */
        subfolder.setFolderName(randomUID());
        InsertResponse updateResponse = guestClient.execute(new UpdateRequest(api, subfolder));
        assertFalse("Errors/warnings in response", updateResponse.hasError() || updateResponse.getResponse().hasWarnings());
        /*
         * verify folder is changed as sharing user
         */
        reloadedSubfolder = getFolder(api, subfolder.getObjectID());
        assertNotNull(reloadedSubfolder);
        super.remember(reloadedSubfolder);
        assertEquals("Folder name wrong", subfolder.getFolderName(), reloadedSubfolder.getFolderName());
        /*
         * try to delete the folder as guest
         */
        CommonDeleteResponse deleteResponse = guestClient.execute(new DeleteRequest(api, subfolder));
        assertFalse("Errors/warnings in response", deleteResponse.hasError() || deleteResponse.getResponse().hasWarnings());
    }

}
