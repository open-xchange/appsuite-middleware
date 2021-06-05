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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.test.common.test.TestClassConfig;
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

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().createApiClient().build();
    }

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
        InfoItemsResponse getRestoredResponse = infostoreApi.getAllInfoItems(
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
        List<InfoItemListElement> idsMoved = moveInfoItems(trashFolderId, Collections.singletonList(toMove));
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
        InfoItemsResponse getRestoredResponse = infostoreApi.getAllInfoItems(
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
