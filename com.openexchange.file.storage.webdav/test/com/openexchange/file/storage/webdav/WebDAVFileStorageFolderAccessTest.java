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

package com.openexchange.file.storage.webdav;

import java.util.Collections;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStoragePermission;


/**
 * {@link WebDAVFileStorageFolderAccessTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class WebDAVFileStorageFolderAccessTest extends AbstractWebDAVFileStorageTest {

    /**
     * Initializes a new {@link WebDAVFileStorageFolderAccessTest}.
     */
    public WebDAVFileStorageFolderAccessTest() {
        super();
    }

    public void testCreateFolder() throws Exception {
        final WebDAVFileStorageAccountAccess accountAccess = getAccountAccess();
        accountAccess.connect();
        try {
            final FileStorageFolderAccess folderAccess = accountAccess.getFolderAccess();

            final DefaultFileStorageFolder folder = new DefaultFileStorageFolder();
            final String name = "TestFolder" + System.currentTimeMillis();
            folder.setName(name);
            final String parentId = folderAccess.getRootFolder().getId();
            folder.setParentId(parentId);
            final DefaultFileStoragePermission p = DefaultFileStoragePermission.newInstance();
            p.setMaxPermissions();
            p.setEntity(accountAccess.getSession().getUserId());
            p.setGroup(false);
            folder.setPermissions(Collections.<FileStoragePermission> singletonList(p));
            folder.setSubscribed(true);

            final String newId = folderAccess.createFolder(folder);
            try {
                /*
                 * Check its presence
                 */
                FileStorageFolder[] subfolders = folderAccess.getSubfolders(parentId, true);
                boolean found = false;
                for (FileStorageFolder fileStorageFolder : subfolders) {
                    if (newId.equals(fileStorageFolder.getId())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    fail("Newly created folder \"" + newId + "\" not found");
                }
            } finally {
                folderAccess.deleteFolder(newId, true);
            }

        } finally {
            accountAccess.close();
        }
    }

    public void testRenameFolder() throws Exception {
        final WebDAVFileStorageAccountAccess accountAccess = getAccountAccess();
        accountAccess.connect();
        try {
            final FileStorageFolderAccess folderAccess = accountAccess.getFolderAccess();
            /*
             * Create a new folder
             */
            final DefaultFileStorageFolder folder = new DefaultFileStorageFolder();
            final String name = "TestFolder" + System.currentTimeMillis();
            folder.setName(name);
            final String parentId = folderAccess.getRootFolder().getId();
            folder.setParentId(parentId);
            final DefaultFileStoragePermission p = DefaultFileStoragePermission.newInstance();
            p.setMaxPermissions();
            p.setEntity(accountAccess.getSession().getUserId());
            p.setGroup(false);
            folder.setPermissions(Collections.<FileStoragePermission> singletonList(p));
            folder.setSubscribed(true);
            String newId = folderAccess.createFolder(folder);
            try {
                /*
                 * Check its presence
                 */
                FileStorageFolder[] subfolders = folderAccess.getSubfolders(parentId, true);
                boolean found = false;
                for (FileStorageFolder fileStorageFolder : subfolders) {
                    if (newId.equals(fileStorageFolder.getId())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    fail("Newly created folder \"" + newId + "\" not found");
                }
                /*
                 * Rename it
                 */
                newId = folderAccess.renameFolder(newId, "Renamed" + name);
                /*
                 * Check its presence
                 */
                subfolders = folderAccess.getSubfolders(parentId, true);
                found = false;
                for (FileStorageFolder fileStorageFolder : subfolders) {
                    if (newId.equals(fileStorageFolder.getId())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    fail("Renamed folder \"" + newId + "\" not found");
                }
            } finally {
                folderAccess.deleteFolder(newId, true);
            }
        } finally {
            accountAccess.close();
        }
    }

}
