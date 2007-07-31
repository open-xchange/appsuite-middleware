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

package com.openexchange.server;

import java.sql.Connection;
import java.sql.SQLException;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * EffectivePermission
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class EffectivePermission extends OCLPermission {

	private static final char CHAR_AT = '@';

	private static final char CHAR_BREAK = '\n';

	private static final char CHAR_PIPE = '|';

	private static final String STR_EFFECTIVE_PERMISSION = "EffectivePermission_";

	private static final String STR_EMPTY = "";

	private static final String STR_ADMIN = "Admin";

	private static final String STR_USER = "User";

	private static final String STR_GROUP = "Group";

	private static final long serialVersionUID = -1303754404748836561L;

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(EffectivePermission.class);

	/**
	 * The configuration profile of the current logged in user
	 */
	private final UserConfiguration userConfig;

	/**
	 * A reference to the associated folder object. Only needed when a
	 * <code>UserConfiguration</code> instance is present.
	 */
	private int folderType = -1;

	/**
	 * A reference to the associated folder object. Only needed when a
	 * <code>UserConfiguration</code> instance is present.
	 */
	private int folderModule = -1;

	private boolean userConfigIsValid;

	private boolean userConfigAlreadyValidated;
	
	private OCLPermission underlyingPerm;

	public EffectivePermission(int entity, int fuid, int folderType, int folderModule, UserConfiguration userConfig) {
		super(entity, fuid);
		this.folderType = folderType;
		this.folderModule = folderModule;
		this.userConfig = userConfig;
		validateUserConfig();
	}

	public boolean hasModuleAccess(final int folderModule) {
		if (validateUserConfig()) {
			switch (folderModule) {
			case FolderObject.MAIL:
				return userConfig.hasWebMail();
			case FolderObject.CALENDAR:
				return userConfig.hasCalendar();
			case FolderObject.CONTACT:
				return userConfig.hasContact();
			case FolderObject.TASK:
				return userConfig.hasTask();
			case FolderObject.INFOSTORE:
				return userConfig.hasInfostore();
			case FolderObject.PROJECT:
				return userConfig.hasProject();
			case FolderObject.SYSTEM_MODULE:
				return true;
			default:
				return true;
			}
		}
		return true;
	}

	@Override
	public boolean isFolderAdmin() {
		if (validateUserConfig()) {
			if (!hasModuleAccess(folderModule)) {
				return false;
			} else if (folderType == FolderObject.PUBLIC && folderModule != FolderObject.INFOSTORE
					&& !userConfig.hasFullPublicFolderAccess()) {
				return false;
			} /*else if (folderType == FolderObject.SHARED && !userConfig.hasFullSharedFolderAccess()) {
				return false;
			}*/
		}
		return super.isFolderAdmin();
	}

	@Override
	public int getFolderPermission() {
		if (validateUserConfig()) {
			if (!hasModuleAccess(folderModule)) {
				return NO_PERMISSIONS;
			} else if (folderType == FolderObject.PUBLIC || getFuid() == FolderObject.SYSTEM_PUBLIC_FOLDER_ID) {
				if (folderModule != FolderObject.INFOSTORE && !userConfig.hasFullPublicFolderAccess()) {
					return super.getFolderPermission() > READ_FOLDER ? READ_FOLDER : super.getFolderPermission();
				}
			} else if (!userConfig.hasFullSharedFolderAccess() && folderType == FolderObject.SHARED) {
				return NO_PERMISSIONS;
			}
		}
		return super.getFolderPermission();
	}

	@Override
	public int getReadPermission() {
		if (validateUserConfig()) {
			if (!hasModuleAccess(folderModule)) {
				return NO_PERMISSIONS;
			} else if (folderType == FolderObject.PUBLIC || getFuid() == FolderObject.SYSTEM_PUBLIC_FOLDER_ID) {
				if (folderModule != FolderObject.INFOSTORE && !userConfig.hasFullPublicFolderAccess()) {
					return super.getReadPermission() > READ_ALL_OBJECTS ? READ_ALL_OBJECTS : super.getReadPermission();
				}
			} else if (!userConfig.hasFullSharedFolderAccess() && folderType == FolderObject.SHARED) {
				return NO_PERMISSIONS;
			}
		}
		return super.getReadPermission();
	}

	@Override
	public int getWritePermission() {
		if (validateUserConfig()) {
			if (!hasModuleAccess(folderModule)) {
				return NO_PERMISSIONS;
			} else if (folderType == FolderObject.PUBLIC || getFuid() == FolderObject.SYSTEM_PUBLIC_FOLDER_ID) {
				if (folderModule != FolderObject.INFOSTORE && !userConfig.hasFullPublicFolderAccess()) {
					return NO_PERMISSIONS;
				}
			} else if (!userConfig.hasFullSharedFolderAccess() && folderType == FolderObject.SHARED) {
				return NO_PERMISSIONS;
			}
		}
		return super.getWritePermission();
	}

	@Override
	public int getDeletePermission() {
		if (validateUserConfig()) {
			if (!hasModuleAccess(folderModule)) {
				return NO_PERMISSIONS;
			} else if (folderType == FolderObject.PUBLIC || getFuid() == FolderObject.SYSTEM_PUBLIC_FOLDER_ID) {
				if (folderModule != FolderObject.INFOSTORE && !userConfig.hasFullPublicFolderAccess()) {
					return NO_PERMISSIONS;
					/*return super.getDeletePermission() > DELETE_ALL_OBJECTS ? DELETE_ALL_OBJECTS : super
							.getDeletePermission();*/
				}
			} else if (!userConfig.hasFullSharedFolderAccess() && folderType == FolderObject.SHARED) {
				return NO_PERMISSIONS;
			}
		}
		return super.getDeletePermission();
	}

	private final boolean validateUserConfig() {
		if (userConfigAlreadyValidated) {
			return userConfigIsValid;
		}
		/*
		 * Permission's entity should be equal to config's user or should be
		 * contained in config's groups. Otherwise given configuration does not
		 * refer to permission's entity and has no effect on OCL permissions.
		 */
		userConfigAlreadyValidated = true;
		userConfigIsValid = false;
		if (userConfig == null) {
			return userConfigIsValid;
		}
		try {
			if (getFuid() <= 0) {
				return userConfigIsValid;
			}
			OXFolderAccess folderAccess = null;
			if (folderType <= 0) {
				folderType = (folderAccess = new OXFolderAccess(userConfig.getContext())).getFolderType(getFuid(),
						userConfig.getUserId());
			}
			if (folderModule <= 0) {
				if (folderAccess == null) {
					folderAccess = new OXFolderAccess(userConfig.getContext());
				}
				folderModule = folderAccess.getFolderModule(getFuid());
			}
		} catch (OXException e) {
			LOG.error(e.getMessage(), e);
			return userConfigIsValid;
		}
		if (userConfig.getUserId() == getEntity()) {
			userConfigIsValid = true;
		} else {
			final int[] groups = userConfig.getGroups();
			for (int i = 0; i < groups.length && !userConfigIsValid; i++) {
				userConfigIsValid = (groups[i] == getEntity());
			}
		}
		return userConfigIsValid;
	}

	@Override
	public boolean isFolderVisible() {
		if (isFolderAdmin()) {
			return true;
		}
		return (getFolderPermission() >= READ_FOLDER);
	}

	@Override
	public boolean canCreateObjects() {
		return (getFolderPermission() >= CREATE_OBJECTS_IN_FOLDER);
	}

	@Override
	public boolean canCreateSubfolders() {
		return (getFolderPermission() >= CREATE_SUB_FOLDERS);
	}

	@Override
	public boolean canReadOwnObjects() {
		return (getReadPermission() >= READ_OWN_OBJECTS);
	}

	@Override
	public boolean canReadAllObjects() {
		return (getReadPermission() >= READ_ALL_OBJECTS);
	}

	@Override
	public boolean canWriteOwnObjects() {
		return (getWritePermission() >= WRITE_OWN_OBJECTS);
	}

	@Override
	public boolean canWriteAllObjects() {
		return (getWritePermission() >= WRITE_ALL_OBJECTS);
	}

	@Override
	public boolean canDeleteOwnObjects() {
		return (getDeletePermission() >= DELETE_OWN_OBJECTS);
	}

	@Override
	public boolean canDeleteAllObjects() {
		return (getDeletePermission() >= DELETE_ALL_OBJECTS);
	}

	public OCLPermission getUnderlyingPermission() {
		if (underlyingPerm == null) {
			underlyingPerm = new OCLPermission();
			underlyingPerm.setEntity(super.getEntity());
			underlyingPerm.setFuid(super.getFuid());
			underlyingPerm.setFolderPermission(super.getFolderPermission());
			underlyingPerm.setReadObjectPermission(super.getReadPermission());
			underlyingPerm.setWriteObjectPermission(super.getWritePermission());
			underlyingPerm.setDeleteObjectPermission(super.getDeletePermission());
			underlyingPerm.setFolderAdmin(super.isFolderAdmin());
			underlyingPerm.setGroupPermission(super.isGroupPermission());
		}
		return underlyingPerm;
	}

	@Override
	public void storePermissions(final Context ctx, final Connection writeCon, final boolean action) throws Exception {
		throw new Exception("Instances of type EffectivePermission MUST NOT be saved");
	}

	@Override
	public void storePermissions(final Context ctx, final Connection writeCon, final int fuid, final boolean insert)
			throws Exception {
		throw new Exception("Instances of type EffectivePermission MUST NOT be saved");
	}

	@Override
	public void deletePermission(final Context ctx, final Connection writeCon) throws SQLException {
		throw new SQLException("Instances of EffectivePermission CAN NOT be deleted");
	}

	@Override
	public boolean loadPermissions(final Context ctx, final Connection con) throws SQLException {
		throw new SQLException("Instances of EffectivePermission CAN NOT be loaded");
	}

	@Override
	public String toString() {
		return new StringBuilder(150).append(STR_EFFECTIVE_PERMISSION)
				.append((isFolderAdmin() ? STR_ADMIN : STR_EMPTY)).append((isGroupPermission() ? STR_GROUP : STR_USER))
				.append(CHAR_AT).append(getFolderPermission()).append(CHAR_PIPE).append(getReadPermission()).append(
						CHAR_PIPE).append(getWritePermission()).append(CHAR_PIPE).append(getDeletePermission()).append(
						CHAR_BREAK).append(super.toString()).toString();
	}

}
