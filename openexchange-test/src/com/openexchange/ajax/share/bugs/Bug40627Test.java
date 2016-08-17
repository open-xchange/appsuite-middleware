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

package com.openexchange.ajax.share.bugs;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.GetInfostoreResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.FileShare;
import com.openexchange.ajax.share.actions.FolderShare;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.util.TimeZones;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.tools.arrays.Arrays;

/**
 * {@link Bug40627Test}
 *
 * Sharing exposes internal and other guests mail addresses to guests
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug40627Test extends ShareTest {

    /**
     * Initializes a new {@link Bug40627Test}.
     *
     * @param name The test name
     */
    public Bug40627Test(String name) {
        super(name);
    }

    public void testCheckExtendedFolderPermissionAsAnonymousGuest() throws Exception {
        testCheckExtendedFolderPermissions(createAnonymousGuestPermission());
    }

    public void testCheckExtendedFolderPermissionAsInvitedGuest() throws Exception {
        testCheckExtendedFolderPermissions(createNamedGuestPermission(randomUID() + "@example.org", "Test Guest"));
    }

    public void testCheckExtendedObjectPermissionAsAnonymousGuest() throws Exception {
        testCheckExtendedObjectPermissions(asObjectPermission(createAnonymousGuestPermission()));
    }

    public void testCheckExtendedObjectPermissionAsInvitedGuest() throws Exception {
        testCheckExtendedObjectPermissions(asObjectPermission(createNamedGuestPermission(randomUID() + "@example.org", "Test Guest")));
    }

    private void testCheckExtendedFolderPermissions(OCLGuestPermission guestPermission) throws Exception {
        /*
         * create shared folder
         */
        int module = randomModule();
        List<OCLPermission> permissions = new ArrayList<OCLPermission>();
        permissions.add(guestPermission);
        OCLPermission groupPermission = new OCLPermission(GroupStorage.GROUP_ZERO_IDENTIFIER, 0);
        groupPermission.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS,
            OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        groupPermission.setGroupPermission(true);
        permissions.add(groupPermission);
        AJAXClient client2 = new AJAXClient(User.User2);
        int userId2 = client2.getValues().getUserId();
        client2.logout();
        OCLPermission userPermission = new OCLPermission(userId2, 0);
        userPermission.setAllPermission(OCLPermission.READ_ALL_OBJECTS, OCLPermission.READ_ALL_OBJECTS,
            OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        permissions.add(userPermission);
        FolderObject folder = insertSharedFolder(EnumAPI.OX_NEW, module, getDefaultFolder(module), randomUID(), permissions.toArray(new OCLPermission[permissions.size()]));
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission permission : folder.getPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId() && false == permission.isGroupPermission() && permission.getEntity() != userId2) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(EnumAPI.OX_NEW, module, folder.getObjectID(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(discoverShareURL(guest), guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
        /*
         * check extended permissions as guest user
         */
        GetResponse getResponse = guestClient.execute(new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_NEW, folder.getObjectID()));
        assertFalse(getResponse.getErrorMessage(), getResponse.hasError());
        FolderShare folderShare = FolderShare.parse((JSONObject) getResponse.getData(), guestClient.getValues().getTimeZone());
        /*
         * expect to see other permission entities based on recipient type
         */
        int[] visibleUserIDs;
        if (RecipientType.ANONYMOUS.equals(guestPermission.getRecipient().getType())) {
            visibleUserIDs = new int[] { getClient().getValues().getUserId() };
        } else {
            visibleUserIDs = new int[] { getClient().getValues().getUserId(), userId2 };
        }
        checkExtendedPermissions(client, folderShare.getExtendedPermissions(), guest.getEntity(), visibleUserIDs);
    }

    private void testCheckExtendedObjectPermissions(FileStorageGuestObjectPermission guestPermission) throws Exception {
        /*
         * create folder and a shared file inside
         */
        List<FileStorageObjectPermission> permissions = new ArrayList<FileStorageObjectPermission>();
        permissions.add(guestPermission);
        permissions.add(new DefaultFileStorageObjectPermission(GroupStorage.GROUP_ZERO_IDENTIFIER, true, FileStorageObjectPermission.READ));
        AJAXClient client2 = new AJAXClient(User.User2);
        int userId2 = client2.getValues().getUserId();
        client2.logout();
        permissions.add(new DefaultFileStorageObjectPermission(userId2, false, FileStorageObjectPermission.WRITE));
        byte[] contents = new byte[64 + random.nextInt(256)];
        random.nextBytes(contents);
        String filename = randomUID();
        FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(FolderObject.INFOSTORE));
        File file = insertSharedFile(folder.getObjectID(), filename, permissions, contents);
        /*
         * check permissions
         */
        FileStorageObjectPermission matchingPermission = null;
        for (FileStorageObjectPermission permission : file.getObjectPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId() && false == permission.isGroup() && permission.getEntity() != userId2) {
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
        GuestClient guestClient = resolveShare(discoverShareURL(guest), guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission, contents);
        /*
         * check extended permissions as guest user
         */
        GetInfostoreRequest getInfostoreRequest = new GetInfostoreRequest(guestClient.getItem());
        getInfostoreRequest.setFailOnError(true);
        GetInfostoreResponse getInfostoreResponse = guestClient.execute(getInfostoreRequest);
        assertFalse(getInfostoreResponse.getErrorMessage(), getInfostoreResponse.hasError());
        FileShare fileShare = FileShare.parse((JSONObject) getInfostoreResponse.getData(), guestClient.getValues().getTimeZone());
        /*
         * expect to see other permission entities based on recipient type
         */
        int[] visibleUserIDs;
        if (RecipientType.ANONYMOUS.equals(guestPermission.getRecipient().getType())) {
            visibleUserIDs = new int[] { getClient().getValues().getUserId() };
        } else {
            visibleUserIDs = new int[] { getClient().getValues().getUserId(), userId2 };
        }
        checkExtendedPermissions(client, fileShare.getExtendedPermissions(), guest.getEntity(), visibleUserIDs);
    }

    private static void checkExtendedPermissions(AJAXClient sharingClient, List<ExtendedPermissionEntity> actual, int guestID, int[] visibleUserIDs) throws Exception {
        assertNotNull(actual);
        for (ExtendedPermissionEntity permissionEntity : actual) {
            switch (permissionEntity.getType()) {
                case GROUP:
                    assertEquals("Group " + permissionEntity.getEntity(), permissionEntity.getDisplayName());
                    break;
                case GUEST:
                    assertEquals(permissionEntity.getEntity(), guestID);
                    break;
                case USER:
                    if (Arrays.contains(visibleUserIDs, permissionEntity.getEntity())) {
                        GetRequest getRequest = new com.openexchange.ajax.user.actions.GetRequest(permissionEntity.getEntity(), TimeZones.UTC);
                        com.openexchange.ajax.user.actions.GetResponse getResponse = sharingClient.execute(getRequest);
                        com.openexchange.groupware.ldap.User expectedUser = getResponse.getUser();
                        assertEquals(expectedUser.getDisplayName(), permissionEntity.getDisplayName());
                        assertNotNull(permissionEntity.getContact());
                        assertEquals(expectedUser.getSurname(), permissionEntity.getContact().getSurName());
                        assertEquals(expectedUser.getGivenName(), permissionEntity.getContact().getGivenName());
                        assertEquals(expectedUser.getMail(), permissionEntity.getContact().getEmail1());
                    } else {
                        assertEquals("User " + permissionEntity.getEntity(), permissionEntity.getDisplayName());
                        assertNotNull(permissionEntity.getContact());
                        assertEquals("User", permissionEntity.getContact().getSurName());
                        assertEquals(String.valueOf(permissionEntity.getEntity()), permissionEntity.getContact().getGivenName());
                    }
                    break;
                default:
                    break;
            }
        }
    }

}
