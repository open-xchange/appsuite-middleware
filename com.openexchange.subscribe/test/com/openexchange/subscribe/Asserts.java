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

package com.openexchange.subscribe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * {@link Asserts}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class Asserts {
    public static void assertKnows(SubscriptionSourceDiscoveryService discoverer, String id) {
        assertTrue("Did not know: "+id, discoverer.knowsSource(id));
    }

    public static void assertDoesNotKnow(SubscriptionSourceDiscoveryService discoverer, String id) {
        assertFalse("Did know: "+id, discoverer.knowsSource(id));
    }

    public static void assertSources(List<SubscriptionSource> sources, String...expectedIdentifiers) {
        List<String> identifier = new ArrayList<String>();
        for(SubscriptionSource source : sources) {
            identifier.add(source.getId());
        }
        List<String> expectedList = Arrays.asList(expectedIdentifiers);
        assertEquals("Expected: "+expectedList+" Got: "+identifier, sources.size(), expectedIdentifiers.length);

        Set<String> actual = new HashSet<String>(identifier);
        for(String expected : expectedIdentifiers) {
            assertTrue("Expected: "+expectedList+" Got: "+identifier +" Missing: "+expected, actual.remove(expected));
        }
        assertTrue("Expected: "+expectedList+" Got: "+identifier, actual.isEmpty());

    }

    public static void assertPriority(List<SubscriptionSource> sources, String identifier, int priority) {
        for (SubscriptionSource subscriptionSource : sources) {
            if (subscriptionSource.getId().equals(identifier)) {
                assertEquals(priority, subscriptionSource.getPriority());
                return;
            }
        }
        fail("Did not found subscription source with identifier "+identifier);
    }

    public static void assertPriority(SubscriptionSource source, int priority) {
        assertEquals(priority, source.getPriority());
    }
}
