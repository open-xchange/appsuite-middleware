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
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.mail.config.MailAccountProperties;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * {@link IgnoreSubscription}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class IgnoreSubscription implements PreferencesItemService, ConfigTreeEquivalent {

    /**
     * Initializes a new {@link IgnoreSubscription}.
     */
    public IgnoreSubscription() {
        super();
    }

    @Override
    public String[] getPath() {
        return new String[] { "modules", "mail", "ignoresubscription" };
    }

    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {

            @Override
            public boolean isAvailable(UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }

            @Override
            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
                MailAccountProperties mailProperties = new MailAccountProperties(null, session.getUserId(), session.getContextId());
                boolean ignoreSubscription = mailProperties.isIgnoreSubscription();
                setting.setSingleValue(Boolean.valueOf(ignoreSubscription));
            }
        };
    }

    @Override
    public String getConfigTreePath() {
        return "modules/mail/ignoresubscription";
    }

    @Override
    public String getJslobPath() {
        return "io.ox/mail//ignoreSubscription";
    }

}
