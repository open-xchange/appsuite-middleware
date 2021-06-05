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

package com.openexchange.mail.osgi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailProviderRegistration;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.api.MailProvider;

/**
 * Service tracker for mail providers
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailProviderServiceTracker implements ServiceTrackerCustomizer<MailProvider,MailProvider> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailProviderServiceTracker.class);

    private final Map<String, ServiceRegistration<MailProviderRegistration>> registrations;
    private final BundleContext context;

    /**
     * Initializes a new {@link MailProviderServiceTracker}
     */
    public MailProviderServiceTracker(BundleContext context) {
        super();
        registrations = new ConcurrentHashMap<String, ServiceRegistration<MailProviderRegistration>>(4, 0.9F, 1);
        this.context = context;
    }

    @Override
    public MailProvider addingService(ServiceReference<MailProvider> reference) {
        final MailProvider provider = context.getService(reference);
        final Object protocol = reference.getProperty("protocol");
        if (null == protocol) {
            LOG.error("Missing protocol in mail provider service: {}", provider.getClass().getName());
            context.ungetService(reference);
            return null;
        }
        try {
            /*
             * TODO: Clarify if proxy object is reasonable or if service itself should be registered
             */
            String sProtocol = protocol.toString();
            if (MailProviderRegistry.registerMailProvider(sProtocol, provider)) {
                LOG.info("Mail provider for protocol '{}' successfully registered", protocol);
                DefaultMailProviderRegistration registration = new DefaultMailProviderRegistration(sProtocol);
                registrations.put(sProtocol, context.registerService(MailProviderRegistration.class, registration, null));
            } else {
                LOG.warn("Mail provider for protocol '{}' could not be added. Another provider which supports the protocol has already been registered.", protocol);
                context.ungetService(reference);
                return null;
            }
        } catch (OXException e) {
            LOG.error("", e);
            context.ungetService(reference);
            return null;
        }
        return provider;
    }

    @Override
    public void modifiedService(ServiceReference<MailProvider> reference, MailProvider service) {
        // Nothing to do
    }

    @Override
    public void removedService(ServiceReference<MailProvider> reference, MailProvider service) {
        if (null != service) {
            try {
                MailProvider provider = service;

                ServiceRegistration<MailProviderRegistration> registration = registrations.remove(provider.getProtocol().toString());
                if (null != registration) {
                    registration.unregister();
                }

                MailProviderRegistry.unregisterMailProvider(provider);
                LOG.info("Mail provider for protocol '{}' successfully unregistered", provider.getProtocol());
            } catch (OXException e) {
                LOG.error("", e);
            } finally {
                context.ungetService(reference);
            }
        }
    }

}
