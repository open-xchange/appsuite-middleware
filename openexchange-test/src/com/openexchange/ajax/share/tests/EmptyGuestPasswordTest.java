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
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.passwordchange.actions.PasswordChangeUpdateRequest;
import com.openexchange.ajax.passwordchange.actions.PasswordChangeUpdateResponse;
import com.openexchange.ajax.session.actions.LoginResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.impl.OCLPermission;


/**
 * {@link EmptyGuestPasswordTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class EmptyGuestPasswordTest extends ShareTest {

    private FolderObject folder;

    public EmptyGuestPasswordTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        deleteFoldersSilently(client, Collections.singletonList(folder.getObjectID()));
        super.tearDown();
    }

    public void testEmptyPassword() throws Exception {
        long now = System.currentTimeMillis();
        OCLGuestPermission perm = createNamedGuestPermission("testGuestPasswordInit" + now + "@example.org", "Test " + now);
        folder = insertSharedFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), client.getValues().getPrivateInfostoreFolder(), perm);
        OCLPermission matchingPermission = null;
        for (OCLPermission permission : folder.getPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        checkPermissions(perm, matchingPermission);
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), folder.getObjectID(), matchingPermission.getEntity());
        checkGuestPermission(perm, guest);
        /*
         * check access to share - a login request must not have been performed
         */
        String shareURL = discoverShareURL(guest);
        GuestClient guestClient = resolveShare(shareURL);
        assertNull(guestClient.getLoginResponse());
        /*
         * Set password for guest user
         */
        String newPW = UUIDs.getUnformattedStringFromRandom();
        PasswordChangeUpdateRequest pwChangeReq = new PasswordChangeUpdateRequest(newPW, "", true);
        PasswordChangeUpdateResponse pwChangeResp = guestClient.execute(pwChangeReq);
        assertFalse(pwChangeResp.hasWarnings());
        assertFalse(pwChangeResp.hasError());
        guestClient.logout();
        /*
         * Re-login with PW
         */
        guestClient = resolveShare(shareURL, ShareTest.getUsername(perm.getRecipient()), newPW);
        LoginResponse response = guestClient.getLoginResponse();
        assertNotNull(response);
        assertFalse(response.hasError());
        assertNotNull(response.getSessionId());
        /*
         * Empty password should now fail with LoginExceptionCodes.INVALID_CREDENTIALS
         */
        guestClient = resolveShare(shareURL);
        response = guestClient.getLoginResponse();
        assertNotNull(response);
        assertTrue(response.hasError());
    }

}
