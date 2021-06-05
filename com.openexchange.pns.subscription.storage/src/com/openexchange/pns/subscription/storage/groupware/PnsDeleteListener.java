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

package com.openexchange.pns.subscription.storage.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.subscription.storage.rdb.RdbPushSubscriptionRegistry;

/**
 * {@link PnsDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PnsDeleteListener implements DeleteListener {

    private final RdbPushSubscriptionRegistry registry;

    /**
     * Initializes a new {@link PnsDeleteListener}.
     */
    public PnsDeleteListener(RdbPushSubscriptionRegistry registry) {
        super();
        this.registry = registry;
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        if (DeleteEvent.TYPE_USER == event.getType()) {
            int contextId = event.getContext().getContextId();
            int userId = event.getId();
            List<byte[]> ids = getSubscriptionIds(userId, contextId, writeCon);
            if (!ids.isEmpty()) {
                for (byte[] id : ids) {
                    registry.deleteById(id, null, writeCon);
                }
            }
        } else if (DeleteEvent.TYPE_CONTEXT == event.getType()) {
            int contextId = event.getContext().getContextId();
            List<byte[]> ids = getSubscriptionIds(0, contextId, writeCon);
            if (!ids.isEmpty()) {
                for (byte[] id : ids) {
                    registry.deleteById(id, null, writeCon);
                }
            }
        }
    }

    private List<byte[]> getSubscriptionIds(int userId, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(userId > 0 ? "SELECT id FROM pns_subscription WHERE cid=? AND user = ?" : "SELECT id FROM pns_subscription WHERE cid=?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            if (userId > 0) {
                stmt.setInt(pos++, userId);
            }
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyList();
            }

            List<byte[]> ids = new LinkedList<>();
            do {
                ids.add(rs.getBytes(1));
            } while (rs.next());
            return ids;
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

}
