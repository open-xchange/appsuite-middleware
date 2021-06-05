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

package com.openexchange.groupware.contexts.impl.sql;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link ContextAttributeCreateTable}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContextAttributeCreateTable extends AbstractCreateTableImpl {

    private static final String[] TABLE = new String[]{"contextAttribute"};

    private static final String[] CREATE_TABLE = new String[] { "CREATE TABLE `contextAttribute` ("
        + " `cid` INT4 unsigned NOT NULL,"
        + " `name` varchar(128) collate utf8mb4_unicode_ci NOT NULL,"
        + " `value` TEXT collate utf8mb4_unicode_ci NOT NULL,"
        + " PRIMARY KEY `cid` (`cid`,`name`)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci" };

    @Override
    protected String[] getCreateStatements() {
        return CREATE_TABLE;
    }

    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    @Override
    public String[] tablesToCreate() {
        return TABLE;
    }

}
