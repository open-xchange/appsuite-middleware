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

package com.openexchange.filestore.sproxyd.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.java.util.UUIDs;

/**
 * {@link SproxydDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SproxydDeleteListener implements DeleteListener {

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        if (event.getType() == DeleteEvent.TYPE_USER) {
            deleteUserEntriesFromDB(event, writeCon);
        } else if (event.getType() == DeleteEvent.TYPE_CONTEXT) {
            deleteContextEntriesFromDB(event, writeCon);
        } else {
            return;
        }
    }

    private void deleteContextEntriesFromDB(DeleteEvent event, Connection writeCon) throws OXException {
        int contextId = event.getContext().getContextId();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            int pos = 1;
            stmt = writeCon.prepareStatement("SELECT scality_id FROM scality_filestore WHERE cid = ?");
            stmt.setInt(pos, contextId);
            rs = stmt.executeQuery();
            List<UUID> uuids = new LinkedList<UUID>();
            while (rs.next()) {
                uuids.add(UUIDs.toUUID(rs.getBytes(1)));
            }
            // TODO: Delete queired files

            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            pos = 1;
            stmt = writeCon.prepareStatement("DELETE FROM scality_filestore WHERE cid = ?");
            stmt.setInt(pos, contextId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (Exception e) {
            throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private void deleteUserEntriesFromDB(DeleteEvent event, Connection writeCon) throws OXException {
        int contextId = event.getContext().getContextId();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            int pos = 1;
            stmt = writeCon.prepareStatement("SELECT scality_id FROM scality_filestore WHERE cid = ? AND user = ?");
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos, event.getId());
            rs = stmt.executeQuery();
            List<UUID> uuids = new LinkedList<UUID>();
            while (rs.next()) {
                uuids.add(UUIDs.toUUID(rs.getBytes(1)));
            }
            // TODO: Delete queired files

            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            pos = 1;
            stmt = writeCon.prepareStatement("DELETE FROM scality_filestore WHERE cid = ? AND user = ?");
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos, event.getId());
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
