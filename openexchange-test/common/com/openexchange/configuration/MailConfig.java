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

package com.openexchange.configuration;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.tools.conf.AbstractConfig;

/**
 * {@link MailConfig}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailConfig extends AbstractConfig {

	/**
	 * Enumeration of all properties in the ajax.properties file.
	 */
	public static enum Property {
		/**
		 * Server host.
		 */
		SERVER("server"),
		/**
		 * port
		 */
		PORT("port"),
		/**
		 * User login
		 */
		LOGIN("login"),
		/**
		 * Password
		 */
		PASSWORD("password"),
		/**
		 * User ID
		 */
		USER("user"),
		/**
		 * Context ID
		 */
		CONTEXT("cid"),
		/**
		 * Directory which contains test mails (rfc 822 files)
		 */
		TEST_MAIL_DIR("testMailDir"),
		/**
		 * The second login
		 */
		SECOND_LOGIN("secondlogin");

		/**
		 * Name of the property in the ajax.properties file.
		 */
		private String propertyName;

		/**
		 * Default constructor.
		 * 
		 * @param propertyName
		 *            Name of the property in the ajax.properties file.
		 */
		private Property(final String propertyName) {
			this.propertyName = propertyName;
		}

		/**
		 * @return the propertyName
		 */
		public String getPropertyName() {
			return propertyName;
		}
	}

	private static final TestConfig.Property KEY = TestConfig.Property.MAIL_PROPS;

	private static final Lock LOCK_INIT = new ReentrantLock();

	private static boolean initialized;

	private static MailConfig singleton;

	/**
	 * Default constructor
	 */
	public MailConfig() {

	}

	@Override
	protected String getPropertyFileName() throws ConfigurationException {
		final String fileName = TestConfig.getProperty(KEY);
		if (null == fileName) {
			throw new ConfigurationException(ConfigurationException.Code.PROPERTY_MISSING, KEY.getPropertyName());
		}
		return fileName;
	}

	/**
	 * Reads the mail configuration.
	 * 
	 * @throws ConfigurationException
	 *             if reading configuration fails.
	 */
	public static void init() throws ConfigurationException {
		TestConfig.init();
		if (!initialized) {
			LOCK_INIT.lock();
			try {
				if (null == singleton) {
					singleton = new MailConfig();
					singleton.loadPropertiesInternal();
				}
			} finally {
				LOCK_INIT.unlock();
			}
		}
	}

	public static String getProperty(final Property key) {
		if (!initialized) {
			try {
				init();
			} catch (final ConfigurationException e) {
				return null;
			}
		}
		return singleton.getPropertyInternal(key.getPropertyName());
	}

}
