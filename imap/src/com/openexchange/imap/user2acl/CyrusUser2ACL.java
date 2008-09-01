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

package com.openexchange.imap.user2acl;

import static com.openexchange.imap.services.IMAPServiceRegistry.getServiceRegistry;
import static com.openexchange.mail.utils.ProviderUtility.toSocketAddr;

import java.net.InetSocketAddress;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailConfig.CredSrc;
import com.openexchange.mail.api.MailConfig.LoginType;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.user.UserService;

/**
 * {@link CyrusUser2ACL} - Handles the ACL entities used by Cyrus IMAP server.
 * <p>
 * The current supported identifiers are:
 * <ul>
 * <li><i>anyone</i> which refers to all users, including the anonymous user</li>
 * </ul>
 * <p>
 * Missing handling for identifiers:
 * <ul>
 * <li><i>anonymous</i> which refers to the anonymous, or unauthenticated user</li>
 * </ul>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CyrusUser2ACL extends User2ACL {

	private static final String AUTH_ID_ANYONE = "anyone";

	/**
	 * Default constructor
	 */
	public CyrusUser2ACL() {
		super();
	}

	@Override
	public String getACLName(final int userId, final Context ctx, final User2ACLArgs user2AclArgs)
			throws AbstractOXException {
		if (userId == OCLPermission.ALL_GROUPS_AND_USERS) {
			return AUTH_ID_ANYONE;
		}
		final UserService userService = getServiceRegistry().getService(UserService.class, true);
		if (LoginType.USER.equals(MailConfig.getLoginType()) && CredSrc.USER_IMAPLOGIN.equals(MailConfig.getCredSrc())) {
			return userService.getUser(userId, ctx).getImapLogin();
		}
		return userService.getUser(userId, ctx).getLoginInfo();
	}

	@Override
	public int getUserID(final String pattern, final Context ctx, final User2ACLArgs user2AclArgs)
			throws AbstractOXException {
		if (AUTH_ID_ANYONE.equalsIgnoreCase(pattern)) {
			return OCLPermission.ALL_GROUPS_AND_USERS;
		}
		final UserService userService = getServiceRegistry().getService(UserService.class, true);
		if (LoginType.USER.equals(MailConfig.getLoginType()) && CredSrc.USER_IMAPLOGIN.equals(MailConfig.getCredSrc())) {
			/*
			 * Find user name by user's imap login
			 */
			final int[] ids = userService.resolveIMAPLogin(pattern, ctx);
			if (ids.length == 1) {
				return ids[0];
			}
			final Object[] args = user2AclArgs.getArguments(IMAPServer.CYRUS);
			if (args == null || args.length == 0) {
				throw new User2ACLException(User2ACLException.Code.MISSING_ARG);
			}
			final InetSocketAddress imapAddr;
			try {
				imapAddr = (InetSocketAddress) args[0];
			} catch (final ClassCastException e) {
				throw new User2ACLException(User2ACLException.Code.MISSING_ARG, e, new Object[0]);
			}
			for (final int id : ids) {
				if (imapAddr.equals(toSocketAddr(MailConfig.getMailServerURL(userService.getUser(id, ctx)), 143))) {
					return id;
				}
			}
			throw new User2ACLException(User2ACLException.Code.RESOLVE_USER_FAILED, pattern);
		}
		/*
		 * Find by name
		 */
		return userService.getUserId(pattern, ctx);
	}

}
