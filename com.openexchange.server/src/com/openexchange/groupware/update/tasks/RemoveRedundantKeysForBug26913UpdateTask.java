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
 * {@link RemoveRedundantKeysForBug26913UpdateTask}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class RemoveRedundantKeysForBug26913UpdateTask extends UpdateTaskAdapter {

    private static final String[] INDEX_COLUMNS = new String[] {"cid", "user"};
    private static final String SNIPPET_TABLE = "snippet";
    private static final String PREVIEW_TABLE = "preview";

    /**
     * Initializes a new {@link RemoveRedundantKeysForBug26913UpdateTask}.
     */
    public RemoveRedundantKeysForBug26913UpdateTask() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            String previewKey = Tools.existsIndex(con, PREVIEW_TABLE, INDEX_COLUMNS);
            if (previewKey != null && previewKey.equals("user")) {
                Tools.dropIndex(con, PREVIEW_TABLE, previewKey);
            }
            String snippetKey = Tools.existsIndex(con, SNIPPET_TABLE, INDEX_COLUMNS);
            if (snippetKey != null && snippetKey.equals("indexUser")) {
                Tools.dropIndex(con, SNIPPET_TABLE, snippetKey);
            }

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
        return new String[0];
    }

}
