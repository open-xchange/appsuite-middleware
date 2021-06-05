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


package com.openexchange.userconf;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * {@link UserConfigurationService} - The user configuration service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface UserConfigurationService {

    /**
     * Determines the instance of <code>UserConfiguration</code> that corresponds to given session.
     *
     * @param session The session providing user/context information
     * @return the instance of <code>UserConfiguration</code>
     * @throws OXException If user's configuration could not be determined
     * @see #getUserConfiguration(int, int[], Context)
     */
    public UserConfiguration getUserConfiguration(Session session) throws OXException;

    /**
     * Determines the instance of <code>UserConfiguration</code> that corresponds to given user ID.
     *
     * @param userId - the user ID
     * @param ctx - the context
     * @return the instance of <code>UserConfiguration</code>
     * @throws OXException If user's configuration could not be determined
     * @see #getUserConfiguration(int, int[], Context)
     */
    public UserConfiguration getUserConfiguration(final int userId, final Context ctx) throws OXException;

    /**
     * Determines the instance of <code>UserConfiguration</code> that corresponds to given user ID.
     *
     * @param userId - the user ID
     * @param ctx - the context
     * @param initExtendedPermissions Whether to initialize extended permissions
     * @return the instance of <code>UserConfiguration</code>
     * @throws OXException If user's configuration could not be determined
     * @see #getUserConfiguration(int, int[], Context)
     */
    public UserConfiguration getUserConfiguration(int userId, Context ctx, boolean initExtendedPermissions) throws OXException;

    /**
     * Determines the instance of <code>UserConfiguration</code> that corresponds to given user ID. If <code>groups</code> argument is set,
     * user's groups need not to be loaded from user storage
     *
     * @param userId - the user ID
     * @param groups - user's groups
     * @param ctx - the context
     * @return the instance of <code>UserConfiguration</code>
     * @throws OXException If user's configuration could not be determined
     */
    public UserConfiguration getUserConfiguration(int userId, int[] groups, Context ctx) throws OXException;

    /**
     * This method reads several user module access permissions. This method is faster than reading separately the {@link UserConfiguration}
     * for every given user.
     * @param ctx the context
     * @param users user objects that module access permission should be loaded.
     * @return an array with the module access permissions of the given users.
     * @throws OXException if users configuration could not be loaded.
     */
    public UserConfiguration[] getUserConfiguration(Context ctx, User[] users) throws OXException;

    /**
     * <p>
     * Clears the whole storage. All kept instances of <code>UserConfiguration</code> are going to be removed from storage.
     * <p>
     * <b>NOTE:</b> Only the instances are going to be removed from storage; underlying database is not affected
     *
     * @throws OXException If clearing fails
     */
    public void clearStorage() throws OXException;

    /**
     * <p>
     * Removes the instance of <code>UserConfiguration</code> that corresponds to given user ID from storage.
     * <p>
     * <b>NOTE:</b> Only the instance is going to be removed from storage; underlying database is not affected
     *
     * @param userId - the user ID
     * @param ctx - the context
     * @throws OXException If removal fails
     */
    public void removeUserConfiguration(int userId, Context ctx) throws OXException;

}
