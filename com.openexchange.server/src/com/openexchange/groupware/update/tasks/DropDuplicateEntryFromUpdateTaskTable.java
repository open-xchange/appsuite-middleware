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

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.database.Databases.startTransaction;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * Drops duplicate entry from updateTask table.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DropDuplicateEntryFromUpdateTaskTable extends UpdateTaskAdapter {

    public DropDuplicateEntryFromUpdateTaskTable() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        boolean rb = false;
        try {
            startTransaction(con);
            rb = true;

            checkNamingForUnifiedMailRenamerTask(con);

            con.commit();
            rb = false;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rb) {
                rollback(con);
            }
            autocommit(con);
        }
    }

    private void checkNamingForUnifiedMailRenamerTask(final Connection con) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT 1 FROM updateTask WHERE BINARY taskName='com.openexchange.groupware.update.tasks.UnifiedInboxRenamerTask'");
            final boolean wrongEntryExists = rs.next();
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT 1 FROM updateTask WHERE BINARY taskName='com.openexchange.groupware.update.tasks.UnifiedINBOXRenamerTask'");
            final boolean correctEntryExists = rs.next();
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            if (correctEntryExists) {
                if (wrongEntryExists) {
                    // Duplicate entry
                    stmt = con.createStatement();
                    stmt.execute("DELETE FROM updateTask WHERE BINARY taskName='com.openexchange.groupware.update.tasks.UnifiedInboxRenamerTask'");
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }
            } else {
                if (wrongEntryExists) {
                    // Needs to be renamed to actual update task name
                    stmt = con.createStatement();
                    stmt.execute("UPDATE updateTask SET taskName='com.openexchange.groupware.update.tasks.UnifiedINBOXRenamerTask' WHERE BINARY taskName='com.openexchange.groupware.update.tasks.UnifiedInboxRenamerTask'");
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }
            }

        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { com.openexchange.groupware.update.tasks.DropFKTask.class.getName() };
    }

}
