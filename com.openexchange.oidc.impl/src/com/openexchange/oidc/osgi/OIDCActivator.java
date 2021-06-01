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
package com.openexchange.oidc.osgi;

import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.lock.LockService;
import com.openexchange.login.listener.LoginListener;
import com.openexchange.mail.api.AuthenticationFailedHandler;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.oidc.OIDCBackend;
import com.openexchange.oidc.http.outbound.OIDCHttpClientConfig;
import com.openexchange.oidc.hz.PortableAuthenticationRequestFactory;
import com.openexchange.oidc.hz.PortableLogoutRequestFactory;
import com.openexchange.oidc.impl.OIDCAuthenticationFailedHandler;
import com.openexchange.oidc.impl.OIDCConfigImpl;
import com.openexchange.oidc.impl.OIDCPasswordGrantAuthentication;
import com.openexchange.oidc.impl.OIDCSessionInspectorService;
import com.openexchange.oidc.impl.OIDCSessionParameterNamesProvider;
import com.openexchange.oidc.impl.OIDCSessionSsoProvider;
import com.openexchange.oidc.spi.OIDCCoreBackend;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.rest.client.httpclient.SpecificHttpClientConfigProvider;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.SessionSsoProvider;
import com.openexchange.session.inspector.SessionInspectorService;
import com.openexchange.session.oauth.SessionOAuthTokenService;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.SessionStorageParameterNamesProvider;
import com.openexchange.sessionstorage.SessionStorageService;
import com.openexchange.user.UserService;
import com.openexchange.version.VersionService;

/**
 * Activates the OpenID feature.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class OIDCActivator extends HousekeepingActivator{

    private OIDCBackendRegistry oidcBackendRegistry;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[]
        {
            LeanConfigurationService.class,
            ConfigurationService.class,
            HttpService.class,
            DispatcherPrefixService.class,
            HazelcastInstance.class,
            SessionReservationService.class,
            ContextService.class,
            UserService.class,
            SessiondService.class,
            ServerConfigService.class,
            SessionOAuthTokenService.class
        };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    private void getOIDCBackends(ServiceLookup services) {
        if (this.oidcBackendRegistry == null) {
            this.oidcBackendRegistry = new OIDCBackendRegistry(context, services);
            this.oidcBackendRegistry.open();
        }
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        Services.setServices(this);
        trackService(SessionStorageService.class);
        trackService(LockService.class);
        trackService(SSLSocketFactoryProvider.class);
        trackService(VersionService.class);
        trackService(HttpClientService.class);
        openTrackers();

        // Initialize configuration for outbound HTTP traffic
        registerService(SpecificHttpClientConfigProvider.class, new OIDCHttpClientConfig());

        OIDCConfigImpl config = new OIDCConfigImpl(this);

        Logger logger = org.slf4j.LoggerFactory.getLogger(OIDCActivator.class);
        if (config.isEnabled()) {
            logger.info("Starting core OpenID Connect support... ");
            getOIDCBackends(this);
            registerService(SessionInspectorService.class, new OIDCSessionInspectorService(oidcBackendRegistry, getService(SessionOAuthTokenService.class)));
            registerService(AuthenticationFailedHandler.class, new OIDCAuthenticationFailedHandler(), 100);
            registerService(CustomPortableFactory.class, new PortableAuthenticationRequestFactory(), null);
            registerService(CustomPortableFactory.class, new PortableLogoutRequestFactory(), null);

            if (config.isPasswordGrantEnabled()) {
                OIDCPasswordGrantAuthentication passwordGrantAuth = new OIDCPasswordGrantAuthentication(oidcBackendRegistry, getService(ServerConfigService.class));
                registerService(AuthenticationService.class, passwordGrantAuth);
                registerService(LoginListener.class, passwordGrantAuth);
            }
        } else {
            logger.info("OpenID Connect support is disabled by configuration. Skipping initialization...");
        }

        //register default oidc backend if configured
        if (config.startDefaultBackend()) {
            registerService(OIDCBackend.class, new OIDCCoreBackend() , null);
        }
        registerService(SessionStorageParameterNamesProvider.class, new OIDCSessionParameterNamesProvider());
        registerService(SessionSsoProvider.class, new OIDCSessionSsoProvider());
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        OIDCBackendRegistry oidcBackends = this.oidcBackendRegistry;
        if (null != oidcBackends) {
            this.oidcBackendRegistry = null;
            oidcBackends.close();
        }
        HttpClientService httpClientService = getService(HttpClientService.class);
        if (httpClientService != null) {
            httpClientService.destroyHttpClient(OIDCHttpClientConfig.getClientIdOidc());
        }
        Services.setServices(null);
        super.stopBundle();
    }
}
