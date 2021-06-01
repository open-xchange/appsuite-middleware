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

package com.openexchange.multifactor.impl;

import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.multifactor.MultifactorProperties;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * {@link MultifactorConfigTreeItem}
 * Pass the configured value of com.openexchange.multifactor.allowMultiple to io.ox/multifactor//allowMultiple
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class MultifactorConfigTreeItem implements ConfigTreeEquivalent, PreferencesItemService {

    @Override
    public String getConfigTreePath() {
        return "modules/multifactor/allowMultiple";
    }

    @Override
    public String getJslobPath() {
        return "io.ox/multifactor//allowMultiple";
    }

    @Override
    public String[] getPath() {
        return new String[] { "modules", "multifactor", "allowMultiple" };
    }

    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {

            @Override
            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
                ConfigViewFactory factory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);

                Boolean enabled = (MultifactorProperties.allowMultiple.getDefaultValue() instanceof Boolean) ? (Boolean) MultifactorProperties.allowMultiple.getDefaultValue() : Boolean.TRUE;

                if (factory != null) {
                    ConfigView view = factory.getView(session.getUserId(), session.getContextId());
                    ComposedConfigProperty<Boolean> property = view.property(MultifactorProperties.allowMultiple.getFQPropertyName(), Boolean.class);
                    if (property.isDefined()) {
                        enabled = property.get();
                    }
                }

                setting.setSingleValue(enabled);
            }

            @Override
            public boolean isAvailable(UserConfiguration userConfig) {
                return true;
            }

        };
    }
}
