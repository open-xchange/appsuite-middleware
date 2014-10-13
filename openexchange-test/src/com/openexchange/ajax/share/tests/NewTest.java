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
import java.util.List;
import com.openexchange.ajax.FolderTest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.NewRequest;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.modules.Module;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.InternalRecipient;
import com.openexchange.share.recipient.ShareRecipient;


/**
 * {@link NewTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class NewTest extends ShareTest {

    private FolderObject calendar;
    private FolderObject contacts;
    private FolderObject infostore;
//    private FolderObject tasks;
    private AJAXClient client2;
    private InfostoreTestManager itm;
    private DefaultFile file;

    /**
     * Initializes a new {@link NewTest}.
     * @param name
     */
    public NewTest(String name) {
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
        infostore = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), values.getPrivateInfostoreFolder());

        file = new DefaultFile();
        file.setFolderId(String.valueOf(infostore.getObjectID()));
        file.setTitle("test knowledge");
        file.setDescription("test knowledge description");
        itm.newAction(file);
        //        tasks = insertPrivateFolder(EnumAPI.OX_NEW, Module.TASK.getFolderConstant(), values.getPrivateTaskFolder());
    }

    public void testShareMultipleFoldersInternally() throws Exception {
        List<ShareTarget> targets = new ArrayList<ShareTarget>(3);
        targets.add(new ShareTarget(Module.CALENDAR.getFolderConstant(), Integer.toString(calendar.getObjectID())));
        targets.add(new ShareTarget(Module.CONTACTS.getFolderConstant(), Integer.toString(contacts.getObjectID())));
        targets.add(new ShareTarget(Module.INFOSTORE.getFolderConstant(), Integer.toString(infostore.getObjectID())));

        InternalRecipient recipient = new InternalRecipient();
        int userId2 = client2.getValues().getUserId();
        recipient.setEntity(userId2);
        int permissions = FolderTest.createPermissionBits(
            OCLPermission.READ_FOLDER,
            OCLPermission.READ_ALL_OBJECTS,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS,
            false);
        recipient.setBits(permissions);

        client.execute(new NewRequest(targets, Collections.<ShareRecipient>singletonList(recipient)));

        /*
         * Reload folders with second client and check permissions
         */
        checkFolderPermission(userId2, permissions, getFolder(EnumAPI.OX_NEW, calendar.getObjectID(), client2));
        checkFolderPermission(userId2, permissions, getFolder(EnumAPI.OX_NEW, contacts.getObjectID(), client2));
        checkFolderPermission(userId2, permissions, getFolder(EnumAPI.OX_NEW, infostore.getObjectID(), client2));
    }

    public void testShareSingleObjectInternally() throws Exception {
        ShareTarget target = new ShareTarget(Module.INFOSTORE.getFolderConstant(), Integer.toString(infostore.getObjectID()), file.getId());
        InternalRecipient recipient = new InternalRecipient();
        int userId2 = client2.getValues().getUserId();
        recipient.setEntity(userId2);
        recipient.setBits(ObjectPermission.READ);

        client.execute(new NewRequest(Collections.<ShareTarget>singletonList(target), Collections.<ShareRecipient>singletonList(recipient)));
        checkFilePermission(userId2, ObjectPermission.READ, itm.getAction(file.getId()));
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
                assertEquals(expectedBits, FolderTest.createPermissionBits(
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
