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

import com.openexchange.exception.OXException;

/**
 * {@link PathKnowingFileStorageFolderAccess} - Extends folder access by providing a path for a certain folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface PathKnowingFileStorageFolderAccess extends FileStorageFolderAccess {

    /**
     * Gets the reverse path identifiers from the folder identified through given identifier to parental default folder. All occurring folders on that
     * path identifiers are contained in reverse order in returned array of {@link FileStorageFolder} instances.
     *
     * @param folderId The folder identifier
     * @return All occurring path identifiers in reverse order as an array of {@link FileStorageFolder} instances.
     * @throws OXException If either folder does not exist or path identifiers cannot be determined
     */
    String[] getPathIds2DefaultFolder(final String folderId) throws OXException;
}
