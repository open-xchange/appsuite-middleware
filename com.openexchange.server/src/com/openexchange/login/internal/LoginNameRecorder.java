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

package com.openexchange.login.internal;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.login.NonTransient;
import com.openexchange.user.User;
import com.openexchange.user.UserExceptionCode;
import com.openexchange.user.UserService;

/**
 * The {@link LoginNameRecorder} stores the user's login name as an user attribute.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class LoginNameRecorder implements LoginHandlerService, NonTransient {

    private final static String LOGIN_ATTRIBUTE_NAME = LoginNameRecorder.class.getSimpleName().toLowerCase() + "/user_login";

    private final UserService userService;

    /**
     * Initializes a new {@link LoginNameRecorder}.
     */
    public LoginNameRecorder(UserService userService) {
        super();
        this.userService = userService;
    }

    @Override
    public void handleLogin(LoginResult login) throws OXException {
        Context ctx = login.getContext();
        if (login.getContext().isReadOnly()) {
            return;
        }
        User user = login.getUser();
        String value = login.getSession().getLogin();
        if (Strings.isNotEmpty(value) && false == value.equals(getStoredUserLogin(user))) {
            try {
                userService.setAttribute(null, LOGIN_ATTRIBUTE_NAME, value, user.getId(), ctx, false);
            } catch (OXException ex) {
                if (!UserExceptionCode.CONCURRENT_ATTRIBUTES_UPDATE.equals(ex)) {
                    throw ex;
                }
                // Do nothing
            }
        }
    }

    @Override
    public void handleLogout(LoginResult logout) {
        // nothing to do
    }

    /**
     * Extracts the currently stored value for the login name from the user's attributes.
     *
     * @param user The user to extract the stored login name for
     * @return The login name, or <code>null</code> if not set at all
     */
    private static String getStoredUserLogin(User user) {
        Map<String, String> attributes = user.getAttributes();
        return null == attributes ? null : attributes.get(LOGIN_ATTRIBUTE_NAME);
    }

}
