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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.ajax.share.tests;

import java.io.IOException;
import org.json.JSONException;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.folder.actions.VersionsRequest;
import com.openexchange.ajax.folder.actions.VersionsResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.infostore.actions.CopyInfostoreRequest;
import com.openexchange.ajax.infostore.actions.CopyInfostoreResponse;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreRequest;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.test.TestInit;

/**
 * 
 * Version-Tests for MW-178
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class CopySharedFilesVersionsRemovalTest extends AbstractSharedFilesTest {

    /**
     * Initializes a new {@link CopySharedFilesVersionsRemovalTest}.
     * 
     * @param name
     */
    public CopySharedFilesVersionsRemovalTest(String name) {
        super(name);
    }

    public void testCopySharedFile_ownerCopiesFile_fileBecomesCopiedWithoutVersions() throws Exception {
        userDestFolder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder(), "dest_" + randomUID());

        AJAXClient client2 = new AJAXClient(User.User2);
        try {
            addUserPermission(client2.getValues().getUserId());
            GuestRecipient recipient = new GuestRecipient();
            recipient.setEmailAddress("test@invalid.invalid");
            addGuestPermission(recipient);
            file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });

            createNewFileVersions();

            //pre assertions
            assertExistingVersions(file.getId(), 5);

            file.setFolderId(Integer.toString(userDestFolder.getObjectID())); // set new target folder
            String newObjectId = infoMgr.copyAction(file.getId(), Integer.toString(userDestFolder.getObjectID()), file);

            File copiedFile = client.execute(new GetInfostoreRequest(newObjectId)).getDocumentMetadata();

            // assert
            assertExistingVersions(copiedFile.getId(), 1);
        } finally {
            client2.logout();
        }
    }

    public void testCopySharedFile_ownerCopiesFileWithDefinedVersion_fileBecomesCopiedWithoutVersions() throws Exception {
        userDestFolder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder(), "dest_" + randomUID());

        AJAXClient client2 = new AJAXClient(User.User2);
        try {
            addUserPermission(client2.getValues().getUserId());
            GuestRecipient recipient = new GuestRecipient();
            recipient.setEmailAddress("test@invalid.invalid");
            addGuestPermission(recipient);
            file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });

            createNewFileVersions();

            //pre assertions
            assertExistingVersions(file.getId(), 5);

            file.setFolderId(Integer.toString(userDestFolder.getObjectID())); // set new target folder

            CopyInfostoreRequest copyRequest = new CopyInfostoreRequest(file.getId(), Integer.toString(userDestFolder.getObjectID()), file, "2");
            CopyInfostoreResponse copyResponse = client.execute(copyRequest);
            String newObjectId = copyResponse.getID();

            File copiedFile = client.execute(new GetInfostoreRequest(newObjectId)).getDocumentMetadata();

            // assert
            assertExistingVersions(copiedFile.getId(), 1);
        } finally {
            client2.logout();
        }
    }

    private void assertExistingVersions(String id, int expectedVersions) throws OXException, IOException, JSONException {
        Field[] fields = new Field[] { Field.NUMBER_OF_VERSIONS, Field.VERSION, Field.CURRENT_VERSION, Field.CREATED_BY };
        VersionsResponse versions = client.execute(new VersionsRequest(id, fields));
        assertEquals("Wrong number of versions", expectedVersions, versions.getVersions().size());
    }

    private void createNewFileVersions() throws OXException, IOException, JSONException, InterruptedException {
        this.createNewFileVersions(client, file);
    }

    private void createNewFileVersions(AJAXClient theClient, File theFile) throws OXException, IOException, JSONException, InterruptedException {
        java.io.File upload = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));
        UpdateInfostoreRequest updateInfostoreRequest = new UpdateInfostoreRequest(theFile, new Field[] { Field.NUMBER_OF_VERSIONS, Field.VERSION, Field.CURRENT_VERSION, Field.CREATED_BY, Field.CONTENT }, upload, theFile.getLastModified());
        // create new versions
        for (int i = 0; i < 4; i++) {
            theClient.execute(updateInfostoreRequest);
            Thread.sleep(1000L);
        }
    }

    public void testCopySharedFile_guestCopiesFile_fileBecomesCopiedWithoutVersions() throws Exception {
        OCLGuestPermission lGuestPermission = createNamedAuthorPermission(randomUID() + "@example.com", "Test Guest", "secret");
        userDestFolder = insertSharedFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder(), "dest_" + randomUID(), lGuestPermission);

        AJAXClient client2 = new AJAXClient(User.User2);
        GuestClient guestClient = null;
        try {
            addUserPermission(client2.getValues().getUserId());
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
                if (permission.getEntity() != client.getValues().getUserId()) {
                    matchingPermission = permission;
                    break;
                }
            }
            ExtendedPermissionEntity guest = discoverGuestEntity(EnumAPI.OX_NEW, FolderObject.INFOSTORE, userDestFolder.getObjectID(), matchingPermission.getEntity());
            checkGuestPermission(lGuestPermission, guest);
            String shareURL = discoverShareURL(guest);

            guestClient = resolveShare(shareURL, getUsername(lGuestPermission.getRecipient()), getPassword(lGuestPermission.getRecipient()));

            CopyInfostoreRequest copyRequest = new CopyInfostoreRequest(sharedFileId, SHARED_FOLDER, file);
            copyRequest.setFailOnError(true);
            CopyInfostoreResponse copyResponse = guestClient.execute(copyRequest);

            File copiedFile = client.execute(new GetInfostoreRequest(copyResponse.getID())).getDocumentMetadata();

            assertExistingVersions(copiedFile.getId(), 1);
        } finally {
            client2.logout();
            if (guestClient != null) {
                guestClient.logout();
            }
        }
    }

    public void testCopySharedFile_guestCreatesNewVersionAndCopiesFile_fileBecomesCopiedWithoutVersions() throws Exception {
        OCLGuestPermission lGuestPermission = createNamedAuthorPermission(randomUID() + "@example.com", "Test Guest", "secret");
        userDestFolder = insertSharedFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder(), "dest_" + randomUID(), lGuestPermission);

        AJAXClient client2 = new AJAXClient(User.User2);
        GuestClient guestClient = null;
        try {
            addUserPermission(client2.getValues().getUserId());
            addGuestPermission(lGuestPermission.getRecipient());
            updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });

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
                if (permission.getEntity() != client.getValues().getUserId()) {
                    matchingPermission = permission;
                    break;
                }
            }
            ExtendedPermissionEntity guest = discoverGuestEntity(EnumAPI.OX_NEW, FolderObject.INFOSTORE, userDestFolder.getObjectID(), matchingPermission.getEntity());
            checkGuestPermission(lGuestPermission, guest);
            String shareURL = discoverShareURL(guest);

            guestClient = resolveShare(shareURL, getUsername(lGuestPermission.getRecipient()), getPassword(lGuestPermission.getRecipient()));
            File sharedFileToCopy = guestClient.execute(new GetInfostoreRequest(sharedFileId)).getDocumentMetadata();

            createNewFileVersions(guestClient, sharedFileToCopy);
            assertExistingVersions(file.getId(), 9);

            CopyInfostoreRequest copyRequest = new CopyInfostoreRequest(sharedFileId, SHARED_FOLDER, file);
            copyRequest.setFailOnError(true);
            CopyInfostoreResponse copyResponse = guestClient.execute(copyRequest);

            File copiedFile1 = client.execute(new GetInfostoreRequest(copyResponse.getID())).getDocumentMetadata();

            assertExistingVersions(copiedFile1.getId(), 1);
        } finally {
            client2.logout();
            if (guestClient != null) {
                guestClient.logout();
            }
        }
    }

    public void testCopySharedFile_internalUserCopiesFile_fileBecomesCopiedWithoutVersions() throws Exception {
        AJAXClient client2 = new AJAXClient(User.User2);
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

            createNewFileVersions();
            //pre assertions
            assertExistingVersions(file.getId(), 5);

            file.setFolderId(Integer.toString(userDestFolder.getObjectID())); // set new target folder

            CopyInfostoreRequest copyRequest = new CopyInfostoreRequest(sharedFileId, SHARED_FOLDER, file);
            copyRequest.setFailOnError(true);
            CopyInfostoreResponse copyResponse = client2.execute(copyRequest);

            File copiedFile = client.execute(new GetInfostoreRequest(copyResponse.getID())).getDocumentMetadata();

            assertExistingVersions(copiedFile.getId(), 1);
        } finally {
            client2.logout();
        }
    }

    public void testCopySharedFile_internalUserCreatesNewVersionsAndCopiesFile_fileBecomesCopiedWithoutVersions() throws Exception {
        AJAXClient client2 = new AJAXClient(User.User2);
        int userId = client2.getValues().getUserId();

        try {
            OCLPermission permission = new OCLPermission(userId, 0);
            permission.setAllPermission(OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS);
            userDestFolder = insertSharedFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder(), "dest_" + randomUID(), permission);

            addUserPermission(client2.getValues().getUserId());
            GuestRecipient recipient = new GuestRecipient();
            recipient.setEmailAddress("test@invalid.invalid");
            addGuestPermission(recipient);
            file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });
            String sharedFileId = sharedFileId(file.getId());

            createNewFileVersions();
            File copiedFile = client2.execute(new GetInfostoreRequest(sharedFileId)).getDocumentMetadata();
            createNewFileVersions(client2, copiedFile);
            //pre assertions
            assertExistingVersions(file.getId(), 9);

            file.setFolderId(Integer.toString(userDestFolder.getObjectID())); // set new target folder

            CopyInfostoreRequest copyRequest = new CopyInfostoreRequest(sharedFileId, SHARED_FOLDER, file);
            copyRequest.setFailOnError(true);
            CopyInfostoreResponse copyResponse = client2.execute(copyRequest);

            File copiedFile1 = client.execute(new GetInfostoreRequest(copyResponse.getID())).getDocumentMetadata();
            assertExistingVersions(copiedFile1.getId(), 1);
        } finally {
            client2.logout();
        }
    }
}
