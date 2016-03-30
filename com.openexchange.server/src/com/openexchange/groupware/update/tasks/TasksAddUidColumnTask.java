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

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import static com.openexchange.tools.sql.DBUtils.startTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.ProgressState;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.sql.DBUtils;
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
        final int ctxId = params.getContextId();
        final ProgressState progress = params.getProgressState();
        final Connection con = Database.getNoTimeout(ctxId, true);
        try {
            startTransaction(con);
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
        } catch (SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            Database.backNoTimeout(ctxId, true, con);
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
