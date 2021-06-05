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

package com.openexchange.caching.events.ms.internal;

import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;
import com.google.common.collect.ImmutableList;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.CacheListener;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.ms.Message;
import com.openexchange.ms.MessageListener;
import com.openexchange.ms.PortableMsService;
import com.openexchange.ms.Topic;

/**
 * {@link MsCacheEventHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class MsCacheEventHandler implements CacheListener {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MsCacheEventHandler.class);

    private static final String TOPIC_PREFIX = "cacheEvents-3-";

    private final PortableMsService messagingService;
    private final CacheEventService cacheEvents;
    private final int topicCount;
    private final List<MessageListener<PortableCacheEvent>> listeners;

    /**
     * Initializes a new {@link MsCacheEventHandler}.
     *
     * @throws OXException
     */
    public MsCacheEventHandler(PortableMsService messagingService, CacheEventService cacheEvents, ConfigurationService configurationService) {
        super();
        this.messagingService = messagingService;
        this.cacheEvents = cacheEvents;
        int defaultTopicCount = 5;
        int topicCount = configurationService.getIntProperty("com.openexchange.caching.events.ms.topicCount", defaultTopicCount);
        if (topicCount <= 0) {
            topicCount = defaultTopicCount;
        }
        this.topicCount = topicCount;
        cacheEvents.addListener(this);

        ImmutableList.Builder<MessageListener<PortableCacheEvent>> listeners = ImmutableList.builderWithExpectedSize(topicCount);
        for (int i = 0; i < topicCount; i++) {
            Topic<PortableCacheEvent> topic = getTopic(i);
            MessageListener<PortableCacheEvent> listener = new CacheEventMessageListener(topic.getName(), cacheEvents);
            topic.addMessageListener(listener);
            listeners.add(listener);
        }
        this.listeners = listeners.build();
    }

    /**
     * Stops this instance.
     */
    public void stop() {
        cacheEvents.removeListener(this);
        for (int i = 0; i < topicCount; i++) {
            try {
                getTopic(i).removeMessageListener(listeners.get(i));
            } catch (RuntimeException e) {
                LOG.warn("Error removing message listener {}", Integer.valueOf(i), e);
            }
        }
    }

    @Override
    public void onEvent(Object sender, CacheEvent cacheEvent, boolean fromRemote) {
        if (false == fromRemote) {
            try {
                for (Entry<String, PortableCacheEvent> entry : PortableCacheEvent.wrap(cacheEvent, topicCount).entrySet()) {
                    String topic = entry.getKey();
                    PortableCacheEvent portableEvent = entry.getValue();
                    LOG.debug("Re-publishing locally received cache event to remote via [{}]: {}", topic, portableEvent);
                    messagingService.getTopic(topic).publish(portableEvent);
                }
            } catch (RuntimeException e) {
                LOG.warn("Error publishing cache event", e);
            }
        }
    }

    private Topic<PortableCacheEvent> getTopic(int contextId) {
        return messagingService.getTopic(getTopicName(contextId, topicCount));
    }

    /**
     * Gets the topic name to use based for a specific cache key.
     *
     * @param key The cache key to get the topic name for
     * @param topicCount The number of topics to use for distribution
     * @return The topic name
     */
    static String getTopicName(Serializable key, int topicCount) {
        return getTopicName(null != key && CacheKey.class.isInstance(key) ? ((CacheKey) key).getContextId() % topicCount : 0, topicCount);
    }

    private static String getTopicName(int contextId, int topicCount) {
        return TOPIC_PREFIX + (contextId % topicCount);
    }

    private static class CacheEventMessageListener implements MessageListener<PortableCacheEvent> {

        private final CacheEventService cacheEvents;
        private final String topic;

        CacheEventMessageListener(String topic, CacheEventService cacheEvents) {
            super();
            this.topic = topic;
            this.cacheEvents = cacheEvents;
        }

        @Override
        public void onMessage(Message<PortableCacheEvent> message) {
            if (null != message && message.isRemote()) {
                PortableCacheEvent cacheEvent = message.getMessageObject();
                if (null != cacheEvent) {
                    LOG.debug("Re-publishing remotely received cache event via [{}] locally: {}", topic, cacheEvent);
                    cacheEvents.notify(this, PortableCacheEvent.unwrap(cacheEvent), true);
                } else {
                    LOG.warn("Discarding empty cache event message.");
                }
            }
        }

    }

}
