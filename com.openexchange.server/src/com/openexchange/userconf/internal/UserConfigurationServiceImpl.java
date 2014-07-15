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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.userconf.internal;

import java.util.HashSet;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;

/**
 * {@link UserConfigurationServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UserConfigurationServiceImpl implements UserConfigurationService {

    private final UserService userService;

    /**
     * Initializes a new {@link UserConfigurationServiceImpl}.
     */
    public UserConfigurationServiceImpl(UserService userService) {
        super();
        this.userService = userService;
    }

    @Override
    public void clearStorage() throws OXException {
        UserConfigurationStorage.getInstance().clearStorage();
    }

    @Override
    public UserConfiguration getUserConfiguration(final int userId, final Context ctx) throws OXException {
        return getUserConfiguration(userId, ctx, true);
    }

    @Override
    public UserConfiguration getUserConfiguration(final int userId, final Context ctx, final boolean initExtendedPermissions) throws OXException {
        User user = userService.getUser(userId, ctx);
        if (user.isGuest()) {
            return adjustGuestConfiguration(user, ctx, UserConfigurationStorage.getInstance().getUserConfiguration(user.getCreatedBy(), null, ctx));
        }

        return UserConfigurationStorage.getInstance().getUserConfiguration(userId, null, ctx);
    }

    @Override
    public UserConfiguration getUserConfiguration(final int userId, final int[] groups, final Context ctx) throws OXException {
        User user = userService.getUser(userId, ctx);
        if (user.isGuest()) {
            return adjustGuestConfiguration(user, ctx, UserConfigurationStorage.getInstance().getUserConfiguration(user.getCreatedBy(), groups, ctx));
        }

        return UserConfigurationStorage.getInstance().getUserConfiguration(userId, groups, ctx);
    }

    @Override
    public UserConfiguration[] getUserConfiguration(final Context ctx, final User[] users) throws OXException {
        User[] realUsers = new User[users.length];
        User[] guests = new User[users.length];
        for (int i = 0; i < users.length; i++) {
            User user = users[i];
            if (user.isGuest()) {
                User realUser = userService.getUser(user.getCreatedBy(), ctx);
                realUsers[i] = realUser;
                guests[i] = user;
            } else {
                realUsers[i] = user;
            }
        }

        UserConfiguration[] configurations = new UserConfiguration[users.length];
        UserConfiguration[] loadedConfigurations = UserConfigurationStorage.getInstance().getUserConfiguration(ctx, realUsers);
        for (int i = 0; i < loadedConfigurations.length; i++) {
            UserConfiguration configuration = loadedConfigurations[i];
            User guest = guests[i];
            if (guest != null) {
                configurations[i] = adjustGuestConfiguration(guest, ctx, configuration);
            } else {
                configurations[i] = configuration;
            }
        }

        return configurations;
    }

    @Override
    public void removeUserConfiguration(final int userId, final Context ctx) throws OXException {
        UserConfigurationStorage.getInstance().invalidateCache(userId, ctx);
    }

    private UserConfiguration adjustGuestConfiguration(User guest, Context ctx, UserConfiguration creatorConfiguration) {
        Set<String> permissions = new HashSet<String>(creatorConfiguration.getExtendedPermissions());
        permissions.remove(Permission.WEBMAIL);
        return new UserConfiguration(permissions, guest.getId(), guest.getGroups(), ctx);
    }
}
