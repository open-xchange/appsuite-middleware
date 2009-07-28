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

package com.openexchange.folderstorage.virtual;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.DBPoolingException;
import com.openexchange.databaseold.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.UpdateException;
import com.openexchange.groupware.update.exception.UpdateExceptionFactory;

/**
 * {@link VirtualTreeCreateTableTask} - Inserts necessary tables to support missing POP3 features.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@OXExceptionSource(classId = Classes.UPDATE_TASK, component = EnumComponent.UPDATE)
public class VirtualTreeCreateTableTask implements UpdateTask {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(VirtualTreeCreateTableTask.class);

    private static final UpdateExceptionFactory EXCEPTION = new UpdateExceptionFactory(VirtualTreeCreateTableTask.class);

    public int addedWithVersion() {
        return 72;
    }

    public int getPriority() {
        return UpdateTaskPriority.HIGHEST.priority;
    }

    private static final String getTable1() {
        return "CREATE TABLE virtualTree (" + 
        		"cid INT4 unsigned NOT NULL, " + 
        		"tree INT4 unsigned NOT NULL, " + 
        		"user INT4 unsigned NOT NULL, " + 
        		"folderId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL, " + 
        		"parentId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL, " + 
        		"name VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL, " + 
        		"PRIMARY KEY (cid, tree, user, folderId), " + 
        		"INDEX (cid, tree, user, parentId), " + 
        		"FOREIGN KEY (cid, user) REFERENCES user (cid, id) " + 
        		") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    }

    private static final String getTable2() {
        return "CREATE TABLE virtualPermission (" + 
        		"cid INT4 unsigned NOT NULL, " + 
        		"tree INT4 unsigned NOT NULL, " + 
        		"user INT4 unsigned NOT NULL, " + 
        		"folderId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL, " + 
        		"entity INT4 unsigned NOT NULL, " + 
        		"fp tinyint(3) unsigned NOT NULL, " + 
        		"orp tinyint(3) unsigned NOT NULL, " + 
        		"owp tinyint(3) unsigned NOT NULL, " + 
        		"odp tinyint(3) unsigned NOT NULL, " + 
        		"adminFlag tinyint(3) unsigned NOT NULL, " + 
        		"groupFlag tinyint(3) unsigned NOT NULL, " + 
        		"system tinyint(3) unsigned NOT NULL default '0', " + 
        		"PRIMARY KEY (cid, tree, user, folderId, entity), " + 
        		"FOREIGN KEY (cid, tree, user, folderId) REFERENCES virtualTree (cid, tree, user, folderId) " + 
        		") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    }

    private static final String getTable3() {
        return "CREATE TABLE virtualSubscription (" + 
        		"cid INT4 unsigned NOT NULL, " + 
        		"tree INT4 unsigned NOT NULL, " + 
        		"user INT4 unsigned NOT NULL, " + 
        		"folderId VARCHAR(192) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL, " + 
        		"subscribed tinyint(3) unsigned NOT NULL, " + 
        		"PRIMARY KEY (cid, tree, user, folderId), " + 
        		"FOREIGN KEY (cid, tree, user, folderId) REFERENCES virtualTree (cid, tree, user, folderId) " + 
        		") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    }

    public void perform(final Schema schema, final int contextId) throws AbstractOXException {
        createTable("virtualTree", getTable1(), contextId);
        createTable("virtualPermission", getTable2(), contextId);
        createTable("virtualSubscription", getTable3(), contextId);
        if (LOG.isInfoEnabled()) {
            LOG.info("UpdateTask 'VirtualTreeCreateTableTask' successfully performed!");
        }
    }

    private void createTable(final String tablename, final String sqlCreate, final int contextId) throws UpdateException {
        final Connection writeCon;
        try {
            writeCon = Database.get(contextId, true);
        } catch (final DBPoolingException e) {
            throw new UpdateException(e);
        }
        PreparedStatement stmt = null;
        try {
            try {
                if (tableExists(tablename, writeCon.getMetaData())) {
                    return;
                }
                stmt = writeCon.prepareStatement(sqlCreate);
                stmt.executeUpdate();
            } catch (final SQLException e) {
                throw createSQLError(e);
            }
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(contextId, true, writeCon);
        }
    }

    /**
     * The object type "TABLE"
     */
    private static final String[] types = { "TABLE" };

    /**
     * Check a table's existence
     * 
     * @param tableName The table name to check
     * @param dbmd The database's meta data
     * @return <code>true</code> if table exists; otherwise <code>false</code>
     * @throws SQLException If a SQL error occurs
     */
    private static boolean tableExists(final String tableName, final DatabaseMetaData dbmd) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = dbmd.getTables(null, null, tableName, types);
            return resultSet.next();
        } finally {
            closeSQLStuff(resultSet, null);
        }
    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR }, desc = { "" }, exceptionId = { 1 }, msg = { "A SQL error occurred while performing task VirtualTreeCreateTableTask: %1$s." })
    private static UpdateException createSQLError(final SQLException e) {
        return EXCEPTION.create(1, e, e.getMessage());
    }
}
