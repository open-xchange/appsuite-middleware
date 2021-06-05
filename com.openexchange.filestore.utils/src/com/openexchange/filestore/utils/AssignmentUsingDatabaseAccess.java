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

package com.openexchange.filestore.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.Assignment;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.DatabaseAccess;
import com.openexchange.filestore.DatabaseTable;
import com.openexchange.filestore.FileStorageCodes;

/**
 * {@link AssignmentUsingDatabaseAccess} - The connection access backed by a concrete {@link Assignment assignment}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class AssignmentUsingDatabaseAccess implements DatabaseAccess {

    private final Assignment assignment;
    private final DatabaseService databaseService;

    /**
     * Initializes a new {@link AssignmentUsingDatabaseAccess}.
     *
     * @param assignment The database assignment
     * @param databaseService The database service
     */
    public AssignmentUsingDatabaseAccess(Assignment assignment, DatabaseService databaseService) {
        super();
        this.assignment = assignment;
        this.databaseService = databaseService;
    }

    /**
     * Gets the assignment
     *
     * @return The assignment
     */
    public Assignment getAssignment() {
        return assignment;
    }

    @Override
    public void createIfAbsent(DatabaseTable... tables) throws OXException {
        Connection con = databaseService.getWritable(assignment, false);
        int rollback = 0;
        try {
            Databases.startTransaction(con);
            rollback = 1;

            for (DatabaseTable databaseTable : tables) {
                if (false == Databases.tableExists(con, databaseTable.getTableName())) {
                    createTable(con, databaseTable.getCreateTableStatement());
                }
            }

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw FileStorageCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
            Databases.close(con);
        }
    }

    /**
     * Executes given <code>CREATE TABLE</code> statement.
     *
     * @param con The connection to use
     * @param createTableStatement The <code>CREATE TABLE</code> statement to execute
     * @throws SQLException If executing given <code>CREATE TABLE</code> statement fails
     */
    private void createTable(Connection con, String createTableStatement) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(createTableStatement);
            stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public Connection acquireReadOnly() throws OXException {
        return databaseService.getReadOnly(assignment, false);
    }

    @Override
    public void releaseReadOnly(Connection con) {
        if (null != con) {
            Databases.close(con);
        }
    }

    @Override
    public Connection acquireWritable() throws OXException {
        return databaseService.getWritable(assignment, false);
    }

    @Override
    public void releaseWritable(Connection con, boolean forReading) {
        if (null != con) {
            Databases.close(con);
        }
    }

}
