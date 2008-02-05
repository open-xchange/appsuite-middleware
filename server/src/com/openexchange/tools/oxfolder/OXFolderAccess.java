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

import static com.openexchange.tools.oxfolder.OXFolderManagerImpl.folderModule2String;
import static com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.openexchange.api2.OXException;
import com.openexchange.cache.OXCachingException;
import com.openexchange.cache.impl.FolderCacheManager;
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
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;

/**
 * OXFolderQuery
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OXFolderAccess {

	/**
	 * Readable connection
	 */
	private final Connection readCon;

	/**
	 * Associated Context
	 */
	private final Context ctx;

	public OXFolderAccess(final Context ctx) {
		this(null, ctx);
	}

	public OXFolderAccess(final Connection readCon, final Context ctx) {
		super();
		this.readCon = readCon;
		this.ctx = ctx;
	}

	/**
	 * Loads matching
	 * <code>com.openexchange.groupware.container.FolderObject</code> instance
	 * either from cache (if enabled) or from underlying storage.
	 * 
	 * @param folderId -
	 *            the folder ID
	 * @return matching
	 *         <code>com.openexchange.groupware.container.FolderObject</code>
	 *         instance
	 * @throws OXException
	 */
	public final FolderObject getFolderObject(final int folderId) throws OXException {
		final FolderObject fo;
		if (FolderCacheManager.isEnabled()) {
			try {
				fo = FolderCacheManager.getInstance().getFolderObject(folderId, true, ctx, readCon);
			} catch (final OXCachingException e) {
				throw new OXException(e);
			}
		} else {
			fo = FolderObject.loadFolderObjectFromDB(folderId, ctx, readCon);
		}
		return fo;
	}

	/**
	 * Creates a <code>java.util.List</code> of <code>FolderObject</code>
	 * instances filles which match given folder IDs
	 * 
	 * @param folderIDs -
	 *            the folder IDs as an <code>int</code> array
	 * @return a <code>java.util.List</code> of <code>FolderObject</code>
	 *         instances
	 * @throws OXException
	 */
	public final List<FolderObject> getFolderObjects(final int[] folderIDs) throws OXException {
		final List<FolderObject> retval = new ArrayList<FolderObject>(folderIDs.length);
		for (int fuid : folderIDs) {
			try {
				retval.add(getFolderObject(fuid));
			} catch (OXFolderNotFoundException e) {
				continue;
			}
		}
		return retval;
	}

	/**
	 * Creates a <code>java.util.List</code> of <code>FolderObject</code>
	 * instances filles which match given folder IDs
	 * 
	 * @param folderIDs -
	 *            the folder IDs backed by a <code>java.util.Collection</code>
	 * @return a <code>java.util.List</code> of <code>FolderObject</code>
	 *         instances
	 * @throws OXException
	 */
	public final List<FolderObject> getFolderObjects(final Collection<Integer> folderIDs) throws OXException {
		final int size = folderIDs.size();
		final List<FolderObject> retval = new ArrayList<FolderObject>(size);
		final Iterator<Integer> iter = folderIDs.iterator();
		for (int i = 0; i < size; i++) {
			try {
				retval.add(getFolderObject(iter.next().intValue()));
			} catch (OXFolderNotFoundException e) {
				continue;
			}
		}
		return retval;
	}

	/**
	 * Determines folder type. The returned value is either
	 * <code>FolderObject.PRIVATE</code>, <code>FolderObject.PUBLIC</code>
	 * or <code>FolderObject.SHARED</code>. <b>NOTE:</b> This method assumes
	 * that given user has read access!
	 * 
	 * @param folderId -
	 *            the folder ID
	 * @param userId -
	 *            the user ID
	 * @return the folder type
	 * @throws OXException
	 */
	public final int getFolderType(final int folderId, final int userId) throws OXException {
		return getFolderObject(folderId).getType(userId);
	}

	/**
	 * Determines the <b>plain</b> folder type meaning the returned value is
	 * either <code>FolderObject.PRIVATE</code> or
	 * <code>FolderObject.PUBLIC</code>. <b>NOTE:</b> Do not use this method
	 * to check if folder is shared (<code>FolderObject.SHARED</code>), use
	 * {@link #getFolderType(int, int)} instead.
	 * 
	 * @param folderId -
	 *            the folder ID
	 * @return the folder type
	 * @throws OXException
	 * @see <code>getFolderType(int, int)</code>
	 */
	public final int getFolderType(final int folderId) throws OXException {
		return getFolderObject(folderId).getType();
	}

	/**
	 * Determines folder module
	 * 
	 * @param folderId -
	 *            the folder ID
	 * @return folder module
	 * @throws OXException
	 */
	public final int getFolderModule(final int folderId) throws OXException {
		return getFolderObject(folderId).getModule();
	}

	/**
	 * Determines folder owner
	 * 
	 * @param folderId -
	 *            the folder ID
	 * @return folder owner
	 * @throws OXException
	 */
	public final int getFolderOwner(final int folderId) throws OXException {
		return getFolderObject(folderId).getCreatedBy();
	}

	/**
	 * Determines if folder is shared. <b>NOTE:</b> This method assumes that
	 * given user has read access!
	 * 
	 * @param folderId -
	 *            the folder ID
	 * @param userId -
	 *            the user ID
	 * @return <code>true</code> if folder is shared, otherwise
	 *         <code>false</code>
	 * @throws OXException
	 */
	public final boolean isFolderShared(final int folderId, final int userId) throws OXException {
		return (getFolderType(folderId, userId) == FolderObject.SHARED);
	}

	/**
	 * Determines if folder is an user's default folder
	 * 
	 * @param folderId -
	 *            the folder ID
	 * @return <code>true</code> if folder is marked as a default folder,
	 *         otherwise <code>false</code>
	 * @throws OXException
	 */
	public final boolean isDefaultFolder(final int folderId) throws OXException {
		return getFolderObject(folderId).isDefaultFolder();
	}

	/**
	 * Determines given folder's name
	 * 
	 * @param folderId -
	 *            the folder ID
	 * @return folder name
	 * @throws OXException
	 */
	public String getFolderName(final int folderId) throws OXException {
		return getFolderObject(folderId).getFolderName();
	}

	/**
	 * Determines given folder's parent ID
	 * 
	 * @param folderId -
	 *            the folder ID
	 * @return folder parent ID
	 * @throws OXException
	 */
	public int getParentFolderID(final int folderId) throws OXException {
		return getFolderObject(folderId).getParentFolderID();
	}

	/**
	 * Determines given folder's last modifies date
	 * 
	 * @param folderId
	 * @return folder's last modifies date
	 * @throws OXException
	 */
	public Date getFolderLastModified(final int folderId) throws OXException {
		return getFolderObject(folderId).getLastModified();
	}

	/**
	 * Determines user's effective permission on the folder matching given
	 * folder ID.
	 * 
	 * @param folderId -
	 *            the folder ID
	 * @param userId -
	 *            the user ID
	 * @param userConfig -
	 *            the user configuration
	 * @return user's effective permission
	 * @throws OXException
	 */
	public final EffectivePermission getFolderPermission(final int folderId, final int userId,
			final UserConfiguration userConfig) throws OXException {
		try {
			final FolderObject fo = getFolderObject(folderId);
			return fo.getEffectiveUserPermission(userId, userConfig, readCon);
		} catch (SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
		} catch (DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
		}
	}

	/**
	 * Determines user's default folder of given module
	 * 
	 * @param userId -
	 *            the user ID
	 * @param module -
	 *            the module
	 * @return user's default folder of given module
	 * @throws OXException
	 */
	public final FolderObject getDefaultFolder(final int userId, final int module) throws OXException {
		try {
			final int folderId = OXFolderSQL.getUserDefaultFolder(userId, module, readCon, ctx);
			if (folderId == -1) {
				throw new OXFolderException(FolderCode.NO_DEFAULT_FOLDER_FOUND, folderModule2String(module),
						getUserName(userId, ctx), Integer.valueOf(ctx.getContextId()));
			}
			return getFolderObject(folderId);
		} catch (SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
		} catch (DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
		}
	}

	/**
	 * Determines if session's user is allowed to delete all objects located in
	 * given folder.
	 * 
	 * @param fo -
	 *            the folder object
	 * @param session -
	 *            current user session
	 * @param ctx -
	 *            the context
	 * @return
	 * @throws OXException
	 */
	public final boolean canDeleteAllObjectsInFolder(final FolderObject fo, final Session session, final Context ctx)
			throws OXException {
		final int userId = session.getUserId();
		final UserConfiguration userConfig = UserConfigurationStorage.getInstance().getUserConfigurationSafe(
				session.getUserId(), ctx);
		try {
			/*
			 * Check user permission on folder
			 */
			final OCLPermission oclPerm = fo.getEffectiveUserPermission(userId, userConfig, readCon);
			if (!oclPerm.isFolderVisible()) {
				/*
				 * Folder is not visible to user
				 */
				return false;
			} else if (oclPerm.canDeleteAllObjects()) {
				/*
				 * Can delete all objects
				 */
				return true;
			} else if (oclPerm.canDeleteOwnObjects()) {
				// TODO: Additional parameter for readable connection
				/*
				 * User may only delete own objects. Check if folder contains
				 * foreign objects which must not be deleted.
				 */
				switch (fo.getModule()) {
				case FolderObject.TASK:
					final Tasks tasks = Tasks.getInstance();
					return !tasks.containsNotSelfCreatedTasks(session, fo.getObjectID());
				case FolderObject.CALENDAR:
					final CalendarSql calSql = new CalendarSql(session);
					return !calSql.checkIfFolderContainsForeignObjects(userId, fo.getObjectID());
				case FolderObject.CONTACT:
					return !Contacts.containsForeignObjectInFolder(fo.getObjectID(), userId, session);
				case FolderObject.PROJECT:
					// TODO:
					break;
				case FolderObject.INFOSTORE:
					final InfostoreFacade db = new InfostoreFacadeImpl(new DBPoolProvider());
					return !db.hasFolderForeignObjects(fo.getObjectID(), ctx, UserStorage.getStorageUser(session
							.getUserId(), ctx), userConfig);
				default:
					throw new OXFolderException(FolderCode.UNKNOWN_MODULE, folderModule2String(fo.getModule()), Integer
							.valueOf(ctx.getContextId()));
				}
			} else {
				/*
				 * No delete permission: Return true if folder is empty
				 */
				switch (fo.getModule()) {
				case FolderObject.TASK:
					final Tasks tasks = Tasks.getInstance();
					return tasks.isFolderEmpty(ctx, fo.getObjectID());
				case FolderObject.CALENDAR:
					final CalendarSql calSql = new CalendarSql(session);
					return calSql.isFolderEmpty(userId, fo.getObjectID());
				case FolderObject.CONTACT:
					return !Contacts.containsAnyObjectInFolder(fo.getObjectID(), ctx);
				case FolderObject.PROJECT:
					break;
				case FolderObject.INFOSTORE:
					final InfostoreFacade db = new InfostoreFacadeImpl(new DBPoolProvider());
					return db.isFolderEmpty(fo.getObjectID(), ctx);
				default:
					throw new OXFolderException(FolderCode.UNKNOWN_MODULE, folderModule2String(fo.getModule()), Integer
							.valueOf(ctx.getContextId()));
				}
			}
			return false;
		} catch (SQLException e) {
			throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
		} catch (DBPoolingException e) {
			throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
		} catch (Throwable t) {
			throw new OXFolderException(FolderCode.RUNTIME_ERROR, t, Integer.valueOf(ctx.getContextId()));
		}
	}

}
