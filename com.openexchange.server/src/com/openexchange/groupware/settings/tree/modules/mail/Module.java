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
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * Contains initialization for the modules configuration tree setting webmail.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Module implements PreferencesItemService {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Module.class);

    /**
     * Default constructor.
     */
    public Module() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPath() {
        return new String[] { "modules", "mail", "module" };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void getValue(final Session session, final Context ctx,
                final User user, final UserConfiguration userConfig,
                final Setting setting) throws OXException {
                // Check if multiple mail accounts are enabled
                if (userConfig.isMultipleMailAccounts() && (ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class) != null)) {
                    setting.setSingleValue(Boolean.TRUE);
                    return;
                }
                // Check if primary mail account is connectable
                MailAccess<?, ?> mail = null;
                try {
                    mail = MailAccess.getInstance(session);
                    mail.connect();
                    setting.setSingleValue(Boolean.TRUE);
                } catch (OXException e) {
                    setting.setSingleValue(Boolean.FALSE);
                    LOG.error("", e);
                } finally {
                    if (null != mail) {
                        mail.close(true);
                    }
                }
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }
        };
    }
}
