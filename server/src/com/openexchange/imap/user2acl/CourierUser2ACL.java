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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.mail.config.MailConfig.CredSrc;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link CourierUser2ACL} - Handles the ACL entities used by Courier IMAP
 * server.
 * <p>
 * The current supported identifers are:
 * <ul>
 * <li><i>owner</i></li>
 * <li><i>anyone</i></li>
 * </ul>
 * <p>
 * Missing handling for identifiers:
 * 
 * <ul>
 * <li><i>anonymous</i> (This is a synonym from <i>anyone</i>)</li>
 * <li><i>user=loginid</i> (Rights or negative rights for IMAP account
 * "loginid")</li>
 * <li><i>group=name</i> (Rights or negative rights for account group "name")</li>
 * <li><i>administrators</i> (This is an alias for <i>group=administrators</i>)</li>
 * </ul>
 * <p>
 * The complete implementation should be able to handle an ACL like this one:
 * 
 * <pre>
 * owner aceilrstwx anyone lr user=john w -user=mary r administrators aceilrstwx
 * </pre>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CourierUser2ACL extends User2ACL {

	private static final String ALIAS_OWNER = "owner";

	private static final String ALIAS_ANYONE = "anyone";

	private static final String ABSTRACT_PATTERN = "#shared#DELIM#([\\p{ASCII}&&[^#DELIM#]]+)#DELIM#\\p{ASCII}+";

	private static final String getSharedFolderOwner(final String sharedFolderName, final char delim) {
		final Matcher m = Pattern.compile(ABSTRACT_PATTERN.replaceAll("#DELIM#", String.valueOf(delim)),
				Pattern.CASE_INSENSITIVE).matcher(sharedFolderName);
		if (m.matches()) {
			return m.group(1).replaceAll("\\s+", String.valueOf(delim));
		}
		return null;
	}

	// session-user, fullname & delimiter

	/**
	 * Default constructor
	 */
	public CourierUser2ACL() {
		super();
	}

	@Override
	public String getACLName(final int userId, final Context ctx, final User2ACLArgs user2AclArgs)
			throws AbstractOXException {
		if (userId == OCLPermission.ALL_GROUPS_AND_USERS) {
			return ALIAS_ANYONE;
		}
		final Object[] args = user2AclArgs.getArguments(IMAPServer.COURIER);
		final int sessionUser = ((Integer) args[0]).intValue();
		final String sharedOwner = getSharedFolderOwner((String) args[1], ((Character) args[2]).charValue());
		if (null == sharedOwner) {
			/*
			 * A non-shared folder
			 */
			if (sessionUser == userId) {
				/*
				 * Logged-in user is equal to given user
				 */
				return ALIAS_OWNER;
			}
			return getACLNameInternal(userId, ctx);
		}
		/*
		 * A shared folder
		 */
		final int sharedOwnerID = getUserIDInternal(sharedOwner, ctx);
		if (sharedOwnerID == userId) {
			/*
			 * Owner is equal to given user
			 */
			return ALIAS_OWNER;
		}
		return getACLNameInternal(userId, ctx);
	}

	private final String getACLNameInternal(final int userId, final Context ctx) throws AbstractOXException {
		if (CredSrc.USER_IMAPLOGIN.equals(IMAPConfig.getCredSrc())) {
			return UserStorage.getInstance().getUser(userId, ctx).getImapLogin();
		}
		return UserStorage.getInstance().getUser(userId, ctx).getLoginInfo();
	}

	@Override
	public int getUserID(final String pattern, final Context ctx, final User2ACLArgs user2AclArgs)
			throws AbstractOXException {
		if (ALIAS_ANYONE.equalsIgnoreCase(pattern)) {
			return OCLPermission.ALL_GROUPS_AND_USERS;
		}
		final Object[] args = user2AclArgs.getArguments(IMAPServer.COURIER);
		final int sessionUser = ((Integer) args[0]).intValue();
		final String sharedOwner = getSharedFolderOwner((String) args[1], ((Character) args[2]).charValue());
		if (null == sharedOwner) {
			/*
			 * A non-shared folder
			 */
			if (ALIAS_OWNER.equalsIgnoreCase(pattern)) {
				/*
				 * Map alias "owner" to logged-in user
				 */
				return sessionUser;
			}
			return getUserIDInternal(pattern, ctx);
		}
		/*
		 * A shared folder
		 */
		if (ALIAS_OWNER.equalsIgnoreCase(pattern)) {
			/*
			 * Map alias "owner" to shared folder owner
			 */
			return getUserIDInternal(sharedOwner, ctx);
		}
		return getUserIDInternal(pattern, ctx);
	}

	private final int getUserIDInternal(final String pattern, final Context ctx) throws AbstractOXException {
		if (CredSrc.USER_IMAPLOGIN.equals(IMAPConfig.getCredSrc())) {
			/*
			 * Find user name by user's imap login
			 */
			return UserStorage.getInstance().resolveIMAPLogin(pattern, ctx);
		}
		/*
		 * Find by name
		 */
		return UserStorage.getInstance().getUserId(pattern, ctx);
	}
}
