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

package com.openexchange.advertisement.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.LoggerFactory;
import com.openexchange.advertisement.AdvertisementConfigService;
import com.openexchange.advertisement.AdvertisementPackageService;
import com.openexchange.advertisement.internal.AdvertisementPackageServiceImpl;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.FailureAwareCapabilityChecker;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.reseller.ResellerService;
import com.openexchange.session.Session;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { CapabilityService.class, ResellerService.class, ConfigurationService.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected void startBundle() throws Exception {
        LoggerFactory.getLogger(Activator.class).info("starting bundle com.openexchange.advertisement");

        final AdvertisementPackageServiceImpl packageService = new AdvertisementPackageServiceImpl(getService(ResellerService.class), getService(ConfigurationService.class));

        // Register capability
        {
            final String sCapability = "ads";
            Dictionary<String, Object> properties = new Hashtable<>(2);
            properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, sCapability);
            registerService(CapabilityChecker.class, new FailureAwareCapabilityChecker() {

                @Override
                public FailureAwareCapabilityChecker.Result checkEnabled(String capability, Session session) {
                    if (sCapability.equals(capability)) {
                        AdvertisementConfigService confService = packageService.getScheme(session.getContextId());
                        if (confService == null) {
                            return FailureAwareCapabilityChecker.Result.DISABLED;
                        }
                        if (confService.isAvailable(session)) {
                            return FailureAwareCapabilityChecker.Result.ENABLED;
                        }
                        return FailureAwareCapabilityChecker.Result.DISABLED;
                    }

                    return FailureAwareCapabilityChecker.Result.ENABLED;
                }
            }, properties);
            getService(CapabilityService.class).declareCapability(sCapability);
        }

        registerService(AdvertisementPackageService.class, packageService);
        registerService(Reloadable.class, packageService);
        final BundleContext context = this.context;
        track(AdvertisementConfigService.class, new ServiceTrackerCustomizer<AdvertisementConfigService, AdvertisementConfigService>() {

            @Override
            public AdvertisementConfigService addingService(ServiceReference<AdvertisementConfigService> reference) {
                AdvertisementConfigService service = context.getService(reference);
                boolean added = packageService.addServiceAndReload(service);
                if (added) {
                    return service;
                }

                context.ungetService(reference);
                return null;
            }

            @Override
            public void modifiedService(ServiceReference<AdvertisementConfigService> reference, AdvertisementConfigService service) {
                // Nothing to do
            }

            @Override
            public void removedService(ServiceReference<AdvertisementConfigService> reference, AdvertisementConfigService service) {
                packageService.removeServiceAndReload(service);
                context.ungetService(reference);
            }

        });
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        LoggerFactory.getLogger(Activator.class).info("stopping bundle com.openexchange.advertisement");

        super.stopBundle();
    }

}
