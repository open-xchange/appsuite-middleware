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
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.sql.DBUtils;
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
        int cid = params.getContextId();
        Connection con = Database.getNoTimeout(cid, true);
        try {
            con.setAutoCommit(false);
            final String table = "del_dates_members";
            fillPfid(table, con);
            Column column = new Column("pfid", "INT(11) NOT NULL DEFAULT -2");
            Tools.modifyColumns(con, table, column);
            if (Tools.hasPrimaryKey(con, table)) {
                Tools.dropPrimaryKey(con, table);
            }
            Tools.createPrimaryKeyIfAbsent(con, table, new String[] { "cid", "object_id", "member_uid", "pfid" });
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
                    DBUtils.closeSQLStuff(stmt2);
                }
            }
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

}
