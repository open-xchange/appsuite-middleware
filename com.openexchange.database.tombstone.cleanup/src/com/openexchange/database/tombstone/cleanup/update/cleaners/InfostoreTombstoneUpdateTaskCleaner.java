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

package com.openexchange.database.tombstone.cleanup.update.cleaners;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.openexchange.database.tombstone.cleanup.cleaners.InfostoreTombstoneCleaner;
import com.openexchange.java.ConcurrentList;

/**
 * {@link InfostoreTombstoneUpdateTaskCleaner}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class InfostoreTombstoneUpdateTaskCleaner extends InfostoreTombstoneCleaner {

    @Override
    public Map<String, Integer> cleanupSafe(Connection connection, long timestamp) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT DISTINCT cid FROM del_infostore WHERE last_modified < ?")) {
            int parameterIndex = 1;
            stmt.setLong(parameterIndex++, timestamp);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                List<Integer> deleteCanditates = readContexts(resultSet);
                deleteContextWise(connection, deleteCanditates, timestamp);

                try (Statement createStatement = connection.createStatement()) {
                    createStatement.addBatch("CREATE TABLE del_infostore_new LIKE del_infostore;");
                    createStatement.addBatch("ALTER TABLE del_infostore_new ENGINE = InnoDB;");
                    createStatement.addBatch("INSERT INTO del_infostore_new SELECT * FROM del_infostore WHERE last_modified >= " + timestamp + ";");
                    createStatement.addBatch("RENAME TABLE del_infostore TO del_infostore_old, del_infostore_new TO del_infostore;");
                    createStatement.addBatch("DROP TABLE del_infostore_old");
                    createStatement.executeBatch();
                }
            }
        }
        return Collections.emptyMap();
    }

    protected List<Integer> readContexts(ResultSet resultSet) throws SQLException {
        List<Integer> affectedContextIds = new ConcurrentList<>();
        while (resultSet.next()) {
            int contextId = resultSet.getInt("cid");
            affectedContextIds.add(I(contextId));
        }
        return affectedContextIds;
    }

    protected void deleteContextWise(Connection connection, List<Integer> deleteCandidatesPerContext, long timestamp) throws SQLException {
        String deleteStmt = "DELETE FROM del_infostore, del_infostore_document USING del_infostore INNER JOIN del_infostore_document ON del_infostore_document.infostore_id = del_infostore.id AND del_infostore_document.cid = del_infostore.cid WHERE del_infostore.last_modified < ? AND del_infostore.cid = ?";

        for (Integer contextId : deleteCandidatesPerContext) {
            if (contextId == null) {
                continue;
            }
            try (PreparedStatement stmt = connection.prepareStatement(deleteStmt)) {
                int parameterIndex = 1;
                stmt.setLong(parameterIndex++, timestamp);
                stmt.setInt(parameterIndex++, contextId.intValue());
                logExecuteUpdate(stmt);
            }
        }
    }
}
