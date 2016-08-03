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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.pns.transport.websocket.osgi;

import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.pns.PushMessageGeneratorRegistry;
import com.openexchange.pns.PushNotificationTransport;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.pns.transport.websocket.internal.WebSocketPushNotificationTransport;
import com.openexchange.timer.TimerService;
import com.openexchange.websockets.WebSocketListener;
import com.openexchange.websockets.WebSocketService;


/**
 * {@link WebSocketPushNotificationTransportActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketPushNotificationTransportActivator extends HousekeepingActivator implements Reloadable {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(WebSocketPushNotificationTransportActivator.class);

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
        try {
            reinit(configService);
        } catch (Exception e) {
            LOG.error("Failed to re-initialize Web Socket transport", e);
        }
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties("com.openexchange.pns.transport.websocket.enabled");
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, PushSubscriptionRegistry.class, PushMessageGeneratorRegistry.class, WebSocketService.class, TimerService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        WebSocketToClientResolverTracker resolverTracker = new WebSocketToClientResolverTracker(context);
        this.resolverTracker = resolverTracker;
        rememberTracker(resolverTracker);
        openTrackers();

        reinit(getService(ConfigurationService.class));
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

    private synchronized void reinit(ConfigurationService configService) {
        List<ServiceRegistration<?>> serviceRegistrations = this.serviceRegistrations;

        if (!configService.getBoolProperty("com.openexchange.pns.transport.websocket.enabled", false)) {
            LOG.info("Web Socket push notification transport is disabled per configuration");

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

            return;
        }

        if (null != serviceRegistrations) {
            // Already registered
            return;
        }

        WebSocketPushNotificationTransport webSocketTransport = new WebSocketPushNotificationTransport(resolverTracker, this);
        this.webSocketTransport = webSocketTransport;
        serviceRegistrations = new ArrayList<>(2);
        serviceRegistrations.add(context.registerService(PushNotificationTransport.class, webSocketTransport, null));
        serviceRegistrations.add(context.registerService(WebSocketListener.class, webSocketTransport, null));
        this.serviceRegistrations = serviceRegistrations;
    }

}
