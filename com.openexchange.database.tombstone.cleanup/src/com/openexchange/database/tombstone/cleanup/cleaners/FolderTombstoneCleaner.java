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
            Set<Integer> contextIdList = candidatesPerContext.get(contextId);
            if (contextIdList == null) {
                contextIdList = new ConcurrentHashSet<>();
                candidatesPerContext.put(contextId, contextIdList);
            }
            contextIdList.add(resultSet.getInt("fuid"));
        }
        return candidatesPerContext;
    }
}
