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

package com.openexchange.folderstorage.type;

import com.openexchange.folderstorage.Type;

/**
 * {@link FileStorageType} - The file storage type.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FileStorageType implements Type {

    private static final FileStorageType instance = new FileStorageType();

    /**
     * Gets the {@link FileStorageType} instance.
     *
     * @return The {@link FileStorageType} instance
     */
    public static FileStorageType getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link FileStorageType}.
     */
    private FileStorageType() {
        super();
    }

    @Override
    public int getType() {
        // Taken from FolderObject.FILE
        return 14;
    }

}
