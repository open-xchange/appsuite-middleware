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
 * {@link CalendarTombstoneCleaner}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class CalendarTombstoneCleaner extends AbstractTombstoneTableCleaner {

    @Override
    public void checkTables(Connection connection) throws OXException, SQLException {
        boolean tablesExist = Databases.tablesExist(connection, "calendar_attendee_tombstone", "calendar_event_tombstone");
        if (!tablesExist) {
            throw TombstoneCleanupExceptionCode.TABLE_NOT_EXISTS_ERROR.create("calendar_attendee_tombstone, calendar_event_tombstone");
        }
        boolean columnsExist = Databases.columnsExist(connection, "calendar_event_tombstone", "cid", "account", "id", "timestamp");
        if (!columnsExist) {
            throw TombstoneCleanupExceptionCode.COLUMN_NOT_EXISTS_ERROR.create("calendar_event_tombstone", "cid, account, id, timestamp");
        }
        columnsExist = Databases.columnsExist(connection, "calendar_attendee_tombstone", "cid", "account", "event");
        if (!columnsExist) {
            throw TombstoneCleanupExceptionCode.COLUMN_NOT_EXISTS_ERROR.create("calendar_attendee_tombstone", "cid, account, event");
        }
    }

    @Override
    public Map<String, Integer> cleanupSafe(Connection connection, long timestamp) throws SQLException {
        Map<String, Integer> deletedRowsPerTable = new HashMap<>();

        // Removes entries from both tables where the relation matches
        String deleteEntriesWithConstraints = "DELETE FROM calendar_event_tombstone, calendar_attendee_tombstone USING calendar_event_tombstone INNER JOIN calendar_attendee_tombstone ON calendar_event_tombstone.cid = calendar_attendee_tombstone.cid AND calendar_event_tombstone.account = calendar_attendee_tombstone.account AND calendar_event_tombstone.id = calendar_attendee_tombstone.event WHERE calendar_event_tombstone.timestamp < ?;";
        int rowsDeleted = delete(connection, timestamp, deleteEntriesWithConstraints);
        deletedRowsPerTable.put("calendar_attendee_tombstone", Autoboxing.I(rowsDeleted));

        String delete = "DELETE FROM calendar_event_tombstone WHERE timestamp < ?";
        rowsDeleted += delete(connection, timestamp, delete);
        deletedRowsPerTable.put("calendar_event_tombstone", Autoboxing.I(rowsDeleted));

        return deletedRowsPerTable;
    }
}
