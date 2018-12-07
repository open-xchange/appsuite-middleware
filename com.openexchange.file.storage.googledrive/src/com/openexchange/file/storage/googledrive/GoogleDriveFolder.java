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

package com.openexchange.file.storage.googledrive;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.List;
import com.google.api.services.drive.model.File;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.TypeAware;

/**
 * {@link GoogleDriveFolder}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GoogleDriveFolder extends DefaultFileStorageFolder implements TypeAware {

    private static final String QUERY_STRING_DIRECTORIES_ONLY = GoogleDriveConstants.QUERY_STRING_DIRECTORIES_ONLY;

    // ---------------------------------------------------------------------------------------------------------------------- //

    private FileStorageFolderType type;

    /**
     * Initializes a new {@link GoogleDriveFolder}.
     */
    public GoogleDriveFolder(final int userId) {
        super();
        type = FileStorageFolderType.NONE;
        holdsFiles = true;
        b_holdsFiles = true;
        holdsFolders = true;
        b_holdsFolders = true;
        exists = true;
        subscribed = true;
        b_subscribed = true;
        final DefaultFileStoragePermission permission = DefaultFileStoragePermission.newInstance();
        permission.setEntity(userId);
        permissions = Collections.<FileStoragePermission> singletonList(permission);
        ownPermission = permission;
        createdBy = userId;
        modifiedBy = userId;
    }

    @Override
    public FileStorageFolderType getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type The type to set
     * @return This folder with type applied
     */
    public GoogleDriveFolder setType(FileStorageFolderType type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return id == null ? super.toString() : id;
    }

    /**
     * Parses specified Google Drive directory.
     *
     * @param dir The Google Drive directory
     * @param rootFolderId The identifier of the root folder
     * @param accountDisplayName The account's display name
     * @param useOptimisticSubfolderDetection Whether to use optimistic sub-folder detection
     * @param drive The Drive reference
     * @throws OXException If parsing Google Drive directory fails
     */
    public GoogleDriveFolder parseDirEntry(File dir, String rootFolderId, String accountDisplayName, boolean useOptimisticSubfolderDetection, Drive drive) throws OXException, IOException {
        if (null != dir) {
            try {
                id = dir.getId();
                rootFolder = isRootFolder(dir.getId(), rootFolderId);
                b_rootFolder = true;

                if (rootFolder) {
                    id = FileStorageFolder.ROOT_FULLNAME;
                    setParentId(null);
                    setName(null == accountDisplayName ? dir.getName() : accountDisplayName);
                } else {
                    String tmp = dir.getParents().get(0);
                    setParentId(isRootFolder(tmp, rootFolderId) ? FileStorageFolder.ROOT_FULLNAME : tmp);
                    setName(dir.getName());
                }

                if (null != dir.getCreatedTime()) {
                    creationDate = new Date(dir.getCreatedTime().getValue());
                }
                if (null != dir.getModifiedTime()) {
                    lastModifiedDate = new Date(dir.getModifiedTime().getValue());
                }

                {
                    final boolean hasSubfolders = useOptimisticSubfolderDetection ? true : hasSubfolder(dir, drive);
                    setSubfolders(hasSubfolders);
                    setSubscribedSubfolders(hasSubfolders);
                }
            } catch (final RuntimeException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return this;
    }

    private static boolean isRootFolder(String id, String rootFolderId) {
        return "root".equals(id) || rootFolderId.equals(id);
    }

    private boolean hasSubfolder(File dir, Drive drive) throws IOException {
        List list = drive.files().list();
        list.setQ(new GoogleFileQueryBuilder(QUERY_STRING_DIRECTORIES_ONLY).searchForChildren(dir.getId()).build());
        list.setPageSize(Integer.valueOf(1));
        return !list.execute().getFiles().isEmpty();
    }

}
