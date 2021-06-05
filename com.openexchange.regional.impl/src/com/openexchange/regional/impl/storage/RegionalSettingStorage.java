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

package com.openexchange.regional.impl.storage;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.regional.RegionalSettings;
import com.openexchange.regional.impl.service.RegionalSettingsImpl;

/**
 * {@link RegionalSettingStorage}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public interface RegionalSettingStorage {

    /**
     * Gets the stored regional settings for the given user
     *
     * @param contextId The context id
     * @param userId The user id
     * @return The stored {@link RegionalSettingsImpl} for this user or null
     * @throws OXException if an error is occurred
     */
    RegionalSettings get(int contextId, int userId) throws OXException;

    /**
     * Creates or updates a regional setting for the given user
     *
     * @param contextId The context id
     * @param userId The user id
     * @param settings The settings to store
     * @throws OXException if an error is occurred
     */
    void upsert(int contextId, int userId, RegionalSettings settings) throws OXException;

    /**
     * Deletes the entries for a given user
     *
     * @param contextId The context id
     * @param userId The user id
     * @throws OXException if an error is occurred
     */
    void delete(int contextId, int userId) throws OXException;

    /**
     * Deletes the regional settings for the specified user with in the specified context
     * by using the specified writeable connection.
     * 
     * @param contextId The context identifier
     * @param userId the user identifier
     * @param writeCon The writeable connection
     * @throws OXException if an error is occurred
     */
    void delete(int contextId, int userId, Connection writeCon) throws OXException;

    /**
     * Deletes the entries for a given context
     *
     * @param contextId The context id
     * @throws OXException if an error is occurred
     */
    void delete(int contextId) throws OXException;

    /**
     * Delets the regional settings for the specified context
     * by using the specified writeable connection.
     * 
     * @param contextId The context identifier
     * @param writeCon The writable connection
     * @throws OXException if an error is occurred
     */
    void delete(int contextId, Connection writeCon) throws OXException;

}
