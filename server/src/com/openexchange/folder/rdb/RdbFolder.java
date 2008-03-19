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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import com.openexchange.folder.Folder;
import com.openexchange.folder.FolderException;
import com.openexchange.folder.FolderModule;
import com.openexchange.folder.FolderPermission;
import com.openexchange.folder.FolderPermissionStatus;
import com.openexchange.folder.FolderType;
import com.openexchange.groupware.userconfiguration.UserConfiguration;

/**
 * {@link RdbFolder} - Represents a folder kept in relational database
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class RdbFolder implements Folder<RdbFolderID>, Serializable {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -5266607144288530231L;

	private int createdBy = -1;

	private Date creationDate;

	private RdbFolderID id;

	private int modifiedBy = -1;

	private Date lastModified;

	private FolderModule module;

	private String name;

	private RdbFolderID parent;

	private RdbFolderPermission[] permissions;

	private FolderType type;

	private boolean hasSubfolders;

	private boolean b_hasSubfolders;

	private boolean defaultFlag;

	private boolean b_defaultFlag;

	private FolderPermissionStatus permissionStatus;

	/**
	 * Initializes a new {@link RdbFolder}
	 */
	public RdbFolder() {
		super();
	}

	public int getCreatedBy() {
		return createdBy;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public RdbFolderID getFolderID() {
		return id;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public int getModifiedBy() {
		return modifiedBy;
	}

	public FolderModule getModule() {
		return module;
	}

	public String getName() {
		return name;
	}

	public RdbFolderID getParentFolderID() {
		return parent;
	}

	public FolderPermission<RdbFolderID>[] getPermissions() {
		if (null == this.permissions) {
			return null;
		}
		final RdbFolderPermission[] permissions = new RdbFolderPermission[this.permissions.length];
		for (int i = 0; i < permissions.length; i++) {
			permissions[i] = (RdbFolderPermission) this.permissions[i].clone();
		}
		return permissions;
	}

	public FolderType getType() {
		return type;
	}

	public boolean hasSubfolders() {
		return hasSubfolders;
	}

	public void reset() {
		createdBy = -1;
		creationDate = null;
		id = null;
		modifiedBy = -1;
		lastModified = null;
		module = null;
		name = null;
		parent = null;
		permissions = null;
		type = null;
		hasSubfolders = false;
		b_hasSubfolders = false;
		defaultFlag = false;
		b_defaultFlag = false;
		permissionStatus = null;
	}

	public void setCreatedBy(final int createdBy) {
		this.createdBy = createdBy;
	}

	public void setCreationDate(final Date creationDate) {
		this.creationDate = (Date) creationDate.clone();
	}

	public void setFolderID(final RdbFolderID id) {
		this.id = id;
	}

	public void setHasSubfolder(final boolean hasSubfolders) {
		this.hasSubfolders = hasSubfolders;
		b_hasSubfolders = true;
	}

	public void setLastModified(final Date lastModified) {
		this.lastModified = (Date) lastModified.clone();
	}

	public void setModifiedBy(final int modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public void setModule(final FolderModule module) {
		this.module = module;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setParentFolderID(final RdbFolderID id) {
		this.parent = id;
	}

	public void setPermissions(final FolderPermission<RdbFolderID>[] permissions) {
		if (null == permissions) {
			this.permissions = null;
			return;
		}
		this.permissions = new RdbFolderPermission[permissions.length];
		for (int i = 0; i < permissions.length; i++) {
			this.permissions[i] = (RdbFolderPermission) permissions[i].clone();
		}
	}

	public void setType(final FolderType type) {
		this.type = type;
	}

	public FolderPermissionStatus getPermissionStatus() {
		return permissionStatus;
	}

	public boolean isDefault() {
		return defaultFlag;
	}

	public void setDefault(final boolean b) {
		this.defaultFlag = b;
		b_defaultFlag = true;
	}

	public void setPermissionStatus(final FolderPermissionStatus permissionStatus) {
		this.permissionStatus = permissionStatus;
	}

	@Override
	public Object clone() {
		try {
			final RdbFolder clone = (RdbFolder) super.clone();
			clone.id = (RdbFolderID) id.clone();
			clone.creationDate = (Date) creationDate.clone();
			clone.lastModified = (Date) lastModified.clone();
			clone.parent = (RdbFolderID) parent.clone();
			if (null != permissions) {
				clone.permissions = new RdbFolderPermission[permissions.length];
				for (int i = 0; i < permissions.length; i++) {
					clone.permissions[i] = (RdbFolderPermission) permissions[i].clone();
				}
			}
			return clone;
		} catch (final CloneNotSupportedException e) {
			throw new InternalError("RdbFolder.clone(): CloneNotSupportedException even though Coneable is implemented");
		}

	}

	public boolean isShared(final int user) {
		return (type == RdbFolderType.TYPE_PRIVATE && createdBy != user);
	}

	public FolderType getType(final int user) {
		return isShared(user) ? RdbFolderType.TYPE_SHARED : type;
	}

	public boolean containsDefault() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean containsHasSubfolder() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Creates the effective folder permission for specified configuration's
	 * entity.
	 * 
	 * @param userConfiguration
	 *            The user configuration
	 * @return The effective folder permission for specified configuration's
	 *         entity.
	 * @throws FolderException
	 *             If effective permission cannot be created
	 */
	public RdbEffectiveFolderPermission getEffectiveUserPermission(final UserConfiguration userConfiguration)
			throws FolderException {
		final int[] idArr;
		{
			final int[] groups = userConfiguration.getGroups();
			idArr = new int[groups.length + 1];
			idArr[0] = userConfiguration.getUserId();
			System.arraycopy(groups, 0, idArr, 1, groups.length);
			Arrays.sort(idArr);
		}
		int fp = 0, rp = 0, wp = 0, dp = 0;
		boolean admin = false;
		NextPerm: for (RdbFolderPermission cur : permissions) {
			if (Arrays.binarySearch(idArr, cur.getEntity()) < 0) {
				continue NextPerm;
			}
			fp = Math.max(fp, cur.getFolderPermission());
			rp = Math.max(rp, cur.getReadPermission());
			wp = Math.max(wp, cur.getWritePermission());
			dp = Math.max(dp, cur.getDeletePermission());
			admin = admin || cur.isAdmin();
		}
		final RdbEffectiveFolderPermission retval = new RdbEffectiveFolderPermission(userConfiguration, this);
		retval.setEntity(userConfiguration.getUserId());
		retval.setFolderID(getFolderID());
		retval.setFolderPermission(fp);
		retval.setDeletePermission(dp);
		retval.setReadPermission(rp);
		retval.setWritePermission(wp);
		retval.setAdmin(admin);
		return retval;
	}
}
