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

package com.openexchange.groupware.update.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.sql.DBUtils;
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

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.groupware.update.UpdateTaskV2#perform(com.openexchange.groupware.update.PerformParameters)
     */
    @Override
    public void perform(PerformParameters params) throws OXException {
        int cid = params.getContextId();
        Connection con = Database.getNoTimeout(cid, true);
        Column column = new Column("uuid", "BINARY(16) DEFAULT NULL");
        try {
            con.setAutoCommit(false);
            if (!Tools.columnExists(con, "user_setting_server", column.name)) {
                Tools.checkAndAddColumns(con, "user_setting_server", column);
            }
            setUUID(con);
            con.commit();
        } catch (SQLException e) {
            DBUtils.rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            DBUtils.rollback(con);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            Database.backNoTimeout(cid, true, con);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.groupware.update.UpdateTaskV2#getDependencies()
     */
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
                    DBUtils.closeSQLStuff(stmt2);
                }
            }
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

}
