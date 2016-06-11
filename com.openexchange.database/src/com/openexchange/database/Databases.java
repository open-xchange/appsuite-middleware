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

package com.openexchange.database;

import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for database resource handling.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Databases {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Databases.class);

    /** The default limit for SQL-IN expressions */
    public static final int IN_LIMIT = 1000;

    private Databases() {
        super();
    }

    /**
     * Closes the given instances.
     *
     * @param closeables The instances to close.
     */
    public static void closeSQLStuff(AutoCloseable... closeables) {
        if (closeables != null) {
            for (AutoCloseable closeable : closeables) {
                closeSQLStuff(closeable);
            }
        }
    }

    /**
     * Closes the instance.
     *
     * @param closeable <code>null</code> or a {@link AutoCloseable} to close.
     */
    public static void closeSQLStuff(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                LOG.error("", e);
            }
        }
    }

    /**
     * Closes the {@link ResultSet} instances.
     *
     * @param results The instances to close.
     */
    public static void closeSQLStuff(ResultSet... results) {
        if (results != null) {
            for (ResultSet result : results) {
                closeSQLStuff(result);
            }
        }
    }

    /**
     * Closes the {@link ResultSet} instance.
     *
     * @param result <code>null</code> or a {@link ResultSet} to close.
     */
    public static void closeSQLStuff(ResultSet result) {
        if (result != null) {
            try {
                result.close();
            } catch (SQLException e) {
                LOG.error("", e);
            }
        }
    }

    /**
     * Closes the {@link Statement} instances.
     *
     * @param stmts The statements to close.
     */
    public static void closeSQLStuff(Statement... stmts) {
        if (null != stmts) {
            for (Statement stmt : stmts) {
                closeSQLStuff(stmt);
            }
        }
    }

    /**
     * Closes the {@link Statement}.
     *
     * @param stmt <code>null</code> or a {@link Statement} to close.
     */
    public static void closeSQLStuff(Statement stmt) {
        if (null != stmt) {
            try {
                stmt.close();
            } catch (SQLException e) {
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
    public static void closeSQLStuff(ResultSet result, Statement stmt) {
        closeSQLStuff(result);
        closeSQLStuff(stmt);
    }

    /**
     * Gets the <code>toString()</code> representation for given <code>Statement</code> instance.
     *
     * @param stmt The statement
     * @return The <code>toString()</code> representation or an empty string if <code>null</code>
     */
    public static String getStatement(Statement stmt) {
        return stmt == null ? "" : stmt.toString();
    }

    /**
     * Gets the SQL statement from given <code>PreparedStatement</code> instance.
     *
     * @param stmt The <code>PreparedStatement</code> instance
     * @param query The optional query to return
     * @return The SQL statement
     */
    public static String getStatement(PreparedStatement stmt, String query) {
        if (stmt == null) {
            return query;
        }
        try {
            return stmt.toString();
        } catch (Exception x) {
            return query;
        }
    }

    /**
     * Gets the SQL statement from given <code>PreparedStatement</code> instance.
     *
     * @param stmt The <code>PreparedStatement</code> instance
     * @param query The optional query associated with given <code>PreparedStatement</code> instance
     * @return The SQL statement
     */
    public static String getSqlStatement(Statement stmt, String query) {
        if (stmt == null) {
            return query;
        }
        try {
            String sql = stmt.toString();
            int pos = sql.indexOf(": ");
            return pos < 0 ? sql : sql.substring(pos + 2);
        } catch (Exception x) {
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
    public static void startTransaction(Connection con) throws SQLException {
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
     * Rolls a transaction of a connection back.
     *
     * @param con connection to roll back.
     */
    public static void rollback(Connection con) {
        if (null == con) {
            return;
        }
        try {
            if (!con.isClosed()) {
                con.rollback();
            }
        } catch (SQLException e) {
            LOG.error("", e);
        }
    }

    /**
     * Convenience method to set the autocommit of a connection to <code>true</code>.
     *
     * @param con connection that should go into autocommit mode.
     */
    public static void autocommit(Connection con) {
        if (null == con) {
            return;
        }
        try {
            if (!con.isClosed()) {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
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
    public static String[] parseTruncatedFields(DataTruncation trunc) {
        Matcher matcher = PAT_TRUNCATED_IDS.matcher(trunc.getMessage());
        List<String> retval = new ArrayList<String>();
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
     * @param sql SQL statement ending with "IN (";
     * @param length number of entries.
     * @return the ready to use SQL statement.
     */
    public static String getIN(String sql, int length) {
        StringBuilder retval = new StringBuilder(sql);
        for (int i = 0; i < length; i++) {
            retval.append("?,");
        }
        retval.setCharAt(retval.length() - 1, ')');
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
    public static int getColumnSize(Connection con, String table, String column) throws SQLException {
        DatabaseMetaData metas = con.getMetaData();
        int retval = -1;
        try (ResultSet result = metas.getColumns(null, null, table, column)) {
            if (result.next()) {
                retval = result.getInt("COLUMN_SIZE");
            }
        }
        return retval;
    }

    /**
     * Filters a given list of tablenames. Returns only those that also exist
     *
     * @param con The connection to the database in which to check for the tables
     * @param tablesToCheck The list of table names to check for.
     * @return A set with all the tables that exist of those to be checked for
     * @throws SQLException If something goes wrong
     */
    public static Set<String> existingTables(Connection con, String... tablesToCheck) throws SQLException {
        Set<String> tables = new HashSet<String>();
        for (String table : tablesToCheck) {
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
    public static boolean tablesExist(Connection con, String... tablesToCheck) throws SQLException {
        for (String table : tablesToCheck) {
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
    public static boolean tableExists(Connection con, String table) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
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

    private static final Pattern DUPLICATE_KEY = Pattern.compile("Duplicate entry '([^']+)' for key '([^']+)'");

    /**
     * Checks if given {@link SQLException} instance denotes an integrity constraint violation due to a PRIMARY KEY conflict.
     *
     * @param e The <code>SQLException</code> instance to check
     * @return <code>true</code> if given {@link SQLException} instance denotes a PRIMARY KEY conflict; otherwise <code>false</code>
     */
    public static boolean isPrimaryKeyConflictInMySQL(SQLException e) {
        return isKeyConflictInMySQL(e, "PRIMARY");
    }

    /**
     * Checks if given {@link SQLException} instance denotes an integrity constraint violation due to a conflict caused by the specified key.
     *
     * @param e The <code>SQLException</code> instance to check
     * @param keyName The name of the key causing the integrity constraint violation
     * @return <code>true</code> if given {@link SQLException} instance denotes a conflict caused by the specified ke; otherwise <code>false</code>
     */
    public static boolean isKeyConflictInMySQL(SQLException e, String keyName) {
        if (null == e || null == keyName) {
            return false;
        }

        /*
         * SQLState 23000: Integrity Constraint Violation
         * Error: 1586 SQLSTATE: 23000 (ER_DUP_ENTRY_WITH_KEY_NAME)
         * Error: 1062 SQLSTATE: 23000 (ER_DUP_ENTRY)
         * com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException: Duplicate entry 'some-data' for key 'key-name'
         * Message: Duplicate entry '%s' for key '%s'
         */
        if ("23000".equals(e.getSQLState())) {
            int errorCode = e.getErrorCode();
            if (1062 == errorCode || 1586 == errorCode) {
                Matcher matcher = DUPLICATE_KEY.matcher(e.getMessage());
                if (matcher.matches() && keyName.equals(matcher.group(2))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Code is correct and will not leave a connection in CLOSED_WAIT state. See CloseWaitTest.java.
     */
    public static void close(Connection con) {
        if (null == con) {
            return;
        }
        try {
            if (!con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Rolls specified connection back to given save-point.
     *
     * @param con The connection to roll-back
     * @param savePoint The save-point to restore to
     */
    public static void rollback(Connection con, Savepoint savePoint) {
        if (null == con || null == savePoint) {
            return;
        }
        try {
            if (!con.isClosed()) {
                con.rollback(savePoint);
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Checks if specified column exists.
     *
     * @param con The connection
     * @param table The table name
     * @param column The column name
     * @return <code>true</code> if specified column exists; otherwise <code>false</code>
     * @throws SQLException If an SQL error occurs
     */
    public static boolean columnExists(final Connection con, final String table, final String column) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        boolean retval = false;
        try {
            rs = metaData.getColumns(null, null, table, column);
            while (rs.next()) {
                retval = rs.getString(4).equalsIgnoreCase(column);
            }
        } finally {
            closeSQLStuff(rs);
        }
        return retval;
    }

}
