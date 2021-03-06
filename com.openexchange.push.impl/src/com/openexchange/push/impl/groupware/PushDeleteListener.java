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

package com.openexchange.push.impl.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.push.impl.PushManagerRegistry;

/**
 * {@link PushDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PushDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link PushDeleteListener}.
     */
    public PushDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        if (DeleteEvent.TYPE_USER == event.getType()) {
            int contextId = event.getContext().getContextId();
            int userId = event.getId();

            // Stop remaining listeners
            PushManagerRegistry.getInstance().unregisterAllPermanentListenersFor(userId, contextId);

            // Cleanse from database
            PreparedStatement stmt = null;
            try {
                // Delete account data
                stmt = writeCon.prepareStatement("DELETE FROM registeredPush WHERE cid = ? AND user = ?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } catch (Exception e) {
                throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(stmt);
            }
        } else if (DeleteEvent.TYPE_CONTEXT == event.getType()) {
            // Cleanse from database
            int contextId = event.getContext().getContextId();
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = writeCon.prepareStatement("SELECT DISTINCT user FROM registeredPush WHERE cid=?");
                stmt.setInt(1, contextId);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    List<Integer> userIds = new LinkedList<Integer>();
                    do {
                        userIds.add(Integer.valueOf(rs.getInt(1)));
                    } while (rs.next());
                    Databases.closeSQLStuff(rs, stmt);
                    rs = null;
                    stmt = null;

                    for (Integer userId : userIds) {
                        // Stop remaining listeners
                        PushManagerRegistry.getInstance().unregisterAllPermanentListenersFor(userId.intValue(), contextId);
                    }
                } else {
                    Databases.closeSQLStuff(rs, stmt);
                    rs = null;
                    stmt = null;
                }

                // Delete account data
                stmt = writeCon.prepareStatement("DELETE FROM registeredPush WHERE cid = ?");
                stmt.setInt(1, contextId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } catch (Exception e) {
                throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }
        }
    }

}
