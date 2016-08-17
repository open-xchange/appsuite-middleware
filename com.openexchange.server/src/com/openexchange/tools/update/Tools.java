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

package com.openexchange.tools.update;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.java.Strings;

/**
 * This class contains some tools to ease update of database.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Tools {

    /**
     * Prevent instantiation
     */
    private Tools() {
        super();
    }

    /**
     * Checks if specified column is nullable (<code>NULL</code> is allowed).
     *
     * @param con The connection to use
     * @param table The table
     * @param column The column
     * @return <code>true</code> if nullable; otherwise <code>false</code>
     * @throws SQLException If column cannot be checked if nullable
     */
    public static final boolean isNullable(final Connection con, final String table, final String column) throws SQLException {
        DatabaseMetaData meta = con.getMetaData();
        ResultSet result = null;
        try {
            result = meta.getColumns(null, null, table, column);
            if (!result.next()) {
                throw new SQLException("Can't get information for column " + column + " in table " + table + '.');
            }

            return DatabaseMetaData.typeNullable == result.getInt(NULLABLE);
        } finally {
            closeSQLStuff(result);
        }
    }

    /**
     * Checks if denoted table has its PRIMARY KEY set to specified columns.
     *
     * @param con The connection to use
     * @param table The tanle to check
     * @param columns The expected PRIMARY KEY
     * @return <code>true</code> it denoted table has such a PRIMARY KEY; otherwise <code>false</code>
     * @throws SQLException If PRIMARY KEY check fails
     */
    public static final boolean existsPrimaryKey(final Connection con, final String table, final String[] columns) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        final List<String> foundColumns = new ArrayList<String>();
        ResultSet result = null;
        try {
            result = metaData.getPrimaryKeys(null, null, table);
            while (result.next()) {
                final String columnName = result.getString(4);
                final int columnPos = result.getInt(5);
                while (foundColumns.size() < columnPos) {
                    foundColumns.add(null);
                }
                foundColumns.set(columnPos - 1, columnName);
            }
        } finally {
            closeSQLStuff(result);
        }
        boolean matches = columns.length == foundColumns.size();
        for (int i = 0; matches && i < columns.length; i++) {
            matches = columns[i].equalsIgnoreCase(foundColumns.get(i));
        }
        return matches;
    }

    /**
     * Lists the names of available indexes for specified table.
     *
     * @param con The connection to use
     * @param table The table name
     * @return The names of available indexes
     * @throws SQLException If listing the names of available indexes fails
     */
    public static final String[] listIndexes(Connection con, String table) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        ResultSet result = null;
        try {
            result = metaData.getIndexInfo(null, null, table, false, false);
            Set<String> names = new LinkedHashSet<String>();
            while (result.next()) {
                String indexName = result.getString(6);
                names.add(indexName);
            }
            return names.toArray(new String[names.size()]);
        } finally {
            closeSQLStuff(result);
        }
    }

    /**
     * @param con readable database connection.
     * @param table table name that indexes should be tested.
     * @param columns column names that the index must cover.
     * @return the name of an index that matches the given columns or <code>null</code> if no matching index is found.
     * @throws SQLException if some SQL problem occurs.
     * @throws NullPointerException if one of the columns is <code>null</code>.
     */
    public static final String existsIndex(final Connection con, final String table, final String[] columns) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        final Map<String, ArrayList<String>> indexes = new HashMap<String, ArrayList<String>>();
        ResultSet result = null;
        try {
            result = metaData.getIndexInfo(null, null, table, false, false);
            while (result.next()) {
                final String indexName = result.getString(6);
                final int columnPos = result.getInt(8);
                final String columnName = result.getString(9);
                ArrayList<String> foundColumns = indexes.get(indexName);
                if (null == foundColumns) {
                    foundColumns = new ArrayList<String>();
                    indexes.put(indexName, foundColumns);
                }
                while (foundColumns.size() < columnPos) {
                    foundColumns.add(null);
                }
                foundColumns.set(columnPos - 1, columnName);
            }
        } finally {
            closeSQLStuff(result);
        }
        String foundIndex = null;
        final Iterator<Entry<String, ArrayList<String>>> iter = indexes.entrySet().iterator();
        while (null == foundIndex && iter.hasNext()) {
            final Entry<String, ArrayList<String>> entry = iter.next();
            final ArrayList<String> foundColumns = entry.getValue();
            if (columns.length != foundColumns.size()) {
                continue;
            }
            boolean matches = true;
            for (int i = 0; matches && i < columns.length; i++) {
                matches = columns[i].equalsIgnoreCase(foundColumns.get(i));
            }
            if (matches) {
                foundIndex = entry.getKey();
            }
        }
        return foundIndex;
    }

    public static final String existsForeignKey(final Connection con, final String primaryTable, final String[] primaryColumns, final String foreignTable, final String[] foreignColumns) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        final Set<ForeignKey> keys = new HashSet<ForeignKey>();
        ResultSet result = null;
        try {
            result = metaData.getImportedKeys(null, null, foreignTable);
            ForeignKey key = null;
            while (result.next()) {
                final String foundPrimaryTable = result.getString("PKTABLE_NAME");
                final String foundForeignTable = result.getString("FKTABLE_NAME");
                final String keyName = result.getString("FK_NAME");
                final ForeignKey tmp = new ForeignKey(keyName, foundPrimaryTable, foundForeignTable);
                if (null == key || !key.isSame(tmp)) {
                    key = tmp;
                    keys.add(key);
                }
                final String primaryColumn = result.getString("PKCOLUMN_NAME");
                final String foreignColumn = result.getString("FKCOLUMN_NAME");
                final int columnPos = result.getInt("KEY_SEQ");
                key.setPrimaryColumn(columnPos - 1, primaryColumn);
                key.setForeignColumn(columnPos - 1, foreignColumn);
            }
        } finally {
            closeSQLStuff(result);
        }
        for (final ForeignKey key : keys) {
            if (key.getPrimaryTable().equalsIgnoreCase(primaryTable) && key.getForeignTable().equalsIgnoreCase(foreignTable) && key.matches(primaryColumns, foreignColumns)) {
                return key.getName();
            }
        }
        return null;
    }

    public static final List<String> allForeignKey(final Connection con, final String foreignTable) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet result = null;
        try {
            result = metaData.getImportedKeys(null, null, foreignTable);
            final Set<String> set = new HashSet<String>();
            while (result.next()) {
                final String keyName = result.getString("FK_NAME");
                if (null != keyName) {
                    set.add(keyName);
                }
            }
            return new ArrayList<String>(set);
        } finally {
            closeSQLStuff(result);
        }
    }

    /**
     * This method drops the primary key on the table. Beware, this method is vulnerable to SQL injection because table and index name can
     * not be set through a {@link PreparedStatement}.
     *
     * @param con writable database connection.
     * @param table table name that primary key should be dropped.
     * @throws SQLExceptionif some SQL problem occurs.
     */
    public static final void dropPrimaryKey(final Connection con, final String table) throws SQLException {
        final String sql = "ALTER TABLE `" + table + "` DROP PRIMARY KEY";
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(sql);
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * This method drops an index with the given name. Beware, this method is vulnerable to SQL injection because table and index name can
     * not be set through a {@link PreparedStatement}.
     *
     * @param con writable database connection.
     * @param table table name that index should be dropped.
     * @param index name of the index to drop.
     * @throws SQLException if some SQL problem occurs.
     */
    public static final void dropIndex(final Connection con, final String table, final String index) throws SQLException {
        final String sql = "ALTER TABLE `" + table + "` DROP INDEX `" + index + "`";
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(sql);
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    public static final void dropForeignKey(final Connection con, final String table, final String foreignKey) throws SQLException {
        final String sql = "ALTER TABLE `" + table + "` DROP FOREIGN KEY `" + foreignKey + "`";
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(sql);
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * This method creates a new primary key on a table. Beware, this method is vulnerable to SQL injection because table and column names
     * can not be set through a {@link PreparedStatement}.
     *
     * @param con writable database connection.
     * @param table name of the table that should get a new primary key.
     * @param columns names of the columns the primary key should cover.
     * @param lengths The column lengths; <code>-1</code> for full column
     * @throws SQLException if some SQL problem occurs.
     */
    public static final void createPrimaryKey(final Connection con, final String table, final String[] columns, final int[] lengths) throws SQLException {
        final StringBuilder sql = new StringBuilder("ALTER TABLE `");
        sql.append(table);
        sql.append("` ADD PRIMARY KEY (");
        {
            final String column = columns[0];
            sql.append('`').append(column).append('`');
            final int len = lengths[0];
            if (len > 0) {
                sql.append('(').append(len).append(')');
            }
        }
        for (int i = 1; i < columns.length; i++) {
            final String column = columns[i];
            sql.append(',');
            sql.append('`').append(column).append('`');
            final int len = lengths[i];
            if (len > 0) {
                sql.append('(').append(len).append(')');
            }
        }
        sql.append(')');
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(sql.toString());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * This method creates a new primary key on a table. Beware, this method is vulnerable to SQL injection because table and column names
     * can not be set through a {@link PreparedStatement}.
     *
     * @param con writable database connection.
     * @param table name of the table that should get a new primary key.
     * @param columns names of the columns the primary key should cover.
     * @throws SQLException if some SQL problem occurs.
     */
    public static final void createPrimaryKeyIfAbsent(final Connection con, final String table, final String[] columns) throws SQLException {
        if (!existsPrimaryKey(con, table, columns)) {
            if (hasPrimaryKey(con, table)) {
                dropPrimaryKey(con, table);
            }
            createPrimaryKey(con, table, columns);
        }
    }

    /**
     * This method creates a new primary key on a table. Beware, this method is vulnerable to SQL injection because table and column names
     * can not be set through a {@link PreparedStatement}.
     *
     * @param con writable database connection.
     * @param table name of the table that should get a new primary key.
     * @param columns names of the columns the primary key should cover.
     * @throws SQLException if some SQL problem occurs.
     */
    public static final void createPrimaryKey(final Connection con, final String table, final String[] columns) throws SQLException {
        final int[] lengths = new int[columns.length];
        Arrays.fill(lengths, -1);
        createPrimaryKey(con, table, columns, lengths);
    }

    /**
     * This method creates a new index on a table. Beware, this method is vulnerable to SQL injection because table and column names can not
     * be set through a {@link PreparedStatement}.
     *
     * @param con writable database connection.
     * @param table name of the table that should get a new index.
     * @param name name of the index or <code>null</code> to let the database define the name.
     * @param columns names of the columns the index should cover.
     * @param unique if this should be a unique index.
     * @throws SQLException if some SQL problem occurs.
     */
    public static final void createIndex(final Connection con, final String table, final String name, final String[] columns, final boolean unique) throws SQLException {
        final StringBuilder sql = new StringBuilder("ALTER TABLE `");
        sql.append(table);
        sql.append("` ADD ");
        if (unique) {
            sql.append("UNIQUE ");
        }
        sql.append("INDEX ");
        if (null != name) {
            sql.append('`');
            sql.append(name);
            sql.append("` ");
        }
        sql.append("(");
        for (final String column : columns) {
            if (column.startsWith("`")) {
                sql.append(column);
                sql.append(",");
            } else {
                sql.append("`");
                sql.append(column);
                sql.append("`,");
            }
        }
        sql.setLength(sql.length() - 1);
        sql.append(')');
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(sql.toString());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * This method creates a new index on a table. Beware, this method is vulnerable to SQL injection because table and column names can not
     * be set through a {@link PreparedStatement}.
     *
     * @param con writable database connection.
     * @param table name of the table that should get a new index.
     * @param columns names of the columns the index should cover.
     * @throws SQLException if some SQL problem occurs.
     */
    public static final void createIndex(final Connection con, final String table, final String[] columns) throws SQLException {
        createIndex(con, table, null, columns, false);
    }

    public static void createForeignKey(final Connection con, final String table, final String[] columns, final String referencedTable, final String[] referencedColumns) throws SQLException {
        createForeignKey(con, null, table, columns, referencedTable, referencedColumns);
    }

    public static void createForeignKey(final Connection con, final String name, final String table, final String[] columns, final String referencedTable, final String[] referencedColumns) throws SQLException {
        final StringBuilder sql = new StringBuilder("ALTER TABLE `");
        sql.append(table);
        sql.append("` ADD FOREIGN KEY ");
        if (null != name) {
            sql.append('`');
            sql.append(name);
            sql.append("` ");
        }
        sql.append("(`");
        for (final String column : columns) {
            sql.append(column);
            sql.append("`,`");
        }
        sql.setLength(sql.length() - 2);
        sql.append(") REFERENCES `");
        sql.append(referencedTable);
        sql.append("`(`");
        for (final String column : referencedColumns) {
            sql.append(column);
            sql.append("`,`");
        }
        sql.setLength(sql.length() - 2);
        sql.append(')');
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(sql.toString());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * Checks if denoted table has any primary key set.
     *
     * @param con The connection
     * @param table The table name
     * @return <code>true</code> if denoted table has any primary key set; otherwise <code>false</code>
     * @throws SQLException If a SQL error occurs
     */
    public static final boolean hasPrimaryKey(final Connection con, final String table) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        // Get primary keys
        final ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, table);
        try {
            return primaryKeys.next();
        } finally {
            closeSQLStuff(primaryKeys);
        }
    }

    /**
     * Checks if denoted column in given table is of type {@link java.sql.Types#VARCHAR}.
     *
     * @param con The connection
     * @param table The table name
     * @param column The column name
     * @return <code>true</code> if denoted column in given table is of type {@link java.sql.Types#VARCHAR}; otherwise <code>false</code>
     * @throws SQLException If a SQL error occurs
     */
    public static final boolean isVARCHAR(final Connection con, final String table, final String column) throws SQLException {
        return isType(con, table, column, java.sql.Types.VARCHAR);
    }

    /**
     * Checks if denoted column in given table is of specified type from {@link java.sql.Types}.
     *
     * @param con The connection
     * @param table The table name
     * @param column The column name
     * @param type The type to check against
     * @return <code>true</code> if denoted column in given table is of specified type; otherwise <code>false</code>
     * @throws SQLException If a SQL error occurs
     */
    public static final boolean isType(final Connection con, final String table, final String column, final int type) throws SQLException {
        return type == getColumnType(con, table, column);
    }

    /**
     * Gets the type of specified column in given table from {@link java.sql.Types}.
     *
     * @param con The connection
     * @param table The table name
     * @param column The column name
     * @return The type of specified column in given table from {@link java.sql.Types} or <code>-1</code> if column does not exist
     * @throws SQLException If a SQL error occurs
     */
    public static final int getColumnType(final Connection con, final String table, final String column) throws SQLException {
        if (!columnExists(con, table, column)) {
            return -1;
        }
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        int type = -1;
        try {
            rs = metaData.getColumns(null, null, table, column);
            while (rs.next()) {
                type = rs.getInt(5);
            }
        } finally {
            closeSQLStuff(rs);
        }
        return type;
    }

    /**
     * Checks if specified column in given table has a default value set.
     *
     * @param con The connection
     * @param table The table name
     * @param column The column name
     * @return <code>true</code> if that column has a default value; otherwise <code>false</code>
     * @throws SQLException If a SQL error occurs
     */
    public static final boolean hasDefaultValue(final Connection con, final String table, final String column) throws SQLException {
        if (!columnExists(con, table, column)) {
            throw new SQLException("Column '" + column + "' does not exist in table '" + table + "'");
        }

        return null != getDefaultValue(con, table, column);
    }

    /**
     * Gets the default value (if any) for specified column in given table.
     *
     * @param con The connection
     * @param table The table name
     * @param column The column name
     * @return <code>true</code> if that column has a default value; otherwise <code>false</code>
     * @throws SQLException If a SQL error occurs
     */
    public static final String getDefaultValue(final Connection con, final String table, final String column) throws SQLException {
        if (!columnExists(con, table, column)) {
            throw new SQLException("Column '" + column + "' does not exist in table '" + table + "'");
        }
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        try {
            rs = metaData.getColumns(null, null, table, column);
            if (rs.next()) {
                return rs.getString("COLUMN_DEF");
            }
            return null;
        } finally {
            closeSQLStuff(rs);
        }
    }

    /**
     * Gets the type of the denoted column in given table.
     *
     * @param con The connection to use
     * @param table The table name
     * @param column The column name
     * @return The type or <code>null</code> if such a column does not exist
     * @throws SQLException If an SQL error occurs
     */
    public static final String getColumnTypeName(final Connection con, final String table, final String column) throws SQLException {
        if (!columnExists(con, table, column)) {
            return null;
        }
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        String typeName = null;
        try {
            rs = metaData.getColumns(null, null, table, column);
            while (rs.next()) {
                // TYPE_NAME String => Data source dependent type name, for a UDT the type name is fully qualified
                typeName = rs.getString(6);
            }
        } finally {
            closeSQLStuff(rs);
        }
        return typeName;
    }

    /**
     * Checks for existence of denoted table.
     *
     * @param con The connection to use
     * @param table The table to check
     * @return <code>true</code> if such a table exists; otherwise <code>false</code>
     * @throws SQLException If an SQL error occurs
     */
    public static final boolean tableExists(final Connection con, final String table) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        boolean retval = false;
        try {
            rs = metaData.getTables(null, null, table, new String[] { TABLE });
            retval = (rs.next() && rs.getString("TABLE_NAME").equalsIgnoreCase(table));
        } finally {
            closeSQLStuff(rs);
        }
        return retval;
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

    private static final int NULLABLE = 11;
    private static final String TABLE = "TABLE";

    public static boolean hasSequenceEntry(final String sequenceTable, final Connection con, final int ctxId) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT 1 FROM " + sequenceTable + " WHERE cid=?");
            stmt.setInt(1, ctxId);
            rs = stmt.executeQuery();
            return rs.next();
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    public static List<Integer> getContextIDs(final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final List<Integer> contextIds = new LinkedList<Integer>();
        try {
            stmt = con.prepareStatement("SELECT DISTINCT cid FROM user");
            rs = stmt.executeQuery();
            while (rs.next()) {
                contextIds.add(I(rs.getInt(1)));
            }
            return contextIds;
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    public static void exec(final Connection con, final String sql, final Object... args) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sql);
            int i = 1;
            for (final Object arg : args) {
                stmt.setObject(i++, arg);
            }
            stmt.execute();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    /**
     * Drops specified table.
     *
     * @param con The connection to use
     * @param tableName The table name
     * @throws SQLException If dropping columns fails
     */
    public static boolean dropTable(final Connection con, final String tableName) throws SQLException {
        if (Strings.isEmpty(tableName)) {
            return false;
        }

        final StringBuffer sql = new StringBuffer("DROP TABLE ").append(tableName);
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(sql.toString());
            return true;
        } finally {
            closeSQLStuff(stmt);
        }
    }

    public static void addColumns(final Connection con, final String tableName, final Column... cols) throws SQLException {
        final StringBuffer sql = new StringBuffer("ALTER TABLE ");
        sql.append(tableName);
        for (final Column column : cols) {
            sql.append(" ADD ");
            sql.append(column.getName());
            sql.append(' ');
            sql.append(column.getDefinition());
            sql.append(',');
        }
        if (sql.charAt(sql.length() - 1) == ',') {
            sql.setLength(sql.length() - 1);
        }
        if (sql.length() == 12 + tableName.length()) {
            return;
        }
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(sql.toString());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    /**
     * Drops specified columns from given table.
     *
     * @param con The connection to use
     * @param tableName The table name
     * @param cols The columns to drop
     * @throws SQLException If dropping columns fails
     */
    public static void dropColumns(final Connection con, final String tableName, final Column... cols) throws SQLException {
        if (null == cols || 0 == cols.length) {
            return;
        }

        final StringBuffer sql = new StringBuffer("ALTER TABLE ");
        sql.append(tableName);
        sql.append(" DROP ");
        sql.append(cols[0].getName());
        for (int i = 1; i < cols.length; i++) {
            sql.append(", DROP ");
            sql.append(cols[i].getName());
        }
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(sql.toString());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    /**
     * Checks absence of specified columns and adds them to table.
     *
     * @param con The connection to use
     * @param tableName The table name
     * @param cols The columns to add
     * @throws SQLException If operation fails
     */
    public static void checkAndAddColumns(final Connection con, final String tableName, final Column... cols) throws SQLException {
        final List<Column> notExisting = new LinkedList<Column>();
        for (final Column col : cols) {
            if (!columnExists(con, tableName, col.getName())) {
                notExisting.add(col);
            }
        }
        if (!notExisting.isEmpty()) {
            addColumns(con, tableName, notExisting.toArray(new Column[notExisting.size()]));
        }
    }

    /**
     * Checks existence of specified columns and drops them from table.
     *
     * @param con The connection to use
     * @param tableName The table name
     * @param cols The columns to drop
     * @throws SQLException If operation fails
     */
    public static void checkAndDropColumns(final Connection con, final String tableName, final Column... cols) throws SQLException {
        final List<Column> existing = new LinkedList<Column>();
        for (final Column col : cols) {
            if (columnExists(con, tableName, col.getName())) {
                existing.add(col);
            }
        }
        if (!existing.isEmpty()) {
            dropColumns(con, tableName, existing.toArray(new Column[existing.size()]));
        }
    }

    public static void modifyColumns(final Connection con, final String tableName, final Collection<Column> columns) throws SQLException {
        modifyColumns(con, tableName, columns.toArray(new Column[columns.size()]));
    }

    /**
     * Modifies specified columns in given table.
     *
     * @param con The connection to use
     * @param tableName The table name
     * @param cols The new column definitions to change to
     * @throws SQLException If operation fails
     */
    public static void modifyColumns(final Connection con, final String tableName, final Column... cols) throws SQLException {
        modifyColumns(con, tableName, false, cols);
    }

    /**
     * Modifies specified columns in given table.
     *
     * @param con The connection to use
     * @param tableName The table name
     * @param ignore adds the keyword IGNORE to the SQL statement to ignore e.g. data truncation.
     * @param cols The new column definitions to change to
     * @throws SQLException If operation fails
     */
    public static void modifyColumns(final Connection con, final String tableName, boolean ignore, final Column... cols) throws SQLException {
        if (null == cols || cols.length == 0) {
            return;
        }
        final StringBuilder sql = new StringBuilder();
        if (ignore) {
            sql.append("ALTER IGNORE TABLE ");
        } else {
            sql.append("ALTER TABLE ");
        }
        sql.append(tableName);
        sql.append(" MODIFY COLUMN ");
        sql.append(cols[0].getName());
        sql.append(' ');
        sql.append(cols[0].getDefinition());
        for (int i = 1; i < cols.length; i++) {
            sql.append(", MODIFY COLUMN ");
            sql.append(cols[i].getName());
            sql.append(' ');
            sql.append(cols[i].getDefinition());
        }
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(sql.toString());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    /**
     * Checks existence of specified columns and modifies them in table.
     *
     * @param con The connection to use
     * @param tableName The table name
     * @param cols The columns to modify
     * @throws SQLException If operation fails
     */
    public static void checkAndModifyColumns(final Connection con, final String tableName, final Column... cols) throws SQLException {
        checkAndModifyColumns(con, tableName, false, cols);
    }

    /**
     * Checks existence of specified columns and modifies them in table.
     *
     * @param con The connection to use
     * @param tableName The table name
     * @param ignore adds the keyword IGNORE to the SQL statement to ignore e.g. data truncation.
     * @param cols The columns to modify
     * @throws SQLException If operation fails
     */
    public static void checkAndModifyColumns(final Connection con, final String tableName, boolean ignore, final Column... cols) throws SQLException {
        final List<Column> toDo = new ArrayList<Column>(cols.length);
        for (final Column col : cols) {
            String columnTypeName = getColumnTypeName(con, tableName, col.getName());
            if ((null != columnTypeName) && (false == col.getDefinition().contains(columnTypeName))) {
                toDo.add(col);
            }
        }
        if (!toDo.isEmpty()) {
            modifyColumns(con, tableName, ignore, toDo.toArray(new Column[toDo.size()]));
        }
    }

    /**
     * Changes the column to a new size
     *
     * @param colName The column to enlarge
     * @param newSize The new size to set the column to
     * @param tableName The table name
     * @param con The connection to use
     * @throws OXException
     */
    public static void changeVarcharColumnSize(final String colName, final int newSize, final String tableName, final Connection con) throws OXException {
        ResultSet rsColumns = null;
        boolean doAlterTable = false;
        try {
            DatabaseMetaData meta = con.getMetaData();
            rsColumns = meta.getColumns(null, null, tableName, null);
            while (rsColumns.next()) {
                final String columnName = rsColumns.getString("COLUMN_NAME");
                if (colName.equals(columnName)) {
                    doAlterTable = true;
                    break;
                }
            }
            Databases.closeSQLStuff(rsColumns);
            rsColumns = null;

            if (doAlterTable) {
                modifyColumns(con, tableName, new Column(colName, "VARCHAR(" + newSize + ")"));
            }
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rsColumns);
        }
    }
}
