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

package com.openexchange.pns.impl.osgi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.pns.PushNotificationTransport;
import com.openexchange.pns.impl.PushNotificationTransportRegistry;

/**
 * {@link PushNotificationTransportTracker} - The tracker for transports.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public final class PushNotificationTransportTracker implements ServiceTrackerCustomizer<PushNotificationTransport, PushNotificationTransport>, PushNotificationTransportRegistry {

    private final ConcurrentMap<String, PushNotificationTransport> transportMap;
    private final BundleContext context;

    /**
     * Initializes a new {@link PushNotificationTransportTracker}.
     */
    public PushNotificationTransportTracker(BundleContext context) {
        super();
        this.transportMap = new ConcurrentHashMap<>(4, 0.9F, 1);
        this.context = context;
    }

    @Override
    public PushNotificationTransport addingService(ServiceReference<PushNotificationTransport> reference) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(PushNotificationTransportTracker.class);

        PushNotificationTransport transport = context.getService(reference);
        if (null == transportMap.putIfAbsent(transport.getId(), transport)) {
            logger.info("Successfully registered '{}' push notification transport", transport.getId());
            return transport;
        }

        logger.error("Failed to register '{}' push notification transport for class {}. There is already such a transport.", transport.getId(), transport.getClass().getName());
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<PushNotificationTransport> reference, PushNotificationTransport service) {
        // Nothing
    }

    @Override
    public void removedService(ServiceReference<PushNotificationTransport> reference, PushNotificationTransport transport) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(PushNotificationTransportTracker.class);

        if (null != transportMap.remove(transport.getId())) {
            logger.info("Successfully unregistered '{}' push notification transport", transport.getId());
            return;
        }

        context.ungetService(reference);
    }

    @Override
    public PushNotificationTransport getTransportFor(String client, String transportId) throws OXException {
        if (null == client || null == transportId) {
            return null;
        }
        PushNotificationTransport transport = transportMap.get(transportId);
        return null == transport ? null : (transport.servesClient(client) ? transport : null);
    }
}