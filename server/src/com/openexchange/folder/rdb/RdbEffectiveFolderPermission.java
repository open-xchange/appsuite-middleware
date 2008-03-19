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

import com.openexchange.folder.FolderModule;
import com.openexchange.folder.FolderType;
import com.openexchange.groupware.userconfiguration.UserConfiguration;

/**
 * {@link RdbEffectiveFolderPermission} - Enhances the common
 * {@link RdbFolderPermission} class by applying a user configuration to user's
 * actual folder permission.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class RdbEffectiveFolderPermission extends RdbFolderPermission {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 6170547189553615852L;

	private UserConfiguration userConfiguration;

	private final FolderModule module;

	private final FolderType type;

	/**
	 * Initializes a new {@link RdbEffectiveFolderPermission}
	 * 
	 * @param userConfiguration
	 *            The user configuration
	 * @param id
	 *            The folder ID
	 * @param module
	 *            The folder module
	 * @param type
	 *            The folder type
	 */
	public RdbEffectiveFolderPermission(final UserConfiguration userConfiguration, RdbFolder folder) {
		super();
		this.module = folder.getModule();
		this.type = folder.getType(userConfiguration.getUserId());
		this.userConfiguration = userConfiguration;
		setFolderID(folder.getFolderID());
		setEntity(userConfiguration.getUserId());
		setGroup(false);
	}

	@Override
	public Object clone() {
		final RdbEffectiveFolderPermission clone = (RdbEffectiveFolderPermission) super.clone();
		clone.userConfiguration = (UserConfiguration) userConfiguration.clone();
		return clone;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((module == null) ? 0 : module.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((userConfiguration == null) ? 0 : userConfiguration.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RdbEffectiveFolderPermission)) {
			return false;
		}
		final RdbEffectiveFolderPermission other = (RdbEffectiveFolderPermission) obj;
		if (!super.equals(other)) {
			return false;
		}
		if (module == null) {
			if (other.module != null) {
				return false;
			}
		} else if (!module.equals(other.module)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		if (userConfiguration == null) {
			if (other.userConfiguration != null) {
				return false;
			}
		} else if (!userConfiguration.equals(other.userConfiguration)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isAdmin() {
		return isAdmin(true);
	}

	public boolean isAdmin(final boolean withConf) {
		if (withConf) {
			if (!userConfiguration.hasModuleAccess(module.getValue())) {
				return false;
			} else if (type == RdbFolderType.TYPE_PUBLIC && module != RdbFolderModule.MODULE_INFOSTORE
					&& !userConfiguration.hasFullPublicFolderAccess()) {
				return false;
			}
		}
		return super.isAdmin();
	}

	@Override
	public int getFolderPermission() {
		return getFolderPermission(true);
	}

	/**
	 * Gets the folder permission
	 * 
	 * @param withConf
	 *            <code>true</code> to apply possible user configuration's
	 *            restrictions; otherwise <code>false</code>
	 * @return The folder permission
	 */
	public int getFolderPermission(final boolean withConf) {
		return determineConfigurationPermission(withConf, super.getFolderPermission() > READ_FOLDER ? READ_FOLDER
				: super.getFolderPermission(), super.getFolderPermission());
	}

	@Override
	public int getReadPermission() {
		return getReadPermission(true);
	}

	/**
	 * Gets the (object) read permission
	 * 
	 * @param withConf
	 *            <code>true</code> to apply possible user configuration's
	 *            restrictions; otherwise <code>false</code>
	 * @return The (object) read permission
	 */
	public int getReadPermission(final boolean withConf) {
		return determineConfigurationPermission(withConf,
				super.getReadPermission() > READ_ALL_OBJECTS ? READ_ALL_OBJECTS : super.getReadPermission(), super
						.getReadPermission());
	}

	@Override
	public int getWritePermission() {
		return getWritePermission(true);
	}

	/**
	 * Gets the (object) write permission
	 * 
	 * @param withConf
	 *            <code>true</code> to apply possible user configuration's
	 *            restrictions; otherwise <code>false</code>
	 * @return The (object) write permission
	 */
	public int getWritePermission(final boolean withConf) {
		return determineConfigurationPermission(withConf, NO_PERMISSION, super.getWritePermission());
	}

	@Override
	public int getDeletePermission() {
		return getDeletePermission(true);
	}

	/**
	 * Gets the (object) delete permission
	 * 
	 * @param withConf
	 *            <code>true</code> to apply possible user configuration's
	 *            restrictions; otherwise <code>false</code>
	 * @return The (object) delete permission
	 */
	public int getDeletePermission(final boolean withConf) {
		return determineConfigurationPermission(withConf, NO_PERMISSION, super.getDeletePermission());
	}

	/**
	 * Determines the effective permission when applying possible user
	 * configuration's restrictions on modules, public folders, and shared
	 * folders.
	 * <p>
	 * If no restrictions exist or <code>withConf</code> argument is set to
	 * <code>false</code>, the value of specified argument
	 * <code>underlyingPerm</code> is returned.
	 * 
	 * @param withConf
	 *            <code>true</code> to apply possible user configuration's
	 *            restrictions; otherwise <code>false</code> to return the
	 *            underlying permission
	 * @param publicPerm
	 *            The permission for public folder if user configuration does
	 *            not grant full public folder access
	 * @param underlyingPerm
	 *            The underlying permission
	 * @return The effective permission.
	 */
	private int determineConfigurationPermission(final boolean withConf, final int publicPerm, final int underlyingPerm) {
		if (withConf) {
			if (!userConfiguration.hasModuleAccess(module.getValue())) {
				/*
				 * No module access
				 */
				return NO_PERMISSION;
			} else if (type == RdbFolderType.TYPE_PUBLIC || getFolderID().fuid == RdbFolderID.SYSTEM_PUBLIC_FOLDER_ID) {
				if (module != RdbFolderModule.MODULE_INFOSTORE && !userConfiguration.hasFullPublicFolderAccess()) {
					/*
					 * Restricted public folder access
					 */
					return publicPerm;
				}
			} else if (!userConfiguration.hasFullSharedFolderAccess() && type == RdbFolderType.TYPE_SHARED) {
				/*
				 * No shared folder access
				 */
				return NO_PERMISSION;
			}
		}
		return underlyingPerm;
	}

	/**
	 * Checks if folder is visible according to folder permission
	 * 
	 * @param withConf
	 *            <code>true</code> to apply possible user configuration's
	 *            restrictions; otherwise <code>false</code> to return the
	 *            underlying permission
	 * @return <code>true</code> if folder is visible according to folder
	 *         permission; otherwise <code>false</code>
	 */
	public boolean isFolderVisible(final boolean withConf) {
		return isAdmin(withConf) || (getFolderPermission(withConf) >= READ_FOLDER);
	}

	/**
	 * Checks if objects may be created according to folder permission
	 * 
	 * @param withConf
	 *            <code>true</code> to apply possible user configuration's
	 *            restrictions; otherwise <code>false</code> to return the
	 *            underlying permission
	 * @return <code>true</code> if objects may be created according to folder
	 *         permission; otherwise <code>false</code>
	 */
	public boolean canCreateObjects(final boolean withConf) {
		return (getFolderPermission(withConf) >= CREATE_OBJECTS_IN_FOLDER);
	}

	/**
	 * Checks if subfolders may be created according to folder permission
	 * 
	 * @param withConf
	 *            <code>true</code> to apply possible user configuration's
	 *            restrictions; otherwise <code>false</code> to return the
	 *            underlying permission
	 * @return <code>true</code> if subfolders may be created according to
	 *         folder permission; otherwise <code>false</code>
	 */
	public boolean canCreateSubfolders(final boolean withConf) {
		return (getFolderPermission(withConf) >= CREATE_SUB_FOLDERS);
	}

	/**
	 * Checks if own objects may be read according to read permission
	 * 
	 * @param withConf
	 *            <code>true</code> to apply possible user configuration's
	 *            restrictions; otherwise <code>false</code> to return the
	 *            underlying permission
	 * @return <code>true</code> if own objects may be read according to read
	 *         permission; otherwise <code>false</code>
	 */
	public boolean canReadOwnObjects(final boolean withConf) {
		return (getReadPermission(withConf) >= READ_OWN_OBJECTS);
	}

	/**
	 * Checks if all objects may be read according to read permission
	 * 
	 * @param withConf
	 *            <code>true</code> to apply possible user configuration's
	 *            restrictions; otherwise <code>false</code> to return the
	 *            underlying permission
	 * @return <code>true</code> if all objects may be read according to read
	 *         permission; otherwise <code>false</code>
	 */
	public boolean canReadAllObjects(final boolean withConf) {
		return (getReadPermission(withConf) >= READ_ALL_OBJECTS);
	}

	/**
	 * Checks if own objects may be written according to write permission
	 * 
	 * @param withConf
	 *            <code>true</code> to apply possible user configuration's
	 *            restrictions; otherwise <code>false</code> to return the
	 *            underlying permission
	 * @return <code>true</code> if own objects may be written according to
	 *         write permission; otherwise <code>false</code>
	 */
	public boolean canWriteOwnObjects(final boolean withConf) {
		return (getWritePermission(withConf) >= WRITE_OWN_OBJECTS);
	}

	/**
	 * Checks if all objects may be written according to write permission
	 * 
	 * @param withConf
	 *            <code>true</code> to apply possible user configuration's
	 *            restrictions; otherwise <code>false</code> to return the
	 *            underlying permission
	 * @return <code>true</code> if all objects may be written according to
	 *         write permission; otherwise <code>false</code>
	 */
	public boolean canWriteAllObjects(final boolean withConf) {
		return (getWritePermission(withConf) >= WRITE_ALL_OBJECTS);
	}

	/**
	 * Checks if own objects may be deleted according to delete permission
	 * 
	 * @param withConf
	 *            <code>true</code> to apply possible user configuration's
	 *            restrictions; otherwise <code>false</code> to return the
	 *            underlying permission
	 * @return <code>true</code> if own objects may be deleted according to
	 *         delete permission; otherwise <code>false</code>
	 */
	public boolean canDeleteOwnObjects(final boolean withConf) {
		return (getDeletePermission(withConf) >= DELETE_OWN_OBJECTS);
	}

	/**
	 * Checks if all objects may be deleted according to delete permission
	 * 
	 * @param withConf
	 *            <code>true</code> to apply possible user configuration's
	 *            restrictions; otherwise <code>false</code> to return the
	 *            underlying permission
	 * @return <code>true</code> if all objects may be deleted according to
	 *         delete permission; otherwise <code>false</code>
	 */
	public boolean canDeleteAllObjects(final boolean withConf) {
		return (getDeletePermission(withConf) >= DELETE_ALL_OBJECTS);
	}
}
