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

package com.openexchange.sessiond.impl.container;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * {@link UserRefCounter} - Manages counters for number of long-term sessions per user.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UserRefCounter {

    private final ConcurrentMap<Integer, ConcurrentMap<Integer, Counter>> context2CountersMap;

    /**
     * Initializes a new {@link UserRefCounter}.
     */
    public UserRefCounter() {
        super();
        context2CountersMap = new ConcurrentHashMap<>(256, 0.9F, 1);
    }

    /**
     * Increments the counter for user-associated long-term sessions by one.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void incrementCounter(int userId, int contextId) {
        Integer iContextId = Integer.valueOf(contextId);
        ConcurrentMap<Integer, Counter> user2Counter = context2CountersMap.get(iContextId);
        if (null == user2Counter) {
            ConcurrentMap<Integer, Counter> newUser2Counter = new ConcurrentHashMap<>(16, 0.9F, 1);
            user2Counter = context2CountersMap.putIfAbsent(iContextId, newUser2Counter);
            if (null == user2Counter) {
                user2Counter = newUser2Counter;
            }
        }

        Integer iUserId = Integer.valueOf(userId);
        while (true) {
            Counter counter = user2Counter.get(iUserId);
            if (null == counter) {
                Counter nuCounter = new Counter();
                counter = user2Counter.putIfAbsent(iUserId, nuCounter);
                if (null == counter) {
                    counter = nuCounter;
                }
            }
            synchronized (counter) {
                if (counter.isDeprecated()) {
                    // Marked as deprecated. Retry.
                } else {
                    counter.increment();
                }
            }
        }
    }

    /**
     * Decrements the counter for user-associated long-term sessions by one.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void decrementCounter(int userId, int contextId) {
        ConcurrentMap<Integer, Counter> user2Counter = context2CountersMap.get(Integer.valueOf(contextId));
        if (null == user2Counter) {
            return;
        }

        Integer iUserId = Integer.valueOf(userId);
        while (true) {
            Counter counter = user2Counter.get(iUserId);
            if (null == counter) {
                return;
            }

            synchronized (counter) {
                if (counter.isDeprecated()) {
                    // Marked as deprecated. Retry.
                } else {
                    if (counter.decrementAndGet() <= 0) {
                        // Remove counter since decremented to 0 (zero)
                        counter.markDeprecated();
                        user2Counter.remove(iUserId);
                    }
                }
            }
        }
    }

    /**
     * Checks if there is at least one long-term session for specified context.
     *
     * @param contextId The context identifier
     * @return <code>true</code> if a long-term session is existent for given context; otherwise <code>false</code>
     */
    public boolean contains(int contextId) {
        ConcurrentMap<Integer, Counter> counters = context2CountersMap.get(Integer.valueOf(contextId));
        if (null == counters) {
            return false;
        }

        for (Counter counter : counters.values()) {
            if (counter != null) {
                synchronized (counter) {
                    if (!counter.isDeprecated() && counter.get() > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if there is at least one long-term session for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if a long-term session is existent for given user; otherwise <code>false</code>
     */
    public boolean contains(int userId, int contextId) {
        ConcurrentMap<Integer, Counter> counters = context2CountersMap.get(Integer.valueOf(contextId));
        if (null == counters) {
            return false;
        }

        Counter counter = counters.get(Integer.valueOf(userId));
        if (counter == null) {
            return false;
        }

        synchronized (counter) {
            return !counter.isDeprecated() && counter.get() > 0;
        }
    }

    /**
     * Clears this collection entirely.
     */
    public void clear() {
        context2CountersMap.clear();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class Counter {

        private int count;
        private boolean deprecated;

        /**
         * Initializes a new {@link Counter} having current count set to <code>0</code> (zero).
         */
        Counter() {
            super();
            count = 0;
            deprecated = false;
        }

        int get() {
            return count;
        }

        void increment() {
            count++;
        }

        int decrementAndGet() {
            return --count;
        }

        void markDeprecated() {
            deprecated = true;
        }

        boolean isDeprecated() {
            return deprecated;
        }
    } // End of class Counter

}
