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

package com.openexchange.subscribe;

import static com.openexchange.subscribe.Asserts.assertDoesNotKnow;
import static com.openexchange.subscribe.Asserts.assertKnows;
import static com.openexchange.subscribe.Asserts.assertPriority;
import static com.openexchange.subscribe.Asserts.assertSources;
import java.util.List;
import junit.framework.TestCase;

/**
 * {@link CompositeSubscriptionSourceDiscoveryServiceTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class CompositeSubscriptionSourceDiscoveryServiceTest extends TestCase {
    private CompositeSubscriptionSourceDiscoveryService compositeDiscoverer;

    @Override
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


    public void testCompositeList() {
        List<SubscriptionSource> allSources = compositeDiscoverer.getSources(-1);
        assertNotNull("compositeDiscoverer returned null!", allSources);
        assertSources(allSources, "com.openexchange.example.source1.1", "com.openexchange.example.source1.2", "com.openexchange.example.source2.1", "com.openexchange.example.source2.2", "com.openexchange.example.source2.3");
        assertPriority(allSources, "com.openexchange.example.source1.1", 2);
    }

    public void testCompositeKnowsSource() {
        assertKnows(compositeDiscoverer, "com.openexchange.example.source1.1");
        assertKnows(compositeDiscoverer, "com.openexchange.example.source1.2");
        assertKnows(compositeDiscoverer, "com.openexchange.example.source2.1");
        assertKnows(compositeDiscoverer, "com.openexchange.example.source2.2");
        assertKnows(compositeDiscoverer, "com.openexchange.example.source2.3");

        assertDoesNotKnow(compositeDiscoverer, "com.openexchange.example.source3.1");
    }

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
