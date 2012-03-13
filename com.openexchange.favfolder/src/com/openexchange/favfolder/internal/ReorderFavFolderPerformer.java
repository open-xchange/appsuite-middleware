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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.favfolder.internal;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ReorderFavFolderPerformer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ReorderFavFolderPerformer extends AbstractFavFolderPerformer {

    /**
     * Initializes a new {@link ReorderFavFolderPerformer}.
     */
    public ReorderFavFolderPerformer(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    /**
     * Reorder according to specified folder collection's iterator order.
     *
     * @param treeId The tree identifier
     * @param folderIds The identifiers of the folders tom reorder
     * @param session The session providing user data
     * @return The reordered folder identifiers
     * @throws OXException If subscribe fails for any reason
     */
    public List<String> reorderFolders(final int treeId, final Collection<String> folderIds, final ServerSession session) throws OXException {
        return reorderSortNums(treeId, folderIds, session);
    }

    private List<String> reorderSortNums(final int tree, final Collection<String> folderIds, final ServerSession session) throws OXException {
        final DatabaseService databaseService = getService(DatabaseService.class);
        final int contextId = session.getContextId();
        final int userId = session.getUserId();
        final Connection con = databaseService.getWritable(contextId);
        try {
            final TIntObjectMap<Set<String>> map;
            final Set<String> toConsider;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement("SELECT folderId, sortNum FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND parentId = ?");
                int pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, userId);
                stmt.setString(pos, FolderStorage.ROOT_ID);
                rs = stmt.executeQuery();
                map = new TIntObjectHashMap<Set<String>>(32);
                toConsider = new HashSet<String>(32);
                while (rs.next()) {
                    int sortNum = rs.getInt(2);
                    if (rs.wasNull()) {
                        sortNum = 0;
                    }
                    Set<String> set = map.get(sortNum);
                    if (null == set) {
                        set = new HashSet<String>(4);
                        map.put(sortNum, set);
                    }
                    final String folderId = rs.getString(1);
                    set.add(folderId);
                    toConsider.add(folderId);
                }
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }
            stmt = null;
            rs = null;
            /*
             * Insert according to iterator's order
             */
            final TIntObjectMap<Set<String>> newOrder = new TIntObjectHashMap<Set<String>>(map.size());
            final Set<String> considered = new HashSet<String>(folderIds);
            int curSortNum = 1;
            for (final String folderId : folderIds) {
                if (toConsider.contains(folderId)) { // root-level folder
                    Set<String> set = newOrder.get(curSortNum);
                    if (null == set) {
                        set = new HashSet<String>(4);
                        newOrder.put(curSortNum, set);
                    }
                    set.add(folderId);
                    curSortNum++;
                } else {
                    considered.remove(folderId);
                }
            }
            /*-
             * Insert existing
             *
             * At first, detect max. sort number
             */
            int maxSortNum = 0;
            for (final int num : map.keys()) {
                if (num > maxSortNum) {
                    maxSortNum = num;
                }
            }
            /*
             * Special handling for sortNum=0
             */
            if (map.containsKey(0)) {
                final Set<String> set = map.get(0);
                for (final String fid : set) {
                    if (!considered.contains(fid)) {
                        Set<String> set2 = newOrder.get(0);
                        if (null == set2) {
                            set2 = new HashSet<String>(set.size());
                            newOrder.put(0, set);
                        }
                        set2.add(fid);
                    }
                }
            }
            /*
             * Append others
             */
            for (int i = 1; i <= maxSortNum; i++) { // Start at 1, because 0 has been explicitly handled before
                if (map.containsKey(i)) {
                    // Hit
                    final Set<String> set = map.get(i);
                    for (final String fid : set) {
                        if (!considered.contains(fid)) {
                            Set<String> set2 = newOrder.get(curSortNum);
                            if (null == set2) {
                                set2 = new HashSet<String>(1); // Singleton HashSet
                                newOrder.put(curSortNum, set);
                            }
                            set2.add(fid);
                            curSortNum++;
                        }
                    }
                }
            }
            /*
             * Write to DB
             */
            try {
                con.setAutoCommit(false); // BEGIN
                final PreparedStatement ps =
                    stmt =
                        con.prepareStatement("UPDATE virtualTree SET sortNum = ? WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?");
                ps.setInt(2, contextId);
                ps.setInt(3, tree);
                ps.setInt(4, userId);
                final TIntObjectProcedureImpl procedure = new TIntObjectProcedureImpl(ps);
                newOrder.forEachEntry(procedure);
                final SQLException exception = procedure.getException();
                if (null != exception) {
                    throw exception;
                }
                stmt.executeBatch();
                con.commit(); // COMMIT
            } catch (final SQLException e) {
                Databases.rollback(con);
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } catch (final RuntimeException e) {
                Databases.rollback(con);
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }
            /*
             * Re-fetch from DB
             */
            final List<String> ret = new ArrayList<String>(32);
            try {
                stmt = con.prepareStatement("SELECT folderId FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND parentId = ? ORDER BY sortNum ASC, name ASC");
                int pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, userId);
                stmt.setString(pos, FolderStorage.ROOT_ID);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    ret.add(rs.getString(1));
                }
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }
            return ret;
        } finally {
            Databases.autocommit(con);
            databaseService.backWritable(contextId, con);
        }
    }

}
