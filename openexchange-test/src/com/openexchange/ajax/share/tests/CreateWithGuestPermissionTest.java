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
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link CreateWithGuestPermissionTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CreateWithGuestPermissionTest extends ShareTest {

    @Test
    public void testCreateSharedFolderRandomly() throws Exception {
        int module = randomModule();
        testCreateSharedFolder(randomFolderAPI(), module, randomGuestPermission(module));
    }

    public void noTestCreateSharedFolderExtensively() throws Exception {
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            for (GuestPermissionType permissionType : GuestPermissionType.values()) {
                OCLGuestPermission guestPermission = createGuestPermission(permissionType);
                for (int module : TESTED_MODULES) {
                    if (isReadOnlySharing(module) && false == isReadOnly(guestPermission)) {
                        continue;
                    }
                    testCreateSharedFolder(api, module, guestPermission);
                }
            }
        }
    }

    @Test
    public void testCreateSharedFileRandomly() throws Exception {
        testCreateSharedFile(randomFolderAPI(), randomGuestPermission());
    }

    public void noTestCreateSharedFileExtensively() throws Exception {
        for (GuestPermissionType permissionType : GuestPermissionType.values()) {
            OCLGuestPermission guestPermission = createGuestPermission(permissionType);
            testCreateSharedFile(EnumAPI.OX_NEW, guestPermission);
        }
    }

    private void testCreateSharedFolder(EnumAPI api, int module, OCLGuestPermission guestPermission) throws Exception {
        testCreateSharedFolder(api, module, getDefaultFolder(module), guestPermission);
    }

    private void testCreateSharedFolder(EnumAPI api, int module, int parent, OCLGuestPermission guestPermission) throws Exception {
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
        GuestClient guestClient = resolveShare(discoverShareURL(guestPermission.getApiClient(), guest), guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
    }

    private void testCreateSharedFile(EnumAPI api, OCLGuestPermission oclGuestPermission) throws Exception {
        testCreateSharedFile(api, getDefaultFolder(FolderObject.INFOSTORE), oclGuestPermission);
    }

    private void testCreateSharedFile(EnumAPI api, int parent, OCLGuestPermission oclGuestPermission) throws Exception {
        FileStorageGuestObjectPermission guestPermission = asObjectPermission(oclGuestPermission);
        /*
         * create folder and a shared file inside
         */
        byte[] contents = new byte[64 + random.nextInt(256)];
        random.nextBytes(contents);
        String filename = randomUID();
        FolderObject folder = insertPrivateFolder(api, FolderObject.INFOSTORE, parent);
        File file = insertSharedFile(folder.getObjectID(), filename, guestPermission, contents);
        /*
         * check permissions
         */
        FileStorageObjectPermission matchingPermission = null;
        for (FileStorageObjectPermission permission : file.getObjectPermissions()) {
            if (permission.getEntity() != getClient().getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created file found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(file.getId(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(discoverShareURL(oclGuestPermission.getApiClient(), guest), guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission, contents);
    }

}
