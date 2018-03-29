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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link AbstractConvertUtf8ToUtf8mb4Task}
 *
 * Automaticaly changes table and column character sets and collations from utf8 to utf8mb4.
 * This is only possible for "non problematic" table structures. E.g. if a key is too long and or a table width is too large, additional actions need to be performed.
 * Either by implementing {@link AbstractConvertUtf8ToUtf8mb4Task#before} and/or {@link AbstractConvertUtf8ToUtf8mb4Task#after} or changing the table manually.
 * Note: Even if everything is performed in one transaction, MySQL can not rollback DDL statements (ALTER TABLE)!
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public abstract class AbstractConvertUtf8ToUtf8mb4Task extends UpdateTaskAdapter {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractConvertUtf8ToUtf8mb4Task.class);

    public enum UTF8MB4Collation {
        utf8mb4_bin,
        utf8mb4_unicode_ci,
        utf8mb4_general_ci
    }

    protected static final String UTF8MB4_CHARSET = "utf8mb4";
    protected static final String UTF8MB4_UNICODE_COLLATION = "utf8mb4_unicode_ci";
    protected static final String UTF8MB4_BIN_COLLATION = "utf8mb4_bin";

    private static final String TABLE_INFORMATION = "SELECT t.TABLE_COLLATION, ccsa.CHARACTER_SET_NAME FROM information_schema.tables t, information_schema.COLLATION_CHARACTER_SET_APPLICABILITY ccsa WHERE t.table_schema = ? AND ccsa.collation_name = t.table_collation AND ccsa.CHARACTER_SET_NAME = 'utf8' AND t.TABLE_NAME = ?";

    private static String SHOW_CREATE_TABLE = "SHOW CREATE TABLE ";

    private static final String COLUMN_INFORMATION = "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE table_schema = ? AND CHARACTER_SET_NAME = 'utf8' AND TABLE_NAME = ?";

    protected static final String[] NO_DEPENDENCIES = new String[] {};

    /**
     * Initialises a new {@link AbstractConvertUtf8ToUtf8mb4Task}.
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
    private void innerPerform(Connection con, String schema) throws SQLException {
        for (String table : tablesToConvert()) {
            try {
                innerPerform(con, schema, table);
            } catch (SQLException e) {
                LOGGER.error("Failed to convert table {} from utf8 to utf8mb4. Reason: {}", table, e.getMessage());
                throw e;
            }
        }
    }

    /**
     * Converts the charset and collation of the specified table in the specified schema
     * 
     * @param con The {@link Connection}
     * @param schema The schema's name
     * @param table The table's name
     * @throws SQLException if an SQL error is occurred
     */
    private void innerPerform(Connection con, String schema, String table) throws SQLException {
        String createTable = getCreateTable(con, table);
        if (createTable == null) {
            LOGGER.info("Table {} not found. Skipping.", table);
            return;
        }
        PreparedStatement tableCharsetStmt = null;
        ResultSet tableCharsetRs = null;
        PreparedStatement alterStmt = null;
        try {
            tableCharsetStmt = con.prepareStatement(TABLE_INFORMATION);
            tableCharsetStmt.setString(1, schema);
            tableCharsetStmt.setString(2, table);
            tableCharsetRs = tableCharsetStmt.executeQuery();
            String tableCollation = null;
            String tableCharset = null;
            if (tableCharsetRs.next()) {
                tableCollation = mb4Collation(tableCharsetRs.getString("TABLE_COLLATION"));
                tableCharset = mb4Charset(tableCharsetRs.getString("CHARACTER_SET_NAME"));
            }
            Databases.closeSQLStuff(tableCharsetRs, tableCharsetStmt);
            tableCharsetRs = null;
            tableCharsetStmt = null;

            List<Column> newColumns = getColumsToModify(con, schema, table);

            String alterTable = alterTable(table, newColumns, tableCharset, tableCollation);
            if (!Strings.isEmpty(alterTable)) {
                alterStmt = con.prepareStatement(alterTable);
                alterStmt.execute();
            }
        } finally {
            Databases.closeSQLStuff(tableCharsetRs, tableCharsetStmt);
            Databases.closeSQLStuff(alterStmt);
        }
    }

    /**
     * Queries the <code>information_schema</code> and retrieves a {@link List}
     * with {@link Column}s of the specified table in the specified schema that
     * need to be converted.
     * 
     * @param con The {@link Connection}
     * @param schema The schema's name
     * @param table The table's name
     * @return A {@link List} with all {@link Column}s that need conversion, or an empty {@link List}
     * @throws SQLException if an SQL error is occurred
     */
    protected List<Column> getColumsToModify(Connection con, String schema, String table) throws SQLException {
        String createTable = getCreateTable(con, table);
        if (createTable == null) {
            return Collections.emptyList();
        }
        PreparedStatement columnStmt = null;
        ResultSet columnRs = null;
        try {
            columnStmt = con.prepareStatement(COLUMN_INFORMATION);
            columnStmt.setString(1, schema);
            columnStmt.setString(2, table);
            columnRs = columnStmt.executeQuery();
            if (false == columnRs.next()) {
                return Collections.emptyList();
            }

            List<Column> newColumns = new ArrayList<>();
            do {
                String columnName = columnRs.getString("COLUMN_NAME");
                Column column = newColumn(columnName, createTable);
                if (column != null) {
                    newColumns.add(column);
                }
            } while (columnRs.next());
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
     * @return The new {@link Column} definition or <code>null</code> if the requested column is not part of the specified
     *         create table statement
     */
    private Column newColumn(String columnName, String createTable) {
        Pattern pattern = Pattern.compile("[`'´\"]" + Pattern.quote(columnName) + "[`'´\"]([^,]*),");
        Matcher matcher = pattern.matcher(createTable);
        if (!matcher.find()) {
            return null;
        }
        String definition = matcher.group(1);
        boolean changed = false;
        if (definition.contains("CHARACTER SET utf8") && !definition.contains("CHARACTER SET utf8mb4")) {
            definition = definition.replace("CHARACTER SET utf8", "CHARACTER SET utf8mb4");
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
     * @param table Tje table's name
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
     * Creates and returns the <code>ALTER TABLE</code> statement for the specified table with the specified {@link List}
     * of {@link Column} and the specified table character set and collation
     * 
     * @param table The table's name
     * @param columns The {@link List} of {@link Column}s
     * @param tableCharset The table's character set
     * @param tableCollation The table's collation
     * @return the <code>ALTER TABLE</code> statement
     */
    protected String alterTable(String table, List<Column> columns, String tableCharset, String tableCollation) {
        if ((columns == null || columns.isEmpty()) && Strings.isEmpty(tableCollation) && Strings.isEmpty(tableCharset)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ");
        sb.append(table);
        if (columns != null && !columns.isEmpty()) {
            sb.append(columns.stream().map(c -> c.getName() + " " + c.getDefinition()).collect(Collectors.joining(", MODIFY COLUMN ", " MODIFY COLUMN ", ",")));
        }
        if (!Strings.isEmpty(tableCharset)) {
            sb.append(" DEFAULT CHARACTER SET=");
            sb.append(tableCharset);
        }
        if (!Strings.isEmpty(tableCollation)) {
            sb.append(" COLLATE=");
            sb.append(tableCollation);
        }
        return sb.toString();
    }

    /**
     * Returns the utf8mb4 derivative of the specified character set
     * 
     * @param charset The utf8 character set
     * @return The utf8mb4 derivative
     */
    protected String mb4Charset(String charset) {
        if (Strings.isEmpty(charset) || charset.contains("utf8")) {
            return "utf8mb4";
        } else {
            return charset;
        }
    }

    /**
     * Returns the utf8mb4 derivative of the specified collation
     * 
     * @param collation The utf8 collation
     * @return The utf8mb4 derivative
     */
    protected String mb4Collation(String collation) {
        //FIXME: support bin and general derivatives as well
        if (Strings.isEmpty(collation)) {
            return "utf8mb4";
        } else if (collation.contains(UTF8MB4_CHARSET)) {
            return collation;
        } else if (collation.contains("utf8")) {
            return collation.replace("utf8", UTF8MB4_CHARSET);
        } else {
            return collation;
        }
    }

    /**
     * Changes the charset and collation of the specified table and (optionally) shrinks the specified
     * varchar columns
     *
     * @param connection The {@link Connection}
     * @param schema The schema name
     * @param table The table name
     * @param varcharColumns The optional varchar columns with their respective varchar sizes
     *            (use only if the column is part of the PK or is a KEY and it's size surpasses the limit of 767 chars in total, i.e. varchar length >= 192)
     * @throws SQLException
     */
    protected void changeTable(Connection connection, String schema, String table, Map<String, Integer> varcharColumns) throws SQLException {
        PreparedStatement alterStmt = null;
        try {
            List<Column> columnsToModify = getColumsToModify(connection, schema, table);
            for (Entry<String, Integer> varcharColumn : varcharColumns.entrySet()) {
                columnsToModify = columnsToModify.stream().map((column) -> shrinkVarcharColumn(varcharColumn.getKey(), varcharColumn.getValue(), column)).collect(Collectors.toList());
            }
            String alterTable = alterTable(table, columnsToModify, UTF8MB4_CHARSET, UTF8MB4_UNICODE_COLLATION);

            if (!Strings.isEmpty(alterTable)) {
                alterStmt = connection.prepareStatement(alterTable);
                alterStmt.execute();
            }
        } finally {
            Databases.closeSQLStuff(alterStmt);
        }
    }

    /**
     * Checks if the specified {@link Column}'s name matches the specified column name and if it does
     * shrinks it from varchar(charLength) to varchar(191), otherwise the {@link Column} is returned.
     * unaltered.
     *
     * @param columnName The column name
     * @param charLength The character length of the existing column
     * @param column The {@link Column} to shrink
     * @return The shrinked {@link Column} if the names match; otherwise the unaltered column
     */
    protected Column shrinkVarcharColumn(String columnName, int charLength, Column column) {
        return column.getName().equals(columnName) ? shrinkVarcharColumn(column, charLength) : column;
    }

    /**
     * Shrinks the specified {@link Column} from varchar(charLength) to varchar(191)
     *
     * @param column The {@link Column} to shrink
     * @param charLength The character length of the existing column
     * @return the shirnked {@link Column}
     */
    private Column shrinkVarcharColumn(Column column, int charLength) {
        return new Column(column.getName(), column.getDefinition().replace("varchar(" + charLength + ")", "varchar(191)"));
    }

    /**
     * The tables to convert. Checks for explicit table and column charsets and (in case of utf8) converts them to utf8mb4.
     * Converts collations accordingly.
     * No further adjustments are done. For more complex tables and additional changes (e.g. key adjustments) perform this manually or use {@link AbstractConvertUtf8ToUtf8mb4Task#before} and {@link AbstractConvertUtf8ToUtf8mb4Task#after}.
     *
     * @return A List of tables to be converted.
     */
    protected abstract List<String> tablesToConvert();

    /**
     * Method beeing executed before the conversion of the tables will start (same transaction).
     * Use provided connection to perform statements in the same transaction.
     *
     * @param params
     * @param connection
     * @throws SQLException
     */
    protected abstract void before(PerformParameters params, Connection connection) throws SQLException;

    /**
     * Method beeing executed after the conversion of the tables has finisehd.
     * Use provided connection to perform statements in the same transaction.
     *
     * @param params
     * @param connection
     * @throws SQLException
     */
    protected abstract void after(PerformParameters params, Connection connection) throws SQLException;

}
