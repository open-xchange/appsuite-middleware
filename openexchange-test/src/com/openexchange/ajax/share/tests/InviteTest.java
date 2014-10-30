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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.infostore.actions.ListInfostoreRequest;
import com.openexchange.ajax.infostore.actions.ListInfostoreRequest.ListItem;
import com.openexchange.ajax.infostore.actions.ListInfostoreResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.AllRequest;
import com.openexchange.ajax.share.actions.InviteRequest;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.modules.Module;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.InternalRecipient;
import com.openexchange.share.recipient.ShareRecipient;


/**
 * {@link InviteTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class InviteTest extends ShareTest {

    private static final int NUM_FILES = 5;

    private static final int FOLDER_READ_PERMISSION = Permissions.createPermissionBits(
        Permission.READ_FOLDER,
        Permission.READ_ALL_OBJECTS,
        Permission.NO_PERMISSIONS,
        Permission.NO_PERMISSIONS,
        false);

    private AJAXClient client2;
    private InfostoreTestManager itm;

    private FolderObject calendar;
    private FolderObject contacts;
    private FolderObject tasks;
    private FolderObject infostore;
    private FolderObject infostore2;
    private List<DefaultFile> files;


    /**
     * Initializes a new {@link InviteTest}.
     * @param name
     */
    public InviteTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        itm = new InfostoreTestManager(client);

        client2 = new AJAXClient(User.User2);
        UserValues values = client.getValues();
        calendar = insertPrivateFolder(EnumAPI.OX_NEW, Module.CALENDAR.getFolderConstant(), values.getPrivateAppointmentFolder());
        contacts = insertPrivateFolder(EnumAPI.OX_NEW, Module.CONTACTS.getFolderConstant(), values.getPrivateContactFolder());
        tasks = insertPrivateFolder(EnumAPI.OX_NEW, Module.TASK.getFolderConstant(), values.getPrivateTaskFolder());
        infostore = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), values.getPrivateInfostoreFolder());
        infostore2 = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), values.getPrivateInfostoreFolder());


        files = new ArrayList<DefaultFile>(NUM_FILES);
        long now = System.currentTimeMillis();
        for (int i = 0; i < NUM_FILES; i++) {
            FolderObject parent = i % 2 == 0 ? infostore : infostore2;
            DefaultFile file = new DefaultFile();
            file.setFolderId(String.valueOf(parent.getObjectID()));
            file.setTitle("NewTest_" + now + "_" + i);
            file.setDescription(file.getTitle());
            itm.newAction(file);
            files.add(file);
        }
    }

    public void testShareMultipleFoldersInternally() throws Exception {
        List<ShareTarget> targets = new ArrayList<ShareTarget>(4);
        targets.add(new ShareTarget(Module.CALENDAR.getFolderConstant(), Integer.toString(calendar.getObjectID())));
        targets.add(new ShareTarget(Module.CONTACTS.getFolderConstant(), Integer.toString(contacts.getObjectID())));
        targets.add(new ShareTarget(Module.TASK.getFolderConstant(), Integer.toString(tasks.getObjectID())));
        targets.add(new ShareTarget(Module.INFOSTORE.getFolderConstant(), Integer.toString(infostore.getObjectID())));

        InternalRecipient recipient = new InternalRecipient();
        int userId2 = client2.getValues().getUserId();
        recipient.setEntity(userId2);
        recipient.setBits(FOLDER_READ_PERMISSION);

        client.execute(new InviteRequest(targets, Collections.<ShareRecipient>singletonList(recipient)));

        /*
         * Reload folders with second client and check permissions
         */
        checkFolderPermission(userId2, FOLDER_READ_PERMISSION, getFolder(EnumAPI.OX_NEW, calendar.getObjectID(), client2));
        checkFolderPermission(userId2, FOLDER_READ_PERMISSION, getFolder(EnumAPI.OX_NEW, contacts.getObjectID(), client2));
        checkFolderPermission(userId2, FOLDER_READ_PERMISSION, getFolder(EnumAPI.OX_NEW, tasks.getObjectID(), client2));
        checkFolderPermission(userId2, FOLDER_READ_PERMISSION, getFolder(EnumAPI.OX_NEW, infostore.getObjectID(), client2));
    }

    public void testShareSingleObjectInternally() throws Exception {
        DefaultFile file = files.get(0);
        ShareTarget target = new ShareTarget(Module.INFOSTORE.getFolderConstant(), file.getFolderId(), file.getId());
        InternalRecipient recipient = new InternalRecipient();
        int userId2 = client2.getValues().getUserId();
        recipient.setEntity(userId2);
        recipient.setBits(FOLDER_READ_PERMISSION);

        client.execute(new InviteRequest(Collections.<ShareTarget>singletonList(target), Collections.<ShareRecipient>singletonList(recipient)));
        checkFilePermission(userId2, ObjectPermission.READ, itm.getAction(file.getId()));
    }

    public void testShareMultipleFoldersAndFilesInAndExternally() throws Exception {
        List<ShareTarget> targets = new ArrayList<ShareTarget>(3 + NUM_FILES);
        targets.add(new ShareTarget(Module.CALENDAR.getFolderConstant(), Integer.toString(calendar.getObjectID())));
        targets.add(new ShareTarget(Module.CONTACTS.getFolderConstant(), Integer.toString(contacts.getObjectID())));
        targets.add(new ShareTarget(Module.TASK.getFolderConstant(), Integer.toString(tasks.getObjectID())));
        for (DefaultFile file : files) {
            targets.add(new ShareTarget(Module.INFOSTORE.getFolderConstant(), file.getFolderId(), file.getId()));
        }

        InternalRecipient internalRecipient = new InternalRecipient();
        int userId2 = client2.getValues().getUserId();
        internalRecipient.setEntity(userId2);
        internalRecipient.setBits(FOLDER_READ_PERMISSION);
        AnonymousRecipient anonymousRecipient = new AnonymousRecipient();
        anonymousRecipient.setBits(FOLDER_READ_PERMISSION);
        anonymousRecipient.setPassword("1234");
        List<ShareRecipient> recipients = new ArrayList<ShareRecipient>(2);
        recipients.add(internalRecipient);
        recipients.add(anonymousRecipient);

        client.execute(new InviteRequest(targets, recipients));

        /*
         * Assert that all shares have been created and create client for anonymous guest
         */
        List<ParsedShare> allShares = client.execute(new AllRequest()).getParsedShares();
        List<ParsedShare> shares = getSharesForTargets(allShares, targets);
        assertEquals(targets.size(), shares.size());
        GuestClient guestClient = new GuestClient(shares.get(0).getShareURL(), null, anonymousRecipient.getPassword());

        /*
         * Check folder permissions for internal recipient
         */
        checkFolderPermission(userId2, FOLDER_READ_PERMISSION, getFolder(EnumAPI.OX_NEW, calendar.getObjectID(), client2));
        checkFolderPermission(userId2, FOLDER_READ_PERMISSION, getFolder(EnumAPI.OX_NEW, contacts.getObjectID(), client2));
        checkFolderPermission(userId2, FOLDER_READ_PERMISSION, getFolder(EnumAPI.OX_NEW, tasks.getObjectID(), client2));

        /*
         * Check folder permissions for guest recipient
         */
        int guestUserId = guestClient.getValues().getUserId();
        checkFolderPermission(guestUserId, FOLDER_READ_PERMISSION, getFolder(EnumAPI.OX_NEW, calendar.getObjectID(), guestClient));
        checkFolderPermission(guestUserId, FOLDER_READ_PERMISSION, getFolder(EnumAPI.OX_NEW, contacts.getObjectID(), guestClient));
        checkFolderPermission(guestUserId, FOLDER_READ_PERMISSION, getFolder(EnumAPI.OX_NEW, tasks.getObjectID(), guestClient));

        /*
         * Check object permissions for internal recipient
         */
        List<ListItem> listItems = new ArrayList<ListItem>(NUM_FILES);
        for (DefaultFile file : files) {
            listItems.add(new ListItem(Integer.toString(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID), new FileID(file.getId()).getFileId()));
        }
        ListInfostoreResponse listResp = client2.execute(new ListInfostoreRequest(listItems, Metadata.columns(Metadata.HTTPAPI_VALUES_ARRAY)));
        assertListResponse(listResp, userId2, ObjectPermission.READ);

        /*
         * Check object permissions for guest recipient
         */
        listResp = guestClient.execute(new ListInfostoreRequest(listItems, Metadata.columns(Metadata.HTTPAPI_VALUES_ARRAY)));
        assertListResponse(listResp, guestUserId, ObjectPermission.READ);
    }

    private void assertListResponse(ListInfostoreResponse listResp, int entity, int permissionBits) {
        Object[][] objects = listResp.getArray();
        for (int i = 0; i < NUM_FILES; i++) {
            Object[] doc = objects[i];
            DefaultFile file = files.get(i);
            FileID fileID = new FileID(file.getId());
            fileID.setFolderId(Integer.toString(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID));
            assertEquals(fileID.toUniqueID(), (String) doc[listResp.getColumnPos(Metadata.ID)]);
            List<FileStorageObjectPermission> permissions = (List<FileStorageObjectPermission>) doc[listResp.getColumnPos(Metadata.OBJECT_PERMISSIONS)];
            boolean foundValidGuestPermission = false;
            for (FileStorageObjectPermission permission : permissions) {
                if (permission.getEntity() == entity && permission.getPermissions() == permissionBits) {
                    foundValidGuestPermission = true;
                }
            }
            assertTrue(foundValidGuestPermission);
        }
    }

    private List<ParsedShare> getSharesForTargets(List<ParsedShare> allShares, List<ShareTarget> targets) {
        List<ParsedShare> shares = new LinkedList<ParsedShare>();
        for (ParsedShare ps : allShares) {
            for (ShareTarget target : targets) {
                if (target.equals(ps.getTarget())) {
                    shares.add(ps);
                    break;
                }
            }
        }

        return shares;
    }

    private void checkFilePermission(int entity, int expectedBits, File file) {
        List<FileStorageObjectPermission> objectPermissions = file.getObjectPermissions();
        if (objectPermissions != null) {
            for (FileStorageObjectPermission permission : objectPermissions) {
                if (permission.getEntity() == entity) {
                    assertEquals(expectedBits, permission.getPermissions());
                    return;
                }
            }
        }

        fail("Did not find permission for entity " + entity);
    }

    private void checkFolderPermission(int entity, int expectedBits, FolderObject folder) {
        for (OCLPermission permission : folder.getPermissions()) {
            if (permission.getEntity() == entity) {
                assertEquals(expectedBits, Permissions.createPermissionBits(
                    permission.getFolderPermission(),
                    permission.getReadPermission(),
                    permission.getWritePermission(),
                    permission.getDeletePermission(),
                    permission.isFolderAdmin()));
                return;
            }
        }

        fail("Did not find permission for entity " + entity);
    }

    @Override
    public void tearDown() throws Exception {
        if (itm != null) {
            itm.cleanUp();
        }
        super.tearDown();
    }

}
