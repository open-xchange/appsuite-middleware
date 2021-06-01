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

import static com.openexchange.groupware.update.UpdateConcurrency.BACKGROUND;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;

/**
 * {@link ContactAdjustLastModifiedForChangedSenderAddress}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class ContactAdjustLastModifiedForChangedSenderAddress extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link ContactAdjustLastModifiedForChangedSenderAddress}.
     */
    public ContactAdjustLastModifiedForChangedSenderAddress() {
        super();
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(BACKGROUND);
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            adjustLastModified(con);

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

    private void adjustLastModified(final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(
                "SELECT prg_contacts.intfield01,prg_contacts.cid FROM prg_contacts LEFT JOIN user_setting_mail " +
                "ON prg_contacts.cid = user_setting_mail.cid AND prg_contacts.userid = user_setting_mail.user " +
                "WHERE prg_contacts.userid > 0 AND prg_contacts.field65 <> user_setting_mail.send_addr;");
            rs = stmt.executeQuery();
            final TIntObjectMap<TIntList> map = new TIntObjectHashMap<TIntList>(1024);
            while (rs.next()) {
                final int cid = rs.getInt(2);
                TIntList ids = map.get(cid);
                if (null == ids) {
                    ids = new TIntLinkedList();
                    map.put(cid, ids);
                }
                ids.add(rs.getInt(1));
            }
            Databases.closeSQLStuff(rs, stmt);

            final AtomicReference<SQLException> exceptionReference = new AtomicReference<SQLException>();
            map.forEachEntry(new TIntObjectProcedure<TIntList>() {

                @Override
                public boolean execute(final int cid, final TIntList ids) {
                    final long now = System.currentTimeMillis();
                    PreparedStatement innerStmt = null;
                    try {
                        innerStmt = con.prepareStatement("UPDATE prg_contacts SET changing_date = ? WHERE cid = ? AND intfield01 = ?;");
                        final PreparedStatement pStmt = innerStmt;
                        ids.forEach(new TIntProcedure() {

                            @Override
                            public boolean execute(final int id) {
                                try {
                                    pStmt.setLong(1, now);
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
                        final SQLException sqlException = exceptionReference.get();
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
            final SQLException sqlException = exceptionReference.get();
            if (null != sqlException) {
                throw sqlException;
            }
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

}
