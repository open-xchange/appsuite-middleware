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

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.folder.manager.FolderApi;
import com.openexchange.ajax.folder.manager.FolderManager;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.passwordchange.actions.PasswordChangeUpdateRequest;
import com.openexchange.ajax.passwordchange.actions.PasswordChangeUpdateResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.test.pool.TestContext;
import com.openexchange.test.pool.TestContextPool;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.InfoItemUpdateResponse;
import com.openexchange.testing.httpclient.models.ShareLinkAnalyzeResponse;
import com.openexchange.testing.httpclient.models.ShareLinkAnalyzeResponseData.StateEnum;
import com.openexchange.testing.httpclient.models.ShareLinkData;
import com.openexchange.testing.httpclient.modules.InfostoreApi;
import com.openexchange.testing.httpclient.modules.ShareManagementApi;

/**
 * {@link ShareManagementSubscriptionTest} - Test for the <code>analyze</code> action of the share management module.
 * <p>
 * User 1 from context A will share the folder
 * User 2 from context B will analyze the share
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class ShareManagementSubscriptionTest extends AbstractShareManagementTest {

    @Override
    protected Map<String, String> getNeededConfigurations() {
        HashMap<String, String> configuration = new HashMap<String, String>();
        configuration.put("com.openexchange.capability.xctx", Boolean.TRUE.toString());
        configuration.put("com.openexchange.api.client.blacklistedHosts", "");
        return configuration;
    }

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
        addTearDownOperation(() -> logoutClient(apiClientC2, true));
        smApiC2 = new ShareManagementApi(apiClientC2);

        AJAXClient ajaxClientC2 = generateClient(testUserC2);
        addTearDownOperation(() -> ajaxClientC2.logout());

        sharedFolderName = this.getClass().getSimpleName() + UUID.randomUUID().toString();
        smApi = new ShareManagementApi(apiClient);
        folderManager = new FolderManager(new FolderApi(apiClient, testUser), "1");
        infostoreRoot = folderManager.findInfostoreRoot();

        setUpConfiguration();
        setUpConfiguration(ajaxClientC2);
    }

    @Override
    public void tearDown() throws Exception {
        try {
            if (null != context2) {
                TestContextPool.backContext(context2);
            }
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testMissingLink_APIException() throws Exception {
        ShareLinkAnalyzeResponse analyzeShareLink = smApiC2.analyzeShareLink(apiClientC2.getSession(), getBody(""));
        assertNull(analyzeShareLink.getData());
        assertNotNull(analyzeShareLink.getError(), analyzeShareLink.getErrorDesc());
        assertTrue(analyzeShareLink.getErrorDesc().equals("Missing the following request parameter: link"));
    }

    @Test
    public void testSomeLink_Unresovable() throws Exception {
        analyze("https://example.org/no/share/link", StateEnum.UNRESOLVABLE);
    }
    
    @Test
    public void testBrokenLink_Unresovable() throws Exception {
        analyze("https://example.org/ajax/share/aaaf78820506e0b2faf7883506ce41388f98fa02a4e314c9/1/8/MTk3Njk0", StateEnum.UNRESOLVABLE);
    }

    @Test
    public void testAnonymousLink_Forbidden() throws Exception {
        String folderId = createFolder();
        ShareLinkData shareLink = getOrCreateShareLink(folderManager, smApi, folderId);

        analyze(shareLink, StateEnum.FORBIDDEN);
    }

    @Test
    public void testSingleFile_Forbidden() throws Exception {
        String folderId = createFolder();

        String item = createFile(folderId, "file" + sharedFolderName);
        ShareLinkData shareLink = getOrCreateShareLink(folderManager, smApi, folderId, item);
        analyze(shareLink, StateEnum.FORBIDDEN);
    }

    @Test
    public void testAnonymousLinkWithPassword_Forbidden() throws Exception {
        String folderId = createFolder();
        ShareLinkData shareLink = getOrCreateShareLink(folderManager, smApi, folderId);

        analyze(shareLink, StateEnum.FORBIDDEN);

        updateLinkWithPassword(folderManager, smApi, folderId);
        analyze(smApiC2, getOrCreateShareLink(folderManager, smApi, folderId), StateEnum.FORBIDDEN);
    }

    @Test
    public void testDeletedAnonymousLink_Unresolvable() throws Exception {
        String folderId = createFolder();
        ShareLinkData shareLink = getOrCreateShareLink(folderManager, smApi, folderId);
        deleteShareLink(folderManager, smApi, folderId);

        analyze(shareLink, StateEnum.UNRESOLVABLE);
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
        updatedPermissions.add(prepareGuest(testUserC2));

        folderId = setFolderPermission(folderId, updatedPermissions);

        /*
         * Receive mail as guest and extract share link
         */
        String shareLink = receiveShareLink(apiClientC2, testUser.getLogin());
        analyze(shareLink, StateEnum.ADDABLE);

        /*
         * Add share and verify analyze changed
         */
        addOXShareAccount(smApiC2, shareLink, null);

        /*
         * Remove guest from folder permission
         */
        folderId = setFolderPermission(folderId, originalPermissions);
        analyze(shareLink, StateEnum.REMOVED);

        /*
         * Re-add guest, account should still exist
         */
        folderId = setFolderPermission(folderId, updatedPermissions);
        analyze(shareLink, StateEnum.SUBSCRIBED);

        /*
         * Delete account and check again
         */
        deleteOXShareAccount(smApiC2, shareLink);
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
        updatedPermissions.add(prepareGuest(testUserC2));

        folderId = setFolderPermission(folderId, updatedPermissions);

        /*
         * Receive mail as guest and extract share link
         */
        String shareLink = receiveShareLink(apiClientC2, testUser.getLogin());
        analyze(shareLink, StateEnum.ADDABLE);

        /*
         * Add share and verify analyze changed
         */
        addOXShareAccount(smApiC2, shareLink, null);

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
        smApiC2.remount(apiClientC2.getSession(), getExtendedBody(shareLink, password, null));
        analyze(shareLink, StateEnum.SUBSCRIBED);

        /*
         * Delete guest from share and verify analyze changed
         */
        folderId = setFolderPermission(folderId, originalPermissions);
        analyze(shareLink, StateEnum.REMOVED);

        /*
         * Re-add guest, account should still exist and session of the guest still be valid
         */
        folderId = setFolderPermission(folderId, updatedPermissions);
        analyze(shareLink, StateEnum.SUBSCRIBED);

        /*
         * Delete account and check again
         */
        deleteOXShareAccount(smApiC2, shareLink);
        analyze(shareLink, StateEnum.ADDABLE_WITH_PASSWORD);
    }

    private void analyze(ShareLinkData shareLink, StateEnum e) throws ApiException {
        analyze(smApiC2, shareLink, e);
    }

    private void analyze(String shareLink, StateEnum e) throws ApiException {
        analyze(smApiC2, shareLink, e);
    }

    /**
     * 
     * Creates a file with the given file name, owner and shared user.
     *
     * @param folderId The ID of the folder to put the file in
     * @param fileName The name of the file.
     * @return An entry with the object id of the file.
     * @throws ApiException
     */
    private String createFile(String folderId, String fileName) throws ApiException {
        InfoItemUpdateResponse uploadResponse = new InfostoreApi(apiClient).uploadInfoItem(folderManager.getSession(), folderId, fileName, new byte[] { 34, 45, 35, 23 }, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertNotNull(uploadResponse);
        assertNull(uploadResponse.getErrorDesc(), uploadResponse.getError());
        return uploadResponse.getData();
    }
}
