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

package com.openexchange.capabilities.groupware;

import com.openexchange.database.AbstractCreateTableImpl;


/**
 * {@link CapabilityCreateTableService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CapabilityCreateTableService extends AbstractCreateTableImpl {

    private static final String TABLE_CAP_CONTEXT = "capability_context";

    private static final String CREATE_CAP_CONTEXT = "CREATE TABLE "+TABLE_CAP_CONTEXT+" (" +
        " cid INT4 unsigned NOT NULL," +
        " cap VARCHAR(64) CHARACTER SET latin1 NOT NULL DEFAULT ''," +
        " PRIMARY KEY (cid, cap)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

    private static final String TABLE_CAP_USER = "capability_user";

    private static final String CREATE_CAP_USER = "CREATE TABLE "+TABLE_CAP_USER+" (" +
        " cid INT4 unsigned NOT NULL," +
        " user INT4 unsigned NOT NULL," +
        " cap VARCHAR(64) CHARACTER SET latin1 NOT NULL DEFAULT ''," +
        " PRIMARY KEY (cid, user, cap)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

    /**
     * Gets the table names.
     *
     * @return The table names.
     */
    public static String[] getTablesToCreate() {
        return new String[] { TABLE_CAP_CONTEXT, TABLE_CAP_USER };
    }

    /**
     * Gets the CREATE-TABLE statements.
     *
     * @return The CREATE statements
     */
    public static String[] getCreateStmts() {
        return new String[] { CREATE_CAP_CONTEXT, CREATE_CAP_USER };
    }

    /**
     * Initializes a new {@link CapabilityCreateTableService}.
     */
    public CapabilityCreateTableService() {
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
