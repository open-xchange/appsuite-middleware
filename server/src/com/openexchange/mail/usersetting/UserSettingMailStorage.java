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

package com.openexchange.mail.usersetting;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicBoolean;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;

/**
 * {@link UserSettingMailStorage} - Access to {@link UserSettingMail}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class UserSettingMailStorage {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(UserSettingMailStorage.class);

	private static UserSettingMailStorage singleton;

	private static final AtomicBoolean initialized = new AtomicBoolean();

	/**
	 * Default constructor
	 */
	protected UserSettingMailStorage() {
		super();
	}

	/**
	 * Gets the singleton instance of {@link UserSettingMailStorage}
	 * 
	 * @return The singleton instance of {@link UserSettingMailStorage}
	 */
	public static final UserSettingMailStorage getInstance() {
		if (!initialized.get()) {
			synchronized (initialized) {
				if (null == singleton) {
					singleton = new CachingUserSettingMailStorage();
					initialized.set(true);
				}
			}
		}
		return singleton;
	}

	/**
	 * Releases this storage instance
	 */
	public static final void releaseInstance() {
		if (initialized.get()) {
			synchronized (initialized) {
				if (null != singleton) {
					singleton.shutdownStorage();
					singleton = null;
					initialized.set(false);
				}
			}
		}
	}

	/**
	 * A convenience method that returns
	 * {@link #getUserSettingMail(int, Context, Connection)} with the connection
	 * parameter set to <code>null</code>.
	 * 
	 * @param user
	 *            The user ID
	 * @param cid
	 *            The context ID
	 * @return The instance of {@link UserSettingMail} which matches given user
	 *         ID and context or <code>null</code> on exception
	 * @throws UserConfigurationException If context cannot be loaded
	 */
	public final UserSettingMail getUserSettingMail(final int user, final int cid) throws UserConfigurationException {
		try {
			return getUserSettingMail(user, ContextStorage.getStorageContext(cid), null);
		} catch (final ContextException e) {
			throw new UserConfigurationException(e);
		}
	}

	/**
	 * A convenience method that returns
	 * {@link #getUserSettingMail(int, Context, Connection)} with the connection
	 * parameter set to <code>null</code>.
	 * 
	 * @param user
	 *            The user ID
	 * @param ctx
	 *            The context
	 * @return The instance of {@link UserSettingMail} which matches given user
	 *         ID and context or <code>null</code> on exception
	 */
	public final UserSettingMail getUserSettingMail(final int user, final Context ctx) {
		return getUserSettingMail(user, ctx, null);
	}

	/**
	 * A convenience method that returns
	 * {@link #loadUserSettingMail(int, Context, Connection)}. If an exception
	 * is thrown in delegated method <code>null</code> is returned.
	 * 
	 * @param user
	 *            The user ID
	 * @param ctx
	 *            The context
	 * @param readCon
	 *            The readable connection (may be <code>null</code>)
	 * @return The instance of {@link UserSettingMail} which matches given user
	 *         ID and context or <code>null</code> on exception
	 */
	public final UserSettingMail getUserSettingMail(final int user, final Context ctx, final Connection readCon) {
		try {
			return loadUserSettingMail(user, ctx, readCon);
		} catch (final UserConfigurationException e) {
			LOG.error(e.getLocalizedMessage(), e);
			return null;
		}
	}

	/**
	 * Saves given user's mail settings to database
	 * 
	 * @param usm
	 *            the user's mail settings to save
	 * @param user
	 *            the user ID
	 * @param ctx
	 *            the context
	 * @throws UserConfigurationException
	 *             if user's mail settings could not be saved
	 */
	public final void saveUserSettingMail(final UserSettingMail usm, final int user, final Context ctx)
			throws UserConfigurationException {
		saveUserSettingMail(usm, user, ctx, null);
	}

	/**
	 * Saves given user's mail settings to database
	 * 
	 * @param usm
	 *            the user's mail settings to save
	 * @param user
	 *            the user ID
	 * @param ctx
	 *            the context
	 * @param writeConArg -
	 *            the writable connection; may be <code>null</code>
	 * @throws UserConfigurationException
	 *             if user's mail settings could not be saved
	 */
	public abstract void saveUserSettingMail(final UserSettingMail usm, final int user, final Context ctx,
			final Connection writeConArg) throws UserConfigurationException;

	/**
	 * Deletes the user's mail settings from database
	 * 
	 * @param user
	 *            the user ID
	 * @param ctx
	 *            the context
	 * @throws UserConfigurationException
	 *             if deletion fails
	 */
	public final void deleteUserSettingMail(final int user, final Context ctx) throws UserConfigurationException {
		deleteUserSettingMail(user, ctx, null);
	}

	/**
	 * Deletes the user's mail settings from database
	 * 
	 * @param user
	 *            the user ID
	 * @param ctx
	 *            the context
	 * @param writeConArg
	 *            the writable connection; may be <code>null</code>
	 * @throws UserConfigurationException -
	 *             if deletion fails
	 */
	public abstract void deleteUserSettingMail(final int user, final Context ctx, final Connection writeConArg)
			throws UserConfigurationException;

	/**
	 * Loads user's mail settings from database
	 * 
	 * @param user
	 *            the user
	 * @param ctx
	 *            the context
	 * @return The instance of {@link UserSettingMail} which matches given user
	 *         ID and context
	 * @throws UserConfigurationException
	 *             if loading fails
	 */
	public final UserSettingMail loadUserSettingMail(final int user, final Context ctx)
			throws UserConfigurationException {
		return loadUserSettingMail(user, ctx, null);
	}

	/**
	 * Loads user's mail settings from database
	 * 
	 * @param user
	 *            the user
	 * @param ctx
	 *            the context
	 * @param readConArg
	 *            the readable connection
	 * @return The instance of {@link UserSettingMail} which matches given user
	 *         ID and context
	 * @throws UserConfigurationException
	 *             if loading fails
	 */
	public abstract UserSettingMail loadUserSettingMail(final int user, final Context ctx, final Connection readConArg)
			throws UserConfigurationException;

	/**
	 * Removes the user's mail settings from cache if any used
	 * 
	 * @param user
	 *            the user
	 * @param ctx
	 *            the context
	 * @throws UserConfigurationException
	 *             if cache removal fails
	 */
	public abstract void removeUserSettingMail(final int user, final Context ctx) throws UserConfigurationException;

	/**
	 * Clears this storage's cache if any used
	 * 
	 * @throws UserConfigurationException
	 *             if cache clearing fails
	 */
	public abstract void clearStorage() throws UserConfigurationException;

	/**
	 * Triggers necessary action to shutdown the storage
	 */
	public abstract void shutdownStorage();
}
