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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.FolderShare;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.test.tryagain.TryAgain;

/**
 * {@link ListFolderSharesTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ListFolderSharesTest extends ShareTest {

    /**
     * Initializes a new {@link ListFolderSharesTest}.
     *
     * @param name The test name
     */
    public ListFolderSharesTest() {
        super();
    }

    @Test
    @TryAgain
    public void testListSharedFoldersToAnonymous() throws Exception {
        int module = randomModule();
        testListSharedFolders(randomGuestPermission(RecipientType.ANONYMOUS, module), module);
    }

    @Test
    @TryAgain
    public void testListSharedFoldersToGuest() throws Exception {
        int module = randomModule();
        testListSharedFolders(randomGuestPermission(RecipientType.GUEST, module), module);
    }

    @Test
    @TryAgain
    public void testListSharedFoldersToGroup() throws Exception {
        OCLPermission permission = new OCLPermission(GroupStorage.GROUP_ZERO_IDENTIFIER, 0);
        permission.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        permission.setGroupPermission(true);
        testListSharedFolders(permission, randomModule());
    }

    @Test
    @TryAgain
    public void testListSharedFoldersToUser() throws Exception {
        int userId = testUser2.getUserId();
        OCLPermission permission = new OCLPermission(userId, 0);
        permission.setAllPermission(OCLPermission.READ_ALL_OBJECTS, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        testListSharedFolders(permission, randomModule());
    }

    private void testListSharedFolders(OCLPermission permission, int module) throws Exception {
        /*
         * create shared folder
         */
        FolderObject folder = insertSharedFolder(EnumAPI.OX_NEW, module, getDefaultFolder(module), permission);
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission oclPermission : folder.getPermissions()) {
            if (oclPermission.getEntity() != getClient().getValues().getUserId()) {
                matchingPermission = oclPermission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        checkPermissions(permission, matchingPermission);
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(EnumAPI.OX_NEW, module, folder.getObjectID(), matchingPermission.getEntity());
        checkGuestPermission(permission, guest);
    }

    @Test
    @TryAgain
    public void testDontListPublicFolders() throws Exception {
        /*
         * create public folder
         */
        int module = randomModule();
        OCLGuestPermission guestPermission = randomGuestPermission(module);
        FolderObject folder = insertPublicFolder(module);
        folder.addPermission(guestPermission);
        folder = updateFolder(EnumAPI.OX_NEW, folder);
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission oclPermission : folder.getPermissions()) {
            if (oclPermission.getEntity() != getClient().getValues().getUserId()) {
                matchingPermission = oclPermission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * check if share appears in user shared folders
         */
        FolderShare matchingShare = null;
        List<FolderShare> shares = getFolderShares(getClient(), EnumAPI.OX_NEW, module);
        for (FolderShare share : shares) {
            if (share.getObjectID() == folder.getObjectID()) {
                matchingShare = share;
                break;
            }
        }
        assertNull("Share in public folder listed", matchingShare);
    }

    @Test
    @TryAgain
    public void testDontListPublicFoldersInSubfolder() throws Exception {
        /*
         * create public folder
         */
        int module = randomModule();
        OCLGuestPermission guestPermission = randomGuestPermission(module);
        FolderObject folder = insertPublicFolder(module);
        FolderObject subfolder = folder.clone();
        subfolder.setParentFolderID(folder.getObjectID());
        subfolder.removeObjectID();
        subfolder.addPermission(guestPermission);
        subfolder = insertFolder(EnumAPI.OX_NEW, subfolder);
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission oclPermission : subfolder.getPermissions()) {
            if (oclPermission.getEntity() != getClient().getValues().getUserId()) {
                matchingPermission = oclPermission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * check if share appears in user shared folders
         */
        FolderShare matchingShare = null;
        List<FolderShare> shares = getFolderShares(getClient(), EnumAPI.OX_NEW, module);
        for (FolderShare share : shares) {
            if (share.getObjectID() == subfolder.getObjectID()) {
                matchingShare = share;
                break;
            }
        }
        assertNull("Share in public folder listed", matchingShare);
    }

}
