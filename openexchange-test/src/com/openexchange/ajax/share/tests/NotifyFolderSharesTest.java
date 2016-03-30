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

import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.NotifyFolderRequest;
import com.openexchange.ajax.smtptest.actions.GetMailsRequest;
import com.openexchange.ajax.smtptest.actions.GetMailsResponse.Message;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.GuestRecipient;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link NotifyFolderSharesTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class NotifyFolderSharesTest extends ShareTest {

    /**
     * Initializes a new {@link NotifyFolderSharesTest}.
     *
     * @param name The test name
     */
    public NotifyFolderSharesTest(String name) {
        super(name);
    }

    public void testNotifyGuest() throws Exception {
        int module = randomModule();
        testNotifyGuest(module, getDefaultFolder(module));
        testNotifyGuest(module, getPublicRoot(module));
    }

    public void testNotifyGroup() throws Exception {
        int module = randomModule();
        testNotifyGroup(module, getDefaultFolder(module));
        testNotifyGroup(module, getPublicRoot(module));
    }

    public void testNotifyUser() throws Exception {
        int module = randomModule();
        testNotifyUser(module, getDefaultFolder(module));
        testNotifyUser(module, getPublicRoot(module));
    }

    private void testNotifyGuest(int module, int parent) throws Exception {
        OCLGuestPermission guestPermission = randomGuestPermission(RecipientType.GUEST, module);
        String emailAddress = ((GuestRecipient) guestPermission.getRecipient()).getEmailAddress();
        testNotify(module, parent, guestPermission, emailAddress);
    }

    private void testNotifyGroup(int module, int parent) throws Exception {
        OCLPermission permission = new OCLPermission(GroupStorage.GROUP_ZERO_IDENTIFIER, 0);
        permission.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS,
            OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        permission.setGroupPermission(true);
        AJAXClient client2 = new AJAXClient(User.User2);
        String emailAddress = client2.getValues().getDefaultAddress();
        client2.logout();
        testNotify(module, parent, permission, emailAddress);
    }

    private void testNotifyUser(int module, int parent) throws Exception {
        AJAXClient client2 = new AJAXClient(User.User2);
        int userId = client2.getValues().getUserId();
        String emailAddress = client2.getValues().getDefaultAddress();
        client2.logout();
        OCLPermission permission = new OCLPermission(userId, 0);
        permission.setAllPermission(OCLPermission.READ_ALL_OBJECTS, OCLPermission.READ_ALL_OBJECTS,
            OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        testNotify(module, parent, permission, emailAddress);
    }

    private void testNotify(int module, int parent, OCLPermission permission, String emailAddress) throws Exception {
        /*
         * create shared folder
         */
        FolderObject folder = insertSharedFolder(EnumAPI.OX_NEW, module, parent, permission);
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission oclPermission : folder.getPermissions()) {
            if (oclPermission.getEntity() != client.getValues().getUserId()) {
                matchingPermission = oclPermission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        checkPermissions(permission, matchingPermission);
        /*
         * pop inbox, then notify recipient again
         */
        client.execute(new GetMailsRequest());
        client.execute(new NotifyFolderRequest(String.valueOf(folder.getObjectID()), matchingPermission.getEntity()));
        /*
         * verify notification message
         */
        Message notificationMessage = discoverInvitationMessage(client, emailAddress);
        assertNotNull(notificationMessage);
    }

}
