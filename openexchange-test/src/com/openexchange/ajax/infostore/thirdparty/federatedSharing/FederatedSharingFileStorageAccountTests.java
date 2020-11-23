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

package com.openexchange.ajax.infostore.thirdparty.federatedSharing;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openexchange.ajax.infostore.thirdparty.AbstractFileStorageAccountTest;
import com.openexchange.java.Strings;
import com.openexchange.test.pool.TestContext;
import com.openexchange.test.pool.TestContextPool;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FileAccountData;
import com.openexchange.testing.httpclient.models.FolderBody;
import com.openexchange.testing.httpclient.models.FolderBodyNotification;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.MailData;
import com.openexchange.testing.httpclient.models.MailListElement;
import com.openexchange.testing.httpclient.models.MailResponse;
import com.openexchange.testing.httpclient.models.MailsCleanUpResponse;
import com.openexchange.testing.httpclient.models.MailsResponse;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.MailApi;
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

    protected static final String XOX_FILE_STORAGE_SERVICE_DISPLAY_NAME = "Federated Sharing test storage";

    protected ShareManagementApi shareApi;

    private FileAccountData account;
    private String sharedFolderId;
    private ApiClient sharingClient;
    private FoldersApi sharingFoldersApi;
    private TestContext context2;
    private final String fileStorageServiceId;

    public static class XOXFileAccountConfiguration {

        private final String shareLink;
        private final String password;

        /**
         * Initializes a new {@link XOXFileAccountConfiguration}.
         *
         * @param shareLink The share link to use
         * @param password The, optional, password
         */
        public XOXFileAccountConfiguration(String shareLink, String password) {
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
     * @param fileStorageService The ID of the service to test
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

        //Acquire a user who shares a folder
        context2 = TestContextPool.acquireContext(this.getClass().getSimpleName());
        TestUser sharingUser = context2.acquireUser();
        sharingClient = generateApiClient(sharingUser);
        sharingFoldersApi = new FoldersApi(sharingClient);

        //The sharing user create a folder which is shared to the actual user
        FolderData sharedFolder = createFolder(sharingFoldersApi, getPrivateInfostoreFolderID(sharingClient), getRandomFolderName());
        sharedFolderId = sharedFolder.getId();

        //Share it
        shareFolder(sharedFolder, sharingFoldersApi, testUser);

        //Get the share link
        String shareLink = receiveShareLinkFor(sharedFolder, getApiClient());

        //Register an XOX account which integrates the share
        XOXFileAccountConfiguration configuration = new XOXFileAccountConfiguration(shareLink, null);
        account = createAccount(fileStorageServiceId, XOX_FILE_STORAGE_SERVICE_DISPLAY_NAME, configuration);
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        HashMap<String, String> configuration = new HashMap<>();
        configuration.put("com.openexchange.capability.filestorage_xox", Boolean.TRUE.toString());
        configuration.put("com.openexchange.capability.filestorage_xctx", Boolean.TRUE.toString());
        configuration.put("com.openexchange.api.client.blacklistedHosts", "");
        return configuration;
    }

    @Override
    protected String getReloadables() {
        return "CapabilityReloadable";
    }

    @Override
    public void tearDown() throws Exception {
        TestContextPool.backContext(context2);
        super.tearDown();
    }


    /**
     * Extracts a share link related to the given folder from the notification email received.
     *
     * @param folder The folder to get the link for
     * @param client The client to use
     * @return The Share-Link
     * @throws Exception
     */
    private String receiveShareLinkFor(FolderData folder, ApiClient client) throws Exception {
        MailData mail = lookupMail(client, "default0%2FINBOX", "shared the folder \"" + folder.getTitle() + "\" with you");
        assertThat("The share mail was not found", mail, is(notNullValue()));
        @SuppressWarnings("unchecked") Map<String, String> headers = (Map<String, String>) mail.getHeaders();
        for (Iterator<Entry<String, String>> iterator = headers.entrySet().iterator(); iterator.hasNext();) {
            Entry<String, String> entry = iterator.next();
            if ("X-Open-Xchange-Share-URL".equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        throw new AssertionError("No \"X-Open-Xchange-Share-URL\" header in mail");
    }

    /**
     * Tries to get an email with the given subject
     *
     * @param apiClient The client to sue
     * @param folder The folder to get the mail from
     * @param subjectToMatch The subject to match
     * @return The (first) email with the given subject in the given folder
     * @throws Exception
     */
    private MailData lookupMail(ApiClient apiClient, String folder, String subjectToMatch) throws Exception {
        for (int i = 0; i < 10; i++) {
            MailApi mailApi = new MailApi(apiClient);
            MailsResponse mailsResponse = mailApi.getAllMails(folder, "600,601,607,610", null, null, null, "610", "desc", null, null, I(10), null);
            checkResponse(mailsResponse.getError(), mailsResponse.getErrorDesc(), mailsResponse.getData());
            for (List<String> mail : mailsResponse.getData()) {
                String subject = mail.get(2);
                if (Strings.isEmpty(subject) || false == subject.contains(subjectToMatch)) {
                    continue;
                }

                //Get The mail
                MailResponse mailResponse = mailApi.getMail(mail.get(1), mail.get(0), null, null, "noimg", Boolean.FALSE, Boolean.TRUE, null, null, null, null, null, null, null);
                MailData mailData = checkResponse(mailResponse.getError(), mailsResponse.getErrorDesc(), mailResponse.getData());

                //Delete the mail
                MailListElement mailToDelete = new MailListElement();
                mailToDelete.setFolder(mailData.getFolderId());
                mailToDelete.setId(mailData.getId());
                MailsCleanUpResponse deleteResponse = mailApi.deleteMails(Collections.singletonList(mailToDelete), L(Long.MAX_VALUE), B(true), B(false));
                List<String> deletedMailIds = checkResponse(deleteResponse.getError(), deleteResponse.getErrorDesc(), deleteResponse.getData());
                assertThat(deletedMailIds, is(empty()));

                return mailData;
            }
            TimeUnit.SECONDS.sleep(1);
        }
        return null;
    }

    /**
     * Shares a folder to another user
     *
     * @param folderToShare The folder to share
     * @param api The {@link FoldersApi} to use
     * @param to The user to share the folder with
     * @throws ApiException
     */
    private void shareFolder(FolderData folderToShare, FoldersApi api, TestUser to) throws ApiException {

        List<FolderPermission> permissions = new ArrayList<FolderPermission>(folderToShare.getPermissions().size() + 1);

        //Take over existing permissions
        permissions.addAll(folderToShare.getPermissions());

        //And ..create new guest permission
        FolderPermission permission = new FolderPermission();
        permission.setBits(I(4227332)); //Author
        permission.setEmailAddress(to.getLogin());
        permission.setDisplayName(to.getLogin());
        permission.setType("guest");
        permissions.add(permission);

        //Set permission
        FolderData data = new FolderData();
        data.setId(folderToShare.getId());
        data.setPermissions(permissions);
        FolderBody body = new FolderBody();
        body.setFolder(data);

        //Send mail notification
        FolderBodyNotification notification = new FolderBodyNotification();
        notification.setTransport("mail");
        body.notification(notification);

        //update with new guest permission
        FolderUpdateResponse updateResponse = api.updateFolder(folderToShare.getId(), body, B(false), null, null, null, null, null, null, null);
        checkResponse(updateResponse.getError(), updateResponse.getErrorDesc(), updateResponse.getData());
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
