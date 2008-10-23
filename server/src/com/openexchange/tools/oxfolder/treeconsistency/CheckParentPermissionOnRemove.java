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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderSQL;

/**
 * {@link CheckParentPermissionOnRemove} - Checks for unnecessary
 * folder-read-permission on parent folders
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class CheckParentPermissionOnRemove extends CheckPermission {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(CheckParentPermissionOnRemove.class);

	/**
	 * Initializes a new {@link CheckParentPermissionOnRemove}
	 * 
	 * @param session
	 *            The session
	 * @param writeCon
	 *            A connection with write capability
	 * @param ctx
	 *            The context
	 */
	public CheckParentPermissionOnRemove(final Session session, final Connection writeCon, final Context ctx) {
		super(session, writeCon, ctx);
	}

	/**
	 * Checks for unnecessary folder-read-permission on parent folders
	 * (presumably auto-created) and removes them.
	 * 
	 * @param parent
	 *            The parent folder ID
	 * @param fuid
	 *            The current folder ID
	 * @param removedPerms
	 *            The removed permissions (by an update operation)
	 * @param lastModified
	 *            The last-modified time stamp
	 * @param enforceAdminPermission
	 *            <code>true</code> to check for admin permission prior to
	 *            adding a folder-read-only-permission; otherwise
	 *            <code>false</code>
	 * @throws OXException
	 *             If checking for unnecessary folder-read-permission on parent
	 *             folders fails
	 */
	public void checkParentPermissionsOnRemove(final int parent, final int fuid, final OCLPermission[] removedPerms,
			final long lastModified, final boolean enforceAdminPermission) throws OXException {
		final UserConfiguration sessionUserConf = UserConfigurationStorage.getInstance().getUserConfiguration(
				sessionUser, ctx);
		final Map<Integer, ToDoPermission> map = new HashMap<Integer, ToDoPermission>();
		checkParentPermissionsOnRemoveRec(parent, fuid, removedPerms, sessionUserConf, enforceAdminPermission, map);
		/*
		 * Auto-delete permission from parent folders to achieve a consistent
		 * folder tree
		 */
		if (!map.isEmpty()) {
			try {
				final int size2 = map.size();
				final Iterator<Map.Entry<Integer, ToDoPermission>> iter2 = map.entrySet().iterator();
				for (int i = 0; i < size2; i++) {
					final Map.Entry<Integer, ToDoPermission> entry = iter2.next();
					final int folderId = entry.getKey().intValue();
					/*
					 * Delete read permissions
					 */
					final int[] users = entry.getValue().getUsers();
					for (int j = 0; j < users.length; j++) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("Auto-Delete permission for user "
									+ UserStorage.getStorageUser(users[j], ctx).getDisplayName()
									+ " from parent folder " + folderId);
						}
						OXFolderSQL.removeSinglePermission(folderId, users[j], writeCon, ctx);
					}
					final int[] groups = entry.getValue().getGroups();
					for (int j = 0; j < groups.length; j++) {
						if (LOG.isDebugEnabled()) {
							try {
								LOG.debug("Auto-Delete permission for group "
										+ GroupStorage.getInstance(true).getGroup(groups[j], ctx).getDisplayName()
										+ " from parent folder " + folderId);
							} catch (final LdapException e) {
								LOG.trace("Logging failed", e);
							}
						}
						OXFolderSQL.removeSinglePermission(folderId, groups[j], writeCon, ctx);
					}
					/*
					 * Update folders last-modified
					 */
					OXFolderSQL.updateLastModified(folderId, lastModified, sessionUser, writeCon, ctx);
					/*
					 * Update caches
					 */
					try {
						if (FolderCacheManager.isEnabled()) {
							FolderCacheManager.getInstance().removeFolderObject(folderId, ctx);
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
			} catch (final DBPoolingException e) {
				throw new OXFolderException(OXFolderException.FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx
						.getContextId()));
			} catch (final SQLException e) {
				throw new OXFolderException(OXFolderException.FolderCode.SQL_ERROR, e, Integer.valueOf(ctx
						.getContextId()));
			}
		}
	}

	private void checkParentPermissionsOnRemoveRec(final int parent, final int folderId,
			final OCLPermission[] removedPerms, final UserConfiguration sessionUserConf,
			final boolean enforceAdminPermission, final Map<Integer, ToDoPermission> map) throws OXException {
		if (parent < FolderObject.MIN_FOLDER_ID) {
			/*
			 * No modification of a context-created folder
			 */
			return;
		}
		final FolderObject parentObj = getFolderFromMaster(parent);
		final UserConfigurationStorage userConfigStorage = UserConfigurationStorage.getInstance();
		/*
		 * Check parent folder's permission
		 */
		try {
			for (final OCLPermission removedPerm : removedPerms) {
				if (removedPerm.isGroupPermission()) {
					final int groupId = removedPerm.getEntity();
					/*
					 * Resolve group
					 */
					try {
						final int[] members = GroupStorage.getInstance(true).getGroup(groupId, ctx).getMember();
						for (final int user : members) {
							final UserConfiguration userConf = userConfigStorage.getUserConfiguration(user, ctx);
							if (parentObj.getEffectiveUserPermission(user, userConf).isFolderVisible()
									&& !isSiblingVisible(parent, folderId, user, userConf)) {
								/*
								 * No sibling visible to removed user, thus
								 * permission can be removed from parent, too.
								 */
								if (enforceAdminPermission
										&& !parentObj.getEffectiveUserPermission(sessionUser, sessionUserConf)
												.isFolderAdmin()) {
									throw new OXFolderException(
											OXFolderException.FolderCode.PARENT_STILL_VISIBLE_GROUP, UserStorage
													.getStorageUser(user, ctx).getDisplayName(), GroupStorage
													.getInstance(true).getGroup(groupId, ctx).getDisplayName());
								}
								/*
								 * Remember to remove permission from parent
								 */
								final Integer key = Integer.valueOf(parent);
								ToDoPermission todo = map.get(key);
								if (todo == null) {
									todo = new ToDoPermission(parent);
									map.put(key, todo);
								}
								todo.addGroup(groupId);
								todo.addUser(user);
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
					final UserConfiguration userConf = userConfigStorage.getUserConfiguration(user, ctx);
					if (parentObj.getEffectiveUserPermission(user, userConf).isFolderVisible()
							&& !isSiblingVisible(parent, folderId, user, userConf)) {
						/*
						 * No sibling visible to removed user, thus permission
						 * can be removed from parent, too.
						 */
						if (enforceAdminPermission
								&& !parentObj.getEffectiveUserPermission(sessionUser, sessionUserConf).isFolderAdmin()) {
							throw new OXFolderException(OXFolderException.FolderCode.PARENT_STILL_VISIBLE_USER,
									UserStorage.getStorageUser(user, ctx).getDisplayName());
						}
						/*
						 * Remember to remove permission from parent
						 */
						final Integer key = Integer.valueOf(parent);
						ToDoPermission todo = map.get(key);
						if (todo == null) {
							todo = new ToDoPermission(parent);
							map.put(key, todo);
						}
						todo.addUser(user);
					}
				}
			}
		} catch (final DBPoolingException e) {
			throw new OXFolderException(OXFolderException.FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx
					.getContextId()));
		} catch (final SQLException e) {
			throw new OXFolderException(OXFolderException.FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
		}
		checkParentPermissionsOnRemoveRec(parentObj.getParentFolderID(), parent, removedPerms, sessionUserConf,
				enforceAdminPermission, map);
	}

	private boolean isSiblingVisible(final int parent, final int folderId, final int entity,
			final UserConfiguration entityConfiguration) throws OXException, DBPoolingException, SQLException {
		/*
		 * Iterate siblings
		 */
		final List<Integer> siblings = FolderObject.getSubfolderIds(parent, ctx, writeCon);
		siblings.remove(Integer.valueOf(folderId));
		for (final Integer sibling : siblings) {
			if (getFolderFromMaster(sibling.intValue()).getEffectiveUserPermission(entity, entityConfiguration,
					writeCon).isFolderVisible()) {
				/*
				 * Sibling is visible
				 */
				return true;
			}
		}
		return false;
	}

}
