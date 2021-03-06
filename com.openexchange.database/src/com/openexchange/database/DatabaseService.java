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
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * Service interface class for accessing the database system.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@SingletonService
public interface DatabaseService extends ConfigDatabaseService, GlobalDatabaseService {

    /**
     * Returns a read only connection to the database of the specified context.
     *
     * @param ctx Context.
     * @return a read only connection to the database of the specified context.
     * @throws OXException if no connection can be obtained.
     */
    Connection getReadOnly(Context ctx) throws OXException;

    /**
     * Returns a read only connection to the database of the context with the specified identifier.
     *
     * @param contextId identifier of the context.
     * @return a read only connection to the database of the specified context.
     * @throws OXException if no connection can be obtained.
     */
    Connection getReadOnly(int contextId) throws OXException;

    /**
     * Returns a writable connection to the database of the specified context.
     *
     * @param ctx Context.
     * @return a writable connection to the database of the specified context.
     * @throws OXException if no connection can be obtained.
     */
    Connection getWritable(Context ctx) throws OXException;

    /**
     * Returns a writable connection to the database of the context with the specified identifier.
     *
     * @param contextId identifier of the context.
     * @return a writable connection to the database of the specified context.
     * @throws OXException if no connection can be obtained.
     */
    Connection getWritable(int contextId) throws OXException;

    /**
     * Returns a writable connection to the database of the context with the specified identifier. This connection will not have a
     * connection timeout to support long running update tasks.
     *
     * @param contextId identifier of the context.
     * @return a writable connection to the database of the specified context without a connection timeout.
     * @throws OXException if no connection can be obtained.
     */
    Connection getForUpdateTask(int contextId) throws OXException;

    /**
     * This method is for moving contexts only.
     *
     * @param poolId identifier of the database pool.
     * @param schema schema name.
     * @return a connection to the database from the given pool directed to the given schema.
     * @throws OXException if no connection can be obtained.
     */
    Connection get(int poolId, String schema) throws OXException;

    /**
     * This method is only for administrative access to contexts.
     *
     * @param poolId identifier of the database pool.
     * @param schema schema name.
     * @return a connection to the database without a time-out from the given pool directed to the given schema.
     * @throws OXException if no connection can be obtained.
     */
    Connection getNoTimeout(int poolId, String schema) throws OXException;

    /**
     * Retrieve a monitored connection used for reading from the master/slave db pool referenced by the poolIds and schema. This features tracking of the
     * replication state and an automatic fallback to the writedb if the slave has not caught up yet. The partitionId can be chosen arbitrarily by clients (just make sure a
     * corresponding entry is available in the replication monitoring table), and controls how parts of the database are invalidated with regards to the replication monitor. When
     * in doubt just set this to 0 always and invalidate the whole schema.
     *
     * @param readPoolId The id referencing the slave db server.
     * @param writePoolId The id referencing the master db server.
     * @param schema The database schema on both the master and slave.
     * @param partitionId The partition the replication monitor tracking should be scoped to.
     * @return The connection to the database
     */
    public Connection getReadOnlyMonitored(int readPoolId, int writePoolId, String schema, int partitionId) throws OXException;

    /**
     * Retrieve a monitored connection used for writing to the master/slave db pool referenced by the poolIds and schema. This features tracking of the
     * replication state and an automatic fallback to the writedb if the slave has not caught up yet. The partitionId can be chosen arbitrarily by clients (just make sure a
     * corresponding entry is available in the replication monitoring table), and controls how parts of the database are invalidated with regards to the replication monitor. When
     * in doubt just set this to 0 always and invalidate the whole schema.
     *
     * @param readPoolId The id referencing the slave db server.
     * @param writePoolId The id referencing the master db server.
     * @param schema The database schema on both the master and slave.
     * @param partitionId The partition the replication monitor tracking should be scoped to.
     * @return The connection to the database
     */
    public Connection getWritableMonitored(int readPoolId, int writePoolId, String schema, int partitionId) throws OXException;

    /**
     * Retrieve a monitored connection used for writing to the master/slave db pool referenced by the poolIds and schema not bound by an automatic timeout.
     * This is useful for long running database operations like modifying the schema. This features tracking of the
     * replication state and an automatic fallback to the writedb if the slave has not caught up yet. The partitionId can be chosen arbitrarily by clients (just make sure a
     * corresponding entry is available in the replication monitoring table), and controls how parts of the database are invalidated with regards to the replication monitor. When
     * in doubt just set this to 0 always and invalidate the whole schema.
     *
     * @param readPoolId The id referencing the slave db server.
     * @param writePoolId The id referencing the master db server.
     * @param schema The database schema on both the master and slave.
     * @param partitionId The partition the replication monitor tracking should be scoped to.
     * @return The connection to the database
     */
    public Connection getWritableMonitoredForUpdateTask(int readPoolId, int writePoolId, String schema, int partitionId) throws OXException;

    /**
     * This method is only for administrative access to contexts.
     *
     * @param poolId identifier of the pool the connection should be returned to.
     * @param con connection to return.
     */
    void backNoTimeoout(int poolId, Connection con);

    /**
     * Returns a read only connection to the database of the specified context to the pool.
     *
     * @param ctx Context.
     * @param con Read only connection to return.
     */
    void backReadOnly(Context ctx, Connection con);

