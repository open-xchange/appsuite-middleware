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
 * {@link CreateInfostoreTables}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class CreateInfostoreTables extends AbstractCreateTableImpl {

    private static final String infostoreTableName = "infostore";
    private static final String infostoreDocumentsTableName = "infostore_document";
    private static final String delInfostoreTableName = "del_infostore";
    private static final String delInfostoreDocumentTableName = "del_infostore_document";
    private static final String infostorePropertyTableName = "infostore_property";
    private static final String infostoreLockTableName = "infostore_lock";
    private static final String lockNullTableName = "lock_null";
    private static final String lockNullLockTableName = "lock_null_lock";

    private static final String createInfostoreTable = "CREATE TABLE `infostore` ("
      + "`cid` int4 unsigned NOT NULL,"
      + "`id` int4 unsigned NOT NULL,"
      + "`folder_id` int4 unsigned NOT NULL,"
      + "`version` int4 unsigned NOT NULL,"
      + "`locked_until` int8 unsigned ,"
      + "`color_label` int4 unsigned NOT NULL,"
      + "`creating_date` int8 NOT NULL,"
      + "`last_modified` int8 NOT NULL,"
      + "`created_by` int4 unsigned NOT NULL,"
      + "`changed_by` int4 unsigned ,"
      + "PRIMARY KEY (`cid`,`id`, `folder_id`),"
      + "INDEX `lastModified` (`cid`,`last_modified`),"
      + "INDEX `folder` (`cid`,`folder_id`)"
    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createInfostoreDocumentTable = "CREATE TABLE `infostore_document` ("
      + "`cid` int4 unsigned NOT NULL,"
      + "`infostore_id` int4 unsigned NOT NULL,"
      + "`version_number` int4 unsigned NOT NULL,"
      + "`creating_date` int8 NOT NULL,"
      + "`last_modified` int8 NOT NULL,"
      + "`created_by` int4 unsigned NOT NULL,"
      + "`changed_by` int4 unsigned ,"
      + "`title` varchar(767) ,"
      + "`url` varchar(256) ,"
      + "`description` text,"
      + "`categories` varchar(255) ,"
      + "`filename` varchar(767) ,"
      + "`file_store_location` varchar(255) ,"
      + "`file_size` bigint(20) ,"
      + "`file_mimetype` varchar(255) ,"
      + "`file_md5sum` varchar(32)  ,"
      + "`file_version_comment` text,"
      + "`meta` BLOB default NULL,"
      + "PRIMARY KEY (`cid`,`infostore_id`,`version_number`),"
      + "FOREIGN KEY (cid, infostore_id) REFERENCES infostore (cid, id),"
      + "INDEX `md5sumIndex` (`cid`, `file_md5sum`)"
    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createDelInfostoreTable = "CREATE TABLE `del_infostore` ("
      + "`cid` int4 unsigned NOT NULL,"
      + "`id` int4 unsigned NOT NULL,"
      + "`folder_id` int4 unsigned NOT NULL,"
      + "`version` int4 unsigned NOT NULL,"
      + "`color_label` int4 unsigned NOT NULL,"
      + "`creating_date` int8 NOT NULL,"
      + "`last_modified` int8 NOT NULL,"
      + "`created_by` int4 unsigned NOT NULL,"
      + "`changed_by` int4 unsigned ,"
      + "PRIMARY KEY (`cid`,`id`,`folder_id`),"
      + "INDEX `lastModified` (`cid`,`last_modified`),"
      + "INDEX `folder` (`cid`,`folder_id`)"
    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createDelInfostoreDocumentTable = "CREATE TABLE `del_infostore_document` ("
      + "`cid` int4 unsigned NOT NULL,"
      + "`infostore_id` int4 unsigned NOT NULL,"
      + "`version_number` int4 unsigned NOT NULL,"
      + "`creating_date` int8 NOT NULL,"
      + "`last_modified` int8 NOT NULL,"
      + "`created_by` int4 unsigned NOT NULL,"
      + "`changed_by` int4 unsigned ,"
      + "`title` varchar(128) ,"
      + "`url` varchar(256) ,"
      + "`description` text ,"
      + "`categories` varchar(255) ,"
      + "`filename` varchar(255) ,"
      + "`file_store_location` varchar(255) ,"
      + "`file_size` int4 unsigned ,"
      + "`file_mimetype` varchar(255) ,"
      + "`file_md5sum` varchar(32) ,"
      + "`file_version_comment` text ,"
      + "`meta` BLOB default NULL,"
      + "PRIMARY KEY (`cid`,`infostore_id`,`version_number`),"
      + "INDEX `md5sumIndex` (`cid`, `file_md5sum`)"
    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createInfostorePropertyTable = "CREATE TABLE `infostore_property` ("
      + "`cid` int4 unsigned NOT NULL,"
      + "`id` int4 unsigned NOT NULL,"
      + "`name` varchar(128) NOT NULL,"
      + "`namespace` varchar(128) NOT NULL,"
      + "`value` varchar(255) ,"
      + "`language` varchar(128) ,"
      + "`xml` boolean,"
      + "PRIMARY KEY (`cid`,`id`,`name`,`namespace`)"
    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createInfostoreLockTable = "CREATE TABLE `infostore_lock` ("
      + "`cid` int4 unsigned NOT NULL,"
      + "`id` int4 unsigned NOT NULL,"
      + "`userid` int4 unsigned NOT NULL,"
      + "`entity` int4 unsigned ,"
      + "`timeout` int8 NOT NULL,"
      + "`type` tinyint unsigned NOT NULL,"
      + "`scope` tinyint unsigned NOT NULL,"
      + "`ownerDesc` varchar(128) ,"
      + "PRIMARY KEY (`cid`,`id`)"
    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createLockNullTable = "CREATE TABLE `lock_null` ("
      + "`cid` int4 unsigned NOT NULL,"
      + "`id` int4 unsigned NOT NULL,"
      + "`url` varchar(255) NOT NULL,"
      + "PRIMARY KEY (`cid`,`id`)"
    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createLockNullLockTable = "CREATE TABLE `lock_null_lock` ("
      + "`cid` int4 unsigned NOT NULL,"
      + "`id` int4 unsigned NOT NULL,"
      + "`userid` int4 unsigned ,"
      + "`entity` int4 unsigned ,"
      + "`timeout` int8,"
      + "`type` tinyint unsigned ,"
      + "`scope` tinyint unsigned ,"
      + "`ownerDesc` varchar(128) ,"
      + "PRIMARY KEY (`cid`,`id`)"
    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Initializes a new {@link CreateInfostoreTables}.
     */
    public CreateInfostoreTables() {
        super();
    }

    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { infostoreTableName, infostoreDocumentsTableName, delInfostoreTableName,
            delInfostoreDocumentTableName, infostorePropertyTableName, infostoreLockTableName, lockNullTableName,
            lockNullLockTableName };
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { createInfostoreTable, createInfostoreDocumentTable, createDelInfostoreTable,
            createDelInfostoreDocumentTable, createInfostorePropertyTable, createInfostoreLockTable, createLockNullTable,
            createLockNullLockTable };
    }

}
