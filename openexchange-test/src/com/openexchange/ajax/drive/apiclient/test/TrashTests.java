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

package com.openexchange.ajax.drive.apiclient.test;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
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
import com.openexchange.testing.httpclient.models.DriveUploadResponse;
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
    private String infostoreFolder;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = getApiClient();
        folderApi = new FolderApi(client, testUser);
        infostoreFolder = folderApi.getInfostoreFolder();
        folderManager = new FolderManager(folderApi, "0");

        FoldersResponse rootFolders = folderApi.getFoldersApi().getRootFolders("1,319", null, "infostore");
        rootId = getFolderId("Infostore", rootFolders);
        FoldersResponse subFolders = folderApi.getFoldersApi().getSubFolders(rootId, "1,319", I(1), "0", "infostore", null, Boolean.FALSE);
        String trashId = getFolderId("Trash", subFolders);
        driveApi = new DriveApi(client);
        driveApi.emptyTrash(client.getSession(), rootId);
        byte[] body = new byte[4];
        DriveUploadResponse uploadFile = driveApi.uploadFile(client.getSession(), trashId, "/", "test.txt", getChecksum(body), body, I(1), null, null, "text/plain", L(0), new Long(4), null, null, null, null, null);
        assertNull(uploadFile.getErrorDesc(), uploadFile.getError());

        folderManager.createFolder(trashId, "trashedFolder", "infostore");
    }

    private String getFolderId(String name, FoldersResponse folders) {
        assertNull(folders.getError());
        Object data = folders.getData();
        @SuppressWarnings("unchecked") ArrayList<ArrayList<Object>> rootFolderArray = (ArrayList<ArrayList<Object>>) data;
        String Id = null;
        for(ArrayList<Object> obj : rootFolderArray) {
            if (obj.get(1).equals(name)) {
                Id = (String) obj.get(0);
                break;
            }
        }
        assertNotNull(Id);
        return Id;
    }

    @Test
    public void testTrashContent() throws ApiException {
        TrashFolderResponse trashContent = driveApi.getTrashContent(client.getSession(), infostoreFolder);
        assertNull(trashContent.getError());
        assertNotNull(trashContent.getData());
        TrashContent data = trashContent.getData();
        assertNotNull(data.getFiles());
        assertFalse(data.getFiles().isEmpty());
        assertEquals(1, data.getFiles().size());
        assertNotNull(data.getDirectories());
        assertFalse(data.getDirectories().isEmpty());
        assertEquals(1, data.getDirectories().size());
    }

    @Test
    public void testDeleteFromTrash() throws ApiException {
        TrashFolderResponse trashContent = driveApi.getTrashContent(client.getSession(), infostoreFolder);
        TrashContent data = trashContent.getData();

        TrashTargetsBody body = new TrashTargetsBody();
        body.addFilesItem(data.getFiles().get(0).getName());
        TrashFolderResponse removeFromTrash = driveApi.deleteFromTrash(client.getSession(), infostoreFolder, body);
        assertNull(removeFromTrash.getErrorDesc(), removeFromTrash.getError());

        trashContent = driveApi.getTrashContent(client.getSession(), "/");
        assertNull(trashContent.getError());
        assertNotNull(trashContent.getData());
        data = trashContent.getData();
        assertNotNull(data.getFiles());
        assertTrue(data.getFiles().isEmpty());
        assertNotNull(data.getDirectories());
        assertFalse(data.getDirectories().isEmpty());
        assertEquals(1, data.getDirectories().size());
    }

    @Test
    public void testRestoreFromTrash() throws ApiException {
        TrashFolderResponse trashContent = driveApi.getTrashContent(client.getSession(), infostoreFolder);
        TrashContent data = trashContent.getData();

        TrashTargetsBody body = new TrashTargetsBody();
        body.addFilesItem(data.getFiles().get(0).getName());
        TrashFolderResponse restoredFromTrash = driveApi.restoreFromTrash(client.getSession(), infostoreFolder, body);
        assertNull(restoredFromTrash.getErrorDesc(), restoredFromTrash.getError());
        assertNotNull(restoredFromTrash.getData());
        TrashContent restoreData = restoredFromTrash.getData();
        assertNotNull(restoreData.getFiles());
        assertEquals(0, restoreData.getFiles().size());

        trashContent = driveApi.getTrashContent(client.getSession(), infostoreFolder);
        assertNull(trashContent.getError());
        assertNotNull(trashContent.getData());
        data = trashContent.getData();
        assertNotNull(data.getFiles());
        assertTrue(data.getFiles().isEmpty());
        assertNotNull(data.getDirectories());
        assertFalse(data.getDirectories().isEmpty());
        assertEquals(1, data.getDirectories().size());
    }

    protected String getChecksum(byte[] bytes) throws Exception {
        MD md5 = new MD("MD5");
        md5.update(bytes);
        return md5.getFormattedValue();
    }

}
