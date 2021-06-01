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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.Iterables;
import com.openexchange.database.Databases;

/**
 * {@link AbstractContextBasedTombstoneTableCleaner}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public abstract class AbstractContextBasedTombstoneTableCleaner extends AbstractTombstoneTableCleaner {

    /**
     * Initializes a new {@link AbstractContextBasedTombstoneTableCleaner}.
     */
    protected AbstractContextBasedTombstoneTableCleaner() {
        super();
    }

    protected Map<Integer, Set<Integer>> gatherDeleteCandidates(Connection connection, long timestamp, String selectStmt) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(selectStmt)) {
            int parameterIndex = 1;
            stmt.setLong(parameterIndex++, timestamp);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                return readItems(resultSet);
            }
        }
    }

    /**
     * Reads the items selected by {@link #gatherDeleteCandidates(Connection, long, String)} and returns the contextId - Set of object IDs mapping
     *
     * @param resultSet The {@link ResultSet} to read from
     * @return Map<Integer, Set<Integer>> containing the items read by {@link #gatherDeleteCandidates(Connection, long, String)}
     * @throws SQLException
     */
    protected abstract Map<Integer, Set<Integer>> readItems(ResultSet resultSet) throws SQLException;

    /**
     * Generic method to (chunk-wise) delete entries based on the given delete statement.
     *
     * @param connection The write connection to delete the entries
     * @param deleteCandidatesPerContext Map containing context id to a set of integer mapping with IDs that should be deleted
     * @param deleteStatement The statement that will be used to delete entries
     * @throws SQLException
     */
    protected int deleteContextWise(Connection connection, Map<Integer, Set<Integer>> deleteCandidatesPerContext, String deleteStatement) throws SQLException {
        int deletedRows = 0;
        for (Map.Entry<Integer, Set<Integer>> candidatesPerContext : deleteCandidatesPerContext.entrySet()) {
            Integer contextId = candidatesPerContext.getKey();
            Set<Integer> ids = candidatesPerContext.getValue();

            for (List<Integer> chunk : Iterables.partition(ids, 500)) {
                StringBuilder stringBuilder = new StringBuilder().append(deleteStatement).append(Databases.getPlaceholders(chunk.size()));
                try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
                    int parameterIndex = 1;
                    stmt.setInt(parameterIndex++, contextId.intValue());
                    for (Integer id : chunk) {
                        stmt.setInt(parameterIndex++, id.intValue());
                    }
                    deletedRows += logExecuteUpdate(stmt);
                }
            }
        }
        return deletedRows;
    }
}
