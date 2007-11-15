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

package com.openexchange.groupware.userconfiguration;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.Initialization;

/**
 * UserConfigurationStorage
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class UserConfigurationStorage {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(UserConfigurationStorage.class);

	private static UserConfigurationStorage singleton;

	private boolean started;

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
	 * @deprecated Use common {@link Initialization#start()}/{@link Initialization#stop()}
	 *             on singleton instance instead
	 */
	public static final void init() throws UserConfigurationException {
		try {
			UserConfigurationStorageInit.getInstance().start();
		} catch (final UserConfigurationException e) {
			throw e;
		} catch (final AbstractOXException e) {
			throw new UserConfigurationException(e);
		}
	}

	/**
	 * Sets the singleton instance of {@link UserConfigurationStorage}
	 * 
	 * @param singleton
	 *            The singleton instance
	 * @throws AbstractOXException
	 *             If singleton cannot be configured
	 */
	static void setInstance(final UserConfigurationStorage singleton) throws AbstractOXException {
		UserConfigurationStorage.singleton = singleton;
		UserConfigurationStorage.singleton.start();
	}

	/**
	 * Factory method for an instance of UserConfigurationStorage.
	 * 
	 * @return an instance implementing the
	 *         <code>UserConfigurationStorage</code> interface
	 */
	public static final UserConfigurationStorage getInstance() {
		return singleton;
	}

	public final void start() throws AbstractOXException {
		if (started) {
			return;
		}
		startInternal();
		started = true;
	}

	public final void stop() throws AbstractOXException {
		if (!started) {
			return;
		}
		stopInternal();
		singleton = null;
		started = false;
	}

	/**
	 * A convenience method that invokes
	 * {@link #getUserConfigurationSafe(int, int[], Context)} with the group
	 * parameter set to <code>null</code>
	 * 
	 * @param userId
	 *            The user ID
	 * @param ctx
	 *            The context
	 * @return The corresponding instance of {@link UserConfiguration} or
	 *         <code>null</code> on exception
	 * @see #getUserConfigurationSafe(int, int[], Context)
	 */
	public final UserConfiguration getUserConfigurationSafe(final int userId, final Context ctx) {
		return getUserConfigurationSafe(userId, null, ctx);
	}

	/**
	 * A convenience method that invokes
	 * {@link #getUserConfiguration(int, int[], Context)}. If an exception
	 * occurs <code>null</code> is returned
	 * 
	 * @param userId
	 *            The user ID
	 * @param groups
	 *            The user's groups
	 * @param ctx
	 *            The contexts
	 * @return The corresponding instance of {@link UserConfiguration} or
	 *         <code>null</code> on exception
	 */
	public final UserConfiguration getUserConfigurationSafe(final int userId, final int[] groups, final Context ctx) {
		try {
			return UserConfigurationStorage.getInstance().getUserConfiguration(userId, groups, ctx);
		} catch (final UserConfigurationException e) {
			LOG.error(e.getLocalizedMessage(), e);
			return null;
		}
	}

	/**
	 * Determines the instance of <code>UserConfiguration</code> that
	 * corresponds to given user ID.
	 * 
	 * @param userId -
	 *            the user ID
	 * @param ctx -
	 *            the context
	 * @return the instance of <code>UserConfiguration</code>
	 * @throws UserConfigurationException -
	 *             if user's configuration could not be determined
	 * @see #getUserConfiguration(int, int[], Context)
	 */
	public final UserConfiguration getUserConfiguration(final int userId, final Context ctx)
			throws UserConfigurationException {
		return getUserConfiguration(userId, null, ctx);
	}

	/**
	 * Perform necessary actions to start instance
	 * 
	 * @throws AbstractOXException
	 */
	protected abstract void startInternal() throws AbstractOXException;

	/**
	 * Perform necessary actions to stop instance
	 * 
	 * @throws AbstractOXException
	 */
	protected abstract void stopInternal() throws AbstractOXException;

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
