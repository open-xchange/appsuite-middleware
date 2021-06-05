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
import static com.openexchange.database.Databases.startTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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
 * {@link AddUUIDForDListTables}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class AddUUIDForDListTables extends UpdateTaskAdapter {

    private static final String TABLE = "prg_dlist";

    private static final String DEL_TABLE = "del_dlist";

    private static final String NULL = " IS NULL ";

    private static final String EQUALS = " = ? ";

    @Override
    public void perform(PerformParameters params) throws OXException {
        ProgressState progress = params.getProgressState();
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            startTransaction(con);
            rollback = 1;

            progress.setTotal(getTotalRows(con));
            Tools.checkAndAddColumns(con, TABLE, new Column("uuid", "BINARY(16) DEFAULT NULL"));
            fillUUIDs(con, TABLE, progress);

            Tools.checkAndAddColumns(con, DEL_TABLE, new Column("uuid", "BINARY(16) DEFAULT NULL"));
            fillUUIDs(con, DEL_TABLE, progress);

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

    public static void fillUUIDs(Connection con, String table, ProgressState progress) throws SQLException {
        Statement select = null;
        ResultSet result = null;
        try {
            select = con.createStatement();
            result = select.executeQuery("SELECT intfield01, intfield02, intfield03, intfield04, field01, field02, field03, cid FROM " + table + " WHERE uuid IS NULL");
            while (result.next()) {
                String update = "UPDATE " + table + " SET uuid=? WHERE uuid IS NULL ";
                List<Object> values = new ArrayList<Object>();
                values.add(UUIDs.toByteArray(UUID.randomUUID()));

                update += "AND intfield01";
                int intfield01 = result.getInt("intfield01");
                if (result.wasNull()) {
                    update += NULL;
                } else {
                    update += EQUALS;
                    values.add(Integer.valueOf(intfield01));
                }

                update += "AND intfield02";
                int intfield02 = result.getInt("intfield02");
                if (result.wasNull()) {
                    update += NULL;
                } else {
                    update += EQUALS;
                    values.add(Integer.valueOf(intfield02));
                }

                update += "AND intfield03";
                int intfield03 = result.getInt("intfield03");
                if (result.wasNull()) {
                    update += NULL;
                } else {
                    update += EQUALS;
                    values.add(Integer.valueOf(intfield03));
                }

                update += "AND intfield04";
                int intfield04 = result.getInt("intfield04");
                if (result.wasNull()) {
                    update += NULL;
                } else {
                    update += EQUALS;
                    values.add(Integer.valueOf(intfield04));
                }

                update += "AND field01";
                String field01 = result.getString("field01");
                if (result.wasNull()) {
                    update += NULL;
                } else {
                    update += EQUALS;
                    values.add(field01);
                }

                update += "AND field02";
                String field02 = result.getString("field02");
                if (result.wasNull()) {
                    update += NULL;
                } else {
                    update += EQUALS;
                    values.add(field02);
                }

                update += "AND field03";
                String field03 = result.getString("field03");
                if (result.wasNull()) {
                    update += NULL;
                } else {
                    update += EQUALS;
                    values.add(field03);
                }

                update += "AND cid";
                int cid = result.getInt("cid");
                if (result.wasNull()) {
                    update += NULL;
                } else {
                    update += EQUALS;
                    values.add(Integer.valueOf(cid));
                }

                update += " LIMIT 1";

                PreparedStatement upd = null;
                try {
                    upd = con.prepareStatement(update);
                    for (int i = 0; i < values.size(); i++) {
                        upd.setObject(i + 1, values.get(i));
                    }
                    int increment = upd.executeUpdate();
                    for (int i = increment; i-- > 0;) {
                        progress.incrementState();
                    }
                } finally {
                    closeSQLStuff(upd);
                }
            }
        } finally {
            closeSQLStuff(result, select);
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
            rs = stmt.executeQuery("SELECT COUNT(*) FROM " + TABLE);
            while (rs.next()) {
                rows += rs.getInt(1);
            }

            stmt2 = con.createStatement();
            rs2 = stmt2.executeQuery("SELECT COUNT(*) FROM " + DEL_TABLE);
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
        return new String[] { "com.openexchange.groupware.update.tasks.DListAddIndexForLookup" };
    }

}
