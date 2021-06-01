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
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.database.Databases.startTransaction;
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
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link AddUUIDForInfostoreReservedPaths}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class AddUUIDForInfostoreReservedPaths extends UpdateTaskAdapter {

    private static final String TABLE = "infostoreReservedPaths";

    @Override
    public void perform(PerformParameters params) throws OXException {
        ProgressState progress = params.getProgressState();
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            startTransaction(con);
            rollback = 1;

            progress.setTotal(getTotalRows(con));
            if (!Tools.columnExists(con, TABLE, "uuid")) {
                Tools.addColumns(con, TABLE, new Column("uuid", "BINARY(16) DEFAULT NULL"));
                fillUUIDs(con, TABLE, progress);
            }

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (1==rollback) {
                    rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

    public static void fillUUIDs(Connection con, String table, ProgressState progress) throws SQLException {
        Statement stmt1 = null;
        ResultSet result = null;
        PreparedStatement stmt2 = null;
        try {
            stmt1 = con.createStatement();
            result = stmt1.executeQuery("SELECT cid, folder, name FROM " + table + " WHERE uuid IS NULL");
            stmt2 = con.prepareStatement("UPDATE " + table + " SET uuid=? WHERE cid=? AND folder=? AND name=?");
            while (result.next()) {
                int cid = result.getInt(1);
                int folder = result.getInt(2);
                String name = result.getString(3);
                stmt2.setBytes(1, UUIDs.toByteArray(UUID.randomUUID()));
                stmt2.setInt(2, cid);
                stmt2.setInt(3, folder);
                stmt2.setString(4, name);
                stmt2.addBatch();
                progress.incrementState();
            }
            stmt2.executeBatch();
        } finally {
            closeSQLStuff(result, stmt1);
            closeSQLStuff(stmt2);
        }
    }

    private static int getTotalRows(Connection con) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        int rows = 0;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM " + TABLE);
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
