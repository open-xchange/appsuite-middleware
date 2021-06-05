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

package com.openexchange.download.limit.rdb;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link FileAccessCreateTableService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public final class FileAccessCreateTableService extends AbstractCreateTableImpl {

    private static final String FILE_ACCESS_TABLE = "fileAccess";

    private static final String LIMIT_TABLE_CREATE_STMT = "CREATE TABLE " + FILE_ACCESS_TABLE + " (\n" +
        "  cid int4 unsigned NOT NULL,\n" +
        "  userid int4 unsigned NOT NULL,\n" +
        "  accessed bigint(20) unsigned NOT NULL,\n" +
        "  size bigint(20) unsigned NOT NULL,\n" +
        "  PRIMARY KEY (`cid`,`userid`,`accessed`)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    /**
     * Gets the table names.
     *
     * @return The table names.
     */
    public static String[] getTablesToCreate() {
        return new String[] { FILE_ACCESS_TABLE };
    }

    /**
     * Gets the CREATE-TABLE statements.
     *
     * @return The CREATE statements
     */
    public static String[] getCreateStmts() {
        return new String[] { LIMIT_TABLE_CREATE_STMT };
    }

    /**
     * Initializes a new {@link FileAccessCreateTableService}.
     */
    public FileAccessCreateTableService() {
        super();
    }

    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    @Override
    public String[] tablesToCreate() {
        return getTablesToCreate();
    }

    @Override
    protected String[] getCreateStatements() {
        return getCreateStmts();
    }
}
