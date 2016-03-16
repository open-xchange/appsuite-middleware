/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.admin.mysql;

import com.openexchange.database.AbstractCreateTableImpl;


/**
 * {@link CreateOXFolderTables}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class CreateOXFolderTables extends AbstractCreateTableImpl {

    private static final String oxfolderTreeTableName = "oxfolder_tree";
    private static final String oxfolderPermissionsTableName = "oxfolder_permissions";
    private static final String oxfolderSpecialfoldersTableName = "oxfolder_specialfolders";
    private static final String oxfolderUserfoldersTableName = "oxfolders_userfolders";
    private static final String oxfolderUserfoldersStandardfoldersTableName = "oxfolder_userfolders_standardfolders";
    private static final String delOxfolderTreeTableName = "del_oxfolder_tree";
    private static final String delOxfolderPermissionsTableName = "del_oxfolder_permissions";
    private static final String oxfolderLockTableName = "oxfolder_lock";
    private static final String oxfolderPropertyTableName = "oxfolder_property";

    private static final String createOxfolderTreeTable = "CREATE TABLE `oxfolder_tree` ("
       + "`fuid` INT4 UNSIGNED NOT NULL,"
       + "`cid` INT4 UNSIGNED NOT NULL,"
       + "`parent` INT4 UNSIGNED NOT NULL,"
       + "`fname` VARCHAR(767) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,"
       + "`module` TINYINT UNSIGNED NOT NULL,"
       + "`type` TINYINT UNSIGNED NOT NULL,"
       + "`creating_date` BIGINT(64) NOT NULL,"
       + "`created_from` INT4 UNSIGNED NOT NULL,"
       + "`changing_date` BIGINT(64) NOT NULL,"
       + "`changed_from` INT4 UNSIGNED NOT NULL,"
       + "`permission_flag` TINYINT UNSIGNED NOT NULL,"
       + "`subfolder_flag` TINYINT UNSIGNED NOT NULL,"
       + "`default_flag` TINYINT UNSIGNED NOT NULL default '0',"
       + "`meta` BLOB default NULL,"
       + "PRIMARY KEY (`cid`, `fuid`),"
       + "INDEX `parentIndex` (`cid`, `parent`),"
       + "INDEX `typeIndex` (`cid`, `type`),"
       + "INDEX `moduleIndex` (`cid`, `module`),"
       + "INDEX `lastModifiedIndex` (`cid`, `changing_date`, `module`),"
       + "FOREIGN KEY (`cid`, `created_from`) REFERENCES user (`cid`, `id`),"
       + "FOREIGN KEY (`cid`, `changed_from`) REFERENCES user (`cid`, `id`)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createOxfolderPermissionsTable = "CREATE TABLE `oxfolder_permissions` ("
       + "`cid` INT4 UNSIGNED NOT NULL,"
       + "`fuid` INT4 UNSIGNED NOT NULL,"
       + "`permission_id` INT4 UNSIGNED NOT NULL,"
       + "`fp` TINYINT UNSIGNED NOT NULL,"
       + "`orp` TINYINT UNSIGNED NOT NULL,"
       + "`owp` TINYINT UNSIGNED NOT NULL,"
       + "`odp` TINYINT UNSIGNED NOT NULL,"
       + "`admin_flag` TINYINT UNSIGNED NOT NULL,"
       + "`group_flag` TINYINT UNSIGNED NOT NULL,"
       + "`system` TINYINT UNSIGNED NOT NULL default '0',"
       + "PRIMARY KEY  (`cid`, `fuid`, `permission_id`, `system`),"
       + "INDEX `principal` (`cid`, `permission_id`, `fuid`),"
       + "FOREIGN KEY (`cid`, `fuid`) REFERENCES oxfolder_tree (`cid`, `fuid`)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createOxfolderSpecialfoldersTable = "CREATE TABLE `oxfolder_specialfolders` ("
        + "`tag` VARCHAR(16) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,"
        + "`cid` INT4 UNSIGNED NOT NULL,"
        + "`fuid` INT4 UNSIGNED NOT NULL,"
        + "PRIMARY KEY (`cid`,`fuid`,`tag`),"
        + "FOREIGN KEY (`cid`, `fuid`) REFERENCES oxfolder_tree (`cid`, `fuid`)"
      + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createOxfolderUserfoldersTable = "CREATE TABLE `oxfolder_userfolders` ("
       + "`module` TINYINT UNSIGNED NOT NULL,"
       + "`cid` INT4 UNSIGNED NOT NULL,"
       + "`linksite` VARCHAR(32) NOT NULL,"
       + "`target` VARCHAR(32) NOT NULL,"
       + "`img` VARCHAR(32) NOT NULL,"
       + "PRIMARY KEY (`cid`,`module`)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createOxfolderUserfoldersStandardfoldersTable = "CREATE TABLE `oxfolder_userfolders_standardfolders` ("
       + "`owner` INT4 UNSIGNED NOT NULL,"
       + "`cid` INT4 UNSIGNED NOT NULL,"
       + "`module` TINYINT UNSIGNED NOT NULL,"
       + "`fuid` INT4 UNSIGNED NOT NULL,"
       + "PRIMARY KEY (`owner`, `cid`, `module`, `fuid`),"
       + "FOREIGN KEY (`cid`, `fuid`) REFERENCES oxfolder_tree (`cid`, `fuid`)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createDelOxfolderTreeTable = "CREATE TABLE `del_oxfolder_tree` ("
       + "`fuid` INT4 UNSIGNED NOT NULL,"
       + "`cid` INT4 UNSIGNED NOT NULL,"
       + "`parent` INT4 UNSIGNED NOT NULL,"
       + "`fname` VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '',"
       + "`module` TINYINT UNSIGNED NOT NULL,"
       + "`type` TINYINT UNSIGNED NOT NULL,"
       + "`creating_date` BIGINT(64) NOT NULL,"
       + "`created_from` INT4 UNSIGNED NOT NULL,"
       + "`changing_date` BIGINT(64) NOT NULL,"
       + "`changed_from` INT4 UNSIGNED NOT NULL,"
       + "`permission_flag` TINYINT UNSIGNED NOT NULL,"
       + "`subfolder_flag` TINYINT UNSIGNED NOT NULL,"
       + "`default_flag` TINYINT UNSIGNED NOT NULL default '0',"
       + "`meta` BLOB default NULL,"
       + "PRIMARY KEY (`cid`, `fuid`),"
       + "INDEX `parentIndex` (`cid`, `parent`),"
       + "INDEX `typeIndex` (`cid`, `type`),"
       + "INDEX `moduleIndex` (`cid`, `module`),"
       + "INDEX `lastModifiedIndex` (`cid`, `changing_date`, `module`),"
       + "FOREIGN KEY (`cid`, `created_from`) REFERENCES user (`cid`, `id`),"
       + "FOREIGN KEY (`cid`, `changed_from`) REFERENCES user (`cid`, `id`)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createDelOxfolderPermissionsTable = "CREATE TABLE `del_oxfolder_permissions` ("
       + "`cid` INT4 UNSIGNED NOT NULL,"
       + "`fuid` INT4 UNSIGNED NOT NULL,"
       + "`permission_id` INT4 UNSIGNED NOT NULL,"
       + "`fp` TINYINT UNSIGNED NOT NULL,"
       + "`orp` TINYINT UNSIGNED NOT NULL,"
       + "`owp` TINYINT UNSIGNED NOT NULL,"
       + "`odp` TINYINT UNSIGNED NOT NULL,"
       + "`admin_flag` TINYINT UNSIGNED NOT NULL,"
       + "`group_flag` TINYINT UNSIGNED NOT NULL,"
       + "`system` TINYINT UNSIGNED NOT NULL default '0',"
       + "PRIMARY KEY  (`cid`,`fuid`,`permission_id`,`system`),"
       + "INDEX `principal` (`cid`, `permission_id`, `fuid`),"
       + "FOREIGN KEY (`cid`, `fuid`) REFERENCES del_oxfolder_tree (`cid`, `fuid`)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createOxfolderLockTable = "CREATE TABLE `oxfolder_lock` ("
       + "`cid` INT4 UNSIGNED NOT NULL,"
       + "`id` INT4 UNSIGNED NOT NULL,"
       + "`userid` INT4 UNSIGNED NOT NULL,"
       + "`entity` INT4 UNSIGNED default NULL,"
       + "`timeout` BIGINT(64) UNSIGNED NOT NULL,"
       + "`depth` TINYINT default NULL,"
       + "`type` TINYINT UNSIGNED NOT NULL,"
       + "`scope` TINYINT UNSIGNED NOT NULL,"
       + "`ownerDesc` VARCHAR(128) default NULL,"
       + "PRIMARY KEY (cid, id)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createOxfolderPropertyTable = "CREATE TABLE `oxfolder_property` ("
       + "`cid` INT4 UNSIGNED NOT NULL,"
       + "`id` INT4 UNSIGNED NOT NULL,"
       + "`name` VARCHAR(128) COLLATE utf8_unicode_ci NOT NULL,"
       + "`namespace` VARCHAR(128) COLLATE utf8_unicode_ci NOT NULL,"
       + "`value` VARCHAR(255) COLLATE utf8_unicode_ci default NULL,"
       + "`language` VARCHAR(128) COLLATE utf8_unicode_ci default NULL,"
       + "`xml` BOOLEAN default NULL,"
       + "PRIMARY KEY (cid, id, name, namespace)"
     + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Initializes a new {@link CreateOXFolderTables}.
     */
    public CreateOXFolderTables() {
        super();
    }

    /* (non-Javadoc)
     * @see com.openexchange.database.CreateTableService#requiredTables()
     */
    @Override
    public String[] requiredTables() {
        return new String[] { "user" };
    }

    /* (non-Javadoc)
     * @see com.openexchange.database.CreateTableService#tablesToCreate()
     */
    @Override
    public String[] tablesToCreate() {
        return new String[] { oxfolderTreeTableName, oxfolderPermissionsTableName, oxfolderSpecialfoldersTableName,
            oxfolderUserfoldersTableName, oxfolderUserfoldersStandardfoldersTableName, delOxfolderTreeTableName,
            delOxfolderPermissionsTableName, oxfolderLockTableName, oxfolderPropertyTableName };
    }

    /* (non-Javadoc)
     * @see com.openexchange.database.AbstractCreateTableImpl#getCreateStatements()
     */
    @Override
    protected String[] getCreateStatements() {
        return new String[] { createOxfolderTreeTable, createOxfolderPermissionsTable, createOxfolderSpecialfoldersTable,
            createOxfolderUserfoldersTable, createOxfolderUserfoldersStandardfoldersTable, createDelOxfolderTreeTable,
            createDelOxfolderPermissionsTable, createOxfolderLockTable, createOxfolderPropertyTable };
    }

}
