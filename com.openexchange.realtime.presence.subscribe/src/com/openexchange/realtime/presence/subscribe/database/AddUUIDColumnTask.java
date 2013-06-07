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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.realtime.presence.subscribe.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link AddUUIDColumnTask}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AddUUIDColumnTask extends UpdateTaskAdapter {

    private final DatabaseService dbService;

    /**
     * Initializes a new {@link AddUUIDColumnTask}.
     */
    public AddUUIDColumnTask(DatabaseService dbService) {
        super();
        this.dbService = dbService;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.update.UpdateTaskV2#perform(com.openexchange.groupware.update.PerformParameters)
     */
    @Override
    public void perform(PerformParameters params) throws OXException {
        int cid = params.getContextId();
        Connection con = dbService.getForUpdateTask(cid);
        Column column = new Column("uuid", "BINARY(16) DEFAULT NULL");
        try {
            con.setAutoCommit(false);
            Tools.checkAndAddColumns(con, "presenceSubscriptions", column);
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
            dbService.backForUpdateTask(cid, con);
        }
    }

    /*
     * (non-Javadoc)
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
            stmt = con.prepareStatement("SELECT fromCid, fromUserId, fromId, toCid, toUserId, status, request, timestamp FROM presenceSubscriptions FOR UPDATE");
            rs = stmt.executeQuery();
            PreparedStatement stmt2 = null;
            try {
                while (rs.next()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("UPDATE presenceSubscriptions SET uuid = ? WHERE fromCid ");
                    oldPos = 1;
                    int fromCid = rs.getInt(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND fromUserId ");
                    int fromUserId = rs.getInt(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND fromId ");
                    String fromId = rs.getString(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND toCid ");
                    int toCid = rs.getInt(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND toUserId ");
                    int toUserId = rs.getInt(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND status ");
                    String status = rs.getString(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND request ");
                    String request = rs.getString(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    sb.append("AND timestamp ");
                    Timestamp timestamp = rs.getTimestamp(oldPos++);
                    if (rs.wasNull()) {
                        sb.append("IS ? ");
                    } else {
                        sb.append("= ? ");
                    }
                    stmt2 = con.prepareStatement(sb.toString());
                    newPos = 1;
                    UUID uuid = UUID.randomUUID();
                    stmt2.setBytes(newPos++, UUIDs.toByteArray(uuid));
                    stmt2.setInt(newPos++, fromCid);
                    stmt2.setInt(newPos++, fromUserId);
                    stmt2.setString(newPos++, fromId);
                    stmt2.setInt(newPos++, toCid);
                    stmt2.setInt(newPos++, toUserId);
                    stmt2.setString(newPos++, status);
                    stmt2.setString(newPos++, request);
                    stmt2.setTimestamp(newPos++, timestamp);
                    stmt2.execute();
                }
            } finally {
                DBUtils.closeSQLStuff(stmt2);
            }
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

}
