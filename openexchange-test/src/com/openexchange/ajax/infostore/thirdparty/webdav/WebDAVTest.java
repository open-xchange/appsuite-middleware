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

package com.openexchange.ajax.infostore.thirdparty.webdav;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import org.jcodec.common.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.Strings;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ConfigResponse;
import com.openexchange.testing.httpclient.models.FileAccountCreationResponse;
import com.openexchange.testing.httpclient.models.FileAccountData;
import com.openexchange.testing.httpclient.models.FileAccountUpdateResponse;
import com.openexchange.testing.httpclient.models.FileAccountsResponse;
import com.openexchange.testing.httpclient.models.FolderBody;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderResponse;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.FoldersCleanUpResponse;
import com.openexchange.testing.httpclient.models.InfoItemData;
import com.openexchange.testing.httpclient.models.InfoItemListElement;
import com.openexchange.testing.httpclient.models.InfoItemResponse;
import com.openexchange.testing.httpclient.models.InfoItemUpdateResponse;
import com.openexchange.testing.httpclient.models.InfoItemsMovedResponse;
import com.openexchange.testing.httpclient.models.InfoItemsResponse;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.modules.ConfigApi;
import com.openexchange.testing.httpclient.modules.FilestorageApi;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.InfostoreApi;

/**
 * {@link WebDAVTest} - Tests the integration of an external WebDAV server as FileStorageAccount
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.4
 */
public class WebDAVTest extends AbstractConfigAwareAPIClientSession {

    /**
     * The constant for the WebDAV FileStorageService
     */
    private static final String WEBDAV_FILE_STORAGE_SERVICE = "webdav";

    /**
     * The constant for the display name of the FileStorageService
     */
    private static final String WEBDAV_FILE_STORAGE_SERVICE_DISPLAY_NAME = "WebDAV test storage";

    /**
     * The base WebDAV URL to use
     */
    private static final String WEB_DAV_URL = "http://localhost:8009/servlet/webdav.infostore";

    private FilestorageApi filestorageApi;
    private InfostoreApi infostoreApi;
    private FoldersApi foldersApi;
    private FileAccountData testFileAccount;
    private String privateInfostoreFolder;

    private final byte[] testContent = "This is a test content".getBytes();;

    public static class WebDAVFileAccountConfiguration {

        private final String url;
        private final String login;
        private final String password;

        /**
         * Initializes a new {@link WebDAVTest.WebDAVFileAccountConfiguration}.
         *
         * @param url The WebDAV root URL to use
         * @param login The login for authentication against the WebDAV system
         * @param password The password for authentication against the WebDAV system
         */
        public WebDAVFileAccountConfiguration(String url, String login, String password) {
            this.url = url;
            this.login = login;
            this.password = password;
        }

        /**
         * The URL of the WebDAV server
         *
         * @return The URL
         */
        public String getUrl() {
            return url;
        }

        /**
         * The login used to access the WebDAV server
         *
         * @return The login
         */
        public String getLogin() {
            return login;
        }

        /**
         * The password used to access the WebDAV server
         *
         * @return
         */
        public String getPassword() {
            return password;
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        super.setUpConfiguration();

        filestorageApi = new FilestorageApi(getApiClient());
        infostoreApi = new InfostoreApi(getApiClient());
        foldersApi = new FoldersApi(getApiClient());

        //Register a new WebDAV FileAccount
        testFileAccount = new FileAccountData();
        testFileAccount.setFilestorageService(getFileStorageService());
        testFileAccount.setDisplayName(getFileStorageServiceDisplayName());
        testFileAccount.setConfiguration(new WebDAVFileAccountConfiguration(WEB_DAV_URL, testUser.getUser(), testUser.getPassword()));
        FileAccountCreationResponse response = filestorageApi.createFileAccount(getSessionId(), testFileAccount);
        String newAccountId = checkResponse(response.getError(), response.getErrorDesc(), response.getData());
        testFileAccount.setId(newAccountId);
    }

