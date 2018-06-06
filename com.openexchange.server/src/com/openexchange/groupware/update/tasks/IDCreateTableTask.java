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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link IDCreateTableTask} - Inserts necessary tables.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class IDCreateTableTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link IDCreateTableTask}.
     */
    public IDCreateTableTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { };
    }

    private static final String getCreate() {
        return "CREATE TABLE sequenceIds ("
        + "cid INT4 UNSIGNED NOT NULL,"
        + "type VARCHAR(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,"
        + "id INT4 UNSIGNED NOT NULL,"
        + "PRIMARY KEY (cid, type)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        createTable("sequenceIds", getCreate(), params.getConnectionProvider().getConnection());
    }

    private static void createTable(String tablename, String sqlCreate, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            try {
                if (tableExists(tablename, con.getMetaData())) {
                    return;
                }
                stmt = con.prepareStatement(sqlCreate);
                stmt.executeUpdate();
            } catch (final SQLException e) {
                throw createSQLError(e);
            }
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * The object type "TABLE"
     */
    private static final String[] types = { "TABLE" };

    /**
     * Check a table's existence
     *
     * @param tableName The table name to check
     * @param dbmd The database's meta data
     * @return <code>true</code> if table exists; otherwise <code>false</code>
     * @throws SQLException If a SQL error occurs
     */
    private static boolean tableExists(final String tableName, final DatabaseMetaData dbmd) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = dbmd.getTables(null, null, tableName, types);
            return resultSet.next();
        } finally {
            closeSQLStuff(resultSet, null);
        }
    }

    private static OXException createSQLError(final SQLException e) {
        return UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
    }
}
