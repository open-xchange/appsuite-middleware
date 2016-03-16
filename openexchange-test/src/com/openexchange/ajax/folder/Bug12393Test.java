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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

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

    public Bug12393Test (final String name) {
        super(name);
    }

    @Override
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
        perm.setAllPermission(
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION);
        final AJAXClient client2 = new AJAXClient(User.User2);
        final int userId2 = client2.getValues().getUserId();
        client2.logout();
        final OCLPermission perm2 = new OCLPermission();
        perm2.setEntity(userId2);
        perm2.setGroupPermission(false);
        perm2.setFolderAdmin(true);
        perm2.setAllPermission(
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION);
        subFolderObject.setPermissionsAsArray(new OCLPermission[] { perm, perm2 });
        originalSubFolderPermissions = subFolderObject.getPermissions();
        request = new InsertRequest(EnumAPI.OX_OLD, subFolderObject);
        response = myClient.execute(request);
        response.fillObject(subFolderObject);
        subFolderId = subFolderObject.getObjectID();
    }

    @Override
    public void tearDown() throws Exception {
        final AJAXClient myClient = getClient();
        // reload the parent folder (it has been changed since its creation by the modification of permissions)
        final GetRequest getRequest = new GetRequest(EnumAPI.OX_OLD, Integer.toString(parentFolderId), FolderObject.ALL_COLUMNS, false);
        final GetResponse getResponse = myClient.execute(getRequest);
        parentFolderObject = getResponse.getFolder();
        // lastModified has to be set separately
        parentFolderObject.setLastModified(getResponse.getTimestamp());
        //delete the parent folder and with it the subfolder
        final com.openexchange.ajax.folder.actions.DeleteRequest folderDeleteRequest  = new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD, parentFolderObject);
        myClient.execute(folderDeleteRequest);

        super.tearDown();
    }

    public void testPermissionsOfSubfoldersRemainIntactAfterRemovalOfFolderGroupPermission() throws Exception{
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
        perm.setAllPermission(
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION);
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
