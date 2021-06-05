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
 * {@link FileStorageAccountAccess} - Provides access to a file storage account.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 * @see WarningsAware
 */
public interface FileStorageAccountAccess extends FileStorageResource {

    /**
     * Gets the account identifier of this access.
     *
     * @return The account identifier
     */
    String getAccountId();

    /**
     * Gets the file access for associated account.
     *
     * @return The file access
     * @throws OXException If file access cannot be returned
     */
    FileStorageFileAccess getFileAccess() throws OXException;

    /**
     * Gets the folder access for associated account.
     *
     * @return The folder access
     * @throws OXException If folder access cannot be returned
     */
    FileStorageFolderAccess getFolderAccess() throws OXException;

    /**
     * Convenience method to obtain root folder in a fast way; meaning no default folder check is performed which is not necessary to return
     * the root folder.
     * <p>
     * The same result is yielded through calling <code>getFolderAccess().getRootFolder()</code> on a connected
     * {@link FileStorageFolderAccess}.
     *
     * @throws OXException If returning the root folder fails
     */
    FileStorageFolder getRootFolder() throws OXException;

    /**
     * Retrieve the parent file storage service
     * @return
     */
    FileStorageService getService();

}
