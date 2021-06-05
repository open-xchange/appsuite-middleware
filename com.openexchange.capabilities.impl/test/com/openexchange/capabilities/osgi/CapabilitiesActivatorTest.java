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

package com.openexchange.capabilities.osgi;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.powermock.api.mockito.PowerMockito;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.openexchange.caching.CacheService;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.ServiceProvider;
import com.openexchange.osgi.SimpleServiceProvider;
import com.openexchange.test.mock.InjectionFieldConstants;
import com.openexchange.test.mock.MockUtils;
import com.openexchange.test.mock.assertion.ServiceMockActivatorAsserter;

/**
 * Unit tests for {@link CapabilitiesActivator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class CapabilitiesActivatorTest {

    /**
     * Instance to test
     */
    private CapabilitiesActivator capabilitiesActivator = null;

    private final ConfigurationService configurationService = PowerMockito.mock(ConfigurationService.class);

    private final CacheService cacheService = PowerMockito.mock(CacheService.class);

    private BundleContext bundleContext;

    private Bundle bundle;

    /**
     * {@inheritDoc}
     */
    @Before
    public void setUp() {
        this.capabilitiesActivator = new CapabilitiesActivator();

        // MEMBERS
        this.bundleContext = PowerMockito.mock(BundleContext.class);
        this.bundle = PowerMockito.mock(Bundle.class);

        ConcurrentMap<Class<?>, ServiceProvider<?>> services = new ConcurrentHashMap<Class<?>, ServiceProvider<?>>();
        services.putIfAbsent(ConfigurationService.class, new SimpleServiceProvider<Object>(configurationService));
        services.putIfAbsent(CacheService.class, new SimpleServiceProvider<Object>(cacheService));
        MockUtils.injectValueIntoPrivateField(this.capabilitiesActivator, InjectionFieldConstants.SERVICES, services);

        // CONTEXT
        Mockito.when(this.bundleContext.getBundle()).thenReturn(this.bundle);
        MockUtils.injectValueIntoPrivateField(this.capabilitiesActivator, InjectionFieldConstants.CONTEXT, bundleContext);
    }

     @Test
     public void testStopBundle_EverythingFine_AllTrackersClosed() throws Exception {
        final List<ServiceTracker<?, ?>> serviceTrackers = new LinkedList<ServiceTracker<?, ?>>();
        ServiceTracker<?, ?> serviceTracker = PowerMockito.mock(ServiceTracker.class);
        serviceTrackers.add(serviceTracker);
        MockUtils.injectValueIntoPrivateField(this.capabilitiesActivator, InjectionFieldConstants.SERVICE_TRACKERS, serviceTrackers);

        this.capabilitiesActivator.stopBundle();

        ServiceMockActivatorAsserter.verifyAllServiceTrackersClosed(this.capabilitiesActivator);
    }

     @Test
     public void testStopBundle_EverythingFine_AllServicesClosed() throws Exception {
        final Multimap<Object, ServiceRegistration<?>> serviceRegistrations = HashMultimap.create(6,2);
        ServiceRegistration<?> serviceRegistration = PowerMockito.mock(ServiceRegistration.class);
        serviceRegistrations.put(CapabilityService.class, serviceRegistration);
        MockUtils.injectValueIntoPrivateField(this.capabilitiesActivator, InjectionFieldConstants.SERVICE_REGISTRATIONS, serviceRegistrations);

        this.capabilitiesActivator.stopBundle();

        ServiceMockActivatorAsserter.verifyAllServicesUnregistered(this.capabilitiesActivator);
    }
}
