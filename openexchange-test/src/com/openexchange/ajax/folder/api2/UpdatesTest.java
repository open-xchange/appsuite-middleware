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

package com.openexchange.ajax.folder.api2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
     * @param name
     */
    public UpdatesTest(String name) {
        super(name);
    }

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
                oclP.setAllPermission(
                    OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION);
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
                final UpdatesRequest request = new UpdatesRequest(EnumAPI.OUTLOOK, new int[] {
                    FolderObject.LAST_MODIFIED_UTC, FolderObject.OBJECT_ID }, -1, null, new Date(timeStamp.getTime() - 1));
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
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void testUpdatesAll() throws Throwable {
        final UpdatesRequest request =
            new UpdatesRequest(EnumAPI.OX_NEW, new int[] { FolderObject.LAST_MODIFIED_UTC }, -1, null, new Date(0));
        final FolderUpdatesResponse response = client.execute(request);

        assertNotNull(response);
        assertFalse("Error occurred: " + response.getResponse().getErrorMessage(), response.getResponse().hasError());
    }

    public void testModifiedAndDeleted() throws Throwable {
        // insert some
        final int numberOfFolders = 8;
        List<FolderObject> newFolders = createAndPersistSeveral("testFolder-" + System.currentTimeMillis(), numberOfFolders);

        // update 2
        List<FolderObject> updatedFolders = new ArrayList<FolderObject>(2);
        List<Integer> expectUpdatedFolderIds = new ArrayList<Integer>(2);
        updatedFolders.add(newFolders.get(0));
        expectUpdatedFolderIds.add(newFolders.get(0).getObjectID());
        updatedFolders.add(newFolders.get(1));
        expectUpdatedFolderIds.add(newFolders.get(1).getObjectID());
        updateFolders(updatedFolders);

        // delete 2
        List<FolderObject> deletedFolders = new ArrayList<FolderObject>(2);
        List<Integer> expectDeletedFolderIds = new ArrayList<Integer>(2);
        deletedFolders.add(newFolders.get(2));
        expectDeletedFolderIds.add(newFolders.get(2).getObjectID());
        deletedFolders.add(newFolders.get(3));
        expectDeletedFolderIds.add(newFolders.get(3).getObjectID());
        deleteFolders(deletedFolders);

        // check modified with timestamp from last
        Date lastModified = newFolders.get(numberOfFolders-1).getLastModified();
        int[] cols = new int[]{ FolderObject.OBJECT_ID, FolderObject.FOLDER_NAME};
        FolderUpdatesResponse modifiedFoldersResponse = listModifiedFolders(cols, lastModified, Ignore.NONE);
        assertTrue(modifiedFoldersResponse.getNewOrModifiedIds().containsAll(expectUpdatedFolderIds));
        assertTrue(modifiedFoldersResponse.getDeletedIds().containsAll(expectDeletedFolderIds));

        // cleanup: delete all remaining
        newFolders.removeAll(deletedFolders);
        deleteFolders(newFolders);
    }

}
