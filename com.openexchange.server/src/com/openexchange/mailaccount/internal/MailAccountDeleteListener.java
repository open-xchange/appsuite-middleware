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

package com.openexchange.mailaccount.internal;

import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCode;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.services.ServerServiceRegistry;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntErrorAwareAbstractProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * {@link MailAccountDeleteListener} - {@link DeleteListener} for mail account storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailAccountDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link MailAccountDeleteListener}.
     */
    public MailAccountDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(final DeleteEvent deleteEvent, final Connection readCon, final Connection writeCon) throws OXException {
        int type = deleteEvent.getType();
        if (type == DeleteEvent.TYPE_USER) {
            final int userId = deleteEvent.getId();
            final int contextId = deleteEvent.getContext().getContextId();

            TIntList ids = getUserMailAccountIDs(userId, contextId, writeCon);

            final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
            final Map<String, Object> emptyMap = Collections.<String, Object> emptyMap();
            TIntErrorAwareAbstractProcedure<OXException> procedure = new TIntErrorAwareAbstractProcedure<OXException>() {

                @Override
                protected boolean next(int accountId) throws OXException {
                    try {
                        storageService.deleteMailAccount(accountId, emptyMap, userId, contextId, true, writeCon);
                        return true;
                    } catch (RuntimeException e) {
                        throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                    }
                }
            };
            ids.forEach(procedure);

            OXException err = procedure.getException();
            if (null != err) {
                throw err;
            }
        } else if (type == DeleteEvent.TYPE_CONTEXT) {
            handleContextDeletion(writeCon, deleteEvent.getContext());
        }
    }

    private void handleContextDeletion(Connection writeCon, Context ctx) throws OXException {
        Statement stmt = null;
        try {
            stmt = writeCon.createStatement();

            stmt.addBatch("DELETE FROM user_transport_account_properties WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM user_transport_account WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM user_mail_account_properties WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM user_mail_account WHERE cid=" + ctx.getContextId());

            stmt.executeBatch();
        } catch (SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private TIntList getUserMailAccountIDs(final int user, final int cid, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT id FROM user_mail_account WHERE cid = ? AND user = ? ORDER BY id");
            stmt.setLong(1, cid);
            stmt.setLong(2, user);
            result = stmt.executeQuery();

            TIntList ids = new TIntArrayList(8);
            TIntSet set = new TIntHashSet(8);

            while (result.next()) {
                int id = result.getInt(1);
                if (set.add(id)) {
                    ids.add(id);
                }
            }

            closeSQLStuff(result, stmt);
            stmt = con.prepareStatement("SELECT id FROM user_transport_account WHERE cid = ? AND user = ? ORDER BY id");
            stmt.setLong(1, cid);
            stmt.setLong(2, user);
            result = stmt.executeQuery();

            while (result.next()) {
                int id = result.getInt(1);
                if (set.add(id)) {
                    ids.add(id);
                }
            }
            return ids;
        } catch (SQLException e) {
            throw DeleteFailedExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

}
