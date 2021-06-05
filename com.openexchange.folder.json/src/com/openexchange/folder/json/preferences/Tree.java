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

package com.openexchange.folder.json.preferences;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.SettingExceptionCodes;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Strings;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * Preferences tree item to allow the user to configure what folder tree he wants to use.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Tree implements PreferencesItemService {

    private static final String PROPERTY_NAME = "com.openexchange.folder.tree";

    private static final String NAME = "tree";

    public Tree() {
        super();
    }

    @Override
    public String[] getPath() {
        return new String[] { "modules", "folder", NAME };
    }

    @Override
    public IValueHandler getSharedValue() {
        return new IValueHandler() {

            @Override
            public int getId() {
                return NO_ID;
            }

            @Override
            public void getValue(final Session session, final Context ctx, final User user, final UserConfiguration userConfig, final Setting setting) throws OXException {
                Integer tree = ServerUserSetting.getInstance().getFolderTree(ctx.getContextId(), user.getId());
                if (null == tree) {
                    final ConfigurationService configurationService = ServiceRegistry.getInstance().getService(ConfigurationService.class, true);
                    final String value = configurationService.getProperty(PROPERTY_NAME, "0");
                    try {
                        tree = Integer.valueOf(value);
                    } catch (NumberFormatException e) {
                        throw ConfigurationExceptionCodes.PROPERTY_NOT_AN_INTEGER.create(e, PROPERTY_NAME);
                    }
                }
                setting.setSingleValue(tree);
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
            public void writeValue(final Session session, final Context ctx, final User user, final Setting setting) throws OXException {
                final String value = setting.getSingleValue().toString();
                final Integer tree;
                try {
                    tree = I(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    throw SettingExceptionCodes.INVALID_VALUE.create(e, value, Strings.join(getPath(), "/"));
                }
                ServerUserSetting.getInstance().setFolderTree(ctx.getContextId(), user.getId(), tree);
            }
        };
    }
}
