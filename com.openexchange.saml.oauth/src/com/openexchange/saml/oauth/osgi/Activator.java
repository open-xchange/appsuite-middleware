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

package com.openexchange.saml.oauth.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.lock.LockService;
import com.openexchange.mail.api.AuthenticationFailedHandler;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.rest.client.httpclient.SpecificHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.saml.oauth.HttpClientOAuthAccessTokenService;
import com.openexchange.saml.oauth.OAuthFailedAuthenticationHandler;
import com.openexchange.saml.oauth.SAMLOAuthConfig;
import com.openexchange.saml.oauth.service.OAuthAccessTokenService;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class Activator extends HousekeepingActivator {

    private static final int SERVICE_RANKING = 100;

    /**
     * Initializes a new {@link Activator}.
     */
    public Activator() {
        super();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { SessiondService.class, ConfigViewFactory.class, SSLSocketFactoryProvider.class, SSLConfigurationService.class, LockService.class, HttpClientService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        HttpClientOAuthAccessTokenService tokenService = new HttpClientOAuthAccessTokenService(this);
        registerService(ForcedReloadable.class, new ForcedReloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                SAMLOAuthConfig.invalidateCache();
            }

        });

        registerService(OAuthAccessTokenService.class, tokenService);
        registerService(AuthenticationFailedHandler.class, new OAuthFailedAuthenticationHandler(tokenService, this), SERVICE_RANKING);
        registerService(SpecificHttpClientConfigProvider.class, new SamlOAuthHttpConfiguration(this));
    }
}
