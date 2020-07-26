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

package com.openexchange.ajax.drive.test;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.groupware.modules.Module;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ConfigResponse;
import com.openexchange.testing.httpclient.models.DriveAction;
import com.openexchange.testing.httpclient.models.DriveDirectoryVersion;
import com.openexchange.testing.httpclient.models.DriveSyncFolderBody;
import com.openexchange.testing.httpclient.models.DriveSyncFolderResponse;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.FoldersCleanUpResponse;
import com.openexchange.testing.httpclient.models.InfoItemBody;
import com.openexchange.testing.httpclient.models.InfoItemData;
import com.openexchange.testing.httpclient.models.InfoItemListElement;
import com.openexchange.testing.httpclient.models.InfoItemPermission;
import com.openexchange.testing.httpclient.models.InfoItemUpdateResponse;
import com.openexchange.testing.httpclient.models.InfoItemsResponse;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.modules.ConfigApi;
import com.openexchange.testing.httpclient.modules.DriveApi;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.InfostoreApi;

/**
 * 
 * {@link MWB358Test}
 * 
 * Deletion of shared file not detected during OX Drive synchronization.
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.4
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class MWB358Test extends AbstractAPIClientSession {

    private ApiClient apiClient1;

    private ApiClient apiClient2;

    private DriveApi driveApi;

    private FoldersApi folderApi;

    private InfostoreApi infostoreAPI;

    private String folderId;

    private final List<String> folders = new ArrayList<>();

    private String privateInfostoreFolder;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        apiClient1 = generateApiClient(testUser);
        rememberClient(apiClient1);

        apiClient2 = generateApiClient(testUser2);
        rememberClient(apiClient2);

        infostoreAPI = new InfostoreApi(apiClient1);
        driveApi = new DriveApi(apiClient2);
        folderApi = new FoldersApi(apiClient1);

        String folderTitle = "MWB358Test_" + UUID.randomUUID().toString();
        folderId = createFolderForTest(folderTitle);
        rememberFolder(folderId);
    }

    @Override
    public void tearDown() throws Exception {
        try {
            if (!folders.isEmpty()) {
                FoldersCleanUpResponse deleteFoldersResponse = folderApi.deleteFolders(apiClient1.getSession(), folders, "1", L(new Date().getTime()), null, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, null);
                assertNull(deleteFoldersResponse.getErrorDesc(), deleteFoldersResponse.getError());
            }
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testMWB358DeleteSharedFileAndTrash() throws Exception {
        Entry<String, Long> file1 = createShareAndSyncTwoFiles();
        deleteFile(file1.getKey(), file1.getValue());
        clearTrash();
        checkForSyncAction();
    }

    @Test
    public void testMoveSharedDocumentToTrash() throws ApiException {
        Entry<String, Long> file1 = createShareAndSyncTwoFiles();
        deleteFile(file1.getKey(), file1.getValue());
        checkForSyncAction();
    }

    @Test
    public void testRemoveSharePermission() throws ApiException {
        Entry<String, Long> file1 = createShareAndSyncTwoFiles();
        removeSharingPermission(file1.getKey(), file1.getValue());
        checkForSyncAction();
    }

    @Test
    public void testDeleteFolderWithSharedFile() throws ApiException {
        createShareAndSyncTwoFiles();
        FoldersCleanUpResponse deleteFoldersResponse = folderApi.deleteFolders(apiClient1.getSession(), folders, "1", L(new Date().getTime()), null, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, null);
        assertNull(deleteFoldersResponse.getErrorDesc(), deleteFoldersResponse.getError());
        checkForSyncAction();
    }

    private void checkForSyncAction() throws ApiException {
        List<DriveAction> actions = syncFolder(apiClient2, "10", "612caec919864455f4664160425550a8", "612caec919864455f4664160425550a8");
        assertEquals(1, actions.size());
        assertEquals("sync", actions.get(0).getAction());
    }

    private Object checkResponse(ConfigResponse resp) {
        assertNull(resp.getErrorDesc(), resp.getError());
        assertNotNull(resp.getData());
        return resp.getData();
    }

    private String checkResponse(FolderUpdateResponse resp) {
        assertNull(resp.getErrorDesc(), resp.getError());
        assertNotNull(resp.getData());
        return resp.getData();
    }

    private void clearTrash() throws ApiException {
        ConfigResponse configResponse = new ConfigApi(apiClient1).getConfigNode("/modules/infostore/folder/trash", apiClient1.getSession());
        assertNull(configResponse.getError(), configResponse.getError());
        String trashFolderId = String.valueOf(configResponse.getData());

        FoldersCleanUpResponse clearFoldersResponse = folderApi.clearFolders(apiClient1.getSession(), Collections.singletonList(trashFolderId), "1", null, null);
        assertNotNull(clearFoldersResponse);
        assertNull(clearFoldersResponse.getErrorDesc(), clearFoldersResponse.getError());
    }

    private String createFolderForTest(String title) throws ApiException {
        final String parent = getPrivateInfostoreFolder();
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setModule(Module.INFOSTORE.getName());
        folder.setSummary(title);
        folder.setTitle(folder.getSummary());
        folder.setSubscribed(Boolean.TRUE);
        folder.setPermissions(null);
        body.setFolder(folder);
        FolderUpdateResponse folderUpdateResponse = folderApi.createFolder(parent, apiClient1.getSession(), body, "1", null, null, null);
        return checkResponse(folderUpdateResponse);
    }

    private Entry<String, Long> createShareAndSyncTwoFiles() throws ApiException {
        /*
         * Share two files to user 2
         */
        Entry<String, Long> file1 = createFile(apiClient1, apiClient2, "MWB358Test1.txt");
        createFile(apiClient1, apiClient2, "MWB358Test2.txt");

        /*
         * sync folder 10 of user 2
         */
        List<DriveAction> actions = syncFolder(apiClient2, "10", "341833daf98ddcb454279c4975ff0419", "341833daf98ddcb454279c4975ff0419");
        assertEquals(1, actions.size());
        assertEquals("sync", actions.get(0).getAction());

        actions = syncFolder(apiClient2, "10", "612caec919864455f4664160425550a8", "341833daf98ddcb454279c4975ff0419");
        assertEquals(2, actions.size());
        assertEquals("acknowledge", actions.get(0).getAction());

        actions = syncFolder(apiClient2, "10", "612caec919864455f4664160425550a8", "612caec919864455f4664160425550a8");
        assertEquals(0, actions.size());
        return file1;
    }

    private Entry<String, Long> createFile(ApiClient owner, ApiClient sharedTo, String fileName) throws ApiException {
        InfoItemUpdateResponse uploadResponse = infostoreAPI.uploadInfoItem(owner.getSession(), folderId, fileName, new byte[] { 34, 45, 35, 23 }, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertNotNull(uploadResponse);
        assertNull(uploadResponse.getErrorDesc(), uploadResponse.getError());
        String objectId = uploadResponse.getData();
        Long timestamp = uploadResponse.getTimestamp();
        if (sharedTo != null) {
            InfoItemBody body = new InfoItemBody();
            InfoItemData file = new InfoItemData();
            InfoItemPermission objectPermission = new InfoItemPermission();
            objectPermission.setEntity(sharedTo.getUserId());
            objectPermission.setGroup(B(false));
            objectPermission.setBits(InfoItemPermission.BitsEnum.NUMBER_1);
            file.setObjectPermissions(Arrays.asList(objectPermission));
            body.setFile(file);

            InfoItemUpdateResponse updateInfoItemResponse = infostoreAPI.updateInfoItem(owner.getSession(), objectId, timestamp, body, null);
            assertNotNull(updateInfoItemResponse);
            assertNull(updateInfoItemResponse.getErrorDesc(), updateInfoItemResponse.getError());
            timestamp = updateInfoItemResponse.getTimestamp();
        }
        return new AbstractMap.SimpleEntry<>(objectId, timestamp);
    }

    private void deleteFile(String objectId, Long timestamp) throws ApiException {
        InfoItemListElement infoItemListElement = new InfoItemListElement();
        infoItemListElement.setFolder(folderId);
        infoItemListElement.setId(objectId);
        InfoItemsResponse deleteInfoItemsResponse = infostoreAPI.deleteInfoItems(apiClient1.getSession(), timestamp, Arrays.asList(infoItemListElement), B(false), null);
        assertNotNull(deleteInfoItemsResponse);
        assertNull(deleteInfoItemsResponse.getErrorDesc(), deleteInfoItemsResponse.getError());
    }

    private String getPrivateInfostoreFolder() throws ApiException {
        if (null == privateInfostoreFolder) {
            ConfigApi configApi = new ConfigApi(apiClient1);
            ConfigResponse configNode = configApi.getConfigNode(Tree.PrivateInfostoreFolder.getPath(), apiClient1.getSession());
            Object data = checkResponse(configNode);
            if (data != null && !data.toString().equalsIgnoreCase("null")) {
                privateInfostoreFolder = String.valueOf(data);
            } else {
                org.junit.Assert.fail("It seems that the user doesn't support drive.");
            }

        }
        return privateInfostoreFolder;
    }

    private void rememberFolder(String folder) {
        folders.add(folder);
    }

    private void removeSharingPermission(String objectId, Long timestamp) throws ApiException {
        InfoItemBody body = new InfoItemBody();
        InfoItemData file = new InfoItemData();
        file.setObjectPermissions(new ArrayList<InfoItemPermission>());
        body.setFile(file);
        InfoItemUpdateResponse updateInfoItemResponse = infostoreAPI.updateInfoItem(apiClient1.getSession(), objectId, timestamp, body, null);
        assertNotNull(updateInfoItemResponse);
        assertNull(updateInfoItemResponse.getErrorDesc(), updateInfoItemResponse.getError());
    }

    private List<DriveAction> syncFolder(ApiClient apiClient, String folderId, String clientChecksum, String originalChecksum) throws ApiException {
        DriveSyncFolderBody driveSyncFolderBody = new DriveSyncFolderBody();

        DriveDirectoryVersion originalVersion = new DriveDirectoryVersion();
        originalVersion.setPath("/");
        originalVersion.setChecksum(originalChecksum);
        driveSyncFolderBody.setOriginalVersion(originalVersion);

        DriveDirectoryVersion clientVersion = new DriveDirectoryVersion();
        clientVersion.setPath("/");
        clientVersion.setChecksum(clientChecksum);
        driveSyncFolderBody.setClientVersion(clientVersion);

        DriveSyncFolderResponse syncFolderResponse = driveApi.syncFolder(apiClient.getSession(), folderId, driveSyncFolderBody, I(2), null, null, null, null);
        assertNotNull(syncFolderResponse);
        assertNull(syncFolderResponse.getErrorDesc(), syncFolderResponse.getError());
        assertNotNull(syncFolderResponse.getData());
        assertNotNull(syncFolderResponse.getData().getActions());
        return syncFolderResponse.getData().getActions();
    }
}
