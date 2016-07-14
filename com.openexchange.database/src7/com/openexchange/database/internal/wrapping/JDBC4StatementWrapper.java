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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.database.internal.wrapping;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.database.Databases;
import com.openexchange.database.IncorrectStringSQLException;

/**
 * The method {@link #getConnection()} must be overwritten to return the {@link JDBC4ConnectionReturner}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class JDBC4StatementWrapper implements Statement {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JDBC4StatementWrapper.class);

    private static final String SQL_STATE_ABSENT_TABLE = "42S02";

    private static final Set<String> IGNORABLE_SQL_STATES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(SQL_STATE_ABSENT_TABLE)));

    /**
     * Logs the given syntax error (if appropriate)
     *
     * @param syntaxError The syntax error to log
     * @param delegate The statement leading to the syntax error
     * @param con The associated connection
     */
    protected static void logSyntaxError(java.sql.SQLSyntaxErrorException syntaxError, Statement delegate, Connection con) {
        String sqlState = syntaxError.getSQLState();
        if ((null == sqlState) || (false == IGNORABLE_SQL_STATES.contains(sqlState))) {
            String catalog = getCatalogSafe(con);
            if (null == catalog) {
                LOG.error("Error in SQL syntax in the following statement: {}", Databases.getSqlStatement(delegate, "<unknown>"), syntaxError);
            } else {
                LOG.error("Error in SQL syntax in the following statement on schema {}: {}", catalog, Databases.getSqlStatement(delegate, "<unknown>"), syntaxError);
            }
        }
    }

    /**
     * Logs the given read timeout error (if appropriate)
     *
     * @param sqlException The possible read timeout error to log
     * @param delegate The statement that encountered the read timeout error
     * @param con The associated connection
     */
    protected static void logReadTimeoutError(java.sql.SQLException sqlException, Statement delegate, Connection con) {
        if (sqlException.getCause() instanceof java.net.SocketTimeoutException) {
            String catalog = getCatalogSafe(con);
            if (null == catalog) {
                LOG.error("Read timeout encountered for the following statement: {}", Databases.getSqlStatement(delegate, "<unknown>"));
            } else {
                LOG.error("Read timeout encountered for the following statement on schema {}: {}", catalog, Databases.getSqlStatement(delegate, "<unknown>"));
            }
        }
    }

    private static String getCatalogSafe(Connection con) {
        if (null != con) {
            try {
                return con.getCatalog();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    private final Statement delegate;
    private final JDBC4ConnectionReturner con;

    /**
     * Initializes a new {@link JDBC4StatementWrapper}.
     *
     * @param delegate The delegate statement
     * @param con The connection returner
     */
    public JDBC4StatementWrapper(final Statement delegate, final JDBC4ConnectionReturner con) {
        super();
        this.delegate = delegate;
        this.con = con;
    }

    @Override
    public void addBatch(final String sql) throws SQLException {
        delegate.addBatch(sql);
    }

    @Override
    public void cancel() throws SQLException {
        delegate.cancel();
    }

    @Override
    public void clearBatch() throws SQLException {
        delegate.clearBatch();
    }

    @Override
    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    @Override
    public void close() throws SQLException {
        delegate.close();
    }

    /*
     * A note on the execute() methods:
     * We can't determine if the given statement led to multiple results without consuming them.
     * It is possible that a result set is followed by an update count. To be safe we have to
     * increment the replication monitor.
     */
    @Override
    public boolean execute(final String sql) throws SQLException {
        try {
            boolean retval = delegate.execute(sql);
            con.updatePerformed();
            return retval;
        } catch (java.sql.SQLSyntaxErrorException syntaxError) {
            logSyntaxError(syntaxError, delegate, con);
            throw syntaxError;
        } catch (java.sql.SQLException sqlException) {
            IncorrectStringSQLException incorrectStringError = IncorrectStringSQLException.instanceFor(sqlException);
            if (null != incorrectStringError) {
                throw incorrectStringError;
            }
            logReadTimeoutError(sqlException, delegate, con);
            throw sqlException;
        }
    }

    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        try {
            boolean retval = delegate.execute(sql, autoGeneratedKeys);
            con.updatePerformed();
            return retval;
        } catch (java.sql.SQLSyntaxErrorException syntaxError) {
            logSyntaxError(syntaxError, delegate, con);
            throw syntaxError;
        } catch (java.sql.SQLException sqlException) {
            IncorrectStringSQLException incorrectStringError = IncorrectStringSQLException.instanceFor(sqlException);
            if (null != incorrectStringError) {
                throw incorrectStringError;
            }
            logReadTimeoutError(sqlException, delegate, con);
            throw sqlException;
        }
    }

    @Override
    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        try {
            boolean retval = delegate.execute(sql, columnIndexes);
            con.updatePerformed();
            return retval;
        } catch (java.sql.SQLSyntaxErrorException syntaxError) {
            logSyntaxError(syntaxError, delegate, con);
            throw syntaxError;
        } catch (java.sql.SQLException sqlException) {
            IncorrectStringSQLException incorrectStringError = IncorrectStringSQLException.instanceFor(sqlException);
            if (null != incorrectStringError) {
                throw incorrectStringError;
            }
            logReadTimeoutError(sqlException, delegate, con);
            throw sqlException;
        }
    }

    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        try {
            boolean retval = delegate.execute(sql, columnNames);
            con.updatePerformed();
            return retval;
        } catch (java.sql.SQLSyntaxErrorException syntaxError) {
            logSyntaxError(syntaxError, delegate, con);
            throw syntaxError;
        } catch (java.sql.SQLException sqlException) {
            IncorrectStringSQLException incorrectStringError = IncorrectStringSQLException.instanceFor(sqlException);
            if (null != incorrectStringError) {
                throw incorrectStringError;
            }
            logReadTimeoutError(sqlException, delegate, con);
            throw sqlException;
        }
    }

    @Override
    public int[] executeBatch() throws SQLException {
        try {
            int[] retval =  delegate.executeBatch();
            con.updatePerformed();
            return retval;
        } catch (java.sql.SQLSyntaxErrorException syntaxError) {
            logSyntaxError(syntaxError, delegate, con);
            throw syntaxError;
        } catch (java.sql.SQLException sqlException) {
            IncorrectStringSQLException incorrectStringError = IncorrectStringSQLException.instanceFor(sqlException);
            if (null != incorrectStringError) {
                throw incorrectStringError;
            }
            logReadTimeoutError(sqlException, delegate, con);
            throw sqlException;
        }
    }

    @Override
    public ResultSet executeQuery(final String sql) throws SQLException {
        try {
            return new JDBC41ResultSetWrapper(delegate.executeQuery(sql), this);
        } catch (java.sql.SQLSyntaxErrorException syntaxError) {
            logSyntaxError(syntaxError, delegate, con);
            throw syntaxError;
        } catch (java.sql.SQLException sqlException) {
            IncorrectStringSQLException incorrectStringError = IncorrectStringSQLException.instanceFor(sqlException);
            if (null != incorrectStringError) {
                throw incorrectStringError;
            }
            logReadTimeoutError(sqlException, delegate, con);
            throw sqlException;
        }
    }

    @Override
    public int executeUpdate(final String sql) throws SQLException {
        try {
            int retval = delegate.executeUpdate(sql);
            con.updatePerformed();
            return retval;
        } catch (java.sql.SQLSyntaxErrorException syntaxError) {
            logSyntaxError(syntaxError, delegate, con);
            throw syntaxError;
        } catch (java.sql.SQLException sqlException) {
            IncorrectStringSQLException incorrectStringError = IncorrectStringSQLException.instanceFor(sqlException);
            if (null != incorrectStringError) {
                throw incorrectStringError;
            }
            logReadTimeoutError(sqlException, delegate, con);
            throw sqlException;
        }
    }

    @Override
    public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        try {
            int retval = delegate.executeUpdate(sql, autoGeneratedKeys);
            con.updatePerformed();
            return retval;
        } catch (java.sql.SQLSyntaxErrorException syntaxError) {
            logSyntaxError(syntaxError, delegate, con);
            throw syntaxError;
        } catch (java.sql.SQLException sqlException) {
            IncorrectStringSQLException incorrectStringError = IncorrectStringSQLException.instanceFor(sqlException);
            if (null != incorrectStringError) {
                throw incorrectStringError;
            }
            logReadTimeoutError(sqlException, delegate, con);
            throw sqlException;
        }
    }

    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        try {
            int retval = delegate.executeUpdate(sql, columnIndexes);
            con.updatePerformed();
            return retval;
        } catch (java.sql.SQLSyntaxErrorException syntaxError) {
            logSyntaxError(syntaxError, delegate, con);
            throw syntaxError;
        } catch (java.sql.SQLException sqlException) {
            IncorrectStringSQLException incorrectStringError = IncorrectStringSQLException.instanceFor(sqlException);
            if (null != incorrectStringError) {
                throw incorrectStringError;
            }
            logReadTimeoutError(sqlException, delegate, con);
            throw sqlException;
        }
    }

    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        try {
            int retval = delegate.executeUpdate(sql, columnNames);
            con.updatePerformed();
            return retval;
        } catch (java.sql.SQLSyntaxErrorException syntaxError) {
            logSyntaxError(syntaxError, delegate, con);
            throw syntaxError;
        } catch (java.sql.SQLException sqlException) {
            IncorrectStringSQLException incorrectStringError = IncorrectStringSQLException.instanceFor(sqlException);
            if (null != incorrectStringError) {
                throw incorrectStringError;
            }
            logReadTimeoutError(sqlException, delegate, con);
            throw sqlException;
        }
    }

    @Override
    public Connection getConnection() {
        return con;
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return delegate.getFetchDirection();
    }

    @Override
    public int getFetchSize() throws SQLException {
        return delegate.getFetchSize();
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return new JDBC41ResultSetWrapper(delegate.getGeneratedKeys(), this);
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return delegate.getMaxFieldSize();
    }

    @Override
    public int getMaxRows() throws SQLException {
        return delegate.getMaxRows();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return delegate.getMoreResults();
    }

    @Override
    public boolean getMoreResults(final int current) throws SQLException {
        return delegate.getMoreResults(current);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return delegate.getQueryTimeout();
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return new JDBC41ResultSetWrapper(delegate.getResultSet(), this);
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return delegate.getResultSetConcurrency();
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return delegate.getResultSetHoldability();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return delegate.getResultSetType();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return delegate.getUpdateCount();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    @Override
    public void setCursorName(final String name) throws SQLException {
        delegate.setCursorName(name);
    }

    @Override
    public void setEscapeProcessing(final boolean enable) throws SQLException {
        delegate.setEscapeProcessing(enable);
    }

    @Override
    public void setFetchDirection(final int direction) throws SQLException {
        delegate.setFetchDirection(direction);
    }

    @Override
    public void setFetchSize(final int rows) throws SQLException {
        delegate.setFetchSize(rows);
    }

    @Override
    public void setMaxFieldSize(final int max) throws SQLException {
        delegate.setMaxFieldSize(max);
    }

    @Override
    public void setMaxRows(final int max) throws SQLException {
        delegate.setMaxRows(max);
    }

    @Override
    public void setQueryTimeout(final int seconds) throws SQLException {
        delegate.setQueryTimeout(seconds);
    }

    @Override
    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return delegate.isPoolable();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        delegate.setPoolable(poolable);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isAssignableFrom(delegate.getClass());
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(delegate.getClass())) {
            return iface.cast(delegate);
        }
        throw new SQLException("Not a wrapper for: " + iface.getName());
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
