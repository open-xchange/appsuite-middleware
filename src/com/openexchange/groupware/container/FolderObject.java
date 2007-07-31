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

package com.openexchange.groupware.container;

import static com.openexchange.tools.sql.DBUtils.closeResources;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.StringHelper;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.EffectivePermission;
import com.openexchange.server.OCLPermission;
import com.openexchange.tools.OXCloneable;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.tools.oxfolder.OXFolderNotFoundException;
import com.openexchange.tools.oxfolder.OXFolderSQL;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;

/**
 * FolderObject
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.bettens@open-xchange.org">Thorben Betten</a>
 */
public class FolderObject extends FolderChildObject implements Cloneable, Serializable {

	private static final long serialVersionUID = 1019652520335292041L;

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(FolderObject.class);

	public static String getFolderString(final int id, final Locale locale) {
		final StringHelper strHelper = new StringHelper(locale);
		switch (id) {
		case SYSTEM_PRIVATE_FOLDER_ID:
			return strHelper.getString(FolderStrings.SYSTEM_PRIVATE_FOLDER_NAME);
		case SYSTEM_PUBLIC_FOLDER_ID:
			return strHelper.getString(FolderStrings.SYSTEM_PUBLIC_FOLDER_NAME);
		case SYSTEM_SHARED_FOLDER_ID:
			return strHelper.getString(FolderStrings.SYSTEM_SHARED_FOLDER_NAME);
		case SYSTEM_FOLDER_ID:
			return strHelper.getString(FolderStrings.SYSTEM_FOLDER_NAME);
		case SYSTEM_GLOBAL_FOLDER_ID:
			return strHelper.getString(FolderStrings.SYSTEM_GLOBAL_FOLDER_NAME);
		case SYSTEM_LDAP_FOLDER_ID:
			return strHelper.getString(FolderStrings.SYSTEM_LDAP_FOLDER_NAME);
		case SYSTEM_OX_FOLDER_ID:
			return strHelper.getString(FolderStrings.SYSTEM_OX_FOLDER_NAME);
		case SYSTEM_OX_PROJECT_FOLDER_ID:
			return strHelper.getString(FolderStrings.SYSTEM_OX_PROJECT_FOLDER_NAME);
		case SYSTEM_INFOSTORE_FOLDER_ID:
			return strHelper.getString(FolderStrings.SYSTEM_INFOSTORE_FOLDER_NAME);
		case VIRTUAL_USER_INFOSTORE_FOLDER_ID:
			return strHelper.getString(FolderStrings.VIRTUAL_USER_INFOSTORE_FOLDER_NAME);
		case VIRTUAL_LIST_TASK_FOLDER_ID:
			return strHelper.getString(FolderStrings.VIRTUAL_LIST_TASK_FOLDER_NAME);
		case VIRTUAL_LIST_CALENDAR_FOLDER_ID:
			return strHelper.getString(FolderStrings.VIRTUAL_LIST_CALENDAR_FOLDER_NAME);
		case VIRTUAL_LIST_CONTACT_FOLDER_ID:
			return strHelper.getString(FolderStrings.VIRTUAL_LIST_CONTACT_FOLDER_NAME);
		case VIRTUAL_LIST_INFOSTORE_FOLDER_ID:
			return strHelper.getString(FolderStrings.VIRTUAL_LIST_INFOSTORE_FOLDER_NAME);
		default:
			return null;
		}
	}

	// Constants for system folders per context
	public static final int SYSTEM_ROOT_FOLDER_ID = 0;

	public static final int SYSTEM_PRIVATE_FOLDER_ID = 1;

	public static final int SYSTEM_PUBLIC_FOLDER_ID = 2;

	public static final int SYSTEM_SHARED_FOLDER_ID = 3;

	public static final int SYSTEM_FOLDER_ID = 4;

	public static final int SYSTEM_GLOBAL_FOLDER_ID = 5;

	public static final int SYSTEM_LDAP_FOLDER_ID = 6;

	public static final int SYSTEM_OX_FOLDER_ID = 7;

	public static final int SYSTEM_OX_PROJECT_FOLDER_ID = 8;

	public static final int SYSTEM_INFOSTORE_FOLDER_ID = 9;

	public static final int VIRTUAL_USER_INFOSTORE_FOLDER_ID = 10;

	public static final int VIRTUAL_LIST_TASK_FOLDER_ID = 11;

	public static final int VIRTUAL_LIST_CALENDAR_FOLDER_ID = 12;

	public static final int VIRTUAL_LIST_CONTACT_FOLDER_ID = 13;

	public static final int VIRTUAL_LIST_INFOSTORE_FOLDER_ID = 14;

	public static final int MIN_FOLDER_ID = 20;

	// Constant identifier for system folders
	public static final String SYSTEM_PRIVATE_FOLDER_NAME = "private";

	public static final String SYSTEM_PUBLIC_FOLDER_NAME = "public";

	public static final String SYSTEM_SHARED_FOLDER_NAME = "shared";

