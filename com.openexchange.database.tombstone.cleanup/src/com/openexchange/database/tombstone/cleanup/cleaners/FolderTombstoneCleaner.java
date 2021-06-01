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

package com.openexchange.database.tombstone.cleanup.cleaners;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.ConcurrentHashSet;

/**
 * {@link FolderTombstoneCleaner}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class FolderTombstoneCleaner extends AbstractContextBasedTombstoneTableCleaner {

    @Override
    public void checkTables(Connection connection) throws OXException, SQLException {
        boolean tablesExist = Databases.tablesExist(connection, "del_oxfolder_tree", "del_oxfolder_permissions");
        if (!tablesExist) {
            throw TombstoneCleanupExceptionCode.TABLE_NOT_EXISTS_ERROR.create("del_oxfolder_tree, del_oxfolder_permissions");
        }
        boolean columnsExist = Databases.columnExists(connection, "del_oxfolder_tree", "changing_date");
        if (!columnsExist) {
            throw TombstoneCleanupExceptionCode.COLUMN_NOT_EXISTS_ERROR.create("del_oxfolder_tree", "changing_date");
        }
        columnsExist = Databases.columnsExist(connection, "del_oxfolder_permissions", "cid", "fuid");
        if (!columnsExist) {
            throw TombstoneCleanupExceptionCode.COLUMN_NOT_EXISTS_ERROR.create("del_oxfolder_permissions", "cid, fuid");
        }
    }

    @Override
    public Map<String, Integer> cleanupSafe(Connection connection, long timestamp) throws SQLException {
        // Because of the explicitly defined foreign key constraint (without having option 'on delete cascade') we have to manually solve the constraint issues.
        String toBeDeleted = "SELECT cid, fuid FROM del_oxfolder_tree WHERE changing_date < ?";
        Map<Integer, Set<Integer>> deleteCandidates = gatherDeleteCandidates(connection, timestamp, toBeDeleted);
        if (deleteCandidates.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Integer> deletedRowsPerTable = new HashMap<>();

        String deleteStatement = "DELETE FROM del_oxfolder_permissions WHERE cid = ? AND fuid";
        int deletedRows = deleteContextWise(connection, deleteCandidates, deleteStatement);
        deletedRowsPerTable.put("del_oxfolder_permissions", Autoboxing.I(deletedRows));
        deleteCandidates = null;

        String delete = "DELETE FROM del_oxfolder_tree WHERE changing_date < ?";
        deletedRows = delete(connection, timestamp, delete);
        deletedRowsPerTable.put("del_oxfolder_tree", Autoboxing.I(deletedRows));

        return deletedRowsPerTable;
    }

    @Override
    protected Map<Integer, Set<Integer>> readItems(ResultSet resultSet) throws SQLException {
        Map<Integer, Set<Integer>> candidatesPerContext = new ConcurrentHashMap<>();
        while (resultSet.next()) {
            int contextId = resultSet.getInt("cid");
            Set<Integer> contextIdList = candidatesPerContext.get(I(contextId));
            if (contextIdList == null) {
                contextIdList = new ConcurrentHashSet<>();
                candidatesPerContext.put(I(contextId), contextIdList);
            }
            contextIdList.add(I(resultSet.getInt("fuid")));
        }
        return candidatesPerContext;
    }
}
