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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.database.internal.wrapping;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.logging.Log;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.internal.AssignmentImpl;
import com.openexchange.database.internal.Pools;
import com.openexchange.database.internal.ReplicationMonitor;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;

/**
 * {@link JDBC4ConnectionReturner}
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class JDBC4ConnectionReturner implements Connection {
    
    private final static Log LOG = LogFactory.getLog(JDBC4ConnectionReturner.class);

    private final Pools pools;

    private final AssignmentImpl assign;

    protected Connection delegate;

    private final boolean noTimeout;

    private final boolean write;

    private boolean usedAsRead;

    private final ReplicationMonitor monitor;

    private boolean usedForUpdate = false;

    public JDBC4ConnectionReturner(final Pools pools, final ReplicationMonitor monitor, final AssignmentImpl assign, final Connection delegate, final boolean noTimeout, final boolean write, final boolean usedAsRead) {
        super();
        this.pools = pools;
        this.assign = assign;
        this.delegate = delegate;
        this.noTimeout = noTimeout;
        this.write = write;
        this.monitor = monitor;
        this.usedAsRead = usedAsRead;
    }

    public void setUsedAsRead(boolean b) {
        this.usedAsRead = b;
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public void clearWarnings() throws SQLException {
        checkForAlreadyClosed();
        delegate.clearWarnings();
    }

    @Override
    public void close() throws SQLException {
        if (null == delegate) {
            throw new SQLException("Connection is already closed.");
        }
        final Connection toReturn = delegate;
        delegate = null;
        if (write && !assign.isToConfigDB() && usedForUpdate) {
            try {
                // Transaction has not been initiated -- Do CAS-style increment
                int contextId = assign.getContextId();
                long transactionCounter;
                do {
                    transactionCounter = getTransactionCount(contextId, toReturn);
                    if (transactionCounter < 0) {
                        throw new SQLException("Updating transaction for replication monitor failed for context " + contextId + ".");
                    }
                } while (!compareAndSet(transactionCounter, transactionCounter + 1, contextId, toReturn));
                assign.setTransaction(transactionCounter + 1);
            } catch (SQLException e) {
                if (1146 == e.getErrorCode()) {
                    if (ReplicationMonitor.getLastLogged() + 300000 < System.currentTimeMillis()) {
                        ReplicationMonitor.setLastLogged(System.currentTimeMillis());
                        final OXException e1 = DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
                        LOG.error(e1.getMessage(), e1);
                    }
                } else {
                    final OXException e1 = DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
                    LOG.error(e1.getMessage(), e1);
                }
            }
        }
        monitor.backAndIncrementTransaction(pools, assign, toReturn, noTimeout, write, usedAsRead, usedForUpdate);
    }
    
    private long getTransactionCount(final int contextId, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT transaction FROM replicationMonitor WHERE cid=?");
            stmt.setInt(1, contextId);
            result = stmt.executeQuery();
            return result.next() ? result.getLong(1) : -1L;
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private boolean compareAndSet(final long expect, final long update, final int contextId, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE replicationMonitor SET transaction=? WHERE cid=? AND transaction=?");
            stmt.setLong(1, update);
            stmt.setInt(2, contextId);
            stmt.setLong(3, expect);
            return stmt.executeUpdate() > 0;
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void commit() throws SQLException {
        checkForAlreadyClosed();
        if (write && !assign.isToConfigDB() && usedForUpdate) {
            int contextId = assign.getContextId();
            // Increment transaction counter dependent on transaction state or not
            boolean inTransaction = !delegate.getAutoCommit();
            if (inTransaction) {
                // Ok -- We are already in a transaction
                Savepoint save = delegate.setSavepoint();
                PreparedStatement stmt = null;
                ResultSet result = null;
                try {
                    stmt = delegate.prepareStatement("UPDATE replicationMonitor SET transaction=transaction+1 WHERE cid=?");
                    stmt.setInt(1, contextId);
                    stmt.execute();
                    stmt.close();
                    stmt = delegate.prepareStatement("SELECT transaction FROM replicationMonitor WHERE cid=?");
                    stmt.setInt(1, assign.getContextId());
                    result = stmt.executeQuery();
                    if (result.next()) {
                        assign.setTransaction(result.getLong(1));
                    } else {
                        LOG.error("Updating transaction for replication monitor failed for context " + contextId + ".");
                    }
                    usedForUpdate = false;
                } catch (SQLException e) {
                    delegate.rollback(save);
                    if (1146 == e.getErrorCode()) {
                        if (ReplicationMonitor.getLastLogged() + 300000 < System.currentTimeMillis()) {
                            ReplicationMonitor.setLastLogged(System.currentTimeMillis());
                            final OXException e1 = DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
                            LOG.error(e1.getMessage(), e1);
                        }
                    } else {
                        final OXException e1 = DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
                        LOG.error(e1.getMessage(), e1);
                    }
                } finally {
                    closeSQLStuff(result, stmt);
                }
            }
        }
        // Delegate commit() to underlying connection
        delegate.commit();
    }

    @Override
    public Statement createStatement() throws SQLException {
        checkForAlreadyClosed();
        return new JDBC41StatementWrapper(delegate.createStatement(), this);
    }

    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
        checkForAlreadyClosed();
        return new JDBC41StatementWrapper(delegate.createStatement(resultSetType, resultSetConcurrency), this);
    }

    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        checkForAlreadyClosed();
        return new JDBC41StatementWrapper(delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability), this);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        checkForAlreadyClosed();
        return delegate.getAutoCommit();
    }

    @Override
    public String getCatalog() throws SQLException {
        checkForAlreadyClosed();
        return delegate.getCatalog();
    }

    @Override
    public int getHoldability() throws SQLException {
        checkForAlreadyClosed();
        return delegate.getHoldability();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        checkForAlreadyClosed();
        return delegate.getMetaData();
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        checkForAlreadyClosed();
        return delegate.getTransactionIsolation();
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        checkForAlreadyClosed();
        return delegate.getTypeMap();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        checkForAlreadyClosed();
        return delegate.getWarnings();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return delegate == null || delegate.isClosed();
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        checkForAlreadyClosed();
        return delegate.isReadOnly();
    }

    @Override
    public String nativeSQL(final String sql) throws SQLException {
        checkForAlreadyClosed();
        return delegate.nativeSQL(sql);
    }

    @Override
    public CallableStatement prepareCall(final String sql) throws SQLException {
        checkForAlreadyClosed();
        return delegate.prepareCall(sql);
    }

    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        checkForAlreadyClosed();
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        checkForAlreadyClosed();
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        checkForAlreadyClosed();
        return new JDBC41PreparedStatementWrapper(delegate.prepareStatement(sql), this);
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
        checkForAlreadyClosed();
        return new JDBC41PreparedStatementWrapper(delegate.prepareStatement(sql, autoGeneratedKeys), this);
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        checkForAlreadyClosed();
        return new JDBC41PreparedStatementWrapper(delegate.prepareStatement(sql, resultSetType, resultSetConcurrency), this);
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        checkForAlreadyClosed();
        return new JDBC41PreparedStatementWrapper(
            delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability),
            this);
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
        checkForAlreadyClosed();
        return new JDBC41PreparedStatementWrapper(delegate.prepareStatement(sql, columnIndexes), this);
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
        checkForAlreadyClosed();
        return new JDBC41PreparedStatementWrapper(delegate.prepareStatement(sql, columnNames), this);
    }

    @Override
    public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
        checkForAlreadyClosed();
        delegate.releaseSavepoint(savepoint);
    }

    @Override
    public void rollback() throws SQLException {
        checkForAlreadyClosed();
        delegate.rollback();
    }

    @Override
    public void rollback(final Savepoint savepoint) throws SQLException {
        checkForAlreadyClosed();
        delegate.rollback(savepoint);
    }

    @Override
    public void setAutoCommit(final boolean autoCommit) throws SQLException {
        checkForAlreadyClosed();
        delegate.setAutoCommit(autoCommit);
    }

    @Override
    public void setCatalog(final String catalog) throws SQLException {
        checkForAlreadyClosed();
        delegate.setCatalog(catalog);
    }

    @Override
    public void setHoldability(final int holdability) throws SQLException {
        checkForAlreadyClosed();
        delegate.setHoldability(holdability);
    }

    @Override
    public void setReadOnly(final boolean readOnly) throws SQLException {
        checkForAlreadyClosed();
        delegate.setReadOnly(readOnly);
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        checkForAlreadyClosed();
        return delegate.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(final String name) throws SQLException {
        checkForAlreadyClosed();
        return delegate.setSavepoint(name);
    }

    @Override
    public void setTransactionIsolation(final int level) throws SQLException {
        checkForAlreadyClosed();
        delegate.setTransactionIsolation(level);
    }

    @Override
    public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
        checkForAlreadyClosed();
        delegate.setTypeMap(map);
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        checkForAlreadyClosed();
        return delegate.createArrayOf(typeName, elements);
    }

    @Override
    public Blob createBlob() throws SQLException {
        checkForAlreadyClosed();
        return delegate.createBlob();
    }

    @Override
    public Clob createClob() throws SQLException {
        checkForAlreadyClosed();
        return delegate.createClob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        checkForAlreadyClosed();
        return delegate.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        checkForAlreadyClosed();
        return delegate.createSQLXML();
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        checkForAlreadyClosed();
        return delegate.createStruct(typeName, attributes);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        checkForAlreadyClosed();
        return delegate.getClientInfo();
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        checkForAlreadyClosed();
        return delegate.getClientInfo(name);
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        checkForAlreadyClosed();
        return delegate.isValid(timeout);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        delegate.setClientInfo(properties);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        delegate.setClientInfo(name, value);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        checkForAlreadyClosed();
        return delegate.isWrapperFor(iface);
    }

    public void updatePerformed() {
        usedForUpdate = true;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        checkForAlreadyClosed();
        return delegate.unwrap(iface);
    }

    protected void checkForAlreadyClosed() throws SQLException {
        if (null == delegate) {
            throw new SQLException("Connection was already closed.");
        }
    }

    // ------------------------------------------------------------------------------ //

    /**
     * Closes the ResultSet.
     *
     * @param result <code>null</code> or a ResultSet to close.
     */
    private static void closeSQLStuff(final ResultSet result) {
        if (result != null) {
            try {
                result.close();
            } catch (final SQLException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Closes the {@link Statement}.
     *
     * @param stmt <code>null</code> or a {@link Statement} to close.
     */
    private static void closeSQLStuff(final Statement stmt) {
        if (null != stmt) {
            try {
                stmt.close();
            } catch (final SQLException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Closes the ResultSet and the Statement.
     *
     * @param result <code>null</code> or a ResultSet to close.
     * @param stmt <code>null</code> or a Statement to close.
     */
    private static void closeSQLStuff(final ResultSet result, final Statement stmt) {
        closeSQLStuff(result);
        closeSQLStuff(stmt);
    }
}
