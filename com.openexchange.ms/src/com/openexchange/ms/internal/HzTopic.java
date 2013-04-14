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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.logging.Log;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.openexchange.java.util.UUIDs;
import com.openexchange.ms.Message;
import com.openexchange.ms.MessageListener;
import com.openexchange.ms.Topic;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link HzTopic}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HzTopic<E> implements Topic<E> {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(HzTopic.class);

    static final String MESSAGE_DATA_OBJECT = HzDataUtility.MESSAGE_DATA_OBJECT;
    static final String MESSAGE_DATA_SENDER_ID = HzDataUtility.MESSAGE_DATA_SENDER_ID;
    static final String MULTIPLE_PREFIX = HzDataUtility.MULTIPLE_PREFIX;
    static final String MULTIPLE_MARKER = HzDataUtility.MULTIPLE_MARKER;

    private final ITopic<Map<String, Object>> hzTopic;
    private final String senderId;
    private final String name;
    private final ConcurrentMap<MessageListener<E>, com.hazelcast.core.MessageListener<Map<String, Object>>> registeredListeners;
    private final HzDelayQueue<HzDelayed<E>> publishQueue;
    private final ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link HzTopic}.
     */
    public HzTopic(final String name, final HazelcastInstance hz) {
        super();
        this.name = name;
        senderId = UUIDs.getUnformattedString(UUID.randomUUID());
        this.hzTopic = hz.getTopic(name);
        registeredListeners = new ConcurrentHashMap<MessageListener<E>, com.hazelcast.core.MessageListener<Map<String, Object>>>(8);
        publishQueue = new HzDelayQueue<HzDelayed<E>>();
        // Timer task
        final TimerService timerService = Services.getService(TimerService.class);
        final Log log = LOG;
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
    public void addMessageListener(final MessageListener<E> listener) {
        final HzMessageListener<E> hzListener = new HzMessageListener<E>(listener, senderId);
        hzTopic.addMessageListener(hzListener);
        registeredListeners.put(listener, hzListener);
    }

    @Override
    public void removeMessageListener(final MessageListener<E> listener) {
        final com.hazelcast.core.MessageListener<Map<String, Object>> hzListener = registeredListeners.remove(listener);
        if (null != hzListener) {
            try {
                hzTopic.removeMessageListener(hzListener);
            } catch (final RuntimeException e) {
                // Removing message listener failed
                if (LOG.isDebugEnabled()) {
                    LOG.warn("Couldn't remove message listener from Hazelcast topic \"" + name + "\".", e);
                } else {
                    LOG.warn("Couldn't remove message listener from Hazelcast topic \"" + name + "\".");
                }
            }
        }
    }

    @Override
    public void destroy() {
        timerTask.cancel();
        hzTopic.destroy();
    }

    @Override
    public void publish(final E message) {
        publishQueue.offerIfAbsent(new HzDelayed<E>(message, false));
        triggerPublish();
    }

    /**
     * Triggers all due messages.
     */
    public void triggerPublish() {
        HzDelayed<E> polled = publishQueue.poll();
        if (null != polled) {
            final List<E> messages = new LinkedList<E>();
            do {
                messages.add(polled.getData());
                polled = publishQueue.poll();
            } while (polled != null);
            publishNow(messages);
        }
    }

    private static final int CHUNK_SIZE = 10;

    /**
     * (Immediately) Publishes specified message to queue.
     *
     * @param message The message to publish
     */
    private void publishNow(final List<E> messages) {
        final int size = messages.size();
        if (0 == size) {
            return;
        }
        if (1 == size) {
            hzTopic.publish(HzDataUtility.generateMapFor(messages.get(0), senderId));
        }
        // Chunk-wise
        final StringBuilder sb = new StringBuilder(MULTIPLE_PREFIX);
        final int reset = MULTIPLE_PREFIX.length();
        final int chunkSize = CHUNK_SIZE;
        int off = 0;
        while (off < size) {
            // Determine end index
            int end = off + chunkSize;
            if (end > size) {
                end = size;
            }
            // Create map carrying multiple messages
            final Map<String, Object> multiple = new LinkedHashMap<String, Object>(chunkSize + 1);
            multiple.put(MULTIPLE_MARKER, Boolean.TRUE);
            for (int i = off; i < end; i++) {
                sb.setLength(reset);
                multiple.put(sb.append(i+1).toString(), HzDataUtility.generateMapFor(messages.get(i), senderId));
            }
            // Publish
            hzTopic.publish(multiple);
            off = end;
        }
    }

    // ------------------------------------------------------------------------ //

    private static final class HzMessageListener<E> implements com.hazelcast.core.MessageListener<Map<String, Object>> {

        private final MessageListener<E> listener;
        private final String senderId;

        /**
         * Initializes a new {@link HzMessageListener}.
         */
        protected HzMessageListener(final MessageListener<E> listener, final String senderId) {
            super();
            this.listener = listener;
            this.senderId = senderId;
        }

        @Override
        public void onMessage(final com.hazelcast.core.Message<Map<String, Object>> message) {
            final Map<String, Object> messageData = message.getMessageObject();
            if (messageData.containsKey(MULTIPLE_MARKER)) {
                final String name = message.getSource().toString();
                for (final Entry<String, Object> entry : messageData.entrySet()) {
                    if (entry.getKey().startsWith(MULTIPLE_PREFIX)) {
                        onMessageReceived(name, (Map<String, Object>) entry.getValue());
                    }
                }
            } else {
                onMessageReceived(message.getSource().toString(), messageData);
            }
        }

        private void onMessageReceived(final String name, final Map<String, Object> messageData) {
            final String messageSender = (String) messageData.get(MESSAGE_DATA_SENDER_ID);
            listener.onMessage(new Message<E>(
                name,
                messageSender,
                (E) messageData.get(MESSAGE_DATA_OBJECT),
                !senderId.equals(messageSender)));
        }
    }
}
