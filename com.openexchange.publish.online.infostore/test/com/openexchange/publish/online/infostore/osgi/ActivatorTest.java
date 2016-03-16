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

package com.openexchange.publish.online.infostore.osgi;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.openexchange.context.ContextService;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.osgi.ServiceProvider;
import com.openexchange.osgi.SimpleServiceProvider;
import com.openexchange.publish.PublicationDataLoaderService;
import com.openexchange.publish.PublicationService;
import com.openexchange.test.mock.InjectionFieldConstants;
import com.openexchange.test.mock.MockUtils;
import com.openexchange.test.mock.assertion.ServiceMockActivatorAsserter;


/**
 * Unit tests for {@link Activator}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4.1
 */
@RunWith(PowerMockRunner.class)
public class ActivatorTest {

    /**
     * Class under test
     */
    @InjectMocks
    private Activator activator = null;

    /**
     * {@link IDBasedFileAccessFactory} mock
     */
    @Mock
    private IDBasedFileAccessFactory idBasedFileAccessFactory;

    /**
     * {@link HttpService} mock
     */
    @Mock
    private HttpService httpService = null;

    /**
     * {@link PublicationDataLoaderService} mock
     */
    @Mock
    private PublicationDataLoaderService publicationDataLoaderService = null;

    /**
     * {@link ContextService} mock
     */
    @Mock
    private ContextService contextService = null;

    /**
     * {@link BundleContext} mock
     */
    @Mock
    private BundleContext bundleContext;

    /**
     * {@link Bundle} mock
     */
    @Mock
    private Bundle bundle;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        // SERVICES
        ConcurrentMap<Class<?>, ServiceProvider<?>> services = new ConcurrentHashMap<Class<?>, ServiceProvider<?>>();
        services.putIfAbsent(IDBasedFileAccessFactory.class, new SimpleServiceProvider<Object>(idBasedFileAccessFactory));
        services.putIfAbsent(HttpService.class, new SimpleServiceProvider<Object>(httpService));
        services.putIfAbsent(PublicationDataLoaderService.class, new SimpleServiceProvider<Object>(publicationDataLoaderService));
        services.putIfAbsent(ContextService.class, new SimpleServiceProvider<Object>(contextService));
        MockUtils.injectValueIntoPrivateField(this.activator, InjectionFieldConstants.SERVICES, services);

        // CONTEXT
        Mockito.when(this.bundleContext.getBundle()).thenReturn(this.bundle);
        MockUtils.injectValueIntoPrivateField(this.activator, InjectionFieldConstants.CONTEXT, bundleContext);
    }

    @Test
    public void testStartBundle_Fine_OneServiceRegistered() throws Exception {
        this.activator.startBundle();

        ServiceMockActivatorAsserter.verifyAllServicesRegistered(this.activator, 1);
    }

    @Test
    public void testStopBundle_Fine_AllServicesUnregistered() throws Exception {
        final Multimap<Object, ServiceRegistration<?>> serviceRegistrations = HashMultimap.create(6,2);
        ServiceRegistration<?> serviceRegistration = PowerMockito.mock(ServiceRegistration.class);
        serviceRegistrations.put(PublicationService.class, serviceRegistration);
        MockUtils.injectValueIntoPrivateField(this.activator, InjectionFieldConstants.SERVICE_REGISTRATIONS, serviceRegistrations);

        this.activator.stopBundle();

        ServiceMockActivatorAsserter.verifyAllServicesUnregistered(this.activator);
    }

    @Test
    public void testStopBundle_Fine_AllTrackersClosed() throws Exception {
        final List<ServiceTracker<?, ?>> serviceTrackers = new LinkedList<ServiceTracker<?, ?>>();
        ServiceTracker<?, ?> serviceTracker = PowerMockito.mock(ServiceTracker.class);
        serviceTrackers.add(serviceTracker);
        MockUtils.injectValueIntoPrivateField(this.activator, InjectionFieldConstants.SERVICE_TRACKERS, serviceTrackers);

        this.activator.stopBundle();

        ServiceMockActivatorAsserter.verifyAllServiceTrackersClosed(this.activator);
    }
}
