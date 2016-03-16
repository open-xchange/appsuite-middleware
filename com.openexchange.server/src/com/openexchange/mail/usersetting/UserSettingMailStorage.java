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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import com.openexchange.cache.registry.CacheAvailabilityListener;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UserSettingMailStorage} - Access to {@link UserSettingMail}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class UserSettingMailStorage implements CacheAvailabilityListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UserSettingMailStorage.class);

    private static volatile UserSettingMailStorage singleton;

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
        UserSettingMailStorage tmp = singleton;
        if (null == tmp) {
            synchronized (UserSettingMailStorage.class) {
                tmp = singleton;
                if (null == tmp) {
                    tmp = singleton = new CachingUserSettingMailStorage();
                }
            }
        }
        return tmp;
    }

    /**
     * Releases this storage instance
     */
    public static final void releaseInstance() {
        if (null != singleton) {
            synchronized (UserSettingMailStorage.class) {
                if (null != singleton) {
                    singleton.shutdownStorage();
                    singleton = null;
                }
            }
        }
    }

    /**
     * A convenience method that returns {@link #getUserSettingMail(int, Context, Connection)} with the connection parameter set to
     * <code>null</code>.
     *
     * @param session The session
     * @return The instance of {@link UserSettingMail} which matches given user ID and context or <code>null</code> on exception
     * @throws OXException If context cannot be loaded
     */
    public final UserSettingMail getUserSettingMail(final Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUserSettingMail();
        }
        return getUserSettingMail(session.getUserId(), session.getContextId());
    }

    /**
     * A convenience method that returns {@link #getUserSettingMail(int, Context, Connection)} with the connection parameter set to
     * <code>null</code>.
     *
     * @param user The user ID
     * @param cid The context ID
     * @return The instance of {@link UserSettingMail} which matches given user ID and context or <code>null</code> on exception
     * @throws OXException If context cannot be loaded
     */
    public final UserSettingMail getUserSettingMail(final int user, final int cid) throws OXException {
        return getUserSettingMail(user, ContextStorage.getStorageContext(cid), null);
    }

    /**
     * A convenience method that returns {@link #getUserSettingMail(int, Context, Connection)} with the connection parameter set to
     * <code>null</code>.
     *
     * @param user The user ID
     * @param ctx The context
     * @return The instance of {@link UserSettingMail} which matches given user ID and context or <code>null</code> on exception
     */
    public final UserSettingMail getUserSettingMail(final int user, final Context ctx) {
        return getUserSettingMail(user, ctx, null);
    }

    /**
     * A convenience method that returns {@link #loadUserSettingMail(int, Context, Connection)}. If an exception is thrown in delegated
     * method <code>null</code> is returned.
     *
     * @param user The user ID
     * @param ctx The context
     * @param readCon The readable connection (may be <code>null</code>)
     * @return The instance of {@link UserSettingMail} which matches given user ID and context or <code>null</code> on exception
     */
    public final UserSettingMail getUserSettingMail(final int user, final Context ctx, final Connection readCon) {
        try {
            return loadUserSettingMail(user, ctx, readCon);
        } catch (final OXException e) {
            LOG.error("", e);
            return null;
        }
    }

    /**
     * Saves given user's mail settings bits to database
     *
     * @param usm the user's mail settings to save
     * @param user the user ID
     * @param ctx the context
     * @throws OXException if user's mail settings could not be saved
     */
    public final void saveUserSettingMailBits(final UserSettingMail usm, final int user, final Context ctx) throws OXException {
        saveUserSettingMailBits(usm, user, ctx, null);
    }

    /**
     * Saves given user's mail settings bits to database
     *
     * @param usm the user's mail settings to save
     * @param user the user ID
     * @param ctx the context
     * @param writeConArg - the writable connection; may be <code>null</code>
     * @throws OXException if user's mail settings could not be saved
     */
    public abstract void saveUserSettingMailBits(final UserSettingMail usm, final int user, final Context ctx, final Connection writeConArg) throws OXException;

    /**
     * Saves given user's mail settings to database
     *
     * @param usm the user's mail settings to save
     * @param user the user ID
     * @param ctx the context
     * @throws OXException if user's mail settings could not be saved
     */
    public final void saveUserSettingMail(final UserSettingMail usm, final int user, final Context ctx) throws OXException {
        saveUserSettingMail(usm, user, ctx, null);
    }

    /**
     * Saves given user's mail settings to database
     *
     * @param usm the user's mail settings to save
     * @param user the user ID
     * @param ctx the context
     * @param writeConArg - the writable connection; may be <code>null</code>
     * @throws OXException if user's mail settings could not be saved
     */
    public abstract void saveUserSettingMail(final UserSettingMail usm, final int user, final Context ctx, final Connection writeConArg) throws OXException;

    /**
     * Deletes the user's mail settings from database
     *
     * @param user the user ID
     * @param ctx the context
     * @throws OXException if deletion fails
     */
    public final void deleteUserSettingMail(final int user, final Context ctx) throws OXException {
        deleteUserSettingMail(user, ctx, null);
    }

    /**
     * Deletes the user's mail settings from database
     *
     * @param user the user ID
     * @param ctx the context
     * @param writeConArg the writable connection; may be <code>null</code>
     * @throws OXException - if deletion fails
     */
    public abstract void deleteUserSettingMail(final int user, final Context ctx, final Connection writeConArg) throws OXException;

    /**
     * Loads user's mail settings from database
     *
     * @param user the user
     * @param ctx the context
     * @return The instance of {@link UserSettingMail} which matches given user ID and context
     * @throws OXException if loading fails
     */
    public final UserSettingMail loadUserSettingMail(final int user, final Context ctx) throws OXException {
        return loadUserSettingMail(user, ctx, null);
    }

    /**
     * Loads user's mail settings from database
     *
     * @param user the user
     * @param ctx the context
     * @param readConArg the readable connection
     * @return The instance of {@link UserSettingMail} which matches given user ID and context
     * @throws OXException if loading fails
     */
    public abstract UserSettingMail loadUserSettingMail(final int user, final Context ctx, final Connection readConArg) throws OXException;

    /**
     * Removes the user's mail settings from cache if any used
     *
     * @param user the user
     * @param ctx the context
     * @throws OXException if cache removal fails
     */
    public abstract void removeUserSettingMail(final int user, final Context ctx) throws OXException;

    /**
     * Clears this storage's cache if any used
     *
     * @throws OXException if cache clearing fails
     */
    public abstract void clearStorage() throws OXException;

    /**
     * Triggers necessary action to shutdown the storage
     */
    public abstract void shutdownStorage();
}
