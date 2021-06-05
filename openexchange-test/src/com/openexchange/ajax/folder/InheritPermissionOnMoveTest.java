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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.util.List;
import org.junit.Test;
import com.openexchange.testing.httpclient.models.FolderBody;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.FolderResponse;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;

/**
 * {@link InheritPermissionOnMoveTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.4
 */
public class InheritPermissionOnMoveTest extends AbstractFolderMovePermissionsTest {

    public InheritPermissionOnMoveTest() {
        super("inherit");
    }

    @Test
    public void testMoveFolderInPrivateFolder() throws Exception {
        String toMoveFolderId = createNewFolder(false, I(0), true, true);
        FolderBody folderBody = new FolderBody();
        FolderData folderData = new FolderData();
        folderData.setFolderId(privateFolderId);
        folderData.setPermissions(null);
        folderBody.setFolder(folderData);
        FolderUpdateResponse response = api.updateFolder(toMoveFolderId, folderBody, Boolean.FALSE, L(System.currentTimeMillis()), TREE, null, Boolean.FALSE, null, Boolean.FALSE, Boolean.TRUE);
        assertNotNull(response);

        FolderResponse resp = api.getFolder(toMoveFolderId, TREE, null, null);
        assertNotNull(resp);
        FolderData respData = resp.getData();
        List<FolderPermission> permissions = respData.getPermissions();
        assertEquals(2, permissions.size());
        for (FolderPermission perm : permissions) {
            if (perm.getEntity().equals(userId1)) {
                assertEquals(BITS_ADMIN, perm.getBits());
            } else if (perm.getEntity().equals(userId2)) {
                assertEquals(BITS_REVIEWER, perm.getBits());
            } else {
                fail("Unexpected permission: " + perm.toString());
            }
        }
    }

    @Test
    public void testMoveFolderInPublicFolder() throws Exception {
        String toMoveFolderId = createNewFolder(false, I(0), true, true);
        FolderBody folderBody = new FolderBody();
        FolderData folderData = new FolderData();
        folderData.setFolderId(publicFolderId);
        folderData.setPermissions(null);
        folderBody.setFolder(folderData);
        FolderUpdateResponse response = api.updateFolder(toMoveFolderId, folderBody, Boolean.FALSE, L(System.currentTimeMillis()), TREE, null, Boolean.FALSE, null, Boolean.FALSE, Boolean.TRUE);
        assertNotNull(response);

        FolderResponse resp = api.getFolder(toMoveFolderId, TREE, null, null);
        assertNotNull(resp);
        FolderData respData = resp.getData();
        List<FolderPermission> permissions = respData.getPermissions();
        assertEquals(3, permissions.size());
        for (FolderPermission perm : permissions) {
            if (perm.getEntity().equals(userId1)) {
                assertEquals(BITS_ADMIN, perm.getBits());
            } else if (perm.getEntity().equals(userId2)) {
                assertEquals(BITS_REVIEWER, perm.getBits());
            } else if (perm.getEntity().equals(I(0))) {
                assertEquals(BITS_VIEWER, perm.getBits());
            } else {
                fail("Unexpected permission: " + perm.toString());
            }
        }
    }

    @Test
    public void testMoveFolderInSharedFolder() throws Exception {
        String toMoveFolderId = createNewFolder(false, I(0), true, true);
        FolderBody folderBody = new FolderBody();
        FolderData folderData = new FolderData();
        folderData.setFolderId(sharedFolderId);
        folderData.setPermissions(null);
        folderBody.setFolder(folderData);
        FolderUpdateResponse response = api.updateFolder(toMoveFolderId, folderBody, Boolean.FALSE, L(System.currentTimeMillis()), TREE, null, Boolean.FALSE, null, Boolean.FALSE, Boolean.TRUE);
        assertNotNull(response);

        FolderResponse resp = api.getFolder(toMoveFolderId, TREE, null, null);
        assertNotNull(resp);
        FolderData respData = resp.getData();
        List<FolderPermission> permissions = respData.getPermissions();
        assertEquals(2, permissions.size());
        for (FolderPermission perm : permissions) {
            if (perm.getEntity().equals(userId1)) {
                assertEquals(BITS_OWNER, perm.getBits());
            } else if (perm.getEntity().equals(userId2)) {
                assertEquals(BITS_ADMIN, perm.getBits());
            } else {
                fail("Unexpected permission: " + perm.toString());
            }
        }
    }

}
