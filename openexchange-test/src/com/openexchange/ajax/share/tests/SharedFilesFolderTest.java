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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreRequest;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreResponse;
import com.openexchange.ajax.share.Abstract2UserShareTest;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link SharedFilesFolderTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class SharedFilesFolderTest extends Abstract2UserShareTest {

    private static final String SHARED_FOLDER = "10";

    private FolderObject folder;
    private File file;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder());
        file = insertFile(folder.getObjectID(), randomUID());
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().createApiClient().withContexts(2).withUserPerContext(3).build();
    }

    @Test
    public void testReShareNotPossibleForInternals() throws Exception {
        AJAXClient client3 = testContext.acquireUser().getAjaxClient();
        List<FileStorageObjectPermission> permissions = new ArrayList<FileStorageObjectPermission>(1);
        permissions.add(new DefaultFileStorageObjectPermission(client2.getValues().getUserId(), false, FileStorageObjectPermission.WRITE));
        file.setObjectPermissions(permissions);
        file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });
        String sharedFileId = sharedFileId(file.getId());

        // Check shareable flag
        assertFalse(client2.execute(new GetInfostoreRequest(sharedFileId)).getDocumentMetadata().isShareable());

        // Try to share it anyway
        DefaultFile toUpdate = new DefaultFile();
        toUpdate.setFolderId(SHARED_FOLDER);
        toUpdate.setId(sharedFileId);
        permissions.add(new DefaultFileStorageObjectPermission(client3.getValues().getUserId(), false, FileStorageObjectPermission.WRITE));
        toUpdate.setObjectPermissions(permissions);

        UpdateInfostoreRequest updateInfostoreRequest = new UpdateInfostoreRequest(toUpdate, new Field[] { Field.OBJECT_PERMISSIONS }, file.getLastModified());
        updateInfostoreRequest.setFailOnError(false);
        UpdateInfostoreResponse updateInfostoreResponse = client2.execute(updateInfostoreRequest);
        assertTrue(updateInfostoreResponse.hasError());
        assertTrue(InfostoreExceptionCodes.NO_WRITE_PERMISSION.equals(updateInfostoreResponse.getException()));
    }

    @Test
    public void testReShareNotPossibleForInvitedGuests() throws Exception {
        List<FileStorageObjectPermission> permissions = new ArrayList<FileStorageObjectPermission>(1);
        OCLGuestPermission guestPermission = createNamedAuthorPermission(false);
        String guestEmail = ((GuestRecipient) guestPermission.getRecipient()).getEmailAddress();
        permissions.add(asObjectPermission(guestPermission));
        file.setObjectPermissions(permissions);
        file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });
        String sharedFileId = sharedFileId(file.getId());

        String invitationLink = discoverInvitationLink(guestPermission.getApiClient(), guestEmail);
        GuestClient guestClient = resolveShare(invitationLink);
        guestPermission.setEntity(guestClient.getValues().getUserId());
        guestClient.checkFileAccessible(sharedFileId, guestPermission);

        // Check shareable flag
        assertFalse(guestClient.execute(new GetInfostoreRequest(sharedFileId)).getDocumentMetadata().isShareable());

        // Try to share it anyway
        DefaultFile toUpdate = new DefaultFile();
        toUpdate.setFolderId(SHARED_FOLDER);
        toUpdate.setId(sharedFileId);
        permissions.add(asObjectPermission(createNamedAuthorPermission(false)));
        toUpdate.setObjectPermissions(permissions);

        UpdateInfostoreRequest updateInfostoreRequest = new UpdateInfostoreRequest(toUpdate, new Field[] { Field.OBJECT_PERMISSIONS }, file.getLastModified());
        updateInfostoreRequest.setFailOnError(false);
        UpdateInfostoreResponse updateInfostoreResponse = guestClient.execute(updateInfostoreRequest);
        assertTrue(updateInfostoreResponse.hasError());
        assertTrue(InfostoreExceptionCodes.NO_WRITE_PERMISSION.equals(updateInfostoreResponse.getException()));
    }

    private static String sharedFileId(String fileId) {
        FileID tmp = new FileID(fileId);
        tmp.setFolderId(SHARED_FOLDER);
        return tmp.toUniqueID();
    }

}
