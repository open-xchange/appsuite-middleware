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
