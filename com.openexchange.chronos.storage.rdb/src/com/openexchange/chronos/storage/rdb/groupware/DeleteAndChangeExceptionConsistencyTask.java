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

package com.openexchange.chronos.storage.rdb.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Tools;

/**
 * {@link DeleteAndChangeExceptionConsistencyTask}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class DeleteAndChangeExceptionConsistencyTask extends UpdateTaskAdapter {

    private static final String CANDIDATES = "SELECT cid, intfield01, field07, field08 FROM prg_dates WHERE intfield01 = intfield02 AND field07 IS NOT NULL AND field07 != '' AND field08 IS NOT NULL AND field08 != '';";

    private static final String FIND_CHANGE_EXCEPTION = "SELECT intfield01 FROM prg_dates WHERE cid = ? AND intfield02 = ? AND field08 = ? AND intfield01 != intfield02;";

    private static final String REPAIR_PREFIX = "UPDATE prg_dates SET ";

    private static final String REPAIR_SUFFIX = " WHERE cid = ? AND intfield01 = ?;";

    @Override
    public String[] getDependencies() {
        return new String[] { "com.openexchange.groupware.update.tasks.CalendarAddIndex2DatesMembersV2" };
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

            innerPerform(con);

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

    private void innerPerform(Connection con) throws SQLException {
        PreparedStatement masterStmt = null;
        ResultSet masterRs = null;
        try {
            masterStmt = con.prepareStatement(CANDIDATES);
            masterRs = masterStmt.executeQuery();
            while (masterRs.next()) {
                int cid = masterRs.getInt("cid");
                int intfield01 = masterRs.getInt("intfield01");
                String field07 = masterRs.getString("field07");
                String field08 = masterRs.getString("field08");
                String[] deleteExceptions = field07.split(",");
                String[] changeExceptions = field08.split(",");
                List<String> duplicates = Arrays.stream(deleteExceptions).filter(deleteException -> Arrays.stream(changeExceptions).anyMatch(changeException -> changeException.equals(deleteException))).collect(Collectors.toList());
                if (duplicates.isEmpty()) {
                    continue;
                }

                List<String> deleteExceptionsToRemove = new ArrayList<>();
                List<String> changeExceptionsToRemove = new ArrayList<>();
                for (String duplicate : duplicates) {
                    if (changeExceptionExists(con, cid, intfield01, duplicate)) {
                        deleteExceptionsToRemove.add(duplicate);
                    } else {
                        changeExceptionsToRemove.add(duplicate);
                    }
                }

                String newField07 = newValue(deleteExceptions, deleteExceptionsToRemove);
                String newField08 = newValue(changeExceptions, changeExceptionsToRemove);

                repair(con, cid, intfield01, newField07, newField08);
            }
        } finally {
            Databases.closeSQLStuff(masterRs, masterStmt);
        }
    }

    private void repair(Connection con, int cid, int intfield01, String newField07, String newField08) throws SQLException {
        if (newField07 == null && newField08 == null) {
            return;
        }

        StringBuilder repair = new StringBuilder();
        repair.append(REPAIR_PREFIX);
        if (newField07 != null) {
            repair.append("field07 = ?");
            if (newField08 != null) {
                repair.append(", ");
            }
        }
        if (newField08 != null) {
            repair.append("field08 = ?");
        }
        repair.append(REPAIR_SUFFIX);

        PreparedStatement repairStmt = null;
        try {
            repairStmt = con.prepareStatement(repair.toString());
            int i = 0;
            if (newField07 != null) {
                repairStmt.setString(++i, newField07);
            }
            if (newField08 != null) {
                repairStmt.setString(++i, newField08);
            }
            repairStmt.setInt(++i, cid);
            repairStmt.setInt(++i, intfield01);
            repairStmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(repairStmt);
        }
    }

    private String newValue(String[] currentExceptions, List<String> exceptionsToRemove) {
        if (exceptionsToRemove.isEmpty()) {
            return null;
        }
        return Arrays.stream(currentExceptions).filter(exception -> exceptionsToRemove.stream().noneMatch(remove -> remove.equals(exception))).collect(Collectors.joining(","));
    }

    private boolean changeExceptionExists(Connection con, int cid, int masterId, String exception) throws SQLException {
        PreparedStatement exceptionStmt = null;
        ResultSet exceptionRs = null;
        try {
            exceptionStmt = con.prepareStatement(FIND_CHANGE_EXCEPTION);
            exceptionStmt.setInt(1, cid);
            exceptionStmt.setInt(2, masterId);
            exceptionStmt.setString(3, exception);
            exceptionRs = exceptionStmt.executeQuery();
            boolean exists = exceptionRs.next();
            return exists;
        } finally {
            Databases.closeSQLStuff(exceptionRs, exceptionStmt);
        }
    }

}
