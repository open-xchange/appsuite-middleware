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
import static com.openexchange.tools.sql.DBUtils.rollback;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.database.DatabaseService;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link CalendarAddUIDValueTask} - Add UIDs to appointments if missing.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class CalendarAddUIDValueTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link CalendarAddUIDValueTask}.
     */
    public CalendarAddUIDValueTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        int cid = params.getContextId();
        DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        Connection con = dbService.getForUpdateTask(cid);
        try {
            con.setAutoCommit(false);
            addUidSingleAppoointments("prg_dates", con);
            addUidRecurringAppoointments("prg_dates", con);
            // not needed for del_dates
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            rollback(con);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.backNoTimeout(cid, true, con);
        }
    }

    private void addUidSingleAppoointments(final String tableName, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = con.prepareStatement("SELECT intfield01,cid FROM " + tableName + " WHERE uid IS NULL AND intfield02 IS NULL");
            resultSet = stmt.executeQuery();
            TIntObjectMap<TIntList> map = new TIntObjectHashMap<TIntList>(1024);
            while (resultSet.next()) {
                int cid = resultSet.getInt(2);
                TIntList ids = map.get(cid);
                if (null == ids) {
                    ids = new TIntLinkedList();
                    map.put(cid, ids);
                }
                ids.add(resultSet.getInt(1));
            }
            DBUtils.closeSQLStuff(resultSet, stmt);

            final AtomicReference<SQLException> exceptionReference = new AtomicReference<SQLException>();
            map.forEachEntry(new TIntObjectProcedure<TIntList>() {

                @Override
                public boolean execute(final int cid, TIntList ids) {
                    PreparedStatement innerStmt = null;
                    try {
                        innerStmt = con.prepareStatement("UPDATE " + tableName + " SET uid=? WHERE cid=? AND intfield01=?");
                        final PreparedStatement pStmt = innerStmt;
                        ids.forEach(new TIntProcedure() {

                            @Override
                            public boolean execute(final int id) {
                                try {
                                    pStmt.setString(1, UUID.randomUUID().toString());
                                    pStmt.setInt(2, cid);
                                    pStmt.setInt(3, id);
                                    pStmt.addBatch();
                                    return true;
                                } catch (SQLException e) {
                                    exceptionReference.set(e);
                                    return false;
                                }
                            }
                        });
                        SQLException sqlException = exceptionReference.get();
                        if (null != sqlException) {
                            throw sqlException;
                        }
                        innerStmt.executeBatch();
                        return true;
                    } catch (SQLException e) {
                        exceptionReference.set(e);
                        return false;
                    } finally {
                        DBUtils.closeSQLStuff(innerStmt);
                    }
                }
            }); // end of for-each procedure
            SQLException sqlException = exceptionReference.get();
            if (null != sqlException) {
                throw sqlException;
            }
        } finally {
            DBUtils.closeSQLStuff(resultSet, stmt);
        }
    }

    private void addUidRecurringAppoointments(final String tableName, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = con.prepareStatement("SELECT intfield01,cid FROM " + tableName + " WHERE uid IS NULL AND intfield01=intfield02");
            resultSet = stmt.executeQuery();
            TIntObjectMap<TIntList> map = new TIntObjectHashMap<TIntList>(1024);
            while (resultSet.next()) {
                int cid = resultSet.getInt(2);
                TIntList ids = map.get(cid);
                if (null == ids) {
                    ids = new TIntLinkedList();
                    map.put(cid, ids);
                }
                ids.add(resultSet.getInt(1));
            }
            DBUtils.closeSQLStuff(resultSet, stmt);

            final AtomicReference<SQLException> exceptionReference = new AtomicReference<SQLException>();
            map.forEachEntry(new TIntObjectProcedure<TIntList>() {

                @Override
                public boolean execute(final int cid, TIntList ids) {
                    PreparedStatement innerStmtMaster = null;
                    PreparedStatement innerStmtExceptions = null;
                    try {
                        innerStmtMaster = con.prepareStatement("UPDATE " + tableName + " SET uid=? WHERE cid=? AND intfield01=?");
                        innerStmtExceptions = con.prepareStatement("UPDATE " + tableName + " SET uid=? WHERE cid=? AND intfield02=?");
                        final PreparedStatement stmtMaster = innerStmtMaster;
                        final PreparedStatement stmtExceptions = innerStmtExceptions;
                        ids.forEach(new TIntProcedure() {

                            @Override
                            public boolean execute(final int id) {
                                String uuid = UUID.randomUUID().toString();
                                try {
                                    stmtMaster.setString(1, uuid);
                                    stmtMaster.setInt(2, cid);
                                    stmtMaster.setInt(3, id);
                                    stmtMaster.addBatch();
                                    stmtExceptions.setString(1, uuid);
                                    stmtExceptions.setInt(2, cid);
                                    stmtExceptions.setInt(3, id);
                                    stmtExceptions.addBatch();
                                    return true;
                                } catch (SQLException e) {
                                    exceptionReference.set(e);
                                    return false;
                                }
                            }
                        });
                        SQLException sqlException = exceptionReference.get();
                        if (null != sqlException) {
                            throw sqlException;
                        }
                        innerStmtMaster.executeBatch();
                        innerStmtExceptions.executeBatch();
                        return true;
                    } catch (SQLException e) {
                        exceptionReference.set(e);
                        return false;
                    } finally {
                        DBUtils.closeSQLStuff(innerStmtMaster);
                        DBUtils.closeSQLStuff(innerStmtExceptions);
                    }
                }
            }); // end of for-each procedure
            SQLException sqlException = exceptionReference.get();
            if (null != sqlException) {
                throw sqlException;
            }
        } finally {
            DBUtils.closeSQLStuff(resultSet, stmt);
        }
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BACKGROUND);
    }

}
