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

package com.openexchange.imap;

import java.sql.Connection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * UserSettingMailStorage
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class UserSettingMailStorage {

	private static final Lock INIT_LOCK = new ReentrantLock();

	private static UserSettingMailStorage singleton;

	private static boolean initialized;

	/**
	 * Default constructor
	 */
	protected UserSettingMailStorage() {
		super();
	}

	/**
	 * @return an instance of <code>UserSettingMailStorage</code>
	 */
	public static final UserSettingMailStorage getInstance() {
		if (!initialized) {
			INIT_LOCK.lock();
			try {
				if (null == singleton) {
					singleton = new CachingUserSettingMailStorage();
					initialized = true;
				}
			} finally {
				INIT_LOCK.unlock();
			}
		}
		return singleton;
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
	 * @throws OXException
	 *             if user's mail settings could not be saved
	 */
	public final void saveUserSettingMail(final UserSettingMail usm, final int user, final Context ctx)
			throws OXException {
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
	 *            the writeable conenction; may be <code>null</code>
	 * @throws OXException
	 *             if user's mail settings could not be saved
	 */
	public abstract void saveUserSettingMail(final UserSettingMail usm, final int user, final Context ctx,
			final Connection writeConArg) throws OXException;

	/**
	 * Deletes the user's mail settings from database
	 * 
	 * @param user
	 *            the user ID
	 * @param ctx
	 *            the context
	 * @throws OXException
	 *             if deletion fails
	 */
	public final void deleteUserSettingMail(final int user, final Context ctx) throws OXException {
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
	 *            the writeable connection; may be <code>null</code>
	 * @throws OXException -
	 *             if deletion fails
	 */
	public abstract void deleteUserSettingMail(final int user, final Context ctx, final Connection writeConArg)
			throws OXException;

	/**
	 * Loads user's mail settings from database
	 * 
	 * @param usm
	 *            the user's mail settings to fill
	 * @param user
	 *            the user
	 * @param ctx
	 *            the context
	 * @throws OXException
	 *             if loading fails
	 */
	public final UserSettingMail loadUserSettingMail(final int user, final Context ctx) throws OXException {
		return loadUserSettingMail(user, ctx, null);
	}

	/**
	 * Loads user's mail settings from database
	 * 
	 * @param usm
	 *            the user's mail settings to fill
	 * @param user
	 *            the user
	 * @param ctx
	 *            the context
	 * @param readConArg
	 *            the readable connection
	 * @throws OXException
	 *             if loading fails
	 */
	public abstract UserSettingMail loadUserSettingMail(final int user, final Context ctx, final Connection readConArg)
			throws OXException;

	/**
	 * Removes the user's mail settings from cache if any used
	 * 
	 * @param user
	 *            the user
	 * @param ctx
	 *            the context
	 * @throws OXException
	 *             if cache removal fails
	 */
	public abstract void removeUserSettingMail(final int user, final Context ctx) throws OXException;

	/**
	 * Clears this storage's cache if any used
	 * 
	 * @throws OXException
	 *             if cache clearing fails
	 */
	public abstract void clearStorage() throws OXException;

}
