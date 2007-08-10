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

import java.io.Serializable;
import java.security.acl.Permission;

import com.openexchange.tools.OXCloneable;

/**
 * OCLPermission
 * 
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OCLPermission implements Permission, Cloneable, Serializable, OXCloneable<OCLPermission> {

	/*
	 * Permisson Matrix # 2 4 8 16 32 64 128 #WERT
	 * 
	 * Folder z 0 Folder +r 2 2 Folder +co 2 4 6 Folder +csf 2 4 8 14 Folder
	 * admin*a 128
	 * 
	 * Object z 0 Object +ro 2 2 Object +ra 2 4 6 Object admin*r 128
	 * 
	 * Object z 0 Object +wo 2 2 Object +wa 2 4 6 Object admin*w 128
	 * 
	 * Object +do 2 2 Object +da 2 4 4 Object admin*d 128
	 * 
	 * (*a) to delete a folder the user needs permissons to delete every object
	 * in the folder!
	 * 
	 * We must be able to: - set the owner - set role (only if principal ==
	 * owner or another admin can add a new entity) - protect existing
	 * permissons
	 * 
	 * 
	 * CREATE TABLE folder ( "fuid" integer, "parent" integer, "fname" text,
	 * "module" text, "type" text, "owner" text, "creator" text, "pid" integer,
	 * "creating_date" timestamp, "created_from" text, "changing_date"
	 * timestamp, "changed_from" text );
	 * 
	 * fuid = unique folder id parent = parent folder (fuid) fname = folder name
	 * module = system, task, calendar, contact, unbound type = system, private,
	 * public, share owner = uid creator = uid pid = pointer to permission
	 * 
	 * CREATE TABLE permission ( "puid" integer, "pid" integer, "role" integer,
	 * "entity" text, "sealed" integer, "fp" integer, "orp" integer, "owp"
	 * integer, "odp" integer );
	 * 
	 * puid = unique permission id pid = permission id (folder.pid) role = role
	 * entity = entity (uid, group, ...) sealed = sealed (0 / n) fp = folder
	 * permission orp = object read permission owp = object write permission odp =
	 * object delete permission
	 * 
	 */

	private static final char CHAR_DOT = '.';

	private static final String STR_USER = "User";

	private static final String STR_GROUP = "Group";

	private static final String STR_EMPTY = "";

	private static final String STR_FOLDER_ADMIN = "_FolderAdmin";

	private static final long serialVersionUID = 3740098766897625419L;

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(OCLPermission.class);

	public static final int NO_PERMISSIONS = 0;

	public static final int ADMIN_PERMISSION = 128;

	public static final int READ_FOLDER = 2;

	public static final int CREATE_OBJECTS_IN_FOLDER = 4;

	public static final int CREATE_SUB_FOLDERS = 8;

	public static final int READ_OWN_OBJECTS = 2;

	public static final int READ_ALL_OBJECTS = 4;

	public static final int WRITE_OWN_OBJECTS = 2;

	public static final int WRITE_ALL_OBJECTS = 4;

	public static final int DELETE_OWN_OBJECTS = 2;

	public static final int DELETE_ALL_OBJECTS = 4;

	public static final int ALL_GROUPS_AND_USERS = 0;

	private String name;

	private int fuid;

	private int entity = -1;

	private int fp;

	private int orp;

	private int owp;

	private int odp;

	/**
	 * This property defines if this permission declares the owner to be the
	 * folder administrator who posseses the rights to alter a folder's
	 * properties or to rename a folder
	 */
	private boolean folderAdmin;

	/**
	 * This property defines if this permission is applied to a system group or
	 * to a single user instead
	 */
	private boolean groupPermission;

	/**
	 * Constructor
	 * 
	 */
	public OCLPermission() {
		super();
	}

	/**
	 * Constructor
	 * 
	 */
	public OCLPermission(final int entity, final int fuid) {
		super();
		this.entity = entity;
		this.fuid = fuid;
	}

	public void reset() {
		name = null;
		fuid = 0;
		entity = -1;
		fp = 0;
		orp = 0;
		owp = 0;
		odp = 0;
		folderAdmin = false;
		groupPermission = false;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setEntity(final int entity) {
		this.entity = entity;
		if (name == null) {
			name = entity + (folderAdmin ? STR_FOLDER_ADMIN : STR_EMPTY) + (groupPermission ? STR_GROUP : STR_USER);
		}
	}

	public void setFolderAdmin(final boolean folderAdmin) {
		this.folderAdmin = folderAdmin;
		if (name == null) {
			name = entity + (folderAdmin ? STR_FOLDER_ADMIN : STR_EMPTY) + (groupPermission ? STR_GROUP : STR_USER);
		}
	}

	public void setGroupPermission(final boolean groupPermission) {
		this.groupPermission = groupPermission;
		if (name == null) {
			name = entity + (folderAdmin ? STR_FOLDER_ADMIN : STR_EMPTY) + (groupPermission ? STR_GROUP : STR_USER);
		}
	}

	public boolean setFolderPermission(final int p) {
		if (validatePermission(p)) {
			this.fp = p;
			return true;
		}
		return false;
	}

	public boolean setReadObjectPermission(final int p) {
		if (validatePermission(p)) {
			this.orp = p;
			return true;
		}
		return false;
	}

	public boolean setWriteObjectPermission(final int p) {
		if (validatePermission(p)) {
			this.owp = p;
			return true;
		}
		return false;
	}

	public boolean setDeleteObjectPermission(final int p) {
		if (validatePermission(p)) {
			this.odp = p;
			return true;
		}
		return false;
	}

	public boolean setAllObjectPermission(final int pr, final int pw, final int pd) {
		if (validatePermission(pr) && validatePermission(pw) && validatePermission(pd)) {
			this.orp = pr;
			this.owp = pw;
			this.odp = pd;
			return true;
		}
		return false;
	}

	public boolean setAllPermission(final int fp, final int opr, final int opw, final int opd) {
		if (validatePermission(fp) && validatePermission(opr) && validatePermission(opw) && validatePermission(opd)) {
			this.fp = fp;
			this.orp = opr;
			this.owp = opw;
			this.odp = opd;
			return true;
		}
		return false;
	}

	private boolean validatePermission(final int p) {
		return ((p % 2 == 0 && (p <= 128 && p >= 0)));
	}

	public void setFuid(final int pid) {
		this.fuid = pid;
	}

	public boolean isFolderAdmin() {
		return folderAdmin;
	}

	public boolean isGroupPermission() {
		return groupPermission;
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

	public int getDeletePermission() {
		return odp;
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

	public String getName() {
		return name;
	}

	public int getEntity() {
		return entity;
	}

	public int getFuid() {
		return fuid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer(50);
		sb.append((folderAdmin ? STR_FOLDER_ADMIN : STR_EMPTY)).append((groupPermission ? STR_GROUP : STR_USER))
				.append(entity).append('@').append(fp).append(CHAR_DOT).append(orp).append(CHAR_DOT).append(owp)
				.append(CHAR_DOT).append(odp);
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@SuppressWarnings("cast")
	@Override
	public Object clone() throws CloneNotSupportedException {
		return ((OCLPermission) super.clone());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.tools.OXCloneable#deepClone()
	 */
	public OCLPermission deepClone() {
		try {
			return ((OCLPermission) super.clone());
		} catch (final CloneNotSupportedException e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

}
