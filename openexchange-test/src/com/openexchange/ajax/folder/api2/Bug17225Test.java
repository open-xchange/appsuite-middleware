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
import java.util.Iterator;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link Bug17225Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug17225Test extends AbstractAJAXSession {

    private AJAXClient client;
    private AJAXClient client2;
    private FolderObject folder;
    private int userId1;

    public Bug17225Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        userId1 = client.getValues().getUserId();
        client2 = testUser2.getAjaxClient();
        int folderId = client.getValues().getPrivateAppointmentFolder();
        GetResponse getR = client.execute(new GetRequest(EnumAPI.OUTLOOK, folderId));
        FolderObject oldFolder = getR.getFolder();
        folder = new FolderObject();
        folder.setObjectID(oldFolder.getObjectID());
        folder.setLastModified(getR.getTimestamp());
        folder.setPermissionsAsArray(new OCLPermission[] { Create.ocl(userId1, false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION), Create.ocl(client2.getValues().getUserId(), false, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS) });
        InsertResponse updateR = client.execute(new UpdateRequest(EnumAPI.OUTLOOK, folder));
        folder.setLastModified(updateR.getTimestamp());
    }

    @Test
    public void testSharedType() throws Throwable {
        ListResponse response = client2.execute(new ListRequest(EnumAPI.OUTLOOK, FolderObject.SHARED_PREFIX + userId1, new int[] { 1, 20, 2, 3, 300, 301, 302, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316 }, false));
        Iterator<FolderObject> iter = response.getFolder();
        while (iter.hasNext()) {
            FolderObject testFolder = iter.next();
            if (testFolder.getObjectID() == folder.getObjectID()) {
                assertEquals("Shared folder is sent with type private.", FolderObject.SHARED, testFolder.getType());
            }
        }
    }
}
