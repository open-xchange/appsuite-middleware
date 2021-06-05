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

package com.openexchange.database;

import java.sql.Connection;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link GlobalDatabaseService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@SingletonService
public interface GlobalDatabaseService {

    /**
     * Gets a value indicating whether at least one global database is available.
     *
     * @return <code>true</code> if a global database is available, <code>false</code>, otherwise
     */
    boolean isGlobalDatabaseAvailable();

    /**
     * Gets a value indicating whether a global database is available for a specific context group or not.
     *
     * @param group The context group to check availability for, or <code>null</code> to check for the default fallback
     * @return <code>true</code> if a global database is available, <code>false</code>, otherwise
     */
    boolean isGlobalDatabaseAvailable(String group) throws OXException;

    /**
     * Gets a value indicating whether a global database is available for a specific group a context is associated with or not.
     *
     * @param group The identifier of the context to check availability for
     * @return <code>true</code> if a global database is available, <code>false</code>, otherwise
     */
    boolean isGlobalDatabaseAvailable(int contextId) throws OXException;

    /**
     * Gets a collection of exactly one (exemplary) context group per global database schema. Useful when iterating over all available
     * global database schemas.
     *
     * @return A listing of distinct context groups per schema, or an empty set if there are none
     */
    Set<String> getDistinctGroupsPerSchema();

    /**
     * Gets a connection for read-only access to the global database of a specific context group.
     *
     * @param group The context group to get the connection for, or <code>null</code> to use the global fallback
     * @return The connection
     */
    Connection getReadOnlyForGlobal(String group) throws OXException;

    /**
     * Gets a connection for read-only access to the global database of a specific group a context is associated with.
     *
     * @param contextId The identifier of the context to get the connection for
     * @return The connection
     */
    Connection getReadOnlyForGlobal(int contextId) throws OXException;

    /**
     * Returns a read-only connection to the global database of a specific group to the pool.
     *
     * @param group The group to back the connection for
     * @param connection The connection to return
     */
    void backReadOnlyForGlobal(String group, Connection connection);

    /**
     * Returns a read-only connection to the global database of a specific group a context is associated with.
     *
     * @param contextId The identifier of the context to back the connection for
     * @param connection The connection to return
     */
    void backReadOnlyForGlobal(int contextId, Connection connection);

    /**
     * Gets a connection for read/write access to the global database of a specific context group.
     *
     * @param group The group to get the connection for
     * @return The connection
     */
    Connection getWritableForGlobal(String group) throws OXException;

    /**
     * Gets a connection for read/write access to the global database of a specific group a context is associated with.
     *
     * @param contextId The identifier of the context to get the connection for
     * @return The connection
     */
    Connection getWritableForGlobal(int contextId) throws OXException;

    /**
     * Returns a read/write connection to the global database of a specific context group to the pool.
     *
     * @param group The group to back the connection for
     * @param connection The connection to return
     */
    void backWritableForGlobal(String group, Connection connection);

    /**
     * Returns a read/write connection to the global database of a specific group a context is associated with.
     *
     * @param contextId The identifier of the context to back the connection for
     * @param connection The connection to return
     */
    void backWritableForGlobal(int contextId, Connection connection);

    /**
     * Returns a read/write connection only used to the global database of a specific context group to the pool.
     * <p>
     * It should be used to return a writable connection if it was only used for reading information from database server.
     * <p>
     * When this connection is returned the replication monitor will not increase the replication counter. Therefore the database pooling
     * component can not determine when written information will be available on the slave.
     * This allows to reduce the write I/O load on the database servers but keep in mind that reading from the master does not scale out.
     *
     * @param group The group to back the connection for
     * @param connection The connection to return
     */
    void backWritableForGlobalAfterReading(String group, Connection connection);

    /**
     * Returns a read/write connection to the global database of a specific group a context is associated with.
     * <p>
     * It should be used to return a writable connection if it was only used for reading information from database server.
     * <p>
     * When this connection is returned the replication monitor will not increase the replication counter. Therefore the database pooling
     * component can not determine when written information will be available on the slave.
     * This allows to reduce the write I/O load on the database servers but keep in mind that reading from the master does not scale out.
     *
     * @param contextId The identifier of the context to back the connection for
     * @param connection The connection to return
     */
    void backWritableForGlobalAfterReading(int contextId, Connection connection);

}
