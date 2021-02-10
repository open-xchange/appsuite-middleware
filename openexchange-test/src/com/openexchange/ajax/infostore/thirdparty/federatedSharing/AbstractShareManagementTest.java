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

import static com.openexchange.ajax.folder.manager.FolderManager.INFOSTORE;
import static com.openexchange.java.Autoboxing.L;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.UUID;
import com.openexchange.ajax.chronos.AbstractEnhancedApiClientSession;
import com.openexchange.ajax.folder.manager.FolderApi;
import com.openexchange.ajax.folder.manager.FolderManager;
import com.openexchange.groupware.modules.Module;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CommonResponse;
import com.openexchange.testing.httpclient.models.ExtendedSubscribeShareBody;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.ShareLinkAnalyzeResponse;
import com.openexchange.testing.httpclient.models.ShareLinkAnalyzeResponseData;
import com.openexchange.testing.httpclient.models.ShareLinkAnalyzeResponseData.StateEnum;
import com.openexchange.testing.httpclient.models.ShareLinkData;
import com.openexchange.testing.httpclient.models.ShareLinkResponse;
import com.openexchange.testing.httpclient.models.ShareLinkUpdateBody;
import com.openexchange.testing.httpclient.models.ShareTargetData;
import com.openexchange.testing.httpclient.models.SubscribeShareBody;
import com.openexchange.testing.httpclient.models.SubscribeShareResponse;
import com.openexchange.testing.httpclient.models.SubscribeShareResponseData;
import com.openexchange.testing.httpclient.modules.ShareManagementApi;

