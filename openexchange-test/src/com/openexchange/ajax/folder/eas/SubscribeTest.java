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
import org.junit.Test;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.SubscribeRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link SubscribeTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SubscribeTest extends AbstractAJAXSession {


    @Test
    public void testSubscribePrivate() throws Throwable {
        AJAXClient client = getClient();
        final String parent = FolderStorage.ROOT_ID;
        String newId = null;
        boolean unsubscribe = false;
        try {
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
            newId = (String) response.getResponse().getData();
            assertNotNull("New ID must not be null!", newId);

            SubscribeRequest subscribeRequest = new SubscribeRequest(EnumAPI.EAS_FOLDERS, parent, true);
            subscribeRequest.addFolderId(newId, true);
            client.execute(subscribeRequest);
            unsubscribe = true;

            ListRequest listRequest = new ListRequest(EnumAPI.EAS_FOLDERS, parent);
            ListResponse listResponse = client.execute(listRequest);
            boolean found = false;
            for (final Object[] vals : listResponse.getArray()) {
                if (newId.equals(vals[0].toString())) {
                    found = true;
                    break;
                }
            }
            assertTrue("Subscribed folder not found.", found);

            subscribeRequest = new SubscribeRequest(EnumAPI.EAS_FOLDERS, parent, true);
            subscribeRequest.addFolderId(newId, false);
            client.execute(subscribeRequest);
            unsubscribe = false;

            listRequest = new ListRequest(EnumAPI.EAS_FOLDERS, parent);
            listResponse = client.execute(listRequest);
            found = false;
            for (final Object[] vals : listResponse.getArray()) {
                if (newId.equals(vals[0].toString())) {
                    found = true;
                    break;
                }
            }
            assertFalse("Unsubscribed folder still available.", found);
        } finally {
            if (null != newId) {
                // Unsubscribe folder
                if (unsubscribe) {
                    final SubscribeRequest subscribeRequest = new SubscribeRequest(EnumAPI.EAS_FOLDERS, parent, true);
                    subscribeRequest.addFolderId(newId, false);
                    client.execute(subscribeRequest);
                }
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

}
