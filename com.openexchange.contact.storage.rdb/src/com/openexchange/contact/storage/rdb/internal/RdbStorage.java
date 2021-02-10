/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
