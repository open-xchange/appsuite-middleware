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

package com.openexchange.tools.oxfolder.treeconsistency;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.openexchange.api2.OXException;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.cache.impl.FolderQueryCacheManager;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.calendar.CalendarCache;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderSQL;

/**
 * {@link CheckSubfolderPermissionOnRemove} - Checks for possible non-visible
 * subfolders.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class CheckSubfolderPermissionOnRemove extends CheckPermission {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(CheckSubfolderPermissionOnRemove.class);

	private int folderId;

	private OCLPermission[] newPerms;

	private final Set<Integer> removedUsers;

	/**
	 * Initializes a new {@link CheckSubfolderPermissionOnRemove}
	 * 
	 * @param session
	 *            The session
	 * @param writeCon
	 *            A connection with write capability
	 * @param ctx
	 *            The context
	 */
	public CheckSubfolderPermissionOnRemove(final Session session, final Connection writeCon, final Context ctx) {
		super(session, writeCon, ctx);
		removedUsers = new HashSet<Integer>();
	}

	/**
	 * Gets the new permissions
	 * 
	 * @return The new permissions
	 */
	public OCLPermission[] getNewPerms() {
		return newPerms;
	}

	/**
	 * Checks for possible non-visible subfolders when removing specified
	 * permissions which cause a folder tree inconsistency
	 * 
	 * @param folderId
	 *            The current folder ID
	 * @param removedPerms
	 *            The removed permissions (by an update operation)
	 * @param The
	 *            new permissions that shall be applied to affected folder
	 * @param lastModified
	 *            The last-modified time stamp
	 * @param enforceAdminPermission
	 *            <code>true</code> to check for admin permission prior to
	 *            editing a folder's permissions; otherwise <code>false</code>
	 * @throws OXException
	 *             If checking for possible non-visible subfolders fails
	 */
	public void checkSubfolderPermissionsOnRemove(final int folderId, final OCLPermission[] removedPerms,
			final OCLPermission[] newPerms, final long lastModified, final boolean enforceAdminPermission)
			throws OXException {
		/*
		 * Preparations
		 */
		this.folderId = folderId;
		this.newPerms = newPerms;
		removedUsers.clear();
		for (int i = 0; i < removedPerms.length; i++) {
			if (!removedPerms[i].isGroupPermission()) {
				removedUsers.add(Integer.valueOf(removedPerms[i].getEntity()));
			}
		}
		final UserConfigurationStorage userConfigStorage = UserConfigurationStorage.getInstance();
		final UserConfiguration sessionUserConf = userConfigStorage.getUserConfiguration(sessionUser, ctx);
		final Map<Integer, ToDoPermission> toRemove = new HashMap<Integer, ToDoPermission>();
		final Map<Integer, ToDoPermission> toAdd = new HashMap<Integer, ToDoPermission>();
		/*
		 * Iterate removed permissions if a subfolder grants folder-read
		 * permission
		 */
		try {
			for (int i = 0; i < removedPerms.length; i++) {
				final OCLPermission removedPerm = removedPerms[i];
				if (removedPerm.isGroupPermission()) {
					/*
					 * Resolve group
					 */
					final int groupId = removedPerm.getEntity();
					try {
						final int[] members = GroupStorage.getInstance(true).getGroup(groupId, ctx).getMember();
						for (final int user : members) {
							if (!areSubfoldersVisible(folderId, user,
									userConfigStorage.getUserConfiguration(user, ctx), groupId, sessionUserConf,
									enforceAdminPermission, toRemove, toAdd)) {
								throw new OXFolderException(OXFolderException.FolderCode.SUBFOLDER_STILL_VISIBLE_GROUP,
										UserStorage.getStorageUser(user, ctx).getDisplayName(), GroupStorage
												.getInstance(true).getGroup(groupId, ctx).getDisplayName());
							}
						}
					} catch (final LdapException e) {
						throw new OXFolderException(e);
					}
				} else {
					/*
					 * Check for user
					 */
					final int user = removedPerm.getEntity();
					if (!areSubfoldersVisible(folderId, user, userConfigStorage.getUserConfiguration(user, ctx), -1,
							sessionUserConf, enforceAdminPermission, toRemove, toAdd)) {
						throw new OXFolderException(OXFolderException.FolderCode.SUBFOLDER_STILL_VISIBLE_USER,
								UserStorage.getStorageUser(user, ctx).getDisplayName());
					}
				}
			}
			if (LOG.isDebugEnabled() && toRemove.isEmpty() && toAdd.isEmpty()) {
				LOG.debug("No auto-delete or auto-insert on subfolders needed for a consisten folder tree");
			}
			/*
			 * Auto-delete permission to make visible subfolder non-visible in
			 * order to achieve a consistent folder tree
			 */
			if (!toRemove.isEmpty()) {
				final int size2 = toRemove.size();
				final Iterator<Map.Entry<Integer, ToDoPermission>> iter2 = toRemove.entrySet().iterator();
				for (int i = 0; i < size2; i++) {
					final Map.Entry<Integer, ToDoPermission> entry = iter2.next();
					final int fuid = entry.getKey().intValue();
					/*
					 * Remove permissions
					 */
					final int[] users = entry.getValue().getUsers();
					for (int j = 0; j < users.length; j++) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("Auto-Delete permission for user "
									+ UserStorage.getStorageUser(users[j], ctx).getDisplayName() + " from subfolder "
									+ fuid);
						}
						OXFolderSQL.removeSinglePermission(fuid, users[j], writeCon, ctx);
					}
					final int[] groups = entry.getValue().getGroups();
					for (int j = 0; j < groups.length; j++) {
						if (LOG.isDebugEnabled()) {
							try {
								LOG.debug("Auto-Delete permission for group "
										+ GroupStorage.getInstance(true).getGroup(groups[j], ctx).getDisplayName()
										+ " from subfolder " + fuid);
							} catch (final LdapException e) {
								LOG.trace("Logging failed", e);
							}
						}
						OXFolderSQL.removeSinglePermission(fuid, groups[j], writeCon, ctx);
					}
					/*
					 * Update folders last-modified
					 */
					OXFolderSQL.updateLastModified(fuid, lastModified, sessionUser, writeCon, ctx);
					/*
					 * Update caches
					 */
					try {
						if (FolderCacheManager.isEnabled()) {
							FolderCacheManager.getInstance().removeFolderObject(fuid, ctx);
						}
						if (FolderQueryCacheManager.isInitialized()) {
							FolderQueryCacheManager.getInstance().invalidateContextQueries(session);
						}
						if (CalendarCache.isInitialized()) {
							CalendarCache.getInstance().invalidateGroup(ctx.getContextId());
						}
					} catch (final AbstractOXException e) {
						LOG.error(e.getMessage(), e);
					}
				}
			}
			/*
			 * Auto-insert folder-read permission to make non-visible parent
			 * folders visible in folder tree
			 */
			if (!toAdd.isEmpty()) {
				final int size2 = toAdd.size();
				final Iterator<Map.Entry<Integer, ToDoPermission>> iter2 = toAdd.entrySet().iterator();
				for (int i = 0; i < size2; i++) {
					final Map.Entry<Integer, ToDoPermission> entry = iter2.next();
					final int cur = entry.getKey().intValue();
					/*
					 * Insert read permissions
					 */
					final int[] users = entry.getValue().getUsers();
					for (int j = 0; j < users.length; j++) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("Auto-Insert folder-read permission for user "
									+ UserStorage.getStorageUser(users[j], ctx).getDisplayName() + " to folder " + cur);
						}
						addFolderReadPermission(cur, users[j], false);
					}
					final int[] groups = entry.getValue().getGroups();
					for (int j = 0; j < groups.length; j++) {
						if (LOG.isDebugEnabled()) {
							try {
								LOG.debug("Auto-Insert folder-read permission for group "
										+ GroupStorage.getInstance(true).getGroup(groups[j], ctx).getDisplayName()
										+ " to folder " + cur);
							} catch (final LdapException e) {
								LOG.trace("Logging failed", e);
							}
						}
						addFolderReadPermission(cur, groups[j], true);
					}
					/*
					 * Update folders last-modified
					 */
					OXFolderSQL.updateLastModified(cur, lastModified, sessionUser, writeCon, ctx);
					/*
					 * Update caches
					 */
					try {
						if (FolderCacheManager.isEnabled()) {
							FolderCacheManager.getInstance().removeFolderObject(cur, ctx);
						}
						if (FolderQueryCacheManager.isInitialized()) {
							FolderQueryCacheManager.getInstance().invalidateContextQueries(session);
						}
						if (CalendarCache.isInitialized()) {
							CalendarCache.getInstance().invalidateGroup(ctx.getContextId());
						}
					} catch (final AbstractOXException e) {
						LOG.error(e.getMessage(), e);
					}
				}
			}
		} catch (final SQLException e) {
			throw new OXFolderException(OXFolderException.FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
		} catch (final DBPoolingException e) {
			throw new OXFolderException(OXFolderException.FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx
					.getContextId()));
		}
	}

	private boolean areSubfoldersVisible(final int folderId, final int entity,
			final UserConfiguration entityConfiguration, final int groupId, final UserConfiguration sessionUserConf,
			final boolean enforceAdminPermission, final Map<Integer, ToDoPermission> toRemove,
			final Map<Integer, ToDoPermission> toAdd) throws DBPoolingException, OXException, SQLException {
		/*
		 * Iterate folder's subfolders
		 */
		final List<Integer> subflds = FolderObject.getSubfolderIds(folderId, ctx, writeCon);
		for (final Integer subfolder : subflds) {
			final FolderObject subfolderObj = getFolderFromMaster(subfolder.intValue());
			if (subfolderObj.getEffectiveUserPermission(entity, entityConfiguration, writeCon).isFolderVisible()) {
				/*
				 * Current subfolder is visible
				 */
				if (groupId == -1) {
					/*
					 * Single user permission; check if session user is allowed
					 * to remove the permission
					 */
					if (enforceAdminPermission
							&& !subfolderObj.getEffectiveUserPermission(sessionUser, sessionUserConf, writeCon)
									.isFolderAdmin()) {
						/*
						 * Modifying user is not allowed to change subfolder's
						 * permissions
						 */
						return false;
					}
					ToDoPermission todo = toRemove.get(subfolder);
					if (todo == null) {
						todo = new ToDoPermission(subfolder.intValue());
						toRemove.put(subfolder, todo);
					}
					todo.addUser(entity);
				} else {
					/*
					 * User was resolved from a group
					 */
					handleRemovedGroupPermission(entity, entityConfiguration, groupId, sessionUserConf,
							enforceAdminPermission, toRemove, toAdd, subfolder, subfolderObj);
				}
			}
			if (!areSubfoldersVisible(subfolder.intValue(), entity, entityConfiguration, groupId, sessionUserConf,
					enforceAdminPermission, toRemove, toAdd)) {
				return false;
			}
		}
		return true;
	}

	private boolean handleRemovedGroupPermission(final int entity, final UserConfiguration entityConfiguration,
			final int groupId, final UserConfiguration sessionUserConf, final boolean enforceAdminPermission,
			final Map<Integer, ToDoPermission> toRemove, final Map<Integer, ToDoPermission> toAdd,
			final Integer subfolder, final FolderObject subfolderObj) throws SQLException, DBPoolingException,
			OXException {
		/*
		 * Check if visibility is granted by group permission only.
		 */
		if (isVisibleThroughGroupPermission(subfolderObj, groupId)) {
			/*
			 * The current folder defines a permission for the group; check if
			 * session user is allowed to remove the permission
			 */
			if (enforceAdminPermission
					&& !subfolderObj.getEffectiveUserPermission(sessionUser, sessionUserConf, writeCon).isFolderAdmin()) {
				/*
				 * Modifying user is not allowed to change subfolder's
				 * permissions
				 */
				return false;
			}
			ToDoPermission todo = toRemove.get(subfolder);
			if (todo == null) {
				todo = new ToDoPermission(subfolder.intValue());
				toRemove.put(subfolder, todo);
			}
			/*
			 * User was resolved from a group, thus add group
			 */
			todo.addGroup(groupId);
			/*
			 * Now check if visibility is lost when this permission is removed.
			 */
			if (!getEffectiveUserPermission(entity, entityConfiguration, subfolderObj,
					getTrimmedPermissions(subfolderObj.getPermissionsAsArray(), new int[] { groupId }))
					.isFolderVisible()) {
				todo.addUser(entity);
			}
		} else {
			if (!removedUsers.contains(Integer.valueOf(entity))) {
				/*
				 * Check if parent folder grants at least visibility to resolved
				 * user
				 */
				int parentId = subfolderObj.getParentFolderID();
				while (parentId >= FolderObject.MIN_FOLDER_ID) {
					final FolderObject subfolderParentObj = getFolderFromMaster(parentId);
					if (parentId == this.folderId) {
						if (!getEffectiveUserPermission(entity, entityConfiguration, subfolderParentObj, newPerms).isFolderVisible()) {
							/*
							 * Extend permissions
							 */
							final OCLPermission[] tmp = newPerms;
							newPerms = new OCLPermission[tmp.length + 1];
							System.arraycopy(tmp, 0, newPerms, 1, tmp.length);
							final OCLPermission perm = new OCLPermission(entity, parentId);
							perm.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.NO_PERMISSIONS,
									OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
							perm.setFolderAdmin(false);
							newPerms[0] = perm;
						}
					} else {
						if (!subfolderParentObj.getEffectiveUserPermission(entity, entityConfiguration, writeCon)
								.isFolderVisible()) {
							/*
							 * Check for admin permission for session user
							 */
							if (!subfolderParentObj.getEffectiveUserPermission(sessionUser, sessionUserConf, writeCon)
									.isFolderAdmin()) {
								/*
								 * Modifying user is not allowed to change
								 * permissions
								 */
								return false;
							}
							/*
							 * Add folder visibility for parent
							 */
							final Integer key = Integer.valueOf(parentId);
							ToDoPermission todo = toAdd.get(key);
							if (todo == null) {
								todo = new ToDoPermission(parentId);
								toAdd.put(key, todo);
							}
							todo.addUser(entity);
						}
					}
					parentId = subfolderParentObj.getParentFolderID();
				}
			}
		}
		return true;
	}

	/**
	 * Checks if a permission is defined for specified group ID for given folder
	 * that grants at least folder visibility
	 * 
	 * @param folder
	 *            The folder
	 * @param groupId
	 *            The group ID
	 * @return <code>true</code> if a permission is defined for specified group
	 *         ID for given folder that grants at least folder visibility;
	 *         otherwise <code>false</code>
	 */
	private boolean isVisibleThroughGroupPermission(final FolderObject folder, final int groupId) {
		if (groupId < 0) {
			throw new IllegalArgumentException("group id is invalid: " + groupId);
		}
		final OCLPermission groupPermission = folder.getPermission(groupId);
		if (groupPermission == null) {
			/*
			 * No permission defined for specified group
			 */
			return false;
		}
		if (!groupPermission.isGroupPermission()) {
			throw new IllegalArgumentException("not a group permission: " + groupId);
		}
		return groupPermission.isFolderVisible();
	}

	/**
	 * Gets the specified permissions trimmed by given entity IDs
	 * 
	 * @param permissions
	 *            The permissions to trim
	 * @param exclude
	 *            The entity IDs to exclude
	 * @return The specified permissions trimmed by given entity IDs
	 */
	private OCLPermission[] getTrimmedPermissions(final OCLPermission[] permissions, final int[] exclude) {
		final List<OCLPermission> list = new ArrayList<OCLPermission>(permissions.length);
		Arrays.sort(exclude);
		for (int i = 0; i < permissions.length; i++) {
			final OCLPermission j = permissions[i];
			if (Arrays.binarySearch(exclude, j.getEntity()) < 0) {
				list.add(j);
			}
		}
		return list.toArray(new OCLPermission[list.size()]);
	}

	/**
	 * Gets the effective user permission
	 * 
	 * @param userId
	 *            The user ID
	 * @param userConfig
	 *            The user's configuration
	 * @param folder
	 *            The folder
	 * @param permissions
	 *            The basic permissions
	 * @return The effective user permission
	 */
	private EffectivePermission getEffectiveUserPermission(final int userId, final UserConfiguration userConfig,
			final FolderObject folder, final OCLPermission[] permissions) {
		final EffectivePermission maxPerm = new EffectivePermission(userId, folder.getObjectID(), folder
				.getType(userId), folder.getModule(), userConfig);
		maxPerm.setAllPermission(OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS,
				OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
		final int[] idArr;
		{
			final int[] groups = userConfig.getGroups();
			idArr = new int[groups.length + 1];
			idArr[0] = userId;
			System.arraycopy(groups, 0, idArr, 1, groups.length);
			Arrays.sort(idArr);
		}
		NextPerm: for (int i = 0; i < permissions.length; i++) {
			final OCLPermission oclPerm = permissions[i];
			if (Arrays.binarySearch(idArr, oclPerm.getEntity()) < 0) {
				continue NextPerm;
			}
			if (oclPerm.getFolderPermission() > maxPerm.getFolderPermission()) {
				maxPerm.setFolderPermission(oclPerm.getFolderPermission());
			}
			if (oclPerm.getReadPermission() > maxPerm.getReadPermission()) {
				maxPerm.setReadObjectPermission(oclPerm.getReadPermission());
			}
			if (oclPerm.getWritePermission() > maxPerm.getWritePermission()) {
				maxPerm.setWriteObjectPermission(oclPerm.getWritePermission());
			}
			if (oclPerm.getDeletePermission() > maxPerm.getDeletePermission()) {
				maxPerm.setDeleteObjectPermission(oclPerm.getDeletePermission());
			}
			if (!maxPerm.isFolderAdmin() && oclPerm.isFolderAdmin()) {
				maxPerm.setFolderAdmin(true);
			}
		}
		return maxPerm;
	}

	private void addFolderReadPermission(final int folderId, final int entity, final boolean isGroup)
			throws DBPoolingException, SQLException {
		/*
		 * Add folder-read permission
		 */
		OXFolderSQL.addSinglePermission(folderId, entity, isGroup, OCLPermission.READ_FOLDER,
				OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, false,
				writeCon, ctx);
	}
}
