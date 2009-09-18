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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.UpdateExceptionFactory;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.update.Tools;

/**
 * Creates the table replicationMonitor and inserts 0 for every context.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@OXExceptionSource(classId = Classes.UPDATE_TASK, component = EnumComponent.UPDATE)
public final class CreateReplicationTableTask implements UpdateTask {

    private static final UpdateExceptionFactory EXCEPTION = new UpdateExceptionFactory(CorrectIndexes.class);

    public CreateReplicationTableTask() {
        super();
    }

    public int addedWithVersion() {
        return 90;
    }

    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    @OXThrows(category=Category.CODE_ERROR, desc="", exceptionId=1, msg="An SQL error occurred: %1$s.")
    public void perform(Schema schema, int contextId) throws AbstractOXException {
        DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class, true);
        final Connection con = dbService.getForUpdateTask(contextId);
        try {
            con.setAutoCommit(false);
            if (!Tools.tableExists(con, "replicationMonitor")) {
                createTable(con);
            }
            insertZeros(con, dbService.getContextsInSameSchema(contextId));
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw EXCEPTION.create(1, e, e.getMessage());
        } finally {
            autocommit(con);
            dbService.backForUpdateTask(contextId, con);
        }
    }

    private void insertZeros(Connection con, int[] ctxIds) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO replicationMonitor (cid, transaction) VALUES (?,0)");
            for (int ctxId : ctxIds) {
                if (!entryExists(con, ctxId)) {
                    stmt.setInt(1, ctxId);
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private boolean entryExists(Connection con, int ctxId) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT transaction FROM replicationMonitor WHERE cid=?");
            stmt.setInt(1, ctxId);
            result = stmt.executeQuery();
            return result.next();
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private void createTable(Connection con) throws SQLException {
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute("CREATE TABLE replicationMonitor (cid INT4 UNSIGNED NOT NULL, transaction INT8 NOT NULL, PRIMARY KEY (cid)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci");
        } finally {
            closeSQLStuff(stmt);
        }
    }
}
