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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.ajax.infostore.actions.AllInfostoreRequest;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.ajax.share.actions.FileShare;
import com.openexchange.ajax.share.actions.FileSharesRequest;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.DefaultFileStorageGuestObjectPermission;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.search.Order;
import com.openexchange.share.recipient.AnonymousRecipient;


/**
 * {@link FileStorageTransactionTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class FileStorageTransactionTest extends ShareTest {

    private static final int TEST_FILES = 10;

    private FolderObject testFolder;
    private InfostoreTestManager itm;
    private List<DefaultFile> files;

    /**
     * Initializes a new {@link FileStorageTransactionTest}.
     * @param name
     */
    public FileStorageTransactionTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        itm = new InfostoreTestManager(client);
        testFolder = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), client.getValues().getPrivateInfostoreFolder());
        files = new ArrayList<DefaultFile>(TEST_FILES);
        long now = System.currentTimeMillis();
        for (int i = 0; i < TEST_FILES; i++) {
            DefaultFile file = new DefaultFile();
            file.setFolderId(String.valueOf(testFolder.getObjectID()));
            file.setTitle("FileStorageTransactionTest_" + now + "_" + i);
            file.setDescription(file.getTitle());
            itm.newAction(file);
            files.add(file);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        if (itm != null) {
            itm.cleanUp();
        }
        super.tearDown();
    }

    public void testShareItemsInFolder() throws Exception {
        DefaultFileStorageGuestObjectPermission permission = new DefaultFileStorageGuestObjectPermission();
        permission.setPermissions(ObjectPermission.READ);
        permission.setRecipient(new AnonymousRecipient());
        Random r = new Random();
        List<DefaultFile> sharedFiles = new ArrayList<DefaultFile>(files.size());
        for (DefaultFile file : files) {
            if (r.nextBoolean()) {
                file.setObjectPermissions(Collections.<FileStorageObjectPermission>singletonList(permission));
                itm.updateAction(file, new Field[] { Field.OBJECT_PERMISSIONS }, new Date());
                sharedFiles.add(file);
            }
        }

        List<FileShare> fileShares = new ArrayList<FileShare>(sharedFiles.size());
        List<FileShare> allShares = client.execute(new FileSharesRequest()).getShares(client.getValues().getTimeZone());
        for (DefaultFile file : sharedFiles) {
            for (FileShare share : allShares) {
                if (share.getId().equals(file.getId())) {
                    fileShares.add(share);
                    break;
                }
            }
        }
        assertEquals("Wrong number of shares", sharedFiles.size(), fileShares.size());

        for (int i = 0; i < fileShares.size(); i++) {
            FileShare share = fileShares.get(i);
            /*
             * check access to share
             */
            assertNotNull(share.getExtendedPermissions());
            assertEquals(1, share.getExtendedPermissions().size());
            ExtendedPermissionEntity guest = share.getExtendedPermissions().get(0);
            checkGuestPermission(permission, guest);
            GuestClient guestClient =  resolveShare(guest, permission.getRecipient());
            guestClient.checkShareModuleAvailable();
            guestClient.checkShareAccessible(permission);
            /*
             * check file listing in folder 10 contains share target
             */
            AbstractColumnsResponse allResp = guestClient.execute(new AllInfostoreRequest(
                FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID,
                Metadata.columns(Metadata.HTTPAPI_VALUES_ARRAY),
                Metadata.ID,
                Order.ASCENDING));

            Object[][] docs = allResp.getArray();
            assertEquals(1, docs.length);
            assertEquals(guestClient.getItem(), docs[0][allResp.getColumnPos(Metadata.ID)]);
        }

    }

}
