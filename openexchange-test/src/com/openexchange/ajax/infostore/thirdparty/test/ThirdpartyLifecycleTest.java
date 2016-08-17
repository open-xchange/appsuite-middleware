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

package com.openexchange.ajax.infostore.thirdparty.test;

import java.util.List;
import com.openexchange.ajax.infostore.thirdparty.AbstractInfostoreThirdpartyTest;
import com.openexchange.ajax.infostore.thirdparty.ProviderIdMapper;
import com.openexchange.ajax.infostore.thirdparty.actions.CreateFolderRequest;
import com.openexchange.ajax.infostore.thirdparty.actions.CreateFolderResponse;
import com.openexchange.ajax.infostore.thirdparty.actions.DeleteFileRequest;
import com.openexchange.ajax.infostore.thirdparty.actions.DeleteFileResponse;
import com.openexchange.ajax.infostore.thirdparty.actions.DeleteFolderRequest;
import com.openexchange.ajax.infostore.thirdparty.actions.DeleteFolderResponse;
import com.openexchange.ajax.infostore.thirdparty.actions.NewFileRequest;
import com.openexchange.ajax.infostore.thirdparty.actions.NewFileResponse;
import com.openexchange.java.Strings;

/**
 * {@link ThirdpartyLifecycleTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class ThirdpartyLifecycleTest extends AbstractInfostoreThirdpartyTest {

    private List<ProviderIdMapper> filestorages;

    /**
     * Initializes a new {@link ThirdpartyLifecycleTest}.
     *
     * @param name
     */
    public ThirdpartyLifecycleTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        filestorages = getConnectedInfostoreId();
    }

    @Override
    protected void tearDown() throws Exception {
        //TODO delete files here...
        super.tearDown();
    }

    public void testLifecycle() throws Exception {
        String folderName = "unittest";
        byte[] file = randomBytes(5);

        for(ProviderIdMapper pid : filestorages) {
            String folderId = createFolder(pid, folderName);
            String fileId = uploadFile(folderId, file);
            deleteFile(folderId, fileId);
            deleteFolder(folderId);
        }
    }

    public String createFolder(ProviderIdMapper filestore, String folderName) throws Exception {
        CreateFolderRequest cfReq = new CreateFolderRequest(filestore.getInfostoreId(), folderName);
        CreateFolderResponse cfResp = client.execute(cfReq);
        assertNotNull("Folder was not successfully created: ", cfResp);
        String folderId = (String) cfResp.getData();
        return folderId;
    }


    public String uploadFile(String folderId, byte[] bytesToUpload) throws Exception {
        NewFileRequest nfReq = new NewFileRequest(bytesToUpload, setFolderId(folderId), "application/octet-stream");
        NewFileResponse nfResp = client.execute(nfReq);
        assertNotNull("File was not successfully uploaded");
        String fileId = (String) nfResp.getData();
        assertFalse("File id is empty", Strings.isEmpty(fileId));
        return fileId;
    }

    public void deleteFile(String folderId, String fileId) throws Exception {
        DeleteFileRequest dfReq = new DeleteFileRequest(fileId, folderId);
        DeleteFileResponse dfResp = client.execute(dfReq);
        assertNotNull(dfResp);
    }

    public void deleteFolder(String folderID) throws Exception {
        DeleteFolderRequest dfReq = new DeleteFolderRequest(folderID, 1);
        DeleteFolderResponse dfResp = client.execute(dfReq);
        assertNotNull(dfResp);
    }
}
