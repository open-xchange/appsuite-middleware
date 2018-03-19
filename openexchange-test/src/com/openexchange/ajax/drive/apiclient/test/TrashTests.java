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

package com.openexchange.ajax.drive.apiclient.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import org.junit.Test;
import com.openexchange.ajax.folder.manager.FolderApi;
import com.openexchange.ajax.folder.manager.FolderManager;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.DriveActionsResponse;
import com.openexchange.testing.httpclient.models.FoldersResponse;
import com.openexchange.testing.httpclient.models.TrashContent;
import com.openexchange.testing.httpclient.models.TrashFolderResponse;
import com.openexchange.testing.httpclient.models.TrashTargetsBody;
import com.openexchange.testing.httpclient.modules.DriveApi;
import jonelo.jacksum.algorithm.MD;

/**
 * {@link TrashTests}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class TrashTests extends AbstractAPIClientSession {

    private DriveApi driveApi;
    private ApiClient client;
    private FolderApi folderApi;
    private FolderManager folderManager;
    private String rootId;
    private Integer infostoreFolder;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = getApiClient();
        folderApi = new FolderApi(client, testUser);
        infostoreFolder = folderApi.getInfostoreFolder();
        folderManager = new FolderManager(folderApi, "0");

        FoldersResponse rootFolders = folderApi.getFoldersApi().getRootFolders(folderApi.getSession(), "1,319", null, "infostore");
        rootId = getFolderId("Infostore", rootFolders);
        FoldersResponse subFolders = folderApi.getFoldersApi().getSubFolders(folderApi.getSession(), rootId, "1,319", 1, "0", "infostore", false);
        String trashId = getFolderId("Trash", subFolders);
        driveApi = new DriveApi(client);
        driveApi.emptyTrash(folderApi.getSession(), rootId);
        byte[] body = new byte[4];
        DriveActionsResponse uploadFile = driveApi.uploadFile(folderApi.getSession(), trashId, "/", "test.txt", getChecksum(body), body, null, null, 1, "text/plain", 0l, new Long(4), null, null, null, null, null);
        assertNull(uploadFile.getErrorDesc(), uploadFile.getError());

        folderManager.createFolder(trashId, "trashedFolder", "infostore");
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        driveApi.emptyTrash(folderApi.getSession(), rootId);
    }

    private String getFolderId(String name, FoldersResponse folders) {
        assertNull(folders.getError());
        Object data = folders.getData();
        @SuppressWarnings("unchecked") ArrayList<ArrayList<Object>> rootFolderArray = (ArrayList<ArrayList<Object>>) data;
        String Id = null;
        for(ArrayList<Object> obj : rootFolderArray) {
            if(obj.get(1).equals(name)) {
                Id = (String) obj.get(0);
                break;
            }
        }
        assertNotNull(Id);
        return Id;
    }

    @Test
    public void testTrashContent() throws ApiException {
        TrashFolderResponse trashContent = driveApi.getTrashContent(folderApi.getSession(), String.valueOf(infostoreFolder));
        assertNull(trashContent.getError());
        assertNotNull(trashContent.getData());
        TrashContent data = trashContent.getData();
        assertNotNull(data.getFiles());
        assertFalse(data.getFiles().isEmpty());
        assertEquals(1, data.getFiles().size());
        assertNotNull(data.getFolders());
        assertFalse(data.getFolders().isEmpty());
        assertEquals(1, data.getFolders().size());
    }

    @Test
    public void testDeleteFromTrash() throws ApiException {
        TrashFolderResponse trashContent = driveApi.getTrashContent(folderApi.getSession(), String.valueOf(infostoreFolder));
        TrashContent data = trashContent.getData();

        TrashTargetsBody body = new TrashTargetsBody();
        body.addFilesItem(data.getFiles().get(0).getName());
        TrashFolderResponse removeFromTrash = driveApi.deleteFromTrash(folderApi.getSession(), String.valueOf(infostoreFolder), body);
        assertNull(removeFromTrash.getErrorDesc(), removeFromTrash.getError());

        trashContent = driveApi.getTrashContent(folderApi.getSession(), "/");
        assertNull(trashContent.getError());
        assertNotNull(trashContent.getData());
        data = trashContent.getData();
        assertNotNull(data.getFiles());
        assertTrue(data.getFiles().isEmpty());
        assertNotNull(data.getFolders());
        assertFalse(data.getFolders().isEmpty());
        assertEquals(1, data.getFolders().size());
    }

    @Test
    public void testRestoreFromTrash() throws ApiException {
        TrashFolderResponse trashContent = driveApi.getTrashContent(folderApi.getSession(), String.valueOf(infostoreFolder));
        TrashContent data = trashContent.getData();

        TrashTargetsBody body = new TrashTargetsBody();
        body.addFilesItem(data.getFiles().get(0).getName());
        TrashFolderResponse restoredFromTrash = driveApi.restoreFromTrash(folderApi.getSession(), String.valueOf(infostoreFolder), body);
        assertNull(restoredFromTrash.getErrorDesc(), restoredFromTrash.getError());
        assertNotNull(restoredFromTrash.getData());
        TrashContent restoreData = restoredFromTrash.getData();
        assertNotNull(restoreData.getFiles());
        assertEquals(1, restoreData.getFiles().size());
        assertTrue(restoreData.getFiles().get(0).getPath().isEmpty()); // Restored to root

        trashContent = driveApi.getTrashContent(folderApi.getSession(), String.valueOf(infostoreFolder));
        assertNull(trashContent.getError());
        assertNotNull(trashContent.getData());
        data = trashContent.getData();
        assertNotNull(data.getFiles());
        assertTrue(data.getFiles().isEmpty());
        assertNotNull(data.getFolders());
        assertFalse(data.getFolders().isEmpty());
        assertEquals(1, data.getFolders().size());
    }

    protected String getChecksum(byte[] bytes) throws Exception {
        MD md5 = new MD("MD5");
        md5.update(bytes);
        return md5.getFormattedValue();
    }

}
