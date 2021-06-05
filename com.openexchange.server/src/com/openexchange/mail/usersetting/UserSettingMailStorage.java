/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail.usersetting;

import java.sql.Connection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
     * Gets the singleton instance of {@link UserSettingMailStorage}.
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
     * Releases this storage instance.
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
    public final UserSettingMail getUserSettingMail(Session session) throws OXException {
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
    public final UserSettingMail getUserSettingMail(int user, int cid) throws OXException {
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
    public final UserSettingMail getUserSettingMail(int user, Context ctx) {
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
    public final UserSettingMail getUserSettingMail(int user, Context ctx, Connection readCon) {
        try {
            return loadUserSettingMail(user, ctx, readCon);
        } catch (OXException e) {
            LOG.error("", e);
            return null;
        }
    }

    /**
     * Saves given user's mail settings bits to database.
     *
     * @param usm the user's mail settings to save
     * @param user the user ID
     * @param ctx the context
     * @throws OXException if user's mail settings could not be saved
     */
    public final void saveUserSettingMailBits(UserSettingMail usm, int user, Context ctx) throws OXException {
        saveUserSettingMailBits(usm, user, ctx, null);
    }

    /**
     * Saves given user's mail settings bits to database.
     *
     * @param usm the user's mail settings to save
     * @param user the user ID
     * @param ctx the context
     * @param writeConArg - the writable connection; may be <code>null</code>
     * @throws OXException if user's mail settings could not be saved
     */
    public abstract void saveUserSettingMailBits(UserSettingMail usm, int user, Context ctx, Connection writeConArg) throws OXException;

    /**
     * Saves given user's mail settings to database.
     *
     * @param usm the user's mail settings to save
     * @param user the user ID
     * @param ctx the context
     * @throws OXException if user's mail settings could not be saved
     */
    public final void saveUserSettingMail(UserSettingMail usm, int user, Context ctx) throws OXException {
        saveUserSettingMail(usm, user, ctx, null);
    }

    /**
     * Saves given user's mail settings to database.
     *
     * @param usm the user's mail settings to save
     * @param user the user ID
     * @param ctx the context
     * @param writeConArg - the writable connection; may be <code>null</code>
     * @throws OXException if user's mail settings could not be saved
     */
    public abstract void saveUserSettingMail(UserSettingMail usm, int user, Context ctx, Connection writeConArg) throws OXException;

    /**
     * Deletes the user's mail settings from database.
     *
     * @param user the user ID
     * @param ctx the context
     * @throws OXException if deletion fails
     */
    public final void deleteUserSettingMail(int user, Context ctx) throws OXException {
        deleteUserSettingMail(user, ctx, null);
    }

    /**
     * Deletes the user's mail settings from database.
     *
     * @param user the user ID
     * @param ctx the context
     * @param writeConArg the writable connection; may be <code>null</code>
     * @throws OXException - if deletion fails
     */
    public abstract void deleteUserSettingMail(int user, Context ctx, Connection writeConArg) throws OXException;

    /**
     * Loads user's mail settings from database.
     *
     * @param user the user
     * @param ctx the context
     * @return The instance of {@link UserSettingMail} which matches given user ID and context
     * @throws OXException if loading fails
     */
    public final UserSettingMail loadUserSettingMail(int user, Context ctx) throws OXException {
        return loadUserSettingMail(user, ctx, null);
    }

    /**
     * Loads user's mail settings from database.
     *
     * @param user the user
     * @param ctx the context
     * @param readConArg the readable connection
     * @return The instance of {@link UserSettingMail} which matches given user ID and context
     * @throws OXException if loading fails
     */
    public abstract UserSettingMail loadUserSettingMail(int user, Context ctx, Connection readConArg) throws OXException;

    /**
     * Removes the user's mail settings from cache if any used.
     *
     * @param user the user
     * @param ctx the context
     * @throws OXException if cache removal fails
     */
    public abstract void removeUserSettingMail(int user, Context ctx) throws OXException;

    /**
     * Clears this storage's cache if any used.
     *
     * @throws OXException if cache clearing fails
     */
    public abstract void clearStorage() throws OXException;

    /**
     * Triggers necessary action to shutdown the storage
     */
    public abstract void shutdownStorage();

    /**
     * Gets the sender address of the given user.
     *
     * @param userId The identifier of the user to retrieve the sender address for
     * @param context The context
     * @param connection An optional connection to use
     * @return The optional user's sender address
     * @throws OXException If sender address cannot be returned
     */
    public Optional<String> getSenderAddress(int userId, Context context, Connection connection) throws OXException {
        Integer iUserId = Integer.valueOf(userId);
        Map<Integer, String> addresses = getSenderAddresses(Collections.singleton(iUserId), context, connection);
        return Optional.ofNullable(addresses.get(iUserId));
    }

    /**
     * Gets the sender addresses of the given users.
     *
     * @param userIds The identifiers of the users to retrieve the sender address for
     * @param context The context
     * @param connection An optional connection to use
     * @return A map associating the user identifier with user's sender address
     * @throws OXException If sender addresses cannot be returned
     */
    public abstract Map<Integer, String> getSenderAddresses(Set<Integer> userIds, Context context, Connection connection) throws OXException;

}
