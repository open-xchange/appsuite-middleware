/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.database.tombstone.cleanup.cleaners;

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
            Set<Integer> contextIdList = candidatesPerContext.get(contextId);
            if (contextIdList == null) {
                contextIdList = new ConcurrentHashSet<>();
                candidatesPerContext.put(contextId, contextIdList);
            }
            contextIdList.add(resultSet.getInt("id"));
        }
        return candidatesPerContext;
    }
}
