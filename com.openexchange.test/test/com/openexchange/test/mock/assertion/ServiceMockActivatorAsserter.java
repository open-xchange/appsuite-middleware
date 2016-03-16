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
