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

package com.openexchange.ajax.drive.test;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.drive.action.DeleteLinkRequest;
import com.openexchange.ajax.drive.action.GetLinkRequest;
import com.openexchange.ajax.drive.action.GetLinkResponse;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.DriveShareTarget;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.test.common.test.TestInit;

/**
 * {@link GetLinkTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class GetLinkTest extends AbstractDriveShareTest {

    @SuppressWarnings("hiding")
    private InfostoreTestManager itm;
    private FolderObject rootFolder, folder;
    private DefaultFile file;
    private FolderObject folder2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        itm = new InfostoreTestManager(getClient());

        UserValues values = getClient().getValues();
        rootFolder = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), values.getPrivateInfostoreFolder());
        folder = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), rootFolder.getObjectID());
        folder2 = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), rootFolder.getObjectID());

        long now = System.currentTimeMillis();
        file = new DefaultFile();
        file.setFolderId(String.valueOf(folder2.getObjectID()));
        file.setTitle("GetLinkTest_" + now);
        file.setFileName(file.getTitle());
        file.setDescription(file.getTitle());
        file.setFileMD5Sum(getChecksum(new File(TestInit.getTestProperty("ajaxPropertiesFile"))));
        itm.newAction(file, new File(TestInit.getTestProperty("ajaxPropertiesFile")));
    }

    @Test
    public void testGetFileLink() throws Exception {
        DriveShareTarget target = new DriveShareTarget();
        target.setDrivePath("/" + folder2.getFolderName());
        target.setName(file.getFileName());
        target.setChecksum(file.getFileMD5Sum());
        performTest(target);
    }

    @Test
    public void testGetFolderLink() throws Exception {
        DriveShareTarget target = new DriveShareTarget();
        target.setDrivePath("/" + folder.getFolderName());
        target.setChecksum(DriveConstants.EMPTY_MD5);
        performTest(target);
    }

    @Test
    public void testBadFileChecksum() throws Exception {
        DriveShareTarget target = new DriveShareTarget();
        target.setDrivePath("/" + folder2.getFolderName());
        target.setName(file.getFileName());
        target.setChecksum("bad");

        GetLinkRequest getLinkRequest = new GetLinkRequest(I(rootFolder.getObjectID()), target, false);
        GetLinkResponse getLinkResponse = getClient().execute(getLinkRequest);
        assertTrue("Expected error.", getLinkResponse.hasError());
        assertTrue("Wrong exception", DriveExceptionCodes.FILEVERSION_NOT_FOUND.equals(getLinkResponse.getException()));
    }

    @Test
    public void testBadDirectoryChecksum() throws Exception {
        DriveShareTarget target = new DriveShareTarget();
        target.setDrivePath("/" + folder.getFolderName());
        target.setChecksum("bad");

        GetLinkRequest getLinkRequest = new GetLinkRequest(I(rootFolder.getObjectID()), target, false);
        GetLinkResponse getLinkResponse = getClient().execute(getLinkRequest);
        assertTrue("Expected error.", getLinkResponse.hasError());
        assertTrue("Wrong exception", DriveExceptionCodes.DIRECTORYVERSION_NOT_FOUND.equals(getLinkResponse.getException()));
    }

    private void performTest(DriveShareTarget target) throws OXException, IOException, JSONException, Exception {
        GetLinkRequest getLinkRequest = new GetLinkRequest(I(rootFolder.getObjectID()), target);
        GetLinkResponse getLinkResponse = getClient().execute(getLinkRequest);
        String url = getLinkResponse.getUrl();

        GuestClient guestClient = resolveShare(url, null, null);
        OCLGuestPermission expectedPermission = createAnonymousGuestPermission();
        expectedPermission.setEntity(guestClient.getValues().getUserId());
        guestClient.checkShareAccessible(expectedPermission);
        int guestID = guestClient.getValues().getUserId();

        getClient().execute(new DeleteLinkRequest(I(rootFolder.getObjectID()), target));
        ExtendedPermissionEntity guestEntity;
        if (target.isFolder()) {
            guestEntity = discoverGuestEntity(EnumAPI.OX_NEW, FolderObject.INFOSTORE, folder2.getObjectID(), guestID);
        } else {
            guestEntity = discoverGuestEntity(file.getId(), guestID);
        }
        assertNull("Share was not deleted", guestEntity);
        List<FileStorageObjectPermission> objectPermissions = getClient().execute(new GetInfostoreRequest(file.getId())).getDocumentMetadata().getObjectPermissions();
        assertTrue("Permission was not deleted", objectPermissions.isEmpty());
    }

}
