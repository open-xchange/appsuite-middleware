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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import com.openexchange.database.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.UpdateExceptionFactory;

/**
 * {@link FolderAddPermColumnUpdateTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
@OXExceptionSource(classId = Classes.UPDATE_TASK, component = EnumComponent.UPDATE)
public class FolderAddPermColumnUpdateTask implements UpdateTask {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
            .getLog(FolderAddPermColumnUpdateTask.class);

    private static final UpdateExceptionFactory EXCEPTION = new UpdateExceptionFactory(
            FolderAddPermColumnUpdateTask.class);

    public int addedWithVersion() {
        return 28;
    }

    public int getPriority() {
        /*
         * Modification on database: highest priority.
         */
        return UpdateTask.UpdateTaskPriority.HIGHEST.priority;
    }

    private static final String SQL_MODIFY1 = "ALTER TABLE oxfolder_permissions ADD COLUMN `system` TINYINT unsigned default '0'";

    private static final String SQL_MODIFY2 = "ALTER TABLE del_oxfolder_permissions ADD COLUMN `system` TINYINT unsigned default '0'";

    private static final String SQL_MODIFY3 = "ALTER TABLE oxfolder_permissions DROP PRIMARY KEY, ADD PRIMARY KEY(cid, fuid, permission_id, system)";

    private static final String SQL_MODIFY4 = "ALTER TABLE del_oxfolder_permissions DROP PRIMARY KEY, ADD PRIMARY KEY(cid, permission_id, fuid, system)";

    @OXThrowsMultiple(category = { Category.CODE_ERROR }, desc = { "" }, exceptionId = { 1 }, msg = { "SQL error occurred while performing task FolderAddPermColumnUpdateTask: %1$s." })
    public void perform(final Schema schema, final int contextId) throws AbstractOXException {
        if (checkColumn(contextId)) {
            if (LOG.isInfoEnabled()) {
                LOG
                        .info("FolderAddPermColumnUpdateTask: Column `system` already present in table oxfolder_permissions.");
            }
        } else {
            /*
             * Column does not exist yet
             */
            Connection writeCon = null;
            PreparedStatement stmt = null;
            try {
                writeCon = Database.get(contextId, true);
                try {
                    stmt = writeCon.prepareStatement(SQL_MODIFY1);
                    stmt.executeUpdate();
                    stmt.close();
                    stmt = writeCon.prepareStatement(SQL_MODIFY2);
                    stmt.executeUpdate();
                } catch (final SQLException e) {
                    throw EXCEPTION.create(1, e, e.getMessage());
                }
            } finally {
                closeSQLStuff(null, stmt);
                if (writeCon != null) {
                    Database.back(contextId, true, writeCon);
                }
            }
        }
        if (checkPrimaryKey(contextId)) {
            if (LOG.isInfoEnabled()) {
                LOG.info("FolderAddPermColumnUpdateTask: Primary already set to " + Arrays.toString(EXPECTED_COLS));
            }
        } else {

            /*
             * Update primary keys
             */
            Connection writeCon = null;
            PreparedStatement stmt = null;
            try {
                writeCon = Database.get(contextId, true);
                try {
                    stmt = writeCon.prepareStatement(SQL_MODIFY3);
                    stmt.executeUpdate();
                    stmt.close();
                    stmt = writeCon.prepareStatement(SQL_MODIFY4);
                    stmt.executeUpdate();
                } catch (final SQLException e) {
                    throw EXCEPTION.create(1, e, e.getMessage());
                }
            } finally {
                closeSQLStuff(null, stmt);
                if (writeCon != null) {
                    Database.back(contextId, true, writeCon);
                }
            }
        }
    }

    private static final String SQL_SELECT_ALL = "SELECT * FROM oxfolder_permissions";

    private static final String COLUMN = "system";

    /**
     * Determines if column named 'system' already exists in table
     * 'oxfolder_permissions'
     *
     * @param contextId
     *            - the context ID
     * @return <code>true</code> if column named 'system' was found; otherwise
     *         <code>false</code>
     * @throws AbstractOXException
     */
    @OXThrowsMultiple(category = { Category.CODE_ERROR }, desc = { "" }, exceptionId = { 2 }, msg = { "SQL error occurred while performing task FolderAddPermColumnUpdateTask: %1$s." })
    private static final boolean checkColumn(final int contextId) throws AbstractOXException {
        Connection readCon = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            readCon = Database.get(contextId, false);
            try {
                stmt = readCon.createStatement();
                rs = stmt.executeQuery(SQL_SELECT_ALL);
                final ResultSetMetaData meta = rs.getMetaData();
                final int length = meta.getColumnCount();
                boolean found = false;
                for (int i = 1; i <= length && !found; i++) {
                    found = COLUMN.equals(meta.getColumnName(i));
                }
                return found;
            } catch (final SQLException e) {
                throw EXCEPTION.create(2, e, e.getMessage());
            }
        } finally {
            closeSQLStuff(rs, stmt);
            if (readCon != null) {
                Database.back(contextId, false, readCon);
            }
        }
    }

    private static final String[] EXPECTED_COLS = { "cid", "permission_id", "fuid", "system" };

    /**
     * Determines if table 'oxfolder_permissions' already has its primary key
     * set to <code>[cid,&nbsp;permission_id,&nbsp;fuid,&nbsp;system]</code>
     *
     * @param contextId
     *            - the context ID
     * @return <code>true</code> if table 'oxfolder_permissions' already has its
     *         primary key properly set; otherwise <code>false</code>
     * @throws AbstractOXException
     */
    @OXThrowsMultiple(category = { Category.CODE_ERROR }, desc = { "" }, exceptionId = { 3 }, msg = { "SQL error occurred while performing task FolderAddPermColumnUpdateTask: %1$s." })
    private static final boolean checkPrimaryKey(final int contextId) throws AbstractOXException {
        Connection readCon = null;
        ResultSet rs = null;
        try {
            readCon = Database.get(contextId, false);
            try {
                final DatabaseMetaData databaseMetaData = readCon.getMetaData();
                rs = databaseMetaData.getPrimaryKeys(null, null, "oxfolder_permissions");
                /**
                 * <pre>
                 * Each primary key column description has the following columns:
                 * 1. TABLE_CAT String =&gt; table catalog (may be null)
                 * 2. TABLE_SCHEM String =&gt; table schema (may be null)
                 * 3. TABLE_NAME String =&gt; table name
                 * 4. COLUMN_NAME String =&gt; column name
                 * 5. KEY_SEQ short =&gt; sequence number within primary key
                 * 6. PK_NAME String =&gt; primary key name (may be null)
                 * </pre>
                 */
                final String[] keyCols = new String[20];
                int keyCount = 0;
                while (rs.next()) {
                    final String colName = rs.getString(4);
                    final int seq_no = rs.getInt(5);
                    keyCols[seq_no - 1] = colName;
                    keyCount = Math.max(keyCount, seq_no);
                }
                rs.close();
                if (keyCount != EXPECTED_COLS.length) {
                    return false;
                }
                for (int i = 0; i < EXPECTED_COLS.length; i++) {
                    if (!EXPECTED_COLS[i].equals(keyCols[i])) {
                        return false;
                    }
                }
                return true;
            } catch (final SQLException e) {
                throw EXCEPTION.create(3, e, e.getMessage());
            }
        } finally {
            closeSQLStuff(rs, null);
            if (readCon != null) {
                Database.back(contextId, false, readCon);
            }
        }
    }

}
