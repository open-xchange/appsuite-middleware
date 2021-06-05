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

package com.openexchange.quota.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaService;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class QuotaProviderTracker implements QuotaService, ServiceTrackerCustomizer<QuotaProvider, QuotaProvider> {

    private static final Logger LOG = LoggerFactory.getLogger(QuotaProviderTracker.class);

    private final ConcurrentMap<String, QuotaProvider> providers = new ConcurrentHashMap<String, QuotaProvider>();

    private final BundleContext context;

    public QuotaProviderTracker(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public List<QuotaProvider> getAllProviders() {
        return new ArrayList<QuotaProvider>(providers.values());
    }

    @Override
    public QuotaProvider getProvider(String module) {
        return providers.get(module);
    }

    @Override
    public QuotaProvider addingService(ServiceReference<QuotaProvider> reference) {
        QuotaProvider provider = context.getService(reference);
        if (provider == null) {
            return null;
        }

        String moduleID = provider.getModuleID();
        LOG.info("Adding QuotaProvider for module {}.", moduleID);
        QuotaProvider existing = providers.put(moduleID, provider);
        if (existing != null) {
            LOG.warn("Detected a duplicate QuotaProvider for module {}. Service {} ({}) was overwritten by {} ({})!",
                moduleID,
                existing.toString(),
                existing.getClass().getName(),
                provider.toString(),
                provider.getClass().getName());
        }
        return provider;
    }

    @Override
    public void modifiedService(ServiceReference<QuotaProvider> reference, QuotaProvider provider) {
        //
    }

    @Override
    public void removedService(ServiceReference<QuotaProvider> reference, QuotaProvider provider) {
        String moduleID = provider.getModuleID();
        if (providers.remove(moduleID, provider)) {
            LOG.info("Removed QuotaProvider for module {}.", moduleID);
        }
    }

}
