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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * Title of Bug: Lost permission on infostore folder
 *
 * Description of Bug: If you remove group 0 as view permission from your infostore folder all
 * subfolders of it lose all their permission in database.
 * ...
 * Steps to Reproduce:
 * 1. Create some folder below your private infostore folder.
 * 2. Give the group 0 view permission on your private infostore folder.
 * 3. Remove that view permission again.
 *
 * Actual Results:
 * All permissions for the subfolders have been deleted.
 *
 * Expected Results:
 * Not deleted permissions on subfolders.
 *
 * This test creates two infostore folders and sets permissions as follows:
 * One user is given permission to see the subfolder, all users are given permission to see the parent
 * folder. Then the latter permission is removed. The first permission is asserted to be still there.
 *
 * @author <a href="mailto:karsten.will@open-xchange.org">Karsten Will</a>
 */
public class Bug12393Test extends AbstractAJAXSession {

    FolderObject subFolderObject;
    FolderObject parentFolderObject;
    int subFolderId;
    int parentFolderId;
    List<OCLPermission> originalSubFolderPermissions;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        final AJAXClient myClient = getClient();
        //create the parent folder
        parentFolderObject = Create.setupPublicFolder("Bug12393Test-parentFolder", FolderObject.INFOSTORE, myClient.getValues().getUserId());
        parentFolderObject.setParentFolderID(myClient.getValues().getPrivateInfostoreFolder());
        InsertRequest request = new InsertRequest(EnumAPI.OX_OLD, parentFolderObject);
        CommonInsertResponse response = myClient.execute(request);
        response.fillObject(parentFolderObject);
        parentFolderId = parentFolderObject.getObjectID();
        //create the subfolder
        subFolderObject = new FolderObject();
        subFolderObject.setFolderName("Bug12393Test-subFolder");
        subFolderObject.setType(FolderObject.PUBLIC);
        subFolderObject.setParentFolderID(parentFolderId);
        subFolderObject.setModule(FolderObject.INFOSTORE);
        // permission for the first user
        final OCLPermission perm = new OCLPermission();
        perm.setEntity(myClient.getValues().getUserId());
        perm.setGroupPermission(false);
        perm.setFolderAdmin(true);
        perm.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        final int userId2 = testUser2.getUserId();
        final OCLPermission perm2 = new OCLPermission();
        perm2.setEntity(userId2);
        perm2.setGroupPermission(false);
        perm2.setFolderAdmin(true);
        perm2.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        subFolderObject.setPermissionsAsArray(new OCLPermission[] { perm, perm2 });
        originalSubFolderPermissions = subFolderObject.getPermissions();
        request = new InsertRequest(EnumAPI.OX_OLD, subFolderObject);
        response = myClient.execute(request);
        response.fillObject(subFolderObject);
        subFolderId = subFolderObject.getObjectID();
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testPermissionsOfSubfoldersRemainIntactAfterRemovalOfFolderGroupPermission() throws Exception {
        final AJAXClient myClient = getClient();
        // reload the parent folder (it has been changed since its creation by the addition of the subfolder)
        GetRequest getRequest = new GetRequest(EnumAPI.OX_OLD, Integer.toString(parentFolderId), FolderObject.ALL_COLUMNS, false);
        GetResponse getResponse = myClient.execute(getRequest);
        parentFolderObject = getResponse.getFolder();
        // lastModified has to be set separately
        parentFolderObject.setLastModified(getResponse.getTimestamp());
        // Set the permission on the parent folder to current user only (thereby effectively deleting the group permission)
        final OCLPermission perm = new OCLPermission();
        perm.setEntity(myClient.getValues().getUserId());
        perm.setGroupPermission(false);
        perm.setFolderAdmin(true);
        perm.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        parentFolderObject.setPermissionsAsArray(new OCLPermission[] { perm });
        // update the parent folder
        final UpdateRequest updateRequest = new UpdateRequest(EnumAPI.OX_OLD, parentFolderObject);
        myClient.execute(updateRequest);
        // load the subfolder
        getRequest = new GetRequest(EnumAPI.OX_OLD, Integer.toString(subFolderId), false);
        getResponse = myClient.execute(getRequest);
        final FolderObject newSubFolderObject = getResponse.getFolder();
        // assert the permission there is still intact
        final Set<String> expectedSet = new HashSet<String>();
        for (final OCLPermission permission : originalSubFolderPermissions) {
            expectedSet.add(permission.toString());
        }
        final Set<String> isSet = new HashSet<String>();
        for (final OCLPermission permission : newSubFolderObject.getPermissions()) {
            isSet.add(permission.toString());
        }

        assertEquals("The permissions of the subfolder have changed. Differing size. ", expectedSet.size(), isSet.size());

        expectedSet.removeAll(isSet);
        assertTrue("The permissions of the subfolder have changed: " + expectedSet.toString(), expectedSet.isEmpty());
    }
}
