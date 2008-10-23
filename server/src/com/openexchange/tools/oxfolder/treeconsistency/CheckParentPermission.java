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
 * {@link CheckParentPermission} - Checks for parental visibility permissions.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class CheckParentPermission extends CheckPermission {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(CheckParentPermission.class);

	/**
	 * Initializes a new {@link CheckParentPermission}
	 * 
	 * @param session
	 *            The session
	 * @param writeCon
	 *            A connection with write capability
	 * @param ctx
	 *            The context
	 */
	public CheckParentPermission(final Session session, final Connection writeCon, final Context ctx) {
		super(session, writeCon, ctx);
	}

	/**
	 * Checks for parental visibility permissions and adds a
	 * folder-read-only-permission for non-tree-visible parent folder if user
	 * has admin permission (optional).
	 * 
	 * @param parent
	 *            The parent folder ID
	 * @param perms
	 *            The current permissions that shall be applied to affected
	 *            folder
	 * @param lastModified
	 *            The last-modified time stamp to use when adding permissions
	 * @param enforceAdminPermission
	 *            <code>true</code> to check for admin permission prior to
	 *            adding a folder-read-only-permission; otherwise
	 *            <code>false</code>
	 * @throws OXException
	 *             If checking parental visibility permissions fails
	 */
	public void checkParentPermissions(final int parent, final OCLPermission[] perms, final long lastModified,
			final boolean enforceAdminPermission) throws OXException {
		final UserConfigurationStorage userConfigStorage = UserConfigurationStorage.getInstance();
		final UserConfiguration sessionUserConf = userConfigStorage.getUserConfiguration(sessionUser, ctx);
		final Map<Integer, ToDoPermission> map = new HashMap<Integer, ToDoPermission>();
		try {
			for (int i = 0; i < perms.length; i++) {
				final OCLPermission assignedPerm = perms[i];
				if (assignedPerm.isGroupPermission()) {
					final int groupId = assignedPerm.getEntity();
					/*
					 * Resolve group
					 */
					try {
						final int[] members = GroupStorage.getInstance(true).getGroup(groupId, ctx).getMember();
						for (final int user : members) {
							if (!isParentVisible(parent, user, userConfigStorage.getUserConfiguration(user, ctx),
									groupId, sessionUserConf, enforceAdminPermission, map)) {
								throw new OXFolderException(OXFolderException.FolderCode.PATH_NOT_VISIBLE_GROUP,
										UserStorage.getStorageUser(user, ctx).getDisplayName(), GroupStorage
												.getInstance(true).getGroup(groupId, ctx).getDisplayName(), UserStorage
												.getStorageUser(sessionUser, ctx).getDisplayName());
							}
						}
					} catch (final LdapException e) {
						throw new OXFolderException(e);
					}
				} else {
					/*
					 * Check for user
					 */
					final int user = assignedPerm.getEntity();
					if (!isParentVisible(parent, user, userConfigStorage.getUserConfiguration(user, ctx), -1,
							sessionUserConf, enforceAdminPermission, map)) {
						throw new OXFolderException(OXFolderException.FolderCode.PATH_NOT_VISIBLE_USER, UserStorage
								.getStorageUser(user, ctx).getDisplayName(), UserStorage.getStorageUser(sessionUser,
								ctx).getDisplayName());
					}
				}
			}
			/*
			 * Auto-insert folder-read permission to make non-visible parent
			 * folders visible in folder tree
			 */
			if (!map.isEmpty()) {
				final int size2 = map.size();
				final Iterator<Map.Entry<Integer, ToDoPermission>> iter2 = map.entrySet().iterator();
				for (int i = 0; i < size2; i++) {
					final Map.Entry<Integer, ToDoPermission> entry = iter2.next();
					final int folderId = entry.getKey().intValue();
					/*
					 * Insert read permissions
					 */
					final int[] users = entry.getValue().getUsers();
					for (int j = 0; j < users.length; j++) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("Auto-Insert folder-read permission for user "
									+ UserStorage.getStorageUser(users[j], ctx).getDisplayName() + " to folder "
									+ folderId);
						}
						addFolderReadPermission(folderId, users[j], false);
					}
					final int[] groups = entry.getValue().getGroups();
					for (int j = 0; j < groups.length; j++) {
						if (LOG.isDebugEnabled()) {
							try {
								LOG.debug("Auto-Insert folder-read permission for group "
										+ GroupStorage.getInstance(true).getGroup(groups[j], ctx).getDisplayName()
										+ " to folder " + folderId);
							} catch (final LdapException e) {
								LOG.trace("Logging failed", e);
							}
						}
						addFolderReadPermission(folderId, groups[j], true);
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
			}
		} catch (final SQLException e) {
			throw new OXFolderException(OXFolderException.FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
		} catch (final DBPoolingException e) {
			throw new OXFolderException(OXFolderException.FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx
					.getContextId()));
		}
	}

	private boolean isParentVisible(final int parent, final int entity, final UserConfiguration entityConfiguration,
			final int groupId, final UserConfiguration sessionUserConf, final boolean enforceAdminPermission,
			final Map<Integer, ToDoPermission> map) throws DBPoolingException, OXException, SQLException {
		if (parent < FolderObject.MIN_FOLDER_ID) {
			/*
			 * We reached a context-created folder
			 */
			return true;
		}
		final FolderObject fo = getFolderFromMaster(parent);
		/*
		 * Check entity's effective permission
		 */
		if (!fo.getEffectiveUserPermission(entity, entityConfiguration, writeCon).isFolderVisible()) {
			/*
			 * Current folder is not visible; check if modifying user is allowed
			 * to grant folder visibility to entity
			 */
			if (enforceAdminPermission
					&& !fo.getEffectiveUserPermission(sessionUser, sessionUserConf, writeCon).isFolderAdmin()) {
				return false;
			}
			final Integer key = Integer.valueOf(parent);
			ToDoPermission todo = map.get(key);
			if (todo == null) {
				todo = new ToDoPermission(parent);
				map.put(key, todo);
			}
			if (groupId == -1) {
				todo.addUser(entity);
			} else {
				/*
				 * User was resolved from a group, thus add group
				 */
				todo.addGroup(groupId);
			}
		}
		return isParentVisible(fo.getParentFolderID(), entity, entityConfiguration, groupId, sessionUserConf,
				enforceAdminPermission, map);
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
