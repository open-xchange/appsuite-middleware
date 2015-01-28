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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.osgi;

import static com.openexchange.osgi.Tools.requireService;
import java.io.ByteArrayInputStream;
import java.util.Map;
import javax.servlet.ServletException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
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
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.OAuthResourceService;
import com.openexchange.oauth.provider.OAuthScopeProvider;
import com.openexchange.oauth.provider.groupware.AuthCodeCreateTableService;
import com.openexchange.oauth.provider.groupware.AuthCodeCreateTableTask;
import com.openexchange.oauth.provider.groupware.AuthCodeDeleteListener;
import com.openexchange.oauth.provider.internal.OAuthProviderServiceImpl;
import com.openexchange.oauth.provider.internal.OAuthResourceServiceImpl;
import com.openexchange.oauth.provider.internal.authcode.DbAuthorizationCodeProvider;
import com.openexchange.oauth.provider.internal.authcode.HzAuthorizationCodeProvider;
import com.openexchange.oauth.provider.internal.authcode.portable.PortableAuthCodeInfoFactory;
import com.openexchange.oauth.provider.internal.client.CachingOAuthClientStorage;
import com.openexchange.oauth.provider.internal.client.OAuthClientStorage;
import com.openexchange.oauth.provider.internal.client.RdbOAuthClientStorage;
import com.openexchange.oauth.provider.servlets.AuthorizationEndpoint;
import com.openexchange.oauth.provider.servlets.TokenEndpoint;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;

