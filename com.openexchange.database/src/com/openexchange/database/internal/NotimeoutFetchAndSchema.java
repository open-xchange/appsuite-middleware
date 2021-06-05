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
import com.openexchange.database.internal.wrapping.ConnectionReturnerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.pooling.PoolingException;

/**
 * Fetches a connection without timeouts and optionally selects the wanted schema.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
final class NotimeoutFetchAndSchema implements FetchAndSchema {

    private final ReplicationMonitor monitor;
    private final boolean setSchema;

    /**
     * Initializes a new {@link NotimeoutFetchAndSchema}.
     *
     * @param monitor The replication monitor
     * @param setSchema <code>true</code> to set the schema name when getting the connection, <code>false</code>, otherwise
     */
    NotimeoutFetchAndSchema(ReplicationMonitor monitor, boolean setSchema) {
        super();
        this.monitor = monitor;
        this.setSchema = setSchema;
    }

    @Override
    public Connection get(Pools pools, AssignmentImpl assign, boolean write, boolean usedAsRead) throws PoolingException, OXException {
        final int poolId;
        if (write) {
            poolId = assign.getWritePoolId();
        } else {
            poolId = assign.getReadPoolId();
        }
        final ConnectionPool pool = pools.getPool(poolId);
        final Connection retval = pool.getWithoutTimeout();
        if (setSchema) {
            try {
                final String schema = assign.getSchema();
                if (null != schema && !retval.getCatalog().equals(schema)) {
                    retval.setCatalog(schema);
                }
            } catch (SQLException e) {
                pool.backWithoutTimeout(retval);
                throw DBPoolingExceptionCodes.SCHEMA_FAILED.create(e);
            }
        }
        return ConnectionReturnerFactory.createConnection(pools, monitor, assign, retval, true, write, usedAsRead);
    }
}
