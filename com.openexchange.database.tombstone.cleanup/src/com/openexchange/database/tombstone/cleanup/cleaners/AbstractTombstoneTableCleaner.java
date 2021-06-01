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

package com.openexchange.database.tombstone.cleanup.cleaners;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import com.openexchange.database.cleanup.CleanUpExecution;
import com.openexchange.database.cleanup.CleanUpExecutionConnectionProvider;
import com.openexchange.exception.OXException;

/**
 * {@link TombstoneTableCleaner}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public abstract class AbstractTombstoneTableCleaner implements CleanUpExecution {

    /** The logger constant */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractTombstoneTableCleaner.class);

    private long timespan = -1;

    /**
     * Initializes a new {@link AbstractTombstoneTableCleaner}.
     */
    protected AbstractTombstoneTableCleaner() {
        super();
    }

    /**
     * Cleans up the associated table based on an already existing schema connection.
     *
     * @param connection Write connection to the destination schema
     * @param timestamp The time stamp defining the border of what will be removed which means older entries than the given time stamp will be removed
     * @return A map containing the number of items that have been deleted by the {@link TombstoneTableCleaner} mapped to the table name
     * @throws SQLException In case data cannot be removed
     */
    public Map<String, Integer> cleanup(Connection connection, long timestamp) throws SQLException {
        try {
            checkTables(connection);
        } catch (SQLException e) {
            LOG.error("Error while checking table design: {}. Skip cleaning up this table.", e.getMessage(), e);
            return Collections.emptyMap();
        } catch (OXException e) {
            LOG.warn("Table(s) is/are in an inappropriate design: {}. Skip cleaning up this table.", e.getMessage(), e);
            return Collections.emptyMap();
        }
        return cleanupSafe(connection, timestamp);
    }

    @Override
    public void executeFor(String schema, int representativeContextId, int databasePoolId, Map<String, Object> state, CleanUpExecutionConnectionProvider connectionProvider) throws OXException {
        try {
            cleanupSafe(connectionProvider.getConnection(), getTimestamp());
        } catch (SQLException e) {
            throw new OXException(e);
        }
    }

    @Override
    public boolean isApplicableFor(String schema, int representativeContextId, int databasePoolId, Map<String, Object> state, CleanUpExecutionConnectionProvider connectionProvider) throws OXException {
        try {
            checkTables(connectionProvider.getConnection());
        } catch (@SuppressWarnings("unused") OXException e) {
            return false;
        } catch (Exception e) {
            throw new OXException(e);
        }
        return true;
    }

    /**
     *
     * Sets the timespan for cleaning tombstone tables. The setting of timespan is mandatory for using {@link #executeFor(String, int, int, Map, CleanUpExecutionConnectionProvider)}.
     *
     * @param timespan The timespan in millis
     */
    public void setTimespan(long timespan) {
        this.timespan = timespan;
    }

    /**
     * Ensures to have tables in a state to be cleaned up. If a table is not in the desired state an {@link OXException} will be thrown.
     *
     * @param connection {@link Connection} to retrieve table meta information
     * @throws OXException In case table validation fails
     * @throws SQLException If an error occurred while retrieving table information
     */
    protected abstract void checkTables(Connection connection) throws OXException, SQLException;

    /**
     * Delegate for {@link TombstoneTableCleaner#cleanup(Connection, long)} that will be called after the table design has been verified by {@link #checkTables(Connection)}
     *
     * @param connection Write connection to the destination schema
     * @param timestamp Timestamp defining the border of what will be removed which means older entries than the given timestamp will be removed
     * @return {@link Map} Containing the number of items that have been deleted by the {@link TombstoneTableCleaner} mapped to the table
     * @throws SQLException In case data can't be removed
     */
    protected abstract Map<String, Integer> cleanupSafe(Connection connection, long timestamp) throws SQLException;

    /**
     * Logs & executes a prepared statement's SQL query.
     *
     * @param stmt The statement to execute the SQL query from
     * @return The result set
     */
    protected final static ResultSet logExecuteQuery(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeQuery();
        }
        String statementString = String.valueOf(stmt);
        long start = System.currentTimeMillis();
        ResultSet resultSet = stmt.executeQuery();
        LOG.debug("executeQuery: {} - {} ms elapsed.", statementString, L(System.currentTimeMillis() - start));
        return resultSet;
    }

    /**
     * Logs & executes a prepared statement's SQL update.
     *
     * @param stmt The statement to execute the SQL update from
     * @return The number of affected rows
     */
    protected final static int logExecuteUpdate(final PreparedStatement stmt) throws SQLException {
        try {
            if (false == LOG.isDebugEnabled()) {
                return stmt.executeUpdate();
            }
            long start = System.currentTimeMillis();
            final int rowCount = stmt.executeUpdate();
            LOG.debug("executeUpdate: {} - {} rows affected, {} ms elapsed.", stmt.toString(), I(rowCount), L(System.currentTimeMillis() - start));
            return rowCount;
        } catch (SQLException e) {
            LOG.warn("Error executing \"{}\": {}", stmt, e.getMessage());
            throw e;
        }
    }

    /**
     * Executes the given delete statement by setting the timestamp
     *
     * @param connection The write connection used for deletion
     * @param timestamp The timestamp to delete all entries before it
     * @param deleteStatement The statement that will be used for deletion
     * @throws SQLException
     */
    protected int delete(Connection connection, long timestamp, String deleteStatement) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(deleteStatement)) {
            int parameterIndex = 1;
            stmt.setLong(parameterIndex++, timestamp);
            return logExecuteUpdate(stmt);
        }
    }

    /**
     * Reads the timespan for cleaning tombstone tables and gets the timestamp until which the entries should be kept.
     *
     * @return The timestamp.
     * @throws OXException If no timespan is set.
     */
    private long getTimestamp() throws OXException {
        if (this.timespan == -1) {
            throw TombstoneCleanupExceptionCode.NO_TIMESPAN_ERROR.create(this.getClass().getName());
        }
        return System.currentTimeMillis() - this.timespan;
    }
}
