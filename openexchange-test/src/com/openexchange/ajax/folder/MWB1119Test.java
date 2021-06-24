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

package com.openexchange.ajax.folder;

import static org.junit.Assert.assertNotNull;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import com.openexchange.ajax.folder.manager.FolderManager;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.groupware.modules.Module;
import com.openexchange.testing.httpclient.models.CommonResponse;
import com.openexchange.testing.httpclient.models.ShareLinkData;
import com.openexchange.testing.httpclient.models.ShareLinkResponse;
import com.openexchange.testing.httpclient.models.ShareLinkUpdateBody;
import com.openexchange.testing.httpclient.models.ShareTargetData;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.ShareManagementApi;

/**
 * {@link MWB1119Test}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Schuerholz</a>
 * @since v7.10.6
 */
public class MWB1119Test extends AbstractConfigAwareAPIClientSession {

    private ShareManagementApi shareApi;
    private FolderManager folderManager;
    private String testFolder;

    public MWB1119Test() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        shareApi = new ShareManagementApi(getApiClient());
        folderManager = new FolderManager(new FoldersApi(getApiClient()), "0");
        testFolder = folderManager.createFolder(folderManager.findInfostoreRoot(), "MWB1119TestFolder", FolderManager.INFOSTORE);

    }

    @Override
    public void tearDown() throws Exception {
        folderManager.cleanUp();
        super.tearDown();
    }

    @Override
    protected String getScope() {
        return "user";
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        Map<String, String> configs = new HashMap<>();
        configs.put("com.openexchange.folderstorage.permissions.moveToPublic", "inherit");
        configs.put("com.openexchange.folderstorage.permissions.moveToShared", "inherit");
        configs.put("com.openexchange.folderstorage.permissions.moveToPrivate", "inherit");
        return configs;
    }

    /**
     * 
     * Tests if it is possible to move a unshared folder to a folder that has an anonymous share link permission
     * of type FolderPermissionType.NORMAL.
     * 
     * Test case for bug fix of MWB-1119.
     *
     * @throws Exception
     */
    @Test
    public void testMoveInAnonymousShareLinkFolderTypeNormal() throws Exception {
        // creates a parent folder
        String parentFolderId = folderManager.createFolder(testFolder, "parentFolder", FolderManager.INFOSTORE);

        // adds a anonymous share link to the folder
        ShareTargetData shareTargetData = new ShareTargetData();
        shareTargetData.setFolder(parentFolderId);
        shareTargetData.setModule(Module.INFOSTORE.getName());
        ShareLinkResponse shareLinkResponse = shareApi.getShareLink(shareTargetData);
        ShareLinkData linkData = checkResponse(shareLinkResponse.getError(), shareLinkResponse.getErrorDesc(), shareLinkResponse.getData());
        assertNotNull("Precondition failed: No link for the parent folder created.", linkData);
        assertNotNull("Precondition failed: No link for the parent folder created.", linkData.getUrl());

        // changes the folder permission type of the link permission to NORMAL
        ShareLinkUpdateBody shareLinkUpdateBody = new ShareLinkUpdateBody();
        shareLinkUpdateBody.setFolder(parentFolderId);
        shareLinkUpdateBody.setIncludeSubfolders(Boolean.FALSE);
        shareLinkUpdateBody.setModule(FolderManager.INFOSTORE);
        CommonResponse updateShareLinkResponse = shareApi.updateShareLink(shareLinkResponse.getTimestamp(), shareLinkUpdateBody);
        checkResponse(updateShareLinkResponse.getError(), updateShareLinkResponse.getErrorDesc());

        // creates a unshared folder to move
        String folderToMove = folderManager.createFolder(testFolder, "folderToMove", FolderManager.INFOSTORE);

        // moves the folder to the new parent, error occurs if this fails
        folderManager.moveFolder(folderToMove, parentFolderId);
    }


}
