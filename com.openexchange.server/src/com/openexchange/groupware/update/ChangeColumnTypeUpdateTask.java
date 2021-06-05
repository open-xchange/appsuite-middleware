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
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;


/**
 * A {@link ChangeColumnTypeUpdateTask} is a utility class for writing update tasks that want to modify a column type.
 * The UpdateTask checks the current column type against the given sqlType (according to java.sql.Types) and, if the
 * type does not match, issues an alter table statement to change the column type.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class ChangeColumnTypeUpdateTask implements UpdateTaskV2 {

    private final DatabaseService dbService;
    private final String tableName;
    private final Column column;

    /**
     *
     * Initializes a new {@link ChangeColumnTypeUpdateTask}.
     * @param tableName The name of the table that may have to be modified
     * @param columnName The name of the column that may have to be modified
     * @param newType The column definition
     * @param sqlType The type the column should be changed into according to java.sql.Types
     * @param getDatabaseService() A database service to access the database
     */
    public ChangeColumnTypeUpdateTask(DatabaseService dbService, String tableName, String columnName, String newType){
        this.column = new Column(columnName, newType);
        this.tableName = tableName;
        this.dbService = dbService;
    }


    @Override
    public TaskAttributes getAttributes() {
        return new Attributes();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            if (correctType(con)) {
                return;
            }

            con.setAutoCommit(false);
            rollback = 1;

            before(con);
            changeType(con);
            after(con);

            con.commit();
            rollback = 2;
        } catch (SQLException x) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(x.getMessage(), x);
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

    public DatabaseService getDatabaseService() {
        return dbService;
    }

    @SuppressWarnings("unused")
    protected void before(Connection con) throws SQLException {
        // May be overridden
    }

    @SuppressWarnings("unused")
    protected void after(Connection con) throws SQLException {
        // May be overridden
    }

    protected void changeType(Connection con) throws SQLException {
        Tools.modifyColumns(con, tableName, modifyColumn(column));
    }

    protected Column modifyColumn(Column c) {
        return c;
    }


    protected boolean correctType(Connection con) throws OXException, SQLException {
        String name = Tools.getColumnTypeName(con, tableName, column.getName());
        if (name == null) {
            throw UpdateExceptionCodes.COLUMN_NOT_FOUND.create(column.getName());
        }
        return name.equalsIgnoreCase(column.getDefinition());
    }


}
