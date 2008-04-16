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

package com.openexchange.tools.oxfolder.downgrade;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.openexchange.api2.OXException;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.cache.impl.FolderQueryCacheManager;
import com.openexchange.groupware.calendar.CalendarCache;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.downgrade.DowngradeEvent;
import com.openexchange.groupware.downgrade.DowngradeFailedException;
import com.openexchange.groupware.downgrade.DowngradeListener;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.tasks.Tasks;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.groupware.tx.StaticDBPoolProvider;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;
import com.openexchange.tools.oxfolder.downgrade.sql.OXFolderDowngradeSQL;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link OXFolderDowngradeListener} - Performs deletion of unused folder data
 * remaining from a former downgrade.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class OXFolderDowngradeListener extends DowngradeListener {

	private static final String TABLE_FOLDER_WORKING = "oxfolder_tree";

	private static final String TABLE_PERMISSIONS_WORKING = "oxfolder_permissions";

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(OXFolderDowngradeListener.class);

	/**
	 * Initializes a new {@link OXFolderDowngradeListener}
	 */
	public OXFolderDowngradeListener() {
		super();
	}

	@Override
	public void downgradePerformed(final DowngradeEvent event) throws DowngradeFailedException {
		final UserConfiguration newUserConfiguration = event.getNewUserConfiguration();
		if (!newUserConfiguration.hasCalendar()) {
			/*
			 * User lost calendar module access:
			 */
			try {
				deleteCalendarFolderData(newUserConfiguration.getUserId(), event);
			} catch (final OXException e) {
				throw new DowngradeFailedException(e);
			}
			if (LOG.isInfoEnabled()) {
				LOG.info("All task-related folder data removed due to loss of task module access");
			}
		}
		if (!newUserConfiguration.hasTask()) {
			/*
			 * User lost task module access:
			 */
			try {
				deleteTaskFolderData(newUserConfiguration.getUserId(), event);
			} catch (final OXException e) {
				throw new DowngradeFailedException(e);
			}
			if (LOG.isInfoEnabled()) {
				LOG.info("All calendar-related folder data removed due to loss of task module access");
			}
		}
		if (!newUserConfiguration.hasInfostore()) {
			/*
			 * User lost infostore module access:
			 */
			try {
				deleteInfostoreFolderData(newUserConfiguration.getUserId(), event);
			} catch (final OXException e) {
				throw new DowngradeFailedException(e);
			}
			if (LOG.isInfoEnabled()) {
				LOG.info("All infostore-related folder data removed due to loss of task module access");
			}
		}
		if (!newUserConfiguration.hasFullSharedFolderAccess()) {
			/*
			 * User lost full shared folder access:
			 */
			try {
				deleteSharedFolderData(newUserConfiguration.getUserId(), event);
			} catch (final OXFolderException e) {
				throw new DowngradeFailedException(e);
			}
			if (LOG.isInfoEnabled()) {
				LOG.info("All shared folder data removed due to loss of full shared folder access");
			}
		}
		/*
		 * Update affected context's query caches
		 */
		try {
			if (FolderQueryCacheManager.isInitialized()) {
				FolderQueryCacheManager.getInstance().invalidateContextQueries(event.getContext().getContextId());
			}
			if (CalendarCache.isInitialized()) {
				CalendarCache.getInstance().invalidateGroup(event.getContext().getContextId());
			}
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * Delete calendar folder data:
	 * <ul>
	 * <li>Delete all private folders whose module is calendar except the
	 * default calendar folder</li>
	 * <li>Delete all shared permissions assigned to default calendar folder</li>
	 * <li>Delete all public calendar folders' permissions assigned to affected
	 * user; reassign to context admin if necessary</li>
	 * </ul>
	 * 
	 * @param entity
	 *            The user's ID
	 * @param event
	 *            The downgrade event
	 * @throws OXException
	 *             If a folder error occurs
	 */
	private static void deleteCalendarFolderData(final int entity, final DowngradeEvent event) throws OXException {
		deleteModuleFolderData(entity, FolderObject.CALENDAR, event, true);
	}

	/**
	 * Delete task folder data:
	 * <ul>
	 * <li>Delete all private folders whose module is task except the default
	 * task folder</li>
	 * <li>Delete all shared permissions assigned to default task folder</li>
	 * <li>Delete all public task folders' permissions assigned to affected
	 * user; reassign to context admin if necessary</li>
	 * </ul>
	 * 
	 * @param entity
	 *            The user's ID
	 * @param event
	 *            The downgrade event
	 * @throws OXException
	 *             If a folder error occurs
	 */
	private static void deleteTaskFolderData(final int entity, final DowngradeEvent event) throws OXException {
		deleteModuleFolderData(entity, FolderObject.TASK, event, true);
	}

	/**
	 * Delete infostore folder data:
	 * <ul>
	 * <li>Delete all public infostore folders' permissions assigned to
	 * affected user; reassign to context admin if necessary</li>
	 * </ul>
	 * <p>
	 * Folder cache is updated, too
	 * 
	 * @param entity
	 *            The user's ID
	 * @param event
	 *            The downgrade event
	 * @throws OXException
	 *             If a folder error occurs
	 */
	private void deleteInfostoreFolderData(final int entity, final DowngradeEvent event) throws OXException {
		final int cid = event.getContext().getContextId();
		try {
			/*
			 * Strip permissions on default folder
			 */
			final int fuid = OXFolderDowngradeSQL.cleanDefaultModuleFolder(entity, FolderObject.INFOSTORE, cid,
					TABLE_FOLDER_WORKING, TABLE_PERMISSIONS_WORKING, event.getWriteCon());
			if (fuid != -1) {
				removeFromFolderCache(new int[] { fuid }, event.getContext());
			}
			/*
			 * Remove subfolders below default folder
			 */
			final int[] fuids = OXFolderDowngradeSQL.gatherSubInfostoreFolders(entity, cid, TABLE_FOLDER_WORKING,
					TABLE_PERMISSIONS_WORKING, event.getWriteCon());
			deleteFoldersContent(fuids, event);
			OXFolderDowngradeSQL.deleteFolderPermissions(fuids, cid, TABLE_PERMISSIONS_WORKING, event.getWriteCon());
			OXFolderDowngradeSQL.deleteFolders(fuids, cid, TABLE_FOLDER_WORKING, event.getWriteCon());
			/*
			 * Update cache
			 */
			removeFromFolderCache(fuids, event.getContext());
		} catch (final SQLException e) {
			throw new OXFolderException(OXFolderException.FolderCode.SQL_ERROR, e, Integer.valueOf(cid));
		}
		/*
		 * Strip all user permission from other (public) infostore folders
		 */
		deleteModuleFolderData(entity, FolderObject.INFOSTORE, event, false);
	}

	/**
	 * Deletes specified module's folder data and updates the folder cache
	 * 
	 * @param entity
	 *            The entity whose folder data ought to be deleted
	 * @param module
	 *            The module
	 * @param event
	 *            The downgrade event providing needed information
	 * @param checkPrivate
	 *            <code>true</code> to also check module's private folders;
	 *            otherwise <code>false</code>
	 * @throws OXException
	 *             If deleting module's folder data fails
	 */
	private static void deleteModuleFolderData(final int entity, final int module, final DowngradeEvent event,
			final boolean checkPrivate) throws OXException {
		final Set<Integer> ids = new HashSet<Integer>(128);
		final int cid = event.getContext().getContextId();
		final Connection writeCon = event.getWriteCon();
		try {
			int[] fuids = null;
			if (checkPrivate) {
				/*
				 * Clear all non-default private folders of specified module
				 */
				fuids = OXFolderDowngradeSQL.getModulePrivateFolders(module, entity, cid, TABLE_FOLDER_WORKING,
						writeCon);
				deleteFoldersContent(fuids, event);
				OXFolderDowngradeSQL.deleteFolderPermissions(fuids, cid, TABLE_PERMISSIONS_WORKING, writeCon);
				OXFolderDowngradeSQL.deleteFolders(fuids, cid, TABLE_FOLDER_WORKING, writeCon);
				for (final int id : fuids) {
					ids.add(Integer.valueOf(id));
				}
				fuids = null;
				/*
				 * Remove default folder's shared permissions
				 */
				final int fuid = OXFolderDowngradeSQL.cleanDefaultModuleFolder(entity, module, cid,
						TABLE_FOLDER_WORKING, TABLE_PERMISSIONS_WORKING, writeCon);
				if (fuid != -1) {
					ids.add(Integer.valueOf(fuid));
				}
			}
			/*
			 * Handle module's public folders
			 */
			fuids = OXFolderDowngradeSQL.getAffectedPublicFolders(entity, module, cid, TABLE_FOLDER_WORKING,
					TABLE_PERMISSIONS_WORKING, writeCon);
			for (final int fuid : fuids) {
				OXFolderDowngradeSQL.handleAffectedPublicFolder(entity, fuid, cid, TABLE_PERMISSIONS_WORKING, writeCon);
				ids.add(Integer.valueOf(fuid));
			}
		} catch (final SQLException e) {
			throw new OXFolderException(OXFolderException.FolderCode.SQL_ERROR, e, Integer.valueOf(cid));
		}
		/*
		 * Update cache
		 */
		removeFromFolderCache(ids, event.getContext());
	}

	/**
	 * Deletes folders' content
	 * 
	 * @param fuids
	 *            The folder IDs
	 * @param event
	 *            The downgrade event
	 * @throws OXException
	 *             If deleting contents fails
	 */
	private static void deleteFoldersContent(int[] fuids, final DowngradeEvent event) throws OXException {
		final OXFolderAccess access = new OXFolderAccess(event.getWriteCon(), event.getContext());
		for (int i = 0; i < fuids.length; i++) {
			// Delete folder content
			final int imodule = access.getFolderModule(fuids[i]);
			switch (imodule) {
			case FolderObject.CALENDAR:
				deleteContainedAppointments(fuids[i], event);
				break;
			case FolderObject.TASK:
				deleteContainedTasks(fuids[i], event);
				break;
			case FolderObject.CONTACT:
				deleteContainedContacts(fuids[i], event);
				break;
			case FolderObject.UNBOUND:
				break;
			case FolderObject.INFOSTORE:
				deleteContainedDocuments(fuids[i], event);
				break;
			case FolderObject.PROJECT:
				break;
			default:
				throw new OXFolderException(OXFolderException.FolderCode.UNKNOWN_MODULE, Integer.valueOf(imodule),
						Integer.valueOf(event.getContext().getContextId()));
			}
		}
	}

	private static void deleteContainedAppointments(final int folderID, final DowngradeEvent event) throws OXException {
		final CalendarSql cSql = new CalendarSql(event.getSession());
		try {
			if (null == event.getWriteCon()) {
				cSql.deleteAppointmentsInFolder(folderID);
			} else {
				cSql.deleteAppointmentsInFolder(folderID, event.getWriteCon());
			}
		} catch (final SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(event.getContext().getContextId()));
		}
	}

	private static void deleteContainedTasks(final int folderID, final DowngradeEvent event) throws OXException {
		final Tasks tasks = Tasks.getInstance();
		if (null == event.getWriteCon()) {
			Connection wc = null;
			try {
				wc = DBPool.pickupWriteable(event.getContext());
				tasks.deleteTasksInFolder(event.getSession(), wc, folderID);
			} catch (final DBPoolingException e) {
				throw new OXException(e);
			} finally {
				if (null != wc) {
					DBPool.closeWriterSilent(event.getContext(), wc);
				}
			}
		} else {
			tasks.deleteTasksInFolder(event.getSession(), event.getWriteCon(), folderID);
		}
	}

	private static void deleteContainedContacts(final int folderID, final DowngradeEvent event) throws OXException {
		try {
			Connection writeCon = event.getWriteCon();
			final boolean createWriteCon = (writeCon == null);
			if (createWriteCon) {
				writeCon = DBPool.pickupWriteable(event.getContext());
			}
			try {
				Contacts.trashContactsFromFolder(folderID, event.getSession(), writeCon, writeCon, false);
			} finally {
				if (createWriteCon && writeCon != null) {
					DBPool.closeWriterSilent(event.getContext(), writeCon);
				}
			}
		} catch (final DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(event.getContext()
					.getContextId()));
		}
	}

	private static void deleteContainedDocuments(final int folderID, final DowngradeEvent event) throws OXException {
		final InfostoreFacade db;
		if (event.getWriteCon() == null) {
			db = new InfostoreFacadeImpl(new DBPoolProvider());
		} else {
			db = new InfostoreFacadeImpl(new StaticDBPoolProvider(event.getWriteCon()));
			db.setCommitsTransaction(false);
		}
		db.setTransactional(true);
		db.startTransaction();
		try {
			db.removeDocument(folderID, System.currentTimeMillis(), new ServerSessionAdapter(event.getSession(), event
					.getContext()));
			db.commit();
		} catch (final OXException x) {
			db.rollback();
			throw x;
		} finally {
			db.finish();
		}
	}

	/**
	 * Deletes the shared folder data and updates cache
	 * 
	 * @param entity
	 *            The entity
	 * @param event
	 *            The downgrade event
	 * @throws OXFolderException
	 *             If deleting the shared folder data fails
	 */
	private static void deleteSharedFolderData(final int entity, final DowngradeEvent event) throws OXFolderException {
		final int cid = event.getContext().getContextId();
		final Set<Integer> set = new HashSet<Integer>();
		try {
			final Set<Integer> tmp = OXFolderDowngradeSQL.removeShareAccess(entity, cid, TABLE_FOLDER_WORKING,
					TABLE_PERMISSIONS_WORKING, event.getWriteCon());
			set.addAll(tmp);
		} catch (final SQLException e) {
			throw new OXFolderException(OXFolderException.FolderCode.SQL_ERROR, e, Integer.valueOf(cid));
		}
		/*
		 * Update cache
		 */
		removeFromFolderCache(set, event.getContext());
	}

	private static void removeFromFolderCache(final Collection<Integer> collection, final Context ctx) {
		final int[] ints = new int[collection.size()];
		final Iterator<Integer> iter = collection.iterator();
		for (int i = 0; i < ints.length; i++) {
			ints[i] = iter.next().intValue();
		}
		removeFromFolderCache(ints, ctx);
	}

	private static void removeFromFolderCache(final int[] folderIDs, final Context ctx) {
		/*
		 * Remove from cache
		 */
		if (FolderCacheManager.isEnabled() && FolderCacheManager.isInitialized()) {
			try {
				FolderCacheManager.getInstance().removeFolderObjects(folderIDs, ctx);
			} catch (final OXException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	public int getOrder() {
		return 10;
	}
}
