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

package com.openexchange.imap;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.imap.user2acl.User2ACL;
import com.openexchange.imap.user2acl.User2ACLArgs;
import com.openexchange.mail.MailException;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.SessionObject;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.Rights;

/**
 * {@link ACLPermission} - Maps existing folder permissions to corresponding
 * IMAP ACL
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ACLPermission extends MailPermission {

	private static final long serialVersionUID = -3140342221453395764L;

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ACLPermission.class);

	private transient ACL acl;

	private UserStorage userStorage;

	/**
	 * Constructor
	 * 
	 * @param sessionUser
	 *            The session user
	 */
	public ACLPermission(final SessionObject session) {
		super(session);
	}

	/**
	 * Constructor
	 * 
	 * @param sessionUser
	 *            The session user
	 * @param userStorage
	 *            The user storage
	 */
	public ACLPermission(final SessionObject session, final UserStorage userStorage) {
		super(session);
		this.userStorage = userStorage;
	}

	private UserStorage getUserStorage() throws MailException {
		if (userStorage == null) {
			try {
				userStorage = UserStorage.getInstance(session.getContext());
			} catch (final LdapException e) {
				throw new MailException(e);
			}
		}

		return userStorage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.OCLPermission#setEntity(int)
	 */
	@Override
	public void setEntity(final int entity) {
		super.setEntity(entity);
		this.acl = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.OCLPermission#setFolderAdmin(boolean)
	 */
	@Override
	public void setFolderAdmin(final boolean folderAdmin) {
		super.setFolderAdmin(folderAdmin);
		this.acl = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.OCLPermission#setGroupPermission(boolean)
	 */
	@Override
	public void setGroupPermission(final boolean groupPermission) {
		super.setGroupPermission(groupPermission);
		this.acl = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.OCLPermission#setFolderPermission(int)
	 */
	@Override
	public boolean setFolderPermission(final int p) {
		this.acl = null;
		return super.setFolderPermission(p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.OCLPermission#setReadObjectPermission(int)
	 */
	@Override
	public boolean setReadObjectPermission(final int p) {
		this.acl = null;
		return super.setReadObjectPermission(p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.OCLPermission#setWriteObjectPermission(int)
	 */
	@Override
	public boolean setWriteObjectPermission(final int p) {
		this.acl = null;
		return super.setWriteObjectPermission(p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.OCLPermission#setDeleteObjectPermission(int)
	 */
	@Override
	public boolean setDeleteObjectPermission(final int p) {
		this.acl = null;
		return super.setDeleteObjectPermission(p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.OCLPermission#setAllObjectPermission(int,
	 *      int, int)
	 */
	@Override
	public boolean setAllObjectPermission(final int pr, final int pw, final int pd) {
		this.acl = null;
		return super.setAllObjectPermission(pr, pw, pd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.OCLPermission#setAllPermission(int, int,
	 *      int, int)
	 */
	@Override
	public boolean setAllPermission(final int fp, final int opr, final int opw, final int opd) {
		this.acl = null;
		return super.setAllPermission(fp, opr, opw, opd);
	}

	private static final String ERR = "This method is not applicable to an IMAP permission";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.OCLPermission#setFuid(int)
	 */
	@Override
	public void setFuid(final int pid) {
		if (LOG.isWarnEnabled()) {
			LOG.warn(ERR);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.OCLPermission#getFuid()
	 */
	@Override
	public int getFuid() {
		if (LOG.isWarnEnabled()) {
			LOG.warn(ERR);
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.OCLPermission#reset()
	 */
	@Override
	public void reset() {
		super.reset();
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

	private static final transient Rights RIGHTS_FOLDER_ADMIN = new Rights(STR_FOLDER_ADMIN);

	private static final transient Rights RIGHTS_FOLDER_VISIBLE = new Rights(STR_FOLDER_VISIBLE);

	private static final transient Rights RIGHTS_FOLDER_CREATE_OBJECTS = new Rights(STR_FOLDER_CREATE_OBJECTS);

	private static final transient Rights RIGHTS_FOLDER_CREATE_SUBFOLDERS = new Rights(STR_FOLDER_CREATE_SUBFOLDERS);

	private static final transient Rights RIGHTS_READ_ALL = new Rights(STR_READ_ALL);

	private static final transient Rights RIGHTS_WRITE_ALL = new Rights(STR_WRITE_ALL);

	private static final transient Rights RIGHTS_DELETE_ALL = new Rights(STR_DELETE_ALL);

	private static final transient Rights RIGHTS_UNMAPPABLE = new Rights(STR_UNMAPPABLE);

	/**
	 * Maps this permission to ACL rights and fills them into an
	 * <code>ACL</code> instance
	 * 
	 * @return mapped <code>ACL</code> instance
	 * @throws AbstractOXException
	 */
	public ACL getPermissionACL(final User2ACLArgs user2aclArgs) throws AbstractOXException {
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
		return (acl = new ACL(User2ACL.getInstance(session.getUserObject()).getACLName(getEntity(), getUserStorage(),
				user2aclArgs), rights));
	}

	/**
	 * Parses the rights given through <code>ACL</code> instance into this
	 * permission object
	 * 
	 * @param acl -
	 *            the <code>ACL</code> instance
	 * @throws AbstractOXException
	 */
	public void parseACL(final ACL acl, final User2ACLArgs user2aclArgs) throws AbstractOXException {
		setEntity(User2ACL.getInstance(session.getUserObject())
				.getUserID(acl.getName(), getUserStorage(), user2aclArgs));
		parseRights(acl.getRights());
		this.acl = acl;
	}

	/**
	 * Parses given rights into this permission object
	 * 
	 * @param rights -
	 *            the rights
	 */
	public void parseRights(final Rights rights) {
		rights2Permission(rights, this);
	}

	/**
	 * Parses specified rights into given permission object
	 * 
	 * @param rights
	 *            The rights to parse
	 * @return The corresponding permission
	 */
	public static OCLPermission rights2Permission(final Rights rights) {
		return rights2Permission(rights, new OCLPermission());
	}

	/**
	 * Parses specified rights into given permission object. If the latter
	 * parameter is left to <code>null</code>, a new instance of
	 * {@link OCLPermission} is going to be created, filled, and returned.
	 * Otherwise the given instance of {@link OCLPermission} is filled and
	 * returned.
	 * 
	 * @param rights
	 *            The rights to parse
	 * @param permission
	 *            The permission object which may be <code>null</code>
	 * @return The corresponding permission
	 */
	public static OCLPermission rights2Permission(final Rights rights, final OCLPermission permission) {
		final OCLPermission oclPermission = permission == null ? new OCLPermission() : permission;
		/*
		 * Folder admin
		 */
		oclPermission.setFolderAdmin(rights.contains(RIGHTS_FOLDER_ADMIN));
		/*
		 * Folder permission
		 */
		if (rights.contains(RIGHTS_FOLDER_CREATE_SUBFOLDERS)) {
			oclPermission.setFolderPermission(OCLPermission.CREATE_SUB_FOLDERS);
		} else if (rights.contains(RIGHTS_FOLDER_CREATE_OBJECTS)) {
			oclPermission.setFolderPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER);
		} else if (rights.contains(RIGHTS_FOLDER_VISIBLE)) {
			oclPermission.setFolderPermission(OCLPermission.READ_FOLDER);
		} else {
			oclPermission.setFolderPermission(OCLPermission.NO_PERMISSIONS);
		}
		/*
		 * Read permission
		 */
		if (rights.contains(RIGHTS_READ_ALL)) {
			oclPermission.setReadObjectPermission(OCLPermission.READ_ALL_OBJECTS);
		} else {
			oclPermission.setReadObjectPermission(OCLPermission.NO_PERMISSIONS);
		}
		/*
		 * Write permission
		 */
		if (rights.contains(RIGHTS_WRITE_ALL)) {
			oclPermission.setWriteObjectPermission(OCLPermission.WRITE_ALL_OBJECTS);
		} else {
			oclPermission.setWriteObjectPermission(OCLPermission.NO_PERMISSIONS);
		}
		/*
		 * Delete permission
		 */
		if (rights.contains(RIGHTS_DELETE_ALL)) {
			oclPermission.setDeleteObjectPermission(OCLPermission.DELETE_ALL_OBJECTS);
		} else {
			oclPermission.setDeleteObjectPermission(OCLPermission.NO_PERMISSIONS);
		}
		return oclPermission;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		try {
			final ACLPermission clone = (ACLPermission) super.clone();
			clone.acl = new ACL(acl.getName(), (Rights) acl.getRights().clone());
			return clone;
		} catch (final CloneNotSupportedException e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}
}
