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

package com.openexchange.folderstorage.filestorage.contentType;

import com.openexchange.folderstorage.ContentType;

/**
 * {@link FileStorageContentType} - The folder storage content type for file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FileStorageContentType implements ContentType {

    private static final long serialVersionUID = 1548536482010132571L;

    private static final FileStorageContentType instance = new FileStorageContentType();

    /**
     * Gets the {@link FileStorageContentType} instance.
     *
     * @return The {@link FileStorageContentType} instance
     */
    public static FileStorageContentType getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link FileStorageContentType}.
     */
    protected FileStorageContentType() {
        super();
    }

    @Override
    public String toString() {
        // return "file";
        /*
         * TODO: Change to own content type when full file storage is supported. SEarch for this string to find all replacements.
         */
        return "infostore";
    }

    @Override
    public int getModule() {
        // From FolderObject.FILE
        /*
         * TODO: Change to own content type when full file storage is supported. SEarch for this string to find all replacements.
         */
        return 8;
    }

    @Override
    public int getPriority() {
        return 0;
    }

}
