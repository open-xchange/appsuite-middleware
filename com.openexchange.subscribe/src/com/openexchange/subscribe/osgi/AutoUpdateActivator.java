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

package com.openexchange.subscribe.osgi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleActivator;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.framework.request.DefaultRequestContext;
import com.openexchange.framework.request.RequestContext;
import com.openexchange.framework.request.RequestContextHolder;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.login.NonTransient;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.secret.SecretService;
import com.openexchange.session.Session;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.internal.SubscriptionExecutionServiceImpl;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link AutoUpdateActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AutoUpdateActivator extends HousekeepingActivator implements BundleActivator {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AutoUpdateActivator.class);

    static final AtomicReference<OSGiSubscriptionSourceDiscoveryCollector> COLLECTOR_REFERENCE = new AtomicReference<>();

    /**
     * Sets the collector to use
     *
     * @param collector The collector to use
     */
    public static void setCollector(OSGiSubscriptionSourceDiscoveryCollector collector) {
        COLLECTOR_REFERENCE.set(collector);
    }

    static final AtomicReference<SubscriptionExecutionServiceImpl> EXECUTOR_REFERENCE = new AtomicReference<>();

    /**
     * Sets the executor to use.
     *
     * @param executor The executor to use
     */
    public static void setExecutor(SubscriptionExecutionServiceImpl executor) {
        EXECUTOR_REFERENCE.set(executor);
    }

    // -------------------------------------------------------------------------------

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigViewFactory.class, SecretService.class, DispatcherPrefixService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(LoginHandlerService.class, new SubscriptionLoginHandler());
    }

    static final Long DEFAULT_INTERVAL = Long.valueOf(24 * 60 * 60 * 1000l);

    private final class SubscriptionLoginHandler implements LoginHandlerService, NonTransient {

        /**
         * Initializes a new {@link AutoUpdateActivator.SubscriptionLoginHandler}.
         */
        SubscriptionLoginHandler() {
            super();
        }

        @Override
        public void handleLogin(LoginResult login) {
            try {
                ConfigView view = getService(ConfigViewFactory.class).getView(login.getUser().getId(), login.getContext().getContextId());
                if (!view.opt("com.openexchange.subscribe.autorun", boolean.class, Boolean.FALSE).booleanValue()) {
                    return;
                }

                OSGiSubscriptionSourceDiscoveryCollector collector = COLLECTOR_REFERENCE.get();
                if (collector == null) {
                    LOG.warn("Autoupdate of subscriptions enabled but collector not available.");
                    return;
                }

                SubscriptionExecutionServiceImpl executor = EXECUTOR_REFERENCE.get();
                if (executor == null) {
                    LOG.warn("Autoupdate of subscriptions enabled but executor not available.");
                    return;
                }

                Context ctx = login.getContext();
                Session session = login.getSession();
                String secret = getService(SecretService.class).getSecret(session);
                long now = System.currentTimeMillis();

                List<SubscriptionSource> sources = collector.getSources();
                List<Subscription> subscriptionsToRefresh = new ArrayList<Subscription>(10);

                for (SubscriptionSource subscriptionSource : sources) {
                    String autorunName = subscriptionSource.getId() + ".autorunInterval";
                    long interval = view.opt(autorunName, Long.class, DEFAULT_INTERVAL).longValue();
                    if (interval < 0) {
                        continue;
                    }
                    Collection<Subscription> subscriptions = subscriptionSource.getSubscribeService().loadSubscriptions(ctx, login.getUser().getId(), secret);
                    for (Subscription subscription : subscriptions) {
                        long lastUpdate = subscription.getLastUpdate();
                        if (now - lastUpdate > interval) {
                            subscriptionsToRefresh.add(subscription);
                        }
                    }
                }

                // Set request context
                RequestContextHolder.set(buildRequestContext(login));

                executor.executeSubscriptions(subscriptionsToRefresh, ServerSessionAdapter.valueOf(session), null);
            } catch (OXException e) {
                LOG.error("", e);
            }
        }

        @Override
        public void handleLogout(LoginResult logout) {
            // nothing to do
        }

        /**
         * Builds the {@link RequestContext} from the specified {@link LoginResult}
         *
         * @param login The {@link LoginResult} from which to build the {@link RequestContext}
         * @return The built {@link RequestContext}
         * @throws OXException If session cannot be initialized
         */
        private RequestContext buildRequestContext(LoginResult login) throws OXException {
            DefaultRequestContext context = new DefaultRequestContext();
            HostData hostData = createHostData(login);
            context.setHostData(hostData);
            context.setUserAgent(login.getRequest().getUserAgent());
            context.setSession(ServerSessionAdapter.valueOf(login.getSession()));
            return context;
        }

        /**
         * Creates and returns the {@link HostData} out of the specified {@link LoginResult}
         *
         * @param login The {@link LoginResult} from which to create the {@link HostData}
         * @return The {@link HostData}
         */
        private HostData createHostData(final LoginResult login) {
            return new HostData() {

                @Override
                public String getHTTPSession() {
                    return login.getRequest().getHttpSessionID();
                }

                @Override
                public boolean isSecure() {
                    return login.getRequest().isSecure();
                }

                @Override
                public String getRoute() {
                    return Tools.extractRoute(getHTTPSession());
                }

                @Override
                public int getPort() {
                    return login.getRequest().getServerPort();
                }

                @Override
                public String getHost() {
                    return login.getRequest().getServerName();
                }

                @Override
                public String getDispatcherPrefix() {
                    return getService(DispatcherPrefixService.class).getPrefix();
                }
            };
        }
    }
}
