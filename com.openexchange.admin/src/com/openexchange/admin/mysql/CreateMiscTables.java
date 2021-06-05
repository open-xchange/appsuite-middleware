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

package com.openexchange.admin.mysql;

import com.openexchange.database.AbstractCreateTableImpl;


/**
 * {@link CreateMiscTables}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class CreateMiscTables extends AbstractCreateTableImpl {

    private static final String prgLinksTableName = "prg_links";
    private static final String reminderTableName = "reminder";
    private static final String filestoreUsageTableName = "filestore_usage";

    private static final String createPrgLinksTablePrimaryKey = "CREATE TABLE prg_links ("
        + "firstid INT4 UNSIGNED NOT NULL,"
        + "firstmodule INT4 UNSIGNED NOT NULL,"
        + "firstfolder INT4 UNSIGNED NOT NULL,"
        + "secondid INT4 UNSIGNED NOT NULL,"
        + "secondmodule INT4 UNSIGNED NOT NULL,"
        + "secondfolder INT4 UNSIGNED NOT NULL,"
        + "cid INT4 UNSIGNED NOT NULL,"
        + "last_modified INT8,"
        + "created_by INT4 UNSIGNED,"
        + "uuid BINARY(16) NOT NULL,"
        + "PRIMARY KEY (cid, uuid),"
        + "INDEX (firstid),"
        + "INDEX (secondid),"
        + "INDEX (cid)"
      + ") ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createReminderTable = "CREATE TABLE reminder ("
        + "cid INT4 UNSIGNED NOT NULL,"
        + "object_id INT4 UNSIGNED NOT NULL,"
        + "last_modified INT8 UNSIGNED,"
        + "target_id VARCHAR(255) NOT NULL,"
        + "module INT1 UNSIGNED NOT NULL,"
        + "userid INT4 UNSIGNED NOT NULL,"
        + "alarm DATETIME NOT NULL,"
        + "recurrence TINYINT NOT NULL,"
        + "description VARCHAR(1028),"
        + "folder VARCHAR(1028),"
        + "PRIMARY KEY (cid,object_id),"
        + "INDEX (cid,userid,alarm),"
        + "INDEX (cid,userid,last_modified),"
        + "CONSTRAINT reminder_unique UNIQUE (cid,target_id(191),module,userid)"
      + ") ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createFilestoreUsageTable = "CREATE TABLE filestore_usage ("
        + "cid INT4 UNSIGNED NOT NULL,"
        + "user INT4 UNSIGNED NOT NULL DEFAULT 0,"
        + "used INT8 NOT NULL,"
        + "PRIMARY KEY(cid, user)"
      + ") ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    /**
     * Initializes a new {@link CreateMiscTables}.
     */
    public CreateMiscTables() {
        super();
    }

    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { prgLinksTableName, reminderTableName, filestoreUsageTableName };
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { createPrgLinksTablePrimaryKey, createReminderTable, createFilestoreUsageTable };
    }

}
