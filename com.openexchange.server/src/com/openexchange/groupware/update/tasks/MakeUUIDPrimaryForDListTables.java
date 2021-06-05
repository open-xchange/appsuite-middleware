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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.ProgressState;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link MakeUUIDPrimaryForDListTables}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class MakeUUIDPrimaryForDListTables extends UpdateTaskAdapter {

    protected static final String TABLE = "prg_dlist";

    protected static final String DEL_TABLE = "del_dlist";

    protected static final String COLUMN = "uuid";

    @Override
    public void perform(PerformParameters params) throws OXException {
        ProgressState progress = params.getProgressState();
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            Databases.startTransaction(connection);
            rollback = 1;

            progress.setTotal(getTotalRows(connection));
            if (!Tools.columnExists(connection, TABLE, COLUMN)) {
                throw UpdateExceptionCodes.COLUMN_NOT_FOUND.create(COLUMN, TABLE);
            }
            if (!Tools.columnExists(connection, DEL_TABLE, COLUMN)) {
                throw UpdateExceptionCodes.COLUMN_NOT_FOUND.create(COLUMN, DEL_TABLE);
            }

            AddUUIDForDListTables.fillUUIDs(connection, TABLE, progress);
            AddUUIDForDListTables.fillUUIDs(connection, DEL_TABLE, progress);

            Tools.modifyColumns(connection, TABLE, new Column(COLUMN, "BINARY(16) NOT NULL"));
            Tools.createPrimaryKeyIfAbsent(connection, TABLE, new String[] { COLUMN, "cid", "intfield01" });
            Tools.modifyColumns(connection, DEL_TABLE, new Column(COLUMN, "BINARY(16) NOT NULL"));
            Tools.createPrimaryKeyIfAbsent(connection, DEL_TABLE, new String[] { COLUMN, "cid", "intfield01" });

            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
            }
        }
    }

    private int getTotalRows(Connection con) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        Statement stmt2 = null;
        ResultSet rs2 = null;
        int rows = 0;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM " + TABLE + " WHERE uuid IS NULL");
            while (rs.next()) {
                rows += rs.getInt(1);
            }
            stmt2 = con.createStatement();
            rs2 = stmt.executeQuery("SELECT COUNT(*) FROM " + DEL_TABLE + " WHERE uuid IS NULL");
            while (rs2.next()) {
                rows += rs2.getInt(1);
            }
        } finally {
            closeSQLStuff(rs, stmt);
            closeSQLStuff(rs2, stmt2);
        }
        return rows;
    }

    @Override
    public String[] getDependencies() {
        return new String[] { "com.openexchange.groupware.update.tasks.AddUUIDForDListTables" };
    }

}