/**
 * {@link OAuthProviderActivator} - The activator for OAuth provider implementation bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OAuthProviderActivator extends HousekeepingActivator {

    private static final class HzConfigTracker implements ServiceTrackerCustomizer<HazelcastConfigurationService, HazelcastConfigurationService> {

        final BundleContext context;
        final OAuthProviderActivator activator;
        private volatile ServiceTracker<HazelcastInstance, HazelcastInstance> hzInstanceTracker;

        HzConfigTracker(BundleContext context, OAuthProviderActivator activator) {
            super();
            this.context = context;
            this.activator = activator;
        }

        @Override
        public HazelcastConfigurationService addingService(ServiceReference<HazelcastConfigurationService> reference) {
            final HazelcastConfigurationService hzConfigService = context.getService(reference);
            final Logger logger = org.slf4j.LoggerFactory.getLogger(OAuthProviderActivator.class);

            try {
                boolean hzEnabled = hzConfigService.isEnabled();
                if (false == hzEnabled) {
                    String msg = "Authorization-Code service is configured to use Hazelcast, but Hazelcast is disabled as per configuration! Start of Authorization-Code service aborted!";
                    logger.error(msg, new Exception(msg));

                    context.ungetService(reference);
                    return null;
                }

                final BundleContext context = this.context;
                ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance> stc = new ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>() {

                    @Override
                    public HazelcastInstance addingService(ServiceReference<HazelcastInstance> reference) {
                        HazelcastInstance hzInstance = context.getService(reference);

                        try {
                            String hzMapName = discoverHzMapName(hzConfigService.getConfig(), HzAuthorizationCodeProvider.HZ_MAP_NAME, logger);
                            if (null == hzMapName) {
                                context.ungetService(reference);
                                return null;
                            }

                            // Add to service look-up
                            activator.addService(HazelcastInstance.class, hzInstance);
                            OAuthProviderServiceImpl oAuthProvider = new OAuthProviderServiceImpl(activator, new HzAuthorizationCodeProvider(hzMapName, activator));
                            registerServlets(activator, oAuthProvider);
                            OAuthResourceServiceImpl resourceService = new OAuthResourceServiceImpl(oAuthProvider);
                            activator.registerService(OAuthResourceService.class, resourceService);
                            return hzInstance;
                        } catch (Exception e) {
                            logger.warn("Couldn't initialize distributed token-session map.", e);
                        }

                        // Something went wrong... Unget tracked service
                        context.ungetService(reference);
                        return null;
                    }

                    @Override
                    public void modifiedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
                        // Nothing
                    }

                    @Override
                    public void removedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
                        Logger logger = org.slf4j.LoggerFactory.getLogger(OAuthProviderActivator.class);
                        logger.info("Unegistering OAuth servlets due to Hazelcast absence");
                        activator.removeService(HazelcastInstance.class);
                        try {
                            unregisterServlets(activator);
//                            activator.unregisterService(service); // TODO
                        } catch (Exception e) {
                            logger.error("Could not unregister OAuth servlets", e);
                        }
                        context.ungetService(reference);
                    }
                }; // End of ServiceTrackerCustomizer definition

                ServiceTracker<HazelcastInstance, HazelcastInstance> hzInstanceTracker = new ServiceTracker<HazelcastInstance, HazelcastInstance>(context, HazelcastInstance.class, stc);
                this.hzInstanceTracker = hzInstanceTracker;
                hzInstanceTracker.open();

                return hzConfigService;
            } catch (Exception e) {
                logger.warn("Failed to start Authorization-Code service!", e);
            }

            context.ungetService(reference);
            return null;
        }

        @Override
        public void modifiedService(ServiceReference<HazelcastConfigurationService> reference, HazelcastConfigurationService service) {
            // Ignore
        }

        @Override
        public void removedService(ServiceReference<HazelcastConfigurationService> reference, HazelcastConfigurationService service) {
            ServiceTracker<HazelcastInstance, HazelcastInstance> hzInstanceTracker = this.hzInstanceTracker;
            if (null != hzInstanceTracker) {
                hzInstanceTracker.close();
                this.hzInstanceTracker = null;
            }

            context.ungetService(reference);
        }

        String discoverHzMapName(Config config, String mapPrefix, Logger logger) throws IllegalStateException {
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

    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link OAuthProviderActivator}.
     */
    public OAuthProviderActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, ConfigurationService.class, ContextService.class, UserService.class,
            HttpService.class, DispatcherPrefixService.class, CryptoService.class, CacheService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = this.context;

        // Create & register portable factory
        registerService(CustomPortableFactory.class, new PortableAuthCodeInfoFactory());

        ConfigurationService configService = getService(ConfigurationService.class);
        if ("hz".equalsIgnoreCase(configService.getProperty("com.openexchange.oauth.provider.authcode.type", "hz").trim())) {
            // Start tracking for Hazelcast
            track(HazelcastConfigurationService.class, new HzConfigTracker(context, this));
        } else {
            OAuthProviderServiceImpl oAuthProvider = new OAuthProviderServiceImpl(this, new DbAuthorizationCodeProvider(this));
            registerServlets(this, oAuthProvider);
            OAuthResourceServiceImpl resourceService = new OAuthResourceServiceImpl(oAuthProvider);
            registerService(OAuthResourceService.class, resourceService);
        }

        trackService(HostnameService.class);
        track(OAuthScopeProvider.class, new OAuthScopeProviderTracker(context));
        openTrackers();

        try {
            String regionName = CachingOAuthClientStorage.REGION_NAME;
            byte[] ccf = ("jcs.region."+regionName+"=LTCP\n" +
                "jcs.region."+regionName+".cacheattributes=org.apache.jcs.engine.CompositeCacheAttributes\n" +
                "jcs.region."+regionName+".cacheattributes.MaxObjects=100000\n" +
                "jcs.region."+regionName+".cacheattributes.MemoryCacheName=org.apache.jcs.engine.memory.lru.LRUMemoryCache\n" +
                "jcs.region."+regionName+".cacheattributes.UseMemoryShrinker=true\n" +
                "jcs.region."+regionName+".cacheattributes.MaxMemoryIdleTimeSeconds=360\n" +
                "jcs.region."+regionName+".cacheattributes.ShrinkerIntervalSeconds=60\n" +
                "jcs.region."+regionName+".elementattributes=org.apache.jcs.engine.ElementAttributes\n" +
                "jcs.region."+regionName+".elementattributes.IsEternal=false\n" +
                "jcs.region."+regionName+".elementattributes.MaxLifeSeconds=-1\n" +
                "jcs.region."+regionName+".elementattributes.IdleTime=360\n" +
                "jcs.region."+regionName+".elementattributes.IsSpool=false\n" +
                "jcs.region."+regionName+".elementattributes.IsRemote=false\n" +
                "jcs.region."+regionName+".elementattributes.IsLateral=false\n").getBytes();
            getService(CacheService.class).loadConfiguration(new ByteArrayInputStream(ccf), true);

            // Add appropriate OAuthClientStorage into this service look-up instance
            addService(OAuthClientStorage.class, new CachingOAuthClientStorage(new RdbOAuthClientStorage(this), this));
        } catch (BundleException x) {
            throw new IllegalStateException(x);
        }

        // Register update task, create table job and delete listener
        registerService(CreateTableService.class, new AuthCodeCreateTableService());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new AuthCodeCreateTableTask(this)));
        registerService(DeleteListener.class, new AuthCodeDeleteListener());

    }

    static void registerServlets(ServiceLookup services, OAuthProviderService oAuthProvider) throws ServletException, NamespaceException, OXException {
        AuthorizationEndpoint authorizationEndpoint = new AuthorizationEndpoint(oAuthProvider, services);
        TokenEndpoint tokenEndpoint = new TokenEndpoint(oAuthProvider);

        HttpService httpService = requireService(HttpService.class, services);
        DispatcherPrefixService dispatcherPrefixService = requireService(DispatcherPrefixService.class, services);
        httpService.registerServlet(dispatcherPrefixService.getPrefix() + OAuthProviderConstants.AUTHORIZATION_SERVLET_ALIAS, authorizationEndpoint, null, httpService.createDefaultHttpContext());
        httpService.registerServlet(dispatcherPrefixService.getPrefix() + OAuthProviderConstants.ACCESS_TOKEN_SERVLET_ALIAS, tokenEndpoint, null, httpService.createDefaultHttpContext());
    }

    static void unregisterServlets(ServiceLookup services) {
        HttpService httpService = services.getOptionalService(HttpService.class);
        if (null != httpService) {
            DispatcherPrefixService dispatcherPrefixService = services.getOptionalService(DispatcherPrefixService.class);
            if (null != dispatcherPrefixService) {
                httpService.unregister(dispatcherPrefixService.getPrefix() + OAuthProviderConstants.AUTHORIZATION_SERVLET_ALIAS);
                httpService.unregister(dispatcherPrefixService.getPrefix() + OAuthProviderConstants.ACCESS_TOKEN_SERVLET_ALIAS);
            }
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        unregisterServlets(this);
        super.stopBundle();
    }

    @Override
    public <S> boolean addService(Class<S> clazz, S service) {
        return super.addService(clazz, service);
    }

    @Override
    public <S> boolean removeService(Class<? extends S> clazz) {
        return super.removeService(clazz);
    }

    @Override
    public <S> void registerService(Class<S> clazz, S service) {
        super.registerService(clazz, service);
    }

}
