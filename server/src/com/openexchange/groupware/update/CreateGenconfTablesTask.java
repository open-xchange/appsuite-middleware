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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.update;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.database.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.update.UpdateTask.UpdateTaskPriority;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.UpdateException;
import com.openexchange.groupware.update.exception.UpdateExceptionFactory;


/**
 * {@link CreateGenconfTablesTask}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
@OXExceptionSource(classId = Classes.UPDATE_TASK, component = EnumComponent.UPDATE)
public class CreateGenconfTablesTask implements UpdateTask {
    private static final UpdateExceptionFactory EXCEPTION = new UpdateExceptionFactory(CreateGenconfTablesTask.class);
    
    private static final String STRING_TABLE_CREATE = "CREATE TABLE `genconf_attributes_strings` ( "+
   "`cid` int(10) unsigned NOT NULL,"+
   "`id` int(10) unsigned NOT NULL,"+
   "`name` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,"+
   "`value` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,"+
   "`widget` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,"+
   "KEY (`cid`,`id`,`name`)"+
   ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    
    private static final String BOOL_TABLE_CREATE = "CREATE TABLE `genconf_attributes_bools` ("+
    "`cid` int(10) unsigned NOT NULL,"+
   "`id` int(10) unsigned NOT NULL,"+
   "`name` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,"+
   "`value` tinyint(1) COLLATE utf8_unicode_ci DEFAULT NULL,"+
   "`widget` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,"+
   "KEY (`cid`,`id`,`name`)"+
   ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    
    private static final String SEQUENCE_TABLE_CREATE = "CREATE TABLE `sequence_genconf` ("+
    "`cid` int(10) unsigned NOT NULL,"+
    "`id` int(10) unsigned NOT NULL,"+
    "PRIMARY KEY (`cid`)"+
    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";
    
    private static final String INSERT_IN_SEQUENCE = "INSERT INTO sequence_genconf (cid, id) VALUES (?, 0)";
    
    public int addedWithVersion() {
        return 36;
    }

    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    public void perform(Schema schema, int contextId) throws AbstractOXException {
        Connection con = null;
        try {
            con = Database.getNoTimeout(contextId, true);
            if(!existsTable(con, "genconf_attributes_strings")) {
                exec(con, STRING_TABLE_CREATE);
            }
            if(!existsTable(con, "genconf_attributes_bools")) {
                exec(con, BOOL_TABLE_CREATE);
            }
            if(!existsTable(con, "sequence_genconf")) {
                exec(con, SEQUENCE_TABLE_CREATE);
            }
            for(int ctxId : getContextIDs(con)) {
                if(!hasSequenceEntry(con, ctxId)) {
                    exec(con, INSERT_IN_SEQUENCE, contextId);
                }
            }
        } catch (SQLException e) {
            throw createSQLError(e);
        } finally {
            if(con != null) {
                Database.back(contextId, true, con);
            }
        }
    }
    private boolean hasSequenceEntry(Connection con, int ctxId) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT 1 FROM sequence_genconf WHERE cid = "+ctxId);
            rs = stmt.executeQuery();
            return rs.next();
        } finally {
            if(rs != null) {
                rs.close();
            }
            if(stmt != null) {
                stmt.close();
            }
        }

    }

    private List<Integer> getContextIDs(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Integer> contextIds = new LinkedList<Integer>();
        try {
            stmt = con.prepareStatement("SELECT DISTINCT cid FROM user");
            rs = stmt.executeQuery();
            while(rs.next()) {
                contextIds.add(rs.getInt(1));
            }
            return contextIds;
        } finally {
            if(rs != null) {
                rs.close();
            }
            if(stmt != null) {
                stmt.close();
            }
        }
    }

    private boolean existsTable(Connection con, String tableName) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SHOW TABLES");
            rs = stmt.executeQuery();
            while(rs.next()) {
                String table = rs.getString(1);
                if(table.equals(tableName)) {
                    return true;
                }
            }
            return false;
        } finally {
            if(rs != null) {
                rs.close();
            }
            if(stmt != null) {
                stmt.close();
            }
        }
    }

    private void exec(Connection con, String sql, Object...args) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = con.prepareStatement(sql);
            int i = 1;
            for(Object arg : args) {
                statement.setObject(i++, arg);
            }
            statement.execute();
        } finally {
            if(statement != null) {
                statement.close();
            }
        }
        
    }
    
    @OXThrowsMultiple(
        category = { Category.CODE_ERROR },
        desc = { "" },
        exceptionId = { 1 },
        msg = { "A SQL error occurred while performing task CreateGenconfTablesTask: %1$s." }
    )
    private static UpdateException createSQLError(final SQLException e) {
        return EXCEPTION.create(1, e, e.getMessage());
    }

}
