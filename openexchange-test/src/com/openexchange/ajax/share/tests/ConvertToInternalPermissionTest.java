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
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.test.common.test.pool.TestUser;
import com.openexchange.test.tryagain.TryAgain;

/**
 * {@link ConvertToInternalPermissionTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ConvertToInternalPermissionTest extends ShareTest {

    @Test
    @TryAgain
    public void testConvertToInternalPermissionRandomly() throws Exception {
        testConvertToInternalPermission(randomFolderAPI(), randomModule(), testContext.acquireUser());
    }

    public void noTestConvertToInternalPermissionExtensively() throws Exception {
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            for (int module : TESTED_MODULES) {
                testConvertToInternalPermission(api, module, testContext.acquireUser());
            }
        }
    }

    @Test
    @TryAgain
    public void testConvertToInternalObjectPermissionRandomly() throws Exception {
        testConvertToInternalObjectPermission(randomFolderAPI(), testContext.acquireUser());
    }

    private void testConvertToInternalPermission(EnumAPI api, int module, TestUser user) throws Exception {
        testConvertToInternalPermission(api, module, getDefaultFolder(module), user);
    }

    private void testConvertToInternalPermission(EnumAPI api, int module, int parent, TestUser user) throws Exception {
        /*
         * prepare guest permission with e-mail address of other other internal user
         */
        AJAXClient userClient = user.getAjaxClient();
        int userID = userClient.getValues().getUserId();
        OCLGuestPermission guestPermission = createNamedGuestPermission(user, true);
        userClient.logout();
        /*
         * create folder shared to guest user
         */
        FolderObject folder = insertSharedFolder(api, module, parent, guestPermission);
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission permission : folder.getPermissions()) {
            if (permission.getEntity() == userID) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * check that no share was created for internal user
         */
        ExtendedPermissionEntity entity = discoverGuestEntity(api, module, folder.getObjectID(), matchingPermission.getEntity());
        assertEquals(RecipientType.USER, entity.getType());
    }

    private void testConvertToInternalObjectPermission(EnumAPI api, TestUser user) throws Exception {
        testConvertToInternalObjectPermission(api, getDefaultFolder(FolderObject.INFOSTORE), user);
    }

    private void testConvertToInternalObjectPermission(EnumAPI api, int parent, TestUser user) throws Exception {
        /*
         * prepare guest permission with e-mail address of other other internal user
         */
        AJAXClient userClient = user.getAjaxClient();
        int userID = userClient.getValues().getUserId();
        FileStorageGuestObjectPermission guestPermission = asObjectPermission(createNamedGuestPermission(user, true));
        userClient.logout();
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
            if (permission.getEntity() == userID) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created file found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * check that no share was created for internal user
         */
        ExtendedPermissionEntity entity = discoverGuestEntity(file.getId(), matchingPermission.getEntity());
        assertEquals(RecipientType.USER, entity.getType());
    }
}
