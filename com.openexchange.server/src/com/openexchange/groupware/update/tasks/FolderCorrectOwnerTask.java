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

package com.openexchange.groupware.update.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import com.openexchange.database.Databases;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.infostore.database.impl.versioncontrol.VersionControlResult;
import com.openexchange.groupware.infostore.database.impl.versioncontrol.VersionControlUtil;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.Reference;
import com.openexchange.tools.sql.DBUtils;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;

/**
 * {@link FolderCorrectOwnerTask} - Corrects values in the 'created_from' column for folders nested below/underneath personal 'Trash' folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderCorrectOwnerTask extends UpdateTaskAdapter {

    /**
     * Default constructor.
     */
    public FolderCorrectOwnerTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BLOCKING);
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Logger log = org.slf4j.LoggerFactory.getLogger(FolderCorrectOwnerTask.class);
        log.info("Performing update task {}", FolderCorrectOwnerTask.class.getSimpleName());

        Connection con = Database.getNoTimeout(params.getContextId(), true);
        boolean rollback = false;
        final Map<Integer, List<Map<Integer, List<VersionControlResult>>>> resultMaps = new LinkedHashMap<Integer, List<Map<Integer, List<VersionControlResult>>>>();
        try {
            Databases.startTransaction(con);
            rollback = true;

            List<int[]> users = getUsers(con);
            params.getProgressState().setTotal(users.size());

            int num = 1;
            for (int[] user : users) {
                final int contextId = user[0];
                final int userId = user[1];

                // Get all trashed InfoStore/Drive folders having a different owner than trash-owning user
                TIntIntMap trashFolders = getTrashFoldersToCheck(userId, contextId, con);
                if (null != trashFolders && !trashFolders.isEmpty()) {
                    final Connection conn = con;
                    final Reference<OXException> ref = new Reference<OXException>();
                    trashFolders.forEachEntry(new TIntIntProcedure() {

                        @Override
                        public boolean execute(int folderId, int oldOwner) {
                            try {
                                adjustTrashOwnershipFor(folderId, oldOwner, resultMaps, userId, contextId, conn);
                                return true;
                            } catch (OXException e) {
                                ref.setValue(e);
                            } catch (SQLException e) {
                                ref.setValue(UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage()));
                            } catch (RuntimeException e) {
                                ref.setValue(UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage()));
                            }
                            return false;
                        }
                    });
                }

                if ((num % 10) == 0) {
                    // Update zero-based state information
                    params.getProgressState().setState(num - 1);
                }
                num++;
            }

            con.commit();
            rollback = false;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback) {
                // Roll-back connection
                Databases.rollback(con);

                // Try to restore files
                Databases.autocommit(con);
                for (Map.Entry<Integer, List<Map<Integer, List<VersionControlResult>>>> entry : resultMaps.entrySet()) {
                    int contextId = entry.getKey().intValue();
                    ContextImpl ctx = new ContextImpl(contextId);

                    for (Map<Integer, List<VersionControlResult>> resultMap : entry.getValue()) {

                        for (Map.Entry<Integer, List<VersionControlResult>> documentEntry : resultMap.entrySet()) {
                            Integer documentId = documentEntry.getKey();
                            List<VersionControlResult> versionInfo = documentEntry.getValue();

                            try {
                                VersionControlUtil.restoreVersionControl(Collections.singletonMap(documentId, versionInfo), ctx, con);
                            } catch (Exception e) {
                                log.error("Failed to restore InfoStore/Drive files for document {} in context {}", documentId, contextId, e);
                            }
                        }

                    }
                }
            }

            // Ensure auto-commit mode is restored & push back to pool
            Databases.autocommit(con);
            Database.backNoTimeout(params.getContextId(), true, con);
        }
        log.info("{} successfully performed.", FolderCorrectOwnerTask.class.getSimpleName());
    }

    private List<int[]> getUsers(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, id FROM user ORDER BY cid, id");
            rs = stmt.executeQuery();

            List<int[]> users = new LinkedList<int[]>();
            while (rs.next()) {
                users.add(new int[] {rs.getInt(1), rs.getInt(2)});
            }
            return users;
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    private TIntIntMap getTrashFoldersToCheck(int userId, int contextId, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT fuid FROM oxfolder_tree WHERE cid=? AND type=? AND default_flag=1 AND created_from=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, FolderObject.TRASH);
            stmt.setInt(3, userId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                // User has no InfoStore/Drive Trash folder
                return null;
            }

            int trashId = rs.getInt(1);
            DBUtils.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            TIntIntMap folderId2Owner = new TIntIntHashMap();
            collectTrashFolders(trashId, folderId2Owner, userId, contextId, con);
            return folderId2Owner;
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    private void collectTrashFolders(int parentTrashId, TIntIntMap folderId2Owner, int userId, int contextId, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT fuid, created_from FROM oxfolder_tree WHERE cid=? AND parent=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, parentTrashId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return;
            }

            TIntList children = new TIntArrayList(8);
            do {
                int subfolderId = rs.getInt(1);
                if (!folderId2Owner.containsKey(subfolderId)) {
                    // Not present before, so examine child's sub-folders, too
                    children.add(subfolderId);

                    int createdFrom = rs.getInt(2);
                    if (createdFrom != userId) {
                        // Owner needs to be changed
                        folderId2Owner.put(subfolderId, createdFrom);
                    }
                }
            } while (rs.next());
            DBUtils.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            for (int childId : children.toArray()) {
                collectTrashFolders(childId, folderId2Owner, userId, contextId, con);
            }
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    void adjustTrashOwnershipFor(int folderId, int oldOwner, Map<Integer, List<Map<Integer, List<VersionControlResult>>>> resultMaps, int userId, int contextId, Connection con) throws SQLException, OXException {
        PreparedStatement stmt = null;
        try {
            // Change folder's owner to trash-owning user
            stmt = con.prepareStatement("UPDATE oxfolder_tree SET created_from=? WHERE cid=? AND fuid=?");
            stmt.setInt(1, userId);
            stmt.setInt(2, contextId);
            stmt.setInt(3, folderId);
            stmt.executeUpdate();

            // Check if files are required to be moved to a user-individual file storage
            Map<Integer, List<VersionControlResult>> resultMap = VersionControlUtil.changeFileStoreLocationsIfNecessary(oldOwner, userId, folderId, new ContextImpl(contextId), con);
            if (null != resultMap && !resultMap.isEmpty()) {
                // Files were moved... Remember for possible restore operation
                Integer key = Integer.valueOf(contextId);
                List<Map<Integer, List<VersionControlResult>>> list = resultMaps.get(key);
                if (null == list) {
                    list = new LinkedList<Map<Integer, List<VersionControlResult>>>();
                    resultMaps.put(key, list);
                }
                list.add(resultMap);
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

}
