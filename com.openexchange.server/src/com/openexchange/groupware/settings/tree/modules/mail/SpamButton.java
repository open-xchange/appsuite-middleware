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

package com.openexchange.groupware.settings.tree.modules.mail;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * Tells the UI if the spam button should be shown or not.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class SpamButton implements PreferencesItemService {

    public SpamButton() {
        super();
    }

    @Override
    public String[] getPath() {
        return new String[] { "modules", "mail", "spambutton" };
    }

    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {
            @Override
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }
            @Override
            public void getValue(final Session session, final Context ctx, final User user, final UserConfiguration userConfig, final Setting setting) throws OXException {
                final UserSettingMail settings = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
                setting.setSingleValue(Boolean.valueOf(settings.isSpamEnabled()));
            }
        };
    }
}
