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

package com.openexchange.groupware.update.tasks.objectpermission;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link ObjectPermissionCreateTableService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ObjectPermissionCreateTableService extends AbstractCreateTableImpl {

    public ObjectPermissionCreateTableService() {
        super();
    }

    @Override
    public String[] getCreateStatements() {
        return new String[] {

            ("CREATE TABLE `object_permission` ("
            + "`cid` INT4 UNSIGNED NOT NULL,"
            + "`permission_id` INT4 UNSIGNED NOT NULL,"
            + "`module` INT4 UNSIGNED NOT NULL,"
            + "`folder_id` INT4 UNSIGNED NOT NULL,"
            + "`object_id` INT4 UNSIGNED NOT NULL,"
            + "`created_by` INT4 UNSIGNED NOT NULL,"
            + "`shared_by` INT4 UNSIGNED NOT NULL,"
            + "`bits` INT4 UNSIGNED NOT NULL,"
            + "`last_modified` BIGINT(64) NOT NULL,"
            + "`group_flag` TINYINT UNSIGNED NOT NULL,"
            + "PRIMARY KEY (`cid`,`permission_id`,`module`,`folder_id`,`object_id`),"
            + "INDEX `created_by_index` (`cid`, `created_by`),"
            + "INDEX `shared_by_index` (`cid`, `shared_by`),"
            + "INDEX `last_modified_index` (`cid`, `permission_id`, `module`, `last_modified`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"),

            ("CREATE TABLE `del_object_permission` ("
            + "`cid` INT4 UNSIGNED NOT NULL,"
            + "`permission_id` INT4 UNSIGNED NOT NULL,"
            + "`module` INT4 UNSIGNED NOT NULL,"
            + "`folder_id` INT4 UNSIGNED NOT NULL,"
            + "`object_id` INT4 UNSIGNED NOT NULL,"
            + "`created_by` INT4 UNSIGNED NOT NULL,"
            + "`shared_by` INT4 UNSIGNED NOT NULL,"
            + "`bits` INT4 UNSIGNED NOT NULL,"
            + "`last_modified` BIGINT(64) NOT NULL,"
            + "`group_flag` TINYINT UNSIGNED NOT NULL,"
            + "PRIMARY KEY (`cid`,`permission_id`,`module`,`folder_id`,`object_id`),"
            + "INDEX `created_by_index` (`cid`, `created_by`),"
            + "INDEX `shared_by_index` (`cid`, `shared_by`),"
            + "INDEX `last_modified_index` (`cid`, `permission_id`, `module`, `last_modified`)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci")
        };
    }

    @Override
    public String[] requiredTables() {
        return new String[] { "user" };
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { "object_permission", "del_object_permission" };
    }

}
