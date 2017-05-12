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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.NotifyFileRequest;
import com.openexchange.ajax.share.actions.NotifyFolderRequest;
import com.openexchange.ajax.smtptest.actions.ClearMailsRequest;
import com.openexchange.ajax.smtptest.actions.GetMailsResponse.Message;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link Bug52843Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class Bug52843Test extends ShareTest {

    @Test
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
            if (oclPermission.getEntity() != getClient().getValues().getUserId()) {
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
        GuestClient guestClient = resolveShare(discoverShareURL(guest), guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission);
        /*
         * try to re-send notification as guest
         */
        getClient().execute(new ClearMailsRequest());
        AbstractAJAXResponse notifyResponse = guestClient.execute(new NotifyFolderRequest(String.valueOf(folder.getObjectID()), getClient().getValues().getUserId()));
        assertTrue("No errors or warnings", notifyResponse.hasError() || notifyResponse.hasWarnings());
        Message notificationMessage = discoverInvitationMessage(getClient(), getClient().getValues().getDefaultAddress());
        assertNull("Notification was received", notificationMessage);
    }

    @Test
    public void testNotifyFile() throws Exception {
        /*
         * insert shared file
         */
        byte[] contents = new byte[64 + random.nextInt(256)];
        random.nextBytes(contents);
        String filename = randomUID();
        int parentFolder = getDefaultFolder(FolderObject.INFOSTORE);
        List<FileStorageObjectPermission> permissions = new ArrayList<FileStorageObjectPermission>();
        FileStorageGuestObjectPermission guestPermission = randomGuestObjectPermission(RecipientType.GUEST);
        permissions.add(guestPermission);
        int otherUserId = getClient2().getValues().getUserId();
        DefaultFileStorageObjectPermission otherUserPermission = new DefaultFileStorageObjectPermission(otherUserId, false, FileStorageObjectPermission.WRITE);
        permissions.add(otherUserPermission);
        File file = insertSharedFile(parentFolder, filename, permissions, contents);
        /*
         * check permissions
         */
        FileStorageObjectPermission matchingPermission = null;
        for (FileStorageObjectPermission objectPermission : file.getObjectPermissions()) {
            if (objectPermission.getEntity() != getClient().getValues().getUserId() &&
                objectPermission.getEntity() != otherUserId) {
                matchingPermission = objectPermission;
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
        GuestClient guestClient = resolveShare(discoverShareURL(guest), guestPermission.getRecipient());
        guestClient.checkShareModuleAvailable();
        guestClient.checkShareAccessible(guestPermission, contents);
        /*
         * try to re-send notification as guest
         */
        getClient().execute(new ClearMailsRequest());
        AbstractAJAXResponse notifyResponse = guestClient.execute(new NotifyFileRequest(guestClient.getItem(), otherUserId));
        assertTrue("No errors or warnings", notifyResponse.hasError() || notifyResponse.hasWarnings());
        Message notificationMessage = discoverInvitationMessage(getClient(), getClient2().getValues().getDefaultAddress());
        assertNull("Notification was received", notificationMessage);
    }

}
