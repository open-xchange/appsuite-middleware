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

package com.openexchange.groupware.settings.tree.modules.calendar;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.SettingExceptionCodes;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class DefaultStatusPrivate implements PreferencesItemService {

    private static final String[] PATH = new String[] { "modules", "calendar", "defaultStatusPrivate" };

    @Override
    public String[] getPath() {
        return PATH;
    }

    @Override
    public IValueHandler getSharedValue() {
        return new IValueHandler() {

            @Override
            public int getId() {
                return -1;
            }

            @Override
            public void getValue(final Session session, final Context ctx, final User user, final UserConfiguration userConfig, final Setting setting) throws OXException {
                Integer value = ServerUserSetting.getInstance().getDefaultStatusPrivate(ctx.getContextId(), user.getId());
                if (value == null) {
                    value = Integer.valueOf(0);
                }
                setting.setSingleValue(value);
            }

            @Override
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasCalendar();
            }

            @Override
            public boolean isWritable() {
                return true;
            }

            @Override
            public void writeValue(final Session session, final Context ctx, final User user, final Setting setting) throws OXException {
                int value;
                try {
                    Object singleValue = setting.getSingleValue();
                    value = (singleValue instanceof Number) ? ((Number) singleValue).intValue() : Integer.parseInt(String.valueOf(singleValue));
                } catch (NumberFormatException e) {
                    throw SettingExceptionCodes.INVALID_VALUE.create(e, setting.getSingleValue());
                }
                if (value < 0 || value > 3) {
                    throw SettingExceptionCodes.INVALID_VALUE.create(setting.getSingleValue());
                }
                ServerUserSetting.getInstance().setDefaultStatusPrivate(ctx.getContextId(), user.getId(), I(value));
            }

        };
    }

}
