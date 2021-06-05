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

import java.io.InputStream;
import com.openexchange.exception.OXException;


/**
 * {@link ThumbnailAware}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public interface ThumbnailAware extends FileStorageFileAccess {

    /**
     * Loads the thumbnail content
     *
     * @param folderId The folder identifier
     * @param id The id of the file which thumbnail shall be returned
     * @param version The version of the file. Pass in CURRENT_VERSION for the current version of the file.
     * @return The thumbnail stream or <code>null</code> if not applicable
     * @throws OXException If operation fails
     */
    InputStream getThumbnailStream(String folderId, String id, String version) throws OXException;

}
