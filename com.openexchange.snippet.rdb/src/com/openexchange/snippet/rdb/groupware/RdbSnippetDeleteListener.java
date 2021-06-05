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

package com.openexchange.snippet.rdb.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.snippet.ReferenceType;
import com.openexchange.snippet.rdb.RdbSnippetManagement;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;

/**
 * {@link RdbSnippetDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RdbSnippetDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link RdbSnippetDeleteListener}.
     */
    public RdbSnippetDeleteListener() {
        super();
    }

    private TIntList getIds(int userId, int contextId, Connection writeCon) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (userId > 0) {
                stmt = writeCon.prepareStatement("SELECT id FROM snippet WHERE cid = ? AND user = ? AND refType=" + ReferenceType.GENCONF.getType());
                stmt.setInt(2, userId);
            } else {
                stmt = writeCon.prepareStatement("SELECT id FROM snippet WHERE cid = ? AND refType=" + ReferenceType.GENCONF.getType());
            }
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();

            TIntList ids = new TIntArrayList(4);
            while (rs.next()) {
                ids.add(Integer.parseInt(rs.getString(1)));
            }
            return ids;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public void deletePerformed(final DeleteEvent event, final Connection readCon, final Connection writeCon) throws com.openexchange.exception.OXException {
        if (DeleteEvent.TYPE_CONTEXT == event.getType()) {
            try {
                int contextId = event.getContext().getContextId();
                RdbSnippetManagement.deleteForContext(contextId, writeCon);
            } catch (RuntimeException e) {
                throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
            }
            return;
        }

        if (DeleteEvent.TYPE_USER == event.getType()) {
            /*
             * Writable connection
             */
            final int contextId = event.getContext().getContextId();
            PreparedStatement stmt = null;
            try {
                final int userId = event.getId();
                TIntList ids = getIds(userId, contextId, writeCon);
                if (ids.isEmpty()) {
                    return;
                }
                /*
                 * Delete them
                 */
                final AtomicReference<OXException> error = new AtomicReference<OXException>();
                ids.forEach(new TIntProcedure() {

                    @Override
                    public boolean execute(final int id) {
                        try {
                            RdbSnippetManagement.deleteSnippet(id, userId, contextId, writeCon);
                            return true;
                        } catch (OXException e) {
                            error.set(e);
                            return false;
                        }
                    }
                });
                final OXException e = error.get();
                if (null != e) {
                    throw e;
                }
            } catch (SQLException e) {
                throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } catch (RuntimeException e) {
                throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(stmt);
            }
        }
    }

}
