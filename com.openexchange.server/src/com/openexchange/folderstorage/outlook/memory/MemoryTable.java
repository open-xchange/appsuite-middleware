/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.folderstorage.outlook.memory;

import gnu.trove.ConcurrentTIntObjectHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.outlook.memory.impl.MemoryFolderImpl;
import com.openexchange.folderstorage.outlook.memory.impl.MemoryTreeImpl;
import com.openexchange.folderstorage.outlook.sql.Utility;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link MemoryTable} - The in-memory representation of the virtual folder table.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MemoryTable {

    /**
     * Gets the memory table for specified session, creates it if absent for given session.
     *
     * @param session The session
     * @return The memory table for specified session
     * @throws OXException If creation of memory table fails
     */
    public static MemoryTable getMemoryTableFor(final Session session) throws OXException {
        return getMemoryTableFor(session, true);
    }

    /**
     * Gets the memory table for specified session.
     *
     * @param session The session
     * @return The memory table for specified session or <code>null</code> if absent
     */
    public static MemoryTable optMemoryTableFor(final Session session) {
        try {
            return getMemoryTableFor(session, false);
        } catch (final OXException e) {
            // Cannot occur
            return null;
        }
    }

    /**
     * Gets the memory table for specified session.
     *
     * @param session The session
     * @param createIfAbsent <code>true</code> to create if absent; otherwise <code>false</code> to possibly return <code>null</code> if
     *            there is no memory table
     * @return The memory table for specified session or <code>null</code> if there is no memory table and <code>createIfAbsent</code> is
     *         <code>false</code>
     * @throws OXException If creation of memory table fails
     */
    private static MemoryTable getMemoryTableFor(final Session session, final boolean createIfAbsent) throws OXException {
        return getMemoryTable0(session, createIfAbsent);
    }

    private static final ConcurrentMap<Key, MemoryTable> MAP = new ConcurrentHashMap<Key, MemoryTable>(1024, 0.9f, 1);

    private static MemoryTable getMemoryTable0(final Session session, final boolean createIfAbsent) throws OXException {
        final Key key = keyFor(session);
        MemoryTable memoryTable = MAP.get(key);
        if (null != memoryTable) {
            return memoryTable;
        }
        if (!createIfAbsent) {
            return null;
        }
        final MemoryTable nuMemoryTable = new MemoryTable();
        nuMemoryTable.initialize(session.getUserId(), session.getContextId());
        memoryTable = MAP.putIfAbsent(key, nuMemoryTable);
        if (null == memoryTable) {
            memoryTable = nuMemoryTable;
        }
        return memoryTable;
    }

    /**
     * Drops the memory table from specified session
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public static void dropMemoryTableFrom(int userId, int contextId) {
        MAP.remove(keyFor(userId, contextId));
    }

    /*-
     * ------------------------ MEMBER STUFF -----------------------------
     */

    private final ConcurrentTIntObjectHashMap<MemoryTree> treeMap;

    /**
     * Initializes a new {@link MemoryTable}.
     */
    private MemoryTable() {
        super();
        treeMap = new ConcurrentTIntObjectHashMap<MemoryTree>();
    }

    /**
     * Clears this memory table.
     */
    public void clear() {
        treeMap.clear();
    }

    /**
     * Checks if this memory table contains specified memory tree.
     *
     * @param treeId The tree identifier
     * @return <code>true</code> if this memory table contains specified memory tree; otherwise <code>false</code>
     */
    public boolean containsTree(final int treeId) {
        return treeMap.containsKey(treeId);
    }

    /**
     * Gets the specified memory tree; atomically creates it if absent.
     *
     * @param treeId The memory tree identifier
     * @param session The session providing user data
     * @return The memory tree
     * @throws OXException If creating memory tree fail
     */
    public MemoryTree getTree(final int treeId, final Session session) throws OXException {
        return getTree(treeId, session.getUserId(), session.getContextId());
    }

    /**
     * Gets the specified memory tree; atomically creates it if absent.
     *
     * @param treeId The memory tree identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The memory tree
     * @throws OXException If creating memory tree fail
     */
    public MemoryTree getTree(final int treeId, final int userId, final int contextId) throws OXException {
        MemoryTree memoryTree = treeMap.get(treeId);
        if (null == memoryTree) {
            synchronized (treeMap) {
                memoryTree = treeMap.get(treeId);
                if (null == memoryTree) {
                    memoryTree = initializeTree(treeId, userId, contextId);
                }
            }
        }
        return memoryTree;
    }

    /**
     * Gets the specified memory tree.
     *
     * @param treeId The memory tree identifier
     * @return The memory tree or <code>null</code> if absent
     */
    public MemoryTree optTree(final int treeId) {
        return treeMap.get(treeId);
    }

    /**
     * Checks if this memory table is empty.
     *
     * @return <code>true</code> if this memory table is empty; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return treeMap.isEmpty();
    }

    /**
     * Removes the specified memory tree from this memory table.
     *
     * @param treeId The memory tree identifier
     * @return The removed memory tree or <code>null</code> if there was no such memory tree
     */
    public MemoryTree remove(final int treeId) {
        return treeMap.remove(treeId);
    }

    /**
     * Gets the number of memory trees held by this memory table
     *
     * @return The number of memory trees
     */
    public int size() {
        return treeMap.size();
    }

    /*-
     * ------------------------------- INIT STUFF -------------------------------
     */

    private void initialize(final int userId, final int contextId) throws OXException {
        final DatabaseService databaseService = Utility.getDatabaseService();
        // Get a connection
        Connection con = databaseService.getReadOnly(contextId);
        try {
            initialize(userId, contextId, con);
        } finally {
            databaseService.backReadOnly(contextId, con);
        }
    }

    private void initialize(final int userId, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT t.tree, t.folderId, t.parentId, t.name, t.lastModified, t.modifiedBy, s.subscribed, t.sortNum FROM virtualTree AS t LEFT JOIN virtualSubscription AS s ON t.cid = s.cid AND t.tree = s.tree AND t.user = s.user AND t.folderId = s.folderId WHERE t.cid = ? AND t.user = ? ORDER BY t.tree");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                /*
                 * No tree found in table
                 */
                return;
            }
            int tree = rs.getInt(1);
            MemoryTree memoryTree = new MemoryTreeImpl(tree);
            treeMap.put(tree, memoryTree);
            do {
                /*
                 * Check for tree identifier
                 */
                {
                    final int tid = rs.getInt(1);
                    if (tree != tid) {
                        tree = tid;
                        memoryTree = new MemoryTreeImpl(tree);
                        treeMap.put(tree, memoryTree);
                    }
                }
                final MemoryFolderImpl memoryFolder = new MemoryFolderImpl();
                memoryFolder.setId(rs.getString(2));
                // Set optional modified-by
                {
                    final int modifiedBy = rs.getInt(6);
                    if (rs.wasNull()) {
                        memoryFolder.setModifiedBy(-1);
                    } else {
                        memoryFolder.setModifiedBy(modifiedBy);
                    }
                }
                // Set optional last-modified time stamp
                {
                    final long date = rs.getLong(5);
                    if (rs.wasNull()) {
                        memoryFolder.setLastModified(null);
                    } else {
                        memoryFolder.setLastModified(new Date(date));
                    }
                }
                memoryFolder.setName(rs.getString(4));
                memoryFolder.setParentId(rs.getString(3));
                {
                    final int subscribed = rs.getInt(7);
                    if (!rs.wasNull()) {
                        memoryFolder.setSubscribed(Boolean.valueOf(subscribed > 0));
                    }
                }
                {
                    final int sortNum = rs.getInt(8);
                    if (!rs.wasNull()) {
                        memoryFolder.setSortNum(sortNum);
                    }
                }
                // Add permissions in a separate query
                addPermissions(memoryFolder, tree, userId, contextId, con);
                // Finally add folder to memory tree
                memoryTree.getCrud().put(memoryFolder);
            } while (rs.next());
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * (Re-)Initializes specified tree.
     *
     * @param treeId The tree identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The (re-)initialized tree
     * @throws OXException If initialization fails
     */
    public MemoryTree initializeTree(final int treeId, final int userId, final int contextId) throws OXException {
        final DatabaseService databaseService = Utility.getDatabaseService();
        // Get a connection
        final Connection con = databaseService.getReadOnly(contextId);
        try {
            return initializeTree(treeId, userId, contextId, con);
        } finally {
            databaseService.backReadOnly(contextId, con);
        }
    }

    /**
     * (Re-)Initializes specified tree.
     *
     * @param treeId The tree identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param con A connection
     * @return The (re-)initialized tree
     * @throws OXException If initialization fails
     */
    public MemoryTree initializeTree(final int treeId, final int userId, final int contextId, final Connection con) throws OXException {
        if (null == con) {
            return initializeTree(treeId, userId, contextId);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT t.folderId, t.parentId, t.name, t.lastModified, t.modifiedBy, s.subscribed, t.sortNum FROM virtualTree AS t LEFT JOIN virtualSubscription AS s ON t.cid = s.cid AND t.tree = s.tree AND t.user = s.user AND t.folderId = s.folderId WHERE t.cid = ? AND t.user = ? AND t.tree = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setInt(3, treeId);
            rs = stmt.executeQuery();
            final MemoryTree memoryTree = new MemoryTreeImpl(treeId);
            if (!rs.next()) {
                treeMap.put(treeId, memoryTree);
                return memoryTree;
            }
            do {
                final MemoryFolderImpl memoryFolder = new MemoryFolderImpl();
                memoryFolder.setId(rs.getString(1));
                // Set optional modified-by
                {
                    final int modifiedBy = rs.getInt(5);
                    if (rs.wasNull()) {
                        memoryFolder.setModifiedBy(-1);
                    } else {
                        memoryFolder.setModifiedBy(modifiedBy);
                    }
                }
                // Set optional last-modified time stamp
                {
                    final long date = rs.getLong(4);
                    if (rs.wasNull()) {
                        memoryFolder.setLastModified(null);
                    } else {
                        memoryFolder.setLastModified(new Date(date));
                    }
                }
                memoryFolder.setName(rs.getString(3));
                memoryFolder.setParentId(rs.getString(2));
                {
                    final int subscribed = rs.getInt(6);
                    if (!rs.wasNull()) {
                        memoryFolder.setSubscribed(Boolean.valueOf(subscribed > 0));
                    }
                }
                {
                    final int sortNum = rs.getInt(7);
                    if (!rs.wasNull()) {
                        memoryFolder.setSortNum(sortNum);
                    }
                }
                // Add permissions in a separate query
                addPermissions(memoryFolder, treeId, userId, contextId, con);
                // Finally add folder to memory tree
                memoryTree.getCrud().put(memoryFolder);
            } while (rs.next());
            /*
             * Return newly initialized tree
             */
            treeMap.put(treeId, memoryTree);
            return memoryTree;
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * (Re-)Initializes specified folder.
     *
     * @param folderId The folder identifier
     * @param treeId The tree identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The (re-)initialized folder
     * @throws OXException If initialization fails
     */
    public MemoryFolder initializeFolder(final String folderId, final int treeId, final int userId, final int contextId) throws OXException {
        final DatabaseService databaseService = Utility.getDatabaseService();
        // Get a connection
        final Connection con = databaseService.getWritable(contextId);
        try {
            return initializeFolder(folderId, treeId, userId, contextId, con);
        } finally {
            databaseService.backWritableAfterReading(contextId, con);
        }
    }

    /**
     * (Re-)Initializes specified folder.
     *
     * @param folderId The folder identifier
     * @param treeId The tree identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param con A connection
     * @return The (re-)initialized folder
     * @throws OXException If initialization fails
     */
    public MemoryFolder initializeFolder(final String folderId, final int treeId, final int userId, final int contextId, final Connection con) throws OXException {
        if (null == con) {
            return initializeFolder(folderId, treeId, userId, contextId);
        }
        MemoryTree memoryTree = optTree(treeId);
        if (null == memoryTree) {
            memoryTree = getTree(treeId, userId, contextId);
            return memoryTree.getCrud().get(folderId);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT t.parentId, t.name, t.lastModified, t.modifiedBy, s.subscribed, t.sortNum FROM virtualTree AS t LEFT JOIN virtualSubscription AS s ON t.cid = s.cid AND t.tree = s.tree AND t.user = s.user AND t.folderId = s.folderId WHERE t.cid = ? AND t.user = ? AND t.tree = ? AND t.folderId = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setInt(3, treeId);
            stmt.setString(4, folderId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw FolderExceptionErrorMessage.NOT_FOUND.create(folderId, Integer.valueOf(treeId));
            }
            final MemoryFolderImpl memoryFolder = new MemoryFolderImpl();
            memoryFolder.setId(folderId);
            // Set optional modified-by
            {
                final int modifiedBy = rs.getInt(4);
                if (rs.wasNull()) {
                    memoryFolder.setModifiedBy(-1);
                } else {
                    memoryFolder.setModifiedBy(modifiedBy);
                }
            }
            // Set optional last-modified time stamp
            {
                final long date = rs.getLong(3);
                if (rs.wasNull()) {
                    memoryFolder.setLastModified(null);
                } else {
                    memoryFolder.setLastModified(new Date(date));
                }
            }
            memoryFolder.setName(rs.getString(2));
            memoryFolder.setParentId(rs.getString(1));
            {
                final int subscribed = rs.getInt(5);
                if (!rs.wasNull()) {
                    memoryFolder.setSubscribed(Boolean.valueOf(subscribed > 0));
                }
            }
            {
                final int sortNum = rs.getInt(6);
                if (!rs.wasNull()) {
                    memoryFolder.setSortNum(sortNum);
                }
            }
            // Add permissions in a separate query
            addPermissions(memoryFolder, treeId, userId, contextId, con);
            // Finally add folder to memory tree
            memoryTree.getCrud().put(memoryFolder);
            return memoryFolder;
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    private static void addPermissions(final MemoryFolderImpl memoryFolder, final int treeId, final int userId, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT entity, fp, orp, owp, odp, adminFlag, groupFlag, system FROM virtualPermission WHERE cid = ? AND user = ? AND tree = ? AND folderId = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setInt(3, treeId);
            stmt.setString(4, memoryFolder.getId());
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return;
            }
            final List<Permission> list = new ArrayList<Permission>(4);
            do {
                list.add(new MemoryPermission(rs));
            } while (rs.next());
            memoryFolder.setPermissions(list.toArray(new Permission[list.size()]));
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    private static Key keyFor(final Session session) {
        return new Key(session.getUserId(), session.getContextId());
    }

    private static Key keyFor(final int userId, final int contextId) {
        return new Key(userId, contextId);
    }

    private static final class Key {

        private final int cid;

        private final int user;

        private final int hash;

        protected Key(final int user, final int cid) {
            super();
            this.user = user;
            this.cid = cid;
            final int prime = 31;
            int result = 1;
            result = prime * result + cid;
            result = prime * result + user;
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key other = (Key) obj;
            if (cid != other.cid) {
                return false;
            }
            if (user != other.user) {
                return false;
            }
            return true;
        }

    }

}
