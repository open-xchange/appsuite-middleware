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

package com.openexchange.oauth.provider.impl.osgi;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Stack;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.authorization.AuthorizationService;
import com.openexchange.caching.CacheService;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.mail.service.MailService;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagement;
import com.openexchange.oauth.provider.authorizationserver.grant.GrantManagement;
import com.openexchange.oauth.provider.authorizationserver.spi.OAuthAuthorizationService;
import com.openexchange.oauth.provider.impl.DefaultAuthorizationService;
import com.openexchange.oauth.provider.impl.OAuthProviderConstants;
import com.openexchange.oauth.provider.impl.OAuthProviderMode;
import com.openexchange.oauth.provider.impl.OAuthProviderProperties;
import com.openexchange.oauth.provider.impl.OAuthResourceServiceImpl;
import com.openexchange.oauth.provider.impl.authcode.AbstractAuthorizationCodeProvider;
import com.openexchange.oauth.provider.impl.authcode.DbAuthorizationCodeProvider;
import com.openexchange.oauth.provider.impl.authcode.HzAuthorizationCodeProvider;
import com.openexchange.oauth.provider.impl.authcode.portable.PortableAuthCodeInfoFactory;
import com.openexchange.oauth.provider.impl.client.ClientManagementImpl;
import com.openexchange.oauth.provider.impl.client.storage.CachingOAuthClientStorage;
import com.openexchange.oauth.provider.impl.client.storage.OAuthClientStorage;
import com.openexchange.oauth.provider.impl.client.storage.RdbOAuthClientStorage;
import com.openexchange.oauth.provider.impl.grant.DbGrantStorage;
import com.openexchange.oauth.provider.impl.grant.GrantManagementImpl;
import com.openexchange.oauth.provider.impl.grant.OAuthGrantStorage;
import com.openexchange.oauth.provider.impl.groupware.AuthCodeConvertUtf8ToUtf8mb4Task;
import com.openexchange.oauth.provider.impl.groupware.AuthCodeCreateTableService;
import com.openexchange.oauth.provider.impl.groupware.AuthCodeCreateTableTask;
import com.openexchange.oauth.provider.impl.groupware.CreateOAuthGrantTableService;
import com.openexchange.oauth.provider.impl.groupware.CreateOAuthGrantTableTask;
import com.openexchange.oauth.provider.impl.groupware.OAuthGrantConvertUtf8ToUtf8mb4Task;
import com.openexchange.oauth.provider.impl.groupware.OAuthProviderDeleteListener;
import com.openexchange.oauth.provider.impl.introspection.OAuthIntrospectionAuthorizationService;
import com.openexchange.oauth.provider.impl.jwt.OAuthJWTScopeService;
import com.openexchange.oauth.provider.impl.jwt.OAuthJwtAuthorizationService;
import com.openexchange.oauth.provider.impl.servlets.AuthorizationEndpoint;
import com.openexchange.oauth.provider.impl.servlets.RevokeEndpoint;
import com.openexchange.oauth.provider.impl.servlets.TokenEndpoint;
import com.openexchange.oauth.provider.impl.servlets.TokenInfo;
import com.openexchange.oauth.provider.impl.servlets.TokenIntrospection;
import com.openexchange.oauth.provider.resourceserver.OAuthResourceService;
import com.openexchange.oauth.provider.resourceserver.scope.OAuthScopeProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.osgi.service.http.HttpServices;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.UserService;

