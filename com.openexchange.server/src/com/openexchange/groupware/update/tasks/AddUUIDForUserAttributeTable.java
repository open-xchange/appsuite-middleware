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

import static com.openexchange.tools.sql.DBUtils.autocommit;
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
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link AddUUIDForUserAttributeTable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AddUUIDForUserAttributeTable extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link AddUUIDForUserAttributeTable}.
     */
    public AddUUIDForUserAttributeTable() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        int ctxId = params.getContextId();
        ProgressState progress = params.getProgressState();
        Connection con = Database.getNoTimeout(ctxId, true);
        try {
            startTransaction(con);
            progress.setTotal(getTotalRows(con));
            if (!Tools.columnExists(con, "user_attribute", "uuid")) {
                Tools.addColumns(con, "user_attribute", new Column("uuid", "BINARY(16) DEFAULT NULL"));
                fillUUIDs(con, progress);
            }
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            rollback(con);
            throw UpdateExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.backNoTimeout(ctxId, true, con);
        }
    }

    public static void fillUUIDs(Connection con, ProgressState progress) throws SQLException {
        Statement stmt1 = null;
        ResultSet result = null;
        PreparedStatement stmt2 = null;
        try {
            stmt1 = con.createStatement();
            result = stmt1.executeQuery("SELECT cid, id, name, value FROM user_attribute WHERE uuid IS NULL");
            stmt2 = con.prepareStatement("UPDATE user_attribute SET uuid=? WHERE cid=? AND id=? AND name=? AND value=?");
            while (result.next()) {
                // Read columns
                int cid = result.getInt(1);
                int userId = result.getInt(2);
                String name = result.getString(3);
                String value = result.getString(4);
                // Set update statement
                stmt2.setBytes(1, UUIDs.toByteArray(UUID.randomUUID()));
                stmt2.setInt(2, cid);
                stmt2.setInt(3, userId);
                stmt2.setString(4, name);
                stmt2.setString(5, value);
                stmt2.addBatch();
                progress.incrementState();
            }
            stmt2.executeBatch();
        } finally {
            closeSQLStuff(result, stmt1);
            closeSQLStuff(stmt2);
        }
    }

    private static int getTotalRows(Connection con) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        int rows = 0;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM user_attribute");
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
