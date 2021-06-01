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

package com.openexchange.database.cleanup.impl;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.cleanup.CleanUpExecutionConnectionProvider;
import com.openexchange.database.cleanup.DatabaseCleanUpExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;


/**
 * {@link ReadWriteCleanUpExecutionConnectionProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class ReadWriteCleanUpExecutionConnectionProvider implements CleanUpExecutionConnectionProvider, AutoCloseable {

    private final ServiceLookup services;
    private final int representativeContextId;
    private final boolean noTimeout;

    private Connection connection;
    private DatabaseService databaseService;

    /**
     * Initializes a new {@link ReadWriteCleanUpExecutionConnectionProvider}.
     *
     * @param representativeContextId The identifier of a representative context in that schema
     * @param noTimeout <code>true</code> to obtain a database connection w/o timeout, <code>false</code>, otherwise
     * @param services The service look-up
     */
    public ReadWriteCleanUpExecutionConnectionProvider(int representativeContextId, boolean noTimeout, ServiceLookup services) {
        super();
        this.services = services;
        this.representativeContextId = representativeContextId;
        this.noTimeout = noTimeout;
    }

    @Override
    public synchronized Connection getConnection() throws OXException {
        Connection connection = this.connection;
        if (connection == null) {
            // Acquire database service
            DatabaseService databaseService = services.getServiceSafe(DatabaseService.class);
            this.databaseService = databaseService;

            // Fetch connection & start transaction on it
            boolean error = true;
            connection = noTimeout ? databaseService.getForUpdateTask(representativeContextId) : databaseService.getWritable(representativeContextId);
            try {
                Databases.startTransaction(connection);
                this.connection = connection;
                error = false;
            } catch (SQLException e) {
                throw DatabaseCleanUpExceptionCode.SQL_ERROR.create(e, e.getMessage());
            } finally {
                if (error) {
                    if (noTimeout) {
                        databaseService.backForUpdateTaskAfterReading(representativeContextId, connection);
                    } else {
                        databaseService.backWritableAfterReading(representativeContextId, connection);
                    }
                }
            }
        }
        return connection;
    }

    /**
     * Commits connection (if any) and puts it back to pool.
     *
     * @throws SQLException If commit fails
     */
    synchronized void commitAfterSuccess() throws SQLException {
        Connection connection = this.connection;
        if (connection != null) {
            connection.commit();
            this.connection = null;
            Databases.autocommit(connection);
            if (noTimeout) {
                databaseService.backForUpdateTask(representativeContextId, connection);
            } else {
                databaseService.backWritable(representativeContextId, connection);
            }
        }
    }

    @Override
    public synchronized void close() {
        Connection connection = this.connection;
        if (connection != null) {
            this.connection = null;
            Databases.rollback(connection);
            Databases.autocommit(connection);
            if (noTimeout) {
                databaseService.backForUpdateTask(representativeContextId, connection);
            } else {
                databaseService.backWritable(representativeContextId, connection);
            }
        }
    }

}
