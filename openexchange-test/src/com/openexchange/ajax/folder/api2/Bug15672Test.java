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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link Bug15672Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug15672Test extends AbstractAJAXSession {

    private AJAXClient client;
    private FolderObject folder;

    public Bug15672Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        folder = new FolderObject();
        folder.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
        folder.setFolderName("Bug 15672 Test folder " + System.currentTimeMillis());
        folder.setModule(FolderObject.TASK);
        //        folder.setPermissionsAsArray(new OCLPermission[0]);
        InsertRequest request = new InsertRequest(EnumAPI.OUTLOOK, folder);
        CommonInsertResponse response = client.execute(request);
        response.fillObject(folder);
    }

    @Test
    public void test4ProperPermissions() throws Throwable {
        GetRequest request = new GetRequest(EnumAPI.OUTLOOK, folder.getObjectID());
        GetResponse response = client.execute(request);
        FolderObject testFolder = response.getFolder();
        List<OCLPermission> permissions = testFolder.getPermissions();
        assertEquals("Folder should have at least 1 defined permission.", 1, permissions.size());
        OCLPermission permission = permissions.get(0);
        assertEquals("Current user should be folder administrator.", client.getValues().getUserId(), permission.getEntity());
        assertTrue(permission.isFolderAdmin());
        assertFalse(permission.isSystem());
        assertEquals(OCLPermission.ADMIN_PERMISSION, permission.getFolderPermission());
        assertEquals(OCLPermission.ADMIN_PERMISSION, permission.getReadPermission());
        assertEquals(OCLPermission.ADMIN_PERMISSION, permission.getWritePermission());
        assertEquals(OCLPermission.ADMIN_PERMISSION, permission.getDeletePermission());
    }
}
