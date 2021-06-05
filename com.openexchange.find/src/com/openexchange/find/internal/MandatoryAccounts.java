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

package com.openexchange.find.internal;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.find.internal.SearchDriverManager.CachedConfig;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;


/**
 * {@link MandatoryAccounts}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class MandatoryAccounts extends FindSetting {

    private final SearchDriverManager driverManager;

    public MandatoryAccounts(final SearchDriverManager driverManager) {
        super();
        this.driverManager = driverManager;
    }

    @Override
    public String[] getPath() {
        return new String[] { "search", "mandatory", "account" };
    }

    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {

            @Override
            public boolean isAvailable(UserConfiguration userConfig) {
                return true;
            }

            @SuppressWarnings({ "synthetic-access", "null" })
            @Override
            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
                ServerSessionAdapter serverSession = new ServerSessionAdapter(session, ctx, user, userConfig);
                List<CachedConfig> configs = driverManager.getConfigurations(serverSession);
                setting.setEmptyMultiValue();
                for (CachedConfig config : configs) {
                    if (config.getSearchConfig().requiresAccount()) {
                        setting.addMultiValue(config.getModule().getIdentifier());
                    }
                }
            }
        };
    }

}
