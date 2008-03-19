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

import com.openexchange.folder.FolderException;
import com.openexchange.folder.FolderPermission;

/**
 * {@link RdbFolderPermission} - The folder permission for folders kept in
 * relational database.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class RdbFolderPermission implements FolderPermission<RdbFolderID>, Serializable {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -4078544855362772158L;

	private int entity;

	private RdbFolderID fuid;

	private boolean group;

	private boolean admin;

	private int fp;

	private int orp;

	private int owp;

	private int odp;

	/**
	 * Initializes a new {@link RdbFolderPermission}
	 */
	public RdbFolderPermission() {
		super();
	}

	@Override
	public Object clone() {
		try {
			final RdbFolderPermission clone = (RdbFolderPermission) super.clone();
			clone.fuid = (RdbFolderID) fuid.clone();
			return clone;
		} catch (final CloneNotSupportedException e) {
			throw new InternalError(
					"RdbFolderPermission.clone(): CloneNotSupportedException even though Cloneable is implemented");
		}
	}

	private boolean validatePermission(final int p) {
		return ((p % 2 == 0 && (p <= 128 && p >= 0)));
	}

	public int getDeletePermission() {
		return odp;
	}

	public int getEntity() {
		return entity;
	}

	public RdbFolderID getFolderID() {
		return fuid;
	}

	public int getFolderPermission() {
		return fp;
	}

	public int getReadPermission() {
		return orp;
	}

	public int getWritePermission() {
		return owp;
	}

	public boolean isAdmin() {
		return admin;
	}

	public boolean isGroup() {
		return group;
	}

	public void setAdmin(final boolean admin) {
		this.admin = admin;
	}

	public void setDeletePermission(final int deletePermission) throws FolderException {
		if (!validatePermission(deletePermission)) {
			throw new FolderException(FolderException.Code.INVALID_PERMISSION, Integer.valueOf(deletePermission));
		}
		this.odp = deletePermission;
	}

	public void setEntity(final int entity) {
		this.entity = entity;
	}

	public void setFolderID(final RdbFolderID id) {
		this.fuid = id;
	}

	public void setFolderPermission(final int folderPermission) throws FolderException {
		if (!validatePermission(folderPermission)) {
			throw new FolderException(FolderException.Code.INVALID_PERMISSION, Integer.valueOf(folderPermission));
		}
		this.fp = folderPermission;
	}

	public void setGroup(final boolean group) {
		this.group = group;
	}

	public void setReadPermission(final int readPermission) throws FolderException {
		if (!validatePermission(readPermission)) {
			throw new FolderException(FolderException.Code.INVALID_PERMISSION, Integer.valueOf(readPermission));
		}
		this.orp = readPermission;
	}

	public void setWritePermission(final int writePermission) throws FolderException {
		if (!validatePermission(writePermission)) {
			throw new FolderException(FolderException.Code.INVALID_PERMISSION, Integer.valueOf(writePermission));
		}
		this.owp = writePermission;
	}

	public final boolean isFolderVisible() {
		return isAdmin() || (getFolderPermission() >= READ_FOLDER);
	}

	public final boolean canCreateObjects() {
		return (getFolderPermission() >= CREATE_OBJECTS_IN_FOLDER);
	}

	public final boolean canCreateSubfolders() {
		return (getFolderPermission() >= CREATE_SUB_FOLDERS);
	}

	public final boolean canReadOwnObjects() {
		return (getReadPermission() >= READ_OWN_OBJECTS);
	}

	public final boolean canReadAllObjects() {
		return (getReadPermission() >= READ_ALL_OBJECTS);
	}

	public final boolean canWriteOwnObjects() {
		return (getWritePermission() >= WRITE_OWN_OBJECTS);
	}

	public final boolean canWriteAllObjects() {
		return (getWritePermission() >= WRITE_ALL_OBJECTS);
	}

	public final boolean canDeleteOwnObjects() {
		return (getDeletePermission() >= DELETE_OWN_OBJECTS);
	}

	public final boolean canDeleteAllObjects() {
		return (getDeletePermission() >= DELETE_ALL_OBJECTS);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (admin ? 1231 : 1237);
		result = prime * result + entity;
		result = prime * result + fp;
		result = prime * result + ((fuid == null) ? 0 : fuid.hashCode());
		result = prime * result + (group ? 1231 : 1237);
		result = prime * result + odp;
		result = prime * result + orp;
		result = prime * result + owp;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RdbFolderPermission)) {
			return false;
		}
		final RdbFolderPermission other = (RdbFolderPermission) obj;
		if (admin != other.admin) {
			return false;
		}
		if (entity != other.entity) {
			return false;
		}
		if (fp != other.fp) {
			return false;
		}
		if (fuid == null) {
			if (other.fuid != null) {
				return false;
			}
		} else if (!fuid.equals(other.fuid)) {
			return false;
		}
		if (group != other.group) {
			return false;
		}
		if (odp != other.odp) {
			return false;
		}
		if (orp != other.orp) {
			return false;
		}
		if (owp != other.owp) {
			return false;
		}
		return true;
	}

}
