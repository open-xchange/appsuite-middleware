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
 * {@link CreateAttachmentTables}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class CreateAttachmentTables extends AbstractCreateTableImpl {

    private static final String prgAttachmentTableName = "prg_attachment";
    private static final String delAttachmentTableName = "del_attachment";

    private static final String createPrgAttachmentTable = "CREATE TABLE `prg_attachment` ("
      + "`cid` INT4 UNSIGNED NOT NULL,"
      + "`id` INT4 UNSIGNED NOT NULL,"
      + "`created_by` INT4 UNSIGNED NOT NULL,"
      + "`creation_date` INT8 NOT NULL,"
      + "`file_mimetype` varchar(255) NOT NULL,"
      + "`file_size` INT4 UNSIGNED NOT NULL,"
      + "`filename` varchar(255) NOT NULL,"
      + "`attached` INT4 UNSIGNED NOT NULL,"
      + "`module` INT4 UNSIGNED NOT NULL,"
      + "`rtf_flag` boolean,"
      + "`comment` varchar(255),"
      + "`file_id` varchar(255) NOT NULL,"
      + "`checksum` varchar(32) DEFAULT NULL,"
      + "PRIMARY KEY  (`cid`,`id`),"
      + "KEY `cid` (`cid`,`attached`,`module`)"
    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private static final String createDelAttachmentTable = "CREATE TABLE `del_attachment` ("
      + "`cid` INT4 UNSIGNED NOT NULL,"
      + "`id` INT4 UNSIGNED NOT NULL,"
      + "`attached` INT4 UNSIGNED NOT NULL,"
      + "`module` INT4 UNSIGNED NOT NULL,"
      + "`del_date` INT8 NOT NULL,"
      + "`checksum` varchar(32) DEFAULT NULL,"
      + "PRIMARY KEY  (`cid`,`id`),"
      + "KEY `cid` (`cid`,`attached`,`module`)"
    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    /**
     * Initializes a new {@link CreateAttachmentTables}.
     */
    public CreateAttachmentTables() {
        super();
    }

    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { prgAttachmentTableName, delAttachmentTableName };
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { createPrgAttachmentTable, createDelAttachmentTable };
    }

}
