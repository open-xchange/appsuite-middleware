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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.Abstrac2UserAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link PublicFolderMovePermissionTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.4
 */
public class PublicFolderMovePermissionTest extends Abstrac2UserAJAXSession {

    private FolderObject folder;
    private FolderObject toMove;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        OCLPermission permission = new OCLPermission();
        permission.setEntity(client2.getValues().getUserId());
        permission.setFolderAdmin(false);
        permission.setGroupPermission(false);
        permission.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_OWN_OBJECTS, OCLPermission.NO_PERMISSIONS);

        folder = new FolderObject();
        folder.setFolderName(UUID.randomUUID().toString());
        folder.setParentFolderID(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID);
        folder.setModule(FolderObject.INFOSTORE);
        folder.setType(FolderObject.PUBLIC);
        OCLPermission perm1 = new OCLPermission();
        perm1.setEntity(client1.getValues().getUserId());
        perm1.setFolderAdmin(true);
        perm1.setGroupPermission(false);
        perm1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        folder.setPermissionsAsArray(new OCLPermission[] {perm1, permission});
        InsertRequest req = new InsertRequest(EnumAPI.OX_NEW, folder);
        InsertResponse resp = client1.execute(req);
        resp.fillObject(folder);

        toMove = new FolderObject();
        toMove.setFolderName(UUID.randomUUID().toString());
        toMove.setParentFolderID(client1.getValues().getPrivateInfostoreFolder());
        toMove.setModule(FolderObject.INFOSTORE);
        toMove.setType(FolderObject.PUBLIC);
        OCLPermission perm2 = new OCLPermission();
        perm2.setEntity(client1.getValues().getUserId());
        perm2.setFolderAdmin(true);
        perm2.setGroupPermission(false);
        perm2.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        toMove.setPermissionsAsArray(new OCLPermission[] {perm2});
        InsertRequest req2 = new InsertRequest(EnumAPI.OX_NEW, toMove);
        InsertResponse resp2 = client1.execute(req2);
        resp2.fillObject(toMove);
    }

    @Test
    public void testPermissionOnMoveIntoPublicFolder() throws Exception {
        toMove.setParentFolderID(folder.getObjectID());
        UpdateRequest updateReq = new UpdateRequest(EnumAPI.OX_NEW, toMove);
        updateReq.setIgnorePermission(true);
        InsertResponse response = client1.execute(updateReq);
        assertNotNull("No folder id in update response", response.getData());
        assertNull("Update request ended with exception", response.getException());

        GetRequest parentGet = new GetRequest(EnumAPI.OX_NEW, folder.getObjectID());
        GetResponse parentResp = client1.execute(parentGet);
        FolderObject parent = parentResp.getFolder();
        GetRequest getReq = new GetRequest(EnumAPI.OX_NEW, toMove.getObjectID());
        GetResponse getResp = client1.execute(getReq);
        FolderObject f = getResp.getFolder();
        Assert.assertNotNull(f);
        List<OCLPermission> perms = f.getPermissions();
        for (OCLPermission parentPerm : parent.getPermissions()) {
            assertThat("Permissions were not equal", comparePermissions(perms, parentPerm));
        }
    }

    @Test
    public void testPermissionOnMoveIntoPublicFolderWithSubfolders() throws Exception {
        FolderObject sub = new FolderObject();
        sub.setFolderName(UUID.randomUUID().toString());
        sub.setParentFolderID(toMove.getObjectID());
        sub.setModule(FolderObject.INFOSTORE);
        sub.setType(FolderObject.PUBLIC);
        InsertRequest req = new InsertRequest(EnumAPI.OX_NEW, sub);
        InsertResponse resp = client1.execute(req);
        resp.fillObject(sub);

        GetRequest get = new GetRequest(EnumAPI.OX_NEW, toMove.getObjectID());
        GetResponse get2 = client1.execute(get);
        toMove = get2.getFolder();
        toMove.setLastModified(new Date());
        toMove.setParentFolderID(folder.getObjectID());
        UpdateRequest updateReq = new UpdateRequest(EnumAPI.OX_NEW, toMove);
        updateReq.setIgnorePermission(true);
        InsertResponse response = client1.execute(updateReq);
        assertNotNull("No folder id in update response", response.getData());
        assertNull("Update request ended with exception", response.getException());

        GetRequest parentGet = new GetRequest(EnumAPI.OX_NEW, folder.getObjectID());
        GetResponse parentResp = client1.execute(parentGet);
        FolderObject parent = parentResp.getFolder();
        GetRequest getReq = new GetRequest(EnumAPI.OX_NEW, sub.getObjectID());
        GetResponse getResp = client1.execute(getReq);
        FolderObject f = getResp.getFolder();
        Assert.assertNotNull(f);
        List<OCLPermission> perms = f.getPermissions();
        for (OCLPermission parentPerm : parent.getPermissions()) {
            assertThat("Permissions were not equal", comparePermissions(perms, parentPerm));
        }
    }

    private boolean comparePermissions(List<OCLPermission> parent, OCLPermission p) {
        // Debug logging
        System.out.println("PublicFolderMovePermissionTest start");
        System.out.println("Parent permissions:");
        for (OCLPermission p2 : parent) {
            // Debug logging
            System.out.println(p2);
            if ((p2.getEntity() == p.getEntity()) &&
            (p2.getFolderPermission() == p.getFolderPermission()) &&
            (p2.getReadPermission() == p.getReadPermission()) &&
            (p2.getWritePermission() == p.getWritePermission()) &&
            (p2.getDeletePermission() == p.getDeletePermission()) &&
            (p2.isFolderAdmin() == p.isFolderAdmin()) &&
            (p2.isSystem() == p.isSystem())) {
                return true;
            }
        }

        // Debug logging
        System.out.println("Permission:");
        System.out.println(p);
        System.out.println("PublicFolderMovePermissionTest end");
        return false;
    }

}
