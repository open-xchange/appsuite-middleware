/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
        FolderUpdateResponse response = api.updateFolder(getSessionId(), toMoveFolderId, folderBody, Boolean.FALSE, L(System.currentTimeMillis()), TREE, null, Boolean.FALSE, null, Boolean.FALSE);
        assertNotNull(response);

        FolderResponse resp = api.getFolder(getSessionId(), toMoveFolderId, TREE, null, null);
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
        FolderUpdateResponse response = api.updateFolder(getSessionId(), toMoveFolderId, folderBody, Boolean.FALSE, L(System.currentTimeMillis()), TREE, null, Boolean.FALSE, null, Boolean.FALSE);
        assertNotNull(response);

        FolderResponse resp = api.getFolder(getSessionId(), toMoveFolderId, TREE, null, null);
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
    public void testMoveFolderInSharedFolder() throws Exception {
        String toMoveFolderId = createNewFolder(false, I(0), true, true);
        FolderBody folderBody = new FolderBody();
        FolderData folderData = new FolderData();
        folderData.setFolderId(sharedFolderId);
        folderData.setPermissions(null);
        folderBody.setFolder(folderData);
        FolderUpdateResponse response = api.updateFolder(getSessionId(), toMoveFolderId, folderBody, Boolean.FALSE, L(System.currentTimeMillis()), TREE, null, Boolean.FALSE, null, Boolean.FALSE);
        assertNotNull(response);

        FolderResponse resp = api.getFolder(getSessionId(), toMoveFolderId, TREE, null, null);
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
