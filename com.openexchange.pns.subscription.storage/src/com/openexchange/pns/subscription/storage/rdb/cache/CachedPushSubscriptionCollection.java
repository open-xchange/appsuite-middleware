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

package com.openexchange.pns.subscription.storage.rdb.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.pns.KnownTopic;
import com.openexchange.pns.PushMatch;
import com.openexchange.pns.PushSubscription;
import com.openexchange.pns.subscription.storage.ClientAndTransport;
import com.openexchange.pns.subscription.storage.MapBackedHits;

/**
 * {@link CachedPushSubscriptionCollection}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class CachedPushSubscriptionCollection {

    private static final String ALL = KnownTopic.ALL.getName();

    private final int userId;
    private final int contextId;

    /** The subscriptions in this list match all events. */
    private final Set<PushSubscription> matchingAllSubscriptions;

    /**
     * This is a map for exact topic matches. The key is the topic,
     * the value is a list of subscriptions.
     */
    private final Map<String, Set<PushSubscription>> matchingTopic;

    /**
     * This is a map for wild-card topics. The key is the prefix of the topic,
     * the value is a list of subscriptions
     */
    private final Map<String, Set<PushSubscription>> matchingPrefixTopic;

    /**
     * Initializes a new {@link CachedPushSubscriptionCollection}.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public CachedPushSubscriptionCollection(int userId, int contextId) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        // Start with empty collections
        this.matchingAllSubscriptions = new LinkedHashSet<>(); // protected by synchronized
        this.matchingTopic = new HashMap<String, Set<PushSubscription>>(); // protected by synchronized
        this.matchingPrefixTopic = new HashMap<String, Set<PushSubscription>>(); // protected by synchronized
    }

    /**
     * Clears this collection.
     */
    public synchronized void clear() {
        matchingAllSubscriptions.clear();
        matchingTopic.clear();
        matchingPrefixTopic.clear();
    }

    /**
     * Checks for interested subscriptions for given topic.
     *
     * @param topic The topic
     * @return <code>true</code> if there is such a subscription; otherwise <code>false</code>
     */
    public synchronized boolean hasInterestedSubscriptions(String topic) {
        return hasInterestedSubscriptions(null, topic);
    }

    /**
     * Checks for interested subscriptions for given client and topic.
     *
     * @param client The client identifier
     * @param topic The topic
     * @return <code>true</code> if there is such a subscription; otherwise <code>false</code>
     */
    public synchronized boolean hasInterestedSubscriptions(String client, String topic) {
        // Check subscriptions matching everything
        boolean hasAny = checkMatches(matchingAllSubscriptions, client);
        if (hasAny) {
            return true;
        }

        // Now check for prefix matches
        if (!matchingPrefixTopic.isEmpty()) {
            int pos = topic.lastIndexOf(':');
            while (pos > 0) {
                String prefix = topic.substring(0, pos);
                Set<PushSubscription> subscriptions = matchingPrefixTopic.get(prefix);
                if (null != subscriptions) {
                    hasAny = checkMatches(subscriptions, client);
                    if (hasAny) {
                        return true;
                    }
                }
                pos = prefix.lastIndexOf(':');
            }
        }

        // Check the subscriptions for matching topic names
        {
            Set<PushSubscription> subscriptions = matchingTopic.get(topic);
            if (null != subscriptions) {
                hasAny = checkMatches(subscriptions, client);
                if (hasAny) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkMatches(Set<PushSubscription> subscriptions, String optMatchingClient) {
        if (null == subscriptions) {
            return false;
        }

        for (PushSubscription subscription : subscriptions) {
            String client = subscription.getClient();
            if (null == optMatchingClient || optMatchingClient.equals(client)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the interested subscriptions for given topic.
     *
     * @param topic The topic
     * @return The subscriptions or an empty collection
     */
    public synchronized MapBackedHits getInterestedSubscriptions(String topic) {
        return getInterestedSubscriptions(null, topic);
    }

    /**
     * Gets the interested subscriptions for given client and topic.
     *
     * @param client The client identifier
     * @param topic The topic
     * @return The subscriptions or an empty collection
     */
    public synchronized MapBackedHits getInterestedSubscriptions(String client, String topic) {
        Map<ClientAndTransport, List<PushMatch>> map = null;

        // Add subscriptions matching everything
        map = checkAndAddMatches(matchingAllSubscriptions, client, ALL, map);

        // Now check for prefix matches
        if (!matchingPrefixTopic.isEmpty()) {
            int pos = topic.lastIndexOf(':');
            while (pos > 0) {
                String prefix = topic.substring(0, pos);
                Set<PushSubscription> subscriptions = matchingPrefixTopic.get(prefix);
                if (null != subscriptions) {
                    map = checkAndAddMatches(subscriptions, client, prefix + ":*", map);
                }
                pos = prefix.lastIndexOf(':');
            }
        }

        // Add the subscriptions for matching topic names
        {
            Set<PushSubscription> subscriptions = matchingTopic.get(topic);
            if (null != subscriptions) {
                map = checkAndAddMatches(subscriptions, client, topic, map);
            }
        }

        return null == map ? MapBackedHits.EMPTY : new MapBackedHits(map);
    }

    private Map<ClientAndTransport, List<PushMatch>> checkAndAddMatches(Set<PushSubscription> subscriptions, String optMatchingClient, String matchingTopic, Map<ClientAndTransport, List<PushMatch>> map) {
        if (null == subscriptions) {
            return map;
        }

        Map<ClientAndTransport, List<PushMatch>> toFill = map;
        for (PushSubscription subscription : subscriptions) {
            String client = subscription.getClient();
            if (null == optMatchingClient || optMatchingClient.equals(client)) {
                String token = subscription.getToken();
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

    /**
     * Adds specified subscriptions.
     *
     * @param subscriptions The subscriptions to add
     */
    public synchronized void addSubscription(Collection<PushSubscription> subscriptions) {
        if (null == subscriptions) {
            return;
        }

        for (PushSubscription subscription : subscriptions) {
            doAddSubscription(subscription);
        }
    }

    /**
     * Adds specified subscription.
     *
     * @param subscription The subscription to add
     */
    public synchronized void addSubscription(PushSubscription subscription) {
        doAddSubscription(subscription);
    }

    private void doAddSubscription(PushSubscription subscription) {
        if (null == subscription) {
            return;
        }

        for (Iterator<String> iter = subscription.getTopics().iterator(); iter.hasNext();) {
            String topic = iter.next();
            if (ALL.equals(topic)) {
                matchingAllSubscriptions.add(subscription);
            } else {
                if (topic.endsWith(":*")) {
                    // Wild-card topic: we remove the /*
                    String prefix = topic.substring(0, topic.length() - 2);
                    Set<PushSubscription> list = matchingPrefixTopic.get(prefix);
                    if (null == list) {
                        Set<PushSubscription> newList = new LinkedHashSet<>();
                        matchingPrefixTopic.put(prefix, newList);
                        list = newList;
                    }
                    list.add(subscription);
                } else {
                    // Exact match
                    Set<PushSubscription> list = matchingTopic.get(topic);
                    if (null == list) {
                        Set<PushSubscription> newList = new LinkedHashSet<>();
                        matchingTopic.put(topic, newList);
                        list = newList;
                    }
                    list.add(subscription);
                }
            }
        }
    }

    /**
     * Removes the specified subscription
     *
     * @param subscription The subscription to remove
     * @return <code>true</code> if removed; otherwise <code>false</code> if no such subscription was contained
     */
    public synchronized boolean removeSubscription(PushSubscription subscription) {
        if (null == subscription) {
            return false;
        }

        boolean removed = matchingAllSubscriptions.remove(subscription);

        for (Iterator<Set<PushSubscription>> it = matchingPrefixTopic.values().iterator(); it.hasNext();) {
            Set<PushSubscription> wrappers = it.next();
            if (wrappers.remove(subscription)) {
                removed = true;
                if (wrappers.isEmpty()) {
                    it.remove();
                }
            }
        }

        for (Iterator<Set<PushSubscription>> it = matchingTopic.values().iterator(); it.hasNext();) {
            Set<PushSubscription> wrappers = it.next();
            if (wrappers.remove(subscription)) {
                removed = true;
                if (wrappers.isEmpty()) {
                    it.remove();
                }
            }
        }

        return removed;
    }

}
