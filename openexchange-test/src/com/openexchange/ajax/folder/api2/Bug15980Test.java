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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.PathRequest;
import com.openexchange.ajax.folder.actions.PathResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.common.test.PermissionTools;

/**
 * {@link Bug15980Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug15980Test extends AbstractAJAXSession {

    private AJAXClient client;
    private FolderObject testFolder;

    public Bug15980Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        testFolder = new FolderObject();
        testFolder.setModule(FolderObject.CALENDAR);
        testFolder.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        testFolder.setPermissions(PermissionTools.P(I(client.getValues().getUserId()), PermissionTools.ADMIN));
        testFolder.setFolderName("testFolder4Bug15980");
        InsertRequest iReq = new InsertRequest(EnumAPI.OUTLOOK, testFolder);
        InsertResponse iResp = client.execute(iReq);
        iResp.fillObject(testFolder);
        // Unfortunately no timestamp when creating a mail folder through Outlook folder tree.
        testFolder.setLastModified(new Date());
    }

    @Test
    public void testPath() throws Throwable {
        PathRequest request = new PathRequest(EnumAPI.OUTLOOK, testFolder.getObjectID(), new int[] { FolderObject.OBJECT_ID, FolderObject.FOLDER_NAME });
        PathResponse response = client.execute(request);
        Object[][] objects = response.getArray();
        int idPos = response.getColumnPos(FolderObject.OBJECT_ID);
        assertTrue("Response should contain folder identifier.", idPos >= 0);
        int namePos = response.getColumnPos(FolderObject.FOLDER_NAME);
        assertTrue("Response should contain folder names.", namePos >= 0);
        assertEquals("Path on Outlook like tree should have 3 parts.", 3, objects.length);
        assertEquals("Path should start with test folder but is folder " + objects[0][namePos], Integer.toString(testFolder.getObjectID()), objects[0][idPos]);
        assertEquals("Parent of created folder should be public folders but is " + objects[1][namePos], FolderStorage.PUBLIC_ID, objects[1][idPos]);
        assertEquals("Root folder should be IPM_ROOT but is " + objects[2][namePos], FolderStorage.PRIVATE_ID, objects[2][idPos]);
    }
}