/**
 * {@link OAuthProviderActivator} - The activator for OAuth provider implementation bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OAuthProviderActivator extends HousekeepingActivator {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(OAuthProviderActivator.class);

    private final Stack<String> registeredServlets = new Stack<>();

    /**
     * Initializes a new {@link OAuthProviderActivator}.
     */
    public OAuthProviderActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, LeanConfigurationService.class, ConfigurationService.class, ContextService.class, UserService.class,
            HttpService.class, DispatcherPrefixService.class, CryptoService.class, CacheService.class, ServerConfigService.class,
            SessiondService.class, CapabilityService.class, ConfigViewFactory.class, NotificationMailFactory.class, HazelcastConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final ServiceLookup serviceLookup = this;
        Services.setServiceLookup(serviceLookup);

        LeanConfigurationService leanConfigService = getServiceSafe(LeanConfigurationService.class);
        if (!leanConfigService.getBooleanProperty(OAuthProviderProperties.ENABLED)) {
            LOG.info("OAuth provider is disabled by configuration.");
            return;
        }

        trackService(HostnameService.class);
        trackService(AuthorizationService.class);
        trackService(TemplateService.class);
        trackService(TranslatorFactory.class);
        trackService(HtmlService.class);
        trackService(MailService.class);
        track(OAuthScopeProvider.class, new OAuthScopeProviderTracker(context));

        OAuthProviderMode mode = OAuthProviderMode.getProviderMode(leanConfigService.getProperty(OAuthProviderProperties.MODE));
        switch (mode) {
            default:
            case AUTH_SEVER:
                if ("hz".equalsIgnoreCase(leanConfigService.getProperty(OAuthProviderProperties.AUTHCODE_TYPE).trim())) {
                    final HazelcastConfigurationService hzConfigService = getService(HazelcastConfigurationService.class);
                    if (!hzConfigService.isEnabled()) {
                        String msg = "OAuth provider is configured to use Hazelcast, but Hazelcast is disabled as per configuration! Aborting start of OAuth provider!";
                        LOG.error(msg, new Exception(msg));
                        return;
                    }
                    registerService(CustomPortableFactory.class, new PortableAuthCodeInfoFactory());

                    String hzMapName = discoverHzMapName(hzConfigService.getConfig(), HzAuthorizationCodeProvider.HZ_MAP_NAME, LOG);
                    track(HazelcastInstance.class, new HzTracker(hzMapName));
                } else {
                    startAuthorizationServer(new DbAuthorizationCodeProvider(this));
                }
                break;
            case EXPECT_JWT:
                startJWTService(leanConfigService);
                break;
            case TOKEN_INTROSPECTION:
                startTokenIntrospectionService(leanConfigService);
                break;
        }

        RankingAwareNearRegistryServiceTracker<OAuthAuthorizationService> oAuthAuthorizationService = new RankingAwareNearRegistryServiceTracker<OAuthAuthorizationService>(context, OAuthAuthorizationService.class);

        rememberTracker(oAuthAuthorizationService);

        openTrackers();

        registerService(OAuthResourceService.class, new OAuthResourceServiceImpl(oAuthAuthorizationService, serviceLookup));

    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected void stopBundle() throws Exception {
        stopAuthorizationServer();
        Services.setServiceLookup(null);
        super.stopBundle();
    }

    private void startAuthorizationServer(AbstractAuthorizationCodeProvider authCodeProvider) throws Exception {
        // Register update task, create table job and delete listener
        registerService(CreateTableService.class, new AuthCodeCreateTableService());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new AuthCodeCreateTableTask(), new AuthCodeConvertUtf8ToUtf8mb4Task()));
        registerService(CreateTableService.class, new CreateOAuthGrantTableService());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new CreateOAuthGrantTableTask(), new OAuthGrantConvertUtf8ToUtf8mb4Task()));
        registerService(DeleteListener.class, new OAuthProviderDeleteListener());

        // Register client and grant management
        OAuthGrantStorage grantStorage = new DbGrantStorage(this);
        OAuthClientStorage clientStorage = initClientStorage();
        ClientManagementImpl clientManagement = new ClientManagementImpl(clientStorage, grantStorage);
        registerService(ClientManagement.class, clientManagement, null);
        GrantManagementImpl grantManagement = new GrantManagementImpl(authCodeProvider, clientManagement, grantStorage, this);
        registerService(GrantManagement.class, grantManagement, null);

        // Register default authorization service
        DefaultAuthorizationService authorizationService = new DefaultAuthorizationService(clientManagement, grantStorage);
        registerService(OAuthAuthorizationService.class, authorizationService, null);

        // Register endpoints
        HttpService httpService = getService(HttpService.class);
        DispatcherPrefixService dispatcherPrefixService = getService(DispatcherPrefixService.class);
        String prefix = dispatcherPrefixService.getPrefix();

        {
            AuthorizationEndpoint authorizationEndpoint = new AuthorizationEndpoint(clientManagement, grantManagement, this);
            String authorizationEndpointAlias = prefix + OAuthProviderConstants.AUTHORIZATION_SERVLET_ALIAS;
            httpService.registerServlet(authorizationEndpointAlias, authorizationEndpoint, null, httpService.createDefaultHttpContext());
            registeredServlets.add(authorizationEndpointAlias);
        }
        {
            TokenEndpoint tokenEndpoint = new TokenEndpoint(clientManagement, grantManagement, this);
            String tokenEndpointAlias = prefix + OAuthProviderConstants.ACCESS_TOKEN_SERVLET_ALIAS;
            httpService.registerServlet(tokenEndpointAlias, tokenEndpoint, null, httpService.createDefaultHttpContext());
            registeredServlets.add(tokenEndpointAlias);
        }
        {
            RevokeEndpoint revokeEndpoint = new RevokeEndpoint(clientManagement, grantManagement, this);
            String revokeEndpointAlias = prefix + OAuthProviderConstants.REVOKE_SERVLET_ALIAS;
            httpService.registerServlet(revokeEndpointAlias, revokeEndpoint, null, httpService.createDefaultHttpContext());
            registeredServlets.add(revokeEndpointAlias);
        }
        {
            TokenInfo tokenInfo = new TokenInfo(authorizationService, clientManagement, grantManagement, this);
            String tokenInfoAlias = prefix + OAuthProviderConstants.TOKEN_INFO_SERVLET_ALIAS;
            httpService.registerServlet(tokenInfoAlias, tokenInfo, null, httpService.createDefaultHttpContext());
            registeredServlets.add(tokenInfoAlias);
        }
        {
            TokenIntrospection tokenIntrospection = new TokenIntrospection(authorizationService, clientManagement, grantManagement, this);
            String tokenIntrospectionAlias = prefix + OAuthProviderConstants.TOKEN_INTROSPECTION_SERVLET_ALIAS;
            httpService.registerServlet(tokenIntrospectionAlias, tokenIntrospection, null, httpService.createDefaultHttpContext());
            registeredServlets.add(tokenIntrospectionAlias);
        }
    }

    private void startJWTService(LeanConfigurationService leanConfigService) throws OXException {
    	OAuthJWTScopeService scopeService = new OAuthJWTScopeService(leanConfigService);
    	registerService(Reloadable.class, scopeService);

        OAuthJwtAuthorizationService jwtAuthorizationService = new OAuthJwtAuthorizationService(leanConfigService, scopeService);
        registerService(OAuthAuthorizationService.class, jwtAuthorizationService);
        registerService(ForcedReloadable.class, jwtAuthorizationService);
    }

    private void startTokenIntrospectionService(LeanConfigurationService leanConfigService) {
        OAuthJWTScopeService scopeService = new OAuthJWTScopeService(leanConfigService);
        registerService(Reloadable.class, scopeService);

        OAuthIntrospectionAuthorizationService tokenIntrospectionAuthorizationService = new OAuthIntrospectionAuthorizationService(leanConfigService, scopeService);
        registerService(OAuthAuthorizationService.class, tokenIntrospectionAuthorizationService, null);
        registerService(Reloadable.class, tokenIntrospectionAuthorizationService);
    }

    private void stopAuthorizationServer() {
        ServiceReference<HttpService> httpServiceRef = context.getServiceReference(HttpService.class);
        if (httpServiceRef != null) {
            try {
                HttpService httpService = context.getService(httpServiceRef);
                if (httpService != null) {
                    while (!registeredServlets.isEmpty()) {
                        String alias = registeredServlets.pop();
                        HttpServices.unregister(alias, httpService);
                    }
                }
            } finally {
                context.ungetService(httpServiceRef);
            }
        }
    }

    private String discoverHzMapName(Config config, String mapPrefix, Logger logger) throws IllegalStateException {
        Map<String, MapConfig> mapConfigs = config.getMapConfigs();
        if (null != mapConfigs && !mapConfigs.isEmpty()) {
            for (String mapName : mapConfigs.keySet()) {
                if (mapName.startsWith(mapPrefix)) {
                    logger.info("Using distributed auth-code map '{}'.", mapName);
                    return mapName;
                }
            }
        }
        logger.info("No distributed auth-code map with mapPrefix {} in hazelcast configuration", mapPrefix);
        return null;
    }

    private OAuthClientStorage initClientStorage() throws Exception {
        String regionName = CachingOAuthClientStorage.REGION_NAME;
        byte[] ccf = (
            "jcs.region." + regionName + "=LTCP\n" +
            "jcs.region." + regionName + ".cacheattributes=org.apache.jcs.engine.CompositeCacheAttributes\n" +
            "jcs.region." + regionName + ".cacheattributes.MaxObjects=100000\n" +
            "jcs.region." + regionName + ".cacheattributes.MemoryCacheName=org.apache.jcs.engine.memory.lru.LRUMemoryCache\n" +
            "jcs.region." + regionName + ".cacheattributes.UseMemoryShrinker=true\n" +
            "jcs.region." + regionName + ".cacheattributes.MaxMemoryIdleTimeSeconds=360\n" +
            "jcs.region." + regionName + ".cacheattributes.ShrinkerIntervalSeconds=60\n" +
            "jcs.region." + regionName + ".elementattributes=org.apache.jcs.engine.ElementAttributes\n" +
            "jcs.region." + regionName + ".elementattributes.IsEternal=false\n" + "jcs.region." + regionName + ".elementattributes.MaxLifeSeconds=-1\n" +
            "jcs.region." + regionName + ".elementattributes.IdleTime=360\n" +
            "jcs.region." + regionName + ".elementattributes.IsSpool=false\n" +
            "jcs.region." + regionName + ".elementattributes.IsRemote=false\n" +
            "jcs.region." + regionName + ".elementattributes.IsLateral=false\n").getBytes(com.openexchange.java.Charsets.UTF_8);
        getService(CacheService.class).loadConfiguration(new ByteArrayInputStream(ccf), true);
        return new CachingOAuthClientStorage(new RdbOAuthClientStorage(this), this);
    }

    private final class HzTracker implements ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance> {

        private final String hzMapName;

        public HzTracker(String hzMapName) {
            super();
            this.hzMapName = hzMapName;
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public HazelcastInstance addingService(ServiceReference<HazelcastInstance> reference) {
            HazelcastInstance hzInstance = context.getService(reference);
            if (hzInstance == null) {
                return null;
            }

            try {
                addService(HazelcastInstance.class, hzInstance);
                startAuthorizationServer(new HzAuthorizationCodeProvider(hzMapName, hzInstance));
            } catch (Exception e) {
                context.ungetService(reference);
                LOG.warn("Couldn't initialize distributed token-session map.", e);
            }

            return hzInstance;
        }

        @Override
        public void modifiedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
            // Nothing
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public void removedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
            stopAuthorizationServer();
            removeService(HazelcastInstance.class);
            context.ungetService(reference);
        }
    }

}
