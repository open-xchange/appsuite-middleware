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

package com.openexchange.database.internal.change.utf8mb4;

import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
import com.openexchange.java.Strings;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * This class is a copy of com.openexchange.groupware.update.AbstractConvertUtf8ToUtf8mb4Task as it could not be used here due to cyclic dependency issues and the special handling for the globaldb which means
 * connections are only possible based on context/group identifier read from the globaldb.yml while startup/configuration reload.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public abstract class AbstractLiquibaseUtf8mb4Adapter implements CustomTaskChange {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractLiquibaseUtf8mb4Adapter.class);

    @Override
    public void setUp() throws SetupException {
        // Nothing
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        // Ignore
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    private static final String TABLE_INFORMATION = "SELECT t.TABLE_COLLATION, ccsa.CHARACTER_SET_NAME FROM information_schema.tables t, information_schema.COLLATION_CHARACTER_SET_APPLICABILITY ccsa WHERE t.table_schema = ? AND ccsa.collation_name = t.table_collation AND ccsa.CHARACTER_SET_NAME = 'utf8' AND t.TABLE_NAME = ?";

    @Override
    public void execute(Database database) throws CustomChangeException {
        DatabaseConnection databaseConnection = database.getConnection();
        if (!(databaseConnection instanceof JdbcConnection)) {
            throw new CustomChangeException("Cannot get underlying connection because database connection is not of type " + JdbcConnection.class.getName() + ", but of type: " + databaseConnection.getClass().getName());
        }

        Connection connection = ((JdbcConnection) databaseConnection).getUnderlyingConnection();
        try {
            String schemaName = connection.getCatalog() != null && Strings.isNotEmpty(connection.getCatalog()) ? connection.getCatalog() : (database.getDefaultCatalogName() != null && Strings.isNotEmpty(database.getDefaultCatalogName()) ? database.getDefaultCatalogName() : getDefaultSchemaName());

            before(connection, schemaName);
            for (String table : tablesToConvert()) {
                changeTable(connection, schemaName, table);
            }
            after(connection, schemaName);

        } catch (SQLException e) {
            LOGGER.error("Failed to convert {} to utf8mb4 for GlobalDB", Strings.concat(",", tablesToConvert()), e);
            throw new CustomChangeException("SQL error", e);
        } catch (RuntimeException e) {
            LOGGER.error("Failed to convert {} to utf8mb4 for GlobalDB", Strings.concat(",", tablesToConvert()), e);
            throw new CustomChangeException("Runtime error", e);
        } finally {
            Databases.autocommit(connection);
        }
    }

    protected abstract List<String> tablesToConvert();

    protected abstract String getDefaultSchemaName();

    protected abstract void before(Connection connection, String schemaName) throws SQLException;

    protected abstract void after(Connection connection, String schemaName) throws SQLException;

    /**
     * Changes the charset/collation of the specified table.
     *
     * @param connection The connection to use
     * @param schema The schema name
     * @param table The table name
     * @throws SQLException If changing the table fails
     */
    protected void changeTable(Connection connection, String schema, String table) throws SQLException {
        changeTable(connection, schema, table, Collections.emptyMap());
    }

    /** The constant for the size of a <code>VARCHAR</code> column if it's part of a UNIQUE or PRIMARY KEY */
    private static final int UNIQUE_VARCHAR_SIZE = 191;

    /**
     * Changes the charset/collation of the specified table and (optionally) shrinks the specified VARCHAR columns and
     * a {@link List} with the definitions of {@link Column}s to modify
     *
     * @param connection The connection to use
     * @param schema The schema name
     * @param table The table name
     * @param optVarcharColumns The optional VARCHAR columns with their respective VARCHAR sizes
     *            (use only if the column is part of the PRIMARY KEY or is a (UNIQUE) KEY and it's size surpasses the limit of 767 bytes in total, i.e. VARCHAR length is greater than 191)
     * @throws SQLException if changing the table fails
     */
    protected void changeTable(Connection connection, String schema, String table, Map<String, Integer> optVarcharColumns) throws SQLException {
        changeTable(connection, schema, table, optVarcharColumns, Collections.emptyList());
    }

    /**
     * Changes the charset/collation of the specified table and (optionally) shrinks the specified VARCHAR columns and
     * a {@link List} with the definitions of {@link Column}s to modify
     *
     * @param connection The connection to use
     * @param schema The schema name
     * @param table The table name
     * @param optVarcharColumns The optional VARCHAR columns with their respective VARCHAR sizes
     *            (use only if the column is part of the PRIMARY KEY or is a (UNIQUE) KEY and it's size surpasses the limit of 767 bytes in total, i.e. VARCHAR length is greater than 191)
     * @param optColumnsToIgnore The optional listing of columns that are supposed to be ignored
     * @throws SQLException if changing the table fails
     */
    protected void changeTable(Connection connection, String schema, String table, Map<String, Integer> optVarcharColumns, List<String> optColumnsToIgnore) throws SQLException {
        String createTable = getCreateTable(connection, table);
        if (createTable == null) {
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

    private static final String COLUMN_INFORMATION = "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE table_schema = ? AND CHARACTER_SET_NAME = ? AND TABLE_NAME = ?";

    /** The constant for <code>utf8</code> character set */
    protected static final String UTF8_CHARSET = "utf8";

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

    private static final String SHOW_CREATE_TABLE = "SHOW CREATE TABLE ";

    /**
     * Retrieves the <code>CREATE TABLE</code> statement for the specified table
     *
     * @param con The {@link Connection}
     * @param table Tje table's name
     * @return the <code>CREATE TABLE</code> statement for the specified table or <code>null</code>
     *         if the specified table does not exist
     * @throws SQLException if an SQL error is occurred
     */
    private String getCreateTable(Connection con, String table) throws SQLException {
        if (false == tableExists(con, table)) {
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

    private static final boolean tableExists(final Connection con, final String table) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        boolean retval = false;
        try {
            rs = metaData.getTables(null, null, table, new String[] { "TABLE" });
            retval = (rs.next() && rs.getString("TABLE_NAME").equalsIgnoreCase(table));
        } finally {
            closeSQLStuff(rs);
        }
        return retval;
    }

    /** The constant for <code>"utf8mb4"</code> character set */
    protected static final String UTF8MB4_CHARSET = "utf8mb4";

    /** The constant for <code>"utf8mb4_bin"</code> collation for case-sensitive comparisons */
    protected static final String UTF8MB4_BIN_COLLATION = "utf8mb4_bin";

    /** The constant for <code>"utf8mb4_unicode_ci"</code> collation for case-insensitive comparisons */
    protected static final String UTF8MB4_UNICODE_COLLATION = "utf8mb4_unicode_ci";

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

}

class Column {

    /** The column name; e.g. <code>"intfield01"</code> */
    public final String name;

    /** The column definition; e.g. <code>"INT4 unsigned NOT NULL"</code> */
    public final String definition;

    /**
     * Initializes a new {@link Column}.
     *
     * @param name The name; e.g. <code>"intfield01"</code>
     * @param definition The definition; e.g. <code>"INT4 unsigned NOT NULL"</code>
     */
    public Column(String name, String definition) {
        super();
        this.name = name;
        this.definition = definition;
    }

    /**
     * Gets the column name; e.g. <code>"intfield01"</code>.
     *
     * @return The column name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the column definition; e.g. <code>"INT4 unsigned NOT NULL"</code>.
     *
     * @return The column definition
     */
    public String getDefinition() {
        return definition;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(32);
        if (name != null) {
            builder.append(name);
        }
        if (definition != null) {
            builder.append(" ").append(definition);
        }
        return builder.toString();
    }

}
