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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.ITopic;
import com.openexchange.ms.Message;
import com.openexchange.ms.MessageListener;

/**
 * {@link HzTopic}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HzTopic<E> extends AbstractHzTopic<E> {

    private final ITopic<Map<String, Object>> hzTopic;

    /**
     * Initializes a new {@link HzTopic}.
     *
     * @param name The topic's name
     * @param hz The hazelcast instance
     */
    public HzTopic(final String name, final HazelcastInstance hz) {
        super(name, hz);
        this.hzTopic = hz.getTopic(name);
    }

    @Override
    protected String registerListener(MessageListener<E> listener, String senderID) {
        try {
            return hzTopic.addMessageListener(new HzMessageListener<E>(listener, senderID));
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    protected boolean unregisterListener(String registrationID) {
        try {
            return hzTopic.removeMessageListener(registrationID);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    protected void publish(String senderId, E message) {
        try {
            hzTopic.publish(HzDataUtility.generateMapFor(message, senderId));
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    protected void publish(String senderId, List<E> messages) {
        // Create map carrying multiple messages
        final StringBuilder sb = new StringBuilder(HzDataUtility.MULTIPLE_PREFIX);
        final int reset = HzDataUtility.MULTIPLE_PREFIX.length();
        final Map<String, Object> multiple = new LinkedHashMap<String, Object>(messages.size() + 1);
        multiple.put(HzDataUtility.MULTIPLE_MARKER, Boolean.TRUE);
        for (int i = 0; i < messages.size(); i++) {
            sb.setLength(reset);
            multiple.put(sb.append(i+1).toString(), HzDataUtility.generateMapFor(messages.get(i), senderId));
        }
        // Publish
        try {
            hzTopic.publish(multiple);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
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
            if (messageData.containsKey(HzDataUtility.MULTIPLE_MARKER)) {
                final String name = message.getSource().toString();
                for (final Entry<String, Object> entry : messageData.entrySet()) {
                    if (entry.getKey().startsWith(HzDataUtility.MULTIPLE_PREFIX)) {
                        onMessageReceived(name, (Map<String, Object>) entry.getValue());
                    }
                }
            } else {
                onMessageReceived(message.getSource().toString(), messageData);
            }
        }

        private void onMessageReceived(final String name, final Map<String, Object> messageData) {
            final String messageSender = (String) messageData.get(HzDataUtility.MESSAGE_DATA_SENDER_ID);
            listener.onMessage(new Message<E>(name, messageSender, (E) messageData.get(HzDataUtility.MESSAGE_DATA_OBJECT), !senderId.equals(messageSender)));
        }
    }
}
