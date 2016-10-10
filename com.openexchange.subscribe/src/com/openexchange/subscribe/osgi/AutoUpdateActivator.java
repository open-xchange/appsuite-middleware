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

package com.openexchange.subscribe.osgi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AutoUpdateActivator.class);

    public static OSGiSubscriptionSourceDiscoveryCollector COLLECTOR;

    public static SubscriptionExecutionServiceImpl EXECUTOR;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigViewFactory.class, SecretService.class, DispatcherPrefixService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(LoginHandlerService.class, new SubscriptionLoginHandler());
    }

    private final class SubscriptionLoginHandler implements LoginHandlerService, NonTransient {

        @Override
        public void handleLogin(LoginResult login) throws OXException {
            try {
                ConfigView view = getService(ConfigViewFactory.class).getView(login.getUser().getId(), login.getContext().getContextId());
                if (!view.opt("com.openexchange.subscribe.autorun", boolean.class, false)) {
                    return;
                }
                if (COLLECTOR == null || EXECUTOR == null) {
                    LOG.warn("Autoupdate of subscriptions enabled but collector {} or executor {} not available.", COLLECTOR, EXECUTOR);
                    return;
                }

                Context ctx = login.getContext();
                Session session = login.getSession();
                String secret = getService(SecretService.class).getSecret(session);
                long now = System.currentTimeMillis();

                List<SubscriptionSource> sources = COLLECTOR.getSources();
                List<Subscription> subscriptionsToRefresh = new ArrayList<Subscription>(10);

                for (SubscriptionSource subscriptionSource : sources) {
                    String autorunName = subscriptionSource.getId() + ".autorunInterval";
                    //Long interval = view.opt(autorunName, Long.class, 24 * 60 * 60 * 1000l);
                    Long interval = view.opt(autorunName, Long.class, 10 * 1000l);
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

                EXECUTOR.executeSubscriptions(subscriptionsToRefresh, ServerSessionAdapter.valueOf(session), null);
            } catch (OXException e) {
                LOG.error("", e);
            }
        }

        @Override
        public void handleLogout(LoginResult logout) throws OXException {
            // nothing to do
        }

        /**
         * Builds the {@link RequestContext} from the specified {@link LoginResult}
         * 
         * @param login The {@link LoginResult} from which to build the {@link RequestContext}
         * @return The built {@link RequestContext}
         */
        private RequestContext buildRequestContext(LoginResult login) {
            DefaultRequestContext context = new DefaultRequestContext();
            HostData hostData = createHostData(login);
            context.setHostData(hostData);
            context.setUserAgent(login.getRequest().getUserAgent());
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
