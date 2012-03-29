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
 * {@link SubscribeFavFolderPerformer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SubscribeFavFolderPerformer extends AbstractFavFolderPerformer {

    /**
     * Initializes a new {@link SubscribeFavFolderPerformer}.
     */
    public SubscribeFavFolderPerformer(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    /**
     * Subscribes specified folders
     *
     * @param sourceTreeId The source tree identifier to subscribe from; default is {@link FolderStorage#REAL_TREE_ID}
     * @param folderIds The identifiers of the folders to subscribe
     * @param targetTreeId The identifier of the tree to subscribe to
     * @param targetParentId The identifier of the parent in target tree; default is {@link FolderStorage#ROOT_ID}
     * @param session The session providing user data
     * @throws OXException If subscribe fails for any reason
     */
    public void subscribeFolders(final int sourceTreeId, final Collection<String> folderIds, final int targetTreeId, final String targetParentId, final ServerSession session) throws OXException {
        final FolderService folderService = getService(FolderService.class);
        final String sourceTreeId2 = sourceTreeId > 0 ? String.valueOf(sourceTreeId) : FolderStorage.REAL_TREE_ID;
        final String targetTreeId2 = String.valueOf(targetTreeId);
        final String parent = null == targetParentId ? FolderStorage.ROOT_ID : targetParentId;
        for (final String folderId : folderIds) {
            folderService.subscribeFolder(sourceTreeId2, folderId, targetTreeId2, parent, session);
        }
        appendSortNums(targetTreeId, folderIds, session);
    }

    private void appendSortNums(final int tree, final Collection<String> folderIds, final ServerSession session) throws OXException {
        final DatabaseService databaseService = getService(DatabaseService.class);
        final int contextId = session.getContextId();
        final int userId = session.getUserId();
        final Connection con = databaseService.getWritable(contextId);
        final Set<String> toConsider = new HashSet<String>();
        try {
            int maxSortNum = 0;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement("SELECT folder, sortNum FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND parentId = ?");
                int pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, userId);
                stmt.setString(pos, FolderStorage.ROOT_ID);
                rs = stmt.executeQuery();
                int sn;
                while (rs.next()) {
                    toConsider.add(rs.getString(1));
                    sn = rs.getInt(2);
                    if (!rs.wasNull()) {
                        maxSortNum = maxSortNum < sn ? sn : maxSortNum;
                    }
                }
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }
            stmt = null;
            rs = null;
            if (maxSortNum < 0) {
                maxSortNum = 0;
            }
            try {
                stmt = con.prepareStatement("UPDATE virtualTree SET sortNum = ? WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?");
                stmt.setInt(2, contextId);
                stmt.setInt(3, tree);
                stmt.setInt(4, userId);
                for (final String folderId : folderIds) {
                    if (toConsider.contains(folderId)) {
                        stmt.setInt(1, ++maxSortNum);
                        stmt.setString(5, folderId);
                        stmt.addBatch();
                    }
                }
                stmt.executeBatch();
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }
        } finally {
            databaseService.backWritable(contextId, con);
        }
    }

}
