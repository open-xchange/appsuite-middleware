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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.DatabaseService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.oauth.provider.AuthorizationCodeService;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.internal.GrantAllProvider;
import com.openexchange.oauth.provider.internal.authcode.DbAuthorizationCodeService;
import com.openexchange.oauth.provider.internal.authcode.HzAuthorizationCodeService;
import com.openexchange.oauth.provider.servlets.AuthServlet;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.UserService;

/**
 * {@link OAuthProviderImplActivator} - The activator for OAuth provider implementation bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OAuthProviderImplActivator extends HousekeepingActivator {

    private static final class HzConfigTracker implements ServiceTrackerCustomizer<HazelcastConfigurationService, HazelcastConfigurationService> {

        final BundleContext context;
        final OAuthProviderImplActivator activator;
        private volatile ServiceTracker<HazelcastInstance, HazelcastInstance> hzInstanceTracker;

        HzConfigTracker(BundleContext context, OAuthProviderImplActivator activator) {
            super();
            this.context = context;
            this.activator = activator;
        }

        @Override
        public HazelcastConfigurationService addingService(ServiceReference<HazelcastConfigurationService> reference) {
            HazelcastConfigurationService hzConfigService = context.getService(reference);

            try {
                boolean hzEnabled = hzConfigService.isEnabled();
                if (false == hzEnabled) {
                    Logger logger = org.slf4j.LoggerFactory.getLogger(OAuthProviderImplActivator.class);
                    String msg = "Authorization-Code service is configured to use Hazelcast, but Hazelcast is disabled as per configuration! Start of Authorization-Code service aborted!";
                    logger.error(msg, new Exception(msg));

                    context.ungetService(reference);
                    return null;
                }

                final BundleContext context = this.context;
                ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance> stc = new ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>() {

                    private volatile ServiceRegistration<AuthorizationCodeService> reg;

                    @Override
                    public HazelcastInstance addingService(ServiceReference<HazelcastInstance> reference) {
                        HazelcastInstance hzInstance = context.getService(reference);
                        activator.addService(HazelcastInstance.class, hzInstance);

                        AuthorizationCodeService authCodeService = new HzAuthorizationCodeService("authcode-1", activator);
                        reg = context.registerService(AuthorizationCodeService.class, authCodeService, null);
                        activator.addService(AuthorizationCodeService.class, authCodeService);

                        return hzInstance;
                    }

                    @Override
                    public void modifiedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
                        // Nothing
                    }

                    @Override
                    public void removedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance service) {
                        ServiceRegistration<AuthorizationCodeService> reg = this.reg;
                        if (null != reg) {
                            reg.unregister();
                            this.reg = null;
                        }

                        activator.removeService(HazelcastInstance.class);
                        activator.removeService(AuthorizationCodeService.class);
                        context.ungetService(reference);
                    }
                };
                ServiceTracker<HazelcastInstance, HazelcastInstance> hzInstanceTracker = new ServiceTracker<HazelcastInstance, HazelcastInstance>(context, HazelcastInstance.class, stc);
                this.hzInstanceTracker = hzInstanceTracker;
                hzInstanceTracker.open();

                return hzConfigService;
            } catch (Exception e) {
                Logger logger = org.slf4j.LoggerFactory.getLogger(OAuthProviderImplActivator.class);
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
    }

    // ---------------------------------------------------------------------------------------------


    private static final String PATH_PREFIX = "oauth2/";

    /**
     * Initializes a new {@link OAuthProviderImplActivator}.
     */
    public OAuthProviderImplActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, ConfigurationService.class, AuthenticationService.class, ContextService.class, UserService.class, CryptoService.class, HttpService.class, DispatcherPrefixService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.set(this);

        String prefix = getService(DispatcherPrefixService.class).getPrefix();
        getService(HttpService.class).registerServlet(prefix + PATH_PREFIX + AuthServlet.PATH, new AuthServlet(), null, null);

        ConfigurationService configService = getService(ConfigurationService.class);
        if ("hz".equalsIgnoreCase(configService.getProperty("com.openexchange.oauth.provider.authcode.type", "hz").trim())) {
            // Start tracking for Hazelcast
            track(HazelcastConfigurationService.class, new HzConfigTracker(context, this));
        } else {
            DbAuthorizationCodeService authCodeService = new DbAuthorizationCodeService(this);
            registerService(AuthorizationCodeService.class, authCodeService);
            addService(AuthorizationCodeService.class, authCodeService);
        }

        openTrackers();

        OAuthProviderService oauth2ProviderService = new GrantAllProvider();
        registerService(OAuthProviderService.class, oauth2ProviderService);
        addService(OAuthProviderService.class, oauth2ProviderService);

        /*
         * Register OAuth provider service
         */
//        final DatabaseOAuthProviderService oauthProviderService = new DatabaseOAuthProviderService(this);
////        registerService(OAuthProviderService.class, oauthProviderService);
//        registerService(Reloadable.class, oauthProviderService);
//        addService(OAuthProviderService.class, oauthProviderService);
        /*
         * Register OAuth v2 provider service
         */
//        final DatabaseOAuth2ProviderService oauth2ProviderService = new DatabaseOAuth2ProviderService(this);
////        registerService(OAuthProviderService.class, oauth2ProviderService);
//        registerService(Reloadable.class, oauth2ProviderService);
//        addService(OAuthProviderService.class, oauth2ProviderService);
        /*
         * Service trackers
         */
//        final OAuthServiceProvider provider = oauthProviderService.getProvider();
        // OAuth v1
//        rememberTracker(new HTTPServletRegistration(context, provider.accessTokenURL, new AccessTokenServlet()));
//        rememberTracker(new HTTPServletRegistration(context, provider.userAuthorizationURL, new AuthorizationServlet()));
//        rememberTracker(new HTTPServletRegistration(context, "/oauth/echo", new EchoServlet()));
//        rememberTracker(new HTTPServletRegistration(context, provider.requestTokenURL, new RequestTokenServlet()));
//        // OAuth v2
//        rememberTracker(new HTTPServletRegistration(context, provider.accessTokenURL+"/v2", new AccessTokenServlet2()));
//        rememberTracker(new HTTPServletRegistration(context, provider.userAuthorizationURL+"/v2", new AuthorizationServlet2()));
//        openTrackers();
        /*
         * Register update task, create table job and delete listener
         */
        {
//            registerService(CreateTableService.class, new OAuthProviderCreateTableService());
//            registerService(CreateTableService.class, new OAuth2ProviderCreateTableService());
//            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new OAuthProviderCreateTableTask(), new OAuth2ProviderCreateTableTask()));
//            registerService(DeleteListener.class, new OAuthProviderDeleteListener());
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        Services.set(null);
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

}
