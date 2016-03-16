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

import java.util.List;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.FileShare;
import com.openexchange.ajax.share.actions.FileSharesRequest;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link ListFileSharesTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ListFileSharesTest extends ShareTest {

    /**
     * Initializes a new {@link ListFileSharesTest}.
     *
     * @param name The test name
     */
    public ListFileSharesTest(String name) {
        super(name);
    }

    public void testListSharedFilesToAnonymous() throws Exception {
        testListSharedFiles(randomGuestObjectPermission(RecipientType.ANONYMOUS));
    }

    public void testListSharedFilesToGuest() throws Exception {
        testListSharedFiles(randomGuestObjectPermission(RecipientType.GUEST));
    }

    public void testListSharedFilesToGroup() throws Exception {
        testListSharedFiles(new DefaultFileStorageObjectPermission(GroupStorage.GROUP_ZERO_IDENTIFIER, true, FileStorageObjectPermission.READ));
    }

    public void testListSharedFilesToUser() throws Exception {
        AJAXClient client2 = new AJAXClient(User.User2);
        int userId = client2.getValues().getUserId();
        client2.logout();
        testListSharedFiles(new DefaultFileStorageObjectPermission(userId, false, FileStorageObjectPermission.WRITE));
    }

    private void testListSharedFiles(FileStorageObjectPermission permission) throws Exception {
        /*
         * create folder and a shared file inside
         */
        String filename = randomUID();
        FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(FolderObject.INFOSTORE));
        File file = insertSharedFile(folder.getObjectID(), filename, permission);
        /*
         * check permissions
         */
        FileStorageObjectPermission matchingPermission = null;
        for (FileStorageObjectPermission objectPermission : file.getObjectPermissions()) {
            if (objectPermission.getEntity() != client.getValues().getUserId()) {
                matchingPermission = objectPermission;
                break;
            }
        }
        assertNotNull("No matching permission in created file found", matchingPermission);
        checkPermissions(permission, matchingPermission);
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(file.getFolderId(), file.getId(), matchingPermission.getEntity());
        checkGuestPermission(permission, guest);
    }

    public void testDontListPublicFiles() throws Exception {
        /*
         * create folder and a shared file inside
         */
        String filename = randomUID();
        FileStorageGuestObjectPermission guestPermission = randomGuestObjectPermission();
        FolderObject folder = insertPublicFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE);
        File file = insertSharedFile(folder.getObjectID(), filename, guestPermission);
        /*
         * check permissions
         */
        FileStorageObjectPermission matchingPermission = null;
        for (FileStorageObjectPermission permission : file.getObjectPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created file found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * check if share appears in user shared files
         */
        FileShare matchingShare = null;
        List<FileShare> shares = client.execute(new FileSharesRequest()).getShares(client.getValues().getTimeZone());
        for (FileShare share : shares) {
            if (share.getId().equals(file.getId())) {
                matchingShare = share;
                break;
            }
        }
        assertNull("Share in public folder listed", matchingShare);
    }

    public void testDontListPublicFilesInSubfolder() throws Exception {
        /*
         * create folder and a shared file inside
         */
        String filename = randomUID();
        FileStorageGuestObjectPermission guestPermission = randomGuestObjectPermission();
        FolderObject folder = insertPublicFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE);
        FolderObject subfolder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, folder.getObjectID());
        File file = insertSharedFile(subfolder.getObjectID(), filename, guestPermission);
        /*
         * check permissions
         */
        FileStorageObjectPermission matchingPermission = null;
        for (FileStorageObjectPermission permission : file.getObjectPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created file found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * check if share appears in user shared files
         */
        FileShare matchingShare = null;
        List<FileShare> shares = client.execute(new FileSharesRequest()).getShares(client.getValues().getTimeZone());
        for (FileShare share : shares) {
            if (share.getId().equals(file.getId())) {
                matchingShare = share;
                break;
            }
        }
        assertNull("Share in public folder listed", matchingShare);
    }

}
