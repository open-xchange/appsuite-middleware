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

package com.openexchange.ajax.infostore.apiclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.File;
import org.junit.Test;
import com.openexchange.ajax.folder.manager.FolderManager;
import com.openexchange.groupware.modules.Module;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.InfoItemBody;
import com.openexchange.testing.httpclient.models.InfoItemData;
import com.openexchange.testing.httpclient.models.InfoItemMovedResponse;
import com.openexchange.testing.httpclient.models.InfoItemUpdateResponse;
import com.openexchange.testing.httpclient.modules.FoldersApi;


/**
 * 
 * {@link UniqueFileIdTest}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Schuerholz</a>
 * @since v7.10.6
 */
public class UniqueFileIdTest extends InfostoreApiClientTest {


    private File file;
    private FolderManager folderManager;
    private String fileId;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        file = File.createTempFile("FileWithUniqueId", ".txt");
        folderManager = new FolderManager(new FoldersApi(getApiClient()), "1");
        String folderId = folderManager.createFolder(this.getPrivateInfostoreFolder(), "UniqueFileIdTestFolder", Module.INFOSTORE.getName());
        fileId = uploadInfoItemToFolder(null, file, folderId, "text/plain", null, null, null, null, null);
    }

    @Override
    public void tearDown() throws Exception {
        folderManager.cleanUp();
        super.tearDown();
    }

    /**
     * 
     * Tests if a file has the same unique id after moving the file to another folder.
     *
     * @throws ApiException
     */
    @Test
    public void testUniqueFileIdFileMove() throws ApiException {
        String newParentFolderId = folderManager.createFolder(this.getPrivateInfostoreFolder(), "NewParentFolder", Module.INFOSTORE.getName());

        InfoItemData infoItemData = getItem(fileId);
        String uniqueIdBeforeMove = infoItemData.getUniqueId();

        InfoItemMovedResponse moveFileResponse = this.moveFile(fileId, folderId, newParentFolderId, false);
        String newFileId = checkResponse(moveFileResponse.getError(), moveFileResponse.getErrorDesc(), moveFileResponse.getData());

        InfoItemData updatedItem = getItem(newFileId);
        String uniqueIdAfterMove = updatedItem.getUniqueId();
        assertEquals(newParentFolderId, updatedItem.getFolderId());
        assertNotNull(uniqueIdAfterMove);
        assertEquals(uniqueIdBeforeMove, uniqueIdAfterMove);
    }

    /**
     * 
     * Tests if a file has the same unique id after renaming the file.
     *
     * @throws ApiException
     */
    @Test
    public void testUniqueFileIdRenaming() throws ApiException {
        InfoItemData infoItemData = getItem(fileId);
        String uniqueIdBeforeRenaming = infoItemData.getUniqueId();

        InfoItemBody infoItemBody = new InfoItemBody();
        InfoItemData updatedInfoItem = new InfoItemData();
        String newFilename = "RenamedFile";
        updatedInfoItem.setFilename(newFilename);
        infoItemBody.setFile(updatedInfoItem);
        InfoItemUpdateResponse renamedFileResponse = infostoreApi.updateInfoItem(fileId, timestamp, infoItemBody, null);
        String newFileId = checkResponse(renamedFileResponse.getError(), renamedFileResponse.getErrorDesc(), renamedFileResponse.getData());

        InfoItemData updatedItem = getItem(newFileId);
        String uniqueIdAfterRenaming = updatedItem.getUniqueId();
        String fileName = updatedItem.getFilename();
        assertEquals(newFilename, fileName);
        assertNotNull(uniqueIdAfterRenaming);
        assertEquals(uniqueIdBeforeRenaming, uniqueIdAfterRenaming);
    }

}
