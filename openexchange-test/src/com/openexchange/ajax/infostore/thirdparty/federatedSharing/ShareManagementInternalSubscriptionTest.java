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
