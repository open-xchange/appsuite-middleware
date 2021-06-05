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

package com.openexchange.groupware.settings.tree;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.user.User;
import com.openexchange.userconf.UserPermissionService;

/**
 * Abstract class for easily adding preferences item returning permissions enabled for a user.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractPermissions implements PreferencesItemService {

    /** The logger constant */
    static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractPermissions.class);

    /**
     * Initializes a new {@link AbstractPermissions}.
     */
    protected AbstractPermissions() {
        super();
    }

    @Override
    public IValueHandler getSharedValue() {

        return new ReadOnlyValue() {

            @Override
            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
                UserPermissionService service = ServerServiceRegistry.getInstance().getService(UserPermissionService.class);
                if (null == service) {
                    LOGGER.warn("Absent service: {}. Cannot check for permission availability.", UserPermissionService.class.getName());
                    setting.setSingleValue(Boolean.FALSE);
                } else {
                    UserPermissionBits permissionBits = service.getUserPermissionBits(user.getId(), ctx);
                    setting.setSingleValue(Boolean.valueOf(hasPermission(permissionBits)));
                }
            }

            @Override
            public boolean isAvailable(final UserConfiguration userConfig) {
                return true;
            }
        };
    }

    /**
     * Checks if required permission(s) is available.
     *
     * @param permissionBits The granted permissions for the user
     * @return <code>true</code> if permission(s) is available; otherwise <code>false</code>
     */
    protected abstract boolean hasPermission(UserPermissionBits permissionBits);
}
