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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link DelDatesMembersPrimaryKeyUpdateTask}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class DelDatesMembersPrimaryKeyUpdateTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link DelDatesMembersPrimaryKeyUpdateTask}.
     */
    public DelDatesMembersPrimaryKeyUpdateTask() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            if (false == Databases.tableExists(con, "del_dates_members")) {
                return;
            }
            con.setAutoCommit(false);
            rollback = 1;

            final String table = "del_dates_members";
            fillPfid(table, con);
            Column column = new Column("pfid", "INT(11) NOT NULL DEFAULT -2");
            Tools.modifyColumns(con, table, column);
            if (Tools.hasPrimaryKey(con, table)) {
                Tools.dropPrimaryKey(con, table);
            }
            Tools.createPrimaryKeyIfAbsent(con, table, new String[] { "cid", "object_id", "member_uid", "pfid" });

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    private void fillPfid(final String table, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        int oldPos, newPos;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT object_id, member_uid, confirm, reason, reminder, cid FROM " + table + " WHERE pfid IS NULL FOR UPDATE");
            rs = stmt.executeQuery();
            while (rs.next()) {
                PreparedStatement stmt2 = null;
                try {
                    oldPos = 1;
                    StringBuilder sb = new StringBuilder();
                    int objectId = rs.getInt(oldPos++);
                    sb.append("UPDATE " + table + " SET pfid = -2 WHERE object_id = ? ");
                    int memberUid = rs.getInt(oldPos++);
                    sb.append("AND member_uid = ? ");
                    int confirm = rs.getInt(oldPos++);
                    sb.append("AND confirm = ? ");
                    String reason = rs.getString(oldPos++);
                    boolean reasonNull = rs.wasNull();
                    if (reasonNull) {
                        sb.append("AND reason IS ? ");
                    } else {
                        sb.append("AND reason = ? ");
                    }
                    int reminder = rs.getInt(oldPos++);
                    boolean reminderNull = rs.wasNull();
                    if (reminderNull) {
                        sb.append("AND reminder IS ? ");
                    } else {
                        sb.append("AND reminder = ? ");
                    }
                    int cid = rs.getInt(oldPos++);
                    sb.append("AND cid = ?");
                    stmt2 = con.prepareStatement(sb.toString());
                    newPos = 1;
                    stmt2.setInt(newPos++, objectId);
                    stmt2.setInt(newPos++, memberUid);
                    stmt2.setInt(newPos++, confirm);
                    if (reasonNull) {
                        stmt2.setNull(newPos++, Types.CHAR);
                    } else {
                        stmt2.setString(newPos++, reason);
                    }
                    if (reminderNull) {
                        stmt2.setNull(newPos++, Types.INTEGER);
                    } else {
                        stmt2.setInt(newPos++, reminder);
                    }
                    stmt2.setInt(newPos++, cid);
                    stmt2.execute();
                } finally {
                    Databases.closeSQLStuff(stmt2);
                }
            }
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

}
