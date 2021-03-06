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

package com.openexchange.oauth.impl.internal.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;

/**
 * {@link OAuthDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OAuthDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link OAuthDeleteListener}.
     */
    public OAuthDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        if (DeleteEvent.TYPE_USER == event.getType()) {
            /*
             * Writable connection
             */
            final int contextId = event.getContext().getContextId();
            PreparedStatement stmt = null;
            try {
                final int userId = event.getId();
                /*
                 * Delete account data
                 */
                stmt = writeCon.prepareStatement("DELETE FROM oauthAccounts WHERE cid = ? AND user = ?");
                int pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, userId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } catch (Exception e) {
                throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(stmt);
            }
        } else if (DeleteEvent.TYPE_CONTEXT == event.getType()) {
            /*
             * Writable connection
             */
            final int contextId = event.getContext().getContextId();
            PreparedStatement stmt = null;
            try {
                /*
                 * Delete account data
                 */
                stmt = writeCon.prepareStatement("DELETE FROM oauthAccounts WHERE cid = ?");
                int pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } catch (Exception e) {
                throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(stmt);
            }
        }
    }

}