	public static final String SYSTEM_FOLDER_NAME = "system";

	public static final String SYSTEM_GLOBAL_FOLDER_NAME = "system_global";

	public static final String SYSTEM_LDAP_FOLDER_NAME = "system_ldap";

	public static final String SYSTEM_OX_FOLDER_NAME = "user";

	public static final String SYSTEM_OX_PROJECT_FOLDER_NAME = "projects";

	public static final String SYSTEM_INFOSTORE_FOLDER_NAME = "infostore";
	
	/**
	 * The UID prefix of a virtual shared folder
	 */
	public static final String SHARED_PREFIX = "u:";

	// Constants for folder fields
	public static final int FOLDER_NAME = 300;

	public static final int MODULE = 301;

	public static final int TYPE = 302;

	public static final int SUBFOLDERS = 304;

	public static final int OWN_RIGHTS = 305;

	public static final int PERMISSIONS_BITS = 306;

	public static final int SUMMARY = 307;

	public static final int STANDARD_FOLDER = 308;

	public static final int TOTAL = 309;

	public static final int NEW = 310;

	public static final int UNREAD = 311;

	public static final int DELETED = 312;

	public static final int CAPABILITIES = 313;
	
	public static final int SUBSCRIBED = 314;

	// Modules
	public static final int TASK = 1;

	public static final int CALENDAR = 2;

	public static final int CONTACT = 3;

	public static final int UNBOUND = 4;

	public static final int SYSTEM_MODULE = 5;

	public static final int PROJECT = 6;

	public static final int MAIL = 7;

	public static final int INFOSTORE = 8;

	// Types
	public static final int PRIVATE = 1;

	public static final int PUBLIC = 2;

	public static final int SHARED = 3;

	public static final int SYSTEM_TYPE = SYSTEM_MODULE; // Formerly 6;

	// SQL string for standard modules
	public static final String SQL_IN_STR_STANDARD_MODULES = new StringBuilder().append('(').append(TASK).append(',')
			.append(CALENDAR).append(',').append(CONTACT).append(',').append(UNBOUND).append(',').append(INFOSTORE)
			.append(')').toString();

	// SQL string for standard modules including system module
	public static final String SQL_IN_STR_STANDARD_MODULES_ALL = new StringBuilder().append('(').append(TASK).append(
			',').append(CALENDAR).append(',').append(CONTACT).append(',').append(UNBOUND).append(',').append(
			SYSTEM_MODULE).append(',').append(INFOSTORE).append(')').toString();

	// Permissions
	public static final int PRIVATE_PERMISSION = 1;

	public static final int PUBLIC_PERMISSION = 2;

	public static final int CUSTOM_PERMISSION = 3;

	// Variables
	protected String folderName;

	protected boolean b_folderName;

	protected int module;

	protected boolean b_module;

	protected int type;

	protected boolean b_type;

	protected boolean defaultFolder;

	protected boolean b_defaultFolder;

	protected int permissionFlag;

	protected boolean b_permissionFlag;

	protected ArrayList<OCLPermission> permissions = new ArrayList<OCLPermission>();

	protected boolean b_permissions;

	protected boolean subfolderFlag;

	protected boolean b_subfolderFlag;

	protected ArrayList<Integer> subfolderIds = new ArrayList<Integer>();

	protected boolean b_subfolderIds;

	protected String fullName;

	protected boolean b_fullName;

	public FolderObject() {
		super();
	}

	public FolderObject(final int objectId) {
		setObjectID(objectId);
	}

	public FolderObject(final String folderName, final int objectId, final int module, final int type, final int creator) {
		this.folderName = folderName;
		b_folderName = true;
		this.module = module;
		b_module = true;
		this.type = type;
		b_type = true;
		setObjectID(objectId);
		setCreatedBy(creator);
	}

	public boolean isDefaultFolder() {
		return defaultFolder;
	}

	public boolean containsDefaultFolder() {
		return b_defaultFolder;
	}

	public void setDefaultFolder(final boolean defaultFolder) {
		this.defaultFolder = defaultFolder;
		b_defaultFolder = true;
	}

	public void removeDefaultFolder() {
		this.defaultFolder = false;
		b_defaultFolder = false;
	}

	public String getFolderName() {
		return folderName;
	}

	public boolean containsFolderName() {
		return b_folderName;
	}

	public void setFolderName(final String folderName) {
		this.folderName = folderName;
		this.b_folderName = true;
	}

	public void removeFolderName() {
		this.folderName = null;
		this.b_folderName = false;
	}

	public int getModule() {
		return module;
	}

	public boolean containsModule() {
		return b_module;
	}

	public void setModule(final int module) {
		this.module = module;
		b_module = true;
	}

	public void removeModule() {
		this.module = 0;
		b_module = false;
	}

	public int getPermissionFlag() {
		return permissionFlag;
	}

	public boolean containsPermissionFlag() {
		return b_permissionFlag;
	}

	public void setPermissionFlag(final int permissionFlag) {
		this.permissionFlag = permissionFlag;
		b_permissionFlag = true;
	}

