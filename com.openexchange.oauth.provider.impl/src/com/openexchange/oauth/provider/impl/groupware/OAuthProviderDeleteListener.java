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

package com.openexchange.oauth.provider.impl.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.tools.update.Tools;

/**
 * {@link OAuthProviderDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OAuthProviderDeleteListener implements DeleteListener {

    @Override
    public void deletePerformed(final DeleteEvent event, final Connection readCon, final Connection writeCon) throws OXException {
        if (event.getType() == DeleteEvent.TYPE_USER) {
            deleteUserEntriesFromDB(event, writeCon);
        } else if (event.getType() == DeleteEvent.TYPE_CONTEXT) {
            deleteContextEntriesFromDB(event, writeCon);
        }
    }

    private void deleteContextEntriesFromDB(DeleteEvent event, Connection writeCon) throws OXException {
        final Context context = event.getContext();
        final int contextId = context.getContextId();
        PreparedStatement stmt = null;
        try {
            if (Tools.tableExists(writeCon, "authCode")) {
                stmt = writeCon.prepareStatement("DELETE FROM authCode WHERE cid=?");
                stmt.setInt(1, contextId);
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }
            if (Tools.tableExists(writeCon, "oauth_grant")) {
                stmt = writeCon.prepareStatement("DELETE FROM oauth_grant WHERE cid=?");
                stmt.setInt(1, contextId);
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }
        } catch (SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private void deleteUserEntriesFromDB(DeleteEvent event, Connection writeCon) throws OXException {
        final int contextId = event.getContext().getContextId();
        PreparedStatement stmt = null;
        try {
            int userId = event.getId();
            int admin = event.getContext().getMailadmin();
            /*
             * Delete in case of administrator
             */
            if (userId == admin) {
                deleteContextEntriesFromDB(event, writeCon);
                return;
            }
            /*
             * Delete
             */
            if (Tools.tableExists(writeCon, "authCode")) {
                stmt = writeCon.prepareStatement("DELETE FROM authCode WHERE cid=? AND user=?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }
            if (Tools.tableExists(writeCon, "oauth_grant")) {
                stmt = writeCon.prepareStatement("DELETE FROM oauth_grant WHERE cid=? AND user=?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
                stmt = null;
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
