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
import gnu.trove.procedure.TIntObjectErrorAwareAbstractProcedure;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UnsubscribeFavFolderPerformer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnsubscribeFavFolderPerformer extends AbstractFavFolderPerformer {

    /**
     * Initializes a new {@link UnsubscribeFavFolderPerformer}.
     */
    public UnsubscribeFavFolderPerformer(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    /**
     * Unsubscribes specified folders
     *
     * @param treeId The tree identifier
     * @param folderIds The identifiers of the folders to unsubscribe
     * @param session The session providing user data
     * @throws OXException If subscribe fails for any reason
     */
    public void unsubscribeFolders(final int treeId, final Collection<String> folderIds, final ServerSession session) throws OXException {
        final FolderService folderService = getService(FolderService.class);
        final String treeId2 = String.valueOf(treeId);
        for (final String folderId : folderIds) {
            folderService.unsubscribeFolder(treeId2, folderId, session);
        }
        reorderRemainingSortNums(treeId, session);
    }

    private void reorderRemainingSortNums(final int tree, final ServerSession session) throws OXException {
        final DatabaseService databaseService = getService(DatabaseService.class);
        final int contextId = session.getContextId();
        final int userId = session.getUserId();
        final Connection con = databaseService.getWritable(contextId);
        try {
            final TIntObjectMap<Set<String>> map;
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
                    set.add(rs.getString(1));
                }
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }
            stmt = null;
            rs = null;
            int maxSortNum = 0;
            for (final int sortNum : map.keys()) {
                if (sortNum > maxSortNum) {
                    maxSortNum = sortNum;
                }
            }
            final TIntObjectMap<Set<String>> newOrder = new TIntObjectHashMap<Set<String>>(map.size());
            int curSortNum = 0;
            for (int i = 0; i <= maxSortNum; i++) {
                final Set<String> set = map.get(i);
                if (null != set) {
                    // Hit
                    Set<String> set2 = newOrder.get(curSortNum);
                    if (null == set2) {
                        set2 = set;
                        newOrder.put(curSortNum, set);
                    } else {
                        set2.addAll(set);
                    }
                    curSortNum++;
                }
            }
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
        } finally {
            Databases.autocommit(con);
            databaseService.backWritable(contextId, con);
        }
    }

    private final class TIntObjectProcedureImpl extends TIntObjectErrorAwareAbstractProcedure<Set<String>, SQLException> {

        private final PreparedStatement ps;

        public TIntObjectProcedureImpl(final PreparedStatement ps) {
            super();
            this.ps = ps;
        }

        @Override
        protected boolean next(final int sortNum, final Set<String> set) throws SQLException {
            ps.setInt(1, sortNum);
            for (final String folderId : set) {
                ps.setString(5, folderId);
                ps.addBatch();
            }
            return true;
        }

    }

}
