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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.testing.httpclient.models.InfoItemData;
import com.openexchange.testing.httpclient.models.InfoItemListElement;
import com.openexchange.testing.httpclient.models.InfoItemsResponse;
import com.openexchange.testing.httpclient.models.InfoItemsRestoreResponseData;


/**
 * {@link RestoreTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class RestoreTest extends InfostoreApiClientTest {

    @Test
    public void testRestore() throws Exception {
        // Create and upload a file
        final File file = File.createTempFile("infostore-restore-test", ".txt");
        String id = uploadInfoItem(file, MIME_TEXT_PLAIN);

        // Delete it
        InfoItemListElement toDelete = new InfoItemListElement();
        toDelete.setFolder(folderId);
        toDelete.setId(id);
        deleteInfoItems(Collections.singletonList(toDelete), Boolean.FALSE);


        // restore it
        toDelete.setFolder(String.valueOf(getClient().getValues().getInfostoreTrashFolder()));
        toDelete.setId(toTrash(id));
        List<InfoItemsRestoreResponseData> restoreInfoItems = restoreInfoItems(Collections.singletonList(toDelete));
        assertEquals(1, restoreInfoItems.size());

        // Check item is the same and restored path is not null
        InfoItemsRestoreResponseData restoredItem = restoreInfoItems.get(0);
        assertEquals(toDelete.getId(), restoredItem.getId());
        assertNotNull(restoredItem.getPath());
        assertFalse(restoredItem.getPath().isEmpty());
        assertEquals(folderId, restoredItem.getPath().get(0).getId());
    }

    @Test
    public void testRestoreRecreateFolders() throws Exception {
        // Create and upload a file
        final File file = File.createTempFile("infostore-restore-test", ".txt");
        String id = uploadInfoItem(file, MIME_TEXT_PLAIN);

        // Delete it
        InfoItemListElement toDelete = new InfoItemListElement();
        toDelete.setFolder(folderId);
        toDelete.setId(id);
        deleteInfoItems(Collections.singletonList(toDelete), Boolean.FALSE);

        // Delete folder
        deleteFolder(folderId, Boolean.TRUE);

        // restore it
        toDelete.setFolder(String.valueOf(getClient().getValues().getInfostoreTrashFolder()));
        toDelete.setId(toTrash(id));
        List<InfoItemsRestoreResponseData> restoreInfoItems = restoreInfoItems(Collections.singletonList(toDelete));
        assertEquals(1, restoreInfoItems.size());

        // Check item is the same and restored path is not null
        InfoItemsRestoreResponseData restoredItem = restoreInfoItems.get(0);
        assertEquals(toDelete.getId(), restoredItem.getId());
        assertNotNull(restoredItem.getPath());
        assertFalse(restoredItem.getPath().isEmpty());
        assertEquals(folderTitle, restoredItem.getPath().get(0).getTitle());

        rememberFolder(restoredItem.getPath().get(0).getId());
    }

    @Test
    public void testRestoreFileCopiedToTrash() throws Exception {
        // Create and upload a file
        final File file = File.createTempFile("infostore-restore-test", ".txt");
        String id = uploadInfoItem(file, MIME_TEXT_PLAIN);

        final String trashFolderId = String.valueOf(getClient().getValues().getInfostoreTrashFolder());

        // Copy to trash
        InfoItemData itemToCopy = new InfoItemData();
        itemToCopy.setFolderId(trashFolderId);
        itemToCopy.setId(id);
        String idCopied = copyInfoItem(id, itemToCopy);
        super.assertFileExistsInFolder(trashFolderId, idCopied);

        // restore it
        InfoItemListElement itemToRestore = new InfoItemListElement();
        itemToRestore.setFolder(trashFolderId);
        itemToRestore.setId(idCopied);
        List<InfoItemsRestoreResponseData> restoreInfoItems = restoreInfoItems(Collections.singletonList(itemToRestore));
        assertEquals(1, restoreInfoItems.size());

        // Check item is the same and restored path is not null
        InfoItemsRestoreResponseData restoredItem = restoreInfoItems.get(0);
        assertEquals(itemToRestore.getId(), restoredItem.getId());
        assertNotNull(restoredItem.getPath());
        assertFalse(restoredItem.getPath().isEmpty());
        assertEquals(folderTitle, restoredItem.getPath().get(0).getTitle());

        String folderId = restoredItem.getPath().get(0).getId();

        //Get all items in the restored folder
        InfoItemsResponse getRestoredResponse = infostoreApi.getAllInfoItems(apiClient.getSession(),
            folderId,
            Integer.toString(Metadata.FILENAME),
            null, null, null, null, null, null);
        List<List<String>> ret = (List<List<String>>)checkResponse(getRestoredResponse.getError(), getRestoredResponse.getErrorDesc(), getRestoredResponse.getData());

        //There should two items in the folder now: the original and the restored copy
        String copiedFileName = file.getName().substring(0, file.getName().lastIndexOf(".")) + " (1)" +
                                file.getName().substring(file.getName().lastIndexOf("."));
        assertTrue("The item is not present in the given folder", ret.stream().filter( l -> l.contains(file.getName())).count() == 1);
        assertTrue("The item is not present in the given folder", ret.stream().filter( l -> l.contains(copiedFileName)).count() == 1);
    }

    @Test
    public void testRestoreFileMovedToTrash() throws Exception {
        // Create and upload a file
        final File file = File.createTempFile("infostore-restore-test", ".txt");
        String id = uploadInfoItem(file, MIME_TEXT_PLAIN);

        final String trashFolderId = String.valueOf(getClient().getValues().getInfostoreTrashFolder());

        // Move to trash
        InfoItemListElement toMove = new InfoItemListElement();
        toMove.setFolder(folderId);
        toMove.setId(id);
        List<String> idsMoved = moveInfoItems(trashFolderId, Collections.singletonList(toMove));
        assertTrue(idsMoved.size() == 0);

        // restore it
        InfoItemListElement itemToRestore = new InfoItemListElement();
        itemToRestore.setFolder(trashFolderId);
        itemToRestore.setId(toTrash(id));
        List<InfoItemsRestoreResponseData> restoreInfoItems = restoreInfoItems(Collections.singletonList(itemToRestore));
        assertEquals(1, restoreInfoItems.size());

        // Check item is the same and restored path is not null
        InfoItemsRestoreResponseData restoredItem = restoreInfoItems.get(0);
        assertEquals(itemToRestore.getId(), restoredItem.getId());
        assertNotNull(restoredItem.getPath());
        assertFalse(restoredItem.getPath().isEmpty());
        assertEquals(folderTitle, restoredItem.getPath().get(0).getTitle());

        String folderId = restoredItem.getPath().get(0).getId();

        //Get all items in the restored folder
        InfoItemsResponse getRestoredResponse = infostoreApi.getAllInfoItems(apiClient.getSession(),
            folderId,
            Integer.toString(Metadata.FILENAME),
            null, null, null, null, null, null);
        List<List<String>> ret = (List<List<String>>)checkResponse(getRestoredResponse.getError(), getRestoredResponse.getErrorDesc(), getRestoredResponse.getData());

        //The item should be back in the original folder
        assertTrue("The item is not present in the given folder", ret.stream().filter( l -> l.contains(file.getName())).count() == 1);
    }

    private String toTrash(String id) throws Exception {
        String objId = id.split("/")[1];
        return getClient().getValues().getInfostoreTrashFolder() + "/" + objId;
    }

}
