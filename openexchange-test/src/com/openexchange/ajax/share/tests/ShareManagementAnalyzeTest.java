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

package com.openexchange.ajax.share.tests;

import static com.openexchange.ajax.folder.manager.FolderManager.INFOSTORE;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractEnhancedApiClientSession;
import com.openexchange.ajax.folder.manager.FolderApi;
import com.openexchange.ajax.folder.manager.FolderManager;
import com.openexchange.ajax.passwordchange.actions.PasswordChangeUpdateRequest;
import com.openexchange.ajax.passwordchange.actions.PasswordChangeUpdateResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.Strings;
import com.openexchange.test.pool.TestContext;
import com.openexchange.test.pool.TestContextPool;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.AddShareResponse;
import com.openexchange.testing.httpclient.models.AddShareResponseData;
import com.openexchange.testing.httpclient.models.CommonResponse;
import com.openexchange.testing.httpclient.models.FederatedShareBody;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.MailData;
import com.openexchange.testing.httpclient.models.MailListElement;
import com.openexchange.testing.httpclient.models.MailResponse;
import com.openexchange.testing.httpclient.models.MailsResponse;
import com.openexchange.testing.httpclient.models.ShareLinkAnalyzeResponse;
import com.openexchange.testing.httpclient.models.ShareLinkAnalyzeResponseData;
import com.openexchange.testing.httpclient.models.ShareLinkAnalyzeResponseData.StateEnum;
import com.openexchange.testing.httpclient.models.ShareLinkData;
import com.openexchange.testing.httpclient.models.ShareLinkResponse;
import com.openexchange.testing.httpclient.models.ShareLinkUpdateBody;
import com.openexchange.testing.httpclient.models.ShareTargetData;
import com.openexchange.testing.httpclient.modules.FilestorageApi;
import com.openexchange.testing.httpclient.modules.MailApi;
import com.openexchange.testing.httpclient.modules.ShareManagementApi;

