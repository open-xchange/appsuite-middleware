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

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(AbstractCreateTableImpl.class);

    /**
     * Initializes a new {@link AbstractCreateTableImpl}.
     */
    protected AbstractCreateTableImpl() {
        super();
    }

    @Override
    public final void perform(final Connection con) throws OXException {
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            for (final String create : getCreateStatements()) {
                final String tableName = extractTableName(create);
                if (null != tableName) {
                    if (tableExists(con, tableName)) {
                        LOG.debug("A table with name \"{}\" already exists. Aborting table creation.", tableName);
                    } else {
                        try {
                            stmt.execute(create);
                        } catch (SQLException e) {
                            final String sep = Strings.getLineSeparator();
                            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, new StringBuilder(256).append(e.getMessage()).append(sep).append("Affected statement:").append(sep).append(create).toString());
                        }
                    }
                }
                final String procedureName = extractProcedureName(create);
                if (null != procedureName) {
                    if (procedureExists(con, procedureName)) {
                        LOG.debug("A procedure with name \"{}\" already exists. Aborting procedure creation.", procedureName);
                    } else {
                        try {
                            stmt.execute(create);
                        } catch (SQLException e) {
                            final String sep = Strings.getLineSeparator();
                            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, new StringBuilder(256).append(e.getMessage()).append(sep).append("Affected statement:").append(sep).append(create).toString());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            // e.printStackTrace();
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static final Pattern PATTERN_CREATE_TABLE = Pattern.compile("CREATE +TABLE +`?(\\w+)`? *\\(");
    private static final Pattern PATTERN_CREATE_PROCEDURE = Pattern.compile("CREATE +PROCEDURE +`?(\\w+)`?");

    private static String extractTableName(final String create) {
        final Matcher m = PATTERN_CREATE_TABLE.matcher(create);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private static String extractProcedureName(final String create) {
        final Matcher m = PATTERN_CREATE_PROCEDURE.matcher(create);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private static final String TABLE = "TABLE";

    private static final boolean tableExists(final Connection con, final String table) throws SQLException {
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

    private static final boolean procedureExists(final Connection con, final String procedure) throws SQLException {
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

    /**
     * Gets the <code>CREATE TABLE</code> statements for the {@link #tablesToCreate() tables}.
     *
     * @return The <code>CREATE TABLE</code> statements
     */
    protected abstract String[] getCreateStatements();

    /**
     * The constant to signal no dependencies to other tables.
     */
    protected static final String[] NO_TABLES = new String[0];

}
