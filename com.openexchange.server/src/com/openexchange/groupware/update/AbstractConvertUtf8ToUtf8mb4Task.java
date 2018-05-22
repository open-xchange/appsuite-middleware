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

package com.openexchange.groupware.update;

import static com.openexchange.tools.update.Tools.existsIndex;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.internal.ExcludableUpdateTask;
import com.openexchange.java.Strings;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link AbstractConvertUtf8ToUtf8mb4Task}
 *
 * <p>Automatically changes table and column character sets and collations from utf8 to utf8mb4.
 * This is only possible for "non problematic" table structures. E.g. if a key is too long and or a table width is too large, additional actions need to be performed.
 * Either by implementing {@link AbstractConvertUtf8ToUtf8mb4Task#before} and/or {@link AbstractConvertUtf8ToUtf8mb4Task#after} or changing the table manually.
 * </p>
 * <span style="color:red;">Note</span>: Even if everything is performed in one transaction, MySQL can not roll-back DDL statements (ALTER TABLE)!
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
@ExcludableUpdateTask(namespace = "com.openexchange.groupware.update.utf8mb4")
public abstract class AbstractConvertUtf8ToUtf8mb4Task extends UpdateTaskAdapter {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractConvertUtf8ToUtf8mb4Task.class);

    /** The constant for the size of a <code>VARCHAR</code> column if it's part of a UNIQUE or PRIMARY KEY */
    private static final int UNIQUE_VARCHAR_SIZE = 191;

    /** Defines the upper character limit for a <code>VARCHAR</code> column. Longer columns will be converted to <code>TEXT</code> */
    private static final int MAX_VARCHAR = 1024;

    /** The constant for <code>utf8</code> character set */
    protected static final String UTF8_CHARSET = "utf8";

    /** The constant for <code>"utf8mb4"</code> character set */
    protected static final String UTF8MB4_CHARSET = "utf8mb4";

    /** The constant for <code>"utf8mb4_bin"</code> collation for case-sensitive comparisons */
    protected static final String UTF8MB4_BIN_COLLATION = "utf8mb4_bin";

    /** The constant for <code>"utf8mb4_unicode_ci"</code> collation for case-insensitive comparisons */
    protected static final String UTF8MB4_UNICODE_COLLATION = "utf8mb4_unicode_ci";

    /** The constant for specifying no dependencies to other update tasks */
    protected static final String[] NO_DEPENDENCIES = new String[0];

    private static final String TABLE_INFORMATION = "SELECT t.TABLE_COLLATION, ccsa.CHARACTER_SET_NAME FROM information_schema.tables t, information_schema.COLLATION_CHARACTER_SET_APPLICABILITY ccsa WHERE t.table_schema = ? AND ccsa.collation_name = t.table_collation AND ccsa.CHARACTER_SET_NAME = 'utf8' AND t.TABLE_NAME = ?";

    private static final String SHOW_CREATE_TABLE = "SHOW CREATE TABLE ";

    private static final String COLUMN_INFORMATION = "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE table_schema = ? AND CHARACTER_SET_NAME = ? AND TABLE_NAME = ?";

    /**
     * Initializes a new {@link AbstractConvertUtf8ToUtf8mb4Task}.
     */
    public AbstractConvertUtf8ToUtf8mb4Task() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            before(params, con);
            innerPerform(con, params.getSchema().getSchema());
            after(params, con);

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

    /**
     * Converts all tables in to the specified schema
     *
     * @param con The {@link Connection} to use
     * @param schema The name of the schema
     * @throws SQLException If an SQL error is occurred
     */
    protected void innerPerform(Connection con, String schema) throws SQLException {
        for (String table : tablesToConvert()) {
            try {
                changeTable(con, schema, table, Collections.emptyList());
            } catch (SQLException e) {
                LOGGER.error("Failed to convert table {} from utf8 to utf8mb4. Reason: {}", table, e.getMessage());
                throw e;
            }
        }
    }

    /**
     * Determines the text columns, which need to be converted since they use
     * <code>"utf8"</code> charset and/or <code>"utf8_unicode_ci"</code> collation.
     *
     * @param con The connection to use
     * @param schema The schema name
     * @param table The name of the table to inspect
     * @param optColumnsToIgnore The optional listing of columns that are supposed to be ignored
     * @return The map view of the columns that need to be altered
     * @throws SQLException If columns cannot be returned
     */
    protected Map<String, Column> getColumsToModifyAsMap(Connection con, String schema, String table, List<String> optColumnsToIgnore) throws SQLException {
        List<Column> columsToModify = getColumsToModify(con, schema, table, optColumnsToIgnore);
        if (null == columsToModify) {
            return null;
        }

        int size = columsToModify.size();
        if (size <= 0) {
            return Collections.emptyMap();
        }

        Map<String, Column> m = new LinkedHashMap<>(size);
        for (Column column : columsToModify) {
            m.put(column.name, column);
        }
        return m;
    }

    /**
     * Determines the text columns, which need to be converted since they use
     * <code>"utf8"</code> charset and/or <code>"utf8_unicode_ci"</code> collation.
     *
     * @param con The connection to use
     * @param schema The schema name
     * @param table The name of the table to inspect
     * @param optColumnsToIgnore The optional listing of columns that are supposed to be ignored
     * @return The columns that need to be altered
     * @throws SQLException If columns cannot be returned
     */
    protected List<Column> getColumsToModify(Connection con, String schema, String table, List<String> optColumnsToIgnore) throws SQLException {
        return getColumsToModify(con, schema, table, UTF8_CHARSET, optColumnsToIgnore);
    }

    /**
     * Determines the text columns, which need to be converted since they use
     * the specified character set
     *
     * @param con The connection to use
     * @param schema The schema name
     * @param table The name of the table to inspect
     * @param charset The character set of the columns
     * @param optColumnsToIgnore The optional listing of columns that are supposed to be ignored
     * @return The columns that need to be altered
     * @throws SQLException If columns cannot be returned
     */
    protected List<Column> getColumsToModify(Connection con, String schema, String table, String charset, List<String> optColumnsToIgnore) throws SQLException {
        String createTable = getCreateTable(con, table);
        if (createTable == null) {
            return Collections.emptyList();
        }
        PreparedStatement columnStmt = null;
        ResultSet columnRs = null;
        try {
            columnStmt = con.prepareStatement(COLUMN_INFORMATION);
            columnStmt.setString(1, schema);
            columnStmt.setString(2, charset);
            columnStmt.setString(3, table);
            columnRs = columnStmt.executeQuery();
            if (false == columnRs.next()) {
                return Collections.emptyList();
            }

            List<Column> newColumns = new ArrayList<>();
            if (null == optColumnsToIgnore || optColumnsToIgnore.isEmpty()) {
                do {
                    String columnName = columnRs.getString("COLUMN_NAME");
                    Column column = newColumn(columnName, createTable, charset);
                    if (column != null) {
                        newColumns.add(column);
                    }
                } while (columnRs.next());
            } else {
                Set<String> ignorees = new HashSet<>(optColumnsToIgnore);
                do {
                    String columnName = columnRs.getString("COLUMN_NAME");
                    if (false == ignorees.contains(columnName)) {
                        Column column = newColumn(columnName, createTable, charset);
                        if (column != null) {
                            newColumns.add(column);
                        }
                    }
                } while (columnRs.next());
            }

            return newColumns;
        } finally {
            Databases.closeSQLStuff(columnRs, columnStmt);
        }
    }

    /**
     * Returns a new {@link Column} definition with its character set and collation set to the utf8mb4 derivative.
     *
     * @param columnName The {@link Column}'s name
     * @param createTable The create table statement
     * @param charset The table's character set
     * @return The new {@link Column} definition or <code>null</code> if the requested column is not part of the specified
     *         create table statement
     */
    private Column newColumn(String columnName, String createTable, String charset) {
        Pattern pattern = Pattern.compile("[`'\u00b4\"]" + Pattern.quote(columnName) + "[`'\u00b4\"]([^,]*),");
        Matcher matcher = pattern.matcher(createTable);
        if (!matcher.find()) {
            return null;
        }
        String definition = matcher.group(1);
        boolean changed = false;
        if (definition.contains("CHARACTER SET " + charset) && !definition.contains("CHARACTER SET utf8mb4")) {
            definition = definition.replace("CHARACTER SET " + charset, "CHARACTER SET utf8mb4");
            changed = true;
        }
        if (definition.contains("COLLATE utf8_")) {
            definition = definition.replace("COLLATE utf8_", "COLLATE utf8mb4_");
            changed = true;
        }

        return changed ? new Column(columnName, definition.trim()) : null;
    }

    /**
     * Retrieves the <code>CREATE TABLE</code> statement for the specified table
     *
     * @param con The {@link Connection}
     * @param table The table's name
     * @return the <code>CREATE TABLE</code> statement for the specified table or <code>null</code>
     *         if the specified table does not exist
     * @throws SQLException if an SQL error is occurred
     */
    private String getCreateTable(Connection con, String table) throws SQLException {
        if (false == Tools.tableExists(con, table)) {
            return null;
        }

        PreparedStatement createTableStmt = null;
        ResultSet createTableRs = null;
        try {
            createTableStmt = con.prepareStatement(SHOW_CREATE_TABLE + table);
            createTableRs = createTableStmt.executeQuery();
            if (createTableRs.next()) {
                return createTableRs.getString(2);
            }
        } finally {
            Databases.closeSQLStuff(createTableRs, createTableStmt);
        }

        return null;
    }

    /**
     * Generates the <code>"ALTER TABLE..."</code> statement to use to convert the table to utf8mb4.
     *
     * @param table The table to process
     * @param columns The columns to modify
     * @param tableCharset The default charset for the table to set
     * @param tableCollation The default collation for the table to set
     * @return The <code>"ALTER TABLE..."</code> statement or <code>null</code> if nothing needs to be changed
     */
    protected String alterTable(String table, List<Column> columns, String tableCharset, String tableCollation) {
        boolean columnsGiven = columns != null && !columns.isEmpty();
        boolean tableCharsetGiven = Strings.isNotEmpty(tableCharset);
        boolean tableCollationGiven = Strings.isNotEmpty(tableCollation);
        if (false == columnsGiven && false == tableCharsetGiven && false == tableCollationGiven) {
            // Nothing to do
            return null;
        }

        StringBuilder sb = new StringBuilder(128);
        sb.append("ALTER TABLE ");
        sb.append(table);

        if (columnsGiven) {
            // Append "MODIFY COLUMN" statements
            boolean first = true;
            for (Column column : columns) {
                if (first) {
                    first = false;
                } else {
                    sb.append(',');
                }
                sb.append(" MODIFY COLUMN ").append(column.getName()).append(' ').append(column.getDefinition());
            }
        }
        if (tableCharsetGiven || tableCollationGiven) {
            // Append modification of default charset/collation
            if (columnsGiven) {
                sb.append(',');
            }
            if (tableCharsetGiven) {
                sb.append(" DEFAULT CHARACTER SET=");
                sb.append(tableCharset);
            }
            if (tableCollationGiven) {
                sb.append(" COLLATE=");
                sb.append(tableCollation);
            }
        }

        return sb.toString();
    }

    /**
     * Determines the "mb4" derivative for specified charset.
     *
     * @param charset The charset to examine
     * @return The appropriate "mb4" derivative
     */
    protected String mb4Charset(String charset) {
        String cs = Strings.asciiLowerCase(charset);
        return Strings.isEmpty(cs) || (cs.contains("utf8") && !cs.contains(UTF8MB4_CHARSET)) ? UTF8MB4_CHARSET : cs;
    }

    /**
     * Returns the utf8mb4 derivative of the specified collation
     *
     * @param collation The utf8 collation
     * @return The utf8mb4 derivative
     */
    protected String mb4Collation(String collation) {
        String col = Strings.asciiLowerCase(collation);
        if (Strings.isEmpty(col)) {
            return UTF8MB4_CHARSET;
        } else if (col.contains(UTF8MB4_CHARSET)) {
            return col;
        } else if (col.contains("utf8")) {
            return col.replace("utf8", UTF8MB4_CHARSET);
        } else {
            return col;
        }
    }

    /**
     * Checks whether to modify the specified <code>VARCHAR</code> column to a <code>TEXT</code>, i.e. if it exceeds the specified size
     *
     * @param schema The schema's name
     * @param tableName The table's name
     * @param columnName The column's name
     * @param size The size
     * @param connection The {@link Connection}
     * @return <code>true</code> if the column should be modified; <code>false</code> otherwise
     * @throws SQLException if an SQL error is occurred
     */
    private boolean modifyVarChar(String schema, String tableName, String columnName, int size, Connection connection) throws SQLException {
        int columnSize = getVarcharColumnSize(tableName, columnName, connection, schema);
        return columnSize >= size;
    }

    /**
     * Retrieves the size of the specified varchar column
     *
     * @param tableName The table's name
     * @param colName The column's name
     * @param con The {@link Connection}
     * @param schema The schema's name
     * @throws SQLException if an SQL error is occurred
     */
    private int getVarcharColumnSize(String tableName, String colName, Connection con, String schema) throws SQLException {
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("SELECT CHARACTER_MAXIMUM_LENGTH,DATA_TYPE FROM information_schema.COLUMNS WHERE table_schema = ? AND TABLE_NAME = ? AND COLUMN_NAME = ?;");
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            stmt.setString(3, colName);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String dataType = rs.getString("DATA_TYPE");
                if ("varchar".equalsIgnoreCase(dataType)) {
                    return rs.getInt("CHARACTER_MAXIMUM_LENGTH");
                }
            }

            // No such VARCHAR column
            return -1;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Changes the charset/collation of the specified table.
     *
     * @param connection The connection to use
     * @param schema The schema name
     * @param table The table name
     * @param optColumnsToIgnore The optional listing of columns that are supposed to be ignored
     * @throws SQLException If changing the table fails
     */
    protected void changeTable(Connection connection, String schema, String table, List<String> optColumnsToIgnore) throws SQLException {
        changeTable(connection, schema, table, Collections.emptyMap(), optColumnsToIgnore);
    }

    /**
     * Changes the charset/collation of the specified table and (optionally) shrinks the specified VARCHAR columns.
     *
     * @param connection The connection to use
     * @param schema The schema name
     * @param table The table name
     * @param optVarcharColumns The optional VARCHAR columns with their respective VARCHAR sizes
     *            (use only if the column is part of the PRIMARY KEY or is a (UNIQUE) KEY and it's size surpasses the limit of 767 bytes in total, i.e. VARCHAR length is greater than 191)
     * @param optColumnsToIgnore The optional listing of columns that are supposed to be ignored
     * @throws SQLException If changing the table fails
     */
    protected void changeTable(Connection connection, String schema, String table, Map<String, Integer> optVarcharColumns) throws SQLException {
        changeTable(connection, schema, table, optVarcharColumns, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Changes the charset/collation of the specified table and (optionally) shrinks the specified VARCHAR columns.
     *
     * @param connection The connection to use
     * @param schema The schema name
     * @param table The table name
     * @param optVarcharColumns The optional VARCHAR columns with their respective VARCHAR sizes
     *            (use only if the column is part of the PRIMARY KEY or is a (UNIQUE) KEY and it's size surpasses the limit of 767 bytes in total, i.e. VARCHAR length is greater than 191)
     * @param optColumnsToIgnore The optional listing of columns that are supposed to be ignored
     * @throws SQLException If changing the table fails
     */
    protected void changeTable(Connection connection, String schema, String table, Map<String, Integer> optVarcharColumns, List<String> optColumnsToIgnore) throws SQLException {
        changeTable(connection, schema, table, optVarcharColumns, Collections.emptyList(), optColumnsToIgnore);
    }

    /**
     * Changes the charset/collation of the specified table and (optionally) shrinks the specified VARCHAR columns and
     * a {@link List} with the definitions of {@link Column}s to modify
     * 
     * @param connection The connection to use
     * @param schema The schema name
     * @param table The table name
     * @param optVarcharColumns The optional VARCHAR columns with their respective VARCHAR sizes
     *            (use only if the column is part of the PRIMARY KEY or a (UNIQUE) KEY and it's size surpasses the limit of 767 bytes in total, i.e. VARCHAR length is greater than 191)
     * @param modifyColumns a {@link List} with the definitions of {@link Column}s to modify
     * @param optColumnsToIgnore The optional listing of columns that are supposed to be ignored
     * @throws SQLException if changing the table fails
     */
    protected void changeTable(Connection connection, String schema, String table, Map<String, Integer> optVarcharColumns, List<Column> modifyColumns, List<String> optColumnsToIgnore) throws SQLException {
        if (!Tools.tableExists(connection, table)) {
            LOGGER.info("Table {} not found. Skipping.", table);
            return;
        }

        // Check table information to see if table's default charset/collation needs to be changed
        String tableCollation = null;
        String tableCharset = null;
        {
            PreparedStatement tableCharsetStmt = null;
            ResultSet tableCharsetRs = null;
            try {
                // Check table information to see if tables default charset/collation needs to be changed
                tableCharsetStmt = connection.prepareStatement(TABLE_INFORMATION);
                tableCharsetStmt.setString(1, schema);
                tableCharsetStmt.setString(2, table);
                tableCharsetRs = tableCharsetStmt.executeQuery();
                if (tableCharsetRs.next()) {
                    tableCollation = mb4Collation(tableCharsetRs.getString("TABLE_COLLATION"));
                    tableCharset = mb4Charset(tableCharsetRs.getString("CHARACTER_SET_NAME"));
                }
            } finally {
                Databases.closeSQLStuff(tableCharsetRs, tableCharsetStmt);
            }
        }

        PreparedStatement alterStmt = null;
        try {
            // Next, determine the 'utf8' columns, which need to be converted to 'utf8mb4'
            Map<String, Column> columnsToModify = getColumsToModifyAsMap(connection, schema, table, optColumnsToIgnore);
            if (false == columnsToModify.isEmpty() && null != optVarcharColumns) {
                for (Map.Entry<String, Integer> varcharColumn : optVarcharColumns.entrySet()) {
                    String columnName = varcharColumn.getKey();
                    Column column = columnsToModify.get(columnName);
                    if (null != column) {
                        int expectedSize = varcharColumn.getValue().intValue();
                        if (possibleDataTruncation(connection, varcharColumn.getKey(), table, UNIQUE_VARCHAR_SIZE)) {
                            throw new SQLException("The update task '" + this.getClass().getName() + "' will result in data truncation for column '" + varcharColumn.getKey() + "' in table '" + table + "'. Aborting execution.");
                        }
                        columnsToModify.put(columnName, shrinkVarcharColumn(column, expectedSize));
                    }
                }
            }

            // Apply any modifications to the columns
            if (null != modifyColumns && !modifyColumns.isEmpty()) {
                for (Column column : modifyColumns) {
                    String columnName = column.getName();
                    if (modifyVarChar(schema, table, columnName, MAX_VARCHAR, connection)) {
                        columnsToModify.put(columnName, column);
                    }
                }
            }

            // Compile the "ALTER TABLE..." statement
            String alterTable = alterTable(table, new ArrayList<>(columnsToModify.values()), tableCharset, tableCollation);
            if (Strings.isNotEmpty(alterTable)) {
                // Execute the "ALTER TABLE..." statement
                alterStmt = connection.prepareStatement(alterTable);
                alterStmt.execute();
            }
        } finally {
            Databases.closeSQLStuff(alterStmt);
        }
    }

    /**
     * Checks whether data stored in the specified column in the specified table exceeds the maximum new column size
     *
     * @param connection The connection
     * @param column The column name
     * @param table The table name
     * @param maxColumnSize The new maximum column size
     * @return <code>true</code> if data truncation will occur; <code>false</code> otherwise
     * @throws SQLException if an SQL error is occurred
     */
    private boolean possibleDataTruncation(Connection connection, String column, String table, int maxColumnSize) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT MAX(LENGTH(" + column + ")) FROM " + table);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return false;
            }
            return rs.getInt(1) > maxColumnSize;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Checks if the specified {@link Column}'s name matches the specified column name and if it does
     * shrinks it from VARCHAR(charLength) to VARCHAR(191), otherwise the column} is returned as-is.
     *
     * @param columnName The column name
     * @param expectedSize The character length of the existing column
     * @param column The {@link Column} to shrink
     * @return The shrinked column if the names match; otherwise the unaltered column
     */
    protected Column shrinkVarcharColumn(String columnName, int expectedSize, Column column) {
        return column.getName().equals(columnName) ? shrinkVarcharColumn(column, expectedSize) : column;
    }

    /**
     * Shrinks the specified column from VARCHAR(charLength) to VARCHAR(191)
     *
     * @param column The column to shrink
     * @param expectedSize The character length of the existing column
     * @return the shrinked column
     */
    private Column shrinkVarcharColumn(Column column, int expectedSize) {
        String columnDefinition = Strings.asciiLowerCase(column.getDefinition());
        String expected = new StringBuilder("varchar(").append(expectedSize).append(')').toString();
        if (columnDefinition.indexOf(expected) < 0) {
            // No such VARCHAR in definition
            return column;
        }

        return new Column(column.getName(), Strings.asciiLowerCase(column.getDefinition()).replace(expected, "varchar(" + UNIQUE_VARCHAR_SIZE + ")"));
    }

    /**
     * Re-creates the specified key for the specified table.
     * 
     * @param connection The {@link Connection}
     * @param table The table's name
     * @param columnNames the column names
     * @param columnSizes The column sizes (use -1 for full column length)
     * @throws SQLException if an SQL error is occurred
     */
    protected void recreateKey(Connection connection, String table, String[] columnNames, int[] columnSizes) throws SQLException {
        String indexName = existsIndex(connection, table, columnNames);
        if (Strings.isNotEmpty(indexName)) {
            Tools.dropKey(connection, table, indexName);
        }
        Tools.createKey(connection, table, columnNames, columnSizes, false, indexName);
    }

    /**
     * Gets the tables to convert.
     * <p>
     * Checks for explicit table and column charsets and (in case of utf8) converts them to utf8mb4. Converts collations accordingly.
     * </p>
     * <p>
     * No further adjustments are done. For more complex tables and additional changes (e.g. key adjustments) perform this manually or use {@link AbstractConvertUtf8ToUtf8mb4Task#before} and/or {@link AbstractConvertUtf8ToUtf8mb4Task#after}.
     * </p>
     *
     * @return A List of tables to be converted.
     */
    protected abstract List<String> tablesToConvert();

    /**
     * Method being executed before the conversion of the tables will start (same transaction).
     * <p>
     * Use provided connection to perform statements in the same transaction.
     *
     * @param params The update task parameters
     * @param connection The connection to use
     * @throws SQLException If an SQL error occurs
     */
    protected abstract void before(PerformParameters params, Connection connection) throws SQLException;

    /**
     * Method being executed after the conversion of the tables has finished.
     * <p>
     * Use provided connection to perform statements in the same transaction.
     *
     * @param params The update task parameters
     * @param connection The connection to use
     * @throws SQLException If an SQL error occurs
     */
    protected abstract void after(PerformParameters params, Connection connection) throws SQLException;

}
