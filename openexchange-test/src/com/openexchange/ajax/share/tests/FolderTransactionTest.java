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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.FolderShare;
import com.openexchange.ajax.share.actions.ResolveShareResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link FolderTransactionTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class FolderTransactionTest extends ShareTest {

    /**
     * Initializes a new {@link FolderTransactionTest}.
     *
     * @param name
     */
    public FolderTransactionTest(String name) {
        super(name);
    }

    public void testDontCreateShareOnFailingFolderCreate() throws Exception {
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            for (int module : TESTED_MODULES) {
                testDontCreateShareOnFailingFolderCreate(api, module);
            }
        }
    }

    public void testDontCreateShareOnFailingFolderCreate(EnumAPI api, int module) throws Exception {
        FolderObject parent = getFolder(api, getDefaultFolder(module));
        FolderObject folder = insertPrivateFolder(api, module, parent.getObjectID());
        List<FolderShare> oldShares = getFolderShares(api, module);
        /*
         * Should fail due to name conflict
         */
        boolean insertionFailed = false;
        try {
            insertSharedFolder(
                api,
                module,
                parent.getObjectID(),
                folder.getFolderName(),
                createAnonymousGuestPermission());
        } catch (Throwable e) {
            insertionFailed = true;
        }
        assertTrue("API: " + api + ", Module: " + module, insertionFailed);

        List<FolderShare> newShares = getFolderShares(api, module);
        assertEquals("The number of shares differs but should not. " + "API: " + api + ", Module: " + module, oldShares.size(), newShares.size());
    }

    public void testDontCreateShareOnFailingFolderUpdate() throws Exception {
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            for (int module : TESTED_MODULES) {
                testDontCreateShareOnFailingFolderUpdate(api, module);
            }
        }
    }

    public void testDontCreateShareOnFailingFolderUpdate(EnumAPI api, int module) throws Exception {
        FolderObject parent = getFolder(api, getDefaultFolder(module));
        FolderObject sharedFolder = insertPrivateFolder(api, module, parent.getObjectID());
        List<FolderShare> oldShares = getFolderShares(api, module);

        OCLGuestPermission guestPermission = createAnonymousGuestPermission();
        ArrayList<OCLPermission> permissions = new ArrayList<OCLPermission>(sharedFolder.getPermissions());
        permissions.add(guestPermission);
        sharedFolder.setPermissions(permissions);

        /*
         * Update should fail because of possible concurrent modification
         */
        sharedFolder.setLastModified(new Date(sharedFolder.getLastModified().getTime() - 1000));
        boolean updateFailed = false;
        try {
            updateFolder(api, sharedFolder);
        } catch (Throwable e) {
            updateFailed = true;
        }
        assertTrue("API: " + api + ", Module: " + module, updateFailed);

        List<FolderShare> newShares = getFolderShares(api, module);
        assertEquals("The number of shares differs but should not." + "API: " + api + ", Module: " + module, oldShares.size(), newShares.size());
    }

    public void testDontRemoveSharesOnFailingFolderUpdate() throws Exception {
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            for (int module : TESTED_MODULES) {
                testDontRemoveSharesOnFailingFolderUpdate(api, module);
            }
        }
    }

    public void testDontRemoveSharesOnFailingFolderUpdate(EnumAPI api, int module) throws Exception {
        FolderObject parent = getFolder(api, getDefaultFolder(module));
        OCLGuestPermission guestPermission = createAnonymousGuestPermission();
        FolderObject sharedFolder = insertSharedFolder(
            api,
            module,
            parent.getObjectID(),
            guestPermission);
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission permission : sharedFolder.getPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found." + "API: " + api + ", Module: " + module, matchingPermission);
        checkPermissions(guestPermission, matchingPermission);

        ExtendedPermissionEntity guest = discoverGuestEntity(api, module, sharedFolder.getObjectID(), matchingPermission.getEntity());
        assertNotNull("API: " + api + ", Module: " + module, guest);
        String shareURL = discoverShareURL(guest);

        /*
         * Update should fail because of invalid permissions
         */
        sharedFolder.setPermissionsAsArray(new OCLPermission[0]);
        boolean updateFailed = false;
        try {
            updateFolder(api, sharedFolder);
        } catch (Throwable e) {
            updateFailed = true;
        }
        assertTrue("API: " + api + ", Module: " + module, updateFailed);

        GuestClient guestClient = new GuestClient(shareURL, guestPermission.getRecipient());
        ResolveShareResponse resolveResponse = guestClient.getShareResolveResponse();
        assertEquals("API: " + api + ", Module: " + module, Integer.toString(sharedFolder.getObjectID()), resolveResponse.getFolder());
    }

}
