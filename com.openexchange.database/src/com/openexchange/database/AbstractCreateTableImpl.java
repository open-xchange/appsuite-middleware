/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * Abstract class for easily implementing {@link CreateTableService} services.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class AbstractCreateTableImpl implements CreateTableService {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractCreateTableImpl.class);
    }

    /**
     * Initializes a new {@link AbstractCreateTableImpl}.
     */
    protected AbstractCreateTableImpl() {
        super();
    }

    @Override
    public final void perform(final Connection con) throws OXException {
        try {
            for (String create : getCreateStatements()) {
                String tableName = extractTableName(create);
                if (null != tableName) {
                    createTable(tableName, create, con);
                }

                String procedureName = extractProcedureName(create);
                if (null != procedureName) {
                    if (procedureExists(con, procedureName)) {
                        LoggerHolder.LOG.debug("A procedure with name \"{}\" already exists. Aborting procedure creation.", procedureName);
                    } else {
                        Statement stmt = con.createStatement();
                        try {
                            stmt.execute(create);
                        } catch (SQLException e) {
                            final String sep = Strings.getLineSeparator();
                            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, new StringBuilder(256).append(e.getMessage()).append(sep).append("Affected statement:").append(sep).append(create).toString());
                        } finally {
                            Databases.closeSQLStuff(stmt);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the <code>CREATE TABLE...</code> statements for the {@link #tablesToCreate() tables}.
     *
     * @return The <code>CREATE TABLE...</code> statements
     */
    protected abstract String[] getCreateStatements();

    /**
     * The constant to signal no dependencies to other tables.
     */
    protected static final String[] NO_TABLES = new String[0];

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates the table according to given <code>"CREATE TABLE..."</code> statement.
     *
     * @param tableName The name of the table
     * @param sqlCreate The <code>"CREATE TABLE..."</code> statement
     * @param con The connection to use
     * @throws OXException If creating the table fails
     */
    protected static void createTable(String tableName, String sqlCreate, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            if (tableExists(con, tableName)) {
                LoggerHolder.LOG.debug("A table with name \"{}\" already exists. Aborting table creation.", tableName);
            } else {
                stmt = con.prepareStatement(sqlCreate);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static final String TABLE = "TABLE";

    /**
     * Checks for existence of denoted table.
     *
     * @param con The connection to use
     * @param table The table to check
     * @return <code>true</code> if such a table exists; otherwise <code>false</code>
     * @throws SQLException If an SQL error occurs
     */
    protected static boolean tableExists(Connection con, String table) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        boolean retval = false;
        try {
            rs = metaData.getTables(null, null, table, new String[] { TABLE });
            retval = (rs.next() && rs.getString("TABLE_NAME").equalsIgnoreCase(table));
        } finally {
            Databases.closeSQLStuff(rs);
        }
        return retval;
    }

    /**
     * Checks for existence of denoted procedure.
     *
     * @param con The connection to use
     * @param procedure The procedure to check
     * @return <code>true</code> if such a procedure exists; otherwise <code>false</code>
     * @throws SQLException If an SQL error occurs
     */
    protected static boolean procedureExists(Connection con, String procedure) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        boolean retval = false;
        try {
            rs = metaData.getProcedures(null, null, procedure);
            retval = (rs.next() && rs.getString("PROCEDURE_NAME").equalsIgnoreCase(procedure));
        } finally {
            Databases.closeSQLStuff(rs);
        }
        return retval;
    }

    private static final Pattern PATTERN_CREATE_TABLE = Pattern.compile("CREATE +TABLE +`?(\\w+)`? *\\(");

    private static String extractTableName(String create) {
        Matcher m = PATTERN_CREATE_TABLE.matcher(create);
        return m.find() ? m.group(1) : null;
    }

    private static final Pattern PATTERN_CREATE_PROCEDURE = Pattern.compile("CREATE +PROCEDURE +`?(\\w+)`?");

    private static String extractProcedureName(String create) {
        Matcher m = PATTERN_CREATE_PROCEDURE.matcher(create);
        return m.find() ? m.group(1) : null;
    }

}
