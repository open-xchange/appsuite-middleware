
package com.openexchange.realtime.presence.subscribe.test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.osgi.framework.BundleContext;
import junit.framework.TestCase;
import com.openexchange.authentication.Cookie;
import com.openexchange.java.util.UUIDs;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.presence.subscribe.PresenceSubscriptionService;
import com.openexchange.realtime.presence.subscribe.test.osgi.Activator;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

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
public class IntegrationTest extends TestCase {

    private static final String user1 = "martin.herfurth";

    private static final String password1 = "netline";

    private static final String user2 = "marcus";

    private static final String password2 = "netline";

    public static PresenceSubscriptionService subscriptionService;
    
    
    @Override
    protected void setUp() throws Exception {
        BundleContext context = Activator.getDefault().getContext();

        assertNotNull(context);
        
        subscriptionService = (PresenceSubscriptionService) context.getService(context.getServiceReference(PresenceSubscriptionService.class.getName()));
        
        assertNotNull(subscriptionService);
        
        super.setUp();
    }

    public void testWas() throws Exception {
        Presence subscription = new Presence();
        ID from = new ID(null, "marcus", "1337", null);
        subscription.setFrom(from);
        ID to = new ID(null, "martin.herfurth", "1337", null);
        subscription.setTo(to);
        subscription.setType(Presence.Type.UNSUBSCRIBED);
        try {
            
            subscriptionService.subscribe(subscription, getSessionOne());
            List<Presence> pendingRequests = subscriptionService.getPendingRequests(getSessionOne());
            assertEquals("Wrong amount of pending requests.", 1, pendingRequests.size());
            List<ID> subscribers = subscriptionService.getSubscribers(getSessionOne());
            assertEquals("No subscribers expected.", 0, subscribers.size());
            
            List<ID> subscriptions = subscriptionService.getSubscriptions(getSessionTwo());
            assertEquals("No subscriptions expected.", 0, subscriptions.size());
            
            subscriptionService.approve(from, true, getSessionOne());
            subscribers = subscriptionService.getSubscribers(getSessionOne());
            assertEquals("One subscriber expected.", 1, subscribers.size());
            ID id = subscribers.get(0);
            assertEquals(from.getUser(), id.getUser());
            assertEquals(from.getContext(), id.getContext());
            
            subscriptions = subscriptionService.getSubscriptions(getSessionTwo());
            assertEquals("One subscription expected.", 1, subscriptions.size());
            id = subscriptions.get(0);
            assertEquals("asdasd", to.getUser(), id.getUser());
            assertEquals(to.getContext(), id.getContext());
            
            subscriptionService.approve(from, false, getSessionOne());
            subscribers = subscriptionService.getSubscribers(getSessionOne());
            assertEquals("No subscribers expected.", 0, subscribers.size());
            pendingRequests = subscriptionService.getPendingRequests(getSessionOne());
            assertEquals("Wrong amount of pending requests.", 0, pendingRequests.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private ServerSession getSessionOne() throws Exception {
        return getSession(user1, password1);
    }
    
    private ServerSession getSessionTwo() throws Exception {
        return getSession(user2, password2);
    }

    private ServerSession getSession(final String user, final String password) throws Exception {
        LoginResult login = LoginPerformer.getInstance().doLogin(new LoginRequest() {

            @Override
            public String getVersion() {
                return "";
            }

            @Override
            public String getUserAgent() {
                return "test";
            }

            @Override
            public String getPassword() {
                return password;
            }

            @Override
            public String getLogin() {
                return user;
            }

            @Override
            public Interface getInterface() {
                return Interface.HTTP_JSON;
            }

            @Override
            public Map<String, List<String>> getHeaders() {
                return Collections.emptyMap();
            }

            @Override
            public String getHash() {
                return "";
            }

            @Override
            public Cookie[] getCookies() {
                return new Cookie[0];
            }

            @Override
            public String getClientIP() {
                return "";
            }

            @Override
            public String getClient() {
                return "chat";
            }

            @Override
            public String getAuthId() {
                return UUIDs.getUnformattedString(UUID.randomUUID());
            }
        });

        return ServerSessionAdapter.valueOf(login.getSession());
    }

}
