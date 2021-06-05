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
import java.util.Date;
import org.json.JSONArray;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.PathRequest;
import com.openexchange.ajax.folder.actions.PathResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link PathTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PathTest extends AbstractAJAXSession {

    private static final String PRIVATE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);

    @Test
    public void testPath1() throws Throwable {
        final PathRequest pathRequest = new PathRequest(EnumAPI.OUTLOOK, String.valueOf(FolderObject.SYSTEM_ROOT_FOLDER_ID));
        final PathResponse pathResponse = getClient().execute(pathRequest);

        final JSONArray jsonArray = (JSONArray) pathResponse.getResponse().getData();
        final int length = jsonArray.length();

        assertEquals("Unexpected path length.", 0, length);
    }

    @Test
    public void testPath2() throws Throwable {
        final PathRequest pathRequest = new PathRequest(EnumAPI.OUTLOOK, PRIVATE_FOLDER_ID);
        final PathResponse pathResponse = getClient().execute(pathRequest);

        final JSONArray jsonArray = (JSONArray) pathResponse.getResponse().getData();
        final int length = jsonArray.length();

        assertEquals("Unexpected path length.", 1, length);

        assertEquals("Unexpected path element.", PRIVATE_FOLDER_ID, jsonArray.getJSONArray(0).getString(0));

    }

    @Test
    public void testPath3() throws Throwable {
        String newId = null;
        try {
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

            newId = (String) response.getResponse().getData();
            assertNotNull("New ID must not be null!", newId);

            final PathRequest pathRequest = new PathRequest(EnumAPI.OUTLOOK, newId);
            final PathResponse pathResponse = getClient().execute(pathRequest);

            final JSONArray jsonArray = (JSONArray) pathResponse.getResponse().getData();
            final int length = jsonArray.length();

            // System.out.println(jsonArray);

            assertEquals("Unexpected path length:" + System.getProperty("line.separator") + jsonArray, 2, length);

            assertEquals("Unexpected path element.", newId, jsonArray.getJSONArray(0).getString(0));
            assertEquals("Unexpected path element.", PRIVATE_FOLDER_ID, jsonArray.getJSONArray(1).getString(0));
        } finally {
            if (null != newId) {
                // Delete folder
                try {
                    final DeleteRequest deleteRequest = new DeleteRequest(EnumAPI.OUTLOOK, newId, new Date());
                    getClient().execute(deleteRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
