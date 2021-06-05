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

package com.openexchange.sessionCount;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.SessionThreadCounter;
import com.openexchange.session.ThreadCountEntry;


/**
 * {@link SessionThreadCounterImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionThreadCounterImpl implements SessionThreadCounter {

    private final ConcurrentMap<String, ThreadCountEntry> map;

    private final int notifyThreashold;

    private final ServiceLookup services;

    /**
     * Initializes a new {@link SessionThreadCounterImpl}.
     */
    public SessionThreadCounterImpl(final int notifyThreashold, final ServiceLookup services) {
        super();
        this.services = services;
        this.notifyThreashold = notifyThreashold;
        map = new ConcurrentHashMap<String, ThreadCountEntry>(1024);
    }

    private ThreadCountEntry getCount(final String sessionId) {
        ThreadCountEntry ret = map.get(sessionId);
        if (null == ret) {
            final ThreadCountEntry newCounter = new ThreadCountEntry(sessionId);
            ret = map.putIfAbsent(sessionId, newCounter);
            if (null == ret) {
                ret = newCounter;
            }
        }
        return ret;
    }

    @Override
    public Map<String, Set<Thread>> getThreads(final int threshold) {
        final int limit = threshold >= 0 ? threshold : 0;
        final List<ThreadCountEntry> list = new LinkedList<ThreadCountEntry>();
        for (final Iterator<ThreadCountEntry> iterator = map.values().iterator(); iterator.hasNext();) {
            final ThreadCountEntry threadCountEntry = iterator.next();
            if (threadCountEntry.get() >= limit) {
                list.add(threadCountEntry);
            }
        }
        Collections.sort(list);
        Collections.reverse(list);
        final Map<String, Set<Thread>> map = new LinkedHashMap<String, Set<Thread>>(list.size());
        for (final ThreadCountEntry entry : list) {
            map.put(entry.getSessionId(), entry.getThreads());
        }
        return map;
    }

    @Override
    public int increment(final String sessionId) {
        if (notifyThreashold <= 0) {
            return getCount(sessionId).incrementAndGet();
        }
        final ThreadCountEntry entry = getCount(sessionId);
        final int updated = entry.incrementAndGet();
        if (updated == notifyThreashold) {
            // Count reached threshold
            final EventAdmin eventAdmin = services.getOptionalService(EventAdmin.class);
            if (null != eventAdmin) {
                final Map<String, Object> props = new HashMap<String, Object>(2);
                props.put(EVENT_PROP_SESSION_ID, sessionId);
                props.put(EVENT_PROP_ENTRY, entry);
                final Event event = new Event(EVENT_TOPIC, props);
                eventAdmin.postEvent(event);
            }
        }
        return updated;
    }

    @Override
    public int decrement(final String sessionId) {
        final ThreadCountEntry ret = map.get(sessionId);
        if (null == ret) {
            return 0;
        }
        return ret.decrementAndGet();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public void remove(final String sessionId) {
        map.remove(sessionId);
    }

}
