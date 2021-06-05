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

import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.ProgressState;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * Adds UIDs to tasks.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class TasksAddUidColumnTask extends UpdateTaskAdapter {

    public TasksAddUidColumnTask() {
        super();
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        ProgressState progress = params.getProgressState();
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            progress.setTotal(getTotalRows(con));
            if (!Tools.columnExists(con, "task", "uid")) {
                Tools.addColumns(con, "task", new Column("uid", "VARCHAR(255)"));
                fillUIDs(con, "task", progress);
                Tools.modifyColumns(con, "task", new Column("uid", "VARCHAR(255) NOT NULL"));
            }
            if (!Tools.columnExists(con, "del_task", "uid")) {
                Tools.addColumns(con, "del_task", new Column("uid", "VARCHAR(255)"));
                fillUIDs(con, "del_task", progress);
                Tools.modifyColumns(con, "del_task", new Column("uid", "VARCHAR(255) NOT NULL"));
            }

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

    private static void fillUIDs(final Connection con, final String table, final ProgressState progress) throws SQLException {
        Statement stmt1 = null;
        ResultSet result = null;
        PreparedStatement stmt2 = null;
        try {
            stmt1 = con.createStatement();
            result = stmt1.executeQuery("SELECT cid,id FROM " + table);
            stmt2 = con.prepareStatement("UPDATE " + table + " SET uid=? WHERE cid=? AND id=?");
            while (result.next()) {
                final int cid = result.getInt(1);
                final int id = result.getInt(2);
                stmt2.setString(1, UUID.randomUUID().toString());
                stmt2.setInt(2, cid);
                stmt2.setInt(3, id);
                stmt2.addBatch();
                progress.incrementState();
            }
            stmt2.executeBatch();
        } finally {
            closeSQLStuff(result, stmt1);
            closeSQLStuff(stmt2);
        }
    }

    private static int getTotalRows(final Connection con) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        int rows = 0;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(id) FROM task UNION SELECT COUNT(id) FROM del_task");
            while (rs.next()) {
                rows += rs.getInt(1);
            }
        } finally {
            closeSQLStuff(rs, stmt);
        }
        return rows;
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }
}
