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

package com.openexchange.ajax.login.osgi;

import static com.openexchange.osgi.Tools.withRanking;
import java.util.regex.Pattern;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.login.AllowedRedirectUris;
import com.openexchange.ajax.login.LoginRequestHandler;
import com.openexchange.ajax.login.RateLimiterByLogin;
import com.openexchange.ajax.login.RedeemReservationLogin;
import com.openexchange.authentication.application.AppAuthenticatorService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.java.Strings;
import com.openexchange.login.LoginRampUpService;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.login.internal.format.CompositeLoginFormatter;
import com.openexchange.login.listener.LoginListener;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.osgi.Tools;
import com.openexchange.password.mechanism.PasswordMechRegistry;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.reservation.Enhancer;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.share.ShareService;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.tokenlogin.TokenLoginService;

/**
 * {@link LoginActivator}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class LoginActivator extends HousekeepingActivator {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {

        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(LoginActivator.class);
    }

    public LoginActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final BundleContext context = this.context;
        class ServerServiceRegistryTracker<S> implements ServiceTrackerCustomizer<S, S> {

            private final Class<? extends S> clazz;

            ServerServiceRegistryTracker(Class<? extends S> clazz) {
                super();
                this.clazz = clazz;
            }

            @Override
            public S addingService(final ServiceReference<S> reference) {
                final S service = context.getService(reference);
                ServerServiceRegistry.getInstance().addService(clazz, service);
                return service;
            }

            @Override
            public void modifiedService(final ServiceReference<S> reference, final S service) {
                // Nothing
            }

            @Override
            public void removedService(final ServiceReference<S> reference, final S service) {
                context.ungetService(reference);
                ServerServiceRegistry.getInstance().removeService(clazz);
            }
        }
        track(ShareService.class, new ServerServiceRegistryTracker<ShareService>(ShareService.class));
        track(PasswordMechRegistry.class, new ServerServiceRegistryTracker<PasswordMechRegistry>(PasswordMechRegistry.class));
        track(ModuleSupport.class, new ServerServiceRegistryTracker<ModuleSupport>(ModuleSupport.class));
        track(AppAuthenticatorService.class, new ServerServiceRegistryTracker<AppAuthenticatorService>(AppAuthenticatorService.class));

        ServiceSet<LoginRampUpService> rampUp = new ServiceSet<LoginRampUpService>();
        track(LoginRampUpService.class, rampUp);

        LoginServletRegisterer loginServletRegisterer = new LoginServletRegisterer(context, rampUp);
        final RedeemReservationLogin redeemReservationLogin = new RedeemReservationLogin();
        loginServletRegisterer.addLoginRequestHandler(LoginServlet.ACTION_REDEEM_RESERVATION, redeemReservationLogin);

        Filter filter = Tools.generateServiceFilter(context,
            ConfigurationService.class,
            HttpService.class,
            DispatcherPrefixService.class,
            LoginRequestHandler.class);
        rememberTracker(new ServiceTracker<Object, Object>(context, filter, loginServletRegisterer));

        track(TokenLoginService.class, new TokenLoginCustomizer(context));
        track(SessionReservationService.class, new SessionReservationCustomizer(context));
        track(Enhancer.class, new SimpleRegistryListener<Enhancer>() {

            @Override
            public void added(ServiceReference<Enhancer> ref, Enhancer service) {
                redeemReservationLogin.addEnhancer(service);
            }

            @Override
            public void removed(ServiceReference<Enhancer> ref, Enhancer service) {
                redeemReservationLogin.removeEnhancer(service);
            }
        });
        openTrackers();

        final ConfigurationService configurationService = getService(ConfigurationService.class);
        final String loginFormat = configurationService.getProperty("com.openexchange.ajax.login.formatstring.login");
        final String logoutFormat = configurationService.getProperty("com.openexchange.ajax.login.formatstring.logout");
        LoginPerformer.setLoginFormatter(new CompositeLoginFormatter(loginFormat, logoutFormat));

        com.openexchange.tools.servlet.http.Tools.setConfigurationService(configurationService);

        // Login name rate limiter
        boolean rateLimitByLogin = configurationService.getBoolProperty("com.openexchange.ajax.login.rateLimitByLogin.enabled", false);
        if (rateLimitByLogin) {
            String propPermits = "com.openexchange.ajax.login.rateLimitByLogin.permits";
            String propTimeFrame = "com.openexchange.ajax.login.rateLimitByLogin.timeFrameInSeconds";
            int permits = configurationService.getIntProperty(propPermits, 3);
            long timeFrameInSeconds = configurationService.getIntProperty(propTimeFrame, 30);
            if (permits > 0 && timeFrameInSeconds > 0) {
                registerService(LoginListener.class, new RateLimiterByLogin(permits, timeFrameInSeconds), withRanking(999));
            } else {
                LoggerHolder.LOG.warn("Value configured for \"{}\" and/or \"{}\" property must be positive. Rate limiting by login name is effectively disabled!", propPermits, propTimeFrame);
            }
        }

        // Allowed paths for redirects
        String allowedRedirectUris = configurationService.getProperty("com.openexchange.ajax.login.allowedRedirectURIsOnLoginError");
        if (Strings.isNotEmpty(allowedRedirectUris)) {
            String[] wildcardPatterns = Strings.splitByComma(allowedRedirectUris);
            AllowedRedirectUris whitelist = AllowedRedirectUris.getInstance();
            for (String wildcardPattern : wildcardPatterns) {
                if (Strings.isNotEmpty(wildcardPattern)) {
                    String wcp = Strings.unquote(wildcardPattern.trim());
                    int starPos = wcp.indexOf('*');
                    int qmarPos = wcp.indexOf('?');
                    if (starPos < 0 && qmarPos < 0) {
                        whitelist.add(new AllowedRedirectUris.IgnoreCaseExactUriMatcher(wcp));
                    } else {
                        int mlen = wcp.length() - 1;
                        if (mlen > 0 && ((starPos >= mlen && qmarPos >= mlen) || (starPos == mlen && qmarPos < 0) || (qmarPos == mlen && starPos < 0))) {
                            whitelist.add(new AllowedRedirectUris.IgnoreCasePrefixUriMatcher(wcp.substring(0, mlen)));
                        } else {
                            Pattern pattern = Pattern.compile(Strings.wildcardToRegex(wcp), Pattern.CASE_INSENSITIVE);
                            whitelist.add(new AllowedRedirectUris.PatternUriMatcher(pattern));
                        }
                    }
                }
            }
        }

    }

}
