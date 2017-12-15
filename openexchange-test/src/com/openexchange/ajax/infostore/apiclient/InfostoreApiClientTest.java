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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.ajax.infostore.apiclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;
import org.junit.Before;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.junit.Assert;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ConfigProperty;
import com.openexchange.testing.httpclient.models.ConfigPropertyResponse;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.InfoItemData;
import com.openexchange.testing.httpclient.models.InfoItemResponse;
import com.openexchange.testing.httpclient.models.InfoItemUpdateResponse;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.modules.ConfigApi;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.InfostoreApi;
import com.openexchange.tools.io.IOTools;

public class InfostoreApiClientTest extends AbstractAPIClientSession {

    protected static final int[] virtualFolders = { FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID, FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID };

    public static final String INFOSTORE_FOLDER = "infostore.folder";

    protected String folderId;

    protected String hostName = null;

    private String privateInfostoreFolder;

    protected InfostoreApi infostoreApi;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        getClient().login(testUser.getLogin(), testUser.getPassword());
        this.folderId = createFolderForTest();
        infostoreApi = new InfostoreApi(getClient());

        //        java.io.File upload = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));
        //        File file1 = InfostoreTestManager.createFile(folderId, "test knowledge", "text/plain");
        //        file1.setDescription("test knowledge description");
        //        itm.newAction(file1, upload);
        //
        //        File file2 = InfostoreTestManager.createFile(folderId, "test url", "text/plain");
        //        file2.setURL("http://www.open-xchange.com");
        //        file2.setDescription("test url description");
        //        itm.newAction(file2, upload);
    }

    protected String uploadInfoItem(File file, String mimeType) throws ApiException, FileNotFoundException, IOException {
        byte[] bytes = IOTools.getBytes(new FileInputStream(file));
        InfoItemUpdateResponse uploadInfoItem = infostoreApi.uploadInfoItem(getClient().getSession(), folderId, bytes, null, file.getName(), file.getName(), mimeType, null, file.getName(), null, null, null, null, null, String.valueOf(bytes.length), false, false, null);
        Assert.assertNull(uploadInfoItem.getError());
        Assert.assertNotNull(uploadInfoItem.getData());
        return uploadInfoItem.getData();
    }

    protected InfoItemData getItem(String id) throws ApiException {
        InfoItemResponse infoItem = infostoreApi.getInfoItem(getClient().getSession(), id, folderId);
        Assert.assertNull(infoItem.getError());
        Assert.assertNotNull(infoItem.getData());
        return infoItem.getData();
    }

    private String createFolderForTest() throws ApiException {
        final String parent = getPrivateInfostoreFolder();
        FoldersApi folderApi = new FoldersApi(getClient());
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setModule(String.valueOf(Module.INFOSTORE.getFolderConstant()));
        folder.setSummary("NewInfostoreFolder" + UUID.randomUUID().toString());
        folder.setTitle(folder.getSummary());
        folder.setType(FolderObject.PUBLIC);
        body.setFolder(folder);
        FolderUpdateResponse folderUpdateResponse = folderApi.createFolder(parent, getClient().getSession(), body, "0", null);
        return checkResponse(folderUpdateResponse);
    }

    public String getPrivateInfostoreFolder() throws ApiException {
        if (null == privateInfostoreFolder) {
            ConfigApi configApi = new ConfigApi(getClient());
            ConfigPropertyResponse configPropertyResponse = configApi.getConfigProperty(getClient().getSession(), Tree.PrivateInfostoreFolder.getPath());
            ConfigProperty property = checkResponse(configPropertyResponse);
            privateInfostoreFolder = (String) property.getValue();

        }
        return privateInfostoreFolder;
    }

    private ConfigProperty checkResponse(ConfigPropertyResponse resp) {
        Assert.assertNull(resp.getError());
        Assert.assertNotNull(resp.getData());
        return resp.getData();
    }


    private String checkResponse(FolderUpdateResponse resp) {
        Assert.assertNull(resp.getError());
        Assert.assertNotNull(resp.getData());
        return resp.getData();
    }

}
