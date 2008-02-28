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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.tools.oxfolder;

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
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;

/**
 * {@link TransactionConnection} - Performs a transaction on delegate connection
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class TransactionConnection implements Connection {

	private final Connection con;

	private final boolean performTransaction;

	/**
	 * Initializes a new {@link TransactionConnection}
	 * 
	 * @param con
	 *            The delegate connection on which a transaction is performed;
	 *            {@link #setAutoCommit(boolean)} is invoked with argument set
	 *            to <code>false</code> only if current auto-commit status is
	 *            set to <code>true</code>.
	 * @throws SQLException
	 *             If given connection's auto-commit status cannot be determined
	 */
	public TransactionConnection(final Connection con) throws SQLException {
		super();
		this.con = con;
		performTransaction = con.getAutoCommit();
		if (performTransaction) {
			con.setAutoCommit(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#clearWarnings()
	 */
	public void clearWarnings() throws SQLException {
		con.clearWarnings();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#close()
	 */
	public void close() throws SQLException {
		con.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#commit()
	 */
	public void commit() throws SQLException {
		if (performTransaction) {
			con.commit();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createArrayOf(java.lang.String,
	 *      java.lang.Object[])
	 */
	public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
		return con.createArrayOf(typeName, elements);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createBlob()
	 */
	public Blob createBlob() throws SQLException {
		return con.createBlob();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createClob()
	 */
	public Clob createClob() throws SQLException {
		return con.createClob();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createNClob()
	 */
	public NClob createNClob() throws SQLException {
		return con.createNClob();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createSQLXML()
	 */
	public SQLXML createSQLXML() throws SQLException {
		return con.createSQLXML();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createStatement()
	 */
	public Statement createStatement() throws SQLException {
		return con.createStatement();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createStatement(int, int)
	 */
	public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
		return con.createStatement(resultSetType, resultSetConcurrency);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createStatement(int, int, int)
	 */
	public Statement createStatement(final int resultSetType, final int resultSetConcurrency,
			final int resultSetHoldability) throws SQLException {
		return con.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createStruct(java.lang.String,
	 *      java.lang.Object[])
	 */
	public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
		return con.createStruct(typeName, attributes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getAutoCommit()
	 */
	public boolean getAutoCommit() throws SQLException {
		/*
		 * Always return false to indicate transaction
		 */
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getCatalog()
	 */
	public String getCatalog() throws SQLException {
		return con.getCatalog();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getClientInfo()
	 */
	public Properties getClientInfo() throws SQLException {
		return con.getClientInfo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getClientInfo(java.lang.String)
	 */
	public String getClientInfo(final String name) throws SQLException {
		return con.getClientInfo(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getHoldability()
	 */
	public int getHoldability() throws SQLException {
		return con.getHoldability();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getMetaData()
	 */
	public DatabaseMetaData getMetaData() throws SQLException {
		return con.getMetaData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getTransactionIsolation()
	 */
	public int getTransactionIsolation() throws SQLException {
		return con.getTransactionIsolation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getTypeMap()
	 */
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return con.getTypeMap();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getWarnings()
	 */
	public SQLWarning getWarnings() throws SQLException {
		return con.getWarnings();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#isClosed()
	 */
	public boolean isClosed() throws SQLException {
		return con.isClosed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#isReadOnly()
	 */
	public boolean isReadOnly() throws SQLException {
		return con.isReadOnly();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#isValid(int)
	 */
	public boolean isValid(final int timeout) throws SQLException {
		return con.isValid(timeout);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#nativeSQL(java.lang.String)
	 */
	public String nativeSQL(final String sql) throws SQLException {
		return con.nativeSQL(sql);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareCall(java.lang.String)
	 */
	public CallableStatement prepareCall(final String sql) throws SQLException {
		return con.prepareCall(sql);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
	 */
	public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency)
			throws SQLException {
		return con.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
	 */
	public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency,
			final int resultSetHoldability) throws SQLException {
		return con.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String)
	 */
	public PreparedStatement prepareStatement(final String sql) throws SQLException {
		return con.prepareStatement(sql);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int)
	 */
	public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
		return con.prepareStatement(sql, autoGeneratedKeys);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
	 */
	public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
		return con.prepareStatement(sql, columnIndexes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String,
	 *      java.lang.String[])
	 */
	public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
		return con.prepareStatement(sql, columnNames);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
	 */
	public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency)
			throws SQLException {
		return con.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int,
	 *      int)
	 */
	public PreparedStatement prepareStatement(final String sql, final int resultSetType,
			final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
		return con.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
	 */
	public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
		con.releaseSavepoint(savepoint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#rollback()
	 */
	public void rollback() throws SQLException {
		if (performTransaction) {
			con.rollback();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	public void rollback(final Savepoint savepoint) throws SQLException {
		if (performTransaction) {
			con.rollback(savepoint);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setAutoCommit(boolean)
	 */
	public void setAutoCommit(final boolean autoCommit) throws SQLException {
		if (autoCommit && performTransaction) {
			con.setAutoCommit(autoCommit);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setCatalog(java.lang.String)
	 */
	public void setCatalog(final String catalog) throws SQLException {
		con.setCatalog(catalog);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setClientInfo(java.util.Properties)
	 */
	public void setClientInfo(final Properties properties) throws SQLClientInfoException {
		con.setClientInfo(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setClientInfo(java.lang.String,
	 *      java.lang.String)
	 */
	public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
		con.setClientInfo(name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setHoldability(int)
	 */
	public void setHoldability(final int holdability) throws SQLException {
		con.setHoldability(holdability);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setReadOnly(boolean)
	 */
	public void setReadOnly(final boolean readOnly) throws SQLException {
		con.setReadOnly(readOnly);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setSavepoint()
	 */
	public Savepoint setSavepoint() throws SQLException {
		return con.setSavepoint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setSavepoint(java.lang.String)
	 */
	public Savepoint setSavepoint(final String name) throws SQLException {
		return con.setSavepoint(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setTransactionIsolation(int)
	 */
	public void setTransactionIsolation(final int level) throws SQLException {
		con.setTransactionIsolation(level);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setTypeMap(java.util.Map)
	 */
	public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
		con.setTypeMap(map);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	public boolean isWrapperFor(final Class<?> iface) throws SQLException {
		return con.isWrapperFor(iface);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	public <T> T unwrap(final Class<T> iface) throws SQLException {
		return con.unwrap(iface);
	}

}
