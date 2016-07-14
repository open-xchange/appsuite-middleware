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

package com.openexchange.tools.sql;

import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.database.Databases;
import com.openexchange.databaseold.Database;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.Strings;

/**
 * Utilities for database resource handling.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DBUtils {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DBUtils.class);

    public static final int IN_LIMIT = 1000;

    private DBUtils() {
        super();
    }

    /**
     * Gets the SQL statement from specified {@link Statement} instance.
     *
     * @param statement The statement
     * @return The extracted SQL string
     */
    public static String getStatementString(final Statement statement) {
        if (null == statement) {
            return null;
        }
        final String str = statement.toString();
        final int pos = str.indexOf(": ");
        return pos < 0 ? str : str.substring(pos + 2);
    }

    /**
     * Closes the ResultSet.
     *
     * @param result <code>null</code> or a ResultSet to close.
     */
    public static void closeSQLStuff(final ResultSet result) {
        if (result != null) {
            try {
                result.close();
            } catch (final SQLException e) {
                LOG.error("", e);
            }
        }
    }

    /**
     * Closes the {@link Statement}.
     *
     * @param stmt <code>null</code> or a {@link Statement} to close.
     */
    public static void closeSQLStuff(final Statement stmt) {
        if (null != stmt) {
            try {
                stmt.close();
            } catch (final SQLException e) {
                LOG.error("", e);
            }
        }
    }

    /**
     * Closes the ResultSet and the Statement.
     *
     * @param result <code>null</code> or a ResultSet to close.
     * @param stmt <code>null</code> or a Statement to close.
     */
    public static void closeSQLStuff(final ResultSet result, final Statement stmt) {
        closeSQLStuff(result);
        closeSQLStuff(stmt);
    }

    /**
     * <p>
     * Closes given <code>java.sql.ResultSet</code> and <code>java.sql.Statement</code> reference and puts back given
     * <code>java.sql.Connection</code> reference into pool. The flag <code>isReadCon</code> determines if connection instance is of type
     * readable or writeable.
     * </p>
     * <p>
     * <b>NOTE:</b> References are not set to <code>null</code>, so the caller has to ensure that these references are not going to be used
     * anymore.
     * </p>
     */
    public static void closeResources(final ResultSet rs, final Statement stmt, final Connection con, final boolean isReadCon, final Context ctx) {
        closeResources(rs, stmt, con, isReadCon, ctx.getContextId());
    }

    /**
     * <p>
     * Closes given <code>java.sql.ResultSet</code> and <code>java.sql.Statement</code> reference and puts back given
     * <code>java.sql.Connection</code> reference into pool. The flag <code>isReadCon</code> determines if connection instance is of type
     * readable or writeable.
     * </p>
     * <p>
     * <b>NOTE:</b> References are not set to <code>null</code>, so the caller has to ensure that these references are not going to be used
     * anymore.
     * </p>
     */
    public static void closeResources(final ResultSet rs, final Statement stmt, final Connection con, final boolean isReadCon, final int cid) {
        /*
         * Close ResultSet
         */
        if (rs != null) {
            try {
                rs.close();
            } catch (final SQLException e) {
                LOG.error("", e);
            }
        }
        /*
         * Close Statement
         */
        if (stmt != null) {
            try {
                stmt.close();
            } catch (final SQLException e) {
                LOG.error("", e);
            }
        }
        /*
         * Close connection
         */
        if (con != null) {
            Database.back(cid, !isReadCon, con);
        }
    }

    public static String getStatement(final Statement stmt) {
        return stmt == null ? "" : stmt.toString();
    }

    public static String getStatement(final PreparedStatement stmt, final String query) {
        if (stmt == null) {
            return query;
        }
        try {
            return stmt.toString();
        } catch (final Exception x) {
            return query;
        }
    }

    /**
     * Starts a transaction on the given connection. This implementation sets autocommit to false and even executes a START TRANSACTION
     * statement to ensure isolation levels for the current connection.
     *
     * @param con connection to start the transaction on.
     * @throws SQLException if starting the transaction fails.
     */
    public static void startTransaction(final Connection con) throws SQLException {
        Statement stmt = null;
        try {
            con.setAutoCommit(false);
            stmt = con.createStatement();
            stmt.execute("START TRANSACTION");
        } finally {
            closeSQLStuff(stmt);
        }
    }

    /**
     * Performs a roll-back for a started transaction.
     *
     * @param con The connection to roll back.
     */
    public static void rollback(final Connection con) {
        if (null == con) {
            return;
        }
        try {
            if (!con.isClosed()) {
                con.rollback();
            }
        } catch (final SQLException e) {
            LOG.error("", e);
        }
    }

    /**
     * Convenience method to set the autocommit of a connection to <code>true</code>.
     *
     * @param con connection that should go into autocommit mode.
     */
    public static void autocommit(final Connection con) {
        if (null == con) {
            return;
        }
        try {
            if (!con.isClosed()) {
                con.setAutoCommit(true);
            }
        } catch (final SQLException e) {
            LOG.error("", e);
        }
    }

    private static final Pattern PAT_TRUNCATED_IDS = Pattern.compile("([^']*')(\\S+)('[^']*)");

    /**
     * This method tries to parse the truncated fields out of the DataTruncation exception. This method has been implemented because mysql
     * doesn't return the column identifier of the truncated field through the getIndex() method of the DataTruncation exception. This
     * method uses the fact that the exception sent by the mysql server encapsulates the truncated fields into single quotes.
     *
     * @param e DataTruncation exception to parse.
     * @return a string array containing all truncated field from the exception.
     */
    public static String[] parseTruncatedFields(final DataTruncation trunc) {
        final Matcher matcher = PAT_TRUNCATED_IDS.matcher(trunc.getMessage());
        final List<String> retval = new ArrayList<String>();
        if (matcher.find()) {
            for (int i = 2; i < matcher.groupCount(); i++) {
                retval.add(matcher.group(i));
            }
        }
        return retval.toArray(new String[retval.size()]);
    }

    /**
     * Extends a SQL statement with enough ? characters in the last IN argument.
     *
     * @param sql SQL statement ending with "IN ("
     * @param length number of entries.
     * @param appendix An optional appendix
     * @return the ready to use SQL statement.
     */
    public static String getIN(final String sql, final int length) {
        return getIN(sql, length, null);
    }

    /**
     * Extends a SQL statement with enough ? characters in the last IN argument.
     *
     * @param sql SQL statement ending with "IN ("
     * @param length number of entries.
     * @param appendix An optional appendix
     * @return the ready to use SQL statement.
     */
    public static String getIN(final String sql, final int length, final String appendix) {
        if (length <= 0) {
            return sql;
        }
        final StringBuilder retval = new StringBuilder(sql);
        retval.append('?');
        for (int i = 1; i < length; i++) {
            retval.append(",?");
        }
        retval.append(')');
        if (null != appendix) {
            if (!appendix.startsWith(" ")) {
                retval.append(" ");
            }
            retval.append(appendix);
        }
        return retval.toString();
    }

    /**
     * This method determines the size of a database column. For strings it gives the maximum allowed characters and for number it returns
     * the precision.
     *
     * @param con read only database connection.
     * @param table name of the table.
     * @param column name of the column.
     * @return the size or <code>-1</code> if the column is not found.
     * @throws SQLException if some exception occurs reading from database.
     */
    public static int getColumnSize(final Connection con, final String table, final String column) throws SQLException {
        final DatabaseMetaData metas = con.getMetaData();
        ResultSet result = null;
        try {
            result = metas.getColumns(null, null, table, column);
            int retval = -1;
            if (result.next()) {
                retval = result.getInt("COLUMN_SIZE");
            }
            return retval;
        } finally {
            DBUtils.closeSQLStuff(result);
        }
    }

    /**
     * Filters a given list of table names. Returns only those that also exist
     *
     * @param con The connection to the database in which to check for the tables
     * @param tablesToCheck The list of table names to check for.
     * @return A set with all the tables that exist of those to be checked for
     * @throws SQLException If something goes wrong
     */
    public static Set<String> existingTables(final Connection con, final String... tablesToCheck) throws SQLException {
        final Set<String> tables = new HashSet<String>();
        for (final String table : tablesToCheck) {
            if (tableExists(con, table)) {
                tables.add(table);
            }
        }
        return tables;
    }

    /**
     * Finds out whether all tables listed exist in the given database
     *
     * @param con The connection to the database in which to check for the tables
     * @param tablesToCheck The list of table names to check for.
     * @return A set with all the tables that exist of those to be checked for
     * @throws SQLException If something goes wrong
     */
    public static boolean tablesExist(final Connection con, final String... tablesToCheck) throws SQLException {
        for (final String table : tablesToCheck) {
            if (!tableExists(con, table)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds out whether a table listed exist in the given database
     *
     * @param con The connection to the database in which to check for the tables
     * @param table The table name to check for.
     * @return A set with all the tables that exist of those to be checked for
     * @throws SQLException If something goes wrong
     */
    public static boolean tableExists(final Connection con, final String table) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        boolean retval = false;
        try {
            rs = metaData.getTables(null, null, table, new String[] { "TABLE" });
            retval = (rs.next() && rs.getString("TABLE_NAME").equals(table));
        } finally {
            closeSQLStuff(rs);
        }
        return retval;
    }

    /**
     * Finds out whether a table listed exist in the given database
     *
     * @param con The connection to the database in which to check for the tables
     * @param table The table name to check for.
     * @return A set with all the tables that exist of those to be checked for
     * @throws SQLException If something goes wrong
     */
    public static boolean procedureExists(final Connection con, final String procedure) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        boolean retval = false;
        try {
            rs = metaData.getProcedures(null, null, procedure);
            retval = (rs.next() && rs.getString("PROCEDURE_NAME").equals(procedure));
        } finally {
            closeSQLStuff(rs);
        }
        return retval;
    }

    public static String forSQLCommand(final Order order) {
        if (order != null) {
            switch (order) {
                case ASCENDING:
                    return " ASC ";
                case DESCENDING:
                    return " DESC ";
                case NO_ORDER:
                    return " ";
            }
        }
        return " ";
    }

    /**
     * Disable MySQL foreign key checks on this connection.<br>
     * For certain cases, you must disable foreign key checks before truncate or delete all table data. Remember that you must use this
     * connection to perform the delete all or the truncate.
     *
     * @param connection {@link Connection}.
     * @throws SQLException If any error occurs during disable.
     * @see #enableMysqlForeignKeyChecks(java.sql.Connection)
     * @see #cleanInsert(String, java.sql.Connection)
     * @see #truncate(String, java.sql.Connection)
     * @see #truncateAndInsert(String, java.sql.Connection)
     */
    public static void disableMysqlForeignKeyChecks(final Connection connection) throws SQLException {
        setMysqlForeignKeyChecks(connection, 0);
    }

    /**
     * Enable MySQL foreign key checks on this connection.<br>
     *
     * @param connection {@link Connection}.
     * @throws SQLException If any error occurs during enable.
     */
    public static void enableMysqlForeignKeyChecks(final Connection connection) throws SQLException {
        setMysqlForeignKeyChecks(connection, 1);
    }

    private static void setMysqlForeignKeyChecks(final Connection connection, final int value) throws SQLException {
        final Statement statement = connection.createStatement();
        statement.execute("SET @@foreign_key_checks = " + value);
        statement.close();
    }

    /**
     * Checks if given {@link SQLException} instance denotes an integrity constraint violation due to a PRIMARY KEY conflict.
     *
     * @param e The <code>SQLException</code> instance to check
     * @return <code>true</code> if given {@link SQLException} instance denotes a PRIMARY KEY conflict; otherwise <code>false</code>
     */
    public static boolean isPrimaryKeyConflictInMySQL(SQLException e) {
        return Databases.isPrimaryKeyConflictInMySQL(e);
    }

    /**
     * Checks if given {@link SQLException} instance denotes an integrity constraint violation due to a conflict caused by the specified key.
     *
     * @param e The <code>SQLException</code> instance to check
     * @param keyName The name of the key causing the integrity constraint violation
     * @return <code>true</code> if given {@link SQLException} instance denotes a conflict caused by the specified ke; otherwise <code>false</code>
     */
    public static boolean isKeyConflictInMySQL(SQLException e, String keyName) {
        return Databases.isKeyConflictInMySQL(e, keyName);
    }

    /**
     * Checks if passed <tt>SQLException</tt> (or any of chained <tt>SQLException</tt>s) indicates a failed transaction roll-back.
     *
     * <pre>
     * Deadlock found when trying to get lock; try restarting transaction
     * </pre>
     *
     * @param sqlException The SQL exception to check
     * @return <code>true</code> if a failed transaction roll-back is indicated; otherwise <code>false</code>
     */
    public static boolean isTransactionRollbackException(final SQLException sqlException) {
        if (null == sqlException) {
            return false;
        }
        if (suggestsRestartingTransaction(sqlException) || sqlException.getClass().getName().endsWith("TransactionRollbackException")) {
            return true;
        }
        if (isTransactionRollbackException(sqlException.getNextException())) {
            return true;
        }
        final Throwable cause = sqlException.getCause();
        if (null == cause || !(cause instanceof Exception)) {
            return false;
        }
        return isTransactionRollbackException((Exception) cause);
    }

    /**
     * Checks if passed <tt>SQLException</tt> (or any of chained <tt>SQLException</tt>s) indicates a failed transaction roll-back.
     *
     * <pre>
     * Deadlock found when trying to get lock; try restarting transaction
     * </pre>
     *
     * @param exception The exception to check
     * @return <code>true</code> if a failed transaction roll-back is indicated; otherwise <code>false</code>
     */
    public static boolean isTransactionRollbackException(final Exception exception) {
        if (null == exception) {
            return false;
        }
        if (exception instanceof SQLException) {
            return isTransactionRollbackException((SQLException) exception);
        }
        final Throwable cause = exception.getCause();
        if (null == cause || !(cause instanceof Exception)) {
            return false;
        }
        return isTransactionRollbackException((Exception) cause);
    }

    /**
     * Checks if specified SQL exception's detail message contains a suggestion to restart the transaction;<br>
     * e.g. <code>"Lock wait timeout exceeded; try restarting transaction"</code>
     *
     * @param sqlException The SQL exception to check
     * @return <code>true</code> if SQL exception suggests restarting transaction; otherwise <code>false</code>
     */
    public static boolean suggestsRestartingTransaction(SQLException sqlException) {
        String message = null == sqlException ? null : sqlException.getMessage();
        return null != message && Strings.asciiLowerCase(message).indexOf("try restarting transaction") >= 0;
    }

    /**
     * Extracts possibly nested <tt>SQLException</tt> reference.
     *
     * @param exception The parental exception to extract from
     * @return The <tt>SQLException</tt> reference or <code>null</code>
     */
    public static SQLException extractSqlException(final Exception exception) {
        if (null == exception) {
            return null;
        }
        if (exception instanceof SQLException) {
            return (SQLException) exception;
        }
        final Throwable cause = exception.getCause();
        if (null == cause || !(cause instanceof Exception)) {
            return null;
        }
        return extractSqlException((Exception) cause);
    }

    /**
     * Checks for retry condition for a failed transaction roll-back.
     */
    public static final class TransactionRollbackCondition {

        private final int max;

        private int count;

        private SQLException transactionRollbackException;

        /**
         * Initializes a new {@link TransactionRollbackCondition}.
         *
         * @param max The max. retry count
         */
        public TransactionRollbackCondition(final int max) {
            super();
            count = 0;
            this.max = max;
        }

        /**
         * Check for a failed transaction roll-back.
         *
         * @param e The SQL exception to check for a failed transaction roll-back
         * @return <code>true</code> a failed transaction roll-back; otherwise <code>false</code>
         */
        public boolean isFailedTransactionRollback(final SQLException e) {
            if (isTransactionRollbackException(e)) {
                transactionRollbackException = e;
                return true;
            }
            return false;
        }

        /**
         * Check for a failed transaction roll-back.
         *
         * @param e The exception to check for a failed transaction roll-back
         * @return <code>true</code> a failed transaction roll-back; otherwise <code>false</code>
         */
        public boolean isFailedTransactionRollback(final Exception e) {
            final SQLException sqle = extractSqlException(e);
            if (null != sqle && isTransactionRollbackException(sqle)) {
                transactionRollbackException = sqle;
                return true;
            }
            return false;
        }

        /**
         * Gets the recently checked <tt>SQLException</tt> reference that indicates a failed transaction roll-back.
         *
         * @return The recently checked <tt>SQLException</tt> reference
         */
        public SQLException getTransactionRollbackException() {
            return transactionRollbackException;
        }

        /**
         * Resets the reference that indicates a failed transaction roll-back.
         */
        public void resetTransactionRollbackException() {
            transactionRollbackException = null;
        }

        /**
         * Check for retry condition.
         * <p>
         * <b>Note</b>: {@link #isFailedTransactionRollback(SQLException)} is expected to be called prior to invoking this method.
         * <p>
         * If check returns <code>true</code>, <tt>SQLException</tt> reference is set to <code>null</code>.
         *
         * @return <code>true</code> if retry condition is met; otherwise <code>false</code>
         * @throws SQLException If retry-count is exceeded and previously checked <tt>SQLException</tt> indicated a failed transaction roll-back
         */
        public boolean checkRetry() throws SQLException {
            if (null == transactionRollbackException) {
                return false;
            }
            if (++count <= max) {
                transactionRollbackException = null;
                return true;
            }
            throw transactionRollbackException;
        }
    }

}
