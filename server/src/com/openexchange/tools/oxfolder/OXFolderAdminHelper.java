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
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.openexchange.api2.OXException;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.cache.impl.FolderCacheNotEnabledException;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.LocaleTools;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;

/**
 * OXFolderAdminHelper
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class OXFolderAdminHelper {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(OXFolderAdminHelper.class);

	public OXFolderAdminHelper() {
		super();
	}

	/*
	 * INSERT INTO oxfolder_tree VALUES (1, 0, 'private', 'system',
	 * 'system','system', 'system', 'now', 'System', null, null); INSERT INTO
	 * oxfolder_tree VALUES (2, 0, 'public', 'system', 'system','system',
	 * 'system', 'now', 'System', null, null); INSERT INTO oxfolder_tree VALUES
	 * (3, 0, 'shared', 'system', 'system','system', 'system', 'now', 'System',
	 * null, null); INSERT INTO oxfolder_tree VALUES (4, 0, 'system', 'system',
	 * 'system','system', 'system', 'now', 'System', null, null); INSERT INTO
	 * oxfolder_tree VALUES (5, 4, 'system_global', 'contact',
	 * 'system','system', 'system', 'now', 'System', null, null); INSERT INTO
	 * oxfolder_tree VALUES (6, 4, 'system_ldap', 'contact', 'system','system',
	 * 'system', 'now', 'System', null, null); INSERT INTO oxfolder_tree VALUES
	 * (7, 0, 'user', 'system', 'system','system', 'system', 'now', 'System',
	 * null, null); INSERT INTO oxfolder_tree VALUES (8, 7, 'projects',
	 * 'projects', 'system','system', 'system', 'now', 'System', null, null);
	 * 
	 * INSERT INTO oxfolder_permissions VALUES ((select nextval('serial_id')),
	 * 1, 512,'all_ox_users_and_ox_groups', 0, 8, 0, 0, 0); INSERT INTO
	 * oxfolder_permissions VALUES ((select nextval('serial_id')), 2, 512,
	 * 'all_ox_users_and_ox_groups', 0, 8, 0, 0, 0); INSERT INTO
	 * oxfolder_permissions VALUES ((select nextval('serial_id')), 3, 512,
	 * 'all_ox_users_and_ox_groups', 0, 2, 0, 0, 0); INSERT INTO
	 * oxfolder_permissions VALUES ((select nextval('serial_id')), 4, 512,
	 * 'all_ox_users_and_ox_groups', 0, 2, 0, 0, 0); INSERT INTO
	 * oxfolder_permissions VALUES ((select nextval('serial_id')), 5, 512,
	 * 'all_ox_users_and_ox_groups', 0, 4, 128, 128, 128); INSERT INTO
	 * oxfolder_permissions VALUES ((select nextval('serial_id')), 6, 512,
	 * 'all_ox_users_and_ox_groups', 0, 2, 4, 0, 0); INSERT INTO
	 * oxfolder_permissions VALUES ((select nextval('serial_id')), 7, 512,
	 * 'all_ox_users_and_ox_groups', 0, 2, 0, 0, 0); INSERT INTO
	 * oxfolder_permissions VALUES ((select nextval('serial_id')), 8, 512,
	 * 'all_ox_users_and_ox_groups', 0, 8, 4, 2, 2); INSERT INTO
	 * oxfolder_permissions VALUES ((select nextval('serial_id')), 8, 32768,
	 * 'mailadmin', 0, 128, 128, 128, 128);
	 * 
	 * 
	 * INSERT INTO oxfolder_specialfolders VALUES ('private', 1); INSERT INTO
	 * oxfolder_specialfolders VALUES ('public', 2); INSERT INTO
	 * oxfolder_specialfolders VALUES ('shared', 3); INSERT INTO
	 * oxfolder_specialfolders VALUES ('system', 4); INSERT INTO
	 * oxfolder_specialfolders VALUES ('system_global', 5); INSERT INTO
	 * oxfolder_specialfolders VALUES ('system_ldap', 6); INSERT INTO
	 * oxfolder_specialfolders VALUES ('user', 7);
	 * 
	 * INSERT INTO oxfolder_userfolders VALUES ('projects',
	 * 'projects/projects_list_all', null, 'folder/item_projects.png');
	 */

	private static final String STR_TABLE = "#TABLE#";

	/**
	 * Creates the standard system folders located in each context for given
	 * context in database and creates the default folders for context's admin
	 * user by invoking
	 * <code>{@link #addUserToOXFolders(int, String, String, int, Connection)}</code>
	 * 
	 * @param cid
	 *            The context ID
	 * @param mailAdminDisplayName
	 *            The display name of context's admin user
	 * @param language
	 *            The language conforming to syntax rules of locales
	 * @param con
	 *            A writeable connection
	 * @throws OXException
	 *             If system folder could not be created successfully or default
	 *             folders for context's admin user could not be created
	 */
	public void addContextSystemFolders(final int cid, final String mailAdminDisplayName, final String language,
			final Connection con) throws OXException {
		try {
			final int contextMalAdmin = getContextAdminID(cid, con);
			addContextSystemFolders(cid, contextMalAdmin, mailAdminDisplayName, language, con);
		} catch (final SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(cid));
		}
	}

	private static int getContextAdminID(final int cid, final Connection readCon) throws OXException {
		try {
			final int retval = OXFolderSQL.getContextAdminID(new ContextImpl(cid), readCon);
			if (retval == -1) {
				throw new OXFolderException(FolderCode.NO_ADMIN_USER_FOUND_IN_CONTEXT, Integer.valueOf(cid));
			}
			return retval;
		} catch (DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(cid));
		} catch (SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(cid));
		}
	}

	private void addContextSystemFolders(final int cid, final int mailAdmin, final String mailAdminDisplayName,
			final String language, final Connection writeCon) throws SQLException, OXException {
		final long creatingTime = System.currentTimeMillis();
		final OCLPermission systemPermission = new OCLPermission();
		systemPermission.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
		systemPermission.setGroupPermission(true);
		/*
		 * Insert system private folder
		 */
		systemPermission.setAllPermission(OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.NO_PERMISSIONS,
				OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
		systemPermission.setFolderAdmin(false);
		createSystemFolder(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, FolderObject.SYSTEM_PRIVATE_FOLDER_NAME,
				systemPermission, FolderObject.SYSTEM_ROOT_FOLDER_ID, FolderObject.SYSTEM_MODULE, true, creatingTime,
				mailAdmin, cid, writeCon);
		/*
		 * Insert system public folder
		 */
		systemPermission.setAllPermission(OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.NO_PERMISSIONS,
				OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
		systemPermission.setFolderAdmin(false);
		createSystemFolder(FolderObject.SYSTEM_PUBLIC_FOLDER_ID, FolderObject.SYSTEM_PUBLIC_FOLDER_NAME,
				systemPermission, FolderObject.SYSTEM_ROOT_FOLDER_ID, FolderObject.SYSTEM_MODULE, true, creatingTime,
				mailAdmin, cid, writeCon);
		/*
		 * Insert system shared folder
		 */
		systemPermission.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.NO_PERMISSIONS,
				OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
		systemPermission.setFolderAdmin(false);
		createSystemFolder(FolderObject.SYSTEM_SHARED_FOLDER_ID, FolderObject.SYSTEM_SHARED_FOLDER_NAME,
				systemPermission, FolderObject.SYSTEM_ROOT_FOLDER_ID, FolderObject.SYSTEM_MODULE, true, creatingTime,
				mailAdmin, cid, writeCon);
		/*
		 * Insert system system folder
		 */
		systemPermission.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.NO_PERMISSIONS,
				OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
		systemPermission.setFolderAdmin(false);
		createSystemFolder(FolderObject.SYSTEM_FOLDER_ID, FolderObject.SYSTEM_FOLDER_NAME, systemPermission,
				FolderObject.SYSTEM_ROOT_FOLDER_ID, FolderObject.SYSTEM_MODULE, true, creatingTime, mailAdmin, cid,
				writeCon);
		/*
		 * Insert system infostore folder
		 */
		systemPermission.setAllPermission(OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.NO_PERMISSIONS,
				OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
		systemPermission.setFolderAdmin(false);
		createSystemFolder(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, FolderObject.SYSTEM_INFOSTORE_FOLDER_NAME,
				systemPermission, FolderObject.SYSTEM_ROOT_FOLDER_ID, FolderObject.SYSTEM_MODULE, true, creatingTime,
				mailAdmin, cid, writeCon);
		/*
		 * Insert system system_global folder aka 'Shared Address Book'
		 */
		systemPermission.setAllPermission(OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.ADMIN_PERMISSION,
				OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
		systemPermission.setFolderAdmin(false);
		createSystemFolder(FolderObject.SYSTEM_GLOBAL_FOLDER_ID, FolderObject.SYSTEM_GLOBAL_FOLDER_NAME,
				systemPermission, FolderObject.SYSTEM_FOLDER_ID, FolderObject.CONTACT, true, creatingTime, mailAdmin,
				cid, writeCon);
		/*
		 * Insert system internal users folder aka 'Global Address Book'
		 */
		if (OXFolderProperties.isEnableInternalUsersEdit()) {
			systemPermission.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS,
					OCLPermission.WRITE_OWN_OBJECTS, OCLPermission.NO_PERMISSIONS);
		} else {
			systemPermission.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS,
					OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
		}
		systemPermission.setFolderAdmin(false);
		createSystemFolder(FolderObject.SYSTEM_LDAP_FOLDER_ID, FolderObject.SYSTEM_LDAP_FOLDER_NAME, systemPermission,
				FolderObject.SYSTEM_FOLDER_ID, FolderObject.CONTACT, true, creatingTime, mailAdmin, cid, writeCon);
		/*
		 * Insert system user folder
		 */
		systemPermission.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.NO_PERMISSIONS,
				OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
		systemPermission.setFolderAdmin(false);
		createSystemFolder(FolderObject.SYSTEM_OX_FOLDER_ID, FolderObject.SYSTEM_OX_FOLDER_NAME, systemPermission,
				FolderObject.SYSTEM_ROOT_FOLDER_ID, FolderObject.SYSTEM_MODULE, true, creatingTime, mailAdmin, cid,
				writeCon);
		/*
		 * Insert system projects folder
		 */
		systemPermission.setAllPermission(OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.READ_ALL_OBJECTS,
				OCLPermission.WRITE_OWN_OBJECTS, OCLPermission.DELETE_OWN_OBJECTS);
		systemPermission.setFolderAdmin(false);
		createSystemFolder(FolderObject.SYSTEM_OX_PROJECT_FOLDER_ID, FolderObject.SYSTEM_OX_PROJECT_FOLDER_NAME,
				systemPermission, FolderObject.SYSTEM_OX_FOLDER_ID, FolderObject.SYSTEM_MODULE, true, creatingTime,
				mailAdmin, cid, writeCon);
		if (LOG.isInfoEnabled()) {
			LOG.info(new StringBuilder("All System folders successfully created for context ").append(cid).toString());
		}
		/*
		 * Add mailadmin's folder rights to context's system folders and create
		 * his standard folders
		 */
		createSingleUserPermission(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, mailAdmin, new int[] {
				OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
				OCLPermission.ADMIN_PERMISSION }, true, cid, writeCon);
		createSingleUserPermission(FolderObject.SYSTEM_GLOBAL_FOLDER_ID, mailAdmin, new int[] {
				OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
				OCLPermission.ADMIN_PERMISSION }, true, cid, writeCon);
		createSingleUserPermission(FolderObject.SYSTEM_OX_PROJECT_FOLDER_ID, mailAdmin, new int[] {
				OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
				OCLPermission.ADMIN_PERMISSION }, true, cid, writeCon);
		addUserToOXFolders(mailAdmin, mailAdminDisplayName, language, cid, writeCon);
		if (LOG.isInfoEnabled()) {
			LOG.info(new StringBuilder("Folder rights for mail admin successfully added for context ").append(cid)
					.toString());
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

	private void createSingleUserPermission(final int fuid, final int userId, final int[] allPerms,
			final boolean isFolderAdmin, final int cid, final Connection writeCon) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = writeCon.prepareStatement(SQL_INSERT_SYSTEM_PERMISSION);
			stmt.setInt(1, cid);
			stmt.setInt(2, fuid);
			stmt.setInt(3, userId);
			stmt.setInt(4, allPerms[0]);
			stmt.setInt(5, allPerms[1]);
			stmt.setInt(6, allPerms[2]);
			stmt.setInt(7, allPerms[3]);
			stmt.setInt(8, isFolderAdmin ? 1 : 0);
			stmt.setInt(9, 0);
			stmt.executeUpdate();
			stmt.close();
			stmt = null;
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	private static final String STR_OXFOLDERTREE = "oxfolder_tree";

	private static final String STR_DELOXFOLDERTREE = "del_oxfolder_tree";

	private static final String STR_OXFOLDERPERMS = "oxfolder_permissions";

	private static final String SQL_DELETE_TABLE = "DELETE FROM #TABLE# WHERE cid = ?";

	/**
	 * Deletes all context associated folder in both working and backup tables
	 * from database
	 * 
	 * @param cid
	 *            The context ID
	 * @param readCon
	 *            A readable connection
	 * @param writeCon
	 *            A writeable connection
	 */
	public void deleteAllContextFolders(final int cid, final Connection readCon, final Connection writeCon) {
		try {
			final Set<String> oxfolderTables = new HashSet<String>();
			final Set<String> delOxfolderTables = new HashSet<String>();
			final DatabaseMetaData databaseMetaData = readCon.getMetaData();
			ResultSet rs = null;
			try {
				rs = databaseMetaData.getTables(null, null, "oxfolder_%", null);
				while (rs.next() && rs.getString(4).equals("TABLE")) {
					oxfolderTables.add(rs.getString(3));
				}
				rs = databaseMetaData.getTables(null, null, "del_oxfolder_%", null);
				while (rs.next() && rs.getString(4).equals("TABLE")) {
					delOxfolderTables.add(rs.getString(3));
				}
			} finally {
				if (rs != null) {
					rs.close();
					rs = null;
				}
			}
			/*
			 * Remove root tables
			 */
			final String rootTable = STR_OXFOLDERTREE;
			final String delRootTable = STR_DELOXFOLDERTREE;
			oxfolderTables.remove(rootTable);
			delOxfolderTables.remove(delRootTable);
			/*
			 * Delete tables with constraints to root tables
			 */
			final boolean performCommit = writeCon.getAutoCommit();
			if (performCommit) {
				writeCon.setAutoCommit(false);
			}
			final String tableReplaceLabel = STR_TABLE;
			PreparedStatement stmt = null;
			try {
				int size = oxfolderTables.size();
				Iterator<String> iter = oxfolderTables.iterator();
				for (int i = 0; i < size; i++) {
					final String tblName = iter.next();
					stmt = writeCon.prepareStatement(SQL_DELETE_TABLE.replaceFirst(tableReplaceLabel, tblName));
					stmt.setInt(1, cid);
					stmt.executeUpdate();
					stmt.close();
					stmt = null;
				}
				size = delOxfolderTables.size();
				iter = delOxfolderTables.iterator();
				for (int i = 0; i < size; i++) {
					final String tblName = iter.next();
					stmt = writeCon.prepareStatement(SQL_DELETE_TABLE.replaceFirst(tableReplaceLabel, tblName));
					stmt.setInt(1, cid);
					stmt.executeUpdate();
					stmt.close();
					stmt = null;
				}
				stmt = writeCon.prepareStatement(SQL_DELETE_TABLE.replaceFirst(tableReplaceLabel, rootTable));
				stmt.setInt(1, cid);
				stmt.executeUpdate();
				stmt.close();
				stmt = null;
				stmt = writeCon.prepareStatement(SQL_DELETE_TABLE.replaceFirst(tableReplaceLabel, delRootTable));
				stmt.setInt(1, cid);
				stmt.executeUpdate();
				stmt.close();
				stmt = null;
				if (performCommit) {
					writeCon.commit();
					writeCon.setAutoCommit(true);
				}
			} finally {
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
				if (performCommit) {
					writeCon.rollback();
					writeCon.setAutoCommit(true);
				}
			}
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private static final String SQL_UPDATE_FOLDER_TIMESTAMP = "UPDATE #FT# AS ot SET ot.changing_date = ? WHERE ot.cid = ? AND ot.fuid = ?";

	private static final String SQL_SELECT_FOLDER_IN_PERMISSIONS = "SELECT ot.fuid FROM #FT# AS ot JOIN #PT# as op ON ot.fuid = op.fuid AND ot.cid = ? AND op.cid = ? WHERE op.permission_id = ? GROUP BY ot.fuid";

	/**
	 * Propagates that a group has been modified throughout affected folders by
	 * touching folder's last-modified timestamp.
	 * 
	 * @param group
	 *            The affected group ID
	 * @param readCon
	 *            A readable connection to database
	 * @param writeCon
	 *            A writeable connection to database
	 * @param cid
	 *            The context ID
	 * @throws SQLException
	 *             If a SQL related error occurs
	 */
	public static void propagateGroupModification(final int group, final Connection readCon, final Connection writeCon,
			final int cid) throws SQLException {
		final long lastModified = System.currentTimeMillis();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			/*
			 * Touch all folder timestamps in whose permissions the group's
			 * entity identifier occurs
			 */
			stmt = readCon.prepareStatement(SQL_SELECT_FOLDER_IN_PERMISSIONS.replaceFirst("#FT#", STR_OXFOLDERTREE)
					.replaceFirst("#PT#", STR_OXFOLDERPERMS));
			stmt.setInt(1, cid);
			stmt.setInt(2, cid);
			stmt.setInt(3, group);
			rs = stmt.executeQuery();
			final List<Integer> list = new ArrayList<Integer>();
			while (rs.next()) {
				list.add(Integer.valueOf(rs.getInt(1)));
			}
			closeSQLStuff(rs, stmt);
			rs = null;
			stmt = null;
			if (!list.isEmpty()) {
				stmt = writeCon.prepareStatement(SQL_UPDATE_FOLDER_TIMESTAMP.replaceFirst("#FT#", STR_OXFOLDERTREE));
				do {
					final int fuid = list.remove(0).intValue();
					stmt.setLong(1, lastModified);
					stmt.setInt(2, cid);
					stmt.setInt(3, fuid);
					stmt.addBatch();
					if (FolderCacheManager.isInitialized()) {
						/*
						 * Remove from cache
						 */
						try {
							FolderCacheManager.getInstance().removeFolderObject(fuid, new ContextImpl(cid));
						} catch (final FolderCacheNotEnabledException e) {
							LOG.error("Folder could not be removed from cache", e);
						} catch (final OXException e) {
							LOG.error("Folder could not be removed from cache", e);
						}
					}
				} while (!list.isEmpty());
				stmt.executeBatch();
			}
		} finally {
			closeResources(rs, stmt, null, true, cid);
		}
	}

	/**
	 * Propagates modifications <b>already</b> performed on an existing user
	 * throughout folder module.
	 * 
	 * @param userId
	 *            The ID of the user who has been modified
	 * @param changedFields
	 *            The changed fields of the user taken from constants defined in
	 *            {@link ContactObject}; e.g.
	 *            {@link ContactObject#DISPLAY_NAME}
	 * @param lastModified
	 *            The last modified timestamp that should be taken on folder
	 *            modifications
	 * @param readCon
	 *            A readable connection if a writeable connection should not be
	 *            used for read access to database
	 * @param writeCon
	 *            A writeable connection
	 * @param cid
	 *            The context ID
	 * @throws OXException
	 *             If user's modifications could not be successfully propagated
	 */
	public static void propagateUserModification(final int userId, final int[] changedFields, final long lastModified,
			final Connection readCon, final Connection writeCon, final int cid) throws OXException {
		Arrays.sort(changedFields);
		final int adminID = getContextAdminID(cid, writeCon);
		if (Arrays.binarySearch(changedFields, ContactObject.DISPLAY_NAME) > -1) {
			propagateDisplayNameModification(userId, lastModified, adminID, readCon, writeCon, cid);
		}
	}

	private static void propagateDisplayNameModification(final int userId, final long lastModified,
			final int contextAdminID, final Connection readCon, final Connection writeCon, final int cid)
			throws OXException {
		final Context ctx = new ContextImpl(cid);
		/*
		 * Update shared folder's last modified timestamp
		 */
		try {
			OXFolderSQL.updateLastModified(FolderObject.SYSTEM_SHARED_FOLDER_ID, lastModified, contextAdminID,
					writeCon, ctx);
			/*
			 * Reload cache entry
			 */
			if (FolderCacheManager.isInitialized()) {
				/*
				 * Distribute remove among remote caches
				 */
				FolderCacheManager.getInstance().removeFolderObject(FolderObject.SYSTEM_SHARED_FOLDER_ID, ctx);
			}
		} catch (final SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(cid));
		} catch (final DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(cid));
		}
		/*
		 * Update user's default infostore folder name
		 */
		try {
			final int defaultInfostoreId = OXFolderSQL.getUserDefaultFolder(userId, FolderObject.INFOSTORE, readCon,
					ctx);
			OXFolderSQL.updateName(defaultInfostoreId, UserStorage.getInstance().getUser(userId, ctx).getDisplayName(),
					contextAdminID, userId, writeCon, ctx);
			/*
			 * Reload cache entry
			 */
			if (FolderCacheManager.isInitialized()) {
				/*
				 * Distribute remove among remote caches
				 */
				FolderCacheManager.getInstance().removeFolderObject(defaultInfostoreId, ctx);
			}
		} catch (final SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(cid));
		} catch (final DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(cid));
		} catch (final LdapException e) {
			throw new OXFolderException(FolderCode.LDAP_ERROR, e, Integer.valueOf(cid));
		}
	}

	private static final String DEFAULT_CAL_NAME = "My Calendar";

	private static final String DEFAULT_CON_NAME = "My Contacts";

	private static final String DEFAULT_TASK_NAME = "My Tasks";

	/**
	 * Creates default folders for modules task, calendar, contact, and
	 * infostore for given user ID
	 * 
	 * @param userId
	 *            The user ID
	 * @param displayName
	 *            The display name which is taken as folder name for user's
	 *            default infostore folder
	 * @param language
	 *            User's language which determines the translation of default
	 *            folder names
	 * @param cid
	 *            The context ID
	 * @param writeCon
	 *            A writeable connection to (master) database
	 * @throws OXException
	 *             If user's default folders could not be created successfully
	 */
	public void addUserToOXFolders(final int userId, final String displayName, final String language, final int cid,
			final Connection writeCon) throws OXException {
		try {
			final Context ctx = new ContextImpl(cid);
			final StringHelper strHelper = new StringHelper(LocaleTools.getLocale(language));
			/*
			 * Check infostore sibling
			 */                        
			if (OXFolderSQL.lookUpFolder(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, displayName, FolderObject.INFOSTORE,
					writeCon, ctx) != -1) {
				throw new OXFolderException(FolderCode.NO_DEFAULT_INFOSTORE_CREATE, displayName,
						FolderObject.SYSTEM_INFOSTORE_FOLDER_NAME, Integer
								.valueOf(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID), Integer.valueOf(ctx.getContextId()));
			}
			/*
			 * Proceed
			 */
			String defaultCalName = strHelper.getString(FolderStrings.DEFAULT_CALENDAR_FOLDER_NAME);
			if (defaultCalName == null || defaultCalName.length() == 0) {
				defaultCalName = DEFAULT_CAL_NAME;
			}
			String defaultConName = strHelper.getString(FolderStrings.DEFAULT_CONTACT_FOLDER_NAME);
			if (defaultConName == null || defaultCalName.length() == 0) {
				defaultConName = DEFAULT_CON_NAME;
			}
			String defaultTaskName = strHelper.getString(FolderStrings.DEFAULT_TASK_FOLDER_NAME);
			if (defaultTaskName == null || defaultTaskName.length() == 0) {
				defaultTaskName = DEFAULT_TASK_NAME;
			}
			/*
			 * GlobalConfig.loadLanguageCodes(propfile); String stdCalFolderName =
			 * GlobalConfig.getCode(language +
			 * "oxfolder_standardfolder_calendar"); if (stdCalFolderName == null ||
			 * stdCalFolderName.length() == 0) { stdCalFolderName = "My
			 * Calendar"; } String stdConFolderName =
			 * GlobalConfig.getCode(language +
			 * "oxfolder_standardfolder_contact"); if (stdConFolderName == null ||
			 * stdConFolderName.length() == 0) { stdConFolderName = "My
			 * Contacts"; } String stdTaskFolderName =
			 * GlobalConfig.getCode(language + "oxfolder_standardfolder_task");
			 * if (stdTaskFolderName == null || stdTaskFolderName.length() == 0) {
			 * stdTaskFolderName = "My Tasks"; }
			 */
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder("Folder names determined for default folders:\n\t").append("Calendar=")
						.append(defaultCalName).append("\tContact=").append(defaultConName).append("\tTask=").append(
								defaultTaskName).toString());
			}
			/*
			 * Insert default calendar folder
			 */
			final List<Integer> stdFolderIDs = new ArrayList<Integer>(4);
			final long creatingTime = System.currentTimeMillis();
			final OCLPermission defaultPerm = new OCLPermission();
			defaultPerm.setEntity(userId);
			defaultPerm.setGroupPermission(false);
			defaultPerm.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
					OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
			defaultPerm.setFolderAdmin(true);
			final FolderObject fo = new FolderObject();
			fo.setPermissionsAsArray(new OCLPermission[] { defaultPerm });
			fo.setDefaultFolder(true);
			fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
			fo.setType(FolderObject.PRIVATE);
			fo.setFolderName(defaultCalName);
			fo.setModule(FolderObject.CALENDAR);
			int newFolderId = OXFolderSQL.getNextSerial(ctx, writeCon);
			OXFolderSQL.insertDefaultFolderSQL(newFolderId, userId, fo, creatingTime, ctx, writeCon);
			stdFolderIDs.add(Integer.valueOf(newFolderId));
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder("User's default CALENDAR folder successfully created").toString());
			}
			/*
			 * Insert default contact folder
			 */
			fo.setFolderName(defaultConName);
			fo.setModule(FolderObject.CONTACT);
			newFolderId = OXFolderSQL.getNextSerial(ctx, writeCon);
			OXFolderSQL.insertDefaultFolderSQL(newFolderId, userId, fo, creatingTime, ctx, writeCon);
			stdFolderIDs.add(Integer.valueOf(newFolderId));
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder("User's default CONTACT folder successfully created").toString());
			}
			/*
			 * Insert default contact folder
			 */
			fo.setFolderName(defaultTaskName);
			fo.setModule(FolderObject.TASK);
			newFolderId = OXFolderSQL.getNextSerial(ctx, writeCon);
			OXFolderSQL.insertDefaultFolderSQL(newFolderId, userId, fo, creatingTime, ctx, writeCon);
			stdFolderIDs.add(Integer.valueOf(newFolderId));
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder("User's default TASK folder successfully created").toString());
			}
			/*
			 * Insert default infostore folder
			 */
			fo.reset();
			fo.setPermissionsAsArray(new OCLPermission[] { defaultPerm });
			fo.setDefaultFolder(true);
			fo.setParentFolderID(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);
			fo.setType(FolderObject.PUBLIC);
			fo.setFolderName(displayName);
			fo.setModule(FolderObject.INFOSTORE);
			newFolderId = OXFolderSQL.getNextSerial(ctx, writeCon);
			OXFolderSQL.insertDefaultFolderSQL(newFolderId, userId, fo, creatingTime, ctx, writeCon);
			stdFolderIDs.add(Integer.valueOf(newFolderId));
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder("User's default INFOSTORE folder successfully created").toString());
				LOG.info(new StringBuilder("All user default folders were successfully created").toString());
				/*
				 * TODO: Set standard special folders (projects, ...) located
				 * beneath system user folder
				 */
				LOG.info(new StringBuilder("User ").append(userId).append(" successfully created").append(
						" in context ").append(cid).toString());
			}
		} catch (final DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(cid));
		} catch (final SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(cid));
		}
	}

}
