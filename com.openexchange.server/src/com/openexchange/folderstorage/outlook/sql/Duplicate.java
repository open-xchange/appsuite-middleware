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

package com.openexchange.folderstorage.outlook.sql;

import static com.openexchange.folderstorage.outlook.sql.Utility.getDatabaseService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import com.davekoelle.AlphanumComparator;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;

/**
 * {@link Duplicate}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Duplicate {

    private static final class ConnectionManager {
        private final DatabaseService databaseService;
        private final int contextId;
        protected boolean readWrite;
        protected Connection connection;

        protected ConnectionManager(final Connection connection, final boolean readWrite, final int contextId, final DatabaseService databaseService) {
            super();
            this.databaseService = databaseService;
            this.contextId = contextId;
            this.connection = connection;
            this.readWrite = readWrite;
        }

        protected void upgradeConnection() throws OXException {
            if (readWrite) {
                // Already a read-write connection
                return;
            }
            releaseConnection();
            this.connection = databaseService.getWritable(contextId);
            this.readWrite = true;
        }

        protected void releaseConnection() {
            final Connection connection = this.connection;
            if (null == connection) {
                return;
            }
            if (readWrite) {
                Databases.autocommit(connection);
                databaseService.backWritable(contextId, connection);
            } else {
                databaseService.backReadOnly(contextId, connection);
            }
            this.connection = null;
        }
    } // End of class ConnectionManager

    /**
     * Initializes a new {@link Duplicate}.
     */
    private Duplicate() {
        super();
    }

    /**
     * Detects & deletes duplicates from virtual table.
     *
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @return The name-2-IDs mapping
     * @throws OXException
     */
    public static Map<String, List<String>> lookupDuplicateNames(final int cid, final int tree, final int user) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        // Get a connection
        final Connection con = databaseService.getReadOnly(cid);
        final ConnectionManager cm = new ConnectionManager(con, false, cid, databaseService);
        try {
            return lookupDuplicateNames(cid, tree, user, cm);
        } catch (RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            cm.releaseConnection();
        }
    }

    /**
     * Detects & deletes duplicates from virtual table.
     *
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param con The connection
     * @return The name-2-IDs mapping
     * @throws OXException
     */
    public static Map<String, List<String>> lookupDuplicateNames(final int cid, final int tree, final int user, final ConnectionManager cm) throws OXException {
        if (null == cm) {
            return lookupDuplicateNames(cid, tree, user);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            int pos;
            final Map<String, String> name2parent;
            try {
                /*
                 * Detect possible duplicates
                 */
                // GROUP BY CLAUSE: ensure ONLY_FULL_GROUP_BY compatibility
                stmt = cm.connection.prepareStatement("SELECT name, COUNT(name), parentId FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? GROUP BY parentId, name");
                pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos, user);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    return java.util.Collections.emptyMap();
                }
                name2parent = new HashMap<String, String>();
                do {
                    if (rs.getInt(2) > 1) {
                        name2parent.put(rs.getString(1), rs.getString(3));
                    }
                } while (rs.next());
                if (name2parent.isEmpty()) {
                    return java.util.Collections.emptyMap();
                }
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }
            /*
             * Get folder identifiers
             */
            final Map<String, List<String>> name2ids = new HashMap<String, List<String>>();
            for (final Entry<String, String> entry : name2parent.entrySet()) {
                try {
                    stmt =
                        cm.connection.prepareStatement("SELECT folderId FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND name = ? AND parentId = ?");
                    final String name = entry.getKey();
                    pos = 1;
                    stmt.setInt(pos++, cid);
                    stmt.setInt(pos++, tree);
                    stmt.setInt(pos++, user);
                    stmt.setString(pos++, name);
                    stmt.setString(pos++, entry.getValue());
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        final List<String> folderIds = new ArrayList<String>(4);
                        do {
                            folderIds.add(rs.getString(1));
                        } while (rs.next());
                        java.util.Collections.sort(folderIds, new AlphanumComparator(Locale.US));
                        folderIds.remove(0); // Remove first
                        name2ids.put(name, folderIds);
                    }
                } finally {
                    Databases.closeSQLStuff(rs, stmt);
                }
            }
            /*
             * Delete duplicates from table
             */
            if (!name2ids.isEmpty()) {
                /*
                 * Do it...
                 */
                final boolean transactional = !cm.connection.getAutoCommit() && cm.readWrite;
                if (transactional) {
                    // Already in transaction state
                    deleteEntries(name2ids, cid, tree, user, cm.connection);
                } else {
                    cm.upgradeConnection();
                    final Connection connection = cm.connection;
                    // Start transaction
                    int rollback = 0;
                    try {
                        connection.setAutoCommit(false); // BEGIN
                        rollback = 1;
                        deleteEntries(name2ids, cid, tree, user, connection);
                        connection.commit(); // COMMIT
                        rollback = 2;
                    } finally {
                        if (rollback > 0) {
                            if (rollback==1) {
                                Databases.rollback(connection); // ROLLBACK
                            }
                            Databases.autocommit(connection);
                        }
                    }
                }
            }
            /*
             * Return
             */
            return name2ids;
        } catch (SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private static void deleteEntries(final Map<String, List<String>> name2ids, final int cid, final int tree, final int user, final Connection con) throws OXException {
        for (final Entry<String, List<String>> entry : name2ids.entrySet()) {
            final List<String> folderIds = entry.getValue();
            final int sz = folderIds.size();
            for (int i = 0; i < sz; i++) { // All but first entry
                Delete.deleteFolder(cid, tree, user, folderIds.get(i), false, false, con);
            }
        }
    }

}
