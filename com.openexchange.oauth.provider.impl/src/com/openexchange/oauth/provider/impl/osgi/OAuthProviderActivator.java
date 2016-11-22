/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.dispatcher.DispatcherPrefixService;
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
import com.openexchange.oauth.provider.impl.groupware.AuthCodeCreateTableService;
import com.openexchange.oauth.provider.impl.groupware.AuthCodeCreateTableTask;
import com.openexchange.oauth.provider.impl.groupware.CreateOAuthGrantTableService;
import com.openexchange.oauth.provider.impl.groupware.CreateOAuthGrantTableTask;
import com.openexchange.oauth.provider.impl.groupware.OAuthProviderDeleteListener;
import com.openexchange.oauth.provider.impl.servlets.AuthorizationEndpoint;
import com.openexchange.oauth.provider.impl.servlets.RevokeEndpoint;
import com.openexchange.oauth.provider.impl.servlets.TokenEndpoint;
import com.openexchange.oauth.provider.impl.servlets.TokenInfo;
import com.openexchange.oauth.provider.resourceserver.OAuthResourceService;
import com.openexchange.oauth.provider.resourceserver.scope.OAuthScopeProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
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
        return new Class<?>[] { DatabaseService.class, ConfigurationService.class, ContextService.class, UserService.class,
            HttpService.class, DispatcherPrefixService.class, CryptoService.class, CacheService.class, ServerConfigService.class,
 SessiondService.class, CapabilityService.class, ConfigViewFactory.class, NotificationMailFactory.class, HazelcastConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final ServiceLookup serviceLookup = this;
        Services.setServiceLookup(serviceLookup);

        ConfigurationService configService = getService(ConfigurationService.class);
        boolean providerEnabled = configService.getBoolProperty(OAuthProviderProperties.ENABLED, false);
        if (!providerEnabled) {
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

        boolean isAuthorizationServer = configService.getBoolProperty(OAuthProviderProperties.IS_AUTHORIZATION_SERVER, true);
        if (isAuthorizationServer) {
            if ("hz".equalsIgnoreCase(configService.getProperty(OAuthProviderProperties.AUTHCODE_TYPE, "hz").trim())) {
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
        }

        track(OAuthAuthorizationService.class, new SimpleRegistryListener<OAuthAuthorizationService>() {

            @Override
            public void added(ServiceReference<OAuthAuthorizationService> ref, OAuthAuthorizationService service) {
                registerService(OAuthResourceService.class, new OAuthResourceServiceImpl(service, serviceLookup));
            }

            @Override
            public void removed(ServiceReference<OAuthAuthorizationService> ref, OAuthAuthorizationService service) {
                unregisterService(service);
            }

        });

        openTrackers();
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
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new AuthCodeCreateTableTask(this)));
        registerService(CreateTableService.class, new CreateOAuthGrantTableService());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new CreateOAuthGrantTableTask(this)));
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
    }

    private void stopAuthorizationServer() {
        ServiceReference<HttpService> httpServiceRef = context.getServiceReference(HttpService.class);
        if (httpServiceRef != null) {
            try {
                HttpService httpService = context.getService(httpServiceRef);
                if (httpService != null) {
                    while (!registeredServlets.isEmpty()) {
                        String alias = registeredServlets.pop();
                        try {
                            httpService.unregister(alias);
                        } catch (Exception e) {
                            LOG.error("Could not unregister servlet '{}'", alias, e);
                        }
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
            "jcs.region." + regionName + ".elementattributes.IsLateral=false\n").getBytes();
        getService(CacheService.class).loadConfiguration(new ByteArrayInputStream(ccf), true);
        return new CachingOAuthClientStorage(new RdbOAuthClientStorage(this), this);
    }

    private final class HzTracker implements ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance> {

        private final String hzMapName;

        public HzTracker(String hzMapName) {
            super();
            this.hzMapName = hzMapName;
        }

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

        @Override
        public void removedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
            stopAuthorizationServer();
            removeService(HazelcastInstance.class);
            context.ungetService(reference);
        }
    }

}
