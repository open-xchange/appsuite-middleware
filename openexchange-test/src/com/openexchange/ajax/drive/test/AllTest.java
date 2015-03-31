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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.ajax.drive.test;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.drive.action.AllRequest;
import com.openexchange.ajax.drive.action.InviteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.drive.DriveShareTarget;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.modules.Module;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.InternalRecipient;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.TestInit;

/**
 * {@link AllTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class AllTest extends AbstractDriveShareTest {

    private InfostoreTestManager itm;
    private FolderObject rootFolder;
    private FolderObject folder;
    private DefaultFile file;
    private AJAXClient client2;
    private FolderTestManager ftm2;
    private FolderObject folder2;

    private static final int FOLDER_READ_PERMISSION = Permissions.createPermissionBits(
        Permission.READ_FOLDER,
        Permission.READ_ALL_OBJECTS,
        Permission.NO_PERMISSIONS,
        Permission.NO_PERMISSIONS,
        false);

    private static String PASSWORD = "password123";

    public AllTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client2 = new AJAXClient(User.User2);
        itm = new InfostoreTestManager(client);
        ftm2 = new FolderTestManager(client2);

        UserValues values = client.getValues();
        rootFolder = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), values.getPrivateInfostoreFolder());
        folder = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), rootFolder.getObjectID());
        folder2 = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), rootFolder.getObjectID());

        long now = System.currentTimeMillis();
        file = new DefaultFile();
        file.setFolderId(String.valueOf(folder2.getObjectID()));
        file.setTitle("GetLinkTest_" + now);
        file.setFileName(file.getTitle());
        file.setDescription(file.getTitle());
        itm.newAction(file, new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile")));
    }

    public void testAll() throws Exception {
        List<DriveShareTarget> targets = new ArrayList<DriveShareTarget>();
        DriveShareTarget fileTarget = new DriveShareTarget();
        fileTarget.setPath(folder2.getFolderName());
        fileTarget.setName(file.getFileName());
        targets.add(fileTarget);
        DriveShareTarget folderTarget = new DriveShareTarget();
        folderTarget.setPath(folder.getFolderName());
        targets.add(folderTarget);

        List<ShareRecipient> recipients = new ArrayList<ShareRecipient>();
        AnonymousRecipient anonymous = new AnonymousRecipient();
        anonymous.setBits(FOLDER_READ_PERMISSION);
        anonymous.setPassword(PASSWORD);
        recipients.add(anonymous);
        InternalRecipient internalRecipient = new InternalRecipient();
        internalRecipient.setEntity(client2.getValues().getUserId());
        internalRecipient.setBits(FOLDER_READ_PERMISSION);
        recipients.add(internalRecipient);

        InviteRequest inviteRequest = new InviteRequest(rootFolder.getObjectID(), targets, recipients);
        client.execute(inviteRequest);

        List<ParsedShare> allShares = client.execute(new AllRequest(rootFolder.getObjectID())).getParsedShares();
        ParsedShare anonymousFile = null;
        ParsedShare anonymousFolder = null;
        ParsedShare internalFile = null;
        ParsedShare internalFolder = null;

        for (ParsedShare parsedShare : allShares) {
            if (parsedShare.getTarget().equals(fileTarget)) {
                if (parsedShare.getRecipient().getType() == RecipientType.ANONYMOUS) {
                    anonymousFile = parsedShare;
                } else if (parsedShare.getRecipient().equals(internalRecipient)) {
                    internalFile = parsedShare;
                }
            } else if (parsedShare.getTarget().equals(folderTarget)) {
                if (parsedShare.getRecipient().getType() == RecipientType.ANONYMOUS) {
                    anonymousFolder = parsedShare;
                } else if (parsedShare.getRecipient().equals(internalRecipient)) {
                    internalFolder = parsedShare;
                }
            }
        }

        assertNotNull("Missing share.", anonymousFile);
        assertNotNull("Missing share.", anonymousFolder);

        GuestClient guestClient = new GuestClient(anonymousFile.getShareURL(), null, anonymous.getPassword());
        InfostoreTestManager itmGuest = new InfostoreTestManager(guestClient);
        checkFilePermission(guestClient.getValues().getUserId(), ObjectPermission.READ, itmGuest.getAction(getId(file)));
        FolderTestManager ftmGuest = new FolderTestManager(guestClient);
        checkFolderPermission(guestClient.getValues().getUserId(), FOLDER_READ_PERMISSION, ftmGuest.getFolderFromServer(folder.getObjectID()));

        //        No need to check internal shares (yet).
        //
        //        assertNotNull("Missing share.", internalFile);
        //        assertNotNull("Missing share.", internalFolder);
    }

    //    public void testInviteFolderExternal() throws Exception {
    //        ShareTarget target = new ShareTarget(FolderObject.INFOSTORE, "/" + folder.getFolderName());
    //        AnonymousRecipient recipient = new AnonymousRecipient();
    //        recipient.setBits(FOLDER_READ_PERMISSION);
    //        recipient.setPassword(PASSWORD);
    //        recipient.setBits(FOLDER_READ_PERMISSION);
    //        InviteRequest inviteRequest = new InviteRequest(rootFolder.getObjectID(), Collections.<ShareTarget> singletonList(target), Collections.<ShareRecipient> singletonList(recipient));
    //        client.execute(inviteRequest);
    //
    //        List<ParsedShare> allShares = client.execute(new AllRequest(rootFolder.getObjectID())).getParsedShares();
    //        ParsedShare share = null;
    //        for (ParsedShare parsedShare : allShares) {
    //            if (parsedShare.getTarget().equals(target)) {
    //                share = parsedShare;
    //                break;
    //            }
    //        }
    //        assertNotNull("Missing share.", share);
    //        
    //        System.out.println(share.getShareURL());
    //
    //        GuestClient guestClient = new GuestClient(share.getShareURL(), null, recipient.getPassword());
    //        FolderTestManager ftmGuest = new FolderTestManager(guestClient);
    //        checkFolderPermission(guestClient.getValues().getUserId(), FOLDER_READ_PERMISSION, ftmGuest.getFolderFromServer(folder.getObjectID()));
    //    }

    @Override
    public void tearDown() throws Exception {
        itm.cleanUp();
        super.tearDown();
    }

}
