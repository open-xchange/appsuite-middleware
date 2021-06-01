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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.tools.update.Tools.tableExists;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * Alters calendar tables and add tables for external participants to support iCal handling with external participants: iMIP.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ExtendCalendarForIMIPHandlingTask extends UpdateTaskAdapter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ExtendCalendarForIMIPHandlingTask.class);

    private final String[] TABLES = { "prg_dates", "del_dates" };
    private final Column[] COLUMNS = { new Column("uid", "VARCHAR(1024)"), new Column("organizer", "VARCHAR(255)"), new Column("sequence", "INT4 UNSIGNED") };

    private static final String DATES_EXTERNAL_CREATE =
        "CREATE TABLE dateExternal (" +
        "cid INT4 UNSIGNED NOT NULL," +
        "objectId INT4 UNSIGNED NOT NULL," +
        "mailAddress VARCHAR(255) NOT NULL," +
        "displayName VARCHAR(255)," +
        "confirm INT4 UNSIGNED NOT NULL," +
        "reason VARCHAR(255)," +
        "PRIMARY KEY (cid,objectId,mailAddress)," +
        "FOREIGN KEY (cid,objectId) REFERENCES prg_dates(cid,intfield01)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

    private static final String DELDATES_EXTERNAL_CREATE =
        "CREATE TABLE delDateExternal (" +
        "cid INT4 UNSIGNED NOT NULL," +
        "objectId INT4 UNSIGNED NOT NULL," +
        "mailAddress VARCHAR(255) NOT NULL," +
        "displayName VARCHAR(255)," +
        "confirm INT4 UNSIGNED NOT NULL," +
        "reason VARCHAR(255)," +
        "PRIMARY KEY (cid,objectId, mailAddress)," +
        "FOREIGN KEY (cid,objectId) REFERENCES del_dates(cid,intfield01)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            if (false == Databases.tablesExist(con, "prg_dates", "del_dates")) {
                return;
            }
            con.setAutoCommit(false);
            rollback = 1;
            innerPerform(con);
            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    Databases.rollback(con);
                }
                autocommit(con);
            }
        }
    }

    private void innerPerform(Connection con) throws SQLException {
        SQLException toThrow = null;
        for (String tableName : TABLES) {
            try {
                Tools.checkAndAddColumns(con, tableName, COLUMNS);
            } catch (SQLException e) {
                LOG.error("", e);
                if (null == toThrow) {
                    toThrow = e;
                }
            }
        }
        try {
            if (!tableExists(con, "dateExternal")) {
                Statement stmt = null;
                try {
                    stmt = con.createStatement();
                    stmt.execute(DATES_EXTERNAL_CREATE);
                } finally {
                    closeSQLStuff(stmt);
                }
            }
        } catch (SQLException e) {
            LOG.error("", e);
            if (null == toThrow) {
                toThrow = e;
            }
        }
        try {
            if (!tableExists(con, "delDateExternal")) {
                Statement stmt = null;
                try {
                    stmt = con.createStatement();
                    stmt.execute(DELDATES_EXTERNAL_CREATE);
                } finally {
                    closeSQLStuff(stmt);
                }
            }
        } catch (SQLException e) {
            LOG.error("", e);
            if (null == toThrow) {
                toThrow = e;
            }
        }
        if (null != toThrow) {
            throw toThrow;
        }
    }
}
