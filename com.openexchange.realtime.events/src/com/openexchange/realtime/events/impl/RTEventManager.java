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

package com.openexchange.realtime.events.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.openexchange.realtime.events.RTEventEmitterService;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.IDEventHandler;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * The {@link RTEventManager} manages subscription state and dispatches events from its {@link RTEventEmitterService} instances to
 * interested clients via the RT messaging system.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RTEventManager {
    
    private ConcurrentHashMap<String, RTEventEmitterService> emitterFactories = new ConcurrentHashMap<String, RTEventEmitterService>(); 

    private ConcurrentHashMap<ID, List<RTEventSubscription>> subscriptions = new ConcurrentHashMap<ID, List<RTEventSubscription>>();

    private ServiceLookup services;
    
    /**
     * Initializes a new {@link RTEventManager}.
     * @param services
     */
    public RTEventManager(ServiceLookup services) {
        super();
        this.services = services;
    }
    
    /**
     * Register an emitter. Usually no one calls this directly, instead export {@link RTEventEmitterService} instances via OSGi.
     * @param factory
     */
    public void addEmitter(RTEventEmitterService factory) {
        emitterFactories.put(factory.getNamespace(), factory);
    }
    
    /**
     * Takes an emitter down, again usually only called when an {@link RTEventEmitterService} disappears from the OSGi system.
     * @param service
     */
    public void removeEmitter(RTEventEmitterService service) {
        emitterFactories.remove(service.getNamespace());
    }

    
    /**
     * Gets the sum total of all supported events. These events are namespaced per known emitter.
     */
    public Set<String> getSupportedEvents() {
        HashSet<String> supportedEvents = new HashSet<String>();
        
        for(RTEventEmitterService factory: emitterFactories.values()) {
            for(String eventName: factory.getSupportedEvents()) {
                supportedEvents.add(factory.getNamespace() + ":" + eventName);
            }
        }
        
        return supportedEvents;
    }
    
    /**
     * Subscribe to a namespaced event
     * @param event The name of the event, complete with the namespace eg. calendar:new
     * @param selector An arbitrary string that will be sent along with all event stanzas as their selector
     * @param id The ID of the client that wants to be notified of events
     * @param session The session of the client
     * @param parameters Arbitrary parameters that may or may not mean something to the {@link RTEventEmitterService} instance associated with the namespace
     */
    public void subscribe(String event, String selector, ID id, Session session, Map<String, String> parameters) {
        String[] parsedEvent = parse(event);
        if (parsedEvent.length != 2) {
            throw new IllegalArgumentException("Event names always consist of a namespace and name tuple separated by a colon, e.g. mail:new. Yours was: " + event);
        }
        String namespace = parsedEvent[0];
        String eventName = parsedEvent[1];
        
        RTEventEmitterService emitter = emitterFactories.get(namespace);
        if (emitter == null) {
            throw new IllegalArgumentException("Don't know namespace: " + namespace);
        }
        
        if (!emitter.getSupportedEvents().contains(eventName)) {
            throw new IllegalArgumentException("Emitter "+ namespace + " does not know about event " + eventName);
        }
        
        List<RTEventSubscription> subscriptions = getSubscriptions(id, true);
        for (RTEventSubscription subscription : subscriptions) {
            if (subscription.getEvent().equals(event)) {
                return; // Already subscribed
            }
        }
        RTEventSubscription subscription = new RTEventSubscription(namespace, eventName, selector, id, session, parameters, services);
        emitter.register(eventName, subscription);
        
        subscriptions.add(subscription);
    }

 
    private String[] parse(String event) {
        return event.split(":");
    }
    
    /**
     * Retrieve all namespaced event names a given ID is subscribed to
     */
    public Set<String> getSubscriptions(ID id) {
        List<RTEventSubscription> list = getSubscriptions(id, false);
        if (list == null) {
            return Collections.<String>emptySet();
        }
        Set<String> subscriptions = new HashSet<String>(list.size());
        
        for (RTEventSubscription rtEventSubscription : list) {
            subscriptions.add(rtEventSubscription.getEvent());
        }

        return subscriptions;
    }

    private List<RTEventSubscription> getSubscriptions(ID id, boolean createIfNeeded) {
        List<RTEventSubscription> list = subscriptions.get(id);
        if (list == null && createIfNeeded) {
            list = new CopyOnWriteArrayList<RTEventSubscription>();
            List<RTEventSubscription> meantime = subscriptions.putIfAbsent(id, list);
            list = (meantime != null) ? meantime : list;
            if (meantime == null) {
                id.on("dispose", new IDEventHandler() {
                    
                    @Override
                    public void handle(String event, ID id, Object source, Map<String, Object> properties) {
                        unsubscribe(id);
                    }
                });
            }
        }
        return list;
    }
    
    /**
     * Unsubscribe from all events this ID subscribed to.
     */
    public void unsubscribe(ID id) {
        List<RTEventSubscription> list = subscriptions.remove(id);
        if (list == null) {
            return;
        }
        for (RTEventSubscription subscription : list) {
            String namespace = subscription.getNamespace();
            String eventName = subscription.getEventName();
            RTEventEmitterService emitter = emitterFactories.get(namespace);
            if (emitter == null) {
                continue;
            }
            emitter.unregister(eventName, subscription);
        }
        
        
    }
    
    /**
     * Unsubscribe from a specific event this ID subscribed to
     */
    public void unsubscribe(String event, ID id) {
        String[] parsedEvent = parse(event);
        if (parsedEvent.length != 2) {
            return;
        }
        String namespace = parsedEvent[0];
        String eventName = parsedEvent[1];
        
        RTEventEmitterService emitter = emitterFactories.get(namespace);
        if (emitter == null) {
            return;
        }
        
        List<RTEventSubscription> list = subscriptions.get(id);
        
        if (list == null) {
            return;
        }
        
        List<RTEventSubscription> toRemove = new ArrayList<RTEventSubscription>();
        
        for (RTEventSubscription subscription : list) {
            if (subscription.getEvent().equals(event)) {
                emitter.unregister(eventName, subscription);
                toRemove.add(subscription);
            }
        }
        
        list.removeAll(toRemove);
    }


}
