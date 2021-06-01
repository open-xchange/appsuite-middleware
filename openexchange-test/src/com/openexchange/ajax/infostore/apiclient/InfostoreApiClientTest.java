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

package com.openexchange.ajax.infostore.apiclient;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.modules.Module;
import com.openexchange.junit.Assert;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ConfigResponse;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.FoldersCleanUpResponse;
import com.openexchange.testing.httpclient.models.InfoItemBody;
import com.openexchange.testing.httpclient.models.InfoItemData;
import com.openexchange.testing.httpclient.models.InfoItemListElement;
import com.openexchange.testing.httpclient.models.InfoItemMovedResponse;
import com.openexchange.testing.httpclient.models.InfoItemPermission;
import com.openexchange.testing.httpclient.models.InfoItemResponse;
import com.openexchange.testing.httpclient.models.InfoItemUpdateResponse;
import com.openexchange.testing.httpclient.models.InfoItemsMovedResponse;
import com.openexchange.testing.httpclient.models.InfoItemsResponse;
import com.openexchange.testing.httpclient.models.InfoItemsRestoreResponse;
import com.openexchange.testing.httpclient.models.InfoItemsRestoreResponseData;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.modules.ConfigApi;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.InfostoreApi;
import com.openexchange.tools.io.IOTools;

