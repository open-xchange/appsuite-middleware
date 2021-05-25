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
