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
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.impl.AbstractUserFuncs;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * Configuration tree entry for the language of the user.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Language implements PreferencesItemService {

    private static final String NAME = "language";

    public Language() {
        super();
    }

    @Override
    public String[] getPath() {
        return new String[] { NAME };
    }

    @Override
    public IValueHandler getSharedValue() {
        return  new AbstractUserFuncs() {
            @Override
            public void getValue(final Session session, final Context ctx, final User user, final UserConfiguration userConfig, final Setting setting) throws OXException {
                setting.setSingleValue(user.getPreferredLanguage());
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
            protected void setValue(final UserImpl newUser, final String value, final User originalUser) {
                newUser.setPreferredLanguage(value);
            }
        };
    }
}
