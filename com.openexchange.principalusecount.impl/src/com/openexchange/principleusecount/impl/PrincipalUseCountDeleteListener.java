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

package com.openexchange.principleusecount.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;


/**
 *
 * {@link PrincipalUseCountDeleteListener}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class PrincipalUseCountDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link PrincipalUseCountDeleteListener}.
     */
    public PrincipalUseCountDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        switch (event.getType()) {
            case DeleteEvent.TYPE_CONTEXT:
                handleContextDeletion(writeCon, event.getContext());
                break;
            case DeleteEvent.TYPE_GROUP:
            case DeleteEvent.TYPE_RESOURCE:
                handlePrincipalDeletion(writeCon, event.getContext(), event.getId());
                break;
            case DeleteEvent.TYPE_USER:
                handleUserDeletion(writeCon, event.getContext(), event.getId());
                break;
        }
    }

    private void handleContextDeletion(Connection writeCon, Context ctx) throws OXException {
        Statement stmt = null;
        try {
            stmt = writeCon.createStatement();
            stmt.executeUpdate("DELETE FROM principalUseCount WHERE cid=" + ctx.getContextId());
        } catch (SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private void handlePrincipalDeletion(Connection writeCon, Context ctx, int id) throws OXException {
        Statement stmt = null;
        try {
            stmt = writeCon.createStatement();
            stmt.executeUpdate("DELETE FROM principalUseCount WHERE cid=" + ctx.getContextId() + " AND principal=" + id);
        } catch (SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private void handleUserDeletion(Connection writeCon, Context ctx, int userId) throws OXException {
        Statement stmt = null;
        try {
            stmt = writeCon.createStatement();
            stmt.executeUpdate("DELETE FROM principalUseCount WHERE cid=" + ctx.getContextId() + " AND user=" + userId);
        } catch (SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }
}
