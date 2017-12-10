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

package com.openexchange.sessiond.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * {@link UserRefCounter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UserRefCounter {

    /** A counter wrapping an <code>AtomicInteger</code> instance, but implements hashcode()/equals() for safe removal from <code>ConcurrentMap</code> */
    private static final class Counter {

        private final AtomicInteger count;

        Counter() {
            super();
            count = new AtomicInteger(0);
        }

        int incrementAndGet() {
            return count.incrementAndGet();
        }

        int decrementAndGet() {
            return count.decrementAndGet();
        }

        int get() {
            return count.get();
        }

        @Override
        public int hashCode() {
            int prime = 31;
            return prime * 1 + count.get();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Counter)) {
                return false;
            }
            Counter other = (Counter) obj;
            return count.get() == other.count.get();
        }
    }

    // ------------------------------------------------------------------------------------------

    private final ConcurrentMap<Integer, ConcurrentMap<Integer, Counter>> longTermUserGuardian;

    /**
     * Initializes a new {@link UserRefCounter}.
     */
    public UserRefCounter() {
        super();
        longTermUserGuardian = new ConcurrentHashMap<>(256, 0.9F, 1);
    }

    public void add(final int uid, final int cid) {
        Integer iContextId = Integer.valueOf(cid);
        ConcurrentMap<Integer, Counter> counters = longTermUserGuardian.get(iContextId);
        if (null == counters) {
            ConcurrentMap<Integer, Counter> newCounters = new ConcurrentHashMap<>(16, 0.9F, 1);
            counters = longTermUserGuardian.putIfAbsent(iContextId, newCounters);
            if (null == counters) {
                counters = newCounters;
            }
        }

        Integer iUserId = Integer.valueOf(uid);
        Counter counter = counters.get(iUserId);
        if (null == counter) {
            Counter nuCounter = new Counter();
            counter = counters.putIfAbsent(iUserId, nuCounter);
            if (null == counter) {
                counter = nuCounter;
            }
        }
        counter.incrementAndGet();
    }

    public void remove(final int uid, final int cid) {
        Integer iContextId = Integer.valueOf(cid);
        ConcurrentMap<Integer, Counter> counters = longTermUserGuardian.get(iContextId);
        if (null == counters) {
            return;
        }

        Integer iUserId = Integer.valueOf(uid);
        Counter counter = counters.get(iUserId);
        if (null == counter) {
            return;
        }

        if (counter.decrementAndGet() <= 0) {
            // Try to remove counter atomically (remove if equal to zero-count)
            if (counters.remove(iUserId, new Counter())) {
                // Counter was removed. Check if counter map is empty now.
                if (counters.isEmpty()) {
                    // Try to remove counter map atomically, that is if it is is equal to an empty map
                    longTermUserGuardian.remove(iContextId, new ConcurrentHashMap<>(0, 0.9F, 1));
                }
            }
        }
    }

    public boolean contains(final int cid) {
        return longTermUserGuardian.containsKey(Integer.valueOf(cid));
    }

    public boolean contains(final int uid, final int cid) {
        Integer iContextId = Integer.valueOf(cid);
        ConcurrentMap<Integer, Counter> counters = longTermUserGuardian.get(iContextId);
        if (null == counters) {
            return false;
        }

        Integer iUserId = Integer.valueOf(uid);
        Counter counter = counters.get(iUserId);
        return null != counter && counter.get() > 0;
    }

    public void clear() {
        longTermUserGuardian.clear();
    }

}
