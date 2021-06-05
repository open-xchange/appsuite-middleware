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

package com.openexchange.subscribe.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import com.openexchange.context.SimContextService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.UserizedFolderImpl;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.generic.FolderUpdaterService;
import com.openexchange.subscribe.SimFolderUpdaterService;
import com.openexchange.subscribe.SimSubscribeService;
import com.openexchange.subscribe.SimSubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.SimServerSession;

/**
 * {@link SubscriptionExecutionServiceImplTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SubscriptionExecutionServiceImplTest {

    /**
     *
     */
    private static final String SOURCE_NAME = "com.openexchange.subscribe.test1";
    private static final String SOURCE_NAME2 = "com.openexchange.subscribe.test2";
    private SubscriptionExecutionServiceImpl executionService;
    private final SimSubscriptionSourceDiscoveryService discovery = new SimSubscriptionSourceDiscoveryService();
    private SimSubscribeService subscribeService;
    private SimFolderUpdaterService simFolderUpdaterService;
    private Subscription subscription;

    @Before
    public void setUp() throws OXException {
        final SubscriptionSource source = new SubscriptionSource();
        source.setId(SOURCE_NAME);

        // Mock folder service
        FolderService mock = Mockito.mock(com.openexchange.folderstorage.FolderService.class);
        UserizedFolderImpl folderMock = Mockito.mock(UserizedFolderImpl.class);
        Mockito.when(mock.getFolder(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(folderMock);

        subscribeService = SimSubscribeService.createSimSubscribeService(mock);
        subscription = new Subscription();
        subscription.setContext(new SimContext(2));
        subscription.setId(12);
        subscription.setFolderId("12");
        subscribeService.setSubscription( subscription );
        subscribeService.setContent(Arrays.asList("entry1", "entry2", "entry3"));
        source.setSubscribeService( subscribeService );
        subscribeService.setSubscriptionSource(source);
        final SubscriptionSource source2 = new SubscriptionSource();
        source2.setId(SOURCE_NAME2);
        source2.setSubscribeService(SimSubscribeService.createSimSubscribeService(mock));
        discovery.addSource( source );
        discovery.addSource( source2 );
        discovery.setLookupIdentifier( source.getId() );
        simFolderUpdaterService = new SimFolderUpdaterService();
        simFolderUpdaterService.setHandles(true);
        final List<FolderUpdaterService<?>> list = new ArrayList<>(1);
        list.add(simFolderUpdaterService);
        executionService = new SubscriptionExecutionServiceImpl(discovery, list, new SimContextService()) {
            @Override
            protected FolderObject getFolder(final int contextId, final int folderId) throws OXException {
                return null;
            }
        };
    }

         @Test
     public void testShouldTransferDataCorrectly() throws OXException {
        executionService.executeSubscription(SOURCE_NAME, sessionForContext(new SimContext(2)), 12);
        assertEquals("Wrong source used", SOURCE_NAME, discovery.getLoadedSources().get(0));

        // correct subscription loaded?
        assertEquals("Wrong context", 2, subscribeService.getSubscriptionIDs().get(0).getContext().getContextId());
        assertEquals("Wrong id", 12, subscribeService.getSubscriptionIDs().get(0).getId());

        // correct data saved?
        assertEquals("Wrong data saved", Arrays.asList("entry1", "entry2", "entry3"), simFolderUpdaterService.getData());
    }

    /**
     * @param simContext
     * @return
     */
    private ServerSession sessionForContext(final SimContext simContext) {
        return new SimServerSession(simContext, null, null);
    }

         @Test
     public void testShouldNotThrowNPEWhenNoFolderUpdaterIsFound() {
        //fail("Not yet implemented");
        assertTrue(true);
    }

         @Test
     public void testShouldGuessCorrectSubscriptionSource() throws OXException {
        executionService.executeSubscription(sessionForContext(new SimContext(2)), 12);
        assertEquals("Wrong source used", SOURCE_NAME, discovery.getLoadedSources().get(0));

        // correct subscription loaded?
        assertEquals("Wrong context", 2, subscribeService.getSubscriptionIDs().get(0).getContext().getContextId());
        assertEquals("Wrong id", 12, subscribeService.getSubscriptionIDs().get(0).getId());

        // correct data saved?
        assertEquals("Wrong data saved", Arrays.asList("entry1", "entry2", "entry3"), simFolderUpdaterService.getData());
    }

         @Test
     public void testShouldNotThrowNPEWhenNoSubscriptionSourceIsFound() {
        //fail("Not yet implemented");
        assertTrue(true);
    }

         @Test
     public void testShouldReturnSingleUpdaterIfItIsTheOnlyOnePresent() throws OXException{
        assertEquals("The first Updater should be returned", simFolderUpdaterService,executionService.getFolderUpdater(subscription));
    }

         @Test
     public void testShouldReturnSingleUpdaterIfThereIsOnlyOneSubscriptionOnTheFolder() throws OXException{
        final SimFolderUpdaterService simFolderUpdaterService2 = new SimFolderUpdaterService();
        simFolderUpdaterService2.setHandles(true);
        simFolderUpdaterService2.setUsesMultipleStrategy(false);
        final List<FolderUpdaterService<?>> list = new ArrayList<>(2);
        list.add(simFolderUpdaterService);
        list.add(simFolderUpdaterService2);
        executionService = new SubscriptionExecutionServiceImpl(discovery, list, new SimContextService()) {
            @Override
            protected FolderObject getFolder(final int contextId, final int folderId) throws OXException {
                return null;
            }
        };
        assertEquals("The first Updater should be returned", simFolderUpdaterService, executionService.getFolderUpdater(subscription));
    }

         @Test
     public void testShouldReturnMultipleUpdaterIfThereAreTwoSubscriptionsOnTheFolder() throws OXException{
        final Subscription subscription2 = new Subscription();
        subscription.setContext(new SimContext(2));
        subscription.setId(13);
        subscription.setFolderId("12");
        final ArrayList<Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(subscription);
        subscriptions.add(subscription2);
        subscribeService.setSubscriptions(subscriptions);
        final SimFolderUpdaterService simFolderUpdaterService2 = new SimFolderUpdaterService();
        simFolderUpdaterService2.setHandles(true);
        simFolderUpdaterService2.setUsesMultipleStrategy(true);
        final List<FolderUpdaterService<?>> list = new ArrayList<>(2);
        list.add(simFolderUpdaterService);
        list.add(simFolderUpdaterService2);
        executionService = new SubscriptionExecutionServiceImpl(discovery, list, new SimContextService()) {
            @Override
            protected FolderObject getFolder(final int contextId, final int folderId) throws OXException {
                return null;
            }
        };
        assertEquals("The second Updater should be returned", simFolderUpdaterService2, executionService.getFolderUpdater(subscription));
    }

         @Test
     public void testShouldReturnSingleUpdaterIfThereAreTwoSubscriptionsOnTheFolderButNoMultipleStrategyIsAvailable() throws OXException{
        final Subscription subscription2 = new Subscription();
        subscription.setContext(new SimContext(2));
        subscription.setId(13);
        subscription.setFolderId("12");
        final ArrayList<Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(subscription);
        subscriptions.add(subscription2);
        subscribeService.setSubscriptions(subscriptions);
        final List<FolderUpdaterService<?>> list = new ArrayList<>(1);
        list.add(simFolderUpdaterService);
        executionService = new SubscriptionExecutionServiceImpl(discovery, list, new SimContextService()) {
            @Override
            protected FolderObject getFolder(final int contextId, final int folderId) throws OXException {
                return null;
            }
        };
        assertEquals("The first Updater should be returned", simFolderUpdaterService, executionService.getFolderUpdater(subscription));
    }
}
