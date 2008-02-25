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

package com.openexchange.tools.oxfolder.deletelistener.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.openexchange.tools.oxfolder.deletelistener.CorruptPermission;
import com.openexchange.tools.oxfolder.deletelistener.Permission;

/**
 * {@link GroupPermissionMerger}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class GroupPermissionMerger {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(GroupPermissionMerger.class);

	/**
	 * Initializes a new {@link GroupPermissionMerger}
	 */
	private GroupPermissionMerger() {
		super();
	}

	/**
	 * Handles corrupt group permissions by re-assigning (merged permission)
	 * them to special "<i>all-groups-and-users</i>" group
	 * 
	 * @param corruptPermissions
	 *            The corrupt group permissions
	 * @param con
	 *            A connection to MySQL database
	 * @throws SQLException
	 *             If corrupt permissions cannot be handled due to a SQL error
	 */
	public static void handleCorruptGroupPermissions(final CorruptPermission[] corruptPermissions, final Connection con)
			throws SQLException {
		final boolean[] delete = new boolean[1];
		final StringBuilder logBuilder;
		if (LOG.isInfoEnabled()) {
			logBuilder = new StringBuilder(128);
		} else {
			logBuilder = null;
		}
		for (final CorruptPermission corruptPermission : corruptPermissions) {
			final int allGroupsAndUsers = 0;
			/*
			 * Yield merged permission and remember if "all-groups-and-users"
			 * already holds a permission on the folder in question. If it does
			 * group's permission has to be deleted whereby
			 * "all-groups-and-users" permission is set to merged permission.
			 * Otherwise entity's permission is merged and assigned to
			 * "all-groups-and-users".
			 */
			delete[0] = false;
			final Permission merged = getMergedPermission(corruptPermission.permission_id, allGroupsAndUsers,
					corruptPermission.fuid, corruptPermission.cid, con, delete);
			if (delete[0]) {
				MergerUtility.deletePermission(corruptPermission.permission_id, corruptPermission.fuid,
						corruptPermission.cid, con);
				if (LOG.isInfoEnabled()) {
					logBuilder.setLength(0);
					LOG.info(logBuilder.append("Permission deleted for group ").append(corruptPermission.permission_id)
							.append(" on folder ").append(corruptPermission.fuid).append(" in context ").append(
									corruptPermission.cid).toString());
				}
				updatePermission(merged, allGroupsAndUsers, allGroupsAndUsers, corruptPermission.fuid,
						corruptPermission.cid, con);
				if (LOG.isInfoEnabled()) {
					logBuilder.setLength(0);
					LOG.info(logBuilder.append("...and merged to \"all-groups-and-users\": ").append(merged.toString())
							.toString());
				}
			} else {
				updatePermission(merged, corruptPermission.permission_id, allGroupsAndUsers, corruptPermission.fuid,
						corruptPermission.cid, con);
				if (LOG.isInfoEnabled()) {
					logBuilder.setLength(0);
					LOG.info(logBuilder.append("Permission merged to \"all-groups-and-users\": ").append(
							merged.toString()).toString());
				}
			}

		}
	}

	private static final String SQL_REASSIGN_UPDATE_PERM = "UPDATE oxfolder_permissions "
			+ "SET fp = ?, orp = ?, owp = ?, odp = ?, admin_flag = ?, permission_id = ?, group_flag = 1 "
			+ "WHERE cid = ? AND permission_id = ? AND fuid = ?";

	private static void updatePermission(final Permission mergedPerm, final int entity, final int setToEntity,
			final int fuid, final int cid, final Connection writeCon) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = writeCon.prepareStatement(SQL_REASSIGN_UPDATE_PERM);
			stmt.setInt(1, mergedPerm.fp);
			stmt.setInt(2, mergedPerm.orp);
			stmt.setInt(3, mergedPerm.owp);
			stmt.setInt(4, mergedPerm.odp);
			stmt.setInt(5, mergedPerm.admin ? 1 : 0);
			stmt.setInt(6, setToEntity);
			stmt.setInt(7, cid);
			stmt.setInt(8, entity);
			stmt.setInt(9, fuid);
			stmt.executeUpdate();
		} finally {
			if (null != stmt) {
				try {
					stmt.close();
				} catch (final SQLException e) {
					System.err.println("Warning: Statement could not be properly closed");
					e.printStackTrace(System.err);
				}
			}
		}
	}

	private static final String SQL_SEL_PERM = "SELECT fp, orp, owp, odp, admin_flag "
			+ "FROM oxfolder_permissions WHERE cid = ? AND permission_id = ? AND fuid = ?";

	private static Permission getMergedPermission(final int entity, final int fallbackEntity, final int fuid,
			final int cid, final Connection readCon, final boolean[] delete) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = readCon.prepareStatement(SQL_SEL_PERM);
			stmt.setInt(1, cid);
			stmt.setInt(2, fallbackEntity);
			stmt.setInt(3, fuid);
			rs = stmt.executeQuery();
			if (!rs.next()) {
				/*
				 * Merged permission is entity's permission since no permission
				 * is defined for fallback entity
				 */
				rs.close();
				stmt.close();
				stmt = readCon.prepareStatement(SQL_SEL_PERM);
				stmt.setInt(1, cid);
				stmt.setInt(2, entity);
				stmt.setInt(3, fuid);
				rs = stmt.executeQuery();
				if (!rs.next()) {
					/*
					 * Empty permission
					 */
					throw new SQLException(new StringBuilder(64).append("Entity ").append(entity).append(
							" has no permission on folder ").append(fuid).append(" in context ").append(cid).toString());
				}
				delete[0] = false;
				return new Permission(fallbackEntity, fuid, rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs
						.getInt(5) > 0);
			}
			final Permission fallbackPerm = new Permission(fallbackEntity, fuid, rs.getInt(1), rs.getInt(2), rs
					.getInt(3), rs.getInt(4), rs.getInt(5) > 0);
			rs.close();
			stmt.close();
			stmt = readCon.prepareStatement(SQL_SEL_PERM);
			stmt.setInt(1, cid);
			stmt.setInt(2, entity);
			stmt.setInt(3, fuid);
			rs = stmt.executeQuery();
			if (!rs.next()) {
				return fallbackPerm;
			}
			final Permission entityPerm = new Permission(entity, fuid, rs.getInt(1), rs.getInt(2), rs.getInt(3), rs
					.getInt(4), rs.getInt(5) > 0);
			/*
			 * Merge
			 */
			final Permission mergedPerm = new Permission(fallbackEntity, fuid,
					Math.max(fallbackPerm.fp, entityPerm.fp), Math.max(fallbackPerm.orp, entityPerm.orp), Math.max(
							fallbackPerm.owp, entityPerm.owp), Math.max(fallbackPerm.odp, entityPerm.odp),
					(fallbackPerm.admin || entityPerm.admin));
			delete[0] = true;
			return mergedPerm;
		} finally {
			if (null != rs) {
				try {
					rs.close();
					rs = null;
				} catch (final SQLException e) {
					System.err.println("Warning: Result set could not be properly closed");
					e.printStackTrace(System.err);
				}
			}
			if (null != stmt) {
				try {
					stmt.close();
				} catch (final SQLException e) {
					System.err.println("Warning: Statement could not be properly closed");
					e.printStackTrace(System.err);
				}
			}
		}
	}
}