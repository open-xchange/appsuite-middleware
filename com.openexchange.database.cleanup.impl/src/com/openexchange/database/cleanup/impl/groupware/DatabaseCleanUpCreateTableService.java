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

package com.openexchange.database.cleanup.impl.groupware;

import static com.openexchange.database.cleanup.CleanUpJobId.MAX_LENGTH;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link DatabaseCleanUpCreateTableService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class DatabaseCleanUpCreateTableService extends AbstractCreateTableImpl {

    //@formatter:off
    public static Map<String, String> getTablesByName() {
        String tableName = "cleanupJobExecution";
        return Collections.singletonMap(tableName, "CREATE TABLE " + tableName + " (" +
            "id VARCHAR(" + MAX_LENGTH + ") CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL," +
            "timestamp BIGINT(64) NOT NULL," +
            "running TINYINT UNSIGNED NOT NULL default '0'," +
            "PRIMARY KEY (id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
    }
    //@formatter:on

    @Override
    public String[] requiredTables() {
        return new String[0];
    }

    @Override
    public String[] tablesToCreate() {
        Set<String> tableNames = getTablesByName().keySet();
        return tableNames.toArray(new String[tableNames.size()]);
    }

    @Override
    public String[] getCreateStatements() {
        Collection<String> createStatements = getTablesByName().values();
        return createStatements.toArray(new String[createStatements.size()]);
    }

}
