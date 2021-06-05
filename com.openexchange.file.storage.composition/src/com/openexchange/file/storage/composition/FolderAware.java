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

package com.openexchange.file.storage.composition;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;

/**
 * {@link FolderAware} - Extends {@link IDBasedFileAccess} by folder access..
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface FolderAware extends IDBasedFileAccess {

    /**
     * Optionally gets the folder identified through given identifier
     *
     * @param id The file identifier
     * @return The corresponding instance of {@link FileStorageFolder} or <code>null</code>
     * @throws OXException If either folder does not exist or could not be fetched
     */
    public FileStorageFolder optFolder(final String id) throws OXException;

    /**
     * Optionally gets the permission for currently logged-in user accessing this folder
     *
     * @param id The file identifier
     * @return The own permission or <code>null</code>
     * @throws OXException If permission cannot be returned
     */
    public FileStoragePermission optOwnPermission(String id) throws OXException;

}
