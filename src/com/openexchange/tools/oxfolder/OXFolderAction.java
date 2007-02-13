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

import com.openexchange.groupware.calendar.CalendarCache;
import static com.openexchange.tools.sql.DBUtils.closeResources;

import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.api2.OXException;
import com.openexchange.cache.FolderCacheManager;
import com.openexchange.cache.FolderQueryCacheManager;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.IDGenerator;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.tasks.Tasks;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.i18n.StringHelper;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.EffectivePermission;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;

/**
 * OXFolderAction
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class OXFolderAction implements DeleteListener {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(OXFolderAction.class);

	/*
	 * SQL statements
	 */
	private static final String SQL_USER_PRIVATE_FOLDERS = "SELECT fuid FROM #TABLE# WHERE cid = ? AND created_from = ? AND type = ?";

	private static final String SQL_DELETE_PRIVATE_PERMISSIONS = "DELETE op FROM #PT# AS op, #FT# AS ot WHERE op.fuid = ot.fuid AND op.cid = ? AND ot.cid = ? AND ot.type = ? AND op.permission_id = ?";

	private static final String SQL_DELETE_PERMISSIONS = "DELETE FROM #TABLE# WHERE cid = ? AND permission_id IN #IDS#";

	private static final String SQL_DELETE_MAILADMIN_REFERENCES = "DELETE FROM #TABLE# WHERE cid = ? AND created_from = ?";

	private static final String SQL_DELETE_SPECIAL_FOLDERS = "DELETE FROM oxfolder_specialfolders WHERE cid = ?";

	private static final String GET_CONTEXT_MAILADMIN = "SELECT user FROM user_setting_admin WHERE cid = ?";

	private static final String SQL_UPDATE_PUBLIC_FOLDER_OWNER = "UPDATE #TABLE# SET created_from = ? WHERE cid = ? AND created_from = ? AND type = ?";

	private static final String SQL_UPDATE_PUBLIC_FOLDER_PERMISSIONS = "UPDATE #PT# AS op, #FT# AS ot SET op.permission_id = ? WHERE op.fuid = ot.fuid AND op.cid = ? AND ot.cid = ? AND permission_id = ? AND ot.type = ?";

	private static final Lock NEXTSERIAL_LOCK = new ReentrantLock();

	private SessionObject session;

	public OXFolderAction() {
		super();
	}

	public OXFolderAction(SessionObject session) {
		this.session = session;
	}

	public void cleanUpTestFolders(final int[] fuids, final Context ctx) {
		for (int i = 0; i < fuids.length; i++) {
			try {
				harddeleteOXFolder(fuids[i], ctx, null);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	private static final String STR_ANDFUID = " AND fuid = ";

	private void harddeleteOXFolder(final int folderId, final Context ctx, final Connection writeConArg)
			throws SQLException, DBPoolingException {
		Connection writeCon = writeConArg;
		final boolean createWriteCon = (writeCon == null);
		if (createWriteCon) {
			try {
				writeCon = DBPool.pickupWriteable(ctx);
			} catch (DBPoolingException e) {
				throw e;
			}
		}
		final boolean isAuto = writeCon.getAutoCommit();
		if (isAuto) {
			writeCon.setAutoCommit(false);
		}
		Statement stmt = null;
		try {
			final String andClause = STR_ANDFUID;
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

			if (FolderCacheManager.isEnabled() && FolderCacheManager.isInitialized()) {
				try {
					FolderCacheManager.getInstance().removeFolderObject(folderId, ctx);
				} catch (OXException e) {
					LOG.warn(e.getMessage(), e);
				}
			}
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
			if (isAuto) {
				writeCon.setAutoCommit(true);
			}
			if (createWriteCon && writeCon != null) {
				DBPool.closeWriterSilent(ctx, writeCon);
			}
		}
	}

	private static final String PREFIX_RENAME = "Folder renaming aborted: ";

	/**
	 * Renames given folder
	 */
	private boolean renameOXFolder(final FolderObject folderObj, final SessionObject sessionObj,
			final boolean checkPermissions, final long lastModified, final Context ctx, final Connection writeConArg)
			throws OXException {
		Connection readCon = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			final FolderObject prevFldObj;
			/*
			 * Load db version
			 */
			if (FolderCacheManager.isEnabled()) {
				prevFldObj = FolderCacheManager.getInstance().getFolderObject(folderObj.getObjectID(), true, ctx, null);
			} else {
				prevFldObj = FolderObject.loadFolderObjectFromDB(folderObj.getObjectID(), ctx);
			}
			/*
			 * Check if rename can be avoided (cause new name equals old one)
			 * and prevent default folder rename
			 */
			if (prevFldObj.getFolderName().equals(folderObj.getFolderName())) {
				return true;
			} else if (prevFldObj.isDefaultFolder()) {
				throw new OXFolderException(FolderCode.NO_DEFAULT_FOLDER_RENAME, PREFIX_RENAME,
						folderObj.getObjectID(), ctx.getContextId());
			}
			if (checkPermissions) {
				/*
				 * Check necessary permissions (visibility & admin access)
				 */
				final EffectivePermission perm = prevFldObj.getEffectiveUserPermission(sessionObj.getUserObject()
						.getId(), sessionObj.getUserConfiguration());
				if (!perm.isFolderVisible()) {
					throw new OXFolderException(FolderCode.NOT_VISIBLE, prevFldObj.getObjectID(), sessionObj
							.getUserObject().getId(), ctx.getContextId());
				} else if (!perm.isFolderAdmin()) {
					throw new OXFolderException(FolderCode.NO_ADMIN_ACCESS, sessionObj.getUserObject().getId(),
							prevFldObj.getObjectID(), ctx.getContextId());
				}
			}
			/*
			 * Check for duplicate folder
			 */
			readCon = DBPool.pickup(ctx);
			try {
				stmt = readCon
						.prepareStatement("SELECT fuid FROM oxfolder_tree WHERE (cid = ?) AND (parent = ?) AND (fname = ?) AND ((type = ?) OR (type = ? AND created_from = ?)) AND (module = ?)");
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, prevFldObj.getParentFolderID());
				stmt.setString(3, folderObj.getFolderName());
				stmt.setInt(4, FolderObject.PUBLIC);
				stmt.setInt(5, FolderObject.PRIVATE);
				stmt.setInt(6, sessionObj.getUserObject().getId());
				stmt.setInt(7, prevFldObj.getModule());
				rs = stmt.executeQuery();
				if (rs.next()) {
					/*
					 * A duplicate folder exsts
					 */
					throw new OXFolderException(FolderCode.NO_DUPLICATE_FOLDER, PREFIX_RENAME, prevFldObj
							.getParentFolderID(), ctx.getContextId());
				}
			} finally {
				closeResources(rs, stmt, readCon, true, ctx);
				rs = null;
				stmt = null;
				readCon = null;
			}
		} catch (SQLException se) {
			throw new OXFolderException(FolderCode.SQL_ERROR, se, true, ctx.getContextId());
		} catch (DBPoolingException dbe) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, dbe, true, ctx.getContextId());
		}
		/*
		 * Everything checked => rename folder
		 */
		Connection writeCon = writeConArg;
		final boolean createWriteCon = (writeCon == null);
		try {
			if (createWriteCon) {
				writeCon = DBPool.pickupWriteable(ctx);
			}
			try {
				int updatedrows = 0;
				final boolean isAuto = writeCon.getAutoCommit();
				if (isAuto) {
					writeCon.setAutoCommit(false);
				}
				PreparedStatement pst = null;
				try {
					pst = writeCon
							.prepareStatement("UPDATE oxfolder_tree SET fname = ?, changing_date = ?, changed_from = ? where cid = ? AND fuid = ?");
					pst.setString(1, folderObj.getFolderName());
					pst.setLong(2, lastModified);
					pst.setInt(3, sessionObj.getUserObject().getId());
					pst.setInt(4, sessionObj.getContext().getContextId());
					pst.setInt(5, folderObj.getObjectID());
					updatedrows = pst.executeUpdate();
					pst.close();
				} catch (SQLException sqle) {
					if (isAuto && writeCon != null) {
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
				if (updatedrows > 0) {
					/*
					 * Update cache
					 */
					if (FolderCacheManager.isInitialized()) {
						FolderCacheManager.getInstance().loadFolderObject(folderObj.getObjectID(),
								sessionObj.getContext(), readCon);
					}
					if (FolderQueryCacheManager.isInitialized()) {
						FolderQueryCacheManager.getInstance().invalidateUserQueries(sessionObj);
					}
                                        if (CalendarCache.isInitialized()) {
                                            CalendarCache.getInstance().invalidateGroup(sessionObj.getContext().getContextId());
                                        }
					return true;
				} else {
					return false;
				}
			} finally {
				if (createWriteCon && writeCon != null) {
					DBPool.closeWriterSilent(ctx, writeCon);
				}
			}
		} catch (DataTruncation e) {
			throw new OXFolderException(FolderCode.TRUNCATED, PREFIX_RENAME, e, false, folderObj.getFolderName());
		} catch (SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, PREFIX_RENAME, e, true, ctx.getContextId());
		} catch (DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, PREFIX_RENAME, e, true, ctx.getContextId());
		}
	}

	private static String PREFIX_CREATE = "Folder creation aborted: ";

	/**
	 * Creates a new folder
	 */
	public int createFolder(final FolderObject folderObj, final int userId, final int[] memberingroups,
			final UserConfiguration userConfig, final boolean checkPermissions, final boolean checkDuplicateFolder,
			final Context ctx, final Connection readConArg, final Connection writeCon, final boolean putIntoCache,
			final boolean allowConnectionFetch) throws OXException {
		if (!folderObj.containsFolderName() || folderObj.getFolderName() == null
				|| folderObj.getFolderName().length() == 0) {
			throw new OXFolderException(FolderCode.MISSING_FOLDER_ATTRIBUTE, PREFIX_CREATE, FolderFields.TITLE, "", ctx
					.getContextId());
		}
		if (folderObj.getPermissions() == null || folderObj.getPermissions().size() == 0) {
			throw new OXFolderException(FolderCode.MISSING_FOLDER_ATTRIBUTE, PREFIX_CREATE, FolderFields.PERMISSIONS,
					"", ctx.getContextId());
		}
		final FolderObject parentFolder;
		if (FolderCacheManager.isEnabled()) {
			parentFolder = FolderCacheManager.getInstance().getFolderObject(folderObj.getParentFolderID(), true, ctx,
					readConArg);
		} else {
			parentFolder = FolderObject.loadFolderObjectFromDB(folderObj.getParentFolderID(), ctx, readConArg);
		}
		try {
			if (checkPermissions) {
				/*
				 * Check, if user holds right to create a subfolder in given
				 * parent folder
				 */
				final EffectivePermission p = parentFolder.getEffectiveUserPermission(userId, userConfig);
				if (!p.canCreateSubfolders()) {
					if (p.getUnderlyingPermission().canCreateSubfolders()) {
						throw new OXFolderPermissionException(FolderCode.NO_CREATE_SUBFOLDER_PERMISSION, PREFIX_CREATE,
								userId, folderObj.getParentFolderID(), ctx.getContextId());
					}
					throw new OXFolderException(FolderCode.NO_CREATE_SUBFOLDER_PERMISSION, PREFIX_CREATE,
							Category.USER_CONFIGURATION, userId, folderObj.getParentFolderID(), ctx.getContextId());
				} else if (parentFolder.getType() == FolderObject.PUBLIC && !userConfig.hasFullPublicFolderAccess()) {
					throw new OXFolderException(FolderCode.NO_PUBLIC_FOLDER_WRITE_ACCESS, PREFIX_CREATE, userId,
							folderObj.getParentFolderID(), ctx.getContextId());
				}
			}
			/*
			 * Check folder types
			 */
			if (!checkFolderTypeAgainstParentType(parentFolder, folderObj.getType())) {
				throw new OXFolderLogicException(FolderCode.INVALID_TYPE, PREFIX_CREATE, folderObj.getParentFolderID(),
						folderObj.getType(), ctx.getContextId());
			}
			/*
			 * Check folder module
			 */
			if (!checkFolderModuleAgainstParentModule(parentFolder, folderObj.getModule())) {
				throw new OXFolderLogicException(FolderCode.INVALID_MODULE, PREFIX_CREATE, folderObj
						.getParentFolderID(), folderObj.getModule(), ctx.getContextId());
			}
			/*
			 * Check if admin exists
			 */
			final boolean isPrivate = (folderObj.getType() == FolderObject.PRIVATE);
			int adminPermissionCount = 0;
			final int permissionsSize = folderObj.getPermissions().size();
			final Iterator<OCLPermission> iter = folderObj.getPermissions().iterator();
			for (int i = 0; i < permissionsSize; i++) {
				final OCLPermission oclPerm = iter.next();
				if (oclPerm.isFolderAdmin()) {
					adminPermissionCount++;
					if (isPrivate && folderObj.getModule() != FolderObject.SYSTEM_MODULE) {
						if (adminPermissionCount > 1) {
							throw new OXFolderLogicException(FolderCode.ONLY_ONE_PRIVATE_FOLDER_ADMIN, PREFIX_CREATE);
						}
						if (oclPerm.isGroupPermission()) {
							throw new OXFolderLogicException(FolderCode.NO_PRIVATE_FOLDER_ADMIN_GROUP, PREFIX_CREATE);
						}
						if (folderObj.containsCreatedBy()) {
							if (folderObj.getCreatedBy() != oclPerm.getEntity()) {
								throw new OXFolderLogicException(FolderCode.ONLY_PRIVATE_FOLDER_OWNER_ADMIN,
										PREFIX_CREATE);
							}
						} else if (userId != oclPerm.getEntity()) {
							throw new OXFolderLogicException(FolderCode.ONLY_PRIVATE_FOLDER_OWNER_ADMIN, PREFIX_CREATE);
						}
					}
				}
			}
			if (adminPermissionCount == 0) {
				throw new OXFolderLogicException(FolderCode.NO_FOLDER_ADMIN, PREFIX_CREATE);
			}
			Connection readCon = readConArg;
			final boolean createReadCon = (readCon == null);
			if (createReadCon && !allowConnectionFetch) {
				throw new OXFolderException(FolderCode.NO_CONNECTION_FETCH, PREFIX_CREATE, ctx.getContextId());
			}
			if (checkDuplicateFolder) {
				/*
				 * Check if duplicate folder exists
				 */
				if (createReadCon) {
					readCon = DBPool.pickup(ctx);
				}
				PreparedStatement dplStmt = null;
				ResultSet dplRS = null;
				try {
					final String dplFolderSel = "SELECT fuid FROM oxfolder_tree WHERE cid = ? AND parent = ? AND fname = ? AND module = ? AND type = ?";
					dplStmt = readCon.prepareStatement(dplFolderSel);
					dplStmt.setInt(1, ctx.getContextId()); // cid
					dplStmt.setInt(2, folderObj.getParentFolderID()); // parent
					dplStmt.setString(3, folderObj.getFolderName()); // fname
					dplStmt.setInt(4, folderObj.getModule()); // module
					dplStmt.setInt(5, folderObj.getType()); // type
					dplRS = dplStmt.executeQuery();
					if (dplRS.next()) {
						/*
						 * A duplicate folder exsts
						 */
						throw new OXFolderException(FolderCode.NO_DUPLICATE_FOLDER, PREFIX_CREATE, folderObj
								.getParentFolderID(), ctx.getContextId());
					}
				} finally {
					closeResources(dplRS, dplStmt, createReadCon ? readCon : null, true, ctx);
				}
			}
			final long creatingTime = System.currentTimeMillis();
			final int fuid = insertFolderSQL(userId, folderObj, creatingTime, false, ctx, writeCon,
					allowConnectionFetch);
			/*
			 * Update folder objects and their cache entries
			 */
			final Date creatingDate = new Date(creatingTime);
			folderObj.setObjectID(fuid);
			folderObj.setCreationDate(creatingDate);
			folderObj.setCreatedBy(userId);
			folderObj.setLastModified(creatingDate);
			folderObj.setModifiedBy(userId);
			folderObj.setSubfolderFlag(false);
			folderObj.setDefaultFolder(false);
			parentFolder.setSubfolderFlag(true);
			parentFolder.setLastModified(creatingDate);
			if (FolderCacheManager.isEnabled()) {
				FolderCacheManager.getInstance().putFolderObject(parentFolder, ctx);
				if (putIntoCache) {
					FolderCacheManager.getInstance().putFolderObject(folderObj, ctx);
				}
			}
			if (FolderQueryCacheManager.isInitialized()) {
				FolderQueryCacheManager.getInstance().invalidateUserQueries(userId, ctx.getContextId());
			}
                        if (CalendarCache.isInitialized()) {
                            CalendarCache.getInstance().invalidateGroup(ctx.getContextId());
                        }                        
			return fuid;
		} catch (DataTruncation e) {
			throw new OXFolderException(FolderCode.TRUNCATED, PREFIX_RENAME, e, false, folderObj.getFolderName());
		} catch (SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, PREFIX_CREATE, e, true, ctx.getContextId());
		} catch (DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, PREFIX_CREATE, e, true, ctx.getContextId());
		}
	}

	private static final String SQL_INSERT_NEW_FOLDER = "INSERT INTO oxfolder_tree VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private static final String SQL_INSERT_NEW_PERMISSIONS = "INSERT INTO oxfolder_permissions (cid, fuid, permission_id, fp, orp, owp, odp, admin_flag, group_flag) VALUES (?,?,?,?,?,?,?,?,?)";

	private static final String SQL_UPDATE_PARENT_SUBFOLDER_FLAG = "UPDATE oxfolder_tree SET subfolder_flag = 1, changing_date = ? WHERE cid = ? AND fuid = ?";

	private int insertFolderSQL(final int userId, final FolderObject folderObj, final long creatingTime,
			final boolean acceptDefaultFlag, final Context ctx, final Connection writeConArg,
			final boolean allowConnectionFetch) throws SQLException, OXException, DBPoolingException {
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
		int fuid = -1;
		final boolean createWriteCon = (writeCon == null);
		if (createWriteCon && !allowConnectionFetch) {
			throw new OXFolderException(FolderCode.NO_CONNECTION_FETCH, PREFIX_CREATE, ctx.getContextId());
		}
		try {
			if (createWriteCon) {
				writeCon = DBPool.pickupWriteable(ctx);
			}
			fuid = getNextSerial(ctx, writeCon, allowConnectionFetch);
			if (fuid < FolderObject.MIN_FOLDER_ID) {
				throw new OXFolderException(FolderCode.INVALID_SEQUENCE_ID, PREFIX_CREATE, fuid,
						FolderObject.MIN_FOLDER_ID, ctx.getContextId());
			}
			final boolean isAuto = writeCon.getAutoCommit();
			if (isAuto) {
				writeCon.setAutoCommit(false);
			}
			try {
				PreparedStatement stmt = null;
				try {
					stmt = writeCon.prepareStatement(SQL_INSERT_NEW_FOLDER);
					stmt.setInt(1, fuid);
					stmt.setInt(2, ctx.getContextId());
					stmt.setInt(3, folderObj.getParentFolderID());
					stmt.setString(4, folderObj.getFolderName());
					stmt.setInt(5, folderObj.getModule());
					stmt.setInt(6, folderObj.getType());
					stmt.setLong(7, creatingTime);
					stmt.setInt(8, userId);
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
						if (ocl.getEntity() < 0) {
							throw new SQLException("Invalid entity found in folder permissions: " + ocl.getEntity()
									+ ". Folder insert aborted!");
						}
						stmt.setInt(1, ctx.getContextId());
						stmt.setInt(2, fuid);
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
					folderObj.setObjectID(fuid);
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
				if (isAuto && writeCon != null) {
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
			if (createWriteCon && writeCon != null) {
				DBPool.closeWriterSilent(ctx, writeCon);
			}
		}
		return fuid;
	}

	private boolean checkFolderTypeAgainstParentType(final FolderObject parentFolder, final int newFolderType) {
		final int enforcedType;
		switch (parentFolder.getObjectID()) {
		case FolderObject.SYSTEM_PRIVATE_FOLDER_ID:
			enforcedType = FolderObject.PRIVATE;
			break;
		case FolderObject.SYSTEM_PUBLIC_FOLDER_ID:
			enforcedType = FolderObject.PUBLIC;
			break;
		case FolderObject.SYSTEM_INFOSTORE_FOLDER_ID:
			enforcedType = FolderObject.PUBLIC;
			break;
		default:
			enforcedType = parentFolder.getType();
		}
		return (newFolderType == enforcedType);
	}

	private boolean checkFolderModuleAgainstParentModule(final FolderObject parentFolder, final int newFolderModule)
			throws OXException {
		final int[] sortedStandardModules = new int[] { FolderObject.TASK, FolderObject.CALENDAR, FolderObject.CONTACT };
		switch (parentFolder.getModule()) {
		case FolderObject.TASK:
		case FolderObject.CALENDAR:
		case FolderObject.CONTACT:
			return (Arrays.binarySearch(sortedStandardModules, newFolderModule) >= 0);
		case FolderObject.SYSTEM_MODULE:
			if (parentFolder.getObjectID() == FolderObject.SYSTEM_PRIVATE_FOLDER_ID
					|| parentFolder.getObjectID() == FolderObject.SYSTEM_PUBLIC_FOLDER_ID) {
				return (Arrays.binarySearch(sortedStandardModules, newFolderModule) >= 0);
			} else if (parentFolder.getObjectID() == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
				return (newFolderModule == FolderObject.INFOSTORE);
			}
			break;
		case FolderObject.PROJECT:
			return (newFolderModule == FolderObject.PROJECT);
		case FolderObject.INFOSTORE:
			return (newFolderModule == FolderObject.INFOSTORE);
		default:
			throw new OXFolderException(FolderCode.UNKNOWN_MODULE, PREFIX_CREATE, parentFolder.getModule(), "");
		}
		return true;
	}

	/**
	 * Create a folder
	 */
	public int createFolder(final FolderObject folderObj, final SessionObject sessionObj,
			final boolean checkPermissions, final Connection readCon, final Connection writeCon,
			final boolean putIntoCache) throws OXException {
		final int userId = sessionObj.getUserObject().getId();
		final int[] memberingroups = sessionObj.getUserObject().getGroups();
		final UserConfiguration userConfig = sessionObj.getUserConfiguration();
		final Context ctx = sessionObj.getContext();
		this.session = sessionObj;
		return createFolder(folderObj, userId, memberingroups, userConfig, checkPermissions, true, ctx, readCon,
				writeCon, putIntoCache, true);
	}

	private static final String PREFIX_UPDATE = "Folder update aborted: ";

	public FolderObject updateMoveRenameFolder(final FolderObject folderObjArg, final SessionObject sessionObj,
			final boolean checkPermissions, final long lastModified, final Connection readCon, final Connection writeCon)
			throws OXException {
		if (checkPermissions) {
			if (folderObjArg.containsType() && folderObjArg.getType() == FolderObject.PUBLIC
					&& !sessionObj.getUserConfiguration().hasFullPublicFolderAccess()) {
				throw new OXFolderException(FolderCode.NO_PUBLIC_FOLDER_WRITE_ACCESS, sessionObj.getUserObject()
						.getId(), folderObjArg.getObjectID(), sessionObj.getContext().getContextId());
			}
			/*
			 * Fetch effective permission from storage to leave folder objects
			 * as it is.
			 */
			final EffectivePermission perm = OXFolderTools.getEffectiveFolderOCL(folderObjArg.getObjectID(), sessionObj
					.getUserObject().getId(), sessionObj.getUserObject().getGroups(), sessionObj.getContext(),
					sessionObj.getUserConfiguration());
			if (!perm.isFolderVisible()) {
				if (!perm.getUnderlyingPermission().isFolderVisible()) {
					throw new OXFolderPermissionException(FolderCode.NOT_VISIBLE, folderObjArg.getObjectID(),
							sessionObj.getUserObject().getId(), sessionObj.getContext().getContextId());
				}
				throw new OXFolderException(FolderCode.NOT_VISIBLE, Category.USER_CONFIGURATION, folderObjArg
						.getObjectID(), sessionObj.getUserObject().getId(), sessionObj.getContext().getContextId());
			}
		}
		FolderObject folderObj = folderObjArg;
		this.session = sessionObj;
		boolean success = true;
		if (folderObj.containsPermissions()) {
			if (folderObj.containsParentFolderID()) {
				success = moveFolder(folderObj.getObjectID(), folderObj.getParentFolderID(), sessionObj.getUserObject()
						.getId(), sessionObj.getUserObject().getGroups(), sessionObj.getUserConfiguration(),
						lastModified, sessionObj.getContext(), writeCon);
			}
			if (success) {
				success &= updateFolder(folderObj, sessionObj, checkPermissions, false, lastModified, readCon, writeCon);
			}
		} else {
			if (folderObj.containsFolderName()) {
				success = renameOXFolder(folderObj, sessionObj, checkPermissions, lastModified,
						sessionObj.getContext(), writeCon);
			}
			if (success && folderObj.containsParentFolderID()) {
				success &= moveFolder(folderObj.getObjectID(), folderObj.getParentFolderID(), sessionObj
						.getUserObject().getId(), sessionObj.getUserObject().getGroups(), sessionObj
						.getUserConfiguration(), lastModified, sessionObj.getContext(), writeCon);
			}
		}
		/*
		 * Update folder object and its cache entry on success
		 */
		if (success) {
			folderObj = FolderObject.loadFolderObjectFromDB(folderObj.getObjectID(), sessionObj.getContext());
			if (FolderCacheManager.isInitialized()) {
				FolderCacheManager.getInstance().putFolderObject(folderObj, sessionObj.getContext(), true, null);
			}
			if (FolderQueryCacheManager.isInitialized()) {
				FolderQueryCacheManager.getInstance().invalidateUserQueries(sessionObj);
			}
                        if (CalendarCache.isInitialized()) {
                            CalendarCache.getInstance().invalidateGroup(sessionObj.getContext().getContextId());
                        }                        
			return folderObj;
		} else {
			throw new OXFolderException(FolderCode.UPDATE_FAILED, PREFIX_UPDATE, folderObjArg.getObjectID(), sessionObj
					.getContext().getContextId());
		}
	}

	/**
	 * Updates a folder
	 */
	private boolean updateFolder(final FolderObject folderObj, final SessionObject sessionObj,
			final boolean checkPermissions, final boolean updateCache, final long lastModified,
			final Connection readCon, final Connection writeCon) throws OXException {
		final int userId = sessionObj.getUserObject().getId();
		final int[] memberingroups = sessionObj.getUserObject().getGroups();
		final Context ctx = sessionObj.getContext();
		return updateFolder(folderObj, userId, memberingroups, checkPermissions, updateCache, lastModified, ctx,
				readCon, writeCon);
	}

	private boolean updateFolder(FolderObject folderObj, final int userId, final int[] memberingroups,
			final boolean checkPermissions, final boolean updateCache, final long lastModified, final Context ctx,
			final Connection readConArg, final Connection writeCon) throws OXException {
		if (folderObj.getObjectID() <= 0) {
			throw new OXFolderException(FolderCode.INVALID_OBJECT_ID, PREFIX_UPDATE, folderObj.getObjectID());
		}
		if (folderObj.getPermissions() == null || folderObj.getPermissions().size() == 0) {
			throw new OXFolderException(FolderCode.MISSING_FOLDER_ATTRIBUTE, PREFIX_UPDATE, FolderFields.PERMISSIONS,
					folderObj.getObjectID(), ctx.getContextId());
		}
		Connection readCon = readConArg;
		Statement stmtTmp = null;
		final boolean createReadCon = (readCon == null);
		try {
			try {
				if (createReadCon) {
					readCon = DBPool.pickup(ctx);
				}
				ResultSet rs = null;
				if (checkPermissions) {
					try {
						/*
						 * Check, if user holds right to update current folder
						 */
						final String permissionIds = StringCollection.getSqlInString(userId, memberingroups);
						stmtTmp = readCon.createStatement();
						rs = stmtTmp.executeQuery("SELECT permission_id FROM oxfolder_permissions WHERE cid = "
								+ ctx.getContextId() + STR_ANDFUID + folderObj.getObjectID() + " AND permission_id IN "
								+ permissionIds + " AND admin_flag = 1");
						if (!rs.next()) {
							throw new OXFolderPermissionException(FolderCode.NO_ADMIN_ACCESS, PREFIX_UPDATE, userId,
									folderObj.getObjectID(), ctx.getContextId());
						}
					} finally {
						closeResources(rs, stmtTmp, createReadCon ? readCon : null, true, ctx);
						rs = null;
						stmtTmp = null;
						if (createReadCon && readCon != null) {
							readCon = null;
						}
					}
				}
				/*
				 * Check parent
				 */
				if (createReadCon && readCon == null) {
					readCon = DBPool.pickup(ctx);
				}
				try {
					if (folderObj.containsParentFolderID()
							&& OXFolderTools.getFolderParent(folderObj.getObjectID(), ctx, null) != folderObj
									.getParentFolderID()) {
						throw new OXFolderLogicException(FolderCode.NO_MOVE_THROUGH_UPDATE, PREFIX_UPDATE, folderObj
								.getObjectID());
					}
				} finally {
					if (createReadCon && readCon != null) {
						DBPool.closeReaderSilent(ctx, readCon);
						readCon = null;
					}
				}
				/*
				 * Check folder name
				 */
				if (createReadCon && readCon == null) {
					readCon = DBPool.pickup(ctx);
				}
				try {
					if (OXFolderTools.getFolderDefaultFlag(folderObj.getObjectID(), ctx, null)
							&& folderObj.containsFolderName()
							&& !folderObj.getFolderName().equals(
									OXFolderTools.getFolderName(folderObj.getObjectID(), ctx, null))) {
						throw new OXFolderException(FolderCode.NO_DEFAULT_FOLDER_RENAME, PREFIX_UPDATE, folderObj
								.getObjectID(), ctx.getContextId());
					}
				} finally {
					if (createReadCon && readCon != null) {
						DBPool.closeReaderSilent(ctx, readCon);
						readCon = null;
					}
				}
				/*
				 * Check Permissions
				 */
				if (createReadCon && readCon == null) {
					readCon = DBPool.pickup(ctx);
				}
				stmtTmp = readCon.createStatement();
				rs = stmtTmp.executeQuery(new StringBuilder("SELECT fuid FROM oxfolder_tree WHERE cid = ").append(
						ctx.getContextId()).append(STR_ANDFUID).append(folderObj.getObjectID()).toString());
				try {
					if (!rs.next()) {
						throw new OXFolderNotFoundException(PREFIX_UPDATE, folderObj.getObjectID(), ctx.getContextId());
					}
				} finally {
					closeResources(rs, stmtTmp, createReadCon ? readCon : null, true, ctx);
					rs = null;
					stmtTmp = null;
					if (createReadCon && readCon != null) {
						readCon = null;
					}
				}
				/*
				 * Check if admin exists
				 */
				final int type = OXFolderTools.getFolderType(folderObj.getObjectID(), userId, ctx);
				final boolean isPrivate = (type == FolderObject.PRIVATE || type == FolderObject.SHARED);
				int adminPermissionCount = 0;
				final int permissionsSize = folderObj.getPermissions().size();
				final Iterator<OCLPermission> iter = folderObj.getPermissions().iterator();
				for (int i = 0; i < permissionsSize; i++) {
					final OCLPermission oclPerm = iter.next();
					if (oclPerm.isFolderAdmin()) {
						adminPermissionCount++;
						if (isPrivate && folderObj.getModule() != FolderObject.SYSTEM_MODULE) {
							if (adminPermissionCount > 1) {
								throw new OXFolderLogicException(FolderCode.ONLY_ONE_PRIVATE_FOLDER_ADMIN,
										PREFIX_UPDATE);
							}
							if (oclPerm.isGroupPermission()) {
								throw new OXFolderLogicException(FolderCode.NO_PRIVATE_FOLDER_ADMIN_GROUP,
										PREFIX_UPDATE);
							}
							if (OXFolderTools.getFolderOwner(folderObj.getObjectID(), ctx, null) != oclPerm.getEntity()) {
								throw new OXFolderLogicException(FolderCode.ONLY_PRIVATE_FOLDER_OWNER_ADMIN,
										PREFIX_UPDATE);
							}
						}
					}
				}
				if (adminPermissionCount == 0) {
					throw new OXFolderLogicException(FolderCode.NO_FOLDER_ADMIN, PREFIX_UPDATE);
				}
				/*
				 * Check if shared
				 */
				if (type == FolderObject.SHARED) {
					throw new OXFolderException(FolderCode.NO_SHARED_FOLDER_UPDATE, PREFIX_UPDATE, folderObj
							.getObjectID(), ctx.getContextId());
				}
			} finally {
				if (createReadCon && null != readCon) {
					DBPool.push(ctx, readCon);
				}
			}
			updateFolderSQL(userId, folderObj, lastModified, ctx, writeCon);
			if (updateCache) {
				/*
				 * Update folder object and its cache entry
				 */
				folderObj = FolderObject.loadFolderObjectFromDB(folderObj.getObjectID(), ctx);
				if (FolderCacheManager.isInitialized()) {
					FolderCacheManager.getInstance().removeFolderObject(folderObj.getObjectID(), ctx);
					FolderCacheManager.getInstance().putFolderObject(folderObj, ctx, true, null);
				}
			}
			if (FolderQueryCacheManager.isInitialized()) {
				FolderQueryCacheManager.getInstance().invalidateUserQueries(userId, ctx.getContextId());
			}
                        if (CalendarCache.isInitialized()) {
                            CalendarCache.getInstance().invalidateGroup(ctx.getContextId());
                        }                        
			return true;
		} catch (DataTruncation e) {
			throw new OXFolderException(FolderCode.TRUNCATED, PREFIX_RENAME, e, false, new Object[0]);
		} catch (SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, PREFIX_UPDATE, ctx.getContextId());
		} catch (DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, PREFIX_UPDATE, ctx.getContextId());
		}
	}

	private static final String SQL_UPDATE_WITH_FOLDERNAME = "UPDATE oxfolder_tree SET fname = ?, changing_date = ?, changed_from = ?, permission_flag = ? WHERE cid = ? AND fuid = ?";

	private static final String SQL_UPDATE_WITHOUT_FOLDERNAME = "UPDATE oxfolder_tree SET changing_date = ?, changed_from = ?, permission_flag = ? WHERE cid = ? AND fuid = ?";

	private static final String SQL_DELETE_EXISTING_PERMISSIONS = "DELETE FROM oxfolder_permissions WHERE cid = ? AND fuid = ?";

	private void updateFolderSQL(final int userId, final FolderObject folderObj, final long lastModified,
			final Context ctx, final Connection writeConArg) throws SQLException, DBPoolingException, OXException {
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
		final boolean createWriteCon = (writeCon == null);
		try {
			if (createWriteCon) {
				writeCon = DBPool.pickupWriteable(ctx);
			}
			final boolean isAuto = writeCon.getAutoCommit();
			if (isAuto) {
				writeCon.setAutoCommit(false);
			}
			try {
				PreparedStatement stmt = null;
				if (folderObj.containsFolderName()
						&& (folderObj.getFolderName() == null || folderObj.getFolderName().replaceAll("\\s", "")
								.length() == 0)) {
					throw new OXFolderException(FolderCode.MISSING_FOLDER_ATTRIBUTE, PREFIX_UPDATE, FolderFields.TITLE,
							folderObj.getObjectID(), ctx.getContextId());
				}
				if (folderObj.containsFolderName()) {
					stmt = writeCon.prepareStatement(SQL_UPDATE_WITH_FOLDERNAME);
					stmt.setString(1, folderObj.getFolderName());
					stmt.setLong(2, lastModified);
					stmt.setInt(3, userId);
					stmt.setInt(4, permissionFlag);
					stmt.setInt(5, ctx.getContextId());
					stmt.setInt(6, folderObj.getObjectID());
					final int count = stmt.executeUpdate();
					stmt.close();
					stmt = null;
					if (count == 0) {
						throw new OXFolderNotFoundException(PREFIX_UPDATE, folderObj.getObjectID(), ctx.getContextId());
					}
				} else {
					stmt = writeCon.prepareStatement(SQL_UPDATE_WITHOUT_FOLDERNAME);
					stmt.setLong(1, lastModified);
					stmt.setInt(2, userId);
					stmt.setInt(3, permissionFlag);
					stmt.setInt(4, ctx.getContextId());
					stmt.setInt(5, folderObj.getObjectID());
					final int count = stmt.executeUpdate();
					stmt.close();
					stmt = null;
					if (count == 0) {
						throw new OXFolderNotFoundException(PREFIX_UPDATE, folderObj.getObjectID(), ctx.getContextId());
					}
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
					if (oclPerm.getEntity() < 0) {
						throw new OXFolderException(FolderCode.INVALID_ENTITY, PREFIX_UPDATE, oclPerm.getEntity(),
								folderObj.getObjectID(), ctx.getContextId());
					}
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
				if (isAuto && writeCon != null) {
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
			if (createWriteCon && writeCon != null) {
				DBPool.closeWriterSilent(ctx, writeCon);
			}
		}
	}

	private static final String PREFIX_MOVE = "Folder move aborted: ";

	/**
	 * Moves folder matching to given source folder id to folder matching to
	 * given target folder id
	 */
	private boolean moveFolder(final int folderId, final int targetFolderId, final int userId, final int[] groups,
			final UserConfiguration userConfig, final long lastModified, final Context ctx, final Connection writeConArg)
			throws OXException {
		try {
			Connection readCon = DBPool.pickup(ctx);
			Statement tmpStmt = null;
			ResultSet tmpRS = null;
			final FolderObject fldObj;
			final FolderObject targetFolder;
			try {
				/*
				 * Load folder object from storage data. Throws a NOT_EXISTS
				 * exception if none found.
				 */
				if (FolderCacheManager.isEnabled()) {
					fldObj = FolderCacheManager.getInstance().getFolderObject(folderId, true, ctx, null);
				} else {
					fldObj = FolderObject.loadFolderObjectFromDB(folderId, ctx, null, true, true);
				}
				/*
				 * Folder is already in target folder
				 */
				if (fldObj.getParentFolderID() == targetFolderId) {
					return true;
				}
				/*
				 * Default folder must not be moved
				 */
				if (fldObj.isDefaultFolder()) {
					throw new OXFolderException(FolderCode.NO_DEFAULT_FOLDER_MOVE, PREFIX_MOVE, folderId, ctx
							.getContextId());
				}
				/*
				 * A duplicate folder already exists in target folder
				 */
				if (FolderCacheManager.isEnabled()) {
					targetFolder = FolderCacheManager.getInstance().getFolderObject(targetFolderId, true, ctx, readCon);
				} else {
					targetFolder = FolderObject.loadFolderObjectFromDB(targetFolderId, ctx, readCon, true, true);
				}
				final StringBuilder sqlBuilder = new StringBuilder("SELECT fname FROM oxfolder_tree").append(
						" WHERE (cid = ").append(ctx.getContextId()).append(") AND (fname = '").append(
						fldObj.getFolderName()).append("') AND (parent = ").append(targetFolderId).append(
						") AND ((type = ").append(FolderObject.PUBLIC).append(") OR (type = ").append(
						FolderObject.PRIVATE).append(" AND created_from = ").append(fldObj.getCreatedBy()).append("))");
				tmpStmt = readCon.createStatement();
				tmpRS = tmpStmt.executeQuery(sqlBuilder.toString());
				if (tmpRS.next()) {
					throw new OXFolderException(FolderCode.TARGET_FOLDER_CONTAINS_DUPLICATE, PREFIX_MOVE, targetFolder
							.getObjectID(), ctx.getContextId());
				}
				tmpStmt.close();
				tmpRS.close();
				tmpStmt = null;
				tmpRS = null;
				/*
				 * Check a bunch of possible errors
				 */
				final OCLPermission folderPerm = fldObj.getEffectiveUserPermission(userId, userConfig);
				final OCLPermission targetFolderPerm = targetFolder.getEffectiveUserPermission(userId, userConfig);
				if (fldObj.isShared(userId)) {
					throw new OXFolderException(FolderCode.NO_SHARED_FOLDER_MOVE, PREFIX_MOVE, fldObj.getObjectID(),
							ctx.getContextId());
				} else if (targetFolder.isShared(userId)) {
					throw new OXFolderException(FolderCode.NO_SHARED_FOLDER_TARGET, PREFIX_MOVE, targetFolder
							.getObjectID(), ctx.getContextId());
				} else if (fldObj.getType() == FolderObject.SYSTEM_TYPE) {
					throw new OXFolderException(FolderCode.NO_SYSTEM_FOLDER_MOVE, PREFIX_MOVE, fldObj.getObjectID(),
							ctx.getContextId());
				} else if (fldObj.getType() == FolderObject.PRIVATE
						&& ((targetFolder.getType() == FolderObject.PUBLIC || (targetFolder.getType() == FolderObject.SYSTEM_TYPE && targetFolderId != FolderObject.SYSTEM_PRIVATE_FOLDER_ID)))) {
					throw new OXFolderException(FolderCode.ONLY_PRIVATE_TO_PRIVATE_MOVE, PREFIX_MOVE, fldObj
							.getObjectID(), ctx.getContextId());
				} else if (fldObj.getType() == FolderObject.PUBLIC
						&& ((targetFolder.getType() == FolderObject.PRIVATE || (targetFolder.getType() == FolderObject.SYSTEM_TYPE && targetFolderId != FolderObject.SYSTEM_PUBLIC_FOLDER_ID)))) {
					throw new OXFolderException(FolderCode.ONLY_PUBLIC_TO_PUBLIC_MOVE, PREFIX_MOVE, fldObj
							.getObjectID(), ctx.getContextId());
				} else if (fldObj.getModule() == FolderObject.INFOSTORE
						&& targetFolder.getModule() != FolderObject.INFOSTORE) {
					throw new OXFolderException(FolderCode.INCOMPATIBLE_MODULES, fldObj.getModule(), targetFolder
							.getModule());
				} else if (fldObj.getModule() != FolderObject.INFOSTORE
						&& targetFolder.getModule() == FolderObject.INFOSTORE) {
					throw new OXFolderException(FolderCode.INCOMPATIBLE_MODULES, fldObj.getModule(), targetFolder
							.getModule());
				} else if (!folderPerm.isFolderAdmin()) {
					throw new OXFolderPermissionException(FolderCode.NO_ADMIN_ACCESS, PREFIX_MOVE, userId, fldObj
							.getObjectID(), ctx.getContextId());
				} else if (targetFolderPerm.getFolderPermission() < OCLPermission.CREATE_SUB_FOLDERS) {
					throw new OXFolderPermissionException(FolderCode.NO_CREATE_SUBFOLDER_PERMISSION, PREFIX_MOVE,
							userId, targetFolder.getObjectID(), ctx.getContextId());
				} else if (folderId == targetFolderId) {
					throw new OXFolderPermissionException(FolderCode.NO_EQUAL_MOVE, PREFIX_MOVE, ctx.getContextId());
				}
				/*
				 * Check if source folder has subfolders
				 */
				if (fldObj.hasSubfolders()) {
					/*
					 * Check if target is a descendant folder
					 */
					final List<Integer> parentIDList = new ArrayList<Integer>(1);
					parentIDList.add(Integer.valueOf(fldObj.getObjectID()));
					if (isDescendantFolder(parentIDList, targetFolderId, readCon, ctx)) {
						throw new OXFolderException(FolderCode.NO_SUBFOLDER_MOVE, PREFIX_MOVE, folderId, ctx
								.getContextId());
					}
					/*
					 * Count all moveable subfolders: TODO: Recursive check???
					 */
					tmpStmt = readCon.createStatement();
					tmpRS = tmpStmt
							.executeQuery(new StringBuilder(300)
									.append(
											"SELECT COUNT(ot.fuid) FROM oxfolder_tree AS ot JOIN oxfolder_permissions AS op ON ot.fuid = op.fuid AND ot.cid = ")
									.append(ctx.getContextId()).append(" AND op.cid = ").append(ctx.getContextId())
									.append(" WHERE op.permission_id IN ").append(
											StringCollection.getSqlInString(userId, groups)).append(
											" AND op.admin_flag > 0 AND ot.parent = ").append(folderId).toString());
					int numOfMoveableSubfolders = 0;
					while (tmpRS.next()) {
						numOfMoveableSubfolders = tmpRS.getInt(1);
					}
					if (numOfMoveableSubfolders != fldObj.getSubfolderIds().size()) {
						throw new OXFolderPermissionException(FolderCode.NO_SUBFOLDER_MOVE_ACCESS, PREFIX_MOVE, userId,
								folderId, ctx.getContextId());
					}
				}
			} finally {
				closeResources(tmpRS, tmpStmt, readCon, true, ctx);
				tmpRS = null;
				tmpStmt = null;
				readCon = null;
			}
			/*
			 * Finally move folder to target position
			 */
			Connection writeCon = writeConArg;
			final boolean createWriteCon = (writeCon == null);
			Connection tmpReadCon = null;
			PreparedStatement pst = null;
			ResultSet subFolderRS = null;
			try {
				tmpReadCon = DBPool.pickup(ctx);
				if (createWriteCon) {
					writeCon = DBPool.pickupWriteable(ctx);
				}
				final boolean isAuto = writeCon.getAutoCommit();
				if (isAuto) {
					writeCon.setAutoCommit(false);
				}
				try {
					String updateStr = "UPDATE oxfolder_tree SET parent = ?, changing_date = ?, changed_from = ? WHERE cid = ? AND fuid = ?";
					pst = writeCon.prepareStatement(updateStr);
					pst.setInt(1, targetFolderId);
					pst.setLong(2, lastModified);
					pst.setInt(3, fldObj.getType() == FolderObject.SYSTEM_TYPE ? ctx.getMailadmin() : userId);
					pst.setInt(4, ctx.getContextId());
					pst.setInt(5, folderId);
					pst.executeUpdate();
					pst.close();
					pst = null;
					/*
					 * Set target/source folder's subfolder flag
					 */
					pst = tmpReadCon.prepareStatement("SELECT fuid FROM oxfolder_tree WHERE cid = ? AND parent = ?");
					pst.setInt(1, ctx.getContextId());
					pst.setInt(2, folderId);
					subFolderRS = pst.executeQuery();
					final boolean srcHasSubfolders = subFolderRS.next();
					subFolderRS.close();
					subFolderRS = null;
					pst.close();
					pst = null;
					updateStr = "UPDATE oxfolder_tree SET subfolder_flag = ?, changing_date = ?, changed_from = ? WHERE cid = ? AND fuid = ?";
					pst = writeCon.prepareStatement(updateStr);
					pst.setInt(1, 1);
					pst.setLong(2, lastModified);
					pst.setInt(3, targetFolder.getType() == FolderObject.SYSTEM_TYPE ? ctx.getMailadmin() : userId);
					pst.setInt(4, ctx.getContextId());
					pst.setInt(5, targetFolderId);
					pst.addBatch();
					pst.setInt(1, srcHasSubfolders ? 1 : 0);
					pst.setLong(2, lastModified);
					pst.setInt(3, fldObj.getType() == FolderObject.SYSTEM_TYPE ? ctx.getMailadmin() : userId);
					pst.setInt(4, ctx.getContextId());
					pst.setInt(5, folderId);
					pst.addBatch();
					pst.executeBatch();
					pst.close();
					pst = null;
				} catch (SQLException se) {
					if (isAuto && writeCon != null) {
						writeCon.rollback();
						writeCon.setAutoCommit(true);
					}
					throw se;
				}
				if (isAuto) {
					writeCon.commit();
					writeCon.setAutoCommit(true);
				}
				/*
				 * Update cache entries
				 */
				if (FolderCacheManager.isInitialized()) {
					FolderCacheManager.getInstance().loadFolderObject(folderId, ctx, null);
					FolderCacheManager.getInstance().loadFolderObject(targetFolderId, ctx, null);
				}
				if (FolderQueryCacheManager.isInitialized()) {
					FolderQueryCacheManager.getInstance().invalidateUserQueries(userId, ctx.getContextId());
				}
                                if (CalendarCache.isInitialized()) {
                                    CalendarCache.getInstance().invalidateGroup(ctx.getContextId());
                                }                                
			} finally {
				if (subFolderRS != null) {
					subFolderRS.close();
					subFolderRS = null;
				}
				if (pst != null) {
					pst.close();
					pst = null;
				}
				if (createWriteCon && writeCon != null) {
					DBPool.closeWriterSilent(ctx, writeCon);
				}
				if (tmpReadCon != null) {
					DBPool.closeReaderSilent(ctx, tmpReadCon);
				}
			}
			return true;
		} catch (SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, PREFIX_MOVE, ctx.getContextId());
		} catch (DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, PREFIX_MOVE, ctx.getContextId());
		}
	}

	private final boolean isDescendantFolder(final List<Integer> parentIDList, final int possibleDescendant,
			final Connection readCon, final Context ctx) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		final int size = parentIDList.size();
		final Iterator<Integer> iter = parentIDList.iterator();
		boolean isDescendant = false;
		for (int i = 0; i < size && !isDescendant; i++) {
			try {
				stmt = readCon.prepareStatement("SELECT fuid FROM oxfolder_tree WHERE cid = ? AND parent = ?");
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, iter.next().intValue());
				rs = stmt.executeQuery();
				if (rs.next()) {
					/*
					 * At least one subfolder
					 */
					final List<Integer> subfolderIDs = new ArrayList<Integer>();
					do {
						final int current = rs.getInt(1);
						isDescendant |= (current == possibleDescendant);
						subfolderIDs.add(Integer.valueOf(current));
					} while (rs.next() && !isDescendant);
					/*
					 * Close resources
					 */
					rs.close();
					rs = null;
					stmt.close();
					stmt = null;
					if (isDescendant) {
						/*
						 * Matching descendant found
						 */
						return true;
					} else {
						/*
						 * Recursive call with collected subfolder ids
						 */
						isDescendant = isDescendantFolder(subfolderIDs, possibleDescendant, readCon, ctx);
					}
				} else {
					continue;
				}
			} finally {
				closeResources(rs, stmt, null, true, null);
			}
		}
		return isDescendant;
	}

	public void deleteFolder(final int folderId, final SessionObject sessionObj, final boolean checkPermissions,
			final long lastModified) throws OXException {
		deleteFolder(folderId, sessionObj, null, checkPermissions, lastModified);
	}

	public void deleteFolder(final int folderId, final SessionObject sessionObj, final Connection writeCon,
			final boolean checkPermissions, final long lastModified) throws OXException {
		this.session = sessionObj;
		deleteFolder(folderId, sessionObj.getUserObject().getId(), sessionObj.getUserObject().getGroups(), sessionObj
				.getUserConfiguration(), checkPermissions, sessionObj.getContext(), writeCon, lastModified);
	}

	private static final String PREFIX_DELETE = "Folder deletion aborted: ";

	/**
	 * Deletes all subfolders, contained objects and the specified folder itself
	 * while checking permissions for each item
	 * 
	 */
	public void deleteFolder(final int folderId, final int userId, final int[] groups,
			final UserConfiguration userConfig, final boolean checkPermissions, final Context ctx,
			final Connection writeConArg, final long lastModified) throws OXException {
		Connection readCon = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			if (checkPermissions) {
				try {
					readCon = DBPool.pickup(ctx);
					/*
					 * Folder does not exist
					 */
					stmt = readCon.createStatement();
					rs = stmt.executeQuery(new StringBuilder("SELECT fname FROM oxfolder_tree WHERE cid = ").append(
							ctx.getContextId()).append(STR_ANDFUID).append(folderId).toString());
					if (!rs.next()) {
						throw new OXFolderNotFoundException(PREFIX_DELETE, folderId, ctx.getContextId());
					}
				} finally {
					closeResources(rs, stmt, readCon, true, ctx);
					rs = null;
					stmt = null;
					readCon = null;
				}
				/*
				 * Check type
				 */
				if (OXFolderTools.getFolderModule(folderId, ctx) == FolderObject.PUBLIC
						&& !userConfig.hasFullPublicFolderAccess()) {
					throw new OXFolderException(FolderCode.NO_PUBLIC_FOLDER_WRITE_ACCESS, PREFIX_DELETE, userId,
							folderId, ctx.getContextId());
				}
			}
			/*
			 * Get parent folder id in advance to later modify cache entries.
			 */
			final int parentId = OXFolderTools.getFolderParent(folderId, ctx, null);
			/*
			 * Gather all deleteable subfolders
			 */
			final HashMap<Integer, HashMap> deleteableFolders;
			deleteableFolders = gatherDeleteableFolders(folderId, userId, userConfig, StringCollection.getSqlInString(
					userId, groups), ctx);
			/*
			 * Delete folders
			 */
			deleteValidatedFolders(deleteableFolders, userId, groups, userConfig, lastModified, ctx, writeConArg, readCon);
			final FolderObject parentFolder = FolderObject.loadFolderObjectFromDB(parentId, ctx);
			/*
			 * Invalidate user queries
			 */
			if (FolderQueryCacheManager.isInitialized()) {
				FolderQueryCacheManager.getInstance().invalidateUserQueries(userId, ctx.getContextId());
			}
                        if (CalendarCache.isInitialized()) {
                            CalendarCache.getInstance().invalidateGroup(ctx.getContextId());
                        }                        
			/*
			 * Check parent subfolder flag
			 */
			try {
				readCon = DBPool.pickup(ctx);
				/*
				 * Folder does not exist
				 */
				stmt = readCon.createStatement();
				rs = stmt.executeQuery(new StringBuilder("SELECT fuid FROM oxfolder_tree WHERE cid = ").append(
						ctx.getContextId()).append(" AND parent = ").append(parentId).toString());
				/*
				 * Update parent folder and its cache entry if no more
				 * subfolders exist after this delete operation
				 */
				if (!rs.next()) {
					parentFolder.setSubfolderFlag(false);
					if (FolderCacheManager.isEnabled()) {
						FolderCacheManager.getInstance().putFolderObject(parentFolder, ctx);
					}
					rs.close();
					rs = null;
					stmt.close();
					stmt = null;
					DBPool.closeReaderSilent(ctx, readCon);
					readCon = null;
					Connection writeCon = writeConArg;
					final boolean createWriteCon = (writeCon == null);
					try {
						if (createWriteCon) {
							writeCon = DBPool.pickupWriteable(ctx);
						}
						stmt = writeCon.createStatement();
						stmt.executeUpdate(new StringBuilder(
								"UPDATE oxfolder_tree SET subfolder_flag = 0, changing_date = ").append(lastModified)
								.append(" WHERE cid = ").append(ctx.getContextId()).append(STR_ANDFUID)
								.append(parentId).toString());
					} finally {
						closeResources(null, stmt, createWriteCon ? writeCon : null, false, ctx);
						stmt = null;
						writeCon = null;
					}
				}
			} finally {
				closeResources(rs, stmt, readCon, true, ctx);
				rs = null;
				stmt = null;
				readCon = null;
			}
		} catch (DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, PREFIX_DELETE, e, true, ctx.getContextId());
		} catch (SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, PREFIX_DELETE, e, true, ctx.getContextId());
		}
	}

	@SuppressWarnings("unchecked")
	private void deleteValidatedFolders(final HashMap<Integer, HashMap> deleteableIDs, final int userId,
			final int[] groups, final UserConfiguration userConf, final long lastModified, final Context ctx,
			final Connection writeCon, final Connection readCon) throws OXException, DBPoolingException, SQLException {
		final int deleteableIDsSize = deleteableIDs.size();
		final Iterator<Map.Entry<Integer, HashMap>> iter = deleteableIDs.entrySet().iterator();
		for (int i = 0; i < deleteableIDsSize; i++) {
			final Map.Entry<Integer, HashMap> entry = iter.next();
			final Integer folderID = entry.getKey();
			final HashMap<Integer, HashMap> hashMap = entry.getValue();
			/*
			 * Delete subfolders first, if any exist
			 */
			if (hashMap != null) {
				deleteValidatedFolders(hashMap, userId, groups, userConf, lastModified, ctx, writeCon, readCon);
			}
			deleteValidatedFolder(folderID.intValue(), userId, groups, userConf, lastModified, ctx, writeCon, readCon);
		}
	}

	private void deleteValidatedFolder(final int folderID, final int userId, final int[] groups,
			final UserConfiguration userConf, final long lastModified, final Context ctx, final Connection writeConArg,
			final Connection readConArg) throws OXException, SQLException, DBPoolingException {
		/*
		 * Delete folder
		 */
		Connection readCon = readConArg;
		Connection writeCon = writeConArg;
		final boolean createReadCon = (readCon == null);
		final boolean createWriteCon = (writeCon == null);
		if (createReadCon) {
			readCon = DBPool.pickup(ctx);
		}
		if (createWriteCon) {
			writeCon = DBPool.pickupWriteable(ctx);
		}
		try {
			final boolean isAuto = writeCon.getAutoCommit();
			if (isAuto) {
				writeCon.setAutoCommit(false);
			}
			try {
				final int module = OXFolderTools.getFolderModule(folderID, ctx, readCon);
				switch (module) {
				case FolderObject.CALENDAR:
					final CalendarSql cSql = new CalendarSql(session);
					cSql.deleteAppointmentsInFolder(folderID);
					break;
				case FolderObject.TASK:
					final Tasks tasks = Tasks.getInstance();
					tasks.deleteTasksInFolder(session, folderID);
					break;
				case FolderObject.CONTACT:
					Contacts.trashContactsFromFolder(folderID, userId, groups, session, readCon, writeCon, false);
					break;
				case FolderObject.INFOSTORE:
					final InfostoreFacade db = new InfostoreFacadeImpl(new DBPoolProvider());
					db.removeDocument(folderID, System.currentTimeMillis(), session);
					break;
				case FolderObject.PROJECT:
					// TODO: Delete all projects in project folder
					break;
				default:
					throw new OXFolderException(FolderCode.UNKNOWN_MODULE, PREFIX_DELETE, module, ctx.getContextId());
				}
				delWorkingOXFolder(folderID, userId, lastModified, ctx, writeCon);
				if (FolderCacheManager.isInitialized()) {
					FolderCacheManager.getInstance().removeFolderObject(folderID, ctx);
				}
				if (isAuto) {
					writeCon.commit();
					writeCon.setAutoCommit(true);
				}
			} catch (OXException e) {
				if (isAuto && writeCon != null) {
					writeCon.rollback();
					writeCon.setAutoCommit(true);
				}
				throw e;
			} catch (SQLException e) {
				if (isAuto && writeCon != null) {
					writeCon.rollback();
					writeCon.setAutoCommit(true);
				}
				throw e;
			} catch (DBPoolingException e) {
				if (isAuto && writeCon != null) {
					writeCon.rollback();
					writeCon.setAutoCommit(true);
				}
				throw e;
			}
		} finally {
			if (createReadCon && readCon != null) {
				DBPool.push(ctx, readCon);
			}
			if (createWriteCon && writeCon != null) {
				DBPool.pushWrite(ctx, writeCon);
			}
		}
	}

	/**
	 * Deletes a folder entry - and its corresponding permission entries as well -
	 * from working tables in storage and creates backup entries.
	 */
	private void delWorkingOXFolder(final int folderId, final int userId, final long lastModified, final Context ctx,
			final Connection writeConArg) throws SQLException, DBPoolingException, OXException {
		delOXFolder(folderId, userId, lastModified, true, true, ctx, writeConArg);
	}

	private static final String STR_OXFOLDERTREE = "oxfolder_tree";

	private static final String STR_DELOXFOLDERTREE = "del_oxfolder_tree";

	private static final String STR_OXFOLDERPERMS = "oxfolder_permissions";

	private static final String STR_DELOXFOLDERPERMS = "del_oxfolder_permissions";

	/**
	 * Deletes a folder entry - and its corresponding permission entries as well -
	 * from underlying storage. <code>deleteWorking</code> determines whether
	 * working or backup tables are affected by delete operation.
	 * <code>createBackup</code> specifies if backup entries are going to be
	 * created and is only allowed if <code>deleteWorking</code> is set to
	 * <code>true</code>.
	 */
	private void delOXFolder(final int folderId, final int userId, final long lastModified,
			final boolean deleteWorking, final boolean createBackup, final Context ctx, final Connection writeConArg)
			throws SQLException, DBPoolingException, OXException {
		Connection writeCon = writeConArg;
		final boolean createWriteCon = (writeCon == null);
		if (createWriteCon) {
			writeCon = DBPool.pickupWriteable(ctx);
		}
		final boolean isAuto = writeCon.getAutoCommit();
		if (isAuto) {
			writeCon.setAutoCommit(false);
		}
		final String folderTable = deleteWorking ? STR_OXFOLDERTREE : STR_DELOXFOLDERTREE;
		final String permTable = deleteWorking ? STR_OXFOLDERPERMS : STR_DELOXFOLDERPERMS;
		final StringBuilder stmtBuilder = new StringBuilder(150);
		final boolean backup = (createBackup && deleteWorking);
		Statement stmt = null;
		try {
			stmt = writeCon.createStatement();
			if (backup) {
				/*
				 * Copy backup entries into del_oxfolder_tree and
				 * del_oxfolder_permissions
				 */
				stmt.addBatch(stmtBuilder.append(
						"INSERT INTO del_oxfolder_tree SELECT * FROM oxfolder_tree WHERE cid = ").append(
						ctx.getContextId()).append(" AND fuid =").append(folderId).toString());
				stmtBuilder.setLength(0);
				stmt.addBatch(stmtBuilder.append(
						"INSERT INTO del_oxfolder_permissions SELECT * FROM oxfolder_permissions WHERE cid = ").append(
						ctx.getContextId()).append(" AND fuid =").append(folderId).toString());
				stmtBuilder.setLength(0);
			}
			if (deleteWorking) {
				/*
				 * Delete from oxfolder_specialfolders
				 */
				stmt.addBatch(stmtBuilder.append("DELETE FROM oxfolder_specialfolders WHERE cid = ").append(
						ctx.getContextId()).append(STR_ANDFUID).append(folderId).toString());
				stmtBuilder.setLength(0);
			}
			/*
			 * Delete from permission table
			 */
			stmt.addBatch(stmtBuilder.append("DELETE FROM ").append(permTable).append(" WHERE cid = ").append(
					ctx.getContextId()).append(" AND (fuid = ").append(folderId).append(')').toString());
			stmtBuilder.setLength(0);
			/*
			 * Delete from folder table
			 */
			stmt.addBatch(stmtBuilder.append("DELETE FROM ").append(folderTable).append(" WHERE cid = ").append(
					ctx.getContextId()).append(" AND (fuid = ").append(folderId).append(')').toString());
			stmtBuilder.setLength(0);
			/*
			 * Execute batch
			 */
			stmt.executeBatch();
			stmt.close();
			stmt = null;
			if (backup) {
				/*
				 * Update last modifed timestamp of entries in backup tables
				 */
				PreparedStatement prepStmt = null;
				try {
					prepStmt = writeCon
							.prepareStatement("UPDATE del_oxfolder_tree SET changing_date = ?, changed_from = ? WHERE cid = ? AND fuid = ?");
					prepStmt.setLong(1, lastModified);
					prepStmt.setInt(2, userId);
					prepStmt.setInt(3, ctx.getContextId());
					prepStmt.setInt(4, folderId);
					prepStmt.executeUpdate();
				} finally {
					if (prepStmt != null) {
						prepStmt.close();
						prepStmt = null;
					}
				}
			}
			/*
			 * Commit
			 */
			if (isAuto) {
				writeCon.commit();
			}
			if (FolderCacheManager.isEnabled() && FolderCacheManager.isInitialized()) {
				try {
					FolderCacheManager.getInstance().removeFolderObject(folderId, ctx);
				} catch (OXException e) {
					if (LOG.isWarnEnabled()) {
						LOG.warn(e.getMessage(), e);
					}
				}
			}
			if (FolderQueryCacheManager.isInitialized()) {
				FolderQueryCacheManager.getInstance().invalidateUserQueries(userId, ctx.getContextId());
			}
                        if (CalendarCache.isInitialized()) {
                            CalendarCache.getInstance().invalidateGroup(ctx.getContextId());
                        }                        
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
			if (isAuto) {
				writeCon.setAutoCommit(true);
			}
			if (createWriteCon && writeCon != null) {
				DBPool.closeWriterSilent(ctx, writeCon);
			}
		}
	}

	/**
	 * Gathers all deleteable folders
	 */
	private HashMap<Integer, HashMap> gatherDeleteableFolders(final int folderID, final int userId,
			final UserConfiguration userConfig, final String permissionIDs, final Context ctx) throws OXException,
			DBPoolingException, SQLException {
		final HashMap<Integer, HashMap> deleteableIDs = new HashMap<Integer, HashMap>();
		gatherDeleteableSubfoldersRecursively(folderID, userId, userConfig, permissionIDs, deleteableIDs, ctx);
		return deleteableIDs;
	}

	/**
	 * Gathers all deleteable folders
	 */
	private void gatherDeleteableSubfoldersRecursively(final int folderID, final int userId,
			final UserConfiguration userConfig, final String permissionIDs,
			final HashMap<Integer, HashMap> deleteableIDs, final Context ctx) throws OXException, DBPoolingException,
			SQLException {
		final FolderObject delFolderWithSubfolderList = FolderObject.loadFolderObjectFromDB(folderID, ctx, null, true,
				true);
		/*
		 * Check if shared
		 */
		if (delFolderWithSubfolderList.isShared(userId)) {
			throw new OXFolderPermissionException(FolderCode.NO_SHARED_FOLDER_DELETION, PREFIX_DELETE, userId,
					folderID, ctx.getContextId());
		}
		/*
		 * Check if marked as default folder
		 */
		if (delFolderWithSubfolderList.isDefaultFolder()) {
			throw new OXFolderPermissionException(FolderCode.NO_DEFAULT_FOLDER_DELETION, PREFIX_DELETE, userId,
					folderID, ctx.getContextId());
		}
		/*
		 * Check user's effective permission
		 */
		final EffectivePermission effectivePerm = delFolderWithSubfolderList.getEffectiveUserPermission(userId,
				userConfig);
		if (!effectivePerm.isFolderVisible()) {
			if (!effectivePerm.getUnderlyingPermission().isFolderVisible()) {
				throw new OXFolderPermissionException(FolderCode.NOT_VISIBLE, PREFIX_DELETE, folderID, userId, ctx
						.getContextId());
			}
			throw new OXFolderException(FolderCode.NOT_VISIBLE, PREFIX_DELETE, Category.USER_CONFIGURATION, folderID,
					userId, ctx.getContextId());
		} else if (!effectivePerm.isFolderAdmin()) {
			if (!effectivePerm.getUnderlyingPermission().isFolderAdmin()) {
				throw new OXFolderPermissionException(FolderCode.NO_ADMIN_ACCESS, PREFIX_DELETE, userId, folderID, ctx
						.getContextId());
			}
			throw new OXFolderException(FolderCode.NO_ADMIN_ACCESS, PREFIX_DELETE, Category.USER_CONFIGURATION, userId,
					folderID, ctx.getContextId());
		}
		/*
		 * Check delete permission on folder's objects
		 */
		if (!OXFolderTools.canDeleteAllObjectsInFolder(delFolderWithSubfolderList, session)) {
			throw new OXFolderPermissionException(FolderCode.NOT_ALL_OBJECTS_DELETION, PREFIX_DELETE, userId, folderID,
					ctx.getContextId());
		}
		/*
		 * Check, if folder has subfolders
		 */
		if (!delFolderWithSubfolderList.hasSubfolders()) {
			deleteableIDs.put(Integer.valueOf(folderID), null);
			return;
		}
		/*
		 * No subfolders detected
		 */
		if (delFolderWithSubfolderList.getSubfolderIds().size() == 0) {
			deleteableIDs.put(Integer.valueOf(folderID), null);
			return;
		}
		final HashMap<Integer, HashMap> subMap = new HashMap<Integer, HashMap>();
		final int size = delFolderWithSubfolderList.getSubfolderIds().size();
		final Iterator<Integer> it = delFolderWithSubfolderList.getSubfolderIds().iterator();
		for (int i = 0; i < size; i++) {
			final int fuid = it.next().intValue();
			gatherDeleteableSubfoldersRecursively(fuid, userId, userConfig, permissionIDs, subMap, ctx);
		}
		deleteableIDs.put(Integer.valueOf(folderID), subMap);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.delete.DeleteListener#deletePerformed(com.openexchange.groupware.delete.DeleteEvent,
	 *      java.sql.Connection, java.sql.Connection)
	 */
	public void deletePerformed(final DeleteEvent delEvent, final Connection readConArg, final Connection writeConArg)
			throws DeleteFailedException {
		Connection readCon = readConArg;
		Connection writeCon = writeConArg;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		if (delEvent.getType() == DeleteEvent.TYPE_USER) {
			boolean performTransaction = true;
			try {
				final Context ctx = delEvent.getContext();
				final boolean createReadCon = (readCon == null);
				if (createReadCon) {
					readCon = DBPool.pickup(ctx);
				}
				final boolean createWriteCon = (writeCon == null);
				if (createWriteCon) {
					writeCon = DBPool.pickupWriteable(ctx);
				}
				try {
					performTransaction = writeCon.getAutoCommit();
					if (performTransaction) {
						writeCon.setAutoCommit(false);
					}
					final int userId = delEvent.getId();
					/*
					 * Get context's mailadmin
					 */
					int mailadmin = ctx.getMailadmin();
					if (mailadmin == -1) {
						stmt = readCon.prepareStatement(GET_CONTEXT_MAILADMIN);
						stmt.setInt(1, ctx.getContextId());
						rs = stmt.executeQuery();
						if (!rs.next()) {
							throw new OXFolderException(FolderCode.NO_ADMIN_USER_FOUND_IN_CONTEXT, ctx.getContextId());
						}
						mailadmin = rs.getInt(1);
					}
					final boolean isMailAdmin = (mailadmin == userId);
					/*
					 * Delete all private folders (task, calendar & contact)
					 */
					deleteEntityPrivateFolders(userId, readCon, writeCon, ctx);
					/*
					 * Delete all infostore folders
					 */
					deleteEntityInfostoreFolders(userId, readCon, writeCon, ctx);
					if (isMailAdmin) {
						/*
						 * Delete all mailadmin permissions
						 */
						deleteMailAdminPermissions(userId, readCon, writeCon, ctx);
						/*
						 * Delete all mailadmin folders
						 */
						deleteMailAdminFolders(userId, readCon, writeCon, ctx);
					} else {
						/*
						 * Reassign ownership of all public folders to mailadmin
						 */
						reassignEntityPublicFolders(userId, mailadmin, readCon, writeCon, ctx);
					}
					if (performTransaction) {
						writeCon.commit();
						writeCon.setAutoCommit(true);
					}
				} finally {
					closeResources(rs, stmt, createReadCon ? readCon : null, true, ctx);
					rs = null;
					stmt = null;
					if (createWriteCon && writeCon != null) {
						DBPool.pushWrite(ctx, writeCon);
					}
				}
			} catch (OXException e) {
				try {
					if (performTransaction && writeCon != null) {
						writeCon.rollback();
						writeCon.setAutoCommit(true);
					}

				} catch (SQLException e1) {
					LOG.warn(e1.getMessage(), e1);
				}
				LOG.error(e.getMessage(), e);
				throw new DeleteFailedException(e);
			} catch (SQLException e) {
				try {
					if (performTransaction && writeCon != null) {
						writeCon.rollback();
						writeCon.setAutoCommit(true);
					}

				} catch (SQLException e1) {
					LOG.warn(e1.getMessage(), e1);
				}
				LOG.error(e.getMessage(), e);
				throw new DeleteFailedException(e);
			} catch (DBPoolingException e) {
				try {
					if (performTransaction && writeCon != null) {
						writeCon.rollback();
						writeCon.setAutoCommit(true);
					}

				} catch (SQLException e1) {
					LOG.warn(e1.getMessage(), e1);
				}
				LOG.error(e.getMessage(), e);
				throw new DeleteFailedException(e);
			}
		} else if (delEvent.getType() == DeleteEvent.TYPE_GROUP) {
			boolean isAuto = true;
			try {
				final Context ctx = delEvent.getContext();
				final boolean createReadCon = (readCon == null);
				final boolean createWriteCon = (writeCon == null);
				if (createReadCon) {
					readCon = DBPool.pickup(ctx);
				}
				if (createWriteCon) {
					writeCon = DBPool.pickupWriteable(ctx);
				}
				try {
					isAuto = writeCon.getAutoCommit();
					if (isAuto) {
						writeCon.setAutoCommit(false);
					}
					final int groupId = delEvent.getId();
					/*
					 * Delete all private folders (task, calendar & contact)
					 */
					deleteEntityPrivateFolders(groupId, readCon, writeCon, ctx);
					/*
					 * Delete all infostore folders
					 */
					deleteEntityInfostoreFolders(groupId, readCon, writeCon, ctx);
					/*
					 * Get context's mailadmin
					 */
					int mailadmin = ctx.getMailadmin();
					if (mailadmin == -1) {
						stmt = readCon.prepareStatement(GET_CONTEXT_MAILADMIN);
						stmt.setInt(1, ctx.getContextId());
						rs = stmt.executeQuery();
						if (!rs.next()) {
							throw new OXFolderException(FolderCode.NO_ADMIN_USER_FOUND_IN_CONTEXT, ctx.getContextId());
						}
						mailadmin = rs.getInt(1);
					}
					/*
					 * Reassign ownership of all public folders to mailadmin
					 */
					reassignEntityPublicFolders(groupId, mailadmin, readCon, writeCon, ctx);
					if (isAuto) {
						writeCon.commit();
						writeCon.setAutoCommit(true);
					}
				} finally {
					closeResources(rs, stmt, createReadCon ? readCon : null, true, ctx);
					rs = null;
					stmt = null;
					if (createWriteCon && writeCon != null) {
						DBPool.pushWrite(ctx, writeCon);
					}
				}
			} catch (OXException e) {
				try {
					if (isAuto && writeCon != null) {
						writeCon.rollback();
						writeCon.setAutoCommit(true);
					}

				} catch (SQLException e1) {
					LOG.warn(e1.getMessage(), e1);
				}
				LOG.error(e.getMessage(), e);
				throw new DeleteFailedException(e);
			} catch (SQLException e) {
				try {
					if (isAuto && writeCon != null) {
						writeCon.rollback();
						writeCon.setAutoCommit(true);
					}

				} catch (SQLException e1) {
					LOG.warn(e1.getMessage(), e1);
				}
				LOG.error(e.getMessage(), e);
				throw new DeleteFailedException(e);
			} catch (DBPoolingException e) {
				try {
					if (isAuto && writeCon != null) {
						writeCon.rollback();
						writeCon.setAutoCommit(true);
					}

				} catch (SQLException e1) {
					LOG.warn(e1.getMessage(), e1);
				}
				LOG.error(e.getMessage(), e);
				throw new DeleteFailedException(e);
			}
		}
	}

	/**
	 * Deletes all user's private folders in both working and backup tables
	 */
	private final void deleteEntityPrivateFolders(final int entityID, final Connection readCon,
			final Connection writeCon, final Context ctx) throws SQLException, DBPoolingException, OXException {
		deleteEntityFolders(entityID, true, readCon, writeCon, ctx);
		deleteEntityFolders(entityID, false, readCon, writeCon, ctx);
	}

	/**
	 * Deletes user's default infostore folder (and its subfolders) in both
	 * working and backup tables
	 */
	private final void deleteEntityInfostoreFolders(final int entityID, final Connection readCon,
			final Connection writeCon, final Context ctx) throws SQLException, DBPoolingException, OXException {
		deleteEntityDefaultInfostoreFolder(entityID, true, readCon, writeCon, ctx);
		deleteEntityDefaultInfostoreFolder(entityID, false, readCon, writeCon, ctx);
	}

	/**
	 * Deletes all mailadmin's permissions in both working and backup tables
	 */
	private final void deleteMailAdminPermissions(final int mailAdmin, final Connection readCon,
			final Connection writeCon, final Context ctx) throws SQLException {
		deleteMailAdminPermissions(mailAdmin, true, readCon, writeCon, ctx);
		deleteMailAdminPermissions(mailAdmin, false, readCon, writeCon, ctx);
	}

	/**
	 * Deletes all mailadmin folders in both working and backup tables
	 */
	private final void deleteMailAdminFolders(final int mailAdmin, final Connection readCon, final Connection writeCon,
			final Context ctx) throws SQLException {
		deleteMailAdminFolders(mailAdmin, true, readCon, writeCon, ctx);
		deleteMailAdminFolders(mailAdmin, false, readCon, writeCon, ctx);
	}

	private static final String STR_TABLE = "#TABLE#";

	private final void deleteEntityFolders(final int entityID, final boolean deleteWorking, final Connection readCon,
			final Connection writeCon, final Context ctx) throws SQLException, DBPoolingException, OXException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			final String folderTable = deleteWorking ? STR_OXFOLDERTREE : STR_DELOXFOLDERTREE;
			final String permTable = deleteWorking ? STR_OXFOLDERPERMS : STR_DELOXFOLDERPERMS;
			/*
			 * Determine & delete all default infostore folder
			 */
			stmt = readCon.prepareStatement(new StringBuilder(SQL_USER_PRIVATE_FOLDERS.replaceFirst(STR_TABLE,
					folderTable)).toString());
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, entityID);
			stmt.setInt(3, FolderObject.PRIVATE);
			rs = stmt.executeQuery();
			final long lastModified = System.currentTimeMillis();
			int fuid = -1;
			while (rs.next()) {
				fuid = rs.getInt(1);
				delOXFolder(fuid, entityID, lastModified, deleteWorking, false, ctx, writeCon);
			}
			closeResources(rs, stmt, null, true, ctx);
			/*
			 * Delete single permission entries for given user on private
			 * folders (shared rights)
			 */
			stmt = writeCon.prepareStatement(new StringBuilder(SQL_DELETE_PRIVATE_PERMISSIONS.replaceFirst("#PT#",
					permTable).replaceFirst("#FT#", folderTable)).toString());
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, ctx.getContextId());
			stmt.setInt(3, FolderObject.PRIVATE);
			stmt.setInt(4, entityID);
			stmt.executeUpdate();
			stmt.close();
			stmt = null;
		} finally {
			closeResources(rs, stmt, null, true, null);
		}
	}

	private final void deleteEntityDefaultInfostoreFolder(final int entityID, final boolean deleteWorking,
			final Connection readCon, final Connection writeCon, final Context ctx) throws SQLException,
			DBPoolingException, OXException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			final String folderTable = deleteWorking ? STR_OXFOLDERTREE : STR_DELOXFOLDERTREE;
			/*
			 * Determine & delete all default infostore folder
			 */
			stmt = readCon.prepareStatement(new StringBuilder(SQL_USER_PRIVATE_FOLDERS.replaceFirst(STR_TABLE,
					folderTable)).append(
					new StringBuilder(" AND module = ").append(FolderObject.INFOSTORE).append(" AND default_flag = 1")
							.toString()).toString());
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, entityID);
			stmt.setInt(3, FolderObject.PUBLIC);
			rs = stmt.executeQuery();
			final long lastModified = System.currentTimeMillis();
			int fuid = -1;
			while (rs.next()) {
				fuid = rs.getInt(1);
				delOXFolder(fuid, entityID, lastModified, deleteWorking, false, ctx, writeCon);
			}
			closeResources(rs, stmt, null, true, ctx);
			/*
			 * Recursive deletion of default infostore folder's subfolders
			 */
			final LinkedList<Integer> parentIds = new LinkedList<Integer>();
			parentIds.add(Integer.valueOf(fuid));
			while (!parentIds.isEmpty()) {
				final int parent = parentIds.removeFirst();
				final List<Integer> subIds = new ArrayList<Integer>();
				PreparedStatement tmpStmt = null;
				ResultSet tmpRs = null;
				try {
					tmpStmt = readCon.prepareStatement(new StringBuilder(150).append("SELECT fuid FROM ").append(
							folderTable).append(" WHERE cid = ? AND parent = ? ORDER BY fuid").toString());
					tmpStmt.setInt(1, ctx.getContextId());
					tmpStmt.setInt(2, parent);
					tmpRs = tmpStmt.executeQuery();
					while (tmpRs.next()) {
						subIds.add(Integer.valueOf(tmpRs.getInt(1)));
					}
				} finally {
					closeResources(tmpRs, tmpStmt, null, true, ctx);
				}
				final int size = subIds.size();
				for (int i = 0; i < size; i++) {
					delOXFolder(subIds.get(i).intValue(), entityID, lastModified, deleteWorking, false, ctx, writeCon);
				}
				parentIds.addAll(subIds);
			}
			/*
			 * Delete single permission entries for given user on infostore
			 * folders (shared rights)
			 */
			final String permTable = deleteWorking ? STR_OXFOLDERPERMS : STR_DELOXFOLDERPERMS;
			stmt = writeCon.prepareStatement(new StringBuilder(SQL_DELETE_PRIVATE_PERMISSIONS.replaceFirst("#PT#",
					permTable).replaceFirst("#FT#", folderTable)).append(
					new StringBuilder(" AND module = ").append(FolderObject.INFOSTORE).toString()).toString());
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, ctx.getContextId());
			stmt.setInt(3, FolderObject.PUBLIC);
			stmt.setInt(4, entityID);
			stmt.executeUpdate();
			stmt.close();
			stmt = null;
		} finally {
			closeResources(rs, stmt, null, true, null);
		}
	}

	private final void reassignEntityPublicFolders(final int entityID, final int mailadmin, final Connection readCon,
			final Connection writeCon, final Context ctx) throws SQLException {
		reassignEntityPublicFolders(entityID, mailadmin, true, readCon, writeCon, ctx);
		reassignEntityPublicFolders(entityID, mailadmin, false, readCon, writeCon, ctx);
	}

	private final void reassignEntityPublicFolders(final int entityID, final int mailadmin,
			final boolean deleteWorking, final Connection readCon, final Connection writeCon, final Context ctx)
			throws SQLException {
		PreparedStatement stmt = null;
		try {
			final String folderTable = deleteWorking ? STR_OXFOLDERTREE : STR_DELOXFOLDERTREE;
			final String permTable = deleteWorking ? STR_OXFOLDERPERMS : STR_DELOXFOLDERPERMS;
			/*
			 * Set public folder ownership on mailadmin
			 */
			stmt = writeCon.prepareStatement(SQL_UPDATE_PUBLIC_FOLDER_OWNER.replaceFirst(STR_TABLE, folderTable));
			stmt.setInt(1, mailadmin);
			stmt.setInt(2, ctx.getContextId());
			stmt.setInt(3, entityID);
			stmt.setInt(4, FolderObject.PUBLIC);
			stmt.executeUpdate();
			stmt.close();
			stmt = null;
			/*
			 * Set public folder permission on mailadmin
			 */
			stmt = writeCon.prepareStatement(SQL_UPDATE_PUBLIC_FOLDER_PERMISSIONS.replaceFirst("#PT#", permTable)
					.replaceFirst("#FT#", folderTable));
			stmt.setInt(1, mailadmin);
			stmt.setInt(2, ctx.getContextId());
			stmt.setInt(3, ctx.getContextId());
			stmt.setInt(4, entityID);
			stmt.setInt(5, FolderObject.PUBLIC);
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

	private final void deleteMailAdminPermissions(final int mailAdmin, final boolean deleteWorking,
			final Connection readCon, final Connection writeCon, final Context ctx) throws SQLException {
		PreparedStatement stmt = null;
		try {
			final String permTable = deleteWorking ? STR_OXFOLDERPERMS : STR_DELOXFOLDERPERMS;
			/*
			 * Delete mailadmin permission entries
			 */
			stmt = writeCon.prepareStatement(SQL_DELETE_PERMISSIONS.replaceFirst(STR_TABLE, permTable).replaceFirst(
					"#IDS#",
					new StringBuilder(10).append('(').append(mailAdmin).append(',').append(
							OCLPermission.ALL_GROUPS_AND_USERS).append(')').toString()));
			stmt.setInt(1, ctx.getContextId());
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

	private final void deleteMailAdminFolders(final int mailAdmin, final boolean deleteWorking,
			final Connection readCon, final Connection writeCon, final Context ctx) throws SQLException {
		PreparedStatement stmt = null;
		try {
			/*
			 * Remove folder references
			 */
			final String folderTable = deleteWorking ? STR_OXFOLDERTREE : STR_DELOXFOLDERTREE;
			if (deleteWorking) {
				stmt = writeCon.prepareStatement(SQL_DELETE_SPECIAL_FOLDERS);
				stmt.setInt(1, ctx.getContextId());
				stmt.executeUpdate();
				stmt.close();
				stmt = null;
			}
			stmt = writeCon.prepareStatement(SQL_DELETE_MAILADMIN_REFERENCES.replaceFirst(STR_TABLE, folderTable));
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, mailAdmin);
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

	private static final String SQL_SELECT_ADMIN = "SELECT user FROM user_setting_admin WHERE cid = ?";

	public void addContextSystemFolders(final int cid, final String mailAdminDisplayName, final String language,
			final Connection con) throws OXException {
		try {
			int contextMalAdmin = -1;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				stmt = con.prepareStatement(SQL_SELECT_ADMIN);
				stmt.setInt(1, cid);
				rs = stmt.executeQuery();
				if (!rs.next()) {
					throw new OXFolderException(FolderCode.NO_ADMIN_USER_FOUND_IN_CONTEXT, cid);
				}
				contextMalAdmin = rs.getInt(1);
			} finally {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
			}
			addContextSystemFolders(cid, contextMalAdmin, mailAdminDisplayName, language, con);
		} catch (SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, e, true, cid);
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
		 * Insert system system_global folder
		 */
		systemPermission.setAllPermission(OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.ADMIN_PERMISSION,
				OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
		systemPermission.setFolderAdmin(false);
		createSystemFolder(FolderObject.SYSTEM_GLOBAL_FOLDER_ID, FolderObject.SYSTEM_GLOBAL_FOLDER_NAME,
				systemPermission, FolderObject.SYSTEM_FOLDER_ID, FolderObject.CONTACT, true, creatingTime, mailAdmin,
				cid, writeCon);
		/*
		 * Insert system internal users folder
		 */
		systemPermission.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS,
				OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
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
		 * Add mailadmin's folder rights to context's system folder and create
		 * his standard folders
		 */
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

	private final void createSingleUserPermission(final int fuid, final int userId, final int[] allPerms,
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

	private static final String SQL_DELETE_TABLE = "DELETE FROM #TABLE# WHERE cid = ?";

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
				if (performCommit && writeCon != null) {
					writeCon.rollback();
					writeCon.setAutoCommit(true);
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * Fetches a unique id from underlying storage. NOTE: This method assumes
	 * that given writeable connection is set to auto-commit! In any case the
	 * <code>commit()</code> will be invoked, so any surrounding BEGIN-COMMIT
	 * mechanisms will be canceled.
	 * 
	 * @return a unique id from underlying storage
	 */
	private static int getNextSerial(final Context ctx, final Connection callWriteConArg,
			final boolean allowConnectionFetch) throws SQLException, OXException {
		NEXTSERIAL_LOCK.lock();
		try {
			Connection callWriteCon = callWriteConArg;
			final boolean createCon = (callWriteCon == null);
			if (createCon && !allowConnectionFetch) {
				throw new OXFolderException(FolderCode.NO_CONNECTION_FETCH, ctx.getContextId());
			}
			boolean isAuto = false;
			try {
				try {
					if (createCon) {
						callWriteCon = DBPool.pickupWriteable(ctx);
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
					if (createCon && callWriteCon != null) {
						DBPool.pushWrite(ctx, callWriteCon);
					}
				}
			} catch (DBPoolingException e) {
				if (isAuto && callWriteCon != null) {
					callWriteCon.rollback(); // ROLLBACK
					callWriteCon.setAutoCommit(true);
				}
				throw new OXFolderException(FolderCode.DBPOOLING_ERROR, ctx.getContextId());
			}
		} finally {
			NEXTSERIAL_LOCK.unlock();
		}
	}

	/**
	 * <code>addUserToOXFolders</code> adds rights to the oxfolder tables so
	 * that the user can use the oxfolders
	 */
	public void addUserToOXFolders(final int userId, final String displayName, final String language, final int cid,
			final Connection writeCon) throws OXException {
		try {
			// final Context ctx = ContextStorage.getInstance().getContext(cid);
			final Context ctx = new ContextImpl(cid);
			final StringHelper strHelper = new StringHelper(SessionObject.createLocale(language));
			/*
			 * Load the propfile manually if not done, yet
			 */
			try {
				SystemConfig.init();
			} catch (ConfigurationException e) {
				/*
				 * Propfile could NOT be loaded
				 */
				LOG.error(e.getMessage(), e);
				return;
			}
			String defaultCalName = strHelper.getString(FolderStrings.DEFAULT_CALENDAR_FOLDER_NAME);
			if (defaultCalName == null || defaultCalName.length() == 0) {
				defaultCalName = "My Calendar";
			}
			String defaultConName = strHelper.getString(FolderStrings.DEFAULT_CONTACT_FOLDER_NAME);
			if (defaultConName == null || defaultCalName.length() == 0) {
				defaultConName = "My Contacts";
			}
			String defaultTaskName = strHelper.getString(FolderStrings.DEFAULT_TASK_FOLDER_NAME);
			if (defaultTaskName == null || defaultTaskName.length() == 0) {
				defaultTaskName = "My Tasks";
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
			stdFolderIDs.add(Integer.valueOf(insertFolderSQL(userId, fo, creatingTime, true, ctx, writeCon, false)));
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder("User's default CALENDAR folder successfully created").toString());
			}
			/*
			 * Insert default contact folder
			 */
			fo.setFolderName(defaultConName);
			fo.setModule(FolderObject.CONTACT);
			stdFolderIDs.add(Integer.valueOf(insertFolderSQL(userId, fo, creatingTime, true, ctx, writeCon, false)));
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder("User's default CONTACT folder successfully created").toString());
			}
			/*
			 * Insert default contact folder
			 */
			fo.setFolderName(defaultTaskName);
			fo.setModule(FolderObject.TASK);
			stdFolderIDs.add(Integer.valueOf(insertFolderSQL(userId, fo, creatingTime, true, ctx, writeCon, false)));
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
			stdFolderIDs.add(Integer.valueOf(insertFolderSQL(userId, fo, creatingTime, true, ctx, writeCon, false)));
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
		} catch (DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, true, cid);
		} catch (SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, e, true, cid);
		}
	}

}
