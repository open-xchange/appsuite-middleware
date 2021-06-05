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

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.startTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.util.UUIDs;

/**
 * {@link RemoveDuplicatesFromUpdateTaskTable} - Removes possible duplicates from the <code>updateTask</code> table
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class RemoveDuplicatesFromUpdateTaskTable extends UpdateTaskAdapter {

    /**
     * Initialises a new {@link RemoveDuplicatesFromUpdateTaskTable}.
     */
    public RemoveDuplicatesFromUpdateTaskTable() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        boolean rollback = true;
        try {
            startTransaction(con);
            removeDuplicates(con);
            con.commit();
            rollback = false;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            autocommit(con);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { AddUUIDForUpdateTaskTable.class.getName() };
    }

    /**
     * Removes any duplicate entries from the <code>updateTask</code> table.
     *
     * @param connection the {@link Connection} to use
     * @throws SQLException if an SQL error is occurred.
     */
    private void removeDuplicates(Connection connection) throws SQLException {
        PreparedStatement selectDuplicates = null;
        ResultSet rs = null;
        try {
            selectDuplicates = connection.prepareStatement("SELECT taskName, COUNT(*) c FROM updateTask GROUP BY taskName HAVING c > 1;");
            rs = selectDuplicates.executeQuery();
            if (false == rs.next()) {
                // No duplicates found
                return;
            }

            List<String> names = new LinkedList<>();
            do {
                names.add(rs.getString(1));
            } while (rs.next());
            Databases.closeSQLStuff(rs, selectDuplicates);
            rs = null;
            selectDuplicates = null;

            for (String duplicateTaskName : names) {
                handleDuplicateName(duplicateTaskName, connection);
            }
        } finally {
            Databases.closeSQLStuff(rs, selectDuplicates);
        }
    }

    private void handleDuplicateName(String duplicateTaskName, Connection connection) throws SQLException {
        PreparedStatement selectStatement = null;
        ResultSet duplicatesResultSet = null;
        try {
            selectStatement = connection.prepareStatement("SELECT lastModified,uuid FROM updateTask WHERE taskName=? ORDER BY lastModified DESC LIMIT 1;");
            selectStatement.setString(1, duplicateTaskName);
            duplicatesResultSet = selectStatement.executeQuery();
            if (duplicatesResultSet.next()) {
                removeDuplicates(connection, duplicateTaskName, UUIDs.toUUID(duplicatesResultSet.getBytes(2)));
            }
        } finally {
            Databases.closeSQLStuff(duplicatesResultSet, selectStatement);
        }
    }

    /**
     * Removes all duplicate entries from the <code>updateTask</code> table except the update task with the specified UUID.
     *
     * @param connection The {@link Connection}
     * @param taskName The task's name
     * @param retained The UUID of the update task to retain
     * @throws SQLException
     */
    private void removeDuplicates(Connection connection, String taskName, UUID retained) throws SQLException {
        PreparedStatement delete = null;
        try {
            delete = connection.prepareStatement("DELETE FROM updateTask WHERE taskName=? AND uuid != ?;");
            delete.setString(1, taskName);
            delete.setBytes(2, UUIDs.toByteArray(retained));
            delete.executeUpdate();
        } finally {
            Databases.closeSQLStuff(delete);
        }
    }
}
