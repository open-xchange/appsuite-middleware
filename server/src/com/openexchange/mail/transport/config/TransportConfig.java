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

package com.openexchange.mail.transport.config;

import java.lang.reflect.InvocationTargetException;

import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.session.Session;

/**
 * {@link TransportConfig} - The user-specific transport configuration
 * <p>
 * Provides access to global transport properties.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class TransportConfig extends MailConfig {

	/**
	 * Default constructor
	 */
	protected TransportConfig() {
		super();
	}

	/**
	 * Gets the user-specific transport configuration
	 * 
	 * @param clazz
	 *            The transport configuration type
	 * @param session
	 *            The session providing needed user data
	 * @return The user-specific transport configuration
	 * @throws MailException
	 *             If user-specific transport configuration cannot be determined
	 */
	public static final <C extends TransportConfig> C getTransportConfig(final Class<? extends C> clazz,
			final Session session) throws MailException {
		final C transportConfig;
		try {
			transportConfig = clazz.getConstructor(CONSTRUCTOR_ARGS).newInstance(INIT_ARGS);
		} catch (final IllegalArgumentException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final SecurityException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final InstantiationException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final IllegalAccessException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final InvocationTargetException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		} catch (final NoSuchMethodException e) {
			throw new MailException(MailException.Code.INSTANTIATION_PROBLEM, e, clazz.getName());
		}
		/*
		 * Fetch user object to determine server URL
		 */
		final User user;
		try {
			user = UserStorage.getStorageUser(session.getUserId(), ContextStorage.getStorageContext(session
					.getContextId()));
		} catch (final ContextException e) {
			throw new MailException(e);
		}
		fillLoginAndPassword(transportConfig, session, user);
		String serverURL = TransportConfig.getTransportServerURL(user);
		if (serverURL == null) {
			if (LoginType.GLOBAL.equals(getLoginType())) {
				throw new MailConfigException(new StringBuilder(128).append("Property \"").append(
						"com.openexchange.mail.transportServer").append("\" not set in mail properties").toString());
			}
			throw new MailConfigException(new StringBuilder(128).append("Cannot determine mail server URL for user ")
					.append(session.getUserId()).append(" in context ").append(session.getContextId()).toString());
		}
		{
			/*
			 * Remove ending '/' character
			 */
			final int lastPos = serverURL.length() - 1;
			if (serverURL.charAt(lastPos) == '/') {
				serverURL = serverURL.substring(0, lastPos);
			}
		}
		transportConfig.parseServerURL(serverURL);
		return transportConfig;
	}

	/**
	 * Gets the transport server URL appropriate to configured login type
	 * 
	 * @param user
	 *            The user
	 * @return The appropriate transport server URL or <code>null</code>
	 */
	public static String getTransportServerURL(final User user) {
		if (LoginType.GLOBAL.equals(getLoginType())) {
			return MailConfig.getTransportServer();
		} else if (LoginType.USER.equals(getLoginType())) {
			return user.getSmtpServer();
		} else if (LoginType.ANONYMOUS.equals(getLoginType())) {
			return user.getSmtpServer();
		}
		return null;
	}

	/**
	 * Gets the transport server URL appropriate to configured login type
	 * 
	 * @param session
	 *            The user session
	 * @return The appropriate transport server URL or <code>null</code>
	 */
	public static String getTransportServerURL(final Session session) {
		return getTransportServerURL(UserStorage.getStorageUser(session.getUserId(), session.getContextId()));
	}

	/**
	 * Gets the referencedPartLimit
	 * 
	 * @return The referencedPartLimit
	 */
	public static int getReferencedPartLimit() {
		return TransportProperties.getInstance().getReferencedPartLimit();
	}

}
