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
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * Adds the configuration tree entry for the possible reload times of the GUI.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ReloadTimes implements PreferencesItemService {

    public static final String NAME = "reloadtimes";

    /**
     * Default constructor.
     */
    public ReloadTimes() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPath() {
        return new String[] { NAME };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {
            private static final int MINUTE = 60 * 1000;
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
            public void getValue(final Session session, final Context ctx,
                final User user, final UserConfiguration userConfig,
                final Setting setting) throws OXException {
                setting.addMultiValue(Integer.valueOf(0)); // Never
                setting.addMultiValue(Integer.valueOf(5 * MINUTE));
                setting.addMultiValue(Integer.valueOf(10 * MINUTE));
                setting.addMultiValue(Integer.valueOf(15 * MINUTE));
                setting.addMultiValue(Integer.valueOf(30 * MINUTE));
            }
        };
    }
}
