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

package com.openexchange.database.cleanup.impl.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.cleanup.CleanUpJob;
import com.openexchange.database.cleanup.DatabaseCleanUpExceptionCode;
import com.openexchange.database.cleanup.impl.DatabaseCleanUpServiceImpl;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;


/**
 * {@link RdbDatabaseCleanUpExecutionManagement}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class RdbDatabaseCleanUpExecutionManagement implements DatabaseCleanUpExecutionManagement {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link RdbDatabaseCleanUpExecutionManagement}.
     *
     * @param services The service look-up
     */
    public RdbDatabaseCleanUpExecutionManagement(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void markExecutionDone(CleanUpJob job, int representativeContextId) throws OXException {
        if (job.isRunsExclusive() == false) {
            return;
        }

        DatabaseService databaseService = services.getServiceSafe(DatabaseService.class);
        boolean modified = false;
        Connection writeCon = databaseService.getWritable(representativeContextId);
        try {
            modified = doMarkExecutionDone(job, writeCon);
        } finally {
            if (modified) {
                databaseService.backWritable(representativeContextId, writeCon);
            } else {
                databaseService.backWritableAfterReading(representativeContextId, writeCon);
            }
        }
    }

    private boolean doMarkExecutionDone(CleanUpJob job, Connection writeCon) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = writeCon.prepareStatement("UPDATE cleanupJobExecution SET timestamp=?, running=0 WHERE id=?");
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, job.getId().getIdentifier());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DatabaseCleanUpExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw DatabaseCleanUpExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public boolean refreshTimeStamp(CleanUpJob job, int representativeContextId) throws OXException {
        if (job.isRunsExclusive() == false) {
            return false;
        }

        DatabaseService databaseService = services.getServiceSafe(DatabaseService.class);
        boolean modified = false;
        Connection writeCon = databaseService.getWritable(representativeContextId);
        try {
            modified = doRefreshTimeStamp(job, writeCon);
            return modified;
        } finally {
            if (modified) {
                databaseService.backWritable(representativeContextId, writeCon);
            } else {
                databaseService.backWritableAfterReading(representativeContextId, writeCon);
            }
        }
    }

    private boolean doRefreshTimeStamp(CleanUpJob job, Connection writeCon) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = writeCon.prepareStatement("UPDATE cleanupJobExecution SET timestamp=? WHERE id=? AND running=1");
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, job.getId().getIdentifier());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DatabaseCleanUpExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw DatabaseCleanUpExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public boolean checkExecutionPermission(CleanUpJob job, int representativeContextId) throws OXException {
        if (job.isRunsExclusive() == false) {
            return true;
        }

        DatabaseService databaseService = services.getServiceSafe(DatabaseService.class);
        ConnectionProvider connectionProvider = null;
        Connection connection = databaseService.getReadOnly(representativeContextId);
        try {
            connectionProvider = new ConnectionProvider(connection, representativeContextId, databaseService);
            connection = null;
            boolean modified = doCheckExecutionPermission(job, connectionProvider);
            if (modified) {
                connectionProvider.markModified();
            }
            return modified;
        } finally {
            if (connectionProvider != null) {
                connectionProvider.closeConnection();
            }
            if (connection != null) {
                databaseService.backReadOnly(representativeContextId, connection);
            }
        }
    }

    private boolean doCheckExecutionPermission(CleanUpJob job, ConnectionProvider connectionProvider) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connectionProvider.getConnection().prepareStatement("SELECT timestamp, running FROM cleanupJobExecution WHERE id=?");
            stmt.setString(1, job.getId().getIdentifier());
            rs = stmt.executeQuery();
            ExecutionInfo executionInfo = rs.next() ? new ExecutionInfo(rs.getLong(1), rs.getInt(2) > 0) : null;
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            if (executionInfo == null) {
                // Not present
                connectionProvider.switchToReadWrite();
                stmt = connectionProvider.getConnection().prepareStatement("INSERT INTO cleanupJobExecution (id, timestamp, running) VALUES (?, ?, 1)");
                stmt.setString(1, job.getId().getIdentifier());
                stmt.setLong(2, System.currentTimeMillis());
                try {
                    stmt.executeUpdate();
                    return true;
                } catch (SQLException e) {
                    if (Databases.isPrimaryKeyConflictInMySQL(e)) {
                        return false;
                    }
                    throw e;
                }
            }

            // Is present
            long now = System.currentTimeMillis();
            long numberOfMillisSinceLastRun = numberOfMillisSinceLastRun(executionInfo, now);
            boolean expired = false;
            if (executionInfo.running) {
                // According to "running" flag another process is still running an execution. Check if considered as expired...
                if (numberOfMillisSinceLastRun <= getExpirationDurationMillis()) {
                    // NOT expired. Apparently another process is running an execution.
                    return false;
                }

                // Consider as expired
                expired = true;
            }

            if (expired == false && numberOfMillisSinceLastRun < job.getDelay().toMillis()) {
                // Another process in cluster already performed the clean-up execution and delay has not yet expired
                return false;
            }

            // Either not running or expired. Try to update (compare-and-set)
            connectionProvider.switchToReadWrite();
            if (expired) {
                stmt = connectionProvider.getConnection().prepareStatement("UPDATE cleanupJobExecution SET timestamp=? WHERE id=? AND timestamp=?");
            } else {
                stmt = connectionProvider.getConnection().prepareStatement("UPDATE cleanupJobExecution SET timestamp=?, running=1 WHERE id=? AND timestamp=? AND running=0");
            }
            stmt.setLong(1, now);
            stmt.setString(2, job.getId().getIdentifier());
            stmt.setLong(3, executionInfo.timestamp);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DatabaseCleanUpExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw DatabaseCleanUpExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private static long getExpirationDurationMillis() {
        return DatabaseCleanUpServiceImpl.EXPIRATION_MILLIS;
    }

    private static long numberOfMillisSinceLastRun(ExecutionInfo executionInfo, long now) {
        return now - executionInfo.timestamp;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class ExecutionInfo {

        /** The time stamp when last execution was finished/started */
        final long timestamp;

        /** Whether execution still running */
        final boolean running;

        /**
         * Initializes a new {@link ExecutionInfo}.
         *
         * @param timestamp The time stamp when last execution was finished/started
         * @param running Whether execution is still running
         */
        ExecutionInfo(long timestamp, boolean running) {
            super();
            this.timestamp = timestamp;
            this.running = running;
        }
    } // End of class ExecutionInfo

    private static enum ConnectionUsage {
        /**
         * A read-only connection was used exclusively.
         */
        READ_ONLY,
        /**
         * A read-write connection was used, but nothing modified.
         */
        UNMODIFIED,
        /**
         * A read-write connection was used and data has been modified.
         */
        MODIFIED;
    }

    private static class ConnectionProvider {

        private final DatabaseService databaseService;
        private final int representativeContextId;
        private Connection connection;
        private ConnectionUsage connectionUsage;

        /**
         * Initializes a new {@link ConnectionProvider}.
         *
         * @param connection The read-only connection
         * @param representativeContextId The representative context identifier
         * @param databaseService The database service
         */
        ConnectionProvider(Connection connection, int representativeContextId, DatabaseService databaseService) {
            super();
            this.connection = connection;
            this.representativeContextId = representativeContextId;
            this.databaseService = databaseService;
            this.connectionUsage = ConnectionUsage.READ_ONLY;
        }

        Connection getConnection() {
            return connection;
        }

        void switchToReadWrite() throws OXException {
            if (connectionUsage == ConnectionUsage.READ_ONLY) {
                databaseService.backReadOnly(representativeContextId, connection);
                connection = null;
                connection = databaseService.getWritable(representativeContextId);
                connectionUsage = ConnectionUsage.UNMODIFIED;
            }
        }

        void markModified() {
            if (connectionUsage == ConnectionUsage.UNMODIFIED) {
                connectionUsage = ConnectionUsage.MODIFIED;
            }
        }

        void closeConnection() {
            switch (connectionUsage) {
                case MODIFIED:
                    databaseService.backWritable(representativeContextId, connection);
                    break;
                case READ_ONLY:
                    databaseService.backReadOnly(representativeContextId, connection);
                    break;
                case UNMODIFIED:
                    databaseService.backWritableAfterReading(representativeContextId, connection);
                    break;
                default:
                    break;
            }
            connection = null;
        }
    }

}
