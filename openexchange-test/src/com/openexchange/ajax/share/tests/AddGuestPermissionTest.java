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

import static com.openexchange.java.Autoboxing.B;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.notification.ShareNotificationService.Transport;

/**
 * {@link AddGuestPermissionTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AddGuestPermissionTest extends ShareTest {

    @Test
    public void testUpdateSharedFolderRandomly() throws Exception {
        int module = randomModule();
        testUpdateSharedFolder(randomFolderAPI(), module, randomGuestPermission(module));
    }

    @Test
    public void testUpdateSharedFileRandomly() throws Exception {
        testUpdateSharedFile(randomFolderAPI(), randomGuestPermission());
    }

    private void testUpdateSharedFolder(EnumAPI api, int module, OCLGuestPermission guestPermission) throws Exception {
        testUpdateSharedFolder(api, module, getDefaultFolder(module), guestPermission);
    }

    private void testUpdateSharedFolder(EnumAPI api, int module, int parent, OCLGuestPermission guestPermission) throws Exception {
        /*
         * create private folder
         */
        FolderObject folder = insertPrivateFolder(api, module, parent);
        /*
         * update folder, add permission for guest
         */
        folder.addPermission(guestPermission);
        folder = updateFolder(api, folder);
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
    }

    @SuppressWarnings("unused")
    private void testUpdateSharedFolderWithCascadingPermissions(EnumAPI api, int module, int parent, OCLGuestPermission guestPermission) throws Exception {
        /*
         * create folder hierarchy
         */
        FolderObject rootFolder = insertPrivateFolder(api, module, parent, "Root_" + randomUID());
        FolderObject subLevel1 = insertPrivateFolder(api, module, rootFolder.getObjectID(), "Sub1" + randomUID());
        FolderObject subLevel2 = insertPrivateFolder(api, module, subLevel1.getObjectID(), "Sub2" + randomUID());
        /*
         * update root folder, add permission for guest
         */
        Date clientLastModified = subLevel2.getLastModified();
        rootFolder.addPermission(guestPermission);
        rootFolder.setLastModified(clientLastModified);
        rootFolder = updateFolder(api, rootFolder, new RequestCustomizer<UpdateRequest>() {
            @Override
            public void customize(UpdateRequest request) {
                request.setCascadePermissions(true);
                request.setNotifyPermissionEntities(Transport.MAIL);
            }
        });
        /*
         * Reload subfolders
         */
        subLevel1 = getFolder(api, subLevel1.getObjectID());
        subLevel2 = getFolder(api, subLevel2.getObjectID());
        /*
         * check permissions
         */
        OCLPermission matchingRootPermission = null;
        for (FolderObject folder : new FolderObject[] { rootFolder, subLevel1, subLevel2 }) {
            OCLPermission matchingPermission = null;
            List<OCLPermission> permissions = folder.getPermissions();
            assertNotNull("No permissions fround for folder " + folder.getObjectID(), permissions);
            assertEquals("Wrong number of permissions on folder " + folder.getObjectID(), 2, permissions.size());
            for (OCLPermission permission : permissions) {
                if (permission.getEntity() != getClient().getValues().getUserId()) {
                    matchingPermission = permission;
                    break;
                }
            }
            assertNotNull("No matching permission in created folder found", matchingPermission);
            checkPermissions(guestPermission, matchingPermission);
            if (folder == rootFolder) {
                matchingRootPermission = matchingPermission;
            } else {
                assertNotNull(matchingRootPermission);
                assertEquals("Unexpected permission entity for subfolder " + folder.getObjectID(), matchingRootPermission.getEntity(), matchingPermission.getEntity());
                assertEquals("Unexpected permission bits for subfolder " + folder.getObjectID(), matchingRootPermission.getReadPermission(), matchingPermission.getReadPermission());
                assertEquals("Unexpected permission bits for subfolder " + folder.getObjectID(), matchingRootPermission.getWritePermission(), matchingPermission.getWritePermission());
                assertEquals("Unexpected permission bits for subfolder " + folder.getObjectID(), matchingRootPermission.getDeletePermission(), matchingPermission.getDeletePermission());
                assertEquals("Unexpected permission bits for subfolder " + folder.getObjectID(), matchingRootPermission.getFolderPermission(), matchingPermission.getFolderPermission());
                assertEquals("Unexpected permission bits for subfolder " + folder.getObjectID(), matchingRootPermission.getSystem(), matchingPermission.getSystem());
                assertEquals("Unexpected permission bits for subfolder " + folder.getObjectID(), B(matchingRootPermission.isFolderAdmin()), B(matchingPermission.isFolderAdmin()));
                assertEquals("Unexpected permission bits for subfolder " + folder.getObjectID(), B(matchingRootPermission.isGroupPermission()), B(matchingPermission.isGroupPermission()));
            }
        }
        /*
         * discover & check guest
         */
        assertNotNull(matchingRootPermission);
        ExtendedPermissionEntity guest = discoverGuestEntity(api, module, rootFolder.getObjectID(), matchingRootPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(guest, guestPermission.getRecipient(), guestPermission.getApiClient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
    }

    private void testUpdateSharedFile(EnumAPI api, OCLGuestPermission guestPermission) throws Exception {
        testUpdateSharedFile(api, getDefaultFolder(FolderObject.INFOSTORE), guestPermission);
    }

    private void testUpdateSharedFile(EnumAPI api, int parent, OCLGuestPermission oclGuestPermission) throws Exception {
        FileStorageGuestObjectPermission guestPermission = asObjectPermission(oclGuestPermission);
        /*
         * create folder and a file inside
         */
        FolderObject folder = insertPrivateFolder(api, FolderObject.INFOSTORE, parent);
        File file = insertFile(folder.getObjectID());
        /*
         * update file, add permissions for guest
         */
        file.setObjectPermissions(Collections.<FileStorageObjectPermission> singletonList(guestPermission));
        file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });
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
        GuestClient guestClient = resolveShare(guest, guestPermission.getRecipient(), oclGuestPermission.getApiClient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
    }
}
