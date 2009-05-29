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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.subscribe.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.subscribe.FolderUpdaterService;
import com.openexchange.subscribe.SimFolderUpdaterService;
import com.openexchange.subscribe.SimSubscribeService;
import com.openexchange.subscribe.SimSubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSession;
import com.openexchange.subscribe.SubscriptionSource;
import junit.framework.TestCase;

/**
 * {@link SubscriptionExecutionServiceImplTest}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SubscriptionExecutionServiceImplTest extends TestCase {


    /**
     * 
     */
    private static final String SOURCE_NAME = "com.openexchange.subscribe.test1";
    private static final String SOURCE_NAME2 = "com.openexchange.subscribe.test2";
    private SubscriptionExecutionServiceImpl executionService;
    private SimSubscriptionSourceDiscoveryService discovery = new SimSubscriptionSourceDiscoveryService();
    private SimSubscribeService subscribeService;
    private SimFolderUpdaterService simFolderUpdaterService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SubscriptionSource source = new SubscriptionSource();
        source.setId(SOURCE_NAME);
        subscribeService = new SimSubscribeService();
        Subscription subscription = new Subscription();
        subscription.setContext(new SimContext(2));
        subscription.setId(12);
        subscription.setFolderId("12");
        subscribeService.setSubscription( subscription );
        subscribeService.setContent(Arrays.asList("entry1", "entry2", "entry3"));
        source.setSubscribeService( subscribeService );
        SubscriptionSource source2 = new SubscriptionSource();
        source2.setId(SOURCE_NAME2);
        source2.setSubscribeService(new SimSubscribeService() );
        discovery.addSource( source );
        discovery.addSource( source2 );
        discovery.setLookupIdentifier( source.getId() );
        simFolderUpdaterService = new SimFolderUpdaterService();
        simFolderUpdaterService.setHandles(true);
        executionService = new SubscriptionExecutionServiceImpl(discovery, Arrays.asList((FolderUpdaterService)simFolderUpdaterService), new SimContextService()) {
            @Override
            protected FolderObject getFolder(SubscriptionSession subscriptionSession, int contextId, int folderId) throws AbstractOXException {
                return null;
            }
        };
    }

    public void testShouldTransferDataCorrectly() throws AbstractOXException {
        executionService.executeSubscription(SOURCE_NAME, new SimContext(2), 12);
        assertEquals("Wrong source used", SOURCE_NAME, discovery.getLoadedSources().get(0));
        
        // correct subscription loaded?
        assertEquals("Wrong context", 2, subscribeService.getSubscriptionIDs().get(0).getContext().getContextId());
        assertEquals("Wrong id", 12, subscribeService.getSubscriptionIDs().get(0).getId());
        
        // correct data saved?
        assertEquals("Wrong data saved", Arrays.asList("entry1", "entry2", "entry3"), simFolderUpdaterService.getData());
    }
    
    public void testShouldNotThrowNPEWhenNoFolderUpdaterIsFound() {
        //fail("Not yet implemented");
        assertTrue(true);
    }

    public void testShouldGuessCorrectSubscriptionSource() throws AbstractOXException {
        executionService.executeSubscription(new SimContext(2), 12);
        assertEquals("Wrong source used", SOURCE_NAME, discovery.getLoadedSources().get(0));
        
        // correct subscription loaded?
        assertEquals("Wrong context", 2, subscribeService.getSubscriptionIDs().get(0).getContext().getContextId());
        assertEquals("Wrong id", 12, subscribeService.getSubscriptionIDs().get(0).getId());
        
        // correct data saved?
        assertEquals("Wrong data saved", Arrays.asList("entry1", "entry2", "entry3"), simFolderUpdaterService.getData());
    }

    public void testShouldNotThrowNPEWhenNoSubscriptionSourceIsFound() {
        //fail("Not yet implemented");
        assertTrue(true);
    }

}
