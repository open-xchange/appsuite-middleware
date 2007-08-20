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

package com.openexchange.groupware;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.configuration.SystemConfig.Property;
import com.openexchange.groupware.UserConfigurationException.UserConfigurationCode;
import com.openexchange.groupware.contexts.Context;

/**
 * UserConfigurationStorage
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class UserConfigurationStorage {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(UserConfigurationStorage.class);

	private static enum UserConfigurationImpl {

		/**
		 * Caching
		 */
		CACHING("Caching", "com.openexchange.groupware.CachingUserConfigurationStorage"),
		/**
		 * Database
		 */
		DB("DB", "com.openexchange.groupware.RdbUserConfigurationStorage");

		private final String alias;

		private final String impl;

		private UserConfigurationImpl(final String alias, final String impl) {
			this.alias = alias;
			this.impl = impl;
		}

		public String getAlias() {
			return alias;
		}

		public String getImpl() {
			return impl;
		}
	}

	private static final String getUserConfigurationImpl(final String alias) {
		final UserConfigurationImpl[] arr = UserConfigurationImpl.values();
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].alias.equalsIgnoreCase(alias)) {
				return arr[i].impl;
			}
		}
		return null;
	}

	private static final Lock INIT_LOCK = new ReentrantLock();

	/**
	 * Proxy attribute for the class implementing this interface.
	 */
	private static Class<? extends UserConfigurationStorage> implementingClass;

	private static UserConfigurationStorage singleton;

	private static boolean initialized;

	/**
	 * Default constructor
	 */
	protected UserConfigurationStorage() {
		super();
	}

	/**
	 * Initializes the user configuration storage implementation.
	 * 
	 * @throws UserConfigurationException
	 *             if initialization fails.
	 */
	public static void init() throws UserConfigurationException {
		INIT_LOCK.lock();
		try {
			if (null != implementingClass) {
				return;
			}
			final String classNameProp = SystemConfig.getProperty(Property.USER_CONF_STORAGE);
			if (null == classNameProp) {
				throw new UserConfigurationException(UserConfigurationCode.MISSING_SETTING, Property.USER_CONF_STORAGE
						.getPropertyName());
			}
			try {
				final String className = getUserConfigurationImpl(classNameProp);
				implementingClass = Class.forName(className == null ? classNameProp : className).asSubclass(
						UserConfigurationStorage.class);
				if (LOG.isInfoEnabled()) {
					LOG.info("UserConfigurationStorage implementation: " + implementingClass.getName());
				}
			} catch (final ClassNotFoundException e) {
				throw new UserConfigurationException(UserConfigurationCode.CLASS_NOT_FOUND, e, classNameProp);
			} catch (final ClassCastException e) {
				throw new UserConfigurationException(UserConfigurationCode.CLASS_NOT_FOUND, e, classNameProp);
			}
		} finally {
			INIT_LOCK.unlock();
		}
	}

	/**
	 * Factory method for an instance of UserConfigurationStorage.
	 * 
	 * @return an instance implementing the
	 *         <code>UserConfigurationStorage</code> interface
	 * @throws UserConfigurationException
	 *             if instantiation fails.
	 */
	public static UserConfigurationStorage getInstance() throws UserConfigurationException {
		if (!initialized) {
			INIT_LOCK.lock();
			try {
				if (singleton == null) {
					try {
						init();
						singleton = implementingClass.newInstance();
						initialized = true;
					} catch (final InstantiationException e) {
						throw new UserConfigurationException(UserConfigurationCode.INSTANTIATION_FAILED, e);
					} catch (final IllegalAccessException e) {
						throw new UserConfigurationException(UserConfigurationCode.INSTANTIATION_FAILED, e);
					}
				}
			} finally {
				INIT_LOCK.unlock();
			}
		}
		return singleton;
	}

	/**
	 * Determines the instance of <code>UserConfiguration</code> that
	 * corresponds to given user ID
	 * 
	 * @param userId -
	 *            the user ID
	 * @param ctx -
	 *            the context
	 * @return the instance of <code>UserConfiguration</code>
	 * @throws UserConfigurationException -
	 *             if user's configuration could not be determined
	 */
	public abstract UserConfiguration getUserConfiguration(int userId, Context ctx) throws UserConfigurationException;

	/**
	 * Determines the instance of <code>UserConfiguration</code> that
	 * corresponds to given user ID. If <code>groups</code> argument is set,
	 * user's groups need not to be loaded from user storage
	 * 
	 * @param userId -
	 *            the user ID
	 * @param groups -
	 *            user's groups
	 * @param ctx -
	 *            the context
	 * @return the instance of <code>UserConfiguration</code>
	 * @throws UserConfigurationException -
	 *             if user's configuration could not be determined
	 */
	public abstract UserConfiguration getUserConfiguration(int userId, int[] groups, Context ctx)
			throws UserConfigurationException;

	/**
	 * <p>
	 * Clears the whole storage. All kept instances of
	 * <code>UserConfiguration</code> are going to be removed from storage.
	 * <p>
	 * <b>NOTE:</b> Only the instances are going to be removed from storage;
	 * underlying database is not affected
	 * 
	 * @throws UserConfigurationException
	 */
	public abstract void clearStorage() throws UserConfigurationException;

	/**
	 * <p>
	 * Removes the instance of <code>UserConfiguration</code> that corresponds
	 * to given user ID from storage.
	 * <p>
	 * <b>NOTE:</b> Only the instance is going to be removed from storage;
	 * underlying database is not affected
	 * 
	 * @param userId -
	 *            the user ID
	 * @param ctx -
	 *            the context
	 * @throws UserConfigurationException -
	 *             if removal fails
	 */
	public abstract void removeUserConfiguration(int userId, Context ctx) throws UserConfigurationException;

}
