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

package com.openexchange.gdpr.dataexport.impl.groupware;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link DataExportCreateTableService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportCreateTableService extends AbstractCreateTableImpl {

    private static final DataExportCreateTableService INSTANCE = new DataExportCreateTableService();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static DataExportCreateTableService getInstance() {
        return INSTANCE;
    }

    //@formatter:off
    public static Map<String, String> getTablesByName() {
        Map<String, String> tablesByName = new LinkedHashMap<String, String>(4);

        String tableName = "dataExportTask";
        tablesByName.put(tableName,
            "CREATE TABLE " + tableName + " (" +
            "uuid binary(16) NOT NULL," +
            "cid INT4 UNSIGNED NOT NULL," +
            "user INT4 UNSIGNED NOT NULL," +
            "timestamp BIGINT(20) DEFAULT NULL," +
            "startTime BIGINT(20) DEFAULT NULL," +
            "creationTime BIGINT(20) NOT NULL," +
            "duration BIGINT(20) DEFAULT NULL," +
            "notificationSent TINYINT(1) NOT NULL DEFAULT 0," +
            "status VARCHAR(32) NOT NULL," +
            "filestore INT(10) unsigned NOT NULL," +
            "arguments TEXT DEFAULT NULL," +
            "PRIMARY KEY (uuid)," +
            "UNIQUE KEY `user_key` (cid,user)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;"
        );

        tableName = "dataExportTaskWorklist";
        tablesByName.put(tableName,
            "CREATE TABLE " + tableName + " (" +
            "cid INT4 UNSIGNED NOT NULL," +
            "uuid binary(16) NOT NULL," +
            "taskId binary(16) NOT NULL," +
            "id VARCHAR(32) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL," +
            "status VARCHAR(32) NOT NULL," +
            "info TEXT DEFAULT NULL," +
            "savepoint TEXT DEFAULT NULL," +
            "filestoreLocation VARCHAR(255) DEFAULT NULL," +
            "failCount INT4 UNSIGNED NOT NULL DEFAULT 0," +
            "PRIMARY KEY (uuid)," +
            "UNIQUE KEY `task_key` (taskId, id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;"
        );

        tableName = "dataExportFilestoreLocation";
        tablesByName.put(tableName,
            "CREATE TABLE " + tableName + " (" +
            "cid INT4 UNSIGNED NOT NULL," +
            "taskId binary(16) NOT NULL," +
            "num INT(10) unsigned NOT NULL," +
            "filestoreLocation VARCHAR(255) NOT NULL," +
            "size BIGINT(20) NOT NULL," +
            "PRIMARY KEY (taskId, num)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;"
        );

        tableName = "dataExportReport";
        tablesByName.put(tableName,
            "CREATE TABLE " + tableName + " (" +
            "cid INT4 UNSIGNED NOT NULL," +
            "messageId binary(16) NOT NULL," +
            "taskId binary(16) NOT NULL," +
            "message TEXT NOT NULL," +
            "timeStamp BIGINT(20) NOT NULL," +
            "moduleId VARCHAR(32) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL," +
            "PRIMARY KEY (messageId)," +
            "KEY `task_key` (taskId)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;"
        );

        return tablesByName;
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
