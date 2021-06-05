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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.Iterator;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.framework.Abstrac2UserAJAXSession;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.common.test.PermissionTools;

/**
 * {@link Bug16303Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug16303Test extends Abstrac2UserAJAXSession {

    private FolderObject createdFolder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        createdFolder = new FolderObject();
        createdFolder.setModule(FolderObject.CALENDAR);
        createdFolder.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        createdFolder.setPermissions(PermissionTools.P(I(client1.getValues().getUserId()), PermissionTools.ADMIN, I(client2.getValues().getUserId()), "arawada"));
        createdFolder.setFolderName("testFolder4Bug16303");
        ftm.insertFolderOnServer(createdFolder);
        // Unfortunately no timestamp when creating a mail folder through Outlook folder tree.
        createdFolder.setLastModified(new Date());

        // Init some caching with other user
        ListRequest listRequest = new ListRequest(EnumAPI.OUTLOOK, FolderStorage.SHARED_ID);
        ListResponse listResponse = client2.execute(listRequest);
        String expectedId = FolderObject.SHARED_PREFIX + client1.getValues().getUserId();
        Iterator<FolderObject> iter = listResponse.getFolder();
        FolderObject foundUserShared = null;
        while (iter.hasNext()) {
            FolderObject folder = iter.next();
            if (expectedId.equals(folder.getFullName())) {
                foundUserShared = folder;
            }
        }
        assertNotNull("Expected user named shared folder below root shared folder.", foundUserShared);

        ListRequest listRequest2 = new ListRequest(EnumAPI.OUTLOOK, foundUserShared.getFullName());
        listResponse = client2.execute(listRequest2);
        iter = listResponse.getFolder();
        FolderObject foundShared = null;
        while (iter.hasNext()) {
            FolderObject folder = iter.next();
            if (folder.getObjectID() == folder.getObjectID()) {
                foundShared = folder;
            }
        }
        assertNotNull("Shared folder expected below shared parent folder.", foundShared);
    }

    @Test
    public void testForDisappearingFolder() throws Throwable {
        GetRequest request = new GetRequest(EnumAPI.OUTLOOK, client1.getValues().getPrivateAppointmentFolder());
        GetResponse response = client1.execute(request);
        FolderObject testFolder = response.getFolder();
        assertTrue("Private appointment folder must have subfolder flag true.", testFolder.hasSubfolders());
    }
}
