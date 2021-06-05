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

package com.openexchange.ajax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.FolderUpdatesResponse;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.UpdatesRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.test.FolderTestManager;

public class FolderTest extends AbstractAJAXSession {

    public static final String FOLDER_URL = "/ajax/folders";

    @Test
    public void testGetRootFolders() {
        final int[] assumedIds = { 1, 2, 3, 9 };
        final List<FolderObject> l = Arrays.asList(ftm.listRootFoldersOnServer());
        assertFalse(l == null || l.size() == 0);
        int i = 0;
        for (Object element : l) {
            final FolderObject rf = (FolderObject) element;
            assertTrue(rf.getObjectID() == assumedIds[i]);
            i++;
        }
    }

    @Test
    public void testDeleteFolder() throws OXException, JSONException, IOException, OXException {
        final int userId = getClient().getValues().getUserId();

        FolderObject parentObj = FolderTestManager.createNewFolderObject("DeleteMeImmediately" + UUID.randomUUID().toString(), Module.CALENDAR.getFolderConstant(), FolderObject.PUBLIC, userId, FolderObject.SYSTEM_PUBLIC_FOLDER_ID);

        final int parent = ftm.insertFolderOnServer(parentObj).getParentFolderID();
        assertFalse(parent == -1);
        final Calendar cal = GregorianCalendar.getInstance();
        ftm.getFolderFromServer(parent);

        FolderObject subFolder = FolderTestManager.createNewFolderObject("DeleteMeImmediatelyChild01" + UUID.randomUUID().toString(), Module.CALENDAR.getFolderConstant(), FolderObject.PUBLIC, userId, parent);
        final int child01 = ftm.insertFolderOnServer(subFolder).getObjectID();
        assertFalse(child01 == -1);
        ftm.getFolderFromServer(child01);

        FolderObject subFolder2 = FolderTestManager.createNewFolderObject("DeleteMeImmediatelyChild02" + UUID.randomUUID().toString(), Module.CALENDAR.getFolderConstant(), FolderObject.PUBLIC, userId, parent);
        final int child02 = ftm.insertFolderOnServer(subFolder2).getObjectID();
        assertFalse(child02 == -1);
        ftm.getFolderFromServer(child02);

        ftm.deleteFolderOnServer(parent, new Date(cal.getTimeInMillis()));
    }

    @Test
    public void testCheckFolderPermissions() throws OXException, IOException, JSONException {
        final int userId = getClient().getValues().getUserId();
        FolderObject newFolder = FolderTestManager.createNewFolderObject("CheckMyPermissions", Module.CALENDAR.getFolderConstant(), FolderObject.PUBLIC, userId, FolderObject.SYSTEM_PUBLIC_FOLDER_ID);

        final int fuid = ftm.insertFolderOnServer(newFolder).getObjectID();
        FolderObject folder = ftm.getFolderFromServer(fuid);
        FolderObject updated = ftm.updateFolderOnServer(folder);
        ftm.deleteFolderOnServer(updated);
    }

