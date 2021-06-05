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

import com.openexchange.exception.OXException;

/**
 * {@link DatabaseAccessProvider} - A database access provider for a certain file storage and prefix.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface DatabaseAccessProvider {

    /**
     * Gets a connection access suitable for given file storage identifier and prefix
     *
     * @param fileStorageId The file storage identifier
     * @param prefix The prefix in-use; e.g. <code>"imageserver"</code>
     * @return A connection access or <code>null</code> if this provider does not serve specified file storage and/or prefix
     * @throws OXException If connection access cannot be initialized
     */
    DatabaseAccess getAccessFor(int fileStorageId, String prefix) throws OXException;

}
