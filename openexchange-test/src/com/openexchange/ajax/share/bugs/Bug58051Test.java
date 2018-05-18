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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
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
        testGetUserAsGuest(asObjectPermission(createNamedGuestPermission("horst@example.com", "Horst Example", "secret")));
    }

    @Test
    public void testGetUserAsAnonyousGuest() throws Exception {
        testGetUserAsGuest(asObjectPermission(createAnonymousGuestPermission()));
    }

    private void testGetUserAsGuest(FileStorageGuestObjectPermission guestPermission) throws Exception {
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
        ExtendedPermissionEntity guest = discoverGuestEntity(file.getFolderId(), file.getId(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(guest, guestPermission.getRecipient());
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
