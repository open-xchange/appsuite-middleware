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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;

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
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            if (false == Databases.tableExists(con, "prg_dates")) {
                return;
            }
            con.setAutoCommit(false);
            rollback = 1;

            addUidSingleAppoointments("prg_dates", con);
            addUidRecurringAppoointments("prg_dates", con);
            // not needed for del_dates

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    Databases.rollback(con);
                }
                autocommit(con);
            }
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
            Databases.closeSQLStuff(resultSet, stmt);

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
                        Databases.closeSQLStuff(innerStmt);
                    }
                }
            }); // end of for-each procedure
            SQLException sqlException = exceptionReference.get();
            if (null != sqlException) {
                throw sqlException;
            }
        } finally {
            Databases.closeSQLStuff(resultSet, stmt);
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
            Databases.closeSQLStuff(resultSet, stmt);

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
                        Databases.closeSQLStuff(innerStmtMaster);
                        Databases.closeSQLStuff(innerStmtExceptions);
                    }
                }
            }); // end of for-each procedure
            SQLException sqlException = exceptionReference.get();
            if (null != sqlException) {
                throw sqlException;
            }
        } finally {
            Databases.closeSQLStuff(resultSet, stmt);
        }
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BACKGROUND);
    }

}
