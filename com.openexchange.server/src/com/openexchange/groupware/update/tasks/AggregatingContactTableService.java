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

package com.openexchange.groupware.update.tasks;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.AbstractCreateTableImpl;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;

/**
 * Note: This one does not use foreign key constraints on prg_contact.userfield20,
 * because it might be used for something else later on.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class AggregatingContactTableService extends AbstractCreateTableImpl implements UpdateTaskV2 {

    private static final String AGGREGATING_CONTACTS = "aggregatingContacts";

    private String getTableSQL() {
        return "CREATE TABLE "+AGGREGATING_CONTACTS+" (" +
                    "contributor BINARY(16) NOT NULL, " +
        		    "aggregator BINARY(16) NOT NULL," +
        		    "state TINYINT NOT NULL" +
        		") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { getTableSQL() };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes();
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        createTable(AGGREGATING_CONTACTS, getTableSQL(), params);
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AggregatingContactTableService.class);
        logger.info("UpdateTask ''{}'' successfully performed!", AggregatingContactTableService.class.getSimpleName());
    }

    @Override
    public String[] requiredTables() {
        return new String[] {};
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { AGGREGATING_CONTACTS };
    }

    private void createTable(String tablename, String sqlCreate, PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        PreparedStatement stmt = null;
        try {
            if (tableExists(con, tablename)) {
                return;
            }
            stmt = con.prepareStatement(sqlCreate);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private boolean tableExists(Connection con, String table) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        try {
            rs = metaData.getTables(null, null, table, new String[] { "TABLE" });
            return (rs.next() && rs.getString("TABLE_NAME").equals(table));
        } finally {
            Databases.closeSQLStuff(rs);
        }
    }

}
