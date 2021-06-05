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

package com.openexchange.authentication.imap.osgi;

import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.imap.impl.IMAPAuthentication;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.UserService;

/**
 * Activator for <code>com.openexchange.authentication.imap</code> bundle.
 */
public class IMAPAuthenticationActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link IMAPAuthenticationActivator}.
     */
    public IMAPAuthenticationActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, ContextService.class, UserService.class, MailAccountStorageService.class, SSLSocketFactoryProvider.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(AuthenticationService.class, new IMAPAuthentication(this));
    }

}
