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

import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.ResolveShareResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link Bug40993Test}
 *
 * Guest unable to login if initial permission is revoked
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug40993Test extends ShareTest {

    /**
     * Initializes a new {@link Bug40993Test}.
     *
     * @param name The test name
     */
    public Bug40993Test(String name) {
        super(name);
    }

    public void testAccessSubfolderRandomly() throws Exception {
        int module = randomModule();
        testAccessSubfolder(randomFolderAPI(), module, randomGuestPermission(RecipientType.GUEST, module));
    }

    private void testAccessSubfolder(EnumAPI api, int module, OCLGuestPermission guestPermission) throws Exception {
        testAccessSubfolder(api, module, getDefaultFolder(module), guestPermission);
    }

    private void testAccessSubfolder(EnumAPI api, int module, int parent, OCLGuestPermission guestPermission) throws Exception {
        /*
         * create folder shared to guest user
         */
        FolderObject folder = insertSharedFolder(api, module, parent, guestPermission);
        /*
         * check permissions
         */
        OCLPermission adminPermission = null;
        OCLPermission matchingPermission = null;
        for (OCLPermission permission : folder.getPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId()) {
                matchingPermission = permission;
            } else {
                adminPermission = permission;
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
        String folderShareURL = discoverShareURL(guest);
        GuestClient guestClient = resolveShare(folderShareURL, guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
        /*
         * create a subfolder, inheriting the permissions from the parent
         */
        FolderObject subfolder = insertSharedFolder(api, module, folder.getObjectID(), matchingPermission);
        /*
         * check permissions
         */
        matchingPermission = null;
        for (OCLPermission permission : subfolder.getPermissions()) {
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
        guest = discoverGuestEntity(api, module, subfolder.getObjectID(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        /*
         * check access to share
         */
        String subfolderShareURL = discoverShareURL(guest);
        guestClient = resolveShare(subfolderShareURL, guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
        /*
         * revoke guest permission for parent folder
         */
        folder.setLastModified(futureTimestamp());
        folder.setPermissionsAsArray(new OCLPermission[] { adminPermission });
        folder = updateFolder(api, folder);
        /*
         * check permissions
         */
        for (OCLPermission permission : folder.getPermissions()) {
            assertTrue("Guest permission still present", permission.getEntity() != matchingPermission.getEntity());
        }
        guest = discoverGuestEntity(api, module, folder.getObjectID(), matchingPermission.getEntity());
        assertNull("guest entity still found", guest);
        /*
         * check share URL to subfolder
         */
        guestClient = resolveShare(subfolderShareURL, guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
        guestClient.checkFolderAccessible(String.valueOf(subfolder.getObjectID()), guestPermission);
        guestClient.checkFolderNotAccessible(String.valueOf(folder.getObjectID()));
        /*
         * check share url to parent folder
         */
        guestClient = resolveShare(folderShareURL, guestPermission.getRecipient());
        assertEquals("Status wrong", ResolveShareResponse.NOT_FOUND_CONTINUE, guestClient.getShareResolveResponse().getStatus());
        guestClient.checkShareModuleAvailable();
        guestClient.checkFolderAccessible(String.valueOf(subfolder.getObjectID()), guestPermission);
        guestClient.checkFolderNotAccessible(String.valueOf(folder.getObjectID()));
    }

}
