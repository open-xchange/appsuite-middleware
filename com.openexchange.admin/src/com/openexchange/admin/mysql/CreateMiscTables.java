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
 * {@link CreateMiscTables}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class CreateMiscTables extends AbstractCreateTableImpl {

    private static final String prgLinksTableName = "prg_links";
    private static final String reminderTableName = "reminder";
    private static final String filestoreUsageTableName = "filestore_usage";

    private static final String createPrgLinksTablePrimaryKey = "CREATE TABLE prg_links ("
        + "firstid INT4 UNSIGNED NOT NULL,"
        + "firstmodule INT4 UNSIGNED NOT NULL,"
        + "firstfolder INT4 UNSIGNED NOT NULL,"
        + "secondid INT4 UNSIGNED NOT NULL,"
        + "secondmodule INT4 UNSIGNED NOT NULL,"
        + "secondfolder INT4 UNSIGNED NOT NULL,"
        + "cid INT4 UNSIGNED NOT NULL,"
        + "last_modified INT8,"
        + "created_by INT4 UNSIGNED,"
        + "uuid BINARY(16) NOT NULL,"
        + "PRIMARY KEY (cid, uuid),"
        + "INDEX (firstid),"
        + "INDEX (secondid),"
        + "INDEX (cid)"
      + ") ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createReminderTable = "CREATE TABLE reminder ("
        + "cid INT4 UNSIGNED NOT NULL,"
        + "object_id INT4 UNSIGNED NOT NULL,"
        + "last_modified INT8 UNSIGNED,"
        + "target_id VARCHAR(255) NOT NULL,"
        + "module INT1 UNSIGNED NOT NULL,"
        + "userid INT4 UNSIGNED NOT NULL,"
        + "alarm DATETIME NOT NULL,"
        + "recurrence TINYINT NOT NULL,"
        + "description VARCHAR(1028),"
        + "folder VARCHAR(1028),"
        + "PRIMARY KEY (cid,object_id),"
        + "INDEX (cid,userid,alarm),"
        + "INDEX (cid,userid,last_modified),"
        + "CONSTRAINT reminder_unique UNIQUE (cid,target_id,module,userid)"
      + ") ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String createFilestoreUsageTable = "CREATE TABLE filestore_usage ("
        + "cid INT4 UNSIGNED NOT NULL,"
        + "user INT4 UNSIGNED NOT NULL DEFAULT 0,"
        + "used INT8 NOT NULL,"
        + "PRIMARY KEY(cid, user)"
      + ") ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Initializes a new {@link CreateMiscTables}.
     */
    public CreateMiscTables() {
        super();
    }

    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { prgLinksTableName, reminderTableName, filestoreUsageTableName };
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { createPrgLinksTablePrimaryKey, createReminderTable, createFilestoreUsageTable };
    }

}
