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

package com.openexchange.admin.storage.sqlStorage;

import java.sql.Connection;
import java.util.Map;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.database.Assignment;
import com.openexchange.database.DatabaseService;

public interface OXAdminPoolInterface {

    void setService(DatabaseService service);

    @Deprecated
    Connection getConnectionForConfigDB() throws PoolException;

    Connection getWriteConnectionForConfigDB() throws PoolException;

    Connection getReadConnectionForConfigDB() throws PoolException;

    Connection getWriteConnectionForConfigDBNoTimeout() throws PoolException;

    Connection getConnectionForContext(int contextId) throws PoolException;

    Connection getConnectionForContextNoTimeout(int contextId) throws PoolException;

    Connection getConnection(int poolId, String schema) throws PoolException;

    @Deprecated
    boolean pushConnectionForConfigDB(Connection con) throws PoolException;

    boolean pushWriteConnectionForConfigDB(Connection con) throws PoolException;

    boolean pushReadConnectionForConfigDB(Connection con) throws PoolException;

    boolean pushWriteConnectionForConfigDBNoTimeout(Connection con) throws PoolException;

    boolean pushConnectionForContext(int contextId, Connection con) throws PoolException;

    boolean pushConnectionForContextAfterReading(int contextId, Connection con) throws PoolException;

    boolean pushConnectionForContextNoTimeout(int contextId, Connection con) throws PoolException;

    boolean pushConnection(int poolId, Connection con) throws PoolException;

    int getServerId() throws PoolException;

    void writeAssignment(Connection con, Assignment assign) throws PoolException;

    void deleteAssignment(Connection con, int contextId) throws PoolException;

    void removeService();

    int[] getContextInSameSchema(Connection con, int contextId) throws PoolException;

    int[] getContextInSchema(Connection con, int poolId, String schema) throws PoolException;

    int[] listContexts(int poolId, int offset, int length) throws PoolException;

    int getWritePool(int contextId) throws PoolException;

    String getSchemaName(int contextId) throws PoolException;

    String[] getUnfilledSchemas(Connection con, int poolId, int maxContexts) throws PoolException;

    /**
     * Gets the number of contexts per schema that are located in given database identified by <code>poolId</code>.
     *
     * @param con The connection to the config database
     * @param poolId The pool identifier
     * @param maxContexts The configured maximum allowed contexts for a database schema.
     * @return A mapping providing the count per schema
     * @throws PoolException If schema count cannot be returned
     */
    Map<String, Integer> getContextCountPerSchema(Connection con, int poolId, int maxContexts) throws PoolException;

    /**
     * Acquires a global lock for specified database
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: Given connection is required to be in transaction mode.
     * </div>
     * <p>
     *
     * @param con The connection (in transaction mode)
     * @param writePoolId The identifier of the (read-write) database for which to acquire a lock
     * @throws PoolException If lock cannot be acquired
     */
    void lock(Connection con, int writePoolId) throws PoolException;
}
