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

package com.openexchange.json.cache;

import org.json.JSONValue;
import com.openexchange.exception.OXException;

/**
 * {@link JsonCacheService} - A simple persistent JSON cache.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface JsonCacheService {

    /**
     * Locks for specified entry.
     *
     * @param id The identifier
     * @param userId The user identifier
     * @param contextId The user's context identifier
     * @return <code>true</code> if successfully locked; otherwise <code>false</code>
     * @throws OXException If lock operation fails
     */
    boolean lock(String id, int userId, int contextId) throws OXException;

    /**
     * Unlocks for specified entry.
     *
     * @param id The identifier
     * @param userId The user identifier
     * @param contextId The user's context identifier
     * @throws OXException If unlock operation fails
     */
    public void unlock(String id, int userId, int contextId) throws OXException;

    /**
     * Gets denoted JSON value from cache.
     *
     * @param id The identifier
     * @param userId The user identifier
     * @param contextId The user's context identifier
     * @return The JSON value
     * @throws OXException If JSON value cannot be returned for any reason
     */
    JSONValue get(String id, int userId, int contextId) throws OXException;

    /**
     * Gets (optionally) denoted JSON value from cache.
     *
     * @param id The identifier
     * @param userId The user identifier
     * @param contextId The user's context identifier
     * @return The JSON value or <code>null</code> no such value exists
     * @throws OXException If JSON value cannot be returned for any reason
     */
    JSONValue opt(String id, int userId, int contextId) throws OXException;

    /**
     * Puts specified JSON value into cache.
     * <p>
     * A <code>null</code> value performs a delete.
     *
     * @param id The identifier
     * @param jsonValue The JSON value to put
     * @param duration The processing duration
     * @param userId The user identifier
     * @param contextId The user's context identifier
     * @throws OXException If JSON value cannot be put into cache for any reason
     */
    void set(String id, JSONValue jsonValue, long duration, int userId, int contextId) throws OXException;

    /**
     * Puts specified JSON value into cache if it differs from possibly existing one.
     * <p>
     * A <code>null</code> value performs a delete.
     *
     * @param id The identifier
     * @param jsonValue The JSON value to put
     * @param duration The processing duration
     * @param userId The user identifier
     * @param contextId The user's context identifier
     * @return <code>true</code> if put into cache; otherwise <code>false</code>
     * @throws OXException If JSON value cannot be put into cache for any reason
     */
    boolean setIfDifferent(String id, JSONValue jsonValue, long duration, int userId, int contextId) throws OXException;

    /**
     * Deletes denoted JSON value from cache.
     *
     * @param id The identifier
     * @param userId The user identifier
     * @param contextId The user's context identifier
     * @throws OXException If JSON value cannot be returned for any reason
     */
    void delete(String id, int userId, int contextId) throws OXException;

}
