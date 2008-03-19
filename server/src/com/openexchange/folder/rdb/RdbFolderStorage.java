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

import java.sql.Connection;
import java.sql.SQLException;

import com.openexchange.folder.Folder;
import com.openexchange.folder.FolderException;
import com.openexchange.folder.FolderPermission;
import com.openexchange.folder.FolderStorage;
import com.openexchange.folder.rdb.cache.RdbFolderCache;
import com.openexchange.folder.rdb.sql.RdbFolderSQL;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.session.Session;

/**
 * {@link RdbFolderStorage}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class RdbFolderStorage implements FolderStorage<RdbFolderID> {

	private final Session session;

	private Connection readCon;

	private Context ctx;

	private UserConfiguration userConfiguration;

	/**
	 * Initializes a new {@link RdbFolderStorage}
	 * 
	 * @param session
	 *            The session providing needed user data
	 */
	public RdbFolderStorage(final Session session) {
		super();
		this.session = session;
	}

	/**
	 * Initializes a new {@link RdbFolderStorage}
	 * 
	 * @param userConfiguration
	 *            The user configuration
	 * @param ctx
	 *            The context
	 * @param readCon
	 *            A readable connection
	 */
	RdbFolderStorage(final UserConfiguration userConfiguration, final Context ctx, final Connection readCon) {
		super();
		this.session = null;
		this.readCon = readCon;
		this.userConfiguration = userConfiguration;
		this.ctx = ctx;
	}

	private Context getContext() throws FolderException {
		if (null == ctx) {
			try {
				ctx = ContextStorage.getStorageContext(session);
			} catch (final ContextException e) {
				throw new FolderException(e);
			}
		}
		return ctx;
	}

	private UserConfiguration getUserConfiguration() throws FolderException {
		if (null == userConfiguration) {
			try {
				userConfiguration = UserConfigurationStorage.getInstance().getUserConfiguration(session.getUserId(),
						getContext());
			} catch (final UserConfigurationException e) {
				throw new FolderException(e);
			}
		}
		return userConfiguration;
	}

	public void delete(final Folder<RdbFolderID> folder, final Long clientLastModified) throws FolderException {
		fill(folder, false);
		if (folder.getType() == RdbFolderType.TYPE_PUBLIC && !getUserConfiguration().hasFullPublicFolderAccess()) {
			throw new RdbFolderException(RdbFolderException.Code.NO_PUBLIC_FOLDER_WRITE_ACCESS, folder.getFolderID());
		}
		if (!exists(folder.getFolderID())) {
			throw new FolderException(FolderException.Code.FOLDER_NOT_FOUND, folder.getFolderID());
		}
		if (clientLastModified != null
				&& getFolder(folder.getFolderID()).getLastModified().getTime() > clientLastModified.longValue()) {
			throw new FolderException(FolderException.Code.CONCURRENT_MODIFICATION, folder.getFolderID().toString());
		}
		if (!userConfiguration.hasModuleAccess(folder.getModule().getValue())) {
			throw new FolderException(FolderException.Code.NO_MODULE_ACCESS, folder.getModule().getName());
		}
		final long lastModified = System.currentTimeMillis();
		new OXFolderManagerImpl(session, oxfolderAccess).deleteFolder(folderobject, false, lastModified);
	}

	public void fill(final Folder<RdbFolderID> folder, final boolean overwrite) throws FolderException {
		if (folder.getFolderID() == null) {
			throw new FolderException(FolderException.Code.MISSING_ID);
		}
		final Folder<RdbFolderID> storageFolder = getFolder(folder.getFolderID());
		if (storageFolder == null) {
			throw new FolderException(FolderException.Code.FOLDER_NOT_FOUND, folder.getFolderID().toString());
		}
		if (overwrite) {
			folder.reset();
		}
		if (overwrite || folder.getCreatedBy() == -1) {
			folder.setCreatedBy(storageFolder.getCreatedBy());
		}
		if (overwrite || folder.getCreationDate() == null) {
			folder.setCreationDate(storageFolder.getCreationDate());
		}
		if (overwrite || !folder.containsDefault()) {
			folder.setDefault(storageFolder.isDefault());
		}
		if (overwrite || folder.getName() == null) {
			folder.setName(storageFolder.getName());
		}
		if (overwrite || folder.getModifiedBy() == -1) {
			folder.setModifiedBy(storageFolder.getModifiedBy());
		}
		if (overwrite || folder.getLastModified() == null) {
			folder.setLastModified(storageFolder.getLastModified());
		}
		if (overwrite || folder.getModule() == null) {
			folder.setModule(storageFolder.getModule());
		}
		if (overwrite || folder.getParentFolderID() == null) {
			folder.setParentFolderID(storageFolder.getParentFolderID());
		}
		if (overwrite || folder.getPermissionStatus() == null) {
			folder.setPermissionStatus(storageFolder.getPermissionStatus());
		}
		if (overwrite || folder.getPermissions() == null) {
			folder.setPermissions(storageFolder.getPermissions());
		}
		if (overwrite || !folder.containsHasSubfolder()) {
			folder.setHasSubfolder(storageFolder.hasSubfolders());
		}
		if (overwrite || folder.getType() == null) {
			folder.setType(storageFolder.getType());
		}
	}

	public Folder<RdbFolderID>[] getAllModifiedFolders(final long timestamp) throws FolderException {

		return null;
	}

	public Folder<RdbFolderID>[] getDeletedFolders(final long timestamp, final int user) throws FolderException {
		// TODO Auto-generated method stub
		return null;
	}

	public FolderPermission<RdbFolderID> getEffectivePermission(final RdbFolderID folderId, final int user)
			throws FolderException {

		// TODO Auto-generated method stub
		return null;
	}

	public boolean exists(final RdbFolderID folderId) throws FolderException {
		try {
			final boolean exists = readCon == null ? RdbFolderSQL.exists(folderId.fuid, folderId.ctx) : RdbFolderSQL
					.exists(folderId.fuid, folderId.ctx, readCon);
			if (!exists && RdbFolderProperties.getInstance().isEnableFolderCache()) {
				/*
				 * Remove from cache
				 */
				RdbFolderCache.getInstance().removeFolderObject(folderId.fuid, folderId.ctx);
			}
			return exists;
		} catch (final DBPoolingException e) {
			throw new FolderException(e);
		} catch (final SQLException e) {
			throw new RdbFolderException(RdbFolderException.Code.SQL_ERROR, e, e.getMessage());
		}
	}

	public Folder<RdbFolderID> getFolder(final RdbFolderID folderId) throws FolderException {
		Folder<RdbFolderID> retval = null;
		if (RdbFolderProperties.getInstance().isEnableFolderCache()) {
			retval = RdbFolderCache.getInstance().getFolderObject(folderId.fuid, true, folderId.ctx, readCon);
		}
		if (null == retval) {
			return loadFromStorage(folderId);
		}
		return retval;
	}

	public Folder<RdbFolderID>[] getModifiedFolders(final long timestamp, final int user) throws FolderException {
		// TODO Auto-generated method stub
		return null;
	}

	public Folder<RdbFolderID>[] getPath2Root(final RdbFolderID folderId, final int user) {
		// TODO Auto-generated method stub
		return null;
	}

	public RdbFolderID getRootFolderId() {
		// TODO Auto-generated method stub
		return null;
	}

	public Folder<RdbFolderID>[] getSubfolders(final RdbFolderID parentId, final Long timestamp) throws FolderException {
		// TODO Auto-generated method stub
		return null;
	}

	public Folder<RdbFolderID>[] getVisibleSubfolders(final RdbFolderID parentId, final int user, final Long timestamp)
			throws FolderException {
		// TODO Auto-generated method stub
		return null;
	}

	public void insert(final Folder<RdbFolderID> folder) throws FolderException {
		// TODO Auto-generated method stub

	}

	public Folder<RdbFolderID> loadFromStorage(final RdbFolderID folderId) throws FolderException {
		try {
			RdbFolder fld = readCon == null ? RdbFolderSQL.loadFolder(folderId.fuid, folderId.ctx) : RdbFolderSQL
					.loadFolder(folderId.fuid, folderId.ctx, readCon);
			if (RdbFolderProperties.getInstance().isEnableFolderCache()) {
				if (null == fld) {
					/*
					 * Remove from cache
					 */
					RdbFolderCache.getInstance().removeFolderObject(folderId.fuid, folderId.ctx);
					/*
					 * Try from backup tables
					 */
					fld = readCon == null ? RdbFolderSQL.loadBackupFolder(folderId.fuid, folderId.ctx) : RdbFolderSQL
							.loadBackupFolder(folderId.fuid, folderId.ctx, readCon);
				} else {
					/*
					 * Put into cache
					 */
					RdbFolderCache.getInstance().putFolderObject(fld, folderId.ctx, true, null);
				}
			}
			return fld;
		} catch (final DBPoolingException e) {
			throw new FolderException(e);
		} catch (final SQLException e) {
			throw new RdbFolderException(RdbFolderException.Code.SQL_ERROR, e, e.getMessage());
		}
	}

	public void update(final Folder<RdbFolderID> folder, final Long clientLastModified) throws FolderException {
		// TODO Auto-generated method stub

	}

}
