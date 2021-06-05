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

package com.openexchange.database.internal;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.Databases;
import com.openexchange.database.internal.wrapping.ConnectionReturnerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.pooling.PoolingException;

/**
 * Fetches a connection with timeouts and selects the wanted schema.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
final class TimeoutFetchAndSchema implements FetchAndSchema {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TimeoutFetchAndSchema.class);
    private final ReplicationMonitor monitor;
    private final boolean setSchema;

    /**
     * Initializes a new {@link TimeoutFetchAndSchema}.
     *
     * @param monitor The replication monitor
     * @param setSchema <code>true</code> to set the schema name when getting the connection, <code>false</code>, otherwise
     */
    TimeoutFetchAndSchema(ReplicationMonitor monitor, boolean setSchema) {
        super();
        this.monitor = monitor;
        this.setSchema = setSchema;
    }

    @Override
    public Connection get(Pools pools, AssignmentImpl assign, boolean write, boolean usedAsRead) throws PoolingException, OXException {
        int poolId = write ? assign.getWritePoolId() : assign.getReadPoolId();
        ConnectionPool pool = pools.getPool(poolId);
        Connection retval = null;
        do {
            try {
                // Pools cleaner may stop a pool just after it is fetched in the above line. See bug 27126. This is a race condition. Fixing
                // this with a lock in the Pools class around fetching the correct ConnectionPool and a connection from it will result in
                // too much threads waiting on that lock if creating the connection once becomes slow.
                retval = pool.get();
            } catch (PoolingException e) {
                // So we will try to catch up here with the pool that has been stopped unexpectedly.
                if (!pool.isStopped()) {
                    throw e;
                }
                pool = pools.getPool(poolId);
            }
        } while (null == retval);
        if (setSchema) {
            try {
                final String schema = assign.getSchema();
                if (null != schema && !retval.getCatalog().equals(schema)) {
                    retval.setCatalog(schema);
                }
            } catch (SQLException e) {
                try {
                    pool.back(retval);
                } catch (PoolingException e1) {
                    Databases.close(retval);
                    LOG.error(e1.getMessage(), e1);
                }
                throw DBPoolingExceptionCodes.SCHEMA_FAILED.create(e);
            }
        }
        return ConnectionReturnerFactory.createConnection(pools, monitor, assign, retval, false, write, usedAsRead);
    }
}
