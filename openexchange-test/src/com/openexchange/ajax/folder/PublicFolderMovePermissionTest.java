/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax.folder;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link PublicFolderMovePermissionTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.4
 */
public class PublicFolderMovePermissionTest extends AbstractAJAXSession {

    private AJAXClient client;
    private AJAXClient client2;
    private FolderObject folder;
    private FolderObject toMove;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        client2 = getClient2();

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
        perm1.setEntity(client.getValues().getUserId());
        perm1.setFolderAdmin(true);
        perm1.setGroupPermission(false);
        perm1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        folder.setPermissionsAsArray(new OCLPermission[] {perm1, permission});
        InsertRequest req = new InsertRequest(EnumAPI.OX_NEW, folder);
        InsertResponse resp = client.execute(req);
        resp.fillObject(folder);

        toMove = new FolderObject();
        toMove.setFolderName(UUID.randomUUID().toString());
        toMove.setParentFolderID(client.getValues().getPrivateInfostoreFolder());
        toMove.setModule(FolderObject.INFOSTORE);
        toMove.setType(FolderObject.PUBLIC);
        OCLPermission perm2 = new OCLPermission();
        perm2.setEntity(client.getValues().getUserId());
        perm2.setFolderAdmin(true);
        perm2.setGroupPermission(false);
        perm2.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        toMove.setPermissionsAsArray(new OCLPermission[] {perm2});
        InsertRequest req2 = new InsertRequest(EnumAPI.OX_NEW, toMove);
        InsertResponse resp2 = client.execute(req2);
        resp2.fillObject(toMove);
    }

    @Override
    public void tearDown() throws Exception {
        if (null != folder) {
            DeleteRequest req = new DeleteRequest(EnumAPI.OX_NEW, folder);
            client.execute(req);
        }
        super.tearDown();
    }

    @Test
    public void testPermissionOnMoveIntoPublicFolder() throws Exception {
        toMove.setParentFolderID(folder.getObjectID());
        UpdateRequest updateReq = new UpdateRequest(EnumAPI.OX_NEW, toMove);
        updateReq.setIgnorePermission(true);
        client.execute(updateReq);

        GetRequest parentGet = new GetRequest(EnumAPI.OX_NEW, folder.getObjectID());
        GetResponse parentResp = client.execute(parentGet);
        FolderObject parent = parentResp.getFolder();
        GetRequest getReq = new GetRequest(EnumAPI.OX_NEW, toMove.getObjectID());
        GetResponse getResp = client.execute(getReq);
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
        InsertResponse resp = client.execute(req);
        resp.fillObject(sub);

        GetRequest get = new GetRequest(EnumAPI.OX_NEW, toMove.getObjectID());
        GetResponse get2 = client.execute(get);
        toMove = get2.getFolder();
        toMove.setLastModified(new Date());
        toMove.setParentFolderID(folder.getObjectID());
        UpdateRequest updateReq = new UpdateRequest(EnumAPI.OX_NEW, toMove);
        updateReq.setIgnorePermission(true);
        client.execute(updateReq);

        GetRequest parentGet = new GetRequest(EnumAPI.OX_NEW, folder.getObjectID());
        GetResponse parentResp = client.execute(parentGet);
        FolderObject parent = parentResp.getFolder();
        GetRequest getReq = new GetRequest(EnumAPI.OX_NEW, sub.getObjectID());
        GetResponse getResp = client.execute(getReq);
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
