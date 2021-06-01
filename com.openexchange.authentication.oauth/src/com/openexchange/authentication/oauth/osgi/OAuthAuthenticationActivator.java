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

package com.openexchange.authentication.oauth.osgi;

import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.oauth.http.OAuthAuthenticationHttpClientConfig;
import com.openexchange.authentication.oauth.impl.DefaultOAuthAuthenticationConfig;
import com.openexchange.authentication.oauth.impl.OAuthAuthenticationConfig;
import com.openexchange.authentication.oauth.impl.PasswordGrantAuthentication;
import com.openexchange.authentication.oauth.impl.PasswordGrantAuthenticationFailedHandler;
import com.openexchange.authentication.oauth.impl.PasswordGrantSessionInspector;
import com.openexchange.authentication.oauth.impl.SessionParameters;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.mail.api.AuthenticationFailedHandler;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.rest.client.httpclient.SpecificHttpClientConfigProvider;
import com.openexchange.session.inspector.SessionInspectorService;
import com.openexchange.session.oauth.SessionOAuthTokenService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.SessionStorageParameterNamesProvider;
import com.openexchange.user.UserService;
import com.openexchange.version.VersionService;

/**
 * {@link OAuthAuthenticationActivator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class OAuthAuthenticationActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link OAuthAuthenticationActivator}.
     */
    public OAuthAuthenticationActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            LeanConfigurationService.class,
            ContextService.class,
            UserService.class,
            SessiondService.class,
            SessionOAuthTokenService.class,
            ConfigurationService.class
        };
    }

    @Override
    protected void startBundle() throws Exception {
        trackService(VersionService.class);
        trackService(HttpClientService.class);
        openTrackers();

        // Initialize configuration for out-bound HTTP traffic
        registerService(SpecificHttpClientConfigProvider.class, new OAuthAuthenticationHttpClientConfig(this));

        OAuthAuthenticationConfig config = new DefaultOAuthAuthenticationConfig(getService(LeanConfigurationService.class));
        PasswordGrantAuthentication passwordGrantAuthentication = new PasswordGrantAuthentication(config, this);
        registerService(AuthenticationService.class, passwordGrantAuthentication);
        registerService(SessionStorageParameterNamesProvider.class, new SessionParameters());
        PasswordGrantSessionInspector sessionInspector =
            new PasswordGrantSessionInspector(config, this);
        registerService(SessionInspectorService.class, sessionInspector);
        registerService(AuthenticationFailedHandler.class, new PasswordGrantAuthenticationFailedHandler(), 100);
    }

    @Override
    protected void stopBundle() throws Exception {
        HttpClientService httpClientService = getService(HttpClientService.class);
        if (httpClientService != null) {
            httpClientService.destroyHttpClient(OAuthAuthenticationHttpClientConfig.getClientIdOAuthAuthentication());
        }
        super.stopBundle();
    }

}
