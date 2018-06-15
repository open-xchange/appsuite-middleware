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

package com.openexchange.rest.services.database.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.rest.services.database.osgi.Services;

/**
 * {@link SetDBEngineUpdateTask}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SetDBEngineUpdateTask extends UpdateTaskAdapter {

    private static final String[] AFFECTED_TABLES = { "serviceSchemaVersion", "serviceSchemaMigrationLock" };

    /**
     * Initialises a new {@link SetDBEngineUpdateTask}.
     */
    public SetDBEngineUpdateTask() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTaskV2#perform(com.openexchange.groupware.update.PerformParameters)
     */
    @Override
    public void perform(PerformParameters params) throws OXException {
        String schemaName = params.getSchema().getSchema();
        int contextId = params.getContextId();
        DatabaseService databaseService = Services.getService(DatabaseService.class);
        Connection connection = databaseService.getForUpdateTask(contextId);
        try {
            for (String tableName : AFFECTED_TABLES) {
                if (isEngineMyISAM(schemaName, tableName, connection)) {
                    changeEngine(tableName, connection);
                }
            }
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            databaseService.backForUpdateTask(contextId, connection);
        }
    }

    /**
     * Checks whether the DB engine is MyISAM
     * 
     * @param schemaName The schema name
     * @param tableName The table name
     * @param connection The {@link Connection}
     * @return <code>true</code> if the engine of the specified table
     *         in the specified schema is of 'MyISAM'; <code>false</code> otherwise
     * @throws SQLException if an SQL error is occurred
     */
    private boolean isEngineMyISAM(String schemaName, String tableName, Connection connection) throws SQLException {
        ResultSet rs = null;
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA=? AND TABLE_NAME=?");
            statement.setString(1, schemaName);
            statement.setString(2, tableName);
            rs = statement.executeQuery();
            if (rs.next()) {
                String engine = rs.getString(1);
                return engine != null && engine.equalsIgnoreCase("myisam");
            }
            return false;
        } finally {
            Databases.closeSQLStuff(statement, rs);
        }
    }

    /**
     * Changes the database engine of the specified table to 'InnoDB'
     * 
     * @param tableName The table's name
     * @param connection The {@link Connection} to use
     * @throws SQLException if an SQL error is occurred
     */
    private void changeEngine(String tableName, Connection connection) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("ALTER TABLE " + tableName + " ENGINE=InnoDB");
            statement.execute();
        } finally {
            Databases.closeSQLStuff(statement);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTaskV2#getDependencies()
     */
    @Override
    public String[] getDependencies() {
        return new String[0];
    }
}
