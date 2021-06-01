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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link UpdateTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UpdateTest extends AbstractAJAXSession {


    @Test
    public void testUpdatePrivate() throws Throwable {
        FolderObject fo = null;
        try {
            fo = new FolderObject();
            fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
            fo.setFolderName("testCalendarFolder" + System.currentTimeMillis());
            fo.setModule(FolderObject.CALENDAR);
            {
                final OCLPermission oclP = new OCLPermission();
                oclP.setEntity(getClient().getValues().getUserId());
                oclP.setGroupPermission(false);
                oclP.setFolderAdmin(true);
                oclP.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                fo.setPermissionsAsArray(new OCLPermission[] { oclP });
            }
            final String newId;
            {
                final InsertRequest request = new InsertRequest(EnumAPI.OUTLOOK, fo);
                final InsertResponse response = getClient().execute(request);
                newId = (String) response.getResponse().getData();
                assertNotNull("New ID must not be null!", newId);
            }
            {
                fo.setLastModified(getClient().execute(new GetRequest(EnumAPI.OUTLOOK, newId)).getTimestamp());
            }
            fo.setFolderName("testCalendarFolderRename" + System.currentTimeMillis());
            fo.setObjectID(Integer.parseInt(newId));
            {
                final OCLPermission oclP = new OCLPermission();
                oclP.setEntity(getClient().getValues().getUserId());
                oclP.setGroupPermission(false);
                oclP.setFolderAdmin(true);
                oclP.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                final OCLPermission oclP2 = new OCLPermission();
                oclP2.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
                oclP2.setGroupPermission(true);
                oclP2.setFolderAdmin(false);
                oclP2.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
                fo.setPermissionsAsArray(new OCLPermission[] { oclP, oclP2 });
            }
            {
                final UpdateRequest updateRequest = new UpdateRequest(EnumAPI.OUTLOOK, fo);
                getClient().execute(updateRequest).getResponse();
            }
            {
                final GetRequest request = new GetRequest(EnumAPI.OUTLOOK, newId);
                final GetResponse response = getClient().execute(request);
                fo.setLastModified(response.getTimestamp());
                final JSONObject jsonObject = (JSONObject) response.getResponse().getData();

                final String name = jsonObject.getString("title");
                assertNotNull("Folder name expected", name);

                assertEquals("Rename failed.", fo.getFolderName(), name);

                final JSONArray permissions = jsonObject.getJSONArray("permissions");
                assertEquals("Unexpected number of permissions.", 2, permissions.length());
            }
        } finally {
            if (null != fo) {
                // Delete folder
                try {
                    final DeleteRequest deleteRequest = new DeleteRequest(EnumAPI.OUTLOOK, fo);
                    getClient().execute(deleteRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
