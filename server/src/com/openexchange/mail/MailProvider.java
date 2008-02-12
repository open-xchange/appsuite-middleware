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

package com.openexchange.mail;

import java.util.concurrent.atomic.AtomicBoolean;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.mail.config.GlobalMailConfig;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.mime.DefaultHeaderLoader;
import com.openexchange.mail.mime.MIMEHeaderLoader;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.session.Session;

/**
 * {@link MailProvider} - The main intention of the provider class is to make
 * the implementing classes available which define the abstract classes of mail
 * API.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class MailProvider {

	private static final AtomicBoolean initialized = new AtomicBoolean();

	private static MailProvider instance;

	/**
	 * Initializes the mail provider
	 * 
	 * @throws MailException
	 *             If initialization of mail provider fails
	 */
	static void initMailProvider() throws MailException {
		if (!initialized.get()) {
			synchronized (initialized) {
				if (!initialized.get()) {
					final String className = SystemConfig.getProperty(SystemConfig.Property.MailProvider);
					try {
						if (className == null) {
							throw new MailConfigException("Missing mail provider");
						}
						instance = Class.forName(className).asSubclass(MailProvider.class).newInstance();
						initialized.set(true);
					} catch (final ClassNotFoundException e) {
						throw new MailException(MailException.Code.INITIALIZATION_PROBLEM, e, new Object[0]);
					} catch (final InstantiationException e) {
						throw new MailException(MailException.Code.INITIALIZATION_PROBLEM, e, new Object[0]);
					} catch (final IllegalAccessException e) {
						throw new MailException(MailException.Code.INITIALIZATION_PROBLEM, e, new Object[0]);
					}
				}
			}
		}
	}

	/**
	 * Resets the mail provider
	 */
	static void resetMailProvider() {
		initialized.set(false);
	}

	/**
	 * Returns the singleton instance of mail provider
	 * 
	 * @return The singleton instance of mail provider
	 */
	public static final MailProvider getInstance() {
		return instance;
	}

	/**
	 * Initializes a new {@link MailProvider}
	 */
	protected MailProvider() {
		super();
	}

	/**
	 * Gets the name of the class implementing {@link MailConnection}
	 * 
	 * @return The name of the class implementing {@link MailConnection}
	 */
	public abstract String getMailConnectionClass();

	/**
	 * Gets the implementation-specific class name of inheriting
	 * {@link GlobalMailConfig} class.
	 * 
	 * @return The class name of inheriting {@link GlobalMailConfig}
	 *         implementation
	 */
	public abstract String getGlobalMailConfigClass();

	/**
	 * Gets the implementation-specific name of {@link MailPermission}
	 * implementation.
	 * <p>
	 * Returns {@link DefaultMailPermission} class name if mailing system does
	 * not support permission(s).
	 * 
	 * @return The name of inheriting {@link MailPermission} implementation
	 */
	public String getMailPermissionClass() {
		return DefaultMailPermission.class.getName();
	}

	/**
	 * Gets the implementation-specific class name of inheriting
	 * {@link MIMEHeaderLoader} class.
	 * <p>
	 * Returns {@link DefaultHeaderLoader} class name if no specific header
	 * loader is needed.
	 * 
	 * @return The class name of inheriting {@link MIMEHeaderLoader}
	 *         implementation
	 */
	public String getHeaderLoaderClass() {
		return DefaultHeaderLoader.class.getName();
	}

	/**
	 * Gets the user-specific mail configuration with properly set login and
	 * password
	 * 
	 * @param session
	 *            The session providing needed user data
	 * @throws MailException
	 *             If user-specific mail configuration cannot be obtained
	 * @return The user-specific mail configuration with properly set login and
	 *         password
	 */
	public abstract MailConfig getMailConfig(Session session) throws MailException;
}
