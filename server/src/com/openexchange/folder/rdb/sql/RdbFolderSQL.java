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

package com.openexchange.folder.rdb.sql;

import static com.openexchange.tools.sql.DBUtils.closeResources;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.openexchange.database.Database;
import com.openexchange.folder.FolderException;
import com.openexchange.folder.FolderPermissionStatus;
import com.openexchange.folder.rdb.RdbFolder;
import com.openexchange.folder.rdb.RdbFolderID;
import com.openexchange.folder.rdb.RdbFolderModule;
import com.openexchange.folder.rdb.RdbFolderPermission;
import com.openexchange.folder.rdb.RdbFolderType;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;

/**
 * {@link RdbFolderSQL}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class RdbFolderSQL {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(RdbFolderSQL.class);

	/**
	 * Initializes a new {@link RdbFolderSQL}
	 */
	private RdbFolderSQL() {
		super();
	}

	private static final String SQL_UDTSUBFLDFLG = "UPDATE oxfolder_tree SET subfolder_flag = ?, changing_date = ? WHERE cid = ? AND fuid = ?";

	/**
	 * Updates the field 'subfolder_flag' of matching folder in underlying
	 * storage. The last-modified timestamp is updated, too.
	 * 
	 * @param folderId
	 *            The folder ID
	 * @param hasSubfolders
	 *            The subfolder flag to set
	 * @param lastModified
	 *            The last-modified timestamp to set
	 * @param ctx
	 *            The context
	 * @throws DBPoolingException
	 *             If a writable connection cannot be obtained from/put back
	 *             into pool
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static void updateSubfolderFlag(final int folderId, final boolean hasSubfolders, final long lastModified,
			final Context ctx) throws DBPoolingException, SQLException {
		final Connection writeCon = Database.get(ctx, true);
		try {
			updateSubfolderFlag(folderId, hasSubfolders, lastModified, ctx, writeCon);
		} finally {
			Database.back(ctx, true, writeCon);
		}
	}

	/**
	 * Updates the field 'subfolder_flag' of matching folder in underlying
	 * storage. The last-modified timestamp is updated, too.
	 * 
	 * @param folderId
	 *            The folder ID
	 * @param hasSubfolders
	 *            The subfolder flag to set
	 * @param lastModified
	 *            The last-modified timestamp to set
	 * @param ctx
	 *            The context
	 * @param writeCon
	 *            A writable connection
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static void updateSubfolderFlag(final int folderId, final boolean hasSubfolders, final long lastModified,
			final Context ctx, final Connection writeCon) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = writeCon.prepareStatement(SQL_UDTSUBFLDFLG);
			stmt.setInt(1, hasSubfolders ? 1 : 0);
			stmt.setLong(2, lastModified);
			stmt.setInt(3, ctx.getContextId());
			stmt.setInt(4, folderId);
			stmt.executeUpdate();
		} finally {
			closeSQLStuff(null, stmt);
		}
	}

	/**
	 * Checks existence of specified folder in given context
	 * 
	 * @param fuid
	 *            The folder ID
	 * @param ctx
	 *            The context
	 * @return <code>true</code> if exists; otherwise <code>false</code>
	 * @throws DBPoolingException
	 *             If a readable connection cannot be fetched from connection
	 *             pool
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static boolean exists(final int fuid, final Context ctx) throws DBPoolingException, SQLException {
		Connection readCon = null;
		try {
			readCon = Database.get(ctx, false);
			return exists(fuid, ctx, readCon);
		} finally {
			closeResources(null, null, readCon, true, ctx);
		}
	}

	/**
	 * Checks existence of specified folder in given context
	 * 
	 * @param fuid
	 *            The folder ID
	 * @param ctx
	 *            The context
	 * @param readCon
	 *            A readable connection
	 * @return <code>true</code> if exists; otherwise <code>false</code>
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static boolean exists(final int fuid, final Context ctx, final Connection readCon) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = readCon.prepareStatement(SQL_LOAD_FLD);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, fuid);
			rs = stmt.executeQuery();
			return rs.next();
		} finally {
			closeSQLStuff(rs, stmt);
		}
	}

	private static final String SQL_LOAD_FLD = "SELECT parent, fname, module, type, creating_date, created_from, changing_date, changed_from, permission_flag, subfolder_flag, default_flag FROM oxfolder_tree WHERE cid = ? AND fuid = ?";

	private static final String SQL_LOAD_FLD_BAK = "SELECT parent, fname, module, type, creating_date, created_from, changing_date, changed_from, permission_flag, subfolder_flag, default_flag FROM del_oxfolder_tree WHERE cid = ? AND fuid = ?";

	private static final String SQL_LOAD_PERMS = "SELECT permission_id, fp, orp, owp, odp, admin_flag, group_flag FROM oxfolder_permissions WHERE cid = ? AND fuid = ?";

	private static final String SQL_LOAD_PERMS_BAK = "SELECT permission_id, fp, orp, owp, odp, admin_flag, group_flag FROM del_oxfolder_permissions WHERE cid = ? AND fuid = ?";

	/**
	 * Loads a folder object from table
	 * 
	 * @param fuid
	 *            The folder ID
	 * @param ctx
	 *            The context
	 * @return The folder object loaded from database
	 * @throws DBPoolingException
	 *             If a readable connection cannot be fetched from connection
	 *             pool
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static RdbFolder loadFolder(final int fuid, final Context ctx) throws DBPoolingException, SQLException {
		Connection readCon = null;
		try {
			readCon = Database.get(ctx, false);
			return loadFolder(fuid, ctx, readCon);
		} finally {
			closeResources(null, null, readCon, true, ctx);
		}
	}

	/**
	 * Loads a folder object from table
	 * 
	 * @param fuid
	 *            The folder ID
	 * @param ctx
	 *            The context
	 * @param readCon
	 *            A readable connection
	 * @return The folder object loaded from database
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static RdbFolder loadFolder(final int fuid, final Context ctx, final Connection readCon) throws SQLException {
		return loadFolder0(fuid, ctx, readCon, false);
	}

	/**
	 * Loads a folder object from backup table
	 * 
	 * @param fuid
	 *            The folder ID
	 * @param ctx
	 *            The context
	 * @return The backup folder object loaded from database
	 * @throws DBPoolingException
	 *             If a readable connection cannot be fetched from connection
	 *             pool
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static RdbFolder loadBackupFolder(final int fuid, final Context ctx) throws DBPoolingException, SQLException {
		Connection readCon = null;
		try {
			readCon = Database.get(ctx, false);
			return loadBackupFolder(fuid, ctx, readCon);
		} finally {
			closeResources(null, null, readCon, true, ctx);
		}
	}

	/**
	 * Loads a folder object from backup table
	 * 
	 * @param fuid
	 *            The folder ID
	 * @param ctx
	 *            The context
	 * @param readCon
	 *            A readable connection
	 * @return The backup folder object loaded from database
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static RdbFolder loadBackupFolder(final int fuid, final Context ctx, final Connection readCon)
			throws SQLException {
		return loadFolder0(fuid, ctx, readCon, true);
	}

	private static RdbFolder loadFolder0(final int fuid, final Context ctx, final Connection readCon,
			final boolean backup) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			/*
			 * Load folder
			 */
			stmt = readCon.prepareStatement(backup ? SQL_LOAD_FLD_BAK : SQL_LOAD_FLD);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, fuid);
			rs = stmt.executeQuery();
			if (!rs.next()) {
				return null;
			}
			final RdbFolderID folderId = new RdbFolderID(fuid, ctx);
			final RdbFolder folderObj = new RdbFolder();
			folderObj.setFolderID(folderId);
			folderObj.setParentFolderID(new RdbFolderID(rs.getInt(1), ctx));
			folderObj.setName(rs.getString(2));
			folderObj.setModule(RdbFolderModule.getModuleByValue(rs.getInt(3)));
			folderObj.setType(RdbFolderType.getTypeByValue(rs.getInt(4)));
			folderObj.setCreationDate(new Date(rs.getLong(5)));
			folderObj.setCreatedBy(rs.getInt(6));
			folderObj.setLastModified(new Date(rs.getLong(7)));
			folderObj.setModifiedBy(rs.getInt(8));
			folderObj.setPermissionStatus(FolderPermissionStatus.getPermissionStatusByValue(rs.getInt(9)));
			folderObj.setHasSubfolder(rs.getInt(10) > 0);
			folderObj.setDefault(rs.getInt(11) > 0);
			closeSQLStuff(rs, stmt);
			/*
			 * Load permissions
			 */
			stmt = readCon.prepareStatement(backup ? SQL_LOAD_PERMS_BAK : SQL_LOAD_PERMS);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, fuid);
			final List<RdbFolderPermission> l = new ArrayList<RdbFolderPermission>();
			while (rs.next()) {
				final RdbFolderPermission p = new RdbFolderPermission();
				p.setEntity(rs.getInt(1));
				try {
					p.setFolderPermission(rs.getInt(2));
					p.setReadPermission(rs.getInt(3));
					p.setWritePermission(rs.getInt(4));
					p.setDeletePermission(rs.getInt(5));
				} catch (final FolderException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
				p.setAdmin(rs.getInt(6) > 0);
				p.setGroup(rs.getInt(7) > 0);
				p.setFolderID(folderId);
				l.add(p);
			}
			folderObj.setPermissions(l.toArray(new RdbFolderPermission[l.size()]));
			return folderObj;
		} finally {
			closeSQLStuff(rs, stmt);
		}
	}

	private static final String SQL_GETSUBFLDIDS = "SELECT fuid FROM oxfolder_tree WHERE cid = ? AND parent = ?";

	/**
	 * Creates a list with all subfolder IDs of given folder
	 * 
	 * @param folderId
	 *            The folder ID
	 * @param ctx
	 *            The context
	 * @return A list with all subfolder IDs of given folder
	 * @throws DBPoolingException
	 *             If connection cannot be obtained from or put back into pool
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static List<Integer> getSubfolderIDs(final int folderId, final Context ctx) throws DBPoolingException,
			SQLException {
		Connection readCon = null;
		try {
			readCon = Database.get(ctx, false);
			return getSubfolderIDs(folderId, ctx, readCon);
		} finally {
			closeResources(null, null, readCon, true, ctx);
		}
	}

	/**
	 * Creates a list with all subfolder IDs of given folder
	 * 
	 * @param folderId
	 *            The folder ID
	 * @param ctx
	 *            The context
	 * @param writeCon
	 *            A writable connection
	 * @return A list with all subfolder IDs of given folder
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static List<Integer> getSubfolderIDs(final int folderId, final Context ctx, final Connection writeCon)
			throws SQLException {
		final List<Integer> retval = new ArrayList<Integer>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = writeCon.prepareStatement(SQL_GETSUBFLDIDS);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, folderId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				retval.add(Integer.valueOf(rs.getInt(1)));
			}
		} finally {
			closeSQLStuff(rs, stmt);
		}
		return retval;
	}

	/**
	 * Deletes a folder from storage
	 * 
	 * @param fuid
	 *            The folder ID
	 * @param userId
	 *            The user ID
	 * @param lastModified
	 *            The last-modified timestamp
	 * @param ctx
	 *            The context
	 * @return The folder object loaded from database
	 * @throws DBPoolingException
	 *             If a readable connection cannot be fetched from connection
	 *             pool
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static void deleteFolder(final int fuid, final int userId, final long lastModified, final Context ctx)
			throws DBPoolingException, SQLException {
		Connection readCon = null;
		try {
			readCon = Database.get(ctx, false);
			deleteFolder(fuid, userId, lastModified, ctx, readCon);
		} finally {
			closeResources(null, null, readCon, true, ctx);
		}
	}

	/**
	 * Deletes a folder from storage
	 * 
	 * @param fuid
	 *            The folder ID
	 * @param userId
	 *            The user ID
	 * @param lastModified
	 *            The last-modified timestamp
	 * @param ctx
	 *            The context
	 * @param writeCon
	 *            A writable connection
	 * @return The folder object loaded from database
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static void deleteFolder(final int fuid, final int userId, final long lastModified, final Context ctx,
			final Connection writeCon) throws SQLException {
		deleteFolder0(fuid, ctx, writeCon, false, lastModified, userId);
	}

	/**
	 * Deletes a folder object from backup table
	 * 
	 * @param fuid
	 *            The folder ID
	 * @param ctx
	 *            The context
	 * @return The backup folder object loaded from database
	 * @throws DBPoolingException
	 *             If a readable connection cannot be fetched from connection
	 *             pool
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static void deleteBackupFolder(final int fuid, final Context ctx) throws DBPoolingException, SQLException {
		Connection readCon = null;
		try {
			readCon = Database.get(ctx, false);
			deleteBackupFolder(fuid, ctx, readCon);
		} finally {
			closeResources(null, null, readCon, true, ctx);
		}
	}

	/**
	 * Deletes a folder object from backup table
	 * 
	 * @param fuid
	 *            The folder ID
	 * @param ctx
	 *            The context
	 * @param writeCon
	 *            A writable connection
	 * @return The backup folder object loaded from database
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static void deleteBackupFolder(final int fuid, final Context ctx, final Connection writeCon)
			throws SQLException {
		deleteFolder0(fuid, ctx, writeCon, true, -1L, -1);
	}

	private static final String SQL_DELETE_FLD = "DELETE FROM oxfolder_tree WHERE cid = ? AND fuid = ?";

	private static final String SQL_DELETE_PERM = "DELETE FROM oxfolder_permissions WHERE cid = ? AND fuid = ?";

	private static final String SQL_DELETE_SF = "DELETE FROM oxfolder_specialfolders WHERE cid = ? AND fuid = ?";

	private static final String SQL_DELETE_INSERT_OT = "INSERT INTO del_oxfolder_tree SELECT * FROM oxfolder_tree WHERE cid = ? AND fuid = ?";

	private static final String SQL_DELETE_INSERT_OP = "INSERT INTO del_oxfolder_permissions SELECT * FROM oxfolder_permissions WHERE cid = ? AND fuid = ?";

	private static final String SQL_DELETE_UPDATE = "UPDATE del_oxfolder_tree SET changing_date = ?, changed_from = ? WHERE cid = ? AND fuid = ?";

	private static final String SQL_DELETE_FLD_BAK = "DELETE FROM del_oxfolder_tree WHERE cid = ? AND fuid = ?";

	private static final String SQL_DELETE_PERM_BAK = "DELETE FROM del_oxfolder_permissions WHERE cid = ? AND fuid = ?";

	private static void deleteFolder0(final int fuid, final Context ctx, final Connection writeCon,
			final boolean deleteBackup, final long lastModified, final int userId) throws SQLException {
		PreparedStatement stmt = null;
		try {
			if (deleteBackup) {
				/*
				 * Delete from folder & permission backup tables
				 */
				stmt = writeCon.prepareStatement(SQL_DELETE_PERM_BAK);
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, fuid);
				stmt.executeUpdate();
				stmt.close();
				stmt = writeCon.prepareStatement(SQL_DELETE_FLD_BAK);
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, fuid);
				stmt.executeUpdate();
				stmt = null;
				return;
			}
			/*
			 * Copy to backup tables
			 */
			stmt = writeCon.prepareStatement(SQL_DELETE_INSERT_OT);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, fuid);
			stmt.executeUpdate();
			stmt.close();
			stmt = writeCon.prepareStatement(SQL_DELETE_INSERT_OP);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, fuid);
			stmt.executeUpdate();
			stmt.close();
			/*
			 * Delete from oxfolder_specialfolders
			 */
			stmt = writeCon.prepareStatement(SQL_DELETE_SF);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, fuid);
			stmt.executeUpdate();
			stmt.close();
			/*
			 * Delete from folder & permission tables
			 */
			stmt = writeCon.prepareStatement(SQL_DELETE_PERM);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, fuid);
			stmt.executeUpdate();
			stmt.close();
			stmt = writeCon.prepareStatement(SQL_DELETE_FLD);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, fuid);
			stmt.executeUpdate();
			/*
			 * Update last-modified timestamp in backup tables
			 */
			stmt = writeCon.prepareStatement(SQL_DELETE_UPDATE);
			stmt.setLong(1, lastModified);
			stmt.setInt(2, userId);
			stmt.setInt(3, ctx.getContextId());
			stmt.setInt(4, fuid);
			stmt.executeUpdate();
			stmt.close();
			stmt = null;
		} finally {
			closeSQLStuff(null, stmt);
		}
	}
}
