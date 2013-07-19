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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.tokenlogin.impl.osgi;

import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import com.openexchange.config.ConfigurationService;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.osgi.ServiceProvider;
import com.openexchange.test.mock.ServiceMockActivator;
import com.openexchange.test.mock.test.AbstractMockTest;
import com.openexchange.test.mock.util.MockUtils;
import com.openexchange.tokenlogin.impl.Services;


/**
 * Unit tests for {@link TokenLoginActivator}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
@PrepareForTest({ Services.class })
public class TokenLoginActivatorTest extends AbstractMockTest {

    /**
     * Instance to test
     */
    private TokenLoginActivator tokenLoginActivator = null;

    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Services.class);

    }

    @Test
    public void testStartBundle_HazelcastDisabled_Registered2ServicesAndNoTracker() throws Exception {
        this.tokenLoginActivator = new TokenLoginActivator();

        ConcurrentMap<Class<?>, ServiceProvider<?>> activateServicesFor = ServiceMockActivator.activateServiceMocks(
            this.tokenLoginActivator,
            HazelcastConfigurationService.class,
            ConfigurationService.class);

        HazelcastConfigurationService hazelcastService = ServiceMockActivator.getActivatedService(
            HazelcastConfigurationService.class,
            activateServicesFor);
        PowerMockito.when(hazelcastService.isEnabled()).thenReturn(false);

        this.tokenLoginActivator.startBundle();

        Mockito.verify(hazelcastService, Mockito.times(1)).isEnabled();

        Map<Object, ServiceRegistration<?>> serviceRegistrations = (Map<Object, ServiceRegistration<?>>) MockUtils.getValueFromField(
            this.tokenLoginActivator,
            "serviceRegistrations");

        assertTrue(2 == serviceRegistrations.size());

        List<ServiceTracker<?, ?>> serviceTrackers = (List<ServiceTracker<?, ?>>) MockUtils.getValueFromField(
            this.tokenLoginActivator, "serviceTrackers");

        assertTrue(0 == serviceTrackers.size());
    }

    @Test
    public void testStartBundle_HazelcastEnabled_Registered2ServicesAnd1Tracker() throws Exception {
        this.tokenLoginActivator = new TokenLoginActivator();

        ServiceMockActivator.activateServiceMocks(
            this.tokenLoginActivator,
            HazelcastConfigurationService.class,
            ConfigurationService.class);

        this.tokenLoginActivator.startBundle();

        Map<Object, ServiceRegistration<?>> serviceRegistrations = (Map<Object, ServiceRegistration<?>>) MockUtils.getValueFromField(
            this.tokenLoginActivator,
            "serviceRegistrations");

        assertTrue(2 == serviceRegistrations.size());

        List<ServiceTracker<?, ?>> serviceTrackers = (List<ServiceTracker<?, ?>>) MockUtils.getValueFromField(
            this.tokenLoginActivator,
            "serviceTrackers");

        assertTrue(1 == serviceTrackers.size());
    }
}
