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

package com.openexchange;

import java.sql.Connection;
import java.sql.SQLException;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.imap.IMAPException;
import com.openexchange.groupware.imap.OXUser2IMAPLogin;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.Rights;

/**
 * IMAPPermission - maps existing folder permissions to corresponding IMAP
 * rights
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class IMAPPermission extends OCLPermission {

	private static final long serialVersionUID = -4781017654229881491L;

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(IMAPPermission.class);

	private String folderFullname;

	private final Context ctx;

	private ACL acl;

	/**
	 * Constructor
	 * 
	 * @param ctx -
	 *            the context
	 */
	public IMAPPermission(final Context ctx) {
		super();
		this.ctx = ctx;
	}

	/**
	 * Constructor
	 * 
	 * @param entity -
	 *            the entity ID
	 * @param ctx -
	 *            the context
	 */
	public IMAPPermission(final int entity, final Context ctx) {
		this(ctx);
		super.setEntity(entity);
	}

	public final String getFolderFullname() {
		return folderFullname;
	}

	public final void setFolderFullname(final String folderFullname) {
		this.folderFullname = folderFullname;
	}

	public void setEntity(final int entity) {
		super.setEntity(entity);
		this.acl = null;
	}

	public void setFolderAdmin(final boolean folderAdmin) {
		super.setFolderAdmin(folderAdmin);
		this.acl = null;
	}

	public void setGroupPermission(final boolean groupPermission) {
		super.setGroupPermission(groupPermission);
		this.acl = null;
	}

	public boolean setFolderPermission(final int p) {
		this.acl = null;
		return super.setFolderPermission(p);
	}

	public boolean setReadObjectPermission(final int p) {
		this.acl = null;
		return super.setReadObjectPermission(p);
	}

	public boolean setWriteObjectPermission(final int p) {
		this.acl = null;
		return super.setWriteObjectPermission(p);
	}

	public boolean setDeleteObjectPermission(final int p) {
		this.acl = null;
		return super.setDeleteObjectPermission(p);
	}

	public boolean setAllObjectPermission(final int pr, final int pw, final int pd) {
		this.acl = null;
		return super.setAllObjectPermission(pr, pw, pd);
	}

	public boolean setAllPermission(final int fp, final int opr, final int opw, final int opd) {
		this.acl = null;
		return super.setAllPermission(fp, opr, opw, opd);
	}

	public void setFuid(final int pid) {
		if (LOG.isWarnEnabled()) {
			LOG.warn(FolderCode.IMAP_PERMISSION_ERROR.getMessage());
		}
	}

	public int getFuid() {
		if (LOG.isWarnEnabled()) {
			LOG.warn(FolderCode.IMAP_PERMISSION_ERROR.getMessage());
		}
		return -1;
	}

	public void storePermissions(final Context ctx, final Connection writeCon, final boolean insert) throws Exception {
		if (LOG.isWarnEnabled()) {
			LOG.warn(FolderCode.IMAP_PERMISSION_ERROR.getMessage());
		}
	}

	public void storePermissions(final Context ctx, final Connection writeConArg, final int fuid, final boolean insert)
			throws Exception {
		if (LOG.isWarnEnabled()) {
			LOG.warn(FolderCode.IMAP_PERMISSION_ERROR.getMessage());
		}
	}

	public void deletePermission(final Context ctx, final Connection writeConArg) throws SQLException,
			DBPoolingException {
		if (LOG.isWarnEnabled()) {
			LOG.warn(FolderCode.IMAP_PERMISSION_ERROR.getMessage());
		}
	}

	public boolean loadPermissions(final Context ctx, final Connection conArg) throws SQLException, DBPoolingException {
		if (LOG.isWarnEnabled()) {
			LOG.warn(FolderCode.IMAP_PERMISSION_ERROR.getMessage());
		}
		return false;
	}

	public void reset() {
		super.reset();
		folderFullname = null;
		this.acl = null;

	}

	/*
	 * Full rights: "acdilprsw"
	 */

	private static final String STR_FOLDER_ADMIN = "acl";

	private static final String STR_FOLDER_VISIBLE = "l";

	private static final String STR_FOLDER_CREATE_OBJECTS = "il";

	private static final String STR_FOLDER_CREATE_SUBFOLDERS = "cil";

	private static final String STR_READ_ALL = "rs";

	private static final String STR_WRITE_ALL = "w";

	private static final String STR_DELETE_ALL = "d";

	private static final String STR_UNMAPPABLE = "p";

	private static final Rights RIGHTS_FOLDER_ADMIN = new Rights(STR_FOLDER_ADMIN);

	private static final Rights RIGHTS_FOLDER_VISIBLE = new Rights(STR_FOLDER_VISIBLE);

	private static final Rights RIGHTS_FOLDER_CREATE_OBJECTS = new Rights(STR_FOLDER_CREATE_OBJECTS);

	private static final Rights RIGHTS_FOLDER_CREATE_SUBFOLDERS = new Rights(STR_FOLDER_CREATE_SUBFOLDERS);

	private static final Rights RIGHTS_READ_ALL = new Rights(STR_READ_ALL);

	private static final Rights RIGHTS_WRITE_ALL = new Rights(STR_WRITE_ALL);

	private static final Rights RIGHTS_DELETE_ALL = new Rights(STR_DELETE_ALL);

	private static final Rights RIGHTS_UNMAPPABLE = new Rights(STR_UNMAPPABLE);

	public ACL getPermissionACL() throws LdapException {
		if (this.acl != null) {
			/*
			 * Return caches ACL
			 */
			return acl;
		}
		final Rights rights = new Rights();
		boolean hasAnyRights = false;
		if (isFolderAdmin()) {
			rights.add(RIGHTS_FOLDER_ADMIN);
			hasAnyRights = true;
		}
		if (canCreateSubfolders()) {
			rights.add(RIGHTS_FOLDER_CREATE_SUBFOLDERS);
			hasAnyRights = true;
		} else if (canCreateObjects()) {
			rights.add(RIGHTS_FOLDER_CREATE_OBJECTS);
			hasAnyRights = true;
		} else if (isFolderVisible()) {
			rights.add(RIGHTS_FOLDER_VISIBLE);
			hasAnyRights = true;
		}
		if (getReadPermission() >= OCLPermission.READ_ALL_OBJECTS) {
			rights.add(RIGHTS_READ_ALL);
			hasAnyRights = true;
		}
		if (getWritePermission() >= OCLPermission.WRITE_ALL_OBJECTS) {
			rights.add(RIGHTS_WRITE_ALL);
			hasAnyRights = true;
		}
		if (getDeletePermission() >= OCLPermission.DELETE_ALL_OBJECTS) {
			rights.add(RIGHTS_DELETE_ALL);
			hasAnyRights = true;
		}
		if (hasAnyRights) {
			rights.add(RIGHTS_UNMAPPABLE);
		}
		return (acl = new ACL(OXUser2IMAPLogin.getIMAPLogin(getEntity(), ctx), rights));
	}

	public final void parseACL(final ACL acl) throws IMAPException, LdapException {
		this.acl = acl;
		setEntity(OXUser2IMAPLogin.getUserID(acl.getName(), ctx));
		parseRights(acl.getRights());
	}
	
	public final void parseRights(final Rights rights) {
		/*
		 * Folder admin
		 */
		setFolderAdmin(rights.contains(RIGHTS_FOLDER_ADMIN));
		/*
		 * Folder permission
		 */
		if (rights.contains(RIGHTS_FOLDER_CREATE_SUBFOLDERS)) {
			setFolderPermission(OCLPermission.CREATE_SUB_FOLDERS);
		} else if (rights.contains(RIGHTS_FOLDER_CREATE_OBJECTS)) {
			setFolderPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER);
		} else if (rights.contains(RIGHTS_FOLDER_VISIBLE)) {
			setFolderPermission(OCLPermission.READ_FOLDER);
		} else {
			setFolderPermission(OCLPermission.NO_PERMISSIONS);
		}
		/*
		 * Read permission
		 */
		if (rights.contains(RIGHTS_READ_ALL)) {
			setReadObjectPermission(OCLPermission.READ_ALL_OBJECTS);
		} else {
			setReadObjectPermission(OCLPermission.NO_PERMISSIONS);
		}
		/*
		 * Write permission
		 */
		if (rights.contains(RIGHTS_WRITE_ALL)) {
			setWriteObjectPermission(OCLPermission.WRITE_ALL_OBJECTS);
		} else {
			setWriteObjectPermission(OCLPermission.NO_PERMISSIONS);
		}
		/*
		 * Delete permission
		 */
		if (rights.contains(RIGHTS_DELETE_ALL)) {
			setDeleteObjectPermission(OCLPermission.DELETE_ALL_OBJECTS);
		} else {
			setDeleteObjectPermission(OCLPermission.NO_PERMISSIONS);
		}
	}

}
