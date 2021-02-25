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
import com.openexchange.session.UserAndContext;


/**
 * {@link UserRefCounter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UserRefCounter {

    private final ConcurrentMap<Integer, AtomicInteger> contextCounters;
    private final ConcurrentMap<UserAndContext, AtomicInteger> userCounters;

    /**
     * Initializes a new {@link UserRefCounter}.
     */
    public UserRefCounter() {
        super();
        contextCounters = new ConcurrentHashMap<>(256, 0.9F, 1);
        userCounters = new ConcurrentHashMap<>(1024, 0.9F, 1);
    }

    /**
     * Increments the counter for user-associated long-term sessions by one.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void incrementCounter(int userId, int contextId) {
        incrementCounter(UserAndContext.newInstance(userId, contextId), userCounters);
        incrementCounter(Integer.valueOf(contextId), contextCounters);
    }

    private static <K> void incrementCounter(K key, ConcurrentMap<K, AtomicInteger> counters) {
        AtomicInteger counter = counters.get(key);
        if (counter == null) {
            AtomicInteger nuCounter = new AtomicInteger(0);
            counter = counters.putIfAbsent(key, nuCounter);
            if (counter == null) {
                counter = nuCounter;
            }
        }
        counter.incrementAndGet();
    }

    /**
     * Decrements the counter for user-associated long-term sessions by one.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void decrementCounter(int userId, int contextId) {
        if (decrementCounter(UserAndContext.newInstance(userId, contextId), userCounters)) {
            decrementCounter(Integer.valueOf(contextId), contextCounters);
        }
    }

    private static <K> boolean decrementCounter(K key, ConcurrentMap<K, AtomicInteger> counters) {
        while (true) {
            AtomicInteger counter = counters.get(key);
            if (counter == null) {
                // No such counter
                return false;
            }

            int current = counter.get();
            if (current <= 0) {
                // Current count is already less than or equal to 0 (zero). No negative counting allowed.
                return false;
            }

            // Current count is greater than 0 (zero)
            if (counter.compareAndSet(current, current - 1) == false) {
                // This thread didn't make it to decrement counter --> Retry...
            } else {
                // This thread made it to decrement counter
                if (current == 1) {
                    // This thread set counter to 0 (zero)
                    counters.remove(key);
                }
                return true;
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
        return contains(Integer.valueOf(contextId), contextCounters);
    }

    /**
     * Checks if there is at least one long-term session for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if a long-term session is existent for given user; otherwise <code>false</code>
     */
    public boolean contains(int userId, int contextId) {
        return contains(UserAndContext.newInstance(userId, contextId), userCounters);
    }

    private static <K> boolean contains(K key, ConcurrentMap<K, AtomicInteger> counters) {
        AtomicInteger counter = counters.get(key);
        return counter != null && counter.get() > 0;
    }

    /**
     * Clears this collection entirely.
     */
    public void clear() {
        userCounters.clear();
    }

}
