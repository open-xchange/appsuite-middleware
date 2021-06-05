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

package com.openexchange.ajax.folder.eas;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.LinkedList;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.SubscribeRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link MultipleSubscribeTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MultipleSubscribeTest extends AbstractAJAXSession {

    /**
     * Initializes a new {@link MultipleSubscribeTest}.
     *
     * @param name The name of the test.
     */
    public MultipleSubscribeTest() {
        super();
    }

    private String createPrivateCalendarFolder() throws Throwable {
        final FolderObject fo = new FolderObject();
        fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
        fo.setFolderName("testCalendarFolder" + System.currentTimeMillis());
        fo.setModule(FolderObject.CALENDAR);

        final OCLPermission oclP = new OCLPermission();
        oclP.setEntity(getClient().getValues().getUserId());
        oclP.setGroupPermission(false);
        oclP.setFolderAdmin(true);
        oclP.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        fo.setPermissionsAsArray(new OCLPermission[] { oclP });
        final InsertRequest request = new InsertRequest(EnumAPI.OUTLOOK, fo);
        final InsertResponse response = getClient().execute(request);
        return (String) response.getResponse().getData();
    }

    @Test
    public void testSubscribeMultiplePrivate() throws Throwable {
        final String parent = FolderStorage.ROOT_ID;
        final LinkedList<String> ids = new LinkedList<String>();
        try {
            final String newId = createPrivateCalendarFolder();
            assertNotNull("New ID must not be null!", newId);
            ids.addFirst(newId);

            SubscribeRequest subscribeRequest = new SubscribeRequest(EnumAPI.EAS_FOLDERS, parent, true);
            subscribeRequest.addFolderId(newId, true);
            getClient().execute(subscribeRequest);

            /*-
             * ---------------------------------------------------
             */

            final String newSubId = createPrivateCalendarFolder();
            assertNotNull("New ID must not be null!", newSubId);
            ids.addFirst(newSubId);

            subscribeRequest = new SubscribeRequest(EnumAPI.EAS_FOLDERS, newId, true);
            subscribeRequest.addFolderId(newSubId, true);
            getClient().execute(subscribeRequest);

            ListRequest listRequest = new ListRequest(EnumAPI.EAS_FOLDERS, newId);
            ListResponse listResponse = getClient().execute(listRequest);
            boolean found = false;
            for (final Object[] vals : listResponse.getArray()) {
                if (newSubId.equals(vals[0].toString())) {
                    found = true;
                    break;
                }
            }
            assertTrue("Subscribed subfolder not found.", found);

            /*-
             * ---------------------------------------------------
             */

            subscribeRequest = new SubscribeRequest(EnumAPI.EAS_FOLDERS, newId, true);
            subscribeRequest.addFolderId(newSubId, false);
            getClient().execute(subscribeRequest);

            listRequest = new ListRequest(EnumAPI.EAS_FOLDERS, newId);
            listResponse = getClient().execute(listRequest);
            found = false;
            for (final Object[] vals : listResponse.getArray()) {
                if (newSubId.equals(vals[0].toString())) {
                    found = true;
                    break;
                }
            }
            assertFalse("Unsubscribed subfolder still available.", found);
        } finally {
            final int size = ids.size();
            for (int i = 0; i < size; i++) {
                final String id = ids.get(i);
                // Try unsubscribe folder
                {
                    final SubscribeRequest subscribeRequest = new SubscribeRequest(EnumAPI.EAS_FOLDERS, parent, true);
                    subscribeRequest.addFolderId(id, false);
                    getClient().execute(subscribeRequest);
                }
                // Delete folder
                try {
                    final DeleteRequest deleteRequest = new DeleteRequest(EnumAPI.OUTLOOK, id, new Date());
                    getClient().execute(deleteRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
