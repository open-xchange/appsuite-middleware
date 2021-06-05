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

package com.openexchange.test.mock.assertion;

import java.util.List;
import org.junit.Assert;
import com.google.common.collect.Multimap;
import com.openexchange.test.mock.InjectionFieldConstants;
import com.openexchange.test.mock.MockUtils;

/**
 * {@link ServiceMockActivatorAsserter} activates mocking for the provided classes.
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class ServiceMockActivatorAsserter {

    /**
     * Verifies if all services associated with the bundle are registered
     * 
     * @param activator - the activator the services should be checked for
     * @param expectedNumberOfRegisteredTrackers - int with the number of expected registered services
     */
    @SuppressWarnings("unchecked")
    public static void verifyAllServicesRegistered(Object activator, int expectedNumberOfRegisteredServices) {
        Multimap<Object, Object> serviceRegistrations = (Multimap<Object, Object>) MockUtils.getValueFromField(
            activator,
            InjectionFieldConstants.SERVICE_REGISTRATIONS);

        Assert.assertEquals(
            "Registered services do not match the number of expected ones",
            expectedNumberOfRegisteredServices,
            serviceRegistrations.size());
    }

    /**
     * Verifies if all services associated with the bundle are unregistered
     * 
     * @param activator - the activator the services should be checked for
     */
    @SuppressWarnings("unchecked")
    public static void verifyAllServicesUnregistered(Object activator) {
        Multimap<Object, Object> serviceRegistrations = (Multimap<Object, Object>) MockUtils.getValueFromField(
            activator,
            InjectionFieldConstants.SERVICE_REGISTRATIONS);

        Assert.assertEquals("Not all services deregistered!", 0, serviceRegistrations.size());
    }

    /**
     * Verifies if all trackers associated with the bundle are registered
     * 
     * @param activator - the activator the trackers should be checked for
     * @param expectedNumberOfRegisteredTrackers - int with the number of expected registered trackers
     */
    public static void verifyAllServiceTrackersRegistered(Object activator, int expectedNumberOfRegisteredTrackers) {
        List<?> serviceTrackers = (List<?>) MockUtils.getValueFromField(
            activator,
            InjectionFieldConstants.SERVICE_TRACKERS);

        Assert.assertEquals(
            "Registered trackers do not match the number of expected ones",
            expectedNumberOfRegisteredTrackers,
            serviceTrackers.size());
    }

    /**
     * Verifies if all trackers associated with the bundle are closed
     * 
     * @param activator - the activator the trackers should be checked for
     */
    public static void verifyAllServiceTrackersClosed(Object activator) {
        List<?> serviceTrackers = (List<?>) MockUtils.getValueFromField(
            activator,
            InjectionFieldConstants.SERVICE_TRACKERS);

        Assert.assertEquals("Not all trackers closed!", 0, serviceTrackers.size());
    }
}
