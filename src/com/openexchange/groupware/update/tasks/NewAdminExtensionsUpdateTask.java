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
package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;

import com.openexchange.database.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.UpdateExceptionFactory;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.server.DBPoolingException;

/**
 * @author choeger
 * 
 */
@OXExceptionSource(
        classId = Classes.TASK_NEW_ADMIN_EXTENSIONS,
        component = Component.UPDATE)
public class NewAdminExtensionsUpdateTask implements UpdateTask {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(NewAdminExtensionsUpdateTask.class);

    private static final UpdateExceptionFactory EXCEPTION = new UpdateExceptionFactory(NewAdminExtensionsUpdateTask.class);

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTask#addedWithVersion()
     */
    public int addedWithVersion() {
        return 7;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTask#getPriority()
     */
    public int getPriority() {
        /*
         * Modification on database: highest priority.
         */
        return UpdateTask.UpdateTaskPriority.HIGHEST.priority;
    }

    private static final String STR_INFO = "Performing update task 'NewAdminExtensionsUpdateTask'";
    
    private static final String CREATE_SEQUENCE_UID  = "CREATE TABLE IF NOT EXISTS `sequence_uid_number` ( `cid` INT4 unsigned NOT NULL, `id` INT4 unsigned NOT NULL, PRIMARY KEY  (`cid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";
    private static final String CREATE_SEQUENCE_GID  = "CREATE TABLE IF NOT EXISTS `sequence_gid_number` ( `cid` INT4 unsigned NOT NULL, `id` INT4 unsigned NOT NULL, PRIMARY KEY  (`cid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";
    private static final String CREATE_SEQUENCE_MAIL = "CREATE TABLE IF NOT EXISTS `sequence_mail_service` ( `cid` INT4 unsigned NOT NULL, `id` INT4 unsigned NOT NULL, PRIMARY KEY  (`cid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

    private static final String TABLE_USER         = "user";
    private static final String TABLE_DEL_USER     = "del_user";
    private static final String TABLE_GROUPS       = "groups";
    private static final String TABLE_DEL_GROUPS   = "del_groups";

    private static final String COL_UID_NUMBER     = "uidNumber"; 
    private static final String COL_GID_NUMBER     = "gidNumber"; 
    private static final String COL_HOME_DIRECTORY = "homeDirectory"; 
    private static final String COL_LOGIN_SHELL    = "loginShell"; 
    private static final String COL_PASSWORD_MECH  = "passwordMech"; 

    private static final int NOGROUP    = 65534;
    private static final int NOBODY     = 65534;
    private static final String NOHOME  = "/dev/null";
    private static final String NOSHELL = "/bin/false";
    private static final String SHA     = "{SHA}";    
    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTask#perform(com.openexchange.groupware.update.Schema,
     *      int)
     */
    @OXThrowsMultiple(category = { Category.SUBSYSTEM_OR_SERVICE_DOWN, Category.CODE_ERROR },
            desc = { "", "" },
            exceptionId = { 1, 2 },
            msg = { "Cannot get database connection.", "SQL Problem: \"%s\"." })
    public void perform(Schema schema, int contextId) throws AbstractOXException {
        if (LOG.isInfoEnabled()) {
            LOG.info(STR_INFO);
        }

        Hashtable<String, ArrayList<String>> missingCols = missingColumns(contextId);
        boolean deleteLastmodified = tableContainsColumn(contextId, "del_user", "lastModified");

        Connection writeCon = null;
        try {
            writeCon = Database.get(contextId, true);
        } catch (DBPoolingException e) {
            throw EXCEPTION.create(1, e);
        }
        try {
            writeCon.setAutoCommit(false);
            createSequenceTables(writeCon, contextId);
            alterTables(writeCon, contextId, missingCols);
            updateTables(writeCon, contextId, missingCols);
            if( deleteLastmodified ) {
                removeColumnFromTable(writeCon, contextId, "lastModified", "del_user");
            }
            writeCon.commit();
        } catch (SQLException e) {
            throw EXCEPTION.create(2, e, e.getMessage());
        } finally {
            try {
                writeCon.setAutoCommit(true);
            } catch (SQLException e) {
                LOG.error("Problem setting autocommit to true.", e);
            }
            Database.back(contextId, true, writeCon);
        }

    }

