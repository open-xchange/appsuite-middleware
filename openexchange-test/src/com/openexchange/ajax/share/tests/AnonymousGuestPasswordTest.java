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

import java.util.Date;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.passwordchange.actions.PasswordChangeUpdateRequest;
import com.openexchange.ajax.passwordchange.actions.PasswordChangeUpdateResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.UpdateLinkRequest;
import com.openexchange.ajax.share.actions.UpdateLinkResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link AnonymousGuestPasswordTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AnonymousGuestPasswordTest extends ShareTest {

    /**
     * Initializes a new {@link AnonymousGuestPasswordTest}.
     *
     * @param name The test name
     */
    public AnonymousGuestPasswordTest(String name) {
        super(name);
    }

    public void testUpdatePasswordForAnonymousGuest() throws Exception {
        OCLGuestPermission guestPermission = createAnonymousGuestPermission();
        /*
         * create folder shared to guest user
         */
        int module = randomModule();
        EnumAPI api = randomFolderAPI();
        FolderObject folder = insertSharedFolder(api, module, getDefaultFolder(module), guestPermission);
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
        assertEquals(RecipientType.ANONYMOUS, guest.getType());
        assertNull("Password is set", guest.getPassword());
        /*
         * update recipient, set a password for the anonymous guest
         */
        ShareTarget shareTarget = new ShareTarget(module, String.valueOf(folder.getObjectID()));
        UpdateLinkRequest updateLinkRequest = new UpdateLinkRequest(shareTarget, client.getValues().getTimeZone(), System.currentTimeMillis());
        updateLinkRequest.setPassword("secret");
        ((AnonymousRecipient) guestPermission.getRecipient()).setPassword("secret"); // for subsequent comparison
        UpdateLinkResponse updateLinkResponse = client.execute(updateLinkRequest);
        Date clientTimestamp = updateLinkResponse.getTimestamp();
        /*
         * discover & check guest
         */
        guest = discoverGuestEntity(api, module, folder.getObjectID(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        assertEquals(RecipientType.ANONYMOUS, guest.getType());
        assertNotNull("Password not set", guest.getPassword());
        assertEquals("Password wrong", "secret", guest.getPassword());
        /*
         * update recipient, change password for the anonymous guest
         */
        updateLinkRequest = new UpdateLinkRequest(shareTarget, client.getValues().getTimeZone(), clientTimestamp.getTime());
        updateLinkRequest.setPassword("geheim");
        ((AnonymousRecipient) guestPermission.getRecipient()).setPassword("geheim"); // for subsequent comparison
        updateLinkResponse = client.execute(updateLinkRequest);
        clientTimestamp = updateLinkResponse.getTimestamp();
        /*
         * discover & check guest
         */
        guest = discoverGuestEntity(api, module, folder.getObjectID(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        assertEquals(RecipientType.ANONYMOUS, guest.getType());
        assertNotNull("Password not set", guest.getPassword());
        assertEquals("Password wrong", "geheim", guest.getPassword());
        /*
         * update recipient remove password for the anonymous guest
         */
        updateLinkRequest = new UpdateLinkRequest(shareTarget, client.getValues().getTimeZone(), clientTimestamp.getTime());
        updateLinkRequest.setPassword(null);
        ((AnonymousRecipient) guestPermission.getRecipient()).setPassword(null); // for subsequent comparison
        updateLinkResponse = client.execute(updateLinkRequest);
        clientTimestamp = updateLinkResponse.getTimestamp();
        /*
         * discover & check guest
         */
        guest = discoverGuestEntity(api, module, folder.getObjectID(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        assertEquals(RecipientType.ANONYMOUS, guest.getType());
        assertNull("Password is set", guest.getPassword());
    }

    public void testDontAllowAnonymousGuestPasswordUpdate() throws Exception {
        OCLGuestPermission guestPermission = createAnonymousGuestPermission("secret");
        /*
         * create folder shared to guest user
         */
        int module = randomModule();
        EnumAPI api = randomFolderAPI();
        FolderObject folder = insertSharedFolder(api, module, getDefaultFolder(module), guestPermission);
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
        guestPermission.setEntity(matchingPermission.getEntity());
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
        /*
         * try to update password
         */
        String newPassword = "secret2";
        PasswordChangeUpdateRequest updateRequest = new PasswordChangeUpdateRequest(
            newPassword, ((AnonymousRecipient) guestPermission.getRecipient()).getPassword(), false);
        PasswordChangeUpdateResponse response = guestClient.execute(updateRequest);
        assertTrue("No errors in response", response.hasError());
        /*
         * check if share link still accessible with old password
         */
        guestClient = resolveShare(guest, guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        /*
         * check access to share with new password
         */
        GuestClient revokedGuestClient = new GuestClient(guest.getShareURL(), null, newPassword, false);
        assertTrue("No errors during login with new password", revokedGuestClient.getLoginResponse().hasError());
        assertNull("Got session ID from login with new password", revokedGuestClient.getLoginResponse().getSessionId());
    }

}
