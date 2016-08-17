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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;

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
                        LOG.info("A table with name \"{}\" already exists. Aborting table creation.", tableName);
                    } else {
                        try {
                            stmt.execute(create);
                        } catch (SQLException e) {
                            final String sep = System.getProperty("line.separator");
                            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, new StringBuilder(256).append(e.getMessage()).append(sep).append("Affected statement:").append(sep).append(create).toString());
                        }
                    }
                }
                final String procedureName = extractProcedureName(create);
                if (null != procedureName) {
                    if (procedureExists(con, procedureName)) {
                        LOG.info("A procedure with name \"{}\" already exists. Aborting procedure creation.", procedureName);
                    } else {
                        try {
                            stmt.execute(create);
                        } catch (SQLException e) {
                            final String sep = System.getProperty("line.separator");
                            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, new StringBuilder(256).append(e.getMessage()).append(sep).append("Affected statement:").append(sep).append(create).toString());
                        }
                    }
                }
            }
        } catch (final SQLException e) {
            // e.printStackTrace();
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static final Pattern PATTERN_CREATE_TABLE = Pattern.compile("CREATE +TABLE +`?(\\w+)`? +\\(");
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
     * Gets the CREATE-TABLE statements.
     *
     * @return The CREATE-TABLE statements
     */
    protected abstract String[] getCreateStatements();

    /**
     * The constant to signal no dependencies to other tables.
     */
    protected static final String[] NO_TABLES = new String[0];

}
