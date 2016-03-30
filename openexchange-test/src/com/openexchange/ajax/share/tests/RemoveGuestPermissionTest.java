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
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.infostore.actions.DeleteInfostoreRequest;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.ResolveShareResponse;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link RemoveGuestPermissionTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RemoveGuestPermissionTest extends ShareTest {

    /**
     * Initializes a new {@link RemoveGuestPermissionTest}.
     *
     * @param name The test name
     */
    public RemoveGuestPermissionTest(String name) {
        super(name);
    }

    public void testUpdateSharedFolderRandomly() throws Exception {
        int module = randomModule();
        testUpdateSharedFolder(randomFolderAPI(), module, randomGuestPermission(module));
    }

    public void testDeleteSharedFolderRandomly() throws Exception {
        int module = randomModule();
        testDeleteSharedFolder(randomFolderAPI(), module, getDefaultFolder(module), randomGuestPermission(module), false);
    }

    public void testHardDeleteSharedFolderRandomly() throws Exception {
        int module = randomModule();
        testDeleteSharedFolder(randomFolderAPI(), module, getDefaultFolder(module), randomGuestPermission(module), true);
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

    public void testDeleteSharedFileRandomly() throws Exception {
        testDeleteSharedFile(randomFolderAPI(), getDefaultFolder(FolderObject.INFOSTORE), randomGuestObjectPermission(), false);
    }

    public void testHardDeleteSharedFileRandomly() throws Exception {
        testDeleteSharedFile(randomFolderAPI(), getDefaultFolder(FolderObject.INFOSTORE), randomGuestObjectPermission(), true);
    }

    public void noTestUpdateSharedFileExtensively() throws Exception {
        for (FileStorageGuestObjectPermission guestPermission : TESTED_OBJECT_PERMISSIONS) {
            testUpdateSharedFile(randomFolderAPI(), guestPermission);
        }
    }

    private void testUpdateSharedFolder(EnumAPI api, int module, OCLGuestPermission guestPermission) throws Exception {
        testUpdateSharedFolder(api, module, getDefaultFolder(module), guestPermission);
    }

    private void testUpdateSharedFolder(EnumAPI api, int module, int parent, OCLGuestPermission guestPermission) throws Exception {
        /*
         * create folder shared to guest user
         */
        FolderObject folder = insertSharedFolder(api, module, parent, guestPermission);
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
        /*
         * update folder, revoke permissions
         */
        folder.getPermissions().remove(matchingPermission);
        folder = updateFolder(api, folder);
        /*
         * check permissions
         */
        for (OCLPermission permission : folder.getPermissions()) {
            assertTrue("Guest permission still present", permission.getEntity() != matchingPermission.getEntity());
        }
        if (RecipientType.ANONYMOUS.equals(guestPermission.getRecipient().getType())) {
            /*
             * for anonymous guest user, check access with previous guest session (after waiting some time until background operations took place)
             */
            checkGuestUserDeleted(matchingPermission.getEntity());
            guestClient.checkSessionAlive(true);
            /*
             * check if share link still accessible
             */
            GuestClient revokedGuestClient = new GuestClient(guest.getShareURL(), guestPermission.getRecipient(), false);
            ResolveShareResponse shareResolveResponse = revokedGuestClient.getShareResolveResponse();
            assertEquals("Status wrong", ResolveShareResponse.NOT_FOUND, shareResolveResponse.getStatus());
        } else {
            /*
             * check share target no longer accessible for non-anonymous guest user
             */
            guestClient.checkFolderNotAccessible(String.valueOf(folder.getObjectID()));
            guestClient.logout();
        }
        /*
         * check guest entities
         */
        guest = discoverGuestEntity(api, module, folder.getObjectID(), matchingPermission.getEntity());
        assertNull("guest entity still found", guest);
    }

    private void testDeleteSharedFolder(EnumAPI api, int module, int parent, OCLGuestPermission guestPermission, boolean hardDelete) throws Exception {
        /*
         * create folder shared to guest user
         */
        FolderObject folder = insertSharedFolder(api, module, parent, guestPermission);
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
        /*
         * delete folder, thus implicitly revoke permissions
         */
        DeleteRequest deleteRequest = new DeleteRequest(api, folder.getObjectID(), folder.getLastModified());
        if (hardDelete) {
            deleteRequest.setHardDelete(Boolean.TRUE);
        }
        client.execute(deleteRequest);
        if (RecipientType.ANONYMOUS.equals(guestPermission.getRecipient().getType())) {
            /*
             * for anonymous guest user, check access with previous guest session (after waiting some time until background operations took place)
             */
            checkGuestUserDeleted(matchingPermission.getEntity());
            guestClient.checkSessionAlive(true);
            /*
             * check if share link still accessible
             */
            GuestClient revokedGuestClient = new GuestClient(guest.getShareURL(), guestPermission.getRecipient(), false);
            ResolveShareResponse shareResolveResponse = revokedGuestClient.getShareResolveResponse();
            assertEquals("Status wrong", ResolveShareResponse.NOT_FOUND, shareResolveResponse.getStatus());
        } else {
            /*
             * check share target no longer accessible for non-anonymous guest user
             */
            guestClient.checkFolderNotAccessible(String.valueOf(folder.getObjectID()));
            guestClient.logout();
        }
        /*
         * check guest entities
         */
        guest = discoverGuestEntity(api, module, folder.getObjectID(), matchingPermission.getEntity());
        assertNull("guest entity still found", guest);
    }

    private void testUpdateSharedFile(EnumAPI api, FileStorageGuestObjectPermission guestPermission) throws Exception {
        testUpdateSharedFile(api, getDefaultFolder(FolderObject.INFOSTORE), guestPermission);
    }

    private void testUpdateSharedFile(EnumAPI api, int parent, FileStorageGuestObjectPermission guestPermission) throws Exception {
        /*
         * create folder and a shared file inside
         */
        FolderObject folder = insertPrivateFolder(api, FolderObject.INFOSTORE, parent);
        File file = insertSharedFile(folder.getObjectID(), guestPermission);
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
        /*
         * update file, revoke permissions
         */
        Date futureTimestamp = new Date(System.currentTimeMillis() + 1000000);
        file.setLastModified(futureTimestamp);
        file.setObjectPermissions(Collections.<FileStorageObjectPermission>emptyList());
        file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS } );
        /*
         * check permissions
         */
        assertTrue("object permissions still present", null == file.getObjectPermissions() || 0 == file.getObjectPermissions().size());
        if (RecipientType.ANONYMOUS.equals(guestPermission.getRecipient().getType())) {
            /*
             * for anonymous guest user, check access with previous guest session (after waiting some time until background operations took place)
             */
            checkGuestUserDeleted(matchingPermission.getEntity());
            guestClient.checkSessionAlive(true);
            /*
             * check if share link still accessible
             */
            GuestClient revokedGuestClient = new GuestClient(guest.getShareURL(), guestPermission.getRecipient(), false);
            ResolveShareResponse shareResolveResponse = revokedGuestClient.getShareResolveResponse();
            assertEquals("Status wrong", ResolveShareResponse.NOT_FOUND, shareResolveResponse.getStatus());
        } else {
            /*
             * check share target no longer accessible for non-anonymous guest user
             */
            guestClient.checkFileNotAccessible(file.getId());
            guestClient.logout();
        }
        /*
         * check guest entities
         */
        guest = discoverGuestEntity(file.getFolderId(), file.getId(), matchingPermission.getEntity());
        assertNull("guest entity still found", guest);
    }

    private void testDeleteSharedFile(EnumAPI api, int parent, FileStorageGuestObjectPermission guestPermission, boolean hardDelete) throws Exception {
        /*
         * create folder and a shared file inside
         */
        FolderObject folder = insertPrivateFolder(api, FolderObject.INFOSTORE, parent);
        File file = insertSharedFile(folder.getObjectID(), guestPermission);
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
        /*
         * delete file, thus implicitly revoke permissions
         */
        Date futureTimestamp = new Date(System.currentTimeMillis() + 1000000);
        DeleteInfostoreRequest deleteInfostoreRequest = new DeleteInfostoreRequest(file.getId(), file.getFolderId(), futureTimestamp);
        if (hardDelete) {
            deleteInfostoreRequest.setHardDelete(Boolean.TRUE);
        }
        getClient().execute(deleteInfostoreRequest);
        /*
         * check permissions
         */
        if (RecipientType.ANONYMOUS.equals(guestPermission.getRecipient().getType())) {
            /*
             * for anonymous guest user, check access with previous guest session (after waiting some time until background operations took place)
             */
            checkGuestUserDeleted(matchingPermission.getEntity());
            guestClient.checkSessionAlive(true);
            /*
             * check if share link still accessible
             */
            GuestClient revokedGuestClient = new GuestClient(guest.getShareURL(), guestPermission.getRecipient(), false);
            ResolveShareResponse shareResolveResponse = revokedGuestClient.getShareResolveResponse();
            assertEquals("Status wrong", ResolveShareResponse.NOT_FOUND, shareResolveResponse.getStatus());
        } else {
            /*
             * check share target no longer accessible for non-anonymous guest user
             */
            guestClient.checkFileNotAccessible(file.getId());
            guestClient.logout();
        }
        /*
         * check guest entities
         */
        guest = discoverGuestEntity(file.getFolderId(), file.getId(), matchingPermission.getEntity());
        assertNull("guest entity still found", guest);
    }

}
