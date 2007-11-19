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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import static com.openexchange.tools.sql.DBUtils.closeResources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.api2.OXException;
import com.openexchange.cache.FolderCacheManager;
import com.openexchange.cache.FolderCacheNotEnabledException;
import com.openexchange.groupware.IDGenerator;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.OCLPermission;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;

/**
 * Contains useful SQL-related helper methods for folder operations
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OXFolderSQL {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(OXFolderSQL.class);

	private OXFolderSQL() {
		super();
	}

	private static final String SQL_SELECT_ADMIN = "SELECT user FROM user_setting_admin WHERE cid = ?";

	/**
	 * Determines the ID of the user who is defined as admin for given context
	 * or <code>-1</code> if none found
	 * 
	 * @param ctx
	 *            The context
	 * @param readConArg
	 *            A readable connection or <code>null</code> to fetch a new
	 *            one from connection pool
	 * @return The ID of context admin or <code>-1</code> if none found
	 * @throws DBPoolingException
	 *             If parameter <code>readConArg</code> is <code>null</code>
	 *             and no readable connection could be fetched from or put back
	 *             into connection pool
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static final int getContextAdminID(final Context ctx, final Connection readConArg)
			throws DBPoolingException, SQLException {
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
			rs = stmt.executeQuery();
			if (!rs.next()) {
				return -1;
			}
			return rs.getInt(1);
		} finally {
			closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
		}
	}

	private static final String SQL_DEFAULTFLD = "SELECT ot.fuid FROM oxfolder_tree AS ot WHERE ot.cid = ? AND ot.created_from = ? AND ot.module = ? AND ot.default_flag = 1";

	public static final int getUserDefaultFolder(final int userId, final int module, final Connection readConArg,
			final Context ctx) throws DBPoolingException, SQLException {
		Connection readCon = readConArg;
		boolean closeReadCon = false;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			if (readCon == null) {
				readCon = DBPool.pickup(ctx);
				closeReadCon = true;
			}
			stmt = readCon.prepareStatement(SQL_DEFAULTFLD);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, userId);
			stmt.setInt(3, module);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
			return -1;
		} finally {
			closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
		}
	}

	private static final String SQL_UPDATE_LAST_MOD = "UPDATE oxfolder_tree SET changing_date = ?, changed_from = ? WHERE cid = ? AND fuid = ?";

	/**
	 * Updates the last modified timestamp of the folder whose ID matches given
	 * parameter <code>folderId</code>.
	 * 
	 * @param folderId
	 *            The folder ID
	 * @param lastModified
	 *            The new last modified timestamp to set
	 * @param modifiedBy
	 *            The user who shall be inserted as modified-by
	 * @param writeConArg
	 *            A writeable connection or <code>null</code> to fetch a new
	 *            one from pool
	 * @param ctx
	 *            The context
	 * @throws DBPoolingException
	 *             If parameter <code>writeConArg</code> is <code>null</code>
	 *             and a pooling error occurs
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static final void updateLastModified(final int folderId, final long lastModified, final int modifiedBy,
			final Connection writeConArg, final Context ctx) throws DBPoolingException, SQLException {
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
			stmt.executeUpdate();
		} finally {
			closeResources(null, stmt, closeWriteCon ? writeCon : null, false, ctx);
		}
	}

	private static final String SQL_UPDATE_NAME = "UPDATE oxfolder_tree SET fname = ?, changing_date = ?, changed_from = ? WHERE cid = ? AND fuid = ?";

	/**
	 * Updates the name of the folder whose ID matches given parameter
	 * <code>folderId</code>.
	 * 
	 * @param folderId
	 *            The folder ID
	 * @param newName
	 *            The new name to set
	 * @param lastModified
	 *            The last modified timestamp
	 * @param modifiedBy
	 *            The user who shall be inserted as modified-by
	 * @param writeConArg
	 *            A writeable connection or <code>null</code> to fetch a new
	 *            one from pool
	 * @param ctx
	 *            The context
	 * @throws DBPoolingException
	 *             If parameter <code>writeConArg</code> is <code>null</code>
	 *             and a pooling error occurs
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static final void updateName(final int folderId, final String newName, final long lastModified,
			final int modifiedBy, final Connection writeConArg, final Context ctx) throws DBPoolingException,
			SQLException {
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
			stmt.executeUpdate();
		} finally {
			closeResources(null, stmt, closeWriteCon ? writeCon : null, false, ctx);
		}
	}

	private static final String SQL_LOOKUPFOLDER = "SELECT fuid FROM oxfolder_tree WHERE cid = ? AND parent = ? AND fname = ? AND module = ?";

	/**
	 * Checks for a duplicate folder in parental folder. A folder is treated as
	 * a duplicate if name and module are equal.
	 * 
	 * @return folder id or <tt>-1</tt> if none found
	 */
	public static final int lookUpFolder(final int parent, final String folderName, final int module,
			final Connection readConArg, final Context ctx) throws DBPoolingException, SQLException {
		return lookUpFolderOnUpdate(-1, parent, folderName, module, readConArg, ctx);
	}

	/**
	 * Checks for a duplicate folder in parental folder. A folder is treated as
	 * a duplicate if name and module are equal.
	 * 
	 * @param folderId
	 *            The ID of the folder whose is equal to given folder name (used
	 *            on update). Set this parameter to <code>-1</code> to ignore.
	 * @param parent
	 *            The parent folder whose subfolders shall be looked up
	 * @param folderName
	 *            The folder name to look for
	 * @param module
	 *            The folder module
	 * @param readConArg
	 *            A readable connection (may be <code>null</code>)
	 * @param ctx
	 *            The context
	 * @return The folder id or <tt>-1</tt> if none found
	 * @throws DBPoolingException
	 * @throws SQLException
	 */
	public static final int lookUpFolderOnUpdate(final int folderId, final int parent, final String folderName,
			final int module, final Connection readConArg, final Context ctx) throws DBPoolingException, SQLException {
		Connection readCon = readConArg;
		boolean closeReadCon = false;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			if (readCon == null) {
				readCon = DBPool.pickup(ctx);
				closeReadCon = true;
			}
			stmt = readCon.prepareStatement(folderId > 0 ? new StringBuilder(SQL_LOOKUPFOLDER).append(" AND fuid != ")
					.append(folderId).toString() : SQL_LOOKUPFOLDER);
			stmt.setInt(1, ctx.getContextId()); // cid
			stmt.setInt(2, parent); // parent
			stmt.setString(3, folderName); // fname
			stmt.setInt(4, module); // module
			rs = stmt.executeQuery();
			return rs.next() ? rs.getInt(1) : -1;
		} finally {
			closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
		}
	}

	private static final String SQL_EXISTS = "SELECT fuid FROM oxfolder_tree WHERE cid = ? AND fuid = ?";

	/**
	 * Checks if underlying storage contains a folder whose ID matches given ID
	 * 
	 * @return <tt>true</tt> if folder exists, otherwise <tt>false</tt>
	 */
	public static final boolean exists(final int folderId, final Connection readConArg, final Context ctx)
			throws DBPoolingException, SQLException {
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
			rs = stmt.executeQuery();
			return rs.next();
		} finally {
			closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
		}
	}

	private static final String SQL_GETSUBFLDIDS = "SELECT fuid FROM oxfolder_tree WHERE cid = ? AND parent = ?";

	/**
	 * Creates a <tt>java.util.List</tt> instance containing all subfolder IDs
	 * of given folder
	 * 
	 * @return a <tt>java.util.List</tt> instance containing all subfolder IDs
	 *         of given folder
	 */
	public static final List<Integer> getSubfolderIDs(final int folderId, final Connection readConArg, final Context ctx)
			throws DBPoolingException, SQLException {
		final List<Integer> retval = new ArrayList<Integer>();
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
			rs = stmt.executeQuery();
			while (rs.next()) {
				retval.add(Integer.valueOf(rs.getInt(1)));
			}
		} finally {
			closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
		}
		return retval;
	}

	private static final String SQL_UDTSUBFLDFLG = "UPDATE oxfolder_tree SET subfolder_flag = ?, changing_date = ? WHERE cid = ? AND fuid = ?";

	/**
	 * Updates the field 'subfolder_flag' of matching folder in underlying
	 * storage
	 */
	public static final void updateSubfolderFlag(final int folderId, final boolean hasSubfolders,
			final long lastModified, final Connection writeConArg, final Context ctx) throws DBPoolingException,
			SQLException {
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
			stmt.executeUpdate();
		} finally {
			closeResources(null, stmt, closeCon ? writeCon : null, false, ctx);
		}
	}

	private static final String SQL_NUMSUB = "SELECT COUNT(ot.fuid) FROM oxfolder_tree AS ot JOIN oxfolder_permissions AS op"
			+ " ON ot.fuid = op.fuid AND ot.cid = ? AND op.cid = ?"
			+ " WHERE op.permission_id IN #IDS# AND op.admin_flag > 0 AND ot.parent = ?";

	/**
	 * @return the number of subfolders of given folder which can be moved
	 *         according to user's permissions
	 */
	public static final int getNumOfMoveableSubfolders(final int folderId, final int userId, final int[] groups,
			final Connection readConArg, final Context ctx) throws DBPoolingException, SQLException {
		Connection readCon = readConArg;
		boolean closeReadCon = false;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			if (readCon == null) {
				readCon = DBPool.pickup(ctx);
				closeReadCon = true;
			}
			stmt = readCon.prepareStatement(SQL_NUMSUB.replaceFirst("#IDS#", StringCollection.getSqlInString(userId,
					groups)));
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, ctx.getContextId());
			stmt.setInt(3, folderId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} finally {
			closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
		}
		return 0;
	}

	private static final String SQL_INSERT_NEW_FOLDER = "INSERT INTO oxfolder_tree VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final String SQL_INSERT_NEW_PERMISSIONS = "INSERT INTO oxfolder_permissions (cid, fuid, permission_id, fp, orp, owp, odp, admin_flag, group_flag) VALUES (?,?,?,?,?,?,?,?,?)";

	private static final String SQL_UPDATE_PARENT_SUBFOLDER_FLAG = "UPDATE oxfolder_tree SET subfolder_flag = 1, changing_date = ? WHERE cid = ? AND fuid = ?";

	public static final void insertFolderSQL(final int newFolderID, final int userId, final FolderObject folderObj,
			final long creatingTime, final Context ctx, final Connection writeConArg) throws SQLException,
			DBPoolingException {
		insertFolderSQL(newFolderID, userId, folderObj, creatingTime, false, ctx, writeConArg);
	}

	public static final void insertDefaultFolderSQL(final int newFolderID, final int userId,
			final FolderObject folderObj, final long creatingTime, final Context ctx, final Connection writeConArg)
			throws SQLException, DBPoolingException {
		insertFolderSQL(newFolderID, userId, folderObj, creatingTime, true, ctx, writeConArg);
	}

	private static final void insertFolderSQL(final int newFolderID, final int userId, final FolderObject folderObj,
			final long creatingTime, final boolean acceptDefaultFlag, final Context ctx, final Connection writeConArg)
			throws SQLException, DBPoolingException {
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
				if (oclPerm.getEntity() == OCLPermission.ALL_GROUPS_AND_USERS
						&& oclPerm.getFolderPermission() > OCLPermission.NO_PERMISSIONS) {
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
					stmt.executeUpdate();
					stmt.close();
					stmt = null;
					/*
					 * Mark parent folder to have subfolders
					 */
					stmt = writeCon.prepareStatement(SQL_UPDATE_PARENT_SUBFOLDER_FLAG);
					stmt.setLong(1, creatingTime);
					stmt.setInt(2, ctx.getContextId());
					stmt.setInt(3, folderObj.getParentFolderID());
					stmt.executeUpdate();
					stmt.close();
					stmt = null;
					/*
					 * Insert permissions
					 */
					stmt = writeCon.prepareStatement(SQL_INSERT_NEW_PERMISSIONS);
					final int permissionsSize = folderObj.getPermissions().size();
					final Iterator<OCLPermission> iter = folderObj.getPermissions().iterator();
					for (int i = 0; i < permissionsSize; i++) {
						final OCLPermission ocl = iter.next();
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
					stmt.executeBatch();
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
			} catch (SQLException e) {
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

	private static final String SQL_UPDATE_WITH_FOLDERNAME = "UPDATE oxfolder_tree SET fname = ?, changing_date = ?, changed_from = ?, permission_flag = ? WHERE cid = ? AND fuid = ?";

	private static final String SQL_UPDATE_WITHOUT_FOLDERNAME = "UPDATE oxfolder_tree SET changing_date = ?, changed_from = ?, permission_flag = ? WHERE cid = ? AND fuid = ?";

	private static final String SQL_DELETE_EXISTING_PERMISSIONS = "DELETE FROM oxfolder_permissions WHERE cid = ? AND fuid = ?";

	public final static void updateFolderSQL(final int userId, final FolderObject folderObj, final long lastModified,
			final Context ctx, final Connection writeConArg) throws SQLException, DBPoolingException {
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
				if (oclPerm.getEntity() == OCLPermission.ALL_GROUPS_AND_USERS
						&& oclPerm.getFolderPermission() > OCLPermission.NO_PERMISSIONS) {
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
				if (folderObj.containsFolderName()) {
					stmt = writeCon.prepareStatement(SQL_UPDATE_WITH_FOLDERNAME);
					stmt.setString(1, folderObj.getFolderName());
					stmt.setLong(2, lastModified);
					stmt.setInt(3, userId);
					stmt.setInt(4, permissionFlag);
					stmt.setInt(5, ctx.getContextId());
					stmt.setInt(6, folderObj.getObjectID());
					stmt.executeUpdate();
					stmt.close();
					stmt = null;
				} else {
					stmt = writeCon.prepareStatement(SQL_UPDATE_WITHOUT_FOLDERNAME);
					stmt.setLong(1, lastModified);
					stmt.setInt(2, userId);
					stmt.setInt(3, permissionFlag);
					stmt.setInt(4, ctx.getContextId());
					stmt.setInt(5, folderObj.getObjectID());
					stmt.executeUpdate();
					stmt.close();
					stmt = null;
				}
				/*
				 * Delete old permissions
				 */
				stmt = writeCon.prepareStatement(SQL_DELETE_EXISTING_PERMISSIONS);
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, folderObj.getObjectID());
				stmt.executeUpdate();
				stmt.close();
				stmt = null;
				/*
				 * Insert new permissions
				 */
				stmt = writeCon.prepareStatement(SQL_INSERT_NEW_PERMISSIONS);
				final int permissionsSize = folderObj.getPermissions().size();
				final Iterator<OCLPermission> iter = folderObj.getPermissions().iterator();
				for (int i = 0; i < permissionsSize; i++) {
					final OCLPermission oclPerm = iter.next();
					stmt.setInt(1, ctx.getContextId());
					stmt.setInt(2, folderObj.getObjectID());
					stmt.setInt(3, oclPerm.getEntity());
					stmt.setInt(4, oclPerm.getFolderPermission());
					stmt.setInt(5, oclPerm.getReadPermission());
					stmt.setInt(6, oclPerm.getWritePermission());
					stmt.setInt(7, oclPerm.getDeletePermission());
					stmt.setInt(8, oclPerm.isFolderAdmin() ? 1 : 0);
					stmt.setInt(9, oclPerm.isGroupPermission() ? 1 : 0);
					stmt.addBatch();
				}
				stmt.executeBatch();
				stmt.close();
				stmt = null;
			} catch (SQLException e) {
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

	public static final void moveFolderSQL(final int userId, final FolderObject src, final FolderObject dest,
			final long lastModified, final Context ctx, final Connection readConArg, final Connection writeConArg)
			throws SQLException, DBPoolingException {
		Connection writeCon = writeConArg;
		boolean closeWriteCon = false;
		Connection readCon = readConArg;
		boolean closeCon = false;
		PreparedStatement pst = null;
		ResultSet subFolderRS = null;
		try {
			if (readCon == null) {
				readCon = DBPool.pickup(ctx);
				closeCon = true;
			}
			if (writeCon == null) {
				writeCon = DBPool.pickupWriteable(ctx);
				closeWriteCon = true;
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
				pst.executeUpdate();
				pst.close();
				pst = null;
				/*
				 * Set target folder's/source parent folder's subfolder flag
				 */
				pst = readCon.prepareStatement(SQL_MOVE_SELECT);
				pst.setInt(1, ctx.getContextId());
				pst.setInt(2, src.getParentFolderID());
				subFolderRS = pst.executeQuery();
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
				pst.executeBatch();
				pst.close();
				pst = null;
			} catch (SQLException se) {
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
			closeResources(subFolderRS, pst, closeCon ? readCon : null, true, ctx);
			if (closeWriteCon && writeCon != null) {
				DBPool.closeWriterSilent(ctx, writeCon);
			}
		}
	}

	private static final String SQL_RENAME_UPDATE = "UPDATE oxfolder_tree SET fname = ?, changing_date = ?, changed_from = ? where cid = ? AND fuid = ?";

	public static final void renameFolderSQL(final int userId, final FolderObject folderObj, final long lastModified,
			final Context ctx, final Connection writeConArg) throws SQLException, DBPoolingException {
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
				pst.executeUpdate();
				pst.close();
				pst = null;
			} catch (SQLException sqle) {
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

	public static final void delWorkingOXFolder(final int folderId, final int userId, final long lastModified,
			final Context ctx, final Connection writeConArg) throws SQLException, DBPoolingException {
		delOXFolder(folderId, userId, lastModified, true, true, ctx, writeConArg);
	}

	private static final String SQL_DELETE_INSERT_OT = "INSERT INTO del_oxfolder_tree SELECT * FROM oxfolder_tree WHERE cid = ? AND fuid = ?";

	private static final String SQL_DELETE_INSERT_OP = "INSERT INTO del_oxfolder_permissions SELECT * FROM oxfolder_permissions WHERE cid = ? AND fuid = ?";

	private static final String SQL_DELETE_DELETE_SF = "DELETE FROM oxfolder_specialfolders WHERE cid = ? AND fuid = ?";

	private static final String SQL_DELETE_DELETE = "DELETE FROM #TABLE# WHERE cid = ? AND fuid = ?";

	private static final String SQL_DELETE_UPDATE = "UPDATE del_oxfolder_tree SET changing_date = ?, changed_from = ? WHERE cid = ? AND fuid = ?";

	/**
	 * Deletes a folder entry - and its corresponding permission entries as well -
	 * from underlying storage. <code>deleteWorking</code> determines whether
	 * working or backup tables are affected by delete operation.
	 * <code>createBackup</code> specifies if backup entries are going to be
	 * created and is only allowed if <code>deleteWorking</code> is set to
	 * <code>true</code>.
	 */
	private static final void delOXFolder(final int folderId, final int userId, final long lastModified,
			final boolean deleteWorking, final boolean createBackup, final Context ctx, final Connection writeConArg)
			throws SQLException, DBPoolingException {
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
				 * Copy backup entries into del_oxfolder_tree and
				 * del_oxfolder_permissions
				 */
				stmt = writeCon.prepareStatement(SQL_DELETE_INSERT_OT);
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, folderId);
				stmt.executeUpdate();
				stmt.close();
				stmt = null;
				stmt = writeCon.prepareStatement(SQL_DELETE_INSERT_OP);
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, folderId);
				stmt.executeUpdate();
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
				stmt.executeUpdate();
				stmt.close();
				stmt = null;
			}
			/*
			 * Delete from permission table
			 */
			stmt = writeCon.prepareStatement(SQL_DELETE_DELETE.replaceFirst("#TABLE#", permTable));
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, folderId);
			stmt.executeUpdate();
			stmt.close();
			stmt = null;
			/*
			 * Delete from folder table
			 */
			stmt = writeCon.prepareStatement(SQL_DELETE_DELETE.replaceFirst("#TABLE#", folderTable));
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, folderId);
			stmt.executeUpdate();
			stmt.close();
			stmt = null;
			if (backup) {
				/*
				 * Update last modifed timestamp of entries in backup tables
				 */
				stmt = writeCon.prepareStatement(SQL_DELETE_UPDATE);
				stmt.setLong(1, lastModified);
				stmt.setInt(2, userId);
				stmt.setInt(3, ctx.getContextId());
				stmt.setInt(4, folderId);
				stmt.executeUpdate();
				stmt.close();
				stmt = null;
			}
			/*
			 * Commit
			 */
			if (isAuto) {
				writeCon.commit();
			}
		} catch (SQLException e) {
			if (isAuto) {
				writeCon.rollback();
			}
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
			if (isAuto) {
				writeCon.setAutoCommit(true);
			}
			if (closeWriteCon) {
				DBPool.closeWriterSilent(ctx, writeCon);
			}
		}
	}

	private static final Lock NEXTSERIAL_LOCK = new ReentrantLock();

	/**
	 * Fetches a unique id from underlying storage. NOTE: This method assumes
	 * that given writeable connection is set to auto-commit! In any case the
	 * <code>commit()</code> will be invoked, so any surrounding BEGIN-COMMIT
	 * mechanisms will be canceled.
	 * 
	 * @return a unique id from underlying storage
	 */
	public static int getNextSerial(final Context ctx, final Connection callWriteConArg) throws SQLException,
			OXException {
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
						 * Commit connection to ensure an unique ID is going to
						 * be returned
						 */
						callWriteCon.commit();
					}
					final int id = IDGenerator.getId(ctx, Types.FOLDER, callWriteCon);
					if (isAuto) {
						callWriteCon.commit(); // COMMIT
						callWriteCon.setAutoCommit(true);
					} else {
						/*
						 * Commit connection to ensure an unique ID is going to
						 * be returned
						 */
						callWriteCon.commit();
					}
					return id;
				} finally {
					if (closeCon && callWriteCon != null) {
						DBPool.pushWrite(ctx, callWriteCon);
					}
				}
			} catch (DBPoolingException e) {
				if (isAuto && callWriteCon != null) {
					callWriteCon.rollback(); // ROLLBACK
					callWriteCon.setAutoCommit(true);
				}
				throw new OXFolderException(FolderCode.DBPOOLING_ERROR, Integer.valueOf(ctx.getContextId()));
			}
		} finally {
			NEXTSERIAL_LOCK.unlock();
		}
	}

	public static void hardDeleteOXFolder(final int folderId, final Context ctx, final Connection writeConArg)
			throws SQLException, DBPoolingException {
		Connection writeCon = writeConArg;
		boolean closeWrite = false;
		if (writeCon == null) {
			try {
				writeCon = DBPool.pickupWriteable(ctx);
			} catch (DBPoolingException e) {
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
			stmt.addBatch(new StringBuilder("DELETE FROM oxfolder_specialfolders WHERE cid = ").append(
					ctx.getContextId()).append(andClause).append(folderId).toString());

			stmt.addBatch(new StringBuilder("DELETE FROM oxfolder_permissions WHERE cid = ").append(ctx.getContextId())
					.append(andClause).append(folderId).toString());

			stmt.addBatch(new StringBuilder("DELETE FROM oxfolder_tree WHERE cid = ").append(ctx.getContextId())
					.append(andClause).append(folderId).toString());

			stmt.executeBatch();

			if (isAuto) {
				writeCon.commit();
			}
		} catch (SQLException e) {
			if (isAuto) {
				writeCon.rollback();
			}
			throw e;
		} finally {
			if (isAuto) {
				writeCon.setAutoCommit(true);
			}
			closeResources(null, stmt, closeWrite ? writeCon : null, false, ctx);
		}
	}

	/*
	 * -------------- Helper methods for OXFolderDeleteListener (User removal)
	 * --------------
	 */

	private static final String SQL_GET_CONTEXT_MAILADMIN = "SELECT user FROM user_setting_admin WHERE cid = ?";

	public static final int getContextMailAdmin(final Connection readConArg, final Context ctx)
			throws DBPoolingException, SQLException {
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
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
			return -1;
		} finally {
			closeResources(rs, stmt, createReadCon ? readCon : null, true, ctx);
		}
	}

	private static final String SQL_SEL_PERMS = "SELECT ot.fuid, ot.type FROM #PERM# AS op JOIN #FOLDER# AS ot ON op.fuid = ot.fuid AND op.cid = ? AND ot.cid = ? WHERE op.permission_id IN #IDS# GROUP BY ot.fuid";

	private static final String TMPL_FOLDER_TABLE = "#FOLDER#";

	private static final String TMPL_PERM_TABLE = "#PERM#";

	private static final String TMPL_IDS = "#IDS#";

	/**
	 * Deletes all permissions assigned to context's mail admin from given
	 * permission table.
	 */
	public static final void handleMailAdminPermissions(final int mailAdmin, final String folderTable,
			final String permTable, final Connection readConArg, final Connection writeConArg, final Context ctx)
			throws DBPoolingException, SQLException {
		handleEntityPermissions(mailAdmin, null, -1L, folderTable, permTable, readConArg, writeConArg, ctx);
	}

	/**
	 * Handles entity' permissions located in given permission table. If
	 * permission is associated with a private folder, it is going to be
	 * deleted. Otherwise the permission is reassigned to mailadmin.
	 */
	public static final void handleEntityPermissions(final int entity, final int mailAdmin, final long lastModified,
			final String folderTable, final String permTable, final Connection readConArg,
			final Connection writeConArg, final Context ctx) throws DBPoolingException, SQLException {
		handleEntityPermissions(entity, Integer.valueOf(mailAdmin), lastModified, folderTable, permTable, readConArg,
				writeConArg, ctx);
	}

	private static final void handleEntityPermissions(final int entity, final Integer mailAdmin,
			final long lastModified, final String folderTable, final String permTable, final Connection readConArg,
			final Connection writeConArg, final Context ctx) throws DBPoolingException, SQLException {
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
				permissionsIDs = new StringBuilder().append('(').append(entity).append(',').append(
						OCLPermission.ALL_GROUPS_AND_USERS).append(')').toString();
			} else {
				permissionsIDs = new StringBuilder().append('(').append(entity).append(')').toString();
			}
			stmt = readCon.prepareStatement(SQL_SEL_PERMS.replaceFirst(TMPL_PERM_TABLE, permTable).replaceFirst(
					TMPL_FOLDER_TABLE, folderTable).replaceFirst(TMPL_IDS, permissionsIDs));
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, ctx.getContextId());
			rs = stmt.executeQuery();
			final Set<Integer> deletePerms = new HashSet<Integer>();
			final Set<Integer> reassignPerms = new HashSet<Integer>();
			while (rs.next()) {
				final int fuid = rs.getInt(1);
				final int type = rs.getInt(2);
				if (isMailAdmin || markForDeletion(type)) {
					deletePerms.add(Integer.valueOf(fuid));
				} else {
					reassignPerms.add(Integer.valueOf(fuid));
				}
				if (FolderCacheManager.isInitialized()) {
					/*
					 * Invalidate cache
					 */
					try {
						FolderCacheManager.getInstance().removeFolderObject(fuid, ctx);
					} catch (FolderCacheNotEnabledException e) {
						LOG.error(e.getMessage(), e);
					} catch (OXException e) {
						LOG.error(e.getMessage(), e);
					}
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
				reassignPermissions(reassignPerms, entity, mailAdmin.intValue(), lastModified, folderTable, permTable,
						readCon, writeConArg, ctx);
			}
		} finally {
			closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
		}
	}

	private static final String SQL_DELETE_PERMS = "DELETE FROM #PERM# WHERE cid = ? AND fuid = ? AND permission_id = ?";

	private static final void deletePermissions(final Set<Integer> deletePerms, final int entity,
			final String permTable, final Connection writeConArg, final Context ctx) throws DBPoolingException,
			SQLException {
		final int size = deletePerms.size();
		final Iterator<Integer> iter = deletePerms.iterator();
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
				stmt.setInt(2, iter.next().intValue());
				stmt.setInt(3, entity);
				stmt.addBatch();
			}
			stmt.executeBatch();
		} finally {
			closeResources(null, stmt, closeWrite ? wc : null, false, ctx);
		}
	}

	private static final String SQL_REASSIGN_PERMS = "UPDATE #PERM# SET permission_id = ? WHERE cid = ? AND fuid = ? AND permission_id = ?";

	private static final String SQL_REASSIGN_UPDATE_TIMESTAMP = "UPDATE #FOLDER# SET changed_from = ?, changing_date = ? WHERE cid = ? AND fuid = ?";

	private static final void reassignPermissions(final Set<Integer> reassignPerms, final int entity,
			final int mailAdmin, final long lastModified, final String folderTable, final String permTable,
			final Connection readConArg, final Connection writeConArg, final Context ctx) throws DBPoolingException,
			SQLException {
		final int size = reassignPerms.size();
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
			// wc.prepareStatement(SQL_REASSIGN_PERMS.replaceFirst(TMPL_PERM_TABLE,
			// permTable));
			Iterator<Integer> iter = reassignPerms.iterator();
			Next: for (int i = 0; i < size; i++) {
				final int fuid = iter.next().intValue();
				/*
				 * Check if admin already holds permission on current folder
				 */
				stmt = rc.prepareStatement(SQL_REASSIGN_SEL_PERM.replaceFirst(TMPL_PERM_TABLE, permTable));
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, mailAdmin);
				stmt.setInt(3, fuid);
				rs = stmt.executeQuery();
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
						final OCLPermission mergedPerm = getMergedPermission(entity, mailAdmin, fuid, permTable,
								readConArg, ctx);
						deleteSingleEntityPermission(entity, fuid, permTable, wc, ctx);
						updateSingleEntityPermission(mergedPerm, mailAdmin, fuid, permTable, wc, ctx);
					} catch (Exception e) {
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
						stmt.executeUpdate();
					} catch (SQLException e) {
						LOG.error(e.getMessage(), e);
						continue Next;
					} finally {
						stmt.close();
						stmt = null;
					}
				}
			}
			// stmt.executeBatch();
			// stmt.close();
			// stmt = null;
			stmt = wc.prepareStatement(SQL_REASSIGN_UPDATE_TIMESTAMP.replaceFirst(TMPL_FOLDER_TABLE, folderTable));
			iter = reassignPerms.iterator();
			for (int i = 0; i < size; i++) {
				stmt.setInt(1, mailAdmin);
				stmt.setLong(2, lastModified);
				stmt.setInt(3, ctx.getContextId());
				stmt.setInt(4, iter.next().intValue());
				stmt.addBatch();
			}
			stmt.executeBatch();
		} finally {
			closeResources(rs, stmt, closeWrite ? wc : null, false, ctx);
			if (closeRead && rc != null) {
				DBPool.closeReaderSilent(ctx, rc);
			}
		}
	}

	private static final String SQL_REASSIGN_DEL_PERM = "DELETE FROM #PERM# WHERE cid = ? AND permission_id = ? AND fuid = ?";

	private static final void deleteSingleEntityPermission(final int entity, final int fuid, final String permTable,
			final Connection writeConArg, final Context ctx) throws DBPoolingException, SQLException {
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
			stmt.executeUpdate();
		} finally {
			closeResources(null, stmt, close ? wc : null, false, ctx);
		}
	}

	private static final String SQL_REASSIGN_UPDATE_PERM = "UPDATE #PERM# SET fp = ?, orp = ?, owp = ?, odp = ?, admin_flag = ? WHERE cid = ? AND permission_id = ? AND fuid = ?";

	private static final void updateSingleEntityPermission(final OCLPermission mergedPerm, final int mailAdmin,
			final int fuid, final String permTable, final Connection writeConArg, final Context ctx)
			throws DBPoolingException, SQLException {
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
			stmt.setInt(6, ctx.getContextId());
			stmt.setInt(7, mailAdmin);
			stmt.setInt(8, fuid);
			stmt.executeUpdate();
		} finally {
			closeResources(null, stmt, close ? wc : null, false, ctx);
		}
	}

	private static final String SQL_REASSIGN_SEL_PERM = "SELECT fp, orp, owp, odp, admin_flag FROM #PERM# WHERE cid = ? AND permission_id = ? AND fuid = ?";

	private static final OCLPermission getMergedPermission(final int entity, final int mailAdmin, final int fuid,
			final String permTable, final Connection readConArg, final Context ctx) throws SQLException,
			DBPoolingException {
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
			innerRs = innerStmt.executeQuery();
			if (!innerRs.next()) {
				return null;
			}
			final OCLPermission mailAdminPerm = new OCLPermission(mailAdmin, fuid);
			mailAdminPerm.setAllPermission(innerRs.getInt(1), innerRs.getInt(2), innerRs.getInt(3), innerRs.getInt(4));
			mailAdminPerm.setFolderAdmin(innerRs.getInt(5) > 0);
			innerRs.close();
			innerStmt.close();
			innerStmt = readCon.prepareStatement(SQL_REASSIGN_SEL_PERM.replaceFirst(TMPL_PERM_TABLE, permTable));
			innerStmt.setInt(1, ctx.getContextId());
			innerStmt.setInt(2, entity);
			innerStmt.setInt(3, fuid);
			innerRs = innerStmt.executeQuery();
			if (!innerRs.next()) {
				return mailAdminPerm;
			}
			final OCLPermission entityPerm = new OCLPermission(entity, fuid);
			entityPerm.setAllPermission(innerRs.getInt(1), innerRs.getInt(2), innerRs.getInt(3), innerRs.getInt(4));
			entityPerm.setFolderAdmin(innerRs.getInt(5) > 0);
			/*
			 * Merge
			 */
			final OCLPermission mergedPerm = new OCLPermission(mailAdmin, fuid);
			mergedPerm.setFolderPermission(Math.max(mailAdminPerm.getFolderPermission(), entityPerm
					.getFolderPermission()));
			mergedPerm.setReadObjectPermission(Math.max(mailAdminPerm.getReadPermission(), entityPerm
					.getReadPermission()));
			mergedPerm.setWriteObjectPermission(Math.max(mailAdminPerm.getWritePermission(), entityPerm
					.getWritePermission()));
			mergedPerm.setDeleteObjectPermission(Math.max(mailAdminPerm.getDeletePermission(), entityPerm
					.getDeletePermission()));
			mergedPerm.setFolderAdmin(mailAdminPerm.isFolderAdmin() || entityPerm.isFolderAdmin());
			return mergedPerm;
		} finally {
			closeResources(innerRs, innerStmt, closeRead ? readCon : null, true, ctx);
		}
	}

	// ------------------- DELETE FOLDERS --------------------------

	public static final void handleMailAdminFolders(final int mailAdmin, final String folderTable,
			final String permTable, final Connection readConArg, final Connection writeConArg, final Context ctx)
			throws DBPoolingException, SQLException {
		handleEntityFolders(mailAdmin, null, -1L, folderTable, permTable, readConArg, writeConArg, ctx);
	}

	public static final void handleEntityFolders(final int entity, final int mailAdmin, final long lastModified,
			final String folderTable, final String permTable, final Connection readConArg,
			final Connection writeConArg, final Context ctx) throws DBPoolingException, SQLException {
		handleEntityFolders(entity, Integer.valueOf(mailAdmin), lastModified, folderTable, permTable, readConArg,
				writeConArg, ctx);
	}

	private static final String SQL_SEL_FOLDERS = "SELECT ot.fuid, ot.type FROM #FOLDER# AS ot WHERE ot.cid = ? AND ot.created_from = ?";

	private static final String SQL_SEL_FOLDERS2 = "SELECT ot.fuid FROM #FOLDER# AS ot WHERE ot.cid = ? AND ot.changed_from = ?";

	private static final void handleEntityFolders(final int entity, final Integer mailAdmin, final long lastModified,
			final String folderTable, final String permTable, final Connection readConArg,
			final Connection writeConArg, final Context ctx) throws DBPoolingException, SQLException {
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
			rs = stmt.executeQuery();
			Set<Integer> deleteFolders = new HashSet<Integer>();
			Set<Integer> reassignFolders = new HashSet<Integer>();
			while (rs.next()) {
				final int fuid = rs.getInt(1);
				final int type = rs.getInt(2);
				if (isMailAdmin || markForDeletion(type)) {
					deleteFolders.add(Integer.valueOf(fuid));
				} else {
					reassignFolders.add(Integer.valueOf(fuid));
				}
				if (FolderCacheManager.isInitialized()) {
					/*
					 * Invalidate cache
					 */
					try {
						FolderCacheManager.getInstance().removeFolderObject(fuid, ctx);
					} catch (FolderCacheNotEnabledException e) {
						LOG.error(e.getMessage(), e);
					} catch (OXException e) {
						LOG.error(e.getMessage(), e);
					}
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
				reassignFolders(reassignFolders, entity, mailAdmin.intValue(), lastModified, folderTable, writeConArg,
						ctx);
			}
			/*
			 * Check column "changed_from"
			 */
			stmt = readCon.prepareStatement(SQL_SEL_FOLDERS2.replaceFirst(TMPL_FOLDER_TABLE, folderTable));
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, entity);
			rs = stmt.executeQuery();
			deleteFolders = new HashSet<Integer>();
			reassignFolders = new HashSet<Integer>();
			while (rs.next()) {
				final int fuid = rs.getInt(1);
				if (isMailAdmin) {
					deleteFolders.add(Integer.valueOf(fuid));
				} else {
					reassignFolders.add(Integer.valueOf(fuid));
				}
				if (FolderCacheManager.isInitialized()) {
					/*
					 * Invalidate cache
					 */
					try {
						FolderCacheManager.getInstance().removeFolderObject(fuid, ctx);
					} catch (FolderCacheNotEnabledException e) {
						LOG.error(e.getMessage(), e);
					} catch (OXException e) {
						LOG.error(e.getMessage(), e);
					}
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
				reassignFolders(reassignFolders, entity, mailAdmin.intValue(), lastModified, folderTable, writeConArg,
						ctx);
			}
		} finally {
			closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
		}
	}

	private static final String SQL_DELETE_FOLDER = "DELETE FROM #FOLDER# WHERE cid = ? AND fuid = ?";

	private static final void deleteFolders(final Set<Integer> deleteFolders, final String folderTable,
			final String permTable, final Connection writeConArg, final Context ctx) throws DBPoolingException,
			SQLException {
		final int size = deleteFolders.size();
		Connection wc = writeConArg;
		boolean closeWrite = false;
		PreparedStatement stmt = null;
		try {
			/*
			 * Delete folder's permissions if any exist
			 */
			Iterator<Integer> iter = deleteFolders.iterator();
			for (int i = 0; i < size; i++) {
				final int fuid = iter.next().intValue();
				checkFolderPermissions(fuid, permTable, writeConArg, ctx);
			}
			/*
			 * Delete references to table 'oxfolder_specialfolders'
			 */
			iter = deleteFolders.iterator();
			for (int i = 0; i < size; i++) {
				final int fuid = iter.next().intValue();
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
				final int fuid = iter.next().intValue();
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, fuid);
				stmt.addBatch();
			}
			stmt.executeBatch();
		} finally {
			closeResources(null, stmt, closeWrite ? wc : null, false, ctx);
		}
	}

	private static final String SQL_DELETE_SPECIAL_REFS = "DELETE FROM oxfolder_specialfolders WHERE cid = ? AND fuid = ?";

	private static final void deleteSpecialfoldersRefs(final int fuid, final Connection writeConArg, final Context ctx)
			throws DBPoolingException, SQLException {
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
			stmt.executeUpdate();
		} finally {
			closeResources(null, stmt, closeWrite ? wc : null, false, ctx);
		}
	}

	private static final String SQL_DELETE_FOLDER_PERMS = "DELETE FROM #PERM# WHERE cid = ? AND fuid = ?";

	private static final void checkFolderPermissions(final int fuid, final String permTable,
			final Connection writeConArg, final Context ctx) throws DBPoolingException, SQLException {
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
			stmt.executeUpdate();
		} finally {
			closeResources(null, stmt, closeWrite ? wc : null, false, ctx);
		}
	}

	private static final String SQL_REASSIGN_FOLDERS = "UPDATE #FOLDER# SET created_from = ?, changed_from = ?, changing_date = ?, default_flag = 0 WHERE cid = ? AND fuid = ?";

	private static final String SQL_REASSIGN_FOLDERS_WITH_NAME = "UPDATE #FOLDER# SET created_from = ?, changed_from = ?, changing_date = ?, default_flag = 0, fname = ? WHERE cid = ? AND fuid = ?";

	private static final void reassignFolders(final Set<Integer> reassignFolders, final int entity,
			final int mailAdmin, final long lastModified, final String folderTable, final Connection writeConArg,
			final Context ctx) throws DBPoolingException, SQLException {
		Connection wc = writeConArg;
		boolean closeWrite = false;
		PreparedStatement stmt = null;
		try {
			if (wc == null) {
				wc = DBPool.pickupWriteable(ctx);
				closeWrite = true;
			}
			int size = reassignFolders.size();
			Iterator<Integer> iter = reassignFolders.iterator();
			{
				/*
				 * Special handling for default infostore folder
				 */
				boolean found = false;
				for (int i = 0; i < size && !found; i++) {
					final int fuid = iter.next().intValue();
					final String fname;
					if ((fname = isDefaultInfostoreFolder(fuid, entity, folderTable, wc, ctx)) != null) {
						iter.remove();
						size--;
						stmt = wc.prepareStatement(SQL_REASSIGN_FOLDERS_WITH_NAME.replaceFirst(TMPL_FOLDER_TABLE,
								folderTable));
						stmt.setInt(1, mailAdmin);
						stmt.setInt(2, mailAdmin);
						stmt.setLong(3, lastModified);
						stmt.setString(4, new StringBuilder(fname).append(fuid).toString());
						stmt.setInt(5, ctx.getContextId());
						stmt.setInt(6, fuid);
						stmt.executeUpdate();
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
				stmt.setInt(5, iter.next().intValue());
				stmt.addBatch();
			}
			stmt.executeBatch();
		} finally {
			closeResources(null, stmt, closeWrite ? wc : null, false, ctx);
		}
	}

	private static final String SQL_DEF_INF = "SELECT fname FROM #FOLDER# WHERE cid = ? AND fuid = ? AND module = ? AND created_from = ? AND default_flag = 1";

	/**
	 * @return The entity's default infostore folder's name if <code>true</code>;
	 *         otherwise <code>null</code>
	 */
	private static String isDefaultInfostoreFolder(final int fuid, final int entity, final String folderTable,
			final Connection con, final Context ctx) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.prepareStatement(SQL_DEF_INF.replaceFirst(TMPL_FOLDER_TABLE, folderTable));
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, fuid);
			stmt.setInt(3, FolderObject.INFOSTORE);
			stmt.setInt(4, entity);
			rs = stmt.executeQuery();
			return rs.next() ? rs.getString(1) : null;
		} finally {
			closeResources(rs, stmt, null, true, ctx);
		}
	}

	/**
	 * @return <code>true</code> if folder type is set to private,
	 *         <code>false</code> otherwise
	 */
	private static final boolean markForDeletion(final int type) {
		return (type == FolderObject.PRIVATE); // || (type ==
		// FolderObject.PUBLIC && module
		// == FolderObject.INFOSTORE &&
		// defaultFlag);
	}

}
