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

import org.json.JSONArray;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * {@link PhishingHeaders} - Requests configured header names which identify
 * phishing headers
 * <p>
 * Path in config tree:<br>
 * <code>modules -&gt; mail -&gt; phishingheaders</code>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PhishingHeaders implements PreferencesItemService {

    /**
     * Default constructor.
     */
    public PhishingHeaders() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPath() {
        return new String[] { "modules", "mail", "phishingheaders" };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {
            @Override
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }

            @Override
            public void getValue(final Session session, final Context ctx, final User user,
                    final UserConfiguration userConfig, final Setting setting) throws OXException {
                final String[] phishingHeaders = MailProperties.getInstance().getPhishingHeaders(session.getUserId(), session.getContextId());
                if (null == phishingHeaders || phishingHeaders.length == 0) {
                    setting.setSingleValue(null);
                } else {
                    final JSONArray jArray = new JSONArray(phishingHeaders.length);
                    for (final String phishingHeader : phishingHeaders) {
                        jArray.put(phishingHeader);
                    }
                    setting.setSingleValue(jArray);
                }
            }
        };
    }
}
