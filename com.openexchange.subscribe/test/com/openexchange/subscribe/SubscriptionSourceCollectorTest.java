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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.UserizedFolderImpl;

/**
 * {@link SubscriptionSourceCollectorTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SubscriptionSourceCollectorTest {

    private SubscriptionSourceCollector collector;

    private List<SubscriptionSource> sources;

    private SubscribeService testService1;

    private FolderService mock;

    @Before
    public void setUp() throws OXException {
        // Mock folder service
        mock = Mockito.mock(com.openexchange.folderstorage.FolderService.class);
        UserizedFolderImpl folderMock = Mockito.mock(UserizedFolderImpl.class);
        Mockito.when(mock.getFolder(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(folderMock);

        collector = new SubscriptionSourceCollector();
        collector.addSubscribeService(testService1 = service("com.openexchange.subscription.test1"));
        collector.addSubscribeService(service("com.openexchange.subscription.test2"));
        collector.addSubscribeService(serviceWithPriority("com.openexchange.subscription.test3", 2));
        collector.addSubscribeService(service("com.openexchange.subscription.test3"));
        collector.addSubscribeService(serviceHandlingNothing("com.openexchange.subscription.testHandlesNoFolder"));
    }

    @Test
    public void testGetSources() {
        sources = collector.getSources(2);
        assertNotNull("Sources was null!", sources);
        assertSources(sources, "com.openexchange.subscription.test1", "com.openexchange.subscription.test2", "com.openexchange.subscription.test3");

        assertPriority(sources, "com.openexchange.subscription.test3", 2);

    }

    @Test
    public void testKnows() {
        assertKnows(collector, "com.openexchange.subscription.test1");
        assertKnows(collector, "com.openexchange.subscription.test2");
        assertKnows(collector, "com.openexchange.subscription.test3");
        assertKnows(collector, "com.openexchange.subscription.testHandlesNoFolder");
        assertDoesNotKnow(collector, "unknown");
    }

    @Test
    public void testGet() {
        assertNotNull("Missing com.openexchange.susbscription.test1", collector.getSource("com.openexchange.subscription.test1"));
        assertNotNull("Missing com.openexchange.susbscription.test2", collector.getSource("com.openexchange.subscription.test2"));
        assertNotNull("Missing com.openexchange.susbscription.test3", collector.getSource("com.openexchange.subscription.test3"));
        assertNotNull("Missing com.openexchange.susbscription.testHandlesNoFolder", collector.getSource("com.openexchange.subscription.testHandlesNoFolder"));
        assertEquals("Didn't remember subscribe service", testService1, collector.getSource("com.openexchange.subscription.test1").getSubscribeService());
        assertNull("Got unknown?!?", collector.getSource("unknown"));

        assertPriority(collector.getSource("com.openexchange.subscription.test3"), 2);
    }

    @Test
    public void testRemove() {
        collector.removeSubscribeService("com.openexchange.subscription.test1");
        sources = collector.getSources(2);
        assertNotNull("Sources was null!", sources);
        assertSources(sources, "com.openexchange.subscription.test2", "com.openexchange.subscription.test3");
        assertDoesNotKnow(collector, "com.openexchange.subscription.test1");
        assertNull("Didn't expect a source", collector.getSource("com.openexchange.subscription.test1"));
    }

    private SubscribeService serviceWithPriority(String string, int i) {
        SubscribeService service = service(string);
        service.getSubscriptionSource().setPriority(i);
        return service;
    }

    private SubscribeService service(String string) {
        SubscriptionSource source = new SubscriptionSource();
        source.setId(string);
        SimSubscribeService service = SimSubscribeService.createSimSubscribeService(mock);
        service.setSubscriptionSource(source);
        source.setSubscribeService(service);
        return service;
    }

    private SubscribeService serviceHandlingNothing(String string) {
        SubscriptionSource source = new SubscriptionSource();
        source.setId(string);

        SimSubscribeService service = new SimSubscribeService(mock) {

            @Override
            public boolean handles(int folderModule) {
                return false;
            }

        };
        service.setSubscriptionSource(source);
        return service;

    }

}
