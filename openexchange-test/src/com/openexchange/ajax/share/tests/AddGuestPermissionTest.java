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

package com.openexchange.ajax.share.tests;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.notification.ShareNotificationService.Transport;

/**
 * {@link AddGuestPermissionTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AddGuestPermissionTest extends ShareTest {

    /**
     * Initializes a new {@link AddGuestPermissionTest}.
     *
     * @param name The test name
     */
    public AddGuestPermissionTest(String name) {
        super(name);
    }

    public void testUpdateSharedFolderRandomly() throws Exception {
        int module = randomModule();
        testUpdateSharedFolder(randomFolderAPI(), module, randomGuestPermission(module));
    }

    public void noTestUpdateSharedFolderExtensively() throws Exception {
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            for (OCLGuestPermission guestPermission : TESTED_PERMISSIONS) {
                for (int module : TESTED_MODULES) {
                    testUpdateSharedFolder(api, module, guestPermission);
                }
            }
        }
    }

    public void testUpdateSharedFileRandomly() throws Exception {
        testUpdateSharedFile(randomFolderAPI(), randomGuestObjectPermission());
    }

    public void noTestUpdateSharedFileExtensively() throws Exception {
        for (FileStorageGuestObjectPermission guestPermission : TESTED_OBJECT_PERMISSIONS) {
            testUpdateSharedFile(randomFolderAPI(), guestPermission);
        }
    }

    public void testUpdateSharedFolderWithCascadingPermissionsRandomly() throws Exception {
        int module = randomModule();
        testUpdateSharedFolderWithCascadingPermissions(randomFolderAPI(), module, getDefaultFolder(module), randomGuestPermission(module));
    }

    public void noTestUpdateSharedFolderWithCascadingPermissionsExtensively() throws Exception {
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            for (OCLGuestPermission guestPermission : TESTED_PERMISSIONS) {
                for (int module : TESTED_MODULES) {
                    testUpdateSharedFolderWithCascadingPermissions(api, module, getDefaultFolder(module), guestPermission);
                }
            }
        }
    }

    private void testUpdateSharedFolder(EnumAPI api, int module, OCLGuestPermission guestPermission) throws Exception {
        testUpdateSharedFolder(api, module, getDefaultFolder(module), guestPermission);
    }

    private void testUpdateSharedFolder(EnumAPI api, int module, int parent, OCLGuestPermission guestPermission) throws Exception {
        /*
         * create private folder
         */
        FolderObject folder = insertPrivateFolder(api, module, parent);
        /*
         * update folder, add permission for guest
         */
        folder.addPermission(guestPermission);
        folder = updateFolder(api, folder);
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission permission : folder.getPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(api, module, folder.getObjectID(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(guest, guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
    }

    private void testUpdateSharedFolderWithCascadingPermissions(EnumAPI api, int module, int parent, OCLGuestPermission guestPermission) throws Exception {
        /*
         * create folder hierarchy
         */
        FolderObject rootFolder = insertPrivateFolder(api, module, parent, "Root_" + randomUID());
        FolderObject subLevel1 = insertPrivateFolder(api, module, rootFolder.getObjectID(), "Sub1" + randomUID());
        FolderObject subLevel2 = insertPrivateFolder(api, module, subLevel1.getObjectID(), "Sub2" + randomUID());
        /*
         * update root folder, add permission for guest
         */
        Date clientLastModified = subLevel2.getLastModified();
        rootFolder.addPermission(guestPermission);
        rootFolder.setLastModified(clientLastModified);
        rootFolder = updateFolder(api, rootFolder, new RequestCustomizer<UpdateRequest>() {
            @Override
            public void customize(UpdateRequest request) {
                request.setCascadePermissions(true);
                request.setNotifyPermissionEntities(Transport.MAIL);
            }
        });
        /*
         * Reload subfolders
         */
        subLevel1 = getFolder(api, subLevel1.getObjectID());
        subLevel2 = getFolder(api, subLevel2.getObjectID());
        /*
         * check permissions
         */
        OCLPermission matchingRootPermission = null;
        for (FolderObject folder : new FolderObject[] { rootFolder, subLevel1, subLevel2 }) {
            OCLPermission matchingPermission = null;
            List<OCLPermission> permissions = folder.getPermissions();
            assertNotNull("No permissions fround for folder " + folder.getObjectID(), permissions);
            assertEquals("Wrong number of permissions on folder " + folder.getObjectID(), 2, permissions.size());
            for (OCLPermission permission : permissions) {
                if (permission.getEntity() != client.getValues().getUserId()) {
                    matchingPermission = permission;
                    break;
                }
            }
            assertNotNull("No matching permission in created folder found", matchingPermission);
            checkPermissions(guestPermission, matchingPermission);
            if (folder == rootFolder) {
                matchingRootPermission = matchingPermission;
            } else {
                assertEquals("Unexpected permission entity for subfolder " + folder.getObjectID(), matchingRootPermission.getEntity(), matchingPermission.getEntity());
                assertEquals("Unexpected permission bits for subfolder " + folder.getObjectID(), matchingRootPermission.getReadPermission(), matchingPermission.getReadPermission());
                assertEquals("Unexpected permission bits for subfolder " + folder.getObjectID(), matchingRootPermission.getWritePermission(), matchingPermission.getWritePermission());
                assertEquals("Unexpected permission bits for subfolder " + folder.getObjectID(), matchingRootPermission.getDeletePermission(), matchingPermission.getDeletePermission());
                assertEquals("Unexpected permission bits for subfolder " + folder.getObjectID(), matchingRootPermission.getFolderPermission(), matchingPermission.getFolderPermission());
                assertEquals("Unexpected permission bits for subfolder " + folder.getObjectID(), matchingRootPermission.getSystem(), matchingPermission.getSystem());
                assertEquals("Unexpected permission bits for subfolder " + folder.getObjectID(), matchingRootPermission.isFolderAdmin(), matchingPermission.isFolderAdmin());
                assertEquals("Unexpected permission bits for subfolder " + folder.getObjectID(), matchingRootPermission.isGroupPermission(), matchingPermission.isGroupPermission());
            }
        }
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(api, module, rootFolder.getObjectID(), matchingRootPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(guest, guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
    }

    private void testUpdateSharedFile(EnumAPI api, FileStorageGuestObjectPermission guestPermission) throws Exception {
        testUpdateSharedFile(api, getDefaultFolder(FolderObject.INFOSTORE), guestPermission);
    }

    private void testUpdateSharedFile(EnumAPI api, int parent, FileStorageGuestObjectPermission guestPermission) throws Exception {
        /*
         * create folder and a file inside
         */
        FolderObject folder = insertPrivateFolder(api, FolderObject.INFOSTORE, parent);
        File file = insertFile(folder.getObjectID());
        /*
         * update file, add permissions for guest
         */
        file.setObjectPermissions(Collections.<FileStorageObjectPermission>singletonList(guestPermission));
        file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });
        /*
         * check permissions
         */
        FileStorageObjectPermission matchingPermission = null;
        for (FileStorageObjectPermission permission : file.getObjectPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created file found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(file.getFolderId(), file.getId(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        /*
         * check access to share
         */
        GuestClient guestClient =  resolveShare(guest, guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
    }

}
