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

package com.openexchange.database.internal;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ConnectionWrapper} is a wrapper for db {@link Connection}s which stores the creation time for the connection. 
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class ConnectionWrapper implements Connection {
    
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionWrapper.class);
    
    private final long start;
    private final Connection delegate;
    
    /**
     * Initializes a new {@link ConnectionWrapper}.
     * 
     * @param start The creation time for the connection
     * @param delegate The connection to delegate to
     */
    public ConnectionWrapper(Connection delegate) {
        this.start = System.nanoTime();
        this.delegate = delegate;
    }
    
    /**
     * The Timeout error code
     */
    private static final int ERROR_CODE = new SQLTimeoutException().getErrorCode();
    
    /**
     * Checks if the connection has run into any timeouts
     * 
     * @return Whether the connection has run into any timeouts or not
     */
    public boolean hasTimeout() {
        SQLWarning warnings;
        try {
            warnings = delegate.getWarnings();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
        if(warnings == null) {
            return false;
        }
        return warnings.getErrorCode() == ERROR_CODE;
    }
    
    /**
     * Gets the delegate
     *
     * @return The delegate
     */
    public Connection getDelegate() {
        return delegate;
    }
    
    /**
     * Gets the start
     *
     * @return The start
     */
    public long getStart() {
        return start;
    }
    
    @Override
    public void abort(Executor executor) throws SQLException {
        delegate.abort(executor);
    }
    @Override
    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }
    @Override
    public void close() throws SQLException {
        delegate.close();
    }
    @Override
    public void commit() throws SQLException {
        delegate.commit();
    }
    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return delegate.createArrayOf(typeName, elements);
    }
    @Override
    public Blob createBlob() throws SQLException {
        return delegate.createBlob();
    }
    @Override
    public Clob createClob() throws SQLException {
        return delegate.createClob();
    }
    @Override
    public NClob createNClob() throws SQLException {
        return delegate.createNClob();
    }
    @Override
    public SQLXML createSQLXML() throws SQLException {
        return delegate.createSQLXML();
    }
    @Override
    public Statement createStatement() throws SQLException {
        return delegate.createStatement();
    }
    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return delegate.createStatement(resultSetType, resultSetConcurrency);
    }
    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return delegate.createStruct(typeName, attributes);
    }
    @Override
    public boolean getAutoCommit() throws SQLException {
        return delegate.getAutoCommit();
    }
    @Override
    public String getCatalog() throws SQLException {
        return delegate.getCatalog();
    }
    @Override
    public Properties getClientInfo() throws SQLException {
        return delegate.getClientInfo();
    }
    @Override
    public String getClientInfo(String name) throws SQLException {
        return delegate.getClientInfo(name);
    }
    @Override
    public int getHoldability() throws SQLException {
        return delegate.getHoldability();
    }
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return delegate.getMetaData();
    }
    @Override
    public int getNetworkTimeout() throws SQLException {
        return delegate.getNetworkTimeout();
    }
    @Override
    public String getSchema() throws SQLException {
        return delegate.getSchema();
    }
    @Override
    public int getTransactionIsolation() throws SQLException {
        return delegate.getTransactionIsolation();
    }
    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return delegate.getTypeMap();
    }
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }
    @Override
    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }
    @Override
    public boolean isReadOnly() throws SQLException {
        return delegate.isReadOnly();
    }
    @Override
    public boolean isValid(int timeout) throws SQLException {
        return delegate.isValid(timeout);
    }
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }
    @Override
    public String nativeSQL(String sql) throws SQLException {
        return delegate.nativeSQL(sql);
    }
    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
    }
    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return delegate.prepareCall(sql);
    }
    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }
    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return delegate.prepareStatement(sql, autoGeneratedKeys);
    }
    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return delegate.prepareStatement(sql, columnIndexes);
    }
    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return delegate.prepareStatement(sql, columnNames);
    }
    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return delegate.prepareStatement(sql);
    }
    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        delegate.releaseSavepoint(savepoint);
    }
    @Override
    public void rollback() throws SQLException {
        delegate.rollback();
    }
    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        delegate.rollback(savepoint);
    }
    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        delegate.setAutoCommit(autoCommit);
    }
    @Override
    public void setCatalog(String catalog) throws SQLException {
        delegate.setCatalog(catalog);
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
    public void setHoldability(int holdability) throws SQLException {
        delegate.setHoldability(holdability);
    }
    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        delegate.setNetworkTimeout(executor, milliseconds);
    }
    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        delegate.setReadOnly(readOnly);
    }
    @Override
    public Savepoint setSavepoint() throws SQLException {
        return delegate.setSavepoint();
    }
    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return delegate.setSavepoint(name);
    }
    @Override
    public void setSchema(String schema) throws SQLException {
        delegate.setSchema(schema);
    }
    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        delegate.setTransactionIsolation(level);
    }
    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        delegate.setTypeMap(map);
    }
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }
    
}
