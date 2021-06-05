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

package com.openexchange.filestore.sproxyd.groupware;

import com.openexchange.database.AbstractCreateTableImpl;
import com.openexchange.database.CreateTableService;


/**
 * {@link SproxydCreateTableService} - The Sproxyd {@link CreateTableService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SproxydCreateTableService extends AbstractCreateTableImpl {

    private static final String TABLE_SCALITY_FILESTORE = "scality_filestore";

    private static final String CREATE_SCALITY_FILESTORE = "CREATE TABLE "+TABLE_SCALITY_FILESTORE+" (" +
        " cid INT4 unsigned NOT NULL," +
        " user INT4 unsigned NOT NULL," +
        " document_id BINARY(16) NOT NULL," +
        " scality_id BINARY(16) NOT NULL," +
        " offset BIGINT(64) NOT NULL," +
        " length BIGINT(64) NOT NULL," +
        " PRIMARY KEY (cid, user, document_id, scality_id)," +
        " UNIQUE KEY `scality_key` (`scality_id`)," +
        " INDEX `scality_index` (`cid`, `scality_id`)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

    /**
     * Gets the table names.
     *
     * @return The table names.
     */
    public static String[] getTablesToCreate() {
        return new String[] { TABLE_SCALITY_FILESTORE };
    }

    /**
     * Gets the CREATE-TABLE statements.
     *
     * @return The CREATE statements
     */
    public static String[] getCreateStmts() {
        return new String[] { CREATE_SCALITY_FILESTORE };
    }

    /**
     * Initializes a new {@link CapabilityCreateTableService}.
     */
    public SproxydCreateTableService() {
        super();
    }

    @Override
    public String[] requiredTables() {
        return new String[] { "user", "infostore_document" };
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
