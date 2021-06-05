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
import java.util.UUID;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link PrgLinksAddPrimaryKeyUpdateTask}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class PrgLinksAddPrimaryKeyUpdateTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link PrgLinksAddPrimaryKeyUpdateTask}.
     */
    public PrgLinksAddPrimaryKeyUpdateTask() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            Column column = new Column("uuid", "BINARY(16) NOT NULL");
            Tools.modifyColumns(con, "prg_links", column);
            Tools.createPrimaryKeyIfAbsent(con, "prg_links", new String[] { "cid", column.name });
            setUUID(con);

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

    @Override
    public String[] getDependencies() {
        return new String[] { PrgLinksAddUuidUpdateTask.class.getName() };
    }

    private void setUUID(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        int oldPos, newPos;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT firstid, firstmodule, firstfolder, secondid, secondmodule, secondfolder, cid, last_modified, created_by FROM prg_links WHERE uuid IS NULL FOR UPDATE");
            rs = stmt.executeQuery();
            while (rs.next()) {
                PreparedStatement stmt2 = null;
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("UPDATE prg_links SET uuid = ? WHERE firstid ");
                    oldPos = 1;
                    int firstid = rs.getInt(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND firstmodule ");
                    int firstmodule = rs.getInt(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND firstfolder ");
                    int firstfolder = rs.getInt(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND secondid ");
                    int secondid = rs.getInt(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND secondmodule ");
                    int secondmodule = rs.getInt(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND secondfolder ");
                    int secondfolder = rs.getInt(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND cid ");
                    int cid = rs.getInt(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND last_modified ");
                    long lastModified = rs.getInt(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND created_by ");
                    int createdBy = rs.getInt(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    stmt2 = con.prepareStatement(sb.toString());
                    newPos = 1;
                    UUID uuid = UUID.randomUUID();
                    stmt2.setBytes(newPos++, UUIDs.toByteArray(uuid));
                    stmt2.setInt(newPos++, firstid);
                    stmt2.setInt(newPos++, firstmodule);
                    stmt2.setInt(newPos++, firstfolder);
                    stmt2.setInt(newPos++, secondid);
                    stmt2.setInt(newPos++, secondmodule);
                    stmt2.setInt(newPos++, secondfolder);
                    stmt2.setInt(newPos++, cid);
                    stmt2.setLong(newPos++, lastModified);
                    stmt2.setInt(newPos++, createdBy);
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
