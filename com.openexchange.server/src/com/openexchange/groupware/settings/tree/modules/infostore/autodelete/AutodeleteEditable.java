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

package com.openexchange.groupware.settings.tree.modules.infostore.autodelete;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.autodelete.InfostoreAutodeleteSettings;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.IValueHandlerExtended;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.SettingExceptionCodes;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.session.Session;
import com.openexchange.user.User;


/**
 * {@link AutodeleteEditable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class AutodeleteEditable implements PreferencesItemService, ConfigTreeEquivalent {

    /**
     * Initializes a new {@link AutodeleteEditable}.
     */
    public AutodeleteEditable() {
        super();
    }

    @Override
    public String[] getPath() {
        return new String[] { "modules", "infostore", "features", "autodelete", "editable" };
    }

    @Override
    public String getConfigTreePath() {
        return "modules/infostore/features/autodelete/editable";
    }

    @Override
    public String getJslobPath() {
        return "io.ox/files//features/autodelete/editable";
    }

    @Override
    public IValueHandler getSharedValue() {
        return new IValueHandlerExtended() {

            @Override
            public void writeValue(Session session, Context ctx, User user, Setting setting) throws OXException {
                throw SettingExceptionCodes.NO_WRITE.create(setting.getName());
            }

            @Override
            public boolean isWritable() {
                return false;
            }

            @Override
            public boolean isWritable(Session session) throws OXException {
                return false;
            }

            @Override
            public boolean isAvailable(UserConfiguration userConfig) {
                return userConfig.hasInfostore();
            }

            @Override
            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
                boolean editable = InfostoreAutodeleteSettings.mayChangeAutodeleteSettings(session);
                setting.setSingleValue(Boolean.valueOf(editable));
            }

            @Override
            public int getId() {
                return NO_ID;
            }

            @Override
            public boolean isAvailable(Session session, UserConfiguration userConfig) throws OXException {
                if (false == userConfig.hasInfostore()) {
                    return false;
                }

                return InfostoreAutodeleteSettings.hasAutodeleteCapability(session);
            }
        };
    }

}
