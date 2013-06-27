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

package com.openexchange.tools.oxfolder;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeResources;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderEventConstants;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.oxfolder.memory.ConditionTreeMapManagement;
import com.openexchange.tools.sql.DBUtils;

/**
 * Contains useful SQL-related helper methods for folder operations
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXFolderSQL {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(OXFolderSQL.class));

    /**
     * Initializes a new OXFolderSQL
     */
    private OXFolderSQL() {
        super();
    }

    /*
     * A possible optimization for this query can be:
     * SELECT ot1.fuid FROM oxfolder_tree AS ot1 LEFT JOIN oxfolder_tree ot2 ON ot1.cid=ot2.cid AND ot1.parent=ot2.fuid WHERE ot1.cid=? AND ot1.parent<>0 AND ot2.fuid IS NULL;
     */
    private static final String SQL_SELECT_WITH_NON_EXISTING_PARENT = "SELECT ot1.fuid FROM oxfolder_tree AS ot1 where ot1.cid = ? AND ot1.parent <> "+FolderObject.SYSTEM_ROOT_FOLDER_ID+" AND NOT EXISTS (SELECT ot2.fuid FROM oxfolder_tree AS ot2 where ot2.cid = ? AND ot1.parent = ot2.fuid)";

    /**
     * Gets the non-existing parents in specified context.
     *
     * @param ctx The context
     * @return The non-existing parents in specified context
     * @throws OXException If operation fails
     */
    public static int[] getNonExistingParents(final Context ctx) throws OXException {
        final Connection con = DBPool.pickup(ctx);
        try {
            return getNonExistingParents(ctx, con);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    /**
     * Gets the non-existing parents in specified context.
     *
     * @param ctx The context
     * @param con The connection to user; <b>must not be <code>null</code></b>
     * @return The non-existing parents in specified context
     * @throws OXException If operation fails
     */
    public static int[] getNonExistingParents(final Context ctx, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_WITH_NON_EXISTING_PARENT);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            rs = executeQuery(stmt);
            final TIntHashSet set = new TIntHashSet(16);
            while (rs.next()) {
                set.add(rs.getInt(1));
            }
            return set.toArray();
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static final String SQL_SELECT_ADMIN = "SELECT user FROM user_setting_admin WHERE cid = ?";

    /**
     * Determines the ID of the user who is defined as admin for given context or <code>-1</code> if none found
     *
     * @param ctx The context
     * @param readConArg A readable connection or <code>null</code> to fetch a new one from connection pool
     * @return The ID of context admin or <code>-1</code> if none found
     * @throws OXException If parameter <code>readConArg</code> is <code>null</code> and no readable connection could be fetched from
     *             or put back into connection pool
     * @throws SQLException If a SQL error occurs
     */
    public static int getContextAdminID(final Context ctx, final Connection readConArg) throws OXException, SQLException {
        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = readCon.prepareStatement(SQL_SELECT_ADMIN);
            stmt.setInt(1, ctx.getContextId());
            rs = executeQuery(stmt);
            if (!rs.next()) {
                return -1;
            }
            return rs.getInt(1);
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
    }

    private static final String SQL_DEFAULTFLD = "SELECT ot.fuid FROM oxfolder_tree AS ot WHERE ot.cid = ? AND ot.created_from = ? AND ot.module = ? AND ot.default_flag = 1";

    /**
     * Gets the specified user's default folder of given module
     *
     * @param userId The user ID
     * @param module The module
     * @param readCon A connection with read capability
     * @param ctx The context
     * @return The folder ID of user's default folder of given module
     * @throws OXException If a pooling error occurs
     * @throws SQLException If a SQL error occurs
     */
    public static int getUserDefaultFolder(final int userId, final int module, final Connection readCon, final Context ctx) throws OXException, SQLException {
        Connection rc = readCon;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (rc == null) {
                rc = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = rc.prepareStatement(SQL_DEFAULTFLD);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, userId);
            stmt.setInt(3, module);
            rs = executeQuery(stmt);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } finally {
            closeResources(rs, stmt, closeReadCon ? rc : null, true, ctx);
        }
    }

    private static final String SQL_SELECT_ALL_SHARED_FLDS = "SELECT ot.fuid FROM oxfolder_tree AS ot WHERE ot.cid = ? AND ot.type = ? AND ot.created_from = ? AND " + "(SELECT COUNT(op.permission_id) FROM oxfolder_permissions AS op WHERE op.cid = ot.cid AND op.fuid = ot.fuid) > 1 GROUP BY ot.fuid";

    /**
     * Gets all private folders of specified owner which are shared to other users.
     *
     * @param owner The owner's ID
     * @param readConArg A readable connection or <code>null</code> to fetch a new one from connection pool
     * @param ctx The context
     * @return All private folders of specified owner which are shared to other users.
     * @throws OXException If parameter <code>readConArg</code> is <code>null</code> and no readable connection could be fetched from
     *             or put back into connection pool
     * @throws SQLException If a SQL error occurs
     */
    public static TIntCollection getSharedFoldersOf(final int owner, final Connection readConArg, final Context ctx) throws OXException, SQLException {
        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = readCon.prepareStatement(SQL_SELECT_ALL_SHARED_FLDS);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, FolderObject.PRIVATE);
            stmt.setInt(3, owner);
            rs = executeQuery(stmt);
            if (!rs.next()) {
                return new TIntArrayList(0);
            }
            final TIntList sia = new TIntArrayList(16);
            do {
                sia.add(rs.getInt(1));
            } while (rs.next());
            return sia;
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
    }

    private static final String SQL_UPDATE_LAST_MOD = "UPDATE oxfolder_tree SET changing_date = ?, changed_from = ? WHERE cid = ? AND fuid = ?";

    /**
     * Updates the last modified timestamp of the folder whose ID matches given parameter <code>folderId</code>.
     *
     * @param folderId The folder ID
     * @param lastModified The new last-modified timestamp to set
     * @param modifiedBy The user who shall be inserted as modified-by
     * @param writeConArg A writable connection or <code>null</code> to fetch a new one from pool
     * @param ctx The context
     * @throws OXException If parameter <code>writeConArg</code> is <code>null</code> and a pooling error occurs
     * @throws SQLException If a SQL error occurs
     */
    public static void updateLastModified(final int folderId, final long lastModified, final int modifiedBy, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection writeCon = writeConArg;
        boolean closeWriteCon = false;
        PreparedStatement stmt = null;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            stmt = writeCon.prepareStatement(SQL_UPDATE_LAST_MOD);
            stmt.setLong(1, lastModified);
            stmt.setInt(2, modifiedBy);
            stmt.setInt(3, ctx.getContextId());
            stmt.setInt(4, folderId);
            executeUpdate(stmt);
        } finally {
            closeResources(null, stmt, closeWriteCon ? writeCon : null, false, ctx);
        }
    }

    private static final String SQL_UPDATE_LAST_MOD2 = "UPDATE oxfolder_tree SET changing_date = ? WHERE cid = ? AND fuid = ?";

    /**
     * Updates the last modified timestamp of the folder whose ID matches given parameter <code>folderId</code>.
     *
     * @param folderId The folder ID
     * @param lastModified The new last-modified timestamp to set
     * @param writeConArg A writable connection or <code>null</code> to fetch a new one from pool
     * @param ctx The context
     * @throws OXException If parameter <code>writeConArg</code> is <code>null</code> and a pooling error occurs
     * @throws SQLException If a SQL error occurs
     */
    private static void updateLastModified(final int folderId, final long lastModified, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection writeCon = writeConArg;
        boolean closeWriteCon = false;
        PreparedStatement stmt = null;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            stmt = writeCon.prepareStatement(SQL_UPDATE_LAST_MOD2);
            stmt.setLong(1, lastModified);
            stmt.setInt(2, ctx.getContextId());
            stmt.setInt(3, folderId);
            executeUpdate(stmt);
        } finally {
            closeResources(null, stmt, closeWriteCon ? writeCon : null, false, ctx);
        }
    }

    private static final String SQL_UPDATE_NAME = "UPDATE oxfolder_tree SET fname = ?, changing_date = ?, changed_from = ? WHERE cid = ? AND fuid = ?";

    /**
     * Updates the name of the folder whose ID matches given parameter <code>folderId</code>.
     *
     * @param folderId The folder ID
     * @param newName The new name to set
     * @param lastModified The last modified timestamp
     * @param modifiedBy The user who shall be inserted as modified-by
     * @param writeConArg A writeable connection or <code>null</code> to fetch a new one from pool
     * @param ctx The context
     * @throws OXException If parameter <code>writeConArg</code> is <code>null</code> and a pooling error occurs
     * @throws SQLException If a SQL error occurs
     */
    static void updateName(final int folderId, final String newName, final long lastModified, final int modifiedBy, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection writeCon = writeConArg;
        boolean closeWriteCon = false;
        PreparedStatement stmt = null;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            stmt = writeCon.prepareStatement(SQL_UPDATE_NAME);
            stmt.setString(1, newName);
            stmt.setLong(2, lastModified);
            stmt.setInt(3, modifiedBy);
            stmt.setInt(4, ctx.getContextId());
            stmt.setInt(5, folderId);
            executeUpdate(stmt);
        } finally {
            closeResources(null, stmt, closeWriteCon ? writeCon : null, false, ctx);
        }
    }

    private static final String SQL_LOOKUPFOLDER = "SELECT fuid,fname FROM oxfolder_tree WHERE cid=? AND parent=? AND fname=? AND module=?";

    /**
     * Returns an {@link TIntList} of folders whose name and module matches the given parameters in the given parent folder.
     * @param folderId
     * @param parent The parent folder whose subfolders shall be looked up
     * @param folderName The folder name to look for
     * @param module The folder module
     * @param readConArg A readable connection (may be <code>null</code>)
     * @param ctx The context
     * @return A list of folders with the same name and module.
     * @throws OXException
     * @throws SQLException
     */
    public static TIntList lookUpFolders(final int parent, final String folderName, final int module, final Connection readConArg, final Context ctx) throws OXException, SQLException {
    	Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final TIntList folderList = new TIntLinkedList();
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = readCon.prepareStatement(SQL_LOOKUPFOLDER);
            stmt.setInt(1, ctx.getContextId()); // cid
            stmt.setInt(2, parent); // parent
            stmt.setString(3, folderName); // fname
            stmt.setInt(4, module); // module
            rs = executeQuery(stmt);
            while (rs.next()) {
                final int fuid = rs.getInt(1);
                final String fname = rs.getString(2);
                if (folderName.equals(fname)) {
                    folderList.add(fuid);
                }
            }
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }

        return folderList;
    }

    /**
     * Checks for a duplicate folder in parental folder. A folder is treated as a duplicate if name and module are equal.
     *
     * @return folder id or <tt>-1</tt> if none found
     */
    public static int lookUpFolder(final int parent, final String folderName, final int module, final Connection readConArg, final Context ctx) throws OXException, SQLException {
        return lookUpFolderOnUpdate(-1, parent, folderName, module, readConArg, ctx);
    }

    /**
     * Checks for a duplicate folder in parental folder. A folder is treated as a duplicate if name and module are equal.
     *
     * @param folderId The ID of the folder whose is equal to given folder name (used on update). Set this parameter to <code>-1</code> to
     *            ignore.
     * @param parent The parent folder whose subfolders shall be looked up
     * @param folderName The folder name to look for
     * @param module The folder module
     * @param readConArg A readable connection (may be <code>null</code>)
     * @param ctx The context
     * @return The folder id or <tt>-1</tt> if none found
     * @throws OXException
     * @throws SQLException
     */
    public static int lookUpFolderOnUpdate(final int folderId, final int parent, final String folderName, final int module, final Connection readConArg, final Context ctx) throws OXException, SQLException {
        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            StringBuilder stmtBuilder = new StringBuilder("SELECT fuid,fname FROM oxfolder_tree WHERE cid=? AND parent=? AND fname=?");
            if (module > 0) {
                final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                if (null != service && service.getBoolProperty("com.openexchange.oxfolder.considerModuleOnDuplicateCheck", false)) {
                    stmtBuilder.append(" AND module=").append(module);
                }
            }
            if (folderId > 0) {
                stmtBuilder.append(" AND fuid!=").append(folderId);
            }
            stmt = readCon.prepareStatement(stmtBuilder.toString());
            stmtBuilder = null;
            stmt.setInt(1, ctx.getContextId()); // cid
            stmt.setInt(2, parent); // parent
            stmt.setString(3, folderName); // fname
            rs = executeQuery(stmt);
            while (rs.next()) {
                final int fuid = rs.getInt(1);
                final String fname = rs.getString(2);
                if (folderName.equals(fname)) {
                    return fuid;
                }
            }
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
        return -1;
    }

    private static final String SQL_EXISTS = "SELECT fuid FROM oxfolder_tree WHERE cid = ? AND fuid = ?";

    /**
     * Checks if underlying storage contains a folder whose ID matches given ID
     *
     * @return <tt>true</tt> if folder exists, otherwise <tt>false</tt>
     */
    public static boolean exists(final int folderId, final Connection readConArg, final Context ctx) throws OXException, SQLException {
        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = readCon.prepareStatement(SQL_EXISTS);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            rs = executeQuery(stmt);
            return rs.next();
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
    }

    /**
     * Checks if underlying storage contains a folder whose ID matches given ID
     *
     * @return <tt>true</tt> if folder exists, otherwise <tt>false</tt>
     */
    public static boolean exists(final int folderId, final Connection readConArg, final Context ctx, final String table) throws OXException, SQLException {
        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = readCon.prepareStatement(new StringBuilder(40).append("SELECT fuid FROM ").append(table).append(
                " WHERE cid = ? AND fuid = ?").toString());
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            rs = executeQuery(stmt);
            return rs.next();
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
    }

    private static final String SQL_UPDATE_PERMS = "UPDATE oxfolder_permissions SET" + " fp = ?, orp = ?, owp = ?, odp = ?" + " WHERE cid = ? AND fuid = ? AND permission_id = ?";

    /**
     * Updates a single folder permission and updates folder's last-modified time stamp
     *
     * @param folderId The folder ID
     * @param permissionId The entity ID; either user or group ID
     * @param folderPermission The folder permission to set
     * @param objectReadPermission The object read permission to set
     * @param objectWritePermission The object write permission to set
     * @param objectDeletePermission The object delete permission to set
     * @param writeCon A connection with write capability; may be <code>null</code> to fetch from pool
     * @param ctx The context
     * @return <code>true</code> if corresponding entry was successfully updated; otherwise <code>false</code>
     * @throws OXException If a pooling error occurred
     * @throws SQLException If a SQL error occurred
     */
    public static boolean updateSinglePermission(final int folderId, final int permissionId, final int folderPermission, final int objectReadPermission, final int objectWritePermission, final int objectDeletePermission, final Connection writeCon, final Context ctx) throws OXException, SQLException {
        Connection wc = writeCon;
        boolean closeWriteCon = false;
        PreparedStatement stmt = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            stmt = wc.prepareStatement(SQL_UPDATE_PERMS);
            int pos = 1;
            stmt.setInt(pos++, folderPermission);
            stmt.setInt(pos++, objectReadPermission);
            stmt.setInt(pos++, objectWritePermission);
            stmt.setInt(pos++, objectDeletePermission);
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, folderId);
            stmt.setInt(pos++, permissionId);
            if (executeUpdate(stmt) != 1) {
                return false;
            }
            closeSQLStuff(null, stmt);
            stmt = null;
            /*
             * Update last-modified to propagate changes to clients
             */
            updateLastModified(folderId, System.currentTimeMillis(), wc, ctx);
            return true;
        } finally {
            closeResources(null, stmt, closeWriteCon ? wc : null, false, ctx);
        }
    }

    private static final String SQL_ADD_PERMS = "INSERT INTO oxfolder_permissions" + " (cid, fuid, permission_id, group_flag, fp, orp, owp, odp, admin_flag, system)" + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /**
     * Inserts a single folder permission.
     *
     * @param folderId The folder ID
     * @param permissionId The entity ID; either user or group ID
     * @param isGroup <code>true</code> if permission ID denotes a group; otherwise <code>false</code>
     * @param folderPermission The folder permission to set
     * @param objectReadPermission The object read permission to set
     * @param objectWritePermission The object write permission to set
     * @param objectDeletePermission The object delete permission to set
     * @param isAdmin <code>true</code> if permission ID is a folder administrator; otherwise <code>false</code>
     * @param system The system bit mask
     * @param writeCon A connection with write capability; may be <code>null</code> to fetch from pool
     * @param ctx The context
     * @return <code>true</code> if corresponding entry was successfully inserted; otherwise <code>false</code>
     * @throws OXException If a pooling error occurred
     * @throws SQLException If a SQL error occurred
     */
    public static boolean addSinglePermission(final int folderId, final int permissionId, final boolean isGroup, final int folderPermission, final int objectReadPermission, final int objectWritePermission, final int objectDeletePermission, final boolean isAdmin, final int system, final Connection writeCon, final Context ctx) throws OXException, SQLException {
        Connection wc = writeCon;
        boolean closeWriteCon = false;
        PreparedStatement stmt = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            stmt = wc.prepareStatement(SQL_ADD_PERMS);
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, folderId);
            stmt.setInt(pos++, permissionId);
            stmt.setInt(pos++, isGroup ? 1 : 0);
            stmt.setInt(pos++, folderPermission);
            stmt.setInt(pos++, objectReadPermission);
            stmt.setInt(pos++, objectWritePermission);
            stmt.setInt(pos++, objectDeletePermission);
            stmt.setInt(pos++, isAdmin ? 1 : 0);
            stmt.setInt(pos++, system);
            return (executeUpdate(stmt) == 1);
        } finally {
            closeResources(null, stmt, closeWriteCon ? wc : null, false, ctx);
        }
    }

    private static final String SQL_REM_SINGLE_SYS_PERM = "DELETE FROM oxfolder_permissions " + "WHERE cid = ? AND fuid = ? AND permission_id = ? AND system = 1";

    /**
     * Deletes a single system permission
     *
     * @param folderId The folder ID
     * @param permissionId The entity ID; either user or group ID
     * @param writeCon A connection with write capability; may be <code>null</code> to fetch from pool
     * @param ctx The context
     * @return <code>true</code> if corresponding entry was successfully deleted; otherwise <code>false</code>
     * @throws OXException If a pooling error occurred
     * @throws SQLException If a SQL error occurred
     */
    public static boolean deleteSingleSystemPermission(final int folderId, final int permissionId, final Connection writeCon, final Context ctx) throws OXException, SQLException {
        Connection wc = writeCon;
        boolean closeWriteCon = false;
        PreparedStatement stmt = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            stmt = wc.prepareStatement(SQL_REM_SINGLE_SYS_PERM);
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, folderId);
            stmt.setInt(pos++, permissionId);
            return (executeUpdate(stmt) == 1);
        } finally {
            closeResources(null, stmt, closeWriteCon ? wc : null, false, ctx);
        }
    }

    private static final String SQL_REM_ALL_SYS_PERM = "DELETE FROM oxfolder_permissions " + "WHERE cid = ? AND fuid = ? AND system = 1";

    /**
     * Deletes all system permission from specified folder
     *
     * @param folderId The folder ID
     * @param writeCon A writable connection
     * @param ctx The context
     * @throws OXException If a pooling error occurred
     * @throws SQLException If a SQL error occurred
     */
    public static void deleteAllSystemPermission(final int folderId, final Connection writeCon, final Context ctx) throws OXException, SQLException {
        Connection wc = writeCon;
        boolean closeWriteCon = false;
        PreparedStatement stmt = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            stmt = wc.prepareStatement(SQL_REM_ALL_SYS_PERM);
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, folderId);
            executeUpdate(stmt);
        } finally {
            closeResources(null, stmt, closeWriteCon ? wc : null, false, ctx);
        }
    }

    private static final String SQL_GETSUBFLDIDS = "SELECT fuid FROM oxfolder_tree WHERE cid = ? AND parent = ?";

    /**
     * Creates a <tt>TIntList</tt> instance containing all subfolder IDs of given folder
     *
     * @return a <tt>TIntList</tt> instance containing all subfolder IDs of given folder
     */
    public static TIntList getSubfolderIDs(final int folderId, final Connection readConArg, final Context ctx) throws OXException, SQLException {
        final TIntList retval = new TIntArrayList();
        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = readCon.prepareStatement(SQL_GETSUBFLDIDS);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            rs = executeQuery(stmt);
            while (rs.next()) {
                retval.add(rs.getInt(1));
            }
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
        return retval;
    }

    private static final String SQL_UDTSUBFLDFLG = "UPDATE oxfolder_tree SET subfolder_flag = ?, changing_date = ? WHERE cid = ? AND fuid = ?";

    /**
     * Updates the field 'subfolder_flag' of matching folder in underlying storage
     */
    static void updateSubfolderFlag(final int folderId, final boolean hasSubfolders, final long lastModified, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection writeCon = writeConArg;
        boolean closeCon = false;
        PreparedStatement stmt = null;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                closeCon = true;
            }
            stmt = writeCon.prepareStatement(SQL_UDTSUBFLDFLG);
            stmt.setInt(1, hasSubfolders ? 1 : 0);
            stmt.setLong(2, lastModified);
            stmt.setInt(3, ctx.getContextId());
            stmt.setInt(4, folderId);
            executeUpdate(stmt);
        } finally {
            closeResources(null, stmt, closeCon ? writeCon : null, false, ctx);
        }
    }

    private static final String SQL_NUMSUB = "SELECT COUNT(ot.fuid) FROM oxfolder_tree AS ot JOIN oxfolder_permissions AS op" + " ON ot.fuid = op.fuid AND ot.cid = ? AND op.cid = ?" + " WHERE op.permission_id IN #IDS# AND op.admin_flag > 0 AND ot.parent = ?";

    /**
     * @return the number of subfolders of given folder which can be moved according to user's permissions
     */
    public static int getNumOfMoveableSubfolders(final int folderId, final int userId, final int[] groups, final Connection readConArg, final Context ctx) throws OXException, SQLException {
        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = readCon.prepareStatement(SQL_NUMSUB.replaceFirst("#IDS#", StringCollection.getSqlInString(userId, groups)));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            stmt.setInt(3, folderId);
            rs = executeQuery(stmt);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
        return 0;
    }

    private static final String SQL_INSERT_NEW_FOLDER = "INSERT INTO oxfolder_tree VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String SQL_INSERT_NEW_PERMISSIONS = "INSERT INTO oxfolder_permissions " + "(cid, fuid, permission_id, fp, orp, owp, odp, admin_flag, group_flag) " + "VALUES (?,?,?,?,?,?,?,?,?)";

    private static final String SQL_UPDATE_PARENT_SUBFOLDER_FLAG = "UPDATE oxfolder_tree " + "SET subfolder_flag = 1, changing_date = ? WHERE cid = ? AND fuid = ?";

    static void insertFolderSQL(final int newFolderID, final int userId, final FolderObject folderObj, final long creatingTime, final Context ctx, final Connection writeConArg) throws SQLException, OXException {
        insertFolderSQL(newFolderID, userId, folderObj, creatingTime, false, ctx, writeConArg);
    }

    static void insertDefaultFolderSQL(final int newFolderID, final int userId, final FolderObject folderObj, final long creatingTime, final Context ctx, final Connection writeConArg) throws SQLException, OXException {
        insertFolderSQL(newFolderID, userId, folderObj, creatingTime, true, ctx, writeConArg);
    }

    private static void insertFolderSQL(final int newFolderID, final int userId, final FolderObject folderObj, final long creatingTime, final boolean acceptDefaultFlag, final Context ctx, final Connection writeConArg) throws SQLException, OXException {
        Connection writeCon = writeConArg;
        /*
         * Insert Folder
         */
        int permissionFlag = FolderObject.CUSTOM_PERMISSION;
        /*
         * Set Permission Flag
         */
        if (folderObj.getType() == FolderObject.PRIVATE) {
            if (folderObj.getPermissions().size() == 1) {
                permissionFlag = FolderObject.PRIVATE_PERMISSION;
            }
        } else if (folderObj.getType() == FolderObject.PUBLIC) {
            final int permissionsSize = folderObj.getPermissions().size();
            final Iterator<OCLPermission> iter = folderObj.getPermissions().iterator();
            for (int i = 0; i < permissionsSize; i++) {
                final OCLPermission oclPerm = iter.next();
                if (oclPerm.getEntity() == OCLPermission.ALL_GROUPS_AND_USERS && oclPerm.getFolderPermission() > OCLPermission.NO_PERMISSIONS) {
                    permissionFlag = FolderObject.PUBLIC_PERMISSION;
                    break;
                }
            }
        }
        boolean closeWriteCon = false;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            final boolean isAuto = writeCon.getAutoCommit();
            if (isAuto) {
                writeCon.setAutoCommit(false);
            }
            try {
                PreparedStatement stmt = null;
                try {
                    stmt = writeCon.prepareStatement(SQL_INSERT_NEW_FOLDER);
                    stmt.setInt(1, newFolderID);
                    stmt.setInt(2, ctx.getContextId());
                    stmt.setInt(3, folderObj.getParentFolderID());
                    stmt.setString(4, folderObj.getFolderName());
                    stmt.setInt(5, folderObj.getModule());
                    stmt.setInt(6, folderObj.getType());
                    stmt.setLong(7, creatingTime);
                    stmt.setInt(8, folderObj.containsCreatedBy() ? folderObj.getCreatedBy() : userId);
                    stmt.setLong(9, creatingTime);
                    stmt.setInt(10, userId);
                    stmt.setInt(11, permissionFlag);
                    stmt.setInt(12, 0); // new folder does not contain
                    // subfolders
                    if (acceptDefaultFlag) {
                        stmt.setInt(13, folderObj.isDefaultFolder() ? 1 : 0); // default_flag
                    } else {
                        stmt.setInt(13, 0); // default_flag
                    }
                    executeUpdate(stmt);
                    stmt.close();
                    stmt = null;
                    /*
                     * Mark parent folder to have subfolders
                     */
                    stmt = writeCon.prepareStatement(SQL_UPDATE_PARENT_SUBFOLDER_FLAG);
                    stmt.setLong(1, creatingTime);
                    stmt.setInt(2, ctx.getContextId());
                    stmt.setInt(3, folderObj.getParentFolderID());
                    executeUpdate(stmt);
                    stmt.close();
                    stmt = null;
                    /*
                     * Insert permissions
                     */
                    stmt = writeCon.prepareStatement(SQL_INSERT_NEW_PERMISSIONS);
                    final OCLPermission[] permissions = folderObj.getNonSystemPermissionsAsArray();
                    for (final OCLPermission ocl : permissions) {
                        stmt.setInt(1, ctx.getContextId());
                        stmt.setInt(2, newFolderID);
                        stmt.setInt(3, ocl.getEntity());
                        stmt.setInt(4, ocl.getFolderPermission());
                        stmt.setInt(5, ocl.getReadPermission());
                        stmt.setInt(6, ocl.getWritePermission());
                        stmt.setInt(7, ocl.getDeletePermission());
                        stmt.setInt(8, ocl.isFolderAdmin() ? 1 : 0);
                        stmt.setInt(9, ocl.isGroupPermission() ? 1 : 0);
                        stmt.addBatch();
                    }
                    executeBatch(stmt);
                    stmt.close();
                    stmt = null;
                    final Date creatingDate = new Date(creatingTime);
                    folderObj.setObjectID(newFolderID);
                    folderObj.setCreationDate(creatingDate);
                    folderObj.setCreatedBy(userId);
                    folderObj.setLastModified(creatingDate);
                    folderObj.setModifiedBy(userId);
                    folderObj.setSubfolderFlag(false);
                    if (!acceptDefaultFlag) {
                        folderObj.setDefaultFolder(false);
                    }
                } finally {
                    if (stmt != null) {
                        stmt.close();
                        stmt = null;
                    }
                }
            } catch (final SQLException e) {
                if (isAuto) {
                    writeCon.rollback();
                    writeCon.setAutoCommit(true);
                }
                throw e;
            }
            if (isAuto) {
                writeCon.commit();
                writeCon.setAutoCommit(true);
            }
        } finally {
            if (closeWriteCon && writeCon != null) {
                DBPool.closeWriterSilent(ctx, writeCon);
            }
        }
    }

    private static final String SQL_UPDATE_WITH_FOLDERNAME = "UPDATE oxfolder_tree SET fname = ?, changing_date = ?, changed_from = ?, " + "permission_flag = ?, module = ? WHERE cid = ? AND fuid = ?";

    private static final String SQL_UPDATE_WITHOUT_FOLDERNAME = "UPDATE oxfolder_tree SET changing_date = ?, changed_from = ?, " + "permission_flag = ?, module = ? WHERE cid = ? AND fuid = ?";

    private static final String SQL_DELETE_EXISTING_PERMISSIONS = "DELETE FROM oxfolder_permissions WHERE cid = ? AND fuid = ? AND system = 0";

    static void updateFolderSQL(final int userId, final FolderObject folderObj, final long lastModified, final Context ctx, final Connection writeConArg) throws SQLException, OXException {
        Connection writeCon = writeConArg;
        /*
         * Update Folder
         */
        int permissionFlag = FolderObject.CUSTOM_PERMISSION;
        if (folderObj.getType() == FolderObject.PRIVATE) {
            if (folderObj.getPermissions().size() == 1) {
                permissionFlag = FolderObject.PRIVATE_PERMISSION;
            }
        } else if (folderObj.getType() == FolderObject.PUBLIC) {
            final int permissionsSize = folderObj.getPermissions().size();
            final Iterator<OCLPermission> iter = folderObj.getPermissions().iterator();
            for (int i = 0; i < permissionsSize; i++) {
                final OCLPermission oclPerm = iter.next();
                if (oclPerm.getEntity() == OCLPermission.ALL_GROUPS_AND_USERS && oclPerm.getFolderPermission() > OCLPermission.NO_PERMISSIONS) {
                    permissionFlag = FolderObject.PUBLIC_PERMISSION;
                    break;
                }
            }
        }
        boolean closeWriteCon = false;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            final boolean isAuto = writeCon.getAutoCommit();
            if (isAuto) {
                writeCon.setAutoCommit(false);
            }
            PreparedStatement stmt = null;
            try {
                int pos = 1;
                if (folderObj.containsFolderName()) {
                    stmt = writeCon.prepareStatement(SQL_UPDATE_WITH_FOLDERNAME);
                    stmt.setString(pos++, folderObj.getFolderName());
                    stmt.setLong(pos++, lastModified);
                    stmt.setInt(pos++, userId);
                    stmt.setInt(pos++, permissionFlag);
                    stmt.setInt(pos++, folderObj.getModule());
                    stmt.setInt(pos++, ctx.getContextId());
                    stmt.setInt(pos++, folderObj.getObjectID());
                    executeUpdate(stmt);
                    stmt.close();
                    stmt = null;
                } else {
                    stmt = writeCon.prepareStatement(SQL_UPDATE_WITHOUT_FOLDERNAME);
                    stmt.setLong(pos++, lastModified);
                    stmt.setInt(pos++, userId);
                    stmt.setInt(pos++, permissionFlag);
                    stmt.setInt(pos++, folderObj.getModule());
                    stmt.setInt(pos++, ctx.getContextId());
                    stmt.setInt(pos++, folderObj.getObjectID());
                    executeUpdate(stmt);
                    stmt.close();
                    stmt = null;
                }
                /*
                 * Delete old non-system-permissions
                 */
                stmt = writeCon.prepareStatement(SQL_DELETE_EXISTING_PERMISSIONS);
                pos = 1;
                stmt.setInt(pos++, ctx.getContextId());
                stmt.setInt(pos++, folderObj.getObjectID());
                executeUpdate(stmt);
                stmt.close();
                stmt = null;
                /*
                 * Insert new non-system-permissions
                 */
                stmt = writeCon.prepareStatement(SQL_INSERT_NEW_PERMISSIONS);
                final OCLPermission[] permissions = folderObj.getNonSystemPermissionsAsArray();
                for (final OCLPermission oclPerm : permissions) {
                    pos = 1;
                    stmt.setInt(pos++, ctx.getContextId());
                    stmt.setInt(pos++, folderObj.getObjectID());
                    stmt.setInt(pos++, oclPerm.getEntity());
                    stmt.setInt(pos++, oclPerm.getFolderPermission());
                    stmt.setInt(pos++, oclPerm.getReadPermission());
                    stmt.setInt(pos++, oclPerm.getWritePermission());
                    stmt.setInt(pos++, oclPerm.getDeletePermission());
                    stmt.setInt(pos++, oclPerm.isFolderAdmin() ? 1 : 0);
                    stmt.setInt(pos++, oclPerm.isGroupPermission() ? 1 : 0);
                    stmt.addBatch();
                }
                executeBatch(stmt);
                stmt.close();
                stmt = null;
            } catch (final SQLException e) {
                if (isAuto) {
                    writeCon.rollback();
                    writeCon.setAutoCommit(true);
                }
                throw e;
            } finally {
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
            }
            if (isAuto) {
                writeCon.commit();
                writeCon.setAutoCommit(true);
            }
        } finally {
            if (closeWriteCon && writeCon != null) {
                DBPool.closeWriterSilent(ctx, writeCon);
            }
        }
    }

    private static final String SQL_MOVE_UPDATE = "UPDATE oxfolder_tree SET parent = ?, changing_date = ?, changed_from = ? WHERE cid = ? AND fuid = ?";

    private static final String SQL_MOVE_SELECT = "SELECT fuid FROM oxfolder_tree WHERE cid = ? AND parent = ?";

    private static final String SQL_MOVE_UPDATE2 = "UPDATE oxfolder_tree SET subfolder_flag = ?, changing_date = ?, changed_from = ? WHERE cid = ? AND fuid = ?";

    static void moveFolderSQL(final int userId, final FolderObject src, final FolderObject dest, final long lastModified, final Context ctx, final Connection readConArg, final Connection writeConArg) throws SQLException, OXException {
        Connection writeCon = writeConArg;
        boolean closeWriteCon = false;
        Connection readCon = readConArg;
        PreparedStatement pst = null;
        ResultSet subFolderRS = null;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            if (readCon == null) {
                /*
                 * Use write-connection as read-connection
                 */
                readCon = writeCon;
            }
            final boolean isAuto = writeCon.getAutoCommit();
            if (isAuto) {
                writeCon.setAutoCommit(false);
            }
            try {
                pst = writeCon.prepareStatement(SQL_MOVE_UPDATE);
                pst.setInt(1, dest.getObjectID());
                pst.setLong(2, lastModified);
                pst.setInt(3, src.getType() == FolderObject.SYSTEM_TYPE ? ctx.getMailadmin() : userId);
                pst.setInt(4, ctx.getContextId());
                pst.setInt(5, src.getObjectID());
                executeUpdate(pst);
                pst.close();
                pst = null;
                /*
                 * Set target folder's/source parent folder's subfolder flag
                 */
                pst = readCon.prepareStatement(SQL_MOVE_SELECT);
                pst.setInt(1, ctx.getContextId());
                pst.setInt(2, src.getParentFolderID());
                subFolderRS = executeQuery(pst);
                final boolean srcParentHasSubfolders = subFolderRS.next();
                subFolderRS.close();
                subFolderRS = null;
                pst.close();
                pst = null;
                pst = writeCon.prepareStatement(SQL_MOVE_UPDATE2);
                pst.setInt(1, 1);
                pst.setLong(2, lastModified);
                pst.setInt(3, dest.getType() == FolderObject.SYSTEM_TYPE ? ctx.getMailadmin() : userId);
                pst.setInt(4, ctx.getContextId());
                pst.setInt(5, dest.getObjectID());
                pst.addBatch();
                pst.setInt(1, srcParentHasSubfolders ? 1 : 0);
                pst.setLong(2, lastModified);
                pst.setInt(3, src.getType() == FolderObject.SYSTEM_TYPE ? ctx.getMailadmin() : userId);
                pst.setInt(4, ctx.getContextId());
                pst.setInt(5, src.getParentFolderID());
                pst.addBatch();
                executeBatch(pst);
                pst.close();
                pst = null;
            } catch (final SQLException se) {
                if (isAuto) {
                    writeCon.rollback();
                    writeCon.setAutoCommit(true);
                }
                throw se;
            }
            if (isAuto) {
                writeCon.commit();
                writeCon.setAutoCommit(true);
            }
        } finally {
            closeSQLStuff(subFolderRS, pst);
            if (closeWriteCon && writeCon != null) {
                DBPool.closeWriterSilent(ctx, writeCon);
            }
        }
    }

    private static final String SQL_RENAME_UPDATE = "UPDATE oxfolder_tree SET fname = ?, changing_date = ?, changed_from = ? where cid = ? AND fuid = ?";

    static void renameFolderSQL(final int userId, final FolderObject folderObj, final long lastModified, final Context ctx, final Connection writeConArg) throws SQLException, OXException {
        Connection writeCon = writeConArg;
        boolean closeWriteCon = false;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            final boolean isAuto = writeCon.getAutoCommit();
            if (isAuto) {
                writeCon.setAutoCommit(false);
            }
            PreparedStatement pst = null;
            try {
                pst = writeCon.prepareStatement(SQL_RENAME_UPDATE);
                pst.setString(1, folderObj.getFolderName());
                pst.setLong(2, lastModified);
                pst.setInt(3, userId);
                pst.setInt(4, ctx.getContextId());
                pst.setInt(5, folderObj.getObjectID());
                executeUpdate(pst);
                pst.close();
                pst = null;
            } catch (final SQLException sqle) {
                if (isAuto) {
                    writeCon.rollback();
                    writeCon.setAutoCommit(true);
                }
                throw sqle;
            } finally {
                if (pst != null) {
                    pst.close();
                    pst = null;
                }
            }
            if (isAuto) {
                writeCon.commit();
                writeCon.setAutoCommit(true);
            }
        } finally {
            if (closeWriteCon && writeCon != null) {
                DBPool.closeWriterSilent(ctx, writeCon);
            }
        }
    }

    private static final String STR_OXFOLDERTREE = "oxfolder_tree";

    private static final String STR_OXFOLDERPERMS = "oxfolder_permissions";

    private static final String STR_DELOXFOLDERTREE = "del_oxfolder_tree";

    private static final String STR_DELOXFOLDERPERMS = "del_oxfolder_permissions";

    static void delWorkingOXFolder(final int folderId, final int userId, final long lastModified, final Context ctx, final Connection writeConArg) throws SQLException, OXException {
        delOXFolder(folderId, userId, lastModified, true, true, ctx, writeConArg);
    }

    private static final String SQL_DELETE_INSERT_OT = "INSERT INTO del_oxfolder_tree SELECT * FROM oxfolder_tree WHERE cid = ? AND fuid = ?";

    private static final String SQL_DELETE_INSERT_OP = "INSERT INTO del_oxfolder_permissions SELECT * FROM oxfolder_permissions WHERE cid = ? AND fuid = ?";

    private static final String SQL_DELETE_DELETE_SF = "DELETE FROM oxfolder_specialfolders WHERE cid = ? AND fuid = ?";

    private static final String SQL_DELETE_DELETE = "DELETE FROM #TABLE# WHERE cid = ? AND fuid = ?";

    private static final String SQL_DELETE_UPDATE = "UPDATE del_oxfolder_tree SET changing_date = ?, changed_from = ? WHERE cid = ? AND fuid = ?";

    /**
     * Deletes a folder entry - and its corresponding permission entries as well - from underlying storage. <code>deleteWorking</code>
     * determines whether working or backup tables are affected by delete operation. <code>createBackup</code> specifies if backup entries
     * are going to be created and is only allowed if <code>deleteWorking</code> is set to <code>true</code>.
     */
    static void delOXFolder(final int folderId, final int userId, final long lastModified, final boolean deleteWorking, final boolean createBackup, final Context ctx, final Connection writeConArg) throws SQLException, OXException {
        Connection writeCon = writeConArg;
        boolean closeWriteCon = false;
        if (writeCon == null) {
            writeCon = DBPool.pickupWriteable(ctx);
            closeWriteCon = true;
        }
        final boolean isAuto = writeCon.getAutoCommit();
        if (isAuto) {
            writeCon.setAutoCommit(false);
        }
        final String folderTable = deleteWorking ? STR_OXFOLDERTREE : STR_DELOXFOLDERTREE;
        final String permTable = deleteWorking ? STR_OXFOLDERPERMS : STR_DELOXFOLDERPERMS;
        final boolean backup = (createBackup && deleteWorking);
        PreparedStatement stmt = null;
        try {
            if (backup) {
                /*
                 * Clean backup tables
                 */
                stmt = writeCon.prepareStatement(SQL_DELETE_DELETE.replaceFirst("#TABLE#", STR_DELOXFOLDERPERMS));
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, folderId);
                executeUpdate(stmt);
                stmt.close();
                stmt = null;
                stmt = writeCon.prepareStatement(SQL_DELETE_DELETE.replaceFirst("#TABLE#", STR_DELOXFOLDERTREE));
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, folderId);
                executeUpdate(stmt);
                stmt.close();
                stmt = null;
                /*
                 * Copy backup entries into del_oxfolder_tree and del_oxfolder_permissions
                 */
                stmt = writeCon.prepareStatement(SQL_DELETE_INSERT_OT);
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, folderId);
                executeUpdate(stmt);
                stmt.close();
                stmt = null;
                stmt = writeCon.prepareStatement(SQL_DELETE_INSERT_OP);
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, folderId);
                executeUpdate(stmt);
                stmt.close();
                stmt = null;
            }
            if (deleteWorking) {
                /*
                 * Delete from oxfolder_specialfolders
                 */
                stmt = writeCon.prepareStatement(SQL_DELETE_DELETE_SF);
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, folderId);
                executeUpdate(stmt);
                stmt.close();
                stmt = null;
            }
            /*
             * Delete from permission table
             */
            stmt = writeCon.prepareStatement(SQL_DELETE_DELETE.replaceFirst("#TABLE#", permTable));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            /*
             * Delete from folder table
             */
            stmt = writeCon.prepareStatement(SQL_DELETE_DELETE.replaceFirst("#TABLE#", folderTable));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            if (backup) {
                /*
                 * Update last-modified timestamp of entries in backup tables
                 */
                stmt = writeCon.prepareStatement(SQL_DELETE_UPDATE);
                stmt.setLong(1, lastModified);
                stmt.setInt(2, userId);
                stmt.setInt(3, ctx.getContextId());
                stmt.setInt(4, folderId);
                executeUpdate(stmt);
                stmt.close();
                stmt = null;
            }
            /*
             * Commit
             */
            if (isAuto) {
                writeCon.commit();
            }
        } catch (final SQLException e) {
            if (isAuto) {
                DBUtils.rollback(writeCon);
            }
            throw e;
        } finally {
            DBUtils.closeSQLStuff(stmt);
            if (isAuto) {
                DBUtils.autocommit(writeCon);
            }
            if (closeWriteCon) {
                DBPool.closeWriterSilent(ctx, writeCon);
            }
        }
    }

    static void backupOXFolder(final int folderId, final int userId, final long lastModified, final Context ctx, final Connection writeConArg) throws SQLException, OXException {
        Connection writeCon = writeConArg;
        boolean closeWriteCon = false;
        if (writeCon == null) {
            writeCon = DBPool.pickupWriteable(ctx);
            closeWriteCon = true;
        }
        final boolean isAuto = writeCon.getAutoCommit();
        if (isAuto) {
            writeCon.setAutoCommit(false);
        }
        PreparedStatement stmt = null;
        try {
            /*
             * Clean backup tables
             */
            stmt = writeCon.prepareStatement(SQL_DELETE_DELETE.replaceFirst("#TABLE#", STR_DELOXFOLDERPERMS));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            stmt = writeCon.prepareStatement(SQL_DELETE_DELETE.replaceFirst("#TABLE#", STR_DELOXFOLDERTREE));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            /*
             * Copy backup entries into del_oxfolder_tree and del_oxfolder_permissions
             */
            stmt = writeCon.prepareStatement(SQL_DELETE_INSERT_OT);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            stmt = writeCon.prepareStatement(SQL_DELETE_INSERT_OP);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            /*
             * Update last-modified timestamp of entries in backup tables
             */
            stmt = writeCon.prepareStatement(SQL_DELETE_UPDATE);
            stmt.setLong(1, lastModified);
            stmt.setInt(2, userId);
            stmt.setInt(3, ctx.getContextId());
            stmt.setInt(4, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            /*
             * Commit
             */
            if (isAuto) {
                writeCon.commit();
            }
        } catch (final SQLException e) {
            if (isAuto) {
                DBUtils.rollback(writeCon);
            }
            throw e;
        } finally {
            DBUtils.closeSQLStuff(stmt);
            if (isAuto) {
                DBUtils.autocommit(writeCon);
            }
            if (closeWriteCon) {
                DBPool.closeWriterSilent(ctx, writeCon);
            }
        }
    }

    private static final String SQL_RESTORE_OT = "INSERT INTO oxfolder_tree SELECT * FROM del_oxfolder_tree WHERE cid = ? AND fuid = ?";

    private static final String SQL_RESTORE_OP = "INSERT INTO oxfolder_permissions SELECT * FROM del_oxfolder_permissions WHERE cid = ? AND fuid = ?";

    public static void restore(final int folderId, final Context ctx, final Connection writeConArg) throws OXException, SQLException {
        Connection writeCon = writeConArg;
        boolean closeWriteCon = false;
        if (writeCon == null) {
            writeCon = DBPool.pickupWriteable(ctx);
            closeWriteCon = true;
        }
        final boolean isAuto = writeCon.getAutoCommit();
        if (isAuto) {
            writeCon.setAutoCommit(false);
        }
        PreparedStatement stmt = null;
        try {
            /*
             * Copy backup entries into oxfolder_tree and oxfolder_permissions
             */
            stmt = writeCon.prepareStatement(SQL_RESTORE_OT);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            stmt = writeCon.prepareStatement(SQL_RESTORE_OP);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            /*
             * Clean backup tables
             */
            stmt = writeCon.prepareStatement(SQL_DELETE_DELETE.replaceFirst("#TABLE#", STR_DELOXFOLDERPERMS));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            stmt = writeCon.prepareStatement(SQL_DELETE_DELETE.replaceFirst("#TABLE#", STR_DELOXFOLDERTREE));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            /*
             * Commit
             */
            if (isAuto) {
                writeCon.commit();
            }
        } catch (final SQLException e) {
            if (isAuto) {
                DBUtils.rollback(writeCon);
            }
            throw e;
        } finally {
            DBUtils.closeSQLStuff(stmt);
            if (isAuto) {
                DBUtils.autocommit(writeCon);
            }
            if (closeWriteCon) {
                DBPool.closeWriterSilent(ctx, writeCon);
            }
        }
    }

    private static final Lock NEXTSERIAL_LOCK = new ReentrantLock();

    /**
     * Fetches an unique id from underlying storage. NOTE: This method assumes that given writable connection is set to auto-commit! In any
     * case the <code>commit()</code> will be invoked, so any surrounding BEGIN-COMMIT mechanisms will be canceled.
     *
     * @param ctx The context
     * @param callWriteConArg A writable connection
     * @return A unique folder id from underlying storage
     * @throws SQLException If a SQL error occurs
     * @throws OXException If writable connection cannot be obtained from/put back into pool
     */
    public static int getNextSerial(final Context ctx, final Connection callWriteConArg) throws SQLException, OXException {
        NEXTSERIAL_LOCK.lock();
        try {
            Connection callWriteCon = callWriteConArg;
            boolean closeCon = false;
            boolean isAuto = false;
            try {
                try {
                    if (callWriteCon == null) {
                        callWriteCon = DBPool.pickupWriteable(ctx);
                        closeCon = true;
                    }
                    isAuto = callWriteCon.getAutoCommit();
                    if (isAuto) {
                        callWriteCon.setAutoCommit(false); // BEGIN
                    } else {
                        /*
                         * Commit connection to ensure an unique ID is going to be returned
                         */
                        callWriteCon.commit();
                    }
                    final int id = IDGenerator.getId(ctx, Types.FOLDER, callWriteCon);
                    if (isAuto) {
                        callWriteCon.commit(); // COMMIT
                        callWriteCon.setAutoCommit(true);
                    } else {
                        /*
                         * Commit connection to ensure an unique ID is going to be returned
                         */
                        callWriteCon.commit();
                    }
                    return id;
                } finally {
                    if (closeCon && callWriteCon != null) {
                        DBPool.pushWrite(ctx, callWriteCon);
                    }
                }
            } catch (final OXException e) {
                if (isAuto && callWriteCon != null) {
                    callWriteCon.rollback(); // ROLLBACK
                    callWriteCon.setAutoCommit(true);
                }
                throw e;
            }
        } finally {
            NEXTSERIAL_LOCK.unlock();
        }
    }

    /**
     * This method is used to generate identifier when creating a context.
     * @param ctx context to create.
     * @param con writable connection to the context database in transaction mode - autocommit is false.
     * @return a unique identifier for a folder.
     * @throws SQLException if generating this unique folder identifier fails.
     */
    public static int getNextSerialForAdmin(final Context ctx, final Connection con) throws SQLException {
        return IDGenerator.getId(ctx, Types.FOLDER, con);
    }

    static void hardDeleteOXFolder(final int folderId, final Context ctx, final Connection writeConArg) throws SQLException, OXException {
        Connection writeCon = writeConArg;
        boolean closeWrite = false;
        if (writeCon == null) {
            try {
                writeCon = DBPool.pickupWriteable(ctx);
            } catch (final OXException e) {
                throw e;
            }
            closeWrite = true;
        }
        final boolean isAuto = writeCon.getAutoCommit();
        if (isAuto) {
            writeCon.setAutoCommit(false);
        }
        Statement stmt = null;
        try {
            final String andClause = " AND fuid = ";
            stmt = writeCon.createStatement();
            stmt.addBatch(new StringBuilder("DELETE FROM oxfolder_specialfolders WHERE cid = ").append(ctx.getContextId()).append(andClause).append(
                folderId).toString());

            stmt.addBatch(new StringBuilder("DELETE FROM oxfolder_permissions WHERE cid = ").append(ctx.getContextId()).append(andClause).append(
                folderId).toString());

            stmt.addBatch(new StringBuilder("DELETE FROM oxfolder_tree WHERE cid = ").append(ctx.getContextId()).append(andClause).append(
                folderId).toString());

            stmt.executeBatch();

            if (isAuto) {
                writeCon.commit();
            }
        } catch (final SQLException e) {
            if (isAuto) {
                writeCon.rollback();
            }
            throw e;
        } finally {
            if (isAuto) {
                autocommit(writeCon);
            }
            closeResources(null, stmt, closeWrite ? writeCon : null, false, ctx);
        }
    }

    /*-
     * -------------- Helper methods for OXFolderDeleteListener (User removal) --------------
     */

    private static final String TMPL_FOLDER_TABLE = "#FOLDER#";

    private static final String TMPL_PERM_TABLE = "#PERM#";

    private static final String TMPL_IDS = "#IDS#";

    private static final String SQL_DROP_SYS_PERMS = "DELETE FROM " + TMPL_PERM_TABLE + " WHERE cid = ? AND permission_id = ? AND system > 0";

    /**
     * Drops all system-permissions belonging to specified entity in given context
     *
     * @param entity The entity
     * @param permTable The permission table
     * @param writeConArg The writable connection
     * @param ctx The context
     * @throws OXException If a pooling error occurs
     * @throws SQLException If a SQL error occurs
     */
    static void cleanseSystemPermissions(final int entity, final String permTable, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection writeCon = writeConArg;
        boolean createReadCon = false;
        PreparedStatement stmt = null;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                createReadCon = true;
            }
            stmt = writeCon.prepareStatement(SQL_DROP_SYS_PERMS.replaceFirst(TMPL_PERM_TABLE, permTable));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, entity);
            executeUpdate(stmt);
        } finally {
            closeResources(null, stmt, createReadCon ? writeCon : null, false, ctx);
        }
    }

    private static final String SQL_GET_CONTEXT_MAILADMIN = "SELECT user FROM user_setting_admin WHERE cid = ?";

    static int getContextMailAdmin(final Connection readConArg, final Context ctx) throws OXException, SQLException {
        Connection readCon = readConArg;
        boolean createReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                createReadCon = true;
            }
            stmt = readCon.prepareStatement(SQL_GET_CONTEXT_MAILADMIN);
            stmt.setInt(1, ctx.getContextId());
            rs = executeQuery(stmt);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } finally {
            closeResources(rs, stmt, createReadCon ? readCon : null, true, ctx);
        }
    }

    private static final String SQL_SEL_PERMS = "SELECT ot.fuid, ot.type FROM " + TMPL_PERM_TABLE + " AS op JOIN " + TMPL_FOLDER_TABLE + " AS ot ON op.fuid = ot.fuid AND op.cid = ? AND ot.cid = ? WHERE op.permission_id IN " + TMPL_IDS + " GROUP BY ot.fuid";

    /**
     * Deletes all permissions assigned to context's mail admin from given permission table.
     */
    static void handleMailAdminPermissions(final int mailAdmin, final String folderTable, final String permTable, final Connection readConArg, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        handleEntityPermissions(mailAdmin, null, -1L, folderTable, permTable, readConArg, writeConArg, ctx);
    }

    /**
     * Handles entity' permissions located in given permission table. If permission is associated with a private folder, it is going to be
     * deleted. Otherwise the permission is reassigned to mailadmin.
     */
    static void handleEntityPermissions(final int entity, final int mailAdmin, final long lastModified, final String folderTable, final String permTable, final Connection readConArg, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        handleEntityPermissions(entity, Integer.valueOf(mailAdmin), lastModified, folderTable, permTable, readConArg, writeConArg, ctx);
    }

    private static void handleEntityPermissions(final int entity, final Integer mailAdmin, final long lastModified, final String folderTable, final String permTable, final Connection readConArg, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final boolean isMailAdmin = (mailAdmin == null);
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            final String permissionsIDs;
            if (isMailAdmin) {
                permissionsIDs = new StringBuilder().append('(').append(entity).append(',').append(OCLPermission.ALL_GROUPS_AND_USERS).append(
                    ')').toString();
            } else {
                permissionsIDs = new StringBuilder().append('(').append(entity).append(')').toString();
            }
            stmt = readCon.prepareStatement(SQL_SEL_PERMS.replaceFirst(TMPL_PERM_TABLE, permTable).replaceFirst(
                TMPL_FOLDER_TABLE,
                folderTable).replaceFirst(TMPL_IDS, permissionsIDs));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            rs = executeQuery(stmt);
            final TIntSet deletePerms = new TIntHashSet();
            final TIntSet reassignPerms = new TIntHashSet();
            while (rs.next()) {
                final int fuid = rs.getInt(1);
                final int type = rs.getInt(2);
                if (isMailAdmin || markForDeletion(type)) {
                    deletePerms.add(fuid);
                } else {
                    reassignPerms.add(fuid);
                }
            }
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            /*
             * Delete
             */
            deletePermissions(deletePerms, entity, permTable, writeConArg, ctx);
            if (!isMailAdmin) {
                /*
                 * Reassign
                 */
                reassignPermissions(
                    reassignPerms,
                    entity,
                    mailAdmin.intValue(),
                    lastModified,
                    folderTable,
                    permTable,
                    readCon,
                    writeConArg,
                    ctx);
            }
            /*
             * Remove from cache
             */
            ConditionTreeMapManagement.dropFor(ctx.getContextId());
            if (FolderCacheManager.isInitialized()) {
                /*
                 * Invalidate cache
                 */
                try {
                    TIntIterator iter = deletePerms.iterator();
                    for (int i = deletePerms.size(); i-- > 0;) {
                        FolderCacheManager.getInstance().removeFolderObject(iter.next(), ctx);
                    }
                    iter = reassignPerms.iterator();
                    for (int i = reassignPerms.size(); i-- > 0;) {
                        FolderCacheManager.getInstance().removeFolderObject(iter.next(), ctx);
                    }
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            /*
             * Post events
             */
            final EventAdmin eventAdmin = ServerServiceRegistry.getInstance().getService(EventAdmin.class);
            if (null != eventAdmin) {
                TIntIterator iter = deletePerms.iterator();
                for (int i = deletePerms.size(); i-- > 0;) {
                    broadcastEvent(iter.next(), false, entity, ctx.getContextId(), eventAdmin);
                }
                iter = reassignPerms.iterator();
                for (int i = reassignPerms.size(); i-- > 0;) {
                    broadcastEvent(iter.next(), false, entity, ctx.getContextId(), eventAdmin);
                }
            }
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
    }

    private static void broadcastEvent(final int fuid, final boolean deleted, final int entity, final int contextId, final EventAdmin eventAdmin) {
        final Dictionary<String, Object> properties = new Hashtable<String, Object>(6);
        properties.put(FolderEventConstants.PROPERTY_CONTEXT, Integer.valueOf(contextId));
        properties.put(FolderEventConstants.PROPERTY_USER, Integer.valueOf(entity));
        properties.put(FolderEventConstants.PROPERTY_FOLDER, Integer.toString(fuid));
        properties.put(FolderEventConstants.PROPERTY_CONTENT_RELATED, Boolean.valueOf(!deleted));
        /*
         * Create event with push topic
         */
        final Event event = new Event(FolderEventConstants.TOPIC, properties);
        /*
         * Finally deliver it
         */
        eventAdmin.sendEvent(event);
        if (LOG.isDebugEnabled()) {
            LOG.debug(new StringBuilder(64).append("Notified ").append("content-related").append(
                "-wise changed folder \"").append(fuid).append(" in context ").append(contextId).toString());
        }
    }

    private static final String SQL_DELETE_PERMS = "DELETE FROM " + TMPL_PERM_TABLE + " WHERE cid = ? AND fuid = ? AND permission_id = ?";

    private static void deletePermissions(final TIntSet deletePerms, final int entity, final String permTable, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        final int size = deletePerms.size();
        if (size == 0) {
            return;
        }
        final TIntIterator iter = deletePerms.iterator();
        Connection wc = writeConArg;
        boolean closeWrite = false;
        PreparedStatement stmt = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWrite = true;
            }
            stmt = wc.prepareStatement(SQL_DELETE_PERMS.replaceFirst(TMPL_PERM_TABLE, permTable));
            for (int i = 0; i < size; i++) {
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, iter.next());
                stmt.setInt(3, entity);
                stmt.addBatch();
            }
            executeBatch(stmt);
        } finally {
            closeResources(null, stmt, closeWrite ? wc : null, false, ctx);
        }
    }

    private static final String SQL_REASSIGN_PERMS = "UPDATE " + TMPL_PERM_TABLE + " SET permission_id = ?, group_flag = 0 WHERE cid = ? AND fuid = ? AND permission_id = ?";

    private static final String SQL_REASSIGN_UPDATE_TIMESTAMP = "UPDATE " + TMPL_FOLDER_TABLE + " SET changed_from = ?, changing_date = ? WHERE cid = ? AND fuid = ?";

    private static void reassignPermissions(final TIntSet reassignPerms, final int entity, final int mailAdmin, final long lastModified, final String folderTable, final String permTable, final Connection readConArg, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        final int size = reassignPerms.size();
        if (size == 0) {
            return;
        }
        Connection wc = writeConArg;
        boolean closeWrite = false;
        Connection rc = readConArg;
        boolean closeRead = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWrite = true;
            }
            if (rc == null) {
                rc = DBPool.pickup(ctx);
                closeRead = true;
            }
            // stmt =
            // wc.prepareStatement(SQL_REASSIGN_PERMS.replaceFirst(TMPL_PERM_TABLE
            // ,
            // permTable));
            TIntIterator iter = reassignPerms.iterator();
            Next: for (int i = 0; i < size; i++) {
                final int fuid = iter.next();
                /*
                 * Check if admin already holds permission on current folder
                 */
                stmt = rc.prepareStatement("SELECT 1 FROM " + permTable + " WHERE cid = ? AND permission_id = ? AND fuid = ?");
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, mailAdmin);
                stmt.setInt(3, fuid);
                rs = executeQuery(stmt);
                final boolean hasPerm = rs.next();
                rs.close();
                rs = null;
                stmt.close();
                stmt = null;
                if (hasPerm) {
                    /*
                     * User (Mail Admin) already holds permission on this folder
                     */
                    try {
                        /*
                         * Set to merged permission
                         */
                        final OCLPermission mergedPerm = getMergedPermission(entity, mailAdmin, fuid, permTable, readConArg, ctx);
                        deleteSingleEntityPermission(entity, fuid, permTable, wc, ctx);
                        updateSingleEntityPermission(mergedPerm, mailAdmin, fuid, permTable, wc, ctx);
                    } catch (final Exception e) {
                        LOG.error(e.getMessage(), e);
                        continue Next;
                    }
                } else {
                    stmt = wc.prepareStatement(SQL_REASSIGN_PERMS.replaceFirst(TMPL_PERM_TABLE, permTable));
                    stmt.setInt(1, mailAdmin);
                    stmt.setInt(2, ctx.getContextId());
                    stmt.setInt(3, fuid);
                    stmt.setInt(4, entity);
                    try {
                        executeUpdate(stmt);
                    } catch (final SQLException e) {
                        LOG.error(e.getMessage(), e);
                        continue Next;
                    } finally {
                        stmt.close();
                    }
                }
            }
            stmt = wc.prepareStatement(SQL_REASSIGN_UPDATE_TIMESTAMP.replaceFirst(TMPL_FOLDER_TABLE, folderTable));
            iter = reassignPerms.iterator();
            for (int i = 0; i < size; i++) {
                stmt.setInt(1, mailAdmin);
                stmt.setLong(2, lastModified);
                stmt.setInt(3, ctx.getContextId());
                stmt.setInt(4, iter.next());
                stmt.addBatch();
            }
            executeBatch(stmt);
        } finally {
            closeResources(rs, stmt, closeWrite ? wc : null, false, ctx);
            if (closeRead && rc != null) {
                DBPool.closeReaderSilent(ctx, rc);
            }
        }
    }

    private static final String SQL_REASSIGN_DEL_PERM = "DELETE FROM " + TMPL_PERM_TABLE + " WHERE cid = ? AND permission_id = ? AND fuid = ?";

    private static void deleteSingleEntityPermission(final int entity, final int fuid, final String permTable, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection wc = writeConArg;
        boolean close = false;
        PreparedStatement stmt = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                close = true;
            }
            stmt = wc.prepareStatement(SQL_REASSIGN_DEL_PERM.replaceFirst(TMPL_PERM_TABLE, permTable));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, entity);
            stmt.setInt(3, fuid);
            executeUpdate(stmt);
        } finally {
            closeResources(null, stmt, close ? wc : null, false, ctx);
        }
    }

    private static final String SQL_REASSIGN_UPDATE_PERM = "UPDATE " + TMPL_PERM_TABLE + " SET fp = ?, orp = ?, owp = ?, odp = ?, admin_flag = ?, group_flag = ? WHERE cid = ? AND permission_id = ? AND fuid = ?";

    private static void updateSingleEntityPermission(final OCLPermission mergedPerm, final int mailAdmin, final int fuid, final String permTable, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection wc = writeConArg;
        boolean close = false;
        PreparedStatement stmt = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                close = true;
            }
            stmt = wc.prepareStatement(SQL_REASSIGN_UPDATE_PERM.replaceFirst(TMPL_PERM_TABLE, permTable));
            stmt.setInt(1, mergedPerm.getFolderPermission());
            stmt.setInt(2, mergedPerm.getReadPermission());
            stmt.setInt(3, mergedPerm.getWritePermission());
            stmt.setInt(4, mergedPerm.getDeletePermission());
            stmt.setInt(5, mergedPerm.isFolderAdmin() ? 1 : 0);
            stmt.setInt(6, mergedPerm.isGroupPermission() ? 1 : 0);
            stmt.setInt(7, ctx.getContextId());
            stmt.setInt(8, mailAdmin);
            stmt.setInt(9, fuid);
            executeUpdate(stmt);
        } finally {
            closeResources(null, stmt, close ? wc : null, false, ctx);
        }
    }

    private static final String SQL_REASSIGN_SEL_PERM = "SELECT fp, orp, owp, odp, admin_flag FROM " + TMPL_PERM_TABLE + " WHERE cid = ? AND permission_id = ? AND fuid = ?";

    private static OCLPermission getMergedPermission(final int entity, final int mailAdmin, final int fuid, final String permTable, final Connection readConArg, final Context ctx) throws SQLException, OXException {
        Connection readCon = readConArg;
        boolean closeRead = false;
        PreparedStatement innerStmt = null;
        ResultSet innerRs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeRead = true;
            }
            innerStmt = readCon.prepareStatement(SQL_REASSIGN_SEL_PERM.replaceFirst(TMPL_PERM_TABLE, permTable));
            innerStmt.setInt(1, ctx.getContextId());
            innerStmt.setInt(2, mailAdmin);
            innerStmt.setInt(3, fuid);
            innerRs = executeQuery(innerStmt);
            if (!innerRs.next()) {
                /*
                 * Merged permission is entity's permission since no permission is defined for admin
                 */
                innerRs.close();
                innerStmt.close();
                innerStmt = readCon.prepareStatement(SQL_REASSIGN_SEL_PERM.replaceFirst(TMPL_PERM_TABLE, permTable));
                innerStmt.setInt(1, ctx.getContextId());
                innerStmt.setInt(2, entity);
                innerStmt.setInt(3, fuid);
                innerRs = executeQuery(innerStmt);
                if (!innerRs.next()) {
                    /*
                     * Empty permission
                     */
                    return new OCLPermission(mailAdmin, fuid);
                }
                final OCLPermission adminPerm = new OCLPermission(mailAdmin, fuid);
                adminPerm.setAllPermission(innerRs.getInt(1), innerRs.getInt(2), innerRs.getInt(3), innerRs.getInt(4));
                adminPerm.setFolderAdmin(innerRs.getInt(5) > 0);
                adminPerm.setGroupPermission(false);
                return adminPerm;
            }
            final OCLPermission adminPerm = new OCLPermission(mailAdmin, fuid);
            adminPerm.setAllPermission(innerRs.getInt(1), innerRs.getInt(2), innerRs.getInt(3), innerRs.getInt(4));
            adminPerm.setFolderAdmin(innerRs.getInt(5) > 0);
            adminPerm.setGroupPermission(false);
            innerRs.close();
            innerStmt.close();
            innerStmt = readCon.prepareStatement(SQL_REASSIGN_SEL_PERM.replaceFirst(TMPL_PERM_TABLE, permTable));
            innerStmt.setInt(1, ctx.getContextId());
            innerStmt.setInt(2, entity);
            innerStmt.setInt(3, fuid);
            innerRs = executeQuery(innerStmt);
            if (!innerRs.next()) {
                return adminPerm;
            }
            final OCLPermission entityPerm = new OCLPermission(entity, fuid);
            entityPerm.setAllPermission(innerRs.getInt(1), innerRs.getInt(2), innerRs.getInt(3), innerRs.getInt(4));
            entityPerm.setFolderAdmin(innerRs.getInt(5) > 0);
            /*
             * Merge
             */
            final OCLPermission mergedPerm = new OCLPermission(mailAdmin, fuid);
            mergedPerm.setFolderPermission(Math.max(adminPerm.getFolderPermission(), entityPerm.getFolderPermission()));
            mergedPerm.setReadObjectPermission(Math.max(adminPerm.getReadPermission(), entityPerm.getReadPermission()));
            mergedPerm.setWriteObjectPermission(Math.max(adminPerm.getWritePermission(), entityPerm.getWritePermission()));
            mergedPerm.setDeleteObjectPermission(Math.max(adminPerm.getDeletePermission(), entityPerm.getDeletePermission()));
            mergedPerm.setFolderAdmin(adminPerm.isFolderAdmin() || entityPerm.isFolderAdmin());
            mergedPerm.setGroupPermission(false);
            return mergedPerm;
        } finally {
            closeResources(innerRs, innerStmt, closeRead ? readCon : null, true, ctx);
        }
    }

    // ------------------- DELETE FOLDERS --------------------------

    static void handleMailAdminFolders(final int mailAdmin, final String folderTable, final String permTable, final Connection readConArg, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        handleEntityFolders(mailAdmin, null, -1L, folderTable, permTable, readConArg, writeConArg, ctx);
    }

    static void handleEntityFolders(final int entity, final int mailAdmin, final long lastModified, final String folderTable, final String permTable, final Connection readConArg, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        handleEntityFolders(entity, Integer.valueOf(mailAdmin), lastModified, folderTable, permTable, readConArg, writeConArg, ctx);
    }

    private static final String SQL_SEL_FOLDERS = "SELECT ot.fuid, ot.type FROM #FOLDER# AS ot WHERE ot.cid = ? AND ot.created_from = ?";

    private static final String SQL_SEL_FOLDERS2 = "SELECT ot.fuid FROM #FOLDER# AS ot WHERE ot.cid = ? AND ot.changed_from = ?";

    private static void handleEntityFolders(final int entity, final Integer mailAdmin, final long lastModified, final String folderTable, final String permTable, final Connection readConArg, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final boolean isMailAdmin = (mailAdmin == null);
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = readCon.prepareStatement(SQL_SEL_FOLDERS.replaceFirst(TMPL_FOLDER_TABLE, folderTable));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, entity);
            rs = executeQuery(stmt);
            TIntSet deleteFolders = new TIntHashSet();
            TIntSet reassignFolders = new TIntHashSet();
            while (rs.next()) {
                final int fuid = rs.getInt(1);
                final int type = rs.getInt(2);
                if (isMailAdmin || markForDeletion(type)) {
                    deleteFolders.add(fuid);
                } else {
                    reassignFolders.add(fuid);
                }
            }
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            /*
             * Delete
             */
            deleteFolders(deleteFolders, folderTable, permTable, writeConArg, ctx);
            if (!isMailAdmin) {
                /*
                 * Reassign
                 */
                reassignFolders(reassignFolders, entity, mailAdmin.intValue(), lastModified, folderTable, writeConArg, ctx);
            }
            /*
             * Remove from cache
             */
            ConditionTreeMapManagement.dropFor(ctx.getContextId());
            if (FolderCacheManager.isInitialized()) {
                /*
                 * Invalidate cache
                 */
                try {
                    TIntIterator iterator = deleteFolders.iterator();
                    for (int i = deleteFolders.size(); i-- > 0;) {
                        FolderCacheManager.getInstance().removeFolderObject(iterator.next(), ctx);
                    }
                    iterator = reassignFolders.iterator();
                    for (int i = reassignFolders.size(); i-- > 0;) {
                        FolderCacheManager.getInstance().removeFolderObject(iterator.next(), ctx);
                    }
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            /*
             * Check column "changed_from"
             */
            stmt = readCon.prepareStatement(SQL_SEL_FOLDERS2.replaceFirst(TMPL_FOLDER_TABLE, folderTable));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, entity);
            rs = executeQuery(stmt);
            deleteFolders = new TIntHashSet();
            reassignFolders = new TIntHashSet();
            while (rs.next()) {
                final int fuid = rs.getInt(1);
                if (isMailAdmin) {
                    deleteFolders.add(fuid);
                } else {
                    reassignFolders.add(fuid);
                }
            }
            /*
             * Delete
             */
            deleteFolders(deleteFolders, folderTable, permTable, writeConArg, ctx);
            if (!isMailAdmin) {
                /*
                 * Reassign
                 */
                reassignFolders(reassignFolders, entity, mailAdmin.intValue(), lastModified, folderTable, writeConArg, ctx);
            }
            /*
             * Remove from cache
             */
            if (FolderCacheManager.isInitialized()) {
                /*
                 * Invalidate cache
                 */
                try {
                    TIntIterator iterator = deleteFolders.iterator();
                    for (int i = deleteFolders.size(); i-- > 0;) {
                        FolderCacheManager.getInstance().removeFolderObject(iterator.next(), ctx);
                    }
                    iterator = reassignFolders.iterator();
                    for (int i = reassignFolders.size(); i-- > 0;) {
                        FolderCacheManager.getInstance().removeFolderObject(iterator.next(), ctx);
                    }
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            /*
             * Post events
             */
            final EventAdmin eventAdmin = ServerServiceRegistry.getInstance().getService(EventAdmin.class);
            if (null != eventAdmin) {
                TIntIterator iterator = deleteFolders.iterator();
                for (int i = deleteFolders.size(); i-- > 0;) {
                    broadcastEvent(iterator.next(), true, entity, ctx.getContextId(), eventAdmin);
                }
                iterator = reassignFolders.iterator();
                for (int i = reassignFolders.size(); i-- > 0;) {
                    broadcastEvent(iterator.next(), false, entity, ctx.getContextId(), eventAdmin);
                }
            }
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
    }

    private static final String SQL_DELETE_FOLDER = "DELETE FROM #FOLDER# WHERE cid = ? AND fuid = ?";

    private static void deleteFolders(final TIntSet deleteFolders, final String folderTable, final String permTable, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        final int size = deleteFolders.size();
        Connection wc = writeConArg;
        boolean closeWrite = false;
        PreparedStatement stmt = null;
        try {
            /*
             * Delete folder's permissions if any exist
             */
            TIntIterator iter = deleteFolders.iterator();
            for (int i = 0; i < size; i++) {
                final int fuid = iter.next();
                checkFolderPermissions(fuid, permTable, writeConArg, ctx);
            }
            /*
             * Delete references to table 'oxfolder_specialfolders'
             */
            iter = deleteFolders.iterator();
            for (int i = 0; i < size; i++) {
                final int fuid = iter.next();
                deleteSpecialfoldersRefs(fuid, writeConArg, ctx);
            }
            /*
             * Delete folders
             */
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWrite = true;
            }
            stmt = wc.prepareStatement(SQL_DELETE_FOLDER.replaceFirst(TMPL_FOLDER_TABLE, folderTable));
            iter = deleteFolders.iterator();
            for (int i = 0; i < size; i++) {
                final int fuid = iter.next();
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, fuid);
                stmt.addBatch();
            }
            executeBatch(stmt);
        } finally {
            closeResources(null, stmt, closeWrite ? wc : null, false, ctx);
        }
    }

    private static final String SQL_DELETE_SPECIAL_REFS = "DELETE FROM oxfolder_specialfolders WHERE cid = ? AND fuid = ?";

    private static void deleteSpecialfoldersRefs(final int fuid, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection wc = writeConArg;
        boolean closeWrite = false;
        PreparedStatement stmt = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWrite = true;
            }
            stmt = wc.prepareStatement(SQL_DELETE_SPECIAL_REFS);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, fuid);
            executeUpdate(stmt);
        } finally {
            closeResources(null, stmt, closeWrite ? wc : null, false, ctx);
        }
    }

    private static final String SQL_DELETE_FOLDER_PERMS = "DELETE FROM #PERM# WHERE cid = ? AND fuid = ?";

    private static void checkFolderPermissions(final int fuid, final String permTable, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection wc = writeConArg;
        boolean closeWrite = false;
        PreparedStatement stmt = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWrite = true;
            }
            stmt = wc.prepareStatement(SQL_DELETE_FOLDER_PERMS.replaceFirst(TMPL_PERM_TABLE, permTable));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, fuid);
            executeUpdate(stmt);
        } finally {
            closeResources(null, stmt, closeWrite ? wc : null, false, ctx);
        }
    }

    private static final String SQL_REASSIGN_FOLDERS = "UPDATE #FOLDER# SET created_from = ?, changed_from = ?, changing_date = ?, default_flag = 0 WHERE cid = ? AND fuid = ?";

    private static final String SQL_REASSIGN_FOLDERS_WITH_NAME = "UPDATE #FOLDER# SET created_from = ?, changed_from = ?, changing_date = ?, default_flag = 0, fname = ? WHERE cid = ? AND fuid = ?";

    private static void reassignFolders(final TIntSet reassignFolders, final int entity, final int mailAdmin, final long lastModified, final String folderTable, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection wc = writeConArg;
        boolean closeWrite = false;
        PreparedStatement stmt = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWrite = true;
            }
            int size = reassignFolders.size();
            TIntIterator iter = reassignFolders.iterator();
            {
                /*
                 * Special handling for default infostore folder
                 */
                boolean found = false;
                for (int i = 0; i < size && !found; i++) {
                    final int fuid = iter.next();
                    final String fname;
                    if ((fname = isDefaultInfostoreFolder(fuid, entity, folderTable, wc, ctx)) != null) {
                        iter.remove();
                        size--;
                        stmt = wc.prepareStatement(SQL_REASSIGN_FOLDERS_WITH_NAME.replaceFirst(TMPL_FOLDER_TABLE, folderTable));
                        stmt.setInt(1, mailAdmin);
                        stmt.setInt(2, mailAdmin);
                        stmt.setLong(3, lastModified);
                        stmt.setString(4, new StringBuilder(fname).append(fuid).toString());
                        stmt.setInt(5, ctx.getContextId());
                        stmt.setInt(6, fuid);
                        executeUpdate(stmt);
                        stmt.close();
                        stmt = null;
                        /*
                         * Leave loop
                         */
                        found = true;
                    }
                }
            }
            /*
             * Iterate rest
             */
            iter = reassignFolders.iterator();
            stmt = wc.prepareStatement(SQL_REASSIGN_FOLDERS.replaceFirst(TMPL_FOLDER_TABLE, folderTable));
            for (int i = 0; i < size; i++) {
                stmt.setInt(1, mailAdmin);
                stmt.setInt(2, mailAdmin);
                stmt.setLong(3, lastModified);
                stmt.setInt(4, ctx.getContextId());
                stmt.setInt(5, iter.next());
                stmt.addBatch();
            }
            executeBatch(stmt);
        } finally {
            closeResources(null, stmt, closeWrite ? wc : null, false, ctx);
        }
    }

    private static final String SQL_DEF_INF = "SELECT fname FROM #FOLDER# WHERE cid = ? AND fuid = ? AND module = ? AND created_from = ? AND default_flag = 1";

    /**
     * @return The entity's default infostore folder's name if <code>true</code> ; otherwise <code>null</code>
     */
    private static String isDefaultInfostoreFolder(final int fuid, final int entity, final String folderTable, final Connection con, final Context ctx) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_DEF_INF.replaceFirst(TMPL_FOLDER_TABLE, folderTable));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, fuid);
            stmt.setInt(3, FolderObject.INFOSTORE);
            stmt.setInt(4, entity);
            rs = executeQuery(stmt);
            return rs.next() ? rs.getString(1) : null;
        } finally {
            closeResources(rs, stmt, null, true, ctx);
        }
    }

    /**
     * @return <code>true</code> if folder type is set to private, <code>false</code> otherwise
     */
    private static boolean markForDeletion(final int type) {
        return (type == FolderObject.PRIVATE); // || (type ==
        // FolderObject.PUBLIC && module
        // == FolderObject.INFOSTORE &&
        // defaultFlag);
    }

    private static int executeUpdate(final PreparedStatement stmt) throws SQLException {
        try {
            return stmt.executeUpdate();
        } catch (final SQLException e) {
            if ("MySQLSyntaxErrorException".equals(e.getClass().getSimpleName())) {
                final String sql = stmt.toString();
                LOG.error(new StringBuilder().append("\nFollowing SQL query contains syntax errors:\n").append(
                    sql.substring(sql.indexOf(": ") + 2)).toString());
            }
            throw e;
        }
    }

    private static int[] executeBatch(final PreparedStatement stmt) throws SQLException {
        try {
            return stmt.executeBatch();
        } catch (final SQLException e) {
            if ("MySQLSyntaxErrorException".equals(e.getClass().getSimpleName())) {
                final String sql = stmt.toString();
                LOG.error(new StringBuilder().append("\nFollowing SQL query contains syntax errors:\n").append(
                    sql.substring(sql.indexOf(": ") + 2)).toString());
            }
            throw e;
        }
    }

    private static ResultSet executeQuery(final PreparedStatement stmt) throws SQLException {
        try {
            return stmt.executeQuery();
        } catch (final SQLException e) {
            if ("MySQLSyntaxErrorException".equals(e.getClass().getSimpleName())) {
                final String sql = stmt.toString();
                LOG.error(new StringBuilder().append("\nFollowing SQL query contains syntax errors:\n").append(
                    sql.substring(sql.indexOf(": ") + 2)).toString());
            }
            throw e;
        }
    }

}
