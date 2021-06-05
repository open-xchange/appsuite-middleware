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
import com.openexchange.tools.update.Index;
import com.openexchange.tools.update.IndexNotFoundException;

/**
 * A {@link CreateIndexUpdateTask} is an abstract superclass for UpdateTasks wanting to create an index. It checks for the presence of a named (using only the name as criterion)
 * index, and, if it is not found, creates the index.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class CreateIndexUpdateTask extends UpdateTaskAdapter {

    private final Index index;

    public CreateIndexUpdateTask(String table, String indexName, String...columns) {
        super();
        index = new Index();
        index.setTable(table);
        index.setName(indexName);
        index.setColumns(columns);
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            if (hasIndex(con)) {
                return;
            }

            con.setAutoCommit(false);
            rollback = 1;

            createIndex(con);

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

    protected void createIndex(Connection con) throws SQLException {
        index.create(con);
    }

    private boolean hasIndex(Connection con) throws SQLException {
        try {
            Index.findByName(con, index.getTable(), index.getName());
        } catch (IndexNotFoundException e) {
            return false;
        }
        return true;
    }
}