	public void removePermissionFlag() {
		this.permissionFlag = 0;
		b_permissionFlag = false;
	}

	public List<OCLPermission> getPermissions() {
		return permissions;
	}

	public OCLPermission[] getPermissionsAsArray() {
		final OCLPermission[] perms = new OCLPermission[permissions.size()];
		System.arraycopy(permissions.toArray(), 0, perms, 0, perms.length);
		return perms;
	}

	public boolean containsPermissions() {
		return b_permissions;
	}

	@SuppressWarnings("unchecked")
	public void setPermissions(final ArrayList permissions) {
		this.permissions = (ArrayList<OCLPermission>) permissions.clone();
		b_permissions = true;
	}

	public void setPermissionsAsArray(final OCLPermission[] permissions) {
		final List<OCLPermission> tmpList = Arrays.asList(permissions);
		if (this.permissions != null) {
			this.permissions.clear();
		} else {
			this.permissions = new ArrayList<OCLPermission>();
		}
		this.permissions.addAll(tmpList);
		b_permissions = true;
	}

	public void removePermissions() {
		this.permissions = null;
		b_permissions = false;
	}

	/**
	 * 
	 * @return the folder's type which is either
	 *         <code>FolderObject.PUBLIC</code> or
	 *         <code>FolderObject.PRIVATE</code>.
	 *         <p>
	 *         NOTE: To check if this folder is shared call the
	 *         <code>isShared(int userId)</code>
	 */
	public int getType() {
		return type;
	}

	/**
	 * 
	 * @param userId
	 * @return the folder's type which is <code>FolderObject.PUBLIC</code>,
	 *         <code>FolderObject.PRIVATE</code> or
	 *         <code>FolderObject.SHARED</code>.
	 */
	public int getType(final int userId) {
		return isShared(userId) ? SHARED : type;
	}

	public boolean containsType() {
		return b_type;
	}

	public void setType(final int type) {
		this.type = type;
		b_type = true;
	}

	public void removeType() {
		this.type = 0;
		b_type = false;
	}

	public int getCreator() {
		return createdBy;
	}

	public boolean containsCreator() {
		return b_created_by;
	}

	public void setCreator(final int creator) {
		this.createdBy = creator;
		this.b_created_by = true;
	}

	public void removeCreator() {
		this.createdBy = 0;
		this.b_created_by = false;
	}

	public boolean hasSubfolders() {
		return subfolderFlag;
	}

	/**
	 * Returns <code>true</code> if this folder contains user-visible
	 * subfolders, <code>false</code> otherwise
	 */
	public final boolean hasVisibleSubfolders(final User userObj, final UserConfiguration userConfig, final Context ctx)
			throws DBPoolingException, OXException, SQLException, SearchIteratorException {
		return hasVisibleSubfolders(userObj.getId(), userObj.getGroups(), userConfig, ctx);
	}

	/**
	 * Returns <code>true</code> if this folder contains user-visible
	 * subfolders, <code>false</code> otherwise
	 */
	public final boolean hasVisibleSubfolders(final int userId, final int[] groups, final UserConfiguration userConfig,
			final Context ctx) throws DBPoolingException, OXException, SQLException, SearchIteratorException {
		SearchIterator iter = null;
		try {
			if (objectId == SYSTEM_ROOT_FOLDER_ID) {
				return true;
			} else if (objectId == SYSTEM_PUBLIC_FOLDER_ID) {
				/*
				 * At least 'internal users' folder is located beneath system
				 * public folder if user has contact module access
				 */
				if (userConfig.hasContact()) {
					return true;
				}
				/*
				 * Search for visible subfolders
				 */
				final int[] modules = { TASK, CALENDAR, CONTACT };
				return (iter = OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfType(userId, groups, userConfig
						.getAccessibleModules(), FolderObject.PUBLIC, modules, SYSTEM_PUBLIC_FOLDER_ID, ctx)).hasNext();
			} else if (objectId == SYSTEM_INFOSTORE_FOLDER_ID) {
				return (iter = OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfType(userId, groups, userConfig
						.getAccessibleModules(), FolderObject.PUBLIC, new int[] { INFOSTORE },
						SYSTEM_INFOSTORE_FOLDER_ID, ctx)).hasNext();
			} else if (!subfolderFlag) {
				/*
				 * Folder has no subfolder(s)
				 */
				return false;
			} else if (objectId == VIRTUAL_USER_INFOSTORE_FOLDER_ID || objectId == SYSTEM_PRIVATE_FOLDER_ID
					|| objectId == VIRTUAL_LIST_CALENDAR_FOLDER_ID || objectId == VIRTUAL_LIST_CONTACT_FOLDER_ID
					|| objectId == VIRTUAL_LIST_TASK_FOLDER_ID || objectId == VIRTUAL_LIST_INFOSTORE_FOLDER_ID) {
				return subfolderFlag;
			}
			return (iter = OXFolderIteratorSQL.getVisibleSubfoldersIterator(objectId, userId, groups, ctx, userConfig, null))
					.hasNext();
		} finally {
			if (iter != null) {
				iter.close();
			}
		}
	}

