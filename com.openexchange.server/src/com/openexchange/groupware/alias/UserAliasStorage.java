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

package com.openexchange.groupware.alias;

import java.sql.Connection;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link UserAliasStorage} - The storage for user aliases.
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.0
 */
@SingletonService
public interface UserAliasStorage {

    /**
     * Gets all aliases of the context.
     *
     * @param contextId The context identifier
     * @return A <code>Set</code> of aliases belonging to given context
     * @throws OXException If aliases cannot be returned
     */
    Set<String> getAliases(int contextId) throws OXException;

    /**
     * Gets all aliases of the user.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return A <code>Set</code> of aliases belonging to given user
     * @throws OXException If aliases cannot be returned
     */
    Set<String> getAliases(int contextId, int userId) throws OXException;

    /**
     * Gets all aliases of the specified users.
     *
     * @param contextId The context identifier
     * @param userIds The user identifiers
     * @return A <code>Set</code> of aliases belonging to given user
     * @throws OXException If aliases cannot be returned
     */
    List<Set<String>> getAliases(int contextId, int... userIds) throws OXException;

    /**
     * Gets the identifier of the user owning the alias.
     *
     * @param contextId The context identifier
     * @param alias The alias to search for
     * @return The user identifier or <code>-1</code> if this alias does not belong to any user
     * @throws OXException If user cannot be returned
     */
    int getUserId(int contextId, String alias) throws OXException;

    /**
     * Gets the identifiers of the users that have an alias within the given domain.
     *
     * @param contextId The context identifier
     * @param domain The domain to search for
     * @return The user identifiers
     * @throws OXException If users cannot be returned
     */
    List<Integer> getUserIdsByAliasDomain(int contextId, String domain) throws OXException;

    /**
     * Creates an alias and assigns to it a random UUID
     *
     * @param con A write connection object to use or <code>null</code> to obtain a new database write connection.
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param alias The alias to create
     * @return <code>true</code> on successful creation; otherwise <code>false</code>
     * @throws OXException If aliases cannot be created
     */
    boolean createAlias(Connection con, int contextId, int userId, String alias) throws OXException;

    /**
     * Updates the alias
     *
     * @param con A write connection object to use or <code>null</code> to obtain a new database write connection.
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param oldAlias The old alias
     * @param newAlias The alias to update
     * @return <code>true</code> if update alias was successful; otherwise <code>false</code>
     * @throws OXException If aliases cannot be updated
     */
    boolean updateAlias(Connection con, int contextId, int userId, String oldAlias, String newAlias) throws OXException;

    /**
     * Deletes a specific alias belonging to user and context
     *
     * @param con A write connection object to use or <code>null</code> to obtain a new database write connection.
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param alias The alias to delete
     * @return <code>true</code> if delete alias was successful; otherwise <code>false</code>
     * @throws OXException If delete attempt fails
     */
    boolean deleteAlias(Connection con, int contextId, int userId, String alias) throws OXException;

    /**
     * Deletes all aliases belonging to user and context
     *
     * @param con A write connection object to use or <code>null</code> to obtain a new database connection..
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return <code>true</code> if delete operation was successful; otherwise <code>false</code>
     * @throws OXException If delete attempt fails
     */
    boolean deleteAliases(Connection con, int contextId, int userId) throws OXException;

    /**
     * Sets a user's aliases.
     *
     * A write connection object to use or <code>null</code> to obtain a new database write connection.
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param aliases The aliases to set
     * @throws OXException If aliases cannot be set
     */
    void setAliases(Connection con, int contextId, int userId, Set<String> aliases) throws OXException;

    /**
     * Invalidates possibly cached aliases.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @throws OXException If invalidation fails
     */
    void invalidateAliases(int contextId, int userId) throws OXException;
}
