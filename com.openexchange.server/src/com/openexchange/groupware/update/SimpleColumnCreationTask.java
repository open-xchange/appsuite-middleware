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

package com.openexchange.groupware.update;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link SimpleColumnCreationTask} -  A simple abstract class for such update tasks that intend adding a column to one or more tables.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 */
public abstract class SimpleColumnCreationTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link SimpleColumnCreationTask}.
     */
    protected SimpleColumnCreationTask() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            String columnName = getColumnName();
            Column columnToAdd = new Column(columnName, getColumnDefinition());
            for (String table : getTableNames()) {
                if (false == Tools.columnExists(con, table, columnName)) {
                    if (0 == rollback) {
                        // Transaction not yet started
                        con.setAutoCommit(false);
                        rollback = 1;
                    }

                    Tools.addColumns(con, table, columnToAdd);
                }
            }

            if (rollback == 1) {
                // Transaction has been started
                con.commit();
                rollback = 2;
            }
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

    /**
     * Gets the names of the tables, which are supposed to be extended by a column
     *
     * @return The table names (never <code>null</code>)
     */
    protected abstract String[] getTableNames();

    /**
     * Gets the name of the column, which should be added.
     *
     * @return The column name
     */
    protected abstract String getColumnName();

    /**
     * Gets the definition of the column, which should be added; e.g. <code>"INT4 UNSIGNED NOT NULL DEFAULT 0"</code>
     *
     * @return The column definition
     */
    protected abstract String getColumnDefinition();

}