	public boolean containsSubfolderFlag() {
		return b_subfolderFlag;
	}

	public void setSubfolderFlag(final boolean subfolderFlag) {
		this.subfolderFlag = subfolderFlag;
		b_subfolderFlag = true;
	}

	public void removeSubfolderFlag() {
		this.subfolderFlag = false;
		b_subfolderFlag = false;
	}

	/**
	 * Returns a <code>java.util.List</code> containing all user-visible
	 * subfolders
	 */
	public final List<FolderObject> getVisibleSubfolders(final User userObj, final UserConfiguration userConfig,
			final Context ctx) throws DBPoolingException, OXException, SQLException, SearchIteratorException {
		return getVisibleSubfolders(userObj.getId(), userObj.getGroups(), userConfig, ctx);
	}

	/**
	 * Returns a <code>java.util.List</code> containing all user-visible
	 * subfolders
	 */
	public final List<FolderObject> getVisibleSubfolders(final int userId, final int[] groups,
			final UserConfiguration userConfig, final Context ctx) throws DBPoolingException, OXException,
			SQLException, SearchIteratorException {
		if (objectId == VIRTUAL_USER_INFOSTORE_FOLDER_ID) {
			throw new OXFolderException(FolderCode.UNSUPPORTED_OPERATION, String.valueOf(objectId), String.valueOf(ctx.getContextId()));
		} else if (b_subfolderFlag && !subfolderFlag) {
			return new ArrayList<FolderObject>(0);
		}
		final List<FolderObject> retval;
		SearchIterator iter = null;
		try {
			if (objectId == VIRTUAL_LIST_TASK_FOLDER_ID) {
				iter = OXFolderIteratorSQL.getVisibleFoldersNotSeenInTreeView(userId, groups, FolderObject.TASK, userConfig,
						ctx);
			} else if (objectId == VIRTUAL_LIST_CALENDAR_FOLDER_ID) {
				iter = OXFolderIteratorSQL.getVisibleFoldersNotSeenInTreeView(userId, groups, FolderObject.CALENDAR,
						userConfig, ctx);
			} else if (objectId == VIRTUAL_LIST_CONTACT_FOLDER_ID) {
				iter = OXFolderIteratorSQL.getVisibleFoldersNotSeenInTreeView(userId, groups, FolderObject.CONTACT,
						userConfig, ctx);
			} else if (objectId == VIRTUAL_LIST_INFOSTORE_FOLDER_ID) {
				iter = OXFolderIteratorSQL.getVisibleFoldersNotSeenInTreeView(userId, groups, FolderObject.INFOSTORE,
						userConfig, ctx);
			} else {
				iter = OXFolderIteratorSQL.getVisibleSubfoldersIterator(objectId, userId, groups, ctx, userConfig, null);
			}
			if (iter.hasSize()) {
				final int size = iter.size();
				retval = new ArrayList<FolderObject>(size);
				for (int i = 0; i < size; i++) {
					retval.add((FolderObject) iter.next());
				}
			} else {
				retval = new ArrayList<FolderObject>();
				while (iter.hasNext()) {
					retval.add((FolderObject) iter.next());
				}
			}
			return retval;
		} finally {
			if (iter != null) {
				iter.close();
				iter = null;
			}
		}
	}

	public final List<Integer> getSubfolderIds() throws OXFolderException {
		if (!b_subfolderIds) {
			throw new OXFolderException(FolderCode.ATTRIBUTE_NOT_SET, "subfolderIds", String.valueOf(getObjectID()), "");
		}
		return subfolderIds;
	}

	/**
	 * Returns a list of subfolder IDs. If <code>enforce</code> is set and
	 * list has not been already loaded, their IDs are going to be loaded from
	 * storage. Otherwise a exception is thrown that no subfolder IDs are
	 * present in this folder object.
	 */
	public final List<Integer> getSubfolderIds(final boolean enforce, final Context ctx) throws DBPoolingException,
			SQLException, OXException {
		return getSubfolderIds(enforce, null, ctx);
	}

	public final List<Integer> getSubfolderIds(final boolean enforce, final Connection readCon, final Context ctx)
			throws DBPoolingException, SQLException, OXException {
		if (!b_subfolderIds) {
			/*
			 * Subfolder list not set, yet
			 */
			if (b_subfolderFlag && !subfolderFlag) {
				/*
				 * Flag indicates no present subfolders
				 */
				return new ArrayList<Integer>(0);
			}
			if (!enforce) {
				throw new OXFolderException(FolderCode.ATTRIBUTE_NOT_SET, "subfolderIds", String.valueOf(getObjectID()), "");
			}
			subfolderIds = getSubfolderIds(objectId, ctx, readCon);
			b_subfolderIds = true;
		}
		return subfolderIds;
	}

	public boolean containsSubfolderIds() {
		return b_subfolderIds;
	}

