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

import static com.openexchange.ajax.infostore.thirdparty.federatedSharing.FederatedSharingUtil.prepareUser;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.util.ArrayList;
import org.junit.Test;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.testing.httpclient.models.ExtendedSubscribeShareBody;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.ShareLinkAnalyzeResponseData.StateEnum;
import com.openexchange.testing.httpclient.models.SubscribeShareResponse;
import com.openexchange.testing.httpclient.modules.ShareManagementApi;

/**
 * {@link ShareManagementInternalSubscriptionTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class ShareManagementInternalSubscriptionTest extends AbstractShareManagementTest {

    private ShareManagementApi smApi2;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        smApi2 = new ShareManagementApi(testUser2.getApiClient());
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createApiClient().withUserPerContext(2).build();
    }

    @Test
    public void testInternalLink_WithItem() throws Exception {
        String shareLink = "http://localhost/appsuite/ui!&app=io.ox/files&folder=9001&id=9001";
        analyze(smApi2, shareLink, StateEnum.UNSUPPORTED);
    }

    @Test
    public void testInternalLink_UnkownResource() throws Exception {
        String shareLink = "https://localhost/appsuite/ui#!&app=io.ox/files&folder=999999";
        analyze(smApi2, shareLink, StateEnum.INACCESSIBLE);
    }

    @Test
    public void testInternalLink_Subscribed() throws Exception {
        String folderId = createFolder();

        /*
         * Add another user to the folder
         */
        FolderData folder = folderManager.getFolder(folderId);
        ArrayList<FolderPermission> originalPermissions = new ArrayList<>(folder.getPermissions());
        ArrayList<FolderPermission> updatedPermissions = new ArrayList<>(folder.getPermissions());
        updatedPermissions.add(prepareUser(testUser2, testUser2.getApiClient().getUserId()));

        folderId = setFolderPermission(folderId, updatedPermissions);

        /*
         * Receive mail as user and extract share link
         */
        String shareLink = receiveShareLink(testUser2.getApiClient(), testUser.getLogin());
        analyze(smApi2, shareLink, StateEnum.SUBSCRIBED);

        /*
         * Remove user from folder permission
         */
        folderId = setFolderPermission(folderId, originalPermissions);
        analyze(smApi2, shareLink, StateEnum.INACCESSIBLE);

        /*
         * Check that share can't be subscribed
         */
        ExtendedSubscribeShareBody body = getExtendedBody(shareLink, null, "Share from " + testUser.getLogin());
        SubscribeShareResponse subscribeShareResponse = smApi2.subscribeShare(smApi2.getApiClient().getSession(), body);
        assertThat(subscribeShareResponse.getErrorDesc(), is(notNullValue()));
    }

}
