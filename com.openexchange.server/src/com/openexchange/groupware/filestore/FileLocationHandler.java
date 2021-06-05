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

package com.openexchange.groupware.filestore;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;


/**
 * {@link FileLocationHandler} - Performs various actions related to file locations.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Renamed & added more methods
 * @since 7.6.0
 */
public interface FileLocationHandler {

    /**
     * Updates file locations
     *
     * @param prevFileName2newFileName The previous file name to new file name mapping
     * @param contextId The context identifier
     * @param con The connection to use
     * @throws SQLException If an SQL error occurs
     */
    void updateFileLocations(Map<String, String> prevFileName2newFileName, int contextId, Connection con) throws SQLException;

    /**
     * Determines the context-associated file locations from this handler.
     *
     * @param contextId The context identifier
     * @param con The connection to use
     * @return The file locations
     * @throws OXException If an Open-Xchange error occurs
     * @throws SQLException If an SQL error occurs
     * @since v7.8.0
     */
    Set<String> determineFileLocationsFor(int contextId, Connection con) throws OXException, SQLException;

    /**
     * Determines the user-associated file locations from this handler.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param con The connection to use
     * @return The file locations
     * @throws OXException If an Open-Xchange error occurs
     * @throws SQLException If an SQL error occurs
     * @since v7.8.0
     */
    Set<String> determineFileLocationsFor(int userId, int contextId, Connection con) throws OXException, SQLException;

}
