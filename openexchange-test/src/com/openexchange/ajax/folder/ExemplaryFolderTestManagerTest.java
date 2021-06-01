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
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * This class contains some examples of tests created for FolderTestManager
 *
 * @author <a href="mailto:karsten.will@open-xchange.org">Karsten Will</a>
 */
public class ExemplaryFolderTestManagerTest extends AbstractAJAXSession {

    private FolderObject folderObject1;
    private FolderObject folderObject2;

    public ExemplaryFolderTestManagerTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        AJAXClient client = getClient();
        // create a folder
        folderObject1 = new FolderObject();
        folderObject1.setFolderName("ExemplaryFolderTestManagerTest-folder1" + System.currentTimeMillis());
        folderObject1.setType(FolderObject.PUBLIC);
        folderObject1.setParentFolderID(client.getValues().getPrivateInfostoreFolder());
        folderObject1.setModule(FolderObject.INFOSTORE);
        // create permissions
        final OCLPermission perm1 = new OCLPermission();
        perm1.setEntity(client.getValues().getUserId());
        perm1.setGroupPermission(false);
        perm1.setFolderAdmin(true);
        perm1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        folderObject1.setPermissionsAsArray(new OCLPermission[] { perm1 });
        ftm.insertFolderOnServer(folderObject1);

        // create another folder
        folderObject2 = new FolderObject();
        folderObject2.setFolderName("ExemplaryFolderTestManagerTest-folder2" + System.currentTimeMillis());
        folderObject2.setType(FolderObject.PUBLIC);
        folderObject2.setParentFolderID(client.getValues().getPrivateInfostoreFolder());
        folderObject2.setModule(FolderObject.INFOSTORE);
        // create permissions
        final OCLPermission perm2 = new OCLPermission();
        perm2.setEntity(client.getValues().getUserId());
        perm2.setGroupPermission(false);
        perm2.setFolderAdmin(true);
        perm2.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        folderObject2.setPermissionsAsArray(new OCLPermission[] { perm2 });
        ftm.insertFolderOnServer(folderObject2);
    }

    @Test
    public void testCreatedFoldersAreReturnedByGetRequest() {
        final FolderObject fo = ftm.getFolderFromServer(folderObject1.getObjectID());
        assertEquals("The folder was not returned.", fo.getFolderName(), folderObject1.getFolderName());
    }

    @Test
    public void testCreatedFoldersAppearInListRequest() throws Exception {
        boolean found1 = false;
        boolean found2 = false;
        final FolderObject[] allFolders = ftm.listFoldersOnServer(getClient().getValues().getPrivateInfostoreFolder());
        for (int i = 0; i < allFolders.length; i++) {
            final FolderObject fo = allFolders[i];
            if (fo.getObjectID() == folderObject1.getObjectID()) {
                found1 = true;
            }
            if (fo.getObjectID() == folderObject2.getObjectID()) {
                found2 = true;
            }
        }
        assertTrue("First folder was not found.", found1);
        assertTrue("Second folder was not found.", found2);
    }

    @Test
    public void testCreatedFoldersAppearAsUpdatedSinceYesterday() {
        boolean found1 = false;
        boolean found2 = false;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        final FolderObject[] allFolders = ftm.getUpdatedFoldersOnServer(cal.getTime());
        for (int i = 0; i < allFolders.length; i++) {
            final FolderObject co = allFolders[i];
            if (co.getObjectID() == folderObject1.getObjectID()) {
                found1 = true;
            }
            if (co.getObjectID() == folderObject2.getObjectID()) {
                found2 = true;
            }
        }
        assertTrue("First folder was not found.", found1);
        assertTrue("Second folder was not found.", found2);
    }
}
