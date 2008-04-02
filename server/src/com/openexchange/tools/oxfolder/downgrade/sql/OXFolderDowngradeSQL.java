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

package com.openexchange.tools.oxfolder.downgrade.sql;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.Collections.SmartIntArray;

/**
 * {@link OXFolderDowngradeSQL} - Provides several SQL commands in order to
 * delete unused user folder data remaining from a former downgrade.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class OXFolderDowngradeSQL {

	/**
	 * {@link Permission} - Simple container for a permission.
	 * 
	 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
	 * 
	 */
	private static final class Permission {

		public final int entity;

		public final int fuid;

		public final int fp;

		public final int orp;

		public final int owp;

		public final int odp;

		public final boolean admin;

		public final boolean group;

		/**
		 * Initializes a new {@link Permission}
		 * 
		 * @param entity
		 *            The entity ID
		 * @param fuid
		 *            The folder ID
		 * @param fp
		 *            The folder permission
		 * @param orp
		 *            The object-read permission
		 * @param owp
		 *            The object-write permission
		 * @param odp
		 *            The object-delete permission
		 * @param admin
		 *            <code>true</code> if admin; otherwise <code>false</code>
		 * @param group
		 *            <code>true</code> if group; otherwise <code>false</code>
		 */
		public Permission(final int entity, final int fuid, final int fp, final int orp, final int owp, final int odp,
				final boolean admin, final boolean group) {
			super();
			this.entity = entity;
			this.fuid = fuid;
			this.admin = admin;
			this.group = group;
			this.fp = fp;
			this.odp = odp;
			this.orp = orp;
			this.owp = owp;
		}

		/**
		 * Initializes a new {@link Permission} from specified permission
		 * 
		 * @param The
		 *            entity ID
		 * @param src
		 *            The permission
		 */
		public Permission(final int entity, final Permission src) {
			super();
			this.entity = entity;
			this.fuid = src.fuid;
			this.admin = src.admin;
			this.group = src.group;
			this.fp = src.fp;
			this.odp = src.odp;
			this.orp = src.orp;
			this.owp = src.owp;
		}

		/**
		 * Initializes a new {@link Permission} from specified permission
		 * 
		 * @param The
		 *            entity ID
		 * @param fuid
		 *            The folder ID
		 * @param group
		 *            <code>true</code> if group; otherwise <code>false</code>
		 * @param src1
		 *            The first source permission
		 * @param src2
		 *            The second source permission
		 */
		public Permission(final int entity, final int fuid, final boolean group, final Permission src1,
				final Permission src2) {
			super();
			this.entity = entity;
			this.fuid = fuid;
			this.admin = src1.admin || src2.admin;
			this.group = group;
			this.fp = Math.max(src1.fp, src2.fp);
			this.odp = Math.max(src1.odp, src2.odp);
			this.orp = Math.max(src1.orp, src2.orp);
			this.owp = Math.max(src1.owp, src2.owp);
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder(64);
			sb.append("Entity=").append(entity).append(", Folder=").append(fuid).append('\n');
			sb.append("fp=").append(fp).append(", orp=").append(orp).append(", owp=").append(owp).append(", odp=")
					.append(odp).append(", admin=").append(admin).append(", group=").append(group).append('\n');
			return sb.toString();
		}

	}

	private static final String RPL_IDS = "#IDS#";

	private static final String RPL_PERM = "#PERM#";

	private static final String RPL_FOLDER = "#FOLDER#";

	/**
	 * Initializes a new {@link OXFolderDowngradeSQL}
	 */
	private OXFolderDowngradeSQL() {
		super();
	}

	private static final String SQL_SEL_MOD_PRIV_FLD = "SELECT ot.fuid FROM " + RPL_FOLDER
			+ " AS ot WHERE ot.cid = ? AND ot.type = ? AND ot.created_from = ? AND ot.module = ?"
			+ " AND ot.default_flag = 0 GROUP BY ot.fuid";

	/**
	 * Gets all user's private folders' IDs of specified module except the
	 * module's default folder.
	 * 
	 * @param module
	 *            The module
	 * @param owner
	 *            The owner
	 * @param cid
	 *            The context ID
	 * @param folderTable
	 *            The folder table identifier
	 * @param readCon
	 *            A readable connection
	 * @return The user's private folders' IDs as an array of <code>int</code>.
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static int[] getModulePrivateFolders(final int module, final int owner, final int cid,
			final String folderTable, final Connection readCon) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = readCon.prepareStatement(SQL_SEL_MOD_PRIV_FLD.replaceFirst(RPL_FOLDER, folderTable));
			stmt.setInt(1, cid);
			stmt.setInt(2, FolderObject.PRIVATE);
			stmt.setInt(3, owner);
			stmt.setInt(4, module);
			rs = stmt.executeQuery();
			final SmartIntArray sia = new SmartIntArray(128);
			while (rs.next()) {
				sia.append(rs.getInt(1));
			}
			return sia.toArray();
		} finally {
			closeSQLStuff(rs, stmt);
		}

	}

	private static final String SQL_DEL_FLD_PERMS = "DELETE FROM " + RPL_PERM
			+ " AS op WHERE op.cid = ? AND op.fuid IN " + RPL_IDS;

	/**
	 * Deletes specified folders' permissions from specified permission table.
	 * 
	 * @param fuids
	 *            The folder IDs
	 * @param cid
	 *            The context ID
	 * @param permTable
	 *            The permission table identifier
	 * @param writeCon
	 *            A writable connection
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static void deleteFolderPermissions(final int[] fuids, final int cid, final String permTable,
			final Connection writeCon) throws SQLException {
		PreparedStatement stmt = null;
		try {
			final String ids;
			{
				final StringBuilder sb = new StringBuilder(32);
				sb.append('(');
				sb.append(fuids[0]);
				for (int i = 1; i < fuids.length; i++) {
					sb.append(',').append(fuids[i]);
				}
				sb.append(')');
				ids = sb.toString();
			}
			stmt = writeCon.prepareStatement(SQL_DEL_FLD_PERMS.replaceFirst(RPL_PERM, permTable).replaceFirst(RPL_IDS,
					ids));
			stmt.setInt(1, cid);
			stmt.executeUpdate();
		} finally {
			closeSQLStuff(null, stmt);
		}
	}

	private static final String SQL_DEL_FLDS = "DELETE FROM " + RPL_FOLDER + " AS ot WHERE ot.cid = ? AND ot.fuid IN "
			+ RPL_IDS;

	/**
	 * Deletes specified folders from specified folder table.
	 * 
	 * @param fuids
	 *            The folder IDs
	 * @param cid
	 *            The context ID
	 * @param folderTable
	 *            The folder table identifier
	 * @param writeCon
	 *            A writable connection
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static void deleteFolders(final int[] fuids, final int cid, final String folderTable,
			final Connection writeCon) throws SQLException {
		PreparedStatement stmt = null;
		try {
			final String ids;
			{
				final StringBuilder sb = new StringBuilder(32);
				sb.append('(');
				sb.append(fuids[0]);
				for (int i = 1; i < fuids.length; i++) {
					sb.append(',').append(fuids[i]);
				}
				sb.append(')');
				ids = sb.toString();
			}
			stmt = writeCon.prepareStatement(SQL_DEL_FLDS.replaceFirst(RPL_FOLDER, folderTable).replaceFirst(RPL_IDS,
					ids));
			stmt.setInt(1, cid);
			stmt.executeUpdate();
		} finally {
			closeSQLStuff(null, stmt);
		}
	}

	private static final String SQL_SEL_PUBLIC_FLDS = "SELECT op.fuid FROM " + RPL_PERM + " AS op JOIN " + RPL_FOLDER
			+ " AS ot ON op.fuid = ot.fuid AND op.cid = ? AND ot.cid = ? WHERE ot.module = ? "
			+ "AND op.permission_id = ? GROUP BY op.fuid";

	/**
	 * Determines the module's public folders' IDs which hold a permission entry
	 * for specified entity
	 * 
	 * @param entity
	 *            The entity
	 * @param module
	 *            The module
	 * @param cid
	 *            The context ID
	 * @param folderTable
	 *            The folder table identifier
	 * @param permTable
	 *            The permission table identifier
	 * @param readCon
	 *            A readable connection
	 * @return The module's public folders' IDs
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static int[] getAffectedPublicFolders(final int entity, final int module, final int cid,
			final String folderTable, final String permTable, final Connection readCon) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = readCon.prepareStatement(SQL_SEL_PUBLIC_FLDS.replaceFirst(RPL_PERM, permTable).replaceFirst(
					RPL_FOLDER, folderTable));
			stmt.setInt(1, cid);
			stmt.setInt(2, cid);
			stmt.setInt(3, module);
			stmt.setInt(4, entity);
			rs = stmt.executeQuery();
			final SmartIntArray sia = new SmartIntArray(128);
			while (rs.next()) {
				sia.append(rs.getInt(1));
			}
			return sia.toArray();
		} finally {
			closeSQLStuff(rs, stmt);
		}
	}

	private static final String SQL_SEL_DEF_FLD = "SELECT ot.fuid FROM " + RPL_FOLDER + "AS ot WHERE ot.cid = ?"
			+ " AND ot.module = ? AND ot.created_from = ? AND ot.default_flag = 1";

	private static final String SQL_DEL_DEF_FLD_PERM = "DELETE FROM " + RPL_PERM
			+ " AS op WHERE op.cid = ? AND op.fuid = ? AND op.permission_id <> ?";

	/**
	 * Removes all shared permissions from module's default folder for specified
	 * entity
	 * 
	 * @param entity
	 *            The entity
	 * @param module
	 *            The module
	 * @param cid
	 *            The context ID
	 * @param folderTable
	 *            The folder table identifier
	 * @param permTable
	 *            The permission table identifier
	 * @param writeCon
	 *            A writable connection
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static void cleanDefaultModuleFolder(final int entity, final int module, final int cid,
			final String folderTable, final String permTable, final Connection writeCon) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		final int fuid;
		try {
			stmt = writeCon.prepareStatement(SQL_SEL_DEF_FLD.replaceFirst(RPL_FOLDER, folderTable));
			stmt.setInt(1, cid);
			stmt.setInt(2, module);
			stmt.setInt(3, entity);
			rs = stmt.executeQuery();
			if (!rs.next()) {
				return;
			}
			fuid = rs.getInt(1);
		} finally {
			closeSQLStuff(rs, stmt);
			rs = null;
			stmt = null;
		}
		try {
			stmt = writeCon.prepareStatement(SQL_DEL_DEF_FLD_PERM.replaceFirst(RPL_PERM, permTable));
			stmt.setInt(1, cid);
			stmt.setInt(2, fuid);
			stmt.setInt(3, entity);
			stmt.executeUpdate();
		} finally {
			closeSQLStuff(rs, stmt);
		}
	}

	private static final String SQL_LOAD_PERMS = "SELECT op.permission_id, op.fp, op.orp, op.owp, op.odp, "
			+ "op.admin_flag, op.group_flag FROM " + RPL_PERM + " AS op WHERE op.cid = ? AND op.fuid = ?";

	/**
	 * Handles the affected public folder's permissions
	 * 
	 * @param entity
	 *            The entity ID
	 * @param fuid
	 *            The folder ID
	 * @param cid
	 *            The context ID
	 * @param permTable
	 *            The permission table identifier
	 * @param writeCon
	 *            A writable connection
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static void handleAffectedPublicFolder(final int entity, final int fuid, final int cid,
			final String permTable, final Connection writeCon) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		final List<Permission> perms;
		try {
			stmt = writeCon.prepareStatement(SQL_LOAD_PERMS.replaceFirst(RPL_PERM, permTable));
			stmt.setInt(1, cid);
			stmt.setInt(2, fuid);
			rs = stmt.executeQuery();
			perms = new ArrayList<Permission>();
			while (rs.next()) {
				perms.add(new Permission(rs.getInt(1), fuid, rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs
						.getInt(6) > 0, rs.getInt(7) > 0));
			}
		} finally {
			closeSQLStuff(rs, stmt);
			stmt = null;
			rs = null;
		}
		final int mailAdmin = getContextAdminID(cid, writeCon);
		/*
		 * Handle according to following conditions
		 */
		if (perms.size() == 1) {
			/*
			 * The only permission entry: reassign
			 */
			updateSingleEntityPermission(perms.get(0), mailAdmin, permTable, writeCon, cid);
		} else {
			final Permission onlyPerm = isOnlyAdmin(perms, entity);
			if (null != onlyPerm) {
				/*
				 * The only admin: reassign
				 */
				final Permission adminPerm = getEntityPerm(perms, mailAdmin);
				if (null == adminPerm) {
					updateSingleEntityPermission(onlyPerm, mailAdmin, permTable, writeCon, cid);
				} else {
					updateSingleEntityPermission(mergePermission(adminPerm, onlyPerm), mailAdmin, permTable, writeCon,
							cid);
				}
			} else {
				/*
				 * Entity permission neither is the only permission nor is the
				 * only admin permission on current folder, so just delete
				 * entity's permission
				 */
				final Permission entityPerm = getEntityPerm(perms, entity);
				deleteSingleEntityPermission(entityPerm, permTable, writeCon, cid);
			}
		}

	}

	private static Permission isOnlyAdmin(final List<Permission> perms, final int entity) {
		final int size = perms.size();
		int count = 0;
		int adminIndex = -1;
		for (int i = 0; i < size && count < 2; i++) {
			final Permission permission = perms.get(i);
			if (permission.admin) {
				count++;
				adminIndex = (permission.entity == entity) ? i : -1;
			}
		}
		return (count == 1 && adminIndex != -1) ? perms.get(adminIndex) : null;
	}

	private static Permission getEntityPerm(final List<Permission> perms, final int entity) {
		for (final Permission permission : perms) {
			if (permission.entity == entity) {
				return permission;
			}
		}
		return null;
	}

	private static Permission mergePermission(final Permission adminPerm, final Permission entityPerm) {
		return new Permission(entityPerm.entity, entityPerm.fuid, entityPerm.group, adminPerm, entityPerm);
	}

	private static final String SQL_REASSIGN_UPDATE_PERM = "UPDATE " + RPL_PERM
			+ " SET fp = ?, orp = ?, owp = ?, odp = ?, admin_flag = ?, group_flag = ?, permission_id = ? "
			+ "WHERE cid = ? AND permission_id = ? AND fuid = ?";

	private static void updateSingleEntityPermission(final Permission permission, final int mailAdmin,
			final String permTable, final Connection wc, final int cid) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = wc.prepareStatement(SQL_REASSIGN_UPDATE_PERM.replaceFirst(RPL_PERM, permTable));
			stmt.setInt(1, permission.fp);
			stmt.setInt(2, permission.orp);
			stmt.setInt(3, permission.owp);
			stmt.setInt(4, permission.odp);
			stmt.setInt(5, permission.admin ? 1 : 0);
			stmt.setInt(6, permission.group ? 1 : 0);
			stmt.setInt(7, mailAdmin);
			stmt.setInt(8, cid);
			stmt.setInt(9, permission.entity);
			stmt.setInt(10, permission.fuid);
			stmt.executeUpdate();
		} finally {
			closeSQLStuff(null, stmt);
		}
	}

	private static final String SQL_DELETE_PERM = "DELETE FROM " + RPL_PERM
			+ " WHERE cid = ? AND permission_id = ? AND fuid = ?";

	private static void deleteSingleEntityPermission(final Permission permission, final String permTable,
			final Connection wc, final int cid) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = wc.prepareStatement(SQL_DELETE_PERM.replaceFirst(RPL_PERM, permTable));
			stmt.setInt(1, cid);
			stmt.setInt(2, permission.entity);
			stmt.setInt(3, permission.fuid);
			stmt.executeUpdate();
		} finally {
			closeSQLStuff(null, stmt);
		}
	}

	private static final String SQL_SELECT_ADMIN = "SELECT user FROM user_setting_admin WHERE cid = ?";

	private static int getContextAdminID(final int cid, final Connection readCon) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = readCon.prepareStatement(SQL_SELECT_ADMIN);
			stmt.setInt(1, cid);
			rs = stmt.executeQuery();
			if (!rs.next()) {
				return -1;
			}
			return rs.getInt(1);
		} finally {
			closeSQLStuff(rs, stmt);
		}
	}

	private static final String SQL_DEL_SHARED_PERMS = "DELETE op FROM " + RPL_PERM + " AS op, " + RPL_FOLDER
			+ " AS ot" + " WHERE op.fuid = ot.fuid AND op.cid = ? AND ot.cid = ? AND ot.created_from = ?"
			+ " AND ot.type = ? AND op.permission_id <> ?";

	private static final String SQL_DEL_SHARED_PERMS_FOREIGN = "DELETE op FROM " + RPL_PERM + " AS op, " + RPL_FOLDER
			+ " AS ot" + " WHERE op.fuid = ot.fuid AND op.cid = ? AND ot.cid = ? AND ot.created_from <> ?"
			+ " AND ot.type = ? AND op.permission_id = ?";

	/**
	 * Removes all shared permissions bound to given entity's private folders
	 * and all shared permissions assigned to given entity by private folders
	 * 
	 * @param entity
	 *            The entity ID
	 * @param cid
	 *            The context ID
	 * @param folderTable
	 *            The folder table identifier
	 * @param permTable
	 *            The permission table identifier
	 * @param writeCon
	 *            A writable connection
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static void removeShareAccess(final int entity, final int cid, final String folderTable,
			final String permTable, final Connection writeCon) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = writeCon.prepareStatement(SQL_DEL_SHARED_PERMS.replaceAll(RPL_PERM, permTable).replaceFirst(
					RPL_FOLDER, folderTable));
			stmt.setInt(1, cid);
			stmt.setInt(2, cid);
			stmt.setInt(3, entity);
			stmt.setInt(4, FolderObject.PRIVATE);
			stmt.setInt(5, entity);
			stmt.executeUpdate();
		} finally {
			closeSQLStuff(null, stmt);
			stmt = null;
		}
		try {
			stmt = writeCon.prepareStatement(SQL_DEL_SHARED_PERMS_FOREIGN.replaceAll(RPL_PERM, permTable).replaceFirst(
					RPL_FOLDER, folderTable));
			stmt.setInt(1, cid);
			stmt.setInt(2, cid);
			stmt.setInt(3, entity);
			stmt.setInt(4, FolderObject.PRIVATE);
			stmt.setInt(5, entity);
			stmt.executeUpdate();
		} finally {
			closeSQLStuff(null, stmt);
		}
	}

	private static final String SQL_SEL_SUB_INFO_FLD = "SELECT ot.fuid FROM " + RPL_FOLDER
			+ " AS ot WHERE ot.parent IN (" + "SELECT ot2.fuid FROM " + RPL_FOLDER
			+ " AS ot2 WHERE ot2.cid = ? AND ot2.module = ? AND ot2.created_from = ? AND ot2.default_flag = 1)";

	private static final String SQL_SEL_SUB2_INFO_FLD = "SELECT ot.fuid FROM " + RPL_FOLDER
			+ " AS ot WHERE ot.cid = ? AND ot.parent = ?";

	private static final String SQL_DEL_INFO_PERM = "DELETE FROM " + RPL_PERM
			+ " AS op WHERE op.cid = ? AND op.fuid = ?";

	private static final String SQL_DEL_INFO = "DELETE FROM " + RPL_FOLDER + " AS ot WHERE ot.cid = ? AND ot.fuid = ?";

	/**
	 * Removes all subfolders below default infostore folder
	 * 
	 * @param entity
	 *            The entity
	 * @param cid
	 *            The context ID
	 * @param folderTable
	 *            The folder table identifier
	 * @param permTable
	 *            The permission table identifier
	 * @param writeCon
	 *            A writable connection
	 * @throws SQLException
	 *             If a SQL error occurs
	 */
	public static void removeSubInfostoreFolders(final int entity, final int cid, final String folderTable,
			final String permTable, final Connection writeCon) throws SQLException {
		/*
		 * Remove all subfolders below default infostore folder
		 */
		PreparedStatement stmt = null;
		ResultSet rs = null;
		final int[] fuids;
		try {
			stmt = writeCon.prepareStatement(SQL_SEL_SUB_INFO_FLD.replaceAll(RPL_FOLDER, folderTable));
			stmt.setInt(1, cid);
			stmt.setInt(2, FolderObject.INFOSTORE);
			stmt.setInt(3, entity);
			rs = stmt.executeQuery();
			final SmartIntArray sia = new SmartIntArray(128);
			while (rs.next()) {
				sia.append(rs.getInt(1));
			}
			fuids = sia.toArray();
		} finally {
			closeSQLStuff(rs, stmt);
			rs = null;
			stmt = null;
		}
		for (final int fuid : fuids) {
			deleteFolder(fuid, cid, folderTable, permTable, writeCon);
		}
	}

	private static void deleteFolder(final int fuid, final int cid, final String folderTable, final String permTable,
			final Connection writeCon) throws SQLException {
		PreparedStatement stmt = null;
		{
			ResultSet rs = null;
			final int[] subFuids;
			try {
				stmt = writeCon.prepareStatement(SQL_SEL_SUB2_INFO_FLD.replaceFirst(RPL_FOLDER, folderTable));
				stmt.setInt(1, cid);
				stmt.setInt(2, fuid);
				rs = stmt.executeQuery();
				final SmartIntArray sia = new SmartIntArray(128);
				while (rs.next()) {
					sia.append(rs.getInt(1));
				}
				subFuids = sia.toArray();
			} finally {
				closeSQLStuff(rs, stmt);
				stmt = null;
			}
			for (final int subFuid : subFuids) {
				deleteFolder(subFuid, cid, folderTable, permTable, writeCon);
			}
		}
		try {
			stmt = writeCon.prepareStatement(SQL_DEL_INFO_PERM.replaceFirst(RPL_PERM, permTable));
			stmt.setInt(1, cid);
			stmt.setInt(2, fuid);
			stmt.executeUpdate();
		} finally {
			closeSQLStuff(null, stmt);
			stmt = null;
		}
		try {
			stmt = writeCon.prepareStatement(SQL_DEL_INFO.replaceFirst(RPL_FOLDER, folderTable));
			stmt.setInt(1, cid);
			stmt.setInt(2, fuid);
			stmt.executeUpdate();
		} finally {
			closeSQLStuff(null, stmt);
			stmt = null;
		}
	}

}