    @Override
    public void tearDown() throws Exception {
        try {
            //Delete the created WebDAV FileAccount again
            if (testFileAccount.getId() != null) {
                FileAccountUpdateResponse response = filestorageApi.deleteFileAccount(getSessionId(), testFileAccount.getFilestorageService(), testFileAccount.getId());
                Integer responseData = checkResponse(response.getError(), response.getErrorDesc(), response.getData());
                Assert.assertThat(responseData, is(I(1)));
            }
        } finally {
            super.tearDown();
        }
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        HashMap<String, String> configuration = new HashMap<String, String>();
        configuration.put("com.openexchange.capability.filestorage_webdav", Boolean.TRUE.toString());
        configuration.put("com.openexchange.file.storage.webdav.blacklistedHosts", "");
        return configuration;
    }

    @Override
    protected String getReloadables() {
        return "CapabilityReloadable";
    }

    /**
     * Gets the FileStorageService to talk to
     *
     * @return {@link WEBDAV_FILE_STORAGE_SERVICE}
     */
    protected String getFileStorageService() {
        return WEBDAV_FILE_STORAGE_SERVICE;
    }

    /**
     * Gets the display name of the file storage account
     *
     * @return {@link WEBDAV_FILE_STORAGE_SERVICE_DISPLAY_NAME}
     */
    protected String getFileStorageServiceDisplayName() {
        return WEBDAV_FILE_STORAGE_SERVICE_DISPLAY_NAME;
    }

    /**
     * Gets the WebDAV root folder
     *
     * @return The root folder
     */
    private String getRootFolderId() {
        return getFolderId(null);
    }


    /**
     * Returns a random file name
     *
     * @return The random file name
     */
    private String getRandomFileName() {
        Random random = new Random();
        return String.format("Test-File-%s", I(random.nextInt()));
    }

    /**
     * Returns a random folder name
     *
     * @return The random folder name
     */
    private String getRandomFolderName() {
        Random random = new Random();
        return String.format("Test-Folder-%s", I(random.nextInt()));
    }

    /**
     *
     * Gets the WebDAV folder ID for a given path
     *
     * @param path The path
     * @return The folderID
     */
    private String getFolderId(String path) {
        //<service>://<id>/<base64path>
        //for example: webdav://18/VXNlcnN0b3JlL2FudG9uJTIwYW50b24v
        testFileAccount.getId();
        testFileAccount.getFilestorageService();
        String rootFolder = String.format("Userstore/%s%s%s/", testUser.getUser(), ",%20", testUser.getUser());
        if (Strings.isNotEmpty(path)) {
            rootFolder += path;
        }
        rootFolder = Base64.getUrlEncoder().withoutPadding().encodeToString(rootFolder.getBytes(StandardCharsets.UTF_8));
        return String.format("%s://%s/%s", testFileAccount.getFilestorageService(), testFileAccount.getId(), rootFolder);
    }

    private InfoItemData getInfoItem(String id, String folder) throws ApiException {
        InfoItemResponse response = infostoreApi.getInfoItem(getSessionId(), id, folder);
        InfoItemData itemData = checkResponse(response.getError(), response.getErrorDesc(), response.getData());
        assertThat(id, is(itemData.getId()));
        assertThat(folder, is(itemData.getFolderId()));
        return itemData;
    }

    /**
     * Helper method to delete an item
     *
     * @param hardDelete Whether or not to perform a hardDelete
     * @param toDelete The items to delete
     * @throws ApiException
     */
    protected void deleteInfoItems(boolean hardDelete, InfoItemListElement... toDelete) throws ApiException {
        InfoItemsResponse deleteInfoItems = infostoreApi.deleteInfoItems(getApiClient().getSession(), L(System.currentTimeMillis()), Arrays.asList(toDelete), B(hardDelete), null);
        Assert.assertNull(deleteInfoItems.getError());
        Assert.assertNotNull(deleteInfoItems.getData());
        Object data = deleteInfoItems.getData();
        Assert.assertTrue(data instanceof ArrayList<?>);
        ArrayList<?> arrayData = (ArrayList<?>) data;
        assertThat(I(0), is(I(arrayData.size())));
    }

