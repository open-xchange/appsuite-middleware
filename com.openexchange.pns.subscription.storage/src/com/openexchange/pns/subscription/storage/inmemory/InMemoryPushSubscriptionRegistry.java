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

package com.openexchange.pns.subscription.storage.inmemory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.pns.DefaultPushSubscription;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.PushMatch;
import com.openexchange.pns.PushNotifications;
import com.openexchange.pns.PushSubscription;
import com.openexchange.pns.PushSubscriptionRegistry;
import com.openexchange.pns.subscription.storage.ClientAndTransport;
import com.openexchange.pns.subscription.storage.MapBackedHits;

/**
 * {@link InMemoryPushSubscriptionRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class InMemoryPushSubscriptionRegistry implements PushSubscriptionRegistry {

    /** The subscriptions in this list match all events. */
    private final Set<PushSubscriptionWrapper> matchingAllSubscriptions;

    /**
     * This is a map for exact topic matches. The key is the topic,
     * the value is a list of subscriptions.
     */
    private final Map<String, Set<PushSubscriptionWrapper>> matchingTopic;

    /**
     * This is a map for wild-card topics. The key is the prefix of the topic,
     * the value is a list of subscriptions
     */
    private final Map<String, Set<PushSubscriptionWrapper>> matchingPrefixTopic;

    public InMemoryPushSubscriptionRegistry() {
        super();
        // Start with empty collections
        this.matchingAllSubscriptions = new LinkedHashSet<>(); // protected by synchronized
        this.matchingTopic = new HashMap<String, Set<PushSubscriptionWrapper>>(); // protected by synchronized
        this.matchingPrefixTopic = new HashMap<String, Set<PushSubscriptionWrapper>>(); // protected by synchronized
    }

    @Override
    public synchronized MapBackedHits getInterestedSubscriptions(int userId, int contextId, String topic) throws OXException {
        Map<ClientAndTransport, List<PushMatch>> map = null;

        // Add subscriptions matching everything
        map = checkAndAddMatches(userId, contextId, matchingAllSubscriptions, "*", map);

        // Now check for prefix matches
        if (!matchingPrefixTopic.isEmpty()) {
            int pos = topic.lastIndexOf('/');
            while (pos > 0) {
                String prefix = topic.substring(0, pos);
                Set<PushSubscriptionWrapper> wrappers = matchingPrefixTopic.get(prefix);
                if (null != wrappers) {
                    map = checkAndAddMatches(userId, contextId, wrappers, prefix + "/*", map);
                }
                pos = prefix.lastIndexOf('/');
            }
        }

        // Add the subscriptions for matching topic names
        {
            Set<PushSubscriptionWrapper> wrappers = matchingTopic.get(topic);
            if (null != wrappers) {
                map = checkAndAddMatches(userId, contextId, wrappers, topic, map);
            }
        }

        return null == map ? MapBackedHits.EMPTY : new MapBackedHits(map);
    }

    private Map<ClientAndTransport, List<PushMatch>> checkAndAddMatches(int userId, int contextId, Set<PushSubscriptionWrapper> wrappers, String matchingTopic, Map<ClientAndTransport, List<PushMatch>> map) {
        if (null == wrappers) {
            return map;
        }

        Map<ClientAndTransport, List<PushMatch>> toFill = map;
        for (PushSubscriptionWrapper wrapper : matchingAllSubscriptions) {
            if (wrapper.belongsTo(userId, contextId)) {
                PushSubscription subscription = wrapper.getSubscription();
                String token = subscription.getToken();
                String client = subscription.getClient();
                String transportId = subscription.getTransportId();

                // Add to appropriate list
                if (null == toFill) {
                    toFill = new LinkedHashMap<>(6);
                }
                ClientAndTransport cat = new ClientAndTransport(client, transportId);
                List<PushMatch> matches = toFill.get(cat);
                if (null == matches) {
                    matches = new LinkedList<PushMatch>();
                    toFill.put(cat, matches);
                }
                matches.add(new InMemoryPushMatch(userId, contextId, client, transportId, token, matchingTopic));
            }
        }

        return toFill;
    }

    @Override
    public synchronized void registerSubscription(PushSubscription subscription) throws OXException {
        if (null == subscription) {
            return;
        }

        for (Iterator<String> iter = subscription.getTopics().iterator(); iter.hasNext();) {
            String topic = iter.next();
            if ("*".equals(topic)) {
                matchingAllSubscriptions.add(new PushSubscriptionWrapper(subscription));
            } else {
                try {
                    PushNotifications.validateTopicName(topic);
                } catch (IllegalArgumentException e) {
                    throw PushExceptionCodes.INVALID_TOPIC.create(e, topic);
                }
                if (topic.endsWith(":*")) {
                    // Wild-card topic: we remove the /*
                    String prefix = topic.substring(0, topic.length() - 2);
                    Set<PushSubscriptionWrapper> list = matchingPrefixTopic.get(prefix);
                    if (null == list) {
                        Set<PushSubscriptionWrapper> newList = new LinkedHashSet<>();
                        matchingPrefixTopic.put(prefix, newList);
                        list = newList;
                    }
                    list.add(new PushSubscriptionWrapper(subscription));
                } else {
                    // Exact match
                    Set<PushSubscriptionWrapper> list = matchingTopic.get(topic);
                    if (null == list) {
                        Set<PushSubscriptionWrapper> newList = new LinkedHashSet<>();
                        matchingTopic.put(topic, newList);
                        list = newList;
                    }
                    list.add(new PushSubscriptionWrapper(subscription));
                }
            }
        }
    }

    @Override
    public synchronized boolean unregisterSubscription(PushSubscription subscription) throws OXException {
        if (null == subscription) {
            return false;
        }

        PushSubscriptionWrapper toRemove = new PushSubscriptionWrapper(subscription);
        boolean removed = matchingAllSubscriptions.remove(toRemove);

        for (Iterator<Set<PushSubscriptionWrapper>> it = matchingPrefixTopic.values().iterator(); it.hasNext();) {
            Set<PushSubscriptionWrapper> wrappers = it.next();
            if (wrappers.remove(toRemove)) {
                removed = true;
                if (wrappers.isEmpty()) {
                    it.remove();
                }
            }
        }

        for (Iterator<Set<PushSubscriptionWrapper>> it = matchingTopic.values().iterator(); it.hasNext();) {
            Set<PushSubscriptionWrapper> wrappers = it.next();
            if (wrappers.remove(toRemove)) {
                removed = true;
                if (wrappers.isEmpty()) {
                    it.remove();
                }
            }
        }

        return removed;
    }

    @Override
    public synchronized int unregisterSubscription(String token, String transportId) throws OXException {
        if (null == token || null == transportId) {
            return 0;
        }

        int numRemoved = 0;

        for (Iterator<PushSubscriptionWrapper> it = matchingAllSubscriptions.iterator(); it.hasNext();) {
            PushSubscriptionWrapper wrapper = it.next();
            if (wrapper.matches(token, transportId)) {
                it.remove();
                numRemoved++;
            }
        }

        for (Iterator<Set<PushSubscriptionWrapper>> iter = matchingPrefixTopic.values().iterator(); iter.hasNext();) {
            Set<PushSubscriptionWrapper> wrappers = iter.next();
            for (Iterator<PushSubscriptionWrapper> it = wrappers.iterator(); it.hasNext();) {
                PushSubscriptionWrapper wrapper = it.next();
                if (wrapper.matches(token, transportId)) {
                    it.remove();
                    numRemoved++;
                }
            }
            if (wrappers.isEmpty()) {
                iter.remove();
            }
        }

        for (Iterator<Set<PushSubscriptionWrapper>> iter = matchingTopic.values().iterator(); iter.hasNext();) {
            Set<PushSubscriptionWrapper> wrappers = iter.next();
            for (Iterator<PushSubscriptionWrapper> it = wrappers.iterator(); it.hasNext();) {
                PushSubscriptionWrapper wrapper = it.next();
                if (wrapper.matches(token, transportId)) {
                    it.remove();
                    numRemoved++;
                }
            }
            if (wrappers.isEmpty()) {
                iter.remove();
            }
        }

        return numRemoved;
    }

    @Override
    public synchronized boolean updateToken(PushSubscription subscription, String newToken) throws OXException {
        if (null == subscription || null == newToken) {
            return false;
        }

        PushSubscriptionWrapper toLookUp = new PushSubscriptionWrapper(subscription);

        boolean updated = updateTokenUsing(toLookUp, newToken, matchingAllSubscriptions);

        for (Set<PushSubscriptionWrapper> wrappers : matchingPrefixTopic.values()) {
            updated |= updateTokenUsing(toLookUp, newToken, wrappers);
        }

        for (Set<PushSubscriptionWrapper> wrappers : matchingTopic.values()) {
            updated |= updateTokenUsing(toLookUp, newToken, wrappers);
        }

        return updated;
    }

    private boolean updateTokenUsing(PushSubscriptionWrapper toLookUp, String newToken, Set<PushSubscriptionWrapper> wrappers) {
        List<PushSubscriptionWrapper> toAdd = null;

        for (Iterator<PushSubscriptionWrapper> iter = wrappers.iterator(); iter.hasNext();) {
            PushSubscriptionWrapper wrapper = iter.next();
            if (wrapper.equals(toLookUp)) {
                iter.remove();

                PushSubscription source = wrapper.getSubscription();
                DefaultPushSubscription.Builder builder = DefaultPushSubscription.builder()
                                                            .client(source.getClient())
                                                            .contextId(source.getContextId())
                                                            .nature(source.getNature())
                                                            .token(newToken)
                                                            .topics(source.getTopics())
                                                            .transportId(source.getTransportId())
                                                            .userId(source.getUserId());

                if (null == toAdd) {
                    toAdd = new LinkedList<PushSubscriptionWrapper>();
                }
                toAdd.add(new PushSubscriptionWrapper(builder.build()));
            }
        }

        if (null != toAdd) {
            wrappers.addAll(toAdd);
            return true;
        }
        return false;
    }

}
