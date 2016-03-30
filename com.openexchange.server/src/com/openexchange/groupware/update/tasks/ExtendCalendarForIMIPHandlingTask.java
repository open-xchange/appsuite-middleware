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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import static com.openexchange.tools.update.Tools.tableExists;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.openexchange.databaseold.Database;
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
        return new String[] { "com.openexchange.groupware.update.tasks.AddAppointmentParticipantsIndexTask" };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = Database.getNoTimeout(params.getContextId(), true);
        try {
            con.setAutoCommit(false);
            innerPerform(con);
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.backNoTimeout(params.getContextId(), true, con);
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
