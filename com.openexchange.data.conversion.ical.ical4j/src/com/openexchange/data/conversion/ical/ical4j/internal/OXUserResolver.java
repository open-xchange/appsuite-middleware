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

package com.openexchange.data.conversion.ical.ical4j.internal;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class OXUserResolver implements UserResolver {

    private UserService userService;

    /**
     * Default constructor.
     */
    public OXUserResolver() {
        super();
    }

    @Override
    public List<User> findUsers(final List<String> mails, final Context ctx) throws OXException {
        final List<User> users = new ArrayList<User>();
        if (mails.isEmpty()) {
            return users;
        }
        if (null == userService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(UserService.class.getName());
        }
        for (final String mail : mails) {
            try {
                users.add(userService.searchUser(mail, ctx));
            } catch (OXException x) {
                if (!LdapExceptionCode.NO_USER_BY_MAIL.equals(x)) {
                    throw x;
                }
            }
        }
        return users;
    }

    @Override
    public User loadUser(final int userId, final Context ctx) throws OXException {
        if (null == userService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(UserService.class.getName());
        }
        return userService.getUser(userId, ctx);
    }

    /**
     * Sets the userService
     *
     * @param userService The userService to set
     */
    public void setUserService(final UserService userService) {
        this.userService = userService;
    }
}
