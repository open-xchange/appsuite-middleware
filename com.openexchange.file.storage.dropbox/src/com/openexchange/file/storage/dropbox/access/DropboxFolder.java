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

package com.openexchange.file.storage.dropbox.access;

import java.util.Collections;
import com.dropbox.core.v2.files.FolderMetadata;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.TypeAware;

/**
 * {@link DropboxFolder}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropboxFolder extends DefaultFileStorageFolder implements TypeAware {

    private FileStorageFolderType type;

    /**
     * Initialises a new {@link DropboxFolder}.
     *
     * @param metadata The {@link FolderMetadata} representing a Dropbox folder
     * @param userId the user identifier
     * @param accountDisplayName The display name of the Dropbox account
     */
    public DropboxFolder(FolderMetadata metadata, int userId, String accountDisplayName, boolean hasSubFolders) {
        this(userId);
        parseMetadata(metadata, accountDisplayName);
        setSubfolders(hasSubFolders);
        setSubscribedSubfolders(hasSubFolders);
    }

    /**
     * Initialises a new {@link DropboxFolder}.
     *
     * @param userId the user identifier
     */
    public DropboxFolder(int userId) {
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

    /**
     * Parses the specified {@link FolderMetadata}
     *
     * @param metadata The {@link FolderMetadata} to parse
     * @param accountDisplayName The account's display name
     */
    private void parseMetadata(FolderMetadata metadata, String accountDisplayName) {
        if (metadata == null) {
            return;
        }
        String path = metadata.getPathDisplay();
        id = path;

        if ("/".equals(path)) {
            rootFolder = true;
            id = FileStorageFolder.ROOT_FULLNAME;
            setParentId(null);
            setName(null == accountDisplayName ? "" : accountDisplayName);
        } else {
            rootFolder = false;
            final int pos = path.lastIndexOf('/');
            setParentId(pos < 0 ? FileStorageFolder.ROOT_FULLNAME : path.substring(0, pos));
            setName(pos < 0 ? path : path.substring(pos + 1));
        }
        b_rootFolder = true;
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
    public DropboxFolder setType(FileStorageFolderType type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return id == null ? super.toString() : id;
    }
}
