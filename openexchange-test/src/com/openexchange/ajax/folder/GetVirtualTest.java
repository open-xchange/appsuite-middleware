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

package com.openexchange.ajax.folder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link GetVirtualTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GetVirtualTest extends AbstractAJAXSession {


    @Test
    public void testGetVirtual() throws Throwable {
        final GetRequest getRequest = new GetRequest(EnumAPI.OX_OLD, FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID, new int[] { FolderObject.OBJECT_ID, FolderObject.FOLDER_NAME, FolderObject.OWN_RIGHTS, FolderObject.PERMISSIONS_BITS });
        final GetResponse getResponse = getClient().execute(getRequest);
        assertFalse("GET request failed.", getResponse.hasError());
        final FolderObject folder = getResponse.getFolder();
        assertEquals("Unexpected object ID: ", FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID, folder.getObjectID());

        final OCLPermission[] perms = folder.getNonSystemPermissionsAsArray();
        assertNotNull("Missing permissions", perms);
        assertEquals("Unexpected number of permissions: ", 1, perms.length);

        final OCLPermission p = perms[0];
        assertNotNull("Missing permission", p);
        assertEquals("Unexpected entity: ", OCLPermission.ALL_GROUPS_AND_USERS, p.getEntity());
        assertEquals("Unexpected folder permission: ", OCLPermission.READ_FOLDER, p.getFolderPermission());
        assertEquals("Unexpected read permission: ", OCLPermission.NO_PERMISSIONS, p.getReadPermission());
        assertEquals("Unexpected write permission: ", OCLPermission.NO_PERMISSIONS, p.getWritePermission());
        assertEquals("Unexpected delete permission: ", OCLPermission.NO_PERMISSIONS, p.getDeletePermission());
        assertFalse("Unexpected folder admin flag: ", p.isFolderAdmin());
        assertTrue("Unexpected group flag: ", p.isGroupPermission());
    }

}
