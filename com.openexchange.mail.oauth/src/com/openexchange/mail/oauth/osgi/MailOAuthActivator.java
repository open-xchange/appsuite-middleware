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

package com.openexchange.mail.oauth.osgi;

import com.openexchange.mail.oauth.MailOAuthProvider;
import com.openexchange.mail.oauth.MailOAuthService;
import com.openexchange.mail.oauth.internal.MailOAuthProviderRegistry;
import com.openexchange.mail.oauth.internal.MailOAuthServiceImpl;
import com.openexchange.oauth.OAuthService;
import com.openexchange.osgi.HousekeepingActivator;


/**
 * {@link MailOAuthActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class MailOAuthActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link MailOAuthActivator}.
     */
    public MailOAuthActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { OAuthService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        MailOAuthProviderRegistry registry = new MailOAuthProviderRegistry();
        track(MailOAuthProvider.class, new MailOAuthProviderTracker(registry, context));
        openTrackers();
        registerService(MailOAuthService.class, new MailOAuthServiceImpl(registry, this));
    }

}
