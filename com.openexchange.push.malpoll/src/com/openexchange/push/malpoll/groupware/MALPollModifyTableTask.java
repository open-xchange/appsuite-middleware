/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.push.malpoll.groupware;

import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import com.openexchange.database.Databases;
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
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

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

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
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
