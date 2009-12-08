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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.AbstractCreateTableImpl;
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
 * {@link MALPollCreateTableTask} - Inserts necessary tables to support MAL Poll bundle features.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@OXExceptionSource(classId = Classes.UPDATE_TASK, component = EnumComponent.UPDATE)
public final class MALPollCreateTableTask extends AbstractCreateTableImpl implements UpdateTask {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MALPollCreateTableTask.class);

    public int addedWithVersion() {
        return 102;
    }

    public int getPriority() {
        return UpdateTaskPriority.HIGHEST.priority;
    }

    private static String getCreateHashTable() {
        return "CREATE TABLE malPollHash (" + 
        " cid INT4 unsigned NOT NULL," + 
        " user INT4 unsigned NOT NULL," + 
        " id INT4 unsigned NOT NULL," + 
        " fullname VARCHAR(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," + 
        " hash VARCHAR(33) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," + 
        " PRIMARY KEY (cid, user, id, fullname)," + 
        " FOREIGN KEY (cid, user) REFERENCES user (cid, id)," + 
        " FOREIGN KEY (cid, user, id) REFERENCES user_mail_account (cid, user, id)" + 
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    }

    private static final String getCreateUIDsTable() {
        return "CREATE TABLE malPollUid (" + 
        " hash VARCHAR(33) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," + 
        " uid VARCHAR(70) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," + 
        " PRIMARY KEY (hash, uid(64))" + 
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    }

    @Override
    public String[] getCreateStatements() {
        return new String[] { getCreateHashTable(), getCreateUIDsTable() };
    }

    public String[] requiredTables() {
        return new String[] { "user", "user_mail_account" };
    }

    public String[] tablesToCreate() {
        return new String[] { "malPollHash", "malPollUid" };
    }

    public void perform(final Schema schema, final int contextId) throws AbstractOXException {
        createTable("malPollHash", getCreateHashTable(), contextId);
        createTable("malPollUid", getCreateUIDsTable(), contextId);
        if (LOG.isInfoEnabled()) {
            LOG.info("UpdateTask 'MALPollCreateTableTask' successfully performed!");
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
            resultSet = dbmd.getTables(null, null, tableName, new String[] { "TABLE" });
            return resultSet.next();
        } finally {
            closeSQLStuff(resultSet, null);
        }
    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR }, desc = { "" }, exceptionId = { 1 }, msg = { "A SQL error occurred while performing task %1$s: %2$s." })
    private static UpdateException createSQLError(final SQLException e) {
        return new UpdateExceptionFactory(MALPollCreateTableTask.class).create(
            1,
            e,
            MALPollCreateTableTask.class.getSimpleName(),
            e.getMessage());
    }

}
