/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
