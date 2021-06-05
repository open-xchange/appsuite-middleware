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
import static org.junit.Assert.assertTrue;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link GetTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GetTest extends AbstractAJAXSession {

    @Test
    public void testGetRoot() throws Throwable {
        // Get root folder
        final GetRequest request = new GetRequest(EnumAPI.OUTLOOK, String.valueOf(FolderObject.SYSTEM_ROOT_FOLDER_ID), true);
        final GetResponse response = getClient().execute(request);

        final JSONObject jsonObject = (JSONObject) response.getResponse().getData();

        assertEquals("Unexpected root folder ID.", "0", jsonObject.get("id"));
    }

    @Test
    public void testGetPrivate() throws Throwable {
        // Get private folder
        final GetRequest request = new GetRequest(EnumAPI.OUTLOOK, String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID), true);
        final GetResponse response = getClient().execute(request);

        final JSONObject jsonObject = (JSONObject) response.getResponse().getData();

        assertEquals("Unexpected private folder ID.", "1", jsonObject.get("id"));
        assertTrue("Subfolder expected below private folder", jsonObject.getBoolean("subfolders"));
    }

    @Test
    public void testGetPublic() throws Throwable {
        // Get public folder
        final GetRequest request = new GetRequest(EnumAPI.OUTLOOK, String.valueOf(FolderObject.SYSTEM_PUBLIC_FOLDER_ID), true);
        final GetResponse response = getClient().execute(request);

        final JSONObject jsonObject = (JSONObject) response.getResponse().getData();

        assertEquals("Unexpected public folder ID.", "2", jsonObject.get("id"));
        assertTrue("Subfolder expected below public folder", jsonObject.getBoolean("subfolders"));
    }

    @Test
    public void testGetShared() throws Throwable {
        // Get shared folder
        final GetRequest request = new GetRequest(EnumAPI.OUTLOOK, String.valueOf(FolderObject.SYSTEM_SHARED_FOLDER_ID), true);
        final GetResponse response = getClient().execute(request);

        final JSONObject jsonObject = (JSONObject) response.getResponse().getData();

        assertEquals("Unexpected public folder ID.", "3", jsonObject.get("id"));
    }

}
