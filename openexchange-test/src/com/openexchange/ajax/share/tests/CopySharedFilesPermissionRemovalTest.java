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
import static org.junit.Assert.assertNull;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.infostore.actions.CopyInfostoreRequest;
import com.openexchange.ajax.infostore.actions.CopyInfostoreResponse;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.test.tryagain.TryAgain;

/**
 *
 * Permission-Tests for MW-178
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class CopySharedFilesPermissionRemovalTest extends AbstractSharedFilesTest {

    @Test
    @TryAgain
    public void testCopySharedFile_ownerCopiesFile_fileBecomesCopiedWithoutObjectPermissions() throws Exception {
        userDestFolder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder(), "dest_" + randomUID());

        AJAXClient client2 = testUser2.getAjaxClient();
        addUserPermission(client2.getValues().getUserId());
        GuestRecipient recipient = new GuestRecipient();
        recipient.setEmailAddress("test@invalid.invalid");
        addGuestPermission(recipient);
        file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });

        //pre assertions
        File documentMetadata = getClient().execute(new GetInfostoreRequest(file.getId())).getDocumentMetadata();
        assertEquals("Wrong number of shares users/guests", 2, documentMetadata.getObjectPermissions().size());

        file.setFolderId(Integer.toString(userDestFolder.getObjectID())); // set new target folder
        infoMgr.copyAction(file.getId(), Integer.toString(userDestFolder.getObjectID()), file);
        String newObjectId = file.getId();

        File copiedFile = getClient().execute(new GetInfostoreRequest(newObjectId)).getDocumentMetadata();

        assertEquals("Object permissions should not be available!", 0, copiedFile.getObjectPermissions().size());
        assertEquals("File not created by main user", getClient().getValues().getUserId(), copiedFile.getCreatedBy());
        assertEquals("Wrong number of versions", 1, copiedFile.getNumberOfVersions());
    }

    @Test
    @TryAgain
    public void testCopySharedFile_guestCopiesFile_fileBecomesCopiedWithoutObjectPermissions() throws Exception {
        OCLGuestPermission lGuestPermission = createNamedAuthorPermission();
        userDestFolder = insertSharedFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder(), "dest_" + randomUID(), lGuestPermission);

        AJAXClient client2 = testUser2.getAjaxClient();
        GuestClient guestClient = null;
        try {
            addUserPermission(client2.getValues().getUserId());
            addGuestPermission(lGuestPermission.getRecipient());
            file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });
            String sharedFileId = sharedFileId(file.getId());

            //pre assertions
            File documentMetadata = getClient().execute(new GetInfostoreRequest(sharedFileId)).getDocumentMetadata();
            assertEquals("Wrong number of shares users/guests", 2, documentMetadata.getObjectPermissions().size());

            file.setFolderId(Integer.toString(userDestFolder.getObjectID())); // set new target folder

            /*
             * check permissions
             */
            OCLPermission matchingPermission = null;
            for (OCLPermission permission : userDestFolder.getPermissions()) {
                if (permission.getEntity() != getClient().getValues().getUserId()) {
                    matchingPermission = permission;
                    break;
                }
            }
            assertNotNull(matchingPermission);
            ExtendedPermissionEntity guest = discoverGuestEntity(EnumAPI.OX_NEW, FolderObject.INFOSTORE, userDestFolder.getObjectID(), matchingPermission.getEntity());
            checkGuestPermission(lGuestPermission, guest);
            String shareURL = discoverShareURL(lGuestPermission.getApiClient(), guest);

            guestClient = resolveShare(shareURL, getUsername(lGuestPermission.getRecipient()), getPassword(lGuestPermission.getRecipient()));

            CopyInfostoreRequest copyRequest = new CopyInfostoreRequest(sharedFileId, SHARED_FOLDER, file);
            copyRequest.setFailOnError(true);
            CopyInfostoreResponse copyResponse = guestClient.execute(copyRequest);

            assertNull("No conflict should occur!", copyResponse.getConflicts());
            assertNull("No error should occur!", copyResponse.getErrorMessage());
            assertNull("No exception should occur!", copyResponse.getException());

            File copiedFile = getClient().execute(new GetInfostoreRequest(copyResponse.getID())).getDocumentMetadata();

            assertEquals("Object permissions should not be available!", 0, copiedFile.getObjectPermissions().size());
            assertEquals("File not created by guest", guestClient.getValues().getUserId(), copiedFile.getCreatedBy());
            assertEquals("Wrong number of versions", 1, copiedFile.getNumberOfVersions());
        } finally {
            client2.logout();
            if (guestClient != null) {
                guestClient.logout();
            }
        }
    }

    @Test
    @TryAgain
    public void testCopySharedFile_internalUserCopiesFile_fileBecomesCopiedWithoutObjectPermissions() throws Exception {
        AJAXClient client2 = testUser2.getAjaxClient();
        int userId = client2.getValues().getUserId();

        try {
            OCLPermission permission = new OCLPermission(userId, 0);
            permission.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
            userDestFolder = insertSharedFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder(), "dest_" + randomUID(), permission);

            addUserPermission(client2.getValues().getUserId());
            GuestRecipient recipient = new GuestRecipient();
            recipient.setEmailAddress("test@invalid.invalid");
            addGuestPermission(recipient);
            file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });
            String sharedFileId = sharedFileId(file.getId());

            //pre assertions
            File documentMetadata = getClient().execute(new GetInfostoreRequest(file.getId())).getDocumentMetadata();
            assertEquals("Wrong number of shares users/guests", 2, documentMetadata.getObjectPermissions().size());

            file.setFolderId(Integer.toString(userDestFolder.getObjectID())); // set new target folder

            CopyInfostoreRequest copyRequest = new CopyInfostoreRequest(sharedFileId, SHARED_FOLDER, file);
            copyRequest.setFailOnError(true);
            CopyInfostoreResponse copyResponse = client2.execute(copyRequest);

            File copiedFile = getClient().execute(new GetInfostoreRequest(copyResponse.getID())).getDocumentMetadata();

            assertEquals("Object permissions should not be available!", 0, copiedFile.getObjectPermissions().size());
            assertEquals("File not created by internal user", userId, copiedFile.getCreatedBy());
            assertEquals("Wrong number of versions", 1, copiedFile.getNumberOfVersions());
        } finally {
            client2.logout();
        }
    }
}
