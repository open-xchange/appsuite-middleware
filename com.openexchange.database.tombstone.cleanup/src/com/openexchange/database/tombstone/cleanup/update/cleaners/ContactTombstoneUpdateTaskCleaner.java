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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;
import com.openexchange.database.tombstone.cleanup.cleaners.ContactTombstoneCleaner;

/**
 * {@link ContactTombstoneUpdateTaskCleaner}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class ContactTombstoneUpdateTaskCleaner extends ContactTombstoneCleaner {

    @Override
    public Map<String, Integer> cleanupSafe(Connection connection, long timestamp) throws SQLException {
        // Removes entries from both tables where the relation matches
        String deleteEntriesWithConstraints = "DELETE FROM del_contacts, del_contacts_image USING del_contacts INNER JOIN del_contacts_image ON del_contacts.cid = del_contacts_image.cid AND del_contacts.intfield01 = del_contacts_image.intfield01 WHERE del_contacts.changing_date < ?";
        delete(connection, timestamp, deleteEntriesWithConstraints);

        try (Statement createStatement = connection.createStatement()) {
            createStatement.addBatch("CREATE TABLE del_contacts_new LIKE del_contacts;");
            createStatement.addBatch("ALTER TABLE del_contacts_new ENGINE = InnoDB;");
            createStatement.addBatch("INSERT INTO del_contacts_new SELECT * FROM del_contacts WHERE changing_date >= " + timestamp + ";");
            createStatement.addBatch("RENAME TABLE del_contacts TO del_contacts_old, del_contacts_new TO del_contacts;");
            createStatement.addBatch("DROP TABLE del_contacts_old");
            createStatement.executeBatch();
        }

        return Collections.emptyMap();
    }
}
