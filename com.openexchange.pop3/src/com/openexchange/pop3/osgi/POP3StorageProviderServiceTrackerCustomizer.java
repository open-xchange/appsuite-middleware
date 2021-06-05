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

package com.openexchange.pop3.osgi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.mailaccount.MailAccountDeleteListener;
import com.openexchange.pop3.storage.POP3StorageProvider;
import com.openexchange.pop3.storage.POP3StorageProviderRegistry;

/**
 * {@link POP3StorageProviderServiceTrackerCustomizer} - Service tracker customizer for POP3 storage provider.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class POP3StorageProviderServiceTrackerCustomizer implements ServiceTrackerCustomizer<POP3StorageProvider,POP3StorageProvider> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(POP3StorageProviderServiceTrackerCustomizer.class);

    private final BundleContext context;

    private final Map<String, List<ServiceRegistration<?>>> registrationMap;

    /**
     * Initializes a new {@link POP3StorageProviderServiceTrackerCustomizer}.
     */
    public POP3StorageProviderServiceTrackerCustomizer(final BundleContext context) {
        super();
        this.context = context;
        registrationMap = new ConcurrentHashMap<String, List<ServiceRegistration<?>>>();
    }

    @Override
    public POP3StorageProvider addingService(final ServiceReference<POP3StorageProvider> reference) {
        final POP3StorageProvider addedService = context.getService(reference);
        if (null == addedService) {
            throw new NullPointerException("Added service is null!");
        }
        if (addPOP3StorageProvider(addedService)) {
            return addedService;
        }
        // Service needs not to be tracked
        context.ungetService(reference);
        return null;
    }

    /**
     * Adds specified provider to registry.
     *
     * @param provider The provider to add to registry
     * @return <code>true</code> if provider is added; otherwise <code>false</code>
     */
    public boolean addPOP3StorageProvider(final POP3StorageProvider provider) {
        final String providerName = provider.getPOP3StorageName();
        if (null == providerName) {
            LOG.error("Missing provider name in storage provider instance: {}", provider.getClass().getName());
            return false;
        }
        if (POP3StorageProviderRegistry.getInstance().addPOP3StorageProvider(providerName, provider)) {
            // Register provider's delete listeners
            final List<MailAccountDeleteListener> listeners = provider.getDeleteListeners();
            final List<ServiceRegistration<?>> registrations = new ArrayList<ServiceRegistration<?>>(listeners.size());
            for (final MailAccountDeleteListener mailAccountDeleteListener : listeners) {
                registrations.add(context.registerService(MailAccountDeleteListener.class, mailAccountDeleteListener, null));
            }
            registrationMap.put(provider.getPOP3StorageName(), registrations);
            LOG.info("POP3 storage provider for name '{}' successfully registered", providerName);
            return true;
        }
        LOG.warn("POP3 storage provider for name '{}' could not be added. Another provider with the same name has already been registered.", providerName);
        return false;
    }

    @Override
    public void modifiedService(final ServiceReference<POP3StorageProvider> reference, final POP3StorageProvider service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<POP3StorageProvider> reference, final POP3StorageProvider service) {
        if (null != service) {
            try {
                removePOP3StorageProvider(service);
            } finally {
                context.ungetService(reference);
            }
        }
    }

    /**
     * Removes specified provider from registry and drops its previously registered delete listeners<br>
     * (if {@link POP3StorageProvider#unregisterDeleteListenersOnAbsence()} says so)
     *
     * @param provider The provider to remove from registry
     */
    public void removePOP3StorageProvider(final POP3StorageProvider provider) {
        if (null == provider) {
            return;
        }
        // Unregister provider's delete listeners
        if (provider.unregisterDeleteListenersOnAbsence()) {
            final List<ServiceRegistration<?>> registrations = registrationMap.remove(provider.getPOP3StorageName());
            if (null != registrations) {
                for (final ServiceRegistration<?> serviceRegistration : registrations) {
                    serviceRegistration.unregister();
                }
            }
        }
        // Remove from registry
        POP3StorageProviderRegistry.getInstance().removePOP3StorageProvider(provider.getPOP3StorageName());
    }

    /**
     * Drops all tracked {@link ServiceRegistration registrations} for {@link MailAccountDeleteListener delete listeners}.
     */
    public void dropAllRegistrations() {
        for (final List<ServiceRegistration<?>> registrations : registrationMap.values()) {
            for (final ServiceRegistration<?> serviceRegistration : registrations) {
                serviceRegistration.unregister();
            }
        }
        registrationMap.clear();
    }

}
