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
        if (null == publishTopic) {
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
                    } catch (final RuntimeException e) {
                        throw MsExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                    }
                }
            }
        }
        DelayPushQueue delayPushQueue = this.delayPushQueue;
        if (delayPushQueue == null) {
            synchronized (this) {
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
                if(delayPushQueue != null) {
                    delayPushQueue.close();
                    this.delayPushQueue=null;
                }
            }
        }
    }

}
