
package com.openexchange.realtime.presence.subscribe.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.Presence.Type;
import com.openexchange.realtime.presence.subscribe.PresenceSubscriptionService;
import com.openexchange.realtime.presence.subscribe.test.osgi.Activator;
import com.openexchange.test.osgi.OSGiTest;

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

/**
 * {@link IntegrationTest}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class IntegrationTest implements OSGiTest {

    public static PresenceSubscriptionService subscriptionService;

    @Before
    public void setUp() throws Exception {
        BundleContext context = Activator.getDefault().getContext();

        assertNotNull(context);

        subscriptionService = (PresenceSubscriptionService) context.getService(context.getServiceReference(PresenceSubscriptionService.class.getName()));

        assertNotNull(subscriptionService);
    }

    @Test
    public void testSubscribe() throws Exception {
        // marcus subscribes to martin
        Presence subscription = new Presence();
        ID marcus = new ID(null, null, "marcus", "1337", null);
        subscription.setFrom(marcus);
        ID martin = new ID(null, null, "martin.herfurth", "1337", null);
        subscription.setTo(martin);
        subscription.setType(Presence.Type.UNSUBSCRIBED);

        // martin approves subscription request
        Presence approval = new Presence();
        approval.setTo(marcus);
        approval.setFrom(martin);
        approval.setMessage("Sicher doch.");
        approval.setType(Type.SUBSCRIBED);
        // marcus wants to subscribe to martin
        subscriptionService.subscribe(subscription, "bitte bitte");

        // martin should have on pending subscription but no subscriber, yet
        List<Presence> pendingRequests = subscriptionService.getPendingRequests(martin);
        assertEquals("Wrong amount of pending requests.", 1, pendingRequests.size());
        assertEquals("Wrong or missing message.", "bitte bitte", pendingRequests.get(0).getMessage());
        List<ID> subscribers = subscriptionService.getSubscribers(martin);
        assertEquals("No subscribers expected.", 0, subscribers.size());

        // marcus shouldn't have any subscriptions either
        List<ID> subscriptions = subscriptionService.getSubscriptions(marcus);
        assertEquals("No subscriptions expected.", 0, subscriptions.size());

        // martin approves marcus' subscription request
        subscriptionService.approve(approval);

        // this should result in marcus being the only one subscribed to martin
        subscribers = subscriptionService.getSubscribers(martin);
        assertEquals("One subscriber expected.", 1, subscribers.size());
        ID id = subscribers.get(0);
        assertEquals(marcus.getUser(), id.getUser());
        assertEquals(marcus.getContext(), id.getContext());

        // and marcus having only one subscription, the one to martin
        subscriptions = subscriptionService.getSubscriptions(marcus);
        assertEquals("One subscription expected.", 1, subscriptions.size());
        id = subscriptions.get(0);
        assertEquals("Subscribed user doesn't match initial subscription", martin.getUser(), id.getUser());
        assertEquals(martin.getContext(), id.getContext());

        // revoke subscription again
        approval.setMessage("Hau ab!");
        approval.setType(Type.UNSUBSCRIBED);
        subscriptionService.approve(approval);

        // this should result in zero people currently being subscribed to martin
        subscribers = subscriptionService.getSubscribers(martin);
        assertEquals("No subscribers expected.", 0, subscribers.size());
        pendingRequests = subscriptionService.getPendingRequests(martin);
        assertEquals("Wrong amount of pending requests.", 0, pendingRequests.size());
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.test.osgi.OSGiTest#getTestClasses()
     */
    @Override
    public Class<?>[] getTestClasses() {
        return new Class<?>[] { IntegrationTest.class };
    }

}
