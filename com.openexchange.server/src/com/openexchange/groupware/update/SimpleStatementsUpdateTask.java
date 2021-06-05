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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.database.Databases;

/**
 * Build a subclass of {@link SimpleStatementsUpdateTask} if all you want to do is execute a bunch of statements.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class SimpleStatementsUpdateTask extends SimpleUpdateTask {

    private final List<StatementHolder> statements = new ArrayList<StatementHolder>();

    public SimpleStatementsUpdateTask() {
        statements();
    }

    /**
     * Define Statements with {@link #add(String, Object...)} here.
     */
    protected abstract void statements();

    /**
     * Add a statement to be executed. The ? in the statement will be filled with objects from the values array
     */
    public void add(String statement, Object...values) {
        statements.add(new StatementHolder(statement, values));
    }

    @Override
    protected final void perform(Connection con) throws SQLException {
        for (StatementHolder sqlStatement : statements) {
            sqlStatement.execute(con);
        }
    }

    protected static class StatementHolder {
        private final String statement;
        private final Object[] values;

        public StatementHolder(String statement, Object...values) {
            this.statement = statement;
            this.values = values;
        }

        public void execute(Connection con) throws SQLException {
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(statement);
                for(int i = 0; i < values.length; i++) {
                    stmt.setObject(i+1, values[i]);
                }
                stmt.execute();
            } finally {
                if (stmt != null) {
                    Databases.closeSQLStuff(stmt);
                }
            }
        }
    }
}