    @OXThrowsMultiple(category = { Category.SUBSYSTEM_OR_SERVICE_DOWN, Category.CODE_ERROR },
            desc = { "", "" },
            exceptionId = { 3, 4 },
            msg = { "Cannot get database connection.", "SQL Problem: \"%s\"." })
    private final Hashtable<String, ArrayList<String>> missingColumns(final int contextId) throws AbstractOXException {
        Connection readCon = null;
        Statement stmt = null;
        ResultSet rs = null;

        Hashtable<String, ArrayList<String>> retTables = new Hashtable<String, ArrayList<String>>();
        for(final String table : new String[]{ TABLE_USER, TABLE_GROUPS, TABLE_DEL_USER, TABLE_DEL_GROUPS } ) {
            ArrayList<String> ret = new ArrayList<String>();
            ret.add(COL_GID_NUMBER);
            if( table.equals(TABLE_USER) || table.equals(TABLE_DEL_USER) ) {
                ret.add(COL_UID_NUMBER);
                ret.add(COL_HOME_DIRECTORY);
                ret.add(COL_LOGIN_SHELL);
            }
            if( table.equals(TABLE_DEL_USER) ) {
                ret.add(COL_PASSWORD_MECH);
            }
            retTables.put(table, ret);
        }

        try {
            readCon = Database.get(contextId, false);
        } catch (DBPoolingException e) {
            throw EXCEPTION.create(3, e);
        }
        try {
            stmt = readCon.createStatement();
            for(final String table : new String[]{ TABLE_USER, TABLE_GROUPS, TABLE_DEL_USER, TABLE_DEL_GROUPS } ) {
                rs = stmt.executeQuery("SELECT * FROM " + table);
                final ResultSetMetaData meta = rs.getMetaData();
                final int length = meta.getColumnCount();
                ArrayList<String> colList = retTables.get(table);
                for (int i = 1; i <= length && colList.size() > 0; i++) {
                    for(final String col : new String[] { COL_GID_NUMBER, COL_UID_NUMBER,
                            COL_HOME_DIRECTORY, COL_LOGIN_SHELL, COL_PASSWORD_MECH} ) {
                        if( col.equals(meta.getColumnName(i)) ) {
                            colList.remove(col);
                        }
                    }
                }

            }

            return retTables;
        } catch (SQLException e) {
            throw EXCEPTION.create(4, e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            if (readCon != null) {
                Database.back(contextId, false, readCon);
            }
        }
    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR },
            desc = { "" },
            exceptionId = { 5 },
            msg = { "SQL Problem: \"%s\"." })
    private void createSequenceTables(final Connection con, final int contextId) throws AbstractOXException {
        PreparedStatement stmt = null;
        try {
            // create missing sequence tables
            for(final String createStmt : new String[]{CREATE_SEQUENCE_UID, CREATE_SEQUENCE_GID,
                    CREATE_SEQUENCE_MAIL}) {
                stmt = con.prepareStatement(createStmt);
                stmt.executeUpdate();
                stmt.close();
            }
        } catch (SQLException e) {
            throw EXCEPTION.create(5, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR },
            desc = { "" },
            exceptionId = { 6 },
            msg = { "SQL Problem: \"%s\"." })
    private void alterTables(final Connection con, final int contextId, final Hashtable<String, ArrayList<String>> missingCols) throws AbstractOXException {
        PreparedStatement stmt = null;
        try {
            for(final String table : missingCols.keySet() ) {
                ArrayList<String> cols = missingCols.get(table);
                if( cols.size() > 0 && 
                        ( table.equals(TABLE_USER) || table.equals(TABLE_DEL_USER) ) ) {
                    for( final String col : cols ) {
                        if( col.equals(COL_GID_NUMBER) ) {
                            stmt = con.prepareStatement("ALTER TABLE "+table+" ADD COLUMN gidNumber INT4 UNSIGNED NOT NULL");
                            stmt.executeUpdate();
                            stmt.close();
                        } else if( col.equals(COL_UID_NUMBER) ) {
                            stmt = con.prepareStatement("ALTER TABLE "+table+" ADD COLUMN uidNumber INT4 UNSIGNED NOT NULL");
                            stmt.executeUpdate();
                            stmt.close();
                        } else if( col.equals(COL_HOME_DIRECTORY) ) {
                            stmt = con.prepareStatement("ALTER TABLE "+table+" ADD COLUMN homeDirectory VARCHAR(128) NOT NULL");
                            stmt.executeUpdate();
                            stmt.close();
                        } else if( col.equals(COL_LOGIN_SHELL) ) {
                            stmt = con.prepareStatement("ALTER TABLE "+table+" ADD COLUMN loginShell VARCHAR(128) NOT NULL");
                            stmt.executeUpdate();
                            stmt.close();
                        } else if( col.equals(COL_PASSWORD_MECH) && table.equals(TABLE_DEL_USER) ) {
                            stmt = con.prepareStatement("ALTER TABLE "+table+" ADD COLUMN passwordMech VARCHAR(128) NOT NULL");
                            stmt.executeUpdate();
                            stmt.close();
                        }
                    }                        
                } else if( cols.size() > 0 && 
                        ( table.equals(TABLE_GROUPS) || table.equals(TABLE_DEL_GROUPS) ) ) {
                    for( final String col : cols ) {
                        if( col.equals(COL_GID_NUMBER) ) {
                            stmt = con.prepareStatement("ALTER TABLE "+table+" ADD COLUMN gidNumber INT4 UNSIGNED NOT NULL");
                            stmt.executeUpdate();
                            stmt.close();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw EXCEPTION.create(6, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR },
            desc = { "" },
            exceptionId = { 7 },
            msg = { "SQL Problem: \"%s\"." })
    private void updateTables(final Connection con, final int contextId, final Hashtable<String, ArrayList<String>> missingCols) throws AbstractOXException {
        PreparedStatement stmt = null;
        try {
            for(final String table : missingCols.keySet() ) {
                ArrayList<String> cols = missingCols.get(table);
                if( cols.size() > 0 && 
                        ( table.equals(TABLE_USER) || table.equals(TABLE_DEL_USER) ) ) {
                    for( final String col : cols ) {
                        if( col.equals(COL_GID_NUMBER) ) {
                            stmt = con.prepareStatement("UPDATE "+table+" SET gidNumber=?");
                            stmt.setInt(1, NOGROUP);
                            stmt.executeUpdate();
                            stmt.close();
                        } else if( col.equals(COL_UID_NUMBER) ) {
                            stmt = con.prepareStatement("UPDATE "+table+" SET uidNumber=?");
                            stmt.setInt(1, NOBODY);
                            stmt.executeUpdate();
                            stmt.close();
                        } else if( col.equals(COL_HOME_DIRECTORY) ) {
                            stmt = con.prepareStatement("UPDATE "+table+" SET homeDirectory=?");
                            stmt.setString(1, NOHOME);
                            stmt.executeUpdate();
                            stmt.close();
                        } else if( col.equals(COL_LOGIN_SHELL) ) {
                            stmt = con.prepareStatement("UPDATE "+table+" SET loginShell=?");
                            stmt.setString(1, NOSHELL);
                            stmt.executeUpdate();
                            stmt.close();
                        } else if( col.equals(COL_PASSWORD_MECH) && table.equals(TABLE_DEL_USER) ) {
                            stmt = con.prepareStatement("UPDATE "+table+" SET passwordMech=?");
                            stmt.setString(1, SHA);
                            stmt.executeUpdate();
                            stmt.close();
                        }
                    }                        
                } else if( cols.size() > 0 && 
                        ( table.equals(TABLE_GROUPS) || table.equals(TABLE_DEL_GROUPS) ) ) {
                    for( final String col : cols ) {
                        if( col.equals(COL_GID_NUMBER) ) {
                            stmt = con.prepareStatement("UPDATE "+table+" SET gidNumber=?");
                            stmt.setInt(1, NOGROUP);
                            stmt.executeUpdate();
                            stmt.close();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw EXCEPTION.create(7, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    @OXThrowsMultiple(category = { Category.SUBSYSTEM_OR_SERVICE_DOWN, Category.CODE_ERROR },
            desc = { "", "" },
            exceptionId = { 8, 9 },
            msg = { "Cannot get database connection.", "SQL Problem: \"%s\"." })
    private final boolean tableContainsColumn(final int contextId, final String table, final String column) throws AbstractOXException {
        Connection readCon = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            readCon = Database.get(contextId, false);
        } catch (DBPoolingException e) {
            throw EXCEPTION.create(8, e);
        }
        try {
            try {
                stmt = readCon.createStatement();
                rs = stmt.executeQuery("SELECT * FROM " + table);
                final ResultSetMetaData meta = rs.getMetaData();
                final int length = meta.getColumnCount();
                boolean found = false;
                for (int i = 1; i <= length && !found; i++) {
                    if( column.equals(meta.getColumnName(i)) ) {
                        found = true;
                    }
                }
                return found;
            } catch (SQLException e) {
                throw EXCEPTION.create(9, e, e.getMessage());
            }
        } finally {
            closeSQLStuff(rs, stmt);
            if (readCon != null) {
                Database.back(contextId, false, readCon);
            }
        }
    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR },
            desc = { "" },
            exceptionId = { 10 },
            msg = { "SQL Problem: \"%s\"." })
    private void removeColumnFromTable(final Connection con, final int contextId, final String column, final String table) throws AbstractOXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("ALTER TABLE "+table+" DROP "+column);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            throw EXCEPTION.create(10, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

}
