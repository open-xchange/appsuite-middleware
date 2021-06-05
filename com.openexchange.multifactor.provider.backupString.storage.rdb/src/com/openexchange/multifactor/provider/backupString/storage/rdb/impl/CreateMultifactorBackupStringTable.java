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

package com.openexchange.multifactor.provider.backupString.storage.rdb.impl;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link CreateMultifactorBackupStringTable}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class CreateMultifactorBackupStringTable extends AbstractCreateTableImpl {

    private static final String TABLE_MULTIFACTOR_TOTP = "multifactor_backup_device";

    private static final String CREATE_MULTIFACTOR_TABLE =
        "CREATE TABLE " + TABLE_MULTIFACTOR_TOTP + " (" +
            "id BINARY(16) NOT NULL, " +
            "name VARCHAR(100) NOT NULL, " +
            "cid int(10) unsigned NOT NULL," +
            "user int(10) unsigned NOT NULL, " +
            "enabled TINYINT(1) DEFAULT 0, " +
            "secret VARCHAR(128) NOT NULL," +
            "length int(10) unsigned NOT NULL, " +
            "PRIMARY KEY (cid,user,id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";



    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] {TABLE_MULTIFACTOR_TOTP};
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] {CREATE_MULTIFACTOR_TABLE};
    }
}
