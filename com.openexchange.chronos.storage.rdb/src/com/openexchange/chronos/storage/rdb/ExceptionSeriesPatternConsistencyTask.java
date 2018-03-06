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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.storage.rdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Tools;

/**
 * {@link ExceptionSeriesPatternConsistencyTask}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class ExceptionSeriesPatternConsistencyTask extends UpdateTaskAdapter {

    private static final String FIND_MISSING_PATTERN = "SELECT exception.cid, exception.intfield01, master.field06 FROM prg_dates exception JOIN prg_dates master ON exception.cid = master.cid AND exception.intfield02 = master.intfield01 AND (exception.field06 != master.field06 OR exception.field06 IS NULL)";

    private static final String REPAIR_MISSING_PATTERN = "UPDATE prg_dates SET field06 = ? WHERE cid = ? AND intfield01 = ?;";

    private static final String FIND_MISSING_RECURRENCE_CALCULATOR = "SELECT exception.cid, exception.intfield01, master.intfield04 FROM prg_dates exception JOIN prg_dates master ON exception.cid = master.cid AND exception.intfield02 = master.intfield01 AND exception.intfield04 != master.intfield04;";

    private static final String REPAIR_MISSING_RECURRENCE_CALCULATOR = "UPDATE prg_dates SET intfield04 = ? WHERE cid = ? AND intfield01 = ?";

    @Override
    public String[] getDependencies() {
        return new String[] { "com.openexchange.groupware.update.tasks.CalendarAddIndex2DatesMembersV2"
        };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        boolean rollback = false;
        try {
            if (!Tools.tableExists(con, "prg_dates")) {
                return;
            }
            con.setAutoCommit(false);
            rollback = true;

            repairPattern(con);
            repairRecurrenceCalculator(con);

            con.commit();
            rollback = false;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
        }
    }

    private void repairPattern(Connection con) throws SQLException {
        ResultSet rs = null;
        PreparedStatement findStmt = null;
        PreparedStatement repairStmt = null;
        try {
            findStmt = con.prepareStatement(FIND_MISSING_PATTERN);
            rs = findStmt.executeQuery();
            repairStmt = con.prepareStatement(REPAIR_MISSING_PATTERN);
            while (rs.next()) {
                int cid = rs.getInt("cid");
                int intfield01 = rs.getInt("intfield01");
                String correctField06 = rs.getString("field06");

                repairStmt.setString(1, correctField06);
                repairStmt.setInt(2, cid);
                repairStmt.setInt(3, intfield01);

                repairStmt.addBatch();
            }
            repairStmt.executeBatch();
        } finally {
            Databases.closeSQLStuff(findStmt);
            Databases.closeSQLStuff(rs, repairStmt);
        }
    }

    private void repairRecurrenceCalculator(Connection con) throws SQLException {
        ResultSet rs = null;
        PreparedStatement findStmt = null;
        PreparedStatement repairStmt = null;
        try {
            findStmt = con.prepareStatement(FIND_MISSING_RECURRENCE_CALCULATOR);
            rs = findStmt.executeQuery();
            repairStmt = con.prepareStatement(REPAIR_MISSING_RECURRENCE_CALCULATOR);
            while (rs.next()) {
                int cid = rs.getInt("cid");
                int intfield01 = rs.getInt("intfield01");
                int correctIntfield04 = rs.getInt("intfield04");

                repairStmt.setInt(1, correctIntfield04);
                repairStmt.setInt(2, cid);
                repairStmt.setInt(3, intfield01);

                repairStmt.addBatch();
            }
            repairStmt.executeBatch();
        } finally {
            Databases.closeSQLStuff(findStmt);
            Databases.closeSQLStuff(rs, repairStmt);
        }
    }

}
