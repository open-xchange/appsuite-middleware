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

package com.openexchange.ajax.share.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug58051Test}
 *
 * Sharing leaks mail address to anonymous guests
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Bug58051Test extends ShareTest {

    @Test
    public void testGetUserAsNamedGuest() throws Exception {
        testGetUserAsGuest(createNamedGuestPermission());
    }

    @Test
    public void testGetUserAsAnonyousGuest() throws Exception {
        testGetUserAsGuest(createAnonymousGuestPermission());
    }

    private void testGetUserAsGuest(OCLGuestPermission oclGuestPermission) throws Exception {
        FileStorageGuestObjectPermission guestPermission = asObjectPermission(oclGuestPermission);
        /*
         * create folder and a shared file inside
         */
        FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(FolderObject.INFOSTORE));
        File file = insertSharedFile(folder.getObjectID(), guestPermission);
        /*
         * check permissions
         */
        FileStorageObjectPermission matchingPermission = null;
        for (FileStorageObjectPermission permission : file.getObjectPermissions()) {
            if (permission.getEntity() != getClient().getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created file found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(file.getId(), matchingPermission.getEntity());
        assertNotNull("No guest permission entitiy", guest);
        checkPermissions(guestPermission, guest.toObjectPermission());
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(guest, guestPermission.getRecipient(), oclGuestPermission.getApiClient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
        /*
         * get user of sharing user as guest via "get" & check email1 property
         */
        com.openexchange.ajax.user.actions.GetRequest userGetRequest = new com.openexchange.ajax.user.actions.GetRequest(getClient().getValues().getUserId(), guestClient.getValues().getTimeZone());
        com.openexchange.ajax.user.actions.GetResponse userGetResponse = guestClient.execute(userGetRequest);
        assertFalse(userGetResponse.hasError());
        assertNotNull(userGetResponse.getContact());
        assertNull(userGetResponse.getContact().getEmail1());
        assertNotNull(userGetResponse.getUser());
        assertNull(userGetResponse.getUser().getMail());
        /*
         * get user of sharing user as guest via "list" & check email1 property
         */
        com.openexchange.ajax.user.actions.ListRequest userListRequest = new com.openexchange.ajax.user.actions.ListRequest(new int[] { getClient().getValues().getUserId() }, new int[] { Contact.EMAIL1 });
        com.openexchange.ajax.user.actions.ListResponse userListResponse = guestClient.execute(userListRequest);
        assertFalse(userListResponse.hasError());
        assertNotNull(userListResponse.getUsers());
        assertEquals(1, userListResponse.getUsers().length);
        assertNull(userListResponse.getUsers()[0].getEmail1());
    }

}
