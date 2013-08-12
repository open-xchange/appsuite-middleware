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

package com.openexchange.test.mock.main;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleContext;
import com.openexchange.osgi.ServiceProvider;
import com.openexchange.osgi.SimpleServiceProvider;
import com.openexchange.server.ServiceLookup;
import com.openexchange.test.mock.main.util.InjectionFieldConstants;
import com.openexchange.test.mock.main.util.MockUtils;

/**
 * {@link ContextAndServicesActivator} activates mocking for the provided classes.
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class ContextAndServicesActivator {

    /**
     * Logger for this class
     */
    protected final static Log LOG = com.openexchange.log.Log.loggerFor(ContextAndServicesActivator.class);

    /**
     * Creates the bundle context and activates the service mocks for the given class.
     * 
     * @param activator - the activator the services and the context should be activated for
     * @param clazz - the (service) classes which should be activated for the activator
     * @return Map with the activated class - service mapping
     */
    public static ConcurrentMap<Class<?>, ServiceProvider<?>> activateContextAndServiceMocks(Object activator, Class<?>... clazz) {

        ConcurrentMap<Class<?>, ServiceProvider<?>> services = null;
        try {
            BundleContext context = MockFactory.getMock(BundleContext.class);
            MockUtils.injectValueIntoPrivateField(activator, InjectionFieldConstants.CONTEXT, context);

            services = activateServiceMocks(activator, clazz);
        } catch (Exception exception) {
            LOG.error("Not able to add the mock to available services!", exception);
        }

        return services;
    }

    /**
     * Creates the bundle context and activates the service mocks for the given class.
     * 
     * @param instance - the instance the services should be activated for
     * @param clazz - the (service) classes which should be activated for the activator
     * @return Map with the activated class - service mapping
     */
    public static ConcurrentMap<Class<?>, ServiceProvider<?>> activateServiceMocks(Object instance, Class<?>... clazz) {

        ConcurrentMap<Class<?>, ServiceProvider<?>> services = new ConcurrentHashMap<Class<?>, ServiceProvider<?>>(clazz.length);
        try {

            for (Class<?> currentClass : clazz) {
                services.putIfAbsent(currentClass, new SimpleServiceProvider(MockFactory.getMock(currentClass)));
            }
        } catch (Exception exception) {
            LOG.error("Not able to add the mock to available services!", exception);
        }

        MockUtils.injectValueIntoPrivateField(instance, InjectionFieldConstants.SERVICES, services);

        return services;
    }

    /**
     * Creates the bundle context and activates the service mocks for the given class. The used ServiceLookupMock will create a mock from
     * the MockFactory if the com.openexchange.test.mock.main.ContextAndServicesActivator.ServiceLookupMock.getService(Class<? extends S>)
     * is called.
     * 
     * @param instance - the instance the services should be activated for
     * @return Map with the activated class - service mapping
     */
    public static ServiceLookup activateServiceLookupMocks(Object instance) {
        ServiceLookup serviceLookup = new ServiceLookupMock();

        MockUtils.injectValueIntoPrivateField(instance, InjectionFieldConstants.SERVICES, serviceLookup);

        return serviceLookup;
    }

    /**
     * Returns the service which was created with com.openexchange.test.mock.main.ContextAndServicesActivator.activateServicesForBundleActivator(Object,
     * Class...)
     * 
     * @param clazz - the class the service should be returned for
     * @param services - the list of services to search within
     * @return Service that is required
     */
    @SuppressWarnings("unchecked")
    public static <T> T getActivatedService(Class<T> clazz, ConcurrentMap<Class<?>, ServiceProvider<?>> services) {
        return (T) services.get(clazz).getService();
    }

    /**
     * Class that deals with ServiceLookups
     * 
     * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
     * @since 7.4
     */
    private static class ServiceLookupMock implements ServiceLookup {

        /**
         * The services
         */
        private Map<Class, Object> services = new HashMap<Class, Object>();

        /**
         * Mock the required class
         * 
         * @param clazz
         * @return
         * @throws InstantiationException
         * @throws IllegalAccessException
         * @throws ClassNotFoundException
         */
        private <S> S mock(Class<? extends S> clazz) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
            Object object = services.get(clazz);
            if (object != null) {
                return (S) object;
            }
            S instance = MockFactory.getMock(clazz);
            services.put(clazz, instance);
            return instance;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <S> S getService(Class<? extends S> clazz) {
            try {
                return mock(clazz);
            } catch (Exception exception) {
                LOG.warn("Not able to mock the class!", exception);
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <S> S getOptionalService(Class<? extends S> clazz) {
            return (S) services.get(clazz);
        }
    }
}