	@SuppressWarnings("unchecked")
	public void setSubfolderIds(final ArrayList<Integer> subfolderIds) {
		this.subfolderIds = (ArrayList) subfolderIds.clone();
		b_subfolderIds = true;
	}

	public void removeSubfolderIds() {
		this.subfolderIds = null;
		b_subfolderIds = false;
	}

	public String getFullName() {
		return fullName;
	}

	public boolean containsFullName() {
		return b_fullName;
	}

	public void setFullName(final String fullName) {
		this.fullName = fullName;
		b_fullName = true;
	}

	public void removeFullName() {
		fullName = null;
		b_fullName = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.container.DataObject#reset()
	 */
	@Override
	public final void reset() {
		super.reset();
		removeCreator();
		removeType();
		removePermissions();
		removePermissionFlag();
		removeModule();
		removeFolderName();
		removeSubfolderFlag();
		removeSubfolderIds();
	}

	/**
	 * Fills this folder with all availbable values from given folder
	 * and returns itself.
	 * 
	 * @return filled folder
	 */
	public final FolderObject fill(final FolderObject other) {
		reset();
		if (other.containsObjectID()) {
			setObjectID(other.getObjectID());
		}
		if (other.containsCreatedBy()) {
			setCreatedBy(other.getCreatedBy());
		}
		if (other.containsCreationDate()) {
			setCreationDate(other.getCreationDate());
		}
		if (other.containsDefaultFolder()) {
			setDefaultFolder(other.isDefaultFolder());
		}
		if (other.containsFolderName()) {
			setFolderName(other.getFolderName());
		}
		if (other.containsFullName()) {
			setFullName(other.getFullName());
		}
		if (other.containsLastModified()) {
			setLastModified(other.getLastModified());
		}
		if (other.containsModifiedBy()) {
			setModifiedBy(other.getModifiedBy());
		}
		if (other.containsModule()) {
			setModule(other.getModule());
		}
		if (other.containsParentFolderID()) {
			setParentFolderID(other.getParentFolderID());
		}
		if (other.containsPermissionFlag()) {
			setPermissionFlag(other.getPermissionFlag());
		}
		if (other.containsPermissions()) {
			setPermissions((ArrayList) other.getPermissions());
		}
		if (other.containsSubfolderFlag()) {
			setSubfolderFlag(other.hasSubfolders());
		}
		if (other.containsSubfolderIds()) {
			try {
				setSubfolderIds((ArrayList<Integer>) other.getSubfolderIds());
			} catch (final OXFolderException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		if (other.containsType()) {
			setType(other.getType());
		}
		return this;
	}

	/**
	 * Checks if this folder exists in underlying storage by checking its object
	 * ID or (if object ID is not present) by its folder name, parent and module. An
	 * <code>OXException</code> is thrown if folder does not hold sufficient
	 * information to verify existence.
	 * 
	 * @return <code>true</code> if a corresponding folder can be detected,
	 *         otherwise <code>false</code>
	 */
	public final boolean exists(final Context ctx) throws OXException {
		if (containsObjectID()) {
			try {
				return OXFolderSQL.exists(getObjectID(), null, ctx);
			} catch (final DBPoolingException e) {
				throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
			} catch (final SQLException e) {
				throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
			}
		} else if (containsParentFolderID() && containsFolderName() && containsModule()) {
			try {
				final int fuid = OXFolderSQL.lookUpFolder(getParentFolderID(), getFolderName(), getModule(), null, ctx);
				if (fuid == -1) {
					return false;
				}
				this.setObjectID(fuid);
				return true;
			} catch (final DBPoolingException e) {
				throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(ctx.getContextId()));
			} catch (final SQLException e) {
				throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
			}
		}
		throw new OXFolderException(FolderCode.UNSUFFICIENT_FOLDER_INFORMATION);
	}

	/**
	 * <b>NOTE:</b> This method does not check user's permissions on this
	 * folder, but only checks if this folder is of type <code>PRIVATE</code>
	 * and user is not folder's creator
	 * 
	 * @return <code>true</code> if this folder is of type PRIVATE and user is
	 *         not folder's creator, <code>false</code> otherwise
	 */
	public final boolean isShared(final int userId) {
		return (type == PRIVATE && createdBy != userId);
	}

	/**
	 * @return <code>true</code> if given user has READ access to this folder,
	 *         <code>false</code> otherwise
	 */
	public final boolean isVisible(final int userId, final UserConfiguration userConfig) throws DBPoolingException,
			SQLException {
		return (getEffectiveUserPermission(userId, userConfig).isFolderVisible());
	}

	/**
	 * This methods yields the effective OCL permission for the currently logged
	 * in user by determining the max. OCL permission which the user has on
	 * folder and applying the user configuration profile.
	 */
	public final EffectivePermission getEffectiveUserPermission(final int userId, final UserConfiguration userConfig)
			throws SQLException, DBPoolingException {
		return getEffectiveUserPermission(userId, userConfig, null);
	}

	/**
	 * This methods yields the effective OCL permission for the currently logged
	 * in user by determining the max. OCL permission which the user has on
	 * folder and applying the user configuration profile.
	 */
	public final EffectivePermission getEffectiveUserPermission(final int userId, final UserConfiguration userConfig,
			final Connection readConArg) throws SQLException, DBPoolingException {
		final EffectivePermission maxPerm = new EffectivePermission(userId, getObjectID(), getType(userId),
				getModule(), userConfig);
		maxPerm.setAllPermission(OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS,
				OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
		final int[] groups = userConfig.getGroups();
		final int[] idArr = new int[groups.length + 1];
		idArr[0] = userId;
		System.arraycopy(groups, 0, idArr, 1, groups.length);
		Arrays.sort(idArr);
		if (!containsPermissions()) {
			setPermissionsAsArray(FolderObject.getFolderPermissions(getObjectID(), userConfig.getContext(), readConArg));
		}
		final int permissionsSize = getPermissions().size();
		final Iterator<OCLPermission> iter = getPermissions().iterator();
		NextPerm: for (int i = 0; i < permissionsSize; i++) {
			final OCLPermission oclPerm = iter.next();
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

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(super.toString()).append('\n');
		if (containsObjectID()) {
			sb.append(" ObjectID=").append(getObjectID());
		}
		if (containsParentFolderID()) {
			sb.append(" Parent Folder ID=").append(getParentFolderID());
		}
		if (containsFolderName()) {
			sb.append(" Folder Name=").append(getFolderName());
		}
		if (containsType()) {
			sb.append(" Type=").append(getType());
		}
		if (containsModule()) {
			sb.append(" Module=").append(getModule());
		}
		if (containsCreatedBy()) {
			sb.append(" Created By=").append(getCreatedBy());
		}
		if (containsCreationDate()) {
			sb.append(" Creation Date=").append(getCreationDate());
		}
		if (containsModifiedBy()) {
			sb.append(" Modified By=").append(getModifiedBy());
		}
		if (containsLastModified()) {
			sb.append(" Last Modified=").append(getLastModified());
		}
		if (containsDefaultFolder()) {
			sb.append(" Default Folder=").append(isDefaultFolder());
		}
		if (containsSubfolderFlag()) {
			sb.append(" Has Subfolders=").append(hasSubfolders());
		}
		if (containsPermissions()) {
			sb.append(" permissions=");
			final int size = getPermissions().size();
			final Iterator<OCLPermission> iter = getPermissions().iterator();
			for (int i = 0; i < size; i++) {
				sb.append(iter.next().toString());
				if (i < size - 1) {
					sb.append('|');
				}
			}
		}
		if (containsSubfolderIds()) {
			try {
				sb.append(" subfolder IDs=");
				final int size = getSubfolderIds().size();
				final Iterator<Integer> iter = getSubfolderIds().iterator();
				for (int i = 0; i < size; i++) {
					sb.append(iter.next().toString());
					if (i < size - 1) {
						sb.append('|');
					}
				}
			} catch (final OXFolderException e) {
				sb.append("");
			}
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		try {
			final FolderObject clone = (FolderObject) super.clone();
			if (b_created_by) {
				clone.setCreatedBy(this.createdBy);
			}
			if (b_creation_date) {
				clone.setCreationDate(this.creationDate);
			}
			if (b_defaultFolder) {
				clone.setDefaultFolder(this.defaultFolder);
			}
			if (b_object_id) {
				clone.setObjectID(this.objectId);
			}
			if (b_folderName) {
				clone.setFolderName(this.folderName);
			}
			if (b_fullName) {
				clone.setFullName(this.fullName);
			}
			if (b_last_modified) {
				clone.setLastModified(this.lastModified);
			}
			if (b_modified_by) {
				clone.setModifiedBy(this.modifiedBy);
			}
			if (b_module) {
				clone.setModule(this.module);
			}
			if (b_parent_folder_id) {
				clone.setParentFolderID(this.parentFolderId);
			}
			if (b_permissionFlag) {
				clone.setPermissionFlag(this.permissionFlag);
			}
			if (b_subfolderFlag) {
				clone.setSubfolderFlag(this.subfolderFlag);
			}
			if (b_subfolderIds) {
				clone.setSubfolderIds(copyIntArrayList(this.subfolderIds));
			}
			if (b_permissions) {
				clone.setPermissions(copyArrayList(this.permissions));
			}
			if (b_type) {
				clone.setType(this.type);
			}
			return clone;
		} catch (final CloneNotSupportedException exc) {
			return null;
		}
	}

	private static final <T extends OXCloneable<T>> ArrayList<T> copyArrayList(final ArrayList<T> original) {
		final int size = original.size();
		final ArrayList<T> copy = new ArrayList<T>(original.size());
		final Iterator<T> iter = original.iterator();
		for (int i = 0; i < size; i++) {
			copy.add(iter.next().deepClone());
		}
		return copy;
	}

	private static final ArrayList<Integer> copyIntArrayList(final ArrayList<Integer> original) {
		final int size = original.size();
		final ArrayList<Integer> copy = new ArrayList<Integer>(original.size());
		for (int i = 0; i < size; i++) {
			copy.add(Integer.valueOf(original.get(i).intValue()));
		}
		return copy;
	}

	public static FolderObject loadFolderObjectFromDB(final int folderId, final Context ctx) throws OXException {
		return loadFolderObjectFromDB(folderId, ctx, null, true, false);
	}

	public static FolderObject loadFolderObjectFromDB(final int folderId, final Context ctx, final Connection readCon)
			throws OXException {
		return loadFolderObjectFromDB(folderId, ctx, readCon, true, false);
	}

	private static final String TABLE_OT = "oxfolder_tree";

	private static final String TABLE_OP = "oxfolder_permissions";

	public static final FolderObject loadFolderObjectFromDB(final int folderId, final Context ctx,
			final Connection readConArg, final boolean loadPermissions, final boolean loadSubfolderList)
			throws OXException {
		return loadFolderObjectFromDB(folderId, ctx, readConArg, loadPermissions, loadSubfolderList, TABLE_OT, TABLE_OP);
	}

	private static final String SQL_LOAD_F = "SELECT parent, fname, module, type, creating_date, created_from, changing_date, changed_from, permission_flag, subfolder_flag, default_flag FROM #TABLE# WHERE cid = ? AND fuid = ?";

	public static final FolderObject loadFolderObjectFromDB(final int folderId, final Context ctx,
			final Connection readConArg, final boolean loadPermissions, final boolean loadSubfolderList,
			final String table, final String permTable) throws OXException {
		try {
			Connection readCon = readConArg;
			boolean closeCon = false;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				if (readCon == null) {
					readCon = DBPool.pickup(ctx);
					closeCon = true;
				}
				stmt = readCon.prepareStatement(SQL_LOAD_F.replaceFirst("#TABLE#", table));
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, folderId);
				rs = stmt.executeQuery();
				if (!rs.next()) {
					throw new OXFolderNotFoundException(folderId, ctx.getContextId());
				}
				final FolderObject folderObj = new FolderObject(rs.getString(2), folderId, rs.getInt(3), rs.getInt(4),
						rs.getInt(6));
				folderObj.setParentFolderID(rs.getInt(1));
				folderObj.setCreatedBy(parseStringValue(rs.getString(6), ctx));
				folderObj.setCreationDate(new Date(rs.getLong(5)));
				folderObj.setSubfolderFlag(rs.getInt(10) > 0 ? true : false);
				folderObj.setLastModified(new Date(rs.getLong(7)));
				folderObj.setModifiedBy(parseStringValue(rs.getString(8), ctx));
				folderObj.setPermissionFlag(rs.getInt(9));
				int defaultFolder = rs.getInt(11);
				if (rs.wasNull()) {
					defaultFolder = 0;
				}
				folderObj.setDefaultFolder(defaultFolder > 0);
				if (loadSubfolderList) {
					final ArrayList<Integer> subfolderList = getSubfolderIds(folderId, ctx, readCon, table);
					folderObj.setSubfolderIds(subfolderList);
				}

				if (loadPermissions) {
					folderObj.setPermissionsAsArray(getFolderPermissions(folderId, ctx, readCon, permTable));
				}
				return folderObj;
			} finally {
				closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
			}
		} catch (final SQLException e) {
			throw new OXFolderException(FolderCode.FOLDER_COULD_NOT_BE_LOADED, e, String.valueOf(folderId), String.valueOf(ctx.getContextId()));
		} catch (final DBPoolingException e) {
			throw new OXFolderException(FolderCode.FOLDER_COULD_NOT_BE_LOADED, e, String.valueOf(folderId), String.valueOf(ctx.getContextId()));
		}
	}

	private static final int parseStringValue(final String str, final Context ctx) {
		if (null == str) {
			return -1;
		}
		try {
			return Integer.parseInt(str);
		} catch (final NumberFormatException e) {
			if (str.equalsIgnoreCase("system")) {
				return ctx.getMailadmin();
			}
		}
		return -1;
	}

	public static final OCLPermission[] getFolderPermissions(final int folderId, final Context ctx,
			final Connection readConArg) throws SQLException, DBPoolingException {
		return getFolderPermissions(folderId, ctx, readConArg, TABLE_OP);
	}

	private static final String SQL_LOAD_P = "SELECT permission_id, fp, orp, owp, odp, admin_flag, group_flag FROM #TABLE# WHERE cid = ? AND fuid = ?";

	/**
	 * Loads folder permissions from database. Creates a new connection if
	 * <code>null</code> is given.
	 * 
	 * @param folderId
	 * @param ctx
	 * @param readCon -
	 *            may be <code>null</code>
	 * @return
	 * @throws SQLException
	 * @throws DBPoolingException
	 */
	public static final OCLPermission[] getFolderPermissions(final int folderId, final Context ctx,
			final Connection readConArg, final String table) throws SQLException, DBPoolingException {
		Connection readCon = readConArg;
		boolean closeCon = false;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			if (readCon == null) {
				readCon = DBPool.pickup(ctx);
				closeCon = true;
			}
			stmt = readCon.prepareStatement(SQL_LOAD_P.replaceFirst("#TABLE#", table));
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, folderId);
			rs = stmt.executeQuery();
			final ArrayList<OCLPermission> permList = new ArrayList<OCLPermission>();
			while (rs.next()) {
				final int entity = rs.getInt(1);
				final OCLPermission p = new OCLPermission();
				p.setEntity(entity);
				p.setAllPermission(rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5));
				p.setFolderAdmin(rs.getInt(6) > 0 ? true : false);
				p.setGroupPermission(rs.getInt(7) > 0 ? true : false);
				permList.add(p);
			}
			stmt.close();
			return permList.toArray(new OCLPermission[permList.size()]);
		} finally {
			closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
		}
	}

