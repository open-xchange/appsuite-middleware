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

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.file.storage.DefaultFileStorageGuestObjectPermission;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.share.recipient.ShareRecipient;

/**
 * {@link AbstractSharedFilesTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public abstract class AbstractSharedFilesTest extends ShareTest {

    protected static final String SHARED_FOLDER = "10";

    protected InfostoreTestManager infoMgr;
    protected FolderObject userSourceFolder;
    protected FolderObject userDestFolder;
    protected File file;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        userSourceFolder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder());

        file = insertFile(userSourceFolder.getObjectID(), randomUID());
        infoMgr = new InfostoreTestManager(getClient());
    }

    protected AbstractSharedFilesTest() {
        super();
    }

    protected void addUserPermission(int userId) {
        List<FileStorageObjectPermission> permissions = new ArrayList<FileStorageObjectPermission>(2);
        permissions.add(new DefaultFileStorageObjectPermission(userId, false, FileStorageObjectPermission.WRITE)); //shared to internal user
        file.getObjectPermissions().addAll(permissions);
    }

    protected void addGuestPermission(ShareRecipient shareRecipient) {
        List<FileStorageObjectPermission> permissions = new ArrayList<FileStorageObjectPermission>(2);

        DefaultFileStorageGuestObjectPermission guestPermission = new DefaultFileStorageGuestObjectPermission();
        guestPermission.setPermissions(ObjectPermission.WRITE);
        guestPermission.setRecipient(shareRecipient);
        permissions.add(guestPermission);

        file.getObjectPermissions().addAll(permissions);
    }

    protected static String sharedFileId(String fileId) {
        FileID tmp = new FileID(fileId);
        tmp.setFolderId(SHARED_FOLDER);
        return tmp.toUniqueID();
    }
}
