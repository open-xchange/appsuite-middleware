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

package com.openexchange.caching.events.ms.internal;

import java.io.Serializable;
import java.util.List;
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
            MessageListener<PortableCacheEvent> listener = new CacheEventMessageListener(cacheEvents);
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
                LOG.warn("Error removing message listener " + i, e);
            }
        }
    }

    @Override
    public void onEvent(Object sender, CacheEvent cacheEvent, boolean fromRemote) {
        if (false == fromRemote) {
            try {
                int contextId = getContextId(cacheEvent);
                Topic<PortableCacheEvent> topic = getTopic(contextId);
                LOG.debug("Re-publishing locally received cache event to remote: {} [{}]", cacheEvent, topic.getSenderId());
                topic.publish(PortableCacheEvent.wrap(cacheEvent));
            } catch (RuntimeException e) {
                LOG.warn("Error publishing cache event", e);
            }
        }
    }

    private int getContextId(CacheEvent event) {
        int contextId = 0;
        if (null != event && null != event.getKeys()) {
            for (Serializable key : event.getKeys()) {
                if (CacheKey.class.isInstance(key)) {
                    CacheKey cacheKey = (CacheKey) key;
                    contextId = cacheKey.getContextId();
                    if (contextId > 0) {
                        break;
                    }
                }
            }
        }
        return contextId;
    }

    private Topic<PortableCacheEvent> getTopic(int contextId) {
        return messagingService.getTopic(TOPIC_PREFIX + (contextId % topicCount));
    }

    private static class CacheEventMessageListener implements MessageListener<PortableCacheEvent> {

        private final CacheEventService cacheEvents;

        CacheEventMessageListener(CacheEventService cacheEvents) {
            super();
            this.cacheEvents = cacheEvents;
        }

        @Override
        public void onMessage(Message<PortableCacheEvent> message) {
            if (null != message && message.isRemote()) {
                PortableCacheEvent cacheEvent = message.getMessageObject();
                if (null != cacheEvent) {
                    LOG.debug("Re-publishing remotely received cache event locally: {} [{}]", message.getMessageObject(), message.getSenderId());
                    cacheEvents.notify(this, PortableCacheEvent.unwrap(cacheEvent), true);
                } else {
                    LOG.warn("Discarding empty cache event message.");
                }
            }
        }

    }

}
