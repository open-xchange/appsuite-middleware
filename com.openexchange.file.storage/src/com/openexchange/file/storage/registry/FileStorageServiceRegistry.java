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

package com.openexchange.file.storage.registry;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link FileStorageServiceRegistry}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
@SingletonService
public interface FileStorageServiceRegistry {

    /**
     * Gets the file storage service associated with specified identifier.
     *
     * @param id The file storage service identifier
     * @return The file storage service associated with specified identifier
     * @throws OXException If file storage service cannot be returned
     */
    public FileStorageService getFileStorageService(String id) throws OXException;

    /**
     * Checks if there is a file storage service associated with specified identifier.
     *
     * @param id The file storage service identifier
     * @return <code>true</code> if there is a file storage service associated with specified identifier; otherwise <code>false</code>
     */
    public boolean containsFileStorageService(String id);

    /**
     * Gets all file storage services kept in this registry.
     *
     * @return All file storage services kept in this registry
     * @throws OXException If file storage services cannot be returned
     */
    public List<FileStorageService> getAllServices() throws OXException;
}
