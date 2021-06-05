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

package com.openexchange.groupware.update.internal;

import com.openexchange.database.AbstractCreateTableImpl;
import com.openexchange.database.CreateTableService;

/**
 * Implements the {@link CreateTableService} for creating the updateTask table.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class CreateUpdateTaskTable extends AbstractCreateTableImpl {

    private static final String[] CREATED_TABLES = { "updateTask" };

    static final String[] CREATES_PRIMARY_KEY = {
         "CREATE TABLE updateTask " +
         "(cid INT4 UNSIGNED NOT NULL," +
         "taskName VARCHAR(1024) NOT NULL," +
         "successful BOOLEAN NOT NULL," +
         "lastModified INT8 NOT NULL," +
         "uuid BINARY(16) NOT NULL," +
         "PRIMARY KEY (cid, uuid)," +
         "INDEX full (cid,taskName(191))) " +
         "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
    };

    public CreateUpdateTaskTable() {
        super();
    }

    @Override
    protected String[] getCreateStatements() {
        return CREATES_PRIMARY_KEY;
    }

    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    @Override
    public String[] tablesToCreate() {
        return CREATED_TABLES.clone();
    }
}
