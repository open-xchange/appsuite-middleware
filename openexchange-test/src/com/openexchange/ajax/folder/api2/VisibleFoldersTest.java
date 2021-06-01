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
import static org.junit.Assert.assertTrue;
import java.util.Iterator;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.VisibleFoldersRequest;
import com.openexchange.ajax.folder.actions.VisibleFoldersResponse;
import com.openexchange.ajax.framework.Abstrac2UserAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.common.test.PermissionTools;

/**
 * {@link VisibleFoldersTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class VisibleFoldersTest extends Abstrac2UserAJAXSession {

    private FolderObject createdPrivateFolder;
    private FolderObject createdPublicFolder;
    private FolderObject createdSharedFolder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        {
            createdPrivateFolder = new FolderObject();
            createdPrivateFolder.setModule(FolderObject.CALENDAR);
            createdPrivateFolder.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
            createdPrivateFolder.setType(FolderObject.PRIVATE);
            createdPrivateFolder.setPermissions(PermissionTools.P(I(client1.getValues().getUserId()), PermissionTools.ADMIN));
            createdPrivateFolder.setFolderName("testPrivateCalendarFolder" + System.currentTimeMillis());
            final InsertRequest iReq = new InsertRequest(EnumAPI.OUTLOOK, createdPrivateFolder);
            final InsertResponse iResp = getClient().execute(iReq);
            iResp.fillObject(createdPrivateFolder);
        }

        {
            createdPublicFolder = new FolderObject();
            createdPublicFolder.setModule(FolderObject.CALENDAR);
            createdPublicFolder.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
            createdPublicFolder.setType(FolderObject.PUBLIC);
            createdPublicFolder.setPermissions(PermissionTools.P(I(client1.getValues().getUserId()), PermissionTools.ADMIN));
            createdPublicFolder.setFolderName("testPublicCalendarFolder" + System.currentTimeMillis());
            final InsertRequest iReq = new InsertRequest(EnumAPI.OUTLOOK, createdPublicFolder);
            final InsertResponse iResp = getClient().execute(iReq);
            iResp.fillObject(createdPublicFolder);
        }

        {
            createdSharedFolder = new FolderObject();
            createdSharedFolder.setModule(FolderObject.CALENDAR);
            createdSharedFolder.setParentFolderID(client2.getValues().getPrivateAppointmentFolder());
            createdSharedFolder.setType(FolderObject.PRIVATE);
            createdSharedFolder.setPermissions(PermissionTools.P(I(client2.getValues().getUserId()), PermissionTools.ADMIN, I(client1.getValues().getUserId()), "arawada"));
            createdSharedFolder.setFolderName("testSharedCalendarFolder" + System.currentTimeMillis());
            final InsertRequest iReq = new InsertRequest(EnumAPI.OUTLOOK, createdSharedFolder);
            final InsertResponse iResp = client2.execute(iReq);
            iResp.fillObject(createdSharedFolder);
        }

    }

    @Test
    public void testForVisibleFolders() throws Throwable {
        final VisibleFoldersRequest req = new VisibleFoldersRequest(EnumAPI.OUTLOOK, "calendar");
        final VisibleFoldersResponse resp = getClient().execute(req);
        /*
         * Iterate private folder and look-up previously created private folder
         */
        {
            final Iterator<FolderObject> privIter = resp.getPrivateFolders();
            boolean found = false;
            while (privIter.hasNext()) {
                final FolderObject privateFolder = privIter.next();
                if (privateFolder.getObjectID() == createdPrivateFolder.getObjectID()) {
                    found = true;
                    break;
                }
            }
            assertTrue("Previously created private folder not found in private folders.", found);
        }
        /*
         * Iterate public folder and look-up previously created public folder
         */
        {
            final Iterator<FolderObject> pubIter = resp.getPublicFolders();
            boolean found = false;
            while (pubIter.hasNext()) {
                final FolderObject publicFolder = pubIter.next();
                if (publicFolder.getObjectID() == createdPublicFolder.getObjectID()) {
                    found = true;
                    break;
                }
            }
            assertTrue("Previously created public folder not found in public folders.", found);
        }
        /*
         * Iterate shared folder and look-up previously created shared folder
         */
        {
            final Iterator<FolderObject> sharedIter = resp.getSharedFolders();
            boolean found = false;
            while (sharedIter.hasNext()) {
                final FolderObject sharedFolder = sharedIter.next();
                if (sharedFolder.getObjectID() == createdSharedFolder.getObjectID()) {
                    found = true;
                    break;
                }
            }
            assertTrue("Previously created shared folder not found in shared folders.", found);
        }
    }

    @Test
    public void testFindingGlobalAddressbook() throws Exception {
        final VisibleFoldersRequest req = new VisibleFoldersRequest(EnumAPI.OUTLOOK, "contacts");
        final VisibleFoldersResponse resp = getClient().execute(req);
        final Iterator<FolderObject> publicFolders = resp.getPublicFolders();

        boolean globalAddressBookFound = false;
        while (publicFolders.hasNext()) {
            if (publicFolders.next().getObjectID() == FolderObject.SYSTEM_LDAP_FOLDER_ID) {
                globalAddressBookFound = true;
            }
        }

        assertTrue("Could not find the global address book", globalAddressBookFound);
    }

}
