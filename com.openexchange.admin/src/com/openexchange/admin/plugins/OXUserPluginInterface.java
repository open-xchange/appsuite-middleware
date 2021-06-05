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

package com.openexchange.admin.plugins;

import java.util.Set;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;

public interface OXUserPluginInterface {

    public void create(final Context ctx, final User usr, final UserModuleAccess access, final Credentials cred) throws PluginException;

    public void delete(final Context ctx, final User[] user, final Credentials cred) throws PluginException;

    public void change(final Context ctx, final User usrdata, final Credentials auth) throws PluginException;

    public User[] getData(final Context ctx, final User[] users, final Credentials cred);

    /**
     * This Method is used for each plugin to check if it makes sense to run as administrator.
     *
     * @return
     */
    public boolean canHandleContextAdmin();

    /**
     * Changes the personal part of specified user's E-Mail address.
     *
     * @param ctx The context
     * @param user The user
     * @param personal The personal to set or <code>null</code> to drop the personal information (if any)
     * @param auth The credentials
     * @throws PluginException If operation fails
     */
    public void changeMailAddressPersonal(Context ctx, User user, String personal, Credentials auth) throws PluginException;

    /**
     * Changes specified user's capabilities.
     *
     * @param ctx The context
     * @param user The user
     * @param capsToAdd The capabilities to add
     * @param capsToRemove The capabilities to remove
     * @param capsToDrop The capabilities to drop; e.g. clean from storage
     * @param auth The credentials
     * @throws PluginException If changing capabilities fails
     */
    public void changeCapabilities(Context ctx, User user, Set<String> capsToAdd, Set<String> capsToRemove, Set<String> capsToDrop, Credentials auth) throws PluginException;

    /**
     * Manipulate user module access within the given context.
     *
     * @param ctx Context object.
     * @param user_id int containing the user id.
     * @param access String containing access combination name.
     * @param auth Credentials for authenticating against server.
     * @throws PluginException If change operation fails
     */
    public void changeModuleAccess(Context ctx, User user, String access_combination_name, Credentials auth) throws PluginException;

    /**
     * Manipulate user module access within the given context.
     *
     * @param ctx Context object.
     * @param userId int[] containing the user id.
     * @param moduleAccess UserModuleAccess containing module access.
     * @param auth Credentials for authenticating against server.
     * @throws PluginException If change operation fails
     */
    public void changeModuleAccess(Context ctx, User user, UserModuleAccess moduleAccess, Credentials auth) throws PluginException;

}