    @Test
    public void testInsertRenameFolder() throws OXException, IOException, JSONException, OXException {
        int fuid = -1;
        try {
            final int userId = getClient().getValues().getUserId();

            FolderObject newFolder = FolderTestManager.createNewFolderObject("NewPrivateFolder" + UUID.randomUUID().toString(), Module.CALENDAR.getFolderConstant(), FolderObject.PRIVATE, userId, FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
            fuid = ftm.insertFolderOnServer(newFolder).getObjectID();
            assertFalse(fuid == -1);

            FolderObject folder = ftm.getFolderFromServer(fuid);
            folder.setFolderName("ChangedPrivateFolderName" + System.currentTimeMillis());
            ftm.updateFolderOnServer(folder);
            FolderObject folderFromServer = ftm.getFolderFromServer(fuid);
            ftm.deleteFolderOnServer(fuid, folderFromServer.getLastModified());
            assertFalse(ftm.getLastResponse().hasError());

            FolderObject newPublicFolder = FolderTestManager.createNewFolderObject("NewPublicFolder" + UUID.randomUUID().toString(), Module.CALENDAR.getFolderConstant(), FolderObject.PRIVATE, userId, FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
            fuid = ftm.insertFolderOnServer(newPublicFolder).getObjectID();
            assertFalse(fuid == -1);
            folderFromServer = ftm.getFolderFromServer(fuid);
            ftm.deleteFolderOnServer(fuid, folderFromServer.getLastModified());
            assertFalse(ftm.getLastResponse().hasError());
            fuid = -1;

            FolderObject newInfostoreFolder = FolderTestManager.createNewFolderObject("NewInfostoreFolder" + UUID.randomUUID().toString(), Module.INFOSTORE.getFolderConstant(), FolderObject.PUBLIC, userId, getClient().getValues().getPrivateInfostoreFolder());
            fuid = ftm.insertFolderOnServer(newInfostoreFolder).getObjectID();
            assertFalse(fuid == -1);
            FolderObject retrievedNewInfoStoreFolder = ftm.getFolderFromServer(fuid);
            retrievedNewInfoStoreFolder.setFolderName("ChangedInfostoreFolderName" + System.currentTimeMillis());
            ftm.updateFolderOnServer(retrievedNewInfoStoreFolder);

            folderFromServer = ftm.getFolderFromServer(fuid);
            assertFalse(ftm.getLastResponse().hasError());
            ftm.deleteFolderOnServer(fuid, folderFromServer.getLastModified());
            assertFalse(ftm.getLastResponse().hasError());
            fuid = -1;
        } finally {
            try {
                if (fuid != -1) {
                    final Calendar cal = GregorianCalendar.getInstance();
                    /*
                     * Call getFolder to receive a valid timestamp for deletion
                     */
                    ftm.getFolderFromServer(fuid);
                    ftm.deleteFolderOnServer(fuid, new Date(cal.getTimeInMillis()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testMoveFolder() throws IOException, JSONException, OXException {
        int parent01 = -1;
        int parent02 = -1;
        int moveFuid = -1;
        final int userId = getClient().getValues().getUserId();

        FolderObject parent1 = FolderTestManager.createNewFolderObject("Parent01" + UUID.randomUUID().toString(), Module.CALENDAR.getFolderConstant(), FolderObject.PRIVATE, userId, FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
        parent01 = ftm.insertFolderOnServer(parent1).getObjectID();
        assertFalse(parent01 == -1);

        FolderObject parent2 = FolderTestManager.createNewFolderObject("Parent02" + UUID.randomUUID().toString(), Module.CALENDAR.getFolderConstant(), FolderObject.PRIVATE, userId, FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
        parent02 = ftm.insertFolderOnServer(parent2).getObjectID();
        assertFalse(parent02 == -1);

        FolderObject moveMe = FolderTestManager.createNewFolderObject("MoveMe" + UUID.randomUUID().toString(), Module.CALENDAR.getFolderConstant(), FolderObject.PRIVATE, userId, parent01);
        moveFuid = ftm.insertFolderOnServer(moveMe).getObjectID();
        assertFalse(moveFuid == -1);

        FolderObject folderToMove = ftm.getFolderFromServer(moveFuid);
        folderToMove.setParentFolderID(parent02);
        folderToMove.setLastModified(moveMe.getLastModified());

        ftm.updateFolderOnServer(folderToMove); //move
        FolderObject movedFolderObj = ftm.getFolderFromServer(moveFuid);
        assertTrue(movedFolderObj.containsParentFolderID() ? movedFolderObj.getParentFolderID() == parent02 : true);
    }

    @Test
    public void testFolderNamesShouldBeEqualRegardlessOfRequestMethod() throws Exception {
        for (boolean altNames : new boolean[] { true, false}) {
            for (FolderObject rootFolder : ftm.listRootFoldersOnServer(altNames)) {
                GetRequest request = new GetRequest(EnumAPI.OX_OLD, rootFolder.getObjectID(), FolderObject.ALL_COLUMNS);
                request.setAltNames(altNames);
                GetResponse getResponse = getClient().execute(request);
                assertFalse(getResponse.hasError());
                FolderObject folder = getResponse.getFolder();
                assertEquals("Foldernames differ : " + rootFolder.getFolderName() + " != " + folder.getFolderName(), rootFolder.getFolderName(), folder.getFolderName());
            }
        }
    }

    // Node 2652

    @Test
    public void testLastModifiedUTCInGet() throws JSONException, OXException, IOException {
        // Load an existing folder
        final GetRequest getRequest = new GetRequest(EnumAPI.OX_OLD, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, new int[] { FolderObject.LAST_MODIFIED_UTC });
        final GetResponse response = Executor.execute(getClient(), getRequest);
        assertTrue(((JSONObject) response.getData()).has("last_modified_utc"));
    }

    // Node 2652

    @Test
    public void testLastModifiedUTCInList() throws JSONException, IOException, OXException {
        // List known folder
        final ListRequest listRequest = new ListRequest(EnumAPI.OX_OLD, "" + FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, new int[] { FolderObject.LAST_MODIFIED_UTC }, false);
        final ListResponse listResponse = getClient().execute(listRequest);
        final JSONArray arr = (JSONArray) listResponse.getData();
        final int size = arr.length();
        assertTrue(size > 0);
        for (int i = 0; i < size; i++) {
            final JSONArray row = arr.optJSONArray(i);
            assertNotNull(row);
            assertTrue(row.length() == 1);
            assertNotNull(row.get(0));
        }
    }

    // Node 2652

    @Test
    public void testLastModifiedUTCInUpdates() throws JSONException, OXException, IOException {
        // List known folder
        final UpdatesRequest updatesRequest = new UpdatesRequest(EnumAPI.OX_OLD, new int[] { FolderObject.LAST_MODIFIED_UTC }, -1, null, new Date(0));
        final FolderUpdatesResponse response = Executor.execute(getClient(), updatesRequest);

        final JSONArray arr = (JSONArray) response.getData();
        final int size = arr.length();
        assertTrue(size > 0);
        for (int i = 0; i < size; i++) {
            final JSONArray row = arr.optJSONArray(i);
            assertNotNull(row);
            assertTrue(row.length() == 1);
            assertNotNull(row.get(0));
        }
    }
}
