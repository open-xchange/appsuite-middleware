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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.Test;
import com.openexchange.test.TestInit;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ConfigResponse;
import com.openexchange.testing.httpclient.models.InfoItemBody;
import com.openexchange.testing.httpclient.models.InfoItemData;
import com.openexchange.testing.httpclient.models.InfoItemListElement;
import com.openexchange.testing.httpclient.models.InfoItemUpdateResponse;
import com.openexchange.testing.httpclient.modules.ConfigApi;
import edu.emory.mathcs.backport.java.util.Collections;

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
        InfoItemUpdateResponse response = infostoreApi.updateInfoItem(getApiClient().getSession(), actual.getId(), timestamp, body);
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
        InfoItemUpdateResponse response = infostoreApi.copyInfoItem(getApiClient().getSession(), actual.getId(), actual);
        assertNull(response.getErrorDesc(), response.getError());
        assertNotNull(response.getData());
        String newId = response.getData();
        InfoItemData copied = getItem(newId);
        assertEquals("Name should be the same", "name.name (1).txt.pgp", copied.getFilename());
    }



    @SuppressWarnings("unchecked")
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
        deleteInfoItems(Collections.singletonList(element), false);

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
        deleteInfoItems(Collections.singletonList(element), false);

        ConfigApi configAPI = new ConfigApi(getApiClient());
        ConfigResponse response = configAPI.getConfigNode("/modules/infostore/folder/trash", getApiClient().getSession());

        assertNull(response.getErrorDesc(), response.getError());
        assertNotNull(response.getData());
        Object trashId = response.getData();
        final String id1 = trashId.toString() + "/" + oldId.substring(oldId.indexOf("/") + 1, oldId.length());
        final String id2 = trashId.toString() + "/" + actual.getId().substring(actual.getId().indexOf("/") + 1, actual.getId().length());

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

        deleteInfoItems(toDelete, true);
    }

}
