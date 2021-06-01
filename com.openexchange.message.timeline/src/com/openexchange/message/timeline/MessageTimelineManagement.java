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

package com.openexchange.message.timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.message.timeline.util.Key;
import com.openexchange.session.Session;


/**
 * {@link MessageTimelineManagement}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessageTimelineManagement {

    private static final MessageTimelineManagement INSTANCE = new MessageTimelineManagement();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static MessageTimelineManagement getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------- //

    private final ConcurrentMap<Key, ConcurrentMap<String, BlockingQueue<Message>>> maps;

    /**
     * Initializes a new {@link MessageTimelineManagement}.
     */
    private MessageTimelineManagement() {
        super();
        maps = new ConcurrentHashMap<Key, ConcurrentMap<String,BlockingQueue<Message>>>(1024, 0.9f, 1);
    }

    /**
     * Drops the entries for session-associated user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void dropFor(int userId, int contextId) {
        maps.remove(Key.valueOf(userId, contextId));
    }

    /**
     * Gets the appropriate queue for specified session.
     *
     * @param session The session
     * @param client The client identifier to store for
     * @return The appropriate queue
     */
    public BlockingQueue<Message> getQueueFor(final Session session, final String client) {
        ConcurrentMap<String, BlockingQueue<Message>> queues;
        {
            final Key key = Key.valueOf(session);
            queues = maps.get(key);
            if (null == queues) {
                final ConcurrentMap<String, BlockingQueue<Message>> newQueues = new ConcurrentHashMap<String, BlockingQueue<Message>>(4, 0.9f, 1);
                queues = maps.putIfAbsent(key, newQueues);
                if (null == queues) {
                    queues = newQueues;
                }
            }
        }

        BlockingQueue<Message> queue = queues.get(client);
        if (null == queue) {
            final BlockingQueue<Message> newQueue = new ArrayBlockingQueue<Message>(50);
            queue = queues.putIfAbsent(client, newQueue);
            if (null == queue) {
                queue = newQueue;
            }
        }

        return queue;
    }

    /**
     * Gets the appropriate queues for specified session.
     *
     * @param session The session
     * @return The appropriate queues
     */
    public List<BlockingQueue<Message>> getQueuesFor(final Session session) {
        ConcurrentMap<String, BlockingQueue<Message>> queues;
        {
            final Key key = Key.valueOf(session);
            queues = maps.get(key);
            if (null == queues) {
                final ConcurrentMap<String, BlockingQueue<Message>> newQueues = new ConcurrentHashMap<String, BlockingQueue<Message>>(4, 0.9f, 1);
                queues = maps.putIfAbsent(key, newQueues);
                if (null == queues) {
                    queues = newQueues;
                }
            }
        }

        return new ArrayList<BlockingQueue<Message>>(queues.values());
    }

}
