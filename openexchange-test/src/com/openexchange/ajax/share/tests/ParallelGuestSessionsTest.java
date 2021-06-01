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

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.test.tryagain.TryAgain;

/**
 * {@link ParallelGuestSessionsTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ParallelGuestSessionsTest extends ShareTest {

    @Test
    @TryAgain
    public void testParallelInvitedGuestSessions() throws Exception {
        int module = randomModule();
        testParallelGuestSessions(randomFolderAPI(), module, getDefaultFolder(module), randomGuestPermission(RecipientType.GUEST, module));
    }

    @Test
    @TryAgain
    public void testParallelAnonymousGuestSessions() throws Exception {
        int module = randomModule();
        testParallelGuestSessions(randomFolderAPI(), module, getDefaultFolder(module), randomGuestPermission(RecipientType.ANONYMOUS, module));
    }

    private void testParallelGuestSessions(EnumAPI api, int module, int parent, OCLGuestPermission guestPermission) throws Exception {
        /*
         * create private folder
         */
        FolderObject folder = insertPrivateFolder(api, module, parent);
        /*
         * update folder, add permission for guest
         */
        folder.addPermission(guestPermission);
        folder = updateFolder(api, folder, new RequestCustomizer<UpdateRequest>() {

            @Override
            public void customize(UpdateRequest request) {
                request.setCascadePermissions(false);
                request.setNotifyPermissionEntities(Transport.MAIL);
            }
        });
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
        ExtendedPermissionEntity guest = discoverGuestEntity(api, module, folder.getObjectID(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        /*
         * check access to share, using the same ajax session as the sharing user
         */
        String shareURL = discoverShareURL(guestPermission.getApiClient(), guest);
        assertNotNull("ShareURL couldn't be discovered.", shareURL);
        AJAXSession sharedSession = getSession();
        String oldSessionID = sharedSession.getId();
        try {
            sharedSession.setId(null);
            GuestClient guestClient = new GuestClient(sharedSession, shareURL, guestPermission.getRecipient(), true, false);
            guestClient.checkShareModuleAvailable();
            guestClient.checkShareAccessible(guestPermission);
        } finally {
            // restore sharing user's session ID for teardown
            sharedSession.setId(oldSessionID);
        }
        /*
         * re-discover guest, using the sharing user's session, thus verifying the old session is still alive, too
         */
        guest = discoverGuestEntity(api, module, folder.getObjectID(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
    }

}
