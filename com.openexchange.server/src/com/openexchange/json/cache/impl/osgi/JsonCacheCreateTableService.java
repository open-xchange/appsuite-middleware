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

package com.openexchange.json.cache.impl.osgi;

import com.openexchange.database.AbstractCreateTableImpl;


/**
 * {@link JsonCacheCreateTableService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JsonCacheCreateTableService extends AbstractCreateTableImpl {

    private static final String TABLE = "jsonCache";

    private static final String CREATE = "CREATE TABLE " + TABLE + " (\n" +
        "  cid INT4 unsigned NOT NULL,\n" +
        "  user INT4 unsigned NOT NULL,\n" +
        "  id VARCHAR(128) CHARACTER SET latin1 NOT NULL,\n" +
        "  json TEXT CHARACTER SET latin1 NOT NULL,\n" +
        "  inProgressSince bigint(64) DEFAULT NULL,\n" +
        "  lastUpdate bigint(64) DEFAULT NULL,\n" +
        "  took bigint(64) DEFAULT 0,\n" +
        "  size bigint(64) DEFAULT 0,\n" +
        "  PRIMARY KEY (cid,user,id)\n" +
        ") ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci";

    /**
     * Gets the table names.
     *
     * @return The table names.
     */
    public static String[] getTablesToCreate() {
        return new String[] { TABLE };
    }

    /**
     * Gets the CREATE-TABLE statements.
     *
     * @return The CREATE statements
     */
    public static String[] getCreateStmts() {
        return new String[] { CREATE };
    }

    /**
     * Initializes a new {@link JsonCacheCreateTableService}.
     */
    public JsonCacheCreateTableService() {
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
