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

import static com.openexchange.subscribe.Asserts.assertDoesNotKnow;
import static com.openexchange.subscribe.Asserts.assertKnows;
import static com.openexchange.subscribe.Asserts.assertPriority;
import static com.openexchange.subscribe.Asserts.assertSources;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link CompositeSubscriptionSourceDiscoveryServiceTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class CompositeSubscriptionSourceDiscoveryServiceTest {    private CompositeSubscriptionSourceDiscoveryService compositeDiscoverer;

    @Before
    public void setUp() {
        SimSubscriptionSourceDiscoveryService discoverer1 = new SimSubscriptionSourceDiscoveryService();
        SimSubscriptionSourceDiscoveryService discoverer2 = new SimSubscriptionSourceDiscoveryService();
        SimSubscriptionSourceDiscoveryService discoverer3 = new SimSubscriptionSourceDiscoveryService();

        discoverer1.addSource(source("com.openexchange.example.source1.1"));
        discoverer1.addSource(source("com.openexchange.example.source1.2"));

        discoverer2.addSource(source("com.openexchange.example.source2.1"));
        discoverer2.addSource(source("com.openexchange.example.source2.2"));
        discoverer2.addSource(source("com.openexchange.example.source2.3"));

        discoverer3.addSource(sourceWithPriority("com.openexchange.example.source1.1", 2));

        this.compositeDiscoverer = new CompositeSubscriptionSourceDiscoveryService();
        compositeDiscoverer.addSubscriptionSourceDiscoveryService(discoverer1);
        compositeDiscoverer.addSubscriptionSourceDiscoveryService(discoverer2);
        compositeDiscoverer.addSubscriptionSourceDiscoveryService(discoverer3);
    }


         @Test
     public void testCompositeList() {
        List<SubscriptionSource> allSources = compositeDiscoverer.getSources(-1);
        assertNotNull("compositeDiscoverer returned null!", allSources);
        assertSources(allSources, "com.openexchange.example.source1.1", "com.openexchange.example.source1.2", "com.openexchange.example.source2.1", "com.openexchange.example.source2.2", "com.openexchange.example.source2.3");
        assertPriority(allSources, "com.openexchange.example.source1.1", 2);
    }

         @Test
     public void testCompositeKnowsSource() {
        assertKnows(compositeDiscoverer, "com.openexchange.example.source1.1");
        assertKnows(compositeDiscoverer, "com.openexchange.example.source1.2");
        assertKnows(compositeDiscoverer, "com.openexchange.example.source2.1");
        assertKnows(compositeDiscoverer, "com.openexchange.example.source2.2");
        assertKnows(compositeDiscoverer, "com.openexchange.example.source2.3");

        assertDoesNotKnow(compositeDiscoverer, "com.openexchange.example.source3.1");
    }

         @Test
     public void testCompositeGet() {
        assertNotNull("Failed getting com.openexchange.example.source1.1", compositeDiscoverer.getSource("com.openexchange.example.source1.1"));
        assertNotNull("Failed getting com.openexchange.example.source1.1", compositeDiscoverer.getSource("com.openexchange.example.source1.2"));
        assertNotNull("Failed getting com.openexchange.example.source1.1", compositeDiscoverer.getSource("com.openexchange.example.source2.1"));
        assertNotNull("Failed getting com.openexchange.example.source1.1", compositeDiscoverer.getSource("com.openexchange.example.source2.2"));
        assertNotNull("Failed getting com.openexchange.example.source1.1", compositeDiscoverer.getSource("com.openexchange.example.source2.3"));

        assertNull("Got com.openexchange.example.source3.1 ?!?", compositeDiscoverer.getSource("com.openexchange.example.source3.1"));

        assertPriority(compositeDiscoverer.getSource("com.openexchange.example.source1.1"),2);
    }

    private SubscriptionSource sourceWithPriority(String string, int i) {
        SubscriptionSource source = source(string);
        source.setPriority(i);
        return source;
    }

    private SubscriptionSource source(String identifier) {
        SubscriptionSource source = new SubscriptionSource();
        source.setId(identifier);
        return source;
    }
}
