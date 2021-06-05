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

import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * {@link LocationLogout}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class LocationLogout implements PreferencesItemService {

    public LocationLogout() {
        super();
    }

    @Override
    public String[] getPath() {
        return new String[] { "customLocations", "logout" };
    }

    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isAvailable(final UserConfiguration userConfig) {
                return true;
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public void getValue(final Session session, final Context ctx, final User user, final UserConfiguration userConfig, final Setting setting) throws OXException {
                final ConfigViewFactory factory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
                final ConfigView view = factory.getView(session.getUserId(), session.getContextId());
                String value = view.get("com.openexchange.locations.logout", String.class);
                if (null == value) {
                    value = "";
                }
                setting.setSingleValue(value);
            }

        };
    }
}
