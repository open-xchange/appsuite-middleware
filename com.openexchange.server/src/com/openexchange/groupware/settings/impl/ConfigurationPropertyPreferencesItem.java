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

import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * Makes a certain configuration setting in the configuration service available as a preferences item service.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ConfigurationPropertyPreferencesItem implements PreferencesItemService {

    private final ConfigurationService config;
    private final String[] path;
    private final String key;

    public ConfigurationPropertyPreferencesItem(String key, ConfigurationService config, String... path) {
        super();
        this.config = config;
        this.path = path;
        this.key = key;
    }

    @Override
    public String[] getPath() {
        return path;
    }

    @Override
    public IValueHandler getSharedValue() {
        final ConfigurationService config = this.config;
        final String key = this.key;
        return new ReadOnlyValue() {

            @Override
            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
                setting.setSingleValue(convert(config.getProperty(key)));
            }

            @Override
            public boolean isAvailable(UserConfiguration userConfig) {
                return config.getProperty(key) != null;
            }
        };
    }

    public Object convert(String property) {
        return property;
    }

}
