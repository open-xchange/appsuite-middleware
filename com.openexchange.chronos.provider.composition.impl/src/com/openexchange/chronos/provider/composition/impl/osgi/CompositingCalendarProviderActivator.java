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

package com.openexchange.chronos.provider.composition.impl.osgi;

import static org.slf4j.LoggerFactory.getLogger;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.provider.FreeBusyProvider;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.chronos.provider.composition.impl.CalendarPrintingCapabilityChecker;
import com.openexchange.chronos.provider.composition.impl.CalendarProviderRegistryImpl;
import com.openexchange.chronos.provider.composition.impl.CalendarProviderTracker;
import com.openexchange.chronos.provider.composition.impl.CompositingIDBasedCalendarAccessFactory;
import com.openexchange.chronos.provider.composition.impl.quota.CalendarQuotaProvider;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.quota.QuotaProvider;

/**
 * {@link CompositingCalendarProviderActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CompositingCalendarProviderActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link CompositingCalendarProviderActivator}.
     */
    public CompositingCalendarProviderActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { CalendarAccountService.class, LeanConfigurationService.class, CapabilityService.class, ConfigViewFactory.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            getLogger(CompositingCalendarProviderActivator.class).info("starting bundle {}", context.getBundle());
            /*
             * track calendar providers & collect in registry
             */
            ServiceSet<FreeBusyProvider> freeBusyProviders = new ServiceSet<FreeBusyProvider>();
            track(FreeBusyProvider.class, freeBusyProviders);
            CalendarProviderTracker providerTracker = new CalendarProviderTracker(context, this);
            rememberTracker(providerTracker);
            openTrackers();
            CalendarProviderRegistryImpl providerRegistry = new CalendarProviderRegistryImpl(providerTracker, freeBusyProviders);
            /*
             * declare calendar-printing capability & register appropriate checker
             */
            getService(CapabilityService.class).declareCapability(CalendarPrintingCapabilityChecker.CAPABILITY_NAME);
            registerService(CapabilityChecker.class, new CalendarPrintingCapabilityChecker(getService(ConfigViewFactory.class)), 
                singletonDictionary(CapabilityChecker.PROPERTY_CAPABILITIES, CalendarPrintingCapabilityChecker.CAPABILITY_NAME));
            /*
             * register further services
             */
            registerService(CalendarProviderRegistry.class, providerRegistry);
            registerService(IDBasedCalendarAccessFactory.class, new CompositingIDBasedCalendarAccessFactory(providerRegistry, this));
            registerService(QuotaProvider.class, new CalendarQuotaProvider(this, providerRegistry));
        } catch (Exception e) {
            getLogger(CompositingCalendarProviderActivator.class).error("error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        getLogger(CompositingCalendarProviderActivator.class).info("stopping bundle {}", context.getBundle());
        super.stopBundle();
    }

}
