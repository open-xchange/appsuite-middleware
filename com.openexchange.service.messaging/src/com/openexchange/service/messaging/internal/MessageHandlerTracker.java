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

package com.openexchange.service.messaging.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.service.messaging.MessageHandler;

/**
 * {@link MessageHandlerTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessageHandlerTracker extends ServiceTracker {

    /**
     * List of all handlers with topic of "*".
     */
    private final List<MessageHandlerWrapper> globalWildcard;

    /**
     * Key is topic prefix of partial wildcard.
     */
    private final Map<String, List<MessageHandlerWrapper>> partialWildcard;

    /**
     * Key is topic name.
     */
    private final Map<String, List<MessageHandlerWrapper>> topicName;

    public MessageHandlerTracker(final BundleContext context) {
        super(context, MessageHandler.class.getName(), null);
        globalWildcard = new ArrayList<MessageHandlerWrapper>();
        partialWildcard = new HashMap<String, List<MessageHandlerWrapper>>();
        topicName = new HashMap<String, List<MessageHandlerWrapper>>();
    }

    @Override
    public Object addingService(final ServiceReference reference) {
        final MessageHandlerWrapper wrapper = new MessageHandlerWrapper(reference, context);
        synchronized (this) {
            if (wrapper.init()) {
                bucket(wrapper);
            }
        }
        return wrapper;
    }

    @Override
    public void modifiedService(final ServiceReference reference, final Object service) {
        final MessageHandlerWrapper wrapper = (MessageHandlerWrapper) service;
        synchronized (this) {
            unbucket(wrapper);
            if (wrapper.init()) {
                bucket(wrapper);
                return;
            }
        }
        /*
         * Needs to be called outside of sync region
         */
        wrapper.flush();
    }

    @Override
    public void removedService(final ServiceReference reference, final Object service) {
        final MessageHandlerWrapper wrapper = (MessageHandlerWrapper) service;
        synchronized (this) {
            unbucket(wrapper);
        }
        /*
         * Needs to be called outside sync region
         */
        wrapper.flush();
    }

    /**
     * Place the wrapper into the appropriate buckets. This is a performance optimization for message delivery.
     *
     * @param wrapper The wrapper to place in buckets.
     */
    private void bucket(final MessageHandlerWrapper wrapper) {
        final String[] topics = wrapper.getTopics();
        if (null == topics) {
            return;
        }
        final int length = topics.length;
        for (int i = 0; i < length; i++) {
            final String topic = topics[i];
            if ("*".equals(topic)) {
                /*
                 * Global wildcard
                 */
                globalWildcard.add(wrapper);
            } else if (topic.endsWith("/*")) {
                /*-
                 * Partial wildcard
                 * Strip off "/*" from the end
                 */
                final String key = topic.substring(0, topic.length() - 2);
                List<MessageHandlerWrapper> wrappers = partialWildcard.get(key);
                if (wrappers == null) {
                    wrappers = new ArrayList<MessageHandlerWrapper>();
                    partialWildcard.put(key, wrappers);
                }
                wrappers.add(wrapper);
            } else {
                /*
                 * Simple topic name
                 */
                List<MessageHandlerWrapper> wrappers = topicName.get(topic);
                if (wrappers == null) {
                    wrappers = new ArrayList<MessageHandlerWrapper>();
                    topicName.put(topic, wrappers);
                }
                wrappers.add(wrapper);
            }
        }
    }

    /**
     * Remove the wrapper from the buckets.
     *
     * @param wrapper The wrapper to remove from the buckets.
     */
    private void unbucket(final MessageHandlerWrapper wrapper) {
        final String[] topics = wrapper.getTopics();
        if (null == topics) {
            return;
        }
        final int length = topics.length;
        for (int i = 0; i < length; i++) {
            final String topic = topics[i];
            /*
             * Global wildcard
             */
            if ("*".equals(topic)) {
                globalWildcard.remove(wrapper);
            } else if (topic.endsWith("/*")) {
                /*-
                 * Partial wildcard
                 * Strip off "/*" from the end
                 */
                final String key = topic.substring(0, topic.length() - 2);
                final List<MessageHandlerWrapper> wrappers = partialWildcard.get(key);
                if (wrappers != null) {
                    wrappers.remove(wrapper);
                    if (wrappers.isEmpty()) {
                        partialWildcard.remove(key);
                    }
                }
            } else {
                /*
                 * Simple topic name
                 */
                final List<MessageHandlerWrapper> wrappers = topicName.get(topic);
                if (wrappers != null) {
                    wrappers.remove(wrapper);
                    if (wrappers.isEmpty()) {
                        topicName.remove(topic);
                    }
                }
            }
        }
    }

    /**
     * Return the set of handlers which subscribe to the event topic. A set is used to ensure a handler is not called for an event more than
     * once.
     *
     * @param topic The topic
     * @return A set of handlers
     */
    public synchronized Set<MessageHandlerWrapper> getHandlers(final String topic) {
        /*
         * Use a set to remove duplicates
         */
        final Set<MessageHandlerWrapper> handlers = new HashSet<MessageHandlerWrapper>();
        /*
         * Add the "*" handlers
         */
        handlers.addAll(globalWildcard);
        /*
         * Add the handlers with partial matches
         */
        if (!partialWildcard.isEmpty()) {
            int index = topic.length();
            while (index >= 0) {
                /*
                 * First subtopic is the complete topic.
                 */
                final String subTopic = topic.substring(0, index);
                final List<MessageHandlerWrapper> wrappers = partialWildcard.get(subTopic);
                if (wrappers != null) {
                    handlers.addAll(wrappers);
                }
                /*-
                 * Strip the last level from the topic. For example, org/osgi/framework becomes org/osgi.
                 * Wildcard topics are inserted into the map with the "/*" stripped off.
                 */
                index = subTopic.lastIndexOf('/');
            }
        }
        /*
         * Add the handlers for matching topic names
         */
        final List<MessageHandlerWrapper> wrappers = topicName.get(topic);
        if (wrappers != null) {
            handlers.addAll(wrappers);
        }
        return handlers;
    }

}
