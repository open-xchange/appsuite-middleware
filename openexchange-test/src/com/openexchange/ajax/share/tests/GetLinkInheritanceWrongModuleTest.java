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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.folder.manager.FolderApi;
import com.openexchange.ajax.folder.manager.FolderManager;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderExtendedPermission;
import com.openexchange.testing.httpclient.models.ShareLinkResponse;
import com.openexchange.testing.httpclient.models.ShareTargetData;
import com.openexchange.testing.httpclient.modules.ShareManagementApi;

/**
 * {@link GetLinkInheritanceWrongModuleTest} tests if the inheritance of getlink permissions are not passed down for other modules
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class GetLinkInheritanceWrongModuleTest extends AbstractAPIClientSession {

    private FolderManager folderManager;
    private ShareManagementApi shareManagementApi;
    private String infostoreRoot;
    private final String MODULE = "calendar";

    private String A, B, C, D, E;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ApiClient client = getApiClient();
        folderManager = new FolderManager(new FolderApi(client, testUser), "1");
        shareManagementApi = new ShareManagementApi(client);
        infostoreRoot = "1";

        /*
         * Create the following folder structure:
         * root
         *  - A
         *    - B
         *      - C
         *  - D
         *    - E
         */
        A = folderManager.createFolder(infostoreRoot, "A_" + UUID.randomUUID(), MODULE);
        B = folderManager.createFolder(A, "B_" + UUID.randomUUID(), MODULE);
        C = folderManager.createFolder(B, "C_" + UUID.randomUUID(), MODULE);
        D = folderManager.createFolder(infostoreRoot, "D_" + UUID.randomUUID(), MODULE);
        E = folderManager.createFolder(D, "E_" + UUID.randomUUID(), MODULE);
    }

    @Test
    public void testCreate() throws ApiException {
        createShareLink(A);

        // Check sub-folder
        checkFolderPermissions(B, 1, 1);
        // Check subsub-folder
        checkFolderPermissions(C, 1, 1);
        // Check no-link folder
        checkFolderPermissions(D, 1, 1);
    }


    @Test
    public void testMoveAway() throws ApiException {
        createShareLink(A);

        // Check sub-folder
        checkFolderPermissions(B, 1, 1);
        // Check subsub-folder
        checkFolderPermissions(C, 1, 1);
        // Check no-link folder
        checkFolderPermissions(D, 1, 1);

        // Move sub-folder to no-link folder
        folderManager.moveFolder(B, D);

        // Check sub-folder
        checkFolderPermissions(B, 1, 1);
        // Check subsub-folder
        checkFolderPermissions(C, 1, 1);
        // Check no-link folder
        checkFolderPermissions(D, 1, 1);
    }

    @Test
    public void testMoveInto() throws ApiException {
        createShareLink(A);

        // Check sub-folder
        checkFolderPermissions(B, 1, 1);
        // Check subsub-folder
        checkFolderPermissions(C, 1, 1);
        // Check no-link folder
        checkFolderPermissions(D, 1, 1);
        // Check no-link sub-folder
        checkFolderPermissions(E, 1, 1);

        // Move no-link folder to parent folder
        folderManager.moveFolder(D, A);

        // Check sub-folder
        checkFolderPermissions(B, 1, 1);
        // Check subsub-folder
        checkFolderPermissions(C, 1, 1);
        // Check no-link folder
        checkFolderPermissions(D, 1, 1);
        // Check no-link sub-folder
        checkFolderPermissions(E, 1, 1);
    }

    /*
     * ---------------------------------- helper methods ------------------------------------------------------------------------
     */

    /**
     * Checks if folder contains the given amount of normal and extended permissions and optionally checks if the extended permissions contains an inherited permissions for the given guest ids.
     *
     * @param folderId The folder to check
     * @param expectedPermSize The amount of normal permissions
     * @param expectededExtendedPermSize The amount of extended permissions
     * @param optGuestIds The optional guest ids to check
     * @throws ApiException
     */
    private void checkFolderPermissions(String folderId, int expectedPermSize, int expectededExtendedPermSize, Integer... optGuestIds) throws ApiException {
        FolderData folder = folderManager.getFolder(folderId);
        assertEquals(expectedPermSize, folder.getPermissions().size());
        assertEquals(expectededExtendedPermSize, folder.getComOpenexchangeShareExtendedPermissions().size());
        if (optGuestIds != null) {
            for (Integer guestId : optGuestIds) {
                checkExtendedPermission(folder, guestId, true);
            }
        }
    }

    /**
     * Checks if the folder has an extended permission for the given entity and checks if this property inherited value is equal to the given value.
     *
     * @param folder The folder
     * @param entity The entity to search
     * @param isInherited Whether the entity should or should not be an inherited permission
     */
    private void checkExtendedPermission(FolderData folder, Integer entity, boolean isInherited) {
        for (FolderExtendedPermission perm : folder.getComOpenexchangeShareExtendedPermissions()) {
            if (perm.getEntity() == entity) {
                assertTrue("The extended permission does not have the expected inherited types", perm.getIsInherited().booleanValue() == isInherited);
                return;
            }
        }
        fail("Folder doesn't contain an extended permission for entity " + entity);
    }

    /**
     * Creates a new share link for the given folder
     *
     * @param folder The folder to create a share link for
     * @return The guest id of for the new link
     * @throws ApiException
     */
    private Integer createShareLink(String folder) throws ApiException {
        ShareTargetData data = new ShareTargetData();
        data.setFolder(folder);
        data.setModule("calendar");
        ShareLinkResponse shareLink = shareManagementApi.getShareLink(data);
        checkResponse(shareLink.getError(), shareLink.getErrorDesc(), shareLink.getData());
        folderManager.setLastTimestamp(shareLink.getTimestamp());
        return shareLink.getData().getEntity();
    }

}
