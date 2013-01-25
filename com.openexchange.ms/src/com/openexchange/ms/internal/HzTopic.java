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

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.openexchange.java.util.UUIDs;
import com.openexchange.ms.Message;
import com.openexchange.ms.MessageListener;
import com.openexchange.ms.Topic;

/**
 * {@link HzTopic}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HzTopic<E> implements Topic<E> {

    private final ITopic<MessageData<E>> hzTopic;
    private final String senderId;
    private final String name;
    private final ConcurrentMap<MessageListener<E>, com.hazelcast.core.MessageListener<MessageData<E>>> registeredListeners;

    /**
     * Initializes a new {@link HzTopic}.
     */
    public HzTopic(final String name, final HazelcastInstance hz) {
        super();
        this.name = name;
        senderId = UUIDs.getUnformattedString(UUID.randomUUID());
        this.hzTopic = hz.getTopic(name);
        registeredListeners = new ConcurrentHashMap<MessageListener<E>, com.hazelcast.core.MessageListener<MessageData<E>>>(8);
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
        final HzMessageListener hzListener = new HzMessageListener(listener);
        hzTopic.addMessageListener(hzListener);
        registeredListeners.put(listener, hzListener);
    }

    @Override
    public void removeMessageListener(final MessageListener<E> listener) {
        final com.hazelcast.core.MessageListener<MessageData<E>> hzListener = registeredListeners.remove(listener);
        if (null != hzListener) {
            hzTopic.removeMessageListener(hzListener);
        }
    }

    @Override
    public void destroy() {
        hzTopic.destroy();
    }

    @Override
    public void publish(final E message) {
        hzTopic.publish(new MessageData<E>(message, senderId));
    }

    // ------------------------------------------------------------------------ //

    private final class HzMessageListener implements com.hazelcast.core.MessageListener<MessageData<E>> {

        private final MessageListener<E> listener;

        /**
         * Initializes a new {@link HzMessageListener}.
         */
        protected HzMessageListener(final MessageListener<E> listener) {
            super();
            this.listener = listener;
        }

        @Override
        public void onMessage(final com.hazelcast.core.Message<MessageData<E>> message) {
            final MessageData<E> messageData = message.getMessageObject();
            listener.onMessage(new Message<E>(getName(), messageData.getSenderId(), messageData.getObject()));
        }
    }
}
