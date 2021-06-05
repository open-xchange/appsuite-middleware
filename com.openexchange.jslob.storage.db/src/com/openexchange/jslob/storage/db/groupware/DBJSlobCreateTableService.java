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

package com.openexchange.jslob.storage.db.groupware;

import com.openexchange.database.AbstractCreateTableImpl;


/**
 * {@link DBJSlobCreateTableService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DBJSlobCreateTableService extends AbstractCreateTableImpl {

    private static final String TABLE = "jsonStorage";

    private static final String CREATE = "CREATE TABLE " + TABLE + " (\n" +
        "  cid INT4 unsigned NOT NULL,\n" +
        "  user INT4 unsigned NOT NULL,\n" +
        "  serviceId varchar(128) collate utf8mb4_unicode_ci NOT NULL,\n" +
        "  id varchar(128) collate utf8mb4_unicode_ci NOT NULL,\n" +
        "  locked tinyint(3) unsigned default '0',\n" +
        "  data MEDIUMBLOB,\n" +
        "  PRIMARY KEY  (cid,user,serviceId,id)\n" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

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
     * Initializes a new {@link DBJSlobCreateTableService}.
     */
    public DBJSlobCreateTableService() {
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
