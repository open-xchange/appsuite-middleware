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

package com.openexchange.groupware.settings.impl;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * This class contains the shared functions for all user settings.
 */
public abstract class AbstractUserFuncs implements IValueHandler {

    /**
     * Initializes a new {@link AbstractUserFuncs}.
     */
    protected AbstractUserFuncs() {
        super();
    }

    @Override
    public void writeValue(final Session session, final Context ctx, final User user, final Setting setting) throws OXException {
        /*
         * write to user storage
         */
        UserImpl newUser = new UserImpl(user);
        setValue(newUser, setting.getSingleValue().toString(), user);
        UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class);
        if (null != userService) {
            userService.updateUser(newUser, ctx);
        } else {
            org.slf4j.LoggerFactory.getLogger(AbstractUserFuncs.class).warn(
                "Unable to access user service, updating directly via storage.", ServiceExceptionCode.absentService(UserService.class));
            UserStorage.getInstance().updateUser(newUser, ctx);
        }
        /*
         * try and set value in passed reference, too
         */
        if (UserImpl.class.isInstance(user)) {
            setValue((UserImpl)user, setting.getSingleValue().toString(), user);
        }
    }

    @Override
    public int getId() {
        return -1;
    }

    /**
     * Sets the value in passed <tt>newUser</tt>.
     *
     * @param newUser In this user object the value should be set.
     * @param value The value to set.
     * @param originalUser The original user fetched from storage
     * @throws OXException If writing of the value fails.
     */
    @SuppressWarnings("unused")
    protected void setValue(UserImpl newUser, String value, User originalUser) throws OXException {
        // Can be overwritten.
    }
}
