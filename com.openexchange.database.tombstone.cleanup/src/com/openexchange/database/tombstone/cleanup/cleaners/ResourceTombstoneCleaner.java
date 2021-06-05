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
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;

/**
 * {@link ResourceTombstoneCleaner}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class ResourceTombstoneCleaner extends AbstractTombstoneTableCleaner {

    @Override
    public void checkTables(Connection connection) throws OXException, SQLException {
        boolean tableExists = Databases.tableExists(connection, "del_resource");
        if (!tableExists) {
            throw TombstoneCleanupExceptionCode.TABLE_NOT_EXISTS_ERROR.create("del_resource");
        }
        boolean columnExists = Databases.columnExists(connection, "del_resource", "lastModified");
        if (!columnExists) {
            throw TombstoneCleanupExceptionCode.COLUMN_NOT_EXISTS_ERROR.create("del_resource", "lastModified");
        }
    }

    @Override
    public Map<String, Integer> cleanupSafe(Connection connection, long timestamp) throws SQLException {
        String delete = "DELETE FROM del_resource WHERE lastModified < ?";
        int deletedRows = delete(connection, timestamp, delete);

        return Collections.singletonMap("del_resource", I(deletedRows));
    }
}
