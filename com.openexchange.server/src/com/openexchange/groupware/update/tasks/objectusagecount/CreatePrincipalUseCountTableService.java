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

package com.openexchange.groupware.update.tasks.objectusagecount;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link CreatePrincipalUseCountTableService}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class CreatePrincipalUseCountTableService extends AbstractCreateTableImpl {

    final static String TABLE = "principalUseCount";

    static String getStatement() {
        // @formatter:off
        return "CREATE TABLE " + TABLE + " (" +
            "cid int(10) unsigned NOT NULL, " +
            "user int(10) unsigned NOT NULL, " +
            "principal int(10) unsigned NOT NULL, " +
            "value int(10) unsigned NOT NULL, " +
            "PRIMARY KEY (cid, user, principal)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        // @formatter:on
    }

    @Override
    public String[] requiredTables() {
        return new String[] { "user" };
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { "principalUseCount" };
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { getStatement() };
    }

}
