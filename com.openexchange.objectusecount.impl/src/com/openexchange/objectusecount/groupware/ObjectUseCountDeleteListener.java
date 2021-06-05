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

package com.openexchange.objectusecount.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;

/**
 * {@link ObjectUseCountDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ObjectUseCountDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link ObjectUseCountDeleteListener}.
     */
    public ObjectUseCountDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        int type = event.getType();
        if (type == DeleteEvent.TYPE_CONTEXT) {
            handleDeletion(writeCon, event.getContext(), 0);
        } else if (type == DeleteEvent.TYPE_USER) {
            handleDeletion(writeCon, event.getContext(), event.getId());
        }
    }

    private void handleDeletion(Connection writeCon, Context ctx, int optUserId) throws OXException {
        PreparedStatement stmt = null;
        try {
            if (optUserId > 0) {
                stmt = writeCon.prepareStatement("DELETE FROM object_use_count WHERE cid=? AND user=?");
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, optUserId);
            } else {
                stmt = writeCon.prepareStatement("DELETE FROM object_use_count WHERE cid=?");
                stmt.setInt(1, ctx.getContextId());
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
