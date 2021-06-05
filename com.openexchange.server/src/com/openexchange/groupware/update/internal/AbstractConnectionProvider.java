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

package com.openexchange.groupware.update.internal;

import static com.openexchange.exception.ExceptionUtils.isEitherOf;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.ConnectionProvider;


/**
 * {@link AbstractConnectionProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public abstract class AbstractConnectionProvider implements ConnectionProvider {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractConnectionProvider.class);
    }

    /** For creating/closing connections */
    private final ConnectionAccess connectionAccess;

    /** The connection reference */
    private Connection connection;

    /**
     * Initializes a new {@link AbstractConnectionProvider}.
     *
     * @param connectionAccess The connection access for creating/closing connections
     */
    protected AbstractConnectionProvider(ConnectionAccess connectionAccess) {
        super();
        this.connectionAccess = connectionAccess;
    }

    /**
     * Checks given connection if suitable for being further used.
     * <p>
     * In detail, this routine attempts to set given connection to auto-commit mode. In case such an attempt yields a
     * <code>java.io.EOFException</code> the given connection appears to be closed by the remote host (database). Then <code>null</code> is
     * is returned.
     *
     * @param connection The connection
     * @return The checked connection or <code>null</code> if closed by remote host (database)
     */
    private Connection checkConnectionElseReturnNull(Connection connection) {
        if (null != connection) {
            // Ensure auto-commit mode is set for given connection
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                if (isEitherOf(e, java.io.EOFException.class)) {
                    // Connection closed by database system
                    return null;
                }
                LoggerHolder.LOG.error("Failed to set auto-commit mode", e);
            }
        }
        return connection;
    }

    @Override
    public synchronized Connection getConnection() throws OXException {
        Connection connection = this.connection;
        if (null == connection) {
            connection = connectionAccess.getConnection();
            this.connection = connection;
            return connection;
        }

        // Already in use. Check connection.
        connection = checkConnectionElseReturnNull(connection);
        if (null == connection) {
            // Closed by remote host (database). Null'ify this.connection and try to lease a new connection
            closeSafelyAfterCheck(this.connection);
            this.connection = null;
            connection = connectionAccess.getConnection();
            this.connection = connection;
        }
        return connection;
    }

    private void closeSafelyAfterCheck(Connection connection) {
        if (null != connection) {
            try {
                connectionAccess.closeConnection(connection);
            } catch (Exception x) {
                LoggerHolder.LOG.debug("Failed to close connection", x);
            }
        }
    }

    /**
     * Closes the connection.
     */
    public synchronized void close() {
        Connection connection = this.connection;
        if (null != connection) {
            this.connection = null;
            connectionAccess.closeConnection(connection);
        }
    }

    @Override
    public int[] getContextsInSameSchema() throws OXException {
        return connectionAccess.getContextsInSameSchema();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Allows to create & close connections.
     */
    public static interface ConnectionAccess {

        /**
         * Gets a new connection.
         *
         * @return The new connection
         * @throws OXException If a new connection cannot be returned
         */
        Connection getConnection() throws OXException;

        /**
         * Closes given connection
         *
         * @param connection The connection to close
         */
        void closeConnection(Connection connection);

        /**
         * Finds all contexts their data is stored in the same schema and on the same database like the given one.
         *
         * @return all contexts having their data in the same schema and on the same database.
         * @throws OXException if some problem occurs.
         */
        int[] getContextsInSameSchema() throws OXException;
    }

}
