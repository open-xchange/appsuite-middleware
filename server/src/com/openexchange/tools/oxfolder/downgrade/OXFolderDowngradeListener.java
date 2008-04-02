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

import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.downgrade.DowngradeEvent;
import com.openexchange.groupware.downgrade.DowngradeFailedException;
import com.openexchange.groupware.downgrade.DowngradeListener;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.downgrade.sql.OXFolderDowngradeSQL;

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

	private static final String TABLE_FOLDER_BACKUP = "del_oxfolder_tree";

	private static final String TABLE_PERMISSIONS_BACKUP = "del_oxfolder_permissions";

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
		final Context ctx = event.getContext();
		final Connection writeCon = event.getWriteCon();
		if (!newUserConfiguration.hasCalendar()) {
			/*
			 * User lost calendar module access:
			 */
			try {
				deleteCalendarFolderData(newUserConfiguration.getUserId(), ctx.getContextId(), writeCon);
			} catch (final OXFolderException e) {
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
				deleteTaskFolderData(newUserConfiguration.getUserId(), ctx.getContextId(), writeCon);
			} catch (OXFolderException e) {
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
				deleteInfostoreFolderData(newUserConfiguration.getUserId(), ctx.getContextId(), writeCon);
			} catch (OXFolderException e) {
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
				deleteSharedFolderData(newUserConfiguration.getUserId(), ctx.getContextId(), writeCon);
			} catch (OXFolderException e) {
				throw new DowngradeFailedException(e);
			}
			if (LOG.isInfoEnabled()) {
				LOG.info("All shared folder data removed due to loss of full shared folder access");
			}
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
	 * @param cid
	 *            The context ID
	 * @param writeCon
	 *            A writable connection
	 * @throws OXFolderException
	 *             If a folder error occurs
	 */
	private static void deleteCalendarFolderData(final int entity, final int cid, final Connection writeCon)
			throws OXFolderException {
		deleteModuleFolderData(entity, FolderObject.CALENDAR, cid, writeCon, true);
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
	 * @param cid
	 *            The context ID
	 * @param writeCon
	 *            A writable connection
	 * @throws OXFolderException
	 *             If a folder error occurs
	 */
	private static void deleteTaskFolderData(final int entity, final int cid, final Connection writeCon)
			throws OXFolderException {
		deleteModuleFolderData(entity, FolderObject.TASK, cid, writeCon, true);
	}

	/**
	 * Delete infostore folder data:
	 * <ul>
	 * <li>Delete all public infostore folders' permissions assigned to
	 * affected user; reassign to context admin if necessary</li>
	 * </ul>
	 * 
	 * @param entity
	 *            The user's ID
	 * @param cid
	 *            The context ID
	 * @param writeCon
	 *            A writable connection
	 * @throws OXFolderException
	 *             If a folder error occurs
	 */
	private static void deleteInfostoreFolderData(final int entity, final int cid, final Connection writeCon)
			throws OXFolderException {
		try {
			/*
			 * Strip permissions on default folder
			 */
			OXFolderDowngradeSQL.cleanDefaultModuleFolder(entity, FolderObject.INFOSTORE, cid, TABLE_FOLDER_BACKUP,
					TABLE_PERMISSIONS_BACKUP, writeCon);
			OXFolderDowngradeSQL.cleanDefaultModuleFolder(entity, FolderObject.INFOSTORE, cid, TABLE_FOLDER_WORKING,
					TABLE_PERMISSIONS_WORKING, writeCon);
			/*
			 * Remove subfolders below default folder
			 */
			OXFolderDowngradeSQL.removeSubInfostoreFolders(entity, cid, TABLE_FOLDER_BACKUP, TABLE_PERMISSIONS_BACKUP,
					writeCon);
			OXFolderDowngradeSQL.removeSubInfostoreFolders(entity, cid, TABLE_FOLDER_WORKING,
					TABLE_PERMISSIONS_WORKING, writeCon);
		} catch (final SQLException e) {
			throw new OXFolderException(OXFolderException.FolderCode.SQL_ERROR, e, Integer.valueOf(cid));
		}
		/*
		 * Strip all user permission from other (public) infostore folders
		 */
		deleteModuleFolderData(entity, FolderObject.INFOSTORE, cid, writeCon, false);
	}

	private static void deleteModuleFolderData(final int entity, final int module, final int cid,
			final Connection writeCon, final boolean checkPrivate) throws OXFolderException {
		try {
			int[] fuids = null;
			if (checkPrivate) {
				/*
				 * Clear all non-default private folders of specified module
				 */
				fuids = OXFolderDowngradeSQL
						.getModulePrivateFolders(module, entity, cid, TABLE_FOLDER_BACKUP, writeCon);
				OXFolderDowngradeSQL.deleteFolderPermissions(fuids, cid, TABLE_PERMISSIONS_BACKUP, writeCon);
				OXFolderDowngradeSQL.deleteFolders(fuids, cid, TABLE_FOLDER_BACKUP, writeCon);
				fuids = OXFolderDowngradeSQL.getModulePrivateFolders(module, entity, cid, TABLE_FOLDER_WORKING,
						writeCon);
				OXFolderDowngradeSQL.deleteFolderPermissions(fuids, cid, TABLE_PERMISSIONS_WORKING, writeCon);
				OXFolderDowngradeSQL.deleteFolders(fuids, cid, TABLE_FOLDER_WORKING, writeCon);
				fuids = null;
				/*
				 * Remove default folder's shared permissions
				 */
				OXFolderDowngradeSQL.cleanDefaultModuleFolder(entity, module, cid, TABLE_FOLDER_BACKUP,
						TABLE_PERMISSIONS_BACKUP, writeCon);
				OXFolderDowngradeSQL.cleanDefaultModuleFolder(entity, module, cid, TABLE_FOLDER_WORKING,
						TABLE_PERMISSIONS_WORKING, writeCon);
			}
			/*
			 * Handle module's public folders
			 */
			fuids = OXFolderDowngradeSQL.getAffectedPublicFolders(entity, module, cid, TABLE_FOLDER_BACKUP,
					TABLE_PERMISSIONS_BACKUP, writeCon);
			for (final int fuid : fuids) {
				OXFolderDowngradeSQL.handleAffectedPublicFolder(entity, fuid, cid, TABLE_PERMISSIONS_BACKUP, writeCon);
			}
			fuids = OXFolderDowngradeSQL.getAffectedPublicFolders(entity, module, cid, TABLE_FOLDER_WORKING,
					TABLE_PERMISSIONS_BACKUP, writeCon);
			for (final int fuid : fuids) {
				OXFolderDowngradeSQL.handleAffectedPublicFolder(entity, fuid, cid, TABLE_PERMISSIONS_WORKING, writeCon);
			}
		} catch (final SQLException e) {
			throw new OXFolderException(OXFolderException.FolderCode.SQL_ERROR, e, Integer.valueOf(cid));
		}
	}

	private static void deleteSharedFolderData(final int entity, final int cid, final Connection writeCon)
			throws OXFolderException {
		try {
			OXFolderDowngradeSQL
					.removeShareAccess(entity, cid, TABLE_FOLDER_BACKUP, TABLE_PERMISSIONS_BACKUP, writeCon);
			OXFolderDowngradeSQL.removeShareAccess(entity, cid, TABLE_FOLDER_WORKING, TABLE_PERMISSIONS_WORKING,
					writeCon);
		} catch (final SQLException e) {
			throw new OXFolderException(OXFolderException.FolderCode.SQL_ERROR, e, Integer.valueOf(cid));
		}
	}

	@Override
	public int getOrder() {
		return 10;
	}
}
