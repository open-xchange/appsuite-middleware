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
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.folder.actions.VersionsRequest;
import com.openexchange.ajax.folder.actions.VersionsResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.infostore.actions.CopyInfostoreRequest;
import com.openexchange.ajax.infostore.actions.CopyInfostoreResponse;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.GetInfostoreResponse;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreRequest;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.test.common.test.TestInit;

/**
 *
 * Version-Tests for MW-178
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class CopySharedFilesVersionsRemovalTest extends AbstractSharedFilesTest {

    private int secondUserId;
    private AJAXClient client2;

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withContexts(2).withUserPerContext(2).build();
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        secondUserId = testUser2.getUserId();
        client2 = testUser2.getAjaxClient();
    }

    @Test
    public void testCopySharedFile_ownerCopiesFile_fileBecomesCopiedWithoutVersions() throws Exception {
        userDestFolder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder(), "dest_" + randomUID());

        addUserPermission(secondUserId);
        GuestRecipient recipient = new GuestRecipient();
        recipient.setEmailAddress("test@invalid.invalid");
        addGuestPermission(recipient);
        file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });

        createNewFileVersions();

        //pre assertions
        assertExistingVersions(file.getId(), 5);

        file.setFolderId(Integer.toString(userDestFolder.getObjectID())); // set new target folder
        infoMgr.copyAction(file.getId(), Integer.toString(userDestFolder.getObjectID()), file);
        String newObjectId = file.getId();

        File copiedFile = getClient().execute(new GetInfostoreRequest(newObjectId)).getDocumentMetadata();

        // assert
        assertExistingVersions(copiedFile.getId(), 1);
    }

    @Test
    public void testCopySharedFile_ownerCopiesFileWithDefinedVersion_fileBecomesCopiedWithoutVersions() throws Exception {
        userDestFolder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder(), "dest_" + randomUID());

        try {
            addUserPermission(secondUserId);
            GuestRecipient recipient = new GuestRecipient();
            recipient.setEmailAddress("test@invalid.invalid");
            addGuestPermission(recipient);
            file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });

            createNewFileVersions();

            //pre assertions
            assertExistingVersions(file.getId(), 5);

            file.setFolderId(Integer.toString(userDestFolder.getObjectID())); // set new target folder

            CopyInfostoreRequest copyRequest = new CopyInfostoreRequest(file.getId(), Integer.toString(userDestFolder.getObjectID()), file, "2");
            CopyInfostoreResponse copyResponse = getClient().execute(copyRequest);
            String newObjectId = copyResponse.getID();

            File copiedFile = getClient().execute(new GetInfostoreRequest(newObjectId)).getDocumentMetadata();

            // assert
            assertExistingVersions(copiedFile.getId(), 1);
        } finally {
            client2.logout();
        }
    }

    private void assertExistingVersions(String id, int expectedVersions) throws OXException, IOException, JSONException {
        int[] fields = new int[] { Field.NUMBER_OF_VERSIONS.getNumber(), Field.VERSION.getNumber(), Field.CURRENT_VERSION.getNumber(), Field.CREATED_BY.getNumber() };
        VersionsResponse versions = getClient().execute(new VersionsRequest(id, fields));
        assertEquals("Wrong number of versions", expectedVersions, versions.getVersions().size());
    }

    private void createNewFileVersions() throws OXException, IOException, JSONException, InterruptedException {
        this.createNewFileVersions(getClient(), file);
    }

    private void createNewFileVersions(AJAXClient theClient, File theFile) throws OXException, IOException, JSONException, InterruptedException {
        java.io.File upload = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));
        Date timestamp = theFile.getMeta() == null ? theFile.getLastModified() : (Date) theFile.getMeta().getOrDefault("timestamp", theFile.getLastModified());
        UpdateInfostoreRequest updateInfostoreRequest = new UpdateInfostoreRequest(theFile, new Field[] { Field.NUMBER_OF_VERSIONS, Field.VERSION, Field.CURRENT_VERSION, Field.CREATED_BY, Field.CONTENT }, upload, timestamp);
        // create new versions
        for (int i = 0; i < 4; i++) {
            UpdateInfostoreResponse response = theClient.execute(updateInfostoreRequest);
            assertNull(response.getErrorMessage(), response.getErrorMessage());
            Thread.sleep(1000L);
            updateInfostoreRequest = new UpdateInfostoreRequest(theFile, new Field[] { Field.NUMBER_OF_VERSIONS, Field.VERSION, Field.CURRENT_VERSION, Field.CREATED_BY, Field.CONTENT }, upload, response.getTimestamp());
        }
    }

    @Test
    public void testCopySharedFile_guestCopiesFile_fileBecomesCopiedWithoutVersions() throws Exception {
        OCLGuestPermission lGuestPermission = createNamedAuthorPermission();
        userDestFolder = insertSharedFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder(), "dest_" + randomUID(), lGuestPermission);

        GuestClient guestClient = null;
        try {
            addUserPermission(secondUserId);
            addGuestPermission(lGuestPermission.getRecipient());
            file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });

            createNewFileVersions();
            //pre assertions
            assertExistingVersions(file.getId(), 5);

            String sharedFileId = sharedFileId(file.getId());
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
            assertNotNull("Missing share url", shareURL);

            guestClient = resolveShare(shareURL, getUsername(lGuestPermission.getRecipient()), getPassword(lGuestPermission.getRecipient()));

            CopyInfostoreRequest copyRequest = new CopyInfostoreRequest(sharedFileId, SHARED_FOLDER, file);
            copyRequest.setFailOnError(true);
            CopyInfostoreResponse copyResponse = guestClient.execute(copyRequest);

            File copiedFile = getClient().execute(new GetInfostoreRequest(copyResponse.getID())).getDocumentMetadata();

            assertExistingVersions(copiedFile.getId(), 1);
        } finally {
            if (guestClient != null) {
                guestClient.logout();
            }
        }
    }

    @Test
    public void testCopySharedFile_guestCreatesNewVersionAndCopiesFile_fileBecomesCopiedWithoutVersions() throws Exception {
        OCLGuestPermission lGuestPermission = createNamedAuthorPermission();
        userDestFolder = insertSharedFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder(), "dest_" + randomUID(), lGuestPermission);

        GuestClient guestClient = null;
        try {
            addUserPermission(secondUserId);
            addGuestPermission(lGuestPermission.getRecipient());
            file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });

            createNewFileVersions();
            //pre assertions
            assertExistingVersions(file.getId(), 5);

            String sharedFileId = sharedFileId(file.getId());
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
            GetInfostoreResponse response = guestClient.execute(new GetInfostoreRequest(sharedFileId));
            File sharedFileToCopy = response.getDocumentMetadata();
            sharedFileToCopy.setMeta(Collections.singletonMap("timestamp", response.getTimestamp()));

            createNewFileVersions(guestClient, sharedFileToCopy);
            assertExistingVersions(file.getId(), 9);

            CopyInfostoreRequest copyRequest = new CopyInfostoreRequest(sharedFileId, SHARED_FOLDER, file);
            copyRequest.setFailOnError(true);
            CopyInfostoreResponse copyResponse = guestClient.execute(copyRequest);

            File copiedFile1 = getClient().execute(new GetInfostoreRequest(copyResponse.getID())).getDocumentMetadata();

            assertExistingVersions(copiedFile1.getId(), 1);
        } finally {
            if (guestClient != null) {
                guestClient.logout();
            }
        }
    }

    @Test
    public void testCopySharedFile_internalUserCopiesFile_fileBecomesCopiedWithoutVersions() throws Exception {
        int userId = secondUserId;

        OCLPermission permission = new OCLPermission(userId, 0);
        permission.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        userDestFolder = insertSharedFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder(), "dest_" + randomUID(), permission);

        addUserPermission(userId);
        GuestRecipient recipient = new GuestRecipient();
        recipient.setEmailAddress("test@invalid.invalid");
        addGuestPermission(recipient);
        file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });
        String sharedFileId = sharedFileId(file.getId());

        createNewFileVersions();
        //pre assertions
        assertExistingVersions(file.getId(), 5);

        file.setFolderId(Integer.toString(userDestFolder.getObjectID())); // set new target folder

        CopyInfostoreRequest copyRequest = new CopyInfostoreRequest(sharedFileId, SHARED_FOLDER, file);
        copyRequest.setFailOnError(true);
        CopyInfostoreResponse copyResponse = client2.execute(copyRequest);

        File copiedFile = getClient().execute(new GetInfostoreRequest(copyResponse.getID())).getDocumentMetadata();

        assertExistingVersions(copiedFile.getId(), 1);
    }

    @Test
    public void testCopySharedFile_internalUserCreatesNewVersionsAndCopiesFile_fileBecomesCopiedWithoutVersions() throws Exception {
        int userId = secondUserId;

        OCLPermission permission = new OCLPermission(userId, 0);
        permission.setAllPermission(OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS);
        userDestFolder = insertSharedFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder(), "dest_" + randomUID(), permission);

        addUserPermission(userId);
        GuestRecipient recipient = new GuestRecipient();
        recipient.setEmailAddress("test@invalid.invalid");
        addGuestPermission(recipient);
        file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });
        String sharedFileId = sharedFileId(file.getId());

        createNewFileVersions();
        GetInfostoreResponse response = client2.execute(new GetInfostoreRequest(sharedFileId));
        File copiedFile = response.getDocumentMetadata();
        copiedFile.setMeta(Collections.singletonMap("timestamp", response.getTimestamp()));
        createNewFileVersions(client2, copiedFile);
        //pre assertions
        assertExistingVersions(file.getId(), 9);

        file.setFolderId(Integer.toString(userDestFolder.getObjectID())); // set new target folder

        CopyInfostoreRequest copyRequest = new CopyInfostoreRequest(sharedFileId, SHARED_FOLDER, file);
        copyRequest.setFailOnError(true);
        CopyInfostoreResponse copyResponse = client2.execute(copyRequest);

        File copiedFile1 = getClient().execute(new GetInfostoreRequest(copyResponse.getID())).getDocumentMetadata();
        assertExistingVersions(copiedFile1.getId(), 1);
    }
}
