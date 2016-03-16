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
      + "PRIMARY KEY  (`cid`,`id`),"
      + "KEY `cid` (`cid`,`attached`,`module`)"
    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    
    private static final String createDelAttachmentTable = "CREATE TABLE `del_attachment` ("
      + "`cid` INT4 UNSIGNED NOT NULL,"
      + "`id` INT4 UNSIGNED NOT NULL,"
      + "`attached` INT4 UNSIGNED NOT NULL,"
      + "`module` INT4 UNSIGNED NOT NULL,"
      + "`del_date` INT8 NOT NULL,"
      + "PRIMARY KEY  (`cid`,`id`),"
      + "KEY `cid` (`cid`,`attached`,`module`)"
    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Initializes a new {@link CreateAttachmentTables}.
     */
    public CreateAttachmentTables() {
        super();
    }

    /* (non-Javadoc)
     * @see com.openexchange.database.CreateTableService#requiredTables()
     */
    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    /* (non-Javadoc)
     * @see com.openexchange.database.CreateTableService#tablesToCreate()
     */
    @Override
    public String[] tablesToCreate() {
        return new String[] { prgAttachmentTableName, delAttachmentTableName };
    }

    /* (non-Javadoc)
     * @see com.openexchange.database.AbstractCreateTableImpl#getCreateStatements()
     */
    @Override
    protected String[] getCreateStatements() {
        return new String[] { createPrgAttachmentTable, createDelAttachmentTable };
    }

}
