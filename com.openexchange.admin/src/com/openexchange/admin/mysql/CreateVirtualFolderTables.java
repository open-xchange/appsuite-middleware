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

package com.openexchange.admin.mysql;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * Creates the tables required for virtual folders.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class CreateVirtualFolderTables extends AbstractCreateTableImpl {

    /**
     * Table name of virtualTree table
     */
    private static final String TABLE_VIRTUAL_TREE = "virtualTree";

    /**
     * SQL statement for virtualTree table
     */
    private static final String CREATE_VIRTUAL_TREE = "CREATE TABLE " + TABLE_VIRTUAL_TREE + " ("
        + "cid INT4 unsigned NOT NULL,"
        + "tree INT4 unsigned NOT NULL,"
        + "user INT4 unsigned NOT NULL,"
        + "folderId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,"
        + "parentId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,"
        + "name VARCHAR(767) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,"
        + "lastModified BIGINT(64) DEFAULT NULL,"
        + "modifiedBy INT4 unsigned DEFAULT NULL,"
        + "shadow VARCHAR(192) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,"
        + "sortNum INT4 unsigned DEFAULT NULL,"
        + "PRIMARY KEY (cid, tree, user, folderId),"
        + "INDEX (cid, tree, user, parentId(191)),"
        + "INDEX (cid, tree, user, shadow(191)),"
        + "INDEX (cid, user),"
        + "INDEX (cid, modifiedBy)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    /**
     * Table name of virtualPermission table
     */
    private static final String TABLE_VIRTUAL_PERMISSION = "virtualPermission";

    /**
     * SQL statement for virtualPermission table
     */
    private static final String CREATE_VIRTUAL_PERMISSION = "CREATE TABLE " + TABLE_VIRTUAL_PERMISSION + " ("
        + "cid INT4 unsigned NOT NULL,"
        + "tree INT4 unsigned NOT NULL,"
        + "user INT4 unsigned NOT NULL,"
        + "folderId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,"
        + "entity INT4 unsigned NOT NULL,"
        + "fp tinyint(3) unsigned NOT NULL,"
        + "orp tinyint(3) unsigned NOT NULL,"
        + "owp tinyint(3) unsigned NOT NULL,"
        + "odp tinyint(3) unsigned NOT NULL,"
        + "adminFlag tinyint(3) unsigned NOT NULL,"
        + "groupFlag tinyint(3) unsigned NOT NULL,"
        + "system tinyint(3) unsigned NOT NULL default '0',"
        + "`type` INT4 UNSIGNED NOT NULL default '0',"
        + "`sharedParentFolder` INT4 UNSIGNED,"
        + "PRIMARY KEY (cid, tree, user, folderId, entity)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    /**
     * Table name of virtualSubscription table
     */
    private static final String TABLE_VIRTUAL_SUBSCRIPTION = "virtualSubscription";

    /**
     * SQL statement for virtualSubscription table
     */
    private static final String CREATE_VIRTUAL_SUBSCRIPTION = "CREATE TABLE " + TABLE_VIRTUAL_SUBSCRIPTION + " ("
        + "cid INT4 unsigned NOT NULL,"
        + "tree INT4 unsigned NOT NULL,"
        + "user INT4 unsigned NOT NULL,"
        + "folderId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,"
        + "subscribed tinyint(3) unsigned NOT NULL,"
        + "PRIMARY KEY (cid, tree, user, folderId)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    /**
     * Table name of virtualBackupTree table
     */
    private static final String TABLE_VIRTUAL_BACKUP_TREE = "virtualBackupTree";

    /**
     * SQL statement for virtualBackupTree table
     */
    private static final String CREATE_VIRTUAL_BACKUP_TREE = "CREATE TABLE " + TABLE_VIRTUAL_BACKUP_TREE + " ("
        + "cid INT4 unsigned NOT NULL,"
        + "tree INT4 unsigned NOT NULL,"
        + "user INT4 unsigned NOT NULL,"
        + "folderId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,"
        + "parentId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,"
        + "name VARCHAR(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',"
        + "lastModified BIGINT(64) DEFAULT NULL,"
        + "modifiedBy INT4 unsigned DEFAULT NULL,"
        + "shadow VARCHAR(192) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,"
        + "sortNum INT4 unsigned DEFAULT NULL,"
        + "PRIMARY KEY (cid, tree, user, folderId),"
        + "INDEX (cid, tree, user, parentId(191)),"
        + "INDEX (cid, tree, user, shadow(191)),"
        + "INDEX (cid, user),"
        + "INDEX (cid, modifiedBy)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    /**
     * Table name of virtualBackupPermission table
     */
    private static final String TABLE_VIRTUAL_BACKUP_PERMISSION = "virtualBackupPermission";

    /**
     * SQL statement for virtualBackupPermission table
     */
    private static final String CREATE_VIRTUAL_BACKUP_PERMISSION = "CREATE TABLE " + TABLE_VIRTUAL_BACKUP_PERMISSION + " ("
        + "cid INT4 unsigned NOT NULL,"
        + "tree INT4 unsigned NOT NULL,"
        + "user INT4 unsigned NOT NULL,"
        + "folderId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,"
        + "entity INT4 unsigned NOT NULL,"
        + "fp tinyint(3) unsigned NOT NULL,"
        + "orp tinyint(3) unsigned NOT NULL,"
        + "owp tinyint(3) unsigned NOT NULL,"
        + "odp tinyint(3) unsigned NOT NULL,"
        + "adminFlag tinyint(3) unsigned NOT NULL,"
        + "groupFlag tinyint(3) unsigned NOT NULL,"
        + "system tinyint(3) unsigned NOT NULL default '0',"
        + "`type` INT4 UNSIGNED NOT NULL default '0',"
        + "`sharedParentFolder` INT4 UNSIGNED,"
        + "PRIMARY KEY (cid, tree, user, folderId, entity)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    /**
     * Table name of virtualBackupSubscription table
     */
    private static final String TABLE_VIRTUAL_BACKUP_SUBSCRIPTION = "virtualBackupSubscription";

    /**
     * SQL statement for virtualBackupSubscription table
     */
    private static final String CREATE_VIRTUAL_BACKUP_SUBSCRIPTION = "CREATE TABLE " + TABLE_VIRTUAL_BACKUP_SUBSCRIPTION + " ("
        + "cid INT4 unsigned NOT NULL,"
        + "tree INT4 unsigned NOT NULL,"
        + "user INT4 unsigned NOT NULL,"
        + "folderId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,"
        + "subscribed tinyint(3) unsigned NOT NULL,"
        + "PRIMARY KEY (cid, tree, user, folderId)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    /**
     * Initializes a new {@link CreateVirtualFolderTables}.
     */
    public CreateVirtualFolderTables() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] tablesToCreate() {
        return new String[] {
            TABLE_VIRTUAL_TREE, TABLE_VIRTUAL_PERMISSION, TABLE_VIRTUAL_SUBSCRIPTION, TABLE_VIRTUAL_BACKUP_TREE,
            TABLE_VIRTUAL_BACKUP_PERMISSION, TABLE_VIRTUAL_BACKUP_SUBSCRIPTION };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getCreateStatements() {
        return new String[] {
            CREATE_VIRTUAL_TREE, CREATE_VIRTUAL_PERMISSION, CREATE_VIRTUAL_SUBSCRIPTION,
            CREATE_VIRTUAL_BACKUP_TREE, CREATE_VIRTUAL_BACKUP_PERMISSION, CREATE_VIRTUAL_BACKUP_SUBSCRIPTION };
    }
}
