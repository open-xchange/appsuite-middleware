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

package com.openexchange.file.storage.infostore.folder;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.infostore.internal.Utils;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link FolderConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class FolderConverter {

    /**
     * Initializes a new {@link FolderConverter}.
     */
    public FolderConverter() {
        super();
    }

    /**
     * Converts a userized folder into its file storage folder equivalent.
     *
     * @param folder The userized folder to convert
     * @return The file storage folder
     */
    public UserizedFileStorageFolder getStorageFolder(UserizedFolder folder) throws OXException {
        if (null == folder) {
            return null;
        }
        try {
            return new UserizedFileStorageFolder(folder);
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Parses a folder from given file storage folder.
     *
     * @param storageFolder The file storage folder
     * @return The parsed folder
     * @throws OXException If parsing folder fails
     */
    public Folder getFolder(FileStorageFolder storageFolder) throws OXException {
        return FolderParser.parseFolder(storageFolder);
    }

    /**
     * Converts an array of userized folders into their file storage folder equivalents.
     *
     * @param folders The userized folders to convert
     * @return The file storage folders
     */
    public UserizedFileStorageFolder[] getStorageFolders(UserizedFolder[] folders) throws OXException {
        return getStorageFolders(folders, true);
    }

    /**
     * Converts an array of userized folders into their file storage folder equivalents.
     *
     * @param folders The userized folders to convert
     * @param infostoreOnly <code>true</code> to exclude folders from other modules, <code>false</code>, otherwise
     * @return The file storage folders
     */
    public UserizedFileStorageFolder[] getStorageFolders(UserizedFolder[] folders, boolean infostoreOnly) throws OXException {
        if (null == folders) {
            return null;
        }
        List<UserizedFileStorageFolder> fileStorageFolders = new ArrayList<UserizedFileStorageFolder>(folders.length);
        for (UserizedFolder folder : folders) {
            if (false == infostoreOnly || false == isNotInfostore(folder)) {
                fileStorageFolders.add(getStorageFolder(folder));
            }
        }
        return fileStorageFolders.toArray(new UserizedFileStorageFolder[fileStorageFolders.size()]);
    }

    /**
     * Gets a value indicating whether the supplied folder is no infostore or system folder.
     *
     * @param folder The folder to check
     * @return <code>true</code> if the folder is no infostore-, file- or system-folder, <code>false</code>, otherwise
     */
    protected boolean isNotInfostore(UserizedFolder folder) {
        if (null != folder) {
            ContentType contentType = folder.getContentType();
            if (null != contentType) {
                int module = contentType.getModule();
                if (FolderObject.INFOSTORE == module || FolderObject.FILE == module) {
                    return false;
                }
                if (FolderObject.SYSTEM_MODULE == module) {
                    try {
                        int numericalID = Utils.parseUnsignedInt(folder.getID());
                        if (FolderObject.SYSTEM_INFOSTORE_FOLDER_ID == numericalID || FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID == numericalID || FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID == numericalID) {
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        // no numerical identifier
                    }
                }
            }
        }
        return true;
    }

}