    /**
     * Helper method to upload a new item
     *
     * @param folderId The ID of the folder to upload an item to
     * @param fileName The name of the item to upload
     * @param content The content to upload
     * @param mimeTyp The mime-type of the content
     * @return The ID of the new item
     * @throws ApiException
     */
    protected String uploadInfoItem(String folderId, String fileName, byte[] content, String mimeType) throws ApiException {
        return uploadInfoItem(folderId, null, fileName, mimeType, null, content, null, null, null);
    }

    /**
     * Helper method to upload a new item
     *
     * @param folderId The ID of the folder to upload an item to
     * @param id The ID of the item
     * @param fileName The name of the item
     * @param mimeType The mime-type of the content
     * @param versionComment A version comment
     * @param bytes The content
     * @param offset The offset
     * @param filesize The size of the content
     * @param timestamp The time stamp
     * @return The ID of the new item
     * @throws ApiException
     */
    protected String uploadInfoItem(String folderId, String id, String fileName, String mimeType, String versionComment, byte[] bytes, Long offset, Long filesize, Long timestamp) throws ApiException {
        InfoItemUpdateResponse uploadInfoItem = infostoreApi.uploadInfoItem(getApiClient().getSession(), folderId, fileName, bytes, timestamp, id, fileName, mimeType, null, null, null, null, versionComment, null, null, filesize == null ? Long.valueOf(bytes.length) : filesize, Boolean.FALSE, Boolean.FALSE, offset, null);
        Assert.assertNull(uploadInfoItem.getErrorDesc(), uploadInfoItem.getError());
        Assert.assertNotNull(uploadInfoItem.getData());
        timestamp = uploadInfoItem.getTimestamp();
        return uploadInfoItem.getData();
    }

    /**
     * Creates a folder
     *
     * @param parentFolder The parent folder of the new folder
     * @param title The title of the new folder
     * @return The ID of the new folder
     * @throws ApiException
     */
    protected String createFolder(String parentFolder, String title) throws ApiException {
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setModule(Module.INFOSTORE.getName());
        folder.setTitle(title);
        body.setFolder(folder);
        FolderUpdateResponse response = foldersApi.createFolder(parentFolder, getSessionId(), body, null, null, null, null);
        return checkResponse(response.getError(), response.getErrorDesc(), response.getData());
    }

    /**
     * Gets a folder
     *
     * @param id The ID of the folder to get
     * @return The {@link FolderData} of the folder with the given id
     * @throws ApiException
     */
    protected FolderData getFolder(String id) throws ApiException {
        FolderResponse response = foldersApi.getFolder(getSessionId(), id, null, null, null);
        return checkResponse(response.getError(), response.getErrorDesc(), response.getData());
    }

    /**
     * Deletes a folder
     *
     * @param folderId The folder to deelte
     * @param hardDelete true to perform a hard-delete, false to put the folder into the trash-bin
     * @return A list of folder IDs which could <b>NOT</b> be removed due conflicts or errors
     * @throws ApiException
     */
    protected List<String> deleteFolder(String folderId, boolean hardDelete) throws ApiException {
        final boolean failOnError = true;
        final boolean extendedResponse = false;
        FoldersCleanUpResponse response = foldersApi.deleteFolders(getSessionId(), Collections.singletonList(folderId), null, null, null, B(hardDelete), B(failOnError), B(extendedResponse), null);
        return checkResponse(response.getError(), response.getErrorDesc(), response.getData());
    }

