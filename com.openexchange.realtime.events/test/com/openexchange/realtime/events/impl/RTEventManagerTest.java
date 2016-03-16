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

package com.openexchange.realtime.events.impl;

import static com.openexchange.realtime.events.RTEventMatcher.isRTEvent;
import static com.openexchange.realtime.packet.StanzaMatcher.isStanza;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.events.RTEvent;
import com.openexchange.realtime.events.RTEventEmitterService;
import com.openexchange.realtime.events.RTListener;
import com.openexchange.realtime.packet.ID;
import com.openexchange.server.MockingServiceLookup;


/**
 * {@link RTEventManagerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RTEventManagerTest {

    private MockingServiceLookup services;
    private RTEventManager events = null;
    private ID id = new ID("test://test1@1");

    private RTListener listener;

    private RTEventEmitterService emitter = new RTEventEmitterService() {

        @Override
        public String getNamespace() {
            return "em1";
        }

        @Override
        public Set<String> getSupportedEvents() {
            return new HashSet<String>(Arrays.asList("ev1", "ev2"));
        }

        @Override
        public void register(String eventName, RTListener listener) {
            RTEventManagerTest.this.listener = listener;
        }

        @Override
        public void unregister(String eventName, RTListener listener) {
            if (RTEventManagerTest.this.listener.equals(listener)) {
                listener = null;
            }
        }

    };

    @Before
    public void setUp() throws Exception {
        services = new MockingServiceLookup();
        events = new RTEventManager(services);
    }

    @Test
    public void shouldListAllAvailableEvents() {
        RTEventEmitterService emitter1 = mock(RTEventEmitterService.class);
        when(emitter1.getNamespace()).thenReturn("em1");
        when(emitter1.getSupportedEvents()).thenReturn(new HashSet<String>(Arrays.asList("ev1", "ev2")));

        RTEventEmitterService emitter2 = mock(RTEventEmitterService.class);
        when(emitter2.getNamespace()).thenReturn("em2");
        when(emitter2.getSupportedEvents()).thenReturn(new HashSet<String>(Arrays.asList("ev1", "ev2")));

        RTEventEmitterService emitter3 = mock(RTEventEmitterService.class);
        when(emitter3.getNamespace()).thenReturn("em3");
        when(emitter3.getSupportedEvents()).thenReturn(new HashSet<String>(Arrays.asList("ev1", "ev2")));

        events.addEmitter(emitter1);
        events.addEmitter(emitter2);
        events.addEmitter(emitter3);

        Set<String> supported = events.getSupportedEvents();

        assertNotNull(supported);

        assertEquals(6, supported.size());

        assertTrue(supported.remove("em1:ev1"));
        assertTrue(supported.remove("em1:ev2"));

        assertTrue(supported.remove("em2:ev1"));
        assertTrue(supported.remove("em2:ev2"));

        assertTrue(supported.remove("em3:ev1"));
        assertTrue(supported.remove("em3:ev2"));
    }

    @Test
    public void shouldManageEventSubscriptions() {

        RTEventEmitterService emitter = setUpEmitter();

        events.subscribe("em1:ev2", "abc", id, null, null);

        Set<String> subscriptions = events.getSubscriptions(id);

        assertNotNull(subscriptions);
        assertEquals(1, subscriptions.size());
        assertTrue(subscriptions.remove("em1:ev2"));

        verify(emitter, times(1)).register( eq("ev2"), Mockito.<RTListener> anyObject() );
    }

    @Test
    public void shouldIgnoreDoubleSubscription() {

        RTEventEmitterService emitter = setUpEmitter();

        events.subscribe("em1:ev2", "abc", id, null, null);
        events.subscribe("em1:ev2", "abc", id, null, null);

        Set<String> subscriptions = events.getSubscriptions(id);

        assertNotNull(subscriptions);
        assertEquals(1, subscriptions.size());
        assertTrue(subscriptions.remove("em1:ev2"));

        verify(emitter, times(1)).register( eq("ev2"), Mockito.<RTListener> anyObject() );
    }

    @Test
    public void shouldPassAlongEvents() throws OXException {
        events.addEmitter(emitter);
        events.subscribe("em1:ev2", "abc", id, null, null);

        // Trigger Event
        listener.handle(new RTEvent("Hello", "string"));


        // Verify the MessageDispatcher was used to send the event
        MessageDispatcher dispatcher = services.getService(MessageDispatcher.class);

        verify(dispatcher).send(argThat(isStanza(id, id, "event", "event", "abc", isRTEvent("em1:ev2", "Hello"))));
    }

    @Test
    public void itShouldBePossibleToUnsubscribeFromAnEvent() {
        RTEventEmitterService emitter = setUpEmitter();

        events.subscribe("em1:ev2", "abc", id, null, null);
        events.subscribe("em1:ev1", "abc", id, null, null);

        events.unsubscribe("em1:ev2", id);

        Set<String> subscriptions = events.getSubscriptions(id);

        assertEquals(1, subscriptions.size());
        assertTrue(subscriptions.remove("em1:ev1"));

        verify(emitter, times(1)).register( eq("ev2"), Mockito.<RTListener> anyObject() );
        verify(emitter, times(1)).register( eq("ev1"), Mockito.<RTListener> anyObject() );
        verify(emitter, times(1)).unregister( eq("ev2"), Mockito.<RTListener> anyObject() );
    }


    @Test
    public void unsubscribeShouldClearAllSubscriptions() {
        RTEventEmitterService emitter = setUpEmitter();

        events.subscribe("em1:ev2", "abc", id, null, null);
        events.subscribe("em1:ev1", "abc", id, null, null);

        assertEquals(2, events.getSubscriptions(id).size());

        events.unsubscribe(id);

        Set<String> subscriptions = events.getSubscriptions(id);
        assertTrue(subscriptions.isEmpty());

        verify(emitter, times(1)).register( eq("ev2"), Mockito.<RTListener> anyObject() );
        verify(emitter, times(1)).register( eq("ev1"), Mockito.<RTListener> anyObject() );
        verify(emitter, times(1)).unregister( eq("ev2"), Mockito.<RTListener> anyObject() );
        verify(emitter, times(1)).unregister( eq("ev1"), Mockito.<RTListener> anyObject() );

    }

    @Test
    public void shouldUnsubscribeWhenIDIsDisposedOf() {
        RTEventEmitterService emitter = setUpEmitter();

        events.subscribe("em1:ev2", "abc", id, null, null);
        events.subscribe("em1:ev1", "abc", id, null, null);

        assertEquals(2, events.getSubscriptions(id).size());

        events.cleanupForId(id);

        Set<String> subscriptions = events.getSubscriptions(id);
        assertTrue(subscriptions.isEmpty());

        verify(emitter, times(1)).register( eq("ev2"), Mockito.<RTListener> anyObject() );
        verify(emitter, times(1)).register( eq("ev1"), Mockito.<RTListener> anyObject() );
        verify(emitter, times(1)).unregister( eq("ev2"), Mockito.<RTListener> anyObject() );
        verify(emitter, times(1)).unregister( eq("ev1"), Mockito.<RTListener> anyObject() );
    }


    private RTEventEmitterService setUpEmitter() {
        RTEventEmitterService emitter = mock(RTEventEmitterService.class);
        when(emitter.getNamespace()).thenReturn("em1");
        when(emitter.getSupportedEvents()).thenReturn(new HashSet<String>(Arrays.asList("ev1", "ev2")));

        events.addEmitter(emitter);


        return emitter;
    }

}
