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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.Autoboxing;

/**
 * {@link InfostoreTombstoneCleaner}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class InfostoreTombstoneCleaner extends AbstractTombstoneTableCleaner {

    @Override
    public void checkTables(Connection connection) throws OXException, SQLException {
        boolean tablesExist = Databases.tablesExist(connection, "del_infostore", "del_infostore_document");
        if (!tablesExist) {
            throw TombstoneCleanupExceptionCode.TABLE_NOT_EXISTS_ERROR.create("del_infostore, del_infostore_document");
        }
        boolean columnsExist = Databases.columnsExist(connection, "del_infostore", "id", "cid", "last_modified");
        if (!columnsExist) {
            throw TombstoneCleanupExceptionCode.COLUMN_NOT_EXISTS_ERROR.create("del_infostore", "id, cid, last_modified, ");
        }
        columnsExist = Databases.columnsExist(connection, "del_infostore_document", "infostore_id", "cid");
        if (!columnsExist) {
            throw TombstoneCleanupExceptionCode.COLUMN_NOT_EXISTS_ERROR.create("del_infostore_document", "infostore_id, cid");
        }
    }

    @Override
    public Map<String, Integer> cleanupSafe(Connection connection, long timestamp) throws SQLException {
        Map<String, Integer> deletedRowsPerTable = new HashMap<>();

        // Removes entries from both tables where the relation matches
        String deleteEntriesWithConstraints = "DELETE FROM del_infostore, del_infostore_document USING del_infostore INNER JOIN del_infostore_document ON del_infostore_document.infostore_id = del_infostore.id AND del_infostore_document.cid = del_infostore.cid WHERE del_infostore.last_modified < ?";
        int deletedRows = delete(connection, timestamp, deleteEntriesWithConstraints);
        deletedRowsPerTable.put("del_infostore_document", Autoboxing.I(deletedRows));

        // Removes entries from the parent table where no relation is available
        String deleteInfostore = "DELETE FROM del_infostore WHERE last_modified < ?";
        deletedRows += delete(connection, timestamp, deleteInfostore);
        deletedRowsPerTable.put("del_infostore", Autoboxing.I(deletedRows));

        return deletedRowsPerTable;
    }
}
