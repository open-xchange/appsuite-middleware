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

package com.openexchange.ajax.folder.api2;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.FolderUpdatesResponse;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.UpdatesRequest;
import com.openexchange.ajax.framework.AbstractUpdatesRequest.Ignore;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link UpdatesTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UpdatesTest extends AbstractFolderTest {

    /**
     * Initializes a new {@link UpdatesTest}.
     *
     * @param name
     */
    public UpdatesTest() {
        super();
    }

    @Test
    public void testUpdates() throws Throwable {
        int newId = -1;
        try {
            final Date timeStamp;
            {
                final FolderObject fo = new FolderObject();
                fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
                fo.setFolderName("testCalendarFolder" + System.currentTimeMillis());
                fo.setModule(FolderObject.CALENDAR);
                final OCLPermission oclP = new OCLPermission();
                oclP.setEntity(client.getValues().getUserId());
                oclP.setGroupPermission(false);
                oclP.setFolderAdmin(true);
                oclP.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                fo.setPermissionsAsArray(new OCLPermission[] { oclP });
                final InsertRequest request = new InsertRequest(EnumAPI.OUTLOOK, fo);
                final InsertResponse response = client.execute(request);
                String tmpId = (String) response.getResponse().getData();
                assertNotNull("New ID must not be null!", tmpId);
                newId = Integer.parseInt(tmpId);
                fo.setObjectID(newId);
                timeStamp = client.execute(new GetRequest(EnumAPI.OUTLOOK, fo.getObjectID())).getTimestamp();
                fo.setLastModified(timeStamp);
            }

            final FolderUpdatesResponse response;
            {
                final UpdatesRequest request = new UpdatesRequest(EnumAPI.OUTLOOK, new int[] { FolderObject.LAST_MODIFIED_UTC, FolderObject.OBJECT_ID }, -1, null, new Date(timeStamp.getTime() - 1));
                response = client.execute(request);
            }

            assertNotNull(response);

            final List<FolderObject> l = response.getFolders();
            boolean found = false;
            for (final FolderObject folderObject : l) {
                found |= (newId == folderObject.getObjectID());
            }
            assertTrue("Newly created folder not contained in action=updates response.", found);

        } finally {
            if (newId > 0) {
                // Delete folder
                try {
                    final DeleteRequest deleteRequest = new DeleteRequest(EnumAPI.OUTLOOK, newId, new Date());
                    client.execute(deleteRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Test
    public void testUpdatesAll() throws Throwable {
        final UpdatesRequest request = new UpdatesRequest(EnumAPI.OX_NEW, new int[] { FolderObject.LAST_MODIFIED_UTC }, -1, null, new Date(0));
        final FolderUpdatesResponse response = client.execute(request);

        assertNotNull(response);
        assertFalse("Error occurred: " + response.getResponse().getErrorMessage(), response.getResponse().hasError());
    }

    @Test
    public void testModifiedAndDeleted() throws Throwable {
        // insert some
        final int numberOfFolders = 8;
        List<FolderObject> newFolders = createAndPersistSeveral("testFolder-" + UUID.randomUUID().toString(), numberOfFolders);

        // update 2
        List<FolderObject> updatedFolders = new ArrayList<FolderObject>(2);
        List<Integer> expectUpdatedFolderIds = new ArrayList<Integer>(2);
        updatedFolders.add(newFolders.get(0));
        expectUpdatedFolderIds.add(I(newFolders.get(0).getObjectID()));
        updatedFolders.add(newFolders.get(1));
        expectUpdatedFolderIds.add(I(newFolders.get(1).getObjectID()));
        updateFolders(updatedFolders);

        // delete 2
        List<FolderObject> deletedFolders = new ArrayList<FolderObject>(2);
        List<Integer> expectDeletedFolderIds = new ArrayList<Integer>(2);
        deletedFolders.add(newFolders.get(2));
        expectDeletedFolderIds.add(I(newFolders.get(2).getObjectID()));
        deletedFolders.add(newFolders.get(3));
        expectDeletedFolderIds.add(I(newFolders.get(3).getObjectID()));
        deleteFolders(deletedFolders);

        // check modified with timestamp from last
        Date lastModified = newFolders.get(numberOfFolders - 1).getLastModified();
        int[] cols = new int[] { FolderObject.OBJECT_ID, FolderObject.FOLDER_NAME };
        FolderUpdatesResponse modifiedFoldersResponse = listModifiedFolders(cols, lastModified, Ignore.NONE);
        assertTrue(modifiedFoldersResponse.getNewOrModifiedIds().containsAll(expectUpdatedFolderIds));
        assertTrue(modifiedFoldersResponse.getDeletedIds().containsAll(expectDeletedFolderIds));

        // cleanup: delete all remaining
        newFolders.removeAll(deletedFolders);
        deleteFolders(newFolders);
    }

}
