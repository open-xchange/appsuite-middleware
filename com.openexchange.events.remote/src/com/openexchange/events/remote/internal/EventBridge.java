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

package com.openexchange.events.remote.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.ms.Message;
import com.openexchange.ms.MessageListener;
import com.openexchange.ms.MsService;
import com.openexchange.ms.Topic;
import com.openexchange.session.Session;

/**
 * {@link EventBridge}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class EventBridge implements MessageListener<Map<String, Object>>, EventHandler {

    /**
     * The prefix of topics to consider by the event bridge.
     */
    public static final String TOPIC_PREFIX = "com/openexchange/";

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(EventBridge.class);
    private static final String TOPIC_NAME = "remoteEvents-0";
    private static final String POJO_PACKAGE = "java.lang.";

    private final EventAdmin eventAdmin;
    private final Topic<Map<String, Object>> messagingTopic;

    /**
     * Initializes a new {@link EventBridge}.
     *
     * @param eventAdmin The event admin for local events
     * @param messagingService The messaging service for remote events
     * @throws OXException
     */
    public EventBridge(EventAdmin eventAdmin, MsService messagingService) {
        super();
        this.eventAdmin = eventAdmin;
        this.messagingTopic = messagingService.getTopic(TOPIC_NAME);
        this.messagingTopic.addMessageListener(this);
        LOG.debug("Using topic {} for remote event distribution.", messagingTopic.getName());
    }

    @Override
    public void handleEvent(Event event) {
        /*
         * check marker properties
         */
        if (false == event.containsProperty(CommonEvent.PUBLISH_MARKER) || event.containsProperty(CommonEvent.REMOTE_MARKER)) {
            return;
        }
        /*
         * check topic
         */
        String topic = event.getTopic();
        if (null == topic ||  false == topic.startsWith(TOPIC_PREFIX)) {
            return; // out of scope
        }
        /*
         * wrap event and publish remote
         */
        LOG.trace("Publishing remote: {}", event);
        messagingTopic.publish(toRemote(event));
    }

    @Override
    public void onMessage(Message<Map<String, Object>> message) {
        /*
         * check message & markers
         */
        if (null != message && message.isRemote()) {
            Map<String, Object> remoteEvent = message.getMessageObject();
            if (null != remoteEvent && false == remoteEvent.containsKey(CommonEvent.REMOTE_MARKER)) {
                /*
                 * publish locally
                 */
                Event event = toLocal(remoteEvent);
                LOG.trace("Publishing locally: {}", event);
                eventAdmin.postEvent(event);
            }
        }
    }

    /**
     * Serializes the supplied {@link Event} to a POJO properties map, ready to be distributed remotely. A
     * {@link CommonEvent#PUBLISH_MARKER} in the event properties is ignored implicitly during conversion.
     *
     * @param event The event
     * @return A map representing the serialized event
     */
    private static Map<String, Object> toRemote(Event event) {
        Map<String, Object> remoteEvent = new HashMap<String, Object>();
        remoteEvent.put("__topic", event.getTopic());
        for (String name : event.getPropertyNames()) {
            if (CommonEvent.PUBLISH_MARKER.equals(name)) {
                continue;
            }
            Object value = event.getProperty(name);
            if (isPojo(value)) {
                remoteEvent.put(name, value);
            } else if (Session.class.isInstance(value)) {
                Map<String, Serializable> wrappedSession = RemoteSession.wrap((Session)value);
                wrappedSession.put("__wrappedSessionName", name);
                remoteEvent.put("__wrappedSession", wrappedSession);
            }
        }
        return remoteEvent;
    }

    /**
     * Deserializes the supplied properties map to a (local) {@link Event}. The {@link CommonEvent#REMOTE_MARKER} is added implicitly to the
     * event properties to indicate that the event was remotely received.
     *
     * @param map The serialized map as received from the distributed topic message
     * @return The event
     */
    private static Event toLocal(Map<String, Object> map) {
        String topic = null;
        Map<String, Object> properties = new HashMap<String, Object>();
        for (Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            if ("__topic".equals(key)) {
                topic = (String) entry.getValue();
            } else if ("__wrappedSession".equals(key)) {
                @SuppressWarnings("unchecked")
                Map<String, Serializable> wrappedSession = (Map<String, Serializable>) entry.getValue();
                properties.put((String) wrappedSession.get("__wrappedSessionName"), RemoteSession.unwrap(wrappedSession));
            } else if (false == CommonEvent.PUBLISH_MARKER.equals(key)) {
                properties.put(key, entry.getValue());
            }
        }
        // Mark that event as remotely received
        properties.put(CommonEvent.REMOTE_MARKER, null);
        return new Event(topic, properties);
    }

    private static boolean isPojo(Object obj) {
        if (null == obj) {
            return false;
        }

        Class<? extends Object> clazz = obj.getClass();
        String className = clazz.getName();
        if (className.startsWith("[")) {
            // Array
            if (clazz.isPrimitive()) {
                return true;
            }

            // Array of objects
            Object[] objects = (Object[]) obj;
            boolean pojo = true;
            for (int i = 0; pojo && i < objects.length; i++) {
                pojo = isPojo(objects[i]);
            }
            return pojo;
        }

        if ((LinkedList.class.equals(clazz)) || (ArrayList.class.equals(clazz))) {
            List<?> list = (List<?>) obj;
            boolean pojo = true;
            for (int i = 0, len = list.size(); pojo && i < len; i++) {
                pojo = isPojo(list.get(i));
            }
            return pojo;
        }

        return className.startsWith(POJO_PACKAGE);
    }

}