/**
 * {@link AbstractShareManagementTest} - Test for the <code>analyze</code> action of the share management module.
 * <p>
 * User 1 from context A will share the folder
 * User 2 from context B will analyze the share
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class AbstractShareManagementTest extends AbstractEnhancedApiClientSession {

    /* Context 1 */
    protected String sharedFolderName;
    protected ShareManagementApi smApi;
    protected FolderManager folderManager;
    protected String infostoreRoot;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        sharedFolderName = this.getClass().getSimpleName() + UUID.randomUUID().toString();
        smApi = new ShareManagementApi(apiClient);
        folderManager = new FolderManager(new FolderApi(apiClient, testUser), "1");
        remember(folderManager);
        infostoreRoot = folderManager.findInfostoreRoot();
    }

    /*
     * ------------------- HELPERS -------------------
     */

    protected final static EnumSet<StateEnum> SUCCESS = EnumSet.of(StateEnum.ADDABLE, StateEnum.ADDABLE_WITH_PASSWORD, StateEnum.SUBSCRIBED);

    protected String createFolder() throws ApiException {
        return folderManager.createFolder(infostoreRoot, sharedFolderName, INFOSTORE);
    }

    protected static Long now() {
        return L(System.currentTimeMillis());
    }

    /**
     * Updates the folder with the given permissions
     *
     * @param folderId The folder to update
     * @param permissions Permissions to set
     * @return The new folder ID
     * @throws ApiException In case of error
     */
    protected String setFolderPermission(String folderId, ArrayList<FolderPermission> permissions) throws ApiException {
        FolderData deltaFolder = new FolderData();
        deltaFolder.setPermissions(permissions);
        return folderManager.updateFolder(folderId, deltaFolder, "A test share for you");
    }

    protected static SubscribeShareBody getBody(String link) {
        SubscribeShareBody body = new SubscribeShareBody();
        body.setLink(link);
        return body;
    }

    protected static ExtendedSubscribeShareBody getExtendedBody(String shareLink, String password, String displayName) {
        ExtendedSubscribeShareBody body = new ExtendedSubscribeShareBody();
        body.setLink(shareLink);
        body.setPassword(password);
        body.setName(displayName);
        return body;
    }

    /**
     * Creates a new share link for the given folder
     *
     * @param folderManager The folder manger to adjust the timestamp in after the link was created
     * @param smApi The API to use
     * @param folder The folder to create a share link for
     * @return The guest id of for the new link
     * @throws ApiException
     */
    protected static ShareLinkData getOrCreateShareLink(FolderManager folderManager, ShareManagementApi smApi, String folder) throws ApiException {
        return getOrCreateShareLink(folderManager, smApi, folder, null);
    }

    /**
     * Creates a new share link for the given folder
     *
     * @param folderManager The folder manger to adjust the timestamp in after the link was created
     * @param smApi The API to use
     * @param folder The folder to create a share link for
     * @param item The item id
     * @return The guest id of for the new link
     * @throws ApiException
     */
    protected static ShareLinkData getOrCreateShareLink(FolderManager folderManager, ShareManagementApi smApi, String folder, String item) throws ApiException {
        ShareTargetData data = new ShareTargetData();
        data.setItem(item);
        data.setFolder(folder);
        data.setModule(INFOSTORE);
        ShareLinkResponse shareLink = smApi.getShareLink(data);
        checkResponse(shareLink.getError(), shareLink.getErrorDesc(), shareLink.getData());
        folderManager.setLastTimestamp(shareLink.getTimestamp());
        return shareLink.getData();
    }

    /**
     * Deletes a share link
     *
     * @param smApi The API to use
     * @param folderId The folder ID to remove the link from
     * @throws ApiException
     */
    protected static void deleteShareLink(FolderManager folderManager, ShareManagementApi smApi, String folderId) throws ApiException {
        ShareTargetData shareTargetData = new ShareTargetData();
        shareTargetData.setFolder(folderId);
        shareTargetData.setModule(INFOSTORE);
        CommonResponse deleteShareLink = smApi.deleteShareLink(now(), shareTargetData);
        assertNull(deleteShareLink.getError(), deleteShareLink.getErrorDesc());
        folderManager.setLastTimestamp(deleteShareLink.getTimestamp());
    }

    /**
     * Updates the share for the given folder with a password
     *
     * @param folderId The folder the share is on
     * @throws ApiException On error
     */
    protected static void updateLinkWithPassword(FolderManager folderManager, ShareManagementApi smApi, String folderId) throws ApiException {
        ShareLinkUpdateBody body = new ShareLinkUpdateBody();
        body.setFolder(folderId);
        body.setModule(INFOSTORE);
        body.setPassword("secret");
        body.setIncludeSubfolders(Boolean.TRUE);
        body.setExpiryDate(null);

        CommonResponse updateShareLink = smApi.updateShareLink(now(), body);
        assertNull(updateShareLink.getError(), updateShareLink.getErrorDesc());
        folderManager.setLastTimestamp(updateShareLink.getTimestamp());
    }

    /**
     * Adds an OX share as filestorage account
     *
     * @param smApi The API to use
     * @param shareLink The share link to add ass storage
     * @param password The optional password to set
     * @return The folder ID
     * @throws ApiException
     */
    protected SubscribeShareResponseData addOXShareAccount(ShareManagementApi smApi, String shareLink, String password) throws ApiException {
        ExtendedSubscribeShareBody body = getExtendedBody(shareLink, password, "Share from " + testUser.getLogin());
        SubscribeShareResponse subscribeResponse = smApi.subscribeShare(smApi.getApiClient().getSession(), body);

        SubscribeShareResponseData data = checkResponse(subscribeResponse.getError(), subscribeResponse.getErrorDesc(), subscribeResponse.getData());

        assertThat(data.getAccount(), notNullValue());
        assertThat(data.getModule(), is(Module.INFOSTORE.getName()));
        assertThat(data.getFolder(), notNullValue());

        analyze(smApi, shareLink, StateEnum.SUBSCRIBED);
        return data;
    }

    /**
     * Analysis the link and checks that the correct state is suggested
     *
     * @param smApi The API to use
     * @param shareLinkData The data
     * @param expectedState The expected state
     * @return The response
     * @throws ApiException In case of error
     */
    protected static ShareLinkAnalyzeResponseData analyze(ShareManagementApi smApi, ShareLinkData shareLinkData, StateEnum expectedState) throws ApiException {
        return analyze(smApi, shareLinkData.getUrl(), expectedState);
    }

    /**
     * Analysis the link and checks that the correct state is suggested
     *
     * @param smApi The API to use
     * @param shareLink The link to analyze
     * @param expectedState The expected state
     * @return The response
     * @throws ApiException In case of error
     */
    protected static ShareLinkAnalyzeResponseData analyze(ShareManagementApi smApi, String shareLink, StateEnum expectedState) throws ApiException {
        ShareLinkAnalyzeResponse analyzeShareLink = smApi.analyzeShareLink(smApi.getApiClient().getSession(), getBody(shareLink));
        checkResponse(analyzeShareLink.getError(), analyzeShareLink.getErrorDesc(), analyzeShareLink.getData());
        ShareLinkAnalyzeResponseData response = analyzeShareLink.getData();
        StateEnum state = response.getState();
        if (null == expectedState) {
            assertThat(state, nullValue());
        } else {
            assertThat(state, is(expectedState));
        }

        if (false == SUCCESS.contains(state)) {
            assertThat("Expected a detailed error message about the state", response.getError(), notNullValue());
        } else if (StateEnum.SUBSCRIBED.equals(expectedState)) {
            assertThat(response.getAccount(), notNullValue());
            assertThat(response.getModule(), is(String.valueOf(Module.INFOSTORE.getName())));
            assertThat(response.getFolder(), notNullValue());
        }

        return response;
    }

    protected String receiveShareLink(ApiClient apiClient, String fromToMatch) throws Exception {
        return FederatedSharingUtil.receiveShareLink(apiClient, fromToMatch, sharedFolderName);
    }

}
