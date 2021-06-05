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

package com.openexchange.admin.storage.mysqlStorage;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;

/**
 * {@link AdminMySQLStorageUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
class AdminMySQLStorageUtil {

    private static final Logger LOG = LoggerFactory.getLogger(AdminMySQLStorageUtil.class);

    //////////////////////////////////// LEASE ////////////////////////////////////

    /**
     * Leases a read-only {@link Connection} for the configuration database
     *
     * @param cache The {@link AdminCache}
     * @return The leased {@link Connection}
     * @throws StorageException if a pool error is occurred
     */
    static Connection leaseReadConnectionForConfigDB(AdminCache cache) throws StorageException {
        try {
            return cache.getReadConnectionForConfigDB();
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
    }

    /**
     * Leases a read/write {@link Connection} for the specified context
     *
     * @param contextId the context identifier
     * @param cache The {@link AdminCache}
     * @return The leased {@link Connection}
     * @throws StorageException if a pool error is occurred
     */
    static Connection leaseConnectionForContext(int contextId, AdminCache cache) throws StorageException {
        try {
            return cache.getConnectionForContext(contextId);
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
    }

    /**
     * Leases a read/write {@link Connection} for the specified context. This connection will not have a
     * connection timeout to support long running update tasks.
     *
     * @param contextId The context identifier
     * @param cache The {@link AdminCache}
     * @return The leased {@link Connection}
     * @throws StorageException if a pool error is occurred
     */
    static Connection leaseWriteContextConnectionWithoutTimeout(int contextId, AdminCache cache) throws StorageException {
        try {
            return cache.getConnectionForContextNoTimeout(contextId);
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e);
        }
    }

    //////////////////////////////////// RELEASE ////////////////////////////////////

    /**
     * Releases the specified {@link Connection}
     *
     * @param connection The {@link Connection} to release
     * @param cache The {@link AdminCache}
     */
    static void releaseConfigDBConnection(Connection connection, AdminCache cache) {
        if (connection == null) {
            return;
        }
        try {
            cache.pushReadConnectionForConfigDB(connection);
        } catch (Exception e) {
            LOG.error("Error pushing configdb connection to pool!", e);
        }
    }

    /**
     * Releases the specified write {@link Connection} for the specified {@link Context}
     *
     * @param connection The {@link Connection} to release
     * @param context The {@link Context}
     * @param cache The {@link AdminCache}
     */
    static void releaseWriteContextConnection(Connection connection, Context context, AdminCache cache) {
        if (connection == null) {
            return;
        }
        int contextId = context.getId().intValue();
        try {
            cache.pushConnectionForContext(contextId, connection);
        } catch (PoolException e) {
            LOG.error("Error pushing write connection to pool for context {}!", I(contextId), e);
        }
    }

    /**
     * Releases the specified {@link Connection} for the specified {@link Context} after performing a read
     *
     * @param connection The {@link Connection}
     * @param contextId The context identifier
     * @param cache The {@link AdminCache}
     */
    static void releaseWriteContextConnectionAfterReading(Connection connection, int contextId, AdminCache cache) {
        if (connection == null) {
            return;
        }
        try {
            cache.pushConnectionForContextAfterReading(contextId, connection);
        } catch (PoolException exp) {
            LOG.error("Pool Error pushing ox read connection to pool!", exp);
        }
    }

    /**
     * Releases the specified {@link Connection} for the specified {@link Context}
     *
     * @param connection The {@link Connection} to release
     * @param context The {@link Context}
     * @param cache The {@link AdminCache}
     */
    static void releaseWriteContextConnectionWithoutTimeout(Connection connection, int contextId, AdminCache cache) {
        if (connection == null) {
            return;
        }
        try {
            cache.pushConnectionForContextNoTimeout(contextId, connection);
        } catch (PoolException e) {
            LOG.error("Pool Error pushing ox write connection to pool!", e);
        }
    }
}
