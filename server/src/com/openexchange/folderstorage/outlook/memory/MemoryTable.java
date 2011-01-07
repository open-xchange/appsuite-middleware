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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import java.util.concurrent.locks.Lock;
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.DatabaseService;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.outlook.memory.impl.MemoryFolderImpl;
import com.openexchange.folderstorage.outlook.memory.impl.MemoryTreeImpl;
import com.openexchange.folderstorage.outlook.sql.Utility;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link MemoryTable}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MemoryTable {

    private static final String PARAM_MEMORY_TABLE = "com.openexchange.folderstorage.outlook.memory.memoryTable";

    /**
     * Gets the memory table for specified session.
     * 
     * @param session The session
     * @param createIfAbsent <code>true</code> to create if absent; otherwise <code>false</code> to possible return <code>null</code> if there is no memory table
     * @return The memory table for specified session or <code>null</code> if there is no memory table and <code>createIfAbsent</code> is <code>false</code>
     * @throws FolderException If creation of memory table fails
     */
    public static MemoryTable getMemoryTableFor(final Session session, final boolean createIfAbsent) throws FolderException {
        final Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
        if (null != lock) {
            lock.lock();
            try {
                return getMemoryTable0(session, createIfAbsent);
            } finally {
                lock.unlock();
            }
        }
        /*
         * Standard synchronization
         */
        synchronized (session) {
            return getMemoryTable0(session, createIfAbsent);
        }
    }

    private static MemoryTable getMemoryTable0(final Session session, final boolean createIfAbsent) throws FolderException {
        MemoryTable memoryTable = (MemoryTable) session.getParameter(PARAM_MEMORY_TABLE);
        if (null != memoryTable) {
            return memoryTable;
        }
        if (!createIfAbsent) {
            return null;
        }
        memoryTable = new MemoryTable();
        memoryTable.initialize(session.getUserId(), session.getContextId());
        session.setParameter(PARAM_MEMORY_TABLE, memoryTable);
        return memoryTable;
    }

    /**
     * Drops the memory table from specified session
     * 
     * @param session The session
     */
    public static void dropMemoryTableFrom(final Session session) {
        final Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
        if (null != lock) {
            lock.lock();
            try {
                session.setParameter(PARAM_MEMORY_TABLE, null);
            } finally {
                lock.unlock();
            }
        } else {
            synchronized (MemoryTable.class) {
                session.setParameter(PARAM_MEMORY_TABLE, null);
            }
        }
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

    public void clear() {
        treeMap.clear();
    }

    public boolean containsTree(final String treeId) {
        return treeMap.containsKey(Integer.parseInt(treeId));
    }

    public MemoryTree getTree(final String treeId) {
        final MemoryTree tree = treeMap.get(Integer.parseInt(treeId));
        if (null == tree) {
            return MemoryTreeImpl.EMPTY_TREE;
        }
        return tree;
    }

    public boolean isEmpty() {
        return treeMap.isEmpty();
    }

    public MemoryTree remove(final String treeId) {
        return treeMap.remove(Integer.parseInt(treeId));
    }

    public int size() {
        return treeMap.size();
    }

    /*-
     * ------------------------------- INIT STUFF -------------------------------
     */

    private void initialize(final int userId, final int contextId) throws FolderException {
        final DatabaseService databaseService = Utility.getDatabaseService();
        // Get a connection
        final Connection con;
        try {
            con = databaseService.getWritable(contextId);
        } catch (final DBPoolingException e) {
            throw new FolderException(e);
        }
        try {
            initialize(userId, contextId, con);
        } finally {
            databaseService.backWritable(contextId, con);
        }
    }

    public void initializeTree(final String treeId, final int userId, final int contextId) throws FolderException {
        final DatabaseService databaseService = Utility.getDatabaseService();
        // Get a connection
        final Connection con;
        try {
            con = databaseService.getWritable(contextId);
        } catch (final DBPoolingException e) {
            throw new FolderException(e);
        }
        try {
            initializeTree(treeId, userId, contextId, con);
        } finally {
            databaseService.backWritable(contextId, con);
        }
    }

    public void initializeFolder(final String folderId, final String treeId, final int userId, final int contextId) throws FolderException {
        final DatabaseService databaseService = Utility.getDatabaseService();
        // Get a connection
        final Connection con;
        try {
            con = databaseService.getWritable(contextId);
        } catch (final DBPoolingException e) {
            throw new FolderException(e);
        }
        try {
            initializeFolder(folderId, treeId, userId, contextId, con);
        } finally {
            databaseService.backWritable(contextId, con);
        }
    }

    private void initialize(final int userId, final int contextId, final Connection con) throws FolderException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt =
                con.prepareStatement("SELECT t.tree, t.folderId, t.parentId, t.name, t.lastModified, t.modifiedBy, s.subscribed FROM virtualTree AS t LEFT JOIN virtualSubscription AS s ON t.cid = s.cid AND t.tree = s.tree AND t.user = s.user AND t.folderId = s.folderId WHERE t.cid = ? AND t.user = ? ORDER BY t.tree");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                int tree = rs.getInt(1);
                MemoryTree memoryTree = new MemoryTreeImpl();
                treeMap.put(tree, memoryTree);
                do {
                    /*
                     * Check for tree identifier
                     */
                    {
                        final int tid = rs.getInt(1);
                        if (tree != tid) {
                            tree = tid;
                            memoryTree = new MemoryTreeImpl();
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
                    // Add permissions in a separate query
                    addPermissions(memoryFolder, tree, userId, contextId, con);
                    // Finally add folder to memory tree
                    memoryTree.getCrud().put(memoryFolder);
                } while (rs.next());
            }

        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    public void initializeTree(final String treeId, final int userId, final int contextId, final Connection con) throws FolderException {
        if (null == con) {
            initializeTree(treeId, userId, contextId);
            return;
        }
        final int tree = Integer.parseInt(treeId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt =
                con.prepareStatement("SELECT t.folderId, t.parentId, t.name, t.lastModified, t.modifiedBy, s.subscribed FROM virtualTree AS t LEFT JOIN virtualSubscription AS s ON t.cid = s.cid AND t.tree = s.tree AND t.user = s.user AND t.folderId = s.folderId WHERE t.cid = ? AND t.user = ? AND t.tree = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setInt(3, tree);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return;
            }
            final MemoryTree memoryTree = new MemoryTreeImpl();
            treeMap.put(tree, memoryTree);
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

    public void initializeFolder(final String folderId, final String treeId, final int userId, final int contextId, final Connection con) throws FolderException {
        if (null == con) {
            initializeFolder(folderId, treeId, userId, contextId);
            return;
        }
        final int tree = Integer.parseInt(treeId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt =
                con.prepareStatement("SELECT t.parentId, t.name, t.lastModified, t.modifiedBy, s.subscribed FROM virtualTree AS t LEFT JOIN virtualSubscription AS s ON t.cid = s.cid AND t.tree = s.tree AND t.user = s.user AND t.folderId = s.folderId WHERE t.cid = ? AND t.user = ? AND t.tree = ? AND t.folderId = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setInt(3, tree);
            stmt.setString(4, folderId);
            rs = stmt.executeQuery();
            ;
            if (!rs.next()) {
                return;
            }
            final MemoryTree memoryTree = treeMap.get(tree);
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
            // Add permissions in a separate query
            addPermissions(memoryFolder, tree, userId, contextId, con);
            // Finally add folder to memory tree
            memoryTree.getCrud().put(memoryFolder);
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    private static void addPermissions(final MemoryFolderImpl memoryFolder, final int treeId, final int userId, final int contextId, final Connection con) throws FolderException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt =
                con.prepareStatement("SELECT entity, fp, orp, owp, odp, adminFlag, groupFlag, system FROM virtualPermission WHERE cid = ? AND user = ? AND tree = ? AND folderId = ?");
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

}
