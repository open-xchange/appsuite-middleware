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

package com.openexchange.push.malpoll;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Tools;

/**
 * {@link MALPollModifyTableTask} - Modifies MAL Poll tables to add "cid" column.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MALPollModifyTableTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link MALPollModifyTableTask}.
     */
    public MALPollModifyTableTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { MALPollCreateTableTask.class.getName() };
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        final int contextId = params.getContextId();
        final Connection con = Database.getNoTimeout(contextId, true);
        try {
            con.setAutoCommit(false); // BEGIN
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        }
        try {

            final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MALPollModifyTableTask.class);

            boolean contentDropped = false;

            final String table1 = "malPollHash";
            if (isVARCHAR(con, table1)) {
                log.info("Data type of column `hash` is VARCHAR, converting to BINARY...");
                dropAllContent(con, log);
                contentDropped = true;

                modifyHashColumn(con, table1, log);
            }

            final String table2 = "malPollUid";
            final String columnName = "cid";
            final boolean columnExists = Tools.columnExists(con, table2, columnName);
            final boolean isVarchar = isVARCHAR(con, table2);

            if (!columnExists || isVarchar) {
                dropPrimaryKey(con, table2, log);
                if (!contentDropped) {
                    dropAllContent(con, log);
                    contentDropped = true;
                }

                if (isVarchar) {
                    log.info("Data type of column `hash` is VARCHAR, converting to BINARY...");

                    modifyHashColumn(con, table2, log);
                }

                if (!columnExists) {
                    createColumn(con, table2, columnName, log);
                }

            }

            if (!Tools.existsPrimaryKey(con, table2, new String[] { "cid", "hash", "uid" })) {
                /*
                 * Has any primary key?
                 */
                if (Tools.hasPrimaryKey(con, table2)) {
                    dropPrimaryKey(con, table2, log);
                    if (!contentDropped) {
                        dropAllContent(con, log);
                        contentDropped = true;
                    }
                }
                addPrimaryKey(con, table2, log);
            }

            con.commit(); // COMMIT
            log.info("Update task {} successfully performed.", MALPollModifyTableTask.class.getName());
        } catch (final SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (final Exception e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.backNoTimeout(contextId, true, con);
        }
    }

    private boolean isVARCHAR(final Connection con, final String table) throws SQLException {
        return Tools.isVARCHAR(con, table, "hash");
    }

    private void dropAllContent(final Connection con, final org.slf4j.Logger log) throws SQLException {
        PreparedStatement stmt = null;
        try {
            log.info("Clearing table malPollUid.");
            stmt = con.prepareStatement("DELETE FROM malPollUid");
            stmt.executeUpdate();
            closeSQLStuff(stmt);

            log.info("Clearing table malPollHash.");
            stmt = con.prepareStatement("DELETE FROM malPollHash");
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void modifyHashColumn(final Connection con, final String table, final org.slf4j.Logger log) throws SQLException {
        PreparedStatement stmt = null;
        try {
            log.info("Modifying column hash from table {}.", table);
            stmt = con.prepareStatement("ALTER TABLE " + table + " MODIFY COLUMN hash BINARY(16) NOT NULL");
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void createColumn(final Connection con, final String tableName, final String columnName, final org.slf4j.Logger log) throws SQLException {
        log.info("Adding column {} to table {}.", columnName, tableName);
        final PreparedStatement stmt =
            con.prepareStatement("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " INT4 unsigned NOT NULL");
        try {
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void dropPrimaryKey(final Connection con, final String tableName, final org.slf4j.Logger log) throws SQLException {
        log.info("Removing old primary key from table {}.", tableName);
        Tools.dropPrimaryKey(con, tableName);
    }

    private void addPrimaryKey(final Connection con, final String tableName, final org.slf4j.Logger log) throws SQLException {
        final String[] columns = new String[] { "cid", "hash", "uid" };
        log.info("Creating new primary key {} on table {}.", Arrays.toString(columns), tableName);
        Tools.createPrimaryKey(con, tableName, columns, new int[] {-1, -1, 32});
    }

}
