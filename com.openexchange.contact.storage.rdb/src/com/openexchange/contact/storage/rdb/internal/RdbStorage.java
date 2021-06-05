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

package com.openexchange.contact.storage.rdb.internal;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.provider.ContactsProviderExceptionCodes;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RdbStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public abstract class RdbStorage {

    private static final Logger LOG = LoggerFactory.getLogger(RdbStorage.class);
    protected final Context context;
    protected final DBProvider dbProvider;
    protected final DBTransactionPolicy txPolicy;

    /**
     * Initializes a new {@link RdbStorage}.
     * 
     * @param context The context
     * @param dbProvider The database provider
     * @param txPolicy The transaction policy
     */
    public RdbStorage(Context context, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super();
        this.context = context;
        this.dbProvider = dbProvider;
        this.txPolicy = txPolicy;
    }

    /**
     * Logs & executes a prepared statement's SQL query.
     *
     * @param stmt The statement to execute the SQL query from
     * @return The result set
     */
    protected ResultSet logExecuteQuery(PreparedStatement stmt) throws SQLException {
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
    protected int logExecuteUpdate(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeUpdate();
        }
        String statementString = String.valueOf(stmt);
        long start = System.currentTimeMillis();
        int rowCount = stmt.executeUpdate();
        LOG.debug("executeUpdate: {} - {} rows affected, {} ms elapsed.", statementString, I(rowCount), L(System.currentTimeMillis() - start));
        return rowCount;
    }

    /**
     * Safely releases a write connection obeying the configured transaction policy, rolling back automatically if not committed before.
     *
     * @param connection The write connection to release
     * @param updated The number of actually updated rows to
     */
    protected void release(Connection connection, int updated) throws OXException {
        if (null == connection) {
            return;
        }
        try {
            if (false == isAutoCommitSafe(connection)) {
                txPolicy.rollback(connection);
            }
            txPolicy.setAutoCommit(connection, true);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            if (0 < updated) {
                dbProvider.releaseWriteConnection(context, connection);
            } else {
                dbProvider.releaseWriteConnectionAfterReading(context, connection);
            }
        }
    }

    /**
     * Tries to retrieve the current auto-commit mode for given connection.
     *
     * @param connection The connection to determine auto-commit mode from
     * @return <code>true</code> if auto-commit mode is enabled; otherwise <code>false</code>
     */
    protected static boolean isAutoCommitSafe(Connection connection) {
        try {
            return connection.getAutoCommit();
        } catch (Exception e) {
            LOG.warn("Failed to retrieve the current auto-commit mode.", e);
        }
        return true;
    }

    /**
     * Gets an {@link OXException} appropriate for the supplied {@link SQLException}.
     *
     * @param e The SQL exception to get the OX exception for
     * @return The OX exception
     */
    protected OXException asOXException(SQLException e) {
        if (DBUtils.isTransactionRollbackException(e)) {
            return ContactsProviderExceptionCodes.DB_ERROR_TRY_AGAIN.create(e, e.getMessage());
        }
        return ContactsProviderExceptionCodes.DB_ERROR.create(e, e.getMessage());
    }
}