    /**
     * Helper method to gain the root folder ID of the original infostore
     *
     * @return the folder ID of the infostore
     * @throws ApiException
     */
    protected String getPrivateInfostoreFolder() throws ApiException {
        if (null == privateInfostoreFolder) {
            ConfigApi configApi = new ConfigApi(getApiClient());
            ConfigResponse configNodeResponse = configApi.getConfigNode(Tree.PrivateInfostoreFolder.getPath(), getApiClient().getSession());
            Object data = checkResponse(configNodeResponse.getError(), configNodeResponse.getErrorDesc(), configNodeResponse.getData());
            if (data != null && !data.toString().equalsIgnoreCase("null")) {
                privateInfostoreFolder = String.valueOf(data);
            } else {
                Assert.fail("It seems that the user doesn't support drive.");
            }

        }
        return privateInfostoreFolder;
    }

    @Test
    public void testGetAllFileAccounts() throws Exception {
        final boolean connectionCheck = true;
        FileAccountsResponse response = filestorageApi.getAllFileAccounts(getSessionId(), null, B(connectionCheck));
        List<FileAccountData> allAccounts = checkResponse(response.getError(), response.getErrorDesc(), response.getData());
        assertThat(allAccounts, is(not(empty())));
        List<FileAccountData> accountData = allAccounts.stream().filter(a -> a.getId().equals(testFileAccount.getId())).collect(Collectors.toList());
        assertThat(accountData, is(not(nullValue())));
        FileAccountData fileAccount = accountData.get(0);
        assertThat(fileAccount.getHasError(), is(nullValue()));
        assertThat(fileAccount.getError(), is(emptyOrNullString()));
    }

    @Test
    public void testRegisterIncorrectAccountNotPossible() throws Exception {
        final String incorrectURL = "http://notExisting.example.org/servlet/webdav.infostore";
        final String incorrectDisplayName = "IncorrectAccount";

        FileAccountData incorrectFileAccount = new FileAccountData();
        incorrectFileAccount.setFilestorageService(getFileStorageService());
        incorrectFileAccount.setDisplayName(incorrectDisplayName);
        incorrectFileAccount.setConfiguration(new WebDAVFileAccountConfiguration(incorrectURL, testUser.getUser(), testUser.getPassword()));
        FileAccountCreationResponse response = filestorageApi.createFileAccount(getSessionId(), incorrectFileAccount);
        assertThat(response.getError(), not(emptyOrNullString()));

        final boolean connectionCheck = true;
        FileAccountsResponse allResponse = filestorageApi.getAllFileAccounts(getSessionId(), null, B(connectionCheck));
        List<FileAccountData> allAccounts = checkResponse(allResponse.getError(), allResponse.getErrorDesc(), allResponse.getData());
        List<FileAccountData> shouldBeEmpty = allAccounts.stream().filter(
            a -> a.getDisplayName().equals(incorrectDisplayName)).collect(Collectors.toList());
        assertThat(shouldBeEmpty, is(empty()));
    }

    /**
     * Tests to create and delete a file
     *
     * @throws Exception
     */
    @Test
    public void testCreateDeleteFile() throws Exception {
        //Create a new file
        String fileName = getRandomFileName();
        String newFileId = uploadInfoItem(getRootFolderId(), fileName, testContent, "application/text");
        InfoItemListElement newItem = new InfoItemListElement();
        newItem.setFolder(getRootFolderId());
        newItem.setId(newFileId);

        //Check if the file is there
        getInfoItem(newItem.getId(), newItem.getFolder());

        //delete the file again
        deleteInfoItems(true, newItem);

        //Gone?
        InfoItemResponse getResponse2 = infostoreApi.getInfoItem(getSessionId(), newItem.getId(), newItem.getFolder());
        assertThat(getResponse2.getData(), is(nullValue()));

        //There was a bug which causes a duplicated, null byte, file. Check that this file is not being created anymore
        String mustNotExist = fileName + " (1)";
        InfoItemsResponse allResponse = infostoreApi.getAllInfoItems(getSessionId(), getPrivateInfostoreFolder(), "700" /*title*/, null, null, null, null, null, null);
        List<List<String>> ret = (List<List<String>>)checkResponse(allResponse.getError(), allResponse.getErrorDesc(), allResponse.getData());
        for(List<String> itemData : ret) {
            for(String data : itemData) {
                assertThat(data, is(not(mustNotExist)));
            }
        }
    }

