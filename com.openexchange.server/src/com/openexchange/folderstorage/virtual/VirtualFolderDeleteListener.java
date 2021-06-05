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

package com.openexchange.folderstorage.virtual;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCode;
import com.openexchange.groupware.delete.DeleteListener;

/**
 * {@link VirtualFolderDeleteListener} - The delete listener for virtual folder tables.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class VirtualFolderDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link VirtualFolderDeleteListener}.
     */
    public VirtualFolderDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(final DeleteEvent event, final Connection readCon, final Connection writeCon) throws OXException {
        if (event.getType() != DeleteEvent.TYPE_USER) {
            return;
        }
        PreparedStatement stmt = null;
        try {
            final int contextId = event.getContext().getContextId();
            final int userId = event.getId();
            // Drop user's subscriptions
            stmt = writeCon.prepareStatement("DELETE FROM virtualSubscription WHERE cid = ? AND user = ?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos, userId);
            stmt.executeUpdate();
            stmt.close();
            stmt = writeCon.prepareStatement("DELETE FROM virtualBackupSubscription WHERE cid = ? AND user = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos, userId);
            stmt.executeUpdate();
            stmt.close();
            // Drop user's folder permissions
            stmt = writeCon.prepareStatement("DELETE FROM virtualPermission WHERE cid = ? AND user = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos, userId);
            stmt.executeUpdate();
            stmt.close();
            stmt = writeCon.prepareStatement("DELETE FROM virtualBackupPermission WHERE cid = ? AND user = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos, userId);
            stmt.executeUpdate();
            stmt.close();
            // Drop user's folders
            stmt = writeCon.prepareStatement("DELETE FROM virtualTree WHERE cid = ? AND user = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos, userId);
            stmt.executeUpdate();
            stmt.close();
            stmt = writeCon.prepareStatement("DELETE FROM virtualBackupTree WHERE cid = ? AND user = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw DeleteFailedExceptionCode.SQL_ERROR.create( e, e.getMessage());
        } finally {
            if (null != stmt) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LoggerFactory.getLogger(VirtualFolderDeleteListener.class).error("", e);
                }
            }
        }

    }

}
