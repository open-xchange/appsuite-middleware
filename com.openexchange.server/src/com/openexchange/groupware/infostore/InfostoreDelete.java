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

package com.openexchange.groupware.infostore;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.tools.session.ServerSessionAdapter;

public class InfostoreDelete implements DeleteListener {

    public InfostoreDelete() {
        super();
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        if (event.getType() == DeleteEvent.TYPE_USER) {
            InfostoreFacade database = new InfostoreFacadeImpl(new SimpleDBProvider(readCon, writeCon));
            database.setTransactional(true);
            database.setCommitsTransaction(false);
            Integer destUser = event.getDestinationUserID();
            database.removeUser(event.getId(), event.getContext(), destUser, ServerSessionAdapter.valueOf(event.getSession(), event.getContext()));
        } else if (event.getType() == DeleteEvent.TYPE_CONTEXT) {
            deleteContext(event, writeCon);
        }
    }

    /**
     * Delete a context from tasks module.
     */
    private void deleteContext(DeleteEvent event, Connection writeCon) throws OXException {
        Context ctx = event.getContext();
        Statement stmt = null;
        try {
            stmt = writeCon.createStatement();

            stmt.addBatch("DELETE FROM del_infostore_document WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM del_infostore WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM lock_null_lock WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM lock_null WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM infostore_lock WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM infostore_property WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM infostore_document WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM infostore WHERE cid=" + ctx.getContextId());

            stmt.executeBatch();
        } catch (SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
