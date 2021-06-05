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

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Tools;


/**
 * {@link DropRendundantIndicesUpdateTask}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8
 */
public class DropRendundantIndicesUpdateTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link DropRendundantIndicesUpdateTask}.
     */
    public DropRendundantIndicesUpdateTask() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            // Drop redundant index 'accountIndex' in mailSync table
            dropIndex(con, "mailSync", new String[] { "cid", "user", "accountId" });

            // Drop redundant index 'module' in indexedFolders table
            dropIndex(con, "indexedFolders", new String[] {"cid", "uid", "module"});

            // Drop redundant index 'account' in indexedFolders table
            dropIndex(con, "indexedFolders", new String[] {"cid", "uid", "module", "account"});

            // Drop redundant index 'userIndex' in oauth2Accessor table
            dropIndex(con, "oauth2Accessor", new String[] {"cid", "user"});

            // Drop redundant index 'userIndex' in oauthAccessor table
            dropIndex(con, "oauthAccessor", new String[] {"cid", "user"});

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }

    }

    @Override
    public String[] getDependencies() {
        return new String[] {"com.openexchange.groupware.update.tasks.AddGuestCreatedByIndexForUserTable"};
    }

    private void dropIndex(Connection con, String table, String[] columns) throws SQLException {
        if (Tools.tableExists(con, table)) {
            String index = Tools.existsIndex(con, table, columns);
            if (null != index && !index.isEmpty()) {
                Tools.dropIndex(con, table, index);
            }
        }
    }

}
