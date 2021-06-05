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
 * {@link ContactTombstoneCleaner}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class ContactTombstoneCleaner extends AbstractTombstoneTableCleaner {

    @Override
    public void checkTables(Connection connection) throws OXException, SQLException {
        boolean tablesExist = Databases.tablesExist(connection, "del_contacts", "del_contacts_image");
        if (!tablesExist) {
            throw TombstoneCleanupExceptionCode.TABLE_NOT_EXISTS_ERROR.create("del_contacts, del_contacts_image");
        }
        boolean columnsExist = Databases.columnsExist(connection, "del_contacts_image", "cid", "intfield01");
        if (!columnsExist) {
            throw TombstoneCleanupExceptionCode.COLUMN_NOT_EXISTS_ERROR.create("del_contacts_image", "cid, intfield01");
        }
        columnsExist = Databases.columnsExist(connection, "del_contacts", "cid", "intfield01", "changing_date");
        if (!columnsExist) {
            throw TombstoneCleanupExceptionCode.COLUMN_NOT_EXISTS_ERROR.create("del_contacts", "cid, intfield01, changing_date");
        }
    }

    @Override
    public Map<String, Integer> cleanupSafe(Connection connection, long timestamp) throws SQLException {
        Map<String, Integer> deletedRowsPerTable = new HashMap<>();

        // Removes entries from both tables where the relation matches
        String deleteEntriesWithConstraints = "DELETE FROM del_contacts, del_contacts_image USING del_contacts INNER JOIN del_contacts_image ON del_contacts.cid = del_contacts_image.cid AND del_contacts.intfield01 = del_contacts_image.intfield01 WHERE del_contacts.changing_date < ?";
        int deletedRows = delete(connection, timestamp, deleteEntriesWithConstraints);
        deletedRowsPerTable.put("del_contacts_image", Autoboxing.I(deletedRows));

        // Removes entries from the parent table where no relation is available
        String delete = "DELETE FROM del_contacts WHERE changing_date < ?";
        deletedRows += delete(connection, timestamp, delete);
        deletedRowsPerTable.put("del_contacts", Autoboxing.I(deletedRows));

        return deletedRowsPerTable;
    }
}
