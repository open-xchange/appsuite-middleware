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

import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.FolderShare;
import com.openexchange.ajax.share.actions.ResolveShareResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.tryagain.TryAgain;

/**
 * {@link FolderTransactionTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class FolderTransactionTest extends ShareTest {

    @Test
    @TryAgain
    public void testDontCreateShareOnFailingFolderCreate() throws Exception {
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            for (int module : TESTED_MODULES) {
                testDontCreateShareOnFailingFolderCreate(api, module);
            }
        }
    }

    private void testDontCreateShareOnFailingFolderCreate(EnumAPI api, int module) throws Exception {
        FolderObject parent = getFolder(api, getDefaultFolder(module));
        FolderObject folder = insertPrivateFolder(api, module, parent.getObjectID());
        List<FolderShare> oldShares = getFolderShares(api, module);
        /*
         * Should fail due to name conflict
         */
        boolean insertionFailed = false;
        try {
            insertSharedFolder(api, module, parent.getObjectID(), folder.getFolderName(), createAnonymousGuestPermission());
        } catch (Throwable e) {
            insertionFailed = true;
        }
        assertTrue("API: " + api + ", Module: " + module, insertionFailed);

        List<FolderShare> newShares = getFolderShares(api, module);
        assertEquals("The number of shares differs but should not. " + "API: " + api + ", Module: " + module, oldShares.size(), newShares.size());
    }

    @Test
    @TryAgain
    public void testDontCreateShareOnFailingFolderUpdate() throws Exception {
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            for (int module : TESTED_MODULES) {
                testDontCreateShareOnFailingFolderUpdate(api, module);
            }
        }
    }

    private void testDontCreateShareOnFailingFolderUpdate(EnumAPI api, int module) throws Exception {
        FolderObject parent = getFolder(api, getDefaultFolder(module));
        FolderObject sharedFolder = insertPrivateFolder(api, module, parent.getObjectID());
        List<FolderShare> oldShares = getFolderShares(api, module);

        OCLGuestPermission guestPermission = createAnonymousGuestPermission();
        ArrayList<OCLPermission> permissions = new ArrayList<OCLPermission>(sharedFolder.getPermissions());
        permissions.add(guestPermission);
        sharedFolder.setPermissions(permissions);

        /*
         * Update should fail because of possible concurrent modification
         */
        Map<String, Object> meta;
        if (sharedFolder.getMeta() == null) {
            meta = new HashMap<>();
        } else {
            meta = new HashMap<>(sharedFolder.getMeta());
        }
        meta.put(AJAXServlet.PARAMETER_TIMESTAMP, L(sharedFolder.getLastModified().getTime() - 1000));
        sharedFolder.setMeta(meta);
        boolean updateFailed = false;
        try {
            updateFolder(api, sharedFolder);
        } catch (Throwable e) {
            updateFailed = true;
        }
        assertTrue("API: " + api + ", Module: " + module, updateFailed);

        List<FolderShare> newShares = getFolderShares(api, module);
        assertEquals("The number of shares differs but should not." + "API: " + api + ", Module: " + module, oldShares.size(), newShares.size());
    }

    @Test
    @TryAgain
    public void testDontRemoveSharesOnFailingFolderUpdate() throws Exception {
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            for (int module : TESTED_MODULES) {
                testDontRemoveSharesOnFailingFolderUpdate(api, module);
            }
        }
    }

    private void testDontRemoveSharesOnFailingFolderUpdate(EnumAPI api, int module) throws Exception {
        FolderObject parent = getFolder(api, getDefaultFolder(module));
        OCLGuestPermission guestPermission = createAnonymousGuestPermission();
        FolderObject sharedFolder = insertSharedFolder(api, module, parent.getObjectID(), guestPermission);
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission permission : sharedFolder.getPermissions()) {
            if (permission.getEntity() != getClient().getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found." + "API: " + api + ", Module: " + module, matchingPermission);
        checkPermissions(guestPermission, matchingPermission);

        ExtendedPermissionEntity guest = discoverGuestEntity(api, module, sharedFolder.getObjectID(), matchingPermission.getEntity());
        assertNotNull("API: " + api + ", Module: " + module, guest);
        String shareURL = discoverShareURL(guestPermission.getApiClient(), guest);

        /*
         * Update should fail because of invalid permissions
         */
        sharedFolder.setPermissionsAsArray(new OCLPermission[0]);
        boolean updateFailed = false;
        try {
            updateFolder(api, sharedFolder);
        } catch (Throwable e) {
            updateFailed = true;
        }
        assertTrue("API: " + api + ", Module: " + module, updateFailed);

        GuestClient guestClient = new GuestClient(shareURL, guestPermission.getRecipient());
        ResolveShareResponse resolveResponse = guestClient.getShareResolveResponse();
        // TODO: Properly fix this by retrieving correct folder id in the first place
        if (module == FolderObject.CALENDAR) {
            assertEquals("API: " + api + ", Module: " + module, "cal://0/" + Integer.toString(sharedFolder.getObjectID()), resolveResponse.getFolder());
        } else {
            assertEquals("API: " + api + ", Module: " + module, Integer.toString(sharedFolder.getObjectID()), resolveResponse.getFolder());
        }
    }

}
