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

package com.openexchange.groupware.delete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;

/**
 * Removes a users POP3 synchronization information. Those table have a foreign key on mail account tables and this information must be
 * removed before removing mail account information.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
final class POP3DeleteListener implements DeleteListener {

    @Override
    public void deletePerformed(final DeleteEvent deleteEvent, final Connection readCon, final Connection writeCon) throws OXException {
        if (DeleteEvent.TYPE_USER == deleteEvent.getType()) {
            PreparedStatement stmt = null;
            try {
                final int contextId = deleteEvent.getContext().getContextId();
                final int user = deleteEvent.getId();

                stmt = writeCon.prepareStatement("DELETE FROM pop3_storage_deleted WHERE cid=? AND user=?");
                int pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, user);
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);

                stmt = writeCon.prepareStatement("DELETE FROM pop3_storage_ids WHERE cid=? AND user=?");
                pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, user);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(stmt);
            }
        }
    }
}
