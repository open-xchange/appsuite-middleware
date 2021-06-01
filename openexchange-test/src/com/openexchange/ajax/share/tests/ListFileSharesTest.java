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
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.FileShare;
import com.openexchange.ajax.share.actions.FileSharesRequest;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link ListFileSharesTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ListFileSharesTest extends ShareTest {

    /**
     * Initializes a new {@link ListFileSharesTest}.
     *
     * @param name The test name
     */
    public ListFileSharesTest() {
        super();
    }

    @Test
    public void testListSharedFilesToAnonymous() throws Exception {
        testListSharedFiles(randomGuestObjectPermission(RecipientType.ANONYMOUS));
    }

    @Test
    public void testListSharedFilesToGuest() throws Exception {
        testListSharedFiles(randomGuestObjectPermission(RecipientType.GUEST));
    }

    @Test
    public void testListSharedFilesToGroup() throws Exception {
        testListSharedFiles(new DefaultFileStorageObjectPermission(GroupStorage.GROUP_ZERO_IDENTIFIER, true, FileStorageObjectPermission.READ));
    }

    @Test
    public void testListSharedFilesToUser() throws Exception {
        int userId = testUser2.getUserId();
        testListSharedFiles(new DefaultFileStorageObjectPermission(userId, false, FileStorageObjectPermission.WRITE));
    }

    private void testListSharedFiles(FileStorageObjectPermission permission) throws Exception {
        /*
         * create folder and a shared file inside
         */
        String filename = randomUID();
        FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(FolderObject.INFOSTORE));
        File file = insertSharedFile(folder.getObjectID(), filename, permission);
        /*
         * check permissions
         */
        FileStorageObjectPermission matchingPermission = null;
        for (FileStorageObjectPermission objectPermission : file.getObjectPermissions()) {
            if (objectPermission.getEntity() != getClient().getValues().getUserId()) {
                matchingPermission = objectPermission;
                break;
            }
        }
        assertNotNull("No matching permission in created file found", matchingPermission);
        checkPermissions(permission, matchingPermission);
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(file.getId(), matchingPermission.getEntity());
        checkGuestPermission(permission, guest);
    }

    @Test
    public void testDontListPublicFiles() throws Exception {
        /*
         * create folder and a shared file inside
         */
        String filename = randomUID();
        FileStorageGuestObjectPermission guestPermission = randomGuestObjectPermission();
        FolderObject folder = insertPublicFolder(FolderObject.INFOSTORE);
        File file = insertSharedFile(folder.getObjectID(), filename, guestPermission);
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
         * check if share appears in user shared files
         */
        FileShare matchingShare = null;
        List<FileShare> shares = getClient().execute(new FileSharesRequest()).getShares(getClient().getValues().getTimeZone());
        for (FileShare share : shares) {
            if (share.getId().equals(file.getId())) {
                matchingShare = share;
                break;
            }
        }
        assertNull("Share in public folder listed", matchingShare);
    }

    @Test
    public void testDontListPublicFilesInSubfolder() throws Exception {
        /*
         * create folder and a shared file inside
         */
        String filename = randomUID();
        FileStorageGuestObjectPermission guestPermission = randomGuestObjectPermission();
        FolderObject folder = insertPublicFolder(FolderObject.INFOSTORE);
        FolderObject subfolder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, folder.getObjectID());
        File file = insertSharedFile(subfolder.getObjectID(), filename, guestPermission);
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
         * check if share appears in user shared files
         */
        FileShare matchingShare = null;
        List<FileShare> shares = getClient().execute(new FileSharesRequest()).getShares(getClient().getValues().getTimeZone());
        for (FileShare share : shares) {
            if (share.getId().equals(file.getId())) {
                matchingShare = share;
                break;
            }
        }
        assertNull("Share in public folder listed", matchingShare);
    }

}
