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

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.events.RTEvent;
import com.openexchange.realtime.events.RTListener;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Message;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * An {@link RTEventSubscription} models an active subscription to a given event. Used only internally. 
 * RTEventSubscription#handle takes an RTEvent and retransmits it to the interested client via the RT messaging system.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RTEventSubscription implements RTListener {

    private final ServiceLookup services;
    private final ID id;
    private final String selector;
    private final String eventName;
    private final String namespace;
    private final Session session;
    private final Map<String, String> parameters;

    /**
     * Initializes a new {@link RTEventSubscription} e.g. mail:new
     * 
     * @param namespace The namespace of the subscription e.g. "mail".
     * @param eventName The name identifying the event in it's namespace
     * @param selector The selector the client chose to associate with this {@link RTEventSubscription} to easily distinguish incoming
     *            messages on the client side
     * @param id The {@link ID} of the subscribing client
     * @param session The active session used by the client
     * @param parameters Subscription parameters
     * @param services The needed ServiceLookup
     */
    public RTEventSubscription(String namespace, String eventName, String selector, ID id, Session session, Map<String, String> parameters, ServiceLookup services) {
        super();
        this.namespace = namespace;
        this.eventName = eventName;
        this.services = services;
        this.id = id;
        this.selector = selector;
        this.session = session;
        this.parameters = parameters;
    }
    
    @Override
    public ID getID() {
        return id;
    }
    
    @Override
    public Session getSession() {
        return session;
    }
    
    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }
    
    public String getEvent() {
        return String.format("%s:%s", namespace, eventName);
    }
    
    public String getEventName() {
        return eventName;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    /**
     * Produces event stanzas that have the following form:
     * {
     *   "selector": $selector,
     *   "element": "message",
     *   "payloads": [
     *     {
     *       "namespace": "event"
     *       "element": "event",
     *       "data": [
     *         {
     *           "namespace": "event"
     *           "element": "name",
     *           "data": $namespaceEventName,
     *         },
     *         {
     *           "namespace": "event",
     *           "element": "data",
     *           "data": $dataWithTheGivenFormatTransformedToJSON
     *         }
     *       ],
     *     }
     *   ],
     *   "from": $id,
     *   "to": $id
     * }
     * 
     */
    @Override
    public void handle(RTEvent rtEvent) {
        MessageDispatcher dispatcher = services.getService(MessageDispatcher.class);
        
        Message eventMessage = new Message();
        eventMessage.setFrom(id);
        eventMessage.setSelector(selector);
        eventMessage.setTo(id);
        
        eventMessage.addPayload(new PayloadTree(PayloadTreeNode.builder()
                .withPayload(null, "json", "event", "event")
                .andChild(getEvent(), "native", "event", "name")
                .andChild(rtEvent.getPayload(), rtEvent.getFormat(), "event", "data")
                .build()));
        
        try {
            dispatcher.send(eventMessage);
        } catch (OXException e) {
            // Ignore, can't do anything about this.
        }
    }

}
