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

import java.util.concurrent.ConcurrentMap;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.ms.MsService;
import com.openexchange.ms.Queue;
import com.openexchange.ms.Topic;

/**
 * {@link HzMsService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HzMsService implements MsService {

    private final HazelcastInstance hz;
    private final ConcurrentMap<String, Queue<?>> queues;
    private final ConcurrentMap<String, Topic<?>> topics;

    /**
     * Initializes a new {@link HzMsService}.
     */
    public HzMsService(final HazelcastInstance hz) {
        super();
        this.hz = hz;
        queues = new NonBlockingHashMap<String, Queue<?>>(8);
        topics = new NonBlockingHashMap<String, Topic<?>>(16);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Queue<E> getQueue(final String name) {
        Queue<E> queue = (Queue<E>) queues.get(name);
        if (null == queue) {
            final HzQueue<E> hzQueue = new HzQueue<E>(name, hz);
            queue = (Queue<E>) queues.putIfAbsent(name, hzQueue);
            if (null == queue) {
                queue = hzQueue;
            }
        }
        return queue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Topic<E> getTopic(final String name) {
        Topic<E> topic = (Topic<E>) topics.get(name);
        if (null == topic) {
            final HzTopic<E> hzTopic = new HzTopic<E>(name, hz);
            topic = (Topic<E>) topics.putIfAbsent(name, hzTopic);
            if (null == topic) {
                topic = hzTopic;
            }
        }
        return topic;
    }

}
