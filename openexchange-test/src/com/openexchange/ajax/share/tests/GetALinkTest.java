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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareAPITest;
import com.openexchange.ajax.share.actions.DeleteLinkRequest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.GetLinkRequest;
import com.openexchange.ajax.share.actions.GetLinkResponse;
import com.openexchange.ajax.share.actions.UpdateLinkRequest;
import com.openexchange.ajax.share.actions.UpdateLinkResponse;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.ShareTarget;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.testing.httpclient.models.InfoItemData;
import com.openexchange.testing.httpclient.models.InfoItemMovedResponse;
import com.openexchange.testing.httpclient.models.InfoItemResponse;
import com.openexchange.testing.httpclient.models.InfoItemUpdateResponse;
import com.openexchange.testing.httpclient.models.ShareLinkData;
import com.openexchange.testing.httpclient.models.ShareLinkResponse;
import com.openexchange.testing.httpclient.models.ShareTargetData;
import com.openexchange.testing.httpclient.modules.InfostoreApi;
import com.openexchange.testing.httpclient.modules.ShareManagementApi;

/**
 * {@link GetALinkTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class GetALinkTest extends ShareAPITest {

    private FolderObject infostore;
    private InfostoreApi infostoreApi;
    private DefaultFile file;
    private ShareManagementApi shareApi;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        infostoreApi = new InfostoreApi(getApiClient());
        shareApi = new ShareManagementApi(getApiClient());

        UserValues values = getClient().getValues();
        infostore = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), values.getPrivateInfostoreFolder());

        long now = System.currentTimeMillis();
        FolderObject parent = infostore;
        file = new DefaultFile();
        file.setFolderId(String.valueOf(parent.getObjectID()));
        String fileName = "GetALinkTest_" + now;
        file.setTitle(fileName);
        file.setFileName(fileName);
        file.setDescription(fileName);

        InfoItemUpdateResponse uploadInfoItem = infostoreApi.uploadInfoItem(file.getFolderId(), file.getFileName(), new byte[] {}, L(now), null, file.getTitle(), null, null, file.getDescription(), null, null, null, null, null, null, null, null, null, null);
        file.setId(checkResponse(uploadInfoItem.getError(), uploadInfoItem.getErrorDesc(), uploadInfoItem.getData()));
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().createApiClient().build();
    }

    @Test
    public void testCreateUpdateAndDeleteLinkForAFolder() throws Exception {
        /*
         * Get a link for the new drive subfolder
         */
        ShareTarget target = new ShareTarget(FolderObject.INFOSTORE, Integer.toString(infostore.getObjectID()));
        GetLinkRequest getLinkRequest = new GetLinkRequest(target, getClient().getValues().getTimeZone());
        GetLinkResponse getLinkResponse = getClient().execute(getLinkRequest);
        String url = getLinkResponse.getShareLink().getShareURL();
        /*
         * Resolve the link and check read permission for folder
         */
        GuestClient guestClient = resolveShare(url, null, null);
        OCLGuestPermission expectedPermission = createAnonymousGuestPermission();
        expectedPermission.setEntity(guestClient.getValues().getUserId());
        guestClient.checkFolderAccessible(Integer.toString(infostore.getObjectID()), expectedPermission);
        File reloaded = (File) guestClient.getItem(infostore, file.getId(), false);
        assertNotNull(reloaded);
        /*
         * Update password and expiry
         */
        String newPassword = UUIDs.getUnformattedString(UUID.randomUUID());
        Date newExpiry = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        UpdateLinkRequest updateLinkRequest = new UpdateLinkRequest(target, getClient().getValues().getTimeZone(), getLinkResponse.getTimestamp().getTime());
        updateLinkRequest.setExpiryDate(newExpiry);
        updateLinkRequest.setPassword(newPassword);
        UpdateLinkResponse updateLinkResponse = getClient().execute(updateLinkRequest);
        /*
         * Resolve link with new credentials and check expiry
         */
        GuestClient newClient = resolveShare(url, null, newPassword);
        int guestId = newClient.getValues().getUserId();
        ExtendedPermissionEntity guest = discoverGuestEntity(EnumAPI.OX_NEW, FolderObject.INFOSTORE, infostore.getObjectID(), guestId);
        assertNotNull(guest);
        assertEquals(newExpiry, guest.getExpiry());
        /*
         * Delete link and verify that share and folder permission are gone
         */
        getClient().execute(new DeleteLinkRequest(target, updateLinkResponse.getTimestamp().getTime()));
        assertNull("Share was not deleted", discoverGuestEntity(EnumAPI.OX_NEW, FolderObject.INFOSTORE, infostore.getObjectID(), guestId));
        List<OCLPermission> reloadedFolderPermissions = getFolder(EnumAPI.OX_NEW, infostore.getObjectID()).getPermissions();
        assertEquals("Permission was not deleted", 1, reloadedFolderPermissions.size());
        assertEquals("Permission was not deleted", getClient().getValues().getUserId(), reloadedFolderPermissions.get(0).getEntity());
    }

    @Test
    public void testCreateUpdateAndDeleteLinkForAFile() throws Exception {
        /*
         * Get a link for the file
         */
        ShareTarget target = new ShareTarget(FolderObject.INFOSTORE, Integer.toString(infostore.getObjectID()), file.getId());
        GetLinkRequest getLinkRequest = new GetLinkRequest(target, getClient().getValues().getTimeZone());
        GetLinkResponse getLinkResponse = getClient().execute(getLinkRequest);
        String url = getLinkResponse.getShareLink().getShareURL();
        /*
         * Resolve the link and check read permission for file
         */
        GuestClient guestClient = resolveShare(url, null, null);
        OCLGuestPermission expectedPermission = createAnonymousGuestPermission();
        expectedPermission.setEntity(guestClient.getValues().getUserId());
        guestClient.checkFileAccessible(file.getId(), expectedPermission);
        /*
         * Update permission, password and expiry
         */
        String newPassword = UUIDs.getUnformattedString(UUID.randomUUID());
        Date newExpiry = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        UpdateLinkRequest updateLinkRequest = new UpdateLinkRequest(target, getClient().getValues().getTimeZone(), getLinkResponse.getTimestamp().getTime());
        updateLinkRequest.setExpiryDate(newExpiry);
        updateLinkRequest.setPassword(newPassword);
        UpdateLinkResponse updateLinkResponse = getClient().execute(updateLinkRequest);
        /*
         * Resolve link with new credentials and check permission and expiry
         */
        GuestClient newClient = resolveShare(url, null, newPassword);
        int guestId = newClient.getValues().getUserId();
        newClient.checkFileAccessible(file.getId(), expectedPermission);
        ExtendedPermissionEntity guest = discoverGuestEntity(file.getId(), guestId);
        assertNotNull(guest);
        assertEquals(newExpiry, guest.getExpiry());
        /*
         * Delete link and verify that share and object permission are gone
         */
        getClient().execute(new DeleteLinkRequest(target, updateLinkResponse.getTimestamp().getTime()));
        assertNull("Share was not deleted", discoverGuestEntity(file.getId(), guestId));
        List<FileStorageObjectPermission> objectPermissions = getClient().execute(new GetInfostoreRequest(file.getId())).getDocumentMetadata().getObjectPermissions();
        assertTrue("Permission was not deleted", null == objectPermissions || 0 == objectPermissions.size());
    }

    @Test
    public void testMoveFileWithExistingLink() throws Exception {
        /*
         * We create a link for a file and then move it to another folder. Afterwards we expect the link to be removed, as
         * it will not work anymore.
         */
        ShareTargetData shareTargetData = new ShareTargetData();
        shareTargetData.setModule(Module.INFOSTORE.getName());
        shareTargetData.setFolder(Integer.toString(infostore.getObjectID()));
        shareTargetData.setItem(file.getId());
        ShareLinkResponse getLinkResponse = shareApi.getShareLink(shareTargetData);
        ShareLinkData shareLinkData = checkResponse(getLinkResponse.getError(), getLinkResponse.getErrorDesc(), getLinkResponse.getData());
        String url = shareLinkData.getUrl();
        /*
         * Resolve the link and check read permission for file
         */
        GuestClient guestClient = resolveShare(url, null, null);
        OCLGuestPermission expectedPermission = createAnonymousGuestPermission();
        expectedPermission.setEntity(guestClient.getValues().getUserId());
        guestClient.checkFileAccessible(file.getId(), expectedPermission);
        guestClient.logout();
        /*
         * Move to "My files"
         */
        String newParentFolderId = Integer.toString(getClient().getValues().getPrivateInfostoreFolder());
        InfoItemMovedResponse moveResponse = infostoreApi.moveFile(L(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)), newParentFolderId, file.getId(), null, Boolean.TRUE);
        assertNotNull("Warning expected", moveResponse.getError());
        assertTrue("Warning does not contain the filename: " + moveResponse.getErrorDesc(), moveResponse.getErrorDesc().contains(file.getDescription()));
        String updatedFileId = moveResponse.getData();
        assertNotNull("File id from successful file update expected", updatedFileId);
        InfoItemResponse infoItemResponse = infostoreApi.getInfoItem(updatedFileId, newParentFolderId);
        InfoItemData infoItem = checkResponse(infoItemResponse.getError(), infoItemResponse.getErrorDesc(), infoItemResponse.getData());
        assertTrue(infoItem.getObjectPermissions().isEmpty());
    }

}
