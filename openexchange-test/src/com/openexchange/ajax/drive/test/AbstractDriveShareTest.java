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

package com.openexchange.ajax.drive.test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import jonelo.jacksum.algorithm.MD;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link AbstractDriveShareTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public abstract class AbstractDriveShareTest extends ShareTest {

    protected AbstractDriveShareTest(String name) {
        // TODO Auto-generated constructor stub
        super(name);
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
