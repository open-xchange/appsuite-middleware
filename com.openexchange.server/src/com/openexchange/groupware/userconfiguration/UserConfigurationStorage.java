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

package com.openexchange.groupware.userconfiguration;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.user.User;
import com.openexchange.userconf.UserConfigurationService;

/**
 * Direct usage of this class is strongly discouraged! Always use {@link UserConfigurationService}!
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class UserConfigurationStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UserConfigurationStorage.class);

    private static volatile UserConfigurationStorage singleton;

    private boolean started;

    /**
     * Default constructor
     */
    protected UserConfigurationStorage() {
        super();
    }

    /**
     * Sets the singleton instance of {@link UserConfigurationStorage}
     *
     * @param singleton The singleton instance
     * @throws OXException If singleton cannot be configured
     */
    static void setInstance(final UserConfigurationStorage singleton) throws OXException {
        UserConfigurationStorage.singleton = singleton;
        singleton.start();
    }

    /**
     * Releases the singleton instance of {@link UserConfigurationStorage}
     *
     * @throws OXException If singleton cannot be configured
     */
    static void releaseInstance() throws OXException {
        singleton.stop();
        singleton = null;
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

    private final void start() throws OXException {
        if (started) {
            return;
        }
        startInternal();
        started = true;
    }

    private final void stop() throws OXException {
        if (!started) {
            return;
        }
        stopInternal();
        started = false;
    }

    /**
     * A convenience method that invokes
     * {@link #getUserConfigurationSafe(int, int[], Context)} with the group
     * parameter set to <code>null</code>
     *
     * @param userId The user ID
     * @param ctx The context
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
     * @param userId The user ID
     * @param groups The user's groups
     * @param ctx The contexts
     * @return The corresponding instance of {@link UserConfiguration} or
     *         <code>null</code> on exception
     */
    public final UserConfiguration getUserConfigurationSafe(final int userId, final int[] groups, final Context ctx) {
        try {
            return UserConfigurationStorage.getInstance().getUserConfiguration(userId, groups, ctx);
        } catch (OXException e) {
            LOG.error("", e);
            return null;
        }
    }

    /**
     * Determines the instance of <code>UserConfiguration</code> that
     * corresponds to given user ID.
     *
     * @param userId - the user ID
     * @param ctx - the context
     * @return the instance of <code>UserConfiguration</code>
     * @throws OXException If user's configuration could not be
     *             determined
     * @see #getUserConfiguration(int, int[], Context)
     */
    public final UserConfiguration getUserConfiguration(final int userId, final Context ctx) throws OXException {
        return getUserConfiguration(userId, null, ctx);
    }

    /**
     * Perform necessary actions to start instance
     *
     * @throws OXException
     */
    protected abstract void startInternal() throws OXException;

    /**
     * Perform necessary actions to stop instance
     *
     * @throws OXException
     */
    protected abstract void stopInternal() throws OXException;

    /**
     * Determines the instance of <code>UserConfiguration</code> that
     * corresponds to given session.
     *
     * @param session The session providing user/context information
     * @return the instance of <code>UserConfiguration</code>
     * @throws OXException If user's configuration could not be determined
     */
    public UserConfiguration getUserConfiguration(Session session) throws OXException {
        return getUserConfiguration(session, null);
    }

    /**
     * Determines the instance of <code>UserConfiguration</code> that
     * corresponds to given session. If <code>groups</code> argument is set,
     * user's groups need not to be loaded from user storage
     *
     * @param session The session providing user/context information
     * @param groups The optional groups of the user
     * @return the instance of <code>UserConfiguration</code>
     * @throws OXException If user's configuration could not be determined
     */
    public abstract UserConfiguration getUserConfiguration(Session session, int[] groups) throws OXException;

    /**
     * Determines the instance of <code>UserConfiguration</code> that
     * corresponds to given user ID. If <code>groups</code> argument is set,
     * user's groups need not to be loaded from user storage
     *
     * @param userId - the user ID
     * @param groups - user's groups
     * @param ctx - the context
     * @return the instance of <code>UserConfiguration</code>
     * @throws OXException If user's configuration could not be
     *             determined
     */
    public abstract UserConfiguration getUserConfiguration(final int userId, final int[] groups, final Context ctx) throws OXException;

    public abstract UserConfiguration[] getUserConfiguration(Context ctx, User[] users) throws OXException;

    public abstract UserConfiguration[] getUserConfigurations(Context ctx, int[] userIds, int[][] groups) throws OXException;


    /**
     * <p>
     * Clears the whole storage. All kept instances of
     * <code>UserConfiguration</code> are going to be removed from storage.
     * <p>
     * <b>NOTE:</b> Only the instances are going to be removed from storage;
     * underlying database is not affected
     *
     * @throws OXException If clearing fails
     */
    public abstract void clearStorage() throws OXException;

    /**
     * <p>
     * Removes the instance of <code>UserConfiguration</code> that corresponds
     * to given user ID from storage.
     * <p>
     * <b>NOTE:</b> Only the instance is going to be removed from storage;
     * underlying database is not affected
     *
     * @param userId - the user ID
     * @param ctx - the context
     * @throws OXException If removal fails
     */
    public abstract void invalidateCache(int userId, Context ctx) throws OXException;

}
