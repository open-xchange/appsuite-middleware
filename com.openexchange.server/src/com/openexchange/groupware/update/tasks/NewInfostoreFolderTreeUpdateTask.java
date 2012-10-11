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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link NewInfostoreFolderTreeUpdateTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class NewInfostoreFolderTreeUpdateTask implements UpdateTask {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(NewInfostoreFolderTreeUpdateTask.class));

    private OCLPermission systemPermission;

    /**
     * Default constructor
     */
    public NewInfostoreFolderTreeUpdateTask() {
        super();
    }

    /**
     * Lazy initialization of system permission
     *
     * @return The system permission
     */
    private OCLPermission getSystemPermission() {
        if (systemPermission == null) {
            systemPermission = new OCLPermission();
            systemPermission.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
            systemPermission.setGroupPermission(true);
        }
        return systemPermission;
    }

    @Override
    public int addedWithVersion() {
        return 32;
    }

    @Override
    public int getPriority() {
        /*
         * Modification on database: highest priority.
         */
        return UpdateTask.UpdateTaskPriority.HIGHEST.priority;
    }

    private static final String SQL_01 = "SELECT cid FROM oxfolder_tree WHERE fuid = "
            + FolderObject.SYSTEM_INFOSTORE_FOLDER_ID + " GROUP BY cid";

    @Override
    public void perform(final Schema schema, final int cid) throws OXException {
        final SortedSet<Integer> contextIds = new TreeSet<Integer>();
        /*
         * Gather all available context IDs
         */
        gatherContextIDs(cid, contextIds);
        /*
         * Iterate over context IDs
         */
        final long creatingTime = System.currentTimeMillis();
        for (final Integer contextId : contextIds) {
            processContext(contextId.intValue(), creatingTime);
        }
    }

    private void gatherContextIDs(final int cid, final SortedSet<Integer> contextIds) throws OXException {
        final Connection writeCon;
        try {
            writeCon = Database.getNoTimeout(cid, true);
        } catch (final OXException e) {
            throw new OXException(e);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = writeCon.prepareStatement(SQL_01);
            rs = stmt.executeQuery();
            while (rs.next()) {
                contextIds.add(Integer.valueOf(rs.getInt(1)));
            }
        } catch (final SQLException e) {
            throw err(e);
        } finally {
            closeSQLStuff(rs, stmt);
            Database.backNoTimeout(cid, true, writeCon);
        }
    }

    private void processContext(final int cid, final long creatingTime) throws OXException {
        LOG.info("Performing 'NewInfostoreFolderTreeUpdateTask' on context " + cid);
        final Connection writeCon;
        try {
            writeCon = Database.getNoTimeout(cid, true);
        } catch (final OXException e) {
            throw new OXException(e);
        }
        try {
            final int admin = getContextAdmin(cid, writeCon);
            writeCon.setAutoCommit(false); // BEGIN
            if (!checkExists(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, cid, writeCon)) {
                /*
                 * Create user store folder in this context
                 */
                createUserStoreFolder(cid, writeCon, creatingTime, admin);
            }
            /*
             * Move affected folder below user store folder
             */
            move2UserStore(cid, writeCon);
            if (!checkExists(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID, cid, writeCon)) {
                /*
                 * Create public infostore folder in this context
                 */
                createPublicInfostoreFolder(cid, writeCon, creatingTime, admin);
            }
            /*
             * Move affected folder below public infostore folder
             */
            move2PublicInfoStore(cid, writeCon);
            /*
             * Change permissions on infostore folder
             */
            updateInfostorePermissions(cid, writeCon, creatingTime, admin);
            writeCon.commit(); // COMMIT
        } catch (final SQLException e) {
            rollback(writeCon);
            LOG.info("Roll-back done in update task 'NewInfostoreFolderTreeUpdateTask' for context " + cid);
            throw err(e);
        } catch (final OXException e) {
            rollback(writeCon);
            LOG.info("Roll-back done in update task 'NewInfostoreFolderTreeUpdateTask' for context " + cid);
            throw e;
        } finally {
            autocommit(writeCon);
            Database.backNoTimeout(cid, true, writeCon);
        }
    }

    private static final String SQL_UPDATE = "UPDATE oxfolder_permissions SET fp = ?, orp = ?, owp = ?, odp = ?, admin_flag = ? WHERE cid = ? AND fuid = "
            + FolderObject.SYSTEM_INFOSTORE_FOLDER_ID + " AND permission_id = " + OCLPermission.ALL_GROUPS_AND_USERS;

    private static final String SQL_UPDATE2 = "UPDATE oxfolder_tree SET changing_date = ?, changed_from = ? WHERE cid = ? AND fuid = "
            + FolderObject.SYSTEM_INFOSTORE_FOLDER_ID;

    private void updateInfostorePermissions(final int cid, final Connection writeCon, final long lastModified,
            final int admin) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = writeCon.prepareStatement(SQL_UPDATE);
            int pos = 1;
            stmt.setInt(pos++, OCLPermission.READ_FOLDER); // fp
            stmt.setInt(pos++, OCLPermission.NO_PERMISSIONS); // orp
            stmt.setInt(pos++, OCLPermission.NO_PERMISSIONS); // owp
            stmt.setInt(pos++, OCLPermission.NO_PERMISSIONS); // odp
            stmt.setInt(pos++, 0); // admin_flag
            stmt.setInt(pos++, cid);
            stmt.executeUpdate();
            stmt.close();
            stmt = writeCon.prepareStatement(SQL_UPDATE2);
            pos = 1;
            stmt.setLong(pos++, lastModified); // changing_date
            stmt.setInt(pos++, admin); // changed_from
            stmt.setInt(pos++, cid);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw err(e);
        } finally {
            closeSQLStuff(null, stmt);
        }

    }

    private void createUserStoreFolder(final int cid, final Connection writeCon, final long creatingTime,
            final int admin) throws SQLException {
        /*
         * Create user store folder with appropriate permissions
         */
        final OCLPermission systemPermission = getSystemPermission();
        systemPermission.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.NO_PERMISSIONS,
                OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        systemPermission.setFolderAdmin(false);
        createSystemFolder(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID,
                FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_NAME, systemPermission,
                FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, FolderObject.INFOSTORE, true, creatingTime, admin, cid,
                writeCon);
    }

    private void createPublicInfostoreFolder(final int cid, final Connection writeCon, final long creatingTime,
            final int admin) throws SQLException {
        /*
         * Create public infostore folder with appropriate permissions
         */
        final OCLPermission systemPermission = getSystemPermission();
        systemPermission.setAllPermission(OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.NO_PERMISSIONS,
                OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        systemPermission.setFolderAdmin(false);
        createSystemFolder(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID,
                FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_NAME, systemPermission,
                FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, FolderObject.INFOSTORE, true, creatingTime, admin, cid,
                writeCon);
    }

    private static final String SQL_02 = "SELECT fuid FROM oxfolder_tree WHERE cid = ? AND fuid = ?";

    private boolean checkExists(final int folderId, final int cid, final Connection con) throws OXException {
        final PreparedStatement stmt;
        try {
            stmt = con.prepareStatement(SQL_02);
        } catch (final SQLException e) {
            throw err(e);
        }
        try {
            stmt.setInt(1, cid);
            stmt.setInt(2, folderId);
            return stmt.executeQuery().next();
        } catch (final SQLException e) {
            throw err(e);
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    private static final String SQL_SELECT_ADMIN = "SELECT user FROM user_setting_admin WHERE cid = ?";

    private int getContextAdmin(final int cid, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_ADMIN);
            stmt.setInt(1, cid);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return -1;
            }
            return rs.getInt(1);
        } catch (final SQLException e) {
            throw err(e);
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static final String SQL_MOVE1 = "UPDATE oxfolder_tree SET parent = "
            + FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID + " WHERE cid = ? AND parent = "
            + FolderObject.SYSTEM_INFOSTORE_FOLDER_ID + " AND module = " + FolderObject.INFOSTORE
            + " AND default_flag = 1 AND fuid NOT IN (" + FolderObject.SYSTEM_INFOSTORE_FOLDER_ID + ", "
            + FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID + ", " + FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID
            + ")";

    private void move2UserStore(final int cid, final Connection writeCon) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = writeCon.prepareStatement(SQL_MOVE1);
            stmt.setInt(1, cid);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw err(e);
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    private static final String SQL_MOVE2 = "UPDATE oxfolder_tree SET parent = "
            + FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID + " WHERE cid = ? AND parent = "
            + FolderObject.SYSTEM_INFOSTORE_FOLDER_ID + " AND module = " + FolderObject.INFOSTORE
            + " AND default_flag = 0 AND fuid NOT IN (" + FolderObject.SYSTEM_INFOSTORE_FOLDER_ID + ", "
            + FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID + ", " + FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID
            + ")";

    private void move2PublicInfoStore(final int cid, final Connection writeCon) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = writeCon.prepareStatement(SQL_MOVE2);
            stmt.setInt(1, cid);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw err(e);
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    private final static String SQL_INSERT_SYSTEM_FOLDER = "INSERT INTO oxfolder_tree "
            + "(fuid, cid, parent, fname, module, type, creating_date, created_from, changing_date, changed_from, permission_flag, subfolder_flag) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String SQL_INSERT_SYSTEM_PERMISSION = "INSERT INTO oxfolder_permissions "
            + "(cid, fuid, permission_id, fp, orp, owp, odp, admin_flag, group_flag) VALUES (?,?,?,?,?,?,?,?,?)";

    private static final String SQL_INSERT_SPECIAL_FOLDER = "INSERT INTO oxfolder_specialfolders "
            + "(tag, cid, fuid) VALUES (?,?,?)";

    private void createSystemFolder(final int systemFolderId, final String systemFolderName,
            final OCLPermission systemPermission, final int parentId, final int module,
            final boolean insertIntoSpecialFolders, final long creatingTime, final int mailAdminId, final int cid,
            final Connection writeCon) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = writeCon.prepareStatement(SQL_INSERT_SYSTEM_FOLDER);
            stmt.setInt(1, systemFolderId);
            stmt.setInt(2, cid);
            stmt.setInt(3, parentId);
            stmt.setString(4, systemFolderName);
            stmt.setInt(5, module);
            stmt.setInt(6, FolderObject.SYSTEM_TYPE);
            stmt.setLong(7, creatingTime);
            stmt.setInt(8, mailAdminId); // created_from
            stmt.setLong(9, creatingTime); // changing_date
            stmt.setInt(10, mailAdminId); // changed_from
            stmt.setInt(11, FolderObject.PUBLIC_PERMISSION); // permission_flag
            stmt.setInt(12, 1); // subfolder_flag
            stmt.executeUpdate();
            stmt.close();
            stmt = writeCon.prepareStatement(SQL_INSERT_SYSTEM_PERMISSION);
            stmt.setInt(1, cid);
            stmt.setInt(2, systemFolderId); // fuid
            stmt.setInt(3, systemPermission.getEntity()); // entity
            stmt.setInt(4, systemPermission.getFolderPermission()); // folder
            // permission
            stmt.setInt(5, systemPermission.getReadPermission()); // read
            // permission
            stmt.setInt(6, systemPermission.getWritePermission()); // write
            // permission
            stmt.setInt(7, systemPermission.getDeletePermission()); // delete
            // permission
            stmt.setInt(8, systemPermission.isFolderAdmin() ? 1 : 0); // admin_flag
            stmt.setInt(9, systemPermission.isGroupPermission() ? 1 : 0); // group_flag
            stmt.executeUpdate();
            stmt.close();
            stmt = null;
            if (insertIntoSpecialFolders) {
                stmt = writeCon.prepareStatement(SQL_INSERT_SPECIAL_FOLDER);
                stmt.setString(1, systemFolderName); // tag
                stmt.setInt(2, cid); // cid
                stmt.setInt(3, systemFolderId); // fuid
                stmt.executeUpdate();
                stmt.close();
                stmt = null;
            }
        } finally {
            if (stmt != null) {
                stmt.close();
                stmt = null;
            }
        }
    }

    private static OXException err(final SQLException e) {
        return UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
    }
}
