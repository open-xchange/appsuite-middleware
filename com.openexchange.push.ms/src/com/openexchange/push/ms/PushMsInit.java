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

package com.openexchange.push.ms;

import java.util.Map;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.ms.MessageListener;
import com.openexchange.ms.MsExceptionCodes;
import com.openexchange.ms.MsService;
import com.openexchange.ms.Topic;

/**
 * {@link PushMsInit} - Initializes the messaging-based push bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PushMsInit {

    private volatile Topic<Map<String, Object>> publishTopic;

    private volatile MessageListener<Map<String, Object>> subscriber;

    private volatile DelayPushQueue delayPushQueue;

    /**
     * Initializes a new {@link PushMsInit}.
     */
    public PushMsInit() {
        super();
    }

    /**
     * Gets the topic to publish messages to.
     *
     * @return The topic
     */
    public Topic<Map<String, Object>> getPublishTopic() {
        return publishTopic;
    }

    /**
     * Gets the message listener receiving incoming messages.
     *
     * @return The message listener
     */
    public MessageListener<Map<String, Object>> getSubscriber() {
        return subscriber;
    }

    /**
     * Get the delaying push queue.
     *
     * @return the delaying push queue.
     */
    public DelayPushQueue getDelayPushQueue() {
        return delayPushQueue;
    }

    /**
     * Initializes the messaging-based push bundle.
     *
     * @throws OXException If initialization fails
     */
    public void init() throws OXException {
        Topic<Map<String, Object>> publishTopic = this.publishTopic;
        DelayPushQueue delayPushQueue = this.delayPushQueue;
        if (null == publishTopic || delayPushQueue == null) {
            synchronized (this) {
                publishTopic = this.publishTopic;
                if (null == publishTopic) {
                    try {
                        final MsService msService = Services.getService(MsService.class);
                        if (null == msService) {
                            throw MsExceptionCodes.ILLEGAL_STATE.create("Missing service: " + MsService.class.getName());
                        }
                        publishTopic = msService.getTopic("oxEventTopic");
                        final PushMsListener listener = new PushMsListener();
                        publishTopic.addMessageListener(listener);
                        this.publishTopic = publishTopic;
                        subscriber = listener;
                    } catch (RuntimeException e) {
                        throw MsExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                    }
                }

                delayPushQueue = this.delayPushQueue;
                if (delayPushQueue == null) {
                    ConfigurationService configService = Services.getService(ConfigurationService.class);
                    if (null == configService) {
                        throw MsExceptionCodes.ILLEGAL_STATE.create("Missing service: " + ConfigurationService.class.getName());
                    }
                    int delayDuration = configService.getIntProperty("com.openexchange.push.ms.delayDuration", 120000);
                    int maxDelays = configService.getIntProperty("com.openexchange.push.ms.maxDelayDuration", 600000);
                    delayPushQueue = new DelayPushQueue(publishTopic, delayDuration, maxDelays).start();
                    this.delayPushQueue = delayPushQueue;
                }
            }
        }
    }

    /**
     * Shuts-down the messaging-based push bundle.
     */
    public void close() {
        Topic<Map<String, Object>> publisher = this.publishTopic;
        if (null != publisher) {
            synchronized (this) {
                publisher = this.publishTopic;
                if (null != publisher) {
                    final MessageListener<Map<String, Object>> listener = subscriber;
                    if (null != listener) {
                        publisher.removeMessageListener(subscriber);
                        subscriber = null;
                    }
                    this.publishTopic = null;
                }
            }
        }
        DelayPushQueue delayPushQueue = this.delayPushQueue;
        if (delayPushQueue != null) {
            synchronized (this) {
                delayPushQueue = this.delayPushQueue;
                if (delayPushQueue != null) {
                    delayPushQueue.close();
                    this.delayPushQueue=null;
                }
            }
        }
    }

}
