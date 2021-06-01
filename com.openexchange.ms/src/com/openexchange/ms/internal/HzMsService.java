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

package com.openexchange.ms.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.openexchange.ms.Member;
import com.openexchange.ms.Message;
import com.openexchange.ms.MessageInbox;
import com.openexchange.ms.MsService;
import com.openexchange.ms.Queue;
import com.openexchange.ms.Topic;

/**
 * {@link HzMsService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HzMsService extends AbstractHzResource implements MsService {

    private final HazelcastInstance hz;
    private final ConcurrentMap<String, Queue<?>> queues;
    private final ConcurrentMap<String, Topic<?>> topics;
    private final MessageInboxImpl messageInbox;

    /**
     * Initializes a new {@link HzMsService}.
     */
    public HzMsService(final HazelcastInstance hz) {
        super();
        messageInbox = MessageInboxImpl.getInstance();
        this.hz = hz;
        queues = new NonBlockingHashMap<String, Queue<?>>(8);
        topics = new NonBlockingHashMap<String, Topic<?>>(16);
    }

    /**
     * Shuts-down this Hazelcast-backed messaging service.
     */
    public void shutDown() {
        for (Queue<?> queue : queues.values()) {
            queue.destroy();
        }
        queues.clear();
        for (Topic<?> topic : topics.values()) {
            topic.destroy();
        }
        topics.clear();
    }

    @Override
    public Set<Member> getMembers() {
        final Set<com.hazelcast.cluster.Member> hzMembers = hz.getCluster().getMembers();
        final Set<Member> set = new HashSet<Member>(hzMembers.size());
        for (final com.hazelcast.cluster.Member hzMember : hzMembers) {
            set.add(new HzMember(hzMember));
        }
        return set;
    }

    @Override
    public MessageInbox getMessageInbox() {
        return messageInbox;
    }

    @Override
    public void directMessage(final Message<?> message, final Member member) {
        com.hazelcast.cluster.Member hzMember = null;
        // Look-up by UUID
        {
            final String uuid = member.getUuid();
            for (final com.hazelcast.cluster.Member cur : hz.getCluster().getMembers()) {
                if (uuid.equals(cur.getUuid().toString())) {
                    hzMember = cur;
                    break;
                }
            }
        }
        if (null == hzMember) {
            // No such member
            return;
        }
        try {
            hz.getExecutorService("default").submitToMember(new MessageAppender(message), hzMember);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Queue<E> getQueue(final String name) {
        Queue<E> queue = (Queue<E>) queues.get(name);
        if (null == queue) {
            try {
                final HzQueue<E> hzQueue = new HzQueue<E>(name, hz);
                queue = (Queue<E>) queues.putIfAbsent(name, hzQueue);
                if (null == queue) {
                    queue = hzQueue;
                }
            } catch (HazelcastInstanceNotActiveException e) {
                throw handleNotActiveException(e);
            }
        }
        return queue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Topic<E> getTopic(final String name) {
        Topic<E> topic = (Topic<E>) topics.get(name);
        if (null == topic) {
            try {
                final HzTopic<E> hzTopic = new HzTopic<E>(name, hz);
                topic = (Topic<E>) topics.putIfAbsent(name, hzTopic);
                if (null == topic) {
                    topic = hzTopic;
                }
            } catch (HazelcastInstanceNotActiveException e) {
                throw handleNotActiveException(e);
            }
        }
        return topic;
    }

}
