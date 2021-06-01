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
 * {@link UserSettingServerAddUuidUpdateTask}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class UserSettingServerAddUuidUpdateTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link UserSettingServerAddUuidUpdateTask}.
     */
    public UserSettingServerAddUuidUpdateTask() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            Column column = new Column("uuid", "BINARY(16) DEFAULT NULL");
            if (!Tools.columnExists(con, "user_setting_server", column.name)) {
                Tools.checkAndAddColumns(con, "user_setting_server", column);
            }
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
        return new String[0];
    }

    private void setUUID(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        int oldPos, newPos;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, user, contact_collect_folder, contact_collect_enabled, defaultStatusPrivate, defaultStatusPublic, contactCollectOnMailTransport, contactCollectOnMailAccess, folderTree FROM user_setting_server FOR UPDATE");
            rs = stmt.executeQuery();
            while (rs.next()) {
                PreparedStatement stmt2 = null;
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("UPDATE user_setting_server SET uuid = ? WHERE cid ");
                    oldPos = 1;
                    int cid = rs.getInt(oldPos++);
                    boolean cidNull = rs.wasNull();
                    if (cidNull) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND user ");
                    int user = rs.getInt(oldPos++);
                    boolean userNull = rs.wasNull();
                    if (userNull) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND contact_collect_folder ");
                    int contactCollectFolder = rs.getInt(oldPos++);
                    boolean contactCollectFolderNull = rs.wasNull();
                    if (contactCollectFolderNull) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND contact_collect_enabled ");
                    boolean contactCollectEnabled = rs.getBoolean(oldPos++);
                    boolean contactCollectEnabledNull = rs.wasNull();
                    if (contactCollectEnabledNull) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND defaultStatusPrivate ");
                    int defaultStatusPrivate = rs.getInt(oldPos++);
                    boolean defaultStatusPrivateNull = rs.wasNull();
                    if (defaultStatusPrivateNull) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND defaultStatusPublic ");
                    int defaultStatusPublic = rs.getInt(oldPos++);
                    boolean defaultStatusPublicNull = rs.wasNull();
                    if (defaultStatusPublicNull) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND contactCollectOnMailTransport ");
                    boolean contactCollectOnMailTransport = rs.getBoolean(oldPos++);
                    boolean contactCollectOnMailTransportNull = rs.wasNull();
                    if (contactCollectOnMailTransportNull) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND contactCollectOnMailAccess ");
                    boolean contactCollectOnMailAccess = rs.getBoolean(oldPos++);
                    boolean contactCollectOnMailAccessNull = rs.wasNull();
                    if (contactCollectOnMailAccessNull) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND folderTree ");
                    int folderTree = rs.getInt(oldPos++);
                    boolean folderTreeNull = rs.wasNull();
                    if (folderTreeNull) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    stmt2 = con.prepareStatement(sb.toString());
                    newPos = 1;
                    UUID uuid = UUID.randomUUID();
                    stmt2.setBytes(newPos++, UUIDs.toByteArray(uuid));
                    if (!cidNull) {
                        stmt2.setInt(newPos++, cid);
                    } else {
                        stmt2.setNull(newPos++, Types.INTEGER);
                    }
                    if (!userNull) {
                        stmt2.setInt(newPos++, user);
                    } else {
                        stmt2.setNull(newPos++, Types.INTEGER);
                    }
                    if (!contactCollectFolderNull) {
                        stmt2.setInt(newPos++, contactCollectFolder);
                    } else {
                        stmt2.setNull(newPos++, Types.INTEGER);
                    }
                    if (!contactCollectEnabledNull) {
                        stmt2.setBoolean(newPos++, contactCollectEnabled);
                    } else {
                        stmt2.setNull(newPos++, Types.BOOLEAN);
                    }
                    if (!defaultStatusPrivateNull) {
                        stmt2.setInt(newPos++, defaultStatusPrivate);
                    } else {
                        stmt2.setNull(newPos++, Types.INTEGER);
                    }
                    if (!defaultStatusPublicNull) {
                        stmt2.setInt(newPos++, defaultStatusPublic);
                    } else {
                        stmt2.setNull(newPos++, Types.INTEGER);
                    }
                    if (!contactCollectOnMailTransportNull) {
                        stmt2.setBoolean(newPos++, contactCollectOnMailTransport);
                    } else {
                        stmt2.setNull(newPos++, Types.BOOLEAN);
                    }
                    if (!contactCollectOnMailAccessNull) {
                        stmt2.setBoolean(newPos++, contactCollectOnMailAccess);
                    } else {
                        stmt2.setNull(newPos++, Types.BOOLEAN);
                    }
                    if (!folderTreeNull) {
                        stmt2.setInt(newPos++, folderTree);
                    } else {
                        stmt2.setNull(newPos++, Types.INTEGER);
                    }
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
