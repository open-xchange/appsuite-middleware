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
import java.io.File;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import com.openexchange.testing.httpclient.models.InfoItemListElement;
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
        deleteInfoItems(Collections.singletonList(toDelete), false);


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
        deleteInfoItems(Collections.singletonList(toDelete), false);

        // Delete folder
        deleteFolder(folderId, true);

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

    private String toTrash(String id) throws Exception {
        String objId = id.split("/")[1];
        return getClient().getValues().getInfostoreTrashFolder() + "/" + objId;
    }

}
