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

package com.openexchange.ajax.folder.api2;

import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.Test;
import com.openexchange.ajax.folder.manager.FolderManager;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.util.Pair;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FolderUpdatesResponse;
import com.openexchange.testing.httpclient.modules.FoldersApi;


/**
 * {@link MWB1030Test}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Schuerholz</a>
 * @since v8.0.0
 */
public class MWB1030Test extends AbstractAPIClientSession {

    private FoldersApi foldersApi;
    private FolderManager folderManager;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        foldersApi = new FoldersApi(getApiClient());
        folderManager = new FolderManager(foldersApi, null);
    }

    @Override
    public void tearDown() throws Exception {
        folderManager.cleanUp();
        super.tearDown();
    }

    /**
     * 
     * Tests that the Folder Updates action does not return duplicates in the response
     * when called after creating a new folder (see MWB-1030).
     *
     * @throws ApiException
     */
    @Test
    public void testForDuplicatesInFoldersUpdatesAfterFolderCreation() throws ApiException {
        Long timestamp = L(System.currentTimeMillis());
        String parent = folderManager.findInfostoreRoot();
        String newFolderId = folderManager.createFolder(parent, "TestFolder", Module.INFOSTORE.getName());
        FolderUpdatesResponse folderUpdates = foldersApi.getFolderUpdates(timestamp, "1,300", null, null, null, null);
        // Expects an array with the structure: [[folderId1, folderName1],[folderId2, folderName2],...]
        ArrayList<Object> data = (ArrayList<Object>) checkResponse(folderUpdates.getError(), folderUpdates.getErrorDesc(), folderUpdates.getData());

        // Test for no duplicates in response data
        Pair<ArrayList<String>, ArrayList<String>> pair = getFolderIds(data);
        ArrayList<String> modifiedFolderIds = pair.getFirst();
        assertTrue("Parent folder not included in updated folders.", modifiedFolderIds.contains(parent));
        assertTrue("New created folder not included in updated folders.", modifiedFolderIds.contains(newFolderId));
    }

    /**
     * 
     * Tests that the Folder Updates action does not return duplicates in the response
     * when called after a folder is deleted.
     *
     * @throws ApiException
     */
    @Test
    public void testForDuplicatesInFoldersUpdatesAfterFolderRemove() throws ApiException {
        String parent = folderManager.findInfostoreRoot();
        String newFolderId = folderManager.createFolder(parent, "TestFolder", Module.INFOSTORE.getName());
        Long timestamp = L(System.currentTimeMillis());
        folderManager.deleteFolder(Collections.singletonList(newFolderId));
        FolderUpdatesResponse folderUpdates = foldersApi.getFolderUpdates(timestamp, "1,300", null, null, null, null);
        // Expects an array with the structure: [[folderId1, folderName1],[folderId2, folderName2],...]
        ArrayList<Object> data = (ArrayList<Object>) checkResponse(folderUpdates.getError(), folderUpdates.getErrorDesc(), folderUpdates.getData());

        // Test for no duplicates in response data
        Pair<ArrayList<String>, ArrayList<String>> pair = getFolderIds(data);
        ArrayList<String> modifiedFolderIds = pair.getFirst();
        ArrayList<String> deletedFolderIds = pair.getSecond();
        assertTrue("Deleted folder not included in response.", deletedFolderIds.contains(newFolderId));
        assertTrue("Parent folder not included in updated folders.", modifiedFolderIds.contains(parent));
    }
    
    /**
     * 
     * Gets the folder ids of the modified and deleted folders from an updates response.
     *
     * @param updatesResponseData An array list with data from FolderUpdatesResponse.
     * @return A pair with two array lists. The lists contain the folder ids from the modified (1) and deleted (2) folders.
     */
    private Pair<ArrayList<String>, ArrayList<String>> getFolderIds(ArrayList<Object> updatesResponseData) {
        ArrayList<String> modifiedFolderIds = new ArrayList<String>();
        ArrayList<String> deletedFolderIds = new ArrayList<String>();
        for (Object item : updatesResponseData) {
            if (item instanceof ArrayList) {
                @SuppressWarnings("unchecked") ArrayList<String> folder = (ArrayList<String>) item;
                String folderId = folder.get(0);
                if (modifiedFolderIds.contains(folderId)) {
                    fail("There are duplicates in the response array: Folder \"" + folder.get(1) + "\" with folder id " + folderId);
                } else {
                    modifiedFolderIds.add(folderId);
                }
            } else if (item instanceof String) {
                String folderId = (String) item;
                if (deletedFolderIds.contains(folderId)) {
                    fail("There are duplicates in the response array: Folder id " + folderId);
                } else {
                    deletedFolderIds.add(folderId);
                }
            }
        }
        return new Pair<ArrayList<String>, ArrayList<String>>(modifiedFolderIds, deletedFolderIds);
    }
}
