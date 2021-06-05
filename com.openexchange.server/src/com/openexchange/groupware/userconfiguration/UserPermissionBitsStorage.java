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

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.user.User;
import com.openexchange.userconf.UserPermissionService;

/**
 * Direct usage of this class is strictly discouraged! Always use {@link UserPermissionService}!
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class UserPermissionBitsStorage {

    private static UserPermissionBitsStorage singleton;

    /**
     * Sets the singleton instance of {@link UserConfigurationStorage}
     *
     * @param singleton The singleton instance
     * @throws OXException If singleton cannot be configured
     */
    static void setInstance(final UserPermissionBitsStorage singleton) throws OXException {
        UserPermissionBitsStorage.singleton = singleton;
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
    public static final UserPermissionBitsStorage getInstance() {
        return singleton;
    }

    private boolean started;

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
     * Retrieve the permission bits for the given user
     */
    public abstract UserPermissionBits getUserPermissionBits(final int userId, final int contextId) throws OXException;

    /**
     * Retrieve the permission bits for the given user
     */
    public abstract UserPermissionBits getUserPermissionBits(final int userId, final Context ctx) throws OXException;

    /**
     * Retrieve the permission bits for the given user
     */
    public abstract UserPermissionBits getUserPermissionBits(final Connection con, final int userId, final Context ctx) throws OXException;

    /**
     * Retrieve the permission bits for the given users
     */
    public abstract UserPermissionBits[] getUserPermissionBits(final Context ctx, final User[] users) throws OXException;

    /**
     * Retrieve the permission bits for the given users
     */
    public abstract UserPermissionBits[] getUserPermissionBits(Context ctx, int[] userIds) throws OXException;

    /**
     * Forget any locally cached entries
     */
    public abstract void clearStorage() throws OXException;

    /**
     * Forget a locally cached entry
     */
    public abstract void removeUserPermissionBits(final int userId, final Context ctx) throws OXException;

    /**
     * Store the permission bits in the database
     */
    public abstract void saveUserPermissionBits(final int permissionBits, final int userId, final Context ctx) throws OXException;

    /**
     * Store the permission bits in the database
     */
    public abstract void saveUserPermissionBits(Connection con, int permissionBits, int userId, Context ctx) throws OXException;


    @SuppressWarnings("unused")
    protected void startInternal() throws OXException {
        // Nope
    }

    @SuppressWarnings("unused")
    protected void stopInternal() throws OXException {
        // Nope
    }


}
