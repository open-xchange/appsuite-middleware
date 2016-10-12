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
        boolean autocommit = false;
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            autocommit = true;
            rollback = true;

            for (DatabaseTable databaseTable : tables) {
                if (false == Databases.tableExists(con, databaseTable.getTableName())) {
                    createTable(con, databaseTable.getCreateTableStatement());
                }
            }

            con.commit();
            rollback = false;
        } catch (SQLException e) {
            throw FileStorageCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            if (autocommit) {
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
