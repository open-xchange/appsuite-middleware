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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.NotifyFolderRequest;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.test.tryagain.TryAgain;
import com.openexchange.testing.httpclient.models.MailData;

/**
 * {@link Bug52843Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class Bug52843Test extends ShareTest {

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().createApiClient().withContexts(2).withUserPerContext(1).build();
    }

    @Test
    @TryAgain
    public void testNotifyFolder() throws Exception {
        /*
         * create shared folder
         */
        int module = FolderObject.INFOSTORE;
        int parent = getDefaultFolder(module);
        OCLGuestPermission guestPermission = randomGuestPermission(RecipientType.GUEST, module);
        FolderObject folder = insertSharedFolder(EnumAPI.OX_NEW, module, parent, guestPermission);
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission oclPermission : folder.getPermissions()) {
            if (oclPermission.getEntity() != testUser.getUserId()) {
                matchingPermission = oclPermission;
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
        GuestClient guestClient = resolveShare(discoverShareURL(guestPermission.getApiClient(), guest), guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
        /*
         * try to re-send notification as guest
         */
        mailManager.clearMails();
        AbstractAJAXResponse notifyResponse = guestClient.execute(new NotifyFolderRequest(String.valueOf(folder.getObjectID()), testUser.getUserId()));
        assertTrue("No errors or warnings", notifyResponse.hasError() || notifyResponse.hasWarnings());
        MailData notificationMessage = discoverInvitationMessage(getApiClient(), testUser.getLogin());
        assertNull("Notification was received", notificationMessage);
    }
}
