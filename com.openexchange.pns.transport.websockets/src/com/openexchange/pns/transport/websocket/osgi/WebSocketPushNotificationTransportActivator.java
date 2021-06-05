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

package com.openexchange.pns.transport.websocket.osgi;

import static com.openexchange.osgi.Tools.withRanking;
import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pns.PushMessageGeneratorRegistry;
import com.openexchange.pns.PushNotificationTransport;
import com.openexchange.pns.PushSubscriptionProvider;
import com.openexchange.pns.transport.websocket.internal.WebSocketClientPushClientChecker;
import com.openexchange.pns.transport.websocket.internal.WebSocketPushNotificationTransport;
import com.openexchange.push.PushClientChecker;
import com.openexchange.websockets.WebSocketService;


/**
 * {@link WebSocketPushNotificationTransportActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketPushNotificationTransportActivator extends HousekeepingActivator implements Reloadable {

    private List<ServiceRegistration<?>> serviceRegistrations;
    private WebSocketPushNotificationTransport webSocketTransport;
    private WebSocketToClientResolverTracker resolverTracker;

    /**
     * Initializes a new {@link WebSocketPushNotificationTransportActivator}.
     */
    public WebSocketPushNotificationTransportActivator() {
        super();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        // Nothing to do as "com.openexchange.pns.transport.websocket.enabled" is read on-the-fly through config-cascade
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(
            "com.openexchange.pns.transport.websocket.enabled"
            );
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, PushMessageGeneratorRegistry.class, WebSocketService.class, ConfigViewFactory.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        WebSocketToClientResolverTracker resolverTracker = new WebSocketToClientResolverTracker(context);
        this.resolverTracker = resolverTracker;
        rememberTracker(resolverTracker);
        openTrackers();

        registerService(PushClientChecker.class, new WebSocketClientPushClientChecker(resolverTracker), withRanking(100));

        reinit();

        registerService(ForcedReloadable.class, new ForcedReloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                WebSocketPushNotificationTransport.invalidateEnabledCache();
            }

        });
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        List<ServiceRegistration<?>> serviceRegistrations = this.serviceRegistrations;
        if (null != serviceRegistrations) {
            for (ServiceRegistration<?> serviceRegistration : serviceRegistrations) {
                serviceRegistration.unregister();
            }
            this.serviceRegistrations = null;
        }

        WebSocketPushNotificationTransport webSocketTransport = this.webSocketTransport;
        if (null != webSocketTransport) {
            this.webSocketTransport = null;
            webSocketTransport.stop();
        }

        this.resolverTracker = null; // Gets closed in stopBundle() method

        super.stopBundle();
    }

    private synchronized void reinit() {
        List<ServiceRegistration<?>> serviceRegistrations = this.serviceRegistrations;
        if (null != serviceRegistrations) {
            for (ServiceRegistration<?> serviceRegistration : serviceRegistrations) {
                serviceRegistration.unregister();
            }
            this.serviceRegistrations = null;
        }

        WebSocketPushNotificationTransport webSocketTransport = this.webSocketTransport;
        if (null != webSocketTransport) {
            this.webSocketTransport = null;
            webSocketTransport.stop();
        }

        webSocketTransport = new WebSocketPushNotificationTransport(resolverTracker, this);
        this.webSocketTransport = webSocketTransport;

        serviceRegistrations = new ArrayList<>(4);
        serviceRegistrations.add(context.registerService(PushSubscriptionProvider.class, webSocketTransport, null));
        serviceRegistrations.add(context.registerService(PushNotificationTransport.class, webSocketTransport, null));
        this.serviceRegistrations = serviceRegistrations;
    }

}
