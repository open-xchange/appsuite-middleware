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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.update.tasks;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.FullPrimaryKeySupportService;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.update.Tools;

/**
 * {@link CreateGenconfTablesTask}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CreateGenconfTablesTask implements UpdateTask {

    private static final String STRING_TABLE_CREATE = "CREATE TABLE `genconf_attributes_strings` ( "+
   "`cid` INT4 unsigned NOT NULL,"+
   "`id` INT4 unsigned NOT NULL,"+
   "`name` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,"+
   "`value` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,"+
   "`uuid` BINARY(16) DEFAULT NULL,"+
   "KEY (`cid`,`id`,`name`)"+
   ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    
    private static final String STRING_TABLE_CREATE_PRIMARY_KEY = "CREATE TABLE `genconf_attributes_strings` ( "+
        "`cid` INT4 unsigned NOT NULL,"+
        "`id` INT4 unsigned NOT NULL,"+
        "`name` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,"+
        "`value` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,"+
        "`uuid` BINARY(16) NOT NULL,"+
        "PRIMARY KEY (cid, id, uuid),"+
        "KEY (`cid`,`id`,`name`)"+
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String BOOL_TABLE_CREATE = "CREATE TABLE `genconf_attributes_bools` ("+
    "`cid` INT4 unsigned NOT NULL,"+
   "`id` INT4 unsigned NOT NULL,"+
   "`name` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,"+
   "`value` tinyint(1) COLLATE utf8_unicode_ci DEFAULT NULL,"+
   "`uuid` BINARY(16) DEFAULT NULL,"+
   "KEY (`cid`,`id`,`name`)"+
   ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    
    private static final String BOOL_TABLE_CREATE_PRIMARY_KEY = "CREATE TABLE `genconf_attributes_bools` ("+
        "`cid` INT4 unsigned NOT NULL,"+
       "`id` INT4 unsigned NOT NULL,"+
       "`name` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,"+
       "`value` tinyint(1) COLLATE utf8_unicode_ci DEFAULT NULL,"+
       "`uuid` BINARY(16) NOT NULL,"+
       "PRIMARY KEY (cid, id, uuid),"+
       "KEY (`cid`,`id`,`name`)"+
       ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private static final String SEQUENCE_TABLE_CREATE = "CREATE TABLE `sequence_genconf` ("+
    "`cid` INT4 unsigned NOT NULL,"+
    "`id` INT4 unsigned NOT NULL,"+
    "PRIMARY KEY (`cid`)"+
    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

    private static final String INSERT_IN_SEQUENCE = "INSERT INTO sequence_genconf (cid, id) VALUES (?, 0)";

    @Override
    public int addedWithVersion() {
        return 44;
    }

    @Override
    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    @Override
    public void perform(final Schema schema, final int contextId) throws OXException {
        Connection con = null;
        try {
            con = Database.getNoTimeout(contextId, true);
            FullPrimaryKeySupportService fullPrimaryKeySupportService = ServerServiceRegistry.getInstance().getService(FullPrimaryKeySupportService.class);
            if(!Tools.tableExists(con, "genconf_attributes_strings")) {
                if (fullPrimaryKeySupportService.isFullPrimaryKeySupported()) {
                    Tools.exec(con, STRING_TABLE_CREATE_PRIMARY_KEY);
                } else {
                    Tools.exec(con, STRING_TABLE_CREATE);
                }
            }
            if(!Tools.tableExists(con, "genconf_attributes_bools")) {
                if (fullPrimaryKeySupportService.isFullPrimaryKeySupported()) {
                    Tools.exec(con, BOOL_TABLE_CREATE_PRIMARY_KEY);
                } else {
                    Tools.exec(con, BOOL_TABLE_CREATE);
                }
            }
            if(!Tools.tableExists(con, "sequence_genconf")) {
                Tools.exec(con, SEQUENCE_TABLE_CREATE);
            }
            for(final int ctxId : Tools.getContextIDs(con)) {
                if(!Tools.hasSequenceEntry("sequence_genconf", con, ctxId)) {
                    Tools.exec(con, INSERT_IN_SEQUENCE, ctxId);
                }
            }
        } catch (final SQLException e) {
            throw createSQLError(e);
        } finally {
            if(con != null) {
                Database.backNoTimeout(contextId, true, con);
            }
        }
    }

    private static OXException createSQLError(final SQLException e) {
        return UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
    }
}
