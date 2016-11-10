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

package com.openexchange.ms.internal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.java.BufferingQueue;
import com.openexchange.java.util.UUIDs;
import com.openexchange.ms.MessageListener;
import com.openexchange.ms.Topic;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link AbstractHzTopic}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class AbstractHzTopic<E> extends AbstractHzResource implements Topic<E> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractHzTopic.class);

    private final String senderId;
    private final String name;
    private final ConcurrentMap<MessageListener<E>, String> registeredListeners;
    private final BufferingQueue<E> publishQueue;
    private final ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link AbstractHzTopic}.
     *
     * @param name The topic's name
     * @param hz The hazelcast instance
     */
    public AbstractHzTopic(String name, HazelcastInstance hz) {
        super();
        this.name = name;
        senderId = UUIDs.getUnformattedString(UUID.randomUUID());
        registeredListeners = new ConcurrentHashMap<MessageListener<E>, String>(8, 0.9f, 1);
        publishQueue = new BufferingQueue<E>(HzDataUtility.DELAY_MSEC);
        // Timer task
        final TimerService timerService = Services.getService(TimerService.class);
        final org.slf4j.Logger log = LOG;
        final Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    triggerPublish();
                } catch (final Exception e) {
                    log.warn("Failed to trigger publishing messages.", e);
                }
            }
        };
        final int delay = HzDataUtility.DELAY_FREQUENCY;
        timerTask = timerService.scheduleWithFixedDelay(r, delay, delay);
    }

    /**
     * Cancels the timer.
     */
    @Override
    public void cancel() {
        timerTask.cancel();
    }

    @Override
    public String getSenderId() {
        return senderId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void addMessageListener(MessageListener<E> listener) {
        String registrationID = registerListener(listener, senderId);
        if (null != registrationID) {
            registeredListeners.put(listener, registrationID);
        }
    }

    @Override
    public void removeMessageListener(MessageListener<E> listener) {
        String registrationID = registeredListeners.remove(listener);
        if (null != registrationID) {
            try {
                unregisterListener(registrationID);
            } catch (RuntimeException e) {
                // Removing message listener failed
                LOG.warn("Couldn't remove message listener from Hazelcast topic \"{}\".", name, e);
            }
        }
    }

    @Override
    public void destroy() {
        timerTask.cancel();
    }

    @Override
    public void publish(final E message) {
        publishQueue.offerIfAbsent(message);
        triggerPublish();
    }

    /**
     * Triggers all due messages.
     */
    public void triggerPublish() {
        List<E> messages = new LinkedList<E>();
        if (0 < publishQueue.drainTo(messages)) {
            publishNow(messages);
        }
    }

    /**
     * (Immediately) Publishes specified messages to queue.
     *
     * @param messages The messages to publish
     */
    private void publishNow(final List<E> messages) {
        final int size = messages.size();
        if (0 == size) {
            return;
        }
        if (size <= HzDataUtility.CHUNK_THRESHOLD) {
            if (1 == size) {
                publish(senderId, messages.get(0));
            } else {
                for (E message : messages) {
                    publish(senderId, message);
                }
            }
        } else {
            // Chunk-wise
            final int chunkSize = HzDataUtility.CHUNK_SIZE;
            int off = 0;
            while (off < size) {
                // Determine end index
                int end = off + chunkSize;
                if (end > size) {
                    end = size;
                }
                // Create map carrying multiple messages
                List<E> messagePayload = new ArrayList<E>(chunkSize);
                for (int i = off; i < end; i++) {
                    messagePayload.add(messages.get(i));
                }
                // Publish
                publish(senderId, messagePayload);
                off = end;
            }
        }
    }

    /**
     * Adds a message listener registration for the underlying topic.
     *
     * @param listener The listener to register
     * @param senderID The listener's sender ID
     * @return The listener registration ID, or <code>null</code> if not registerd
     */
    protected abstract String registerListener(MessageListener<E> listener, String senderID);

    /**
     * Removes a previously registered message listener for the underlying topic.
     *
     * @param registrationID The listener's registration ID
     * @return <code>true</code> if the listener was unregistered successfully, <code>false</code>, otherwise
     */
    protected abstract boolean unregisterListener(String registrationID);

    /**
     * Publishes a message to the underlying topic-
     *
     * @param senderID The sender ID to use for the message
     * @param message The message
     */
    protected abstract void publish(String senderID, E message);

    /**
     * Publishes multiple messages to the underlying topic-
     *
     * @param senderID The sender ID to use for the message
     * @param messages The messages
     */
    protected abstract void publish(String senderID, List<E> messages);

}
