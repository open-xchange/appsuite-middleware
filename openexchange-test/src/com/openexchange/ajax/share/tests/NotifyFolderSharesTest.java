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
import com.openexchange.ajax.share.Abstract2UserShareTest;
import com.openexchange.ajax.share.actions.NotifyFolderRequest;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.test.tryagain.TryAgain;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.models.MailData;

/**
 * {@link NotifyFolderSharesTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class NotifyFolderSharesTest extends Abstract2UserShareTest {

    @Test
    @TryAgain
    public void testNotifyGuest() throws Exception {
        int module = randomModule();
        testNotifyGuest(module, getDefaultFolder(module));
        testNotifyGuest(module, getPublicRoot(module));
    }

    @Test
    @TryAgain
    public void testNotifyGroup() throws Exception {
        int module = randomModule();
        testNotifyGroup(module, getDefaultFolder(module));
        testNotifyGroup(module, getPublicRoot(module));
    }

    @Test
    @TryAgain
    public void testNotifyUser() throws Exception {
        int module = randomModule();
        testNotifyUser(module, getDefaultFolder(module));
        testNotifyUser(module, getPublicRoot(module));
    }

    private void testNotifyGuest(int module, int parent) throws Exception {
        OCLGuestPermission guestPermission = randomGuestPermission(RecipientType.GUEST, module);
        String emailAddress = ((GuestRecipient) guestPermission.getRecipient()).getEmailAddress();
        testNotify(module, parent, guestPermission, emailAddress, guestPermission.getApiClient());
    }

    private void testNotifyGroup(int module, int parent) throws Exception {
        OCLPermission permission = new OCLPermission(GroupStorage.GROUP_ZERO_IDENTIFIER, 0);
        permission.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        permission.setGroupPermission(true);
        String emailAddress = client2.getValues().getDefaultAddress();
        client2.logout();
        testNotify(module, parent, permission, emailAddress, apiClient2);
    }

    private void testNotifyUser(int module, int parent) throws Exception {
        int userId = client2.getValues().getUserId();
        String emailAddress = client2.getValues().getDefaultAddress();
        client2.logout();
        OCLPermission permission = new OCLPermission(userId, 0);
        permission.setAllPermission(OCLPermission.READ_ALL_OBJECTS, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        testNotify(module, parent, permission, emailAddress, apiClient2);
    }

    private void testNotify(int module, int parent, OCLPermission permission, String emailAddress, ApiClient apiClient) throws Exception {
        /*
         * create shared folder
         */
        FolderObject folder = insertSharedFolder(EnumAPI.OX_NEW, module, parent, permission);
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission oclPermission : folder.getPermissions()) {
            if (oclPermission.getEntity() != getClient().getValues().getUserId()) {
                matchingPermission = oclPermission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        checkPermissions(permission, matchingPermission);
        /*
         * pop inbox, then notify recipient again
         */
        mailManager.clearMails();
        getClient().execute(new NotifyFolderRequest(String.valueOf(folder.getObjectID()), matchingPermission.getEntity()));
        /*
         * verify notification message
         */
        MailData notificationMessage = discoverInvitationMessage(apiClient, emailAddress);
        assertNotNull(notificationMessage);
    }

}
