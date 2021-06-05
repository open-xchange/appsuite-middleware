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

import static com.openexchange.java.Autoboxing.B;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.SettingExceptionCodes;
import com.openexchange.groupware.settings.impl.AbstractUserFuncs;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * {@link BetaFeatures} - Configuration tree entry to enabled/disable beta features for a certain user.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BetaFeatures implements PreferencesItemService {

    private static final String NAME = "beta";

    private static final String PROP_BETA = "com.openexchange.user.beta";

    public BetaFeatures() {
        super();
    }

    @Override
    public String[] getPath() {
        return new String[] { NAME };
    }

    @Override
    public IValueHandler getSharedValue() {
        return new AbstractUserFuncs() {
            @Override
            public void getValue(final Session session, final Context ctx, final User user, final UserConfiguration userConfig, final Setting setting) throws OXException {
                String set = user.getAttributes().get(NAME);
                if (null == set) {
                    // Return global configuration setting for beta features
                    setting.setSingleValue(B(getBooleanProperty(PROP_BETA, true)));
                } else {
                    // Return user's individual setting for beta features
                    setting.setSingleValue(Boolean.valueOf(set));
                }
            }
            @Override
            public boolean isAvailable(final UserConfiguration userConfig) {
                return true;
            }
            @Override
            public boolean isWritable() {
                return true;
            }
            @Override
            public void writeValue(Session session, Context ctx, User user, Setting setting) throws OXException {
                String value = setting.getSingleValue().toString();
                if (!("true".equalsIgnoreCase(value)) && !("false".equalsIgnoreCase(value))) {
                    throw SettingExceptionCodes.INVALID_VALUE.create(value, NAME);
                }

                // Only update if different
                String set = user.getAttributes().get(NAME);
                if (null == set) {
                    UserStorage.getInstance().setAttribute(NAME, value, user.getId(), ctx);
                } else if (Boolean.parseBoolean(set) != Boolean.parseBoolean(value)) {
                    UserStorage.getInstance().setAttribute(NAME, value, user.getId(), ctx);
                }

            }
        };
    }

    /**
     * Gets the specified <code>boolean</code> property from configuration service.
     *
     * @param name The property's name
     * @param defaultValue The default <code>boolean</code> value to return if property is missing
     * @return The <code>boolean</code> value
     */
    static boolean getBooleanProperty(final String name, final boolean defaultValue) {
        final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (null == service) {
            return defaultValue;
        }
        return service.getBoolProperty(name, defaultValue);
    }
}