/**
 * {@link ShareManagementAnalyzeTest} - Test for the <code>analyze</code> action of the share management module.
 * <p>
 * User 1 from context A will share the folder
 * User 2 from context B will analyze the share
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class ShareManagementAnalyzeTest extends AbstractEnhancedApiClientSession {

    /** See also com.openexchange.file.storage.oxshare.OXShareStorageConstants.ID */
    private final static String FILESTORE_SERVICE = "xox" + Module.INFOSTORE.getFolderConstant();

    /* Context 1 */
    private String sharedFolderName;
    private ShareManagementApi smApi;
    private FolderManager folderManager;
    private String infostoreRoot;

    /* Context 2 */
    private TestContext context2;
    private TestUser testUserC2;
    private ApiClient apiClientC2;
    private ShareManagementApi smApiC2;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        context2 = TestContextPool.acquireContext(this.getClass().getSimpleName());
        testUserC2 = context2.acquireUser();
        apiClientC2 = generateApiClient(testUserC2);
        rememberClient(apiClientC2);
        smApiC2 = new ShareManagementApi(apiClientC2);

        sharedFolderName = this.getClass().getSimpleName() + UUID.randomUUID().toString();
        smApi = new ShareManagementApi(apiClient);
        folderManager = new FolderManager(new FolderApi(apiClient, testUser), "1");
        infostoreRoot = folderManager.findInfostoreRoot();
    }

    @Override
    public void tearDown() throws Exception {
        try {
            if (null != context2) {
                TestContextPool.backContext(context2);
            }
            if (null != folderManager) {
                folderManager.cleanUp();
            }
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testMissingLink_APIException() throws Exception {
        ShareLinkAnalyzeResponse analyzeShareLink = smApiC2.analyzeShareLink(apiClientC2.getSession(), "");
        assertNull(analyzeShareLink.getData());
        assertNotNull(analyzeShareLink.getError(), analyzeShareLink.getErrorDesc());
        assertTrue(analyzeShareLink.getErrorDesc().equals("Missing the following request parameter: link"));
    }

    @Test
    public void testSomeLink_APIException() throws Exception {
        ShareLinkAnalyzeResponse analyzeShareLink = smApiC2.analyzeShareLink(apiClientC2.getSession(), "https://example.org/no/share/link");
        assertNull(analyzeShareLink.getData());
        assertNotNull(analyzeShareLink.getError(), analyzeShareLink.getErrorDesc());
        assertTrue(analyzeShareLink.getErrorDesc().startsWith("Unexpected error"));
    }

    @Test
    public void testAnonymousLink_Addable() throws Exception {
        String folderId = createFolder();
        ShareLinkData shareLink = getOrCreateShareLink(folderId);

        analyze(shareLink, StateEnum.ADDABLE);
    }

    @Test
    public void testAnonymousLinkWithPassword_Addable() throws Exception {
        String folderId = createFolder();
        ShareLinkData shareLink = getOrCreateShareLink(folderId);

        analyze(shareLink, StateEnum.ADDABLE);

        updateLinkWithPassword(folderId);
        analyze(getOrCreateShareLink(folderId), StateEnum.ADDABLE_WITH_PASSWORD);
    }

    @Test
    public void testAnonymousLink_Inaccessible() throws Exception {
        String folderId = createFolder();
        ShareLinkData shareLink = getOrCreateShareLink(folderId);
        deleteShareLink(folderId);

        analyze(shareLink, StateEnum.INACCESSIBLE);
    }

    /**
     * Test most status for a guest user. This is done in one test because some states
     * can only be tested by doing the same ground work.
     *
     * @throws Exception In case of failure
     */
    @Test
    public void testGuest() throws Exception {
        String folderId = createFolder();

        /*
         * Add a guest to the folder
         */
        FolderData folder = folderManager.getFolder(folderId);
        ArrayList<FolderPermission> originalPermissions = new ArrayList<>(folder.getPermissions());
        ArrayList<FolderPermission> updatedPermissions = new ArrayList<>(folder.getPermissions());
        updatedPermissions.add(prepareGuest());

        folderId = setFolderPermission(folderId, updatedPermissions);

        /*
         * Receive mail as guest and extract share link
         */
        String shareLink = receiveShareLink(apiClientC2, testUser.getLogin());
        analyze(shareLink, StateEnum.ADDABLE);

        /*
         * Add share and verify analyze changed
         */
        String accountId = addOXShareAccount(shareLink);

        /*
         * Remove guest from folder permission
         */
        folderId = setFolderPermission(folderId, originalPermissions);
        analyze(shareLink, StateEnum.INACCESSIBLE);

        /*
         * Re-add guest, account should still exist
         */
        folderId = setFolderPermission(folderId, updatedPermissions);
        analyze(shareLink, StateEnum.SUBSCRIBED);

        /*
         * Delete account and check again
         */
        new FilestorageApi(apiClientC2).deleteFileAccount(apiClientC2.getSession(), FILESTORE_SERVICE, accountId);
        analyze(shareLink, StateEnum.ADDABLE);
    }

    /**
     * Test status for a changed guest user password. This is done in one test because some states
     * can only be tested by doing the same ground work.
     *
     * @throws Exception In case of failure
     */
    @Test
    public void testGuest_PWDChange() throws Exception {
        String folderId = createFolder();

        /*
         * Add a guest to the folder
         */
        FolderData folder = folderManager.getFolder(folderId);
        ArrayList<FolderPermission> originalPermissions = new ArrayList<>(folder.getPermissions());
        ArrayList<FolderPermission> updatedPermissions = new ArrayList<>(folder.getPermissions());
        updatedPermissions.add(prepareGuest());

        folderId = setFolderPermission(folderId, updatedPermissions);

        /*
         * Receive mail as guest and extract share link
         */
        String shareLink = receiveShareLink(apiClientC2, testUser.getLogin());
        analyze(shareLink, StateEnum.ADDABLE);

        /*
         * Add share and verify analyze changed
         */
        addOXShareAccount(shareLink);

        /*
         * Change password of guest and verify response.
         */
        GuestClient guestClient = new GuestClient(shareLink, null, null, true);
        String password = "secret";
        PasswordChangeUpdateResponse response = guestClient.execute(new PasswordChangeUpdateRequest(password, null, true));
        assertThat(response.getErrorMessage(), nullValue());
        assertThat(response.getException(), nullValue());
        Object data = response.getData();
        assertThat(data, notNullValue());

        analyze(shareLink, StateEnum.CREDENTIALS_REFRESH);

        /*
         * Update password in local instance and check response
         */
        FederatedShareBody body = new FederatedShareBody();
        body.setPassword(password);
        smApiC2.updateShare(apiClientC2.getSession(), shareLink, FILESTORE_SERVICE, body);
        analyze(shareLink, StateEnum.SUBSCRIBED);

        /*
         * Delete guest from share and verify analyze changed
         */
        folderId = setFolderPermission(folderId, originalPermissions);
        analyze(shareLink, StateEnum.INACCESSIBLE);

        /*
         * Re-add guest, account should still exist and session of the guest still be valid
         */
        folderId = setFolderPermission(folderId, updatedPermissions);
        analyze(shareLink, StateEnum.SUBSCRIBED);

        /*
         * Delete account and check again
         */
        deleteOXShareAccount(shareLink);
        analyze(shareLink, StateEnum.ADDABLE_WITH_PASSWORD);
    }

    /*
     * ------------------- HELPERS -------------------
     */

    private String createFolder() throws ApiException {
        return folderManager.createFolder(infostoreRoot, sharedFolderName, INFOSTORE);
    }

    private Long now() {
        return L(System.currentTimeMillis());
    }

    /**
     * Prepares a guest permission
     * 
     * @return the guest
     */
    private FolderPermission prepareGuest() {
        FolderPermission guest = new FolderPermission();
        guest.setBits(I(257));
        guest.setEmailAddress(testUserC2.getLogin());
        guest.setType("guest");
        return guest;
    }

    /**
     * Updates the folder with the given permissions
     *
     * @param folderId The folder to update
     * @param permissions Permissions to set
     * @return The new folder ID
     * @throws ApiException In case of error
     */
    private String setFolderPermission(String folderId, ArrayList<FolderPermission> permissions) throws ApiException {
        FolderData deltaFolder = new FolderData();
        deltaFolder.setPermissions(permissions);
        return folderManager.updateFolder(folderId, deltaFolder, null);
    }

    /**
     * Creates a new share link for the given folder
     *
     * @param folder The folder to create a share link for
     * @return The guest id of for the new link
     * @throws ApiException
     */
    private ShareLinkData getOrCreateShareLink(String folder) throws ApiException {
        ShareTargetData data = new ShareTargetData();
        data.setFolder(folder);
        data.setModule(INFOSTORE);
        ShareLinkResponse shareLink = smApi.getShareLink(folderManager.getSession(), data);
        checkResponse(shareLink.getError(), shareLink.getErrorDesc(), shareLink.getData());
        folderManager.setLastTimestamp(shareLink.getTimestamp());
        return shareLink.getData();
    }

    /**
     * Deletes a share link
     *
     * @param folderId The folder ID to remove the link from
     * @throws ApiException
     */
    private void deleteShareLink(String folderId) throws ApiException {
        ShareTargetData shareTargetData = new ShareTargetData();
        shareTargetData.setFolder(folderId);
        shareTargetData.setModule(INFOSTORE);
        CommonResponse deleteShareLink = smApi.deleteShareLink(folderManager.getSession(), now(), shareTargetData);
        assertNull(deleteShareLink.getError(), deleteShareLink.getErrorDesc());
    }

    /**
     * Updates the share for the given folder with a password
     *
     * @param folderId The folder the share is on
     * @throws ApiException On error
     */
    private void updateLinkWithPassword(String folderId) throws ApiException {
        ShareLinkUpdateBody body = new ShareLinkUpdateBody();
        body.setFolder(folderId);
        body.setModule(INFOSTORE);
        body.setPassword("secret");
        body.setIncludeSubfolders(Boolean.TRUE);
        body.setExpiryDate(null);

        CommonResponse updateShareLink = smApi.updateShareLink(folderManager.getSession(), now(), body);
        assertNull(updateShareLink.getError(), updateShareLink.getErrorDesc());
    }

    /**
     * Adds an OX share as filestorage account
     *
     * @param shareLink The share link to add ass storage
     * @return The account ID
     * @throws ApiException
     */
    private String addOXShareAccount(String shareLink) throws ApiException {
        return addOXShareAccount(shareLink, null);
    }

    /**
     * Adds an OX share as filestorage account
     *
     * @param shareLink The share link to add ass storage
     * @return The account ID
     * @throws ApiException
     */
    private String addOXShareAccount(String shareLink, String password) throws ApiException {
        FederatedShareBody body = new FederatedShareBody();
        body.setPassword(password);

        AddShareResponse addShare = smApiC2.addShare(apiClientC2.getSession(), shareLink, FILESTORE_SERVICE, "Share from " + testUser.getLogin(), body);
        AddShareResponseData data = checkResponse(addShare.getError(), addShare.getErrorDesc(), addShare.getData());

        String accountId = data.getAccount();
        assertThat(accountId, not(nullValue()));
        addTearDownOperation(() -> deleteOXShareAccount(shareLink));

        analyze(shareLink, StateEnum.SUBSCRIBED);
        return accountId;
    }

    private void deleteOXShareAccount(String shareLink) throws Exception {
        CommonResponse response = smApiC2.deleteShare(apiClientC2.getSession(), shareLink, FILESTORE_SERVICE);
        checkResponse(response);
    }

    /**
     * Analysis the link and checks that the correct state is suggested
     *
     * @param shareLinkData The data
     * @param expectedState The expected state
     * @throws ApiException In case of error
     */
    private void analyze(ShareLinkData shareLinkData, StateEnum expectedState) throws ApiException {
        analyze(shareLinkData.getUrl(), expectedState);
    }

    /**
     * Analysis the link and checks that the correct state is suggested
     *
     * @param shareLink The link to analyze
     * @param expectedState The expected state
     * @throws ApiException In case of error
     */
    private void analyze(String shareLink, StateEnum expectedState) throws ApiException {
        ShareLinkAnalyzeResponse analyzeShareLink = smApiC2.analyzeShareLink(apiClientC2.getSession(), shareLink);
        checkResponse(analyzeShareLink.getError(), analyzeShareLink.getErrorDesc(), analyzeShareLink.getData());
        ShareLinkAnalyzeResponseData response = analyzeShareLink.getData();
        StateEnum state = response.getState();
        if (null == expectedState) {
            assertThat(state, nullValue());
        } else {
            assertThat(state, is(expectedState));
        }
        assertThat(response.getServiceId(), is(FILESTORE_SERVICE));
    }

    protected String receiveShareLink(ApiClient apiClient, String fromToMatch) throws Exception {
        return receiveShareLink(apiClient, fromToMatch, "shared the folder \"" + sharedFolderName + "\" with you");
    }

    /**
     * Receives the share link from the <code>X-Open-Xchange-Share-URL</code> header
     *
     * @param apiClient The client to use
     * @param fromToMatch The sender of the mail
     * @param subjectToMatch A part of the subject the mail must have
     * @return The share link
     * @throws Exception In case of error
     */
    protected String receiveShareLink(ApiClient apiClient, String fromToMatch, String subjectToMatch) throws Exception {
        MailData mail = receiveShareMail(apiClient, fromToMatch, subjectToMatch);
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
     * Receive the share mail from the inbox
     *
     * @param apiClient The {@link ApiClient} to use
     * @param fromToMatch The mail of the originator of the message
     * @param subjectToMatch The summary of the event
     * @return The mail as {@link MailData}
     * @throws Exception If the mail can't be found or something mismatches
     */
    protected MailData receiveShareMail(ApiClient apiClient, String fromToMatch, String subjectToMatch) throws Exception {
        for (int i = 0; i < 10; i++) {
            MailData mailData = lookupMail(apiClient, "default0%2FINBOX", fromToMatch, subjectToMatch);
            if (null != mailData) {
                return mailData;
            }
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
        }
        throw new AssertionError("No mail with " + subjectToMatch + " from " + fromToMatch + " received");
    }

    private MailData lookupMail(ApiClient apiClient, String folder, String fromToMatch, String subjectToMatch) throws Exception {
        MailApi mailApi = new MailApi(apiClient);
        MailsResponse mailsResponse = mailApi.getAllMails(apiClient.getSession(), folder, "600,601,607,610", null, null, null, "610", "desc", null, null, I(10), null);
        checkResponse(mailsResponse.getError(), mailsResponse.getErrorDesc(), mailsResponse.getData());
        for (List<String> mail : mailsResponse.getData()) {
            String subject = mail.get(2);
            if (Strings.isEmpty(subject) || false == subject.contains(subjectToMatch)) {
                continue;
            }
            MailResponse mailResponse = mailApi.getMail(apiClient.getSession(), mail.get(1), mail.get(0), null, null, "noimg", Boolean.FALSE, Boolean.TRUE, null, null, null, null, null, null, null);
            MailData mailData = checkResponse(mailResponse.getError(), mailsResponse.getErrorDesc(), mailResponse.getData());
            if (null == extractMatchingAddress(mailData.getFrom(), fromToMatch)) {
                continue;
            }
            rememberMail(mailApi, mailData);
            return mailData;
        }
        return null;
    }

    protected void rememberMail(MailApi mailApi, MailData data) {
        if (null == mailApi || null == data) {
            return;
        }
        MailListElement elm = new MailListElement();
        elm.setId(data.getId());
        elm.setFolder(data.getFolderId());
        addTearDownOperation(() -> {
            mailApi.deleteMails(mailApi.getApiClient().getSession(), Collections.singletonList(elm), now(), Boolean.TRUE, Boolean.FALSE);
        });
    }

    private static List<String> extractMatchingAddress(List<List<String>> addresses, String email) {
        if (null != addresses) {
            for (List<String> address : addresses) {
                assertEquals(2, address.size());
                if (null != address.get(1) && address.get(1).contains(email)) {
                    return address;
                }
            }
        }
        return null;
    }
}
