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
 * {@link TaskTombstoneCleaner}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class TaskTombstoneCleaner extends AbstractContextBasedTombstoneTableCleaner {

    @Override
    public void checkTables(Connection connection) throws OXException, SQLException {
        boolean tablesExist = Databases.tablesExist(connection, "del_task", "del_task_folder", "del_task_participant");
        if (!tablesExist) {
            throw TombstoneCleanupExceptionCode.TABLE_NOT_EXISTS_ERROR.create("del_task, del_task_folder, del_task_participant");
        }
        boolean columnsExist = Databases.columnsExist(connection, "del_task", "id", "cid", "last_modified");
        if (!columnsExist) {
            throw TombstoneCleanupExceptionCode.COLUMN_NOT_EXISTS_ERROR.create("del_task", "id, cid, last_modified");
        }
        columnsExist = Databases.columnsExist(connection, "del_task_folder", "id", "cid");
        if (!columnsExist) {
            throw TombstoneCleanupExceptionCode.COLUMN_NOT_EXISTS_ERROR.create("del_task_folder", "id, cid");
        }
        columnsExist = Databases.columnsExist(connection, "del_task_participant", "task", "cid");
        if (!columnsExist) {
            throw TombstoneCleanupExceptionCode.COLUMN_NOT_EXISTS_ERROR.create("del_task_participant", "task, cid");
        }
    }

    @Override
    public Map<String, Integer> cleanupSafe(Connection connection, long timestamp) throws SQLException {
        // Because of the explicitly defined foreign key constraint (without having option 'on delete cascade') we have to manually solve the constraint issues.
        String toBeDeleted = "SELECT cid, id FROM del_task WHERE last_modified < ?";
        Map<Integer, Set<Integer>> deleteCandidates = gatherDeleteCandidates(connection, timestamp, toBeDeleted);
        if (deleteCandidates.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Integer> deletedRowsPerTable = new HashMap<>();
        String deleteTaskFolder = "DELETE FROM del_task_folder WHERE cid = ? AND id";
        int deleteContextWise = deleteContextWise(connection, deleteCandidates, deleteTaskFolder);
        deletedRowsPerTable.put("del_task_folder", Autoboxing.I(deleteContextWise));

        String deleteTaskParticipant = "DELETE FROM del_task_participant WHERE cid = ? AND task";
        int deleteContextWise2 = deleteContextWise(connection, deleteCandidates, deleteTaskParticipant);
        deletedRowsPerTable.put("del_task_participant", Autoboxing.I(deleteContextWise2));
        deleteCandidates = null;

        String deleteTask = "DELETE FROM del_task WHERE last_modified < ?";
        int deletedRows = delete(connection, timestamp, deleteTask);
        deletedRowsPerTable.put("del_task", Autoboxing.I(deletedRows));

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
            contextIdList.add(I(resultSet.getInt("id")));
        }
        return candidatesPerContext;
    }
}
