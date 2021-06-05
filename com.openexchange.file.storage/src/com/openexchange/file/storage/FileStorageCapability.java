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

package com.openexchange.file.storage;

/**
 * {@link FileStorageCapability}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public enum FileStorageCapability {

    /**
     * Support for sequence numbers of files and folders.
     */
    SEQUENCE_NUMBERS,

    /**
     * Support for saving files without creating a new version.
     */
    IGNORABLE_VERSION,

    /**
     * Support for storing multiple versions of a document.
     */
    FILE_VERSIONS,

    /**
     * Support for reading and writing files at specific offsets.
     */
    RANDOM_FILE_ACCESS,

    /**
     * Support for searching files by advanced search terms.
     */
    SEARCH_BY_TERM,

    /**
     * Support for E-Tags of folders.
     */
    FOLDER_ETAGS,

    /**
     * Support for recursive E-Tags of folders.
     */
    RECURSIVE_FOLDER_ETAGS,

    /**
     * Support for thumbnail images of files.
     */
    THUMBNAIL_IMAGES,

    /**
     * Support for persistent folder- and file-IDs, i.e. identifiers don't change during rename operations.
     */
    PERSISTENT_IDS,

    /**
     * Support for efficient retrieval of file metadata and contents considering a client-supplied E-Tag.
     */
    EFFICIENT_RETRIEVAL,

    /**
     * Support for locking/unlocking files.
     */
    LOCKS,

    /**
     * Support for individual permissions per file.
     */
    OBJECT_PERMISSIONS,

    /**
     * Support for pagination/ranges.
     */
    RANGES,

    /**
     * Support for storing extended metadata attributes like notes or categories for files.
     */
    EXTENDED_METADATA,

    /**
     * Support for moving multiple files at once.
     */
    MULTI_MOVE,

    /**
     * File storage only supports read-only access
     */
    READ_ONLY,

    /**
     * File storage contains mail attachments
     */
    MAIL_ATTACHMENTS,

    /**
     * Automatic add new file version if file already exists
     */
    AUTO_NEW_VERSION,

    /**
     * A folder's content can be downloaded as a ZIP archive
     */
    ZIPPABLE_FOLDER,

    /**
     * The file count is known for a folder.
     * <p>
     * Not supporting that capability typically means that there is too much performance overhead in order to determine the file count.
     */
    COUNT_TOTAL,

    /**
     * Signals that file names and folder names are treated case insensitive; e.g. a file named <code>"abc.txt"</code> is considered equal to <code>"ABC.txt"</code> and will cause a conflict.
     */
    CASE_INSENSITIVE,

    /**
     * Signals that folder names are automatically renamed in case a conflict occurs; e.g. a conflicting folder named <code>"abc"</code> will be changed to <code>"abc (1)"</code>.
     */
    AUTO_RENAME_FOLDERS,

    /**
     * Has the capability to restore files from trash folder to their original location
     */
    RESTORE,

    /**
     * Support for generating a direct link back to a file or folder's in its original client interface.
     */
    BACKWARD_LINK,

    /**
     * Support for searching folders by name.
     */
    SEARCH_IN_FOLDER_NAME(false),

    ;

    private final boolean fileAccessCapability;

    private FileStorageCapability() {
        this(true);
    }

    private FileStorageCapability(boolean fileAccessCapability) {
        this.fileAccessCapability = fileAccessCapability;
    }

    /**
     * Checks if this capability is a file access capability.
     *
     * @return <code>true</code> for file access capability; otherwise <code>false</code>
     */
    public boolean isFileAccessCapability() {
        return fileAccessCapability;
    }

    /**
     * Checks if this capability is a folder access capability.
     *
     * @return <code>true</code> folder access capability; otherwise <code>false</code>
     */
    public boolean isFolderAccessCapability() {
        return fileAccessCapability == false;
    }
}
