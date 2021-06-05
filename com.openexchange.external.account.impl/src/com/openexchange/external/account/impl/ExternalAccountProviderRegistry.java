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

package com.openexchange.external.account.impl;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.external.account.ExternalAccountExceptionCodes;
import com.openexchange.external.account.ExternalAccountModule;
import com.openexchange.external.account.ExternalAccountProvider;

/**
 * {@link ExternalAccountProviderRegistry}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class ExternalAccountProviderRegistry implements ServiceTrackerCustomizer<ExternalAccountProvider, ExternalAccountProvider> {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalAccountProviderRegistry.class);

    private final ConcurrentMap<ExternalAccountModule, ExternalAccountProvider> registry;
    private final BundleContext context;

    /**
     * Initializes a new {@link ExternalAccountProviderRegistry}.
     *
     * @param context The {@link BundleContext}
     */
    public ExternalAccountProviderRegistry(BundleContext context) {
        super();
        this.context = context;
        registry = new ConcurrentHashMap<>();
    }

    /**
     * Gets the external account provider for given module.
     *
     * @param module The module
     * @return The appropriate provider
     * @throws OXException If no such provider is available
     */
    public ExternalAccountProvider getProviderFor(ExternalAccountModule module) throws OXException {
        Optional<ExternalAccountProvider> optionalAccountProvider = optProviderFor(module);
        if (!optionalAccountProvider.isPresent()) {
            throw ExternalAccountExceptionCodes.PROVIDER_NOT_FOUND.create(module);
        }
        return optionalAccountProvider.get();
    }

    /**
     * Gets the external account provider for given module.
     *
     * @param module The module
     * @return The optional provider
     */
    public Optional<ExternalAccountProvider> optProviderFor(ExternalAccountModule module) {
        return module == null ? Optional.empty() : Optional.ofNullable(registry.get(module));
    }

    @Override
    public ExternalAccountProvider addingService(ServiceReference<ExternalAccountProvider> reference) {
        ExternalAccountProvider provider = context.getService(reference);
        if (registry.putIfAbsent(provider.getModule(), provider) == null) {
            LOG.info("Added external account provider with id '{}'", provider.getModule());
            return provider;
        }

        LOG.warn("There is already another provider registered with id '{}'", provider.getModule());
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<ExternalAccountProvider> reference, ExternalAccountProvider service) {
        // nope
    }

    @Override
    public void removedService(ServiceReference<ExternalAccountProvider> reference, ExternalAccountProvider provider) {
        ExternalAccountProvider removed = registry.remove(provider.getModule());
        if (removed != null) {
            context.ungetService(reference);
        }
    }
}
