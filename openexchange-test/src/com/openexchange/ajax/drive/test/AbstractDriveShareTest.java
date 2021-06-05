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

package com.openexchange.ajax.drive.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import jonelo.jacksum.algorithm.MD;

/**
 * {@link AbstractDriveShareTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public abstract class AbstractDriveShareTest extends ShareTest {

    protected AbstractDriveShareTest() {
        // TODO Auto-generated constructor stub
        super();
    }

    protected void checkFilePermission(int entity, int expectedBits, File file) {
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

    protected void checkFolderPermission(int entity, int expectedBits, FolderObject folder) {
        for (OCLPermission permission : folder.getPermissions()) {
            if (permission.getEntity() == entity) {
                assertEquals(expectedBits, Permissions.createPermissionBits(permission.getFolderPermission(), permission.getReadPermission(), permission.getWritePermission(), permission.getDeletePermission(), permission.isFolderAdmin()));
                return;
            }
        }

        fail("Did not find permission for entity " + entity);
    }

    protected String getId(DefaultFile file) {
        FileID fileID = new FileID(file.getId());
        fileID.setFolderId(Integer.toString(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID));
        return fileID.toUniqueID();
    }

    protected String getChecksum(java.io.File file) throws Exception {
        InputStream document = new FileInputStream(file);

        byte[] buffer = new byte[2048];
        MD md5 = new MD("MD5");
        int read;
        do {
            read = document.read(buffer);
            if (0 < read) {
                md5.update(buffer, 0, read);
            }
        } while (-1 != read);

        document.close();

        return md5.getFormattedValue();
    }

}
