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

package com.openexchange.imap.user2imap;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.imap.IMAPProperties;
import com.openexchange.imap.IMAPPropertiesFactory.IMAPCredSrc;
import com.openexchange.server.OCLPermission;

/**
 * <p>
 * CyrusUser2IMAP - Handles the ACL entities used by Cyrus IMAP server. The
 * current supported identifers are: <i>anyone</i>. Missing handling for
 * identifiers: <i>anonymous</i>
 * <p>
 * The identifier <i>anonymous</i> refers to the anonymous, or unauthenticated
 * user. The identifier <i>anyone</i> refers to all users, including the
 * anonymous user.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CyrusUser2IMAP extends User2IMAP {

	private static final String AUTH_ID_ANYONE = "anyone";

	/**
	 * Default constructor
	 */
	public CyrusUser2IMAP() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.User2IMAP#getIMAPLogin(int,
	 *      com.openexchange.groupware.contexts.Context)
	 */
	@Override
	public String getACLName(final int userId, final Context ctx, final User2IMAPInfo user2IMAPInfo)
			throws AbstractOXException {
		return getACLName(userId, UserStorage.getInstance(ctx), user2IMAPInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.User2IMAP#getIMAPLogin(int,
	 *      com.openexchange.groupware.ldap.UserStorage)
	 */
	@Override
	public String getACLName(final int userId, final UserStorage userStorage, final User2IMAPInfo user2IMAPInfo)
			throws AbstractOXException {
		if (userId == OCLPermission.ALL_GROUPS_AND_USERS) {
			return AUTH_ID_ANYONE;
		} else if (IMAPCredSrc.USER_IMAPLOGIN.equals(IMAPProperties.getImapCredSrc())) {
			return userStorage.getUser(userId).getImapLogin();
		}
		return userStorage.getUser(userId).getLoginInfo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.imap.User2IMAP#getUserID(java.lang.String,
	 *      com.openexchange.groupware.ldap.UserStorage)
	 */
	@Override
	public int getUserID(final String pattern, final UserStorage userStorage, final User2IMAPInfo user2IMAPInfo)
			throws AbstractOXException {
		if (!IMAPProperties.isSupportsACLs()) {
			return -1;
		} else if (AUTH_ID_ANYONE.equalsIgnoreCase(pattern)) {
			return OCLPermission.ALL_GROUPS_AND_USERS;
		} else if (IMAPCredSrc.USER_IMAPLOGIN.equals(IMAPProperties.getImapCredSrc())) {
			/*
			 * Find user name by user's imap login
			 */
			return userStorage.resolveIMAPLogin(pattern);
		}
		/*
		 * Find by name
		 */
		return userStorage.getUserId(pattern);
	}

}
