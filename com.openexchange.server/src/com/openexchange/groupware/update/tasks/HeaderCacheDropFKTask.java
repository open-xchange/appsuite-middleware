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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Tools;

/**
 * {@link HeaderCacheDropFKTask} - Drops rather needless foreign keys from pop3 tables.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HeaderCacheDropFKTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link HeaderCacheDropFKTask}.
     */
    public HeaderCacheDropFKTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] {};
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        boolean doRollback = false;
        try {
            Databases.startTransaction(con);
            doRollback = true;

            /*
             * Drop foreign keys
             */
            {
                final List<String> tables = Arrays.asList("mailUUID", "headersAsBlob");
                for (final String table : tables) {
                    dropForeignKeysFrom(table, con);
                }
            }

            con.commit();
            doRollback = false;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (doRollback) {
                rollback(con);
            }
            autocommit(con);
        }
    }

    private void dropForeignKeysFrom(final String table, final Connection con) throws SQLException {
        if (Tools.tableExists(con, table)) {
            final List<String> keyNames = Tools.allForeignKey(con, table);
            Statement stmt = null;
            for (final String keyName : keyNames) {
                try {
                    stmt = con.createStatement();
                    stmt.execute("ALTER TABLE " + table + " DROP FOREIGN KEY " + keyName);
                } finally {
                    Databases.closeSQLStuff(null, stmt);
                }
            }
        }
    }

}
