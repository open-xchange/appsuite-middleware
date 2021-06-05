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

package com.openexchange.ajax.share.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.session.actions.AutologinRequest;
import com.openexchange.ajax.session.actions.AutologinRequest.AutologinParameters;
import com.openexchange.ajax.session.actions.AutologinResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.test.tryagain.TryAgain;

/**
 * {@link GuestAutologinTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.6.2
 */
public class GuestAutologinTest extends ShareTest {

    @Test
    @TryAgain
    public void testGuestAutologin() throws Exception {
        /*
         * create folder shared to guest user
         */
        int module = randomModule();
        OCLGuestPermission guestPermission = randomGuestPermission(RecipientType.GUEST, module);
        ShareRecipient recipient = guestPermission.getRecipient();
        FolderObject folder = insertSharedFolder(EnumAPI.OX_NEW, module, getDefaultFolder(module), guestPermission);
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission permission : folder.getPermissions()) {
            if (permission.getEntity() != getClient().getValues().getUserId()) {
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
        String shareURL = discoverShareURL(guestPermission.getApiClient(), guest);
        /*
         * login & store guest session for auto-login, then try to auto-login
         */
        String client = AJAXClient.class.getName();
        AJAXSession sharedSession = getSession();
        String oldSessionID = sharedSession.getId();
        try {
            getSession().setId(null);
            GuestClient guestClient = new GuestClient(sharedSession, shareURL, getUsername(recipient), getPassword(recipient), client, true, true, false);
            AutologinRequest autologin = new AutologinRequest(new AutologinParameters(randomUID(), client, AJAXClient.VERSION), false);
            AutologinResponse response = guestClient.execute(autologin);
            assertFalse(response.getErrorMessage(), response.hasError());
            assertEquals(guestClient.getSession().getId(), response.getSessionId());
            assertEquals(guestClient.getValues().getUserId() + "@" + guestClient.getValues().getContextId(), response.getUser());
        } finally {
            sharedSession.setId(oldSessionID);
        }
    }
}