    /**
     * Returns a read only connection to the database of the context with the specified identifier to the pool.
     *
     * @param contextId identifier of the context.
     * @param con Read only connection to return.
     */
    void backReadOnly(int contextId, Connection con);

    /**
     * Returns a writable connection to the database of the specified context to the pool.
     *
     * @param ctx Context.
     * @param con Writable connection to return.
     */
    void backWritable(Context ctx, Connection con);

    /**
     * Returns a writable connection to the database of the context with the specified identifier to the pool.
     *
     * @param contextId identifier of the context.
     * @param con Writable connection to return.
     */
    void backWritable(int contextId, Connection con);

    /**
     * Returns a writable connection to the database of the specified context to the pool. It should be used to return a writable connection
     * if it was only used for reading information from the master database server.
     * When this connection is returned the replication monitor will not increase the replication counter. Therefore the database pooling
     * component can not determine when written information will be available on the slave.
     * This allows to reduce the write IO load on the database servers but keep in mind that reading from the master does not scale out.
     *
     * @param ctx Context.
     * @param con Writable connection to return.
     */
    void backWritableAfterReading(Context ctx, Connection con);

    /**
     * Returns a writable connection to the database of the specified context to the pool. It should be used to return a writable connection
     * if it was only used for reading information from the master database server.
     * When this connection is returned the replication monitor will not increase the replication counter. Therefore the database pooling
     * component can not determine when written information will be available on the slave.
     * This allows to reduce the write IO load on the database servers but keep in mind that reading from the master does not scale out.
     *
     * @param contextId identifier of the context.
     * @param con Writable connection to return.
     */
    void backWritableAfterReading(int contextId, Connection con);

    /**
     * Returns a writable connection to the database of the context with the specified identifier to the pool. This method must be used if
     * the connection is obtained with {@link #getForUpdateTask(int)}.
     *
     * @param contextId identifier of the context.
     * @param con Writable connection to return.
     */
    void backForUpdateTask(int contextId, Connection con);

    /**
     * Returns a writable connection to the database of the context with the specified identifier to the pool. It should be used to return
     * a writable connection if it was only used for reading information from the master database server. This method must be used if
     * the connection is obtained with {@link #getForUpdateTask(int)}.
     *
     * @param contextId identifier of the context.
     * @param con Writable connection to return.
     */
    void backForUpdateTaskAfterReading(int contextId, Connection con);

    /**
     * This method is for moving contexts only.
     *
     * @param poolId identifier of the pool the connection should be returned to.
     * @param con connection to return.
     */
    void back(int poolId, Connection con);

    /**
     * Returns a read only connection to the database master/slave pair referenced by the poolIds, schema and partitionId.
     *
     * @param readPoolId The id referencing the slave db server.
     * @param writePoolId The id referencing the master db server.
     * @param schema The database schema on both the master and slave.
     * @param partitionId The partition the replication monitor tracking should be scoped to.
     * @param con The connection to return.
     **/
    public void backReadOnlyMonitored(int readPoolId, int writePoolId, String schema, int partitionId, Connection con);

    /**
     * Returns a writable connection to the database master/slave pair referenced by the poolIds, schema and partitionId.
     *
     * @param readPoolId The id referencing the slave db server.
     * @param writePoolId The id referencing the master db server.
     * @param schema The database schema on both the master and slave.
     * @param partitionId The partition the replication monitor tracking should be scoped to.
     * @param con The connection to return.
     */
    public void backWritableMonitored(int readPoolId, int writePoolId, String schema, int partitionId, Connection con);

    /**
     * Returns a writable connection without timeout to the database master/slave pair referenced by the poolIds, schema and partitionId.
     *
     * @param readPoolId The id referencing the slave db server.
     * @param writePoolId The id referencing the master db server.
     * @param schema The database schema on both the master and slave.
     * @param partitionId The partition the replication monitor tracking should be scoped to.
     * @param con The connection to return.
     */
    public void backWritableMonitoredForUpdateTask(int readPoolId, int writePoolId, String schema, int partitionId, Connection con);

    /**
     * Creates the replication monitoring tables in the schema on the given writePool. Does nothing if the tables already exist
     *
     * @param writePoolId The id referencing the master db server
     * @param schema The name of the schema
     */
    public void initMonitoringTables(int writePoolId, String schema) throws OXException;

    /**
     * Add these partition ids to the replication monitor table
     *
     * @param writePoolId The id referencing the master db server
     * @param schema The database schema name
     * @param partitions The partitions to add to the replication monitor table
     */
    public void initPartitions(int writePoolId, String schema, int... partitions) throws OXException;

    /**
     * Returns a writable connection based on the given {@link Assignment}
     *
     * @param assignment The {@link Assignment} to get a {@link Connection} for
     * @param noTimeout Flag if the writable {@link Connection} should have a timeout or not
     * @return a writable connection to the database of the specified {@link Assignment}
     * @throws OXException
     */
    Connection getWritable(Assignment assignment, boolean noTimeout) throws OXException;

    /**
     * Returns a read only connection based on the given {@link Assignment}
     *
     * @param assignment The {@link Assignment} to get a {@link Connection} for
     * @param noTimeout Flag if the writable {@link Connection} should have a timeout or not
     * @return a read only connection to the database of the specified {@link Assignment}
     * @throws OXException
     */
    Connection getReadOnly(Assignment assignment, boolean noTimeout) throws OXException;
}
