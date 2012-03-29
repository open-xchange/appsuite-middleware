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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.sessionCount;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.session.SessionThreadCounter;
import com.openexchange.session.ThreadCountEntry;


/**
 * {@link SessionThreadCounterImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionThreadCounterImpl implements SessionThreadCounter {

    private final ConcurrentMap<String, ThreadCountEntry> map;

    /**
     * Initializes a new {@link SessionThreadCounterImpl}.
     */
    public SessionThreadCounterImpl() {
        super();
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
        final List<ThreadCountEntry> list = new LinkedList<ThreadCountEntry>();
        for (final Iterator<ThreadCountEntry> iterator = map.values().iterator(); iterator.hasNext();) {
            final ThreadCountEntry threadCountEntry = iterator.next();
            if (threadCountEntry.get() >= threshold) {
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
        return getCount(sessionId).incrementAndGet();
    }

    @Override
    public int decrement(final String sessionId) {
        return getCount(sessionId).decrementAndGet();
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
