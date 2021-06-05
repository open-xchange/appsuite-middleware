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

package com.openexchange.client.onboarding.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import com.openexchange.client.onboarding.OnboardingProvider;
import com.openexchange.client.onboarding.internal.OnboardingServiceImpl;

/**
 * {@link OnboardingProviderTracker} - Tracks registered providers and adds them to on-boaring service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class OnboardingProviderTracker extends ServiceTracker<OnboardingProvider, OnboardingProvider> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(OnboardingProviderTracker.class);
    private final OnboardingServiceImpl serviceImpl;

    /**
     * Initializes a new {@link OnboardingProviderTracker}.
     *
     * @param context The bundle context
     */
    public OnboardingProviderTracker(BundleContext context, OnboardingServiceImpl serviceImpl) {
        super(context, OnboardingProvider.class, null);
        this.serviceImpl = serviceImpl;
    }

    @Override
    public OnboardingProvider addingService(ServiceReference<OnboardingProvider> reference) {
        OnboardingProvider provider = context.getService(reference);
        if (serviceImpl.addProviderIfAbsent(provider)) {
            return provider;
        }

        LOG.warn("An on-boarding provider already exists with identifier '{}'. Ignoring '{}' instance", provider.getId(), provider.getClass().getName());
        context.ungetService(reference);
        return null;
    }

    @Override
    public void removedService(ServiceReference<OnboardingProvider> reference, OnboardingProvider provider) {
        serviceImpl.removeProvider(provider.getId());
        context.ungetService(reference);
    }

}
