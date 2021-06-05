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

package com.openexchange.sessiond.impl.container;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.session.UserAndContext;


/**
 * {@link UserRefCounter} - Manages counters for number of long-term sessions per user.
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