    /**
     * Tests to copy a file to another folder
     *
     * @throws Exception
     */
    @Test
    public void testCopyFile() throws Exception {
        //Create a new file in the root folder
        String newFileId = uploadInfoItem(getRootFolderId(), getRandomFileName(), testContent, "application/text");
        InfoItemListElement newItem = new InfoItemListElement();
        newItem.setFolder(getRootFolderId());
        newItem.setId(newFileId);

        //Create a destination folder
        String destinationFolderId = createFolder(getRootFolderId(), getRandomFolderName());
        FolderData folder = getFolder(destinationFolderId);
        assertThat(folder.getId(), is(destinationFolderId));

        //Copy the file
        InfoItemData fileToCopy = getInfoItem(newItem.getId(), newItem.getFolder());
        fileToCopy.setFolderId(destinationFolderId);
        InfoItemUpdateResponse response = infostoreApi.copyInfoItem(getSessionId(), fileToCopy.getId(), fileToCopy, null);
        String copiedId = checkResponse(response.getError(), response.getErrorDesc(), response.getData());

        //Get The file and check if the content is okay
        InfoItemData copiedInfoItem = getInfoItem(copiedId, destinationFolderId);
        //Check if the file is present get the content
        File copiedContent = infostoreApi.getInfoItemDocument(getSessionId(), destinationFolderId, copiedInfoItem.getId(), null, null, null, null, null, null, null, null, null, null, null, null, null);
        try {
            assertThat(copiedContent, is(not(nullValue())));
            byte[]  data = IOUtils.readFileToByteArray(copiedContent);
            assertThat(data, is(testContent));
        }
        finally {
            //cleanup
            Files.delete(copiedContent.toPath());
        }

        final boolean hardDelete = true;
        deleteInfoItems(hardDelete, newItem);
        deleteFolder(destinationFolderId, hardDelete);
    }


    /**
     * Test to move a file to another folder
     *
     */
    @Test
    public void testMoveFile() throws Exception {

        //Create a new file in the root folder
        String newFileId = uploadInfoItem(getRootFolderId(), getRandomFileName(), testContent, "application/text");
        InfoItemListElement newItem = new InfoItemListElement();
        newItem.setFolder(getRootFolderId());
        newItem.setId(newFileId);

        //Create a destination folder
        String destinationFolderId = createFolder(getRootFolderId(), getRandomFolderName());
        FolderData folder = getFolder(destinationFolderId);
        assertThat(folder.getId(), is(destinationFolderId));

        //InfoItemsMovedResponse response = infostoreApi.moveFile(getSessionId(), L(0), destinationFolderId, newFileId, null);
        InfoItemListElement itemToMove = new InfoItemListElement();
        itemToMove.setFolder(getRootFolderId());
        itemToMove.setId(newFileId);
        InfoItemsMovedResponse response = infostoreApi.moveInfoItems(getSessionId(), destinationFolderId, Collections.singletonList(itemToMove), null);
        List<String> notMoved = checkResponse(response.getError(), response.getErrorDesc(), response.getData());
        assertThat(notMoved, is(empty()));

        //check that the original file is not present anymore
        InfoItemResponse checkResponse = infostoreApi.getInfoItem(getSessionId(), newItem.getId(), newItem.getFolder());
        assertThat(checkResponse.getError(), not(emptyOrNullString()));
        assertThat(checkResponse.getErrorDesc(), not(emptyOrNullString()));
        assertThat(checkResponse.getCode(), is("FILE_STORAGE-0026"));

        //Get the moved file and check it's content
        InfoItemsResponse allResponse = infostoreApi.getAllInfoItems(getSessionId(), destinationFolderId, "1", null, null, null, null, null, null);
        List<List<String>> ret = (List<List<String>>)checkResponse(allResponse.getError(), allResponse.getErrorDesc(), allResponse.getData());
        assertThat(ret, is(not(empty())));
        assertThat(I(ret.size()), is(I(1)));
        String movedId = ret.get(0).get(0);
        File movedContent = infostoreApi.getInfoItemDocument(getSessionId(), destinationFolderId, movedId, null, null, null, null, null, null, null, null, null, null, null, null, null);
        try {
            assertThat(movedContent, is(not(nullValue())));
            byte[]  data = IOUtils.readFileToByteArray(movedContent);
            assertThat(data, is(testContent));
        }
        finally {
            //cleanup
            Files.delete(movedContent.toPath());
        }

        final boolean hardDelete = true;
        deleteFolder(destinationFolderId, hardDelete);

    }

