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

package com.openexchange.api2;

import static com.openexchange.tools.oxfolder.OXFolderManagerImpl.folderModule2String;
import static com.openexchange.tools.oxfolder.OXFolderManagerImpl.getFolderName;
import static com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.cache.OXCachingException;
import com.openexchange.cache.impl.FolderQueryCacheManager;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.FolderObjectIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderManagerImpl;
import com.openexchange.tools.oxfolder.OXFolderNotFoundException;
import com.openexchange.tools.oxfolder.OXFolderPermissionException;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;

/**
 * RdbFolderSQLInterface
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class RdbFolderSQLInterface implements FolderSQLInterface {

	public static enum FolderQuery {

		NON_TREE_VISIBLE_CALENDAR(1), NON_TREE_VISIBLE_TASK(2), NON_TREE_VISIBLE_CONTACT(3), NON_TREE_VISIBLE_INFOSTORE(
				4), ROOT_FOLDERS(5);

		private final int queryNum;

		private FolderQuery(final int queryNum) {
			this.queryNum = queryNum;
		}

		public int getQueryNum() {
			return queryNum;
		}
	}

	private static final int getNonTreeVisibleNum(final int module) {
		switch (module) {
		case FolderObject.CALENDAR:
			return FolderQuery.NON_TREE_VISIBLE_CALENDAR.queryNum;
		case FolderObject.TASK:
			return FolderQuery.NON_TREE_VISIBLE_TASK.queryNum;
		case FolderObject.CONTACT:
			return FolderQuery.NON_TREE_VISIBLE_CONTACT.queryNum;
		case FolderObject.INFOSTORE:
			return FolderQuery.NON_TREE_VISIBLE_INFOSTORE.queryNum;
		default:
			return -1;
		}
	}

	private static final Set<Integer> getNonTreeVisibleModules() {
		final Set<Integer> retval = new HashSet<Integer>();
		retval.add(Integer.valueOf(FolderObject.CALENDAR));
		retval.add(Integer.valueOf(FolderObject.TASK));
		retval.add(Integer.valueOf(FolderObject.CONTACT));
		retval.add(Integer.valueOf(FolderObject.INFOSTORE));
		return retval;
	}

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(RdbFolderSQLInterface.class);

	private final int userId;

	private final int[] groups;

	private final Context ctx;

	private final Session sessionObj;

	private final User user;

	private final UserConfiguration userConfiguration;

	private final OXFolderAccess oxfolderAccess;

	/**
	 * @param sessionObj
	 */
	public RdbFolderSQLInterface(final Session sessionObj) {
		this(sessionObj, null);
	}

	public RdbFolderSQLInterface(final Session sessionObj, final OXFolderAccess oxfolderAccess) {
		this.sessionObj = sessionObj;
		userConfiguration = UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(),
				sessionObj.getContext());
		user = UserStorage.getStorageUser(sessionObj.getUserId(), sessionObj.getContext());
		this.userId = user.getId();
		this.groups = user.getGroups();
		this.ctx = sessionObj.getContext();
		this.oxfolderAccess = oxfolderAccess == null ? new OXFolderAccess(ctx) : oxfolderAccess;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.FolderSQLInterface#getUsersInfostoreFolder()
	 */
	public FolderObject getUsersInfostoreFolder() throws OXException {
		if (!userConfiguration.hasInfostore()) {
			throw new OXFolderException(FolderCode.NO_MODULE_ACCESS, getUserName(sessionObj, user),
					folderModule2String(FolderObject.INFOSTORE), Integer.valueOf(ctx.getContextId()));
		}
		return oxfolderAccess.getDefaultFolder(userId, FolderObject.INFOSTORE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.FolderSQLInterface#getFolderById(int)
	 */
	public FolderObject getFolderById(final int id) throws OXException {
		try {
			final FolderObject fo = oxfolderAccess.getFolderObject(id);
			final EffectivePermission perm = fo.getEffectiveUserPermission(userId, userConfiguration);
			if (!perm.isFolderVisible()) {
				if (!perm.getUnderlyingPermission().isFolderVisible()) {
					throw new OXFolderPermissionException(FolderCode.NOT_VISIBLE, getFolderName(id, ctx), getUserName(
							sessionObj, user), Integer.valueOf(ctx.getContextId()));
				}
				throw new OXFolderException(FolderCode.NOT_VISIBLE, getFolderName(id, ctx), getUserName(sessionObj,
						user), Integer.valueOf(ctx.getContextId()));
			} else if (fo.isShared(sessionObj.getUserId()) && !userConfiguration.hasFullSharedFolderAccess()) {
				throw new OXFolderException(FolderCode.NO_SHARED_FOLDER_ACCESS, getUserName(sessionObj, user),
						getFolderName(id, ctx), Integer.valueOf(ctx.getContextId()));
			} else if (Arrays.binarySearch(userConfiguration.getAccessibleModules(), fo.getModule()) < 0) {
				throw new OXFolderException(FolderCode.NO_MODULE_ACCESS, getUserName(sessionObj, user),
						folderModule2String(fo.getModule()), Integer.valueOf(ctx.getContextId()));
			}
			return fo;
		} catch (final DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
		} catch (final SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.FolderSQLInterface#saveFolderObject(com.openexchange.groupware.container.FolderObject,
	 *      java.util.Date)
	 */
	public FolderObject saveFolderObject(final FolderObject folderobjectArg, final Date clientLastModified)
			throws OXException {
		if (folderobjectArg.getType() == FolderObject.PUBLIC && !userConfiguration.hasFullPublicFolderAccess()
				&& (!folderobjectArg.containsModule() || folderobjectArg.getModule() != FolderObject.INFOSTORE)) {
			throw new OXFolderException(
					FolderCode.NO_PUBLIC_FOLDER_WRITE_ACCESS,
					getUserName(sessionObj, user),
					(folderobjectArg.containsObjectID() && folderobjectArg.getObjectID() > 0 ? getFolderName(folderobjectArg)
							: ""), Integer.valueOf(ctx.getContextId()));
		}
		final FolderObject folderobject = folderobjectArg;
		final boolean insert = (!folderobject.containsObjectID() || folderobject.getObjectID() == -1);
		final OXFolderManager manager = new OXFolderManagerImpl(sessionObj, oxfolderAccess);
		try {
			if (insert) {
				if (folderobject.containsParentFolderID()) {
					if (folderobject.getParentFolderID() == FolderObject.SYSTEM_PUBLIC_FOLDER_ID
							&& !userConfiguration.hasFullPublicFolderAccess()) {
						throw new OXFolderException(
								FolderCode.NO_PUBLIC_FOLDER_WRITE_ACCESS,
								getUserName(sessionObj, user),
								(folderobjectArg.containsObjectID() && folderobjectArg.getObjectID() > 0 ? getFolderName(folderobjectArg)
										: ""), Integer.valueOf(ctx.getContextId()));
					}
					final int[] virtualIDs = new int[] { FolderObject.VIRTUAL_USER_INFOSTORE_FOLDER_ID,
							FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID, FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID,
							FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID, FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID };
					if (Arrays.binarySearch(virtualIDs, folderobject.getParentFolderID()) > -1) {
						throw new OXFolderPermissionException(FolderCode.NO_CREATE_SUBFOLDER_PERMISSION, getUserName(
								sessionObj, user), getFolderName(folderobject.getParentFolderID(), ctx), Integer
								.valueOf(ctx.getContextId()));
					}
				} else {
					throw new OXFolderException(FolderCode.MISSING_FOLDER_ATTRIBUTE, FolderFields.FOLDER_ID, Integer
							.valueOf(ctx.getContextId()));
				}
				final FolderObject parentFolder = oxfolderAccess.getFolderObject(folderobject.getParentFolderID());
				final EffectivePermission parentalEffectivePerm = parentFolder.getEffectiveUserPermission(userId,
						userConfiguration);
				if (!parentalEffectivePerm.hasModuleAccess(folderobject.getModule())) {
					throw new OXFolderException(FolderCode.NO_MODULE_ACCESS, getUserName(sessionObj, user),
							folderModule2String(folderobject.getModule()), Integer.valueOf(ctx.getContextId()));
				}
				if (!parentalEffectivePerm.isFolderVisible()) {
					if (!parentalEffectivePerm.getUnderlyingPermission().isFolderVisible()) {
						throw new OXFolderPermissionException(FolderCode.NOT_VISIBLE, getFolderName(folderobject
								.getParentFolderID(), ctx), getUserName(sessionObj, user), Integer.valueOf(ctx
								.getContextId()));
					}
					throw new OXFolderException(FolderCode.NOT_VISIBLE, Category.USER_CONFIGURATION, getFolderName(
							folderobject.getParentFolderID(), ctx), getUserName(sessionObj, user), Integer.valueOf(ctx
							.getContextId()));
				}
				if (!parentalEffectivePerm.canCreateSubfolders()) {
					if (!parentalEffectivePerm.getUnderlyingPermission().canCreateSubfolders()) {
						throw new OXFolderPermissionException(FolderCode.NO_CREATE_SUBFOLDER_PERMISSION, getUserName(
								sessionObj, user), getFolderName(folderobject.getParentFolderID(), ctx), Integer
								.valueOf(ctx.getContextId()));
					}
					throw new OXFolderException(FolderCode.NO_CREATE_SUBFOLDER_PERMISSION, Category.USER_CONFIGURATION,
							getUserName(sessionObj, user), getFolderName(folderobject.getParentFolderID(), ctx),
							Integer.valueOf(ctx.getContextId()));
				}
				folderobject.setType(getFolderType(folderobject.getParentFolderID()));
				final long createTime = System.currentTimeMillis();
				manager.createFolder(folderobject, false, createTime);
			} else {
				folderobject.fill(oxfolderAccess.getFolderObject(folderobject.getObjectID()), false);
				if (!folderobject.exists(ctx)) {
					throw new OXFolderNotFoundException(folderobject.getObjectID(), ctx.getContextId());
				}
				if (clientLastModified != null
						&& oxfolderAccess.getFolderLastModified(folderobject.getObjectID()).after(clientLastModified)) {
					throw new OXConcurrentModificationException(Component.FOLDER,
							OXFolderException.DETAIL_NUMBER_CONCURRENT_MODIFICATION, new Object[0]);
				}
				final EffectivePermission effectivePerm = oxfolderAccess.getFolderPermission(
						folderobject.getObjectID(), userId, userConfiguration);
				if (!effectivePerm.hasModuleAccess(folderobject.getModule())) {
					throw new OXFolderException(FolderCode.NO_MODULE_ACCESS, getUserName(sessionObj, user),
							folderModule2String(folderobject.getModule()), Integer.valueOf(ctx.getContextId()));
				}
				if (!effectivePerm.isFolderVisible()) {
					if (!effectivePerm.getUnderlyingPermission().isFolderVisible()) {
						throw new OXFolderPermissionException(FolderCode.NOT_VISIBLE, getFolderName(folderobject),
								getUserName(sessionObj, user), Integer.valueOf(ctx.getContextId()));
					}
					throw new OXFolderException(FolderCode.NOT_VISIBLE, Category.USER_CONFIGURATION,
							getFolderName(folderobject), getUserName(sessionObj, user), Integer.valueOf(ctx
									.getContextId()));
				}
				if (!effectivePerm.isFolderAdmin()) {
					if (!effectivePerm.getUnderlyingPermission().isFolderAdmin()) {
						throw new OXFolderPermissionException(FolderCode.NO_ADMIN_ACCESS,
								getUserName(sessionObj, user), getFolderName(folderobject), Integer.valueOf(ctx
										.getContextId()));
					}
					throw new OXFolderException(FolderCode.NO_ADMIN_ACCESS, Category.USER_CONFIGURATION, getUserName(
							sessionObj, user), getFolderName(folderobject), Integer.valueOf(ctx.getContextId()));
				}
				folderobject.getObjectID();
				final long lastModfified = System.currentTimeMillis();
				manager.updateFolder(folderobject, false, lastModfified);
			}
			return folderobject;
		} catch (final DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
		} catch (final SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.FolderSQLInterface#deleteFolderObject(com.openexchange.groupware.container.FolderObject,
	 *      java.util.Date)
	 */
	public int deleteFolderObject(final FolderObject folderobject, final Date clientLastModified) throws OXException {
		try {
			folderobject.fill(oxfolderAccess.getFolderObject(folderobject.getObjectID()), false);
			if (folderobject.getType() == FolderObject.PUBLIC && !userConfiguration.hasFullPublicFolderAccess()) {
				throw new OXFolderException(FolderCode.NO_PUBLIC_FOLDER_WRITE_ACCESS, getUserName(sessionObj, user),
						getFolderName(folderobject), Integer.valueOf(ctx.getContextId()));
			}
			if (!folderobject.exists(ctx)) {
				throw new OXFolderNotFoundException(folderobject.getObjectID(), ctx.getContextId());
			}
			if (clientLastModified != null
					&& oxfolderAccess.getFolderLastModified(folderobject.getObjectID()).after(clientLastModified)) {
				throw new OXConcurrentModificationException(Component.FOLDER,
						OXFolderException.DETAIL_NUMBER_CONCURRENT_MODIFICATION, new Object[0]);
			}
			final EffectivePermission effectivePerm = folderobject
					.getEffectiveUserPermission(userId, userConfiguration);
			if (!effectivePerm.hasModuleAccess(folderobject.getModule())) {
				throw new OXFolderException(FolderCode.NO_MODULE_ACCESS, getUserName(sessionObj, user),
						folderModule2String(folderobject.getModule()), Integer.valueOf(ctx.getContextId()));
			}
			if (!effectivePerm.isFolderVisible()) {
				if (!effectivePerm.getUnderlyingPermission().isFolderVisible()) {
					throw new OXFolderPermissionException(FolderCode.NOT_VISIBLE, getFolderName(folderobject),
							getUserName(sessionObj, user), Integer.valueOf(ctx.getContextId()));
				}
				throw new OXFolderException(FolderCode.NOT_VISIBLE, Category.USER_CONFIGURATION,
						getFolderName(folderobject), getUserName(sessionObj, user), Integer.valueOf(ctx.getContextId()));
			}
			if (!effectivePerm.isFolderAdmin()) {
				if (!effectivePerm.getUnderlyingPermission().isFolderAdmin()) {
					throw new OXFolderPermissionException(FolderCode.NO_ADMIN_ACCESS, getUserName(sessionObj, user),
							getFolderName(folderobject), Integer.valueOf(ctx.getContextId()));
				}
				throw new OXFolderException(FolderCode.NO_ADMIN_ACCESS, Category.USER_CONFIGURATION, getUserName(
						sessionObj, user), getFolderName(folderobject), Integer.valueOf(ctx.getContextId()));
			}
			final long lastModified = System.currentTimeMillis();
			new OXFolderManagerImpl(sessionObj, oxfolderAccess).deleteFolder(folderobject, false, lastModified);
			return folderobject.getObjectID();
		} catch (final DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
		} catch (final SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
		}
	}

	/**
	 * Fills folder query cache with "non-tree-visible" folders which applies to
	 * given session
	 * 
	 * @param session
	 *            The session to which the query applies
	 * @throws SearchIteratorException
	 *             If iterator fails
	 * @throws OXException
	 *             If a caching error occurs
	 */
	private static final void loadNonTreeVisibleFoldersIntoQueryCache(final Session session,
			UserConfiguration userConfiguration) throws SearchIteratorException, OXException {
		/*
		 * Fetch queue from iterator (which implicitly puts referenced objects
		 * into cache!)
		 */
		final Context ctx = session.getContext();
		final int userId;
		final int[] groups;
		{
			final User u = UserStorage.getStorageUser(session.getUserId(), session.getContext());
			userId = u.getId();
			groups = u.getGroups();
		}
		final Queue<FolderObject> q = ((FolderObjectIterator) OXFolderIteratorSQL
				.getAllVisibleFoldersNotSeenInTreeView(userId, groups, userConfiguration, ctx)).asQueue();
		final int size = q.size();
		final Iterator<FolderObject> iter = q.iterator();
		final Set<Integer> stdModules = getNonTreeVisibleModules();
		/*
		 * Iterate result queue
		 */
		try {
			int prevModule = -1;
			final LinkedList<Integer> cacheQueue = new LinkedList<Integer>();
			for (int i = 0; i < size; i++) {
				final FolderObject f = iter.next();
				if (prevModule != f.getModule()) {
					FolderQueryCacheManager.getInstance().putFolderQuery(getNonTreeVisibleNum(prevModule), cacheQueue,
							session, false);
					prevModule = f.getModule();
					stdModules.remove(Integer.valueOf(prevModule));
					cacheQueue.clear();
				}
				cacheQueue.add(Integer.valueOf(f.getObjectID()));
			}
			FolderQueryCacheManager.getInstance().putFolderQuery(getNonTreeVisibleNum(prevModule), cacheQueue, session,
					false);
			final int setSize = stdModules.size();
			final Iterator<Integer> iter2 = stdModules.iterator();
			for (int i = 0; i < setSize; i++) {
				FolderQueryCacheManager.getInstance().putFolderQuery(getNonTreeVisibleNum(iter2.next().intValue()),
						new LinkedList<Integer>(), session, false);
			}
		} catch (final OXCachingException e) {
			throw new OXException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.FolderSQLInterface#getNonTreeVisiblePublicCalendarFolders()
	 */
	public SearchIterator<?> getNonTreeVisiblePublicCalendarFolders() throws OXException {
		try {
			if (!userConfiguration.hasCalendar()) {
				throw new OXFolderException(FolderCode.NO_MODULE_ACCESS, getUserName(sessionObj, user),
						folderModule2String(FolderObject.CALENDAR), Integer.valueOf(ctx.getContextId()));
			}
			LinkedList<Integer> result;
			if ((result = FolderQueryCacheManager.getInstance().getFolderQuery(
					FolderQuery.NON_TREE_VISIBLE_CALENDAR.queryNum, sessionObj)) == null) {
				loadNonTreeVisibleFoldersIntoQueryCache(sessionObj, userConfiguration);
				result = FolderQueryCacheManager.getInstance().getFolderQuery(
						FolderQuery.NON_TREE_VISIBLE_CALENDAR.queryNum, sessionObj);
			}
			return new FolderObjectIterator(int2folder(result, oxfolderAccess), false);
		} catch (final OXException e) {
			throw e;
		} catch (final SearchIteratorException e) {
			throw new OXException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.FolderSQLInterface#getNonTreeVisiblePublicTaskFolders()
	 */
	public SearchIterator<?> getNonTreeVisiblePublicTaskFolders() throws OXException {
		try {
			if (!userConfiguration.hasTask()) {
				throw new OXFolderException(FolderCode.NO_MODULE_ACCESS, getUserName(sessionObj, user),
						folderModule2String(FolderObject.TASK), Integer.valueOf(ctx.getContextId()));
			}
			LinkedList<Integer> result;
			if ((result = FolderQueryCacheManager.getInstance().getFolderQuery(
					FolderQuery.NON_TREE_VISIBLE_TASK.queryNum, sessionObj)) == null) {
				loadNonTreeVisibleFoldersIntoQueryCache(sessionObj, userConfiguration);
				result = FolderQueryCacheManager.getInstance().getFolderQuery(
						FolderQuery.NON_TREE_VISIBLE_TASK.queryNum, sessionObj);
			}
			return new FolderObjectIterator(int2folder(result, oxfolderAccess), false);
		} catch (final SearchIteratorException e) {
			throw new OXException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.FolderSQLInterface#getNonTreeVisiblePublicContactFolders()
	 */
	public SearchIterator<?> getNonTreeVisiblePublicContactFolders() throws OXException {
		try {
			if (!userConfiguration.hasContact()) {
				throw new OXFolderException(FolderCode.NO_MODULE_ACCESS, getUserName(sessionObj, user),
						folderModule2String(FolderObject.CONTACT), Integer.valueOf(ctx.getContextId()));
			}
			LinkedList<Integer> result;
			if ((result = FolderQueryCacheManager.getInstance().getFolderQuery(
					FolderQuery.NON_TREE_VISIBLE_CONTACT.queryNum, sessionObj)) == null) {
				loadNonTreeVisibleFoldersIntoQueryCache(sessionObj, userConfiguration);
				result = FolderQueryCacheManager.getInstance().getFolderQuery(
						FolderQuery.NON_TREE_VISIBLE_CONTACT.queryNum, sessionObj);
			}
			return new FolderObjectIterator(int2folder(result, oxfolderAccess), false);
		} catch (final SearchIteratorException e) {
			throw new OXException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.FolderSQLInterface#getNonTreeVisiblePublicInfostoreFolders()
	 */
	public SearchIterator<?> getNonTreeVisiblePublicInfostoreFolders() throws OXException {
		try {
			if (!userConfiguration.hasInfostore()) {
				throw new OXFolderException(FolderCode.NO_MODULE_ACCESS, getUserName(sessionObj, user),
						folderModule2String(FolderObject.INFOSTORE), Integer.valueOf(ctx.getContextId()));
			}
			LinkedList<Integer> result;
			if ((result = FolderQueryCacheManager.getInstance().getFolderQuery(
					FolderQuery.NON_TREE_VISIBLE_INFOSTORE.queryNum, sessionObj)) == null) {
				loadNonTreeVisibleFoldersIntoQueryCache(sessionObj, userConfiguration);
				result = FolderQueryCacheManager.getInstance().getFolderQuery(
						FolderQuery.NON_TREE_VISIBLE_INFOSTORE.queryNum, sessionObj);
			}
			return new FolderObjectIterator(int2folder(result, oxfolderAccess), false);
		} catch (final SearchIteratorException e) {
			throw new OXException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.FolderSQLInterface#getRootFolderForUser()
	 */
	public SearchIterator<?> getRootFolderForUser() throws OXException {
		try {
			return OXFolderIteratorSQL.getUserRootFoldersIterator(userId, groups, userConfiguration, ctx);
		} catch (final SearchIteratorException e) {
			throw new OXException(e);
		}
	}

	/**
	 * @param folderId
	 * @return
	 */
	private final int getFolderType(final int folderIdArg) throws OXException {
		int type = -1;
		int folderId = folderIdArg;
		/*
		 * Special treatment for system folders
		 */
		if (folderId == FolderObject.SYSTEM_SHARED_FOLDER_ID) {
			folderId = FolderObject.SYSTEM_PRIVATE_FOLDER_ID;
			type = FolderObject.SHARED;
		} else if (folderId == FolderObject.SYSTEM_PRIVATE_FOLDER_ID) {
			type = FolderObject.PRIVATE;
		} else if (folderId == FolderObject.SYSTEM_PUBLIC_FOLDER_ID
				|| folderId == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
			type = FolderObject.PUBLIC;
		} else if (folderId == FolderObject.SYSTEM_OX_PROJECT_FOLDER_ID) {
			type = FolderObject.PROJECT;
		} else {
			type = oxfolderAccess.getFolderType(folderId);
		}
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.FolderSQLInterface#getSubfolders(int,
	 *      java.sql.Timestamp)
	 */
	public SearchIterator<?> getSubfolders(final int parentId, final Timestamp since) throws OXException {
		try {
			if (parentId == FolderObject.SYSTEM_SHARED_FOLDER_ID && !userConfiguration.hasFullSharedFolderAccess()) {
				throw new OXFolderPermissionException(FolderCode.NO_SHARED_FOLDER_ACCESS,
						getUserName(sessionObj, user), FolderObject.getFolderString(
								FolderObject.SYSTEM_SHARED_FOLDER_ID, UserStorage.getStorageUser(sessionObj.getUserId(),
										sessionObj.getContext()).getLocale()), Integer.valueOf(ctx.getContextId()));
			} else if (oxfolderAccess.isFolderShared(parentId, userId)) {
				return FolderObjectIterator.EMPTY_FOLDER_ITERATOR;
			}
			return OXFolderIteratorSQL.getVisibleSubfoldersIterator(parentId, userId, groups, ctx, userConfiguration,
					since);
		} catch (final SearchIteratorException e) {
			throw new OXException(e);
		} catch (final DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
		} catch (final SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.FolderSQLInterface#getSharedFoldersFrom(int,
	 *      java.sql.Timestamp)
	 */
	public SearchIterator<?> getSharedFoldersFrom(final int owner, final Timestamp since) throws OXException {
		try {
			if (!userConfiguration.hasFullSharedFolderAccess()) {
				throw new OXFolderPermissionException(FolderCode.NO_SHARED_FOLDER_ACCESS,
						getUserName(sessionObj, user), Integer.valueOf(ctx.getContextId()));
			}
			return OXFolderIteratorSQL.getVisibleSharedFolders(userId, groups,
					userConfiguration.getAccessibleModules(), owner, ctx, since);
		} catch (final SearchIteratorException e) {
			throw new OXException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.FolderSQLInterface#getPathToRoot(int)
	 */
	public SearchIterator<?> getPathToRoot(final int folderId) throws OXException {
		try {
			return OXFolderIteratorSQL.getFoldersOnPathToRoot(folderId, userId, userConfiguration, UserStorage.getStorageUser(
					sessionObj.getUserId(), sessionObj.getContext()).getLocale(), ctx);
		} catch (final OXException e) {
			throw e;
		} catch (final SearchIteratorException e) {
			throw new OXException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.FolderSQLInterface#getDeletedFolders(java.util.Date)
	 */
	public SearchIterator<?> getDeletedFolders(final Date since) throws OXException {
		try {
			return OXFolderIteratorSQL.getDeletedFoldersSince(since, userId, groups, userConfiguration
					.getAccessibleModules(), ctx);
		} catch (final SearchIteratorException e) {
			throw new OXException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.FolderSQLInterface#getModifiedUserFolders(java.util.Date)
	 */
	public SearchIterator<?> getModifiedUserFolders(final Date since) throws OXException {
		try {
			return OXFolderIteratorSQL.getModifiedFoldersSince(since == null ? new Date(0) : since, userId, groups,
					userConfiguration.getAccessibleModules(), false, ctx);
		} catch (final SearchIteratorException e) {
			throw new OXException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.api2.FolderSQLInterface#getAllModifiedFolders(java.util.Date)
	 */
	public SearchIterator<?> getAllModifiedFolders(final Date since) throws OXException {
		try {
			return OXFolderIteratorSQL.getAllModifiedFoldersSince(since == null ? new Date(0) : since, ctx);
		} catch (final SearchIteratorException e) {
			throw new OXException(e);
		}
	}

	private static final Queue<FolderObject> int2folder(final Queue<Integer> iq, final OXFolderAccess oxfolderAccess) {
		final Queue<FolderObject> retval = new LinkedList<FolderObject>();
		final int size = iq.size();
		final Iterator<Integer> iter = iq.iterator();
		for (int i = 0; i < size; i++) {
			try {
				retval.add(oxfolderAccess.getFolderObject(iter.next().intValue()));
			} catch (final OXException e) {
				LOG.error(e.getMessage(), e);
				continue;
			}
		}
		return retval;
	}

}
