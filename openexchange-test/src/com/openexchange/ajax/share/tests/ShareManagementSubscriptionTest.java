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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.openexchange.ajax.folder.manager.FolderApi;
import com.openexchange.ajax.folder.manager.FolderManager;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.passwordchange.actions.PasswordChangeUpdateRequest;
import com.openexchange.ajax.passwordchange.actions.PasswordChangeUpdateResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.groupware.modules.Module;
import com.openexchange.test.pool.TestContext;
import com.openexchange.test.pool.TestContextPool;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.ShareLinkAnalyzeResponse;
import com.openexchange.testing.httpclient.models.ShareLinkAnalyzeResponseData.StateEnum;
import com.openexchange.testing.httpclient.models.ShareLinkData;
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
@RunWith(Parameterized.class)
public class ShareManagementSubscriptionTest extends AbstractShareManagementTest {

    //@formatter:off
    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "filestorage_xox", "xox" + Module.INFOSTORE.getFolderConstant() },
            { "filestorage_xctx", "xctx" + Module.INFOSTORE.getFolderConstant() },
        });
    }
    //@formatter:on

    private final String capability;
    private final String filtestorage;

    /**
     * Initializes a new {@link ShareManagementSubscriptionTest}.
     * 
     * @param capability The capability to set
     * @param filtestorage The provider ID to match in analyze
     */
    public ShareManagementSubscriptionTest(String capability, String filtestorage) {
        super();
        this.capability = capability;
        this.filtestorage = filtestorage;
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        HashMap<String, String> configuration = new HashMap<String, String>();
        configuration.put("com.openexchange.capability." + capability, Boolean.TRUE.toString());
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
    public void testSomeLink_APIException() throws Exception {
        ShareLinkAnalyzeResponse analyzeShareLink = smApiC2.analyzeShareLink(apiClientC2.getSession(), getBody("https://example.org/no/share/link"));
        assertNull(analyzeShareLink.getData());
        assertNotNull(analyzeShareLink.getError(), analyzeShareLink.getErrorDesc());
    }

    @Test
    public void testAnonymousLink_Addable() throws Exception {
        String folderId = createFolder();
        ShareLinkData shareLink = getOrCreateShareLink(folderManager, smApi, folderId);

        analyze(shareLink, StateEnum.ADDABLE);
    }

    @Test
    public void testAnonymousLinkWithPassword_Addable() throws Exception {
        String folderId = createFolder();
        ShareLinkData shareLink = getOrCreateShareLink(folderManager, smApi, folderId);

        analyze(shareLink, StateEnum.ADDABLE);

        updateLinkWithPassword(folderManager, smApi, folderId);
        analyze(smApiC2, getOrCreateShareLink(folderManager, smApi, folderId), StateEnum.ADDABLE_WITH_PASSWORD, filtestorage);
    }

    @Test
    public void testAnonymousLink_Inaccessible() throws Exception {
        String folderId = createFolder();
        ShareLinkData shareLink = getOrCreateShareLink(folderManager, smApi, folderId);
        deleteShareLink(folderManager, smApi, folderId);

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
        addOXShareAccount(smApiC2, shareLink, null, filtestorage);

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
        addOXShareAccount(smApiC2, shareLink, null, filtestorage);

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
        analyze(shareLink, StateEnum.INACCESSIBLE);

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
        analyze(smApiC2, shareLink, e, filtestorage);
    }

    private void analyze(String shareLink, StateEnum e) throws ApiException {
        analyze(smApiC2, shareLink, e, filtestorage);
    }
}
