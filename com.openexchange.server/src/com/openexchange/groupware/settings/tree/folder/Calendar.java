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

package com.openexchange.groupware.settings.tree.folder;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.user.User;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Calendar implements PreferencesItemService {

    /**
     * Default constructor.
     */
    public Calendar() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPath() {
        return new String[] { "folder", "calendar" };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {

            @Override
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasCalendar();
            }

            @Override
            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
                if (false == user.isGuest()) {
                    setting.setSingleValue(String.valueOf(new OXFolderAccess(ctx).getDefaultFolderID(user.getId(), FolderObject.CALENDAR)));
                }
            }
        };
    }
}
