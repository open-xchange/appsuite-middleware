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
import static org.junit.Assert.assertNull;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.Test;
import com.openexchange.test.common.test.TestInit;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ConfigResponse;
import com.openexchange.testing.httpclient.models.InfoItemBody;
import com.openexchange.testing.httpclient.models.InfoItemData;
import com.openexchange.testing.httpclient.models.InfoItemListElement;
import com.openexchange.testing.httpclient.models.InfoItemUpdateResponse;
import com.openexchange.testing.httpclient.modules.ConfigApi;

/**
 * {@link Bug40142Test}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class Bug40142Test extends InfostoreApiClientTest {

    public Bug40142Test() {
        super();
    }

    @Test
    public void testCreatingTwoEquallyNamedFiles() throws ApiException, FileNotFoundException, IOException {
        String filename = "name.name.txt.pgp";
        {
            final java.io.File file = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));
            String uploadInfoItem = uploadInfoItem(null, file, "text/plain", null, filename);
            final InfoItemData actual = getItem(uploadInfoItem);
            assertEquals("Name should be the same", filename, actual.getFilename());
        }

        {
            final java.io.File file = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));
            String uploadInfoItem = uploadInfoItem(null, file, "text/plain", null, filename);
            final InfoItemData actual = getItem(uploadInfoItem);
            assertEquals("Name should be the same", "name.name (1).txt.pgp", actual.getFilename());
        }

    }

    @Test
    public void testUpdateFileWithExistingName() throws FileNotFoundException, ApiException, IOException {
        String filename = "name.name.txt.pgp";
        {
            final java.io.File file = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));
            String uploadInfoItem = uploadInfoItem(null, file, "text/plain", null, filename);
            final InfoItemData actual = getItem(uploadInfoItem);
            assertEquals("Name should be the same", filename, actual.getFilename());
        }

        InfoItemData actual;
        {
            String secondName = "name.name.txt";
            final java.io.File file = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));
            String uploadInfoItem = uploadInfoItem(null, file, "text/plain", null, secondName);
            actual = getItem(uploadInfoItem);
            assertEquals("Name should be the same", secondName, actual.getFilename());
        }

        actual.setFilename("name.name.txt.pgp");
        InfoItemBody body = new InfoItemBody();
        body.setFile(actual);
        InfoItemUpdateResponse response = infostoreApi.updateInfoItem(actual.getId(), timestamp, body, null);
        assertNull(response.getErrorDesc(), response.getError());

        InfoItemData changed = getItem(actual.getId());

        assertEquals("Name should be the same", "name.name (1).txt.pgp", changed.getFilename());
    }

    @Test
    public void testCopyFile() throws FileNotFoundException, ApiException, IOException {

        InfoItemData actual;
        {
            String filename = "name.name.txt.pgp";
            final java.io.File file = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));
            String uploadInfoItem = uploadInfoItem(null, file, "text/plain", null, filename);
            actual = getItem(uploadInfoItem);
            assertEquals("Name should be the same", filename, actual.getFilename());
        }
        InfoItemUpdateResponse response = infostoreApi.copyInfoItem(actual.getId(), actual, null);
        assertNull(response.getErrorDesc(), response.getError());
        assertNotNull(response.getData());
        String newId = response.getData();
        InfoItemData copied = getItem(newId);
        assertEquals("Name should be the same", "name.name (1).txt.pgp", copied.getFilename());
    }



    @Test
    public void testDeleteFileWithExistingNameInTrash() throws FileNotFoundException, ApiException, IOException {

        InfoItemData actual;
        {
            String filename = "name.name.txt.pgp";
            final java.io.File file = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));
            String uploadInfoItem = uploadInfoItem(null, file, "text/plain", null, filename);
            actual = getItem(uploadInfoItem);
            assertEquals("Name should be the same", filename, actual.getFilename());
        }

        String oldId = actual.getId();
        InfoItemListElement element = new InfoItemListElement();
        element.setFolder(folderId);
        element.setId(actual.getId());
        deleteInfoItems(Collections.singletonList(element), Boolean.FALSE);

        actual = null;

        {
            String filename = "name.name.txt.pgp";
            final java.io.File file = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));
            String uploadInfoItem = uploadInfoItem(null, file, "text/plain", null, filename);
            actual = getItem(uploadInfoItem);
            assertEquals("Name should be the same", filename, actual.getFilename());
        }

        element = new InfoItemListElement();
        element.setFolder(folderId);
        element.setId(actual.getId());
        deleteInfoItems(Collections.singletonList(element), Boolean.FALSE);

        ConfigApi configAPI = new ConfigApi(getApiClient());
        ConfigResponse response = configAPI.getConfigNode("/modules/infostore/folder/trash");

        assertNull(response.getErrorDesc(), response.getError());
        assertNotNull(response.getData());
        Object trashId = response.getData();
        final String id1 = trashId.toString() + "/" + oldId.substring(oldId.indexOf('/') + 1, oldId.length());
        final String id2 = trashId.toString() + "/" + actual.getId().substring(actual.getId().indexOf('/') + 1, actual.getId().length());

        InfoItemData deleted = getItem(id2);
        assertEquals("Name should be the same", "name.name (1).txt.pgp", deleted.getFilename());

        ArrayList<InfoItemListElement> toDelete = new ArrayList<>(2);
        element = new InfoItemListElement();
        element.setFolder(trashId.toString());
        element.setId(id1);
        toDelete.add(element);
        InfoItemListElement element2 = new InfoItemListElement();
        element2.setFolder(trashId.toString());
        element2.setId(id2);
        toDelete.add(element2);

        deleteInfoItems(toDelete, Boolean.TRUE);
    }

}
