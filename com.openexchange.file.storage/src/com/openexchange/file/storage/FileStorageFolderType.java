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
 * {@link FileStorageFolderType} - Enumeration of known folder types.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum FileStorageFolderType {

    /**
     * No special meaning associated with folder.
     */
    NONE,
    /**
     * Folder is current user's home directory.
     */
    HOME_DIRECTORY,
    /**
     * Folder is a public folder for current user.
     */
    PUBLIC_FOLDER,
    /**
     * Folder is a trash folder for current user.
     */
    TRASH_FOLDER,

    PICTURES_FOLDER,

    DOCUMENTS_FOLDER,

    MUSIC_FOLDER,

    VIDEOS_FOLDER,

    TEMPLATES_FOLDER
    ;

    public static final String PICTURES = "<PICTURES>";

    public static final String DOCUMENTS = "<DOCUMENTS>";

    public static final String MUSIC = "<MUSIC>";

    public static final String VIDEOS = "<VIDEOS>";

    public static final String TEMPLATES = "<TEMPLATES>";
}
