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
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UserRefCounter {

    private final ConcurrentMap<Integer, ConcurrentMap<Integer, AtomicInteger>> longTermUserGuardian;

    /**
     * Initializes a new {@link UserRefCounter}.
     */
    public UserRefCounter() {
        super();
        longTermUserGuardian = new ConcurrentHashMap<>(256, 0.9F, 1);
    }

    public void add(int userId, int contextId) {
        Integer iContextId = Integer.valueOf(contextId);
        ConcurrentMap<Integer, AtomicInteger> counters = longTermUserGuardian.get(iContextId);
        if (null == counters) {
            ConcurrentMap<Integer, AtomicInteger> newCounters = new ConcurrentHashMap<>(16, 0.9F, 1);
            counters = longTermUserGuardian.putIfAbsent(iContextId, newCounters);
            if (null == counters) {
                counters = newCounters;
            }
        }

        Integer iUserId = Integer.valueOf(userId);
        while (true) {
            AtomicInteger counter = counters.get(iUserId);
            if (null == counter) {
                AtomicInteger nuCounter = new AtomicInteger(1);
                counter = counters.putIfAbsent(iUserId, nuCounter);
                if (null == counter) {
                    // This thread was able to put the new counter with initial count of 1
                    return;
                }
            }
            if (counter.getAndIncrement() > 0) {
                return;
            }
            // Became invalid in the meantime: Revert the increment & retry
            counter.decrementAndGet();
        }
    }

    public void remove(int userId, int contextId) {
        ConcurrentMap<Integer, AtomicInteger> counters = longTermUserGuardian.get(Integer.valueOf(contextId));
        if (null == counters) {
            return;
        }

        Integer iUserId = Integer.valueOf(userId);
        AtomicInteger counter = counters.get(iUserId);
        if (null == counter) {
            return;
        }

        if (counter.decrementAndGet() <= 0) {
            // Remove counter
            counters.remove(iUserId);
        }
    }

    public boolean contains(int contextId) {
        ConcurrentMap<Integer, AtomicInteger> counters = longTermUserGuardian.get(Integer.valueOf(contextId));
        if (null == counters) {
            return false;
        }

        for (AtomicInteger counter : counters.values()) {
            if (counter.get() > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(int userId, int contextId) {
        ConcurrentMap<Integer, AtomicInteger> counters = longTermUserGuardian.get(Integer.valueOf(contextId));
        if (null == counters) {
            return false;
        }

        AtomicInteger counter = counters.get(Integer.valueOf(userId));
        return null != counter && counter.get() > 0;
    }

    public void clear() {
        longTermUserGuardian.clear();
    }

}
