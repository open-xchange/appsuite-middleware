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

import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
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
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.oauth.provider.OAuthScopeProvider;
import com.openexchange.oauth.provider.groupware.CreateOAuthGrantTableService;
import com.openexchange.oauth.provider.groupware.CreateOAuthGrantTableTask;
import com.openexchange.oauth.provider.groupware.OAuthProviderDeleteListener;
import com.openexchange.oauth.provider.internal.OAuthProviderProperties;
import com.openexchange.oauth.provider.internal.authcode.DbAuthorizationCodeProvider;
import com.openexchange.oauth.provider.internal.authcode.HzAuthorizationCodeProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.UserService;

/**
 * {@link OAuthProviderActivator} - The activator for OAuth provider implementation bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OAuthProviderActivator extends HousekeepingActivator {

    /** The logger constant */
    static final Logger LOG = org.slf4j.LoggerFactory.getLogger(OAuthProviderActivator.class);

    private static final class HzConfigTracker implements ServiceTrackerCustomizer<HazelcastConfigurationService, HazelcastConfigurationService> {

        final OAuthProvider provider;
        final BundleContext context;
        final OAuthProviderActivator activator;
        private volatile ServiceTracker<HazelcastInstance, HazelcastInstance> hzInstanceTracker;

        HzConfigTracker(BundleContext context, OAuthProviderActivator activator, OAuthProvider provider) {
            super();
            this.context = context;
            this.activator = activator;
            this.provider = provider;
        }

        @Override
        public HazelcastConfigurationService addingService(ServiceReference<HazelcastConfigurationService> reference) {
            final HazelcastConfigurationService hzConfigService = context.getService(reference);
            try {
                boolean hzEnabled = hzConfigService.isEnabled();
                if (false == hzEnabled) {
                    String msg = "OAuth 2.0 provider is configured to use Hazelcast, but Hazelcast is disabled as per configuration! Aborting start of OAuth 2.0 provider!";
                    LOG.error(msg, new Exception(msg));

                    context.ungetService(reference);
                    return null;
                }

                final BundleContext context = this.context;
                ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance> stc = new ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>() {

                    @Override
                    public HazelcastInstance addingService(ServiceReference<HazelcastInstance> reference) {
                        HazelcastInstance hzInstance = context.getService(reference);
                        try {
                            String hzMapName = discoverHzMapName(hzConfigService.getConfig(), HzAuthorizationCodeProvider.HZ_MAP_NAME, LOG);
                            if (null == hzMapName) {
                                context.ungetService(reference);
                                return null;
                            }

                            // Add to service look-up
                            activator.addService(HazelcastInstance.class, hzInstance);
                            provider.start(new HzAuthorizationCodeProvider(hzMapName, activator));
                            return hzInstance;
                        } catch (Exception e) {
                            LOG.warn("Couldn't initialize distributed token-session map.", e);
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
                            provider.stop();
                        } catch (Exception e) {
                            LOG.error("Could not orderly shutdown OAuth 2.0 provider", e);
                        }
                        context.ungetService(reference);
                    }
                }; // End of ServiceTrackerCustomizer definition

                ServiceTracker<HazelcastInstance, HazelcastInstance> hzInstanceTracker = new ServiceTracker<HazelcastInstance, HazelcastInstance>(context, HazelcastInstance.class, stc);
                this.hzInstanceTracker = hzInstanceTracker;
                hzInstanceTracker.open();

                return hzConfigService;
            } catch (Exception e) {
                LOG.warn("Failed to start Authorization-Code service!", e);
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

    private volatile OAuthProvider provider;

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
        Services.setServiceLookup(this);
        final BundleContext context = this.context;

        // Register update task, create table job and delete listener
//        registerService(CreateTableService.class, new AuthCodeCreateTableService());
//        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new AuthCodeCreateTableTask(this)));
        registerService(CreateTableService.class, new CreateOAuthGrantTableService());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new CreateOAuthGrantTableTask(this)));
        registerService(DeleteListener.class, new OAuthProviderDeleteListener());

        ConfigurationService configService = getService(ConfigurationService.class);
        boolean providerEnabled = configService.getBoolProperty(OAuthProviderProperties.ENABLED, false);
        if (!providerEnabled) {
            LOG.info("OAuth provider is disabled by configuration.");
            return;
        }

        trackService(HostnameService.class);
        trackService(ConfigViewFactory.class);
        trackService(AuthorizationService.class);
        trackService(CapabilityService.class);
        trackService(TemplateService.class);
        trackService(TranslatorFactory.class);
        trackService(HtmlService.class);
        track(OAuthScopeProvider.class, new OAuthScopeProviderTracker(context));

        OAuthProvider provider = new OAuthProvider(this, context);
        this.provider = provider;
        if ("hz".equalsIgnoreCase(configService.getProperty(OAuthProviderProperties.AUTHCODE_TYPE, "hz").trim())) {
            track(HazelcastConfigurationService.class, new HzConfigTracker(context, this, provider));
            openTrackers();
        } else {
            openTrackers();
            provider.start(new DbAuthorizationCodeProvider(this));
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        OAuthProvider provider = this.provider;
        if (provider != null) {
            provider.stop();
            this.provider = null;
        }
        Services.setServiceLookup(null);
        super.stopBundle();
    }
}
