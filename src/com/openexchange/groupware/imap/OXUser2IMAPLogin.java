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

package com.openexchange.groupware.imap;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.SessionObject;

/**
 * OXUser2IMAPLogin - Maps an user ID to his IMAP login and vice versa
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OXUser2IMAPLogin {

	private OXUser2IMAPLogin() {
		super();
	}

	/**
	 * Determines the IMAP login of the user whose ID matches given
	 * <code>userId</code>.
	 * 
	 * @param userId -
	 *            the user ID
	 * @param ctx -
	 *            the context
	 * @return the IMAP login of the user whose ID matches given
	 *         <code>userId</code>
	 * @throws LdapException
	 */
	public static String getIMAPLogin(final int userId, final Context ctx) throws LdapException {
		final UserStorage us = UserStorage.getInstance(ctx);
		return us.getUser(userId).getLoginInfo();
	}

	/**
	 * Determines the IMAP login of the user whose ID matches given
	 * <code>userId</code>.
	 * 
	 * @param userId -
	 *            the user ID
	 * @param userStorage -
	 *            associated user storage implementation
	 * @return the IMAP login of the user whose ID matches given
	 *         <code>userId</code>
	 * @throws LdapException
	 */
	public static String getIMAPLogin(final int userId, final UserStorage userStorage) throws LdapException {
		if (userId == OCLPermission.ALL_GROUPS_AND_USERS) {
			return AUTH_ID_ANYONE;
		}
		return userStorage.getUser(userId).getLoginInfo();
	}

	/**
	 * Determines IMAP login for session-associated user. If
	 * <code>lookUpIMAPLogin</code> is <code>true</code>, this routine
	 * tries to fetch the IMAP login from <code>User.getImapLogin()</code> and
	 * falls back to session-supplied user login info. Otherwise
	 * session-supplied user login info is directly taken as return value.
	 * 
	 * @param session -
	 *            the user's session
	 * @param lookUpIMAPLogin -
	 *            determines whether to look up <code>User.getImapLogin()</code>
	 *            or not
	 * @return current user's IMAP login
	 */
	public static String getLocalIMAPLogin(final SessionObject session, final boolean lookUpIMAPLogin) {
		String imapLogin = lookUpIMAPLogin ? session.getUserObject().getImapLogin() : null;
		if (imapLogin == null || imapLogin.length() == 0) {
			imapLogin = session.getUserlogin() != null && session.getUserlogin().length() > 0 ? session.getUserlogin()
					: session.getUsername();
		}
		return imapLogin;
	}

	/**
	 * Determines the user ID whose IMAP login mtaches given
	 * <code>imapLogin</code>. <b>NOTE:</b> this routine returns
	 * <code>-1</code> if ACLs are not supported by underlying IMAP server or
	 * if ACLs are disabled per config file (imap.properties).
	 * 
	 * @param imapLogin -
	 *            the IMAP login
	 * @param ctx -
	 *            the context
	 * @return the user ID whose IMAP login mtaches given <code>imapLogin</code>
	 * @throws IMAPException
	 * @throws LdapException
	 */
	public static int getUserID(final String imapLogin, final Context ctx) throws IMAPException, LdapException {
		if (!IMAPProperties.isSupportsACLs()) {
			return -1;
		}
		final UserStorage us = UserStorage.getInstance(ctx);
		return us.getUserId(imapLogin);
	}
	
	private static final String AUTH_ID_ANYONE = "anyone";

	/**
	 * Determines the user ID whose IMAP login mtaches given
	 * <code>imapLogin</code>. <b>NOTE:</b> this routine returns
	 * <code>-1</code> if ACLs are not supported by underlying IMAP server or
	 * if ACLs are disabled per config file (imap.properties).
	 * 
	 * @param imapLogin -
	 *            the IMAP login
	 * @param userStorage -
	 *            the associated user storage implementation
	 * @return the user ID whose IMAP login mtaches given <code>imapLogin</code>
	 * @throws IMAPException
	 * @throws LdapException
	 */
	public static int getUserID(final String imapLogin, final UserStorage userStorage) throws IMAPException,
			LdapException {
		if (!IMAPProperties.isSupportsACLs()) {
			return -1;
		} else if (AUTH_ID_ANYONE.equalsIgnoreCase(imapLogin)) {
			return OCLPermission.ALL_GROUPS_AND_USERS;
		}
		return userStorage.getUserId(imapLogin);
	}

}
