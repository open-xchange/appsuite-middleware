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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.imap.config.IMAPConfig;

/**
 * {@link User2ACL} - Maps numeric entity IDs to corresponding IMAP login name
 * (used in ACLs) and vice versa
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class User2ACL {

	private static final AtomicBoolean instancialized = new AtomicBoolean();

	/**
	 * Singleton
	 */
	private static User2ACL singleton;

	/**
	 * Creates a new instance implementing the {@link User2ACL} interface.
	 * 
	 * @param imapConfig
	 *            The user's IMAP config
	 * @return an instance implementing the {@link User2ACL} interface.
	 * @throws User2ACLException
	 *             if the instance can't be created.
	 */
	public static final User2ACL getInstance(final IMAPConfig imapConfig) throws User2ACLException {
		if (!instancialized.get()) {
			/*
			 * Auto-detect dependent on user's IMAP settings
			 */
			return getUser2ACLImpl(imapConfig.getServer(), imapConfig.getPort());
		}
		return singleton;
	}

	private static final User2ACL getUser2ACLImpl(final String imapServer, final int port) throws User2ACLException {
		try {
			return User2ACLAutoDetector.getUser2ACLImpl(imapServer, port);
		} catch (final IOException e) {
			throw new User2ACLException(User2ACLException.Code.IO_ERROR, e, e.getLocalizedMessage());
		}
	}

	/**
	 * Resets user2acl
	 */
	final static void resetUser2ACL() {
		singleton = null;
		instancialized.set(false);
		User2ACLAutoDetector.resetUser2ACLMappings();
	}

	/**
	 * Only invoked if auto-detection is turned off
	 * 
	 * @param singleton
	 *            The singleton instance of {@link User2ACL}
	 */
	final static void setInstance(final User2ACL singleton) {
		User2ACL.singleton = singleton;
		instancialized.set(true);
	}

	protected User2ACL() {
		super();
	}

	/**
	 * Determines the entity name of the user whose ID matches given
	 * <code>userId</code> that is used in IMAP server's ACL list.
	 * 
	 * @param userId -
	 *            the user ID
	 * @param ctx -
	 *            the context
	 * @param user2AclArgs -
	 *            the arguments container
	 * @return the IMAP login of the user whose ID matches given
	 *         <code>userId</code>
	 * @throws AbstractOXException -
	 *             if user could not be found
	 */
	public abstract String getACLName(int userId, Context ctx, User2ACLArgs user2AclArgs) throws AbstractOXException;

	/**
	 * Determines the user ID whose either ACL entity name or user name matches
	 * given <code>pattern</code>.
	 * 
	 * @param pattern -
	 *            the pattern for either IMAP login or user name
	 * @param ctx -
	 *            the context
	 * @param user2AclArgs -
	 *            the arguments container
	 * @return the user ID whose IMAP login matches given <code>pattern</code>
	 * @throws AbstractOXException -
	 *             if user search fails
	 */
	public abstract int getUserID(final String pattern, Context ctx, User2ACLArgs user2AclArgs)
			throws AbstractOXException;

}