/**
 *
 * {@link InfostoreApiClientTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class InfostoreApiClientTest extends AbstractConfigAwareAPIClientSession {

    protected static final int[] virtualFolders = { FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID, FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID };

    public static final String INFOSTORE_FOLDER = "infostore.folder";

    protected String folderId;
    protected String folderTitle;
    protected List<InfoItemListElement> fileIds = new ArrayList<>();
    protected List<String> folders = new ArrayList<>();

    protected String hostName = null;

    private String privateInfostoreFolder;

    protected InfostoreApi infostoreApi;

    protected static final String MIME_TEXT_PLAIN = "text/plain";
    protected static final String MIME_IMAGE_JPG = "image/jpeg";

    protected Long timestamp = null;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folderTitle = "NewInfostoreFolder" + UUID.randomUUID().toString();
        folderId = createFolderForTest(folderTitle);
        rememberFolder(folderId);
        infostoreApi = new InfostoreApi(getApiClient());
    }

    protected String uploadInfoItem(File file, String mimeType) throws ApiException, FileNotFoundException, IOException {
        return uploadInfoItem(null, file, mimeType, null);
    }

    protected String uploadInfoItem(String id, File file, String mimeType, String versionComment) throws ApiException, FileNotFoundException, IOException {
        byte[] bytes = IOTools.getBytes(new FileInputStream(file));
        return uploadInfoItem(id, file, mimeType, versionComment, bytes, null, null, null);
    }

    protected String uploadInfoItem(String id, File file, String mimeType, String versionComment, String filename) throws ApiException, FileNotFoundException, IOException {
        byte[] bytes = IOTools.getBytes(new FileInputStream(file));
        return uploadInfoItem(id, file, mimeType, versionComment, bytes, null, null, filename);
    }

    protected String uploadInfoItem(String id, File file, String mimeType, String versionComment, byte[] bytes, Long offset, Long filesize, String filename) throws ApiException {
        String name = filename == null ? file.getName() : filename;
        InfoItemUpdateResponse uploadInfoItem = infostoreApi.uploadInfoItem(folderId, name, bytes, timestamp, id, name, mimeType, null, null, null, null, versionComment, null, null, filesize == null ? Long.valueOf(bytes.length) : filesize, Boolean.FALSE, Boolean.FALSE, offset, null);
        Assert.assertNull(uploadInfoItem.getErrorDesc(), uploadInfoItem.getError());
        Assert.assertNotNull(uploadInfoItem.getData());
        timestamp = uploadInfoItem.getTimestamp();
        return uploadInfoItem.getData();
    }

    protected String uploadInfoItem(String id, InfoItemData file, String mimeType, String versionComment, byte[] bytes, Long offset, Long filesize, String filename) throws ApiException {
        String name = filename == null ? file.getFilename() : filename;
        // @formatter:off
        InfoItemUpdateResponse uploadInfoItem = infostoreApi.uploadInfoItem(
            folderId,
            name,
            bytes,
            timestamp,
            id,
            name,
            mimeType,
            null,
            null,
            null,
            null,
            versionComment,
            null,
            null,
            filesize == null ? Long.valueOf(bytes.length) : filesize,
            Boolean.FALSE,
            Boolean.FALSE,
            offset,
            null);
        // @formatter:on
        Assert.assertNull(uploadInfoItem.getErrorDesc(), uploadInfoItem.getError());
        Assert.assertNotNull(uploadInfoItem.getData());
        timestamp = uploadInfoItem.getTimestamp();
        return uploadInfoItem.getData();
    }

    protected String uploadInfoItemToFolder(String id, File file, String parentFolderId, String mimeType, String versionComment, byte[] bytes, Long offset, Long filesize, String filename) throws ApiException, FileNotFoundException, IOException {
        String name = filename == null ? file.getName() : filename;
        byte[] bytesData = bytes == null ? IOTools.getBytes(new FileInputStream(file)) : bytes;
        InfoItemUpdateResponse uploadInfoItem = infostoreApi.uploadInfoItem(parentFolderId, name, bytesData, timestamp, id, name, mimeType, null, null, null, null, versionComment, null, null, filesize == null ? Long.valueOf(bytesData.length) : filesize, Boolean.FALSE, Boolean.FALSE, offset, null);
        Assert.assertNull(uploadInfoItem.getErrorDesc(), uploadInfoItem.getError());
        Assert.assertNotNull(uploadInfoItem.getData());
        timestamp = uploadInfoItem.getTimestamp();
        return uploadInfoItem.getData();
    }

    protected void uploadInfoItemWithError(String id, File file, String mimeType, String versionComment) throws FileNotFoundException, IOException {
        byte[] bytes = IOTools.getBytes(new FileInputStream(file));
        uploadInfoItemWithError(id, file, mimeType, versionComment, bytes, null, null, null);
    }

    protected void uploadInfoItemWithError(String id, File file, String mimeType, String versionComment, String filename) throws FileNotFoundException, IOException {
        byte[] bytes = IOTools.getBytes(new FileInputStream(file));
        uploadInfoItemWithError(id, file, mimeType, versionComment, bytes, null, null, filename);
    }

    protected void uploadInfoItemWithError(String id, File file, String mimeType, String versionComment, byte[] bytes, Long offset, Long filesize, String filename) {
        try {
            String name = filename == null ? file.getName() : filename;
            infostoreApi.uploadInfoItem(folderId, name, bytes, timestamp, id, name, mimeType, null, null, null, null, versionComment, null, null, filesize == null ? Long.valueOf(bytes.length) : filesize, Boolean.FALSE, Boolean.FALSE, offset, null);
            // Should not succeed
            Assert.fail("Request expected to fail but succeeded");
        } catch (ApiException e) {
            // Expected to get here
            Assert.assertEquals("Status code does not match", 413, e.getCode());
            Assert.assertTrue("Wrong error message", e.getMessage().contains("exceeds the maximum configured file size"));
        }
    }

    protected void rememberFile(String id, String folder) {
        InfoItemListElement element = new InfoItemListElement();
        element.setId(id);
        element.setFolder(folder);
        fileIds.add(element);
    }

    protected void rememberFolder(String folder) {
        folders.add(folder);
    }

    protected InfoItemData getItem(String id) throws ApiException {
        InfoItemResponse infoItem = infostoreApi.getInfoItem(id, folderId);
        Assert.assertNull(infoItem.getError());
        Assert.assertNotNull(infoItem.getData());
        return infoItem.getData();
    }

    private String createFolderForTest(String title) throws ApiException {
        final String parent = getPrivateInfostoreFolder();
        FoldersApi folderApi = new FoldersApi(getApiClient());
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setModule(Module.INFOSTORE.getName());
        folder.setSummary(title);
        folder.setTitle(folder.getSummary());
        folder.setSubscribed(Boolean.TRUE);
        folder.setPermissions(null);
        body.setFolder(folder);
        FolderUpdateResponse folderUpdateResponse = folderApi.createFolder(parent, body, "1", null, null, null);
        return checkResponse(folderUpdateResponse);
    }

    protected void deleteFolder(String folderId, Boolean hardDelete) throws ApiException {
        FoldersApi folderApi = new FoldersApi(getApiClient());
        FoldersCleanUpResponse deleteFolderResponse = folderApi.deleteFolders(Collections.singletonList(folderId), "1", timestamp, null, hardDelete, Boolean.TRUE, Boolean.FALSE, null, Boolean.FALSE);
        Assert.assertNull(deleteFolderResponse.getErrorDesc(), deleteFolderResponse.getError());
        Assert.assertNotNull(deleteFolderResponse.getData());
        Assert.assertEquals(0, deleteFolderResponse.getData().size());
    }


    public String getPrivateInfostoreFolder() throws ApiException {
        if (null == privateInfostoreFolder) {
            privateInfostoreFolder = getPrivateInfostoreFolder(getApiClient());
        }
        return privateInfostoreFolder;
    }

    public String getPrivateInfostoreFolder(ApiClient apiClient) throws ApiException {
        ConfigApi configApi = new ConfigApi(apiClient);
        ConfigResponse configNode = configApi.getConfigNode(Tree.PrivateInfostoreFolder.getPath());
        Object data = checkResponse(configNode);
        if (data != null && !data.toString().equalsIgnoreCase("null")) {
            return String.valueOf(data);
        }
        Assert.fail("It seems that the user doesn't support drive.");

        return null;
    }

    private Object checkResponse(ConfigResponse resp) {
        Assert.assertNull(resp.getErrorDesc(), resp.getError());
        Assert.assertNotNull(resp.getData());
        return resp.getData();
    }

    private String checkResponse(FolderUpdateResponse resp) {
        Assert.assertNull(resp.getErrorDesc(), resp.getError());
        Assert.assertNotNull(resp.getData());
        return resp.getData();
    }

    protected void deleteInfoItems(List<InfoItemListElement> toDelete, Boolean hardDelete) throws ApiException {
        InfoItemsResponse deleteInfoItems = infostoreApi.deleteInfoItems(timestamp == null ? L(System.currentTimeMillis()) : timestamp, toDelete, hardDelete, null);
        Assert.assertNull(deleteInfoItems.getError());
        Assert.assertNotNull(deleteInfoItems.getData());
        timestamp = deleteInfoItems.getTimestamp();
        Object data = deleteInfoItems.getData();
        Assert.assertTrue(data instanceof ArrayList<?>);
        ArrayList<?> arrayData = (ArrayList<?>) data;
        assertEquals(0, arrayData.size());
    }

    protected String copyInfoItem(String id, InfoItemData modifiedData) throws ApiException {
        InfoItemUpdateResponse response = infostoreApi.copyInfoItem(id, modifiedData, null);
        Assert.assertNull(response.getError());
        Assert.assertNotNull(response.getData());
        timestamp = response.getTimestamp();
        return response.getData();
    }

    protected List<InfoItemListElement> moveInfoItems(String id, List<InfoItemListElement> toMove) throws ApiException {
        InfoItemsMovedResponse response = infostoreApi.moveInfoItems(id, toMove, null, null);
        Assert.assertNull(response.getError());
        Assert.assertNotNull(response.getData());
        timestamp = response.getTimestamp();
        return response.getData();
    }

    protected InfoItemsMovedResponse moveInfoItem(String fileId, String sourceFolderId, String destinationFolderId, boolean ignoreWarnings) throws ApiException {
        InfoItemListElement itemToMove = new InfoItemListElement();
        itemToMove.setFolder(sourceFolderId);
        itemToMove.setId(fileId);
        InfoItemsMovedResponse response = infostoreApi.moveInfoItems(destinationFolderId, Collections.singletonList(itemToMove), null, B(ignoreWarnings));
        if (response.getError() == null) {
            timestamp = response.getTimestamp();
        }
        return response;
    }

    protected InfoItemsMovedResponse moveInfoItems(List<String> fileIds, String sourceFolderId, String destinationFolderId, boolean ignoreWarnings) throws ApiException {
        List<InfoItemListElement> itemsToMove = new ArrayList<InfoItemListElement>();
        for (String fileId : fileIds) {
            InfoItemListElement itemToMove = new InfoItemListElement();
            itemToMove.setFolder(sourceFolderId);
            itemToMove.setId(fileId);
            itemsToMove.add(itemToMove);
        }
        InfoItemsMovedResponse response = infostoreApi.moveInfoItems(destinationFolderId, itemsToMove, null, B(ignoreWarnings));
        if (response.getError() == null) {
            timestamp = response.getTimestamp();
        }
        return response;
    }

    protected InfoItemMovedResponse moveFile(String fileId, String sourceFolderId, String destinationFolderId, boolean ignoreWarnings) throws ApiException {
        InfoItemListElement itemToMove = new InfoItemListElement();
        itemToMove.setFolder(sourceFolderId);
        itemToMove.setId(fileId);
        InfoItemMovedResponse response = infostoreApi.moveFile(timestamp, destinationFolderId, fileId, null, B(ignoreWarnings));
        if (response.getError() == null) {
            timestamp = response.getTimestamp();
        }
        return response;
    }

    protected List<InfoItemsRestoreResponseData> restoreInfoItems(List<InfoItemListElement> toRestore) throws ApiException {
        InfoItemsRestoreResponse restoredItems = infostoreApi.restoreInfoItemsFromTrash(toRestore, null);
        Assert.assertNull(restoredItems.getError());
        Assert.assertNotNull(restoredItems.getData());
        timestamp = restoredItems.getTimestamp();
        return restoredItems.getData();
    }

    protected void assertFileExistsInFolder(String folderId, String itemId) throws Exception {
        // @formatter:off
        InfoItemsResponse allInfoItems = infostoreApi.getAllInfoItems(
            folderId,
            Integer.toString(Metadata.ID),
            null,
            null,
            null,
            null,
            null,
            null);
        // @formatter:off
        List<List<String>> ret = (List<List<String>>)checkResponse(allInfoItems.getError(), allInfoItems.getErrorDesc(), allInfoItems.getData());
        Assert.assertTrue("The item is not present in the given folder", ret.stream().filter( l -> l.contains(itemId)).count() == 1);
    }

    /**
     * Updates the permissions of the document with the given id
     *
     * @param id The id of the document
     * @param perms The new permissions
     * @throws ApiException
     */
    protected void updatePermissions(String id, List<InfoItemPermission> perms) throws ApiException {
        updatePermissions(id, perms, Optional.empty());
    }

    /**
     *
     * Updates the permissions of the document with the given id
     *
     * @param id The id of the document
     * @param perms The new permissions
     * @param errorCode The expected error code
     * @throws ApiException
     */
    protected void updatePermissions(String id, List<InfoItemPermission> perms, Optional<String> errorCode) throws ApiException {
        InfoItemData file = new InfoItemData();
        file.setObjectPermissions(perms);
        InfoItemBody body = new InfoItemBody();
        body.file(file);
        InfoItemUpdateResponse resp = infostoreApi.updateInfoItem(id, timestamp, body, null);
        if (errorCode.isPresent()) {
            assertNotNull("Expected an error but the response didn't contain one.", resp.getError());
            assertEquals("Request returned with an unexpected error: " + resp.getErrorDesc(), errorCode.get(), resp.getCode());
            return;
        }
        assertNull(resp.getError());
        assertNotNull(resp.getData());
        timestamp = resp.getTimestamp();
    }

    protected boolean fileExistsInFolder(String fileId, String folderId) throws ApiException {
        InfoItemResponse infoItem = infostoreApi.getInfoItem(fileId, folderId);
        return infoItem.getError() == null;
    }

}
