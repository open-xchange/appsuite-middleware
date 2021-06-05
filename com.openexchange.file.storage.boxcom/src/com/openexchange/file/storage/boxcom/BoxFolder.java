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

package com.openexchange.file.storage.boxcom;

import java.util.Collections;
import java.util.Date;
import com.box.sdk.BoxFolder.Info;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.TypeAware;

/**
 * {@link BoxFolder}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class BoxFolder extends DefaultFileStorageFolder implements TypeAware {

    private FileStorageFolderType type;

    /**
     * Initializes a new {@link BoxFolder}.
     */
    public BoxFolder(final int userId) {
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
    public BoxFolder setType(FileStorageFolderType type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return id == null ? super.toString() : id;
    }

    /**
     * Parses specified Box.com directory.
     *
     * @param dir The Box.com directory
     * @param rootFolderId The identifier of the root folder
     * @param accountDisplayName The account's display name
     * @param hasSubfolders <code>true</code> if this folder has sub-folders; otherwise <code>false</code>
     * @throws OXException If parsing Box.com directory fails
     */
    public BoxFolder parseDirEntry(Info dir, String rootFolderId, String accountDisplayName, boolean hasSubfolders) throws OXException {
        if (null != dir) {
            try {
                id = dir.getID();
                rootFolder = isRootFolder(dir.getID(), rootFolderId);
                b_rootFolder = true;

                if (rootFolder) {
                    id = FileStorageFolder.ROOT_FULLNAME;
                    setParentId(null);
                    setName(null == accountDisplayName ? dir.getName() : accountDisplayName);
                } else {
                    Info parent = dir.getParent();
                    // A shared folder does not have a parent in the current user's "context"
                    if (parent == null) {
                        setParentId(FileStorageFolder.ROOT_FULLNAME);
                    } else {
                        setParentId(isRootFolder(parent.getID(), rootFolderId) ? FileStorageFolder.ROOT_FULLNAME : parent.getID());
                    }
                    setName(dir.getName());
                }

                {
                    Date createdAt = dir.getCreatedAt();
                    if (null != createdAt) {
                        creationDate = createdAt;
                    }
                }
                {
                    Date modifiedAt = dir.getModifiedAt();
                    if (null != modifiedAt) {
                        lastModifiedDate = modifiedAt;
                    }
                }

                setSubfolders(hasSubfolders);
                setSubscribedSubfolders(hasSubfolders);
            } catch (RuntimeException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return this;
    }

    private static boolean isRootFolder(String id, String rootFolderId) {
        return "0".equals(id) || rootFolderId.equals(id);
    }

}
