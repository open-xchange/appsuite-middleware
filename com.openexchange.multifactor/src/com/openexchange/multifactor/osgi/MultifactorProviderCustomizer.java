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

package com.openexchange.multifactor.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.multifactor.MultifactorProvider;
import com.openexchange.multifactor.MultifactorProviderRegistry;
import com.openexchange.multifactor.MultifactorProviderRegistryImpl;

/**
 * {@link MultifactorProviderCustomizer}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class MultifactorProviderCustomizer implements ServiceTrackerCustomizer<MultifactorProvider, MultifactorProvider> {

    private final MultifactorProviderRegistryImpl registry;
    private final BundleContext                   context;

    /**
     * Initializes a new {@link MultifactorProviderCustomizer}.
     *
     * @param context The {@link BundleContext}
     * @param registry The {@link MultifactorProviderRegistry}
     */
    public MultifactorProviderCustomizer(BundleContext context, MultifactorProviderRegistryImpl registry) {
        this.context = context;
        this.registry = registry;
    }

    @Override
    public MultifactorProvider addingService(ServiceReference<MultifactorProvider> reference) {
        final MultifactorProvider provider = context.getService(reference);
        this.registry.registerProvider(provider);
        return provider;
    }

    @Override
    public void modifiedService(ServiceReference<MultifactorProvider> reference, MultifactorProvider service) { /* nothing to do here */ }

    @Override
    public void removedService(ServiceReference<MultifactorProvider> reference, MultifactorProvider service) {
        registry.unRegisterProvider(service);
    }
}