	public static final ArrayList<Integer> getSubfolderIds(final int folderId, final Context ctx,
			final Connection readConArg) throws SQLException, DBPoolingException {
		return getSubfolderIds(folderId, ctx, readConArg, TABLE_OT);
	}

	private static final String SQL_SEL = "SELECT fuid FROM #TABLE# WHERE cid = ? AND parent = ?";

	public static final ArrayList<Integer> getSubfolderIds(final int folderId, final Context ctx,
			final Connection readConArg, final String table) throws SQLException, DBPoolingException {
		Connection readCon = readConArg;
		boolean closeCon = false;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			if (readCon == null) {
				readCon = DBPool.pickup(ctx);
				closeCon = true;
			}
			stmt = readCon.prepareStatement(SQL_SEL.replaceFirst("#TABLE#", table));
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, folderId);
			rs = stmt.executeQuery();
			final ArrayList<Integer> retval = new ArrayList<Integer>();
			while (rs.next()) {
				retval.add(Integer.valueOf(rs.getInt(1)));
			}
			return retval;
		} finally {
			closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
		}
	}

	public static final OCLPermission VIRTUAL_FOLDER_PERMISSION = new OCLPermission();

	static {
		VIRTUAL_FOLDER_PERMISSION.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
		VIRTUAL_FOLDER_PERMISSION.setFolderAdmin(false);
		VIRTUAL_FOLDER_PERMISSION.setGroupPermission(true);
		VIRTUAL_FOLDER_PERMISSION.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.NO_PERMISSIONS,
				OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
	}

	public static final FolderObject createVirtualFolderObject(final int objectID, final String name, final int module,
			final boolean hasSubfolders, final int type) {
		return createVirtualFolderObject(objectID, name, module, hasSubfolders, type, null);
	}

	public static final int mapVirtualID2SystemID(final int virtualID) {
		switch (virtualID) {
		case VIRTUAL_USER_INFOSTORE_FOLDER_ID:
			return SYSTEM_INFOSTORE_FOLDER_ID;
		default:
			return virtualID;
		}
	}

	public static final int mapSystemID2VirtualID(final int systemID) {
		switch (systemID) {
		case SYSTEM_INFOSTORE_FOLDER_ID:
			return VIRTUAL_USER_INFOSTORE_FOLDER_ID;
		default:
			return systemID;
		}
	}

	public static final FolderObject createVirtualFolderObject(final int objectID, final String name, final int module,
			final boolean hasSubfolders, final int type, final OCLPermission virtualPerm) {
		final OCLPermission p = virtualPerm == null ? VIRTUAL_FOLDER_PERMISSION : virtualPerm;
		final FolderObject virtualFolder = new FolderObject(objectID);
		virtualFolder.setFolderName(name);
		virtualFolder.setModule(module);
		virtualFolder.setSubfolderFlag(hasSubfolders);
		virtualFolder.setType(type);
		p.setFuid(objectID);
		virtualFolder.setPermissionsAsArray(new OCLPermission[] { p });
		return virtualFolder;
	}

	public static final FolderObject createVirtualFolderObject(final String fullName, final String name,
			final int module, final boolean hasSubfolders, final int type) {
		final OCLPermission p = VIRTUAL_FOLDER_PERMISSION;
		final FolderObject virtualFolder = new FolderObject();
		virtualFolder.setFullName(fullName);
		virtualFolder.setFolderName(name);
		virtualFolder.setModule(module);
		virtualFolder.setSubfolderFlag(hasSubfolders);
		virtualFolder.setType(type);
		virtualFolder.setPermissionsAsArray(new OCLPermission[] { p });
		return virtualFolder;
	}

	public static final FolderObject createVirtualSharedFolderObject(final int createdBy,
			final String creatorDisplayName) {
		return createVirtualFolderObject(new StringBuilder(20).append(SHARED_PREFIX).append(createdBy).toString(),
				creatorDisplayName, FolderObject.SYSTEM_MODULE, true, FolderObject.SYSTEM_TYPE);
	}

}
