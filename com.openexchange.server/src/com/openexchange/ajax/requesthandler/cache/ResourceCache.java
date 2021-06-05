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

package com.openexchange.ajax.requesthandler.cache;

import com.openexchange.exception.OXException;

/**
 * {@link ResourceCache} - The cache for all kinds of requested resources that support ETag-wise access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ResourceCache {

    /**
     * Checks whether caching is allowed for the given (contextId, userId) tuple. This must
     * be invoked before any other operation.
     * @param userId The user identifier. May be &lt;= 0 for context-global caching.
     * @param contextId The context identifier.
     * @return <code>true</code> if caching is allowed, <code>false</code> if not.
     * @throws OXException If operations fails
     */
    boolean isEnabledFor(int contextId, int userId) throws OXException;

    /**
     * Stores given resource's binary content. Before saving, you have to check, if caching is allowed.
     * See {@link ResourceCache#isEnabledFor(int, int)}.
     *
     * @param id The identifier (cache key) for the cached document
     * @param resource The cached preview
     * @param userId The user identifier. May be &lt;= 0 context-global caching.
     * @param contextId The context identifier
     * @return <code>true</code> if successfully saved; otherwise <code>false</code> if impossible to store (e.g. due to quota restrictions)
     * @throws OXException If operations fails
     */
    boolean save(String id, CachedResource resource, int userId, int contextId) throws OXException;

    /**
     * Gets the resource.
     *
     * @param id The document identifier
     * @param userId The user identifier or &lt;= 0 for context-global document
     * @param contextId The context identifier
     * @return The preview document or <code>null</code>
     * @throws OXException If retrieving document data fails
     */
    CachedResource get(String id, int userId, int contextId) throws OXException;

    /**
     * Removes the resources associated with specified user.
     *
     * @param userId The user identifier or &lt;= 0 for context-global document
     * @param contextId The context identifier
     * @throws OXException If deleting document data fails
     */
    void remove(int userId, int contextId) throws OXException;

    /**
     * Removes the resources associated with specified user.
     *
     * @param id The document identifier prefix
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If deleting document data fails
     */
    void removeAlikes(String id, int userId, int contextId) throws OXException;

    /**
     * Clears all cache entries belonging to given context.
     *
     * @param contextId The context identifier
     * @throws OXException If clear operation fails
     */
    void clearFor(int contextId) throws OXException;

    /**
     * Tests for existence of denoted resource.
     *
     * @param id The identifier (cache key) for the cached resource
     * @param userId The user identifier or &lt;= 0 for context-global document
     * @param contextId The context identifier
     * @return <code>true</code> if exists; otherwise <code>false</code>
     * @throws OXException If an error occurs while checking existence
     */
    boolean exists(String id, int userId, int contextId) throws OXException;

}
