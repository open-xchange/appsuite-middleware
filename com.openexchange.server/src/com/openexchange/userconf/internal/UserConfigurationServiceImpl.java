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

package com.openexchange.userconf.internal;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.session.Session;
import com.openexchange.user.User;
import com.openexchange.userconf.UserConfigurationService;

/**
 * {@link UserConfigurationServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UserConfigurationServiceImpl implements UserConfigurationService {

    /**
     * Initializes a new {@link UserConfigurationServiceImpl}.
     */
    public UserConfigurationServiceImpl() {
        super();
    }

    @Override
    public void clearStorage() throws OXException {
        UserConfigurationStorage.getInstance().clearStorage();
    }

    @Override
    public UserConfiguration getUserConfiguration(Session session) throws OXException {
        return UserConfigurationStorage.getInstance().getUserConfiguration(session);
    }

    @Override
    public UserConfiguration getUserConfiguration(final int userId, final Context ctx) throws OXException {
        return getUserConfiguration(userId, ctx, true);
    }

    @Override
    public UserConfiguration getUserConfiguration(final int userId, final Context ctx, final boolean initExtendedPermissions) throws OXException {
        return UserConfigurationStorage.getInstance().getUserConfiguration(userId, null, ctx);
    }

    @Override
    public UserConfiguration getUserConfiguration(final int userId, final int[] groups, final Context ctx) throws OXException {
        return UserConfigurationStorage.getInstance().getUserConfiguration(userId, groups, ctx);
    }

    @Override
    public UserConfiguration[] getUserConfiguration(final Context ctx, final User[] users) throws OXException {
        return UserConfigurationStorage.getInstance().getUserConfiguration(ctx, users);
    }

    @Override
    public void removeUserConfiguration(final int userId, final Context ctx) throws OXException {
        UserConfigurationStorage.getInstance().invalidateCache(userId, ctx);
    }
}
