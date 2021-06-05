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

package com.openexchange.groupware.infostore.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.filestore.AbstractFileLocationHandler;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.Reference;
import com.openexchange.user.User;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntProcedure;


/**
 * {@link InfostoreFilestoreLocationUpdater}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public class InfostoreFilestoreLocationUpdater extends AbstractFileLocationHandler {

    /**
     * Initializes a new {@link InfostoreFilestoreLocationUpdater}.
     */
    public InfostoreFilestoreLocationUpdater() {
        super();
    }

    @Override
    public void updateFileLocations(Map<String, String> prevFileName2newFileName, int contextId, Connection con) throws SQLException {
        String selectStmt = "SELECT file_store_location FROM infostore_document WHERE cid=? AND file_store_location IN ";
        String updateStmt = "UPDATE infostore_document SET file_store_location = ? WHERE cid = ? AND file_store_location = ?";
        updateFileLocationsUsing(prevFileName2newFileName, contextId, selectStmt, updateStmt, con);
    }

    @Override
    public Set<String> determineFileLocationsFor(int contextId, Connection con) throws OXException, SQLException {
        // Determine InfoStore folders
        TIntObjectMap<TIntList> folders = fetchFolders(contextId, con);

        // Determine those user that do not have a dedicated storage
        Context context = ContextStorage.getInstance().getContext(contextId);
        UserStorage userStorage = UserStorage.getInstance();
        Set<String> locations = new LinkedHashSet<String>();
        for (TIntObjectIterator<TIntList> it = folders.iterator(); it.hasNext();) {
            it.advance();

            // Check associated user
            User user = userStorage.getUser(it.key(), context);
            if (user.getFilestoreId() <= 0) {
                // No specific, but context-related file storage.
                // Therefore applicable
                locations.addAll(getFileLocationsFor(it.value(), contextId, con));
            }
        }

        return locations;
    }

    @Override
    public Set<String> determineFileLocationsFor(int userId, int contextId, Connection con) throws OXException, SQLException {
        // Determine InfoStore folders
        TIntList folderIds = fetchFolders(userId, contextId, con);

        // Get folders' files
        return getFileLocationsFor(folderIds, contextId, con);
    }

    // -----------------------------------------------------------------------------------------------------------------

    private Set<String> getFileLocationsFor(TIntList folderIds, final int contextId, final Connection con) throws OXException, SQLException {
        final Reference<Exception> error = new Reference<Exception>();
        final Set<String> locations = new LinkedHashSet<String>();
        folderIds.forEach(new TIntProcedure() {

            @Override
            public boolean execute(int folderId) {
                boolean keepOn = false;
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = con.prepareStatement("SELECT d.file_store_location FROM infostore AS i JOIN infostore_document AS d ON i.cid=? AND d.cid=? AND i.id=d.infostore_id WHERE i.cid=? and folder_id=? AND d.file_store_location IS NOT NULL");
                    stmt.setInt(1, contextId);
                    stmt.setInt(2, contextId);
                    stmt.setInt(3, contextId);
                    stmt.setInt(4, folderId);
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        locations.add(rs.getString(1));
                    }
                    keepOn = true;
                } catch (SQLException e) {
                    error.setValue(e);
                } catch (RuntimeException e) {
                    error.setValue(e);
                } finally {
                    Databases.closeSQLStuff(rs, stmt);
                }
                return keepOn;
            }
        });
        Exception exception = error.getValue();
        if (null != exception) {
            if (exception instanceof SQLException) {
                throw (SQLException) exception;
            }
            throw OXException.general("", exception);
        }
        return locations;
    }

    private TIntObjectMap<TIntList> fetchFolders(int contextId, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT created_from, fuid FROM oxfolder_tree WHERE cid=? AND module=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, FolderObject.INFOSTORE);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return new TIntObjectHashMap<TIntList>(0);
            }
            TIntObjectMap<TIntList> map = new TIntObjectHashMap<TIntList>(2048);
            do {
                int owner = rs.getInt(1);
                TIntList folderIds = map.get(owner);
                if (null == folderIds) {
                    folderIds = new TIntLinkedList();
                    map.put(owner, folderIds);
                }
                int folderId = rs.getInt(2);
                folderIds.add(folderId);
            } while (rs.next());
            return map;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private TIntList fetchFolders(int userId, int contextId, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT fuid FROM oxfolder_tree WHERE cid=? AND created_from=? AND module=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setInt(3, FolderObject.INFOSTORE);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return new TIntLinkedList();
            }
            TIntList folderIds = new TIntLinkedList();
            do {
                int folderId = rs.getInt(1);
                folderIds.add(folderId);
            } while (rs.next());
            return folderIds;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

}
