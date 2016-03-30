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

package com.openexchange.user.copy.internal.folder;

import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.connection.ConnectionFetcherTask;
import com.openexchange.user.copy.internal.context.ContextLoadTask;
import com.openexchange.user.copy.internal.folder.util.FolderEqualsWrapper;
import com.openexchange.user.copy.internal.folder.util.FolderPermission;
import com.openexchange.user.copy.internal.folder.util.Tree;
import com.openexchange.user.copy.internal.user.UserCopyTask;

/**
 * {@link FolderCopyTask} - Copies all private folders, the users private infostore folder and, if necessary, the mail attachment folder.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class FolderCopyTask implements CopyUserTaskService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FolderCopyTask.class);

    private static final String SELECT_FOLDERS =
        "SELECT "+
            "fuid, parent, fname, module, type, creating_date, " +
            "changing_date, changed_from, permission_flag, " +
            "subfolder_flag, default_flag " +
        "FROM " +
            "oxfolder_tree " +
        "WHERE " +
            "cid = ? AND created_from = ? AND (module = 8 OR type = 1)";

    private static final String INSERT_FOLDERS =
        "INSERT INTO " +
            "oxfolder_tree " +
            "(fuid, cid, parent, fname, module, type, creating_date, " +
            "created_from, changing_date, changed_from, permission_flag, " +
            "subfolder_flag, default_flag) " +
        "VALUES " +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_VIRTUAL_FOLDERS =
        "SELECT " +
            "tree, folderId, parentId, " +
            "name, lastModified, modifiedBy, shadow, sortNum " +
        "FROM " +
            "virtualTree " +
        "WHERE " +
            "cid = ? " +
        "AND " +
            "user = ?";

    private static final String INSERT_VIRTUAL_FOLDERS =
        "INSERT INTO " +
            "virtualTree " +
            "(cid, tree, user, folderId, parentId, name, lastModified, modifiedBy, shadow, sortNum) " +
        "VALUES " +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_PERMISSIONS =
        "INSERT INTO " +
            "oxfolder_permissions " +
            "(cid, fuid, permission_id, fp, orp, owp, odp, admin_flag, group_flag, system) " +
        "VALUES " +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getAlreadyCopied()
     */
    @Override
    public String[] getAlreadyCopied() {
        return new String[] {
            UserCopyTask.class.getName(),
            ContextLoadTask.class.getName(),
            ConnectionFetcherTask.class.getName()
        };
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getObjectName()
     */
    @Override
    public String getObjectName() {
        return FolderObject.class.getName();
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#copyUser(java.util.Map)
     */
    @Override
    public ObjectMapping<FolderObject> copyUser(final Map<String, ObjectMapping<?>> copied) throws OXException {
        final CopyTools tools = new CopyTools(copied);
        final Integer srcCtxId = tools.getSourceContextId();
        final Integer dstCtxId = tools.getDestinationContextId();
        final Integer srcUsrId = tools.getSourceUserId();
        final Integer dstUsrId = tools.getDestinationUserId();
        final Connection srcCon = tools.getSourceConnection();
        final Connection dstCon = tools.getDestinationConnection();

        /*
         * Load all private folders from oxfolder_tree and modify object and parent ids.
         */
        final SortedMap<Integer, FolderEqualsWrapper> loadedFolders = loadFoldersFromDB(srcCon, i(srcCtxId), i(srcUsrId));
        final Tree<FolderEqualsWrapper> folderTree = buildFolderTree(loadedFolders);
        final SortedMap<Integer, FolderEqualsWrapper> originFolders = new TreeMap<Integer, FolderEqualsWrapper>();
        for (final FolderEqualsWrapper folder : folderTree.getAllNodesAsSet()) {
            try {
                final int id = folder.getObjectID();
                if (!ignoreFolder(id)) {
                    originFolders.put(id, folder.clone());
                }
            } catch (final CloneNotSupportedException e) {
                throw UserCopyExceptionCodes.UNKNOWN_PROBLEM.create(e);
            }
        }

        final Map<Integer, Integer> idMapping = new HashMap<Integer, Integer>();
        exchangeIds(folderTree, folderTree.getRoot(), i(dstCtxId), i(dstUsrId), dstCon, -1, idMapping);

        /*
         * Write folders and permissions.
         */
        writeFoldersToDB(dstCon, folderTree, i(dstCtxId));
        writePermissionsToDB(idMapping.values(), dstCon, i(dstCtxId), i(dstUsrId));

        /*
         * Load and write virtual folders.
         */
        final List<VirtualFolder> virtualFolders = loadVirtualFoldersFromDB(srcCon, i(srcCtxId), i(srcUsrId));
        writeVirtualFoldersToDB(virtualFolders, idMapping, dstCon, i(dstCtxId), i(dstUsrId));

        /*
         * Create mapping between origin and target folders.
         */
        final SortedMap<Integer, FolderEqualsWrapper> movedFolders = loadFoldersFromDB(dstCon, i(dstCtxId), i(dstUsrId));
        final FolderMapping folderMapping = new FolderMapping();

        for (final Integer fuid : originFolders.keySet()) {
            final FolderEqualsWrapper originWrapper = originFolders.get(fuid);
            final Integer targetId = idMapping.get(fuid);
            if (targetId == null) {
                throw UserCopyExceptionCodes.UNKNOWN_PROBLEM.create();
            }

            final FolderEqualsWrapper targetWrapper = movedFolders.get(targetId);
            if (targetId == null || targetWrapper == null) {
                throw UserCopyExceptionCodes.UNKNOWN_PROBLEM.create();
            }
            folderMapping.addMapping(fuid, originWrapper.getFolder(), targetId, targetWrapper.getFolder());
        }

        return folderMapping;
    }

    private boolean ignoreFolder(final int id) {
        return id == 0 || id == 1 || id == 9 || id == 10 || id == 15;
    }

    SortedMap<Integer, FolderEqualsWrapper> loadFoldersFromDB(final Connection con, final int cid, final int uid) throws OXException {
        final SortedMap<Integer, FolderEqualsWrapper> folderMap = new TreeMap<Integer, FolderEqualsWrapper>();

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SELECT_FOLDERS);
            stmt.setInt(1, cid);
            stmt.setInt(2, uid);
            rs = stmt.executeQuery();

            while (rs.next()) {
                final FolderObject folder = buildFolderFromResultSet(rs);
                folderMap.put(folder.getObjectID(), new FolderEqualsWrapper(folder, "orig"));
            }
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }

        return folderMap;
    }

    private FolderObject buildFolderFromResultSet(final ResultSet rs) throws SQLException {
        int i = 1;
        final FolderObject folder = new FolderObject(rs.getInt(i++));
        folder.setParentFolderID(rs.getInt(i++));
        folder.setFolderName(rs.getString(i++));
        folder.setModule(rs.getInt(i++));
        folder.setType(rs.getInt(i++));
        folder.setCreationDate(new Date(rs.getLong(i++)));
        folder.setLastModified(new Date(rs.getLong(i++)));
        folder.setModifiedBy(rs.getInt(i++));
        folder.setPermissionFlag(rs.getInt(i++));
        folder.setSubfolderFlag(rs.getBoolean(i++));
        folder.setDefaultFolder(rs.getBoolean(i++));

        return folder;
    }

    private Tree<FolderEqualsWrapper> buildFolderTree(final SortedMap<Integer, FolderEqualsWrapper> folderMap) throws OXException {
        final FolderEqualsWrapper rootFolder = new FolderEqualsWrapper(new FolderObject(FolderObject.SYSTEM_ROOT_FOLDER_ID), "orig");
        rootFolder.setParentFolderID(-1);
        final FolderEqualsWrapper privateFolder = new FolderEqualsWrapper(new FolderObject(FolderObject.SYSTEM_PRIVATE_FOLDER_ID), "orig");
        privateFolder.setParentFolderID(FolderObject.SYSTEM_ROOT_FOLDER_ID);
        final FolderEqualsWrapper systemInfostoreFolder = new FolderEqualsWrapper(new FolderObject(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID), "orig");
        systemInfostoreFolder.setParentFolderID(FolderObject.SYSTEM_ROOT_FOLDER_ID);
        final FolderEqualsWrapper userInfostoreFolder = new FolderEqualsWrapper(new FolderObject(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID), "orig");
        userInfostoreFolder.setParentFolderID(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);
        final FolderEqualsWrapper publicInfostoreFolder = new FolderEqualsWrapper(new FolderObject(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID), "orig");
        publicInfostoreFolder.setParentFolderID(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);

        final SortedMap<Integer, FolderEqualsWrapper> extendedMap = new TreeMap<Integer, FolderEqualsWrapper>();
        extendedMap.putAll(folderMap);
        extendedMap.put(privateFolder.getObjectID(), privateFolder);
        extendedMap.put(systemInfostoreFolder.getObjectID(), systemInfostoreFolder);
        extendedMap.put(userInfostoreFolder.getObjectID(), userInfostoreFolder);
        extendedMap.put(publicInfostoreFolder.getObjectID(), publicInfostoreFolder);

        /*
         * A recursion is used here to be sure that the folder tree always contains a folders parent before the folder is added.
         * If the tree does not contain the parent already, the parent will be added first.
         */
        final Tree<FolderEqualsWrapper> folderTree = new Tree<FolderEqualsWrapper>(rootFolder);
        for (final Integer folderId : extendedMap.keySet()) {
            final FolderEqualsWrapper folder = extendedMap.get(folderId);
            addFoldersRecursive(extendedMap, folderTree, folder);
        }

        folderTree.removeChild(publicInfostoreFolder);
        return folderTree;
    }

    private void addFoldersRecursive(final SortedMap<Integer, FolderEqualsWrapper> folderMap, final Tree<FolderEqualsWrapper> folderTree, final FolderEqualsWrapper folder) throws OXException {
        final int folderId = folder.getObjectID();
        final int parentFolderId = folder.getParentFolderID();
        final FolderEqualsWrapper parent = folderMap.get(parentFolderId);
        if (parentFolderId != 0 && parent == null) {
            LOG.warn(String.format("A private folder (%1$s) without existing parent (%2$s) was found. The folder will be ignored!", folderId, folder.getParentFolderID()));
            return;
        }

        if (parentFolderId == 0) {
            /*
             * Folder is a subfolder of root and can be added immediately.
             */
            folderTree.addChild(folder, folderTree.getRoot());
        } else {
            /*
             * If the folders parent is already part of the tree the folder will be added.
             * If not the parent will be added first. We also have to check if the folder already exists in the tree.
             */
            if (folderTree.containsChild(parent)) {
                if (!folderTree.containsChild(folder)) {
                    folderTree.addChild(folder, parent);
                }
            } else {
                addFoldersRecursive(folderMap, folderTree, parent);
                folderTree.addChild(folder, parent);
            }
        }
    }

    private void writeFoldersToDB(final Connection con, final Tree<FolderEqualsWrapper> folderTree, final int cid) throws OXException {
        final Set<FolderEqualsWrapper> allFolders = folderTree.getAllNodesAsSet();
        final List<FolderObject> foldersToWrite = new ArrayList<FolderObject>();
        for (final FolderEqualsWrapper folderWrapper : allFolders) {
            final FolderObject folder = folderWrapper.getFolder();
            if (!ignoreFolder(folder.getObjectID())) {
                foldersToWrite.add(folder);
            }
        }

        writeFoldersToDB(con, foldersToWrite, cid);
    }

    private void writeFoldersToDB(final Connection con, final List<FolderObject> folders, final int cid) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_FOLDERS);
            for (final FolderObject folder : folders) {
                int i = 1;
                stmt.setInt(i++, folder.getObjectID());
                stmt.setInt(i++, cid);
                stmt.setInt(i++, folder.getParentFolderID());
                stmt.setString(i++, folder.getFolderName());
                stmt.setInt(i++, folder.getModule());
                stmt.setInt(i++, folder.getType());
                stmt.setLong(i++, folder.getCreationDate().getTime());
                stmt.setInt(i++, folder.getCreator());
                stmt.setLong(i++, folder.getLastModified().getTime());
                stmt.setInt(i++, folder.getModifiedBy());
                stmt.setInt(i++, folder.getPermissionFlag());
                stmt.setInt(i++, folder.hasSubfolders() ? 1 : 0);
                stmt.setInt(i++, folder.isDefaultFolder() ? 1 : 0);

                stmt.addBatch();
            }

            stmt.executeBatch();
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private void exchangeIds(final Tree<FolderEqualsWrapper> folderTree, final FolderEqualsWrapper root, final int cid, final int uid, final Connection con, final int newParent, final Map<Integer, Integer> idMapping) throws OXException {
        try {
            final int origId = root.getObjectID();
            int newId = origId;
            if (!ignoreFolder(origId)) {
                newId = IDGenerator.getId(cid, com.openexchange.groupware.Types.FOLDER, con);
                idMapping.put(origId, newId);
            }

            final FolderEqualsWrapper rootClone = root.clone();
            rootClone.setObjectID(newId);
            rootClone.setCreator(uid);
            rootClone.setModifiedBy(uid);
            rootClone.setParentFolderID(newParent);
            rootClone.setKey("clone");
            if (folderTree.exchangeNodes(root, rootClone) && !folderTree.isLeaf(rootClone)) {
                final Set<FolderEqualsWrapper> children = folderTree.getChildren(rootClone);
                for (final FolderEqualsWrapper folder : children) {
                    exchangeIds(folderTree, folder, cid, uid, con, newId, idMapping);
                }
            }
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } catch (final CloneNotSupportedException e) {
            throw UserCopyExceptionCodes.UNKNOWN_PROBLEM.create(e);
        }
    }

    private void writePermissionsToDB(final Collection<Integer> folderIds, final Connection con, final int cid, final int uid) throws OXException {
        final List<FolderPermission> permissions = new ArrayList<FolderPermission>();
        for (final int folderId : folderIds) {
            final FolderPermission permission = new FolderPermission();
            permission.setUserId(uid);
            permission.setFolderId(folderId);
            permission.setFp(128);
            permission.setOrp(128);
            permission.setOwp(128);
            permission.setOdp(128);
            permission.setAdminFlag(true);
            permission.setGroupFlag(false);
            permission.setSystem(false);

            permissions.add(permission);
        }

        writePermissionsToDB(permissions, con, cid);
    }

    private void writePermissionsToDB(final List<FolderPermission> permissions, final Connection con, final int cid) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_PERMISSIONS);
            for (final FolderPermission permission : permissions) {
                int i = 1;
                stmt.setInt(i++, cid);
                stmt.setInt(i++, permission.getFolderId());
                stmt.setInt(i++, permission.getUserId());
                stmt.setInt(i++, permission.getFp());
                stmt.setInt(i++, permission.getOrp());
                stmt.setInt(i++, permission.getOwp());
                stmt.setInt(i++, permission.getOdp());
                stmt.setInt(i++, permission.hasAdminFlag() ? 1 : 0);
                stmt.setInt(i++, permission.hasGroupFlag() ? 1 : 0);
                stmt.setInt(i++, permission.hasSystem() ? 1 : 0);
                stmt.addBatch();
            }

            stmt.executeBatch();
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    List<VirtualFolder> loadVirtualFoldersFromDB(final Connection con, final int cid, final int uid) throws OXException {
        final List<VirtualFolder> folderList = new ArrayList<VirtualFolder>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SELECT_VIRTUAL_FOLDERS);
            stmt.setInt(1, cid);
            stmt.setInt(2, uid);
            rs = stmt.executeQuery();

            while (rs.next()) {
                final VirtualFolder folder = new VirtualFolder();
                int i = 1;
                folder.setTree(rs.getInt(i++));
                folder.setFolderId(rs.getString(i++));
                folder.setParentId(rs.getString(i++));
                folder.setName(rs.getString(i++));
                folder.setLastModified(rs.getLong(i++));
                folder.setModifiedBy(rs.getInt(i++));
                folder.setShadow(rs.getString(i++));
                folder.setSortNum(i++);
                if (rs.wasNull()) {
                    folder.setSortNum(-1);
                }

                folderList.add(folder);
            }
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }

        return folderList;
    }

    private void writeVirtualFoldersToDB(final List<VirtualFolder> folderList, final Map<Integer, Integer> idMapping, final Connection con, final int cid, final int uid) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_VIRTUAL_FOLDERS);
            for (final VirtualFolder folder : folderList) {
                /*
                 * Correct folderId and parentId if necessary.
                 */
                final String folderIdStr = folder.getFolderId();
                final String parentIdStr = folder.getParentId();
                Integer folderId = null;
                Integer parentId = null;
                try {
                    Integer tmp = Integer.parseInt(folderIdStr);
                    final Integer newFolderId = idMapping.get(tmp);
                    if (newFolderId != null) {
                        folderId = newFolderId;
                    }

                    tmp = Integer.parseInt(parentIdStr);
                    final Integer newParentId = idMapping.get(tmp);
                    if (newParentId != null) {
                        parentId = newParentId;
                    }
                } catch (final NumberFormatException e) {
                    // do nothing
                }

                int i = 1;
                stmt.setInt(i++, cid);
                stmt.setInt(i++, folder.getTree());
                stmt.setInt(i++, uid);
                if (folderId == null) {
                    stmt.setString(i++, folderIdStr);
                } else {
                    stmt.setString(i++, String.valueOf(i(folderId)));
                }
                if (parentId == null) {
                    stmt.setString(i++, parentIdStr);
                } else {
                    stmt.setString(i++, String.valueOf(i(parentId)));
                }
                stmt.setString(i++, folder.getName());
                if (folder.getLastModified() == 0) {
                    stmt.setNull(i++, java.sql.Types.BIGINT);
                } else {
                    stmt.setLong(i++, folder.getLastModified());
                }
                if (folder.getModifiedBy() == 0) {
                    stmt.setNull(i++, java.sql.Types.INTEGER);
                } else {
                    stmt.setInt(i++, folder.getModifiedBy());
                }
                stmt.setString(i++, folder.getShadow());
                if (folder.getSortNum() == -1) {
                    stmt.setNull(i++, java.sql.Types.INTEGER);
                } else {
                    stmt.setInt(i++, folder.getSortNum());
                }

                stmt.addBatch();
            }

            stmt.executeBatch();
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#done(java.util.Map, boolean)
     */
    @Override
    public void done(final Map<String, ObjectMapping<?>> copied, final boolean failed) {
    }

}