    /**
     * Tests to create and delete a folder
     *
     * @throws Exception
     */
    @Test
    public void testCreateDeleteFolder() throws Exception {

        //Create
        String newFolderId = createFolder(getRootFolderId(), getRandomFolderName());
        FolderData folder = getFolder(newFolderId);
        assertThat(folder.getId(), is(newFolderId));

        //Delete the folder
        final boolean hardDelete = true;
        List<String> notDeletedIds = deleteFolder(newFolderId, hardDelete);
        assertThat(notDeletedIds, not(contains("")));

        //Folder gone?
        FolderResponse response = foldersApi.getFolder(getSessionId(), newFolderId, null, null, null);
        assertThat(response.getError(), not(emptyOrNullString()));
        assertThat(response.getErrorDesc(), not(emptyOrNullString()));
        assertThat(response.getCode(), is("FILE_STORAGE-0007"));  /* FOLDER_NOT_FOUND */
    }

    /**
     *
     * Tests to move a folder into another folder
     *
     * @throws Exception
     */
    @Test
    public void TestMoveFolder() throws Exception {

        //Create a folder
        String newFolderId = createFolder(getRootFolderId(), getRandomFolderName());
        FolderData folder = getFolder(newFolderId);
        assertThat(folder.getId(), is(newFolderId));

        //Create a 2nd folder
        String newFolderId2 = createFolder(getRootFolderId(), getRandomFolderName());
        FolderData folder2 = getFolder(newFolderId2);
        assertThat(folder2.getId(), is(newFolderId2));

        //Move the 2nd folder into the first one
        FolderBody folderBody = new FolderBody();
        FolderData folderData = new FolderData();
        folderData.setFolderId(newFolderId);
        folderBody.setFolder(folderData);
        FolderUpdateResponse updateResponse = foldersApi.updateFolder(getSessionId(), newFolderId2, folderBody, null, null, null, null, null, null, null);
        String movedFolderId = checkResponse(updateResponse.getError(), updateResponse.getErrorDesc(), updateResponse.getData());

        //Folder present?
        FolderResponse checkResponse = foldersApi.getFolder(getSessionId(), movedFolderId, null, null, null);
        FolderData checkedFolderData = checkResponse(checkResponse.getError(), checkResponse.getErrorDesc(), checkResponse.getData());
        assertThat(checkedFolderData.getId(), is(movedFolderId));

        //Check that the moved folder is gone
        FolderResponse checkMoved = foldersApi.getFolder(getSessionId(), newFolderId2, null, null, null);
        assertThat(checkMoved.getError(), not(emptyOrNullString()));
        assertThat(checkMoved.getErrorDesc(), not(emptyOrNullString()));
        assertThat(checkMoved.getCode(), is("FILE_STORAGE-0007"));  /* FOLDER_NOT_FOUND */

        final boolean hardDelete = true;
        deleteFolder(newFolderId, hardDelete);
    }
}
