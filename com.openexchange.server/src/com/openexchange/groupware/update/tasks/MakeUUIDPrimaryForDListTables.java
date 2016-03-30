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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
        Connection connection = Database.getNoTimeout(params.getContextId(), true);
        try {
            DBUtils.startTransaction(connection);
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
        } catch (SQLException e) {
            DBUtils.rollback(connection);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(connection);
            Database.backNoTimeout(params.getContextId(), true, connection);
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
