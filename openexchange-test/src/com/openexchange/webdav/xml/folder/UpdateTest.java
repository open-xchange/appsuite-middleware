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

package com.openexchange.webdav.xml.folder;

import java.util.Date;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.GroupUserTest;

public class UpdateTest extends FolderTest {

    public UpdateTest(final String name) {
        super(name);
    }

    public void testRenameFolder() throws Throwable {
        FolderObject folderObj = createFolderObject(userId, "testInsertRenameFolder", FolderObject.TASK, true);
        int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password, context);

        folderObj = new FolderObject();
        folderObj.setFolderName("testRenameFolder" + System.currentTimeMillis());
        folderObj.setObjectID(objectId);
        folderObj.setParentFolderID(2);
        updateFolder(webCon, folderObj, PROTOCOL + hostName, login, password, context);

        FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password, context);
        compareFolder(folderObj, loadFolder);
    }

    public void testMoveFolder() throws Exception {
        FolderObject folderObj = createFolderObject(userId, "testMoveFolder1", FolderObject.TASK, true);
        final int parentFolderId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password, context);

        folderObj = createFolderObject(userId, "testMoveFolder2", FolderObject.TASK, true);
        final int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password, context);
        folderObj.setObjectID(objectId);
        folderObj.setParentFolderID(parentFolderId);
        updateFolder(webCon, folderObj, PROTOCOL + hostName, login, password, context);

        final FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password, context);
        compareFolder(folderObj, loadFolder);
    }

    public void testChangePermissionsOfPrivateFolder() throws Exception {
        final int userParticipantId = GroupUserTest.searchUser(webCon, userParticipant2, new Date(0), PROTOCOL + hostName, login, password, context)[0].getInternalUserId();
        final int groupParticipantId = GroupUserTest.searchGroup(webCon, groupParticipant, new Date(0), PROTOCOL + hostName, login, password, context)[0].getIdentifier();

        final FolderObject folderObj = createFolderObject(userId, "testChangePermissionOfPrivateFolder", FolderObject.TASK, false);
        final int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password, context);
        folderObj.setObjectID(objectId);

        final OCLPermission oclp[] = new OCLPermission[3];
        oclp[0] = createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, true);
        oclp[1] = createPermission( userParticipantId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_OWN_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, false);
        oclp[2] = createPermission( groupParticipantId, true, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, false);

        folderObj.setPermissionsAsArray( oclp );

        updateFolder(webCon, folderObj, PROTOCOL + hostName, login, password, context);

        final FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password, context);
        compareFolder(folderObj, loadFolder);
    }

    public void testChangePermissionsOfPublicFolder() throws Exception {
        final int userParticipantId = GroupUserTest.searchUser(webCon, userParticipant2, new Date(0), PROTOCOL + hostName, login, password, context)[0].getInternalUserId();
        final int groupParticipantId = GroupUserTest.searchGroup(webCon, groupParticipant, new Date(0), PROTOCOL + hostName, login, password, context)[0].getIdentifier();

        final FolderObject folderObj = createFolderObject(userId, "testChangePermissionOfPublicFolder", FolderObject.TASK, true);
        final int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password, context);
        folderObj.setObjectID(objectId);

        final OCLPermission oclp[] = new OCLPermission[3];
        oclp[0] = createPermission( userId, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp[1] = createPermission( userParticipantId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_OWN_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS);
        oclp[2] = createPermission( groupParticipantId, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);

        folderObj.setPermissionsAsArray( oclp );

        updateFolder(webCon, folderObj, PROTOCOL + hostName, login, password, context);

        final FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password, context);
        compareFolder(folderObj, loadFolder);
    }
}
