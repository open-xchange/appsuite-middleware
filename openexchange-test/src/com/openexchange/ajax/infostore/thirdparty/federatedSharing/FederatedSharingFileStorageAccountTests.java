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

package com.openexchange.ajax.infostore.thirdparty.federatedSharing;

import static com.openexchange.ajax.infostore.thirdparty.federatedSharing.FederatedSharingUtil.cleanInbox;
import static com.openexchange.ajax.infostore.thirdparty.federatedSharing.FederatedSharingUtil.prepareGuest;
import static com.openexchange.ajax.infostore.thirdparty.federatedSharing.FederatedSharingUtil.receiveShareLink;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openexchange.ajax.folder.manager.FolderManager;
import com.openexchange.ajax.infostore.thirdparty.AbstractFileStorageAccountTest;
import com.openexchange.ajax.infostore.thirdparty.federatedSharing.FederatedSharingUtil.PermissionLevel;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.test.common.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FileAccountData;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.ShareManagementApi;

/**
 * {@link FederatedSharingFileStorageAccountTests}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
@RunWith(Parameterized.class)
public class FederatedSharingFileStorageAccountTests extends AbstractFileStorageAccountTest {

    private static final String XOX8 = "xox8";
    private static final String XCTX8 = "xctx8";

    protected static final String FILE_STORAGE_SERVICE_DISPLAY_NAME = "Federated Sharing test storage";

    protected ShareManagementApi shareApi;

    private FileAccountData account;
    private String sharedFolderId;
    private FoldersApi sharingFoldersApi;
    private final String fileStorageServiceId;

    /**
     * {@link FederatedSharingFileAccountConfiguration}
     *
     * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
     * @since v7.10.5
     */
    public static class FederatedSharingFileAccountConfiguration {

        private final String shareLink;
        private final String password;

        /**
         * Initializes a new {@link FederatedSharingFileAccountConfiguration}.
         *
         * @param shareLink The share link to use
         * @param password The, optional, password
         */
        public FederatedSharingFileAccountConfiguration(String shareLink, String password) {
            super();
            this.shareLink = shareLink;
            this.password = password;
        }

        /**
         * Gets the shareLink
         *
         * @return The shareLink
         */
        @JsonProperty("url")
        public String getShareLink() {
            return shareLink;
        }

        /**
         * Gets the optional password
         *
         * @return The optional password
         */
        public String getPassword() {
            return password;
        }
    }

    /**
     * The file storages to test
     *
     * @return The storages
     */
    @SuppressWarnings("rawtypes")
    @Parameterized.Parameters(name = "{0} filestorage provider")
    public static Collection getFileStorageServicesToTest() {
        //@formatter:off
        return Arrays.asList(new Object[] {
            XCTX8,
            XOX8,
        });
        //@formatter:on
    }

    /**
     * Initializes a new {@link FederatedSharingFileStorageAccountTests}.
     *
     * @param fileStorageServiceId The ID of the service to test
     */
    public FederatedSharingFileStorageAccountTests(String fileStorageServiceId) {
        this.fileStorageServiceId = fileStorageServiceId;
    }

    protected String toXOXId(FileAccountData account, String folderId) {
        return String.format("%s://%s/%s", fileStorageServiceId, account.getId(), folderId);
    }

    protected String toXOXId(FileAccountData account, String folderId, String fileId) {
        return String.format("%s://%s/%s/%s", fileStorageServiceId, account.getId(), folderId, fileId.replace("/", "=2F"));
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.shareApi = new ShareManagementApi(getApiClient());

        // Clear inbox for the user who will receive the share
        cleanInbox(getApiClient());

        //Acquire a user who shares a folder
        TestUser context2user = testContextList.get(1).acquireUser();
        sharingFoldersApi = new FoldersApi(context2user.getApiClient());
        FolderManager folderManager = new FolderManager(sharingFoldersApi, "0");

        //The sharing user create a folder which is shared to the actual user
        FolderData sharedFolder = createFolder(folderManager, getPrivateInfostoreFolderID(context2user.getApiClient()), getRandomFolderName());
        sharedFolderId = sharedFolder.getId();

        //Share it
        shareFolder(sharedFolder, testUser, folderManager);

        //Get the share link
        String shareLink = receiveShareLink(getApiClient(), context2user.getLogin(), sharedFolder.getTitle());

        //Register an XOX account which integrates the share
        FederatedSharingFileAccountConfiguration configuration = new FederatedSharingFileAccountConfiguration(shareLink, null);
        account = createAccount(fileStorageServiceId, FILE_STORAGE_SERVICE_DISPLAY_NAME, configuration);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createApiClient().withContexts(2).build();
    }

    @Override
    public Optional<Map<String, String>> optContextConfig() {
        HashMap<String, String> configuration = new HashMap<>();
        configuration.put("com.openexchange.capability.filestorage_xox", Boolean.TRUE.toString());
        configuration.put("com.openexchange.capability.filestorage_xctx", Boolean.TRUE.toString());
        configuration.put("com.openexchange.api.client.blacklistedHosts", "");
        return Optional.of(configuration);
    }

    /**
     * Shares a folder to another user
     *
     * @param folderToShare The folder to share
     * @param to The user to share the folder with
     * @param folderManager The folder manager to update the folder with
     * @throws ApiException
     */
    private void shareFolder(FolderData folderToShare, TestUser to, FolderManager folderManager) throws ApiException {
        List<FolderPermission> permissions = new ArrayList<FolderPermission>(folderToShare.getPermissions().size() + 1);

        //Take over existing permissions
        permissions.addAll(folderToShare.getPermissions());

        //And ..create new guest permission
        permissions.add(prepareGuest(to, PermissionLevel.AUTHOR));

        //Set permission
        FolderData data = new FolderData();
        data.setId(folderToShare.getId());
        data.setPermissions(permissions);

        //update with new guest permission and send mail notification
        folderManager.updateFolder(sharedFolderId, data, null);
    }

    @Override
    protected String getRootFolderId() throws Exception {
        return toXOXId(account, sharedFolderId);
    }

    @Override
    protected FileAccountData getAccountData() throws Exception {
        return account;
    }

    @Override
    protected TestFile createTestFile() throws Exception {
        String fileName = getRandomFileName();
        String newFileId = uploadInfoItem(toXOXId(account, sharedFolderId), fileName, testContent, "application/text");
        return new TestFile(toXOXId(account, sharedFolderId), newFileId, fileName);
    }

    @Override
    protected Object getWrongFileStorageConfiguration() {
        return null;
    }

}
