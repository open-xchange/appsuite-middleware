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

package com.openexchange.filestore;

import java.net.URI;

/**
 * {@link FileStorageInfo} - Provides basic provisioning information for a file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface FileStorageInfo {

    /**
     * Gets the identifier of the registered file storage.
     *
     * @return The identifier
     */
    int getId();

    /**
     * Gets the maximum number of contexts that are allowed to be assigned to the registered file storage
     *
     * @return The max. number of contexts
     */
    long getMaxContext();

    /**
     * Gets the size (in bytes) for the registered file storage that is allowed to be occupied.
     *
     * @return The size
     */
    long getSize();

    /**
     * Gets the base URI for the registered file storage
     *
     * @return The base URI
     */
    URI getUri();

}
