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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.ajax.infostore.actions.AllInfostoreRequest;
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
    private List<DefaultFile> files;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        testFolder = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), getClient().getValues().getPrivateInfostoreFolder());
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

    @Test
    public void testShareItemsInFolder() throws Exception {
        DefaultFileStorageGuestObjectPermission permission = new DefaultFileStorageGuestObjectPermission();
        permission.setPermissions(ObjectPermission.READ);
        permission.setRecipient(new AnonymousRecipient());
        Random r = new Random();
        List<DefaultFile> sharedFiles = new ArrayList<DefaultFile>(files.size());
        for (DefaultFile file : files) {
            if (r.nextBoolean()) {
                file.setObjectPermissions(Collections.<FileStorageObjectPermission>singletonList(permission));
                itm.updateAction(file, new Field[] { Field.OBJECT_PERMISSIONS }, new Date(Long.MAX_VALUE));
                assertFalse(itm.getLastResponse().getErrorMessage(), itm.getLastResponse().hasError());
                sharedFiles.add(file);
            }
        }

        List<FileShare> fileShares = new ArrayList<FileShare>(sharedFiles.size());
        List<FileShare> allShares = getClient().execute(new FileSharesRequest()).getShares(getClient().getValues().getTimeZone());
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
            GuestClient guestClient = resolveShare(guest, permission.getRecipient(), null);
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
