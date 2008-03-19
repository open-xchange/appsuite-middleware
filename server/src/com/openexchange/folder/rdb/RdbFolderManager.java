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

package com.openexchange.folder.rdb;

import static com.openexchange.tools.oxfolder.OXFolderManagerImpl.folderModule2String;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.openexchange.api2.OXException;
import com.openexchange.event.impl.EventClient;
import com.openexchange.folder.Folder;
import com.openexchange.folder.FolderException;
import com.openexchange.folder.FolderModule;
import com.openexchange.folder.FolderPermission;
import com.openexchange.folder.rdb.cache.RdbFolderCache;
import com.openexchange.folder.rdb.sql.RdbFolderSQL;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.calendar.CalendarCache;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Tasks;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link RdbFolderManager}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class RdbFolderManager {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(RdbFolderManager.class);

	private final Session session;

	private final Connection writeCon;

	private final UserConfiguration userConfiguration;

	private final Context ctx;

	private RdbFolderStorage folderStorage;

	/**
	 * Initializes a new {@link RdbFolderManager}
	 * 
	 * @param writeCon
	 *            A writable connection
	 */
	public RdbFolderManager(final Session session, final UserConfiguration userConfiguration, final Context ctx,
			final Connection writeCon) {
		super();
		this.session = session;
		this.ctx = ctx;
		this.writeCon = writeCon;
		this.userConfiguration = userConfiguration;
	}

	private RdbFolderStorage getFolderStorage() {
		if (folderStorage == null) {
			folderStorage = new RdbFolderStorage(userConfiguration, ctx, writeCon);
		}
		return folderStorage;
	}

	public RdbFolder deleteFolder(final RdbFolder fo, final boolean checkPermissions, final long lastModified)
			throws FolderException {
		if (fo.getParentFolderID() == null) {
			/*
			 * Incomplete, whereby its existence is checked
			 */
			final Folder<RdbFolderID> tmp = getFolderStorage().getFolder(fo.getFolderID());
			if (null == tmp) {
				throw new FolderException(FolderException.Code.FOLDER_NOT_FOUND, fo.getFolderID().toString());
			}
			fo.setParentFolderID(tmp.getParentFolderID());
		} else {
			/*
			 * Check existence
			 */
			if (!getFolderStorage().exists(fo.getFolderID())) {
				throw new FolderException(FolderException.Code.FOLDER_NOT_FOUND, fo.getFolderID().toString());
			}
		}
		final RdbEffectiveFolderPermission effectivePerm = ((RdbFolder) fo)
				.getEffectiveUserPermission(userConfiguration);
		if (!effectivePerm.isFolderVisible()) {
			if (!(effectivePerm.isAdmin(false) || (effectivePerm.getFolderPermission(false) >= FolderPermission.READ_FOLDER))) {
				throw new FolderException(FolderException.Code.NOT_VISIBLE, fo.getFolderID().toString());
			}
			final FolderException exc = new FolderException(FolderException.Code.NOT_VISIBLE, fo.getFolderID()
					.toString());
			exc.setCategory(Category.USER_CONFIGURATION);
			throw exc;
		}
		if (!effectivePerm.isAdmin()) {
			if (!effectivePerm.isAdmin(false)) {
				throw new FolderException(FolderException.Code.NO_ADMIN_ACCESS, fo.getFolderID().toString());
			}
			final FolderException exc = new FolderException(FolderException.Code.NO_ADMIN_ACCESS, fo.getFolderID()
					.toString());
			exc.setCategory(Category.USER_CONFIGURATION);
			throw exc;
		}
		/*
		 * Get parent
		 */
		final RdbFolder parent = (RdbFolder) getFolderStorage().getFolder(fo.getParentFolderID());
		/*
		 * Gather all deletable subfolders
		 */
		final HashMap<Integer, HashMap<?, ?>> deleteableFolders;
		try {
			deleteableFolders = gatherDeleteableFolders(fo.getFolderID());
		} catch (final SQLException e) {
			throw new RdbFolderException(RdbFolderException.Code.SQL_ERROR, e, e.getLocalizedMessage());
		}
		/*
		 * Delete folders
		 */
		deleteValidatedFolders(deleteableFolders, lastModified);
		/*
		 * TODO: Invalidate query caches
		 */
		/**
		 * <pre>
		 * if (FolderQueryCacheManager.isInitialized()) {
		 * 	FolderQueryCacheManager.getInstance().invalidateContextQueries(session);
		 * }
		 * </pre>
		 */
		if (CalendarCache.isInitialized()) {
			CalendarCache.getInstance().invalidateGroup(ctx.getContextId());
		}
		/*
		 * Check parent subfolder flag
		 */
		final boolean hasSubfolders = (RdbFolderSQL.getSubfolderIDs(parent.getFolderID().fuid, ctx, writeCon).size() > 0);
		RdbFolderSQL.updateSubfolderFlag(parent.getFolderID().fuid, hasSubfolders, lastModified, ctx, writeCon);
		/*
		 * Update cache
		 */
		if (RdbFolderProperties.getInstance().isEnableFolderCache()) {
			RdbFolderCache.getInstance().loadFolderObject(parent.getFolderID().fuid, ctx, writeCon);
		}
		/*
		 * Load return value
		 */
		getFolderStorage().fill(fo, true);
		try {
			new EventClient(session).delete(fo);
		} catch (final EventException e) {
			LOG.warn("Delete event could not be enqueued", e);
		} catch (final ContextException e) {
			LOG.warn("Delete event could not be enqueued", e);
		}
		return fo;
	}

	@SuppressWarnings("unchecked")
	private void deleteValidatedFolders(final HashMap<Integer, HashMap<?, ?>> deleteableIDs, final long lastModified)
			throws FolderException {
		final int deleteableIDsSize = deleteableIDs.size();
		final Iterator<Map.Entry<Integer, HashMap<?, ?>>> iter = deleteableIDs.entrySet().iterator();
		for (int i = 0; i < deleteableIDsSize; i++) {
			final Map.Entry<Integer, HashMap<?, ?>> entry = iter.next();
			final Integer folderID = entry.getKey();
			final HashMap<Integer, HashMap<?, ?>> hashMap = (HashMap<Integer, HashMap<?, ?>>) entry.getValue();
			/*
			 * Delete subfolders first, if any exist
			 */
			if (hashMap != null) {
				deleteValidatedFolders(hashMap, lastModified);
			}
			deleteValidatedFolder(folderID.intValue(), lastModified);
		}
	}

	private void deleteValidatedFolder(final int folderID, final long lastModified) throws FolderException {
		try {
			/*
			 * Delete folder
			 */
			final FolderModule module = getFolderStorage().getFolder(new RdbFolderID(folderID, ctx)).getModule();
			if (RdbFolderModule.MODULE_TASK.equals(module)) {
				deleteContainedTasks(folderID);
			} else if (RdbFolderModule.MODULE_CALENDAR.equals(module)) {
				deleteContainedAppointments(folderID);
			} else if (RdbFolderModule.MODULE_CONTACT.equals(module)) {
				deleteContainedContacts(folderID);
			} else if (RdbFolderModule.MODULE_PROJECT.equals(module)) {
				// TODO: Projects
				if (LOG.isTraceEnabled()) {
					LOG.trace("Currently unsupported module: " + RdbFolderModule.MODULE_PROJECT.getName());
				}
			} else if (RdbFolderModule.MODULE_INFOSTORE.equals(module)) {
				deleteContainedDocuments(folderID, lastModified);
			} else {
				throw new FolderException(FolderException.Code.UNKNOWN_MODULE, Integer.valueOf(module.getValue()));
			}
			/*
			 * Call SQL delete
			 */
			RdbFolderSQL.deleteFolder(folderID, userConfiguration.getUserId(), lastModified, ctx, writeCon);
			/*
			 * Remove from cache
			 */
			// TODO: Folder query cache
			/**
			 * <pre>
			 * if (FolderQueryCacheManager.isInitialized()) {
			 * 	FolderQueryCacheManager.getInstance().invalidateContextQueries(ctx.getContextId());
			 * }
			 * </pre>
			 */
			if (CalendarCache.isInitialized()) {
				CalendarCache.getInstance().invalidateGroup(ctx.getContextId());
			}
			if (RdbFolderProperties.getInstance().isEnableFolderCache()) {
				RdbFolderCache.getInstance().removeFolderObject(folderID, ctx);
			}
		} catch (final FolderException e) {
			throw e;
		} catch (final AbstractOXException e) {
			throw new FolderException(e);
		} catch (final SQLException e) {
			throw new RdbFolderException(RdbFolderException.Code.SQL_ERROR, e, e.getLocalizedMessage());
		}
	}

	private void deleteContainedAppointments(final int folderID) throws AbstractOXException, SQLException {
		new CalendarSql(session).deleteAppointmentsInFolder(folderID);
	}

	private void deleteContainedTasks(final int folderID) throws AbstractOXException {
		Tasks.getInstance().deleteTasksInFolder(session, folderID);
	}

	private void deleteContainedContacts(final int folderID) throws AbstractOXException {
		Contacts.trashContactsFromFolder(folderID, session, writeCon, writeCon, false);
	}

	private void deleteContainedDocuments(final int folderID, final long lastModified) throws AbstractOXException {
		final InfostoreFacade db = new InfostoreFacadeImpl(new DBPoolProvider());
		db.setTransactional(true);
		db.startTransaction();
		try {
			db.removeDocument(folderID, lastModified, new ServerSessionAdapter(session, ctx));
			db.commit();
		} catch (final AbstractOXException x) {
			db.rollback();
			throw x;
		} finally {
			db.finish();
		}
	}

	private HashMap<Integer, HashMap<?, ?>> gatherDeleteableFolders(final RdbFolderID folderID) throws FolderException,
			SQLException {
		final HashMap<Integer, HashMap<?, ?>> deleteableIDs = new HashMap<Integer, HashMap<?, ?>>();
		gatherDeleteableSubfoldersRecursively(folderID, deleteableIDs, folderID);
		return deleteableIDs;
	}

	private void gatherDeleteableSubfoldersRecursively(final RdbFolderID folderID,
			final HashMap<Integer, HashMap<?, ?>> deleteableIDs, final RdbFolderID initParent) throws FolderException,
			SQLException {
		final RdbFolder delFolder = (RdbFolder) getFolderStorage().getFolder(folderID);
		/*
		 * Check if shared
		 */
		if (delFolder.isShared(userConfiguration.getUserId())) {
			throw new FolderException(FolderException.Code.NO_SHARED_FOLDER_DELETION, folderID.toString());
		}
		/*
		 * Check if marked as default folder
		 */
		if (delFolder.isDefault()) {
			throw new FolderException(FolderException.Code.NO_DEFAULT_FOLDER_DELETION, folderID.toString());
		}
		/*
		 * Check user's effective permission
		 */
		final RdbEffectiveFolderPermission effectivePerm = delFolder.getEffectiveUserPermission(userConfiguration);
		if (!effectivePerm.isFolderVisible()) {
			if (!effectivePerm.isFolderVisible(false)) {
				if (initParent.equals(folderID)) {
					throw new FolderException(FolderException.Code.NOT_VISIBLE, folderID.toString());
				}
				throw new FolderException(FolderException.Code.HIDDEN_FOLDER_ON_DELETION, initParent.toString());
			}
			if (initParent.equals(folderID)) {
				final FolderException exc = new FolderException(FolderException.Code.NOT_VISIBLE, folderID.toString());
				exc.setCategory(Category.USER_CONFIGURATION);
				throw exc;
			}
			final FolderException exc = new FolderException(FolderException.Code.HIDDEN_FOLDER_ON_DELETION, initParent
					.toString());
			exc.setCategory(Category.USER_CONFIGURATION);
			throw exc;
		} else if (!effectivePerm.isAdmin()) {
			if (!effectivePerm.isAdmin(false)) {
				throw new FolderException(FolderException.Code.NO_ADMIN_ACCESS, folderID.toString());
			}
			final FolderException exc = new FolderException(FolderException.Code.NO_ADMIN_ACCESS, folderID.toString());
			exc.setCategory(Category.USER_CONFIGURATION);
			throw exc;
		}
		/*
		 * Check delete permission on folder's objects
		 */
		if (!canDeleteAllObjectsInFolder(delFolder)) {
			throw new FolderException(FolderException.Code.NOT_ALL_OBJECTS_DELETION, folderID.toString());
		}
		/*
		 * Check, if folder has subfolders
		 */
		final List<Integer> subfolders = RdbFolderSQL.getSubfolderIDs(folderID.fuid, ctx, writeCon);
		if (subfolders.isEmpty()) {
			/*
			 * No subfolders detected
			 */
			deleteableIDs.put(Integer.valueOf(folderID.fuid), null);
			return;
		}
		final HashMap<Integer, HashMap<?, ?>> subMap = new HashMap<Integer, HashMap<?, ?>>();
		final int size = subfolders.size();
		final Iterator<Integer> it = subfolders.iterator();
		for (int i = 0; i < size; i++) {
			final int fuid = it.next().intValue();
			gatherDeleteableSubfoldersRecursively(new RdbFolderID(fuid, ctx), subMap, initParent);
		}
		deleteableIDs.put(Integer.valueOf(folderID.fuid), subMap);
	}

	private boolean canDeleteAllObjectsInFolder(final RdbFolder fo) throws FolderException {
		final int userId = session.getUserId();
		try {
			/*
			 * Check user permission on folder
			 */
			final RdbEffectiveFolderPermission effectivePerm = fo.getEffectiveUserPermission(userConfiguration);
			if (!effectivePerm.isFolderVisible()) {
				/*
				 * Folder is not visible to user
				 */
				return false;
			} else if (effectivePerm.canDeleteAllObjects()) {
				/*
				 * Can delete all objects
				 */
				return true;
			} else if (effectivePerm.canDeleteOwnObjects()) {
				// TODO: Additional parameter for readable connection
				/*
				 * User may only delete own objects. Check if folder contains
				 * foreign objects which must not be deleted.
				 */
				if (RdbFolderModule.MODULE_TASK.equals(fo.getModule())) {
					return !Tasks.getInstance().containsNotSelfCreatedTasks(session, fo.getFolderID().fuid);
				} else if (RdbFolderModule.MODULE_CALENDAR.equals(fo.getModule())) {
					return !new CalendarSql(session).checkIfFolderContainsForeignObjects(userId, fo.getFolderID().fuid);
				} else if (RdbFolderModule.MODULE_CONTACT.equals(fo.getModule())) {
					return !Contacts.containsForeignObjectInFolder(fo.getFolderID().fuid, userId, session);
				} else if (RdbFolderModule.MODULE_PROJECT.equals(fo.getModule())) {
					// TODO: Projects
					if (LOG.isTraceEnabled()) {
						LOG.trace("Currently unsupported module: " + RdbFolderModule.MODULE_PROJECT.getName());
					}
				} else if (RdbFolderModule.MODULE_INFOSTORE.equals(fo.getModule())) {
					return !new InfostoreFacadeImpl(new DBPoolProvider()).hasFolderForeignObjects(
							fo.getFolderID().fuid, ctx, UserStorage.getStorageUser(session.getUserId(), ctx),
							userConfiguration);
				} else {
					throw new FolderException(FolderException.Code.UNKNOWN_MODULE, Integer.valueOf(fo.getModule()
							.getValue()));
				}
			} else {
				/*
				 * No delete permission: Return true if folder is empty
				 */
				if (RdbFolderModule.MODULE_TASK.equals(fo.getModule())) {
					return !Tasks.getInstance().isFolderEmpty(ctx, fo.getFolderID().fuid);
				} else if (RdbFolderModule.MODULE_CALENDAR.equals(fo.getModule())) {
					return !new CalendarSql(session).isFolderEmpty(userId, fo.getFolderID().fuid);
				} else if (RdbFolderModule.MODULE_CONTACT.equals(fo.getModule())) {
					return !Contacts.containsAnyObjectInFolder(fo.getFolderID().fuid, ctx);
				} else if (RdbFolderModule.MODULE_PROJECT.equals(fo.getModule())) {
					// TODO: Projects
					if (LOG.isTraceEnabled()) {
						LOG.trace("Currently unsupported module: " + RdbFolderModule.MODULE_PROJECT.getName());
					}
				} else if (RdbFolderModule.MODULE_INFOSTORE.equals(fo.getModule())) {
					return !new InfostoreFacadeImpl(new DBPoolProvider()).isFolderEmpty(fo.getFolderID().fuid, ctx);
				} else {
					throw new FolderException(FolderException.Code.UNKNOWN_MODULE, Integer.valueOf(fo.getModule()
							.getValue()));
				}
			}
			return false;
		} catch (SQLException e) {
			throw new RdbFolderException(RdbFolderException.Code.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
		} catch (DBPoolingException e) {
			throw new FolderException(e);
		} catch (Throwable t) {
			throw new FolderException(FolderException.Code.RUNTIME_ERROR, t, t.getLocalizedMessage());
		}
	}
}
