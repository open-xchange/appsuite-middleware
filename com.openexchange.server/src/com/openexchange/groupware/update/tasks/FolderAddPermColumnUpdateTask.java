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

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link FolderAddPermColumnUpdateTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FolderAddPermColumnUpdateTask implements UpdateTask {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(FolderAddPermColumnUpdateTask.class));

    @Override
    public int addedWithVersion() {
        return 28;
    }

    @Override
    public int getPriority() {
        /*
         * Modification on database: highest priority.
         */
        return UpdateTask.UpdateTaskPriority.HIGHEST.priority;
    }

    private static final String SQL_MODIFY1 = "ALTER TABLE oxfolder_permissions ADD COLUMN `system` TINYINT unsigned default '0'";

    private static final String SQL_MODIFY2 = "ALTER TABLE del_oxfolder_permissions ADD COLUMN `system` TINYINT unsigned default '0'";

    private static final String SQL_MODIFY3 = "ALTER TABLE oxfolder_permissions DROP PRIMARY KEY, ADD PRIMARY KEY(cid, fuid, permission_id, system)";

    private static final String SQL_MODIFY4 = "ALTER TABLE del_oxfolder_permissions DROP PRIMARY KEY, ADD PRIMARY KEY(cid, fuid, permission_id, system)";

    @Override
    public void perform(final Schema schema, final int contextId) throws OXException {
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
                writeCon = Database.getNoTimeout(contextId, true);
                try {
                    stmt = writeCon.prepareStatement(SQL_MODIFY1);
                    stmt.executeUpdate();
                    stmt.close();
                    stmt = writeCon.prepareStatement(SQL_MODIFY2);
                    stmt.executeUpdate();
                } catch (final SQLException e) {
                    throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
                }
            } finally {
                closeSQLStuff(null, stmt);
                if (writeCon != null) {
                    Database.backNoTimeout(contextId, true, writeCon);
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
                writeCon = Database.getNoTimeout(contextId, true);
                try {
                    stmt = writeCon.prepareStatement(SQL_MODIFY3);
                    stmt.executeUpdate();
                    stmt.close();
                    stmt = writeCon.prepareStatement(SQL_MODIFY4);
                    stmt.executeUpdate();
                } catch (final SQLException e) {
                    throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
                }
            } finally {
                closeSQLStuff(null, stmt);
                if (writeCon != null) {
                    Database.backNoTimeout(contextId, true, writeCon);
                }
            }
        }
    }

    private static final String TABLE = "oxfolder_permissions";

    private static final String COLUMN = "system";

    /**
     * Determines if column named 'system' already exists in table
     * 'oxfolder_permissions'
     *
     * @param contextId - the context ID
     * @return <code>true</code> if column named 'system' was found; otherwise
     *         <code>false</code>
     * @throws OXException
     */
    private static final boolean checkColumn(final int contextId) throws OXException {
        Connection readCon = null;
        ResultSet rs = null;
        try {
            readCon = Database.getNoTimeout(contextId, false);
            try {
                final DatabaseMetaData databaseMetaData = readCon.getMetaData();
                rs = databaseMetaData.getColumns(null, null, TABLE, COLUMN);
                /*-
                 * Each column description has the following columns:
                 * 1. TABLE_CAT String => table catalog (may be null)
                 * 2. TABLE_SCHEM String => table schema (may be null)
                 * 3. TABLE_NAME String => table name
                 * 4. COLUMN_NAME String => column name
                 * 5. DATA_TYPE int => SQL type from java.sql.Types
                 * 6. TYPE_NAME String => Data source dependent type name, for a UDT the type name is fully qualified
                 * 7. COLUMN_SIZE int => column size. For char or date types this is the maximum number of characters,
                 *                       for numeric or decimal types this is precision.
                 * 8. BUFFER_LENGTH is not used.
                 * 9. DECIMAL_DIGITS int => the number of fractional digits
                 * 10. NUM_PREC_RADIX int => Radix (typically either 10 or 2)
                 * 11. NULLABLE int => is NULL allowed.
                 *                     - columnNoNulls - might not allow NULL values
                 *                     - columnNullable - definitely allows NULL values
                 *                     - columnNullableUnknown - nullability unknown
                 * 12. REMARKS String => comment describing column (may be null)
                 * 13. COLUMN_DEF String => default value (may be null)14. SQL_DATA_TYPE int => unused
                 * 15. SQL_DATETIME_SUB int => unused
                 * 16. CHAR_OCTET_LENGTH int => for char types the maximum number of bytes in the column
                 * 17. ORDINAL_POSITION int => index of column in table (starting at 1)
                 * 18. IS_NULLABLE String => "NO" means column definitely does not allow NULL values; "YES" means the
                 *                           column might allow NULL values. An empty string means nobody knows.
                 * 19. SCOPE_CATLOG String => catalog of table that is the scope of a reference attribute
                 *                            (null if DATA_TYPE isn't REF)
                 * 20. SCOPE_SCHEMA String => schema of table that is the scope of a reference attribute
                 *                            (null if the DATA_TYPE isn't REF)
                 * 21. SCOPE_TABLE String => table name that this the scope of a reference attribute
                 *                           (null if the DATA_TYPE isn't REF)
                 * 22. SOURCE_DATA_TYPE short => source type of a distinct type or user-generated Ref type, SQL type
                 *                               from java.sql.Types (null if DATA_TYPE isn't DISTINCT or user-generated
                 *                               REF)
                 */
                boolean found = false;
                while (rs.next() && !found) {
                    final String colName = rs.getString(4);
                    if (COLUMN.equals(colName)) {
                        found = true;
                    }
                }
                return found;
            } catch (final SQLException e) {
                throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
            }
        } finally {
            DBUtils.closeSQLStuff(rs, null);
            if (readCon != null) {
                Database.backNoTimeout(contextId, false, readCon);
            }
        }
    }

    private static final String[] EXPECTED_COLS = { "cid", "fuid", "permission_id", "system" };

    /**
     * Determines if table 'oxfolder_permissions' already has its primary key
     * set to <code>[cid,&nbsp;fuid,&nbsp;permission_id,&nbsp;system]</code>
     *
     * @param contextId - the context ID
     * @return <code>true</code> if table 'oxfolder_permissions' already has its
     *         primary key properly set; otherwise <code>false</code>
     * @throws OXException
     */
    private static final boolean checkPrimaryKey(final int contextId) throws OXException {
        Connection readCon = null;
        ResultSet rs = null;
        try {
            readCon = Database.get(contextId, false);
            try {
                final DatabaseMetaData databaseMetaData = readCon.getMetaData();
                rs = databaseMetaData.getPrimaryKeys(null, null, TABLE);
                /*-
                 * Each primary key column description has the following columns:
                 * 1. TABLE_CAT String => table catalog (may be null)
                 * 2. TABLE_SCHEM String => table schema (may be null)
                 * 3. TABLE_NAME String => table name
                 * 4. COLUMN_NAME String => column name
                 * 5. KEY_SEQ short => sequence number within primary key
                 * 6. PK_NAME String => primary key name (may be null)
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
                throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
            }
        } finally {
            closeSQLStuff(rs, null);
            if (readCon != null) {
                Database.back(contextId, false, readCon);
            }
        }
    }

}
