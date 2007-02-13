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

import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.oxfolder.OXFolderTools;

public class EffectivePermission extends OCLPermission {

	private static final long serialVersionUID = -1303754404748836561L;
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(EffectivePermission.class);

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

	private boolean userConfigIsValid = false;

	private boolean userConfigAlreadyValidated = false;

	public EffectivePermission(int entity, int fuid, int folderType,
			int folderModule, UserConfiguration userConfig) {
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

	public boolean isFolderAdmin() {
		if (validateUserConfig()) {
			if (folderType == FolderObject.PUBLIC
					&& folderModule != FolderObject.INFOSTORE
					&& !userConfig.hasFullPublicFolderAccess()) {
				return false;
			} else if (!hasModuleAccess(folderModule)) {
				return false;
			}
		}
		return super.isFolderAdmin();
	}

	public int getFolderPermission() {
		if (validateUserConfig()) {
			if (!hasModuleAccess(folderModule)) {
				return NO_PERMISSIONS;
			} else if (folderType == FolderObject.PUBLIC) {
				if (folderModule != FolderObject.INFOSTORE
						&& !userConfig.hasFullPublicFolderAccess()) {
					return super.getFolderPermission() > READ_FOLDER ? READ_FOLDER
							: super.getFolderPermission();
				}
			} else if (!userConfig.hasFullSharedFolderAccess()
					&& folderType == FolderObject.SHARED) {
				return NO_PERMISSIONS;
			}
		}
		return super.getFolderPermission();
	}

	public int getReadPermission() {
		if (validateUserConfig()) {
			if (!hasModuleAccess(folderModule)) {
				return NO_PERMISSIONS;
			} else if (folderType == FolderObject.PUBLIC) {
				if (folderModule != FolderObject.INFOSTORE
						&& !userConfig.hasFullPublicFolderAccess()) {
					return super.getReadPermission() > READ_ALL_OBJECTS ? READ_ALL_OBJECTS
							: super.getReadPermission();
				}
			} else if (!userConfig.hasFullSharedFolderAccess()
					&& folderType == FolderObject.SHARED) {
				return NO_PERMISSIONS;
			}
		}
		return super.getReadPermission();
	}

	public int getWritePermission() {
		if (validateUserConfig()) {
			if (!hasModuleAccess(folderModule)) {
				return NO_PERMISSIONS;
			} else if (folderType == FolderObject.PUBLIC) {
				if (folderModule != FolderObject.INFOSTORE
						&& !userConfig.hasFullPublicFolderAccess()) {
					return NO_PERMISSIONS;
				}
			} else if (!userConfig.hasFullSharedFolderAccess()
					&& folderType == FolderObject.SHARED) {
				return NO_PERMISSIONS;
			}
		}
		return super.getWritePermission();
	}

	public int getDeletePermission() {
		if (validateUserConfig()) {
			if (!hasModuleAccess(folderModule)) {
				return NO_PERMISSIONS;
			} else if (folderType == FolderObject.PUBLIC) {
				if (folderModule != FolderObject.INFOSTORE
						&& !userConfig.hasFullPublicFolderAccess()) {
					return super.getDeletePermission() > DELETE_ALL_OBJECTS ? DELETE_ALL_OBJECTS
							: super.getDeletePermission();
				}
			} else if (!userConfig.hasFullSharedFolderAccess()
					&& folderType == FolderObject.SHARED) {
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
			if (folderType <= 0) {
				folderType = OXFolderTools.getFolderType(getFuid(), userConfig
						.getUserId(), userConfig.getContext());
			}
			if (folderModule <= 0) {
				folderModule = OXFolderTools.getFolderModule(getFuid(),
						userConfig.getContext());
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return userConfigIsValid;
		}
		if (userConfig.getUserId() == getEntity()) {
			userConfigIsValid = true;
		}
		else {
			for (int i = 0; i < userConfig.getGroups().length
					&& !userConfigIsValid; i++) {
				userConfigIsValid = (userConfig.getGroups()[i] == getEntity());
			}
		}
		return userConfigIsValid;
	}

	public boolean isFolderVisible() {
		if (isFolderAdmin()) {
			return true;
		}
		return (getFolderPermission() >= READ_FOLDER);
	}

	public boolean canCreateObjects() {
		return (getFolderPermission() >= CREATE_OBJECTS_IN_FOLDER);
	}

	public boolean canCreateSubfolders() {
		return (getFolderPermission() >= CREATE_SUB_FOLDERS);
	}

	public boolean canReadOwnObjects() {
		return (getReadPermission() >= READ_OWN_OBJECTS);
	}

	public boolean canReadAllObjects() {
		return (getReadPermission() >= READ_ALL_OBJECTS);
	}

	public boolean canWriteOwnObjects() {
		return (getWritePermission() >= WRITE_OWN_OBJECTS);
	}

	public boolean canWriteAllObjects() {
		return (getWritePermission() >= WRITE_ALL_OBJECTS);
	}

	public boolean canDeleteOwnObjects() {
		return (getDeletePermission() >= DELETE_OWN_OBJECTS);
	}

	public boolean canDeleteAllObjects() {
		return (getDeletePermission() >= DELETE_ALL_OBJECTS);
	}

	public OCLPermission getUnderlyingPermission() {
		final OCLPermission p = new OCLPermission();
		p.setEntity(super.getEntity());
		p.setFuid(super.getFuid());
		p.setFolderPermission(super.getFolderPermission());
		p.setReadObjectPermission(super.getReadPermission());
		p.setWriteObjectPermission(super.getWritePermission());
		p.setDeleteObjectPermission(super.getDeletePermission());
		p.setFolderAdmin(super.isFolderAdmin());
		p.setGroupPermission(super.isGroupPermission());
		return p;
	}

	public void storePermissions(final Context ctx, final Connection writeCon,
			final boolean action) throws Exception {
		throw new Exception(
				"Instances of type EffectivePermission MUST NOT be saved");
	}

	public void storePermissions(final Context ctx, final Connection writeCon, final int fuid,
			final boolean insert) throws Exception {
		throw new Exception(
				"Instances of type EffectivePermission MUST NOT be saved");
	}

	public void deletePermission(final Context ctx, final Connection writeCon)
			throws SQLException {
		throw new SQLException(
				"Instances of EffectivePermission CAN NOT be deleted");
	}

	public boolean loadPermissions(final Context ctx, final Connection con)
			throws SQLException {
		throw new SQLException(
				"Instances of EffectivePermission CAN NOT be loaded");
	}

	public String toString() {
		return "EffectivePermission_" + (isFolderAdmin() ? "Admin" : "")
				+ (isGroupPermission() ? "Group" : "User") + "@"
				+ getFolderPermission() + "|" + getReadPermission() + "|"
				+ getWritePermission() + "|" + getDeletePermission() + "\n"
				+ super.toString();
	}

}
